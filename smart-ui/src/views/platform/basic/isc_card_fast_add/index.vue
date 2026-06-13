<!-- 基础信息：ISC卡片快速维护 -->
<template>
  <div class="my-basic-container isc-card-fast-add">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu clear">
          <div class="page-head">
            <div class="page-title">
              <span class="title-bar"></span>
              <span>ISC卡片快速维护</span>
            </div>
            <div class="page-desc">先选择园区和员工，再刷卡或批量粘贴卡号；确认无误后提交，系统会自动同步到ISC。</div>
          </div>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" :disabled="!selectedPark || staffLoading" @click="searchStaff">搜索员工</el-button>
            <el-button type="primary" icon="el-icon-delete" plain @click="resetPage">清空</el-button>
            <el-button type="primary" icon="el-icon-document" plain :disabled="!selectedPark" @click="openPasteDialog">批量粘贴</el-button>
            <el-button type="primary" icon="el-icon-check" :loading="submitting" :disabled="!canSubmit" @click="submitQueue">批量提交{{ submitCountText }}</el-button>
            <el-button icon="el-icon-refresh" @click="goTaskPage">查看同步任务</el-button>
          </div>
        </div>

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
          <div class="work-panel">
            <div class="panel-title">
              <span>当前录入</span>
              <el-tag v-if="selectedStaff" size="mini" :type="selectedStaff.status === 0 ? 'danger' : 'success'">{{ staffStatusText(selectedStaff.status) }}</el-tag>
            </div>

            <div v-if="selectedStaff" class="staff-card">
              <div class="staff-main">
                <div class="staff-avatar">{{ staffAvatarText }}</div>
                <div>
                  <div class="staff-name">{{ selectedStaff.name || '-' }} <span>{{ selectedStaff.badge || '-' }}</span></div>
                  <div class="staff-meta">{{ selectedStaff.compName || '-' }} / {{ selectedStaff.depName || '-' }}</div>
                </div>
              </div>
              <el-row class="staff-kv" :gutter="8">
                <el-col :span="8">园区</el-col>
                <el-col :span="16">{{ selectedStaff.parkName || '-' }}</el-col>
                <el-col :span="8">岗位</el-col>
                <el-col :span="16">{{ selectedStaff.jobName || '-' }}</el-col>
                <el-col :span="8">入职日期</el-col>
                <el-col :span="16">{{ selectedStaff.createTime || '-' }}</el-col>
              </el-row>
              <div class="staff-actions">
                <el-button type="text" size="mini" @click="goStaffDetail(selectedStaff)">查看人员详情</el-button>
              </div>
            </div>
            <div v-else class="empty-state">先输入工号或姓名定位员工</div>

            <div v-if="staffCandidates.length" class="candidate-list">
              <div class="candidate-title">匹配人员</div>
              <el-table :data="staffCandidates" size="mini" border max-height="180">
                <el-table-column prop="badge" label="工号" width="90"></el-table-column>
                <el-table-column prop="name" label="姓名" width="90"></el-table-column>
                <el-table-column prop="depName" label="部门" min-width="120" show-overflow-tooltip></el-table-column>
                <el-table-column label="操作" width="70" fixed="right">
                  <template slot-scope="scope">
                    <el-button type="text" size="mini" @click="selectStaff(scope.row)">选择</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div class="staff-card-list">
              <div class="candidate-title">已有ISC卡片</div>
              <el-table v-loading="staffCardLoading" :data="staffCards" size="mini" border empty-text="暂无ISC实体卡">
                <el-table-column prop="cardNo" label="卡号" min-width="120"></el-table-column>
                <el-table-column prop="dispatcherParkName" label="ISC平台" min-width="120"></el-table-column>
                <el-table-column label="同步状态" width="92">
                  <template slot-scope="scope">
                    <el-tag size="mini" :type="cardSyncStatusType(scope.row.syncStatus)" :title="scope.row.lastSyncRemark || ''">
                      {{ cardSyncStatusText(scope.row) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" fixed="right">
                  <template slot-scope="scope">
                    <el-button
                      type="text"
                      size="mini"
                      class="danger-text-button"
                      :loading="staffCardDeleting === scope.row.id"
                      :disabled="!!staffCardDeleting && staffCardDeleting !== scope.row.id"
                      @click="removeStaffCard(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>

          <div class="work-panel queue-panel">
            <div class="panel-title">
              <span>待提交队列</span>
              <span class="panel-count">共{{ queue.length }}条，可提交{{ readyQueue.length }}条，异常{{ invalidQueue.length }}条</span>
            </div>
            <el-table
              :data="queue"
              size="mini"
              border
              max-height="360"
              empty-text="暂无待提交卡片"
              :row-class-name="queueRowClass">
              <el-table-column label="状态" width="92">
                <template slot-scope="scope">
                  <el-tag size="mini" :type="queueStatusType(scope.row.status)">{{ queueStatusText(scope.row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="badge" label="工号" width="100"></el-table-column>
              <el-table-column prop="name" label="姓名" width="90"></el-table-column>
              <el-table-column prop="parkName" label="园区" width="110" show-overflow-tooltip></el-table-column>
              <el-table-column prop="dispatcherParkName" label="ISC平台" width="120" show-overflow-tooltip></el-table-column>
              <el-table-column label="卡号 / 结果" min-width="210">
                <template slot-scope="scope">
                  <span class="card-no">{{ scope.row.cardNo || '-' }}</span>
                  <div v-if="scope.row.message" class="row-message">{{ scope.row.message }}</div>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="92" fixed="right">
                <template slot-scope="scope">
                  <el-button type="text" size="mini" :disabled="submitting || scope.row.status === 'saving'" @click="removeQueueRow(scope.$index)">移除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="queue-footer">
              <div class="queue-note">保存成功后会自动创建ISC新增卡片同步任务；失败行会保留在队列中。</div>
              <div>
                <el-button size="mini" plain :disabled="submitting" @click="removeFinishedRows">清除成功行</el-button>
                <el-button size="mini" plain :disabled="submitting" @click="clearQueue">清空队列</el-button>
                <el-button type="primary" size="mini" :loading="submitting" :disabled="!canSubmit" @click="submitQueue">提交队列</el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="task-block">
          <div class="panel-title">
            <span>最近录卡结果 / 同步任务追踪</span>
            <div>
              <el-button size="mini" icon="el-icon-refresh" @click="loadTaskList">刷新</el-button>
            </div>
          </div>
          <el-table v-loading="taskLoading" :data="taskTableData" size="mini" border empty-text="暂无同步任务">
            <el-table-column prop="parkName" label="园区" width="110"></el-table-column>
            <el-table-column prop="badge" label="工号" width="100"></el-table-column>
            <el-table-column prop="name" label="姓名" width="90"></el-table-column>
            <el-table-column prop="cardNo" label="卡 ID" width="140"></el-table-column>
            <el-table-column label="任务动作" width="100">
              <template slot-scope="scope">{{ taskActionText(scope.row) }}</template>
            </el-table-column>
            <el-table-column prop="remark" label="执行结果" min-width="220" show-overflow-tooltip></el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="165"></el-table-column>
            <el-table-column prop="optUser" label="操作人" width="100"></el-table-column>
          </el-table>
        </div>
      </section>
    </el-scrollbar>

    <el-dialog
      title="批量粘贴录卡"
      :visible.sync="pasteDialogVisible"
      width="680px"
      custom-class="isc-paste-dialog"
      :close-on-click-modal="!pasteResolving"
      :close-on-press-escape="!pasteResolving"
      :show-close="!pasteResolving"
      append-to-body>
      <div class="paste-dialog-body">
        <div class="paste-guide">
          <div class="paste-guide-main">
            <div class="paste-guide-title">
              <i class="el-icon-document-copy"></i>
              <span>按行粘贴工号和卡号</span>
            </div>
            <div class="paste-desc">每行一条，工号在前、卡号在后；支持空格、Tab、逗号分隔，最多200行。</div>
          </div>
          <div class="paste-example">
            <div class="example-title">示例</div>
            <div>10288 1024388812</div>
            <div>10290 1024388845</div>
          </div>
        </div>
        <el-input
          v-model="pasteText"
          class="paste-input"
          type="textarea"
          :rows="9"
          resize="vertical"
          :disabled="pasteResolving"
          :placeholder="pastePlaceholder">
        </el-input>
        <div class="paste-summary" :class="{ ready: pasteRows.length && !pasteErrors.length && pasteRows.length <= 200, invalid: pasteErrors.length || pasteRows.length > 200 }">
          <div class="paste-stat">
            <span>已识别</span>
            <strong>{{ pasteRows.length }}</strong>
            <span>/ 200行</span>
          </div>
          <div v-if="!pasteRows.length" class="paste-status">
            <i class="el-icon-edit-outline"></i>
            <span>等待粘贴数据</span>
          </div>
          <div v-else-if="pasteRows.length > 200" class="paste-status error">
            <i class="el-icon-warning-outline"></i>
            <span>超出{{ pasteRows.length - 200 }}行</span>
          </div>
          <div v-else-if="pasteErrors.length" class="paste-status error">
            <i class="el-icon-warning-outline"></i>
            <span>发现{{ pasteErrors.length }}条问题</span>
          </div>
          <div v-else class="paste-status ok">
            <i class="el-icon-success"></i>
            <span>格式校验通过</span>
          </div>
        </div>
        <div v-if="pasteErrors.length || pasteRows.length > 200" class="paste-errors">
          <div class="paste-errors-title">请先处理以下问题</div>
          <div v-if="pasteRows.length > 200">超过200行，请删除多余数据后再提交。</div>
          <div v-for="item in pasteVisibleErrors" :key="item.line">第{{ item.line }}行：{{ item.message }}</div>
          <div v-if="pasteErrors.length > pasteVisibleErrors.length" class="paste-errors-more">
            还有{{ pasteErrors.length - pasteVisibleErrors.length }}条问题未显示
          </div>
        </div>
      </div>
      <span slot="footer" class="paste-footer">
        <el-button size="mini" :disabled="pasteResolving" @click="pasteDialogVisible = false">取消</el-button>
        <el-button type="primary" size="mini" :loading="pasteResolving" :disabled="!pasteRows.length || pasteRows.length > 200 || !!pasteErrors.length" @click="confirmPaste">校验并加入队列</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { fetchList as fetchStaffList, getStaffByBadge } from '@/api/platform/basic/staff_info'
import { fetchIscStaffCards, saveIscStaffCard, deleteIscStaffCard, fetchIscParkConfigs } from '@/api/platform/basic/staff_info_detail'
import { fetchList as fetchIscCardTaskList } from '@/api/platform/records/isc_card_task'
import { staffStatusInit } from '@/filters/index'

const emptySearchForm = () => ({
  parkId: '',
  staffKeyword: ''
})

const trimValue = value => (value === null || value === undefined ? '' : String(value).trim())

export default {
  name: 'iscCardFastAdd',
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
      fetchIscParkConfigs({
        current: 1,
        size: 1000
      }).then(response => {
        const data = response.data.data || {}
        const records = data.records || []
        this.iscParkOptions = records.filter(item => this.isParkSyncEnabled(item))
      })
    },
    loadTaskList() {
      this.taskLoading = true
      const query = {
        current: 1,
        size: 8
      }
      if (this.searchForm.parkId) {
        query.parkId = this.searchForm.parkId
      }
      if (this.selectedStaff && this.selectedStaff.badge) {
        query.badge = this.selectedStaff.badge
      }
      fetchIscCardTaskList(query).then(response => {
        const data = response.data.data || {}
        this.taskTableData = data.records || []
      }).finally(() => {
        this.taskLoading = false
      })
    },
    parkOptionLabel(item) {
      if (!item) {
        return ''
      }
      const parkName = item.parkName || item.parkId || '-'
      return parkName
    },
    isParkSyncEnabled(item) {
      return !!(item && item.cardSyncEnabled === 1 && item.dispatcherParkId !== null && item.dispatcherParkId !== undefined)
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
      const query = {
        current: 1,
        size: 10
      }
      if (/^\d+$/.test(keyword)) {
        this.searchExactStaffByBadge(keyword)
        return
      } else {
        query.name = keyword
      }
      fetchStaffList(query).then(response => {
        const data = response.data.data || {}
        const records = data.records || []
        this.staffCandidates = records
        if (!records.length) {
          this.selectedStaff = null
          this.staffCards = []
          this.$message({
            message: '未找到匹配员工',
            type: 'warning'
          })
          return
        }
        const exactMatches = records.filter(item => item.name === keyword)
        if (exactMatches.length === 1) {
          this.selectStaff(exactMatches[0])
          return
        }
        this.selectedStaff = null
        this.staffCards = []
        this.$message({
          message: `找到${records.length}名候选员工，请手动选择`,
          type: 'warning'
        })
      }).finally(() => {
        this.staffLoading = false
      })
    },
    searchExactStaffByBadge(badge) {
      getStaffByBadge(badge).then(response => {
        const staff = response.data.data
        if (!staff) {
          this.selectedStaff = null
          this.staffCards = []
          this.staffCandidates = []
          this.$message({
            message: '未找到该工号对应员工',
            type: 'warning'
          })
          return
        }
        this.staffCandidates = [staff]
        this.selectStaff(staff)
      }).finally(() => {
        this.staffLoading = false
      })
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
        this.staffCards = []
        this.staffCardLoading = false
        return
      }
      const requestStaffId = staffId
      this.staffCardLoading = true
      fetchIscStaffCards(requestStaffId).then(response => {
        if (!this.selectedStaff || this.selectedStaff.id !== requestStaffId) {
          return
        }
        this.staffCards = response.data.data || []
      }).finally(() => {
        if (this.selectedStaff && this.selectedStaff.id !== requestStaffId) {
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
          const response = await deleteIscStaffCard(row.id)
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
      if (row && row.syncStatusDesc) {
        return row.syncStatusDesc
      }
      const syncStatus = Number(row && row.syncStatus)
      if (syncStatus === 0) {
        return '待同步'
      }
      if (syncStatus === 1) {
        return '已同步'
      }
      if (syncStatus === 2) {
        return '同步失败'
      }
      if (syncStatus === 3) {
        return '本地取消'
      }
      return '未知'
    },
    cardSyncStatusType(syncStatus) {
      const normalizedStatus = Number(syncStatus)
      if (normalizedStatus === 1) {
        return 'success'
      }
      if (normalizedStatus === 2) {
        return 'danger'
      }
      if (normalizedStatus === 3) {
        return 'info'
      }
      if (normalizedStatus === 0) {
        return 'warning'
      }
      return 'info'
    },
    validateCardNo(cardNo) {
      const normalizedCardNo = trimValue(cardNo)
      if (!normalizedCardNo) {
        return { valid: false, message: 'ISC卡号不能为空' }
      }
      if (!/^[0-9A-Z]{8,20}$/.test(normalizedCardNo)) {
        return { valid: false, message: 'ISC卡号必须为8-20位数字或大写字母' }
      }
      if (normalizedCardNo.startsWith('999')) {
        return { valid: false, message: '999开头为ISC虚拟卡号，不允许维护' }
      }
      return { valid: true, message: '' }
    },
    validateQueueCandidate(staff, cardNo, park) {
      if (!park) {
        return { valid: false, message: '请选择园区' }
      }
      if (!this.isParkSyncEnabled(park)) {
        return { valid: false, message: '当前园区未启用ISC卡片同步' }
      }
      if (!staff || !staff.id) {
        return { valid: false, message: '未找到有效员工' }
      }
      if (Number(staff.status) === 0) {
        return { valid: false, message: '员工已离职，不允许维护ISC卡片' }
      }
      const cardValidation = this.validateCardNo(cardNo)
      if (!cardValidation.valid) {
        return cardValidation
      }
      const duplicate = this.queue.find(item => item.status !== 'success' && String(item.parkId) === String(park.parkId) && item.cardNo === trimValue(cardNo))
      if (duplicate) {
        return { valid: false, message: '卡号已在待提交队列中' }
      }
      return { valid: true, message: '' }
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
      const validation = this.validateQueueCandidate(staff, cardNo, park)
      return {
        queueId: this.nextQueueId++,
        staffId: staff && staff.id,
        badge: staff && staff.badge,
        name: staff && staff.name,
        staffStatus: staff && staff.status,
        parkId: park && park.parkId,
        parkName: park && park.parkName,
        dispatcherParkId: park && park.dispatcherParkId,
        dispatcherParkName: park && park.dispatcherParkName,
        cardNo: trimValue(cardNo),
        status: validation.valid ? 'ready' : 'invalid',
        message: validation.message
      }
    },
    buildInvalidQueueRow(badge, cardNo, message) {
      return {
        queueId: this.nextQueueId++,
        staffId: null,
        badge,
        name: '-',
        staffStatus: null,
        parkId: this.selectedPark && this.selectedPark.parkId,
        parkName: this.selectedPark && this.selectedPark.parkName,
        dispatcherParkId: this.selectedPark && this.selectedPark.dispatcherParkId,
        dispatcherParkName: this.selectedPark && this.selectedPark.dispatcherParkName,
        cardNo: trimValue(cardNo),
        status: 'invalid',
        message
      }
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
        row.status = 'saving'
        row.message = '正在保存并创建ISC同步任务...'
        try {
          const response = await saveIscStaffCard({
            staffId: row.staffId,
            parkId: row.parkId,
            cardNo: row.cardNo
          })
          if (response.data.data) {
            row.status = 'success'
            row.message = '保存成功，ISC同步任务已创建'
            successCount += 1
          } else {
            row.status = 'failed'
            row.message = this.responseMessage(response, '保存失败')
            failedCount += 1
          }
        } catch (error) {
          row.status = 'failed'
          row.message = this.errorMessage(error)
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
      return (text || '').split(/\n/).map((line, index) => {
        const normalizedLine = trimValue(line)
        if (!normalizedLine) {
          return null
        }
        const parts = normalizedLine.split(/[\s,，]+/)
        if (parts.length !== 2) {
          return {
            line: index + 1,
            badge: parts[0] || '',
            cardNo: parts[1] || '',
            message: parts.length < 2 ? '缺少工号或卡号' : '每行只能填写工号和卡号'
          }
        }
        const badge = parts[0]
        const cardNo = parts[1]
        const cardValidation = this.validateCardNo(cardNo)
        return {
          line: index + 1,
          badge,
          cardNo,
          message: cardValidation.valid ? '' : cardValidation.message
        }
      }).filter(Boolean)
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
            this.queue.push(this.buildInvalidQueueRow(item.badge, item.cardNo, item.message))
            return
          }
          const staff = staffMap[String(item.badge)]
          if (!staff) {
            this.queue.push(this.buildInvalidQueueRow(item.badge, item.cardNo, '未找到该工号对应员工'))
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
      const uniqueBadges = Array.from(new Set(badges.map(item => trimValue(item)).filter(Boolean)))
      if (!uniqueBadges.length) {
        return {}
      }
      const response = await fetchStaffList({
        current: 1,
        size: uniqueBadges.length,
        badges: uniqueBadges.join(' ')
      })
      const data = response.data.data || {}
      const records = data.records || []
      return records.reduce((result, staff) => {
        result[String(staff.badge)] = staff
        return result
      }, {})
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
      const statusMap = {
        ready: '待提交',
        invalid: '校验失败',
        saving: '保存中',
        success: '成功',
        failed: '失败'
      }
      return statusMap[status] || '-'
    },
    queueStatusType(status) {
      const typeMap = {
        ready: 'warning',
        invalid: 'danger',
        saving: 'info',
        success: 'success',
        failed: 'danger'
      }
      return typeMap[status] || 'info'
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
      if (row && row.actionDesc) {
        return row.actionDesc
      }
      if (Number(row && row.action) === 1) {
        return '新增卡片'
      }
      if (Number(row && row.action) === 2) {
        return '删除卡片'
      }
      return '-'
    },
    errorMessage(error) {
      if (error && error.response) {
        return this.responseMessage(error.response, '保存失败')
      }
      return (error && error.message) || '保存失败'
    },
    responseMessage(response, fallback) {
      const responseData = response && response.data
      return (responseData && (responseData.msg || responseData.message)) || fallback
    }
  }
}
</script>

<style lang="scss" scoped>
.isc-card-fast-add {
  min-width: 1180px;

  .top-menu {
    min-height: 64px;
    position: relative;
  }

  .page-head {
    padding-right: 580px;
  }

  .page-title {
    display: flex;
    align-items: center;
    color: #333;
    font-size: 16px;
    font-weight: 600;
    line-height: 24px;
  }

  .title-bar {
    width: 3px;
    height: 16px;
    margin-right: 8px;
    border-radius: 2px;
    background: #ed6d00;
  }

  .page-desc {
    margin-top: 6px;
    color: #999;
    font-size: 12px;
    line-height: 18px;
  }

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

  .work-panel,
  .task-block {
    border: 1px solid #e8e9ed;
    border-radius: 4px;
    background: #fff;
  }

  .panel-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 40px;
    padding: 0 12px;
    border-bottom: 1px solid #eee;
    background: #fafafa;
    color: #333;
    font-size: 13px;
    font-weight: 600;
  }

  .panel-count {
    color: #999;
    font-size: 12px;
    font-weight: 400;
  }

  .staff-card {
    padding: 12px;
  }

  .staff-main {
    display: flex;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px dashed #eee;
  }

  .staff-avatar {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 42px;
    height: 42px;
    margin-right: 10px;
    border-radius: 4px;
    background: #f0f2f5;
    color: #666;
    font-size: 16px;
    font-weight: 600;
  }

  .staff-name {
    color: #333;
    font-size: 15px;
    font-weight: 600;

    span {
      margin-left: 8px;
      color: #999;
      font-size: 12px;
      font-weight: 400;
    }
  }

  .staff-meta {
    margin-top: 4px;
    color: #666;
    font-size: 12px;
  }

  .staff-kv {
    padding-top: 10px;
    color: #666;
    font-size: 12px;
    line-height: 26px;

    .el-col:nth-child(2n + 1) {
      color: #999;
      text-align: right;
    }
  }

  .staff-actions {
    padding-top: 6px;
    text-align: right;
  }

  .candidate-list,
  .staff-card-list {
    padding: 0 12px 12px;
  }

  .staff-card-list {
    .danger-text-button {
      color: #e7292e;

      &:hover,
      &:focus {
        color: #c11d22;
      }
    }
  }

  .candidate-title {
    padding: 8px 0;
    color: #666;
    font-size: 12px;
  }

  .queue-panel {
    min-width: 0;
  }

  .card-no {
    font-family: Consolas, Menlo, monospace;
  }

  .row-message {
    margin-top: 3px;
    color: #e7292e;
    font-size: 12px;
  }

  .queue-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    border-top: 1px solid #eee;
    background: #fafafa;
  }

  .queue-note {
    color: #999;
    font-size: 12px;
  }

  .task-block {
    margin: 0 20px 20px;
  }

  ::v-deep .queue-row-error td {
    background: #fff8f8 !important;
  }

  ::v-deep .queue-row-success td {
    background: #f4fbf8 !important;
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
