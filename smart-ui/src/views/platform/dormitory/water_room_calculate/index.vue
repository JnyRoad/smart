<!--个人水电明细模板  -->
<template>
  <div class="my-basic-container mycard2 room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <div class="box-outer box-left">
        <el-scrollbar class="my-lit-scrollbar" :native="false">
          <div class="cont-left">
            <span class="tiplbl">房间检索</span>
            <div style="margin: 10px 0">
              <el-input placeholder="输入关键字进行过滤" clearable v-model="filterText" size="mini">
                <el-button type="info" slot="append" icon="el-icon-search"></el-button>
              </el-input>
            </div>
            <!-- :default-expanded-keys="[curTreeId]" -->
            <el-tree
              class="filter-tree"
              :data="treedata"
              :highlight-current="true"
              node-key="id"
              :props="defaultProps"
              :expand-on-click-node ="false"
              :filter-node-method="filterNode"
              @node-click="handleNodeClick"
              ref="roomtree"
            ></el-tree>
          </div>
        </el-scrollbar>
      </div>
      <section class="my-basic-inner">
        <div class="box-outer">
          <div class="top-menu">
            <div class="top-right">
              <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
              <el-button
                type="primary"
                icon="el-icon-delete"
                @click="resetFrom('searchForm')"
                plain
              >重置</el-button>
              <el-button
                type="primary"
                @click="importMeterData"
                icon="icon-yutong-upload"
                plain
              >导入抄表数据</el-button>
              <el-button
                type="primary"
                @click="exportMeterData"
                icon="icon-yutong-download"
              >导出</el-button>
              <el-button type="primary" @click="sureGenerateDetail" plain>计算水电</el-button>
              <el-button type="primary" @click="resetSdDetail" plain>批量保存水电</el-button>
            </div>
          </div>
          <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini" class="topForm">
            <el-form-item label="抄表月份" prop="meterMonth">
              <el-date-picker v-model="searchForm.meterMonth" type="month" value-format="yyyy-MM" placeholder="选择月份" :clearable="false"></el-date-picker>
            </el-form-item>
          </el-form>
          <div class="table-outer">
            <el-scrollbar class="scroll1" :native="false">
              <div class="scroll1-inner">
                <table>
                  <thead>
                    <tr>
                      <th rowspan="2" class="w1">房间号</th>
                      <th rowspan="2" class="w2">操作</th>
                      <th colspan="5">用电</th>
                      <th colspan="2">冷水</th>
                      <th colspan="2">热水</th>
                      <th colspan="3">用水</th>
                      <th colspan="2">单价</th>
                      <th rowspan="2" class="w3">超标金额</th>
                      <th rowspan="2" class="w3">住宿总天数</th>
                      <th rowspan="2" class="w3">日平均金额</th>
                    </tr>
                    <tr>
                      <!-- 用电 -->
                      <th class="w2">上月</th>
                      <th class="w2" style="background: #00f3ff">本月</th>
                      <th class="w2">实用</th>
                      <th class="w2">标准</th>
                      <th class="w2">超标</th>
                      <!-- 冷水 -->
                      <th class="w2">上月</th>
                      <th class="w2" style="background: #00f3ff">本月</th>
                      <!-- 热水 -->
                      <th class="w2">上月</th>
                      <th class="w2" style="background: #00f3ff">本月</th>
                      <!-- 用水 -->
                      <th class="w2">实用</th>
                      <th class="w2">标准</th>
                      <th class="w2">超标</th>
                      <!-- 单价 -->
                      <th class="w2">电</th>
                      <th class="w2">水</th>
                    </tr>
                  </thead>
                </table>
                <el-scrollbar class="scroll2" :native="false">
                  <table style="margin-bottom: 30px">
                    <tbody v-if="tableData && tableData.length>0">
                      <tr v-for="(item, index) in tableData" :key="index" :style="{'background-color': getRowColor(item.changeList)}" style="background-color: yellow">
                        <td class="w1">{{item.roomName}}</td>
                        <td class="w2">
                          <el-button type="text" @click="edit(item)">修改</el-button>
                        </td>
                        <!-- 电 -->
                        <td class="w2">{{item.elePreMonthNum}}</td>
                        <td class="w2">{{item.eleCurMonthNum}}</td>
                        <td class="w2 c_sy">{{item.eleUse}}</td>
                        <td class="w2 c_bz">{{item.eleQty}}</td>
                        <td class="w2 c_cbz">{{item.eleOverUse}}</td>
                        <!-- 冷水 -->
                        <td class="w2">{{item.coldPreMonthNum}}</td>
                        <td class="w2">{{item.coldCurMonthNum}}</td>
                        <!-- 热水 -->
                        <td class="w2">{{item.hotPreMonthNum}}</td>
                        <td class="w2">{{item.hotCurMonthNum}}</td>
                        <!-- 用水 -->
                        <td class="w2 c_sy">{{item.coldUse}}</td>
                        <td class="w2 c_bz">{{item.coldQty}}</td>
                        <td class="w2 c_cbz">{{item.coldOverUse}}</td>
                        <!-- 单价 -->
                        <td class="w2">{{item.eleOverFee}}</td>
                        <td class="w2">{{item.coldOverFee}}</td>
                        <td class="w3 c_cbz">{{item.totalAmount}}</td>
                        <td class="w3">
                          <span class="ft-blue" @click="editInDays(item)" > {{item.inDays}} </span>
                        </td>
                        <td class="w3">{{item.avgAmount}}</td>
                      </tr>
                    </tbody>
                    <tbody v-else>
                      <tr>
                        <td colspan="19">请选择楼层查询信息！</td>
                      </tr>
                    </tbody>
                  </table>
                </el-scrollbar>
              </div>
            </el-scrollbar>
          </div>
        </div>
      </section>
    </el-scrollbar>
    <DlgWaterDorm ref="DlgWaterDorm" :curItem="curItem" @refresh="refresh"/>
    <DlgImportMeterData ref="DlgImportMeterData" @refresh="refresh"/>
    <DlgExportMeterData ref="DlgExportMeterData" :meterMonth="searchForm.meterMonth" @refresh="refresh"/>
    <dlgEditAvg ref="dlgeditavg" :row="curItem" @refresh="refresh"/>
  </div>
