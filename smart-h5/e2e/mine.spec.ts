import { expect, test, type Page } from '@playwright/test'

async function seedLogin(page: Page) {
  await page.addInitScript(() => {
    localStorage.setItem(
      'xc-access_token',
      JSON.stringify({ dataType: 'string', content: 'tok-e2e', datetime: Date.now() }),
    )
  })
}

const BASE_INFO = {
  employeeName: '王建国',
  employeeBadge: 'YT20180326',
  employeeSex: 0,
  mobile: '13837415260',
  buName: '裕同科技',
  deptName: '智能制造一部',
  jobName: '设备工程师',
  statusDes: '在职',
  status: 1,
}

test('个人中心：信息展示与导航', async ({ page }) => {
  await seedLogin(page)
  await page.route('**/app/employee/baseinfo', (route) =>
    route.fulfill({ json: { code: 0, data: BASE_INFO } }),
  )

  await page.goto('/mine')
  await expect(page.getByText('王建国')).toBeVisible()
  await expect(page.getByText('13837415260')).toBeVisible()
  await expect(page.getByText('裕同科技许昌园区')).toBeVisible()
  await expect(page.getByText('智能制造一部')).toBeVisible()
  await expect(page.getByText('设备工程师')).toBeVisible()
  await expect(page.getByText('在职')).toBeVisible()

  // 头部 → 个人信息页
  await page.getByText('王建国').click()
  await page.waitForURL('**/mine/detail')
  await expect(page.getByText('YT20180326')).toBeVisible()
  await expect(page.getByText('福利层次')).toBeVisible()
  // 缺省字段显示 -
  await expect(page.getByText('-').first()).toBeVisible()
})

test('个人中心：解绑确认 → 清 token → OAuth 回绑定页', async ({ page }) => {
  await seedLogin(page)
  await page.route('**/app/employee/baseinfo', (route) =>
    route.fulfill({ json: { code: 0, data: BASE_INFO } }),
  )
  await page.route('**/app/wechat/xc/unbind', (route) =>
    route.fulfill({ json: { code: 0 } }),
  )
  await page.route('https://open.weixin.qq.com/**', (route) =>
    route.fulfill({ contentType: 'text/html', body: '<title>wechat-oauth-stub</title>' }),
  )

  await page.goto('/mine')
  await page.getByText('微信解绑').click()
  await expect(page.getByText('是否确认解除微信绑定？')).toBeVisible()
  await page.getByRole('button', { name: '确定' }).click()

  await page.waitForURL('https://open.weixin.qq.com/**')
  const url = new URL(page.url())
  expect(decodeURIComponent(url.searchParams.get('redirect_uri') ?? '')).toContain('/login/badge')

  const state = await page.context().storageState()
  const appOrigin = state.origins.find((o) => o.origin.includes('localhost:3100'))
  const stored = appOrigin?.localStorage.find((e) => e.name === 'xc-access_token')
  expect(stored).toBeUndefined()
})

test('个人中心：解绑失败 toast 后端 message', async ({ page }) => {
  await seedLogin(page)
  await page.route('**/app/employee/baseinfo', (route) =>
    route.fulfill({ json: { code: 0, data: BASE_INFO } }),
  )
  await page.route('**/app/wechat/xc/unbind', (route) =>
    route.fulfill({ json: { code: 1, message: '解绑失败，请稍后再试' } }),
  )

  await page.goto('/mine')
  await page.getByText('微信解绑').click()
  await page.getByRole('button', { name: '确定' }).click()
  await expect(page.getByText('解绑失败，请稍后再试')).toBeVisible()
})
