<template>
  <section class="print-page" :aria-busy="busy">
    <header class="print-heading"><div><h1>正反面组合</h1><p>在系统中关联两份已发布的单面版本。单面新版发布后，组合仍保留原版本。</p></div><button class="primary" :disabled="busy || !parkId" @click="beginEdit()">新建组合</button></header>
    <p v-if="error" role="alert" class="error">{{ error }}</p><p v-if="message" role="status">{{ message }}</p>
    <button :disabled="busy || !parkId" @click="refresh">刷新组合</button>
    <table><thead><tr><th>组合名称</th><th>人员类型</th><th>正面</th><th>背面</th><th>状态</th><th>操作</th></tr></thead><tbody><tr v-for="row in records" :key="row.pairId"><td>{{ row.name }}</td><td>{{ personTypes[row.personType] }}</td><td>{{ versionLabel(row.frontTemplateVersionId, row.frontVersionNo) }}</td><td>{{ versionLabel(row.backTemplateVersionId, row.backVersionNo) }}</td><td>{{ row.status === 'ACTIVE' ? '可用' : '已归档' }}</td><td><button :disabled="busy || row.status !== 'ACTIVE'" @click="beginEdit(row)">修改关联</button><button :disabled="busy" @click="preview(row)">预览两面</button><button :disabled="busy || row.status !== 'ACTIVE'" @click="archive(row)">归档</button></td></tr></tbody></table>
    <p v-if="!busy && !records.length">{{ parkId ? '还没有正反面组合。请先分别发布一份正面和背面模板。' : '请先选择园区。' }}</p>
    <div class="print-toolbar"><button :disabled="busy || page === 1" @click="changePage(-1)">上一页</button><span>第 {{ page }} 页 · 共 {{ total }} 组</span><button :disabled="busy || page * 20 >= total" @click="changePage(1)">下一页</button></div>
    <print-preview v-if="previewResult" :key="previewResult.previewId" :initial="previewResult" @close="previewResult = null" />
    <section v-if="form" class="print-dialog" role="dialog" aria-label="系统正反面关联"><h2>{{ form.pairId ? '修改关联' : '新建组合' }}</h2>
      <form @submit.prevent="save">
        <label>组合名称 <input v-model.trim="form.name" aria-label="组合名称" maxlength="100" :disabled="busy"></label>
        <label>正面版本 <select v-model="form.frontTemplateVersionId" aria-label="正面版本" :disabled="busy" @change="checkBack"><option value="">请选择已发布的正面</option><option v-for="item in frontChoices" :key="item.templateVersionId" :value="item.templateVersionId">{{ item.label }}</option></select></label>
        <label>背面版本 <select v-model="form.backTemplateVersionId" aria-label="背面版本" :disabled="busy"><option value="">请选择兼容的背面</option><option v-for="item in backChoices" :key="item.templateVersionId" :value="item.templateVersionId">{{ item.label }}</option></select></label>
        <p>可复用通用背面。两面的人员分类、尺寸和方向须一致；手动、自动双面使用同一组合。</p>
        <button class="primary" :disabled="busy" type="submit">保存组合</button><button :disabled="busy" type="button" @click="form = null">取消</button>
      </form>
    </section>
  </section>
