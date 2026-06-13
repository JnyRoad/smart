<!--宿舍管理，入住信息  -->
<template>
  <div class="my-basic-container mycard2 sd_public">
    <el-scrollbar class="my-scrollbar" :native="false">
      <div class="box-outer box-left">
        <el-scrollbar class="my-lit-scrollbar" :native="false">
          <div class="cont-left">
            <div class="top-menu clear">
              公摊水电表
              <div class="top-right">
                <el-button type="primary" icon="el-icon-plus" @click="addSd">新增水电表</el-button>
              </div>
            </div>
            <div>
              <template v-for="(item, index) in commonsdData">
                <div class="sd_item" :key="index" @click="getDetail(item)">
                  <div class="sdi_t">
                    <div>{{item.sdName}}</div>
                    <div>{{item.dormitoryName}}</div>
                  </div>
                  <div class="sdi_b">
                    {{item.roomNameList}}
                  </div>
                </div>
              </template>
              <div class="info" v-if="!commonsdData||commonsdData.length==0">当前没有公摊水电表记录！</div>
            </div>
          </div>
        </el-scrollbar>
      </div>
      <div class="my-basic-inner">
        <div class="box-outer" v-if="!detailInfo">
          <div class="info">暂无信息！</div>
        </div>
        <div class="box-outer" v-else>
          <div class="top-menu clear">
            {{detailInfo.sdName}}
            <div class="top-right">
              <el-button type="primary" icon="el-icon-plus" @click="addCb">新增抄表</el-button>
              <el-button
                type="primary"
                icon="el-icon-edit"
                @click="editCommonsd(detailInfo)"
              >编辑</el-button>
              <el-button
                type="primary"
                icon="el-icon-delete"
                @click="delCommonsd(detailInfo)"
                plain
              >删除</el-button>
            </div>
          </div>
          <div class="sd_info">
            <div>楼栋宿舍：{{detailInfo.dormitoryName}}</div>
            <div>设备类型：{{detailInfo.categoryId | fl_getCategory}}</div>
            <div>公摊房间：{{detailInfo.roomNameList}}</div>
          </div>
          <avue-crud
            ref="staffTable"
            :page="page"
            :data="tableData"
            :option="tableOption"
            @size-change="sizeChange"
            @current-change="currentChange"
          >
            <!-- 上月表读数 -->
            <template slot-scope="scope" slot="preMonthNum">
              <span :class="{'isRevise':scope.row.sdCategory.isRevise==1}">{{scope.row.sdCategory.preMonthNum}}</span>
            </template>
            <!-- 当月表读数 -->
            <template slot-scope="scope" slot="curMonthNum">
              <span>{{scope.row.sdCategory.curMonthNum}}</span>
            </template>
            <!-- 月度用量 -->
            <template slot-scope="scope" slot="perMonth">
              <span>{{scope.row.sdCategory.preMonthNum | fl_getmNum(scope.row.sdCategory.curMonthNum)}}</span>
            </template>
            <template slot-scope="scope" slot="menu">
              <el-button
                type="text"
                icon="el-icon-edit"
                @click="handleEdit(scope.row,scope.$index)"
              >编辑</el-button>
              <el-button
                type="text"
                icon="el-icon-delete"
                @click="handleDel(scope.row,scope.$index)"
              >删除</el-button>
            </template>
          </avue-crud>
        </div>
      </div>
    </el-scrollbar>
    <dlgAmmeter :visible="dlg2Visible" :mTitle="dlg2Title" :commonId="this.commonId" :row="curDlg2Obj" @dlgdo="dlg2do" @dlgdoSuccess="dlgdo2Success"/>
    <dlgWaterDomPublic :visible="dlg1Visible" :mTitle="dlg1Title"  :commonId="this.commonId" :row="curDlg1Obj" @dlgdo="dlg1do" @dlgdoSuccess="dlgdo1Success"/>

  </div>
</template>
<script>
import { commonsd, commonsdDel, meterreadDel, meterreadList } from "./_service.js";
import { tableOption } from "@/const/crud/platform/dormitory/sd_public";
import dlgWaterDomPublic from "./compnents/dlg_water_dorm_public";
import dlgAmmeter from "./compnents/dlg_ammeter";
import {validatenull} from '@/util/validate'
import { floatNumMinus } from "@/util/util";


