import { expect, test, type Page } from '@playwright/test'

/** E2E 默认关闭 mock：拦截 config.js 并覆盖 visitorRecordsMock。 */
async function disableMock(page: Page) {
  await page.route('**/config.js', (route) =>
    route.fulfill({
      contentType: 'application/javascript',
      body: `window.__SMART_CONFIG__ = {
        tenant: 'xuchang', parkId: 5000021, parkName: '裕同科技许昌园区',
        parkAddress: '许昌数字经济产业园', weatherCity: '许昌',
        wxAppId: 'wx5c0d26056102d41e', flows: { visitor: 'standard' },
        features: { visitorRecordsMock: false },
      }`,
    }),
  )
}

const LIST_RESULT = {
  queryToken: 'tok-e2e',
  maskedName: '李明',
  maskedMobile: '137****1234',
  records: [
    {
      applyId: 'a-pending',
      parkName: '裕同科技许昌园区',
      applyStatus: 'PENDING',
      receptionistName: '王强',
      startTime: '2026-06-12 09:30',
      endTime: '2026-06-12 18:00',
      fellowCount: 2,
      plates: ['豫A·D88E6'],
      currentNode: '部门负责人 张三 审批中',
      submitTime: '2026-06-10 15:08',
    },
    {
      applyId: 'a-passed',
      parkName: '裕同科技许昌园区',
      applyStatus: 'PASSED',
      receptionistName: '刘洋',
      startTime: '2026-06-05 14:00',
      endTime: '2026-06-05 17:30',
      fellowCount: 0,
      plates: [],
      dispatchStatus: 'SUCCESS',
      submitTime: '2026-06-04 10:21',
    },
    {
      applyId: 'a-rejected',
      parkName: '裕同科技许昌园区',
      applyStatus: 'REJECTED',
      receptionistName: '王强',
      startTime: '2026-06-08 10:00',
      endTime: '2026-06-08 12:00',
      fellowCount: 0,
      plates: [],
      submitTime: '2026-06-07 18:12',
    },
  ],
}

async function mockListApis(page: Page) {
  await page.route('**/app/sms/visitor/send', async (route) => {
    expect(route.request().method()).toBe('POST')
    expect(route.request().postDataJSON()).toEqual({ mobile: '13712341234' })
    await route.fulfill({ json: { code: 0 } })
  })
  await page.route('**/platform/admittance/apply/app/listMyApply', (route) =>
    route.fulfill({ json: { code: 0, data: LIST_RESULT } }),
  )
}

async function seedQuerySession(
  page: Page,
  session = { queryToken: 'tok-e2e', maskedName: '李明', maskedMobile: '137****1234' },
) {
  await page.addInitScript((value) => {
    localStorage.setItem('visitor-query-session', JSON.stringify({ ...value, savedAt: Date.now() }))
  }, session)
}

/** Walks the verify phase to the record list. */
async function verifyToList(page: Page) {
  await page.goto('/visitor/records')
  await expect(page.getByText('验证身份后查看记录')).toBeVisible()
  await page.getByPlaceholder('点击输入手机号').fill('13712341234')
  await page.getByRole('button', { name: '获取验证码' }).click()
  await expect(page.getByText('发送成功')).toBeVisible()
  await page.getByPlaceholder('点击输入验证码').fill('123456')
  await page.getByRole('button', { name: '查看申请记录' }).click()
  await expect(page.getByText('当前查询：')).toBeVisible()
}

async function expectEmptyStateCentered(page: Page, label: string) {
  const emptyState = page.getByRole('status', { name: label })
  await expect(emptyState).toBeVisible()

  const metrics = await emptyState.evaluate((node) => {
    const visual = node.querySelector('.adm-error-block-image svg, .adm-error-block-image img')
    const title = node.querySelector('.adm-error-block-description-title')
    const stateBox = node.getBoundingClientRect()
    const visualBox = visual?.getBoundingClientRect()
    const titleBox = title?.getBoundingClientRect()

    if (!visualBox || !titleBox) return null

    return {
      stateCenter: stateBox.left + stateBox.width / 2,
      visualCenter: visualBox.left + visualBox.width / 2,
      titleCenter: titleBox.left + titleBox.width / 2,
    }
  })

  expect(metrics).not.toBeNull()
  expect(Math.abs(metrics!.visualCenter - metrics!.stateCenter)).toBeLessThanOrEqual(1)
  expect(Math.abs(metrics!.visualCenter - metrics!.titleCenter)).toBeLessThanOrEqual(1)
}

