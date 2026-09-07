/** 批量检查统一客户端页面的截图、运行时异常和跨视口布局边界。 */
import assert from 'node:assert/strict'
import { createRequire } from 'node:module'
import { mkdir } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const require = createRequire(
  process.env.PLAYWRIGHT_MODULE_PATH
    ? path.join(process.env.PLAYWRIGHT_MODULE_PATH, 'package.json')
    : import.meta.url,
)
const { chromium } = require('playwright')

const projectRoot = fileURLToPath(new URL('../', import.meta.url))
const screenshotDir = path.join(projectRoot, 'test-results', 'ui', 'ignored')
const previewUrl = (process.env.CLIENT_PREVIEW_URL || 'http://127.0.0.1:5179').replace(/\/$/, '')
const pageErrors = []

await mkdir(screenshotDir, { recursive: true })

const browser = await chromium.launch({ headless: true })
const page = await browser.newPage({
  viewport: { width: 390, height: 844 },
  locale: 'zh-CN',
})
page.setDefaultTimeout(8000)
page.setDefaultNavigationTimeout(15000)
page.on('pageerror', error => {
  pageErrors.push({ message: error.message, url: page.url() })
})

/** 等待页面的稳定中文标题，避免用固定延时掩盖路由或渲染错误。 */
async function waitForText(value) {
  const scope = value === '工号登录' ? page : page.locator('.page:visible')
  const locator = scope.getByText(value, { exact: true }).first()
  await locator.waitFor({ state: 'visible' })
  return locator
}

/** 点击编译后 uni-button 的可见文字，保持与现有 E2E 的定位方式一致。 */
async function clickButton(label) {
  const locator = page
    .locator('uni-button:visible')
    .filter({ has: page.getByText(label, { exact: true }) })
    .first()
  await locator.waitFor({ state: 'visible' })
  await locator.click()
}

/** 以 hash 切换本地页面，保留当前演示会话和同一页面上下文。 */
async function navigateHash(route) {
  await page.evaluate(nextRoute => {
    window.location.hash = nextRoute
  }, route)
  await page.waitForFunction(expected => window.location.hash.includes(expected), route)
}

/** 检查文档根节点和 body 是否产生横向滚动条。 */
async function checkDocumentOverflow(label) {
  const dimensions = await page.evaluate(() => {
    const root = document.documentElement
    return {
      viewportWidth: window.innerWidth,
      documentWidth: root.scrollWidth,
      bodyWidth: document.body ? document.body.scrollWidth : 0,
    }
  })
  const widest = Math.max(dimensions.documentWidth, dimensions.bodyWidth)
  assert.ok(
    widest <= dimensions.viewportWidth + 2,
    `${label} 存在横向溢出：viewport=${dimensions.viewportWidth}, document=${dimensions.documentWidth}, body=${dimensions.bodyWidth}`,
  )
}

/** 检查可见交互控件有尺寸并保持在当前视口水平范围内。 */
async function checkVisibleControls(label) {
  const controls = await page.evaluate(() => {
    const selector =
      'button, input, textarea, select, [role="button"], uni-button, uni-input, uni-textarea, uni-picker, .uni-tabbar__item'
    return Array.from(document.querySelectorAll(selector))
      .filter(element => {
        const style = window.getComputedStyle(element)
        const rect = element.getBoundingClientRect()
        return (
          style.display !== 'none' &&
          style.visibility !== 'hidden' &&
          Number(style.opacity || 1) > 0 &&
          rect.width > 0 &&
          rect.height > 0
        )
      })
      .map(element => {
        const rect = element.getBoundingClientRect()
        return {
          tag: element.tagName.toLowerCase(),
          text: (element.textContent || '').trim().replace(/\s+/g, ' ').slice(0, 80),
          left: rect.left,
          right: rect.right,
          width: rect.width,
          height: rect.height,
        }
      })
  })
  const viewportWidth = page.viewportSize().width
  const outside = controls.filter(control => control.left < -2 || control.right > viewportWidth + 2)
  assert.equal(
    outside.length,
    0,
    `${label} 存在越界交互控件：${JSON.stringify(outside.slice(0, 4))}`,
  )
}

