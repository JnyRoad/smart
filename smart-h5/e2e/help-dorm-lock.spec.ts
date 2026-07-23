import { expect, test, type Page } from '@playwright/test'

/** Seed a valid login token (legacy xc- envelope). */
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

async function mockFrontCamera(page: Page) {
  await page.addInitScript(() => {
    Object.defineProperty(navigator, 'mediaDevices', {
      configurable: true,
      value: {
        getUserMedia: async () => new MediaStream(),
      },
    })
    Object.defineProperty(HTMLVideoElement.prototype, 'play', {
      configurable: true,
      value: async function () {
        Object.defineProperty(this, 'videoWidth', { configurable: true, get: () => 320 })
        Object.defineProperty(this, 'videoHeight', { configurable: true, get: () => 240 })
      },
    })
    Object.defineProperty(HTMLCanvasElement.prototype, 'getContext', {
      configurable: true,
      value: () => ({ drawImage() {} }),
    })
    Object.defineProperty(HTMLCanvasElement.prototype, 'toDataURL', {
      configurable: true,
      value: () => 'data:image/jpeg;base64,camera-raw-base64',
    })
  })
}

const QUESTIONS = [
  { questionId: 1, questionTitle: '如何申请宿舍？' },
  { questionId: 2, questionTitle: '门禁卡丢失怎么办？' },
]

test('帮助中心：列表 → 详情富文本', async ({ page }) => {
  await seedLogin(page)
  await page.route('**/app/guide/help/question/list*', async (route) => {
    const url = new URL(route.request().url())
    expect(url.searchParams.get('size')).toBe('10')
    await route.fulfill({ json: { code: 0, data: { records: QUESTIONS, pages: 1 } } })
  })
  await page.route('**/app/guide/help/question/answer/1', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: { questionTitle: '如何申请宿舍？', answerContent: '<p>登录后进入宿舍申请页面提交即可。</p>' },
      },
    }),
  )

  await page.goto('/help')
  await expect(page.getByText('如何申请宿舍？')).toBeVisible()
  await expect(page.getByText('门禁卡丢失怎么办？')).toBeVisible()
  await page.getByText('如何申请宿舍？').click()
  await page.waitForURL('**/help/1')
  await expect(page.getByText('登录后进入宿舍申请页面提交即可。')).toBeVisible()
})

test('帮助中心：空态', async ({ page }) => {
  await seedLogin(page)
  await page.route('**/app/guide/help/question/list*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )
  await page.goto('/help')
  await expect(page.getByText('暂无帮助内容')).toBeVisible()
})

test('死链闭合：mine 菜单「帮助中心」「我的宿舍」落到真实页面', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/app/guide/help/question/list*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: QUESTIONS, pages: 1 } } }),
  )

  await page.goto('/mine')
  await page.getByText('帮助中心').click()
  await page.waitForURL('**/help')
  await expect(page.getByText('如何申请宿舍？')).toBeVisible()

  await page.goto('/mine')
  await page.getByText('我的宿舍').click()
  await page.waitForURL('**/dorm')
  await expect(page.getByRole('button', { name: /门锁动态码 智能门锁/ })).toBeVisible()
  await expect(page.getByRole('button', { name: /水电扣费明细/ })).toBeVisible()
})

const STATEMENT = {
  staffName: '王建国',
  staffBadge: 'YT20180326',
  statementDate: '2026-06-01',
  meterMonth: '2026-05',
  cateInfos: [
    { cateName: '热水', fee: 5 },
    { cateName: '冷水', fee: 12 },
    { cateName: '电', fee: 36 },
  ],
  totalFee: 48,
}

test('水电明细：默认当月 → 查询全部 → 展示规则', async ({ page }) => {
  await seedLogin(page)
  const months: (string | null)[] = []
  await page.route('**/platform/dormitory/staff/statementdetail/record*', async (route) => {
    const url = new URL(route.request().url())
    months.push(url.searchParams.get('statementMonth'))
    await route.fulfill({ json: { code: 0, data: { records: [STATEMENT], pages: 1 } } })
  })

  await page.goto('/dorm/water-elec')
  await expect(page.getByText('王建国-YT20180326')).toBeVisible()
  // 默认当月（YYYY-MM）
  expect(months[0]).toMatch(/^\d{4}-\d{2}$/)
  // 展示规则：热水被过滤、冷水改名「水」
  await expect(page.getByText('房间水费')).toBeVisible()
  await expect(page.getByText('房间电费')).toBeVisible()
  await expect(page.getByText('热水')).not.toBeVisible()
  await expect(page.getByText('¥48')).toBeVisible()

  // 查询全部：statementMonth 传空 + 显示「未选择」
  await page.getByRole('button', { name: '查询全部' }).click()
  await expect(page.getByText('未选择 ▾')).toBeVisible()
  await expect(page.getByText('王建国-YT20180326')).toBeVisible()
  expect(months[months.length - 1]).toBe('')
})

