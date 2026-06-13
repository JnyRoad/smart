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

// ===== 办公区审批 =====

const WORK_DETAIL = {
  id: 9,
  status: 2,
  name: '李四',
  deptName: '研发部',
  isUploadImg: 0,
  applyMain: {
    sffcDesc: '否',
    fxqcDesc: '厂外',
    fxddDesc: 'B栋',
    ddddDesc: 'C栋',
    fxsxDesc: '非保密物品放行',
    wpfxlbDesc: '其它',
    sqrjbDesc: '课长级',
    fxsx: 1,
  },
  personDetailList: [],
  thingDetailList: [{ wpbm: 'A1', wpmc: '电脑', wpdw: '台', wpsl: 1 }],
  approvalProcess: [],
}

test('办公区审批：列表（保安搜索含放行事项）→ 详情需图拦截 → 通过跳本模块 ?tab=done（旧 bug 修正回归）', async ({
  page,
}) => {
  await seedLogin(page)
  await mockBaseInfo(page, 0)
  const queries: URLSearchParams[] = []
  await page.route('**/platform/articlesrelease/office/page*', (route) => {
    queries.push(new URL(route.request().url()).searchParams)
    return route.fulfill({
      json: {
        code: 0,
        data: {
          records: [
            { id: 9, name: '李四', backStatus: '待审批', deptName: '研发部', releaseItemDesc: '非保密物品放行', createTime: '2026-06-12 09:00', oaNode: '' },
          ],
          pages: 1,
        },
      },
    })
  })
  await page.route('**/platform/articlesrelease/detail/9', (route) =>
    route.fulfill({ json: { code: 0, data: WORK_DETAIL } }),
  )
  let securityBody: Record<string, unknown> | undefined
  await page.route('**/platform/articlesrelease/status/security/update', async (route) => {
    securityBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, data: true } })
  })

  await page.goto('/backlog/release-work')
  await expect(page.getByText('李四提交的放行条')).toBeVisible()
  expect(queries[0]?.get('approvalStatus')).toBe('0')

  // 保安搜索（含放行事项下拉）
  await page.getByRole('button', { name: /搜 索/ }).click()
  await page.getByPlaceholder('工号').fill('YT9')
  await page.getByRole('button', { name: '放行事项', exact: true }).click()
  // a11y 轮选第一项「人员放行」
  const current = page.locator('[aria-label="当前选择的是：人员放行"]').last()
  for (let i = 0; i < 20; i++) {
    if ((await current.count()) > 0) break
    await page.locator('.adm-picker-view-column-accessible-button[role=button]').last().dispatchEvent('click')
  }
  await page.getByRole('button', { name: '确定' }).last().click()
  await page.getByRole('button', { name: '确定' }).first().click()
  await expect.poll(() => queries.at(-1)?.get('badge')).toBe('YT9')
  expect(queries.at(-1)?.get('releaseItem')).toBe('0')

  // 详情：status=2 显示按钮；需图未传拦截
  await page.goto('/backlog/release-work/detail?id=9')
  await expect(page.getByText('A1-电脑1台')).toBeVisible()
  await page.getByRole('button', { name: '通 过' }).click()
  await expect(page.getByText('请至少上传一张照片')).toBeVisible()

  await page.setInputFiles('[data-testid=image-list-input]', {
    name: 'g.png',
    mimeType: 'image/png',
    buffer: Buffer.from('89504e470d0a1a0a', 'hex'),
  })
  await expect(page.getByTestId('image-list-item')).toHaveCount(1)
  await page.getByRole('button', { name: '通 过' }).click()
  // 修正回归：跳本模块列表而非生活区
  await page.waitForURL('**/backlog/release-work?tab=done')
  expect((securityBody as Record<string, unknown>).status).toBe(4)
  expect((securityBody as Record<string, unknown>).parkId).toBe(5000021)

  // tab=done 只读
  await page.goto('/backlog/release-work/detail?id=9&tab=done')
  await expect(page.getByText('A1-电脑1台')).toBeVisible()
  await expect(page.getByRole('button', { name: '通 过' })).not.toBeVisible()
})

// ===== 报修审批 =====

