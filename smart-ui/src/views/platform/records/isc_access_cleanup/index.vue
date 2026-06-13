<!-- 日志管理：ISC权限残留清理 -->
<template>
  <div class="my-basic-container isc-access-cleanup">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="reloadData">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" plain @click="resetFrom('searchForm')">清空</el-button>
            <el-button type="primary" icon="el-icon-s-tools" :loading="executeLoading" @click="executeSelected">生成删除任务</el-button>
          </div>
        </div>

        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId" :canClearable="true"></parkSelect>
          </el-form-item>
          <el-form-item label="人员类型" prop="personType">
            <el-select v-model="searchForm.personType" placeholder="请选择" clearable>
              <el-option label="访客" value="VISITOR"></el-option>
              <el-option label="离职人员" value="STAFF"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="处理状态" prop="cleanupStatus">
            <el-select v-model="searchForm.cleanupStatus" placeholder="请选择" clearable>
              <el-option label="待处理" value="EXECUTABLE"></el-option>
              <el-option label="保留" value="PROTECTED"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="关键词" prop="keyword">
            <el-input v-model="searchForm.keyword" placeholder="姓名/证件/工号/人员ID" clearable></el-input>
          </el-form-item>
          <el-form-item label="设备编码" prop="deviceCode">
            <el-input v-model="searchForm.deviceCode" placeholder="设备编码" clearable></el-input>
          </el-form-item>
          <el-form-item label="结束时间" prop="timeRange">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="daterange"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              range-separator="-"
              start-placeholder="起始日期"
              end-placeholder="截止日期"
              clearable
            ></el-date-picker>
          </el-form-item>
        </el-form>

        <div class="summary-row">
          <div class="summary-item">
            <span class="summary-label">总数</span>
            <strong>{{ summary.totalCount || 0 }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-label">待处理</span>
            <strong>{{ summary.executableCount || 0 }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-label">保留</span>
            <strong>{{ summary.protectedCount || 0 }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-label">访客</span>
            <strong>{{ summary.visitorCount || 0 }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-label">离职人员</span>
            <strong>{{ summary.staffCount || 0 }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-label">未生成</span>
            <strong>{{ summary.missingDeleteTaskCount || 0 }}</strong>
          </div>
          <div class="summary-item">
            <span class="summary-label">待重试</span>
            <strong>{{ summary.retryDeleteTaskCount || 0 }}</strong>
          </div>
        </div>

        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @selection-change="selectionChange"
          @size-change="sizeChange"
          @current-change="currentChange"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-s-tools"
              :disabled="scope.row.cleanupStatus !== 'EXECUTABLE'"
              @click="executeRow(scope.row)"
            >生成任务</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { executeCleanup, fetchList, fetchSummary } from '@/api/platform/records/isc_access_cleanup'
import { tableOption } from '@/const/crud/platform/records/isc_access_cleanup'

const emptySearchForm = () => ({
  parkId: '',
  personType: '',
  cleanupStatus: 'EXECUTABLE',
  keyword: '',
  deviceCode: '',
  timeRange: []
})

export default {
  name: 'iscAccessCleanup',
  data() {
    return {
      searchForm: emptySearchForm(),
      summary: {},
      tableLoading: false,
      executeLoading: false,
      tableData: [],
      selectedRows: [],
      tableOption,
      page: {
        total: 0,
        currentPage: 1,
        pageSize: 20
      }
    }
  },
  created() {
    this.reloadData()
  },
  methods: {
    buildQuery(page, params) {
      const query = Object.assign({
        current: page.currentPage,
        size: page.pageSize
      }, params || {})
      return this.normalizeQueryTimeRange(query)
    },
    buildFilterQuery(params) {
      return this.normalizeQueryTimeRange(Object.assign({}, params || {}))
    },
    normalizeQueryTimeRange(query) {
      if (query.timeRange && query.timeRange.length === 2) {
        query.startTime = query.timeRange[0]
        query.endTime = query.timeRange[1]
      }
      delete query.timeRange
      return query
    },
    reloadData() {
      this.page.currentPage = 1
      this.getList(this.page, this.searchForm)
      this.getSummary(this.searchForm)
    },
    getList(page, params) {
      this.tableLoading = true
      fetchList(this.buildQuery(page, params)).then(response => {
        const data = response.data.data || {}
        this.tableData = data.records || []
        this.page.total = data.total || 0
        this.tableLoading = false
      }).catch(() => {
        this.tableLoading = false
      })
    },
    getSummary(params) {
      fetchSummary(this.buildQuery({ currentPage: 1, pageSize: 1 }, params)).then(response => {
        this.summary = response.data.data || {}
      })
    },
    resetFrom(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].resetFields()
      }
      this.searchForm = emptySearchForm()
      this.reloadData()
    },
    selectionChange(selection) {
      this.selectedRows = selection || []
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page, this.searchForm)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.searchForm)
    },
    executeSelected() {
      const executableRows = this.selectedRows.filter(row => row.cleanupStatus === 'EXECUTABLE')
      const payload = executableRows.length > 0
        ? { downRecordIds: executableRows.map(row => row.downRecordId) }
        : this.buildFilterQuery(this.searchForm)
      this.execute(payload)
    },
    executeRow(row) {
      if (!row || row.cleanupStatus !== 'EXECUTABLE') {
        return
      }
      this.execute({ downRecordIds: [row.downRecordId] })
    },
    execute(payload) {
      this.executeLoading = true
      executeCleanup(payload).then(response => {
        const data = response.data.data || {}
        this.$message.success(`已创建${data.createdCount || 0}条，已刷新${data.updatedCount || 0}条，跳过${data.skipCount || 0}条`)
        this.executeLoading = false
        this.getList(this.page, this.searchForm)
        this.getSummary(this.searchForm)
      }).catch(() => {
        this.executeLoading = false
      })
    }
  }
}
</script>

<style scoped>
.summary-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(118px, 1fr));
  gap: 10px;
  margin: 6px 0 14px;
}

.summary-item {
  min-height: 58px;
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;
}

.summary-label {
  display: block;
  margin-bottom: 6px;
  color: #909399;
  font-size: 12px;
}

.summary-item strong {
  color: #303133;
  font-size: 20px;
  line-height: 24px;
}
</style>
