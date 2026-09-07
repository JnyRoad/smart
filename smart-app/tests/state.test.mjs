/** 用内存平台替身验证协调层的身份隔离与用户操作，不调用真实网络。 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { registerHooks } from 'node:module'
registerHooks({
  /** 领域协调测试只需透传响应对象，不以浏览器响应式机制为验证目标。 */
  resolve(name, context, next) {
    if (name === 'vue') return {url:'data:text/javascript,export const reactive = value => value; export const nextTick = callback => Promise.resolve().then(callback)',shortCircuit:true}
    return next(name, context)
  }
})
const storage = new Map()
let requests = []
let responseStatus = 200
let responseBody = []
globalThis.uni = {
  getStorageSync: key => storage.get(key),
  setStorageSync: (key, value) => storage.set(key, value),
  switchTab: () => {}, reLaunch: () => {}, navigateTo: () => {}, showToast: () => {},
  request: options => {
    requests.push(options)
		const body = options.url.endsWith('/api/v1/sessions')
			? { token: responseBody.token, expiresAt: responseBody.expiresAt }
			: options.url.endsWith('/api/v1/me')
				? responseBody.identity
				: responseBody
    options.success({statusCode:responseStatus,data:body})
  }
}
const { runtimeConfig } = await import('../config/runtime.uts')
runtimeConfig.apiBaseUrl = 'https://example.invalid'
const state = await import('../state/session.uts')
const { createDemoApplications } = await import('../core/demo.uts')
const executionPostFixtures = state.DEMO_IDENTITIES.find(
  identity => identity.subjectId === 'demo-security-001',
).posts.map(post => ({ ...post }))

const liveVerification = {
  id: 'live-verification-1', badgeId: 'LIVE-BADGE-001', visitorName: '实时访客', supplierName: '实时单位',
  admissionId: 'live-admission-1', postId: 'east-gate', areaName: '东门保密区',
  validFrom: '2000-01-01T00:00:00.000Z', validUntil: '2999-09-06T00:00:00.000Z', expiresAt: '2999-09-05T00:00:00.000Z',
  allowed: true, reason: '', presence: 'outside', allowedDirections: ['enter', 'leave'],
}

/** 创建可被纯内存接口接受的正式会话，权限由测试输入明确指定。 */
async function signIn(permissions) {
  responseStatus = 200
  responseBody = {
    token:'test-token',
    expiresAt:Date.now()+60000,
    identity:{
      ...state.DEMO_IDENTITIES[0],
      subjectId:'test-live',
      permissions,
      posts: permissions.some(permission => permission.endsWith(':execute'))
        ? executionPostFixtures.map(post => ({ ...post }))
        : [],
    },
  }
  await state.login('0001','test-password')
}

test('删除全部默认常用后保持为空，切账号和重新进入不恢复默认项', () => {
  state.enterDemo('demo-employee-001')
  const initial = state.favorites().map(item => item.id)
  for (const id of initial) state.toggleFavorite(id)
  assert.equal(state.favorites().length,0)
  state.logout()
  state.enterDemo('demo-outsourced-001')
  assert.ok(state.favorites().length > 0)
  state.enterDemo('demo-employee-001')
  assert.equal(state.favorites().length,0)
})
test('审批权限无需另发全量记录权限即可处理当前审批待办', async () => {
  await signIn(['item-pass:approve'])
  responseBody = createDemoApplications()
  await state.loadApplications()
  assert.equal(state.todos().filter(item=>item.kind==='item-pass').length,1)
})
test('供应商旧申请在提交前直接拒绝且不发送请求', async () => {
  state.enterDemo('demo-employee-001')
  requests = []
  await assert.rejects(()=>state.submitApplication({kind:'supplier',title:'测试',reason:'核验',fromPostId:'',toPostId:'unauthorized',supplierName:'测试单位',visitorName:'测试人员',materials:'',seals:[]}), /供应商|厂牌/)
  assert.equal(requests.length, 0)
})
test('供应商核验401清空当前会话且不自动回退演示', async () => {
  await signIn(['supplier:execute'])
  state.selectPost('east-gate')
  responseBody = liveVerification
  await state.verifySupplierBadge('LIVE-BADGE-001')
  responseStatus = 401
  requests = []
  await state.recordSupplierPassage('enter')
  assert.equal(state.clientState.identity,null)
  assert.equal(requests.length,1)
})

