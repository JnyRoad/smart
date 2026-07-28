import { expect, test, type Page } from '@playwright/test'

async function seedLogin(page: Page) {
  await page.addInitScript(() => {
    localStorage.setItem(
      'xc-access_token',
      JSON.stringify({ dataType: 'string', content: 'tok-e2e', datetime: Date.now() }),
    )
  })
}

/**
 * 当前 H5 只能请求本人路由，不能通过工号、身份证号或地址拼接员工查询。
 */
test('入住与门锁页面只调用当前认证用户路由', async ({ page }) => {
  await seedLogin(page)
  const requestedUrls: string[] = []
  page.on('request', (request) => requestedUrls.push(request.url()))

  await page.route('**/platform/staff/define/badge*', (route) => route.abort())
  await page.route('**/platform/dormitory/staff/get/pwd*badge=*', (route) => route.abort())
  await page.route('**/platform/staff/me/check-in-profile', (route) =>
    route.fulfill({ json: { code: 0, data: { name: '测试员工', profileComplete: true, maskedCertNo: '**************0000' } } }),
  )
  await page.route('**/platform/dormitory/queryDormitory', (route) =>
    route.fulfill({ json: { code: 0, data: [] } }),
  )
  await page.route('**/platform/dormitory/staff/me/pwd', (route) =>
    route.fulfill({ json: { code: 0, data: '' } }),
  )

  await page.goto('/check-in')
  await expect(page.getByText('宿舍申请')).toBeVisible()
  await page.goto('/dorm/lock')
  await expect(page.getByText('门锁动态码')).toBeVisible()

  expect(requestedUrls.some((url) => url.includes('/platform/staff/me/check-in-profile'))).toBeTruthy()
  expect(requestedUrls.some((url) => url.includes('/platform/dormitory/staff/me/pwd'))).toBeTruthy()
  expect(requestedUrls.some((url) => /\/staff\/define\/badge|\/staff\/get\/pwd.*badge=/.test(url))).toBeFalsy()
  expect(requestedUrls.some((url) => /certno|homeAddress|badge=/.test(url))).toBeFalsy()
})
