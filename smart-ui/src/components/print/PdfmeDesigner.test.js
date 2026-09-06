import { mount } from '@vue/test-utils'
import { beforeAll, expect, it, vi } from 'vitest'
import Designer from './PdfmeDesigner.vue'
const template = { basePdf: { width: 85.6, height: 53.98 }, schemas: [[{ name: '姓名组件', type: 'text', content: '示例', readOnly: true }]] }
const Host = { template: '<div />', methods: { getTemplate: () => JSON.parse(JSON.stringify(template)) } }
beforeAll(() => { vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, arrayBuffer: async () => new ArrayBuffer(16437364) })) })
it('图片可以选择当前人员照片并锁定必填，解除后清除占位和旧上传引用', async () => {
  const picture = { ...template, schemas: [[{ name: '证件照', type: 'image', content: 'data:image/png;base64,old', resourceRef: { objectId: 'old' } }, template.schemas[0][0]]] }
  const PhotoHost = { props: ['template'], template: '<div />', methods: { getTemplate() { return JSON.parse(JSON.stringify(this.template)) } } }
  const wrapper = mount(Designer, { propsData: { template: picture, printItemType: 'STAFF_CARD' }, stubs: { PdfmeHost: PhotoHost } })
  await new Promise(resolve => setTimeout(resolve, 0))
  expect(wrapper.find('select[aria-label="证件照的数据来源"]').text()).toContain('当前人员照片')
  expect(wrapper.find('select[aria-label="证件照的数据来源"]').text()).not.toContain('工号')
  expect(wrapper.find('select[aria-label="姓名组件的数据来源"]').text()).not.toContain('当前人员照片')
  await wrapper.find('select[aria-label="证件照的数据来源"]').setValue('personPhoto')
  await wrapper.setProps({ fieldSchema: wrapper.emitted('binding-change')[0][0] })
  expect(wrapper.find('input[type="checkbox"]').element.disabled).toBe(true)
  const photo = wrapper.vm.getTemplate().schemas[0][0]
  expect(photo.resourceRef).toBeUndefined(); expect(photo.content).not.toContain('base64,old'); expect(photo.readOnly).toBe(true)
  await wrapper.find('select[aria-label="证件照的数据来源"]').setValue('')
  await wrapper.setProps({ fieldSchema: wrapper.emitted('binding-change').at(-1)[0] })
  const fixed = wrapper.vm.getTemplate().schemas[0][0]
  expect(fixed.content || '').toBe(''); expect(fixed.resourceRef).toBeUndefined()
  wrapper.destroy()
})
it('业务字段与单面组件关联，保存时启用字段填充并保留必填属性', async () => {
  const wrapper = mount(Designer, { propsData: { template, printItemType: 'STAFF_CARD', fieldSchema: { fields: [] } }, stubs: { PdfmeHost: Host } })
  await new Promise(resolve => setTimeout(resolve, 0))
  await wrapper.find('select[aria-label="姓名组件的数据来源"]').setValue('staffName')
  expect(wrapper.emitted('binding-change')[0][0]).toEqual({ fields: [{ key: 'staffName', schemaName: '姓名组件', required: true }] })
  await wrapper.setProps({ fieldSchema: wrapper.emitted('binding-change')[0][0] })
  expect(wrapper.vm.getTemplate().schemas[0][0].readOnly).toBe(false)
  wrapper.destroy()
})
it('解除文字和码字段绑定后保存固定内容，避免生成器把画布内容印为空白', async () => {
  const fixed = { ...template, schemas: [[{ name: '固定标题', type: 'text', content: '员工厂牌', readOnly: false }, { name: '固定码', type: 'qrcode', content: 'VISITOR', readOnly: false }]] }
  const wrapper = mount(Designer, { propsData: { template: fixed, printItemType: 'STAFF_CARD', fieldSchema: { fields: [] } }, stubs: { PdfmeHost: { template: '<div />', methods: { getTemplate: () => JSON.parse(JSON.stringify(fixed)) } } } })
  await new Promise(resolve => setTimeout(resolve, 0))
  expect(wrapper.vm.getTemplate().schemas[0].map(item => [item.readOnly, item.content])).toEqual([[true, '员工厂牌'], [true, 'VISITOR']])
  wrapper.destroy()
})
it('组件改名或删除后显示失效绑定并允许清除后继续保存', async () => {
  const wrapper = mount(Designer, { propsData: { template, printItemType: 'STAFF_CARD', fieldSchema: { fields: [{ key: 'staffName', schemaName: '旧姓名组件', required: true }] } }, stubs: { PdfmeHost: Host } })
  await new Promise(resolve => setTimeout(resolve, 0))
  expect(wrapper.text()).toContain('旧姓名组件')
  const clear = wrapper.find('button[aria-label="清除旧姓名组件的失效绑定"]')
  expect(clear.exists()).toBe(true)
  await clear.trigger('click')
  await wrapper.setProps({ fieldSchema: wrapper.emitted('binding-change')[0][0] })
  expect(() => wrapper.vm.getTemplate()).not.toThrow()
  wrapper.destroy()
})
it('等待保存期间冻结画布和字段绑定，延迟事件不能修改业务草稿', async () => {
  const wrapper = mount(Designer, { propsData: { template, printItemType: 'STAFF_CARD', disabled: true }, stubs: { PdfmeHost: Host } })
  await new Promise(resolve => setTimeout(resolve, 0))
  expect(wrapper.attributes('inert')).toBeDefined()
  expect(wrapper.find('fieldset').element.disabled).toBe(true)
  wrapper.vm.setBinding('姓名组件', 'staffName')
  wrapper.vm.canvasChanged({ ...template, schemas: [[]] })
  expect(wrapper.emitted('binding-change')).toBeUndefined()
  expect(wrapper.emitted('change')).toBeUndefined()
  wrapper.destroy()
})
