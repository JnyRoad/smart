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

/** 办公区编辑链路先创建服务端草稿，人员查询只能绑定该草稿。 */
async function mockOfficeDraftAndStaffLookup(page: Page) {
  await page.route('**/platform/articlesrelease/office/draft', (route) =>
    route.fulfill({ json: { code: 0, data: { releaseId: 8001 } } }),
  )
  await page.route('**/platform/articlesrelease/8001/staff/lookup**', (route) =>
    route.fulfill({ json: { code: 0, data: { id: 9, name: '李四' } } }),
  )
}

async function pickWheelOption(page: Page, text: string) {
  const current = page.locator(`[aria-label="当前选择的是：${text}"]`).last()
  for (let i = 0; i < 20; i++) {
    if ((await current.count()) > 0) break
    await page
      .locator('.adm-picker-view-column-accessible-button[role=button]')
      .last()
      .dispatchEvent('click')
  }
  await expect(current).toBeAttached()
  await page.getByRole('button', { name: '确定' }).last().click()
}

async function fillMainForm(page: Page) {
  await page.goto('/good-release/work')
  // 表单组一
  for (const [label, option] of [
    ['放行去处', '厂外'],
    ['是否返厂', '是'],
    ['出发地点', 'B栋'],
  ] as const) {
    await page.getByRole('button', { name: '请选择' }).first().click()
    void label
    await pickWheelOption(page, option)
  }
  await page.getByPlaceholder('请输入').first().fill('B栋门口')
  // 到达地点（此时第一个未填 picker 是到达地点）
  await page.getByRole('button', { name: '请选择' }).first().click()
  await pickWheelOption(page, 'C栋')
  await page.getByPlaceholder('请输入').nth(1).fill('C栋仓库')
  await page.getByRole('button', { name: '请选择' }).first().click()
  await pickWheelOption(page, '课长级')
}

