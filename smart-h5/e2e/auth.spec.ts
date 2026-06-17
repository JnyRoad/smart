import { expect, test, type Page } from '@playwright/test'

const WECHAT_OAUTH = 'https://open.weixin.qq.com/connect/oauth2/authorize*'

/** Stub the external WeChat OAuth page so redirects can be observed. */
async function stubWechatOAuth(page: Page) {
  await page.route(WECHAT_OAUTH, (route) =>
    route.fulfill({ contentType: 'text/html', body: '<title>wechat-oauth-stub</title>' }),
  )
}

test('回调换 token 成功：已绑定员工直接进入主页（code 只兑换一次）', async ({ page }) => {
  let exchangeCalls = 0
  await page.route('**/auth/wx/public/token', async (route) => {
    exchangeCalls += 1
    expect(route.request().headers()['authorization']).toMatch(/^Basic /)
    expect(route.request().postDataJSON()).toEqual({ code: 'code-bound', type: 'F' })
    await route.fulfill({
      json: { access_token: 'tok-e2e', refresh_token: 'ref-e2e', expires_in: 3600 },
    })
  })

  await page.goto('/login/wechat/callback?code=code-bound')
  await page.waitForURL('**/home')

  const stored = await page.evaluate(() => localStorage.getItem('xc-access_token'))
  expect(JSON.parse(stored as string).content).toBe('tok-e2e')
  // WeChat OAuth codes are single-use; StrictMode double-effects must not re-exchange.
  expect(exchangeCalls).toBe(1)
})

test('回调返回员工状态异常：同样转绑定页', async ({ page }) => {
  await stubWechatOAuth(page)
  await page.route('**/auth/wx/public/token', (route) =>
    route.fulfill({ json: { code: 1, data: '员工状态异常' } }),
  )

  await page.goto('/login/wechat/callback?code=code-abnormal')
  await page.waitForURL(WECHAT_OAUTH)
  const url = new URL(page.url())
  expect(decodeURIComponent(url.searchParams.get('redirect_uri') ?? '')).toContain('/login/badge')
})

test('回调换 token 失败（HTTP 401 状态码 + invalid_token body）：重走 OAuth', async ({ page }) => {
  await stubWechatOAuth(page)
  await page.route('**/auth/wx/public/token', (route) =>
    route.fulfill({ status: 401, json: { code: 401, msg: 'invalid_token' } }),
  )

  await page.goto('/login/wechat/callback?code=code-expired')
  await page.waitForURL(WECHAT_OAUTH)
})

test('回调业务失败（非绑定类）：展示错误态与重新授权按钮', async ({ page }) => {
  await stubWechatOAuth(page)
  await page.route('**/auth/wx/public/token', (route) =>
    route.fulfill({ json: { code: 1, message: '系统繁忙，请稍后再试' } }),
  )

  await page.goto('/login/wechat/callback?code=code-busy')
  await expect(page.getByText('系统繁忙，请稍后再试')).toBeVisible()
  await page.getByRole('button', { name: '重新授权' }).click()
  await page.waitForURL(WECHAT_OAUTH)
})

test('回调缺少 code：展示错误态与重新授权按钮', async ({ page }) => {
  await stubWechatOAuth(page)
  await page.goto('/login/wechat/callback')
  await expect(page.getByText('缺少微信授权参数')).toBeVisible()
  await page.getByRole('button', { name: '重新授权' }).click()
  await page.waitForURL(WECHAT_OAUTH)
})

test('回调返回未绑定：重新发起 OAuth 跳绑定页', async ({ page }) => {
  await stubWechatOAuth(page)
  await page.route('**/auth/wx/public/token', (route) =>
    route.fulfill({ json: { code: 1, data: '账号未绑定工号，请先绑定' } }),
  )

  await page.goto('/login/wechat/callback?code=code-unbound')
  await page.waitForURL(WECHAT_OAUTH)

  const url = new URL(page.url())
  expect(url.searchParams.get('scope')).toBe('snsapi_base')
  expect(decodeURIComponent(url.searchParams.get('redirect_uri') ?? '')).toContain('/login/badge')
})

test('绑定字母工号成功：重新发起 OAuth 完成登录', async ({ page }) => {
  await stubWechatOAuth(page)
  await page.route('**/app/wechat/xc/banging/badge', async (route) => {
    expect(route.request().postDataJSON()).toEqual({
      parkId: 5000021,
      code: 'code-bind',
      badge: 'YT100200',
      lastCertNum: '123456',
    })
    await route.fulfill({ json: { code: 0, message: '绑定成功' } })
  })

  await page.goto('/login/badge?code=code-bind')
  await expect(page.getByPlaceholder('输入员工号')).not.toHaveAttribute('inputmode', /.+/)
  await page.getByPlaceholder('输入员工号').fill('YT100200')
  await page.getByPlaceholder('输入身份证后六位').fill('123456')
  await page.getByRole('button', { name: '绑定' }).click()

  await page.waitForURL(WECHAT_OAUTH)
  const url = new URL(page.url())
  expect(decodeURIComponent(url.searchParams.get('redirect_uri') ?? '')).toContain(
    '/login/wechat/callback',
  )
})

test('绑定失败：toast 后端 message', async ({ page }) => {
  await stubWechatOAuth(page)
  await page.route('**/app/wechat/xc/banging/badge', (route) =>
    route.fulfill({ json: { code: 1, message: '工号或身份证后六位不匹配' } }),
  )

  await page.goto('/login/badge?code=code-bad')
  await page.getByPlaceholder('输入员工号').fill('100200')
  await page.getByPlaceholder('输入身份证后六位').fill('000000')
  await page.getByRole('button', { name: '绑定' }).click()

  await expect(page.getByText('工号或身份证后六位不匹配')).toBeVisible()
})

test('登录页：验证码校验与协议拦截', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: '获取验证码' }).click()
  await expect(page.getByText('请输入手机号')).toBeVisible()

  await page.getByPlaceholder('点击输入手机号').fill('123')
  await page.getByRole('button', { name: '获取验证码' }).click()
  await expect(page.getByText('手机号格式不正确')).toBeVisible()
})

test('微信跳板页：立即重定向微信授权', async ({ page }) => {
  await stubWechatOAuth(page)
  await page.goto('/login/wechat')
  await page.waitForURL(WECHAT_OAUTH)
  const url = new URL(page.url())
  expect(url.searchParams.get('appid')).toBe('wx5c0d26056102d41e')
})
