import { expect, test, type Page, type Request } from '@playwright/test'

/** 选项接口只接受当前微信 OAuth 草稿，不能退回裸匿名枚举。 */
function expectVisitorDraftHeaders(request: Request) {
  const headers = request.headers()
  expect(headers['x-visitor-draft-token']).toBe('draft-token')
  expect(headers['x-visitor-draft-id']).toBe('draft-id')
}

/** 上传动作票据必须绑定当前草稿和明确的单一动作。 */
function expectVisitorActionCapability(request: Request, action: string | string[]) {
  expect(request.headers()['x-visitor-draft-token']).toBe('draft-token')
  const body = request.postDataJSON() as Record<string, unknown>
  expect(body).toMatchObject({ draftId: 'draft-id' })
  if (Array.isArray(action)) {
    expect(action).toContain(body.action)
  } else {
    expect(body.action).toBe(action)
  }
}

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

async function seedDraft(page: Page) {
  await page.addInitScript(() => {
    if (localStorage.getItem('visitor-flow')) return
    localStorage.setItem(
      'visitor-flow',
      JSON.stringify({
        state: {
          host: {
            receptionistBadge: 'YT001', receptionistName: '赵经理', receptionistPhone: '13800001111',
            visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id',
          },
          visitor: {
            visitorName: '王五', visitorPhotoId: 'photo-001', certNo: '11010519491231002X',
            company: '测试公司', cause: { code: 1, desc: '商务洽谈' },
            startTime: '', endTime: '', permitFactoryType: '15',
          },
          areasByFactory: {}, fellows: [], cars: [], phone: '',
        },
        version: 0,
      }),
    )
  })
}

async function mockAreaApis(page: Page) {
  await page.route('**/platform/admittance/visitor-entry/options/area-options*', (route) => {
    expectVisitorDraftHeaders(route.request())
    return route.fulfill({ json: { code: 0, data: AREA_CONFIG } })
  })
  await page.route('**/platform/admittance/visitor-entry/options/cause', (route) => {
    expectVisitorDraftHeaders(route.request())
    return route.fulfill({ json: { code: 0, data: [{ code: 1, desc: '商务洽谈' }] } })
  })
}

test('区域选择：无搜索框、全选、确定回写 info 已选数', async ({ page }) => {
  await seedDraft(page)
  await mockAreaApis(page)

  // 先落同源页，使确定后的 router.back() 有可断言的落点
  await page.goto('/visitor/cars')
  await page.goto('/visitor/area?type=1&factoryType=15&parkId=5000021')
  await expect(page.getByText('已选 0/3')).toBeVisible()

  // 授权区域很少，访客侧不保留低价值搜索入口。
  await expect(page.getByPlaceholder('搜索授权区域')).toHaveCount(0)
  await expect(page.getByText('办公区', { exact: true })).toBeVisible()
  await expect(page.getByText('生产一区')).toBeVisible()
  await expect(page.getByText('仓储区')).toBeVisible()

  // 全选 → 取消全选 → 单选
  await page.getByRole('button', { name: '全选' }).click()
  await expect(page.getByText('已选 3/3')).toBeVisible()
  await page.getByRole('button', { name: '取消全选' }).click()
  await expect(page.getByText('已选 0/3')).toBeVisible()
  await page.getByText('仓储区').click()
  await page.getByPlaceholder('可补充详细位置（选填）').fill('三号门岗东侧')

  // 确定 → 草稿写入并返回上一页
  await page.getByRole('button', { name: '确定' }).click()
  await page.waitForURL('**/visitor/cars')
  const draft = await page.evaluate(() => JSON.parse(localStorage.getItem('visitor-flow') as string))
  expect(draft.state.areasByFactory['15']).toEqual({ list: ['6'], custom: '三号门岗东侧' })
})

test('区域选择：配置匹配不到厂区 → 清缓存 + toast', async ({ page }) => {
  await seedDraft(page)
  await page.addInitScript(() => {
    localStorage.setItem('visitor-area-options-5000021', JSON.stringify([{ factoryType: 'STALE', areas: [] }]))
  })
  await page.route('**/platform/admittance/visitor-entry/options/area-options*', (route) => {
    expectVisitorDraftHeaders(route.request())
    return route.fulfill({
      json: { code: 0, data: { parkId: 5000021, factories: [{ factoryType: 'OTHER', factoryName: '其他', areaFlag: 1, sort: 1, areas: [] }] } },
    })
  })

  await page.goto('/visitor/area?type=1&factoryType=GONE&parkId=5000021')
  await expect(page.getByText('授权区域配置不可用，请联系管理员').first()).toBeVisible()
  const cache = await page.evaluate(() => localStorage.getItem('visitor-area-options-5000021'))
  expect(cache).toBeNull()
  // 配置不可用时无确定按钮，不可能写入脏厂区 key
  await expect(page.getByRole('button', { name: '确定' })).not.toBeVisible()
  const draft = await page.evaluate(() => JSON.parse(localStorage.getItem('visitor-flow') as string))
  expect(draft.state.areasByFactory.GONE).toBeUndefined()
})

