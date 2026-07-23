import { expect, test, type Page } from '@playwright/test'
import { injectTestKey, legacyCipherHex } from './helpers'

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

/** Same a11y-layer picker driver as dorm-services.spec.ts. */
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

// Mock 形状以当前页面消费的字段为准（楼栋/房型/床位主键都是 id，房间总床数 bedTotal，
// 本人入住资料接口仅返回最小化摘要，详情特征码嵌套在 lockPwd）。
const DORMS = {
  code: 0,
  data: [
    { id: 'D1', dormitoryName: '新工厂宿舍楼' },
    { id: 'D2', dormitoryName: '东区宿舍' },
  ],
}
const CHECK_IN_PROFILE = {
  code: 0,
  data: {
    name: '王建国',
    profileComplete: true,
    maskedCertNo: '**************1234',
  },
}

async function mockCheckInBase(page: Page) {
  await mockBaseInfo(page)
  await page.route('**/platform/dormitory/queryDormitory', (route) => route.fulfill({ json: DORMS }))
  await page.route('**/platform/staff/me/check-in-profile', (route) => route.fulfill({ json: CHECK_IN_PROFILE }))
  await page.route('**/platform/dormitory/type/by/park-and-dormitory*', (route) =>
    route.fulfill({ json: { code: 0, data: [{ id: 1, typeName: '四人间' }, { id: 2, typeName: '六人间' }] } }),
  )
}

test('宿舍申请：身份获取失败 → 红字提示且无申请按钮', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/platform/dormitory/queryDormitory', (route) => route.fulfill({ json: DORMS }))
  await page.route('**/platform/staff/me/check-in-profile', (route) =>
    route.fulfill({ json: { code: 1, message: 'not found' } }),
  )

  await page.goto('/check-in')
  await expect(page.getByText('获取用户信息失败！')).toBeVisible()
  await expect(page.getByRole('button', { name: '申请' })).not.toBeVisible()
})

test('宿舍申请：自选房间全流程（满房拦截/床位过滤/草稿持久）→ 提交体断言', async ({ page }) => {
  await seedLogin(page)
  await mockCheckInBase(page)
  // 楼层在 data[0].children[0].children（园区 → 楼栋 → 楼层）
  await page.route('**/platform/park/tree/condition*', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: [
          {
            id: 'P1',
            label: '裕同科技许昌园区',
            children: [
              { id: 'B1', label: '新工厂宿舍楼', children: [{ id: 'F1', label: '1' }, { id: 'F2', label: '2' }] },
            ],
          },
        ],
      },
    }),
  )
  await page.route('**/platform/dormitory/room/search/condition*', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: [
          { roomId: 'R301', roomName: '301', roomSex: 0, freeBedNum: 0, bedTotal: 4 },
          { roomId: 'R302', roomName: '302', roomSex: 1, freeBedNum: 2, bedTotal: 4 },
        ],
      },
    }),
  )
  await page.route('**/platform/dormitory/room/bedDetail/R302', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: [
          { id: 'B1', bedNumber: 1, staffBadge: null, delFlag: 0 },
          { id: 'B2', bedNumber: 2, staffBadge: 'YT9', delFlag: 0 },
          { id: 'B3', bedNumber: 3, staffBadge: null, delFlag: 1 },
          { id: 'B4', bedNumber: 4, staffBadge: null, delFlag: 0 },
        ],
      },
    }),
  )
  let allotBody: Record<string, unknown> | undefined
  await page.route('**/platform/dormitory/room/self/autoallot', async (route) => {
    allotBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0 } })
  })
  await page.route('**/platform/dormitory/staff/me/roomList', (route) =>
    route.fulfill({ json: { code: 0, data: [] } }),
  )
  await page.route('**/platform/dormitory/staff/me/pwd', (route) =>
    route.fulfill({ json: { code: 0, data: '' } }),
  )

  await page.goto('/check-in')
  await page.getByRole('button', { name: '请选择楼栋' }).click()
  await pickWheelOption(page, '新工厂宿舍楼')
  await page.getByRole('button', { name: '请选择房间类型' }).click()
  await pickWheelOption(page, '四人间')

  await page.getByRole('button', { name: '请选择房间' }).click()
  await page.waitForURL('**/check-in/select-room**')
  // 左栏楼层取自树的第三层
  await expect(page.getByRole('button', { name: '1层', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '2层', exact: true })).toBeVisible()
  // 房间格展示性别与床位余量
  await expect(page.getByRole('button', { name: /302房/ })).toContainText('女 2/4')
  // 满房拦截
  await page.getByRole('button', { name: /301房/ }).click()
  await expect(page.getByText('该房间无可选用的床位！')).toBeVisible()
  // 床位过滤：占用(B2)与停用(B3)不出现在 picker
  await page.getByRole('button', { name: /302房/ }).click()
  await expect(page.locator('[aria-label*="4床"]').last()).toBeAttached()
  await expect(page.locator('[aria-label*="2床"]')).toHaveCount(0)
  await expect(page.locator('[aria-label*="3床"]')).toHaveCount(0)
  await pickWheelOption(page, '4床')

  // 草稿回填：返回后楼栋/房型/房间/床位全部还原
  await page.waitForURL(/\/check-in$/)
  await expect(page.getByRole('button', { name: '302房', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '4号床', exact: true })).toBeVisible()
  // 刷新后草稿仍在（sessionStorage）
  await page.reload()
  await expect(page.getByRole('button', { name: '新工厂宿舍楼', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '四人间', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '302房', exact: true })).toBeVisible()

  await page.getByRole('button', { name: '申请' }).click()
  await page.waitForURL('**/check-in/detail')

  // 提交体只能包含宿舍选择，员工身份资料必须由服务端按认证主体回填。
  const body = allotBody as Record<string, unknown>
  expect(Object.keys(body).sort()).toEqual(['bedId', 'dormitoryId', 'floorId', 'parkId', 'roomId', 'roomType'])
  expect(body.dormitoryId).toBe('D1')
  expect(body.roomType).toBe(1)
  expect(body.parkId).toBe(5000021)
  expect(body.floorId).toBe('F1')
  expect(body.roomId).toBe('R302')
  expect(body.bedId).toBe('B4')
})

