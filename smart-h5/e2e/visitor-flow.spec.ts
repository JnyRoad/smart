import { expect, test, type Page, type Request } from '@playwright/test'

const WECHAT_OAUTH = 'https://open.weixin.qq.com/connect/oauth2/authorize*'
const LEGACY_VISITOR_ENDPOINTS = [
  '/admittance/apply/app/searchReceptionist',
  '/admittance/apply/enum/cause',
  '/admittance/apply/app/area-options',
  '/admittance/apply/save/apply',
]

/** 正常访客链路的草稿凭证必须随受保护入口传递。 */
function expectVisitorDraftHeaders(request: Request, requireCapability = false) {
  const headers = request.headers()
  expect(headers['x-visitor-draft-token']).toBe('draft-token')
  expect(headers['x-visitor-draft-id']).toBe('draft-id')
  if (requireCapability) expect(headers['x-visitor-action-capability']).toBeTruthy()
}

/** capability 只能由当前草稿换取，测试不得用旧匿名路由掩盖这一约束。 */
function expectCapabilityIssueRequest(request: Request, action?: string | string[]) {
  expect(request.headers()['x-visitor-draft-token']).toBe('draft-token')
  const body = request.postDataJSON() as Record<string, unknown>
  expect(body).toMatchObject({ draftId: 'draft-id' })
  if (Array.isArray(action)) {
    expect(action).toContain(body.action)
  } else if (action) {
    expect(body.action).toBe(action)
  }
}

/** 成功主链必须不再向历史裸匿名访客接口发出请求。 */
function observeNoLegacyVisitorRequests(page: Page) {
  const legacyRequests: string[] = []
  page.on('request', (request) => {
    const pathname = new URL(request.url()).pathname
    if (LEGACY_VISITOR_ENDPOINTS.includes(pathname)) legacyRequests.push(pathname)
  })
  return () => expect(legacyRequests).toEqual([])
}

async function stubWechatOAuth(page: Page) {
  await page.route(WECHAT_OAUTH, (route) =>
    route.fulfill({ contentType: 'text/html', body: '<title>wechat-oauth-stub</title>' }),
  )
}

/** Default mocks for the /visitor entry page. */
async function mockEntryApis(page: Page, { needNotice = 0 } = {}) {
  await page.route('**/platform/common/config/admittance/notice*', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: { isNeedNotice: needNotice, content: '<p>入园请佩戴口罩，凭码通行。</p>' },
      },
    }),
  )
  await page.route('**/platform/admittance/apply/get/openId*', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: { visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id' },
      },
    }),
  )
}

test('入口无 code：跳微信 OAuth 且回跳 /visitor', async ({ page }) => {
  await stubWechatOAuth(page)
  await page.goto('/visitor')
  await page.waitForURL(WECHAT_OAUTH)
  const url = new URL(page.url())
  expect(decodeURIComponent(url.searchParams.get('redirect_uri') ?? '')).toContain('/visitor')
  expect(url.searchParams.get('scope')).toBe('snsapi_base')
})

test('入口：温馨提示弹窗 + 被访人查询成功跳 info', async ({ page }) => {
  const expectNoLegacyRequests = observeNoLegacyVisitorRequests(page)
  await mockEntryApis(page, { needNotice: 1 })
  await page.route('**/platform/admittance/visitor-action/capability', async (route) => {
    expectCapabilityIssueRequest(route.request(), 'RECEPTIONIST_SEARCH')
    await route.fulfill({ json: { code: 0, data: { capability: 'receptionist-capability' } } })
  })
  await page.route('**/platform/admittance/visitor-entry/receptionist', async (route) => {
    const headers = route.request().headers()
    // 接待人查询由 capability 绑定草稿；真实请求只传 draftId，草稿 token 仅用于换票据。
    expect(headers['x-visitor-draft-id']).toBe('draft-id')
    expect(headers['x-visitor-action-capability']).toBe('receptionist-capability')
    expect(route.request().postDataJSON()).toEqual({
      parkId: 5000021,
      receptionistName: '赵经理',
      receptionistPhone: '13800001111',
    })
    await route.fulfill({
      json: {
        code: 0,
        data: { receptionistBadge: 'YT001', receptionistName: '赵经理', receptionistPhone: '13800001111' },
      },
    })
  })

  await page.goto('/visitor?code=visitor-code')
  await expect(page.getByText('入园请佩戴口罩')).toBeVisible()
  await page.getByRole('button', { name: '知道了' }).click()

  await page.getByPlaceholder('请输入被访人姓名').fill('赵经理')
  await page.getByPlaceholder('请输入被访人手机号').fill('13800001111')
  await page.getByRole('button', { name: '下一步' }).click()
  await page.waitForURL('**/visitor/info')
  expectNoLegacyRequests()
})

