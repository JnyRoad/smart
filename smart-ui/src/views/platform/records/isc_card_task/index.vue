<!-- 日志管理：IC卡片同步任务 -->
<template>
  <div class="my-basic-container isc-card-task">
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
          <el-form-item label="工号" prop="badge">
            <el-input v-model="searchForm.badge" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="searchForm.name" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="卡 ID" prop="cardNo">
            <el-input v-model="searchForm.cardNo" placeholder="卡 ID" clearable></el-input>
          </el-form-item>
          <el-form-item label="任务动作" prop="action">
            <el-select v-model="searchForm.action" placeholder="请选择" clearable>
              <el-option label="新增卡片" :value="1"></el-option>
              <el-option label="删除卡片" :value="2"></el-option>
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
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
        ></avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList } from '@/api/platform/records/isc_card_task'
import { tableOption } from '@/const/crud/platform/records/isc_card_task'

const emptySearchForm = () => ({
  parkId: '',
  badge: '',
  name: '',
  cardNo: '',
  action: '',
  timeRange: []
})

export default {
  name: 'iscCardTask',
  data() {
    return {
      searchForm: emptySearchForm(),
      tableLoading: false,
      tableData: [],
      tableOption,
      page: {
        total: 0,
        currentPage: 1,
        pageSize: 20
      }
    }
  },
  created() {
    this.getList(this.page, this.searchForm)
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true
      const query = Object.assign({
        current: page.currentPage,
        size: page.pageSize
      }, params || {})
      if (query.timeRange && query.timeRange.length === 2) {
        query.startTime = query.timeRange[0]
        query.endTime = query.timeRange[1]
      }
      delete query.timeRange
      fetchList(query).then(response => {
        const data = response.data.data || {}
        this.tableData = (data.records || []).map(this.normalizeRow)
        this.page.total = data.total || 0
        this.tableLoading = false
      }).catch(() => {
        this.tableLoading = false
      })
    },
    searchSubmit(form) {
      this.page.currentPage = 1
      this.getList(this.page, form)
    },
    resetFrom(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].resetFields()
      }
      this.searchForm = emptySearchForm()
      this.page.currentPage = 1
      this.getList(this.page, this.searchForm)
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
    normalizeRow(row) {
      const task = Object.assign({}, row)
      task.actionDesc = this.actionText(task)
      task.remark = task.remark || '-'
      return task
    },
    actionText(row) {
      if (row && row.actionDesc) {
        return row.actionDesc
      }
      if (!row || row.action === null || row.action === undefined || row.action === '') {
        return '-'
      }
      const action = Number(row.action)
      if (action === 1) {
        return '新增卡片'
      }
      if (action === 2) {
        return '删除卡片'
      }
      return `未知动作(${row.action})`
    }
  }
}
</script>
