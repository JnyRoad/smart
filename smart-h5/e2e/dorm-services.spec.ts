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


/**
 * Drives an antd-mobile picker via its accessibility buttons (the visual
 * wheel does not select reliably under automation). Steps "next" until the
 * target is current, then confirms.
 */
async function pickWheelOption(page: Page, text: string) {
  // The accessible layer is visually hidden — locate via aria attributes and
  // dispatch clicks directly (role queries and real clicks skip hidden nodes).
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

// ===== dorm-repairs =====

test('报修：区域联动 → 提交体断言 → 跳列表', async ({ page }) => {
  await seedLogin(page)
  await page.route('**/app/dormitory/repair/enum/range', (route) =>
    route.fulfill({ json: { code: 0, data: [{ code: 1, desc: '宿舍' }, { code: 2, desc: '办公室' }] } }),
  )
  let addBody: Record<string, unknown> | undefined
  await page.route('**/platform/dormitory/repair/add', async (route) => {
    addBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0 } })
  })
  await page.route('**/platform/dormitory/repair/query/record*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  await page.goto('/dorm-repairs')
  // 区域选「办公室」→ 楼栋默认首项「餐厅三楼」
  await page.getByRole('button', { name: '请选择维修区域' }).click()
  await pickWheelOption(page, '办公室')
  await expect(page.getByRole('button', { name: '餐厅三楼' })).toBeVisible()

  await page.getByRole('button', { name: '请选择维修类别' }).click()
  await pickWheelOption(page, '空调')

  await page.getByPlaceholder('请输入所在房间').fill('302')
  await page.getByPlaceholder('请描述故障情况').fill('空调不制冷')
  // base64 图片
  await page.setInputFiles('[data-testid=image-list-input]', {
    name: 'a.png',
    mimeType: 'image/png',
    buffer: Buffer.from('89504e470d0a1a0a', 'hex'),
  })
  await expect(page.getByTestId('image-list-item')).toHaveCount(1)

  await page.getByRole('button', { name: '申请' }).click()
  await page.waitForURL('**/dorm-repairs/list')

  const body = addBody as Record<string, unknown>
  expect(body.rangeType).toBe(2)
  expect(body.repairType).toBe(7)
  expect(body.dormitoryName).toBe('餐厅三楼')
  expect(body.roomName).toBe('302')
  expect(body.faultDesc).toBe('空调不制冷')
  expect((body.faultImgs as string[]).length).toBe(1)
  expect((body.faultImgs as string[])[0]).toMatch(/^[A-Za-z0-9+/=]+$/) // raw base64
  expect(body.parkId).toBe(5000021)
})

test('报修：列表状态配色与详情维修结果区', async ({ page }) => {
  await seedLogin(page)
  await page.route('**/platform/dormitory/repair/query/record*', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: {
          records: [
            { id: 1, name: '王建国', status: 3, statusDesc: '维修完成', rangeTypeDesc: '宿舍', repairTypeDesc: '空调', dormitoryName: '新工厂宿舍楼', faultDesc: '不制冷', createTime: '2026-06-10 10:00' },
            { id: 2, name: '王建国', status: 4, statusDesc: '无法维修', rangeTypeDesc: '车间', repairTypeDesc: '灯', dormitoryName: '一楼', faultDesc: '灯坏了', createTime: '2026-06-09 09:00' },
          ],
          pages: 1,
        },
      },
    }),
  )
  await page.route('**/platform/dormitory/repair/query/detail/1', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: {
          id: 1, name: '王建国', staffBadge: 'YT20180326', compName: '裕同科技', depName: '智能制造一部',
          status: 3, statusDesc: '维修完成', rangeTypeDesc: '宿舍', repairTypeDesc: '空调',
          dormitoryName: '新工厂宿舍楼', roomName: '302', parkName: '裕同科技许昌园区',
          faultDesc: '不制冷', createTime: '2026-06-10 10:00',
          approvalProcess: [
            { statusName: '宿管审批', approvers: [{ name: '张**', result: 1, time: '2026-06-10 11:00' }] },
          ],
          repairReplyList: [
            { replyStatusDesc: '维修成功', replyTime: '2026-06-11 15:00', replyName: '维修工老李', replyDesc: '已加氟利昂' },
          ],
        },
      },
    }),
  )

  await page.goto('/dorm-repairs/list')
  await expect(page.getByText('维修完成')).toBeVisible()
  await expect(page.getByText('无法维修')).toBeVisible()

  await page.getByText('维修完成').click()
  await page.waitForURL('**/dorm-repairs/detail?id=1')
  await expect(page.getByText('新工厂宿舍楼#302')).toBeVisible()
  await expect(page.getByText('张** - 通过')).toBeVisible()
  await expect(page.getByText('维修成功')).toBeVisible()
  await expect(page.getByText('维修工老李：已加氟利昂')).toBeVisible()
})