test('记录列表：验证 → 列表渲染 → 筛选 → 换个手机号', async ({ page }) => {
  await disableMock(page)
  await mockListApis(page)
  let listBody: Record<string, unknown> | undefined
  await page.route('**/platform/admittance/apply/app/listMyApply', async (route) => {
    listBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, data: LIST_RESULT } })
  })

  await verifyToList(page)
  expect(listBody).toEqual({ mobile: '13712341234', smsCode: '123456' })

  // 列表内容
  await expect(page.getByText('李明 137****1234')).toBeVisible()
  await expect(page.getByText('当前节点：')).toBeVisible()
  await expect(page.getByText('部门负责人 张三 审批中')).toBeVisible()
  await expect(page.getByText('已下发成功')).toBeVisible()
  await expect(page.getByText('随行 2 人 · 豫A·D88E6')).toBeVisible()

  // 筛选（卡片数量断言，避免空 locator 恒真）
  const cards = page.locator('button:has-text("查看")')
  await expect(cards).toHaveCount(3)
  await page.getByRole('tab', { name: '已通过' }).click()
  await expect(cards).toHaveCount(1)
  await expect(page.getByText('刘洋')).toBeVisible()
  await page.getByRole('tab', { name: '已过期' }).click()
  await expectEmptyStateCentered(page, '暂无申请记录')
  await page.getByRole('tab', { name: '全部' }).click()
  await expect(cards).toHaveCount(3)

  // 换个手机号 → 回验证态并清 session
  await page.getByRole('button', { name: '换个手机号' }).click()
  await expect(page.getByText('验证身份后查看记录')).toBeVisible()
  const session = await page.evaluate(() => localStorage.getItem('visitor-query-session'))
  expect(session).toBeNull()
})

test('记录列表：重进页面用 token 头免验刷新', async ({ page }) => {
  await disableMock(page)
  let refreshHeaders: Record<string, string> | undefined
  await page.route('**/platform/admittance/apply/app/listMyApply', async (route) => {
    refreshHeaders = route.request().headers()
    await route.fulfill({ json: { code: 0, data: LIST_RESULT } })
  })
  await seedQuerySession(page, { queryToken: 'tok-saved', maskedName: '李明', maskedMobile: '137****1234' })

  await page.goto('/visitor/records')
  await expect(page.getByText('当前查询：')).toBeVisible()
  expect(refreshHeaders?.['x-visitor-query-token']).toBe('tok-saved')
})

test('记录列表：token 失效（403）回验证态', async ({ page }) => {
  await disableMock(page)
  await page.route('**/platform/admittance/apply/app/listMyApply', (route) =>
    route.fulfill({ status: 403, json: { code: 403, message: 'token expired' } }),
  )
  await seedQuerySession(page, { queryToken: 'tok-dead', maskedName: '李明', maskedMobile: '137****1234' })

  await page.goto('/visitor/records')
  await expect(page.getByText('验证身份后查看记录')).toBeVisible()
  const session = await page.evaluate(() => localStorage.getItem('visitor-query-session'))
  expect(session).toBeNull()
})

test('mock 开关冒烟：显式开启后列表走 fixture，但短信仍真实发送', async ({ page }) => {
  await page.route('**/config.js', (route) =>
    route.fulfill({
      contentType: 'application/javascript',
      body: `window.__SMART_CONFIG__ = {
        tenant: 'xuchang', parkId: 5000021, parkName: '裕同科技许昌园区',
        parkAddress: '许昌数字经济产业园', weatherCity: '许昌',
        wxAppId: 'wx5c0d26056102d41e', flows: { visitor: 'standard' },
        features: { visitorRecordsMock: true },
      }`,
    }),
  )
  let smsRequestCount = 0
  await page.route('**/app/sms/visitor/send', async (route) => {
    smsRequestCount += 1
    expect(route.request().method()).toBe('POST')
    expect(route.request().postDataJSON()).toEqual({ mobile: '13700000000' })
    await route.fulfill({ json: { code: 0 } })
  })

  await page.goto('/visitor/records')
  await expect(page.getByText('验证身份后查看记录')).toBeVisible()
  await page.getByPlaceholder('点击输入手机号').fill('13700000000')
  await page.getByRole('button', { name: '获取验证码' }).click()
  await expect.poll(() => smsRequestCount).toBe(1)
  await page.getByPlaceholder('点击输入验证码').fill('888888')
  await page.getByRole('button', { name: '查看申请记录' }).click()
  await expect(page.getByText('当前查询：')).toBeVisible()
  await expect(page.getByText('李明 137****1234')).toBeVisible()
  await expect(page.getByText('部门负责人 张三 审批中')).toBeVisible()
  await expect(page.getByText('审批中').first()).toBeVisible()
})