export default {
  name: "sd_public",
    components:{
      dlgAmmeter,
      dlgWaterDomPublic
    },
  data() {
    return {
      dlg1Visible: false, //新增抄表
      dlg1Title: '新增抄表',
      curDlg1Obj: {},
      dlg2Visible: false, //新增水电表
      curDlg2Obj: {},
      dlg2Title: '新增水电表',
      tableData: [],
      tableOption: tableOption,
      tableLoading: false,
      commonId: undefined,
      commonsdData: [],//水电表记录
      detailInfo: null, //当前水电表详情
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    };
  },
  created() {
    this.getCommonsd()
  },
  mounted: function() {},
  computed: {},
  watch: {},
  filters: {
    fl_getCategory: function(val) {
      if(validatenull(val)){
        return
      }
      const arr = ['热水','冷水','电']
      return  arr[val-1]
    },
    fl_getmNum: function(val, val2) {
      if(validatenull(val)||validatenull(val2)){
        return
      }
      return floatNumMinus(val2, val)
    }
  },
  methods: {
    dlg1do(val){
      this.dlg1Visible = val
    },
    dlgdo1Success(){
      this.getList(this.page)
    },
    dlg2do(val){
      this.dlg2Visible = val
    },
    dlgdo2Success(){
      this.getCommonsd()
    },
    //新增水电表
    addSd(){
      this.dlg2Visible = true
      this.dlg2Title = '新增水电表'
      this.curDlg2Obj = null
    },
    //编辑水电表
    editCommonsd(item){
      this.dlg2Visible = true
      this.dlg2Title = '编辑水电表'
      this.curDlg2Obj = item
    },
    //删除水电表
    delCommonsd(item){
      var _this = this
      this.$confirm('是否确认删除 "' + item.sdName + '"?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function () {
        return commonsdDel(item.id)
      }).then(() => {
        this.getCommonsd()
        this.detailInfo = null
        _this.$message({
          showClose: true,
          message: '删除成功',
          type: 'success'
        })
      }).catch(error => { console.error(error) })
    },
    async getCommonsd(){
      const res = await commonsd({
        current: 1,
        size: 100
      })
      this.commonsdData = res.data.data.records
    },
    getDetail(obj){
      this.detailInfo = obj
      this.getList(this.page)
    },
    //新增抄表
    addCb(){
      this.dlg1Visible = true
      this.dlg1Title = '新增抄表'
      this.commonId = this.detailInfo.id
    },
    //编辑抄表数据
    handleEdit(item){
      this.dlg1Visible = true
      this.dlg1Title = '编辑抄表'
      this.commonId = this.detailInfo.id
      this.curDlg1Obj = item
    },
    //抄表记录
    getList(page) {
      this.tableLoading = true;
      meterreadList(
        {
          current: page.currentPage,
          size: page.pageSize,
          commId: this.detailInfo.id
        }
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });

      this.tableLoading = false;
    },
    //删除抄表数据
    handleDel(row, index){
      var _this = this
      this.$confirm('是否确认删除此抄表数据吗?', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function () {
        return meterreadDel(row.sdCategory.mrId)
      }).then(() => {
        this.getList(this.page)
        _this.$message({
          showClose: true,
          message: '删除成功',
          type: 'success'
        })
      }).catch(error => { console.error(error) })
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page);
    },
  }
};
</script>

<style lang="scss" scoped>
  .sd_public{
    .isRevise{
      color: red;
    }
    .sd_item{
      border: 1px solid #e0e0e0;
      padding: 10px;
      margin-top: 20px;
      border-radius: 5px;
      cursor: pointer;
      &:hover{
        border-color: #ed6d00;
        background: #FDF6EC;
      }
      .sdi_t{
        display: flex;
        font-weight: bold;
        font-size: 16px;
        margin-bottom: 10px;
        >div{
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        >div:nth-child(2){
          text-align: right;
        }
      }
      .sdi_b{
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    .sd_info{
      padding-top: 10px;
      line-height: 30px;
    }
    .info{
      text-align: center;
      color: #666;
      margin-top: 50px;
    }
  }
</style>