test('死链回归：home 宫格「园区报修」入口落到真实页', async ({ page }) => {
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
    route.fulfill({ json: { code: 0, data: { serviceModule: [{ moduleName: '园区报修', moduleUrl: '/dormRepairs' }] } } }),
  )
  await page.route('**/platform/approve/**', (route) => route.fulfill({ json: { code: 0, data: { total: 0 } } }))
  await page.route('**/platform/dor/quit/list/approval*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0 } } }),
  )
  await page.route('**/app/dormitory/repair/enum/range', (route) =>
    route.fulfill({ json: { code: 0, data: [] } }),
  )

  await page.goto('/home')
  await page.getByRole('button', { name: '园区报修', exact: true }).click()
  await page.waitForURL('**/dorm-repairs')
  await expect(page.getByRole('tab', { name: '发起提交' })).toBeVisible()
})

test('报修：状态配色 class 与提交失败 toast', async ({ page }) => {
  await seedLogin(page)
  await page.route('**/platform/dormitory/repair/query/record*', (route) =>
    route.fulfill({
      json: {
        code: 0,
        data: {
          records: [
            { id: 1, name: '王建国', status: 3, statusDesc: '维修完成', createTime: '2026-06-10' },
            { id: 2, name: '王建国', status: 4, statusDesc: '无法维修', createTime: '2026-06-09' },
            { id: 3, name: '王建国', status: 1, statusDesc: '待处理', createTime: '2026-06-08' },
          ],
          pages: 1,
        },
      },
    }),
  )
  await page.goto('/dorm-repairs/list')
  // 配色断言：3 绿 / 4 红 / 其余灰
  await expect(page.getByText('维修完成')).toHaveClass(/text-\[#16a673\]/)
  await expect(page.getByText('无法维修')).toHaveClass(/text-\[#d83b36\]/)
  await expect(page.getByText('待处理')).toHaveClass(/text-mid/)

  // 提交失败 toast
  await page.route('**/app/dormitory/repair/enum/range', (route) =>
    route.fulfill({ json: { code: 0, data: [] } }),
  )
  await page.route('**/platform/dormitory/repair/add', (route) =>
    route.fulfill({ json: { code: 1, message: '工单提交过于频繁' } }),
  )
  await page.goto('/dorm-repairs')
  await page.getByRole('button', { name: '请选择维修区域' }).click()
  await pickWheelOption(page, '宿舍')
  await page.getByRole('button', { name: '请选择维修类别' }).click()
  await pickWheelOption(page, '灯')
  await page.getByPlaceholder('请输入所在房间').fill('101')
  await page.getByPlaceholder('请描述故障情况').fill('灯坏了')
  await page.getByRole('button', { name: '申请' }).click()
  await expect(page.getByText('工单提交过于频繁')).toBeVisible()
  await expect(page).toHaveURL(/\/dorm-repairs$/)
})

// ===== dorm-exit =====

const MY_ROOMS = {
  code: 0,
  data: { data: [
    { dormitoryId: 'D1', roomId: 'R1', dormitoryName: '新工厂宿舍楼', roomName: '302' },
    { dormitoryId: 'D1', roomId: 'R2', dormitoryName: '新工厂宿舍楼', roomName: '303' },
  ] },
}

test('退宿：房间多选去重/删除 → 提交体断言', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/app/appdormitory/roomList/**', (route) => route.fulfill({ json: MY_ROOMS }))
  let saveBody: Record<string, unknown> | undefined
  await page.route('**/platform/dor/quit/apply', async (route) => {
    saveBody = route.request().postDataJSON() as Record<string, unknown>
    await route.fulfill({ json: { code: 0, data: true } })
  })
  await page.route('**/platform/dor/quit/page*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  await page.goto('/dorm-exit')
  // 选两次同一房间 → 去重为 1
  await page.getByRole('button', { name: '请选择退宿房间（可多选）' }).click()
  await pickWheelOption(page, '新工厂宿舍楼/302号房')
  await page.getByRole('button', { name: '请选择退宿房间（可多选）' }).click()
  await pickWheelOption(page, '新工厂宿舍楼/302号房')
  await expect(page.getByTestId('selected-room')).toHaveCount(1)
  // 再选 303，再删 302
  await page.getByRole('button', { name: '请选择退宿房间（可多选）' }).click()
  await pickWheelOption(page, '新工厂宿舍楼/303号房')
  await page.getByRole('button', { name: '删除新工厂宿舍楼/302号房' }).click()
  await expect(page.getByTestId('selected-room')).toHaveCount(1)
  await expect(page.getByTestId('selected-room')).toContainText('303号房')

  await page.getByRole('button', { name: '请选择退宿原因' }).click()
  await pickWheelOption(page, '自离')
  await page.getByRole('button', { name: '请选择预计离开日期' }).click()
  await page.getByRole('button', { name: '确定' }).last().click()
  await page.getByRole('button', { name: '申请' }).click()
  await page.waitForURL('**/dorm-exit/list')

  const body = saveBody as Record<string, unknown>
  expect(body.dormitoryIds).toEqual(['D1'])
  expect(body.roomIds).toEqual(['R2'])
  expect(body.quitReason).toBe(5)
  expect(String(body.applyLeaveTime)).toMatch(/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:00$/)
  expect(body.badge).toBe('YT20180326')
  expect(body.name).toBe('王建国')
  expect(body.parkId).toBe(5000021)
})

test('退宿详情：status 2/4/5 三态渲染', async ({ page }) => {
  await seedLogin(page)
  const base = {
    id: 9, name: '王建国', dorDetailStr: ['新工厂宿舍楼/302号房'], quitReasonDesc: '自离',
    applyLeaveTime: '2026-06-20 10:00', createTime: '2026-06-12 09:00',
    approvalProcess: [{ statusName: '宿管审批', approvers: [{ name: '张**', result: 1, time: '2026-06-12 10:00' }] }],
  }
  let status = 2
  await page.route('**/platform/dor/quit/detail/9', (route) =>
    route.fulfill({
      json: { code: 0, data: { ...base, status, qrCode: status === 2 ? 'aGk=' : undefined } },
    }),
  )

  await page.goto('/dorm-exit/detail?id=9')
  await expect(page.getByAltText('退宿放行二维码')).toBeVisible()
  await expect(page.getByText('在门卫处出示放行码')).toBeVisible()

  status = 4
  await page.goto('/dorm-exit/detail?id=9')
  await expect(page.getByText('已出厂', { exact: true })).toBeVisible()
  await expect(page.getByText('已同意出厂')).toBeVisible()
  await expect(page.getByAltText('退宿放行二维码')).not.toBeVisible()

  status = 5
  await page.goto('/dorm-exit/detail?id=9')
  await expect(page.getByText('已拒绝出厂')).toBeVisible()
  // 信息卡与时间线恒在
  await expect(page.getByText('张** - 通过')).toBeVisible()
})

test('死链回归：home 宫格「退宿申请」入口落到真实页', async ({ page }) => {
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
    route.fulfill({ json: { code: 0, data: { serviceModule: [{ moduleName: '退宿申请', moduleUrl: '/xuchang/dormExit' }] } } }),
  )
  await page.route('**/platform/approve/**', (route) => route.fulfill({ json: { code: 0, data: { total: 0 } } }))
  await page.route('**/platform/dor/quit/list/approval*', (route) =>
    route.fulfill({ json: { code: 0, data: { total: 0 } } }),
  )
  await page.route('**/app/appdormitory/roomList/**', (route) => route.fulfill({ json: MY_ROOMS }))

  await page.goto('/home')
  await page.getByRole('button', { name: '退宿申请', exact: true }).click()
  await page.waitForURL('**/dorm-exit')
  await expect(page.getByRole('tab', { name: '发起提交' })).toBeVisible()
})

test('退宿：未选房间拦截 + SegmentTabs 双向 + status5 无码', async ({ page }) => {
  await seedLogin(page)
  await mockBaseInfo(page)
  await page.route('**/app/appdormitory/roomList/**', (route) => route.fulfill({ json: MY_ROOMS }))
  await page.route('**/platform/dor/quit/page*', (route) =>
    route.fulfill({ json: { code: 0, data: { records: [], pages: 1 } } }),
  )

  await page.goto('/dorm-exit')
  // 未选房间直接申请 → 拦截
  await page.getByRole('button', { name: '申请' }).click()
  await expect(page.getByText('请选择退宿房间！')).toBeVisible()

  // SegmentTabs 双向（修复旧版误跳报修 bug 的回归防护）
  await page.getByRole('tab', { name: '查看数据' }).click()
  await page.waitForURL('**/dorm-exit/list')
  await page.getByRole('tab', { name: '发起提交' }).click()
  await page.waitForURL(/\/dorm-exit$/)

  // status 5 不渲染二维码
  await page.route('**/platform/dor/quit/detail/5', (route) =>
    route.fulfill({
      json: { code: 0, data: { id: 5, name: '王建国', status: 5, qrCode: 'aGk=', approvalProcess: [] } },
    }),
  )
  await page.goto('/dorm-exit/detail?id=5')
  await expect(page.getByText('已拒绝出厂')).toBeVisible()
  await expect(page.getByAltText('退宿放行二维码')).not.toBeVisible()
})
