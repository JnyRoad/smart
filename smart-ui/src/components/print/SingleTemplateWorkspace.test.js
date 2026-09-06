// 使用真实工作台和宿主，仅替换需要浏览器 Worker 的 pdfme 画布边界。
import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Workspace from './SingleTemplateWorkspace.vue'
import { loadPdfmeRuntime } from './pdfme-runtime'

vi.mock('./pdfme-runtime', () => ({ loadPdfmeRuntime: vi.fn() }))
const font = { NotoSansSC: { data: new Uint8Array([1]), fallback: true } }
let wrapper
let pending = false

/** 合成两份正面、一个共用背面和一份访客模板，各自只有一页。 */
function examples() {
  return [
    ['front-a', '员工正面 A', 'STAFF_CARD', 'FRONT', '正面初稿'],
    ['front-b', '员工正面 B', 'STAFF_CARD', 'FRONT', '另一正面'],
    ['back', '通用背面', 'STAFF_CARD', 'BACK', '背面须知'],
    ['visitor', '访客凭条', 'VISITOR_SLIP', 'FRONT', '示例访客']
  ].map(([id, name, printItemType, faceRole, content]) => ({
    id, name, printItemType, faceRole,
    template: { basePdf: { width: 85.6, height: 53.98, padding: [0, 0, 0, 0] }, schemas: [[{
      name: 'title', type: 'text', content, position: { x: 5, y: 5 }, width: 40, height: 8
    }]] }
  }))
}

/** 等待 Vue 宿主的异步挂载。 */
async function settle() { await new Promise(resolve => setTimeout(resolve, 0)) }

/** 通过用户可见按钮操作，缺失时给出断言而非选择器异常。 */
async function click(label) {
  const button = wrapper.findAll('button').wrappers.find(item => item.text() === label)
  expect(button, `应提供 ${label} 操作`).toBeDefined()
  await button.trigger('click')
  await settle()
}

beforeEach(async () => {
  pending = false
  loadPdfmeRuntime.mockResolvedValue({
    mountDesigner({ domContainer, template }) {
      const input = document.createElement('textarea')
      input.setAttribute('aria-label', '画布文字')
      input.value = template.schemas[0][0].content
      domContainer.appendChild(input)
      return {
        getTemplate() {
          if (pending) throw new Error('属性正在应用，请稍后操作')
          const snapshot = JSON.parse(JSON.stringify(template))
          snapshot.schemas[0][0].content = input.value
          return snapshot
        },
        destroy() { input.remove() }
      }
    }
  })
  wrapper = mount(Workspace, { propsData: { initialTemplates: examples(), font } })
  await settle()
})
afterEach(() => wrapper.destroy())

it('每次只编辑一面，切换后保留各自草稿并独立保存重开', async () => {
  expect(wrapper.findAll('textarea')).toHaveLength(1)
  await wrapper.find('textarea').setValue('正面已修改')
  await click('通用背面')
  expect(wrapper.find('textarea').element.value).toBe('背面须知')
  await wrapper.find('textarea').setValue('背面已修改')
  await click('保存当前模板')
  await click('员工正面 A')
  expect(wrapper.find('textarea').element.value).toBe('正面已修改')
  await click('保存当前模板')
  await wrapper.find('textarea').setValue('未保存内容')
  await click('重开已保存版本')
  expect(wrapper.find('textarea').element.value).toBe('正面已修改')
  await click('通用背面')
  expect(wrapper.find('textarea').element.value).toBe('背面已修改')
  expect(wrapper.findAll('textarea')).toHaveLength(1)
})

it('组合固定各自版本，正面另存新版不会替换已关联版本', async () => {
  await click('保存当前模板')
  await wrapper.find('[aria-label="正面版本"]').setValue('front-a@2')
  await click('关联为厂牌组合')
  expect(wrapper.find('[aria-label="已关联组合"]').text()).toContain('员工正面 A · v2')
  expect(wrapper.find('[aria-label="已关联组合"]').text()).toContain('通用背面 · v1')
  await wrapper.find('textarea').setValue('新正面第三版')
  await click('保存当前模板')
  await wrapper.find('[aria-label="正面版本"]').setValue('front-b@1')
  await wrapper.find('[aria-label="翻面方式"]').setValue('AUTO_DUPLEX')
  await click('恢复已关联组合')
  expect(wrapper.find('[aria-label="正面版本"]').element.value).toBe('front-a@2')
  expect(wrapper.find('[aria-label="背面版本"]').element.value).toBe('back@1')
  expect(wrapper.find('[aria-label="翻面方式"]').element.value).toBe('AUTO_DUPLEX')
  expect(wrapper.find('[aria-label="已关联组合"]').text()).not.toContain('v3')
})

it('访客只编辑一份模板，不提供背面或翻面选项', async () => {
  await click('访客凭条')
  expect(wrapper.find('textarea').element.value).toBe('示例访客')
  expect(wrapper.find('[aria-label="背面版本"]').exists()).toBe(false)
  expect(wrapper.find('[aria-label="翻面方式"]').exists()).toBe(false)
  await click('保存当前模板')
  await click('重开已保存版本')
  expect(wrapper.findAll('textarea')).toHaveLength(1)
})

it('属性待应用时阻止保存和切换，保留原模板直到可安全读取', async () => {
  expect(wrapper.findAll('textarea')).toHaveLength(1)
  await wrapper.find('textarea').setValue('尚在输入的新正面')
  pending = true
  await click('通用背面')
  expect(wrapper.find('[role="status"]').text()).toMatch(/稍后/)
  expect(wrapper.find('textarea').element.value).toBe('尚在输入的新正面')
  await click('保存当前模板')
  expect(wrapper.find('[aria-label="当前模板摘要"]').text()).toContain('v1')
  pending = false
  await click('保存当前模板')
  await click('通用背面')
  await click('员工正面 A')
  expect(wrapper.find('textarea').element.value).toBe('尚在输入的新正面')
  expect(wrapper.find('[aria-label="当前模板摘要"]').text()).toContain('v2')
})

it.each(['资源加载失败', '模板页数错误'])('%s 后仍能切换到其他模板恢复', async reason => {
  wrapper.destroy()
  const templates = examples()
  if (reason === '资源加载失败') loadPdfmeRuntime.mockRejectedValueOnce(new Error(reason))
  else templates[0].template.schemas.push([])
  wrapper = mount(Workspace, { propsData: { initialTemplates: templates, font } })
  await settle()
  const backButton = wrapper.findAll('button').wrappers.find(button => button.text() === '通用背面')
  expect(backButton.attributes('disabled')).toBeUndefined()
  await click('通用背面')
  expect(wrapper.find('textarea').element.value).toBe('背面须知')
  await click('保存当前模板')
  expect(wrapper.find('[aria-label="当前模板摘要"]').text()).toContain('v2')
})

it('资源加载失败后可点击当前模板重试，已有草稿仍保留', async () => {
  await wrapper.find('textarea').setValue('草稿仍需保留')
  await click('通用背面')
  loadPdfmeRuntime.mockRejectedValueOnce(new Error('资源加载失败'))
  await click('员工正面 A')
  const currentButton = wrapper.findAll('button').wrappers.find(button => button.text() === '员工正面 A')
  expect(currentButton.attributes('disabled')).toBeUndefined()
  await click('员工正面 A')
  expect(wrapper.find('textarea').element.value).toBe('草稿仍需保留')
})
