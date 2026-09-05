// 只替换 pdfme 渲染边界，快捷键绑定 document、弹层挂到 body，宿主冻结逻辑保持真实。
import { mount } from '@vue/test-utils'
import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import Designer from './PdfmeDesigner.vue'
import { loadPdfmeRuntime } from './pdfme-runtime'

vi.mock('./pdfme-runtime', () => ({ loadPdfmeRuntime: vi.fn() }))

const template = { basePdf: { width: 85.6, height: 53.98, padding: [0, 0, 0, 0] }, schemas: [[{ name: '姓名', type: 'text', content: '保存前', position: { x: 5, y: 5 }, width: 50, height: 8 }]] }
const clone = value => JSON.parse(JSON.stringify(value))
let wrapper
let portal
let cleanup = []

beforeEach(() => {
  vi.resetAllMocks()
  cleanup = []
  vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, arrayBuffer: async () => new ArrayBuffer(16437364) }))
  loadPdfmeRuntime.mockResolvedValue({ mountDesigner(options) {
    const snapshot = clone(options.template)
    const publish = () => options.onChange(clone(snapshot))
    const shortcut = event => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'z') snapshot.schemas[0][0].content = '撤销后'
      else if (event.key.startsWith('Arrow')) snapshot.schemas[0][0].position.x += 1
      else if (['Delete', 'Backspace'].includes(event.key)) snapshot.schemas[0] = []
      else return
      publish()
    }
    document.addEventListener('keydown', shortcut)
    portal = document.createElement('div')
    portal.setAttribute('role', 'dialog')
    const input = document.createElement('input')
    input.value = snapshot.schemas[0][0].content
    input.addEventListener('input', () => { snapshot.schemas[0][0].content = input.value; publish() })
    const button = document.createElement('button')
    button.textContent = '应用颜色'
    button.addEventListener('click', () => { snapshot.schemas[0][0].fontColor = '#ffffff'; publish() })
    portal.append(input, button)
    document.body.appendChild(portal)
    return {
      getTemplate: () => clone(snapshot),
      destroy() { document.removeEventListener('keydown', shortcut); portal.remove() }
    }
  } })
})

afterEach(() => {
  if (wrapper) { wrapper.destroy(); wrapper.element.remove(); wrapper = null }
  cleanup.forEach(remove => remove())
  vi.unstubAllGlobals()
})

async function createDesigner() {
  wrapper = mount(Designer, { attachTo: document.body, propsData: { template: clone(template), printItemType: 'STAFF_CARD' } })
  await new Promise(resolve => setTimeout(resolve, 0))
  expect(wrapper.vm.getTemplate().schemas[0][0].content).toBe('保存前')
  return wrapper
}

const keyEvent = options => new KeyboardEvent('keydown', { ...options, bubbles: true, cancelable: true })

it.each([
  ['Ctrl+Z', { key: 'z', ctrlKey: true }],
  ['Cmd+Z', { key: 'z', metaKey: true }],
  ['方向键', { key: 'ArrowRight' }],
  ['Shift+方向键', { key: 'ArrowLeft', shiftKey: true }],
  ['Delete', { key: 'Delete' }],
  ['Backspace', { key: 'Backspace' }]
])('保存期间 document 的%s不能改画布，解除冻结后恢复原快捷键', async (_, options) => {
  await createDesigner()
  await wrapper.setProps({ disabled: true })
  const before = wrapper.vm.getTemplate()
  const blocked = keyEvent(options)
  document.body.dispatchEvent(blocked)
  expect(wrapper.vm.getTemplate()).toEqual(before)
  expect(blocked.defaultPrevented).toBe(true)
  await wrapper.setProps({ disabled: false })
  document.body.dispatchEvent(keyEvent(options))
  expect(wrapper.vm.getTemplate()).not.toEqual(before)
})

it('外置弹层输入和按钮在冻结期间不能改画布，解除后恢复编辑', async () => {
  await createDesigner()
  expect(wrapper.element.contains(portal)).toBe(false)
  const input = portal.querySelector('input')
  input.focus()
  const enterText = text => {
    const beforeInput = new InputEvent('beforeinput', { bubbles: true, cancelable: true, inputType: 'insertText', data: text })
    if (input.dispatchEvent(beforeInput)) { input.value = text; input.dispatchEvent(new Event('input', { bubbles: true })) }
  }
  await wrapper.setProps({ disabled: true })
  const before = wrapper.vm.getTemplate()
  enterText('冻结期间新内容')
  portal.querySelector('button').click()
  expect(wrapper.vm.getTemplate()).toEqual(before)
  expect(input.value).toBe('保存前')
  await wrapper.setProps({ disabled: false })
  enterText('恢复编辑')
  portal.querySelector('button').click()
  expect(wrapper.vm.getTemplate().schemas[0][0]).toMatchObject({ content: '恢复编辑', fontColor: '#ffffff' })
})

it('冻结期间键盘释放仍能清除快捷键状态，卸载后不拦截其他页面', async () => {
  await createDesigner()
  let released = false
  let otherPageEdits = 0
  const release = () => { released = true }
  const otherPage = () => { otherPageEdits++ }
  document.addEventListener('keyup', release)
  document.addEventListener('keydown', otherPage)
  cleanup.push(() => document.removeEventListener('keyup', release), () => document.removeEventListener('keydown', otherPage))
  await wrapper.setProps({ disabled: true })
  document.body.dispatchEvent(new KeyboardEvent('keyup', { key: 'Control', bubbles: true }))
  expect(released).toBe(true)
  document.body.dispatchEvent(keyEvent({ key: 'ArrowRight' }))
  expect(otherPageEdits).toBe(0)
  wrapper.destroy()
  const afterDestroy = keyEvent({ key: 'ArrowRight' })
  document.body.dispatchEvent(afterDestroy)
  expect(otherPageEdits).toBe(1)
  expect(afterDestroy.defaultPrevented).toBe(false)
})
