<!--外包人员申请模板  -->
<template>
  <div class="my-basic-container room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>重置</el-button>
            <el-button type="primary" icon="el-icon-upload2" @click="importDialog.visible = true">导入excel申请</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini" class="topForm">
          <el-form-item label="申请时间" prop="snapTime" clearable>
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
          <el-form-item label="状态" prop="status">
            <el-select v-model="searchForm.status" placeholder="请选择">
              <el-option v-for="item in statusArr" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="handleCheckDetail(scope.row)">查看</el-button>
          </template>
        </avue-crud>
        <!--导入人员 弹出框 -->
        <el-dialog title="导入人员" class="dialog_form" width="1200px" :visible.sync="importDialog.visible">
          <div class="import-dialog-inner">
            <componentImport v-if="importDialog.visible" @complete="importComplete"></componentImport>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
  import { fetchList } from '@/api/platform/outsourcing/apply'
  import { tableOption } from '@/const/crud/platform/outsourcing/apply'
  import componentImport from './import/index'

  const statusArr = [
    { label: '待审批', value: 0 },
    { label: '已通过', value: 1 },
    { label: '已拒绝', value: 2 },
  ]
  export default {
    name: 'outsrc_apply',
    components: {
      componentImport
    },
    data() {
      return {
        importDialog: {
          visible: false
        },
        snapTime: [],
        statusArr: statusArr,
        searchForm: {
          applyStartTime: undefined, //申请开始时间
          applyEndTime: undefined, //申请结束时间
          status: undefined, // 状态
          compName: undefined,
          isApprove: false
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
    created() {
      this.$nextTick(() => {
        // 详情带参数返回
        if (this.$route.query.queryForm !== undefined) {
          let queryPage = this.$route.query.queryPage
          let queryForm = this.$route.query.queryForm
          if (queryPage && queryPage.constructor === Object) {
            this.page = Object.assign(queryPage, {})
          }
          if (queryForm && queryForm.constructor === Object) {
            this.searchForm = Object.assign(queryForm, {})
          }
        }
        this.getList(this.page, this.searchForm)
      })
    },
    mounted: function () {},
    computed: {},
    methods: {
      getList(page, params) {
        this.tableLoading = true

        if (this.snapTime) {
          if (this.snapTime.length === 2) {
            this.searchForm.applyStartTime = this.snapTime[0]
            this.searchForm.applyEndTime = this.snapTime[1]
          }
        }
        fetchList(
          Object.assign(
            {
              asc: 'id',
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
        this.searchForm = {
          applyStartTime: undefined, //申请开始时间
          applyEndTime: undefined, //申请结束时间
          status: undefined, // 状态
          compName: undefined,
          isApprove: false
        }
        this.getList(this.page, this.searchForm)
      },
      importComplete(){
        this.importDialog.visible = false
        this.getList(this.page, this.searchForm)
      },
      handleCheckDetail(row) {
        this.$router.push({
          path: `/platform/outsourcing/detail/${row.applyId}`,
          query: {
            queryPage: this.page,
            queryForm: this.searchForm,
            isApprove: false
          }
        });
      },
    }
  }
</script>
<style lang="scss" scoped>
</style>
