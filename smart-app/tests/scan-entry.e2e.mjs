import { createRequire } from 'node:module'
import { mkdir } from 'node:fs/promises'

const require = createRequire(
  process.env.PLAYWRIGHT_MODULE_PATH
    ? process.env.PLAYWRIGHT_MODULE_PATH + '/package.json'
    : import.meta.url,
)
const { chromium } = require('playwright')

const baseUrl = process.env.CLIENT_PREVIEW_URL || 'http://127.0.0.1:5179'
const screenshotDir = new URL('../test-results/', import.meta.url)
await mkdir(screenshotDir, { recursive: true })
const browser = await chromium.launch({ headless: true })
const page = await browser.newPage({ viewport: { width: 390, height: 844 }, locale: 'zh-CN' })
page.setDefaultTimeout(8000)

/** uni 将占位文字渲染为独立节点，通过控件容器定位真实输入元素。 */
function field(label) {
  return page.locator('uni-input:visible, uni-textarea:visible').filter({ hasText: label }).locator('input, textarea')
}

/** 点击可见业务按钮本身，兼容 uni-button 内部文字的指针事件屏蔽。 */
async function button(label) {
  await page.locator('uni-button:visible').filter({ has: page.getByText(label, { exact: true }) }).first().click()
}

/** 从固定底栏切换工作区，不注入路由或应用状态。 */
async function tab(label) {
  await page.locator('.uni-tabbar__item').filter({ hasText: label }).click()
}

/** 等待演示工作台完成路由切换。 */
async function enterDemo() {
  await button('体验演示')
  await button('正式员工')
  await page.getByText('常用应用', { exact: true }).waitFor()
}

/** 只通过真实 DOM 焦点确认扫码输入框已恢复，不读取应用内部状态。 */
async function hasScanFocus() {
  return page.evaluate(() => {
    const active = document.activeElement
    const scanInput = document.querySelector('.scan-field input')
    return scanInput != null && active === scanInput
  })
}

const failures = []
function check(condition, message) {
  if (!condition) failures.push(message)
}