test('办公区：人员分支全流程（弹窗查人 → 草稿持久 → 提交体断言）', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await mockOfficeDraftAndStaffLookup(page)
  let saveBody: Record<string, unknown> | undefined
  await page.route('**/platform/articlesrelease/office/save', async (route) => {
    saveBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/platform/articlesrelease/office/page*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  await fillMainForm(page)
  // 放行事项 → 人员放行（0）
  await page.getByRole('button', { name: '请选择' }).first().click()
  await pickWheelOption(page, '人员放行')
  await page.getByRole('button', { name: '请选择' }).first().click()
  await pickWheelOption(page, '领/退/转料')

  // 人员入口 → 列表 → 新增
  await page.getByRole('button', { name: '请添加' }).click()
  await page.waitForURL('**/good-release/work/persons')
  await expect(page.getByText('暂无放行人员信息')).toBeVisible()
  await page.getByRole('button', { name: '新增人员' }).click()
  await page.waitForURL('**/good-release/work/persons/edit')

  // 工号弹窗查人回填
  await page.getByRole('button', { name: '请输入' }).first().click()
  await page.getByPlaceholder('请输入', { exact: true }).fill('YT0002')
  await page.getByRole('button', { name: '确定' }).click()
  await expect(page.getByRole('button', { name: 'YT0002' })).toBeVisible()
  await expect(page.getByRole('button', { name: '李四' })).toBeVisible()
  await page.getByPlaceholder('请输入离厂事由').fill('出差')
  await page.getByRole('button', { name: '请选择离厂日期' }).click()
  await page.getByRole('button', { name: '确定' }).last().click()
  await page.getByRole('button', { name: '确认添加放行人员' }).click()

  // 回人员列表 → 确定回主表单，标签回显
  await page.waitForURL('**/good-release/work/persons')
  await expect(page.getByText('已添加放行人员（1人）')).toBeVisible()
  await page.getByRole('button', { name: '确 定' }).click()
  await page.waitForURL(/\/good-release\/work$/)
  await expect(page.getByText('李四')).toBeVisible()

  // 草稿持久：刷新后主表单与人员仍在
  await page.reload()
  await expect(page.getByText('李四')).toBeVisible()
  await expect(page.getByRole('button', { name: '厂外' })).toBeVisible()

  // 提交
  await page.getByRole('button', { name: '申请' }).click()
  await page.waitForURL('**/good-release/work/list')

  const body = saveBody as Record<string, unknown>
  const applyMain = body.applyMain as Record<string, unknown>
  expect(applyMain.fxqc).toBe(1)
  expect(applyMain.sffc).toBe(0)
  expect(applyMain.fxdd).toBe(1)
  expect(applyMain.fxddxq).toBe('B栋门口')
  expect(applyMain.dddd).toBe(2)
  expect(applyMain.ddddxq).toBe('C栋仓库')
  expect(applyMain.sqrjb).toBe(1)
  expect(applyMain.fxsx).toBe(0)
  expect(applyMain.wpfxlb).toBe(0)
  expect(body.releaseId).toBe(8001)
  expect(body).not.toHaveProperty('badge')
  expect(body.parkId).toBe(5000021)
  expect(body.status).toBe(1)
  const personList = body.personList as Record<string, unknown>[]
  expect(personList).toHaveLength(1)
  expect(personList[0]).toMatchObject({ gh: 'YT0002', xm: 9, name: '李四', lcsy: '出差' })
  expect(String(personList[0]?.lcrq)).toMatch(/^\d{4}-\d{2}-\d{2}$/)
  expect(String(personList[0]?.lcsj)).toMatch(/^\d{2}:\d{2}$/)
  expect(body.thingList).toEqual([])

  // 提交成功清空草稿
  const draft = await page.evaluate(() => localStorage.getItem('goods-work-draft'))
  expect(JSON.parse(draft ?? '{}').state?.persons ?? []).toEqual([])
})

test('办公区：物品分支（增改删 + 提交体断言）', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await mockOfficeDraftAndStaffLookup(page)
  let saveBody: Record<string, unknown> | undefined
  await page.route('**/platform/articlesrelease/office/save', async (route) => {
    saveBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/platform/articlesrelease/office/page*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  await fillMainForm(page)
  await page.getByRole('button', { name: '请选择' }).first().click()
  await pickWheelOption(page, '非保密物品放行')
  await page.getByRole('button', { name: '请选择' }).first().click()
  await pickWheelOption(page, '其它')

  // 物品入口
  await page.getByRole('button', { name: '请添加' }).click()
  await page.waitForURL('**/good-release/work/goods')
  await page.getByRole('button', { name: '新增物品' }).click()
  await page.waitForURL('**/good-release/work/goods/edit')

  // 必填校验：直接提交提示资产编码
  await page.getByRole('button', { name: '确认添加放行物品' }).click()
  await expect(page.getByText('请输入资产编码')).toBeVisible()

  const inputs = page.getByPlaceholder('请输入')
  await inputs.nth(0).fill('A1')
  await inputs.nth(1).fill('显示器')
  await inputs.nth(2).fill('台')
  await inputs.nth(3).fill('2')
  await inputs.nth(4).fill('裕同科技')
  await page.getByRole('button', { name: '请选择放行日期' }).click()
  await page.getByRole('button', { name: '确定' }).last().click()
  await page.getByRole('button', { name: '请选择', exact: true }).click()
  await pickWheelOption(page, '货车')
  await page.getByRole('button', { name: '确认添加放行物品' }).click()
  await page.waitForURL('**/good-release/work/goods')
  await expect(page.getByText('已添加放行物品（1项）')).toBeVisible()

  // 编辑改名
  await page.getByRole('button', { name: '编辑显示器' }).click()
  await page.waitForURL('**/goods/edit?index=0')
  await page.getByPlaceholder('请输入').nth(1).fill('电脑')
  await page.getByRole('button', { name: '确认修改放行物品' }).click()
  await expect(page.getByText('电脑 A1')).toBeVisible()

  // 确定回主表单，标签「A1-电脑2台」
  await page.getByRole('button', { name: '确 定' }).click()
  await page.waitForURL(/\/good-release\/work$/)
  await expect(page.getByText('A1-电脑2台')).toBeVisible()

  await page.getByRole('button', { name: '申请' }).click()
  await page.waitForURL('**/good-release/work/list')

  const body = saveBody as Record<string, unknown>
  expect(body.releaseId).toBe(8001)
  expect(body).not.toHaveProperty('badge')
  expect((body.applyMain as Record<string, unknown>).fxsx).toBe(1)
  expect(body.personList).toEqual([])
  const thingList = body.thingList as Record<string, unknown>[]
  expect(thingList).toHaveLength(1)
  expect(thingList[0]).toMatchObject({
    wpbm: 'A1',
    wpmc: '电脑',
    wpdw: '台',
    wpsl: '2',
    jsdw: '裕同科技',
    ysfs: 1,
    cph: '',
  })
  expect(String(thingList[0]?.fxrq)).toMatch(/^\d{4}-\d{2}-\d{2}$/)
})

test('办公区列表与详情：type=5 断言 + 只读链完整落地', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  let listQuery: URLSearchParams | undefined
  await page.route('**/platform/articlesrelease/office/page*', (route) => {
    listQuery = new URL(route.request().url()).searchParams
    return route.fulfill({
      json: {
        code: 0,
        data: {
          records: [
            {
              id: 9,
              name: '王建国',
              oaNode: '审批中',
              compName: '裕同科技',
              releaseItemDesc: '非保密物品放行',
              createTime: '2026-06-12 09:00',
              backStatus: '未返厂',
            },
          ],
          pages: 1,
        },
      },
    })
  })
  await page.route('**/platform/articlesrelease/detail/9', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: {
          id: 9,
          status: 2,
          expire: false,
          qrCodePic: 'aGk=',
          name: '王建国',
          deptName: '智能制造一部',
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
          thingDetailList: [
            { wpbm: 'A1', wpmc: '电脑', wpdw: '台', wpsl: 2, jsdw: '裕同', fxrq: '2026-06-20', bz: '', ysfs: 1, ysfsDesc: '货车', xm: '王五', cph: '豫A12345' },
          ],
          approvalProcess: [],
        },
      },
    }),
  )

  await page.goto('/good-release/work/list')
  await expect(page.getByText('王建国提交的放行条')).toBeVisible()
  expect(listQuery?.get('type')).toBe('5')
  expect(listQuery?.get('badge')).toBe('YT20180326')

  // 列表 → 详情
  await page.getByText('王建国提交的放行条').click()
  await page.waitForURL('**/good-release/work/detail?id=9')
  await expect(page.getByAltText('放行二维码')).toBeVisible()
  await expect(page.getByText('不返厂')).toBeVisible()

  // 标签云 → 物品只读列表 → 单条
  await page.getByText('A1-电脑2台').click()
  await page.waitForURL('**/good-release/work/detail/goods')
  await expect(page.getByText('放行物品（1项）')).toBeVisible()
  await page.getByText('电脑 A1').click()
  await page.waitForURL('**/detail/goods/item?i=0')
  await expect(page.getByText('货车')).toBeVisible()
  await expect(page.getByText('豫A12345')).toBeVisible()
  await expect(page.getByText('王五')).toBeVisible()
})

