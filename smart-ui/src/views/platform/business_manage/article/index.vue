<!--业务管理-物品放行  -->
<template>
  <div class="my-basic-container room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>重置</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini" class="topForm">
          <el-form-item label="申请人" prop="name">
            <el-input v-model="searchForm.name" placeholder="申请人" clearable></el-input>
          </el-form-item>
          <el-form-item label="工号" prop="badge">
            <el-input v-model="searchForm.badge" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="创建时间" prop="snapTime" clearable>
            <el-date-picker
              v-model="snapTime"
              type="daterange"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              range-separator="-"
              start-placeholder="起始日期"
              end-placeholder="截止日期"
            ></el-date-picker>
          </el-form-item>
        </el-form>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="handleCheckDetail(scope.row)">查看详情</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList } from './_service'
import { tableOption } from '@/const/crud/platform/business_manage/article'

export default {
  name: 'dorm_mng',
  data() {
    return {
      replyRules: {
        status: [{ required: true, message: '请选择回复类型', trigger: 'change' }]
      },
      searchForm: {
        name: undefined,
        badge: undefined,
        startTime: undefined, //开始时间
        endTime: undefined, //结束时间
      },
      snapTime:[],
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
  created() {
    this.$nextTick(() => {
      // 详情带参数返回
      if (this.$route.query.queryForm != undefined) {
        let queryPage = this.$route.query.queryPage
        let queryForm = this.$route.query.queryForm
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {})
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {})
        }
        this.getList(this.page, this.searchForm)
      } else {
        this.getList(this.page)
      }
    })
  },
  mounted: function () {},
  computed: {},
  methods: {
    getList(page, params) {
      this.tableLoading = true

      if (this.snapTime.length == 2) {
        this.searchForm.startTime = this.snapTime[0] + " 00:00:00"
        this.searchForm.endTime = this.snapTime[1] + " 23:59:59"
      }
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
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
    handleCheckDetail(row) {
      this.$router.push({
        path: `/platform/business_manage/article/detail/${row.id}`,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      })
    },
    searchSubmit(form) {
      //搜索
      this.page.currentPage = 1
      this.getList(this.page, form)
    },
    resetFrom(formName) {
      //清空
      this.snapTime = []
      this.$refs[formName].resetFields()
      this.page.currentPage = 1
      this.getList(this.page)
    },
  }
}
</script>
<style lang="scss" scoped>
.config_form ::v-deep {
  .el-form--inline .el-form-item {
    margin: 0;
    width: 100%;
    height: 100%;
  }
  .el-form--inline .el-form-item__content {
    width: 100%;
  }
  .el-input--mini .el-input__inner {
    text-align: center;
    border: none;
  }
  .el-input.is-disabled .el-input__inner {
    background-color: transparent;
    border-color: transparent;
    color: #333;
    cursor: not-allowed;
  }
  .config-inner {
    position: relative;
  }
  .tips {
    color: red;
    display: inline-block;
    margin-left: 50px;
    font-size: 12px;
  }
  .status-bg {
    position: absolute;
    right: 0;
    top: 20px;
    margin-right: 0;
  }
  .ruleTbl {
    width: 100%;
    font-size: 12px;
    margin-bottom: 20px;
    margin-top: 15px;
    td {
      border: 1px solid #e0e0e0;
      padding: 5px 0 12px 0;
      text-align: center;
    }
    td:first-child {
      width: 100px;
    }
    .tdL {
      padding: 0 10px;
    }
    .el-button {
      font-size: 12px;
    }
  }
}
</style>
