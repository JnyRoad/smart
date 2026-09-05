<template>
  <section class="print-page" :aria-busy="busy">
    <header class="print-heading"><div><h1>单面模板</h1><p>正面与背面分别设计、保存和发布，在“正反面组合”中建立关联。</p></div><button :disabled="busy || !parkId || !!current" class="primary" @click="beginCreate">新建模板</button></header>
    <p v-if="error" role="alert" class="error">{{ error }}</p><p v-if="message" role="status">{{ message }}</p>
    <template v-if="!current">
      <div class="print-toolbar"><label>打印物 <select v-model="filter" :disabled="busy" @change="refresh"><option value="">全部</option><option value="STAFF_CARD">人员厂牌</option><option value="VISITOR_SLIP">访客凭条</option></select></label><button :disabled="busy || !parkId" @click="refresh">刷新列表</button></div>
      <table><thead><tr><th>模板名称</th><th>人员类型</th><th>面</th><th>尺寸</th><th>当前发布</th><th>操作</th></tr></thead><tbody><tr v-for="row in records" :key="row.templateId"><td>{{ row.name }}</td><td>{{ personTypes[row.personType] }}</td><td>{{ row.faceRole === 'BACK' ? '背面' : '正面' }}</td><td>{{ sizeLabel(row) }}</td><td>{{ row.currentPublishedVersionId ? '已发布' : '尚未发布' }}</td><td><button :disabled="busy" @click="open(row.templateId)">编辑</button><button :disabled="busy" @click="showVersions(row.templateId)">版本</button></td></tr></tbody></table>
      <p v-if="!busy && !records.length">{{ parkId ? '还没有模板，可先新建一份正面或背面模板。' : '请先选择园区。' }}</p>
      <div class="print-toolbar"><button :disabled="busy || page === 1" @click="changePage(-1)">上一页</button><span>第 {{ page }} 页 · 共 {{ total }} 份</span><button :disabled="busy || page * 20 >= total" @click="changePage(1)">下一页</button></div>
    </template>
    <section v-else class="print-editor">
      <div class="print-toolbar"><label>名称 <input v-model.trim="draft.name" maxlength="100" :disabled="busy" @input="markDirty"></label><span>{{ personTypes[draft.personType] }} · {{ draft.faceRole === 'BACK' ? '背面' : '正面' }} · {{ draft.pageSpecJson.widthMm }} × {{ draft.pageSpecJson.heightMm }} mm</span><button :disabled="busy" @click="save">保存草稿</button><button :disabled="busy || !current.templateId || dirty" @click="publish">发布已保存草稿</button><button :disabled="busy || !current.templateId || dirty" @click="preview">预览已保存草稿</button><button :disabled="busy" @click="closeEditor">返回列表</button></div>
      <p>每次只设计一面。图片支持 PNG/JPEG Logo 和背景，单张最多20 MiB、每面累计32 MiB；人员照片需独立授权。发布新版不会替换已有组合引用的版本。</p>
      <pdfme-designer ref="designer" :key="editorKey" :template="canvas" :print-item-type="draft.printItemType" :field-schema="draft.fieldSchemaJson" :disabled="busy" @binding-change="setBindings" @change="markDirty" @error="error = $event.message" />
    </section>
    <print-preview v-if="previewResult" :key="previewResult.previewId" :initial="previewResult" @close="previewResult = null" />
    <section v-if="createVisible" class="print-dialog" role="dialog" aria-label="新建单面模板">
      <h2>新建单面模板</h2><form @submit.prevent="confirmCreate">
        <label>模板名称 <input v-model.trim="createForm.name" required maxlength="100"></label>
        <label>打印物 <select v-model="createForm.printItemType" @change="resetCreateType"><option value="STAFF_CARD">人员厂牌</option><option value="VISITOR_SLIP">访客凭条</option></select></label>
        <label>人员类型 <select v-model="createForm.personType"><option v-for="(label, value) in allowedPersonTypes" :key="value" :value="value">{{ label }}</option></select></label>
        <label>模板面 <select v-model="createForm.faceRole"><option value="FRONT">正面</option><option v-if="createForm.printItemType === 'STAFF_CARD'" value="BACK">背面</option></select></label>
        <label v-if="createForm.printItemType === 'VISITOR_SLIP'">分类 <select v-model="createForm.classificationCode"><option value="VISITOR_NORMAL">普通访客</option><option value="VISITOR_SECURITY">保密访客（必须带码）</option></select></label>
        <button class="primary" type="submit">开始设计</button><button type="button" @click="createVisible = false">取消</button>
      </form>
    </section>
    <section v-if="versionDetail" class="print-dialog" role="dialog" aria-label="模板版本"><h2>{{ versionDetail.name }} · 版本</h2><table><thead><tr><th>版本</th><th>状态</th><th>操作</th></tr></thead><tbody><tr v-for="version in versionDetail.versions" :key="version.templateVersionId"><td>v{{ version.versionNo }}</td><td>{{ version.versionStatus === 'PUBLISHED' ? '已发布' : '草稿' }}</td><td><button v-if="version.versionStatus === 'PUBLISHED' && version.templateVersionId !== versionDetail.currentPublishedVersionId" :disabled="busy" @click="rollback(version)">回滚到此版本</button></td></tr></tbody></table><button :disabled="busy" @click="versionDetail = null">关闭</button></section>
  </section>