const DETAIL_BASE = {
  applyId: 'a-1',
  applyNo: 'VA20260610-0027',
  parkName: '裕同科技许昌园区',
  receptionistName: '王强',
  startTime: '2026-06-12 09:30',
  endTime: '2026-06-12 18:00',
  cause: '供应商打样确认',
  visitorName: '李明',
  visitorPhone: '137****1234',
  fellows: [{ name: '赵六', phone: '150****8821' }],
  vehicles: [{ plate: '豫A·D88E6', type: '小型客车' }],
  areas: ['研发楼 A 座', '样品展示厅'],
  submitTime: '2026-06-10 15:08',
}
const DONE = { title: '被访人审批', state: 'done', approverName: '王强', time: '2026-06-10 16:55' }

async function seedSession(page: Page) {
  await seedQuerySession(page)
}

async function mockDetail(page: Page, detail: Record<string, unknown>, nodes: unknown[]) {
  let detailHeaders: Record<string, string> | undefined
  await page.route('**/platform/admittance/apply/app/applyDetail*', async (route) => {
    detailHeaders = route.request().headers()
    await route.fulfill({ json: { code: 0, data: detail } })
  })
  await page.route('**/platform/admittance/apply/app/approvalProgress*', (route) =>
    route.fulfill({ json: { code: 0, data: { nodes } } }),
  )
  return () => detailHeaders
}

test('详情：审批中态渲染 + token 头', async ({ page }) => {
  await disableMock(page)
  await seedSession(page)
  const headers = await mockDetail(page, { ...DETAIL_BASE, applyStatus: 'PENDING' }, [
    DONE,
    { title: '部门负责人审批', state: 'current', approverName: '张三' },
  ])

  await page.goto('/visitor/records/a-1')
  await expect(page.getByText('审批中', { exact: true })).toBeVisible()
  await expect(page.getByText(/当前停留在 部门负责人审批 张三/)).toBeVisible()
  await expect(page.getByText('等待其审批中')).toBeVisible()
  await expect(page.getByText('VA20260610-0027')).toBeVisible()
  await expect(page.getByText('赵六 150****8821')).toBeVisible()
  await expect(page.getByRole('button', { name: '查看入园通行码' })).not.toBeVisible()
  expect(headers()?.['x-visitor-query-token']).toBe('tok-e2e')
})

test('详情：通过·下发成功 → 通行码跳转', async ({ page }) => {
  await disableMock(page)
  await seedSession(page)
  await mockDetail(page, { ...DETAIL_BASE, applyStatus: 'PASSED', dispatchStatus: 'SUCCESS' }, [DONE])
  await page.route('**/platform/admittance/apply/search/Detail/*', (route) =>
    route.fulfill({ json: { code: 0, data: { delFlag: 0, qrCode: 'aGk=', smsCode: '668866', parkName: '裕同科技许昌园区' } } }),
  )
  await page.route('**/platform/admittance/apply/enum/factory/type*', (route) =>
    route.fulfill({ json: { code: 0, data: [] } }),
  )

  await page.goto('/visitor/records/a-1')
  await expect(page.getByText('审批已通过 · 权限已下发')).toBeVisible()
  await page.getByRole('button', { name: '查看入园通行码' }).click()
  await page.waitForURL('**/visitor/code?id=a-1')
})

test('详情：已拒绝 → 拒绝意见 + 重新预约预填', async ({ page }) => {
  await disableMock(page)
  await seedSession(page)
  await mockDetail(page, { ...DETAIL_BASE, applyStatus: 'REJECTED' }, [
    { title: '被访人审批', state: 'rejected', approverName: '王强', time: '2026-06-10 18:31', comment: '当日园区有接待安排，请改约下周' },
  ])
  await page.route('https://open.weixin.qq.com/**', (route) =>
    route.fulfill({ contentType: 'text/html', body: 'stub' }),
  )

  await page.goto('/visitor/records/a-1')
  await expect(page.getByText('审批未通过')).toBeVisible()
  await expect(page.getByText('当日园区有接待安排，请改约下周')).toBeVisible()
  await page.getByRole('button', { name: '修改信息重新预约' }).click()
  // 详情载荷是展示值，重新预约从干净草稿开始，避免把格式化数据当成可提交草稿。
  const draft = await page.evaluate(() => {
    const raw = localStorage.getItem('visitor-flow')
    return raw ? JSON.parse(raw) : null
  })
  expect(draft?.state?.host?.receptionistName ?? '').toBe('')
})

