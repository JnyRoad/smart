<!--宿舍水电结算模板  -->
<template>
  <div class="my-basic-container room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchform)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchform')" plain>重置</el-button>
            <!-- <el-button type="primary" icon="icon-yutong-download">导出水电扣费报表</el-button> -->
          </div>
        </div>
        <el-form ref="searchform" :inline="true" :model="searchform" size="mini" class="topForm">
          <el-form-item label="园区" prop="parkId">
            <parkSelect @doChange="doParkChange" v-model="searchform.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="楼栋" prop="dormitoryId">
            <dormSelect @doChange="doDormChange" :parkId="searchform.parkId" v-model="searchform.dormitoryId"></dormSelect>
          </el-form-item>
          <el-form-item label="楼层" prop="floorId">
            <floorSelect :parkId="searchform.parkId" :dormitoryId="searchform.dormitoryId" v-model="searchform.floorId"></floorSelect>
          </el-form-item>
          <el-form-item label="房间号" prop="roomName">
            <el-input v-model="searchform.roomName" clearable></el-input>
          </el-form-item>
          <el-form-item label="抄表月份" prop="meterMonth">
            <el-date-picker v-model="searchform.meterMonth" type="month" value-format="yyyy-MM" placeholder="选择月份"></el-date-picker>
          </el-form-item>
          <!-- <el-form-item label="状态" prop="status">
            <el-select v-model="searchform.status" placeholder="请选择">
              <el-option v-for="item in isMeterStatusArr" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item> -->
        </el-form>
        <!-- <el-button type="primary" @click="sureGenerateDetail">同步到EHR</el-button> -->
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-view" @click="queryStatementDetail(scope.row)">查看明细</el-button>
          </template>
          <template slot-scope="scope" slot="statementStatus">
            <span>{{ scope.row.statementStatus === 1 ? '已生成' : '已同步' }}</span>
          </template>
        </avue-crud>

        <!-- 房间结算明细弹窗 -->
        <el-dialog title="结算明细" ref="mdialog"  class="dialog_form config_form" width="90%" @close="closeStatementDetail" :visible.sync="statementDetailVisible">
          <el-scrollbar class="my-scrollbar" :native="false">
            <section class="my-basic-inner config-inner">
              <el-form :inline="true" size="mini" :disabled="isRead">
                <div>抄表月份：{{ statementDetailData.statementMonth }}</div>
                <div id="#a1" ref="a1" class="a1">
                  <p class="box-orange">房间结算</p>
                </div>
                <template v-for="(details, index) in statementDetailData.categoryDataList">
                  <table class="ruleTbl" :key="index">
                    <tr>
                      <td :colspan="details.meterType==1?9:9" class="tdTitle">
                        <!-- meterType 1普通 2公摊 -->
                        <template v-if="details.meterType==2">公摊</template>
                        {{ getType(details.categoryId) }}
                      </td>
                    </tr>
                    <tr class="tr2">
                      <td class="tdL">上月底数</td>
                      <td class="tdL">本月底数</td>
                      <td class="tdL">实际用量</td>
                      <template v-if="details.meterType==1">
                        <td class="tdL">总配额</td>
                        <td class="tdL">超出用量</td>
                        <td class="tdL">超出单价</td>
                      </template>
                      <template v-if="details.meterType==2">
                        <td class="tdL">用量单价</td>
                      </template>
                      <td class="tdL">抄表开始时间</td>
                      <td class="tdL">抄表结束时间</td>
                      <td class="tdL">费用小计</td>
                      <template v-if="details.meterType==2">
                        <td class="tdL">分摊房间数</td>
                        <td class="tdL">房间均摊费用</td>
                      </template>
                    </tr>
                    <tr class="tr2">
                      <td class="tdL">{{ details.preMonthNum }}</td>
                      <td class="tdL">{{ details.curMonthNum }}</td>
                      <td class="tdL">{{ details.actualQty }}</td>
                      <template v-if="details.meterType==1">
                        <td class="tdL">{{ details.totalQty }}</td>
                        <td class="tdL">{{ details.overQty }}</td>
                        <td class="tdL">{{ details.overFee }}</td>
                      </template>
                      <template v-if="details.meterType==2">
                        <td class="tdL">{{ details.overFee }}</td>
                      </template>
                      <td class="tdL">{{ details.meterStartTime | dateFormat2 }}</td>
                      <td class="tdL">{{ details.meterEndTime | dateFormat2 }}</td>
                      <td class="tdL"> <span class="fb1">{{ details.totalFee }}</span></td>
                      <template v-if="details.meterType==2">
                        <td class="tdL"> <span class="fb1">{{ details.roomNum }}</span></td>
                        <td class="tdL"> <span class="fb1">{{ details.roomAvgFee }}</span></td>
                      </template>
                    </tr>
                  </table>
                </template>
                <div id="#a2" ref="a2" class="a2">
                  <p class="box-orange">个人结算
                    <span class="c-main" style="font-size: 12px;padding-left: 30px;">说明：结算费用=（各项费用小计/入住总天数）*个人结算天数</span>
                  </p>
                </div>
                <template v-for="(item, index) in statementDetailData.staffStatmentDataList">
                  <table class="ruleTbl" :key="index">
                    <tr class="tr2 tr2Title">
                      <td class="tdL">工号</td>
                      <td class="tdL">姓名</td>
                      <td class="tdL">入住日期</td>
                      <td class="tdL">结算项目</td>
                      <td class="tdL">当月统计开始时间</td>
                      <td class="tdL">当月统计结束时间</td>
                      <td class="tdL">入住总天数</td>
                      <td class="tdL">个人修正天数</td>
                      <td class="tdL">个人结算天数</td>
                      <td class="tdL">日均摊费用</td>
                      <td class="tdL">个人结算费用</td>
                      <td class="tdL">个人当月分摊总费用</td>
                    </tr>
                    <template v-for="(item2, index2) in item.staffCategoryInfo">
                      <tr class="tr2" :key="'d'+index2">
                        <template v-if="index2==0">
                          <td class="tdL" :rowspan="item.staffCategoryInfo.length">{{item.staffBadge}}</td>
                          <td class="tdL" :rowspan="item.staffCategoryInfo.length">{{item.staffName}}</td>
                          <td class="tdL" :rowspan="item.staffCategoryInfo.length">{{item.inTime | dateFormat2}}</td>
                        </template>
                        <td class="tdL">
                          <!-- meterType 1普通 2公摊 -->
                          <template v-if="item2.meterType==2">公摊</template>
                          {{getType(item2.categoryId)}}
                        </td>
                        <td class="tdL">{{ item2.statiStartTime | dateFormat2 }}</td>
                        <td class="tdL">{{ item2.statiEndTime | dateFormat2 }}</td>
                        <td class="tdL">{{item2.inTotalDays}}</td>
                        <td class="tdL">{{item2.reviseDays}}</td>
                        <td class="tdL">{{item2.statementDays}}</td>
                        <td class="tdL"><span class="fb1">{{item2.avgFee}}</span></td>
                        <td class="tdL"><span class="fb1">{{item2.statementFee}}</span></td>
                        <template v-if="index2==0">
                          <td class="tdL" :rowspan="item.staffCategoryInfo.length"><span class="fb1">{{item.fee}}</span></td>
                        </template>
                      </tr>
                    </template>
                  </table>
                </template>
              </el-form>
            </section>
          </el-scrollbar>
          <div class="nav-btn">
            <p><el-button type="text" @click="goAnchor('a1')">房间结算</el-button></p>
            <p><el-button type="text" @click="goAnchor('a2')">个人结算</el-button></p>
          </div>
          <div slot="footer" class="dialog-footer">
            <template>
              <el-button type="primary" @click="closeStatementDetail">关 闭</el-button>
            </template>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchStatementList,
  addRecord,
  delTemplate,
  updateDormitorySDTemplate,
  addRoomSdData,
  getRoomMeterReadDetail
} from '@/api/platform/dormitory/sd_meterread'
import { queryRoomStatementDetail } from '@/api/platform/dormitory/room_sdstatement'
import { tableOption } from '@/const/crud/platform/dormitory/room_sdstatement'
import { validatenum } from '@/util/validate'
import { mapGetters } from 'vuex'

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
