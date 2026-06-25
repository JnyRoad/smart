<!-- 基础信息：ISC卡片快速维护 -->
<template>
  <div class="my-basic-container isc-card-fast-add">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <page-toolbar
          :selected-park="selectedPark"
          :staff-loading="staffLoading"
          :submitting="submitting"
          :can-submit="canSubmit"
          :submit-count-text="submitCountText"
          @search-staff="searchStaff"
          @reset="resetPage"
          @open-paste="openPasteDialog"
          @submit="submitQueue"
          @open-tasks="goTaskPage"
        />

        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="园区" prop="parkId">
            <el-select
              v-model="searchForm.parkId"
              placeholder="请选择园区"
              filterable
              clearable
              @change="onParkChange">
              <el-option
                v-for="item in iscParkOptions"
                :key="item.parkId"
                :label="parkOptionLabel(item)"
                :value="item.parkId">
                <span>{{ parkOptionLabel(item) }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="工号 / 姓名" prop="staffKeyword">
            <el-input
              v-model.trim="searchForm.staffKeyword"
              class="staff-keyword-input"
              placeholder="输入工号或姓名后回车"
              clearable
              :disabled="!selectedPark"
              @keyup.enter.native="searchStaff">
              <el-button slot="append" :loading="staffLoading" :disabled="!selectedPark" @click="searchStaff">搜索</el-button>
            </el-input>
          </el-form-item>
          <el-form-item label="录入模式">
            <el-radio-group v-model="inputMode" size="mini">
              <el-radio-button label="continue">保存后继续</el-radio-button>
              <el-radio-button label="queue">仅加入队列</el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-form>

        <div class="sync-tip" :class="{ danger: !selectedPark || (selectedPark && !isParkSyncEnabled(selectedPark)) }">
          <i :class="!selectedPark || (selectedPark && !isParkSyncEnabled(selectedPark)) ? 'el-icon-warning' : 'el-icon-success'"></i>
          <span v-if="selectedPark && isParkSyncEnabled(selectedPark)">当前园区已启用ISC卡片同步：{{ selectedParkLabel }}</span>
          <span v-else-if="selectedPark">当前园区未启用ISC卡片同步，不能维护实体卡</span>
          <span v-else>请先选择园区，选择后才能搜索员工、录入卡号和批量粘贴。</span>
        </div>

        <div class="scan-box" :class="{ invalid: cardInputError }">
          <div class="scan-icon">
            <i class="el-icon-tickets"></i>
          </div>
          <div class="scan-main">
            <div class="scan-label">读卡器卡号输入 · 仅支持8-20位数字或大写字母，999开头虚拟卡禁止维护</div>
            <el-input
              ref="cardInput"
              v-model.trim="cardNo"
              class="card-input"
              placeholder="请刷卡或手动输入卡号，回车加入队列"
              maxlength="20"
              :disabled="!canInputCard"
              @keyup.enter.native="addCurrentCard">
            </el-input>
          </div>
          <div class="scan-action">
            <el-button type="primary" size="mini" :disabled="!canQueueCard" @click="addCurrentCard">加入队列</el-button>
          </div>
        </div>
        <div class="scan-message" :class="{ error: cardInputError, ok: cardInputOk }">{{ cardInputMessage }}</div>

        <div class="work-grid">
          <staff-panel
            :selected-staff="selectedStaff"
            :staff-candidates="staffCandidates"
            :staff-cards="staffCards"
            :staff-card-loading="staffCardLoading"
            :staff-card-deleting="staffCardDeleting"
            :staff-avatar-text="staffAvatarText"
            :staff-status-text="staffStatusText"
            :card-sync-status-text="cardSyncStatusText"
            :card-sync-status-type="cardSyncStatusType"
            @select-staff="selectStaff"
            @open-detail="goStaffDetail"
            @remove-card="removeStaffCard"
          />

          <queue-table
            :rows="queue"
            :ready-count="readyQueue.length"
            :invalid-count="invalidQueue.length"
            :submitting="submitting"
            :can-submit="canSubmit"
            :format-status-text="queueStatusText"
            :format-status-type="queueStatusType"
            :row-class-name="queueRowClass"
            @remove-row="removeQueueRow"
            @remove-finished="removeFinishedRows"
            @clear="clearQueue"
            @submit="submitQueue"
          />
        </div>

        <task-table
          :loading="taskLoading"
          :rows="taskTableData"
          :format-action="taskActionText"
          @refresh="loadTaskList"
        />
      </section>
    </el-scrollbar>

    <paste-dialog
      :visible.sync="pasteDialogVisible"
      :resolving="pasteResolving"
      :text="pasteText"
      :rows="pasteRows"
      :errors="pasteErrors"
      :visible-errors="pasteVisibleErrors"
      :placeholder="pastePlaceholder"
      @update:text="pasteText = $event"
      @confirm="confirmPaste"
    />
  </div>
</template>

<script>
import { staffStatusInit } from '@/filters/index'
import PageToolbar from './PageToolbar.vue'
import PasteDialog from './PasteDialog.vue'
import QueueTable from './QueueTable.vue'
import StaffPanel from './StaffPanel.vue'
import TaskTable from './TaskTable.vue'
import {
  deleteStaffCard,
  fetchIscParkRecords,
  fetchRecentCardTaskRecords,
  fetchStaffCardRecords,
  fetchStaffMapByBadges,
  saveStaffCard,
  searchStaffByBadge as requestStaffByBadge,
  searchStaffByName as requestStaffByName
} from './api'
import {
  emptySearchForm,
  trimValue,
  isStaffBadgeKeyword,
  isParkSyncEnabled as checkParkSyncEnabled,
  parkOptionLabel as formatParkOptionLabel,
  validateCardNo as validateIscCardNo,
  cardSyncStatusText as formatCardSyncStatusText,
  cardSyncStatusType as formatCardSyncStatusType,
  queueStatusText as formatQueueStatusText,
  queueStatusType as formatQueueStatusType,
  taskActionText as formatTaskActionText,
  errorMessage as formatErrorMessage,
  responseMessage as formatResponseMessage
} from './flow-rules'
import {
  buildRecentTaskQuery,
  canApplyStaffCardResult,
  emptyStaffCardState,
  runBadgeStaffSearch,
  runNameStaffSearch
} from './staff-flow'
import {
  validateQueueCandidate as validateQueueCandidateRule,
  buildQueueRow as createQueueRow,
  buildInvalidQueueRow as createInvalidQueueRow,
  parsePasteText as parsePastedQueueRows,
  queueSavingPatch,
  queueSuccessPatch,
  queueFailedPatch
} from './queue-flow'

export default {
  name: 'iscCardFastAdd',
  components: {
    PageToolbar,
    PasteDialog,
    QueueTable,
    StaffPanel,
    TaskTable
  },
  data() {
    return {
      searchForm: emptySearchForm(),
      inputMode: 'continue',
      iscParkOptions: [],
      selectedStaff: null,
      staffCandidates: [],
      staffCards: [],
      staffCardDeleting: null,
      cardNo: '',
      queue: [],
      taskTableData: [],
      staffLoading: false,
      staffCardLoading: false,
      taskLoading: false,
      submitting: false,
      pasteDialogVisible: false,
      pasteResolving: false,
      pasteText: '',
      nextQueueId: 1
    }
  },
  computed: {
    selectedPark() {
      return this.iscParkOptions.find(item => String(item.parkId) === String(this.searchForm.parkId)) || null
    },
    selectedParkLabel() {
      return this.selectedPark ? this.parkOptionLabel(this.selectedPark) : ''
    },
    canInputCard() {
      return !!(this.selectedPark && this.selectedStaff && this.isParkSyncEnabled(this.selectedPark))
    },
    canQueueCard() {
      return this.canInputCard && !!trimValue(this.cardNo)
    },
    cardInputValidation() {
      const cardNo = trimValue(this.cardNo)
      if (!cardNo) {
        return { valid: false, message: '提示：刷卡完成后按 Enter 加入队列。' }
      }
      return this.validateCardNo(cardNo)
    },
    cardInputError() {
      return !!trimValue(this.cardNo) && !this.cardInputValidation.valid
    },
    cardInputOk() {
      return !!trimValue(this.cardNo) && this.cardInputValidation.valid
    },
    cardInputMessage() {
      if (!this.canInputCard) {
        return '请先选择已启用同步的园区，并定位员工。'
      }
      return this.cardInputValidation.message || '卡号格式合法，按 Enter 加入队列。'
    },
    readyQueue() {
      return this.queue.filter(item => item.status === 'ready')
    },
    invalidQueue() {
      return this.queue.filter(item => item.status === 'invalid' || item.status === 'failed')
    },
    canSubmit() {
      return this.readyQueue.length > 0 && !this.submitting
    },
    submitCountText() {
      return this.readyQueue.length ? `(${this.readyQueue.length})` : ''
    },
    staffAvatarText() {
      return this.selectedStaff && this.selectedStaff.name ? this.selectedStaff.name.substring(0, 1) : '员'
    },
    pastePlaceholder() {
      return '10288 1024388812\n10290 1024388845'
    },
    pasteRows() {
      return this.parsePasteText(this.pasteText)
    },
    pasteErrors() {
      return this.pasteRows.filter(item => item.message)
    },
    pasteVisibleErrors() {
      return this.pasteErrors.slice(0, 5)
    }
  },
  created() {
    this.loadIscParkOptions()
    this.loadTaskList()
  },
  methods: {
    loadIscParkOptions() {
      fetchIscParkRecords().then(records => {
        this.iscParkOptions = records.filter(item => this.isParkSyncEnabled(item))
      })
    },
    loadTaskList() {
      this.taskLoading = true
      fetchRecentCardTaskRecords(buildRecentTaskQuery({
        searchForm: this.searchForm,
        selectedStaff: this.selectedStaff
      })).then(records => {
        this.taskTableData = records
      }).finally(() => {
        this.taskLoading = false
      })
    },
    parkOptionLabel(item) {
      return formatParkOptionLabel(item)
    },
    isParkSyncEnabled(item) {
      return checkParkSyncEnabled(item)
    },
    onParkChange() {
      this.cardNo = ''
      this.selectedStaff = null
      this.staffCandidates = []
      this.staffCards = []
      this.staffCardLoading = false
      this.loadTaskList()
      this.$nextTick(() => this.focusCardInput())
    },
    searchStaff() {
      if (!this.selectedPark) {
        this.$message({
          message: '请先选择园区',
          type: 'warning'
        })
        return
      }
      const keyword = trimValue(this.searchForm.staffKeyword)
      if (!keyword) {
        this.$message({
          message: '请输入工号或姓名',
          type: 'warning'
        })
        return
      }
      this.staffLoading = true
      if (isStaffBadgeKeyword(keyword)) {
        return this.searchExactStaffByBadge(keyword, true)
      }
      return this.searchStaffByName(keyword)
    },
    searchStaffByName(keyword) {
      return runNameStaffSearch({
        keyword,
        selectedPark: this.selectedPark,
        requestStaffByName
      }).then(result => {
        this.applyStaffSearchResult(result)
      }).finally(() => {
        this.staffLoading = false
      })
    },
    searchExactStaffByBadge(badge, fallbackToName) {
      return runBadgeStaffSearch({
        badge,
        selectedPark: this.selectedPark,
        fallbackToName,
        readFallbackPark: () => this.selectedPark,
        requestStaffByBadge,
        requestStaffByName
      }).then(result => {
        this.applyStaffSearchResult(result)
      }).finally(() => {
        this.staffLoading = false
      })
    },
    applyStaffSearchResult(result) {
      this.staffCandidates = result.staffCandidates
      if (result.staffToSelect) {
        this.selectStaff(result.staffToSelect)
        return
      }
      this.selectedStaff = result.selectedStaff
      this.staffCards = result.staffCards
      if (result.message) {
        this.$message(result.message)
      }
    },
    selectStaff(row) {
      this.selectedStaff = row
      this.searchForm.staffKeyword = row.badge || row.name || ''
      this.loadStaffCards(row.id)
      this.loadTaskList()
      this.$nextTick(() => this.focusCardInput())
    },
    loadStaffCards(staffId) {
      if (!staffId) {
        Object.assign(this, emptyStaffCardState())
        return
      }
      const requestStaffId = staffId
      this.staffCardLoading = true
      fetchStaffCardRecords(requestStaffId).then(cards => {
        if (!canApplyStaffCardResult(this.selectedStaff, requestStaffId)) {
          return
        }
        this.staffCards = cards
      }).finally(() => {
        if (this.selectedStaff && !canApplyStaffCardResult(this.selectedStaff, requestStaffId)) {
          return
        }
        this.staffCardLoading = false
      })
    },
    removeStaffCard(row) {
      if (!row || !row.id) {
        this.$message({
          message: '未找到要删除的ISC卡片',
          type: 'warning'
        })
        return
      }
      this.$confirm(`确认删除ISC卡片 ${row.cardNo}？`, '提示', {
        type: 'warning'
      }).then(async () => {
        this.staffCardDeleting = row.id
        try {
          const response = await deleteStaffCard(row.id)
          if (response.data.data) {
            this.$notify({
              title: '成功',
              message: '删除ISC卡片成功',
              type: 'success'
            })
            if (this.selectedStaff) {
              this.loadStaffCards(this.selectedStaff.id)
            }
            this.loadTaskList()
          } else {
            this.$message({
              message: '删除ISC卡片失败',
              type: 'error'
            })
          }
        } finally {
          this.staffCardDeleting = null
        }
      }).catch(action => {
        if (action !== 'cancel' && action !== 'close') {
          this.$message({
            message: '删除ISC卡片失败',
            type: 'error'
          })
        }
      })
    },
    cardSyncStatusText(row) {
      return formatCardSyncStatusText(row)
    },
    cardSyncStatusType(syncStatus) {
      return formatCardSyncStatusType(syncStatus)
    },
    validateCardNo(cardNo) {
      return validateIscCardNo(cardNo)
    },
    validateQueueCandidate(staff, cardNo, park) {
      return validateQueueCandidateRule(staff, cardNo, park, this.queue)
    },
    addCurrentCard() {
      if (!this.selectedPark) {
        this.$message({
          message: '请先选择园区',
          type: 'warning'
        })
        return
      }
      if (!this.selectedStaff) {
        this.$message({
          message: '请先定位员工',
          type: 'warning'
        })
        return
      }
      const cardNo = trimValue(this.cardNo)
      if (!cardNo) {
        this.$message({
          message: '请先刷卡或输入卡号',
          type: 'warning'
        })
        return
      }
      this.queue.unshift(this.buildQueueRow(this.selectedStaff, cardNo, this.selectedPark))
      this.cardNo = ''
      this.$nextTick(() => this.focusCardInput())
      if (this.inputMode === 'continue') {
        this.$message({
          message: '已加入队列，请继续搜索下一位员工或刷下一张卡',
          type: 'success'
        })
      }
    },
    buildQueueRow(staff, cardNo, park) {
      return createQueueRow({
        queueId: this.nextQueueId++,
        staff,
        cardNo,
        park,
        queue: this.queue
      })
    },
    buildInvalidQueueRow(badge, cardNo, message, park = this.selectedPark) {
      return createInvalidQueueRow({
        queueId: this.nextQueueId++,
        badge,
        cardNo,
        message,
        park
      })
    },
    removeQueueRow(index) {
      if (this.submitting) {
        return
      }
      this.queue.splice(index, 1)
    },
    clearQueue() {
      if (this.submitting) {
        return
      }
      this.queue = []
    },
    removeFinishedRows() {
      if (this.submitting) {
        return
      }
      this.queue = this.queue.filter(item => item.status !== 'success')
    },
    async submitQueue() {
      if (!this.readyQueue.length) {
        return
      }
      const rowsToSubmit = this.readyQueue.slice()
      this.submitting = true
      let successCount = 0
      let failedCount = 0
      for (const row of rowsToSubmit) {
        Object.assign(row, queueSavingPatch())
        try {
          const response = await saveStaffCard({
            staffId: row.staffId,
            parkId: row.parkId,
            cardNo: row.cardNo
          })
          if (response.data.data) {
            Object.assign(row, queueSuccessPatch())
            successCount += 1
          } else {
            Object.assign(row, queueFailedPatch(this.responseMessage(response, '保存失败')))
            failedCount += 1
          }
        } catch (error) {
          Object.assign(row, queueFailedPatch(this.errorMessage(error)))
          failedCount += 1
        }
      }
      this.submitting = false
      if (this.selectedStaff && successCount > 0) {
        this.loadStaffCards(this.selectedStaff.id)
      }
      this.loadTaskList()
      this.$notify({
        title: successCount > 0 ? '提交完成' : '提交失败',
        message: `成功${successCount}条，失败${failedCount}条`,
        type: successCount > 0 ? 'success' : 'warning'
      })
    },
    openPasteDialog() {
      if (!this.selectedPark) {
        this.$message({
          message: '请先选择园区',
          type: 'warning'
        })
        return
      }
      this.pasteDialogVisible = true
    },
    parsePasteText(text) {
      return parsePastedQueueRows(text)
    },
    async confirmPaste() {
      if (!this.selectedPark) {
        this.$message({
          message: '请先选择园区',
          type: 'warning'
        })
        return
      }
      if (this.pasteRows.length > 200) {
        this.$message({
          message: '批量粘贴最多支持200行',
          type: 'warning'
        })
        return
      }
      if (this.pasteErrors.length) {
        this.$message({
          message: '请先处理批量粘贴中的格式问题',
          type: 'warning'
        })
        return
      }
      const pastePark = this.selectedPark
      const rows = this.pasteRows.slice()
      this.pasteResolving = true
      try {
        const staffMap = await this.fetchStaffMap(rows.map(item => item.badge).filter(Boolean))
        rows.forEach(item => {
          if (item.message) {
            this.queue.push(this.buildInvalidQueueRow(item.badge, item.cardNo, item.message, pastePark))
            return
          }
          const staff = staffMap[String(item.badge)]
          if (!staff) {
            this.queue.push(this.buildInvalidQueueRow(item.badge, item.cardNo, '未找到该工号对应员工', pastePark))
            return
          }
          this.queue.push(this.buildQueueRow(staff, item.cardNo, pastePark))
        })
        this.pasteDialogVisible = false
        this.pasteText = ''
        this.$message({
          message: '批量数据已加入待提交队列',
          type: 'success'
        })
      } catch (error) {
        this.$message({
          message: this.errorMessage(error),
          type: 'error'
        })
      } finally {
        this.pasteResolving = false
      }
    },
    async fetchStaffMap(badges) {
      return fetchStaffMapByBadges(badges)
    },
    resetPage() {
      if (this.$refs.searchForm) {
        this.$refs.searchForm.resetFields()
      }
      this.searchForm = emptySearchForm()
      this.selectedStaff = null
      this.staffCandidates = []
      this.staffCards = []
      this.cardNo = ''
      this.loadTaskList()
    },
    focusCardInput() {
      if (this.$refs.cardInput && this.canInputCard) {
        this.$refs.cardInput.focus()
      }
    },
    goStaffDetail(row) {
      if (!row || !row.id) {
        return
      }
      this.$router.push({
        path: `/platform/basic/staff_info/detail/${row.id}`
      })
    },
    goTaskPage() {
      this.$router.push({
        path: '/platform/records/isc_card_task'
      })
    },
    staffStatusText(status) {
      return staffStatusInit(status) || '-'
    },
    queueStatusText(status) {
      return formatQueueStatusText(status)
    },
    queueStatusType(status) {
      return formatQueueStatusType(status)
    },
    queueRowClass({ row }) {
      if (row.status === 'invalid' || row.status === 'failed') {
        return 'queue-row-error'
      }
      if (row.status === 'success') {
        return 'queue-row-success'
      }
      return ''
    },
    taskActionText(row) {
      return formatTaskActionText(row)
    },
    errorMessage(error) {
      if (error && error.response) {
        return this.responseMessage(error.response, '保存失败')
      }
      return formatErrorMessage(error)
    },
    responseMessage(response, fallback) {
      return formatResponseMessage(response, fallback)
    }
  }
}
</script>

<style lang="scss" scoped>
.isc-card-fast-add {
  min-width: 1180px;

  .topForm {
    padding: 14px 20px 0;

    ::v-deep .el-select,
    ::v-deep .el-input {
      width: 220px;
    }

    ::v-deep .staff-keyword-input {
      width: 320px;

      .el-input-group__append {
        padding: 0;
        width: 84px;
        box-sizing: border-box;
        border-color: #dcdfe6;
        background: #f5f7fa;
        overflow: hidden;
      }

      .el-button {
        width: 84px;
        min-width: 84px;
        height: 26px;
        margin: 0;
        padding: 0;
        border: 0;
        border-radius: 0;
        box-sizing: border-box;
        color: #606266;
        font-size: 12px;
        font-weight: 500;
        line-height: 26px;
        transition: color 0.15s ease, background-color 0.15s ease;

        &:hover,
        &:focus {
          background: #fff7ef;
          color: #ed6d00;
        }
      }
    }
  }

  .sync-tip {
    margin: 8px 20px 0;
    padding: 8px 12px;
    border: 1px solid #b7eddb;
    border-radius: 4px;
    background: #e6faf3;
    color: #0a9b6c;
    font-size: 12px;

    i {
      margin-right: 6px;
    }

    &.danger {
      border-color: #f6c4c6;
      background: #fef0f0;
      color: #c11d22;
    }
  }

  .scan-box {
    display: flex;
    align-items: center;
    margin: 14px 20px 0;
    border: 2px solid #ed6d00;
    border-radius: 4px;
    background: #fffaf3;

    &.invalid {
      border-color: #e7292e;
      background: #fff6f6;

      .scan-icon {
        color: #e7292e;
      }
    }
  }

  .scan-icon {
    width: 54px;
    color: #ed6d00;
    text-align: center;
    font-size: 26px;
  }

  .scan-main {
    flex: 1;
    padding: 9px 12px;
  }

  .scan-label {
    margin-bottom: 4px;
    color: #666;
    font-size: 12px;
  }

  .card-input {
    ::v-deep .el-input__inner {
      border: 0;
      background: transparent;
      color: #333;
      font-family: Consolas, Menlo, monospace;
      font-size: 22px;
      font-weight: 600;
      letter-spacing: 2px;
      padding-left: 0;
    }
  }

  .scan-action {
    padding-right: 12px;
  }

  .scan-message {
    min-height: 22px;
    margin: 6px 20px 0;
    color: #999;
    font-size: 12px;

    &.error {
      color: #e7292e;
    }

    &.ok {
      color: #10cc8e;
    }
  }

  .work-grid {
    display: grid;
    grid-template-columns: 360px 1fr;
    gap: 14px;
    padding: 12px 20px;
  }

}
</style>

<style lang="scss">
.isc-paste-dialog {
  border-radius: 6px;
  overflow: hidden;

  .el-dialog__header {
    padding: 24px 32px 10px;
  }

  .el-dialog__title {
    color: #333;
    font-size: 20px;
    font-weight: 600;
  }

  .el-dialog__body {
    padding: 16px 32px 18px;
  }

  .el-dialog__footer {
    padding: 16px 32px 24px;
    border-top: 1px solid #eef0f4;
    background: #fafafa;
  }

  .paste-dialog-body {
    padding: 0;
  }

  .paste-guide {
    display: flex;
    justify-content: space-between;
    gap: 14px;
    margin-bottom: 14px;
    padding: 14px 16px;
    border: 1px solid #f0d4bd;
    border-radius: 6px;
    background: #fffaf3;
  }

  .paste-guide-main {
    flex: 1;
    min-width: 0;
  }

  .paste-guide-title {
    display: flex;
    align-items: center;
    margin-bottom: 6px;
    color: #333;
    font-size: 15px;
    font-weight: 600;

    i {
      margin-right: 8px;
      color: #ed6d00;
      font-size: 18px;
    }
  }

  .paste-desc {
    color: #666;
    font-size: 12px;
    line-height: 20px;
  }

  .paste-example {
    flex: 0 0 190px;
    padding: 10px 12px;
    border: 1px solid #e8e9ed;
    border-radius: 4px;
    background: #fff;
    color: #333;
    font-family: Menlo, Consolas, monospace;
    font-size: 12px;
    line-height: 20px;
  }

  .example-title {
    margin-bottom: 4px;
    color: #999;
    font-family: inherit;
  }

  .paste-input textarea {
    color: #333;
    font-size: 14px;
    line-height: 22px;
  }

  .paste-summary {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 12px;
    padding: 10px 12px;
    border: 1px solid #e8e9ed;
    border-radius: 4px;
    background: #fafafa;
    color: #666;
    font-size: 12px;

    &.ready {
      border-color: #b7eddb;
      background: #f4fbf8;
    }

    &.invalid {
      border-color: #f6c4c6;
      background: #fff8f8;
    }
  }

  .paste-stat {
    display: flex;
    align-items: baseline;
    gap: 4px;

    strong {
      color: #333;
      font-size: 18px;
      line-height: 20px;
    }
  }

  .paste-status {
    display: inline-flex;
    align-items: center;
    color: #666;
    font-weight: 600;

    i {
      margin-right: 6px;
      font-size: 14px;
    }

    &.ok {
      color: #0a9b6c;
    }

    &.error {
      color: #c11d22;
    }
  }

  .paste-errors {
    margin-top: 10px;
    padding: 10px 12px;
    border: 1px solid #f6c4c6;
    border-radius: 4px;
    background: #fef0f0;
    color: #c11d22;
    font-size: 12px;
    line-height: 22px;
  }

  .paste-errors-title {
    margin-bottom: 4px;
    font-weight: 600;
  }

  .paste-errors-more {
    margin-top: 4px;
    color: #8f1d22;
  }

  .paste-footer {
    display: inline-flex;
    gap: 8px;
  }
}
</style>