test('较早列表响应不得覆盖较晚刷新，403后清除旧单据', async () => {
  await signIn(['item-pass:read'])
  const original = uni.request
  const pending = []
  uni.request = options => pending.push(options)
  try {
    const first = state.loadApplications()
    const second = state.loadApplications()
    pending[1].success({statusCode:200,data:createDemoApplications().slice(0,1).map(item=>({...item,title:'较新记录'}))})
    await second
    pending[0].success({statusCode:200,data:createDemoApplications()})
    await first
    assert.equal(state.clientState.applications[0].title,'较新记录')
  } finally { uni.request = original }
  responseStatus = 403
  await state.loadApplications()
  assert.equal(state.clientState.applications.length,0)
})
test('真实驳回空白原因在发请求前拒绝', async () => {
  await signIn(['item-pass:approve','item-pass:read'])
  responseBody = createDemoApplications()
  await state.loadApplications()
  requests = []
  await assert.rejects(()=>state.actOnApplication('demo-release-pending','reject','  '), /原因/)
  assert.equal(requests.length,0)
})
test('供应商事件网络结果未知时同一方向重试复用幂等键', async () => {
  await signIn(['supplier:execute'])
  state.selectPost('east-gate')
  responseBody = liveVerification
  await state.verifySupplierBadge('LIVE-BADGE-001')
  const original = uni.request
  const keys = []
  uni.request = options => { keys.push(options.header['Idempotency-Key']); options.fail({}) }
  try {
    assert.equal(await state.recordSupplierPassage('enter'), null)
    assert.equal(await state.recordSupplierPassage('enter'), null)
    assert.equal(keys.length,2)
    assert.equal(keys[0],keys[1])
  } finally { uni.request = original }
})

test('同一厂牌登记并发失败时两个调用都按空结果收敛', async () => {
  await signIn(['supplier:execute'])
  state.selectPost('east-gate')
  responseBody = liveVerification
  await state.verifySupplierBadge('LIVE-BADGE-001')
  const original = uni.request
  let pendingWrite
  uni.request = options => { pendingWrite = options }
  try {
    const first = state.recordSupplierPassage('enter')
    const second = state.recordSupplierPassage('enter')
    assert.ok(pendingWrite)
    pendingWrite.fail({errMsg:'request:fail timeout'})
    assert.deepEqual(await Promise.all([first, second]), [null, null])
    assert.match(state.supplierState.error, /网络连接失败/)
  } finally { uni.request = original }
})

test('供应商真实核验服务故障不自动切换演示数据', async () => {
  await signIn(['supplier:execute'])
  state.selectPost('east-gate')
  const original = uni.request
  requests = []
  uni.request = options => { requests.push(options); options.success({statusCode:503,data:{}}) }
  try {
    await state.verifySupplierBadge('DEMO-BADGE-001')
    assert.equal(state.supplierState.verification, null)
    assert.match(state.supplierState.error,/服务暂时不可用/)
    assert.equal(requests.length,1)
  } finally {uni.request=original}
})
test('状态操作结果未知时人工重试复用原幂等键', async () => {
  await signIn(['item-pass:approve','item-pass:read'])
  responseBody=createDemoApplications()
  await state.loadApplications()
  const original=uni.request
  const keys=[]
  uni.request=options=>{keys.push(options.header['Idempotency-Key']);options.fail({})}
  try {
    await assert.rejects(()=>state.actOnApplication('demo-release-pending','approve','同意'),/网络连接失败/)
    await assert.rejects(()=>state.actOnApplication('demo-release-pending','approve','同意'),/网络连接失败/)
    assert.equal(keys.length,2)
    assert.equal(keys[0],keys[1])
  } finally {uni.request=original}
})

test('会话在状态操作前失效时必须返回错误，不能给页面假成功', async () => {
  state.enterDemo('demo-supervisor-001')
  responseBody = createDemoApplications()
  await state.loadApplications()
  state.logout()
  await assert.rejects(() => state.actOnApplication('demo-release-pending', 'approve', ''), /登录|会话/)
})

