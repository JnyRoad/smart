<template>
  <div :inert="disabled ? '' : null" :aria-busy="disabled">
    <p
      v-if="error"
      role="alert">{{ error }}</p>
    <p
      v-else-if="!font"
      role="status">正在加载中文字体…</p>
    <pdfme-host
      v-else
      ref="host"
      :template="hostTemplate"
      :print-item-type="printItemType"
      :font="font"
      :disabled="disabled"
      @change="canvasChanged"
      @error="$emit('error', $event)" />
    <fieldset
      v-if="bindable.length || invalidBindings.length"
      :disabled="disabled"
      class="print-field-bindings">
      <legend>组件的数据来源</legend>
      <p>固定文字保留画布内容；选择业务字段后，打印时由系统填入对应人员信息。</p>
      <div v-for="field in invalidBindings" :key="`invalid-${field.schemaName}`" role="alert">
        <span>失效绑定：{{ field.schemaName }}。组件已更名、删除或不再支持此字段，请清除后为新组件设置来源。</span>
        <button type="button" :aria-label="`清除${field.schemaName}的失效绑定`" @click="setBinding(field.schemaName, '')">清除失效绑定</button>
      </div>
      <div
        v-for="component in bindable"
        :key="component.name">
        <label>{{ component.name }} <select
          :aria-label="`${component.name}的数据来源`"
          :value="binding(component.name).key || ''"
          @change="setBinding(component.name, $event.target.value)"><option value="">{{ component.type === 'image' ? '固定图片' : '固定内容' }}</option><option
            v-for="field in allowedFields(component)"
            :key="field.key"
            :value="field.key">{{ field.label }}</option></select></label>
        <label v-if="binding(component.name).key"><input
          :checked="binding(component.name).required"
          :disabled="binding(component.name).key === 'personPhoto'"
          type="checkbox"
          @change="setRequired(component.name, $event.target.checked)">打印时必填</label>
      </div>
    </fieldset>
  </div>
</template>
<script>
import PdfmeHost from './PdfmeHost.vue'
import { applyPersonPhotoBindings } from './person-photo'
const staffFields = [{ key: 'staffName', label: '人员姓名' }, { key: 'staffNo', label: '工号' }, { key: 'departmentName', label: '部门' }, { key: 'companyName', label: '所属单位' }, { key: 'cardNo', label: '已登记卡号' }, { key: 'employeeGradeName', label: '职级名称' }]
const visitorFields = [{ key: 'visitorName', label: '访客姓名' }, { key: 'companyName', label: '来访单位' }, { key: 'visitorCredentialPayload', label: '访客凭证码' }]
const commonFields = [{ key: 'parkName', label: '园区名称' }, { key: 'validFrom', label: '有效期开始' }, { key: 'validTo', label: '有效期结束' }]
let fontPromise
/** 固定中文字体同源加载；失败不保留 Promise，重进页面可重试。 */
function loadFont() {
  if (!fontPromise) fontPromise = fetch(`${process.env.BASE_URL || '/'}print-designer/fonts/NotoSansCJKsc-Regular.otf`)
    .then(response => { if (!response.ok) throw new Error('中文字体加载失败'); return response.arrayBuffer() })
    .then(data => { if (data.byteLength !== 16437364) throw new Error('中文字体文件不完整'); return { NotoSansSC: { data: new Uint8Array(data), fallback: true } } })
    .catch(error => { fontPromise = null; throw error })
  return fontPromise
}
export default {
  name: 'PdfmeDesigner', components: { PdfmeHost },
  props: { template: { type: Object, required: true }, printItemType: { type: String, required: true }, fieldSchema: { type: Object, default: () => ({ fields: [] }) }, disabled: { type: Boolean, default: false } },
  data() { return { font: null, error: '', latestTemplate: this.template, hostTemplate: this.template } },
  computed: {
    bindable() { return (this.latestTemplate.schemas[0] || []).filter(item => ['text', 'qrcode', 'code128', 'image'].includes(item.type)) },
    invalidBindings() { return this.fieldSchema.fields.filter(field => !this.bindable.some(item => item.name === field.schemaName && this.allowedFields(item).some(choice => choice.key === field.key))) }
  },
  watch: { template(value) { this.latestTemplate = value; this.hostTemplate = value } },
  /** 只加载固定资源，不读取人员资料；页面离开后不再写状态。 */
  async mounted() { try { const font = await loadFont(); if (!this._isDestroyed) this.font = font } catch (error) { if (!this._isDestroyed) this.error = error.message } },
  methods: {
    /** 保存必须等待真实画布完成编辑，不能回退为旧 props。 */
    getTemplate() {
      if (!this.$refs.host) throw new Error('设计器尚未就绪')
      const template = this.$refs.host.getTemplate()
      for (const field of this.fieldSchema.fields) {
        const schema = template.schemas[0].find(item => item.name === field.schemaName)
        if (!schema || !this.allowedFields(schema).some(choice => choice.key === field.key)) throw new Error(`字段对应的组件“${field.schemaName}”已更名或删除，请清除失效绑定后重新设置数据来源`)
      }
      // pdfme 的非只读组件只取 inputs；未绑定的文字和码必须明确恢复固定内容。
      for (const schema of template.schemas[0]) if (['text', 'qrcode', 'code128', 'image'].includes(schema.type)) schema.readOnly = !this.fieldSchema.fields.some(field => field.schemaName === schema.name)
      applyPersonPhotoBindings(template, this.fieldSchema)
      return template
    },
    canvasChanged(value) { if (this.disabled) return; this.latestTemplate = value; this.$emit('change', value) },
    binding(name) { return this.fieldSchema.fields.find(item => item.schemaName === name) || {} },
    allowedFields(component) { if (component.type === 'image') return [{ key: 'personPhoto', label: '当前人员照片' }]; const choices = this.printItemType === 'STAFF_CARD' ? staffFields : visitorFields; return [...choices, ...commonFields].filter(field => field.key !== 'visitorCredentialPayload' || component.type === 'qrcode') },
    setBinding(name, key) {
      if (this.disabled) return
      const component = this.bindable.find(item => item.name === name)
      if (key && (!component || !this.allowedFields(component).some(field => field.key === key))) return
      const previous = this.binding(name).key
      const fields = this.fieldSchema.fields.filter(item => item.schemaName !== name)
      if (key) fields.push({ key, schemaName: name, required: true })
      if (component && (key === 'personPhoto' || previous === 'personPhoto')) {
        const canvas = this.$refs.host.getTemplate()
        const image = canvas.schemas[0].find(item => item.name === name)
        delete image.resourceRef; delete image.content; image.readOnly = true
        applyPersonPhotoBindings(canvas, { fields })
        // 仅切换资源来源时重建画布；普通拖动仍保留编辑器实例和当前选区。
        this.latestTemplate = canvas; this.hostTemplate = canvas
        this.$emit('change', canvas)
      }
      this.$emit('binding-change', { fields })
    },
    setRequired(name, required) { if (this.disabled || this.binding(name).key === 'personPhoto') return; this.$emit('binding-change', { fields: this.fieldSchema.fields.map(item => item.schemaName === name ? { ...item, required } : item) }) }
  }
}
</script>