</template>
<script>
import PrintPreview from '@/components/print/PrintPreview.vue'
import { personTypes, publishedChoices } from '@/components/print/template-model'
import { recordsOf, newIdempotencyKey } from '@/api/platform/print/client'
import { listTemplates, getTemplate } from '@/api/platform/print/templates'
import * as api from '@/api/platform/print/pairs'
export default {
  name: 'PrintPairs', components: { PrintPreview }, props: { parkId: { type: [String, Number], default: '' } },
  data() { return { personTypes, records: [], templates: [], page: 1, total: 0, busy: false, error: '', message: '', form: null, previewResult: null, pendingMutation: null, pendingArchive: null } },
  computed: {
    frontChoices() { return publishedChoices(this.templates, 'FRONT') },
    backChoices() { const front = this.frontChoices.find(item => this.form && item.templateVersionId === this.form.frontTemplateVersionId); return publishedChoices(this.templates, 'BACK').filter(back => !front || this.compatible(front, back)) }
  },
  watch: { parkId: { immediate: true, handler() { this.form = null; this.page = 1; this.refresh() } }, form(value) { this.$emit('editing-state', !!value) }, busy(value) { this.$emit('busy-state', value) } },
  beforeRouteLeave(to, from, next) { if (this.busy) return next(false); if (!this.form) return next(); this.$confirm('当前组合尚未保存，确定离开？', '未保存的关联').then(() => next()).catch(() => next(false)) },
  methods: {
    /** 组合候选只使用系统已发布版本；客户端筛选只是提示，后台再次校验。 */
    compatible(front, back) { const left = front.pageSpecJson || {}; const right = back.pageSpecJson || {}; return front.personType === back.personType && front.classificationCode === back.classificationCode && left.widthMm === right.widthMm && left.heightMm === right.heightMm && left.orientation === right.orientation },
    async run(action) { if (this.busy) return; this.busy = true; this.error = ''; this.message = ''; try { await action() } catch (error) { this.error = error.message || '组合操作失败' } finally { this.busy = false } },
    refresh() { return this.run(() => this.loadList()) },
    async loadList() { if (!this.parkId) { this.records = []; this.templates = []; return } const data = await api.listPairs({ parkId: this.parkId, current: this.page, size: 20 }); this.records = recordsOf(data); this.total = Number(data.total || this.records.length) },
    changePage(step) { this.page += step; this.refresh() },
    versionLabel(id, number) { const version = [...publishedChoices(this.templates, 'FRONT'), ...publishedChoices(this.templates, 'BACK')].find(item => item.templateVersionId === id); return version ? version.label : `v${number || '—'}` },
    async beginEdit(row) { return this.run(async () => { const summaries = []; let current = 1; let total; do { const data = await listTemplates({ parkId: this.parkId, printItemType: 'STAFF_CARD', lifecycleStatus: 'ACTIVE', current, size: 100 }); const records = recordsOf(data); summaries.push(...records); total = Number(data.total || summaries.length); if (!records.length) break; current++ } while (summaries.length < total); this.templates = await Promise.all(summaries.map(item => getTemplate(item.templateId, this.parkId))); const detail = row ? await api.getPair(row.pairId, this.parkId) : null; this.form = detail ? { ...detail } : { name: '', frontTemplateVersionId: '', backTemplateVersionId: '' }; this.pendingMutation = null }) },
    checkBack() { if (!this.backChoices.some(item => item.templateVersionId === this.form.backTemplateVersionId)) this.form.backTemplateVersionId = '' },
    async save() { return this.run(async () => { const front = this.frontChoices.find(item => item.templateVersionId === this.form.frontTemplateVersionId); const back = this.backChoices.find(item => item.templateVersionId === this.form.backTemplateVersionId); if (!front || !back) throw new Error('请选齐兼容的正面和背面版本'); if (!this.form.name) throw new Error('请输入组合名称'); const data = { parkId: this.parkId, name: this.form.name, printItemType: 'STAFF_CARD', personType: front.personType, classificationCode: front.classificationCode, frontTemplateVersionId: front.templateVersionId, backTemplateVersionId: back.templateVersionId }; if (this.form.pairId) data.revision = this.form.revision; const signature = JSON.stringify({ id: this.form.pairId, data }); if (!this.pendingMutation || this.pendingMutation.signature !== signature) this.pendingMutation = { signature, key: newIdempotencyKey() }; await api.savePair(this.form.pairId || null, data, this.pendingMutation.key); this.form = null; this.pendingMutation = null; await this.loadList(); this.message = '正反面关联已保存到系统'; }) },
    async preview(row) { return this.run(async () => { this.previewResult = await api.previewPair(row.pairId, { parkId: this.parkId, revision: row.revision, sampleData: {} }) }) },
    async archive(row) { return this.run(async () => {
      const data = { parkId: this.parkId, revision: row.revision }
      const signature = JSON.stringify({ id: row.pairId, data })
      if (!this.pendingArchive || this.pendingArchive.signature !== signature) {
        try { await this.$confirm('归档后不再供新任务选择，历史任务保持原版本。', '归档组合', { type: 'warning' }) } catch (_) { return }
        this.pendingArchive = { signature, data, key: newIdempotencyKey() }
      }
      await api.archivePair(row.pairId, this.pendingArchive.data, this.pendingArchive.key)
      this.pendingArchive = null
      row.status = 'ARCHIVED'
      await this.loadList()
    }) }
  }
}
</script>
<style src="../print.css" />