test('并发厂牌核验只接收当前请求，身份切换立即清除旧核验', async () => {
  await signIn(['supplier:execute'])
  state.selectPost('east-gate')
  const original = uni.request
  const pending = []
  uni.request = options => pending.push(options)
  try {
    const first = state.verifySupplierBadge('LIVE-BADGE-FIRST')
    const second = state.verifySupplierBadge('LIVE-BADGE-SECOND')
    assert.equal(pending.length, 2)
    pending[1].success({statusCode:200,data:{...liveVerification,id:'verification-second',badgeId:'LIVE-BADGE-SECOND'}})
    await second
    pending[0].success({statusCode:200,data:{...liveVerification,id:'verification-first',badgeId:'LIVE-BADGE-FIRST'}})
    await first
    assert.equal(state.supplierState.verification.id, 'verification-second')
    state.enterDemo('demo-supervisor-001')
    assert.equal(state.supplierState.verification, null)
  } finally { uni.request = original }
})

test('事件响应厂牌不匹配时不接纳，修复后沿用原幂等键重试', async () => {
  await signIn(['supplier:execute'])
  state.selectPost('east-gate')
  responseBody = liveVerification
  await state.verifySupplierBadge('LIVE-BADGE-001')
  const original = uni.request
  const keys = []
  let attempt = 0
  const validEvent = {
    id:'live-event-1', verificationId:'live-verification-1', badgeId:'LIVE-BADGE-001', visitorName:'实时访客', supplierName:'实时单位',
    admissionId:'live-admission-1', postId:'east-gate', areaName:'东门保密区', direction:'enter', operatorName:'测试人员', occurredAt:'2026-09-05T00:10:00.000Z',
  }
  uni.request = options => {
    keys.push(options.header['Idempotency-Key'])
    attempt += 1
    options.success({statusCode:200,data:attempt == 1 ? {...validEvent,badgeId:'OTHER-BADGE'} : validEvent})
  }
  try {
    assert.equal(await state.recordSupplierPassage('enter'), null)
    assert.equal(await state.recordSupplierPassage('enter').then(value => value == null ? null : value.id), 'live-event-1')
    assert.equal(keys.length, 2)
    assert.equal(keys[0], keys[1])
    assert.equal(state.supplierState.events.filter(event => event.id == 'live-event-1').length, 1)
  } finally { uni.request = original }
})

test('每次厂牌登记后必须重新扫描，同一方向也产生独立事件', async () => {
  await signIn(['supplier:execute'])
  state.selectPost('east-gate')
  const original = uni.request
  const verification = {...liveVerification, id:'fixed-verification', badgeId:'FIXED-BADGE-001'}
  const eventIds = ['fixed-enter-1', 'fixed-leave-1', 'fixed-enter-2']
  let verificationCount = 0
  let eventCount = 0
  const makeEvent = (id, direction) => ({
    id, verificationId:verification.id, badgeId:verification.badgeId, visitorName:verification.visitorName,
    supplierName:verification.supplierName, admissionId:verification.admissionId, postId:verification.postId,
    areaName:verification.areaName, direction, operatorName:'测试人员', occurredAt:'2026-09-05T00:10:00.000Z',
  })
  uni.request = options => {
    if (options.url.includes('/visitor-checks')) {
      verificationCount += 1
      options.success({statusCode:200,data:{...verification,presence:verificationCount == 2 ? 'inside' : 'outside'}})
      return
    }
    if (options.url.includes('/visitor-passes')) {
      const direction = options.data.direction
      options.success({statusCode:200,data:makeEvent(eventIds[eventCount], direction)})
      eventCount += 1
      return
    }
    throw new Error('意外的供应商请求')
  }
  try {
    await state.verifySupplierBadge('FIXED-BADGE-001')
    const first = await state.recordSupplierPassage('enter')
    assert.ok(first)
    assert.equal(state.supplierState.verification, null)

    await state.verifySupplierBadge('FIXED-BADGE-001')
    const second = await state.recordSupplierPassage('leave')
    assert.ok(second)
    assert.equal(state.supplierState.verification, null)

    await state.verifySupplierBadge('FIXED-BADGE-001')
    const third = await state.recordSupplierPassage('enter')
    assert.ok(third)
    assert.notEqual(third.id, first.id)
    assert.equal(verificationCount, 3)
    assert.equal(eventCount, 3)
  } finally { uni.request = original }
})

