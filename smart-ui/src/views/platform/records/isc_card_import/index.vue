<!-- 日志管理：IC卡片初始化同步记录 -->
<template>
  <div class="my-basic-container isc-card-import">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" plain @click="resetFrom('searchForm')">清空</el-button>
          </div>
        </div>

        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId" :canClearable="true"></parkSelect>
          </el-form-item>
          <el-form-item label="模式" prop="mode">
            <el-select v-model="searchForm.mode" placeholder="请选择" clearable>
              <el-option label="预检" value="DRY_RUN"></el-option>
              <el-option label="初始化导入" value="IMPORT"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="初始化" value="INIT"></el-option>
              <el-option label="执行中" value="RUNNING"></el-option>
              <el-option label="完成" value="SUCCESS"></el-option>
              <el-option label="失败" value="FAIL"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="创建时间" prop="timeRange">
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

        <avue-crud
          ref="batchCrud"
          :page="batchPage"
          :data="batchData"
          :table-loading="batchLoading"
          :option="batchTableOption"
          @size-change="batchSizeChange"
          @current-change="batchCurrentChange"
        >
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-view" @click="openDetail(scope.row)">明细</el-button>
          </template>
        </avue-crud>

        <div class="detail-title">
          <span>同步明细</span>
          <span v-if="detailSearch.batchId" class="detail-subtitle">批次：{{ detailSearch.batchId }}</span>
        </div>
        <el-form ref="detailSearchForm" :inline="true" :model="detailSearch" class="topForm" size="mini">
          <el-form-item label="工号" prop="badge">
            <el-input v-model="detailSearch.badge" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="detailSearch.name" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="ISC卡号" prop="iscCardNo">
            <el-input v-model="detailSearch.iscCardNo" placeholder="ISC卡号" clearable></el-input>
          </el-form-item>
          <el-form-item label="结果" prop="resultCode">
            <el-select v-model="detailSearch.resultCode" placeholder="请选择" clearable>
              <el-option label="可导入" value="READY_IMPORT"></el-option>
              <el-option label="已导入" value="IMPORTED"></el-option>
              <el-option label="已清理" value="REMOVED"></el-option>
              <el-option label="已一致" value="SKIP_SAME"></el-option>
              <el-option label="冲突" value="CONFLICT"></el-option>
              <el-option label="本地多出" value="LOCAL_ONLY"></el-option>
              <el-option label="ISC无卡" value="ISC_EMPTY"></el-option>
              <el-option label="ISC无人员" value="STAFF_NOT_FOUND"></el-option>
              <el-option label="无效卡" value="INVALID"></el-option>
              <el-option label="失败" value="FAIL"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" @click="searchDetail">搜索明细</el-button>
          </el-form-item>
        </el-form>

        <avue-crud
          ref="detailCrud"
          :page="detailPage"
          :data="detailData"
          :table-loading="detailLoading"
          :option="detailTableOption"
          @size-change="detailSizeChange"
          @current-change="detailCurrentChange"
        ></avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchBatchList, fetchDetailList } from '@/api/platform/records/isc_card_import'
import { batchTableOption, detailTableOption } from '@/const/crud/platform/records/isc_card_import'

const emptySearchForm = () => ({
  parkId: '',
  mode: '',
  status: '',
  timeRange: []
})

const emptyDetailSearch = () => ({
  batchId: '',
  badge: '',
  name: '',
  iscCardNo: '',
  resultCode: ''
})

const isCodeText = value => /^[A-Z_]+$/.test(String(value || ''))

