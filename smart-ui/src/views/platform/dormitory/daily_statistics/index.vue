<!--宿舍水电日计算表  -->
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
            <el-tree
              class="filter-tree"
              :data="treedata"
              :highlight-current="true"
              node-key="id"
              :props="defaultProps"
              :expand-on-click-node="false"
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
            </div>
          </div>
          <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini" class="topForm">
            <el-form-item label="结算日期" prop="timeSlot">
              <el-date-picker
                v-model="searchForm.timeSlot"
                type="datetimerange"
                range-separator="-"
                value-format="yyyy-MM-dd HH:mm:ss"
                :default-time="['00:00:00', '23:59:59']"
                start-placeholder="起始时间"
                end-placeholder="截止时间"
                clearable
              ></el-date-picker>
              <!-- <el-date-picker v-model="searchForm.meterDay" type="date" value-format="yyyy-MM-dd" placeholder="选择日期"></el-date-picker> -->
            </el-form-item>
          </el-form>
          <div class="table-outer">
            <el-scrollbar class="scroll1" :native="false">
              <div class="scroll1-inner">
                <table>
                  <thead>
                    <tr>
                      <th rowspan="2" class="w1">房间号</th>
                      <th colspan="3">用电</th>
                      <th colspan="2">冷水</th>
                      <th colspan="2">热水</th>
                      <th rowspan="2" class="w1">用水</th>
                      <!-- <th colspan="2">单价</th> -->
                      <!-- <th rowspan="2" class="w3">超标金额</th>
                      <th rowspan="2" class="w3">住宿总天数</th>
                      <th rowspan="2" class="w3">日平均金额</th> -->
                      <th rowspan="2" class="w3">抄表日期</th>
                    </tr>
                    <tr>
                      <!-- 用电 -->
                      <th class="w2">昨日</th>
                      <th class="w2" style="background: #00f3ff">今日</th>
                      <th class="w2">实用</th>
                      <!-- <th class="w2">标准</th>
                      <th class="w2">超标</th> -->
                      <!-- 冷水 -->
                      <th class="w2">昨日</th>
                      <th class="w2" style="background: #00f3ff">今日</th>
                      <!-- 热水 -->
                      <th class="w2">昨日</th>
                      <th class="w2" style="background: #00f3ff">今日</th>
                      <!-- 用水 -->
                      <!-- <th class="w2">实用</th> -->
                      <!-- <th class="w2">标准</th>
                      <th class="w2">超标</th> -->
                      <!-- 单价 -->
                      <!-- <th class="w2">电</th>
                      <th class="w2">水</th> -->
                    </tr>
                  </thead>
                </table>
                <el-scrollbar class="scroll2" :native="false">
                  <table style="margin-bottom: 30px">
                    <tbody v-if="tableData && tableData.length>0">
                      <tr v-for="(item, index) in tableData" :key="index">
                        <td class="w1">{{item.roomName}}</td>
                        <!-- 电 -->
                        <td class="w2">{{item.preEleNum}}</td>
                        <td class="w2">{{item.curEleNum}}</td>
                        <td class="w2 c_sy">{{item.actEleNum}}</td>
                        <!-- <td class="w2 c_bz">{{item.eleQty}}</td>
                        <td class="w2 c_cbz">{{item.eleOverUse}}</td> -->
                        <!-- 冷水 -->
                        <td class="w2">{{item.preColdNum}}</td>
                        <td class="w2">{{item.curColdNum}}</td>
                        <!-- 热水 -->
                        <td class="w2">{{item.preHotNum}}</td>
                        <td class="w2">{{item.curHotNum}}</td>
                        <!-- 用水 -->
                        <td class="w1 c_sy">{{item.actWaterNum}}</td>
                        <!-- <td class="w2 c_bz">{{item.coldQty}}</td>
                        <td class="w2 c_cbz">{{item.coldOverUse}}</td> -->
                        <!-- 单价 -->
                        <!-- <td class="w2">{{item.eleOverFee}}</td>
                        <td class="w2">{{item.coldOverFee}}</td> -->
                        <!-- <td class="w3 c_cbz">{{item.totalAmount}}</td>
                        <td class="w3">
                          {{item.inDays}}
                        </td>
                        <td class="w3">{{item.avgAmount}}</td> -->
                        <td class="w3">{{item.meterMonth}}</td>
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

  </div>
</template>

<script>
import { fetchList } from "./_service";
import { allList } from "@/api/platform/dormitory/staff";
import { tableOption } from "@/const/crud/platform/dormitory/daily_statistics";
import { dateFormat2 } from "@/util/date";

export default {
  name: "dorm_mng",
  data() {
    return {
      searchForm: {
        timeSlot: undefined //结算日期
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      treedata: [],
      filterText: "",
      params: null,
      curTreeId: '',
      defaultProps: {
        children: "children",
        label: "label",
        value: "value"
      }
    };
  },
  created() {
    this.getTree()
    const yesterday = dateFormat2(new Date(new Date().setDate(new Date().getDate() - 1)));//昨天
    this.searchForm.timeSlot = [yesterday + ' 00:00:00', yesterday + ' 23:23:59']
  },
  mounted: function() {},
  watch: {
    filterText(val) {
      this.$refs.roomtree.filter(val);
    }
  },
  methods: {
    refresh(){
      this.getList(this.page, this.searchForm)
    },
    dateFormat(val) {
      if (!this.validatenull(val)) {
        return dateFormat2(new Date(val));
      }
    },
    getList(page, form) {
      if(this.params === null){
        this.$message.error('请选择楼栋或者楼层！');
        return
      }
      if (form.timeSlot && form.timeSlot != null) {
        form.startTime = form.timeSlot[0]
        form.endTime = form.timeSlot[1]
      } else {
        form.startTime = null
        form.endTime = null
      }
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          form,
          this.params
        )
      ).then(response => {
        this.tableData = response.data.data;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });

      this.tableLoading = false;
    },
    getTree: function() {
      allList().then(response => {
        this.treedata = response.data.data;
      });
    },
    handleNodeClick(data, node) {
      let level = node.level;
      let params = null;
      // this.curTreeId = ''
      if (level == 1) {
        this.$message.error('请选择楼栋或者楼层！');
        return
      } else if (level == 2) {
        params = { dormitoryId: data.id };
      } else if (level == 3) {
        params = { floorId: data.id };
      } else if (level == 4) {
        params = { roomId: data.id };
      }
      this.params = params
      this.page.currentPage = 1
      this.getList(this.page, this.searchForm);
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page, this.searchForm);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page, this.searchForm);
    },
    searchSubmit(form) {
      //搜索
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
    resetFrom(formName) {
      //清空
      this.$refs[formName].resetFields();
      this.dormitoryData = [];
      this.page.currentPage = 1;
      this.getList(this.page);
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
        width: 7%;
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
