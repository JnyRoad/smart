<!--设备管理，闸机和门禁的通关人员 -->
<template>
  <div class="my-basic-container device">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
          <span style="font-size: 16px;margin-left: 10px;line-height: 30px;">当前设备名称：{{waterMeterName}}</span>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>清空</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="采集时间" prop="timeRange">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="datetimerange"
              range-separator="-"
              value-format="yyyy-MM-dd HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
              start-placeholder="起始时间"
              end-placeholder="截止时间"
              clearable
            ></el-date-picker>
          </el-form-item>
        </el-form>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" @size-change="sizeChange" @current-change="currentChange" :option="tableOption">
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { recordList } from '../_service.js'
import { tableOption } from '@/const/crud/platform/device/record'

export default {
  name: 'device',
  data() {
    return {
      waterMeterId: null,
      waterMeterName: null,
      searchForm: {
        timeRange: undefined
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    }
  },
  created: function () {
    this.waterMeterId = this.$route.params.id
    this.waterMeterName = this.$route.query.name
    this.getList(this.page)
  },
  mounted: function () {},
  computed: {
    startTime: function () {
      if (this.searchForm.timeRange) {
        return this.searchForm.timeRange[0]
      } else {
        return undefined
      }
    },
    endTime: function () {
      if (this.searchForm.timeRange) {
        return this.searchForm.timeRange[1]
      } else {
        return undefined
      }
    }
  },
  methods: {
    goBack() {
      const path = `/platform/device/waterManage`
      this.$router.push({
        path: path,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      })
    },
    getList(page, params) {
      this.tableLoading = true
      recordList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize,
            waterMeterId: this.waterMeterId,
            startTime: this.startTime,
            endTime: this.endTime
          }
        )
      ).then((response) => {
        this.tableData = response.data.data.records
        this.page.total = response.data.data.total
        this.tableLoading = false
      })

      this.tableLoading = false
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
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1
      this.getList(this.page, form)
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields()
        this.page.currentPage = 1
        this.getList(this.page)
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.img-outer {
  width: 80px;
  height: 80px;
  padding-top: 0;
  background: #f7f7f7;
  .img-inner {
    background: #f7f7f7;
    top: 2px;
    right: 2px;
    bottom: 2px;
    left: 2px;
  }
}
</style>
