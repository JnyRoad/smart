<template>
  <section class="print-page">
    <h1>{{ visitor ? '访客凭条打印' : '人员厂牌打印' }}</h1>
    <p>{{ visitor ? '选择获准入厂的访客，核对单面凭条后打印。' : '选择人员，核对厂牌两面；现场设备决定使用手动或自动翻面。' }}</p>
    <p v-if="error" role="alert" class="error">{{ error }}</p><p v-if="message" role="status">{{ message }}</p>
    <fieldset :disabled="busy || historyBusy || !parkId" class="print-work-section">
      <legend>1 · 选择人员</legend>
      <form class="print-toolbar" @submit.prevent="search(1)">
        <label>人员来源 <select v-model="subjectType" aria-label="人员来源" @change="clearPeople"><option v-for="source in sources" :key="source.value" :value="source.value">{{ source.label }}</option></select></label>
        <label>查找 <input v-model.trim="keyword" aria-label="查找人员" maxlength="80" :placeholder="visitor ? '姓名或预约码' : '姓名或工号'"></label>
        <button type="submit">查询</button>
      </form>
      <table v-if="subjects.length"><thead><tr><th>选择</th><th>姓名</th><th>{{ visitor ? '来源' : '工号' }}</th><th v-if="!visitor">厂牌等级</th></tr></thead><tbody><tr v-for="person in subjects" :key="personKey(person)"><td><input type="checkbox" :checked="selected.some(row => personKey(row) === personKey(person))" :aria-label="'选择 ' + person.displayName" @change="toggleSubject(person, $event.target.checked)"></td><td>{{ person.displayName }}</td><td>{{ visitor ? sourceLabel(person.subjectType) : person.staffNo }}</td><td v-if="!visitor">{{ person.employeeGradeName || '由档案校验' }}</td></tr></tbody></table>
      <p v-else>查询后选择本次要打印的人员。</p>
      <div v-if="subjectTotal" class="print-toolbar"><button :disabled="subjectPage <= 1" @click="search(subjectPage - 1)">上一页</button><span>第 {{ subjectPage }} 页 · 共 {{ subjectTotal }} 人</span><button :disabled="subjectPage * 20 >= subjectTotal" @click="search(subjectPage + 1)">下一页</button></div>
    </fieldset>
    <fieldset :disabled="busy || historyBusy || !parkId" class="print-work-section">
      <legend>2 · 选择模板和打印机</legend>
      <div class="print-toolbar"><label>模板来源 <select v-model="selectionKind" aria-label="模板来源"><option value="BOUND">自动匹配适用规则</option><option v-if="!visitor" value="PAIR">手选已关联的正反面组合</option><option value="EXPLICIT">手选已发布单面模板</option></select></label></div>
      <div v-if="selectionKind === 'PAIR'" class="print-toolbar"><label>正反面组合 <select v-model="pairId" aria-label="正反面组合"><option value="">请选择组合</option><option v-for="pair in pairs" :key="pair.pairId" :value="pair.pairId">{{ pair.name }} · 修订 {{ pair.revision }}</option></select></label></div>
      <div v-if="selectionKind === 'EXPLICIT'" class="print-toolbar"><label>{{ visitor ? '凭条模板' : '正面模板' }} <select v-model="frontId" aria-label="正面模板"><option value="">请选择已发布版本</option><option v-for="version in frontOptions" :key="version.templateVersionId" :value="version.templateVersionId">{{ version.label }}</option></select></label><label v-if="!visitor">背面模板 <select v-model="backId" aria-label="背面模板"><option value="">请选择已发布版本</option><option v-for="version in backOptions" :key="version.templateVersionId" :value="version.templateVersionId">{{ version.label }}</option></select></label></div>
      <p v-if="selectionKind !== 'BOUND'">可以选择尚未关联适用规则的新模板；人员类型、园区、卡面和尺寸仍会校验。</p>
      <div class="print-toolbar"><label>打印机 <select v-model="printerId" aria-label="打印机" @change="printerChanged"><option value="">请选择已登记设备</option><option v-for="printer in usablePrinters" :key="printer.printerProfileId" :value="printer.printerProfileId">{{ printer.displayName }}{{ printer.busy ? ' · 有待处理任务' : '' }}</option></select></label><label v-if="!visitor">翻面方式 <select v-model="printMode" aria-label="翻面方式"><option v-for="mode in modes" :key="mode" :value="mode">{{ mode === 'AUTO_DUPLEX' ? '自动翻面（已验收设备）' : '手动翻面' }}</option></select></label><span v-else>单面打印</span><button @click="loadOptions">刷新模板与设备</button></div>
    </fieldset>
    <section class="print-work-section" aria-label="打印前确认">
      <h2>3 · 核对本次打印</h2><p v-if="!selected.length">尚未选择人员。</p>
      <table v-else><thead><tr><th>人员</th><th>模板核对</th><th>操作</th></tr></thead><tbody><tr v-for="person in selected" :key="personKey(person)"><td>{{ person.displayName }}<br>{{ person.staffNo }} {{ person.employeeGradeName }}</td><td><p v-if="person.previewError" class="error">{{ person.previewError }}</p><p v-if="person.resolution">{{ resolutionLabel(person.resolution) }}</p><label><input v-model="person.confirmed" type="checkbox" :disabled="busy || !person.verified" :aria-label="'确认 ' + person.displayName + ' 的模板与资料'"> 已核对{{ visitor ? '凭条' : '正反面' }}和人员资料</label></td><td><button :disabled="busy || !printerId" @click="previewSubject(person)">查看实际预览</button><button :disabled="busy" @click="toggleSubject(person, false)">移除</button></td></tr></tbody></table>
      <button class="primary" :disabled="busy || !canSubmit" @click="submit">{{ busy ? '处理中…' : '提交 ' + selected.length + ' 人打印' }}</button>
    </section>
    <print-preview v-if="activePreview" :key="activePreview.result.previewId" :initial="activePreview.result" business :expected-faces="visitor ? ['FRONT'] : ['FRONT', 'BACK']" :load-preview="getJobPreview" :load-artifact="downloadJobPreview" @verified="previewVerified" @close="activePreview = null" />
    <job-history ref="history" @busy-state="historyBusy = $event" @editing-state="historyEditing = $event" :park-id="parkId" :print-item-type="visitor ? 'VISITOR_SLIP' : 'STAFF_CARD'" />
  </section>
