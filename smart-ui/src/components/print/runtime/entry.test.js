// 只替换第三方 Designer，验证适配层对延迟编辑和非法页面恢复的真实行为。
import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import { mountDesigner } from './entry.mjs'

const mocks = vi.hoisted(() => ({ instances: [] }))
vi.mock('@pdfme/ui', () => ({
  Designer: class {
    /** 记录第三方实例的可观察生命周期，模拟其保留当前页面的行为。 */
    constructor({ template }) { this.template = template; this.destroy = vi.fn(); mocks.instances.push(this) }
    /** 保存上游编辑事件的回调，供反例送入异常模板。 */
    onChangeTemplate(callback) { this.change = callback }
    /** 返回上游当前已提交模板。 */
    getTemplate() { return this.template }
    /** 模拟上游更新时保留页游标并同步触发回调。 */
    updateTemplate(template) { this.template = template; this.change(template) }
  }
}))

let runtime
let container
beforeEach(() => { vi.useFakeTimers(); mocks.instances = []; container = document.createElement('div') })
afterEach(() => { if (runtime) runtime.destroy(); runtime = null; vi.useRealTimers() })

/** 使用单面合成模板挂载真实适配层。 */
function mountVisitor(onChange = vi.fn(), onError = vi.fn()) {
  runtime = mountDesigner({ domContainer: container, template: { basePdf: { width: 58, height: 80, padding: [0, 0, 0, 0] }, schemas: [[]] }, printItemType: 'VISITOR_SLIP', font: {}, onChange, onError })
  return runtime
}

it('属性尚在上游延迟队列中时拒绝读取旧快照，稳定后允许保存', () => {
  mountVisitor()
  container.dispatchEvent(new Event('input', { bubbles: true }))
  expect(() => runtime.getTemplate()).toThrow(/属性.*应用|正在.*修改/)
  vi.advanceTimersByTime(200)
  expect(runtime.getTemplate().schemas).toHaveLength(1)
})

it('非法增加访客页后重建有效画布，避免遗留越界的页游标', () => {
  const onChange = vi.fn()
  const onError = vi.fn()
  mountVisitor(onChange, onError)
  const original = mocks.instances[0]
  original.change({ ...original.template, schemas: [[], []] })
  expect(onError).toHaveBeenCalledOnce()
  vi.runAllTimers()
  expect(original.destroy).toHaveBeenCalledOnce()
  expect(mocks.instances).toHaveLength(2)
  expect(runtime.getTemplate().schemas).toHaveLength(1)
  expect(onChange).toHaveBeenLastCalledWith(expect.objectContaining({ schemas: [[]] }))
})

it('恢复调度期间离开页面不会重建已退出的画布', () => {
  mountVisitor()
  const original = mocks.instances[0]
  original.change({ ...original.template, schemas: [[], []] })
  runtime.destroy()
  vi.runAllTimers()
  expect(mocks.instances).toHaveLength(1)
  expect(original.destroy).toHaveBeenCalledOnce()
  expect(() => runtime.getTemplate()).toThrow(/销毁|退出/)
})
