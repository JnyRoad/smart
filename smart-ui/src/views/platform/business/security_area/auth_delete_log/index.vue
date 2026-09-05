<!-- 保密区权限自动删除记录报表：只展示服务端审计快照和关联任务状态。 -->
<template>
  <div class="my-basic-container auth-delete-log">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="report-notice">
          <i class="el-icon-info"></i>
          成功仅表示关联任务记录成功，不代表设备已完成物理确认；任务记录缺失或状态非法时显示“任务状态未知”。
        </div>

        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini" @submit.native.prevent>
          <el-form-item label="执行日期">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="datetimerange"
              value-format="yyyy-MM-dd HH:mm:ss"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              :clearable="true"
            />
          </el-form-item>
          <el-form-item label="园区">
            <parkSelect v-model="searchForm.parkId" :canClearable="true"></parkSelect>
          </el-form-item>
          <el-form-item label="工号">
            <el-input v-model="searchForm.staffBadge" clearable placeholder="请输入工号" @keyup.enter.native="handleSearch"></el-input>
          </el-form-item>
          <el-form-item label="姓名">
            <el-input v-model="searchForm.staffName" clearable placeholder="请输入姓名" @keyup.enter.native="handleSearch"></el-input>
          </el-form-item>
          <el-form-item label="部门">
            <el-input v-model="searchForm.department" clearable placeholder="请输入部门"></el-input>
          </el-form-item>
          <el-form-item label="权限组">
            <el-input v-model="searchForm.authName" clearable placeholder="请输入权限组"></el-input>
          </el-form-item>
          <el-form-item label="结果">
            <el-select v-model="searchForm.result" clearable placeholder="请选择结果">
              <el-option v-for="item in resultOptions" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
            <el-button icon="el-icon-refresh-left" @click="resetSearch">重置</el-button>
            <el-button icon="el-icon-refresh" :loading="tableLoading" @click="refreshList">刷新</el-button>
            <el-button
              v-if="permissions['platform_security_auth_delete_log_export']"
              type="primary"
              plain
              icon="el-icon-download"
              :loading="exportLoading"
              @click="exportReport"
            >导出</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="tableLoading" :data="tableData" border stripe class="report-table">
          <el-table-column prop="execTime" label="执行时间" min-width="155"></el-table-column>
          <el-table-column prop="parkId" label="园区" min-width="110">
            <template slot-scope="scope">{{ displayValue(scope.row.parkName || scope.row.parkId) }}</template>
          </el-table-column>
          <el-table-column prop="staffBadge" label="工号" min-width="100"></el-table-column>
          <el-table-column prop="staffName" label="姓名" min-width="90"></el-table-column>
          <el-table-column prop="department" label="部门" min-width="120"></el-table-column>
          <el-table-column prop="authName" label="权限组" min-width="140"></el-table-column>
          <el-table-column prop="lastSnapTime" label="最后进出时间" min-width="155">
            <template slot-scope="scope">{{ displayValue(scope.row.lastSnapTime) }}</template>
          </el-table-column>
          <el-table-column prop="triggerReason" label="触发原因" min-width="125">
            <template slot-scope="scope">{{ displayValue(scope.row.triggerReason) }}</template>
          </el-table-column>
          <el-table-column prop="result" label="结果" min-width="125">
            <template slot-scope="scope">
              <el-tag size="mini" :type="resultTagType(scope.row.result)">{{ resultLabel(scope.row.result) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="任务统计" min-width="145">
            <template slot-scope="scope">
              <span v-if="hasTaskSummary(scope.row)">
                {{ scope.row.successCount || 0 }}/{{ scope.row.taskCount || 0 }} 成功
                <span v-if="scope.row.failCount">，失败 {{ scope.row.failCount }}</span>
                <span v-if="scope.row.pendingCount">，处理中 {{ scope.row.pendingCount }}</span>
                <span v-if="scope.row.unknownCount">，未知 {{ scope.row.unknownCount }}</span>
              </span>
              <span v-else>—</span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="说明" min-width="180">
            <template slot-scope="scope">{{ displayValue(scope.row.remark) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="90" fixed="right">
            <template slot-scope="scope">
              <el-button
                type="text"
                size="mini"
                :disabled="!canViewTasks(scope.row)"
                @click="openTaskDetail(scope.row)"
              >任务明细</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :current-page="page.currentPage"
            :page-size="page.pageSize"
            :page-sizes="[20, 50, 100]"
            :total="page.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          ></el-pagination>
        </div>
      </section>
    </el-scrollbar>

    <el-dialog title="关联任务明细" width="900px" :visible.sync="detailVisible" @close="closeTaskDetail">
      <div v-if="detailRow" class="detail-summary">
        <span>人员：{{ displayValue(detailRow.staffName) }}（{{ displayValue(detailRow.staffBadge) }}）</span>
        <span>权限组：{{ displayValue(detailRow.authName) }}</span>
        <span>记录结果：{{ resultLabel(detailRow.result) }}</span>
      </div>
      <el-alert
        v-if="detailError"
        title="任务明细加载失败，请稍后重试"
        type="error"
        :closable="false"
        show-icon
      ></el-alert>
      <el-table v-loading="detailLoading" :data="detailTasks" border stripe>
        <el-table-column prop="taskSource" label="任务来源" width="100">
          <template slot-scope="scope">{{ taskSourceLabel(scope.row.taskSource) }}</template>
        </el-table-column>
        <el-table-column prop="taskId" label="任务 ID" min-width="160"></el-table-column>
        <el-table-column prop="deviceCode" label="设备编号" min-width="140"></el-table-column>
        <el-table-column prop="action" label="动作" width="90">
          <template slot-scope="scope">{{ displayValue(scope.row.action) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="任务状态" min-width="125">
          <template slot-scope="scope">
            <el-tag size="mini" :type="taskStatusTagType(scope.row.status)">{{ taskStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="返回码" min-width="90">
          <template slot-scope="scope">{{ displayValue(scope.row.code) }}</template>
        </el-table-column>
        <el-table-column prop="remark" label="失败说明" min-width="160">
          <template slot-scope="scope">{{ displayValue(scope.row.remark) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="155"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="155"></el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { exportLogs, fetchPage, fetchTasks } from '@/api/platform/securityAuthDeleteLog'

export default {
  name: 'SecurityAuthDeleteLog',
  data() {
    return {
      searchForm: {
        dateRange: [],
        parkId: '',
        staffBadge: '',
        staffName: '',
        department: '',
        authName: '',
        result: ''
      },
      resultOptions: [
        { value: 'SKIPPED_WHITELIST', label: '白名单跳过' },
        { value: 'SKIPPED_NOT_DUE', label: '未到删除期限' },
        { value: 'SKIPPED_NO_DEVICE', label: '无关联设备' },
        { value: 'SKIPPED_STAFF_MISSING', label: '人员不存在' },
        { value: 'SKIPPED_MISSING_TIME', label: '缺少判定时间' },
        { value: 'DRY_RUN', label: '演练命中' },
        { value: 'PROCESSING', label: '任务执行中' },
        { value: 'SUCCESS', label: '任务记录成功' },
        { value: 'FAILED', label: '处理或任务失败' },
        { value: 'UNKNOWN', label: '任务状态未知' }
      ],
      page: {
        currentPage: 1,
        pageSize: 20,
        total: 0
      },
      tableData: [],
      tableLoading: false,
      exportLoading: false,
      detailVisible: false,
      detailLoading: false,
      detailError: '',
      detailRow: null,
      detailTasks: [],
      listRequestId: 0,
      detailRequestId: 0
    }
  },
  computed: {
    ...mapGetters(['permissions'])
  },
  created() {
    this.getList()
  },
  methods: {
    /**
     * 将当前筛选表单转换为后端分页参数，省略空筛选避免改变服务端查询含义。
     * @returns {Object} 包含分页和组合筛选条件的请求参数。
     */
    buildPageQuery() {
      const query = {
        current: this.page.currentPage,
        size: this.page.pageSize
      }
      return Object.assign(query, this.buildFilterQuery())
    },

    /**
     * 将当前筛选表单转换为导出或分页共用的筛选参数。
     * @returns {Object} 只包含非空园区、日期、人员、部门、权限组和结果条件的参数。
     */
    buildFilterQuery() {
      const query = {}
      const dateRange = this.searchForm.dateRange || []
      if (dateRange[0]) {
        query.startTime = dateRange[0]
      }
      if (dateRange[1]) {
        query.endTime = dateRange[1]
      }
      ;['parkId', 'staffBadge', 'staffName', 'department', 'authName', 'result'].forEach(key => {
        const value = this.searchForm[key]
        if (value !== '' && value !== null && value !== undefined) {
          query[key] = value
        }
      })
      return query
    },

    /**
     * 请求当前页审计记录，并以递增请求标识阻止旧筛选结果覆盖新结果。
     * @returns {Promise<void>} 列表请求完成；接口失败时保留已有列表并提示错误。
     */
    async getList() {
      const requestId = (this.listRequestId || 0) + 1
      this.listRequestId = requestId
      this.tableLoading = true
      try {
        const response = await fetchPage(this.buildPageQuery())
        if (requestId !== this.listRequestId) {
          return
        }
        if (!response || !response.data || response.data.code !== 0) {
          throw new Error((response && response.data && response.data.msg) || '记录查询失败')
        }
        const result = response.data.data || {}
        this.tableData = Array.isArray(result.records) ? result.records : []
        this.page.total = Number(result.total) || 0
      } catch (error) {
        if (requestId === this.listRequestId) {
          this.$message({ message: this.getErrorMessage(error, '记录查询失败'), type: 'error' })
        }
      } finally {
        if (requestId === this.listRequestId) {
          this.tableLoading = false
        }
      }
    },

    /**
     * 应用组合筛选并从第一页重新查询记录。
     * @returns {Promise<void>} 新筛选条件的列表请求。
     */
    async handleSearch() {
      this.page.currentPage = 1
      await this.getList()
    },

    /**
     * 清空所有筛选条件并重新加载第一页。
     * @returns {Promise<void>} 重置后的列表请求。
     */
    async resetSearch() {
      this.searchForm = {
        dateRange: [],
        parkId: '',
        staffBadge: '',
        staffName: '',
        department: '',
        authName: '',
        result: ''
      }
      this.page.currentPage = 1
      await this.getList()
    },

    /**
     * 按当前筛选条件刷新列表，保留当前页位置。
     * @returns {Promise<void>} 刷新请求。
     */
    async refreshList() {
      await this.getList()
    },

    /**
     * 切换每页数量并回到第一页，避免新页大小落在不存在的页码上。
     * @param {number} pageSize 新的每页记录数。
     * @returns {Promise<void>} 新页大小的列表请求。
     */
    async handleSizeChange(pageSize) {
      this.page.pageSize = pageSize
      this.page.currentPage = 1
      await this.getList()
    },

    /**
     * 切换当前页并请求对应记录。
     * @param {number} currentPage 新的页码。
     * @returns {Promise<void>} 新页码的列表请求。
     */
    async handleCurrentChange(currentPage) {
      this.page.currentPage = currentPage
      await this.getList()
    },

    /**
     * 下载当前筛选范围的服务端 CSV，接口失败时明确提示且不伪造成功。
     * @returns {Promise<void>} 导出请求完成。
     */
    async exportReport() {
      this.exportLoading = true
      try {
        const response = await exportLogs(this.buildFilterQuery())
        const exportError = this.getExportErrorMessage(response)
        if (exportError) {
          throw new Error(exportError)
        }
        if (!response || response.data === undefined || response.data === null) {
          throw new Error('导出响应为空')
        }
        this.downloadBlob(response.data, '保密区权限自动删除记录.csv')
      } catch (error) {
        this.$message({ message: this.getErrorMessage(error, '导出失败'), type: 'error' })
      } finally {
        this.exportLoading = false
      }
    },

    /**
     * 识别导出接口返回的 JSON 业务错误或 HTTP 错误，避免把错误内容保存成 CSV。
     * @param {Object} response axios 导出响应。
     * @returns {string} 错误文案；空字符串表示可以继续下载二进制文件。
     */
    getExportErrorMessage(response) {
      if (!response) {
        return '导出失败'
      }
      if (response.status && response.status >= 400) {
        return '导出失败'
      }
      const data = response.data
      const isBinary = data instanceof Blob || data instanceof ArrayBuffer || ArrayBuffer.isView(data)
      if (!isBinary && data && typeof data === 'object') {
        if (data.code !== undefined && data.code !== 0) {
          return data.msg || '导出失败'
        }
        return '导出响应不是文件'
      }
      const headers = response.headers || {}
      const contentType = headers['content-type'] || headers['Content-Type'] || ''
      if (/json/i.test(contentType)) {
        return '导出失败'
      }
      return ''
    },

    /**
     * 将服务端 CSV 二进制响应交给浏览器下载，并释放临时 URL。
     * @param {ArrayBuffer|Blob} data 服务端返回的 CSV 数据。
     * @param {string} filename 浏览器保存文件名。
     * @returns {void} 触发一次浏览器下载。
     */
    downloadBlob(data, filename) {
      const blob = data instanceof Blob ? data : new Blob([data], { type: 'text/csv;charset=utf-8' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    },

    /**
     * 打开任务明细并按请求标识绑定当前审计记录，防止晚响应串到另一条记录。
     * @param {Object} row 审计记录行，必须包含服务端返回的字符串主键。
     * @returns {Promise<void>} 任务详情请求完成。
     */
    async openTaskDetail(row) {
      const requestId = (this.detailRequestId || 0) + 1
      this.detailRequestId = requestId
      this.detailRow = row
      this.detailTasks = []
      this.detailError = ''
      this.detailVisible = true
      this.detailLoading = true
      try {
        const response = await fetchTasks(String(row.id))
        if (requestId !== this.detailRequestId) {
          return
        }
        if (!response || !response.data || response.data.code !== 0) {
          throw new Error((response && response.data && response.data.msg) || '任务明细加载失败')
        }
        this.detailTasks = Array.isArray(response.data.data) ? response.data.data : []
      } catch (error) {
        if (requestId === this.detailRequestId) {
          this.detailError = '任务明细加载失败，请稍后重试'
          this.$message({ message: this.getErrorMessage(error, '任务明细加载失败'), type: 'error' })
        }
      } finally {
        if (requestId === this.detailRequestId) {
          this.detailLoading = false
        }
      }
    },

    /**
     * 关闭任务明细并使尚未返回的请求失效。
     * @returns {void} 清理详情加载状态。
     */
    closeTaskDetail() {
      this.detailRequestId = (this.detailRequestId || 0) + 1
      this.detailLoading = false
      this.detailVisible = false
    },

    /**
     * 返回优先使用后端信息的安全错误文案。
     * @param {Error} error 请求或业务异常。
     * @param {string} fallback 没有可展示异常信息时的兜底文案。
     * @returns {string} 不包含敏感信息的错误提示。
     */
    getErrorMessage(error, fallback) {
      return error && error.message ? error.message : fallback
    },

    /**
     * 将空快照统一展示为短横线，避免把创建时间误当最后进出时间。
     * @param {*} value 服务端快照字段。
     * @returns {*} 原值或空值占位符。
     */
    displayValue(value) {
      return value === null || value === undefined || value === '' ? '-' : value
    },

    /**
     * 判断记录是否存在可下钻的设备任务统计。
     * @param {Object} row 审计记录行。
     * @returns {boolean} 有任务或任务状态汇总时允许打开详情。
     */
    hasTaskSummary(row) {
      return Number(row.taskCount) > 0 || Number(row.unknownCount) > 0
    },

    /**
     * 仅允许正式删权记录打开关联任务，其他分支不伪造设备任务。
     * @param {Object} row 审计记录行。
     * @returns {boolean} 是否可以请求任务详情。
     */
    canViewTasks(row) {
      return ['PROCESSING', 'SUCCESS', 'FAILED', 'UNKNOWN'].indexOf(row.result) !== -1 && this.hasTaskSummary(row)
    },

    /**
     * 将审计结果代码转换为管理端可读文案。
     * @param {string} result 后端结果代码。
     * @returns {string} 状态文案，未知代码保留未知提示。
     */
    resultLabel(result) {
      const labels = {
        SKIPPED_WHITELIST: '白名单跳过',
        SKIPPED_NOT_DUE: '未到删除期限',
        SKIPPED_NO_DEVICE: '无关联设备',
        SKIPPED_STAFF_MISSING: '人员不存在',
        SKIPPED_MISSING_TIME: '缺少判定时间',
        DRY_RUN: '演练命中',
        PROCESSING: '任务执行中',
        SUCCESS: '任务记录成功',
        FAILED: '处理或任务失败',
        UNKNOWN: '任务状态未知'
      }
      return labels[result] || '未知结果'
    },

    /**
     * 根据审计结果选择 Element 标签颜色。
     * @param {string} result 后端结果代码。
     * @returns {string} Element UI 标签类型。
     */
    resultTagType(result) {
      if (result === 'SUCCESS') return 'success'
      if (result === 'PROCESSING' || result === 'DRY_RUN') return 'warning'
      if (result === 'FAILED' || result === 'UNKNOWN') return 'danger'
      return 'info'
    },

    /**
     * 将设备任务来源转换为可读文案，未知来源不隐藏。
     * @param {string} source 任务来源 NORMAL 或 ISC。
     * @returns {string} 来源文案。
     */
    taskSourceLabel(source) {
      if (source === 'NORMAL') return '普通设备'
      if (source === 'ISC') return 'ISC'
      return '未知来源'
    },

    /**
     * 按统一状态口径展示普通设备和 ISC 任务的当前状态。
     * @param {number|null} status 任务状态，缺失或非法状态都按未知处理。
     * @returns {string} 任务状态文案。
     */
    taskStatusLabel(status) {
      if (status === 1 || status === '1') return '成功（任务记录）'
      if ([0, 3, 6, '0', '3', '6'].indexOf(status) !== -1) return '执行中'
      if ([2, 4, 5, '2', '4', '5'].indexOf(status) !== -1) return '失败'
      return '任务状态未知'
    },

    /**
     * 根据任务状态选择 Element 标签颜色，未知状态保持醒目错误色。
     * @param {number|null} status 任务状态。
     * @returns {string} Element UI 标签类型。
     */
    taskStatusTagType(status) {
      if (status === 1 || status === '1') return 'success'
      if ([0, 3, 6, '0', '3', '6'].indexOf(status) !== -1) return 'warning'
      if ([2, 4, 5, '2', '4', '5'].indexOf(status) !== -1) return 'danger'
      return 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.auth-delete-log {
  .report-notice {
    padding: 10px 14px;
    margin-bottom: 14px;
    color: #856404;
    background: #fff8e1;
    border: 1px solid #ffe8a1;
    border-radius: 3px;
  }

  .report-notice i {
    margin-right: 5px;
  }

  .report-table {
    margin-top: 5px;
  }

  .pagination-wrap {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .detail-summary {
    display: flex;
    gap: 28px;
    margin-bottom: 16px;
    color: #606266;
  }
}
</style>
