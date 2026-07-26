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

async function seedQueryToken(page: Page, token = 'query-token') {
  await page.addInitScript((value) => {
    localStorage.setItem('visitor-query-session', JSON.stringify({ queryToken: value, maskedName: '王五', maskedMobile: '139****2222', savedAt: Date.now() }))
  }, token)
}

test('二维码页：仅携带 queryToken 调用最小通行码端点并渲染二维码', async ({ page }) => {
  await seedQueryToken(page)
  await page.route('**/platform/admittance/apply/app/passCode*', async (route) => {
    expect(route.request().headers()['x-visitor-query-token']).toBe('query-token')
    await route.fulfill({ json: { code: 0, data: { applyId: '1001', valid: true, smsCode: '668866', qrCode: 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==' } } })
  })
  await page.goto('/visitor/code?id=1001')
  await expect(page.getByAltText('访客通行二维码')).toBeVisible()
  await expect(page.getByText('668866')).toBeVisible()
  await expect(page.getByText('赵经理', { exact: false })).not.toBeVisible()
})

test('二维码页：无 token 不请求通行码并进入短信验证', async ({ page }) => {
  let called = false
  await page.route('**/platform/admittance/apply/app/passCode*', (route) => {
    called = true
    return route.fulfill({ json: { code: 0 } })
  })
  await page.goto('/visitor/code?id=1001')
  await page.waitForURL('**/visitor/records?redirect=1001')
  expect(called).toBe(false)
})

test('二维码页：过期或非本人 token 被拒绝后清凭证并进入短信验证', async ({ page }) => {
  await seedQueryToken(page, 'expired-token')
  await page.route('**/platform/admittance/apply/app/passCode*', (route) =>
    route.fulfill({ status: 403, json: { code: 403, message: 'forbidden' } }),
  )
  await page.goto('/visitor/code?id=1001')
  await page.waitForURL('**/visitor/records?redirect=1001')
  expect(await page.evaluate(() => localStorage.getItem('visitor-query-session'))).toBeNull()
})