test('宿舍申请：切换楼栋清空房间类型与已选房间', async ({ page }) => {
  await seedLogin(page)
  await mockCheckInBase(page)
  await page.addInitScript(() => {
    sessionStorage.setItem(
      'check-in-room',
      JSON.stringify({ floorId: 'F1', roomId: 'R302', roomName: '302', bedId: 'B4', bedNumber: 4 }),
    )
    sessionStorage.setItem(
      'check-in-form',
      JSON.stringify({ dormitoryId: 'D1', dormitoryName: '新工厂宿舍楼', roomTypeCode: 1, roomTypeDesc: '四人间' }),
    )
  })

  await page.goto('/check-in')
  await expect(page.getByRole('button', { name: '302房', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '四人间', exact: true })).toBeVisible()

  // 切到另一栋 → 房间类型与已选房间一起清空
  await page.getByRole('button', { name: '新工厂宿舍楼', exact: true }).click()
  await pickWheelOption(page, '东区宿舍')
  await expect(page.getByRole('button', { name: '请选择房间类型', exact: true })).toBeVisible()
  await expect(page.getByRole('button', { name: '请选择房间', exact: true })).toBeVisible()
})

test('宿舍申请详情：分配记录 + 动态码解密展示', async ({ page }) => {
  await seedLogin(page)
  await injectTestKey(page)
  await page.route('**/platform/dormitory/staff/me/roomList', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: [
          {
            dormitoryName: '新工厂宿舍楼',
            roomName: '302',
            bedNumber: 4,
            lockPwd: {
              fingerprintCode: 1,
              fingerprintDesc: '指纹已录入',
              dynamicCode: 3,
              dynamicDesc: '',
            },
          },
        ],
      },
    }),
  )
  await page.route('**/platform/dormitory/staff/me/pwd', (route) =>
    route.fulfill({ json: { code: 0, data: legacyCipherHex('246810') } }),
  )

  await page.goto('/check-in/detail')
  await expect(page.getByText('新工厂宿舍楼')).toBeVisible()
  await expect(page.getByText('302房间')).toBeVisible()
  await expect(page.getByText('4号床')).toBeVisible()
  await expect(page.getByText('指纹已录入')).toBeVisible()
  await expect(page.getByText('已录入', { exact: true })).toBeVisible()
  // 密文经 AES 解出明文动态码
  await expect(page.getByTestId('checkin-lock-code')).toHaveText('246810')
})

test('死链回归：home 宫格「宿舍申请」入口落到真实页', async ({ page }) => {
  await seedLogin(page)
  await mockCheckInBase(page)
  await page.route('**/app/employee/fullinfo', (route) =>
    route.fulfill({ json: { code: 0, data: { employeeId: 'YT20180326', employeeName: '王建国' } } }),
  )
  await page.route('**/app/common/weather*', (route) => route.fulfill({ json: { code: 0, data: {} } }))
  await page.route('**/app/home/bbs/list*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0, records: [] } } }),
  )
  await page.route('**/app/service/module/list', (route) =>
    route.fulfill({
      json: { code: 0, data: { serviceModule: [{ moduleName: '宿舍申请', moduleUrl: '/xuchang/checkIn' }] } },
    }),
  )
  await page.route('**/platform/approve/**', (route) => route.fulfill({ json: { code: 0, data: { total: 0 } } }))
  await page.route('**/platform/dor/quit/list/approval*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0 } } }),
  )

  await page.goto('/home')
  await page.getByRole('button', { name: '宿舍申请', exact: true }).click()
  await page.waitForURL('**/check-in')
  await expect(page.getByRole('tab', { name: '发起提交' })).toBeVisible()
})
