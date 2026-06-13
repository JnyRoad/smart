import { expect, test } from '@playwright/test'

// E2E 跑在 dev server 上，/dev-login 可用（生产构建该页恒 404）。

test('dev-login：粘贴信封 JSON 注入 token 并进入主页', async ({ page }) => {
  await page.route('**/app/employee/baseinfo', (route) =>
    route.fulfill({
      json: { code: 0, data: { employeeName: '王建国', employeeBadge: 'YT20180326', status: 1 } },
    }),
  )
  await page.route('**/app/employee/fullinfo', (route) =>
    route.fulfill({ json: { code: 0, data: { employeeId: 'YT20180326', employeeName: '王建国' } } }),
  )
  await page.route('**/app/common/weather*', (route) => route.fulfill({ json: { code: 0, data: {} } }))
  await page.route('**/app/home/bbs/list*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0, records: [] } } }),
  )
  await page.route('**/app/service/module/list', (route) =>
    route.fulfill({ json: { code: 0, data: { serviceModule: [] } } }),
  )
  await page.route('**/platform/approve/**', (route) => route.fulfill({ json: { code: 0, data: { total: 0 } } }))
  await page.route('**/platform/dor/quit/list/approval*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0 } } }),
  )

  await page.goto('/dev-login')
  await expect(page.getByText('未登录')).toBeVisible()
  await page
    .getByTestId('dev-token-input')
    .fill('{"dataType":"string","content":"tok-dev-e2e","datetime":1718160000000}')
  await page.getByRole('button', { name: '注入并进入' }).click()
  await page.waitForURL('**/home')

  const stored = await page.evaluate(() => localStorage.getItem('xc-access_token'))
  expect(JSON.parse(stored ?? '{}').content).toBe('tok-dev-e2e')

  // 回到 dev-login 显示已注入，可一键清除
  await page.goto('/dev-login')
  await expect(page.getByText('已注入 token')).toBeVisible()
  await page.getByRole('button', { name: /清除当前 token/ }).click()
  await expect(page.getByText('未登录')).toBeVisible()
})

test('dev-login：空输入拦截', async ({ page }) => {
  await page.goto('/dev-login')
  await page.getByRole('button', { name: '注入并进入' }).click()
  await expect(page.getByText('请先粘贴 token')).toBeVisible()
  await expect(page).toHaveURL(/\/dev-login$/)
})