</template>
<script>
import PrintPreview from '@/components/print/PrintPreview.vue'
import PdfmeDesigner from '@/components/print/PdfmeDesigner.vue'
import { personTypes, newTemplateDraft, canvasFromVersion } from '@/components/print/template-model'
import { prepareTemplateResources, hydrateTemplateResources } from '@/components/print/template-resources'
import { recordsOf, newIdempotencyKey } from '@/api/platform/print/client'
import * as api from '@/api/platform/print/templates'

export default {
  name: 'PrintTemplates', components: { PdfmeDesigner, PrintPreview }, props: { parkId: { type: [String, Number], default: '' } },
  data() { return { personTypes, records: [], page: 1, total: 0, filter: '', busy: false, error: '', message: '', current: null, draft: null, canvas: null, dirty: false, editorKey: 0, createVisible: false, createForm: {}, versionDetail: null, previewResult: null, pendingPublish: null, pendingRollback: null } },
  computed: { allowedPersonTypes() { return this.createForm.printItemType === 'VISITOR_SLIP' ? { VISITOR: '访客' } : { EMPLOYEE: '正式员工', OUTSOURCED: '外包人员', DISPATCHED: '派遣人员', SUPPLIER: '供应商人员' } } },
  watch: { parkId: { immediate: true, handler() { this.page = 1; this.current = null; this.versionDetail = null; this.refresh() } }, current(value) { this.$emit('editing-state', !!value) }, busy(value) { this.$emit('busy-state', value) } },
  created() { this.resourceCache = new Map() },
  mounted() { window.addEventListener('beforeunload', this.beforeUnload) },
  beforeDestroy() { window.removeEventListener('beforeunload', this.beforeUnload) },
  /** 路由离开时核对未保存草稿，避免误丢单面设计。 */
  beforeRouteLeave(to, from, next) { this.confirmDiscard().then(allowed => next(allowed)) },
  methods: {
    beforeUnload(event) { if (this.dirty || this.busy) { event.preventDefault(); event.returnValue = '' } },
    /** 操作期间禁用重复提交；错误保留画布和服务端修订供用户处理。 */
    async run(action) { if (this.busy) return; this.busy = true; this.error = ''; this.message = ''; try { await action() } catch (error) { this.error = error.message || '操作失败，请重试' } finally { this.busy = false } },
    async refresh() { return this.run(() => this.loadList()) },
    async loadList() { if (!this.parkId) { this.records = []; this.total = 0; return } const data = await api.listTemplates({ parkId: this.parkId, printItemType: this.filter || undefined, current: this.page, size: 20 }); this.records = recordsOf(data); this.total = Number(data.total || this.records.length) },
    changePage(step) { this.page += step; this.refresh() },
    sizeLabel(row) { const page = row.pageSpecJson || (row.draft && row.draft.pageSpecJson); return page ? `${page.widthMm} × ${page.heightMm} mm` : '查看模板' },
    setBindings(value) { if (this.busy) return; this.draft.fieldSchemaJson = value; this.markDirty() },
    markDirty() { if (!this.busy) this.dirty = true },
    async confirmDiscard() { if (this.busy) return false; if (!this.dirty) return true; try { await this.$confirm('当前单面设计尚未保存，确定离开？', '未保存的草稿', { type: 'warning' }); return true } catch (_) { return false } },
    async closeEditor() { if (!(await this.confirmDiscard())) return; this.current = null; this.dirty = false; this.refresh() },
    beginCreate() { this.createForm = { name: '', printItemType: 'STAFF_CARD', personType: 'EMPLOYEE', faceRole: 'FRONT', classificationCode: 'VISITOR_NORMAL' }; this.createVisible = true },
    resetCreateType() { this.createForm.personType = this.createForm.printItemType === 'VISITOR_SLIP' ? 'VISITOR' : 'EMPLOYEE'; this.createForm.faceRole = 'FRONT' },
    confirmCreate() { const draft = newTemplateDraft(this.createForm.printItemType, this.createForm.personType, this.createForm.faceRole); draft.name = this.createForm.name; if (draft.personType === 'VISITOR') draft.classificationCode = this.createForm.classificationCode; this.current = {}; this.draft = draft; this.canvas = canvasFromVersion(draft); this.editorKey++; this.dirty = true; this.createVisible = false },
    async open(id) { return this.run(() => this.loadDetail(id)) },
    async loadDetail(id) { const detail = await api.getTemplate(id, this.parkId); const version = detail.draft || detail.versions.find(item => item.templateVersionId === detail.currentPublishedVersionId); if (!version) throw new Error('模板没有可读取的版本'); const draft = { ...version, name: detail.name, printItemType: detail.printItemType, personType: detail.personType, classificationCode: detail.classificationCode, faceRole: detail.faceRole, sideCount: 1 }; const canvas = await hydrateTemplateResources(draft, { parkId: this.parkId, downloadResource: api.downloadTemplateResource, cache: this.resourceCache }); this.current = detail; this.draft = draft; this.canvas = canvas; this.editorKey++; this.dirty = false },
    async save() { return this.run(async () => { if (!this.draft.name) throw new Error('请输入模板名称'); const body = await prepareTemplateResources(this.draft, this.$refs.designer.getTemplate(), { parkId: this.parkId, uploadResource: api.uploadTemplateResource, cache: this.resourceCache }); body.parkId = this.parkId; let id = this.current.templateId; if (id) { body.draftRevision = this.current.draftRevision; const result = await api.saveDraft(id, body); this.current = { ...this.current, ...result }; this.dirty = false } else { const result = await api.createTemplate(body); id = result.templateId; this.current = { ...this.current, ...result }; this.dirty = false } await this.loadDetail(id); this.message = '草稿已保存到系统'; }) },
    async publish() { return this.run(async () => { const data = { parkId: this.parkId, draftRevision: this.current.draftRevision, draftVersionId: this.current.currentDraftVersionId }; const signature = JSON.stringify({ id: this.current.templateId, data }); if (!this.pendingPublish || this.pendingPublish.signature !== signature) this.pendingPublish = { signature, key: newIdempotencyKey() }; await api.publishTemplate(this.current.templateId, data, this.pendingPublish.key); await this.loadDetail(this.current.templateId); this.message = '当前单面模板已发布，已有组合保持原版本'; }) },
    async preview() { return this.run(async () => { this.previewResult = await api.previewTemplate(this.current.templateId, { parkId: this.parkId, versionId: this.current.currentDraftVersionId || this.current.currentPublishedVersionId, draftRevision: this.current.draftRevision, sampleData: {} }) }) },
    async showVersions(id) { return this.run(async () => { this.versionDetail = await api.getTemplate(id, this.parkId) }) },
    async rollback(version) { return this.run(async () => {
      const id = this.versionDetail.templateId
      const data = { parkId: this.parkId, targetVersionId: version.templateVersionId, expectedPublishedVersionId: this.versionDetail.currentPublishedVersionId }
      const signature = JSON.stringify({ id, data })
      // 回执不明时保留原原因、发布指针和键，不能把重试变成另一条业务操作。
      if (!this.pendingRollback || this.pendingRollback.signature !== signature) {
        let result
        try { result = await this.$prompt('请填写回滚原因', '回滚单面模板', { inputValidator: value => !!(value && value.trim()) || '请填写原因' }) } catch (_) { return }
        this.pendingRollback = { signature, data: { ...data, reason: result.value }, key: newIdempotencyKey() }
      }
      await api.rollbackTemplate(id, this.pendingRollback.data, this.pendingRollback.key)
      this.pendingRollback = null
      this.versionDetail = { ...this.versionDetail, currentPublishedVersionId: version.templateVersionId }
      this.versionDetail = await api.getTemplate(id, this.parkId)
      await this.loadList()
      this.message = '已回滚发布指针，既有组合与任务保持原版本'
    }) }
  }
}
</script>
<style src="../print.css" />
