import { expect, test, type Page } from '@playwright/test'

/** Seed a valid legacy-format token before the app boots. */
async function seedLogin(page: Page) {
  await page.addInitScript(() => {
    localStorage.setItem(
      'xc-access_token',
      JSON.stringify({ dataType: 'string', content: 'tok-e2e', datetime: Date.now() }),
    )
  })
}

async function mockHomeApis(page: Page) {
  await page.route('**/app/employee/fullinfo', (route) =>
    route.fulfill({ json: { code: 0, data: { employeeId: 'YT20180326', employeeName: '王建国' } } }),
  )
  await page.route('**/app/employee/baseinfo', (route) =>
    route.fulfill({ json: { code: 0, data: { employeeName: '王建国', status: 1, isSecurityGuard: 0 } } }),
  )
  await page.route('**/app/common/weather*', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: { city: '许昌', wendu: '27', forecast: [{ type: '多云', fengxiang: '东南风' }] },
      },
    }),
  )
  await page.route('**/app/home/bbs/list*', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: {
          total: 2,
          records: [
            { bbsId: 11, bbsTitle: '关于五一假期园区门禁开放时间调整的通知', contentLinkType: 2 },
            { bbsId: 12, bbsTitle: '宿舍区消防演练通知', contentLinkType: 2 },
          ],
        },
      },
    }),
  )
  await page.route('**/app/service/module/list', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: {
          serviceModule: [
            { moduleName: '园区报修', moduleUrl: '/dormRepairs' },
            { moduleName: '宿舍申请', moduleUrl: '/xuchang/checkIn' },
            { moduleName: '扫码放行', moduleUrl: '' },
          ],
        },
      },
    }),
  )
  await page.route('**/platform/approve/list/new/page*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 5 } } }),
  )
  await page.route('**/platform/approve/list/repairs/list*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0 } } }),
  )
  await page.route('**/platform/dor/quit/list/approval*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 120 } } }),
  )
}

test('首页：员工条/天气/公告/角标/服务宫格', async ({ page }) => {
  await seedLogin(page)
  await mockHomeApis(page)

  await page.goto('/home')
  await expect(page.getByText('YT20180326')).toBeVisible()
  await expect(page.getByText('王建国')).toBeVisible()
  await expect(page.getByText('许昌 多云 27°C 东南风')).toBeVisible()
  await expect(page.getByText('关于五一假期园区门禁开放时间调整的通知')).toBeVisible()
  // 角标：5 显示、0 隐藏、120 显示 99+
  await expect(page.getByText('5', { exact: true })).toBeVisible()
  await expect(page.getByText('99+', { exact: true })).toBeVisible()
  await expect(page.getByText('园区报修', { exact: true })).toBeVisible()
})

test('首页：未登录跳转微信 OAuth', async ({ page }) => {
  await page.route('https://open.weixin.qq.com/**', (route) =>
    route.fulfill({ contentType: 'text/html', body: '<title>wechat-oauth-stub</title>' }),
  )
  await page.goto('/home')
  await page.waitForURL('https://open.weixin.qq.com/**')
})

test('首页：已离职弹窗 → 清 token 并重新 OAuth 到绑定页', async ({ page }) => {
  await seedLogin(page)
  await mockHomeApis(page)
  await page.route('**/app/employee/baseinfo', (route) =>
    route.fulfill({ json: { code: 0, data: { employeeName: '王建国', status: 0 } } }),
  )
  await page.route('https://open.weixin.qq.com/**', (route) =>
    route.fulfill({ contentType: 'text/html', body: '<title>wechat-oauth-stub</title>' }),
  )

  await page.goto('/home')
  await expect(page.getByText('该用户已离职，已为你自动退出登录')).toBeVisible()
  await page.getByRole('button', { name: '确定' }).click()
  await page.waitForURL('https://open.weixin.qq.com/**')
  const url = new URL(page.url())
  expect(decodeURIComponent(url.searchParams.get('redirect_uri') ?? '')).toContain('/login/badge')
})

test('公告列表 → 富文本详情', async ({ page }) => {
  await seedLogin(page)
  await mockHomeApis(page)
  await page.route('**/app/home/bbs/detail/11', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: {
          bbsTitle: '关于五一假期园区门禁开放时间调整的通知',
          bbsContent: '<p>五一期间访客通道开放时间调整为 8:00–18:00。</p>',
        },
      },
    }),
  )

  await page.goto('/home/bbs')
  await page.getByText('关于五一假期园区门禁开放时间调整的通知').click()
  await page.waitForURL('**/home/bbs/11?isPdf=false')
  await expect(page.getByText('五一期间访客通道开放时间调整为 8:00–18:00。')).toBeVisible()
})

test('首页：401 invalid_token 自动重走微信授权', async ({ page }) => {
  await seedLogin(page)
  await mockHomeApis(page)
  await page.route('**/app/employee/fullinfo', (route) =>
    route.fulfill({ json: { code: 401, msg: 'invalid_token' } }),
  )
  await page.route('https://open.weixin.qq.com/**', (route) =>
    route.fulfill({ contentType: 'text/html', body: '<title>wechat-oauth-stub</title>' }),
  )

  await page.goto('/home')
  await page.waitForURL('https://open.weixin.qq.com/**')
  // The page now sits on the WeChat origin; read the app origin's storage.
  const state = await page.context().storageState()
  const appOrigin = state.origins.find((o) => o.origin.includes('localhost:3100'))
  const stored = appOrigin?.localStorage.find((e) => e.name === 'xc-access_token')
  expect(JSON.parse(stored?.value ?? '{}').content).toBe('invalid_token')
})
