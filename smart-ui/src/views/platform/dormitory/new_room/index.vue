<!--宿舍管理 -->
<template>
  <div class="my-basic-container newPage" :class="{'padCss': isPad}">
    <el-scrollbar class="my-scrollbar" :native="false">
      <div style="padding-right: 10px;padding-bottom: 40px">
        <!-- 图表 -->
        <section class="s1">
          <div class="s1_l bgFff">
            <chart1 :dataObj="countList" ref="chart1"></chart1>
          </div>
          <div class="s1_r bgFff">
            <chart2 :dataObj="countBuilding" ref="chart2"></chart2>
          </div>
        </section>
        <!-- 筛选条件 -->
        <section class="s2 bgFff">
          <div class="top-menu" v-if="!isPad">
            <div class="top-left">筛选条件</div>
            <div class="top-right">
              <!-- <template v-if="!isPad"> -->
                <el-button type="primary" @click="handleAmmeter()" plain><i class="i_icon i1"></i>公摊抄表</el-button>
                <el-button type="primary" @click="handleWaterBatch(3)" plain><i class="i_icon i2"></i>批量抄电</el-button>
                <el-button type="primary" @click="handleWaterBatch(1)" plain><i class="i_icon i2"></i>批量抄热水</el-button>
                <el-button type="primary" @click="handleWaterBatch(2)" plain><i class="i_icon i2"></i>批量抄冷水</el-button>
              <!-- </template> -->
              <el-button type="primary" icon="el-icon-search" @click="handleSearch('searchform')">查询</el-button>
            </div>
          </div>
          <el-form ref="searchform" :inline="true" :model="searchform" size="mini" class="topForm">
            <el-form-item label="园区选择" prop="parkId">
              <parkSelect
                :defaultSelected="true"
                @defaultHandle="parkDefault"
                @doChange="doParkChange"
                v-model="searchform.parkId"
              ></parkSelect>
            </el-form-item>
            <el-form-item label="楼栋选择" prop="dormitoryId">
              <dormSelect
                @doChange="doDormChange"
                :parkId="searchform.parkId"
                v-model="searchform.dormitoryId"
              ></dormSelect>
            </el-form-item>
            <el-form-item label="楼层选择" prop="floorId">
              <floorSelect
                :parkId="searchform.parkId"
                :dormitoryId="searchform.dormitoryId"
                v-model="searchform.floorId"
              ></floorSelect>
            </el-form-item>
            <el-form-item label="房间类型" prop="roomType">
              <roomTypeSelect
                :parkId="searchform.parkId"
                v-model="searchform.roomType"
              ></roomTypeSelect>
            </el-form-item>
            <el-form-item label="" v-if="isPad">
              <el-button type="primary" icon="el-icon-search" @click="handleSearch('searchform')">查询</el-button>
            </el-form-item>
            <template v-if="!isPad">
              <br/>
              <el-form-item label="入住状态" prop="inStatus">
                <el-radio-group v-model="searchform.inStatus" class="mG">
                  <el-radio v-for="(item, index) in inStates" :label="index+1" :key="index">{{item}}</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="可住性别" prop="sex">
                <el-radio-group v-model="searchform.sex" class="mG">
                  <el-radio v-for="(item, index) in roomSexes" :label="index" :key="index">{{item}}</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="入住状态" prop="isNormal">
                <el-checkbox v-model="isNormal2" class="mG" :label="2">仅查看入住状态异常房间</el-checkbox>
              </el-form-item>
              <el-form-item label="" class="tp-item">
                <div>
                  <span class="tp tp1"><i></i>在职</span>
                  <span class="tp tp2"><i></i>离职未退宿</span>
                  <span class="tp tp3"><i></i>未入职</span>
                </div>
              </el-form-item>
            </template>
          </el-form>
        </section>
        <!-- 宿舍 -->
        <section class="s3">
          <template v-for="(item, index) in rooms">
            <div class="s3_c bgFff" :key="index">
              <div class="s3_c_top">
                <div class="s3ct_c">
                  <div class="s3ct_t">{{item.roomName}}—{{item.roomTypeName || '无'}}</div>
                  <template v-if="isPad">
                    <el-button v-if="item.bedDetailList&&item.bedDetailList.length>0" type="text" @click.native="handleWaterDorm(item, index)">水电抄表</el-button>
                    <template v-else><div style="font-size: 12px; color: #999">当前未设置房间类型</div></template>
                  </template>
                  <template v-else>
                    <el-dropdown placement="bottom" v-if="item.bedDetailList&&item.bedDetailList.length>0">
                      <span class="el-dropdown-link">
                        <i class="el-icon-more"></i>
                      </span>
                      <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item @click.native="handleChangeDorm(item)">退换宿舍</el-dropdown-item>
                        <el-dropdown-item @click.native="handleWaterDorm(item, index)">水电抄表</el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                    <template v-else><div style="font-size: 12px; color: #999">当前未设置房间类型</div></template>
                  </template>
                </div>
              </div>
              <div class="s3_c_cont">
                <template v-for="(bitem, bindex) in item.bedDetailList">
                  <div class="s3cc_q" :class="bitem.inStatus|bedStatus" :key="bindex"></div>
                </template>
              </div>
              <div class="s3_c_num">
                {{item.sex|fl_roomSex}} {{item.actCount}}/{{item.bedTotal}}
              </div>
            </div>
          </template>
          <template v-if="rooms&&rooms.length==0">
            <div class="tips">请输入条件进行查询！</div>
          </template>
        </section>
        <div class="page-outer">
          <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="page.currentPage"
            :page-sizes="[8, 16, 24, 32, 40]"
            :page-size="page.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="page.total"
          ></el-pagination>
        </div>
      </div>
      <dlgWaterDorm :isPad="isPad" :visible="dlg1Visible" :rows="rooms" :curItem="curDlg1Obj" :curIndex="curDlg1ObjIndex" @dlgdo="dlg1do" @dlgdoSuccess="dlg1doSuccess"/>
      <!-- <dlgAmmeterBatch :visible="dlg3Visible" :row="curDlg3Obj" @dlgdo="dlg3do" /> -->
      <dlgWaterBatch :visible="dlg2Visible" @dlgdo="dlg2do" :itemType="dlg2ItemType"/>
      <dlgChangeCheckOutDorm :visible="dlg4Visible" :row="curDlg4Obj" @dlgdo="dlg4do" @dlgChangeDorm="changeDorm" @dlgdoSuccess="dlg4doSuccess"/>
      <dlgDormChange :visible="dlg5Visible" :row="curDlg5Obj" @dlgdo="dlg5do" @dlgdoSuccess="dlg5doSuccess"/>
    </el-scrollbar>
  </div>
