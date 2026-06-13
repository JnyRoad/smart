<!--宿舍报修模板  -->
<template>
  <div class="my-basic-container room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>重置</el-button>
            <el-button type="primary" :loading="exportLoading" @click="export2Excel" icon="icon-yutong-download">导出excel</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini" class="topForm">
          <el-form-item label="维修区域" prop="rangeType">
            <el-select v-model="searchForm.rangeType" placeholder="请选择">
              <el-option v-for="item in rangeTypeArr" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="维修类别" prop="repairType">
            <el-select v-model="searchForm.repairType" placeholder="请选择">
              <el-option v-for="item in repairTypeArr" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
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
            <el-button type="text" icon="el-icon-view" v-if="scope.row.status === 1" @click="handleConfirmData(scope.row)">回复</el-button>
            <el-button type="text" icon="el-icon-view" v-if="scope.row.status === 2" @click="handleRepairData(scope.row)">回复</el-button>
          </template>
        </avue-crud>
        <!-- 生成房间水电记录弹框 -->
        <el-dialog title="添加房间水电记录" class="dialog_form" width="550px" @close="resetAddForm('addForm')" :visible.sync="addFormVisible">
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="resetAddForm('addForm')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">保 存</el-button>
          </div>
        </el-dialog>

        <!--待确认回复模板 -->
        <el-dialog title="待确认" class="dialog_form config_form" width="600px" @close="resetReplyFormData('replyForm')" :visible.sync="replyFormVisible">
          <span>请前往OA系统提交相应的维修单，待维修完毕后可将维修结果填入到后台界面中</span>
          <el-form :rules="replyRules" ref="replyForm" :model="replyForm" label-width="150px">
            <el-form-item label="回复类型" prop="status">
              <el-radio v-model="replyForm.status" v-if="replyType === 1" label="2">已安排人员维修</el-radio><br />
              <el-radio v-model="replyForm.status" v-if="replyType === 2" label="3">维修完毕</el-radio><br />
              <el-radio v-model="replyForm.status" label="4">无法维修,关闭报修单</el-radio>
            </el-form-item>
            <el-form-item label="回复内容" prop="result">
              <el-input v-model="replyForm.result" maxlength="100" type="textarea" clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="resetReplyFormData('replyForm')" plain>取 消</el-button>
            <el-button type="primary" @click="replyDataSubmit('replyForm')" :loading="setLoading">保 存</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchListYuto, addRecord, replyRepair } from '@/api/platform/dormitory/dormitory_repairs'
import { tableOption } from '@/const/crud/platform/dormitory/dormitory_repairs_yuto'

