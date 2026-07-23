import { expect, test, type Page } from '@playwright/test'

async function mockTruckApis(page: Page) {
  await page.route('**/platform/admittance/apply/enum/car/cause', (route) =>
    route.fulfill({ json: { code: 0, data: [{ code: 11, desc: '送货' }, { code: 12, desc: '提货' }] } }),
  )
  await page.route('**/app/sms/visitor/send', (route) => route.fulfill({ json: { code: 0 } }))
  await page.route('**/app/sms/visitor/verify', (route) => route.fulfill({ json: { code: 0 } }))
}

test('货车预约：填表 → 短信验证 → 提交体正确 → 结果页', async ({ page }) => {
  await mockTruckApis(page)
  let applyBody: Record<string, unknown> | undefined
  await page.route('**/platform/admittance/apply/save/car/apply', async (route) => {
    applyBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0 } })
  })

  await page.goto('/visitor/truck')
  await page.getByPlaceholder('请输入车牌号').fill('B88888')
  await page.getByText('请选择来访事由').click()
  await page.getByText('送货').last().click()
  await page.getByRole('button', { name: '确定' }).last().click()
  await page.getByPlaceholder('请输入访客姓名').fill('老司机')
  await page.getByPlaceholder('请输入出发地').fill('郑州中转仓')
  await page.getByText('请选择预约时间').click()
  await page.getByRole('button', { name: '确定' }).last().click()
  await page.getByPlaceholder('请填写内托/原材/成品/其他').fill('成品')
  await page.getByPlaceholder('点击输入手机号').fill('13900003333')
  await page.getByRole('button', { name: '获取验证码' }).click()
  await page.getByPlaceholder('点击输入验证码').fill('123456')
  await page.getByRole('button', { name: '提交申请' }).click()

  await page.waitForURL('**/visitor/truck/result')
  await expect(page.getByText('等待系统审批')).toBeVisible()

  const body = applyBody as Record<string, unknown>
  expect(body.visitorName).toBe('老司机')
  expect(body.visitorPhone).toBe('13900003333')
  expect(body.company).toBe('郑州中转仓')
  expect(body.cause).toBe(11)
  expect(body.remark).toBe('成品')
  expect(String(body.startTime)).toMatch(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:00$/)
  expect(body.vehicleList).toEqual([{ name: '老司机', plate: '豫B88888' }])

  // 再预约一次
  await page.getByText('再预约一次').click()
  await page.waitForURL('**/visitor/truck')
})

test('货车预约：验证码校验失败 toast 且不提交不跳转', async ({ page }) => {
  await mockTruckApis(page)
  await page.route('**/app/sms/visitor/verify', (route) =>
    route.fulfill({ json: { code: 1, message: '验证码错误或已过期' } }),
  )
  let applyCalled = false
  await page.route('**/platform/admittance/apply/save/car/apply', async (route) => {
    applyCalled = true
    await route.fulfill({ json: { code: 0 } })
  })

  await page.goto('/visitor/truck')
  await page.getByPlaceholder('请输入车牌号').fill('B88888')
  await page.getByText('请选择来访事由').click()
  await page.getByText('送货').last().click()
  await page.getByRole('button', { name: '确定' }).last().click()
  await page.getByPlaceholder('请输入访客姓名').fill('老司机')
  await page.getByPlaceholder('请输入出发地').fill('郑州中转仓')
  await page.getByText('请选择预约时间').click()
  await page.getByRole('button', { name: '确定' }).last().click()
  await page.getByPlaceholder('点击输入手机号').fill('13900003333')
  await page.getByPlaceholder('点击输入验证码').fill('000000')
  await page.getByRole('button', { name: '提交申请' }).click()
  await expect(page.getByText('验证码错误或已过期')).toBeVisible()
  // verify 失败必须阻断保存且停留本页
  expect(applyCalled).toBe(false)
  await expect(page).toHaveURL(/\/visitor\/truck$/)
})

const DETAIL_BASE = {
  parkName: '裕同科技许昌园区',
  causeDesc: '商务洽谈',
  receptionistName: '赵经理',
  receptionistPhone: '13800001111',
  visitorName: '王五',
  visitorPhone: '13900002222',
  permitFactoryTypeDesc: '新工厂',
  permitArea: 'A1,A2',
  permitOldArea: '',
  startTime: '2026-06-15 09:00:00',
  endTime: '2026-06-15 18:00:00',
  smsCode: '668866',
  qrCode: 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==',
}

async function mockCodeApis(page: Page, delFlag: number) {
  await page.route('**/platform/admittance/apply/search/Detail/*', (route) =>
    route.fulfill({ json: { code: 0, data: { ...DETAIL_BASE, delFlag } } }),
  )
  await page.route('**/platform/admittance/apply/enum/factory/type*', (route) => {
    const url = new URL(route.request().url())
    const flag = url.searchParams.get('flag')
    return route.fulfill({
      json: {
        code: 0,
        data: flag === '1' ? [{ code: 'A1', desc: '办公区' }, { code: 'A2', desc: '生产一区' }] : [],
      },
    })
  })
}

test('二维码页：有效态（delFlag=0）完整渲染', async ({ page }) => {
  await mockCodeApis(page, 0)
  await page.goto('/visitor/code?id=apply-1')

  await expect(page.getByText('裕同科技许昌园区欢迎您')).toBeVisible()
  await expect(page.getByAltText('访客通行二维码')).toBeVisible()
  await expect(page.getByText('668866')).toBeVisible()
  await expect(page.getByText('首次扫码，打印有效')).toBeVisible()
  await expect(page.getByText('赵经理 13800001111（被访人）')).toBeVisible()
  await expect(page.getByText('王五 13900002222（访客）')).toBeVisible()
  // 区域 code → 名称映射
  await expect(page.getByText('办公区，生产一区')).toBeVisible()
  await expect(page.getByText('使用指引')).toBeVisible()
})

test('二维码页：已删除（delFlag=1）失效但保留预约信息', async ({ page }) => {
  await mockCodeApis(page, 1)
  await page.goto('/visitor/code?id=apply-1')

  await expect(page.getByText('二维码已失效')).toBeVisible()
  await expect(page.getByAltText('访客通行二维码')).not.toBeVisible()
  await expect(page.getByText('赵经理 13800001111（被访人）')).toBeVisible()
  await expect(page.getByText('使用指引')).not.toBeVisible()
})

test('二维码页：已过期（delFlag=2）仅失效占位', async ({ page }) => {
  await mockCodeApis(page, 2)
  await page.goto('/visitor/code?id=apply-1')

  await expect(page.getByText('二维码已失效')).toBeVisible()
  await expect(page.getByText('赵经理', { exact: false })).not.toBeVisible()
  await expect(page.getByText('使用指引')).not.toBeVisible()
})
