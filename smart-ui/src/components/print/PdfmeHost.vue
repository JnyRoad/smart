<template>
  <section
    :aria-busy="loading || disabled"
    class="pdfme-host">
    <p
      v-if="errorMessage"
      role="alert"
      class="pdfme-host__error">{{ errorMessage }}</p>
    <div
      ref="canvas"
      class="pdfme-host__canvas" />
  </section>
</template>

<script>
// Vue2 管理 pdfme 实例的生命周期；正式业务鉴权、版本和打印仍由平台负责。
import { loadPdfmeRuntime } from './pdfme-runtime'
import { assertSinglePageTemplate } from './template-shape'

// pdfme 6.1.12 快捷键监听 document，弹层可挂在 body；宿主 inert 无法隔离这些入口。
// 不拦截 keyup，让冻结前按下的修饰键正常释放，避免恢复编辑后留下按键状态。
const EDIT_EVENTS = ['keydown', 'keypress', 'beforeinput', 'input', 'change', 'paste', 'cut', 'drop',
  'compositionstart', 'compositionupdate', 'compositionend', 'click', 'dblclick', 'contextmenu',
  'pointerdown', 'pointermove', 'pointerup', 'mousedown', 'mousemove', 'mouseup',
  'touchstart', 'touchmove', 'touchend', 'dragstart', 'dragover', 'dragend', 'wheel']

export default {
  name: 'PdfmeHost',
  props: {
    template: { type: Object, required: true },
    printItemType: { type: String, required: true },
    font: { type: Object, required: true },
    disabled: { type: Boolean, default: false }
  },
  /** 仅将页面状态放入 Vue 响应式对象，第三方实例另行持有。 */
  data() {
    return { loading: true, errorMessage: '' }
  },
  watch: {
    template: 'mountDesigner',
    printItemType: 'mountDesigner',
    font: 'mountDesigner'
  },
  /** 建立实例与异步请求标记，避免 Vue 深度观测 React 内部对象。 */
  created() {
    this.designerInstance = null
    this.mountSequence = 0
    this.editEventTarget = null
  },
  /** DOM 就绪后按需加载运行时。 */
  mounted() {
    this.editEventTarget = this.$el.ownerDocument
    // 在加载 pdfme 前建立捕获监听，即使其稍后创建外置弹层也不能绕过冻结。
    EDIT_EVENTS.forEach(type => this.editEventTarget.addEventListener(type, this.blockEditing, { capture: true, passive: false }))
    this.mountDesigner()
  },
  /** 页面离开时使异步结果失效，并释放画布、监听器和观察器。 */
  beforeDestroy() {
    if (this.editEventTarget) EDIT_EVENTS.forEach(type => this.editEventTarget.removeEventListener(type, this.blockEditing, true))
    this.editEventTarget = null
    this.mountSequence += 1
    this.releaseDesigner()
  },
  methods: {
    /** 业务请求等待期间先阻止第三方编辑及原生输入；恢复后让相同事件原样通过。 */
    blockEditing(event) {
      if (!this.disabled) return
      if (event.cancelable) event.preventDefault()
      event.stopImmediatePropagation()
    },
    /** 重建当前模板实例；旧请求完成时不再写入已退出或切换的页面。 */
    async mountDesigner() {
      const sequence = ++this.mountSequence
      this.releaseDesigner()
      this.loading = true
      this.errorMessage = ''
      try {
        // 输入先校验，避免错误打印物创建画布。
        assertSinglePageTemplate(this.template, this.printItemType)
        const runtime = await loadPdfmeRuntime()
        if (sequence !== this.mountSequence) return
        // 使用独立快照，不让第三方编辑器修改调用方的版本数据。
        this.designerInstance = runtime.mountDesigner({
          domContainer: this.$refs.canvas,
          template: JSON.parse(JSON.stringify(this.template)),
          printItemType: this.printItemType,
          font: this.font,
          onChange: template => { this.errorMessage = ''; this.$emit('change', template) },
          onError: error => this.reportError(error)
        })
        this.$emit('ready')
      } catch (error) {
        if (sequence === this.mountSequence) {
          this.reportError(error)
          // 初始化失败时没有可读取的画布，允许业务壳保留原草稿并切换或重试。
          this.$emit('load-error', error)
        }
      } finally {
        if (sequence === this.mountSequence) this.loading = false
      }
    },
    /** 返回可保存的独立模板快照；尚未初始化或面数异常时拒绝。 */
    getTemplate() {
      if (!this.designerInstance) throw new Error('模板设计器尚未就绪')
      const template = this.designerInstance.getTemplate()
      assertSinglePageTemplate(template, this.printItemType)
      return JSON.parse(JSON.stringify(template))
    },
    /** 释放当前实例，可重复调用；实例只销毁一次。 */
    releaseDesigner() {
      if (!this.designerInstance) return
      const instance = this.designerInstance
      this.designerInstance = null
      instance.destroy()
    },
    /** 展示加载或模板错误，并通知上层页面阻止保存。 */
    reportError(error) {
      this.errorMessage = error.message || '模板设计器初始化失败'
      this.$emit('error', error)
    }
  }
}
</script>

<style scoped>
.pdfme-host { width: 100%; min-height: 640px; }
.pdfme-host__canvas { width: 100%; height: 680px; }
.pdfme-host__error { color: #b42318; padding: 12px; background: #fff1f0; }
</style>
