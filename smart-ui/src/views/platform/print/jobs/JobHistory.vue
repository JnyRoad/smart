<template>
  <section class="print-work-section">
    <h2>打印记录与现场处理</h2>
    <form
      class="print-toolbar"
      @submit.prevent="page = 1; refresh()"><label>状态 <select v-model="status"><option value="">全部状态</option><option
        v-for="(label, key) in filterStatuses"
        :key="key"
        :value="key">{{ label }}</option></select></label><button :disabled="busy || !parkId">刷新记录</button></form>
    <p
      v-if="error"
      class="error"
      role="alert">{{ error }}</p>
    <table v-if="records.length"><thead><tr><th>创建时间</th><th>人员标识</th><th>状态</th><th>方式</th><th>操作</th></tr></thead><tbody><tr
      v-for="job in records"
      :key="job.jobId"><td>{{ localTime(job.createdAt) }}</td><td>{{ job.subjectSummary && job.subjectSummary.displayName || job.subjectId }} {{ job.subjectSummary && job.subjectSummary.employeeGradeName }}</td><td>{{ statuses[job.status] || job.status }}</td><td>{{ modes[job.printMode] }}</td><td><button
        :disabled="busy"
        @click="open(job.jobId)">查看与处理</button></td></tr></tbody></table><p v-else>当前条件下没有打印记录。</p>
    <div
      v-if="total"
      class="print-toolbar"><button
        :disabled="busy || page <= 1"
        @click="page--; refresh()">上一页</button><span>第 {{ page }} 页 · 共 {{ total }} 项</span><button
          :disabled="busy || page * 20 >= total"
          @click="page++; refresh()">下一页</button></div>
    <section
      v-if="detail"
      class="print-dialog"
      role="dialog"
      aria-label="打印任务现场处理">
      <h2>{{ statuses[detail.status] || detail.status }}</h2><p>任务 {{ detail.jobId }}<br>人员 {{ detail.subjectSummary && detail.subjectSummary.displayName || detail.subjectId }}<span v-if="detail.subjectSummary && detail.subjectSummary.staffNo"> · 工号 {{ detail.subjectSummary.staffNo }}</span> · {{ modes[detail.printMode] }}</p>
      <p>原打印机：{{ detail.printerSummary && detail.printerSummary.displayName || detail.printerProfileId }} {{ detail.printerSummary && detail.printerSummary.model }} · 工作站 {{ detail.deviceIdentity }}</p>
      <section aria-label="本任务设备取放卡指引"><h3>本任务设备取放卡指引</h3><p>正面：{{ calibration.frontFeedInstruction || '未配置取放卡说明，请按原工作站现场验收记录核对' }}<span v-if="Number.isInteger(calibration.frontRotation)"> · 版面旋转 {{ calibration.frontRotation }}°</span></p><p v-if="detail.printMode !== 'SINGLE'">背面：{{ calibration.backFeedInstruction || '未配置取放卡说明，请按原工作站现场验收记录核对' }}<span v-if="Number.isInteger(calibration.backRotation)"> · 版面旋转 {{ calibration.backRotation }}°</span></p></section>
      <print-preview
        v-if="frozenPreview"
        :key="detail.jobId + ':' + detailSequence"
        :initial="frozenPreview"
        :expected-faces="detail.printMode === 'SINGLE' ? ['FRONT'] : ['FRONT', 'BACK']"
        :load-artifact="loadFrozenArtifact"
        business
        embedded
        frozen />
      <p v-else>本任务卡面尚未生成，请刷新任务状态后查看。</p>
      <p
        v-if="error"
        class="error"
        role="alert">{{ error }}</p><p v-if="detail.errorCode">错误：{{ detail.errorCode }}</p>
      <p>设备接收请求后，仍需核对实物。请在原打印机上处理当前任务。</p>
      <ol v-if="detail.attempts"><li
        v-for="attempt in detail.attempts"
        :key="attempt.attemptId">{{ faces[attempt.face] }} · 第 {{ attempt.attemptNo }} 次 · {{ statuses[attempt.state] || attempt.state }}</li></ol>
      <form
        v-if="detail.status === 'AWAITING_FLIP'"
        @submit.prevent="flip"><p>取回当前已核对的正面卡，按这台打印机已验收的方向翻面并放回原设备。</p><label><input
          v-model="orientationConfirmed"
          type="checkbox"> 已确认是同一张卡，背面朝向与放置方向正确</label><button
            :disabled="busy || !orientationConfirmed"
            class="primary">确认翻面，继续背面</button></form>
      <form
        v-if="checkable"
        @submit.prevent="output">
        <label>本次实物结果 <select
          v-model="decision"
          aria-label="实物结果"><option value="">请选择已核对的结果</option><option
            v-if="terminal"
            value="DEVICE_CLEARANCE">保持原结果，仅补录设备清空检查</option><option
              v-if="!terminal"
              value="CONFIRMED_OUT">已出卡，内容正确</option><option
                v-if="!terminal"
                value="CONFIRMED_NOT_OUT">确认未出卡</option><option
                  v-if="!terminal"
                  value="CONFIRMED_DAMAGED">已出卡但损坏或内容错误</option></select></label>
        <label>设备内状态 <select v-model="physicalState"><option value="">请选择现场状态</option><option value="NO_CARD_IN_DEVICE">确认设备内无卡</option><option value="CARD_IN_DEVICE">设备内仍有卡</option><option value="STATE_UNKNOWN">设备内状态尚不确定</option></select></label>
        <label v-if="decision === 'CONFIRMED_NOT_OUT'">后续处理 <select v-model="resolution"><option value="CANCEL">取消当前任务</option><option value="CONTINUE">有安全证据后继续当前卡面</option></select></label>
        <p v-if="decision === 'CONFIRMED_NOT_OUT' && resolution === 'CONTINUE'">继续需要驱动明确未提交，或手动模式已在原工作站终止旧命令。系统会检查证据，结果不明时不会自动重印。</p>
        <label v-if="decision === 'CONFIRMED_NOT_OUT' && resolution === 'CONTINUE'"><input
          v-model="sameCard"
          type="checkbox"> 已确认是同一张卡及同一卡面</label>
        <label>现场核对说明 <textarea
          v-model.trim="note"
          maxlength="1000"
          required /></label>
        <label v-if="!['CONFIRMED_OUT', 'DEVICE_CLEARANCE'].includes(decision)">取消原因 <input
          v-model.trim="reason"
          maxlength="1000"></label>
        <button
          :disabled="busy || !decision || !physicalState || !note"
          class="primary">提交实物核对</button>
      </form>
      <form
        v-if="!terminal"
        @submit.prevent="cancel"><label>取消原因 <input
          v-model.trim="reason"
          maxlength="1000"
          required></label><button :disabled="busy || !reason">取消任务</button></form>
      <p v-if="detail.operatorCheckId">核对记录：<code>{{ detail.operatorCheckId }}</code><br>取出卡片并清理原队列后，在原工作站提交设备清空确认；平台确认释放后才能领取下一项。</p>
      <details v-if="events.length"><summary>操作记录</summary><ul><li
        v-for="(event, i) in events"
        :key="event.auditId || i">{{ localTime(event.createdAt) }} · {{ event.action }}</li></ul></details>
      <button
        :disabled="busy"
        @click="open(detail.jobId)">刷新当前状态</button><button
          :disabled="busy"
          @click="close">关闭</button>
    </section>
  </section>
