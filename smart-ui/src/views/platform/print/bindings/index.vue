<template>
  <section class="print-page" :aria-busy="busy">
    <header class="print-heading">
      <div><h1>模板适用规则</h1><p>按人员职级、公司或供应商关联模板。未关联的新模板仍可在打印时手动选择。</p></div>
      <button class="primary" :disabled="busy || !parkId" @click="beginEdit()">新增适用规则</button>
    </header>
    <p v-if="error" role="alert" class="error">{{ error }}</p>
    <p v-if="message" role="status">{{ message }}</p>
    <button :disabled="busy || !parkId" @click="refresh">刷新规则</button>
    <table>
      <thead><tr><th>人员与分类</th><th>适用职级</th><th>范围</th><th>模板关联</th><th>优先级</th><th>有效期</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="row in records" :key="row.bindingRuleId">
          <td>{{ personTypes[row.personType] }} · {{ row.classificationCode }}</td>
          <td>{{ gradeLabels(row.employeeGradeCodes) }}</td>
          <td>{{ scopes[row.scopeType] }} {{ row.scopeId || '' }}</td>
          <td>{{ row.pairName || row.templateName || row.pairId || row.templateId }}</td>
          <td>{{ row.priority }}</td><td>{{ displayTime(row.validFrom) }} 至 {{ displayTime(row.validTo) }}</td>
          <td>{{ row.status === 'ACTIVE' ? '启用' : '停用' }}</td>
          <td><button :disabled="busy" @click="beginEdit(row)">编辑</button><button :disabled="busy || row.status !== 'ACTIVE'" @click="disable(row)">停用</button></td>
        </tr>
      </tbody>
    </table>
    <p v-if="!busy && !records.length">{{ parkId ? '暂无适用规则。可先发布模板并维护正反面组合。' : '请先选择园区。' }}</p>
    <div class="print-toolbar"><button :disabled="busy || page === 1" @click="changePage(-1)">上一页</button><span>第 {{ page }} 页 · 共 {{ total }} 条</span><button :disabled="busy || page * 20 >= total" @click="changePage(1)">下一页</button></div>
    <section v-if="form" class="print-dialog" role="dialog" aria-label="模板适用规则配置">
      <h2>{{ form.bindingRuleId ? '修改规则' : '新增规则' }}</h2>
      <form @submit.prevent="save">
        <fieldset :disabled="busy" class="print-form-fields">
          <label>人员类型 <select v-model="form.personType" aria-label="人员类型" :disabled="!!form.bindingRuleId" @change="changeType"><option v-for="(label, key) in personTypes" :key="key" :value="key">{{ label }}</option></select></label>
          <label>{{ visitor ? '单面模板' : '模板组合' }} <select v-model="form.targetId" :aria-label="visitor ? '单面模板' : '模板组合'"><option value="">请选择</option><option v-for="item in targets" :key="item.pairId || item.templateId" :value="item.pairId || item.templateId">{{ item.name }} · {{ item.classificationCode }}</option></select></label>
          <template v-if="form.personType === 'EMPLOYEE'">
            <p v-if="gradeError" role="alert" class="error">{{ gradeError }}</p>
            <label>适用职级 <select v-model="form.employeeGradeCodes" multiple aria-label="适用职级" :disabled="!!gradeError"><option v-for="grade in grades" :key="grade.code" :value="grade.code">{{ grade.name }}</option></select></label>
            <p>可以选择多个职级；字典来自系统已确认的 DHR 职层配置。</p>
          </template>
          <label>适用范围 <select v-model="form.scopeType" aria-label="适用范围" @change="form.scopeId = ''"><option v-for="(label, key) in scopes" :key="key" :value="key">{{ label }}</option></select></label>
          <label v-if="form.scopeType !== 'EXPLICIT_DEFAULT'">{{ form.scopeType === 'COMPANY' ? '公司编码' : '供应商编码' }} <input v-model.trim="form.scopeId" aria-label="范围编码" maxlength="64"></label>
          <label>优先级 <input v-model.number="form.priority" type="number" aria-label="优先级" min="0" max="100000"></label>
          <label>生效时间 <input v-model="form.validFromInput" type="datetime-local" aria-label="生效时间"></label>
          <label>失效时间 <input v-model="form.validToInput" type="datetime-local" aria-label="失效时间"></label>
          <p>优先匹配具体公司或供应商，再使用明确默认。同级规则范围和生效时间重叠时，系统拒绝保存。</p>
          <button class="primary" type="submit">保存规则</button><button type="button" @click="form = null">取消</button>
        </fieldset>
      </form>
    </section>
  </section>
