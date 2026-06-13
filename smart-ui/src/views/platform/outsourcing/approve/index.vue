<!--外包人员申请模板  -->
<template>
  <div class="my-basic-container room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit()">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>重置</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini" class="topForm">
          <el-form-item label="单位名称" prop="compName">
            <el-input v-model="searchForm.compName" placeholder="单位名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="申请时间" prop="snapTime">
            <el-date-picker
              v-model="snapTime"
              type="daterange"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              range-separator="-"
              start-placeholder="起始日期"
              end-placeholder="截止日期"
              clearable
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
            <el-button icon="el-icon-check" v-show="scope.row.statusDesc === '待审批'" type="text" @click="pass(scope.row.applyId)">通过</el-button>
            <el-button icon="el-icon-close" v-show="scope.row.statusDesc === '待审批'" type="text" @click="handleRefuseData(scope.row)">拒绝</el-button>
            <el-button type="text" icon="el-icon-edit" @click="handleCheckDetail(scope.row)">查看</el-button>
          </template>
        </avue-crud>

        <!--待确认回复模板 -->
        <el-dialog class="dialog_form config_form" width="500px" @close="resetReplyFormData('replyForm')" :visible.sync="replyFormVisible">
          <el-form :rules="replyRules" ref="replyForm" :model="replyForm" label-width="80px">
            <el-form-item label="拒绝原因" prop="reason">
              <el-input :rows="4" v-model="replyForm.reason" maxlength="100" type="textarea" clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="resetReplyFormData('replyForm')" plain>取 消</el-button>
            <el-button type="primary" @click="refuse('replyForm')" :loading="setLoading">保 存</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
  import { fetchList, passOrRefuse } from '@/api/platform/outsourcing/apply'
  import { tableOption } from '@/const/crud/platform/outsourcing/approve'

  const statusArr = [
    { label: '待审批', value: 0 },
    { label: '已通过', value: 1 },
    { label: '已拒绝', value: 2 },
  ]
  export default {
    name: 'outsrc_approve',
    data() {
      return {
        snapTime: [],
        statusArr: statusArr,
        searchForm: {
          applyStartTime: undefined, //申请开始时间
          applyEndTime: undefined, //申请结束时间
          status: undefined, // 状态
          compName: undefined,
          isApprove: true
        },
        tableLoading: false,
        tableData: [],
        tableOption: tableOption,
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 20 // 每页显示多少条
        },
        replyForm: {
          applyId: undefined, //申请单号
          reason: undefined, //拒绝原因
        },
        replyFormVisible: false, //是否弹出拒绝弹窗
        setLoading: false,
        replyRules: {
          reason: [{ required: true, message: '请输入拒绝原因', trigger: 'change' }]
        },
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
          if(this.snapTime.length === 2) {
            this.searchForm.applyStartTime = this.snapTime[0]
            this.searchForm.applyEndTime = this.snapTime[1]
          }
        } else {
          this.searchForm.applyStartTime = undefined
          this.searchForm.applyEndTime = undefined
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
      searchSubmit() {
        //搜索
        this.page.currentPage = 1
        this.getList(this.page, this.searchForm)
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
          isApprove: true
        }
        this.getList(this.page, this.searchForm)
      },
      handleCheckDetail(row) {
        this.$router.push({
          path: `/platform/outsourcing/detail/${row.applyId}`,
          query: {
            queryPage: this.page,
            queryForm: this.searchForm,
            isApprove: true
          }
        });
      },
      async pass(applyId) {
        await this.$confirm('确认审批通过', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        passOrRefuse(
          Object.assign(
            {
              applyId: applyId,
              status: 1
            }
          )
        ).then((response) => {
          if (response.data.code === 0) {
            this.$message({
              showClose: true,
              message: '审批成功',
              type: 'success'
            })
          } else {
            this.$message({
              message: response.data.message,
              type: 'error'
            })
          }
          this.resetFrom('searchForm')
        }).catch(error => { console.error(error) })
      },
      resetReplyFormData(formName) {
        this.replyFormVisible = false
        this.setLoading = false
        this.$refs[formName] ? this.$refs[formName].resetFields() : ''
        this.$refs[formName] ? this.$refs[formName].clearValidate() : ''
      },
      handleRefuseData(row) {
        this.replyFormVisible = true
        this.replyForm.applyId = row.applyId
      },
      refuse(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.setLoading = true
            passOrRefuse(
              Object.assign(
                {
                  applyId: this.replyForm.applyId,
                  status: 2,
                  reason: this.replyForm.reason
                }
              )
            ).then((response) => {
              if (response.data.code === 0) {
                this.$message({
                  showClose: true,
                  message: '审批成功',
                  type: 'success'
                })
              } else {
                this.$message({
                  message: response.data.message,
                  type: 'error'
                })
              }
              this.setLoading = false
              this.resetReplyFormData(formName)
              this.resetFrom('searchForm')
            }).catch(() => {
              this.setLoading = false
            })
          } else {
            return false
          }
        })
      }
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