export default {
  name: 'iscCardImport',
  data() {
    return {
      searchForm: emptySearchForm(),
      batchLoading: false,
      batchData: [],
      batchTableOption,
      batchPage: {
        total: 0,
        currentPage: 1,
        pageSize: 20
      },
      detailSearch: emptyDetailSearch(),
      detailLoading: false,
      detailData: [],
      detailTableOption,
      detailPage: {
        total: 0,
        currentPage: 1,
        pageSize: 20
      }
    }
  },
  created() {
    if (this.$route.query.parkId) {
      this.searchForm.parkId = Number(this.$route.query.parkId)
    }
    if (this.$route.query.batchId) {
      this.detailSearch.batchId = this.$route.query.batchId
    }
    this.getBatchList(this.batchPage, this.searchForm)
    if (this.detailSearch.batchId) {
      this.getDetailList(this.detailPage, this.detailSearch)
    }
  },
  methods: {
    getBatchList(page, params) {
      this.batchLoading = true
      const query = Object.assign({
        current: page.currentPage,
        size: page.pageSize
      }, params || {})
      if (query.timeRange && query.timeRange.length === 2) {
        query.startTime = query.timeRange[0]
        query.endTime = query.timeRange[1]
      }
      delete query.timeRange
      fetchBatchList(query).then(response => {
        const data = response.data.data || {}
        this.batchData = (data.records || []).map(this.normalizeBatchRow)
        this.batchPage.total = data.total || 0
        this.batchLoading = false
      }).catch(() => {
        this.batchLoading = false
      })
    },
    getDetailList(page, params) {
      if (!params.batchId) {
        this.detailData = []
        this.detailPage.total = 0
        return
      }
      this.detailLoading = true
      const query = Object.assign({
        current: page.currentPage,
        size: page.pageSize
      }, params || {})
      fetchDetailList(query).then(response => {
        const data = response.data.data || {}
        this.detailData = (data.records || []).map(this.normalizeDetailRow)
        this.detailPage.total = data.total || 0
        this.detailLoading = false
      }).catch(() => {
        this.detailLoading = false
      })
    },
    searchSubmit(form) {
      this.batchPage.currentPage = 1
      this.getBatchList(this.batchPage, form)
    },
    resetFrom(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].resetFields()
      }
      this.searchForm = emptySearchForm()
      this.batchPage.currentPage = 1
      this.getBatchList(this.batchPage, this.searchForm)
    },
    batchSizeChange(val) {
      this.batchPage.currentPage = 1
      this.batchPage.pageSize = val
      this.getBatchList(this.batchPage, this.searchForm)
    },
    batchCurrentChange(val) {
      this.batchPage.currentPage = val
      this.getBatchList(this.batchPage, this.searchForm)
    },
    openDetail(row) {
      this.detailSearch = Object.assign(emptyDetailSearch(), {
        batchId: row.id
      })
      this.detailPage.currentPage = 1
      this.getDetailList(this.detailPage, this.detailSearch)
    },
    searchDetail() {
      this.detailPage.currentPage = 1
      this.getDetailList(this.detailPage, this.detailSearch)
    },
    detailSizeChange(val) {
      this.detailPage.currentPage = 1
      this.detailPage.pageSize = val
      this.getDetailList(this.detailPage, this.detailSearch)
    },
    detailCurrentChange(val) {
      this.detailPage.currentPage = val
      this.getDetailList(this.detailPage, this.detailSearch)
    },
    normalizeBatchRow(row) {
      const batch = Object.assign({}, row)
      batch.modeDesc = this.modeText(batch.mode)
      batch.staffScopeDesc = batch.staffScopeDesc || this.staffScopeText(batch.staffScope)
      batch.statusDesc = this.statusText(batch.status)
      return batch
    },
    normalizeDetailRow(row) {
      const detail = Object.assign({}, row)
      if (!detail.resultDesc || isCodeText(detail.resultDesc)) {
        detail.resultDesc = this.resultText(detail.resultCode || detail.resultDesc)
      }
      return detail
    },
    modeText(mode) {
      return {
        DRY_RUN: '预检',
        IMPORT: '初始化导入'
      }[mode] || (mode ? `未知模式(${mode})` : '-')
    },
    staffScopeText(staffScope) {
      return {
        ALL: '全部人员',
        ACTIVE: '在职人员',
        RESIGNED: '离职人员'
      }[staffScope] || (staffScope ? `未知范围(${staffScope})` : '全部人员')
    },
    statusText(status) {
      return {
        INIT: '初始化',
        RUNNING: '执行中',
        SUCCESS: '完成',
        FAIL: '失败'
      }[status] || (status ? `未知状态(${status})` : '-')
    },
    resultText(resultCode) {
      return {
        READY_IMPORT: '可导入',
        IMPORTED: '已导入',
        REMOVED: '已清理',
        SKIP_SAME: '已一致',
        CONFLICT: '冲突',
        LOCAL_ONLY: '本地多出',
        ISC_EMPTY: 'ISC无卡',
        STAFF_NOT_FOUND: 'ISC无人员',
        INVALID: '无效卡',
        FAIL: '失败'
      }[resultCode] || (resultCode ? `未知结果(${resultCode})` : '-')
    }
  }
}
</script>

<style scoped>
.detail-title {
  display: flex;
  align-items: center;
  margin: 18px 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.detail-subtitle {
  margin-left: 12px;
  font-size: 12px;
  font-weight: 400;
  color: #909399;
}
</style>