test('水电明细：空态', async ({ page }) => {
  await seedLogin(page)
  await page.route('**/platform/dormitory/staff/statementdetail/record*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )
  await page.goto('/dorm/water-elec')
  await expect(page.getByText('暂无数据')).toBeVisible()
})

// ===== 门锁（注入测试 key，用旧算法现场生成真密文） =====
import { injectTestKey, legacyCipherHex } from './helpers'

test('门锁：密文解密展示 + 修改三连', async ({ page }) => {
  await seedLogin(page)
  await injectTestKey(page)
  await mockBaseInfo(page)
  let pwdCipher = legacyCipherHex('123456')
  await page.route('**/platform/dormitory/staff/get/pwd*', (route) =>
    route.fulfill({ json: { code: 0, data: pwdCipher } }),
  )
  let updateBody: Record<string, unknown> | undefined
  await page.route('**/platform/dormitory/staff/update/lock/pwd', async (route) => {
    updateBody = route.request().postDataJSON() as Record<string, unknown>
    pwdCipher = legacyCipherHex('654321')
    await route.fulfill({ json: { code: 0 } })
  })

  await page.goto('/dorm/lock')
  await expect(page.getByTestId('lock-code')).toHaveText('123456')

  await page.getByRole('button', { name: '修改动态码' }).click()
  const input = page.getByPlaceholder('请输入6位数字动态码')
  await input.fill('12345')
  await page.getByRole('button', { name: '确定' }).click()
  await expect(page.getByText('请输入6位数字动态码')).toBeVisible()

  await input.fill('123456')
  await page.getByRole('button', { name: '确定' }).click()
  await expect(page.getByText('请输入跟当前动态码不一样的新的动态码')).toBeVisible()

  await input.fill('654321')
  await page.getByRole('button', { name: '确定' }).click()
  await expect(page.getByTestId('lock-code')).toHaveText('654321')
  expect(updateBody).toEqual({ badge: 'YT20180326', newPwd: '654321' })
})

test('门锁：未入住 alert 回跳 /dorm', async ({ page }) => {
  await seedLogin(page)
  await injectTestKey(page)
  await mockBaseInfo(page)
  await page.route('**/platform/dormitory/staff/get/pwd*', (route) =>
    route.fulfill({ json: { code: 0, data: '' } }),
  )

  await page.goto('/dorm/lock')
  await expect(page.getByText('您暂未入住智能宿舍，请联系宿管入住！')).toBeVisible()
  await page.getByRole('button', { name: '确定' }).click()
  await page.waitForURL('**/dorm')
})

test('刷新动态码：人脸比对 → 生成 → 回门锁页', async ({ page }) => {
  await seedLogin(page)
  await injectTestKey(page)
  await mockBaseInfo(page)
  await mockFrontCamera(page)
  let cutBody: Record<string, unknown> | undefined
  let checkFaceBody: Record<string, unknown> | undefined
  await page.route('**/app/employee/face/crop', async (route) => {
    cutBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, message: 'success', data: 'cut-base64' } })
  })
  // 已登录门锁不再复用匿名访客上传路径；仍需复用上传后的 base64 刷新动态码。
  await page.route('**/app/employee/photo/upload', async (route) => {
    checkFaceBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({
      json: { code: 0, message: 'success', data: { photoId: 'p-lock' } },
    })
  })
  // 哨兵 value 不能被当成 photoId 去回查网关图片（坏图回归防护）。
  let brokenImg = false
  await page.route('**/platform/image/view/**', (route) => {
    brokenImg = true
    return route.fulfill({ status: 404, body: '' })
  })
  let refreshBody: Record<string, unknown> | undefined
  await page.route('**/platform/dormitory/staff/update/pwd', async (route) => {
    refreshBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0 } })
  })
  await page.route('**/platform/dormitory/staff/get/pwd*', (route) =>
    route.fulfill({ json: { code: 0, data: legacyCipherHex('888888') } }),
  )

  await page.goto('/dorm/get-code')
  await expect(page.locator('input[type="file"]')).toHaveCount(0)
  await page.getByRole('button', { name: '打开摄像头' }).click()
  await page.getByRole('button', { name: '拍照识别' }).click()
  await expect(page.getByText('人脸对比成功')).toBeVisible()
  expect(cutBody).toEqual({ imageData: 'camera-raw-base64' })
  expect(checkFaceBody).toEqual({ visitorPhoto: 'cut-base64' })
  expect(brokenImg).toBe(false)
  await page.getByRole('button', { name: '生成动态码' }).click()
  await expect(page.getByText('刷新动态码成功！')).toBeVisible()
  expect(refreshBody).toEqual({ badge: 'YT20180326', facePic: 'cut-base64' })
  await page.getByRole('button', { name: '确定' }).click()
  await page.waitForURL('**/dorm/lock')
  await expect(page.getByTestId('lock-code')).toHaveText('888888')
})

