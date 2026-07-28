import { expect, test, type Page } from '@playwright/test'

async function seedLogin(page: Page) {
  await page.addInitScript(() => {
    localStorage.setItem(
      'xc-access_token',
      JSON.stringify({ dataType: 'string', content: 'tok-e2e', datetime: Date.now() }),
    )
  })
}

async function mockBaseInfo(page: Page) {
  await page.route('**/app/employee/baseinfo', (route) =>
    route.fulfill({
      json: { code: 0, data: { employeeName: '王建国', employeeBadge: 'YT20180326', status: 1 } },
    }),
  )
}

async function pickWheelOption(page: Page, text: string) {
  const current = page.locator(`[aria-label="当前选择的是：${text}"]`).last()
  for (let i = 0; i < 20; i++) {
    if ((await current.count()) > 0) break
    await page
      .locator('.adm-picker-view-column-accessible-button[role=button]')
      .last()
      .dispatchEvent('click')
  }
  await expect(current).toBeAttached()
  await page.getByRole('button', { name: '确定' }).last().click()
}

// 旧版选项事实：value 四段 id、label「宿舍名/楼层名层/房间名号房/床号床」（床位主键是 id）
const LIVE_ROOMS = {
  code: 0,
  data: [
    {
      dormitoryId: 'D1',
      floorId: 'F2',
      roomId: 'R3',
      id: 'B4',
      dormitoryName: '新工厂宿舍楼',
      floorName: '2',
      roomName: '302',
      bedNumber: 4,
    },
  ],
}

test('生活区放行：无房间拦截（toast + 提交阻断）', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/platform/dormitory/staff/me/roomList', (route) =>
    route.fulfill({ json: { code: 0, data: [] } }),
  )

  await page.goto('/good-release/live')
  await expect(
    page.getByText('没有查询到您的房间信息，不能进行物品放行（生活区）申请'),
  ).toBeVisible()
})

test('生活区放行：空图拦截 → 正常提交体断言', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/platform/dormitory/staff/me/roomList', (route) => route.fulfill({ json: LIVE_ROOMS }))
  let saveBody: Record<string, unknown> | undefined
  await page.route('**/platform/articlesrelease/living/save', async (route) => {
    saveBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/platform/articlesrelease/page*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  await page.goto('/good-release/live')
  await expect(page.getByText('宿舍生活物品')).toBeVisible()
  await page.getByPlaceholder('请输入物品名称').fill('行李箱')
  await page.getByRole('button', { name: '请选择房间' }).click()
  await pickWheelOption(page, '新工厂宿舍楼/2层/302号房/4床')
  await page.getByRole('button', { name: '请选择预计离厂日期' }).click()
  await page.getByRole('button', { name: '确定' }).last().click()

  // 照片必填：空图拦截
  await page.getByRole('button', { name: '申请' }).click()
  await expect(page.getByText('请上传物品照片')).toBeVisible()
  await expect(page).toHaveURL(/\/good-release\/live$/)

  await page.setInputFiles('[data-testid=image-list-input]', {
    name: 'a.png',
    mimeType: 'image/png',
    buffer: Buffer.from('89504e470d0a1a0a', 'hex'),
  })
  await expect(page.getByTestId('image-list-item')).toHaveCount(1)
  await page.getByRole('button', { name: '申请' }).click()
  await page.waitForURL('**/good-release/live/list')

  const body = saveBody as Record<string, unknown>
  expect(body.articlesDesc).toBe('行李箱')
  expect(body.articlesType).toBe(3)
  expect(body.badge).toBe('YT20180326')
  expect(body.parkId).toBe(5000021)
  expect(body.dormitoryId).toBe('D1')
  expect(body.floorId).toBe('F2')
  expect(body.roomId).toBe('R3')
  expect(body.bedId).toBe('B4')
  expect(body.carrier).toBe('王建国')
  expect(body.name).toBe('王建国')
  expect(String(body.plannedDepartureTime)).toMatch(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}$/)
  expect(body.status).toBe(1)
  expect(String(body.oneImg)).toMatch(/^[A-Za-z0-9+/=]+$/)
  expect(body.twoImg).toBe('')
  expect(body.threeImg).toBe('')
})