test('清除厂牌核验后，挂起写入的迟到响应不得恢复核验或本地事件', async () => {
  await signIn(['supplier:execute'])
  state.selectPost('east-gate')
  const original = uni.request
  const pendingEvents = []
  const verification = {...liveVerification, id:'clear-verification', badgeId:'CLEAR-BADGE-001'}
  const event = {
    id:'cleared-event-1', verificationId:verification.id, badgeId:verification.badgeId,
    visitorName:verification.visitorName, supplierName:verification.supplierName,
    admissionId:verification.admissionId, postId:verification.postId, areaName:verification.areaName,
    direction:'enter', operatorName:'测试人员', occurredAt:'2026-09-05T00:11:00.000Z',
  }
  uni.request = options => {
    if (options.url.includes('/visitor-checks')) {
      options.success({statusCode:200,data:verification})
      return
    }
    if (options.url.includes('/visitor-passes')) {
      pendingEvents.push(options)
      return
    }
    throw new Error('意外的供应商请求')
  }
  try {
    await state.verifySupplierBadge('CLEAR-BADGE-001')
    const write = state.recordSupplierPassage('enter')
    assert.equal(pendingEvents.length, 1)
    state.clearSupplierVerification()
    pendingEvents[0].success({statusCode:200,data:event})
    assert.equal(await write, null)
    assert.equal(state.supplierState.verification, null)
    assert.equal(state.supplierState.events.some(item => item.id == event.id), false)
  } finally { uni.request = original }
})

test('新核验覆盖挂起写入后，旧事件响应不得污染新核验上下文', async () => {
  await signIn(['supplier:execute'])
  state.selectPost('east-gate')
  const original = uni.request
  const pendingEvents = []
  const oldVerification = {...liveVerification, id:'old-verification', badgeId:'OLD-BADGE-001'}
  const newVerification = {...liveVerification, id:'new-verification', badgeId:'NEW-BADGE-001'}
  const oldEvent = {
    id:'old-context-event-1', verificationId:oldVerification.id, badgeId:oldVerification.badgeId,
    visitorName:oldVerification.visitorName, supplierName:oldVerification.supplierName,
    admissionId:oldVerification.admissionId, postId:oldVerification.postId, areaName:oldVerification.areaName,
    direction:'enter', operatorName:'测试人员', occurredAt:'2026-09-05T00:12:00.000Z',
  }
  let verificationCount = 0
  uni.request = options => {
    if (options.url.includes('/visitor-checks')) {
      verificationCount += 1
      options.success({statusCode:200,data:verificationCount == 1 ? oldVerification : newVerification})
      return
    }
    if (options.url.includes('/visitor-passes')) {
      pendingEvents.push(options)
      return
    }
    throw new Error('意外的供应商请求')
  }
  try {
    await state.verifySupplierBadge('OLD-BADGE-001')
    const write = state.recordSupplierPassage('enter')
    assert.equal(pendingEvents.length, 1)
    await state.verifySupplierBadge('NEW-BADGE-001')
    assert.equal(state.supplierState.verification.id, newVerification.id)
    pendingEvents[0].success({statusCode:200,data:oldEvent})
    assert.equal(await write, null)
    assert.equal(state.supplierState.verification.id, newVerification.id)
    assert.equal(state.supplierState.events.some(item => item.id == oldEvent.id), false)
  } finally { uni.request = original }
})

test('写入成功后，迟到的旧事件列表响应不得覆盖新增事件', async () => {
  await signIn(['supplier:execute', 'supplier:read'])
  state.selectPost('east-gate')
  const original = uni.request
  const pendingLists = []
  const verification = {...liveVerification, id:'list-race-verification', badgeId:'LIST-RACE-BADGE-001'}
  const event = {
    id:'list-race-event-1', verificationId:verification.id, badgeId:verification.badgeId,
    visitorName:verification.visitorName, supplierName:verification.supplierName,
    admissionId:verification.admissionId, postId:verification.postId, areaName:verification.areaName,
    direction:'enter', operatorName:'测试人员', occurredAt:'2026-09-05T00:13:00.000Z',
  }
  uni.request = options => {
    if (options.url.includes('/visitor-checks')) {
      options.success({statusCode:200,data:verification})
      return
    }
    if (options.url.includes('/visitor-passes') && options.method == 'GET') {
      pendingLists.push(options)
      return
    }
    if (options.url.includes('/visitor-passes')) {
      options.success({statusCode:200,data:event})
      return
    }
    throw new Error('意外的供应商请求')
  }
  try {
    await state.verifySupplierBadge('LIST-RACE-BADGE-001')
    const oldLoad = state.loadSupplierPassages()
    assert.equal(pendingLists.length, 1)
    const written = await state.recordSupplierPassage('enter')
    assert.equal(written.id, event.id)
    assert.equal(state.supplierState.events.some(item => item.id == event.id), true)
    pendingLists[0].success({statusCode:200,data:[]})
    await oldLoad
    assert.equal(state.supplierState.events.some(item => item.id == event.id), true)
  } finally { uni.request = original }
})