test('详情：通过·下发中态与已过期态', async ({ page }) => {
  await disableMock(page)
  await seedSession(page)
  await mockDetail(page, { ...DETAIL_BASE, applyStatus: 'PASSED', dispatchStatus: 'ISSUING' }, [DONE])
  await page.goto('/visitor/records/a-1')
  await expect(page.getByText('审批已通过 · 权限下发中')).toBeVisible()
  await expect(page.getByRole('button', { name: '查看入园通行码' })).toBeVisible()

  await page.unroute('**/platform/admittance/apply/app/applyDetail*')
  await page.route('**/platform/admittance/apply/app/applyDetail*', (route) =>
    route.fulfill({ json: { code: 0, data: { ...DETAIL_BASE, applyStatus: 'EXPIRED' } } }),
  )
  await page.goto('/visitor/records/a-1')
  await expect(page.getByText('申请已过期')).toBeVisible()
  await expect(page.getByRole('button', { name: '再次预约' })).toBeVisible()
})

test('详情：403 → 清 token 回验证并带回跳参数', async ({ page }) => {
  await disableMock(page)
  await seedSession(page)
  await page.route('**/platform/admittance/apply/app/applyDetail*', (route) =>
    route.fulfill({ status: 403, json: { code: 403, message: 'token expired' } }),
  )
  await page.route('**/platform/admittance/apply/app/approvalProgress*', (route) =>
    route.fulfill({ status: 403, json: { code: 403 } }),
  )

  await page.goto('/visitor/records/a-1')
  await page.waitForURL('**/visitor/records?redirect=a-1')
  await expect(page.getByText('验证身份后查看记录')).toBeVisible()
  const session = await page.evaluate(() => localStorage.getItem('visitor-query-session'))
  expect(session).toBeNull()
})

test('详情：下发失败态无通行码按钮', async ({ page }) => {
  await disableMock(page)
  await seedSession(page)
  await mockDetail(page, { ...DETAIL_BASE, applyStatus: 'PASSED', dispatchStatus: 'FAILED' }, [DONE])

  await page.goto('/visitor/records/a-1')
  await expect(page.getByText('审批已通过 · 权限下发失败')).toBeVisible()
  await expect(page.getByText(/请联系被访人 王强/)).toBeVisible()
  await expect(page.getByRole('button', { name: '查看入园通行码' })).not.toBeVisible()
})

test('详情：深链无 session → 验证后回跳', async ({ page }) => {
  await disableMock(page)
  await mockListApis(page)
  await mockDetail(page, { ...DETAIL_BASE, applyStatus: 'PENDING' }, [DONE])

  await page.goto('/visitor/records/a-1')
  await page.waitForURL('**/visitor/records?redirect=a-1')
  await page.getByPlaceholder('点击输入手机号').fill('13712341234')
  await page.getByPlaceholder('点击输入验证码').fill('123456')
  await page.getByRole('button', { name: '查看申请记录' }).click()
  await page.waitForURL('**/visitor/records/a-1')
  await expect(page.getByText('VA20260610-0027')).toBeVisible()
})

test('入口：visitor 首页与 result 页跳转记录页', async ({ page }) => {
  // result 页入口
  await page.goto('/visitor/result')
  await page.getByRole('button', { name: '查看审批进度' }).click()
  await page.waitForURL('**/visitor/records')

  // visitor 首页入口（带 code 避免 OAuth 跳转，mock 入口接口）
  await page.route('**/platform/common/config/admittance/notice*', (route) =>
    route.fulfill({ json: { code: 0, data: { isNeedNotice: 0 } } }),
  )
  await page.route('**/platform/admittance/apply/get/openId*', (route) =>
    route.fulfill({ json: { code: 0, data: { openId: 'oid-1' } } }),
  )
  await page.goto('/visitor?code=visitor-code')
  await page.getByText('查看申请记录与审批进度').click()
  await page.waitForURL('**/visitor/records')
})

test('详情：二次 403 不再循环重定向，展示无权查看', async ({ page }) => {
  await disableMock(page)
  await seedSession(page)
  await page.addInitScript(() => sessionStorage.setItem('visitor-record-denied-a-1', '1'))
  await page.route('**/platform/admittance/apply/app/applyDetail*', (route) =>
    route.fulfill({ status: 403, json: { code: 403 } }),
  )
  await page.route('**/platform/admittance/apply/app/approvalProgress*', (route) =>
    route.fulfill({ status: 403, json: { code: 403 } }),
  )

  await page.goto('/visitor/records/a-1')
  await expect(page.getByText('无权查看该申请')).toBeVisible()
  await expect(page).toHaveURL(/\/visitor\/records\/a-1/)
})