test('随行人员：增 → 列表 → 编辑 → 删除 → 空态；身份证非法拦截', async ({ page }) => {
  await seedDraft(page)
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
    expectVisitorActionCapability(route.request(), 'DOCUMENT_UPLOAD')
    return route.fulfill({ json: { code: 0, data: { capability: 'visitor-action-capability' } } })
  })
  await page.route('**/app/wechat/visit/checkFace', (route) => {
    const headers = route.request().headers()
    expect(headers['x-visitor-action-capability']).toBe('visitor-action-capability')
    expect(headers['x-visitor-draft-id']).toBe('draft-id')
    return route.fulfill({ json: { code: 0, message: 'success', data: { photoId: 'photo-fellow' } } })
  })

  await page.goto('/visitor/persons')
  await expect(page.getByText('暂无随行人员信息')).toBeVisible()
  await page.getByRole('button', { name: '新增随行人员' }).click()

  await page.getByPlaceholder('请输入', { exact: true }).fill('李四')
  await page.setInputFiles('[data-testid=face-upload-input]', {
    name: 'face.png',
    mimeType: 'image/png',
    buffer: Buffer.from('89504e470d0a1a0a', 'hex'),
  })
  await expect(page.locator('[data-testid=face-upload-button] img')).toBeVisible()
  // 身份证非法
  await page.getByPlaceholder('请输入身份证号码').fill('110105194912310021')
  await page.getByRole('button', { name: '确认添加随行人员' }).click()
  await expect(page.getByText('证件号码校验位不正确')).toBeVisible()
  // 修正提交
  await page.getByPlaceholder('请输入身份证号码').fill('110105199001011005')
  await page.getByRole('button', { name: '确认添加随行人员' }).click()
  await page.waitForURL('**/visitor/persons')
  await expect(page.getByText('已添加随行人员（1人）')).toBeVisible()
  await expect(page.getByText('李四')).toBeVisible()

  // 编辑
  await page.getByRole('button', { name: '编辑' }).click()
  await expect(page.getByRole('button', { name: '确认修改随行人员' })).toBeVisible()
  await page.getByPlaceholder('请输入', { exact: true }).fill('李四四')
  await page.getByRole('button', { name: '确认修改随行人员' }).click()
  await page.waitForURL('**/visitor/persons')
  await expect(page.getByText('李四四')).toBeVisible()

  // 删除（无二次确认）
  await page.getByRole('button', { name: '删除' }).click()
  await expect(page.getByText('暂无随行人员信息')).toBeVisible()
})