// Mirrors the real /area-options shape: data is an object; factoryType is the
// backend value ("15"/"16"); areas use areaCode (number) / areaName.
const AREA_CONFIG = {
  parkId: 5000021,
  inlineAreaLimit: 8,
  factories: [
    {
      factoryType: '15',
      factoryName: '新工厂',
      areaFlag: 1,
      sort: 1,
      areas: [
        { areaCode: 3, areaName: '办公区', isCommon: false, sort: 1 },
        { areaCode: 5, areaName: '生产一区', isCommon: false, sort: 2 },
        { areaCode: 6, areaName: '仓储区', isCommon: false, sort: 3 },
      ],
    },
    {
      factoryType: '16',
      factoryName: '老工厂',
      areaFlag: 0,
      sort: 2,
      areas: [{ areaCode: 4, areaName: '老办公区', isCommon: false, sort: 1 }],
    },
  ],
}

/** Mocks for the /visitor/info page (enums, areas, face upload). */
async function mockInfoApis(page: Page) {
  await page.route('**/platform/admittance/visitor-entry/options/cause', (route) => {
    expectVisitorDraftHeaders(route.request())
    return route.fulfill({ json: { code: 0, data: [{ code: 1, desc: '商务洽谈' }, { code: 2, desc: '参观访问' }] } })
  })
  await page.route('**/platform/admittance/visitor-entry/options/area-options*', (route) => {
    expectVisitorDraftHeaders(route.request())
    return route.fulfill({ json: { code: 0, data: AREA_CONFIG } })
  })
  await page.route('**/platform/admittance/visitor-face/capability', (route) => {
    expect(route.request().headers()['x-visitor-draft-token']).toBe('draft-token')
    expect(route.request().postDataJSON()).toEqual({ draftId: 'draft-id' })
    return route.fulfill({ json: { code: 0, data: { capability: 'one-time-capability' } } })
  })
  await page.route('**/platform/admittance/visitor-face/crop', (route) => {
    expect(route.request().headers()['x-visitor-face-capability']).toBe('one-time-capability')
    return route.fulfill({ json: { code: 0, message: 'success', data: { imageData: 'cut-base64', uploadCapability: 'face-upload-capability' } } })
  })
  await page.route('**/platform/admittance/visitor-action/capability', (route) => {
    expectCapabilityIssueRequest(route.request(), 'APPLY_PRECHECK')
    return route.fulfill({ json: { code: 0, data: { capability: 'visitor-action-capability' } } })
  })
  await page.route('**/app/wechat/visit/checkFace', (route) => {
    const headers = route.request().headers()
    expect(headers['x-visitor-action-capability']).toBe('face-upload-capability')
    expect(headers['x-visitor-draft-id']).toBe('draft-id')
    return route.fulfill({ json: { code: 0, message: 'success', data: { photoId: 'photo-001' } } })
  })
}

