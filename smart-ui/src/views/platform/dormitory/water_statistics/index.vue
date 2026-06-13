<!--水电用量排行统计  -->
<template>
  <div class="my-basic-container room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchform)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchform')" plain>重置</el-button>
          </div>
        </div>
        <el-form ref="searchform" :inline="true" :model="searchform" size="mini" class="topForm">
          <el-form-item label="园区" prop="dormitoryId">
            <dormSelect @doChange="doDormChange" :parkId="searchform.parkId" v-model="searchform.dormitoryId"></dormSelect>
          </el-form-item>
          <el-form-item label="选择楼栋" prop="dormitoryId">
            <dormSelect @doChange="doDormChange" :parkId="searchform.parkId" v-model="searchform.dormitoryId"></dormSelect>
          </el-form-item>
          <el-form-item label="" prop="dormitoryId">
            <el-form-item label="统计时间" prop="dormitoryId">
              <el-select v-model="dateStyle" placeholder="请选择" @change="changeDateStyle" style="width:30%">
                <el-option label="按月" value="0"></el-option>
                <el-option label="按日" value="1"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="" prop="dormitoryId">
              <el-form-item label="" prop="dormitoryId">
                <el-date-picker
                  v-model="dateList[0]"
                  :type="dateType"
                  @change="changeDate"
                  :placeholder="datePlaceholder0">
                </el-date-picker>
              </el-form-item>
              <el-form-item label="" prop="dormitoryId">
                <el-date-picker
                  v-model="dateList[1]"
                  :type="dateType"
                  @change="changeDate"
                  :placeholder="datePlaceholder1">
                </el-date-picker>
              </el-form-item>
            </el-form-item>
          </el-form-item>
          <el-form-item label="统计类型" prop="dormitoryId">
              <el-select v-model="dateStyle" placeholder="统计类型" @change="changeStyle" style="width:30%">
                <el-option label="用水" value="0"></el-option>
                <el-option label="用电" value="1"></el-option>
              </el-select>
          </el-form-item>
        </el-form>
        <div class="top-right">
          <el-button type="primary" icon="icon-yutong-download" @click="exportExcel">导出excel</el-button>
        </div>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange" @selection-change="selectChange">
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchStatementList,
} from '@/api/platform/dormitory/sd_meterread'
import { queryRoomStatementDetail } from '@/api/platform/dormitory/room_sdstatement'
import { tableOption } from '@/const/crud/platform/dormitory/water_statistics'

//抄表状态 0：未抄表	1：已抄表
const isMeterStatus = [
  { label: '未抄表', value: 0 },
  { label: '已抄表', value: 1 }
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
    var validateIsNum = (rule, value, callback) => {
      let regName = /^-?\d+$/
      if (value === 0) {
        callback(new Error('请输入非0整数'))
      }
      if (!regName.test(value)) {
        callback(new Error('请输入整数'))
      } else {
        callback()
      }
    }
    return {
      dateType: 'month',
      datePlaceholder0: '开始月份',
      datePlaceholder1: '结束月份',
      dateList: [],
      dateStyle: '0',
      statementDetailData: {
        statementMonth: '',
        categoryDataList: [], //公司结算
        staffStatmentDataList: [] //个人结算
      },
      setLoading: false,
      isRead: true,
      addLoading: false, //是否正在添加
      editLoading: false, //是否正在编辑
      addFormVisible: false, //添加楼层
      statementDetailVisible: false, //房间结算明细弹窗
      isMeterStatusArr: isMeterStatus,
      searchform: {
        parkId: undefined, //园区ID
        dormitoryId: undefined, //楼栋ID
        floorId: undefined, //楼层ID
        roomName: undefined, //房间号
        meterMonth: undefined, //月份
        status: undefined //状态
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
    this.getList(this.page)
  },
  mounted: function () {},
  computed: {},
  methods: {
    goAnchor(selector) {
      let anchor = this.$refs[selector]
      this.$refs.mdialog.$el.scrollTop = this.$refs[selector].offsetTop
    },
    doParkChange(e) {
      this.searchform.dormitoryId = ''
      this.searchform.floorId = ''
    },
    doDormChange(e) {
      this.searchform.floorId = ''
    },
    getType(type) {
      const obj = ['', '热水', '冷水', '电']
      return obj[type]
    },
    getList(page, params) {
      this.tableLoading = true
      fetchStatementList(
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
      this.getList(this.page, this.searchform)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.searchform)
    },
    queryStatementDetail(row) {
      this.statementDetailVisible = true
      queryRoomStatementDetail(row.id).then((response) => {
        this.statementDetailData = response.data.data
      })
    },
    searchSubmit(form) {
      //搜索
      this.page.currentPage = 1
      this.getList(this.page, form)
    },
    resetFrom(formName) {
      //清空
      this.$refs[formName].resetFields()
      this.dormitoryData = []
      this.page.currentPage = 1
      this.getList(this.page)
    },
    closeStatementDetail() {
      //关闭明细弹窗
      this.statementDetailVisible = false
    },
    selectChange(val) {
      //列表左侧选择框事件（包括全选和取消全选）
    },
    ignoreAlarm() {
      //忽略报警
    },
    processed() {
      //已处理
    },
    changeDateStyle(value) {
      this.dateList = []
      this.dateStyle = value
      if (value == 0) {
        this.dateType = 'month'
        this.datePlaceholder0 = '开始月份'
        this.datePlaceholder1 = '结束月份'
      }else{
        this.dateType = 'date'
        this.datePlaceholder0 = '开始日期'
        this.datePlaceholder1 = '结束日期'
      }
    },
    changeStyle(){
    },
    changeDate (e) {
      if (this.dateList.length != 1) {
        var date0 = this.dateList[0]
        var date1 = this.dateList[1]
        if (date0.getTime() > date1.getTime()) {
          this.dateList.reverse()
        }
      }
    },
    exportExcel () {
      ///导出
    }
  }
}
</script>
<style lang="scss" scoped>
.config_form ::v-deep {
  .a1,.a2{
    font-size: 16px;
    color: #333;
  }
  .a1{
    margin-top: 10px;;
  }
  .a2{
    margin-top: 30px;;
  }
  .fb1{
    color: red;
    font-size: 14px;
    font-weight: bold;
  }
  .nav-btn{
    position: fixed;
    top: 35%;
    right: 6%;
    background: #fff;
    width: 100px;
    text-align: center;
    padding: 10px 0;
    box-shadow: -2px 0 4px rgba(0, 0, 0, 0.35);
    .el-button{
      width: 100%;
    }
    >p{
      margin-bottom: 5px;
    }
  }
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
    padding-right: 60px;
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
    .tdTitle{
      font-weight: bold;
      font-size: 16px;
      color: #333;
      background: #eef1f6;
    }
    .tr2Title{
      background: #eef1f6;
    }
    td {
      border: 1px solid #e0e0e0;
      padding: 10px;
      text-align: center;
    }
    td:first-child {
      width: 100px;
    }
    .tdL {
      padding: 10px;
    }
    .el-button {
      font-size: 12px;
    }
  }
}
</style>
