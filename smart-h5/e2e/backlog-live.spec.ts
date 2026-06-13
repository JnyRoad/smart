import { expect, test, type Page } from '@playwright/test'

async function seedLogin(page: Page) {
  await page.addInitScript(() => {
    localStorage.setItem(
      'xc-access_token',
      JSON.stringify({ dataType: 'string', content: 'tok-e2e', datetime: Date.now() }),
    )
  })
}

async function mockBaseInfo(page: Page, isSecurityGuard = 1) {
  await page.route('**/app/employee/baseinfo', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: { employeeName: '王建国', employeeBadge: 'YT20180326', status: 1, isSecurityGuard },
      },
    }),
  )
}

const LIVE_RECORDS = (state: 0 | 1) => ({
  code: 0,
  data: {
    records: [
      {
        approveId: 9,
        approveName: '王建国提交的物品放行',
        approveNodeDesc: '室友审批',
        approveDesc: state === 1 ? '已通过' : undefined,
        approveState: '1',
        sort: 1,
        articlesTypeDesc: '宿舍生活物品',
        articleName: '行李箱',
        carrier: '王建国',
        roomInfo: '新工厂宿舍楼302',
        createTime: '2026-06-12 09:00',
      },
      ...(state === 1
        ? [
            {
              approveId: 10,
              approveName: '李四提交的物品放行',
              approveDesc: '已拒绝',
              approveState: '2',
              sort: 1,
              articlesTypeDesc: '宿舍生活物品',
              articleName: '风扇',
              carrier: '李四',
              roomInfo: '新工厂宿舍楼303',
              createTime: '2026-06-11 09:00',
            },
          ]
        : []),
    ],
    pages: 1,
  },
})

const DETAIL_BASE = {
  id: 9,
  name: '王建国',
  carrier: '王建国',
  articlesTypeName: '宿舍生活物品',
  articlesDesc: '行李箱',
  dormitoryName: '新工厂宿舍楼',
  roomName: '302',
  plannedDepartureTime: '2026-06-20 10:00',
  approvalProcess: [
    { statusName: '室友审批', approvers: [{ name: '张**', result: 0 }] },
  ],
}

test('生活区审批：双 Tab 字段配色 + 保安搜索显隐与请求断言', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page, 0) // 保安
  const queries: URLSearchParams[] = []
  await page.route('**/platform/approve/list/new/page*', (route) => {
    const query = new URL(route.request().url()).searchParams
    queries.push(query)
    return route.fulfill({ json: LIVE_RECORDS(query.get('recordState') === '1' ? 1 : 0) })
  })

  await page.goto('/backlog/release-live')
  await expect(page.getByText('王建国提交的物品放行')).toBeVisible()
  await expect(page.getByText('室友审批', { exact: true })).toBeVisible()
  expect(queries[0]?.get('recordType')).toBe('3')
  expect(queries[0]?.get('recordState')).toBe('0')
  expect(queries[0]?.has('licensePlate')).toBe(false)

  // 我审批的：approveState 配色
  await page.getByRole('tab', { name: '我审批的', exact: true }).click()
  await expect(page.getByText('已通过')).toHaveClass(/text-\[#16a673\]/)
  await expect(page.getByText('已拒绝')).toHaveClass(/text-\[#d83b36\]/)

  // 保安可见搜索 → 条件重查
  await page.getByRole('button', { name: /搜 索/ }).click()
  await page.getByPlaceholder('工号').fill('YT9')
  await page.getByRole('button', { name: '确定' }).first().click()
  await expect.poll(() => queries.at(-1)?.get('badge')).toBe('YT9')
  expect(queries.at(-1)?.get('recordState')).toBe('1')
})

test('生活区审批：非保安不显示搜索按钮', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page, 1)
  await page.route('**/platform/approve/list/new/page*', (route) => route.fulfill({ json: LIVE_RECORDS(0) }))

  await page.goto('/backlog/release-live')
  await expect(page.getByText('王建国提交的物品放行')).toBeVisible()
  await expect(page.getByRole('button', { name: /搜 索/ })).not.toBeVisible()
})

test('生活区审批详情：sort=1 通过（GET 参数断言）与 tab=done 只读', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/platform/articlesrelease/detail/9', (route) =>
    route.fulfill({ json: { code: 0, data: { ...DETAIL_BASE, status: 1 } } }),
  )
  let updateQuery: URLSearchParams | undefined
  await page.route('**/platform/articlesrelease/status/update*', (route) => {
    updateQuery = new URL(route.request().url()).searchParams
    return route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/platform/approve/list/new/page*', (route) => route.fulfill({ json: LIVE_RECORDS(1) }))

  await page.goto('/backlog/release-live/detail?id=9&sort=1')
  await expect(page.getByText('行李箱')).toBeVisible()
  await page.getByPlaceholder('请输入内容').fill('同意携带')
  await page.getByRole('button', { name: '通 过' }).click()
  await page.waitForURL('**/backlog/release-live?tab=done')
  expect(updateQuery?.get('approveBadge')).toBe('YT20180326')
  expect(updateQuery?.get('id')).toBe('9')
  expect(updateQuery?.get('status')).toBe('2')
  expect(updateQuery?.get('remark')).toBe('同意携带')

  // tab=done 只读：无按钮、无意见输入
  await page.goto('/backlog/release-live/detail?id=9&sort=1&tab=done')
  await expect(page.getByText('行李箱')).toBeVisible()
  await expect(page.getByRole('button', { name: '通 过' })).not.toBeVisible()
  await expect(page.getByPlaceholder('请输入内容')).not.toBeVisible()
})