//抄表状态 0：未抄表	1：已抄表
const repairTypeArr = [
  { label: '灯', value: 1 },
  { label: '插座', value: 2 },
  { label: '水龙头', value: 3 },
  { label: '水管', value: 4 },
  { label: '门窗', value: 5 },
  { label: '锁', value: 6 },
  { label: '空调', value: 7 },
  {label: '其他', value: 8},
  {label: '床', value: 9},
  {label: '柜子', value: 10},
  {label: '玻璃', value: 11},
  {label: '洗手台', value: 12},
  {label: '桌椅', value: 11},
  {label: '地漏', value: 12}
]
const rangeTypeArr = [
  { label: '宿舍', value: 1 },
  { label: '办公室', value: 2 },
  { label: '车间', value: 3 },
  { label: '园区周边', value: 4 }
]
const statusArr = [
  { label: '待审批', value: 0 },
  { label: '待确认', value: 1 },
  { label: '已安排维修', value: 2 },
  { label: '维修成功', value: 3 },
  { label: '已关闭', value: 4 },
  { label: '已通过', value: 5 },
  { label: '已拒绝', value: 6 },
]
let meterReadDetailList = []
for (let i = 1; i < 4; i++) {
  meterReadDetailList.push({
    preMonthNum: '',
    curMonthNum: '',
    categoryId: i
  })
}
export default {
  name: 'dorm_mng',
  data() {
    return {
      configform: {
        mrId: '',
        roomId: '',
        meterMonth: '',
        meterReadDetailList: meterReadDetailList
      },
      poupTitle: '', //弹出框标题
      isConfirmPoup: true, //是否为确认弹窗
      snapTime: [],
      repairTypeArr: repairTypeArr,
      rangeTypeArr: rangeTypeArr,
      statusArr: statusArr,
      setLoading: false,
      exportLoading: false,
      isRead: true,
      isStatement: false, //是否已生成明细
      addLoading: false, //是否正在添加
      editLoading: false, //是否正在编辑
      addFormVisible: false, //添加楼层
      replyFormVisible: false, //是否弹出回复弹窗
      replyType: 0, //回复类型
      replyForm: {
        id: undefined, //记录标识
        status: undefined, //回复状态
        result: undefined //回复描述
      },
      replyRules: {
        status: [{ required: true, message: '请选择回复类型', trigger: 'change' }]
      },
      searchForm: {
        rangeType: undefined, // 维修区域
        repairType: undefined, //维修类别
        beginTime: undefined, //申请开始时间
        endTime: undefined, //申请结束时间
        status: undefined, // 状态
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,

      dormitoryData: [],
      floorData: [],
      roomDate: [],

      addDormitoryData: [],
      addFloorData: [],
      addRoomDate: [],
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
    getType(type) {
      const obj = ['', '热水', '冷水', '电']
      return obj[type]
    },
    getList(page, params) {
      this.tableLoading = true

      if (this.snapTime.length == 2) {
        this.searchForm.beginTime = this.snapTime[0]
        this.searchForm.endTime = this.snapTime[1]
      }
      fetchListYuto(
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
    addSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.addLoading = true
          addRecord(this.addForm)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true) {
                this.addFormVisible = false
                this.addLoading = false
                this.getList(this.page, this.searchForm)
                this.$notify({
                  title: '成功',
                  message: msg,
                  type: 'success',
                  duration: 2000
                })
              } else if (dataResult === false) {
                this.addLoading = false
                this.$notify({
                  title: '失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
            })
            .catch(() => {
              this.addLoading = false
            })
        } else {
          return false
        }
      })
    },
    resetAddForm(formName) {
      this.addFormVisible = false
      this.addLoading = false
      this.hasStartNum = false //将起始编号状态恢复默认设置
      this.$refs[formName].resetFields()
    },
    resetEditForm(formName) {
      this.editFormVisible = false
      this.editLoading = false
      this.$refs[formName].clearValidate()
    },
    handleCheckDetail(row) {
      this.$router.push({
        path: `/platform/dormitory/repairs/detail/${row.id}`,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    handleConfirmData(row) {
      this.poupTitle = '待确认'
      this.replyFormVisible = true
      this.replyType = 1
      this.replyForm.id = row.id
    },
    handleRepairData(row) {
      this.poupTitle = '待维修'
      this.replyFormVisible = true
      this.replyType = 2
      this.replyForm.id = row.id
    },
    resetReplyFormData(formName) {
      this.replyFormVisible = false
      this.setLoading = false
      this.$refs[formName] ? this.$refs[formName].resetFields() : ''
      this.$refs[formName] ? this.$refs[formName].clearValidate() : ''
    },
    replyDataSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.editLoading = true
          replyRepair(this.replyForm)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true || dataResult === 1) {
                this.replyFormVisible = false
                this.editLoading = false
                this.getList(this.page, this.searchForm)
                this.$notify({
                  title: '成功',
                  message: msg,
                  type: 'success',
                  duration: 2000
                })
              } else if (dataResult === false) {
                this.editLoading = false
                this.$notify({
                  title: '失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
            })
            .catch(() => {
              this.editLoading = false
            })
        } else {
          return false
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
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "维修区域",
          "维修类别",
          "所在楼栋",
          "所在房间",
          "故障描述",
          "申请时间",
          "申请人",
          "状态"
        ];
        const filterVal = [
          "rangeTypeDesc",
          "repairTypeDesc",
          "dormitoryName",
          "roomName",
          "faultDesc",
          "createTime",
          "name",
          "statusDesc"
        ];
        fetchListYuto(
          Object.assign(
            {
              asc: 'id',
              current: this.page.currentPage,
              size: this.page.pageSize
            },
            this.searchForm
          )
        )
          .then(response => {
            const list = response.data.data.records;
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, `园区报修信息&(${this.snapTime})`);
            this.exportLoading = false;
          })
          .catch(err => {
            this.exportLoading = false;
          });
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
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
