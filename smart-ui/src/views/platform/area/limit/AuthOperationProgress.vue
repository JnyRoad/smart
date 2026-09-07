<template>
  <el-dialog
    :visible="value"
    :close-on-click-modal="false"
    class="auth-operation-progress"
    title="权限任务"
    width="92%"
    top="5vh"
    @close="requestClose"
  >
    <p class="auth-operation-progress__scope-note">
      这里展示当前账号可访问园区的权限任务。可使用精确操作键查询已接入的新任务。
    </p>

    <p v-if="batchFilters.sourceId && !batchLoading && !batchError && !batches.length">
      未找到当前操作的可访问批次，请核对操作键或稍后刷新。
    </p>
    <div class="auth-operation-progress__toolbar">
      <el-input
        v-model.trim="batchFilters.sourceId"
        class="auth-operation-progress__operation-key"
        clearable
        size="small"
        placeholder="精确操作键（sourceId）"
        @keyup.enter.native="searchBatches"
      />
      <el-select
        v-model="batchFilters.status"
        clearable
        size="small"
        placeholder="批次状态">
        <el-option
          v-for="option in statusOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value" />
      </el-select>
      <el-button
        :disabled="batchLoading"
        type="primary"
        size="small"
        @click="searchBatches">查询</el-button>
      <el-button
        :disabled="batchLoading"
        plain
        size="small"
        @click="loadBatchPage">刷新</el-button>
    </div>

    <div
      v-if="batchError"
      class="auth-operation-progress__error"
      role="alert">
      <span>{{ batchError }}</span>
      <el-button
        class="auth-operation-progress__batch-retry"
        type="text"
        size="small"
        @click="loadBatchPage">重试</el-button>
    </div>

    <el-table
      v-loading="batchLoading"
      :data="batches"
      border
      size="small"
      empty-text="暂无可访问的权限任务">
      <el-table-column
        label="批次 ID"
        min-width="170">
        <template slot-scope="scope">
          <code
            :title="scope.row.batchId"
            class="auth-operation-progress__long-text">{{ scope.row.batchId }}</code>
        </template>
      </el-table-column>
      <el-table-column
        prop="parkId"
        label="园区"
        min-width="90" />
      <el-table-column
        label="业务动作"
        min-width="90">
        <template slot-scope="scope">{{ actionLabel(scope.row.action) }}</template>
      </el-table-column>
      <el-table-column
        label="批次状态"
        min-width="132">
        <template slot-scope="scope">
          <span :class="statusClass(scope.row.status)">{{ statusLabel(scope.row.status) }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="说明 / 失败原因"
        min-width="180">
        <template slot-scope="scope">
          <span
            :title="scope.row.failureReason"
            class="auth-operation-progress__long-text">{{ scope.row.failureReason || '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="expectedCount"
        label="预期"
        min-width="78" />
      <el-table-column
        prop="expandedCount"
        label="已展开"
        min-width="78" />
      <el-table-column
        label="受理时间"
        min-width="168">
        <template slot-scope="scope">{{ formatDate(scope.row.acceptedAt) }}</template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="74"
        fixed="right">
        <template slot-scope="scope">
          <el-button
            type="text"
            size="small"
            @click="selectBatch(scope.row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      :current-page="batchPage.currentPage"
      :page-size="batchPage.pageSize"
      :page-sizes="[20, 50, 100]"
      :total="batchPage.total"
      class="auth-operation-progress__pagination"
      background
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="changeBatchSize"
      @current-change="changeBatchPage"
    />

    <section
      v-if="selectedBatchId"
      class="auth-operation-progress__detail"
      aria-label="当前批次进度">
      <div class="auth-operation-progress__detail-heading">
        <div>
          <span class="auth-operation-progress__eyebrow">当前批次</span>
          <code
            :title="selectedBatchId"
            class="auth-operation-progress__selected-id">{{ selectedBatchId }}</code>
        </div>
        <el-button
          :disabled="pollInFlight"
          plain
          size="small"
          @click="refreshSelectedBatch(true)">刷新进度</el-button>
      </div>

      <div
        v-if="detailError"
        class="auth-operation-progress__error"
        role="alert">
        <span>{{ detailError }}</span>
        <el-button
          type="text"
          size="small"
          @click="loadSelectedDetail">重试</el-button>
      </div>

      <div
        v-loading="detailLoading"
        class="auth-operation-progress__summary">
        <div class="auth-operation-progress__status-line">
          <span
            :class="statusClass(currentStatus)"
            class="auth-operation-progress__current-status">{{ statusLabel(currentStatus) }}</span>
          <span class="auth-operation-progress__percent">{{ progressPercentage }}%</span>
          <span class="auth-operation-progress__refresh-hint">活跃任务每 4 秒自动刷新，页面隐藏或面板关闭时暂停</span>
        </div>
        <p
          v-if="selectedBatch && selectedBatch.failureReason"
          :title="selectedBatch.failureReason"
          class="auth-operation-progress__batch-reason"
        >{{ selectedBatch.failureReason }}</p>
        <el-progress
          :percentage="progressPercentage"
          :status="progressBarStatus" />
        <div class="auth-operation-progress__metrics">
          <div><span>预期目标</span><strong>{{ progressCount('expectedCount') }}</strong></div>
          <div><span>已展开</span><strong>{{ progressCount('expandedCount') }}</strong></div>
          <div><span>尚未展开</span><strong>{{ unexpandedCount }}</strong></div>
          <div><span>待设备确认</span><strong>{{ progressCount('waitingConfirmCount') }}</strong></div>
          <div><span>待核验</span><strong>{{ progressCount('verifyingCount') }}</strong></div>
          <div><span>已确认待收敛</span><strong>{{ progressCount('confirmedCount') }}</strong></div>
          <div><span>已收敛</span><strong>{{ progressCount('convergedCount') }}</strong></div>
          <div><span>失败</span><strong>{{ progressCount('failedCount') }}</strong></div>
          <div><span>后台未完成</span><strong>{{ progressCount('unfinishedCount') }}</strong></div>
        </div>
      </div>

      <div class="auth-operation-progress__target-toolbar">
        <el-select
          v-model="targetFilters.states"
          multiple
          collapse-tags
          clearable
          size="small"
          placeholder="目标状态">
          <el-option
            v-for="option in statusOptions"
            :key="option.value"
            :label="option.label"
            :value="option.value" />
        </el-select>
        <el-input
          v-model.trim="targetFilters.deviceId"
          clearable
          size="small"
          placeholder="精确设备 ID" />
        <el-input
          v-model.trim="targetFilters.subjectType"
          clearable
          size="small"
          placeholder="精确主体类型" />
        <el-button
          :disabled="targetLoading"
          size="small"
          @click="searchTargets">查询明细</el-button>
      </div>

      <div
        v-if="targetError"
        class="auth-operation-progress__error"
        role="alert">
        <span>{{ targetError }}</span>
        <el-button
          type="text"
          size="small"
          @click="loadTargetPage">重试</el-button>
      </div>

      <el-table
        v-loading="targetLoading"
        :data="targets"
        border
        size="small"
        empty-text="当前筛选条件下暂无目标明细">
        <el-table-column
          label="目标 ID"
          min-width="160">
          <template slot-scope="scope">
            <code
              :title="scope.row.targetId"
              class="auth-operation-progress__long-text">{{ scope.row.targetId }}</code>
          </template>
        </el-table-column>
        <el-table-column
          prop="deviceId"
          label="设备 ID"
          min-width="140"
          show-overflow-tooltip />
        <el-table-column
          prop="subjectType"
          label="主体类型"
          min-width="100" />
        <el-table-column
          label="目标状态"
          min-width="132">
          <template slot-scope="scope">
            <span :class="statusClass(scope.row.state)">{{ statusLabel(scope.row.state) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="失败原因"
          min-width="220">
          <template slot-scope="scope">
            <span
              :title="scope.row.failureReason"
              class="auth-operation-progress__long-text">{{ scope.row.failureReason || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="latestAttemptNo"
          label="最新尝试"
          min-width="86" />
        <el-table-column
          prop="latestAttemptStatus"
          label="尝试状态"
          min-width="100"
          show-overflow-tooltip />
        <el-table-column
          label="外部批次 / 命令"
          min-width="190">
          <template slot-scope="scope">
            <span
              :title="externalReference(scope.row)"
              class="auth-operation-progress__long-text">{{ externalReference(scope.row) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="确认时间"
          min-width="168">
          <template slot-scope="scope">{{ formatDate(scope.row.confirmedAt) }}</template>
        </el-table-column>
      </el-table>

      <el-pagination
        :current-page="targetPage.currentPage"
        :page-size="targetPage.pageSize"
        :page-sizes="[20, 50, 100]"
        :total="targetPage.total"
        class="auth-operation-progress__pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="changeTargetSize"
        @current-change="changeTargetPage"
      />
    </section>
  </el-dialog>
</template>

<script>
import {
  fetchOperationBatchPage,
  fetchOperationTargetPage,
  getOperationBatchDetail
} from '@/api/platform/area/limit'

const STATUS_META = {
  PREPARING: { label: '准备中', tone: 'info' },
  QUEUED: { label: '已排队', tone: 'info' },
  EXECUTING: { label: '执行中', tone: 'warning' },
  WAITING_CONFIRM: { label: '待设备确认', tone: 'warning' },
  VERIFYING: { label: '待核验', tone: 'warning' },
  CONFIRMED: { label: '设备已确认，待本地收敛', tone: 'warning' },
  CONVERGED: { label: '已收敛', tone: 'success' },
  FAILED: { label: '失败', tone: 'danger' },
  RETAINED: { label: '已验证保留', tone: 'success' }
}

const ACTIVE_BATCH_STATUSES = ['PREPARING', 'QUEUED', 'EXECUTING', 'WAITING_CONFIRM', 'VERIFYING', 'CONFIRMED']

export default {
  name: 'AuthOperationProgress',
  props: {
    operationKey: { type: String, default: '' },
    value: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      appliedOperationKey: '',
      panelIsOpen: false,
      batches: [],
      batchLoading: false,
      batchError: '',
      batchFilters: { sourceId: '', status: '' },
      batchPage: { currentPage: 1, pageSize: 20, total: 0 },
      selectedBatchId: '',
      selectedBatch: null,
      detailLoading: false,
      detailError: '',
      targets: [],
      targetLoading: false,
      targetError: '',
      targetFilters: { states: [], deviceId: '', subjectType: '' },
      targetPage: { currentPage: 1, pageSize: 20, total: 0 },
      pollTimer: null,
      pollInFlight: false,
      batchRequestSequence: 0,
      detailRequestSequence: 0,
      targetRequestSequence: 0,
      refreshSequence: 0
    }
  },
  computed: {
    statusOptions () {
      return Object.keys(STATUS_META).map(value => ({ value, label: STATUS_META[value].label }))
    },
    currentStatus () {
      if (!this.selectedBatch) return ''
      return this.selectedBatch.status || (this.selectedBatch.progress && this.selectedBatch.progress.batchStatus) || ''
    },
    progressPercentage () {
      const progress = (this.selectedBatch && this.selectedBatch.progress) || {}
      const expected = this.toCount(progress.expectedCount)
      const converged = this.toCount(progress.convergedCount)
      if (expected <= 0) return 0
      const percentage = Math.min(100, Math.max(0, Math.round(converged * 100 / expected)))
      return percentage === 100 && this.currentStatus !== 'CONVERGED' ? 99 : percentage
    },
    progressBarStatus () {
      return this.currentStatus === 'CONVERGED' ? 'success' : undefined
    },
    unexpandedCount () {
      const progress = (this.selectedBatch && this.selectedBatch.progress) || {}
      return Math.max(0, this.toCount(progress.expectedCount) - this.toCount(progress.expandedCount))
    }
  },
  watch: {
    operationKey () {
      if (this.adoptOperationKey() && this.value && this.panelIsOpen) this.loadBatchPage()
    },
    value: {
      immediate: true,
      handler (visible) {
        if (visible) this.openPanel()
        else {
          this.panelIsOpen = false
          this.stopPanelActivity()
        }
      }
    }
  },
  mounted () {
    document.addEventListener('visibilitychange', this.handleVisibilityChange)
  },
  beforeDestroy () {
    document.removeEventListener('visibilitychange', this.handleVisibilityChange)
    this.stopPanelActivity()
  },
  methods: {
    openPanel () {
      this.panelIsOpen = true
      this.adoptOperationKey()
      this.loadBatchPage()
      if (this.selectedBatchId && this.isActiveBatch()) this.refreshSelectedBatch(true)
    },
    adoptOperationKey () {
      if (this.appliedOperationKey === this.operationKey) return false
      // 切换操作先使所有在途查询失效，防止迟到的批次和目标覆盖新结果。
      this.stopPanelActivity()
      this.appliedOperationKey = this.operationKey
      this.batchFilters = { sourceId: this.operationKey, status: '' }
      this.batchPage.currentPage = 1
      this.batchPage.total = 0
      this.batches = []
      this.batchError = ''
      this.selectedBatchId = ''
      this.selectedBatch = null
      this.targets = []
      this.detailError = ''
      this.targetError = ''
      this.targetFilters = { states: [], deviceId: '', subjectType: '' }
      this.targetPage.currentPage = 1
      this.targetPage.total = 0
      return true
    },
    requestClose () {
      this.stopPanelActivity()
      this.$emit('input', false)
    },
    stopPanelActivity () {
      this.clearPollTimer()
      this.batchRequestSequence += 1
      this.detailRequestSequence += 1
      this.targetRequestSequence += 1
      this.refreshSequence += 1
      this.pollInFlight = false
      this.batchLoading = false
      this.detailLoading = false
      this.targetLoading = false
    },
    compactParams (params) {
      return Object.keys(params).reduce((result, key) => {
        const value = params[key]
        if (value !== '' && value !== null && value !== undefined) result[key] = value
        return result
      }, {})
    },
    async loadBatchPage () {
      const requestSequence = ++this.batchRequestSequence
      this.batchLoading = true
      this.batchError = ''
      try {
        const response = await fetchOperationBatchPage(this.compactParams({
          current: this.batchPage.currentPage,
          size: this.batchPage.pageSize,
          sourceId: this.batchFilters.sourceId,
          status: this.batchFilters.status
        }))
        if (!this.value || requestSequence !== this.batchRequestSequence) return
        const page = response.data.data
        this.batches = Array.isArray(page.records) ? page.records : []
        this.batchPage.total = this.toCount(page.total)
      } catch (error) {
        if (this.value && requestSequence === this.batchRequestSequence) {
          this.batchError = this.errorMessage(error, '权限任务列表加载失败')
        }
      } finally {
        if (requestSequence === this.batchRequestSequence) this.batchLoading = false
      }
    },
    searchBatches () {
      this.batchPage.currentPage = 1
      return this.loadBatchPage()
    },
    changeBatchSize (size) {
      this.batchPage.currentPage = 1
      this.batchPage.pageSize = Math.min(100, Math.max(1, size))
      return this.loadBatchPage()
    },
    changeBatchPage (currentPage) {
      this.batchPage.currentPage = currentPage
      return this.loadBatchPage()
    },
    selectBatch (batch) {
      this.clearPollTimer()
      this.selectedBatchId = String(batch.batchId)
      this.selectedBatch = { ...batch, batchId: this.selectedBatchId }
      this.targetPage.currentPage = 1
      this.detailError = ''
      this.targetError = ''
      return this.refreshSelectedBatch(true)
    },
    async refreshSelectedBatch (force = false) {
      if (!this.value || !this.selectedBatchId || (!force && this.pollInFlight)) return
      const batchId = this.selectedBatchId
      const refreshSequence = ++this.refreshSequence
      this.pollInFlight = true
      try {
        await Promise.all([this.loadSelectedDetail(batchId), this.loadTargetPage(batchId)])
      } finally {
        if (refreshSequence === this.refreshSequence) {
          this.pollInFlight = false
          this.schedulePoll()
        }
      }
    },
    async loadSelectedDetail (batchId = this.selectedBatchId) {
      if (!this.value || !batchId) return
      const requestSequence = ++this.detailRequestSequence
      this.detailLoading = true
      this.detailError = ''
      try {
        const response = await getOperationBatchDetail(batchId)
        if (!this.value || batchId !== this.selectedBatchId || requestSequence !== this.detailRequestSequence) return
        this.selectedBatch = response.data.data
      } catch (error) {
        if (this.value && batchId === this.selectedBatchId && requestSequence === this.detailRequestSequence) {
          this.detailError = this.errorMessage(error, '批次进度加载失败')
        }
      } finally {
        if (requestSequence === this.detailRequestSequence) this.detailLoading = false
      }
    },
    async loadTargetPage (batchId = this.selectedBatchId) {
      if (!this.value || !batchId) return
      const requestSequence = ++this.targetRequestSequence
      this.targetLoading = true
      this.targetError = ''
      try {
        const response = await fetchOperationTargetPage(this.compactParams({
          batchId,
          current: this.targetPage.currentPage,
          size: this.targetPage.pageSize,
          state: this.targetFilters.states.join(','),
          deviceId: this.targetFilters.deviceId,
          subjectType: this.targetFilters.subjectType
        }))
        if (!this.value || batchId !== this.selectedBatchId || requestSequence !== this.targetRequestSequence) return
        const page = response.data.data
        this.targets = Array.isArray(page.records) ? page.records : []
        this.targetPage.total = this.toCount(page.total)
      } catch (error) {
        if (this.value && batchId === this.selectedBatchId && requestSequence === this.targetRequestSequence) {
          this.targetError = this.errorMessage(error, '目标明细加载失败')
        }
      } finally {
        if (requestSequence === this.targetRequestSequence) this.targetLoading = false
      }
    },
    searchTargets () {
      this.targetPage.currentPage = 1
      return this.loadTargetPage()
    },
    changeTargetSize (size) {
      this.targetPage.currentPage = 1
      this.targetPage.pageSize = Math.min(100, Math.max(1, size))
      return this.loadTargetPage()
    },
    changeTargetPage (currentPage) {
      this.targetPage.currentPage = currentPage
      return this.loadTargetPage()
    },
    schedulePoll () {
      this.clearPollTimer()
      if (!this.value || !this.selectedBatchId || !this.isActiveBatch() || document.hidden) return
      this.pollTimer = setTimeout(() => this.refreshSelectedBatch(), 4000)
    },
    clearPollTimer () {
      if (this.pollTimer) clearTimeout(this.pollTimer)
      this.pollTimer = null
    },
    handleVisibilityChange () {
      if (document.hidden) this.clearPollTimer()
      else if (this.value && this.selectedBatchId && this.isActiveBatch()) this.schedulePoll()
    },
    isActiveBatch () {
      return ACTIVE_BATCH_STATUSES.indexOf(this.currentStatus) !== -1
    },
    statusLabel (status) {
      return (STATUS_META[status] && STATUS_META[status].label) || status || '未知状态'
    },
    statusClass (status) {
      const tone = (STATUS_META[status] && STATUS_META[status].tone) || 'info'
      return `auth-operation-progress__status is-${tone}`
    },
    actionLabel (action) {
      return { ADD: '新增 / 更新', DELETE: '撤权' }[action] || action || '—'
    },
    progressCount (field) {
      const progress = (this.selectedBatch && this.selectedBatch.progress) || {}
      return this.toCount(progress[field])
    },
    toCount (value) {
      const count = Number(value)
      return Number.isFinite(count) && count >= 0 ? count : 0
    },
    formatDate (value) {
      if (!value) return '—'
      const date = new Date(value)
      if (Number.isNaN(date.getTime())) return value
      return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
      }).format(date)
    },
    externalReference (target) {
      return target.latestExternalBatchId || target.latestExternalCommandId || '—'
    },
    errorMessage (error, fallback) {
      return (error && error.response && error.response.data && error.response.data.msg) ||
        (error && error.message) || fallback
    }
  }
}
</script>

<style lang="scss" scoped>
.auth-operation-progress {
  --progress-accent: #ec6c00;
  --progress-accent-soft: #fff1e3;
  --progress-text: #595757;
  --progress-muted: #6f6c6b;
  --progress-border: #e5e3e3;
  --progress-surface: #f7f7f7;
  --progress-success: #16a673;
  --progress-warning: #c98416;
  --progress-danger: #d83b36;
  color: var(--progress-text);
}
.auth-operation-progress__scope-note {
  margin: 0 0 14px;
  padding: 10px 14px;
  border: 1px solid rgba(236, 108, 0, 0.28);
  border-radius: 6px;
  background: var(--progress-accent-soft);
  color: #5a2600;
  line-height: 1.6;
}
.auth-operation-progress__toolbar,
.auth-operation-progress__target-toolbar,
.auth-operation-progress__detail-heading,
.auth-operation-progress__status-line {
  display: flex;
  align-items: center;
  gap: 10px;
}
.auth-operation-progress__toolbar,
.auth-operation-progress__target-toolbar {
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.auth-operation-progress__operation-key { width: 280px; }
.auth-operation-progress__pagination { margin-top: 14px; text-align: right; }
.auth-operation-progress__error {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin: 10px 0;
  padding: 10px 14px;
  border: 1px solid rgba(216, 59, 54, 0.25);
  border-radius: 6px;
  background: rgba(216, 59, 54, 0.08);
  color: var(--progress-danger);
}
.auth-operation-progress__detail {
  margin-top: 24px;
  padding: 18px;
  border: 1px solid var(--progress-border);
  border-radius: 10px;
  background: #fff;
}
.auth-operation-progress__detail-heading { justify-content: space-between; margin-bottom: 14px; }
.auth-operation-progress__eyebrow { margin-right: 10px; color: var(--progress-muted); font-size: 12px; }
.auth-operation-progress__selected-id,
.auth-operation-progress__long-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-variant-numeric: tabular-nums;
  user-select: all;
}
.auth-operation-progress__long-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  vertical-align: bottom;
  white-space: nowrap;
}
.auth-operation-progress__summary {
  min-height: 120px;
  margin-bottom: 18px;
  padding: 14px;
  border-radius: 10px;
  background: var(--progress-surface);
}
.auth-operation-progress__status-line { flex-wrap: wrap; margin-bottom: 10px; }
.auth-operation-progress__batch-reason {
  margin: 0 0 10px;
  padding: 8px 10px;
  border-radius: 6px;
  background: rgba(201, 132, 22, 0.12);
  color: #7d500d;
  line-height: 1.5;
  white-space: pre-wrap;
}
.auth-operation-progress__status {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  border-radius: 6px;
  background: rgba(35, 118, 217, 0.1);
  color: #2376d9;
  font-size: 12px;
  font-weight: 600;
}
.auth-operation-progress__status.is-warning { background: rgba(201, 132, 22, 0.12); color: var(--progress-warning); }
.auth-operation-progress__status.is-success { background: rgba(22, 166, 115, 0.1); color: var(--progress-success); }
.auth-operation-progress__status.is-danger { background: rgba(216, 59, 54, 0.1); color: var(--progress-danger); }
.auth-operation-progress__percent {
  color: var(--progress-text);
  font-size: 20px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}
.auth-operation-progress__refresh-hint { color: var(--progress-muted); font-size: 12px; }
.auth-operation-progress__metrics {
  display: grid;
  grid-template-columns: repeat(5, minmax(110px, 1fr));
  gap: 10px;
  margin-top: 14px;
}
.auth-operation-progress__metrics > div {
  padding: 10px;
  border: 1px solid #efeeee;
  border-radius: 6px;
  background: #fff;
}
.auth-operation-progress__metrics span,
.auth-operation-progress__metrics strong { display: block; }
.auth-operation-progress__metrics span { color: var(--progress-muted); font-size: 12px; }
.auth-operation-progress__metrics strong { margin-top: 4px; font-size: 18px; font-variant-numeric: tabular-nums; }
.auth-operation-progress ::v-deep .el-input__inner:focus,
.auth-operation-progress ::v-deep .el-button:focus-visible {
  border-color: var(--progress-accent);
  box-shadow: 0 0 0 3px rgba(236, 108, 0, 0.22);
  outline: none;
}
.auth-operation-progress ::v-deep .el-progress-bar__inner {
  background-color: var(--progress-accent);
}
@media (max-width: 1180px) {
  .auth-operation-progress__metrics { grid-template-columns: repeat(3, minmax(110px, 1fr)); }
}
</style>
