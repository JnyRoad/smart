import { expect, test, type Page } from '@playwright/test'

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

function backRecord(name: string, backStatus: string) {
  return {
    id: 9,
    name,
    backStatus,
    deptName: '智能制造一部',
    releaseItemDesc: '非保密物品放行',
    createTime: '2026-06-12 09:00',
    oaNode: '',
  }
}

test('返厂列表：双 Tab 重查、?tab=confirmed 直达与搜索条件断言', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  const queries: URLSearchParams[] = []
  await page.route('**/platform/articlesrelease/back/page*', (route) => {
    const query = new URL(route.request().url()).searchParams
    queries.push(query)
    const confirmed = query.get('approvalStatus') === '1'
    return route.fulfill({
      json: {
        code: 0,
        data: { records: [backRecord(confirmed ? '李四' : '王建国', confirmed ? '已返厂' : '未返厂')], pages: 1 },
      },
    })
  })

  await page.goto('/return-factory')
  await expect(page.getByText('王建国提交的放行条')).toBeVisible()
  await expect(page.getByText('未返厂')).toBeVisible()
  // OA 节点空显示 -
  await expect(page.getByText('OA节点')).toBeVisible()
  expect(queries[0]?.get('approvalStatus')).toBe('0')
  expect(queries[0]?.get('size')).toBe('10')

  // 切到我确认的
  await page.getByRole('tab', { name: '我确认的' }).click()
  await expect(page.getByText('李四提交的放行条')).toBeVisible()
  expect(queries.at(-1)?.get('approvalStatus')).toBe('1')

  // 搜索弹层 → 条件重查当前 Tab
  await page.getByRole('button', { name: /搜 索/ }).click()
  await page.getByPlaceholder('工号').fill('YT9')
  await page.getByPlaceholder('携带人姓名').fill('李四')
  await page.getByRole('button', { name: '确定' }).first().click()
  await expect.poll(() => queries.at(-1)?.get('badge')).toBe('YT9')
  expect(queries.at(-1)?.get('name')).toBe('李四')
  expect(queries.at(-1)?.get('approvalStatus')).toBe('1')

  // ?tab=confirmed 直达第二 Tab
  await page.goto('/return-factory?tab=confirmed')
  await expect(page.getByRole('tab', { name: '我确认的' })).toHaveAttribute('aria-selected', 'true')
})

const DETAIL_BASE = {
  id: 9,
  name: '王建国',
  deptName: '智能制造一部',
  applyMain: {
    sffcDesc: '是',
    fxqcDesc: '厂外',
    fxddDesc: 'A栋',
    ddddDesc: '其它',
    fxsxDesc: '非保密物品放行',
    wpfxlbDesc: '其它',
    sqrjbDesc: '课长级',
    fxsx: 1,
  },
  thingDetailList: [{ wpbm: 'A1', wpmc: '电脑', wpdw: '台', wpsl: 1, jsdw: '裕同', fxrq: '2026-06-20', ysfs: 1, xm: '王五' }],
  personDetailList: [],
  approvalProcess: [
    { statusName: '部门审批', approvers: [{ name: '张**', result: 1, time: '2026-06-12 10:00' }] },
  ],
}

test('返厂详情：确认按钮两态 + 确认后跳「我确认的」', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  let variant: Record<string, unknown> = { status: 4, backTime: null }
  await page.route('**/platform/articlesrelease/detail/9', (route) =>
    route.fulfill({ json: { code: 0, data: { ...DETAIL_BASE, ...variant } } }),
  )
  await page.route('**/platform/articlesrelease/back/confirm/9', (route) =>
    route.fulfill({ json: { code: 0, data: true } }),
  )
  await page.route('**/platform/articlesrelease/back/page*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  // status=4 且无 backTime → 显示按钮，确认后跳我确认 Tab
  await page.goto('/return-factory/detail?id=9')
  await expect(page.getByText('王建国|智能制造一部')).toBeVisible()
  await expect(page.getByText('返厂', { exact: true })).toBeVisible()
  await expect(page.getByText('A1-电脑1台')).toBeVisible()
  await page.getByRole('button', { name: '确认返厂' }).click()
  await page.waitForURL('**/return-factory?tab=confirmed')
  await expect(page.getByRole('tab', { name: '我确认的' })).toHaveAttribute('aria-selected', 'true')

  // 已有 backTime → 无按钮
  variant = { status: 4, backTime: '2026-06-12 18:00' }
  await page.goto('/return-factory/detail?id=9')
  await expect(page.getByText('张** - 通过')).toBeVisible()
  await expect(page.getByRole('button', { name: '确认返厂' })).not.toBeVisible()

  // status≠4 → 无按钮
  variant = { status: 2, backTime: null }
  await page.goto('/return-factory/detail?id=9')
  await expect(page.getByRole('button', { name: '确认返厂' })).not.toBeVisible()
})

test('返厂详情：标签云点击写快照并跳只读链 URL（work 分支未合并，只断言快照与 URL）', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/platform/articlesrelease/detail/9', (route) =>
    route.fulfill({ json: { code: 0, data: { ...DETAIL_BASE, status: 2, backTime: null } } }),
  )

  await page.goto('/return-factory/detail?id=9')
  await page.getByText('A1-电脑1台').click()
  await page.waitForURL('**/good-release/work/detail/goods')
  const snapshot = await page.evaluate(() => sessionStorage.getItem('good-release-detail-items'))
  expect(JSON.parse(snapshot ?? '{}').goods?.[0]?.wpbm).toBe('A1')
})

test('返厂详情：人员放行分支（fxsx=0）标签云写 persons 快照并跳人员只读链', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/platform/articlesrelease/detail/9', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: {
          ...DETAIL_BASE,
          status: 2,
          backTime: null,
          applyMain: { ...DETAIL_BASE.applyMain, fxsx: 0, fxsxDesc: '人员放行' },
          personDetailList: [
            { gh: 'YT2', xm: '李四', lcsy: '出差', lcrq: '2026-06-20', lcsj: '09:30' },
          ],
          thingDetailList: [],
        },
      },
    }),
  )

  await page.goto('/return-factory/detail?id=9')
  await expect(page.getByText('放行人员')).toBeVisible()
  await page.getByText('李四', { exact: true }).click()
  await page.waitForURL('**/good-release/work/detail/persons')
  const snapshot = await page.evaluate(() => sessionStorage.getItem('good-release-detail-items'))
  expect(JSON.parse(snapshot ?? '{}').persons?.[0]?.gh).toBe('YT2')
})

test('死链回归：home 宫格「返厂确认」入口落到真实页', async ({ page }) => {
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
      json: { code: 0, data: { serviceModule: [{ moduleName: '返厂确认', moduleUrl: '/returnFactory' }] } },
    }),
  )
  await page.route('**/platform/approve/**', (route) => route.fulfill({ json: { code: 0, data: { total: 0 } } }))
  await page.route('**/platform/dor/quit/list/approval*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0 } } }),
  )
  await page.route('**/platform/articlesrelease/back/page*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  await page.goto('/home')
  await page.getByRole('button', { name: '返厂确认', exact: true }).click()
  await page.waitForURL('**/return-factory')
  await expect(page.getByRole('tab', { name: '待确认的' })).toBeVisible()
})