/** 检查页签数量、文字和图标资源；无底栏的详情页不强制创建底栏。 */
async function checkTabIcons(label, required = false) {
  const tabs = page.locator('.uni-tabbar__item:visible')
  const count = await tabs.count()
  if (required) assert.equal(count, 4, `${label} 应显示四个底部页签`)
  if (count === 0) return

  for (const tabLabel of ['工作台', '待办', '消息', '我的']) {
    assert.equal(
      await tabs.filter({ hasText: tabLabel }).count(),
      1,
      `${label} 缺少页签：${tabLabel}`,
    )
  }

  const iconCount = await page.locator('.uni-tabbar__item:visible .uni-tabbar__icon img:visible').count()
  assert.equal(iconCount, count, `${label} 页签图标数量与页签数量不一致`)
  const icons = await tabs.evaluateAll(items =>
    items.map(item => {
      const image = item.querySelector('.uni-tabbar__icon img')
      return image == null ? '' : image.getAttribute('src') || ''
    }),
  )
  assert.ok(icons.every(source => source.length > 0), `${label} 存在没有资源地址的页签图标`)
}

/** 对单页执行标题、溢出、控件和可选页签检查，再保存完整页面截图。 */
async function captureRoute(route, name, title, options = {}) {
  await navigateHash(route)
  await waitForText(title)
  await checkDocumentOverflow(name)
  await checkVisibleControls(name)
  await checkTabIcons(name, options.requiredTabs === true)
  await page.screenshot({
    path: path.join(screenshotDir, `${name}.png`),
    fullPage: true,
  })
  console.log(`已检查并截图：${name}`)
}

/** 在登录页显式进入某个演示身份；候选文案兼容外包/派遣标签调整。 */
async function loginDemo(labels) {
  await page.goto(previewUrl, { waitUntil: 'networkidle' })
  await waitForText('工号登录')
  await clickButton('体验演示')
  for (const label of labels) {
    const locator = page
      .locator('uni-button:visible')
      .filter({ has: page.getByText(label, { exact: true }) })
      .first()
    if ((await locator.count()) > 0) {
      await locator.click()
      await waitForText('常用应用')
      return label
    }
  }
  throw new Error(`没有找到演示身份按钮：${labels.join('、')}`)
}

/** 审批页单独检查同排动作的间隔，避免窄屏按钮重叠。 */
async function checkApprovalLayout() {
  await waitForText('审批处理')
  const approve = page
    .locator('uni-button:visible')
    .filter({ has: page.getByText('同意申请', { exact: true }) })
    .first()
  const reject = page
    .locator('uni-button:visible')
    .filter({ has: page.getByText('驳回申请', { exact: true }) })
    .first()
  await approve.scrollIntoViewIfNeeded()
  const approveBox = await approve.boundingBox()
  const rejectBox = await reject.boundingBox()
  assert.ok(approveBox != null && rejectBox != null, '审批按钮没有可测量的边界')
  assert.ok(Math.abs(approveBox.y - rejectBox.y) <= 2, '审批按钮没有保持在同一行')
  assert.ok(approveBox.x - (rejectBox.x + rejectBox.width) >= 10, '审批按钮之间的间距不足10px')
  await page.screenshot({path:path.join(screenshotDir, 'approval-actions-' + page.viewportSize().width + '.png'), fullPage:true})
}

/** 在同一演示身份下检查一组页面，不通过逐页重载破坏内存状态。 */
async function captureRoutes(routes) {
  for (const route of routes) await captureRoute(route.route, route.name, route.title, route.options)
}