test('生活区审批详情：sort=3 保安需图拦截 → 传图确认放行（POST 断言）', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page, 0)
  await page.route('**/platform/articlesrelease/detail/9', (route) =>
    route.fulfill({ json: { code: 0, data: { ...DETAIL_BASE, status: 2, isUploadImg: 0 } } }),
  )
  let securityBody: Record<string, unknown> | undefined
  await page.route('**/platform/articlesrelease/status/security/update', async (route) => {
    securityBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/platform/approve/list/new/page*', (route) => route.fulfill({ json: LIVE_RECORDS(1) }))

  await page.goto('/backlog/release-live/detail?id=9&sort=3')
  // 未传图拦截
  await page.getByRole('button', { name: '确认放行' }).click()
  await expect(page.getByText('请至少上传一张照片')).toBeVisible()

  await page.setInputFiles('[data-testid=image-list-input]', {
    name: 'g.png',
    mimeType: 'image/png',
    buffer: Buffer.from('89504e470d0a1a0a', 'hex'),
  })
  await expect(page.getByTestId('image-list-item')).toHaveCount(1)
  await page.getByRole('button', { name: '确认放行' }).click()
  await page.waitForURL('**/backlog/release-live?tab=done')

  const body = securityBody as Record<string, unknown>
  expect(String(body.guardOneImg)).toMatch(/^[A-Za-z0-9+/=]+$/)
  expect(body.guardTwoImg).toBe('')
  expect(body.guardThreeImg).toBe('')
  expect(body.id).toBe('9')
  expect(body.parkId).toBe(5000021)
  expect(body.status).toBe(4)
  expect(body.badge).toBe('YT20180326')
})

test('/code：五态互斥 + tel 链接 + 免登录访问', async ({ page }) => {
  // 不 seed token：免登录
  let variant: Record<string, unknown> = { status: 2, qrCodePic: 'aGk=', expire: false }
  await page.route('**/platform/articlesrelease/detail/9', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: {
          ...DETAIL_BASE,
          ...variant,
          parkName: '裕同科技许昌园区',
          floorName: '3',
          bedName: '2',
          phone: '0374-1234567',
          remarks: '行李箱一个',
        },
      },
    }),
  )

  await page.goto('/code?id=9')
  await expect(page.getByAltText('放行二维码')).toBeVisible()
  await expect(page.getByText('扫描放行码以识别备案物进行放行')).toBeVisible()
  await expect(page.getByText('新工厂宿舍楼-3层-302号房-2床')).toBeVisible()
  await expect(page.getByRole('link', { name: '电话联系' })).toHaveAttribute('href', 'tel:0374-1234567')

  // 审批中
  variant = { status: 1, expire: false }
  await page.goto('/code?id=9')
  await expect(page.getByText('放行码仍在审批中，请稍后')).toBeVisible()

  // 过期压过出码态
  variant = { status: 2, qrCodePic: 'aGk=', expire: true }
  await page.goto('/code?id=9')
  await expect(page.getByText('放行码已过期')).toBeVisible()
  await expect(page.getByAltText('放行二维码')).not.toBeVisible()

  // 拒绝放行：显示拒绝原因与放行人员
  variant = { status: 5, expire: false, remark: '物品不符', securityStaff: '保安老王' }
  await page.goto('/code?id=9')
  await expect(page.getByText('拒绝放行')).toBeVisible()
  await expect(page.getByText('物品不符')).toBeVisible()
  await expect(page.getByText('保安老王')).toBeVisible()
})

test('待办首页：3 入口导航 + home 宫格死链回归', async ({ page }) => {
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
      json: { code: 0, data: { serviceModule: [{ moduleName: '待办事项', moduleUrl: '/approve' }] } },
    }),
  )
  await page.route('**/platform/approve/**', (route) => route.fulfill({ json: { code: 0, data: { total: 0 } } }))
  await page.route('**/platform/dor/quit/list/approval*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0 } } }),
  )

  await page.route('**/platform/approve/list/new/page*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  // home 审批砖直达（回归：曾指向不存在的 /backlog/good-release-live）
  await page.goto('/home')
  await page.getByRole('button', { name: /宿舍物品放行审批/ }).click()
  await page.waitForURL('**/backlog/release-live')
  await expect(page.getByRole('tab', { name: '待我审批的' })).toBeVisible()

  // 待办事项模块入口
  await page.goto('/home')
  await page.getByRole('button', { name: '待办事项', exact: true }).click()
  await page.waitForURL('**/backlog')
  await expect(page.getByText('物品放行（生活区）')).toBeVisible()

  // 入口导航
  await page.getByRole('button', { name: '物品放行（生活区）' }).click()
  await page.waitForURL('**/backlog/release-live')
  await expect(page.getByRole('tab', { name: '待我审批的' })).toBeVisible()
})