test('帮助中心：上拉加载第二页（追加不重复）', async ({ page }) => {
  await seedLogin(page)
  await page.route('**/app/guide/help/question/list*', async (route) => {
    const url = new URL(route.request().url())
    const current = url.searchParams.get('current')
    const records =
      current === '1'
        ? Array.from({ length: 10 }, (_, i) => ({ questionId: i + 1, questionTitle: `问题${i + 1}` }))
        : [{ questionId: 11, questionTitle: '问题11' }]
    await route.fulfill({ json: { code: 0, data: { records, pages: 2 } } })
  })

  await page.goto('/help')
  await expect(page.getByText('问题1', { exact: true })).toBeVisible()
  // 触发上拉加载（滚动到底部）
  await page.mouse.wheel(0, 4000)
  await expect(page.getByText('问题11', { exact: true })).toBeVisible()
  await expect(page.locator('button', { hasText: /^问题/ })).toHaveCount(11)
})

test('门锁：修改失败 alert、解密失败 toast、接口业务失败重试块', async ({ page }) => {
  await seedLogin(page)
  await injectTestKey(page)
  await mockBaseInfo(page)
  // 1) 解密失败（非法 hex）→ ****** + toast
  await page.route('**/platform/dormitory/staff/get/pwd*', (route) =>
    route.fulfill({ json: { code: 0, data: 'not-valid-hex-!!' } }),
  )
  await page.goto('/dorm/lock')
  await expect(page.getByTestId('lock-code')).toHaveText('******')
  await expect(page.getByText('动态码解析失败')).toBeVisible()
  // 解密失败时修改按钮禁用
  await expect(page.getByRole('button', { name: '修改动态码' })).toBeDisabled()

  // 2) 接口业务失败 → 重试块（不显示伪装码）
  await page.unroute('**/platform/dormitory/staff/get/pwd*')
  await page.route('**/platform/dormitory/staff/get/pwd*', (route) =>
    route.fulfill({ json: { code: 1, msg: '动态码服务繁忙' } }),
  )
  await page.goto('/dorm/lock')
  await expect(page.getByText('动态码服务繁忙')).toBeVisible()
  await expect(page.getByRole('button', { name: '重试' })).toBeVisible()

  // 3) 修改失败 alert
  await page.unroute('**/platform/dormitory/staff/get/pwd*')
  await page.route('**/platform/dormitory/staff/get/pwd*', (route) =>
    route.fulfill({ json: { code: 0, data: legacyCipherHex('123456') } }),
  )
  await page.route('**/platform/dormitory/staff/update/lock/pwd', (route) =>
    route.fulfill({ json: { code: 1, msg: '动态码修改过于频繁' } }),
  )
  await page.goto('/dorm/lock')
  await page.getByRole('button', { name: '修改动态码' }).click()
  await page.getByPlaceholder('请输入6位数字动态码').fill('654321')
  await page.getByRole('button', { name: '确定' }).click()
  await expect(page.getByText('动态码修改过于频繁')).toBeVisible()
})
