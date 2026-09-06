<template>
  <section class="print-page">
    <div class="print-heading"><div><h1>打印机配置</h1><p>登记工作站、介质和已验收的打印能力；新配置只影响后续创建的任务。</p></div><button
      :disabled="busy || !parkId"
      class="primary"
      @click="begin()">登记打印机</button></div>
    <p
      v-if="error"
      role="alert"
      class="error">{{ error }}</p>
    <table v-if="records.length"><thead><tr><th>打印机</th><th>工作站</th><th>允许方式</th><th>状态</th><th>操作</th></tr></thead><tbody><tr
      v-for="printer in records"
      :key="printer.printerProfileId"><td>{{ printer.displayName }}<br>{{ printer.manufacturer }} {{ printer.model }}</td><td>{{ printer.deviceIdentity }}</td><td>{{ (Array.isArray(printer.allowedPrintModes) ? printer.allowedPrintModes : []).map(mode => modeLabels[mode] || mode).join('、') }}</td><td>{{ statusLabels[printer.status] }}{{ printer.activeJobId ? ' · 有在途任务' : '' }}</td><td><button
        :disabled="busy"
        @click="begin(printer)">查看与编辑</button><button
          :disabled="busy || printer.status !== 'ENABLED'"
          @click="disableTarget = printer">停用</button></td></tr></tbody></table><p v-else>当前园区尚未登记打印机。</p>
    <div
      v-if="total"
      class="print-toolbar"><button
        :disabled="busy || page <= 1"
        @click="page--; refresh()">上一页</button><span>第 {{ page }} 页 · 共 {{ total }} 台</span><button
          :disabled="busy || page * 20 >= total"
          @click="page++; refresh()">下一页</button></div>
    <section
      v-if="form"
      class="print-dialog"
      role="dialog"
      aria-label="打印机档案">
      <h2>{{ current ? '编辑打印机' : '登记打印机' }}</h2><p
        v-if="error"
        role="alert"
        class="error">{{ error }}</p>
      <p v-if="current && current.activeJobId">已有任务继续使用原配置。请等待原任务与队列清空后再更新本机配置。</p>
      <form @submit.prevent="save"><fieldset :disabled="busy">
        <label>显示名称 <input
          v-model.trim="form.displayName"
          maxlength="100"
          required></label>
        <label>工作站标识 <input
          v-model.trim="form.deviceIdentity"
          maxlength="64"
          required
          @input="calibrationChanged"></label>
        <label>型号 <select
          v-model="form.model"
          @change="modelChanged"><option value="CS220">HiTi CS220</option><option value="CS-220e">HiTi CS-220e</option><option value="QL-800">Brother QL-800</option></select></label>
        <label>驱动版本 <input
          v-model.trim="form.driverVersion"
          maxlength="100"
          @input="calibrationChanged"></label><label>客户端 / b-PAC 版本 <input
            v-model.trim="form.sdkOrBridgeVersion"
            maxlength="100"
            @input="calibrationChanged"></label>
        <label>模板宽度（毫米） <input
          v-model.number="form.widthMm"
          :max="labelPrinter ? 58 : 200"
          type="number"
          step="0.01"
          min="1"
          required
          @change="calibrationChanged"></label>
        <label>模板高度（毫米） <input
          v-model.number="form.heightMm"
          type="number"
          step="0.01"
          min="1"
          max="1000"
          required
          @change="calibrationChanged"></label>
        <label>分辨率（DPI） <select
          v-model.number="form.dpi"
          @change="calibrationChanged"><option :value="300">300</option><option :value="600">600</option></select></label>
        <label>水平偏移（毫米） <input
          v-model.number="form.offsetX"
          type="number"
          min="-5"
          max="5"
          step="0.1"
          @change="calibrationChanged"></label><label>垂直偏移（毫米） <input
            v-model.number="form.offsetY"
            type="number"
            min="-5"
            max="5"
            step="0.1"
            @change="calibrationChanged"></label>
        <label>正面旋转 <select
          v-model.number="form.frontRotation"
          @change="calibrationChanged"><option :value="0">0°</option><option :value="180">180°</option></select></label><label v-if="!labelPrinter">背面旋转 <select
            v-model.number="form.backRotation"
            @change="calibrationChanged"><option :value="0">0°</option><option :value="180">180°</option></select></label>
        <label>正面取放卡说明 <textarea
          v-model.trim="form.frontFeedInstruction"
          maxlength="500"
          placeholder="按此工作站实测填写朝向和入卡边"
          @change="calibrationChanged"/></label><label v-if="!labelPrinter">背面取放卡说明 <textarea
            v-model.trim="form.backFeedInstruction"
            maxlength="500"
            placeholder="按此工作站实测填写翻转方式和放回方向"
            @change="calibrationChanged"/></label>
        <label><input
          v-model="form.calibrationVerified"
          type="checkbox"> 已在此工作站完成当前介质、偏移和方向校准</label>
        <template v-if="!labelPrinter"><p>手动翻面始终可配置；自动翻面需要安装并验收翻面模块。</p><label><input
          v-model="form.auto"
          type="checkbox"> 同时允许自动翻面</label>
          <template v-if="form.auto"><label><input
            v-model="form.flipVerified"
            type="checkbox"> 已完成同一卡正反面自动输出验收</label><label>验收人员 <input
              v-model.trim="form.verifiedBy"
              maxlength="100"></label><label>验收时间 <input
                v-model="form.verifiedAt"
                type="datetime-local"></label><label>默认翻面 <select v-model="form.defaultMode"><option value="MANUAL_DUPLEX">手动翻面</option><option value="AUTO_DUPLEX">自动翻面</option></select></label></template>
        </template>
        <template v-else><p>仅支持单面，最大打印宽度 58 毫米。模板尺寸与本机固定图像容器须一致。</p><label><input
          v-model="form.blackRed"
          type="checkbox"> 黑红双色介质</label><label v-if="form.blackRed"><input
            v-model="form.blackRedVerified"
            type="checkbox"> 已确认双色介质并完成黑红输出验收</label></template>
        <button class="primary">{{ busy ? '保存中…' : '保存档案' }}</button><button
          type="button"
          @click="form = null; current = null">取消</button>
      </fieldset></form>
      <details v-if="current"><summary>工作站配置标识</summary><p>档案 ID：<code>{{ current.printerProfileId }}</code><br>配置修订：{{ current.configRevision }}<br>配置 hash：<code>{{ current.printerSnapshotHash }}</code></p></details>
    </section>
    <section
      v-if="disableTarget"
      class="print-dialog"
      role="dialog"
      aria-label="停用打印机"><h2>停用 {{ disableTarget.displayName }}</h2><p>停止为这份档案创建新任务。在途任务仍需在原工作站完成核对与清空。</p><button
        :disabled="busy"
        @click="disable">确认停用</button><button
          :disabled="busy"
          @click="disableTarget = null">取消</button></section>
  </section>
