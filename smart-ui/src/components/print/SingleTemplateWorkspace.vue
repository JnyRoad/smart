<template>
  <section class="single-workspace">
    <nav aria-label="选择单面模板" class="template-library">
      <button
        v-for="item in templates"
        :key="item.id"
        :aria-pressed="item.id === activeId"
        :disabled="!ready && !loadFailed"
        @click="selectTemplate(item.id)">{{ item.name }}</button>
    </nav>
    <div v-if="current" class="workspace-layout">
      <main class="editor-panel">
        <header class="editor-heading">
          <div>
            <h2>{{ current.name }}</h2>
            <p aria-label="当前模板摘要">{{ faceLabel }} · 单面 · 已保存 v{{ latest.versionNo }}</p>
          </div>
          <div class="editor-actions">
            <button :disabled="!ready" class="primary" @click="saveTemplate">保存当前模板</button>
            <button :disabled="!ready" @click="reopen">重开已保存版本</button>
          </div>
        </header>
        <p role="status" class="workspace-status">{{ status }}</p>
        <pdfme-host
          ref="host"
          :key="activeId"
          :template="editingTemplate"
          :print-item-type="current.printItemType"
          :font="font"
          @ready="onReady"
          @change="onChange"
          @load-error="onLoadError"
          @error="onError" />
        <details class="snapshot-details">
          <summary>查看当前模板已保存内容</summary>
          <pre aria-label="已保存模板内容">{{ savedSummary }}</pre>
        </details>
      </main>
      <aside v-if="current.printItemType === 'STAFF_CARD'" class="pair-panel">
        <h2>厂牌正反面组合</h2>
        <p>分别选择已保存的模板版本。可提前关联，也可在打印前选择。</p>
        <label>正面模板
          <select v-model="frontVersionId" aria-label="正面版本">
            <option v-for="option in frontOptions" :key="option.id" :value="option.id">{{ option.label }}</option>
          </select>
        </label>
        <label>背面模板
          <select v-model="backVersionId" aria-label="背面版本">
            <option v-for="option in backOptions" :key="option.id" :value="option.id">{{ option.label }}</option>
          </select>
        </label>
        <label>翻面方式
          <select v-model="printMode" aria-label="翻面方式">
            <option value="MANUAL_DUPLEX">手动翻面</option>
            <option value="AUTO_DUPLEX">自动翻面（需翻面模块）</option>
          </select>
        </label>
        <p v-if="pairError" role="alert">{{ pairError }}</p>
        <button :disabled="Boolean(pairError)" class="primary" @click="savePair">关联为厂牌组合</button>
        <button :disabled="!savedPair" @click="restorePair">恢复已关联组合</button>
        <div v-if="savedPair" aria-label="已关联组合" class="saved-pair">
          <strong>已关联 · 修订 {{ savedPair.revision }}</strong>
          <p>正面：{{ versionLabel(savedPair.frontVersionId) }}</p>
          <p>背面：{{ versionLabel(savedPair.backVersionId) }}</p>
          <small>保存模板新版不会自动改变这组版本。手动和自动翻面可使用同一组合。</small>
        </div>
        <p class="demo-note">本页只验证选择与关联；不会发送打印指令。自动翻面仍需设备能力验证。</p>
      </aside>
      <aside v-else class="pair-panel">
        <h2>访客单面凭条</h2>
        <p>使用当前这一份模板即可，打印输出为一面。</p>
      </aside>
    </div>
  </section>
</template>
<script>
// 此工作台仅用于合成数据的内存验证；正式版本、组合及权限由后续平台接口管理。
import PdfmeHost from './PdfmeHost.vue'

/** 隔离画布、草稿与已保存版本，编辑任一引用不能污染其他版本。 */
function copy(value) { return JSON.parse(JSON.stringify(value)) }

