// 验证 Vue 宿主对模板面数、异步挂载和销毁的约束；真实画布另由浏览器验证。
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import PdfmeHost from './PdfmeHost.vue'
import { loadPdfmeRuntime } from './pdfme-runtime'

vi.mock('./pdfme-runtime', () => ({ loadPdfmeRuntime: vi.fn() }))

const font = { NotoSansSC: { data: new Uint8Array([1]), fallback: true } }
let wrappers = []
let instances = []
let mountDesigner

/** 创建独立的单人模板，面数预期由测试直接指定。 */
function templateFor(pages) {
  return {
    basePdf: { width: 85.6, height: 53.98, padding: [0, 0, 0, 0] },
    schemas: Array.from({ length: pages }, (_, index) => [{
      name: `name_${index}`, type: 'text', content: '测试员工王小明',
      position: { x: 5, y: 5 }, width: 50, height: 8, fontName: 'NotoSansSC'
    }])
  }
}

/** 等待宿主的异步加载与 Vue 更新，不接触外部服务。 */
async function settle() {
  await new Promise(resolve => setTimeout(resolve, 0))
}

/** 挂载并记录待清理的宿主；props 仅使用合成数据。 */
function createHost(printItemType = 'STAFF_CARD', pages = 1) {
  const wrapper = mount(PdfmeHost, { propsData: { template: templateFor(pages), printItemType, font } })
  wrappers.push(wrapper)
  return wrapper
}

beforeEach(() => {
  vi.resetAllMocks()
  instances = []
  // pdfme 依赖浏览器的画布/Worker；只替换该外部渲染边界，宿主生命周期保持真实。
  mountDesigner = vi.fn(options => {
    const instance = { destroy: vi.fn(), getTemplate: () => options.template }
    instances.push(instance)
    return instance
  })
  loadPdfmeRuntime.mockResolvedValue({ mountDesigner })
})

afterEach(() => {
  wrappers.forEach(wrapper => wrapper.destroy())
  wrappers = []
})

describe('pdfme Vue2 兼容宿主', () => {
  it.each([['STAFF_CARD', 1], ['VISITOR_SLIP', 1]])('挂载 %s 并保留中文和 %i 个业务面', async (type, pages) => {
    const wrapper = createHost(type, pages)
    await settle()
    expect(wrapper.emitted('ready')).toHaveLength(1)
    expect(mountDesigner).toHaveBeenCalledTimes(1)
    const passed = mountDesigner.mock.calls[0][0]
    expect(passed.template.schemas).toHaveLength(pages)
    expect(passed.template.schemas[0][0].content).toBe('测试员工王小明')
    expect(passed.font.NotoSansSC.data).toEqual(new Uint8Array([1]))
    expect(passed.template).not.toBe(wrapper.props('template'))
    expect(wrapper.vm.getTemplate().schemas).toHaveLength(pages)
  })

  it.each([['STAFF_CARD', 2], ['VISITOR_SLIP', 2], ['STAFF_CARD', 0]])('拒绝 %s 的错误面数 %i', async (type, pages) => {
    const wrapper = createHost(type, pages)
    await settle()
    expect(wrapper.emitted('error')).toHaveLength(1)
    expect(mountDesigner).not.toHaveBeenCalled()
  })

  it('页面退出销毁实例，重新进入不会累积旧画布', async () => {
    const first = createHost()
    await settle()
    first.destroy()
    expect(instances).toHaveLength(1)
    expect(instances[0].destroy).toHaveBeenCalledTimes(1)
    const second = createHost()
    await settle()
    expect(second.emitted('ready')).toHaveLength(1)
    expect(mountDesigner).toHaveBeenCalledTimes(2)
  })

  it('退出后才完成加载时不创建幽灵实例', async () => {
    let resolveLoad
    loadPdfmeRuntime.mockReturnValue(new Promise(resolve => { resolveLoad = resolve }))
    const wrapper = createHost()
    wrapper.destroy()
    resolveLoad({ mountDesigner })
    await settle()
    expect(mountDesigner).not.toHaveBeenCalled()
    expect(wrapper.emitted('ready')).toBeUndefined()
  })

  it('切换模板销毁旧实例并载入新快照', async () => {
    const wrapper = createHost()
    await settle()
    await wrapper.setProps({ template: templateFor(1), printItemType: 'VISITOR_SLIP' })
    await settle()
    expect(instances[0].destroy).toHaveBeenCalledTimes(1)
    expect(wrapper.vm.getTemplate().schemas).toHaveLength(1)
  })

  it('加载失败发出可见错误，不报告就绪', async () => {
    loadPdfmeRuntime.mockRejectedValue(new Error('资源加载失败'))
    const wrapper = createHost()
    await settle()
    expect(wrapper.emitted('error')).toHaveLength(1)
    expect(wrapper.emitted('error')[0][0].message).toBe('资源加载失败')
    expect(wrapper.emitted('ready')).toBeUndefined()
  })
})