</template>
<script>
import * as api from '@/api/platform/print/printers'
import { recordsOf, newIdempotencyKey } from '@/api/platform/print/client'
const clone = value => JSON.parse(JSON.stringify(value))
const uuid = () => { const key = newIdempotencyKey(); return [key.slice(0, 8), key.slice(8, 12), key.slice(12, 16), key.slice(16, 20), key.slice(20)].join('-') }
const localDate = value => { if (!value) return ''; const date = new Date(value); if (!Number.isFinite(date.getTime())) return ''; return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16) }
export default {
  name: 'PrintPrinters', props: { parkId: { type: String, default: '' } },
  data() { return { records: [], total: 0, page: 1, form: null, current: null, disableTarget: null, busy: false, error: '', pending: null, generation: 0, modeLabels: { SINGLE: '单面', MANUAL_DUPLEX: '手动翻面', AUTO_DUPLEX: '自动翻面' }, statusLabels: { ENABLED: '启用', DISABLED: '停用', QUARANTINED: '隔离' } } },
  computed: { labelPrinter() { return this.form && this.form.model === 'QL-800' } },
  watch: { parkId: { immediate: true, handler() { this.generation++; this.page = 1; this.form = null; this.current = null; this.disableTarget = null; this.records = []; this.refresh() } }, busy(value) { this.$emit('busy-state', value) }, form(value) { this.$emit('editing-state', !!value) } },
  beforeDestroy() { this.generation++ },
  beforeRouteLeave(to, from, next) { if (this.busy) return next(false); next(!this.form || window.confirm('离开将放弃未保存的打印机配置，是否离开？')) },
  methods: {
    async refresh() { if (!this.parkId) return; const generation = this.generation; try { const data = await api.listPrinters({ parkId: this.parkId, page: this.page, size: 20 }); if (generation !== this.generation) return; this.records = recordsOf(data); this.total = Number(data.total || 0) } catch (error) { if (generation === this.generation) this.error = error.message } },
    async begin(row) { this.error = ''; this.pending = null; const generation = this.generation; try { const current = row ? await api.getPrinter(row.printerProfileId) : null; if (generation !== this.generation) return; this.current = current; const p = current || {}; const media = p.mediaSpec || {}; const calibration = p.calibration || {}; const evidence = (p.capabilityEvidence || []).find(e => e.type === 'FLIP_MODULE') || {}; this.form = { displayName: p.displayName || '', deviceIdentity: p.deviceIdentity || '', model: p.model || 'CS220', driverVersion: p.driverVersion || '', sdkOrBridgeVersion: p.sdkOrBridgeVersion || '', widthMm: media.widthMm || 85.6, heightMm: media.heightMm || 53.98, dpi: calibration.dpi || 300, offsetX: calibration.offsetXMm || 0, offsetY: calibration.offsetYMm || 0, frontRotation: calibration.frontRotation || 0, backRotation: calibration.backRotation || 0, frontFeedInstruction: calibration.frontFeedInstruction || '', backFeedInstruction: calibration.backFeedInstruction || '', calibrationVerified: calibration.verified === true, auto: (p.allowedPrintModes || []).includes('AUTO_DUPLEX'), flipVerified: p.capabilityStatus === 'VERIFIED' && p.flipCapability === 'AUTO_VERIFIED', verifiedBy: evidence.verifiedBy || '', verifiedAt: localDate(evidence.verifiedAt), evidenceId: evidence.evidenceId || uuid(), defaultMode: p.defaultPrintMode || 'MANUAL_DUPLEX', blackRed: media.blackRed === true, blackRedVerified: media.blackRedCapabilityVerified === true && media.blackRedMediaConfirmed === true } } catch (error) { this.error = error.message } },
    calibrationChanged() { this.form.calibrationVerified = false; this.form.flipVerified = false; this.form.blackRedVerified = false; this.form.verifiedBy = ''; this.form.verifiedAt = ''; this.form.evidenceId = uuid() },
    modelChanged() { this.calibrationChanged(); this.form.frontFeedInstruction = ''; this.form.backFeedInstruction = ''; this.form.auto = false; this.form.flipVerified = false; this.form.calibrationVerified = false; this.form.blackRed = false; this.form.blackRedVerified = false; this.form.widthMm = this.labelPrinter ? 58 : 85.6; this.form.heightMm = this.labelPrinter ? 76 : 53.98; this.form.defaultMode = this.labelPrinter ? 'SINGLE' : 'MANUAL_DUPLEX' },
    payload() { const f = this.form; if (!f.displayName || !/^[A-Za-z0-9_-]{1,64}$/.test(f.deviceIdentity)) throw new Error('请填写名称和工作站标识（1–64位英文字母、数字、下划线或连字符）'); if (![f.widthMm, f.heightMm, f.offsetX, f.offsetY].every(Number.isFinite) || f.widthMm <= 0 || f.heightMm <= 0 || f.widthMm > (this.labelPrinter ? 58 : 200) || f.heightMm > 1000 || Math.abs(f.offsetX) > 5 || Math.abs(f.offsetY) > 5) throw new Error('介质尺寸或偏移超出允许范围');
      if (!this.labelPrinter && f.auto && (!f.flipVerified || !f.calibrationVerified || !f.driverVersion || !f.verifiedBy || !f.verifiedAt || !Number.isFinite(new Date(f.verifiedAt).getTime()))) throw new Error('自动翻面需要完整的模块、介质校准、驱动与验收记录'); if (this.labelPrinter && f.blackRed && !f.blackRedVerified) throw new Error('黑红打印需要介质与能力验收');
      const data = { parkId: this.parkId, displayName: f.displayName, deviceIdentity: f.deviceIdentity, manufacturer: this.labelPrinter ? 'Brother' : 'HiTi', model: f.model, deviceType: this.labelPrinter ? 'LABEL_PRINTER' : 'CARD_PRINTER', connectionType: 'USB_LOCAL_CLIENT', driverVersion: f.driverVersion, sdkOrBridgeVersion: f.sdkOrBridgeVersion, allowedPrintModes: this.labelPrinter ? ['SINGLE'] : f.auto ? ['MANUAL_DUPLEX', 'AUTO_DUPLEX'] : ['MANUAL_DUPLEX'], defaultPrintMode: this.labelPrinter ? 'SINGLE' : f.auto ? f.defaultMode : 'MANUAL_DUPLEX', capabilityStatus: f.auto && !this.labelPrinter ? 'VERIFIED' : 'UNVERIFIED', flipCapability: this.labelPrinter ? 'NONE' : f.auto ? 'AUTO_VERIFIED' : 'MANUAL_ONLY', mediaSpec: { ...clone((this.current || {}).mediaSpec || {}), widthMm: f.widthMm, heightMm: f.heightMm }, calibration: { ...clone((this.current || {}).calibration || {}), dpi: f.dpi, offsetXMm: f.offsetX, offsetYMm: f.offsetY, frontRotation: f.frontRotation, backRotation: this.labelPrinter ? 0 : f.backRotation, frontFeedInstruction: f.frontFeedInstruction || '', backFeedInstruction: this.labelPrinter ? '' : f.backFeedInstruction || '', verified: f.calibrationVerified }, capabilityEvidence: clone(((this.current || {}).capabilityEvidence || []).filter(e => e.type !== 'FLIP_MODULE')) }
      if (this.labelPrinter) Object.assign(data.mediaSpec, { maxPrintableWidthMm: 58, blackRed: f.blackRed, blackRedMediaConfirmed: f.blackRedVerified, blackRedCapabilityVerified: f.blackRedVerified }); else { delete data.mediaSpec.maxPrintableWidthMm; delete data.mediaSpec.blackRed; delete data.mediaSpec.blackRedMediaConfirmed; delete data.mediaSpec.blackRedCapabilityVerified }
      if (f.auto && !this.labelPrinter) data.capabilityEvidence.push({ type: 'FLIP_MODULE', verified: true, evidenceId: f.evidenceId, driverVersion: f.driverVersion, verifiedBy: f.verifiedBy, verifiedAt: new Date(f.verifiedAt).toISOString() }); if (this.current) data.configRevision = this.current.configRevision; return data
    },
    async save() { if (this.busy) return; this.error = ''; try { const data = this.payload(); const id = this.current && this.current.printerProfileId; const signature = JSON.stringify([id, data]); if (!this.pending || this.pending.signature !== signature) this.pending = { signature, data, key: newIdempotencyKey() }; this.busy = true; await api.savePrinter(id, this.pending.data, this.pending.key); this.form = null; this.current = null; this.pending = null; await this.refresh() } catch (error) { this.error = error.message } finally { this.busy = false } },
    async disable() { if (this.busy || !this.disableTarget) return; const row = this.disableTarget; const data = { configRevision: row.configRevision, status: 'DISABLED' }; const signature = JSON.stringify([row.printerProfileId, data]); if (!this.pending || this.pending.signature !== signature) this.pending = { signature, data, key: newIdempotencyKey() }; this.busy = true; try { await api.disablePrinter(row.printerProfileId, this.pending.data, this.pending.key); this.disableTarget = null; this.pending = null; await this.refresh() } catch (error) { this.error = error.message } finally { this.busy = false } }
  }
}
</script>