export default {
  name: 'SingleTemplateWorkspace',
  components: { PdfmeHost },
  props: {
    initialTemplates: { type: Array, required: true },
    font: { type: Object, required: true }
  },
  /** 将每份合成模板作为独立 v1；刷新页面即丢失后续内存保存结果。 */
  data() {
    const templates = this.initialTemplates.map(item => ({
      ...copy(item), draft: copy(item.template),
      versions: [{ id: `${item.id}@1`, versionNo: 1, template: copy(item.template) }]
    }))
    const front = templates.find(item => item.printItemType === 'STAFF_CARD' && item.faceRole === 'FRONT')
    const back = templates.find(item => item.printItemType === 'STAFF_CARD' && item.faceRole === 'BACK')
    return {
      templates, activeId: templates[0] && templates[0].id,
      editingTemplate: templates[0] && copy(templates[0].draft),
      ready: false, loadFailed: false, status: '正在加载单面设计器…',
      frontVersionId: front ? front.versions[0].id : '',
      backVersionId: back ? back.versions[0].id : '',
      printMode: 'MANUAL_DUPLEX', savedPair: null
    }
  },
  computed: {
    current() { return this.templates.find(item => item.id === this.activeId) },
    latest() { return this.current.versions[this.current.versions.length - 1] },
    faceLabel() { return this.current.faceRole === 'BACK' ? '背面模板' : '正面模板' },
    frontOptions() { return this.versionOptions('FRONT') },
    backOptions() { return this.versionOptions('BACK') },
    savedSummary() {
      return JSON.stringify({
        name: this.current.name, version: this.latest.versionNo, pages: 1,
        fields: this.latest.template.schemas[0].map(field => ({ name: field.name, content: field.content, position: field.position }))
      }, null, 2)
    },
    pairError() {
      const front = this.frontOptions.find(item => item.id === this.frontVersionId)
      const back = this.backOptions.find(item => item.id === this.backVersionId)
      if (!front || !back) return '请选齐正面和背面模板版本'
      const frontSize = front.template.basePdf
      const backSize = back.template.basePdf
      if (frontSize.width !== backSize.width || frontSize.height !== backSize.height) return '正反面尺寸或方向不一致'
      return ''
    }
  },
  methods: {
    /** 仅列出当前夹具库的厂牌已保存版本，访客版本不参与双面组合。 */
    versionOptions(faceRole) {
      return this.templates.filter(item => item.printItemType === 'STAFF_CARD' && item.faceRole === faceRole)
        .reduce((options, item) => options.concat(item.versions.map(version => ({
          ...version, label: `${item.name} · v${version.versionNo}`
        }))), [])
    },
    versionLabel(id) { return this.frontOptions.concat(this.backOptions).find(item => item.id === id).label },
    onReady() { this.ready = true; this.loadFailed = false; this.status = '当前只编辑这一份模板；切换时会保留各自草稿。' },
    onChange() { this.ready = true; this.status = '当前模板有修改，可独立保存。' },
    onError(error) { this.ready = false; this.status = error.message },
    onLoadError(error) { this.loadFailed = true; this.status = `${error.message}。可切换模板，或点击当前模板重试。` },
    /** 读取上游稳定快照；延迟属性尚未应用时拒绝操作，避免丢失编辑。 */
    captureCurrent() {
      try {
        this.current.draft = this.$refs.host.getTemplate()
        return true
      } catch (error) {
        this.status = error.message
        return false
      }
    },
    selectTemplate(id) {
      if (id === this.activeId && !this.loadFailed) return
      // 只有初始化失败才能跳过读取；属性延迟和异常页面恢复仍需稳定快照，不能丢弃编辑。
      if (!this.loadFailed && !this.captureCurrent()) return
      this.ready = false
      this.loadFailed = false
      this.activeId = id
      this.editingTemplate = copy(this.current.draft)
    },
    /** 追加本模板的内存版本；组合只保留原 ID，不自动升级。 */
    saveTemplate() {
      if (!this.captureCurrent()) return
      const versionNo = this.latest.versionNo + 1
      this.current.versions.push({ id: `${this.activeId}@${versionNo}`, versionNo, template: copy(this.current.draft) })
      this.status = `已在内存保存 ${this.current.name} · v${versionNo}，其他模板及已关联组合保持原版本。`
    },
    /** 用户显式重开已保存版本时，用新引用重建单个画布。 */
    reopen() {
      if (!this.captureCurrent()) return
      this.current.draft = copy(this.latest.template)
      this.ready = false
      this.editingTemplate = copy(this.current.draft)
    },
    savePair() {
      if (this.pairError) return
      this.savedPair = {
        frontVersionId: this.frontVersionId, backVersionId: this.backVersionId,
        revision: this.savedPair ? this.savedPair.revision + 1 : 1
      }
      this.status = '已关联两份具体版本，可供手动或自动双面使用。'
    },
    restorePair() {
      this.frontVersionId = this.savedPair.frontVersionId
      this.backVersionId = this.savedPair.backVersionId
      this.status = '已恢复关联的正反面版本。'
    }
  }
}
</script>
<style scoped>
.single-workspace { color: #202939; font: 14px system-ui, sans-serif; }
.single-workspace button, .single-workspace select { font: inherit; min-height: 38px; border: 1px solid #c5ceda; border-radius: 5px; background: #fff; color: inherit; padding: 8px 12px; }
.single-workspace button { cursor: pointer; }
.single-workspace button:disabled { cursor: default; opacity: .5; }
.single-workspace button:focus-visible, .single-workspace select:focus-visible { outline: 2px solid #1767b3; outline-offset: 2px; }
.single-workspace button.primary, .template-library button[aria-pressed="true"] { background: #1767b3; border-color: #1767b3; color: white; }
.template-library, .editor-actions { display: flex; flex-wrap: wrap; gap: 8px; }
.template-library { margin: 20px 0; }
.workspace-layout { display: grid; grid-template-columns: minmax(0, 1fr) 280px; gap: 16px; align-items: start; }
.editor-panel, .pair-panel { background: #fff; border: 1px solid #dce1e8; border-radius: 8px; overflow: hidden; }
.editor-heading { padding: 16px; display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
.single-workspace h2 { margin: 0 0 8px; font-size: 18px; }
.single-workspace p { margin: 8px 0; line-height: 1.6; }
.editor-heading p { color: #536176; }
.workspace-status { padding: 0 16px; min-height: 24px; }
.pair-panel { padding: 20px; }
.pair-panel label { display: block; margin: 20px 0 12px; }
.pair-panel select { display: block; width: 100%; margin-top: 8px; }
.pair-panel button { width: 100%; margin-top: 8px; }
.saved-pair { margin-top: 20px; padding-top: 16px; border-top: 1px solid #dce1e8; }
.demo-note, .saved-pair small { color: #536176; font-size: 12px; line-height: 1.6; }
.snapshot-details { padding: 16px; border-top: 1px solid #dce1e8; }
.snapshot-details summary { cursor: pointer; }
.snapshot-details pre { white-space: pre-wrap; overflow-wrap: anywhere; }
@media (max-width: 1050px) { .workspace-layout { grid-template-columns: minmax(0, 1fr); } }
</style>