/** 设置授权东门和硬件扫码，再核验演示厂牌，检查完整人员卡。 */
async function prepareSupplierVerification() {
  await navigateHash('/pages/settings/settings')
  await waitForText('安检岗位')
  await page.locator('uni-picker:visible').click()
  await page.getByText('演示园区东门', {exact:true}).and(page.locator(':visible')).last().waitFor()
  const confirm = page.locator('.uni-picker-action-confirm:visible')
  if (await confirm.count() > 0) await confirm.last().click()
  else await page.getByText('演示园区东门', {exact:true}).and(page.locator(':visible')).last().click()
  await page.locator('.scan-setting').filter({has:page.getByText('硬件扫码头',{exact:true})}).locator('uni-button').click()
  await clickButton('保存设置')
  await waitForText('设置已保存')
  await navigateHash('/pages/supplier-access/supplier-access')
  await waitForText('供应商厂牌核验')
  const input = page.locator('.page:visible uni-input').locator('input')
  await input.fill('DEMO-BADGE-001')
  await input.press('Enter')
  await waitForText('核验通过')
}

/** 检查指定视口下的三个关键页面和供应商两页。 */
async function captureKeyViewport(width, height) {
  await page.setViewportSize({ width, height })
  await loginDemo(['正式员工'])
  await captureRoute('/pages/workbench/workbench', `workbench-${width}x${height}`, '常用应用', {
    requiredTabs: true,
  })

  await loginDemo(['审批主管'])
  await captureRoute(
    '/pages/detail/detail?id=demo-release-pending',
    `approval-${width}x${height}`,
    '申请详情',
  )
  await checkApprovalLayout()

  await loginDemo(['现场安检'])
  await prepareSupplierVerification()
  await captureRoute(
    '/pages/supplier-access/supplier-access',
    `supplier-access-${width}x${height}`,
    '供应商厂牌核验',
  )
  await captureRoute(
    '/pages/supplier-records/supplier-records',
    `supplier-records-${width}x${height}`,
    '供应商通行记录',
  )
}

try {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto(previewUrl, { waitUntil: 'networkidle' })
  await waitForText('工号登录')
  await checkDocumentOverflow('login-mobile')
  await checkVisibleControls('login-mobile')
  await page.screenshot({
    path: path.join(screenshotDir, 'login-mobile.png'),
    fullPage: true,
  })
  console.log('已检查并截图：login-mobile')

  await loginDemo(['正式员工'])
  await captureRoutes([
    { route: '/pages/workbench/workbench', name: 'workbench-mobile', title: '常用应用', options: { requiredTabs: true } },
    { route: '/pages/todos/todos', name: 'todos-mobile', title: '待办' },
    { route: '/pages/messages/messages', name: 'messages-mobile', title: '消息' },
    { route: '/pages/mine/mine', name: 'mine-mobile', title: '我的' },
    { route: '/pages/apps/apps', name: 'apps-mobile', title: '全部应用' },
    { route: '/pages/settings/settings', name: 'settings-mobile', title: '设置' },
    { route: '/pages/application/application?kind=item-pass', name: 'release-application-mobile', title: '物品放行申请' },
    { route: '/pages/records/records?kind=item-pass&view=records', name: 'release-records-mobile', title: '物品放行记录' },
  ])

  await loginDemo(['审批主管'])
  await captureRoute(
    '/pages/detail/detail?id=demo-release-pending',
    'approval-mobile',
    '申请详情',
  )
  await checkApprovalLayout()

  await loginDemo(['现场安检'])
  await captureRoutes([
    { route: '/pages/settings/settings', name: 'security-settings-mobile', title: '安检岗位' },
    { route: '/pages/scan/scan?kind=item-pass', name: 'release-scan-mobile', title: '扫码定位单据' },
    { route: '/pages/supplier-access/supplier-access', name: 'supplier-access-mobile', title: '供应商厂牌核验' },
    { route: '/pages/supplier-records/supplier-records', name: 'supplier-records-mobile', title: '供应商通行记录' },
  ])

  await captureKeyViewport(320, 844)
  await captureKeyViewport(768, 1000)
  await captureKeyViewport(1440, 1000)

  assert.deepEqual(pageErrors, [], `发现页面运行时异常：${JSON.stringify(pageErrors)}`)
  console.log(`UI 页面检查完成，截图目录：${screenshotDir}`)
} catch (error) {
  try {
    await page.screenshot({ path: path.join(screenshotDir, 'failure.png'), fullPage: true })
  } catch (_) {
    // 预览服务不可用时可能没有可截图页面，保留原始失败信息。
  }
  throw error
} finally {
  await browser.close()
}