const REPAIR_DETAIL = {
  id: 9,
  name: '王建国',
  staffBadge: 'YT20180326',
  compName: '裕同科技',
  depName: '智能制造一部',
  rangeTypeDesc: '宿舍',
  repairTypeDesc: '空调',
  dormitoryName: '新工厂宿舍楼',
  roomName: '302',
  parkName: '裕同科技许昌园区',
  faultDesc: '不制冷',
  createTime: '2026-06-10 10:00',
  statusDesc: '待接单',
  // 旧详情字段是 imgs；原始 base64（无 data: 前缀）应补前缀渲染。
  imgs: ['aGk='],
  approvalProcess: [],
  repairReplyList: [
    { replyStatusDesc: '维修成功', replyTime: '2026-06-11 15:00', replyName: '维修工老李', replyDesc: '已加氟' },
  ],
}

test('报修审批：status 0 接单（GET 参数断言 → 无参列表）与 status 1 维修结果（POST 断言 → ?tab=done）', async ({
  page,
}) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  let detailStatus = 0
  await page.route('**/platform/dormitory/repair/query/detail/9', (route) =>
    route.fulfill({ json: { code: 0, data: { ...REPAIR_DETAIL, status: detailStatus } } }),
  )
  let updateQuery: URLSearchParams | undefined
  await page.route('**/platform/dormitory/repair/status/update*', (route) => {
    updateQuery = new URL(route.request().url()).searchParams
    return route.fulfill({ json: { code: 0, data: true } })
  })
  let replyBody: Record<string, unknown> | undefined
  await page.route('**/platform/dormitory/repair/reply', async (route) => {
    replyBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/platform/approve/list/repairs/list*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  // status=0：接单
  await page.goto('/backlog/repairs/detail?id=9')
  await expect(page.getByText('不制冷')).toBeVisible()
  await expect(page.getByAltText('物品照片')).toBeVisible()
  await page.getByPlaceholder('请输入内容').fill('马上来')
  await page.getByRole('button', { name: '接单', exact: true }).click()
  await page.waitForURL(/\/backlog\/repairs$/)
  expect(updateQuery?.get('approveBadge')).toBe('YT20180326')
  expect(updateQuery?.get('status')).toBe('1')
  expect(updateQuery?.get('remark')).toBe('马上来')

  // status=1：已安排维修
  detailStatus = 1
  await page.goto('/backlog/repairs/detail?id=9')
  await page.getByPlaceholder('请输入内容').fill('已修复')
  await page.getByRole('button', { name: '已安排维修' }).click()
  await page.waitForURL('**/backlog/repairs?tab=done')
  expect(replyBody?.status).toBe(3)
  expect(replyBody?.result).toBe('已修复')

  // tab=done：只读 + 维修结果列表
  await page.goto('/backlog/repairs/detail?id=9&tab=done')
  await expect(page.getByText('维修成功')).toBeVisible()
  await expect(page.getByText('维修工老李：已加氟')).toBeVisible()
  await expect(page.getByRole('button', { name: '接单', exact: true })).not.toBeVisible()
})

// ===== 退宿审批 =====

const EXIT_DETAIL = {
  id: 9,
  name: '王建国',
  status: 1,
  dorDetailStr: ['新工厂宿舍楼/302号房'],
  quitReasonDesc: '自离',
  applyLeaveTime: '2026-06-20 10:00',
  createTime: '2026-06-12 09:00',
  // 审批侧时间线字段是 processRecord（归一化回归点）
  processRecord: [
    { statusName: '主管审批', staffInfos: [{ staffName: '张**', result: 0, resultDesc: '待审批' }] },
  ],
}

test('退宿审批：isApprove 缺失只读（高风险回归）→ isApprove=true 主管/保安两段流转 + 搜索断言', async ({
  page,
}) => {
  await seedLogin(page)
  await mockBaseInfo(page, 0)
  let detailVariant: Record<string, unknown> = { ...EXIT_DETAIL } // 无 isApprove
  await page.route('**/platform/dor/quit/detail/9', (route) =>
    route.fulfill({ json: { code: 0, data: detailVariant } }),
  )
  let updateQuery: URLSearchParams | undefined
  await page.route('**/platform/dor/quit/status/update*', (route) => {
    updateQuery = new URL(route.request().url()).searchParams
    return route.fulfill({ json: { code: 0, data: true } })
  })
  const listBodies: Record<string, unknown>[] = []
  await page.route('**/platform/dor/quit/list/approval*', async (route) => {
    listBodies.push((route.request().postDataJSON() ?? {}) as Record<string, unknown>)
    await route.fulfill({
      json: {
        code: 0,
        data: {
          records: [
            { id: 9, name: '王建国', status: 1, statusDesc: '已通过', dorDetailStr: ['新工厂宿舍楼/302号房'], quitReasonDesc: '自离', applyLeaveTime: '2026-06-20 10:00', createTime: '2026-06-12 09:00' },
          ],
          pages: 1,
        },
      },
    })
  })

  // isApprove 缺失 → 只读：时间线可见（processRecord 归一化），但无按钮
  await page.goto('/backlog/dorm-exit/detail?id=9')
  await expect(page.getByText('主管审批')).toBeVisible()
  await expect(page.getByText('张** - 待审批')).toBeVisible()
  await expect(page.getByRole('button', { name: '通过' })).not.toBeVisible()
  await expect(page.getByPlaceholder('请输入内容')).not.toBeVisible()

  // isApprove=true 且 status=1 → 主管通过
  detailVariant = { ...EXIT_DETAIL, isApprove: true }
  await page.goto('/backlog/dorm-exit/detail?id=9')
  await page.getByPlaceholder('请输入内容').fill('同意')
  await page.getByRole('button', { name: '通过', exact: true }).click()
  await page.waitForURL('**/backlog/dorm-exit?tab=done')
  expect(updateQuery?.get('status')).toBe('2')
  expect(updateQuery?.get('approveBadge')).toBe('YT20180326')
  expect(updateQuery?.get('remark')).toBe('同意')

  // 列表（已落在 ?tab=done）：POST 体断言 + 配色
  await expect.poll(() => listBodies.at(-1)?.status).toBe(1)
  expect(listBodies.at(-1)?.isSecurityGuard).toBe(0)
  expect(listBodies.at(-1)?.parkId).toBe(5000021)
  await expect(page.getByText('已通过')).toHaveClass(/text-\[#16a673\]/)

  // 保安搜索（badge/name）
  await page.getByRole('button', { name: /搜 索/ }).click()
  await page.getByPlaceholder('工号').fill('YT9')
  await page.getByRole('button', { name: '确定' }).first().click()
  await expect.poll(() => listBodies.at(-1)?.badge).toBe('YT9')

  // isApprove=true 且 status=2 → 保安通过
  detailVariant = { ...EXIT_DETAIL, status: 2, isApprove: true }
  await page.goto('/backlog/dorm-exit/detail?id=9')
  await page.getByRole('button', { name: '保安通过' }).click()
  await page.waitForURL('**/backlog/dorm-exit?tab=done')
  expect(updateQuery?.get('status')).toBe('4')
})

test('退宿审批：code=0 但 data=false 视为失败（toast 留页，不跳转）', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page, 0)
  await page.route('**/platform/dor/quit/detail/9', (route) =>
    route.fulfill({ json: { code: 0, data: { ...EXIT_DETAIL, isApprove: true } } }),
  )
  await page.route('**/platform/dor/quit/status/update*', (route) =>
    route.fulfill({ json: { code: 0, data: false, message: '单据已被他人处理' } }),
  )

  await page.goto('/backlog/dorm-exit/detail?id=9')
  await page.getByRole('button', { name: '通过', exact: true }).click()
  await expect(page.getByText('单据已被他人处理')).toBeVisible()
  await expect(page).toHaveURL(/\/backlog\/dorm-exit\/detail\?id=9$/)
})

test('待办首页入口：报修审批与退宿审批落到真实页（过渡 404 闭合回归）', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/platform/approve/list/repairs/list*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )
  await page.route('**/platform/dor/quit/list/approval*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  await page.goto('/backlog')
  await page.getByRole('button', { name: '园区报修审批' }).click()
  await page.waitForURL('**/backlog/repairs')
  await expect(page.getByRole('tab', { name: '待我审批的' })).toBeVisible()

  await page.goto('/backlog')
  await page.getByRole('button', { name: '退宿审批' }).click()
  await page.waitForURL('**/backlog/dorm-exit')
  await expect(page.getByRole('tab', { name: '待我审批的' })).toBeVisible()
})