/** Walks the info form to a fully valid state (without times). */
async function fillInfoForm(page: Page) {
  await page.getByPlaceholder('请输入访客姓名').fill('王五')
  await page.setInputFiles('[data-testid=face-upload-input]', {
    name: 'face.png',
    mimeType: 'image/png',
    buffer: Buffer.from('89504e470d0a1a0a', 'hex'),
  })
  await expect(page.locator('[data-testid=face-upload-button] img')).toBeVisible()
  await page.getByPlaceholder('请输入身份证号码').fill('11010519491231002X')
  await page.getByPlaceholder('请输入来访单位').fill('测试公司')
  await page.getByText('请选择来访事由').click()
  await page.getByText('商务洽谈').last().click()
  await page.getByRole('button', { name: '确定' }).click()
  await page.getByRole('button', { name: /新工厂/ }).click()
  await page.getByRole('button', { name: '办公区', exact: true }).click()
}

test('访客信息页：填写校验与 capability 预校验通过跳 tel', async ({ page }) => {
  const expectNoLegacyRequests = observeNoLegacyVisitorRequests(page)
  await mockInfoApis(page)
  await page.route('**/platform/admittance/visitor-entry/precheck', async (route) => {
    expectVisitorDraftHeaders(route.request(), true)
    const body = route.request().postDataJSON() as Record<string, unknown>
    // 旧版请求体（visitorInfo.vue:304-339）：fellowList（非 visitorList）+ 顶层实名/区域字段，不带 parkId。
    expect(body.parkId).toBeUndefined()
    expect(body.receptionistBadge).toBe('YT001')
    expect(body.visitorName).toBe('王五')
    expect(body.visitorPhotoId).toBe('photo-001')
    expect(body.company).toBe('测试公司')
    expect(body.cause).toBe(1)
    expect(body.thing).toBe(4)
    expect(body.permitFactoryType).toBe('15')
    expect(body.permitArea).toBe('')
    expect(body.areaType).toEqual(['3'])
    const fellowList = body.fellowList as Record<string, unknown>[]
    expect(fellowList).toHaveLength(1)
    expect(fellowList[0]).toMatchObject({
      certNo: '11010519491231002X',
      fellowName: '王五',
      fellowPhotoId: 'photo-001',
      isMain: 1,
      nativePlace: '',
    })
    // 旧版成功判定需要 data 为真值。
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.addInitScript(() => {
    // Seed only on first load — reloads must keep the in-progress draft.
    if (localStorage.getItem('visitor-flow')) return
    localStorage.setItem(
      'visitor-flow',
      JSON.stringify({
        state: {
          host: { openId: 'oid-1', visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id', receptionistBadge: 'YT001', receptionistName: '赵经理', receptionistPhone: '13800001111' },
          visitor: { visitorName: '', visitorPhotoId: '', certNo: '', company: '', startTime: '', endTime: '' },
          areasByFactory: {}, fellows: [], cars: [], phone: '',
        },
        version: 0,
      }),
    )
  })

  await page.goto('/visitor/info')
  // 区域空校验先触发
  await page.getByPlaceholder('请输入访客姓名').fill('王五')
  await fillInfoForm(page)

  // 时间校验与通过路径：DatePicker 滚轮交互脆弱，时间值经 store 注入
  // （store 是 DatePicker onConfirm 的同一写入口，校验代码路径一致）。
  await page.evaluate(() => {
    const raw = JSON.parse(localStorage.getItem('visitor-flow') as string)
    raw.state.visitor.startTime = '2026-06-15 09:00'
    raw.state.visitor.endTime = '2026-06-15 09:00'
    localStorage.setItem('visitor-flow', JSON.stringify(raw))
  })
  await page.reload()
  await page.getByRole('button', { name: '下一步' }).click()
  await expect(page.getByText('离开时间应大于来访时间!')).toBeVisible()

  await page.evaluate(() => {
    const raw = JSON.parse(localStorage.getItem('visitor-flow') as string)
    raw.state.visitor.endTime = '2026-06-15 18:00'
    localStorage.setItem('visitor-flow', JSON.stringify(raw))
  })
  await page.reload()
  await page.getByRole('button', { name: '下一步' }).click()
  await page.waitForURL('**/visitor/tel')
  expectNoLegacyRequests()
})

test('访客照片上传：嵌套 data.photoId 取值（预览显示 + 存真实 id + 无英文 success）', async ({ page }) => {
  await mockInfoApis(page)
  // 覆盖 checkFace 以断言收到的是 faceCut 裁剪后的 base64（证明没在 cut 步早退）。
  let checkFaceBody: Record<string, unknown> | undefined
  await page.route('**/app/wechat/visit/checkFace', async (route) => {
    const headers = route.request().headers()
    expect(headers['x-visitor-action-capability']).toBe('face-upload-capability')
    expect(headers['x-visitor-draft-id']).toBe('draft-id')
    checkFaceBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, message: 'success', data: { photoId: 'photo-001' } } })
  })
  await page.addInitScript(() => {
    if (localStorage.getItem('visitor-flow')) return
    localStorage.setItem(
      'visitor-flow',
      JSON.stringify({
        state: {
          host: { openId: 'oid-1', visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id', receptionistBadge: 'YT001', receptionistName: '赵经理', receptionistPhone: '13800001111' },
          visitor: { visitorName: '', visitorPhotoId: '', certNo: '', company: '', startTime: '', endTime: '' },
          areasByFactory: {}, fellows: [], cars: [], phone: '',
        },
        version: 0,
      }),
    )
  })

  await page.goto('/visitor/info')
  await page.setInputFiles('[data-testid=face-upload-input]', {
    name: 'face.png',
    mimeType: 'image/png',
    buffer: Buffer.from('89504e470d0a1a0a', 'hex'),
  })
  // 预览显示本地图（不再空白）
  await expect(page.locator('[data-testid=face-upload-button] img')).toBeVisible()
  // 存进草稿的是 data.photoId 的真实值，而非 [object Object]
  await expect
    .poll(() => page.evaluate(() => JSON.parse(localStorage.getItem('visitor-flow') as string).state.visitor.visitorPhotoId))
    .toBe('photo-001')
  // 成功路径不弹任何 toast（旧实现会弹英文 success）
  await expect(page.getByText('success', { exact: true })).toHaveCount(0)
  // checkFace 收到的是裁剪后的 base64（faceCut 的 data 字符串），而非原图或空
  expect(checkFaceBody?.visitorPhoto).toBe('cut-base64')
})

test('访客信息页：授权区域为空拦截', async ({ page }) => {
  await mockInfoApis(page)
  await page.addInitScript(() => {
    if (localStorage.getItem('visitor-flow')) return
    localStorage.setItem(
      'visitor-flow',
      JSON.stringify({
        state: {
          host: { visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id', receptionistBadge: 'YT001', receptionistName: '赵经理', receptionistPhone: '13800001111' },
          visitor: { visitorName: '', visitorPhotoId: '', certNo: '', company: '', startTime: '', endTime: '' },
          areasByFactory: {}, fellows: [], cars: [], phone: '',
        },
        version: 0,
      }),
    )
  })
  await page.goto('/visitor/info')
  await page.getByPlaceholder('请输入访客姓名').fill('王五')
  await page.setInputFiles('[data-testid=face-upload-input]', {
    name: 'face.png',
    mimeType: 'image/png',
    buffer: Buffer.from('89504e470d0a1a0a', 'hex'),
  })
  await expect(page.locator('[data-testid=face-upload-button] img')).toBeVisible()
  await page.getByPlaceholder('请输入身份证号码').fill('11010519491231002X')
  await page.getByPlaceholder('请输入来访单位').fill('测试公司')
  await page.getByText('请选择来访事由').click()
  await page.getByText('商务洽谈').last().click()
  await page.getByRole('button', { name: '确定' }).click()
  await page.getByRole('button', { name: '下一步' }).click()
  await expect(page.getByText('授权区域不能为空！')).toBeVisible()
})

test('访客信息页：访客姓名格式非法当页拦截（不到提交才报）', async ({ page }) => {
  await mockInfoApis(page)
  let equalCalled = false
  await page.route('**/platform/admittance/visitor-entry/precheck', (route) => {
    expectVisitorDraftHeaders(route.request(), true)
    equalCalled = true
    return route.fulfill({ json: { code: 0, data: true } })
  })
  await page.addInitScript(() => {
    if (localStorage.getItem('visitor-flow')) return
    localStorage.setItem(
      'visitor-flow',
      JSON.stringify({
        state: {
          host: { visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id', receptionistBadge: 'YT001', receptionistName: '赵经理', receptionistPhone: '13800001111' },
          visitor: { visitorName: '', visitorPhotoId: '', certNo: '', company: '', startTime: '', endTime: '' },
          areasByFactory: {}, fellows: [], cars: [], phone: '',
        },
        version: 0,
      }),
    )
  })
  await page.goto('/visitor/info')
  // 含特殊字符的非法姓名，点下一步应被本页拦截
  await page.getByPlaceholder('请输入访客姓名').fill('王五@!')
  await page.setInputFiles('[data-testid=face-upload-input]', {
    name: 'face.png',
    mimeType: 'image/png',
    buffer: Buffer.from('89504e470d0a1a0a', 'hex'),
  })
  await expect(page.locator('[data-testid=face-upload-button] img')).toBeVisible()
  await page.getByRole('button', { name: '下一步' }).click()
  await expect(page.getByText('访客姓名输入汉字、英文、数字及下划线1-30个字符')).toBeVisible()
  await expect(page).toHaveURL(/\/visitor\/info$/)
  // 不应发起 capability 预校验（前端先拦下）
  expect(equalCalled).toBe(false)
})

test('访客信息页：提交时去除姓名/证件号的全部空格（含中间）、单位去首尾', async ({ page }) => {
  await mockInfoApis(page)
  let body: Record<string, unknown> | undefined
  await page.route('**/platform/admittance/visitor-entry/precheck', async (route) => {
    expectVisitorDraftHeaders(route.request(), true)
    body = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.addInitScript(() => {
    if (localStorage.getItem('visitor-flow')) return
    localStorage.setItem(
      'visitor-flow',
      JSON.stringify({
        state: {
          host: { openId: 'oid-1', visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id', receptionistBadge: 'YT001', receptionistName: '赵经理', receptionistPhone: '13800001111' },
          visitor: {
            visitorName: '  王 五  ',
            visitorPhotoId: 'photo-001',
            certNo: '  1101 0519 491231002x  ',
            company: '  测试 公司  ',
            cause: { code: 1, desc: '商务洽谈' },
            permitFactoryType: '15',
            startTime: '2026-06-15 09:00',
            endTime: '2026-06-15 18:00',
          },
          areasByFactory: { '15': { list: ['3'], custom: '' } },
          fellows: [], cars: [], phone: '',
        },
        version: 0,
      }),
    )
  })
  await page.goto('/visitor/info')
  await page.getByRole('button', { name: '下一步' }).click()
  await page.waitForURL('**/visitor/tel')
  // 姓名/证件号去全部空格（含中间），单位仅去首尾（保留中间空格）
  expect(body?.visitorName).toBe('王五')
  expect(body?.company).toBe('测试 公司')
  const fellowList = body?.fellowList as Record<string, unknown>[]
  expect(fellowList[0]).toMatchObject({ certNo: '11010519491231002X', fellowName: '王五' })
})

/** Seeds a fully-filled draft so tel-page tests start ready to submit. */
async function seedFilledDraft(page: Page) {
  await page.addInitScript(() => {
    if (localStorage.getItem('visitor-flow')) return
    localStorage.setItem(
      'visitor-flow',
      JSON.stringify({
        state: {
          host: {
            openId: 'oid-1', unionId: 'uid-1', visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id',
            receptionistBadge: 'YT001', receptionistName: '赵经理', receptionistPhone: '13800001111',
          },
          visitor: {
            visitorName: '王五', visitorPhotoId: 'photo-001', certNo: '11010519491231002X',
            company: '测试公司', cause: { code: 1, desc: '商务洽谈' },
            startTime: '2026-06-15 09:00', endTime: '2026-06-15 18:00', permitFactoryType: '15',
          },
          areasByFactory: { '15': { list: ['3'], custom: '三楼会议室' } },
          fellows: [{ fellowName: '李四', fellowPhotoId: 'photo-002', certNo: '110105199001011005' }],
          cars: [],
          phone: '',
        },
        version: 0,
      }),
    )
  })
}

async function mockTelApis(page: Page) {
  await page.route('**/app/sms/visitor/send', (route) => route.fulfill({ json: { code: 0 } }))
  await page.route('**/app/sms/visitor/verify', (route) => route.fulfill({ json: { code: 0 } }))
  await page.route('**/platform/admittance/visitor-entry/options/area-options*', (route) => {
    expectVisitorDraftHeaders(route.request())
    return route.fulfill({ json: { code: 0, data: AREA_CONFIG } })
  })
  await page.route('**/app/wechat/visit/checkBlackVisitor', (route) =>
    route.fulfill({ json: { code: 0, data: true } }),
  )
  await page.route('**/platform/admittance/visitor-action/capability', (route) => {
    expectCapabilityIssueRequest(route.request(), ['BLACKLIST_CHECK', 'APPLY_SUBMIT'])
    return route.fulfill({ json: { code: 0, data: { capability: 'blacklist-capability' } } })
  })
}

test('提交链：短信验证 → 黑名单通过 → protected apply 请求体正确 → result', async ({ page }) => {
  const expectNoLegacyRequests = observeNoLegacyVisitorRequests(page)
  await seedFilledDraft(page)
  await mockTelApis(page)
  let applyBody: Record<string, unknown> | undefined
  await page.route('**/platform/admittance/visitor-entry/apply', async (route) => {
    expectVisitorDraftHeaders(route.request(), true)
    applyBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0 } })
  })

  await page.goto('/visitor/tel')
  await page.getByPlaceholder('点击输入手机号').fill('13900002222')
  await page.getByRole('button', { name: '获取验证码' }).click()
  await page.getByPlaceholder('点击输入验证码').fill('123456')
  await page.getByRole('button', { name: '下一步' }).click()
  await page.waitForURL('**/visitor/result')
  expectNoLegacyRequests()
  await expect(page.getByText('等待被访对象审批')).toBeVisible()

  // 提交体契约断言
  expect(applyBody).toBeDefined()
  const body = applyBody as Record<string, unknown>
  expect(body.startTime).toBe('2026-06-15 09:00:00')
  expect(body.endTime).toBe('2026-06-15 18:00:00')
  // 区域字段按旧版语义：permitArea 是当前工厂自定义文本（非 code），areaType 是勾选 code，remark 空
  expect(body.visitorName).toBe('王五')
  expect(body.visitorPhotoId).toBe('photo-001')
  expect(body.permitFactoryType).toBe('15')
  expect(body.permitArea).toBe('三楼会议室')
  expect(body.permitOldArea).toBe('')
  // save 接口要数字数组（旧版 parseInt），equal/check 才是字符串
  expect(body.areaType).toEqual([3])
  expect(body.remark).toBe('')
  expect(body.visitorPhone).toBe('13900002222')
  const fellowList = body.fellowList as { isMain: number; fellowName: string }[]
  expect(fellowList[0]).toMatchObject({ isMain: 1, fellowName: '王五' })
  expect(fellowList[1]).toMatchObject({ isMain: 0, fellowName: '李四' })
  expect(body.vehicleList).toEqual([])

  // 成功后草稿清空
  const draft = await page.evaluate(() => localStorage.getItem('visitor-flow'))
  expect(JSON.parse(draft as string).state.visitor.visitorName).toBe('')

  // 再预约一次回到入口
  await page.route('https://open.weixin.qq.com/**', (route) =>
    route.fulfill({ contentType: 'text/html', body: 'stub' }),
  )
  await page.getByText('再预约一次').click()
})

test('提交链：黑名单拦截', async ({ page }) => {
  await seedFilledDraft(page)
  await mockTelApis(page)
  await page.route('**/app/wechat/visit/checkBlackVisitor', (route) =>
    route.fulfill({ json: { code: 0, data: false } }),
  )

  await page.goto('/visitor/tel')
  await page.getByPlaceholder('点击输入手机号').fill('13900002222')
  await page.getByPlaceholder('点击输入验证码').fill('123456')
  await page.getByRole('button', { name: '下一步' }).click()
  await expect(page.getByText('抱歉，你已被加入访客黑名单，不能进行入厂申请!')).toBeVisible()
  await expect(page).toHaveURL(/\/visitor\/tel/)
})

test('提交链：黑名单接口报错时拒绝提交（fail closed）', async ({ page }) => {
  await seedFilledDraft(page)
  await mockTelApis(page)
  let applyCalled = false
  await page.route('**/app/wechat/visit/checkBlackVisitor', (route) =>
    route.fulfill({ json: { code: 1, message: '黑名单服务繁忙' } }),
  )
  await page.route('**/platform/admittance/visitor-entry/apply', async (route) => {
    expectVisitorDraftHeaders(route.request(), true)
    applyCalled = true
    await route.fulfill({ json: { code: 0 } })
  })

  await page.goto('/visitor/tel')
  await page.getByPlaceholder('点击输入手机号').fill('13900002222')
  await page.getByPlaceholder('点击输入验证码').fill('123456')
  await page.getByRole('button', { name: '下一步' }).click()
  await expect(page.getByText('黑名单服务繁忙')).toBeVisible()
  expect(applyCalled).toBe(false)
})

test('提交链：黑名单校验体与 save 同口径去空格、证件号大写', async ({ page }) => {
  await page.addInitScript(() => {
    if (localStorage.getItem('visitor-flow')) return
    localStorage.setItem(
      'visitor-flow',
      JSON.stringify({
        state: {
          host: {
            openId: 'oid-1', unionId: 'uid-1', visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id',
            receptionistBadge: 'YT001', receptionistName: '赵经理', receptionistPhone: '13800001111',
          },
          visitor: {
            visitorName: '  王 五  ', visitorPhotoId: 'photo-001', certNo: '  1101 0519 491231002x  ',
            company: '测试公司', cause: { code: 1, desc: '商务洽谈' },
            startTime: '2026-06-15 09:00', endTime: '2026-06-15 18:00', permitFactoryType: '15',
          },
          areasByFactory: { '15': { list: ['3'], custom: '三楼会议室' } },
          fellows: [], cars: [], phone: '',
        },
        version: 0,
      }),
    )
  })
  await mockTelApis(page)
  let blackBody: Record<string, unknown> | undefined
  await page.route('**/app/wechat/visit/checkBlackVisitor', async (route) => {
    blackBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/platform/admittance/visitor-entry/apply', (route) => {
    expectVisitorDraftHeaders(route.request(), true)
    return route.fulfill({ json: { code: 0 } })
  })

  await page.goto('/visitor/tel')
  await page.getByPlaceholder('点击输入手机号').fill('13900002222')
  await page.getByPlaceholder('点击输入验证码').fill('123456')
  await page.getByRole('button', { name: '下一步' }).click()
  await page.waitForURL('**/visitor/result')

  expect(blackBody?.visitorName).toBe('王五')
  expect(blackBody?.certNo).toBe('11010519491231002X')
})

test('提交链：获取验证码后端限流(code!=0)时弹后端提示且不启动倒计时', async ({ page }) => {
  await seedFilledDraft(page)
  await mockTelApis(page)
  // 覆盖默认发送 mock：HTTP 200 但 code=1，客户端必须提示失败且不启动倒计时。
  await page.route('**/app/sms/visitor/send', (route) =>
    route.fulfill({ json: { code: 1, message: '同一号码验证码提交过快', data: '' } }),
  )

  await page.goto('/visitor/tel')
  await page.getByPlaceholder('点击输入手机号').fill('13900002222')
  await page.getByRole('button', { name: '获取验证码' }).click()

  // 应弹后端真实提示，而不是「发送成功」；按钮保持「获取验证码」可点（未误入倒计时）
  await expect(page.getByText('同一号码验证码提交过快')).toBeVisible()
  await expect(page.getByText('发送成功')).toHaveCount(0)
  await expect(page.getByRole('button', { name: '获取验证码' })).toBeEnabled()
})

test('tel 页：草稿不完整直接进入被守卫回退', async ({ page }) => {
  await page.route('https://open.weixin.qq.com/**', (route) =>
    route.fulfill({ contentType: 'text/html', body: 'stub' }),
  )
  await page.goto('/visitor/tel')
  // 空草稿 → 回 /visitor（入口无 code 再跳 OAuth）
  await page.waitForURL(/visitor(?!\/tel)|open\.weixin/)
})

test('访客信息页：预校验失败 toast、身份证错误 toast、时间清空联动', async ({ page }) => {
  await mockInfoApis(page)
  await seedFilledDraft(page)
  await page.route('**/platform/admittance/visitor-entry/precheck', (route) => {
    expectVisitorDraftHeaders(route.request(), true)
    return route.fulfill({ json: { code: 1, message: '该访客已有进行中的申请' } })
  })

  await page.goto('/visitor/info')
  // 身份证错误
  await page.getByPlaceholder('请输入身份证号码').fill('110105194912310021')
  await page.getByRole('button', { name: '下一步' }).click()
  await expect(page.getByText('证件号码校验位不正确')).toBeVisible()

  // 修正后 capability 预校验失败 toast
  await page.getByPlaceholder('请输入身份证号码').fill('11010519491231002X')
  await page.getByRole('button', { name: '下一步' }).click()
  await expect(page.getByText('该访客已有进行中的申请')).toBeVisible()

  // DatePicker onConfirm：重选来访时间会清空离开时间（旧版联动）
  await page.getByRole('button', { name: '2026-06-15 09:00' }).click()
  await page.getByRole('button', { name: '确定' }).last().click()
  await expect(page.getByRole('button', { name: '请选择离开时间' })).toBeVisible()
})

test('提交链：区域复核剪空 → toast 并回 info', async ({ page }) => {
  await seedFilledDraft(page)
  await mockTelApis(page)
  // 区域配置已不包含草稿中的工厂 '15'
  await page.route('**/platform/admittance/visitor-entry/options/area-options*', (route) => {
    expectVisitorDraftHeaders(route.request())
    return route.fulfill({
      json: {
        code: 0,
        data: {
          parkId: 5000021,
          factories: [{ factoryType: '99', factoryName: '其他厂', areaFlag: 1, sort: 1, areas: [{ areaCode: 99, areaName: '新区' }] }],
        },
      },
    })
  })

  await page.goto('/visitor/tel')
  await page.getByPlaceholder('点击输入手机号').fill('13900002222')
  await page.getByPlaceholder('点击输入验证码').fill('123456')
  await page.getByRole('button', { name: '下一步' }).click()
  await expect(page.getByText('请选择授权区域')).toBeVisible()
  await page.waitForURL('**/visitor/info')
})

test('入口：被访人查询失败 toast 后端 message', async ({ page }) => {
  await mockEntryApis(page)
  await page.route('**/platform/admittance/visitor-action/capability', (route) => {
    expectCapabilityIssueRequest(route.request(), 'RECEPTIONIST_SEARCH')
    return route.fulfill({ json: { code: 0, data: { capability: 'receptionist-capability' } } })
  })
  await page.route('**/platform/admittance/visitor-entry/receptionist', (route) => {
    const headers = route.request().headers()
    expect(headers['x-visitor-draft-id']).toBe('draft-id')
    expect(headers['x-visitor-action-capability']).toBe('receptionist-capability')
    return route.fulfill({ json: { code: 1, message: '查无此人，请确认被访人信息' } })
  })

  await page.goto('/visitor?code=visitor-code')
  await page.getByPlaceholder('请输入被访人姓名').fill('不存在')
  await page.getByPlaceholder('请输入被访人手机号').fill('13800001111')
  await page.getByRole('button', { name: '下一步' }).click()
  await expect(page.getByText('查无此人，请确认被访人信息')).toBeVisible()
})