test('生活区放行：列表状态配色与卡片字段（断言 type=3 与 badge）', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  let listQuery: URLSearchParams | undefined
  await page.route('**/platform/articlesrelease/page*', (route) => {
    listQuery = new URL(route.request().url()).searchParams
    return route.fulfill({
      json: {
        code: 0,
        data: {
          records: [
            { id: 1, name: '王建国', status: 2, oaNode: '已通过', articlesTypeName: '宿舍生活物品', articlesDesc: '行李箱', carrier: '王建国', createTime: '2026-06-12 10:00' },
            { id: 2, name: '王建国', status: 3, oaNode: '已拒绝', articlesTypeName: '宿舍生活物品', articlesDesc: '风扇', carrier: '王建国', createTime: '2026-06-11 09:00' },
            { id: 3, name: '王建国', status: 1, oaNode: '审批中', articlesTypeName: '宿舍生活物品', articlesDesc: '台灯', carrier: '王建国', createTime: '2026-06-10 08:00' },
          ],
          pages: 1,
        },
      },
    })
  })

  await page.goto('/good-release/live/list')
  await expect(page.getByText('已通过')).toHaveClass(/text-\[#16a673\]/)
  await expect(page.getByText('已拒绝')).toHaveClass(/text-\[#d83b36\]/)
  await expect(page.getByText('审批中')).toHaveClass(/text-mid/)
  await expect(page.getByText('行李箱')).toBeVisible()
  // 生活区列表关键查询参数
  expect(listQuery?.get('type')).toBe('3')
  expect(listQuery?.get('badge')).toBe('YT20180326')
  expect(listQuery?.get('size')).toBe('10')
})

test('生活区放行详情：三态互斥 + 放行信息区', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  const base = {
    id: 9,
    name: '王建国',
    carrier: '王建国',
    articlesTypeName: '宿舍生活物品',
    articlesDesc: '行李箱',
    dormitoryName: '新工厂宿舍楼',
    roomName: '302',
    plannedDepartureTime: '2026-06-20 10:00',
    approvalProcess: [
      { statusName: '宿管审批', approvers: [{ name: '张**', result: 1, time: '2026-06-12 10:00' }] },
    ],
  }
  let variant: Record<string, unknown> = { status: 2, qrCodePic: 'aGk=', expire: false }
  await page.route('**/platform/articlesrelease/detail/9', (route) =>
    route.fulfill({ json: { code: 0, data: { ...base, ...variant } } }),
  )

  // 出码态
  await page.goto('/good-release/live/detail?id=9')
  await expect(page.getByAltText('放行二维码')).toBeVisible()
  await expect(page.getByText('在门卫处出示放行码')).toBeVisible()

  // 过期态
  variant = { status: 2, qrCodePic: 'aGk=', expire: true }
  await page.goto('/good-release/live/detail?id=9')
  await expect(page.getByText('放行码已过期')).toBeVisible()
  await expect(page.getByAltText('放行二维码')).not.toBeVisible()

  // 已出厂态 + 放行信息区
  variant = {
    status: 4,
    expire: true,
    statusName: '已出厂',
    securityStaff: '保安老王',
    departureTime: '2026-06-12 18:00',
    remark: '正常放行',
  }
  await page.goto('/good-release/live/detail?id=9')
  await expect(page.getByText('已出厂', { exact: true }).first()).toBeVisible()
  await expect(page.getByText('放行信息')).toBeVisible()
  await expect(page.getByText('保安老王')).toBeVisible()
  // 审批中不显示放行信息
  variant = { status: 1, expire: false }
  await page.goto('/good-release/live/detail?id=9')
  await expect(page.getByText('放行信息')).not.toBeVisible()
  await expect(page.getByText('张** - 通过')).toBeVisible()

  // 缺 id 直接快速失败，不能无限转圈
  await page.goto('/good-release/live/detail')
  await expect(page.getByText('加载失败')).toBeVisible()
})

test('死链回归：home 宫格「物品放行（生活区）」入口落到真实页', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/app/employee/fullinfo', (route) =>
    route.fulfill({ json: { code: 0, data: { employeeId: 'YT20180326', employeeName: '王建国' } } }),
  )
  await page.route('**/app/common/weather*', (route) => route.fulfill({ json: { code: 0, data: {} } }))
  await page.route('**/app/home/bbs/list*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0, records: [] } } }),
  )
  await page.route('**/app/service/module/list', (route) =>
    route.fulfill({
      json: { code: 0, data: { serviceModule: [{ moduleName: '物品放行（生活区）', moduleUrl: '/releaseGoods' }] } },
    }),
  )
  await page.route('**/platform/approve/**', (route) => route.fulfill({ json: { code: 0, data: { total: 0 } } }))
  await page.route('**/platform/dor/quit/list/approval*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0 } } }),
  )
  await page.route('**/platform/dormitory/staff/me/roomList', (route) => route.fulfill({ json: LIVE_ROOMS }))

  await page.goto('/home')
  await page.getByRole('button', { name: '物品放行（生活区）', exact: true }).click()
  await page.waitForURL('**/good-release/live')
  await expect(page.getByRole('tab', { name: '发起提交' })).toBeVisible()
})