</template>
<script>
import { personTypes } from '@/components/print/template-model'
import { recordsOf, newIdempotencyKey } from '@/api/platform/print/client'
import { listPairs } from '@/api/platform/print/pairs'
import { listTemplates } from '@/api/platform/print/templates'
import * as api from '@/api/platform/print/bindings'
const scopes = { EXPLICIT_DEFAULT: '明确默认', COMPANY: '指定公司', SUPPLIER: '指定供应商' }
const localTime = value => { if (!value) return ''; const date = new Date(value); return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16) }
export default {
  name: 'PrintBindings', props: { parkId: { type: [String, Number], default: '' } },
  data() { return { personTypes, scopes, records: [], pairs: [], templates: [], grades: [], gradeError: '', form: null, busy: false, error: '', message: '', total: 0, page: 1, pending: null, pendingDisable: null } },
  computed: {
    visitor() { return this.form && this.form.personType === 'VISITOR' },
    targets() { return (this.visitor ? this.templates : this.pairs).filter(row => row.personType === this.form.personType) }
  },
  watch: {
    parkId: { immediate: true, handler() { this.form = null; this.page = 1; this.refresh() } },
    busy(value) { this.$emit('busy-state', value) }, form(value) { this.$emit('editing-state', !!value) }
  },
  beforeRouteLeave(to, from, next) { if (this.busy) return next(false); if (!this.form) return next(); this.$confirm('适用规则尚未保存，确定离开？', '未保存的规则').then(() => next()).catch(() => next(false)) },
  methods: {
    async run(action) { if (this.busy) return; this.busy = true; this.error = ''; this.message = ''; try { await action() } catch (error) { this.error = error.message || '规则操作失败' } finally { this.busy = false } },
    refresh() { return this.run(() => this.loadList()) },
    async loadList() { if (!this.parkId) { this.records = []; this.total = 0; return } const result = await api.listBindings({ parkId: this.parkId, current: this.page, size: 20 }); this.records = recordsOf(result); this.total = Number(result.total || 0) },
    changePage(step) { this.page += step; this.refresh() },
    displayTime(value) { return value ? new Date(value).toLocaleString() : '长期' },
    gradeLabels(values) { return (values || []).map(code => { const grade = this.grades.find(item => item.code === code); return grade ? grade.name : code }).join('、') || '不适用' },
    /** 候选分页读完，避免新增或较旧的合法模板在关联界面被隐藏。 */
    async all(fetch, params) { const rows = []; let current = 1; let total; do { const result = await fetch({ ...params, current, size: 100 }); const batch = recordsOf(result); rows.push(...batch); total = Number(result.total || rows.length); if (!batch.length) break; current++ } while (rows.length < total); return rows },
    beginEdit(row) { return this.run(async () => {
      this.pairs = (await this.all(listPairs, { parkId: this.parkId, status: 'ACTIVE' })).filter(item => item.status === 'ACTIVE')
      this.templates = (await this.all(listTemplates, { parkId: this.parkId, printItemType: 'VISITOR_SLIP', lifecycleStatus: 'ACTIVE' })).filter(item => item.currentPublishedVersionId && item.faceRole === 'FRONT')
      this.grades = []; this.gradeError = ''
      try { const result = await api.employeeGrades(this.parkId); if (result.confirmed !== true) throw new Error('DHR职层映射尚未确认'); this.grades = recordsOf(result) } catch (error) { this.gradeError = error.message || 'DHR职层映射尚未确认' }
      this.form = row ? { ...row, targetId: row.pairId || row.templateId, employeeGradeCodes: [...(row.employeeGradeCodes || [])], scopeId: row.scopeId || '', validFromInput: localTime(row.validFrom), validToInput: localTime(row.validTo) } : { personType: 'EMPLOYEE', targetId: '', employeeGradeCodes: [], scopeType: 'EXPLICIT_DEFAULT', scopeId: '', priority: 100, validFromInput: localTime(new Date()), validToInput: '' }
      this.pending = null
    }) },
    changeType() { this.form.targetId = ''; this.form.employeeGradeCodes = [] },
    save() { return this.run(async () => {
      if (!this.form) return
      if (this.form.personType === 'EMPLOYEE' && (this.gradeError || !this.form.employeeGradeCodes.length || this.form.employeeGradeCodes.some(code => !this.grades.some(grade => grade.code === code)))) throw new Error(this.gradeError || '请选择已确认的适用职级')
      const target = this.targets.find(item => (item.pairId || item.templateId) === this.form.targetId)
      if (!target) throw new Error('请选择已发布的模板或有效组合')
      if (this.form.scopeType !== 'EXPLICIT_DEFAULT' && !this.form.scopeId) throw new Error('请填写公司或供应商编码')
      const validFrom = new Date(this.form.validFromInput); const validTo = this.form.validToInput ? new Date(this.form.validToInput) : null
      if (!Number.isFinite(validFrom.getTime()) || (validTo && (!Number.isFinite(validTo.getTime()) || validTo <= validFrom))) throw new Error('请填写正确的生效和失效时间')
      const data = { parkId: this.parkId, printItemType: this.visitor ? 'VISITOR_SLIP' : 'STAFF_CARD', personType: this.form.personType, classificationCode: target.classificationCode, scopeType: this.form.scopeType, scopeId: this.form.scopeType === 'EXPLICIT_DEFAULT' ? null : this.form.scopeId, employeeGradeCodes: this.form.personType === 'EMPLOYEE' ? [...this.form.employeeGradeCodes] : null, priority: this.form.priority, validFrom: validFrom.toISOString(), validTo: validTo ? validTo.toISOString() : null }
      if (this.visitor) data.templateId = target.templateId; else data.pairId = target.pairId
      if (this.form.bindingRuleId) data.revision = this.form.revision
      const signature = JSON.stringify({ id: this.form.bindingRuleId, data })
      if (!this.pending || this.pending.signature !== signature) this.pending = { signature, key: newIdempotencyKey() }
      await api.saveBinding(this.form.bindingRuleId || null, data, this.pending.key)
      this.form = null; this.pending = null; await this.loadList(); this.message = '模板适用规则已保存到系统'
    }) },
    disable(row) { return this.run(async () => {
      const data = { parkId: this.parkId, revision: row.revision }; const signature = JSON.stringify({ id: row.bindingRuleId, data })
      if (!this.pendingDisable || this.pendingDisable.signature !== signature) {
        try { await this.$confirm('停用后不再自动匹配此规则，历史打印任务不受影响。', '停用规则') } catch (_) { return }
        this.pendingDisable = { signature, key: newIdempotencyKey() }
      }
      await api.disableBinding(row.bindingRuleId, data, this.pendingDisable.key); this.pendingDisable = null; await this.loadList()
    }) }
  }
}
</script>
<style src="../print.css" />
<style scoped>.print-form-fields { border: 0; padding: 0; margin: 0; } select[multiple] { min-height: 100px; }</style>
