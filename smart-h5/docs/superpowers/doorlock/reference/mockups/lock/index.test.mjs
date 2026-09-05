import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import vm from 'node:vm'

const mockupPath = new URL('./index.html', import.meta.url)
const mockup = readFileSync(mockupPath, 'utf8')
const script = mockup.match(/<script>([\s\S]*?)<\/script>/)?.[1]

assert.ok(script, '门锁原型必须包含可执行的状态脚本')

/** 提供状态脚本所需的最小 DOM 属性，测试仍执行 HTML 中的真实函数。 */
class FakeElement {
  constructor(id) {
    this.id = id
    this.className = ''
    this.textContent = ''
    this.value = ''
    this.disabled = false
    this.style = { display: '' }
    this.classList = {
      add: (name) => {
        this.className = `${this.className} ${name}`.trim()
      },
      remove: (name) => {
        this.className = this.className
          .split(/\s+/)
          .filter((current) => current && current !== name)
          .join(' ')
      },
      contains: (name) => this.className.split(/\s+/).includes(name),
    }
  }
}

/** 从当前 HTML 的 id 属性构造测试 DOM，避免脚本与标记脱节时假绿。 */
function runMockup() {
  const ids = [...mockup.matchAll(/\bid="([^"]+)"/g)].map((match) => match[1])
  assert.equal(new Set(ids).size, ids.length, '门锁原型的 id 必须唯一')
  const elements = new Map(ids.map((id) => [id, new FakeElement(id)]))
  const alerts = []
  const context = {
    alert: (message) => alerts.push(message),
    clearTimeout: () => {},
    document: { getElementById: (id) => elements.get(id) },
    setTimeout: () => 1,
  }

  const sandbox = vm.createContext(context)
  vm.runInContext(script, sandbox, { filename: mockupPath.pathname })
  return { alerts, elements, context: sandbox }
}

/** 读取指定按钮在 HTML 中声明的内联入口。 */
function buttonForLabel(label) {
  const line = mockup.split('\n').find((current) => current.includes(`>${label}</button>`))
  const id = line?.match(/\bid="([^"]+)"/)?.[1]
  const handler = line?.match(/onclick="([^"]+)"/)?.[1]
  assert.ok(id, `找不到「${label}」按钮的 id`)
  assert.ok(handler, `找不到「${label}」按钮的内联入口`)
  return { id, handler }
}

/** 模拟浏览器对 disabled 按钮的点击分发，再执行 HTML 内联处理器。 */
function clickAction(label, elements, context) {
  const button = buttonForLabel(label)
  const element = elements.get(button.id)
  assert.ok(element, `按钮「${label}」未映射到测试 DOM`)
  if (element.disabled) return false
  vm.runInContext(button.handler, context, { filename: mockupPath.pathname })
  return true
}

/** 执行 HTML 中其他真实内联入口，覆盖绕过按钮点击的调用路径。 */
function runInline(handler, context) {
  vm.runInContext(handler, context, { filename: mockupPath.pathname })
}

test('确认未入住后编辑动作应保持不可用且不能修改动态码', () => {
  const submitHandler = mockup.match(/<button class="ok" onclick="([^"]+)">确定<\/button>/)?.[1]
  assert.ok(submitHandler, '找不到修改弹窗的确定入口')

  const { elements, context } = runMockup()
  context.setState('out')
  context.confirmOut()

  const clicked = clickAction('修改动态码', elements, context)
  elements.get('newPwd').value = '123456'
  runInline(submitHandler, context)

  assert.deepEqual(
    {
      clicked,
      editDialogVisible: elements.get('editMask').classList.contains('show'),
      code: context.curCode,
    },
    { clicked: false, editDialogVisible: false, code: '638274' },
  )
})

test('确认未入住后刷新动作应不可执行', () => {
  const { alerts, elements, context } = runMockup()
  context.setState('out')
  context.confirmOut()

  const clicked = clickAction('刷新动态码（人脸识别）', elements, context)

  assert.deepEqual({ clicked, alertCount: alerts.length }, { clicked: false, alertCount: 0 })
})

test('未入住态直接调用编辑、提交和刷新入口也不能动作', () => {
  const { alerts, elements, context } = runMockup()
  context.setState('in')
  context.openEdit()
  elements.get('newPwd').value = '123456'
  context.setState('out')
  assert.equal(elements.get('editMask').classList.contains('show'), false)
  context.confirmOut()

  context.openEdit()
  assert.equal(elements.get('editMask').classList.contains('show'), false)
  elements.get('newPwd').value = '123456'
  context.submitEdit()
  context.refreshLock()

  assert.deepEqual(
    {
      editDialogVisible: elements.get('editMask').classList.contains('show'),
      code: context.curCode,
      alertCount: alerts.length,
    },
    { editDialogVisible: false, code: '638274', alertCount: 0 },
  )
})

test('从未入住切回已入住应恢复编辑和刷新动作', () => {
  const { alerts, elements, context } = runMockup()
  context.setState('out')
  context.setState('in')

  assert.equal(elements.get('codeNum').textContent, '638274')
  assert.equal(elements.get('outMask').classList.contains('show'), false)
  assert.equal(elements.get('editBtn').disabled, false)
  assert.equal(elements.get('refreshBtn').disabled, false)

  assert.equal(clickAction('修改动态码', elements, context), true)
  assert.equal(elements.get('editMask').classList.contains('show'), true)
  elements.get('newPwd').value = '123456'
  context.submitEdit()
  assert.equal(context.curCode, '123456')
  assert.equal(elements.get('codeNum').textContent, '123456')
  assert.equal(clickAction('刷新动态码（人脸识别）', elements, context), true)
  assert.equal(alerts.length, 1)
})