test('只读链回退三件套：快照缺失 / i 越界 / 快照损坏 → 回 /good-release/work', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)

  // 缺失
  await page.goto('/good-release/work/detail/persons')
  await page.waitForURL(/\/good-release\/work$/)

  // 越界
  await page.evaluate(() =>
    sessionStorage.setItem('good-release-detail-items', JSON.stringify({ goods: [{ wpbm: 'A1' }] })),
  )
  await page.goto('/good-release/work/detail/goods/item?i=5')
  await page.waitForURL(/\/good-release\/work$/)

  // 损坏
  await page.evaluate(() => sessionStorage.setItem('good-release-detail-items', 'not-json'))
  await page.goto('/good-release/work/detail/goods')
  await page.waitForURL(/\/good-release\/work$/)
})

test('死链回归：home 宫格「物品放行（办公区）」入口落到真实页', async ({ page }) => {
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
      json: { code: 0, data: { serviceModule: [{ moduleName: '物品放行（办公区）', moduleUrl: '/articlesrelease' }] } },
    }),
  )
  await page.route('**/platform/approve/**', (route) => route.fulfill({ json: { code: 0, data: { total: 0 } } }))
  await page.route('**/platform/dor/quit/list/approval*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0 } } }),
  )

  await page.goto('/home')
  await page.getByRole('button', { name: '物品放行（办公区）', exact: true }).click()
  await page.waitForURL('**/good-release/work')
  await expect(page.getByRole('tab', { name: '发起提交' })).toBeVisible()
})