try {
  await page.goto(baseUrl, { waitUntil: 'networkidle' })
  await page.getByText('工号登录', { exact: true }).waitFor()
  await enterDemo()

  await tab('我的')
  await button('设置')
  await page.locator('.scan-setting').filter({ has: page.getByText('硬件扫码头', { exact: true }) }).locator('uni-button').click()
  await button('保存设置')
  await page.locator('.save-success:visible').getByText('设置已保存', { exact: true }).waitFor()
  await page.goBack()
  await tab('工作台')
  await button('物品放行申请')

  const placeholder = '按设备扫码键，或输入后确认'
  const scan = field(placeholder)
  await scan.waitFor()
  check(await page.getByText('确认扫码内容', { exact: true }).count() === 0, '硬件扫码头模式不应显示确认扫码内容按钮')

  await scan.focus()
  await scan.pressSequentially('00001234', { delay: 0 })
  await scan.press('Enter')
  await page.getByText('00001234', { exact: true }).waitFor()
  await page.waitForTimeout(200)
  check(await hasScanFocus(), '首次回车添加后扫码输入框应自动获得焦点')

  // 第二次完全依赖真实键盘当前焦点，验证无需再次点击输入框即可连续扫码。
  await page.keyboard.type('00000042', { delay: 0 })
  await page.keyboard.press('Enter')
  await page.getByText('00000042', { exact: true }).waitFor()
  await page.waitForTimeout(200)
  check(await hasScanFocus(), '连续扫码第二次添加后扫码输入框应保持焦点')

  const chips = page.locator('.seal-chip:visible')
  check(await chips.count() === 2, '两次扫码后应显示两枚封条标签')
  if (await chips.count() >= 2) {
    const values = (await chips.locator('.seal-value').allTextContents()).map((value) => value.trim())
    check(values[0] === '00001234' && values[1] === '00000042', '封条标签应按扫描先后排列，最新结果追加在最后')
    const firstChip = await chips.nth(0).boundingBox()
    const fieldBox = await scan.boundingBox()
    check(firstChip != null && fieldBox != null && firstChip.y + firstChip.height <= fieldBox.y + 1, '封条标签应位于编辑框上方')
    check(await chips.nth(0).locator('.seal-remove .app-icon').count() === 1, '每枚封条应使用统一 AppIcon 删除图标')
    const removeBox = await chips.nth(0).locator('.seal-remove').boundingBox()
    check(removeBox != null && removeBox.width >= 44 && removeBox.height >= 44, '封条删除按钮触控区域应至少为44px')
  }

  // 连续加入更多封条后，列表应在有限高度内滚动，新增的最新封条仍保持可见且焦点不丢失。
  for (const value of ['00000043', '00000044', '00000045', '00000046']) {
    await page.keyboard.type(value, { delay: 0 })
    await page.keyboard.press('Enter')
    await page.getByText(value, { exact: true }).waitFor()
  }
  await page.waitForTimeout(200)
  const sealList = page.locator('.seal-list:visible')
  const listBox = await sealList.boundingBox()
  const latestChip = await chips.last().boundingBox()
  const latestValue = (await chips.last().locator('.seal-value').textContent() || '').trim()
  check(await chips.count() === 6, '连续扫码六次后应显示全部六枚封条')
  check(listBox != null && listBox.height >= 44 && listBox.height <= 180, '六枚封条列表高度应受约156px上限约束')
  check(latestValue === '00000046', '限高列表的最后一枚封条应为最新扫码结果')
  check(
    listBox != null && latestChip != null && latestChip.y >= listBox.y - 1 && latestChip.y + latestChip.height <= listBox.y + listBox.height + 1,
    '连续扫码后最新封条应在列表可视区域内',
  )
  check(await hasScanFocus(), '连续扫码六次后扫码输入框仍应保持真实键盘焦点')

  const longCode = '1234567890123456789012345678901234567890'
  await page.keyboard.type(longCode, { delay: 0 })
  await page.keyboard.press('Enter')
  await page.getByText(longCode, { exact: true }).waitFor()
  await page.waitForTimeout(200)
  const longChip = chips.filter({ has: page.getByText(longCode, { exact: true }) })
  const shortChip = chips.nth(0)
  const nextShortChip = chips.nth(1)
  const longBox = await longChip.boundingBox()
  const shortBox = await shortChip.boundingBox()
  const nextShortBox = await nextShortChip.boundingBox()
  const longListBox = await sealList.boundingBox()
  check(shortBox != null && nextShortBox != null && Math.abs(shortBox.y - nextShortBox.y) <= 1, '短封条应按编号长度在同一行排列')
  check(longBox != null && shortBox != null && longBox.width > shortBox.width + 16, '长短封条标签宽度应随编号长度自适应')
  check(
    longListBox != null && longBox != null && longBox.x >= longListBox.x - 1 && longBox.x + longBox.width <= longListBox.x + longListBox.width + 1,
    '窄屏下长封条不得横向溢出列表区域',
  )
  check(await hasScanFocus(), '新增长封条后扫码输入框仍应保持真实键盘焦点')
  await sealList.screenshot({ path: new URL('scan-entry-short-row.png', screenshotDir).pathname })
  await page.setViewportSize({ width: 320, height: 844 })
  await page.waitForTimeout(200)
  await sealList.screenshot({ path: new URL('scan-entry-long-320.png', screenshotDir).pathname })
  await page.setViewportSize({ width: 390, height: 844 })

  // 切换扫码方式时申请草稿和已有封条仍应保留，封条位置继续紧邻当前扫码控件。
  await button('前往设置')
  await page.locator('.scan-setting').filter({ has: page.getByText('摄像头', { exact: true }) }).locator('uni-button').click()
  await button('保存设置')
  await page.locator('.save-success:visible').getByText('设置已保存', { exact: true }).waitFor()
  await page.goBack()
  await page.getByText('打开摄像头扫码', { exact: true }).waitFor()
  check(await chips.count() === 7, '切换摄像头模式后已有封条仍应显示')
  check(await page.getByText(longCode, { exact: true }).count() === 1, '切换摄像头模式后最新封条编号仍应保留')

  await button('前往设置')
  await page.locator('.scan-setting').filter({ has: page.getByText('硬件扫码头', { exact: true }) }).locator('uni-button').click()
  await button('保存设置')
  await page.locator('.save-success:visible').getByText('设置已保存', { exact: true }).waitFor()
  await page.goBack()
  await scan.waitFor()

  if (await longChip.count() > 0 && await longChip.locator('.seal-remove').count() > 0) {
    await longChip.locator('.seal-remove').click()
    await page.waitForTimeout(200)
    check(await page.getByText(longCode, { exact: true }).count() === 0, '删除封条后不应继续显示被删除的编号')
    check(await page.getByText('00000046', { exact: true }).count() === 1, '删除封条后其余编号应继续保留')
    const focusAfterDelete = await hasScanFocus()
    check(focusAfterDelete, '删除封条后扫码输入框应自动获得焦点')
    await page.keyboard.type('00009999', { delay: 0 })
    await page.keyboard.press('Enter')
    await page.getByText('00009999', { exact: true }).waitFor()
    check(await chips.count() === 7, '删除后无需手动点击即可继续添加新的封条')
    check(((await chips.last().locator('.seal-value').textContent()) || '').trim() === '00009999', '删除再扫描的编号仍追加到最后')
  } else {
    failures.push('封条标签缺少可操作的删除按钮')
  }

  // 等过去重窗口，再验证重复扫码不会新增封条且下一次异常扫码仍能恢复。
  await page.waitForTimeout(1100)
  await field(placeholder).focus()
  await page.keyboard.type('00001234', { delay: 0 })
  await page.keyboard.press('Enter')
  await page.waitForTimeout(100)
  check(await chips.count() === 7, '重复封条不应新增标签')

  await field(placeholder).fill('x'.repeat(4097))
  await field(placeholder).press('Enter')
  await page.getByText('扫码内容不能超过4096字符', { exact: true }).waitFor()
  check(await field(placeholder).inputValue() === '', '异常超长码处理后输入框应清空')
  await page.waitForTimeout(200)
  check(await hasScanFocus(), '异常扫码后输入框应恢复焦点以继续扫码')

  await page.keyboard.type('00008888', { delay: 0 })
  await page.keyboard.press('Enter')
  await page.getByText('00008888', { exact: true }).waitFor()
  check(await chips.count() === 8, '异常扫码后新的有效码仍应能添加')

  // 验证 scroll-view 内部真正限高且可滚到最旧封条，避免只检查外层尺寸。
  const scrollMetrics = await sealList.evaluate((node) => {
    const candidates = [node, ...node.querySelectorAll('*')]
    const scrollNode = candidates.find((candidate) => candidate.clientHeight > 0 && candidate.scrollHeight > candidate.clientHeight + 1)
    if (scrollNode == null) return null
    return { clientHeight: scrollNode.clientHeight, scrollHeight: scrollNode.scrollHeight }
  })
  check(scrollMetrics != null, '封条列表应存在真实可滚动的内部节点')
  if (scrollMetrics != null) {
    check(scrollMetrics.clientHeight <= 180, '封条内部滚动视口高度应受约156px上限约束')
    check(scrollMetrics.scrollHeight > scrollMetrics.clientHeight, '封条内部滚动内容高度应大于视口高度')
  }
  const oldestChip = chips.filter({ has: page.getByText('00001234', { exact: true }) }).first()
  await sealList.hover()
  await page.mouse.wheel(0, -1200)
  await page.waitForTimeout(300)
  const oldestBox = await oldestChip.boundingBox()
  const scrollBox = await sealList.boundingBox()
  check(
    oldestBox != null && scrollBox != null && oldestBox.y >= scrollBox.y - 1 && oldestBox.y + oldestBox.height <= scrollBox.y + scrollBox.height + 1,
    '滚动封条列表后最旧编号应进入可视区域',
  )
  if (oldestBox != null && scrollBox != null && oldestBox.y >= scrollBox.y - 1 && oldestBox.y + oldestBox.height <= scrollBox.y + scrollBox.height + 1) {
    await oldestChip.locator('.seal-remove').click()
    await page.waitForTimeout(200)
    check(await page.getByText('00001234', { exact: true }).count() === 0, '滚动到最旧封条后应可点击删除')
  }
} finally {
  await browser.close()
}

if (failures.length > 0) {
  throw new Error('连续扫码 RED：\n' + failures.map((message) => '- ' + message).join('\n'))
}

console.log('连续扫码、封条顺序、删除、异常恢复与自动回焦通过')
