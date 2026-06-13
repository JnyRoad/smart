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
              <el-button
                type="primary"
                @click="exportMeterData"
                icon="icon-yutong-download"
              >导出表格</el-button>
              <el-button
                type="primary"
                @click="exportShareData"
                icon="icon-yutong-download"
              >部门分摊水电表</el-button>
            </div>
          </div>
          <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini" class="topForm">
            <el-form-item label="抄表月份" prop="meterMonth">
              <el-date-picker v-model="searchForm.meterMonth" type="month" value-format="yyyy-MM" placeholder="选择月份"></el-date-picker>
            </el-form-item>
          </el-form>
          <avue-crud
            ref="crud"
            :page="page"
            :data="tableData"
            :table-loading="tableLoading"
            :option="tableOption"
            @size-change="sizeChange"
            @current-change="currentChange"
          >
            <template slot-scope="scope" slot="status">
              <span :class="{'ft-danger': scope.row.status===0}">{{scope.row.status | staffStatusInit}}</span>
            </template>
            <template slot-scope="scope" slot="createTime">
              <span
              >{{dateFormat(scope.row.createTime)}}</span>
            </template>
             <template slot-scope="scope" slot="inTime">
              <span
              >{{dateFormat(scope.row.inTime)}}</span>
            </template>
            <template slot-scope="scope" slot="outTime">
              <span
              >{{dateFormat(scope.row.outTime)}}</span>
            </template>
            <template slot-scope="scope" slot="meterMonth">
              <span
              >{{dateFormat(scope.row.meterMonth)}}</span>
            </template>
          </avue-crud>
        </div>
      </section>
    </el-scrollbar>
    <DlgExportMeterData ref="DlgExportMeterData" :meterMonth="searchForm.meterMonth" @refresh="refresh"/>
    <DlgExportShareData ref="DlgExportShareData" :meterMonth="searchForm.meterMonth" @refresh="refresh"/>

  </div>
</template>

<script>
import { fetchList } from "./_service";
import { allList } from "@/api/platform/dormitory/staff";
import { tableOption } from "@/const/crud/platform/dormitory/water_inStaff";
import { dateFormat2 } from "@/util/date";
import { getDateMonth } from "@/util/util";
import DlgExportMeterData from "./components/exportMeterData/index";
import DlgExportShareData from "./components/exportMeterData/exportShareData";

export default {
  name: "dorm_mng",
  components: {
    DlgExportMeterData,
    DlgExportShareData
  },
  data() {
    return {
      searchForm: {
        meterMonth: undefined //抄表月份
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
    this.searchForm.meterMonth = getDateMonth()
    this.getTree()
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
    exportMeterData(){
      this.$refs.DlgExportMeterData && this.$refs.DlgExportMeterData.open()
    },
    exportShareData(){
      this.$refs.DlgExportShareData && this.$refs.DlgExportShareData.open()
    },
    getList(page, form) {
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
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });

      this.tableLoading = false;
    },
    getTree: function() {
      allList().then(response => {
        this.treedata = response.data.data;
        this.initTree(this.treedata, false)
        // for(let i = 0; i<this.treedata.length; i++){
        //   if(this.treedata[i].children && this.treedata[i].children.length>0){
        //     this.curTreeId = this.treedata[i].children[0].id
        //     this.params = { dormitoryId: this.curTreeId };
        //     break
        //   }
        // }
        // this.$nextTick(_ => {
        //   this.$refs.roomtree && this.$refs.roomtree.setCurrentKey(this.curTreeId);
        // });
        // this.getList(this.page);
      });
    },
    initTree(list, t){
      const me = this
      if(list.length > 0){
        if(!t){
          list.forEach(element => {
            element['parkId'] = element.id
            if(element.children !== null){
              me.initTree(element.children, element.id)
            }
          });
        }else{
          list.forEach(element => {
            element['parkId'] = t
            if(element.children !== null){
              me.initTree(element.children, t)
            }
          });
        }
      }
    },
    handleNodeClick(data, node) {
      let level = node.level;
      let params = null;
      // this.curTreeId = ''
      if (level == 1) {
        this.$message.error('请选择楼栋或者楼层！');
        return
        // params = { parkId: data.id };
      } else if (level == 2) {
        params = { dormitoryId: data.id, parkId: data.parkId };
      } else if (level == 3) {
        params = { floorId: data.id, parkId: data.parkId };
      } else if (level == 4) {
        params = { roomId: data.id, parkId: data.parkId};
        // this.curTreeId = data.id
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
  .topForm ::v-deep {
    .el-form-item__label {
      width: 130px;
    }
  }
  .tabs ::v-deep {
    flex: 1;
    .el-tabs__item{
      height: 50px;
    }
  }
</style>