</template>

<script>
import { fetchList, resetSdDetail } from "./_service";
import { allList } from "@/api/platform/dormitory/staff";
import { dateFormat2 } from "@/util/date";
import { getDateMonth } from "@/util/util";
import DlgWaterDorm from "./components/dlg_water_dorm";
import DlgImportMeterData from "./components/importMeterData/index";
import DlgExportMeterData from "./components/exportMeterData/index";
import dlgEditAvg from "../sd_meterread/components/dlg_edit_avg";
import { generateSDStatementDetail } from "@/api/platform/dormitory/sd_meterread";

export default {
  name: "",
  components: {
    DlgWaterDorm,
    DlgImportMeterData,
    DlgExportMeterData,
    dlgEditAvg
  },
  data() {
    return {
      pageLoading: null,
      exportLoading: false,
      searchForm: {
        meterMonth: undefined //抄表月份
      },
      treedata: [],
      tableData: [],
      curItem: {},
      filterText: "",
      params: null,
      curDataId: '',
      curTreeId: '',
      defaultProps: {
        children: "children",
        label: "label",
        value: "value"
      },
    };
  },
  created() {
    this.searchForm.meterMonth = getDateMonth()
    this.getTree()
  },
  mounted: function() {},
  computed: {},
  watch: {
    filterText(val) {
      this.$refs.roomtree.filter(val);
    }
  },
  methods: {
    getRowColor(list) {
      //获取男生数量进度条百分比值
      if (list.length > 0) {
        return 'yellow';
      } else {
        return '#fff';
      }
    },
    editInDays(item){
      this.curItem = {id: item.mrId, totalStayDays: item.inDays}
      this.$refs.dlgeditavg && this.$refs.dlgeditavg.open()
    },
    importMeterData(){
      this.$refs.DlgImportMeterData && this.$refs.DlgImportMeterData.open()
    },
    exportMeterData(){
      this.$refs.DlgExportMeterData && this.$refs.DlgExportMeterData.open()
    },
    edit(item){
      this.$refs.DlgWaterDorm && this.$refs.DlgWaterDorm.open()
      this.curItem = item
    },
    refresh(){
      this.getList()
    },
    dateFormat(val) {
      if (!this.validatenull(val)) {
        return dateFormat2(new Date(val));
      }
    },
    getList() {
      fetchList(Object.assign({},this.searchForm, this.params)).then(response => {
        this.tableData = response.data.data;
      }).catch(err => { console.error(err) });
    },
    getTree: function() {
      allList().then(response => {
        this.treedata = response.data.data;
        // for(let i = 0; i<this.treedata.length; i++){
        //   if(this.treedata[i].children && this.treedata[i].children.length>0){
        //     let dorm = this.treedata[i].children[0]
        //       if(dorm.children && dorm.children.length>0){
        //         this.curTreeId = dorm.children[0].id
        //         this.params = { floorId: this.curTreeId };
        //         break
        //       }
        //   }
        // }
        // this.$nextTick(_ => {
        //   this.$refs.roomtree && this.$refs.roomtree.setCurrentKey(this.curTreeId);
        // });
        // this.getList();
      });
    },
    handleNodeClick(data, node) {
      let level = node.level;
      let params = null;
      // this.curTreeId = ''
      if (level == 1) {
        this.$message.error('请选择楼栋或者楼层！');
        return
        // params = { parkId: data.id };
      } else{
        if (level == 2) {
          params = { dormitoryId: data.id };
        } else if (level == 3) {
          params = { floorId: data.id };
        } else if (level == 4) {
          params = { roomId: data.id };
          // this.curTreeId = data.id
        }
        this.params = params
        this.getList();
      }
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    searchSubmit() {
      if(!this.searchForm.meterMonth){
        this.$message.error('请选择抄表月份');
        return
      }
      this.getList()
    },
    resetFrom(formName) {
      this.$refs[formName].resetFields();
      this.getList()
    },
    sureGenerateDetail(){
      let _this = this
      const elm = this.$createElement;
      if(!(this.params && this.params.dormitoryId)){
        this.$message.error('请选择要计算的楼栋!');
        return
      }
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("div", null, [
            elm("p", null, "是否计算水电，数据生成后，将不可修改"),
            elm("p", null, "注意：核算水电明细较为耗时，请耐心等待结果")
          ])
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      }).then(function() {
          _this.pageLoading = _this.$loading({
            lock: true,
            text: "正在执行，请稍后…",
            spinner: "el-icon-loading",
            background: "rgba(0, 0, 0, 0.7)",
            customClass: "bigdata-loading"
          });
          return generateSDStatementDetail({dormitoryId: _this.params.dormitoryId});
        })
        .then(data => {
          _this.pageLoading.close();
          _this.getList()
          if (data.data.code == 0) {
            _this.$alert(data.data.data, '计算水电成功', {
            confirmButtonText: '确定',
            callback: action => {}
          });
          }
        })
        .catch((e) => {
          _this.pageLoading.close();
        });
    },
    resetSdDetail(){
      let _this = this
      const elm = this.$createElement;
      if(!(this.params && (this.params.dormitoryId || this.params.floorId || this.params.roomId))){
        this.$message.error('请选择要修改的楼栋、楼层或房间!');
        return
      }
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("div", null, [
            elm("p", null, "是否确定批量修改水电数据？"),
            elm("p", null, "注意：批量修改水电较为耗时，请耐心等待结果")
          ])
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      }).then(function() {
          _this.pageLoading = _this.$loading({
            lock: true,
            text: "正在执行，请稍后…",
            spinner: "el-icon-loading",
            background: "rgba(0, 0, 0, 0.7)",
            customClass: "bigdata-loading"
          });
          return resetSdDetail(
            Object.assign({
              meterMonth: _this.searchForm.meterMonth
            }, _this.params)
          );
        })
        .then(data => {
          _this.pageLoading.close();
          _this.getList()
          if (data.data.code == 0) {
            _this.$message({
              message: '修改水电成功',
              type: 'success'
            });
          }
        })
        .catch((e) => {
          _this.pageLoading.close();
        });
    }
  }
};
</script>
<style lang="scss" scoped>
  $c1: #10cc8e;
  $c2: #999;
  $c3: #e7292e;
  .my-basic-inner{
    height: 100%;
    .box-outer{
      display: flex;
      flex-direction: column;
      height: 100%;
      overflow: hidden;
    }
  }
  .table-outer{
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: stretch;
    overflow: hidden;
    width: 100%;
    .scroll1 ::v-deep {
      height: 100%;
      .el-scrollbar__view{
        display: flex;
        justify-content: stretch;
      }
    }
    .scroll1-inner{
      flex: 1;
      display: flex;
      flex-direction: column;
    }
    .scroll2 ::v-deep {
      min-width: 1035px;
      flex: 1;
      .el-scrollbar__view{
        display: block;
        height: auto;
      }
    }
    table{
      width: 100%;
      th, td{
        text-align: center;
        padding: 5px;
        word-break: break-all;
      }
      th{
        border: 1px solid #e0e0e0;
        background: #eef1f6;
        line-height: 35px;
      }
      td{
        border: 1px solid #e0e0e0 !important;
        line-height: 35px;
      }
      .w1{
        width: 5.4%;
        min-width: 45px;
      }
      .w2{
        width: 4.76%;
        min-width: 45px;
      }
      .w3{
        width: 7.7%;
        min-width: 45px;
      }
    }
  }
  .topForm ::v-deep {
    .el-form-item__label {
      width: 80px;
    }
  }
  .tabs ::v-deep {
    flex: 1;
    .el-tabs__item{
      height: 50px;
    }
  }
  .c_sy{
    color: $c1;
  }
  .c_bz{
    color: $c2;
  }
  .c_cbz{
    color: $c3;
  }
</style>