</template>
<script>
import {
  countList,
  countBuilding
} from "@/api/platform/dormitory/room";
import { fetchList } from "./_service.js";
import {validatenull} from '@/util/validate'
import chart1 from "./compnents/chart1";
import chart2 from "./compnents/chart2";
import dlgWaterDorm from "./compnents/dlg_water_dorm";
// import dlgAmmeterBatch from "./compnents/dlg_ammeter_batch";
import dlgWaterBatch from "./compnents/dlg_water_batch";
import dlgChangeCheckOutDorm from "./compnents/dlg_change_check_out_dorm";
import dlgDormChange from "../ks_checkIn/compnents/dlg_dorm_change";

const INSTATE_OPTION = ['未满','已满','空房']
const ROOMSEX_OPTION = ['男','女','夫妻/家属','其他']
export default {
  components:{
    chart1,
    chart2,
    dlgWaterDorm,
    dlgWaterBatch,
    dlgChangeCheckOutDorm,
    dlgDormChange
  },
  data() {
    return {
      isPad: false,
      dlg1Visible: false, //水电抄表
      curDlg1Obj: {},
      curDlg1ObjIndex: undefined,
      dlg2Visible: false, //批量抄水
      dlg2ItemType: 1, // 1热水， 2冷水
      dlg3Visible: false, //批量抄电
      curDlg3Obj: {},
      dlg4Visible: false, //退换宿舍
      curDlg4Obj: {},
      dlg5Visible: false, //更换宿舍
      curDlg5Obj: {},
      inStates: INSTATE_OPTION, //入住状态
      roomSexes: ROOMSEX_OPTION, //可住性别
      rooms: [], //查询的房间
      searchform: {
        parkId: '',
        dormitoryId: '',
        floorId: '',
        roomType: ''
      },
      isNormal2:[],
      countList: {}, //园区统计信息
      countBuilding: [], //各楼层统计信息
      defaultPark: null, //默认选中的园区 {label,value}
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 8 // 每页显示多少
      }
    };
  },
  created() {
    this.isPad = this.$route.meta.isPad
  },
  mounted: function() {},
  filters: {
    fl_roomSex: function(val) {
      if(validatenull(val)){
        return
      }
      return  ROOMSEX_OPTION[val]
    },
    bedStatus: function(val) {
      //val 1-在职， 2-离职未退宿， 3-未入职
      //bed1: 在职
      //bed2: 离职未退宿
      //bed3: 未入职
      //bed4: 空床位
      if(val==1){
        return 'bed1'
      }else if(val==2){
        return 'bed2'
      }else if(val==3){
        return 'bed3'
      }else{
        return 'bed4'
      }
    }
  },
  methods: {
    handleSizeChange(val) {
      this.page.pageSize = val;
      this.handleSearch()
    },
    handleCurrentChange(val) {
      this.page.currentPage = val;
      this.handleSearch()
    },
    parkDefault(e){
      this.defaultPark = e
      this.searchform.parkId = e.value
      this.getCountList(this.defaultPark.value)
      this.getCountBuilding(this.defaultPark.value)
      this.handleSearch()
    },
    //获取园区总统计
    getCountList(parkId) {
      this.countList = {};
      countList({ parkId: parkId }).then(response => {
        if (response.data.data.length > 0) {
          this.countList = response.data.data[0];
          this.$nextTick(() => {
            this.$refs.chart1.updateChart();
          });
        } else {
          this.countList = {};
        }
      });
    },
    //获取各楼栋统计
    getCountBuilding(parkId) {
      this.countBuilding = [];
      countBuilding({ parkId: parkId }).then(res => {
        if (res.data.data.length > 0) {
          this.hasBuilding = true;

          this.countBuilding = res.data.data;
          // this.$nextTick(() => {
          //   this.$refs.chart2.updateChart();
          // });
        } else {
          this.hasBuilding = false;
          this.countBuilding = [];
        }
      });
    },
    doParkChange(e){
      this.searchform.dormitoryId = ''
      this.searchform.floorId = ''
      this.searchform.roomType = ''

      this.getCountList(e)
      this.getCountBuilding(e)
    },
    doDormChange(e){
      this.searchform.floorId = ''
    },
    dlg1do(val){
      this.dlg1Visible = val
    },
    dlg2do(val){
      this.dlg2Visible = val
    },
    dlg3do(val){
      this.dlg3Visible = val
    },
    dlg4do(val){
      this.dlg4Visible = val
    },
    dlg5do(val){
      this.dlg5Visible = val
    },
    /**
     * 更换宿舍
     */
    changeDorm(val){
      this.dlg5Visible = true
      this.curDlg5Obj = val
    },
    dlg1doSuccess(val){
      this.dlg1Visible = false
      this.handleSearch()
    },
    dlg5doSuccess(val){
      this.dlg4Visible = false
      this.handleSearch()
      // this.curDlg5Obj = null
    },
    dlg4doSuccess(){
      this.dlg4Visible = false
      this.handleSearch()
      // this.curDlg5Obj = null
    },
    /**
     * 公摊抄表
     */
    handleAmmeter(){
      //切换可视化页面
      const src = `/platform/dormitory/new_room/sd_public`;
      this.$router.push({
        path: src,
        query: {}
      });
    },
    /**
     * 批量抄表
     */
    handleAmmeterBatch(){
      this.dlg3Visible = true
    },
    /**
     * 批量抄水
     */
    handleWaterBatch(e){
      this.dlg2ItemType = e
      this.dlg2Visible = true
    },
    /**
     * 查询
     */
    async handleSearch(){
      if(this.isNormal2&&this.isNormal2.length>0){
        this.searchform.isNormal = this.isNormal2[0] //异常
      }else{
        this.searchform.isNormal = null //不设置，正常和异常都有，1是正常
      }
      let obj = Object.assign(
        {
          current: this.page.currentPage,
          size: this.page.pageSize
        },
        this.searchform
      )
      const res = await fetchList(obj)
      this.rooms = res.data.data.records
      this.page.total = res.data.data.total;
    },
    /**
     * 退换宿舍
     */
    handleChangeDorm(row){
      this.dlg4Visible = true
      this.curDlg4Obj = row
    },
    /**
     * 宿舍-水电抄表--打开
     */
    handleWaterDorm(item, index){
      this.dlg1Visible = true
      this.curDlg1Obj = item
      this.curDlg1ObjIndex = index
    }
  }
};
</script>
<style lang="scss" scoped>
  .theme-yutong .topForm ::v-deep .el-form-item__content {
    width: 235px;
  }
  .newPage ::v-deep {
    padding: 0 0 10px 10px;
    $c1: #0dbc82;
    $c2: #e7292e;
    $c3: #678fd1;
    $c4: #d9e0e4;
    .bgFff{
      background-color: #fff;
    }
    .tips{
      text-align: center;
      color: #999;
      padding: 100px 0;
      width: 100%;
      background: #fff;
    }
    .i_icon{
      display: inline-block;
      vertical-align: middle;
      margin-right: 5px;
      margin-top: -2px;
    }
    .i1{
      width: 14px;
      height: 14px;
      background-image: url('/img/dorm/gtcb_i.png');
    }
    .i2{
      width: 12px;
      height: 15px;
      background-image: url('/img/dorm/plcd_i.png');
    }
    .top-menu{
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 10px;
      .top-left{
        font-size: 18px;
        font-weight: bold;
      }
      .top-right{
        position: static;
      }
      .el-button.is-plain:hover {
        background-color: #FDF6EC;
        border-color: #fa7b0c;
        color: #fa7b0c;
      }
    }
    .tp-item{
      .el-form-item__content{
        padding-left: 10px;
        width: 100%;
      }
    }
    .tp{
      margin-right: 20px;
      font-size: 16px;
      font-weight: bold;
      i{
        width: 6px;
        height: 6px;
        display: inline-block;
        border-radius: 50%;
        vertical-align: middle;
        margin-right: 3px;
      }
    }
    .tp1{
      color: $c1;
      i{
        background-color: $c1;
      }
    }
    .tp2{
      color: $c2;
      i{
        background-color: $c2;
      }
    }
    .tp3{
      color: $c3;
      i{
        background-color: $c3;
      }
    }
    .mG{
      .el-radio+.el-radio{
        margin-left: 12px;
      }
      .el-radio__label{
        padding-left: 5px;
        font-weight: normal;
      }
    }
    .s1{
      height: 200px;
      display: flex;
      margin-bottom: 10px;
      .s1_l{
        width: 500px;
        margin-right: 10px;
      }
      .s1_r{
        flex: 1;
        overflow: hidden;
      }
    }
    .s2{
      margin-bottom: 10px;
    }
    .s3{
      display: flex;
      flex-wrap: wrap;
      padding-bottom: 30px;
      &_c{
        width: 380px;
        height: 215px;
        // padding: 0 20px;
        margin: 0 10px 10px 0;
        display: flex;
        flex-direction: column;
        &_top{
          padding: 0 20px;
          height: 50px;
          .s3ct_c{
            display: flex;
            justify-content: space-between;
            align-items: center;
            height: 100%;
            border-bottom: 1px solid #eee;
            .s3ct_t{
              font-size: 16px;
              font-weight: bold;
            }
          }
        }
        &_cont{
          flex: 1;
          display: flex;
          justify-content: center;
          align-items: center;
          flex-wrap: wrap;
          padding-top: 15px;
          overflow: hidden;
          .s3cc_q{
            width: 60px;
            height: 40px;
            margin: 0 5px 15px 5px;
            background-size: 100% 100%;
            background-repeat: no-repeat;
            background-image: url('/img/dorm/bed1.png');
          }
          .bed1{
            background-image: url('/img/dorm/bed1.png');
          }
          .bed2{
            background-image: url('/img/dorm/bed2.png');
          }
          .bed3{
            background-image: url('/img/dorm/bed3.png');
          }
          .bed4{
            background-image: url('/img/dorm/bed4.png');
          }
        }
        &_num{
          text-align: center;
          padding-top: 5px;
          height: 40px;
          color: #666;
        }
      }
    }
  }
  .padCss{
    padding: 20px 20px 0 20px;
    .s1{
      display: none;
    }
    .s3_c{
      width: 48%;
      margin: 0px 8px 10px 8px;
      box-shadow: 2px 2px 5px #999;
    }
  }
</style>