test('旧登记的迟到错误不得覆盖新厂牌的核验反馈', async () => {
  await signIn(['supplier:execute'])
  state.selectPost('east-gate')
  const original = uni.request
  let pendingWrite
  uni.request = options => {
    if (options.url.includes('/visitor-checks')) {
      options.success({statusCode:200, data:{...liveVerification, id:options.data.credentialCode}})
    } else pendingWrite = options
  }
  try {
    await state.verifySupplierBadge('OLD-CONTEXT')
    const writing = state.recordSupplierPassage('enter')
    await state.verifySupplierBadge('NEW-CONTEXT')
    pendingWrite.fail({errMsg:'request:fail timeout'})
    assert.equal(await writing, null)
    assert.equal(state.supplierState.verification.id, 'NEW-CONTEXT')
    assert.equal(state.supplierState.error, '')
  } finally { uni.request = original }
})


test('页面守卫立即拒绝业务，但等待当前页面渲染结束后再重定向', async () => {
  state.enterDemo('demo-employee-001')
  const original = uni.switchTab
  const destinations = []
  uni.switchTab = options => destinations.push(options.url)
  try {
    assert.equal(state.guard('item-pass:approve'), false)
    assert.deepEqual(destinations, [])
    await Promise.resolve()
    assert.deepEqual(destinations, ['/pages/workbench/workbench'])
  } finally { uni.switchTab = original }
})

test('页面守卫的延迟跳转不能影响随后切换的新会话', async () => {
  state.enterDemo('demo-employee-001')
  const original = uni.switchTab
  const destinations = []
  uni.switchTab = options => destinations.push(options.url)
  try {
    assert.equal(state.guard('item-pass:approve'), false)
    state.enterDemo('demo-security-001')
    destinations.length = 0
    await Promise.resolve()
    assert.deepEqual(destinations, [])
  } finally { uni.switchTab = original }
})


test('保存的扫码方式跨退出与启动恢复，且不恢复身份或执行岗位', () => {
  state.enterDemo('demo-security-001')
  state.selectPost('east-gate')
  state.setScanMode('hardware')
  assert.equal(storage.get('smart-app.scan-mode'), 'hardware')
  state.logout()
  state.clientState.scanMode = 'camera'
  state.restoreDevicePreferences()
  assert.equal(state.clientState.scanMode, 'hardware')
  assert.equal(state.clientState.identity, null)
  assert.equal(state.clientState.postId, '')
})

test('扫码设置写盘失败或输入非法时保留原有生效配置', () => {
  state.setScanMode('hardware')
  assert.throws(() => state.setScanMode('automatic'), /不支持/)
  const original = uni.setStorageSync
  uni.setStorageSync = () => { throw new Error('存储不可用') }
  try {
    assert.throws(() => state.setScanMode('camera'), /存储不可用/)
    assert.equal(state.clientState.scanMode, 'hardware')
    assert.equal(storage.get('smart-app.scan-mode'), 'hardware')
  } finally { uni.setStorageSync = original }
})

test('申请人从独立候选填写申请岗位，但不能绑定安检执行岗位', async () => {
  state.enterDemo('demo-employee-001')
  await state.loadApplicationOptions()
  assert.ok(state.applicationPosts().length > 0)
  assert.equal(state.availablePosts().length, 0)
  assert.equal(state.executionPosts().length, 0)
  assert.equal(state.guardExecutionPost(), false)
  assert.throws(() => state.selectPost('east-gate'), /安检|执行权限/)
  assert.equal(state.clientState.postId, '')
})

test('执行岗位按权限而非正式或外包派遣来源授权，换人后清空岗位', async () => {
  for (const employmentType of ['employee','outsourced','dispatched']) {
    responseStatus = 200
    responseBody = {token:'test-token',expiresAt:Date.now()+60000,identity:{...state.DEMO_IDENTITIES[0],subjectId:'test-'+employmentType,employmentType,permissions:['supplier:execute'],posts:executionPostFixtures.map(post=>({...post}))}}
    await state.login('0001','test-password')
    assert.ok(state.executionPosts().length > 0)
    assert.equal(state.guardExecutionPost(), true)
    state.selectPost('east-gate')
    assert.equal(state.clientState.postId, 'east-gate')
    state.enterDemo('demo-employee-001')
    assert.equal(state.clientState.postId, '')
  }
})