</template>
<script>
import PrintPreview from '@/components/print/PrintPreview.vue'
import * as api from '@/api/platform/print/jobs'
import { recordsOf, newIdempotencyKey } from '@/api/platform/print/client'
const statuses = { QUEUED: '等待生成', RENDERING: '正在生成', READY: '等待设备领取', RENDER_FAILED: '版面生成失败', FRONT_IN_PROGRESS: '正面正在处理', BACK_IN_PROGRESS: '背面正在处理', AUTO_IN_PROGRESS: '自动双面正在处理', AWAITING_FRONT_CHECK: '待核对正面实物', AWAITING_FLIP: '待手动翻面', AWAITING_OUTPUT_CHECK: '待核对输出实物', RESULT_UNKNOWN: '结果不明，待现场核对', COMPLETED: '内容已核对完成', CANCELLED: '已取消', FAILED: '已失败' }
export default {
  name: 'JobHistory', components: { PrintPreview }, props: { parkId: { type: String, default: '' }, printItemType: { type: String, default: 'STAFF_CARD' } },
  data() { return { statuses, modes: { SINGLE: '单面', MANUAL_DUPLEX: '手动翻面', AUTO_DUPLEX: '自动翻面' }, faces: { FRONT: '正面', BACK: '背面', BOTH: '正反面' }, records: [], total: 0, page: 1, status: '', detail: null, events: [], busy: false, error: '', orientationConfirmed: false, decision: '', physicalState: '', resolution: 'CANCEL', sameCard: false, note: '', reason: '', pending: null, generation: 0, detailSequence: 0, listSequence: 0 } },
  computed: { calibration() { return this.detail && this.detail.printerSummary && this.detail.printerSummary.calibration || {} }, frozenPreview() { return this.detail && this.detail.artifacts && this.detail.artifacts.length ? { previewId: this.detail.jobId, status: 'READY', artifacts: this.detail.artifacts } : null }, filterStatuses() { const hidden = ['BACK_IN_PROGRESS', 'AUTO_IN_PROGRESS', 'AWAITING_FLIP']; return Object.fromEntries(Object.entries(this.statuses).filter(([key]) => this.printItemType !== 'VISITOR_SLIP' || !hidden.includes(key))) }, currentAttempt() { return this.detail && (this.detail.attempts || []).find(a => a.attemptId === this.detail.currentAttemptId) }, terminal() { return this.detail && ['COMPLETED', 'CANCELLED', 'FAILED'].includes(this.detail.status) }, checkable() { return this.currentAttempt && ['AWAITING_FRONT_CHECK', 'AWAITING_OUTPUT_CHECK', 'RESULT_UNKNOWN', 'CANCELLED', 'COMPLETED', 'FAILED'].includes(this.detail.status) } },
  watch: { busy(value) { this.$emit('busy-state', value) }, detail(value) { this.$emit('editing-state', !!value) }, parkId: { immediate: true, handler() { this.generation++; this.detail = null; this.records = []; this.page = 1; this.refresh() } }, printItemType() { this.generation++; this.detail = null; this.page = 1; this.refresh() } },
  beforeDestroy() { this.generation++ },
  methods: {
    localTime(value) { return value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '' },
    async refresh() { if (!this.parkId) return; const generation = this.generation; const sequence = ++this.listSequence; try { const data = await api.listJobs({ parkId: this.parkId, printItemType: this.printItemType, status: this.status || undefined, page: this.page, size: 20 }); if (generation !== this.generation || sequence !== this.listSequence) return; this.records = recordsOf(data); this.total = Number(data.total || 0) } catch (error) { if (generation === this.generation && sequence === this.listSequence) this.error = error.message } },
    async open(id) { const generation = this.generation; const sequence = ++this.detailSequence; try { const [detail, events] = await Promise.all([api.getJob(id), api.getJobEvents(id)]); if (generation !== this.generation || sequence !== this.detailSequence) return; this.detail = detail; this.events = recordsOf(events); this.orientationConfirmed = false; this.decision = ''; this.physicalState = ''; this.sameCard = false; this.note = ''; this.reason = ''; this.error = ''; this.pending = null } catch (error) { if (generation === this.generation && sequence === this.detailSequence) this.error = error.message } },
    close() { this.detailSequence++; this.detail = null },
    async loadFrozenArtifact(jobId, artifactId) { const detail = this.detail; const artifact = detail && detail.jobId === jobId && (detail.artifacts || []).find(item => item.artifactId === artifactId); if (!artifact || !['FRONT', 'BACK'].includes(artifact.face)) throw new Error('当前任务冻结卡面不匹配'); return api.downloadJobArtifact(jobId, artifact.face) },
    async mutate(action, body) { if (this.busy || !this.detail) return; const id = this.detail.jobId; const signature = JSON.stringify([id, action, body]); if (!this.pending || this.pending.signature !== signature) this.pending = { signature, key: newIdempotencyKey(), body }; this.busy = true; this.error = ''; try { await api[action](id, this.pending.body, this.pending.key); await this.open(id); await this.refresh() } catch (error) { this.error = error.message } finally { this.busy = false } },
    flip() { if (!this.orientationConfirmed || !this.currentAttempt || this.detail.status !== 'AWAITING_FLIP') { this.error = '请先核对同一卡片和翻面方向'; return } return this.mutate('flipJob', { attemptId: this.currentAttempt.attemptId, orientationConfirmed: true }) },
    output() { if (!this.currentAttempt || !this.decision || !this.physicalState || !this.note) { this.error = '请选择实际输出和设备状态，并填写现场说明'; return } if (this.terminal && (this.decision !== 'DEVICE_CLEARANCE' || this.physicalState !== 'NO_CARD_IN_DEVICE')) { this.error = '终态任务只允许补录设备已清空的检查'; return } const resolution = ['CONFIRMED_OUT', 'DEVICE_CLEARANCE'].includes(this.decision) ? 'NONE' : this.decision === 'CONFIRMED_DAMAGED' ? 'CANCEL' : this.resolution; if (resolution === 'CANCEL' && !this.reason) { this.error = '请填写取消原因'; return } return this.mutate('checkJobOutput', { attemptId: this.currentAttempt.attemptId, face: this.currentAttempt.face, decision: this.decision, resolution, physicalCheck: { state: this.physicalState, operatorNote: this.note, sameCardFaceVerified: this.sameCard }, ...(resolution === 'CANCEL' ? { reason: this.reason } : {}) }) },
    cancel() { if (!this.reason || this.terminal) return; return this.mutate('cancelJob', { reason: this.reason }) }
  }
}
</script>