test('车辆：添加（默认司机姓名/证件类型）→ 列表 → 删除', async ({ page }) => {
  await seedDraft(page)
  await page.route('**/platform/admittance/visitor-entry/options/vehicle-cert', (route) => {
    expectVisitorDraftHeaders(route.request())
    return route.fulfill({ json: { code: 0, data: [{ code: 2, desc: '身份证复印件' }, { code: 1, desc: '行驶证' }] } })
  })
  await page.route('**/app/wechat/visit/checkFace', (route) => {
    const headers = route.request().headers()
    expect(headers['x-visitor-action-capability']).toBe('document-capability')
    expect(headers['x-visitor-draft-id']).toBe('draft-id')
    return route.fulfill({ json: { code: 0, message: 'success', data: { photoId: 'photo-cert' } } })
  })
  await page.route('**/platform/admittance/visitor-action/capability', (route) => {
    expectVisitorActionCapability(route.request(), 'DOCUMENT_UPLOAD')
    return route.fulfill({ json: { code: 0, data: { capability: 'document-capability' } } })
  })

  await page.goto('/visitor/cars')
  await expect(page.getByText('暂无车辆信息')).toBeVisible()
  await page.getByRole('button', { name: '新增车辆' }).click()

  // 默认值：司机姓名带访客姓名、证件类型身份证复印件
  await expect(page.getByPlaceholder('请输入司机姓名')).toHaveValue('王五')
  await expect(page.getByRole('button', { name: '身份证复印件' })).toBeVisible()

  await page.getByPlaceholder('请输入车牌号').fill('A12345')
  await page.setInputFiles('[data-testid=face-upload-input]', {
    name: 'cert.png',
    mimeType: 'image/png',
    buffer: Buffer.from('89504e470d0a1a0a', 'hex'),
  })
  await expect(page.locator('[data-testid=face-upload-button] img')).toBeVisible()
  await page.getByRole('button', { name: '确认添加车辆' }).click()

  await page.waitForURL('**/visitor/cars')
  await expect(page.getByText('已添加车辆（1辆）')).toBeVisible()
  await expect(page.getByText('豫A12345')).toBeVisible()
  await expect(page.getByText('司机姓名: 王五')).toBeVisible()

  const draft = await page.evaluate(() => JSON.parse(localStorage.getItem('visitor-flow') as string))
  expect(draft.state.cars[0]).toMatchObject({ plate: '豫A12345', name: '王五', certImg: 'photo-cert' })
  expect(draft.state.cars[0].certType.code).toBe(2)

  await page.getByRole('button', { name: '删除' }).click()
  await expect(page.getByText('暂无车辆信息')).toBeVisible()
})

test('主链回归：带随行人员与车辆的提交体映射', async ({ page }) => {
  await page.addInitScript(() => {
    if (localStorage.getItem('visitor-flow')) return
    localStorage.setItem(
      'visitor-flow',
      JSON.stringify({
        state: {
          host: { openId: 'oid-1', unionId: 'uid-1', visitorDraftToken: 'draft-token', visitorDraftId: 'draft-id', receptionistBadge: 'YT001', receptionistName: '赵经理', receptionistPhone: '13800001111' },
          visitor: {
            visitorName: '王五', visitorPhotoId: 'photo-001', certNo: '11010519491231002X',
            company: '测试公司', cause: { code: 1, desc: '商务洽谈' },
            startTime: '2026-06-15 09:00', endTime: '2026-06-15 18:00', permitFactoryType: '15',
          },
          areasByFactory: { '15': { list: ['3'], custom: '' } },
          fellows: [{ fellowName: '李四', fellowPhotoId: 'photo-002', certNo: '110105199001011005' }],
          cars: [{ plate: '豫A12345', name: '王五', certType: { code: 2, desc: '身份证复印件' }, certImg: 'photo-cert' }],
          phone: '',
        },
        version: 0,
      }),
    )
  })
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
    expectVisitorActionCapability(route.request(), ['BLACKLIST_CHECK', 'APPLY_SUBMIT'])
    return route.fulfill({ json: { code: 0, data: { capability: 'visitor-action-capability' } } })
  })
  let applyBody: Record<string, unknown> | undefined
  await page.route('**/platform/admittance/visitor-entry/apply', async (route) => {
    const headers = route.request().headers()
    expect(headers['x-visitor-draft-token']).toBe('draft-token')
    expect(headers['x-visitor-draft-id']).toBe('draft-id')
    expect(headers['x-visitor-action-capability']).toBeTruthy()
    applyBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0 } })
  })

  await page.goto('/visitor/tel')
  await page.getByPlaceholder('点击输入手机号').fill('13900002222')
  await page.getByPlaceholder('点击输入验证码').fill('123456')
  await page.getByRole('button', { name: '下一步' }).click()
  await page.waitForURL('**/visitor/result')

  const body = applyBody as Record<string, unknown>
  expect(body.vehicleList).toEqual([
    { plate: '豫A12345', name: '王五', certType: 2, certImg: 'photo-cert' },
  ])
  const fellowList = body.fellowList as Record<string, unknown>[]
  expect(fellowList).toHaveLength(2)
  expect(fellowList[0]).toMatchObject({ isMain: 1, fellowName: '王五', fellowPhotoId: 'photo-001' })
  expect(fellowList[1]).toMatchObject({ isMain: 0, fellowName: '李四', certNo: '110105199001011005' })
  // 区域字段按旧版语义：custom 为空时 permitArea 为空串；save 接口要数字数组
  expect(body.permitArea).toBe('')
  expect(body.permitOldArea).toBe('')
  expect(body.areaType).toEqual([3])
  expect(body.permitFactoryType).toBe('15')
})