</template>
<script>
import * as jobs from '@/api/platform/print/jobs'
import { parseVisitorSelection } from '@/api/platform/print/cutover'
import { listPrinterOptions } from '@/api/platform/print/printers'
import { listPairs } from '@/api/platform/print/pairs'
import { listTemplates, listVersions } from '@/api/platform/print/templates'
import { recordsOf, newIdempotencyKey } from '@/api/platform/print/client'
import PrintPreview from '@/components/print/PrintPreview.vue'
import JobHistory from './JobHistory.vue'
const staffSources = [{ value: 'STAFF', label: '员工、外包与派遣' }]
const visitorSources = [{ value: 'ADMITTANCE', label: '入厂申请主访客' }, { value: 'ADMITTANCE_COMPANION', label: '入厂申请随行人员' }, { value: 'VISITOR', label: '历史预约主访客' }, { value: 'VISITOR_COMPANION', label: '历史预约随行人员' }, { value: 'SUPPLIER_PERSON', label: '供应商人员（单面凭条）' }]
/** 候选读取到完整分页；不能只展示第一页而误导为模板不存在。 */
async function allPages(loader, params, pageKey = 'current') {
  const rows = []; let page = 1
  do { const data = await loader({ ...params, [pageKey]: page, size: 100 }); const batch = recordsOf(data); rows.push(...batch); if (!batch.length || rows.length >= Number(data.total || rows.length)) return rows; page++ } while (page <= 1000)
  throw new Error('候选列表过大，请联系管理员限定范围')
}
export default {
  name: 'PrintWorkbench', components: { PrintPreview, JobHistory }, props: { parkId: { type: String, default: '' }, visitor: Boolean },
  data() { return { subjectType: this.visitor ? 'ADMITTANCE' : 'STAFF', keyword: '', subjects: [], subjectPage: 1, subjectTotal: 0, selected: [], printers: [], pairs: [], versions: [], printerId: '', printMode: this.visitor ? 'SINGLE' : 'MANUAL_DUPLEX', selectionKind: 'BOUND', pairId: '', frontId: '', backId: '', busy: false, historyBusy: false, historyEditing: false, error: '', message: '', activePreview: null, generation: 0, searchGeneration: 0, optionsGeneration: 0, previewSequence: 0, pendingSubmit: null } },
  computed: {
    sources() { return this.visitor ? visitorSources : staffSources },
    usablePrinters() { return this.printers.filter(p => p.status === 'ENABLED' && p.deviceType === (this.visitor ? 'LABEL_PRINTER' : 'CARD_PRINTER')) },
    modes() { const printer = this.usablePrinters.find(p => p.printerProfileId === this.printerId); return (printer ? printer.allowedPrintModes : []).filter(mode => this.visitor ? mode === 'SINGLE' : ['MANUAL_DUPLEX', 'AUTO_DUPLEX'].includes(mode)) },
    frontOptions() { return this.versions.filter(v => v.faceRole === 'FRONT' && v.printItemType === (this.visitor ? 'VISITOR_SLIP' : 'STAFF_CARD')) },
    backOptions() { return this.versions.filter(v => v.faceRole === 'BACK' && v.printItemType === 'STAFF_CARD') },
    selectionSignature() { return JSON.stringify([this.parkId, this.visitor, this.printerId, this.printMode, this.selectionKind, this.pairId, this.frontId, this.backId]) },
    canSubmit() { return !!this.printerId && this.selected.length > 0 && this.selected.every(person => person.verified && person.confirmed && person.previewId) }
  },
  watch: {
    parkId: { immediate: true, handler() { this.reset(); this.loadOptions(); this.loadIncomingSelection() } },
    visitor() { this.subjectType = this.visitor ? 'ADMITTANCE' : 'STAFF'; this.printMode = this.visitor ? 'SINGLE' : 'MANUAL_DUPLEX'; this.selectionKind = 'BOUND'; this.reset(); this.loadOptions() },
    selectionSignature() { this.invalidate() }, busy() { this.$emit('busy-state', this.busy || this.historyBusy) }, historyBusy() { this.$emit('busy-state', this.busy || this.historyBusy) }, historyEditing() { this.$emit('editing-state', this.selected.length > 0 || this.historyEditing) },
    selected(value) { this.$emit('editing-state', value.length > 0 || this.historyEditing) }
  },
  beforeDestroy() { this.generation++; this.searchGeneration++; this.optionsGeneration++; this.previewSequence++ },
  beforeRouteLeave(to, from, next) { if (this.busy) return next(false); next(!this.selected.length || window.confirm('离开后本次未提交的人员选择和预览确认将清空，是否离开？')) },
  methods: {
    async loadIncomingSelection() {
      const query = this.$route && this.$route.query
      if (!this.visitor || !this.parkId || !query || !query.subjects || String(query.parkId) !== this.parkId) return
      const generation = this.searchGeneration
      try {
        const subjects = parseVisitorSelection(query.subjects)
        const result = await jobs.loadSubjectSelection({ parkId: this.parkId, subjects })
        if (generation !== this.searchGeneration) return
        for (const person of recordsOf(result)) this.toggleSubject(person, true)
        this.message = '已读取本次访客申请，请选择打印机并逐人核对实际凭条。'
      } catch (error) { if (generation === this.searchGeneration) this.error = error.message }
    },
    canLeave() { return !(this.busy || this.historyBusy) && (!(this.selected.length || this.historyEditing) || window.confirm('离开将清空未提交的人员选择与确认，是否离开？')) },
    getJobPreview: jobs.getJobPreview, downloadJobPreview: jobs.downloadJobPreview,
    personKey(person) { return person.subjectType + ':' + person.subjectId },
    sourceLabel(type) { return (this.sources.find(s => s.value === type) || {}).label || type },
    reset() { this.clearPeople(); this.printerId = ''; this.printers = []; this.pairs = []; this.versions = [] },
    clearPeople() { this.searchGeneration++; this.subjects = []; this.subjectTotal = 0; this.selected = []; this.invalidate() },
    invalidate() { this.generation++; this.previewSequence++; this.activePreview = null; this.pendingSubmit = null; this.selected.forEach(person => { person.verified = false; person.confirmed = false; person.resolution = null; person.previewError = ''; person.previewId = '' }) },
    printerChanged() { const printer = this.usablePrinters.find(p => p.printerProfileId === this.printerId); this.printMode = this.visitor ? 'SINGLE' : printer && this.modes.includes(printer.defaultPrintMode) ? printer.defaultPrintMode : this.modes[0] || '' },
    toggleSubject(person, checked) { if (checked) { if (this.selected.length >= 100) { this.error = '一次最多选择 100 人'; return } if (!this.selected.some(row => this.personKey(row) === this.personKey(person))) this.selected.push({ ...person, verified: false, confirmed: false, resolution: null, previewError: '', previewId: '' }) } else this.selected = this.selected.filter(row => this.personKey(row) !== this.personKey(person)); this.pendingSubmit = null; if (this.activePreview && this.activePreview.key === this.personKey(person) && !checked) this.activePreview = null },
    async search(page = 1) { if (!this.parkId) return; const sequence = ++this.searchGeneration; this.error = ''; try { const data = await jobs.searchSubjects({ parkId: this.parkId, subjectType: this.subjectType, keyword: this.keyword, current: page, size: 20 }); if (sequence !== this.searchGeneration) return; this.subjects = recordsOf(data); this.subjectTotal = Number(data.total || 0); this.subjectPage = page } catch (error) { if (sequence === this.searchGeneration) this.error = error.message } },
    async loadOptions() { this.invalidate(); const generation = ++this.optionsGeneration; const parkId = this.parkId; if (!parkId) return; this.error = ''; try {
      const [printers, pairs, templates] = await Promise.all([allPages(listPrinterOptions, { parkId }, 'page'), allPages(listPairs, { parkId }), allPages(listTemplates, { parkId })])
      const versions = (await Promise.all(templates.filter(t => t.lifecycleStatus !== 'ARCHIVED').map(async template => recordsOf(await listVersions(template.templateId, parkId)).filter(v => v.versionStatus === 'PUBLISHED').map(v => ({ ...v, faceRole: template.faceRole, printItemType: template.printItemType, label: template.name + ' · v' + v.versionNo }))))).flat()
      if (generation !== this.optionsGeneration) return; this.printers = printers; this.pairs = pairs.filter(p => p.status === 'ACTIVE'); this.versions = versions
    } catch (error) { if (generation === this.optionsGeneration) this.error = error.message } },
    requestFor(person, confirmed) {
      if (!this.parkId || !this.printerId) throw new Error('请选择园区和打印机')
      let selection = { kind: 'BOUND' }
      if (this.selectionKind === 'PAIR' && !this.visitor) { const pair = this.pairs.find(p => p.pairId === this.pairId); if (!pair) throw new Error('请选择系统内已保存的正反面组合'); selection = { kind: 'PAIR', pairId: pair.pairId, pairRevision: pair.revision, manualSelectionConfirmed: confirmed } }
      else if (this.selectionKind === 'EXPLICIT') { if (!this.frontId || (!this.visitor && !this.backId)) throw new Error('请选择本次打印所需的已发布卡面'); selection = { kind: 'EXPLICIT', frontTemplateVersionId: this.frontId, manualSelectionConfirmed: confirmed }; if (!this.visitor) selection.backTemplateVersionId = this.backId }
      return { parkId: this.parkId, subjectId: person.subjectId, subjectType: person.subjectType, printerProfileId: this.printerId, printMode: this.visitor ? 'SINGLE' : this.printMode, selection, ...(confirmed ? { previewId: person.previewId } : {}) }
    },
    async previewSubject(person) {
      this.error = ''; person.verified = false; person.confirmed = false; person.previewError = ''; person.previewId = ''; const generation = this.generation; const sequence = ++this.previewSequence; const key = this.personKey(person)
      try { const result = await jobs.previewJob(this.requestFor(person, false)); if (generation !== this.generation || sequence !== this.previewSequence || !this.selected.includes(person)) return; person.resolution = result.resolution; this.activePreview = { result, key, generation } } catch (error) { if (generation === this.generation && sequence === this.previewSequence) person.previewError = error.message }
    },
    previewVerified(previewId) { const active = this.activePreview; if (!active || active.result.previewId !== previewId || active.generation !== this.generation) return; const person = this.selected.find(row => this.personKey(row) === active.key); if (person) { person.verified = true; person.previewId = previewId } },
    resolutionLabel(resolution) { const auto = resolution.automaticResolution; const prefix = auto ? auto.status === 'MATCHED' ? resolution.differsFromAutomatic ? '已手选，与自动推荐不同' : '已手选，与自动推荐一致' : '已手选，当前无唯一适用规则' : '自动匹配成功'; const name = id => (this.versions.find(v => v.templateVersionId === id) || {}).label || id; return prefix + ' · ' + [resolution.frontTemplateVersionId, resolution.backTemplateVersionId].filter(Boolean).map(name).join(' / ') },
    async submit() {
      if (this.busy) return; this.error = ''; this.message = ''
      if (!this.canSubmit) { this.error = '请逐人查看实际预览，并确认模板与人员资料'; return }
      this.busy = true
      try { let data = this.requestFor(this.selected[0], true); const batch = this.selected.length > 1; if (batch) { delete data.subjectId; delete data.subjectType; delete data.previewId; data.subjects = this.selected.map(person => ({ subjectId: person.subjectId, subjectType: person.subjectType, previewId: person.previewId })) }
        const signature = JSON.stringify(data); if (!this.pendingSubmit || this.pendingSubmit.signature !== signature) this.pendingSubmit = { signature, key: newIdempotencyKey(), data }
        await (batch ? jobs.createJobBatch : jobs.createJob)(this.pendingSubmit.data, this.pendingSubmit.key)
        this.message = '打印任务已创建，请在下方查看状态并按提示核对实物。'; this.selected = []; this.activePreview = null; this.pendingSubmit = null
        if (this.$refs.history && this.$refs.history.refresh) await this.$refs.history.refresh()
      } catch (error) { if (['PRINT_PREVIEW_STALE', 'PRINT_PREVIEW_REQUIRED', 'BATCH_VALIDATION_FAILED'].includes(error.code)) this.invalidate(); this.error = error.message } finally { this.busy = false }
    }
  }
}
</script>
