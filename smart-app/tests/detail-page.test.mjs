/** 直接执行详情页脚本，覆盖实际审批/执行上下文；不复制页面判断公式。 */
import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { stripTypeScriptTypes } from 'node:module'
import { DEMO_IDENTITIES, createDemoApplications } from '../core/demo.uts'
import { hasPermission } from '../core/catalog.uts'
import { canAct, transitionApplication } from '../core/workflow.uts'

const source = readFileSync(new URL('../pages/detail/detail.uvue', import.meta.url), 'utf8')
const script = source.match(/<script setup lang="uts">([\s\S]*?)<\/script>/)[1]
const executable = stripTypeScriptTypes(script.replace(/^import[^\n]*\n/gm, ''), { mode: 'strip' })

/** 桩只承载响应式读取和生命周期，业务判断与动作仍执行真实页面及领域函数。 */
function mountDetail(view = '', identityOverride = null) {
  const identity = identityOverride ?? structuredClone(DEMO_IDENTITIES.find(item => item.subjectId === 'demo-supervisor-001'))
  if (identityOverride == null) identity.permissions = ['item-pass:approve', 'item-pass:execute']
  const pending = createDemoApplications()[0]
  const state = { identity, mode: 'demo', postId: '', applications: [pending], loading: false, error: '' }
  const lifecycle = {}
  const dependencies = {
    ref: value => ({ value }),
    computed: read => ({ get value() { return read() } }),
    watch: () => {},
    onLoad: callback => { lifecycle.load = callback },
    onShow: callback => { lifecycle.show = callback },
    onHide: callback => { lifecycle.hide = callback },
    onUnload: callback => { lifecycle.unload = callback },
    clientState: state,
    guard: permission => !permission || hasPermission(identity, permission),
    loadApplications: async () => {},
    loadExecutionApplications: async () => {},
    availablePosts: () => identity.posts,
    hasExecutionPost: () => state.postId.length > 0,
    postName: () => state.postId,
    actOnApplication: async (id, action, comment, execution) => {
      const updated = transitionApplication(pending, identity, state.postId, action, comment, execution)
      // 办理结束后单据移出该权限对应的列表，页面仍应保留成功响应快照。
      state.applications = []
      return updated
    },
    errorMessage: issue => issue.message,
    recordDemoEscortSwipe: () => '',
    clearEscortVerification: () => {},
    hasPermission,
    canAct,
    uni: { showToast: () => {} },
  }
  const createPage = new Function(...Object.keys(dependencies), executable + '\nreturn {actions, blocked, executionContext, item, postLabel, act};')
  const page = createPage(...Object.values(dependencies))
  lifecycle.load({ id: pending.id, view })
  lifecycle.show()
  return { page, state }
}

test('双权限身份无需岗位审批，同意后仍显示审批结果且不自动进入安检', async () => {
  const { page } = mountDetail()
  assert.equal(page.blocked.value, false)
  assert.ok(page.actions.value.some(action => action.id === 'approve'))
  await page.act('approve')
  assert.equal(page.executionContext.value, false, '审批意图不能随已批准状态切换成执行意图')
  assert.equal(page.blocked.value, false)
  assert.equal(page.item.value.status, 'approved')
  assert.equal(page.item.value.fromPostName, '演示园区东门')
  assert.equal(page.item.value.toPostName, '演示园区西门')
  assert.equal(page.actions.value.some(action => ['depart', 'arrive'].includes(action.id)), false)
})

test('同一双权限身份显式打开执行详情仍要求安检岗位', () => {
  const { page } = mountDetail('execute')
  assert.equal(page.executionContext.value, true)
  assert.equal(page.blocked.value, true)
  assert.equal(page.item.value, null)
  assert.deepEqual(page.actions.value, [])
})

test('安检身份通过记录入口只读查看时，不因安检权限被要求选岗', () => {
  const identity = structuredClone(DEMO_IDENTITIES.find(item => item.subjectId === 'demo-security-001'))
  const { page } = mountDetail('read', identity)
  assert.equal(page.executionContext.value, false)
  assert.equal(page.blocked.value, false)
  assert.ok(page.item.value != null)
  assert.equal(page.actions.value.some(action => ['depart', 'arrive'].includes(action.id)), false)
})

test('双权限身份通过只读记录入口查看时不提供审批或执行动作', () => {
  const { page } = mountDetail('read')
  assert.equal(page.blocked.value, false)
  assert.ok(page.item.value != null)
  assert.deepEqual(page.actions.value, [])
})

test('无执行岗位的申请人与主管详情优先显示单据点位名称快照', () => {
  for (const subjectId of ['demo-employee-001', 'demo-supervisor-001']) {
    const identity = structuredClone(DEMO_IDENTITIES.find(item => item.subjectId === subjectId))
    assert.deepEqual(identity.posts, [])
    const { page } = mountDetail('read', identity)
    assert.equal(
      page.postLabel(page.item.value.fromPostId, page.item.value.fromPostName),
      '演示园区东门',
    )
    assert.equal(
      page.postLabel(page.item.value.toPostId, page.item.value.toPostName),
      '演示园区西门',
    )
  }
})

test('详情名称快照优先于当前目录，旧单缺省时依次回退目录和岗位ID', () => {
  const security = structuredClone(DEMO_IDENTITIES.find(item => item.subjectId === 'demo-security-001'))
  const { page: securityPage } = mountDetail('read', security)
  assert.equal(securityPage.postLabel('east-gate', '历史东门'), '历史东门')
  assert.equal(securityPage.postLabel('east-gate', undefined), '演示园区东门')

  const employee = structuredClone(DEMO_IDENTITIES.find(item => item.subjectId === 'demo-employee-001'))
  const { page: employeePage } = mountDetail('read', employee)
  assert.equal(employeePage.postLabel('legacy-post', undefined), 'legacy-post')
})
