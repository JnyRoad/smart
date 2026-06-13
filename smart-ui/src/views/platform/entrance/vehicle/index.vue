<!--出入记录：车辆出入 -->
<template>
  <div class="my-basic-container vehicle">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-tabs v-model="activeName" @tab-click="tabChange" class="tabs">
            <el-tab-pane label="内部车辆" name="insider"></el-tab-pane>
            <el-tab-pane label="外部车辆" name="outsider"></el-tab-pane>
          </el-tabs>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button type="primary" icon="el-icon-download" :loading="exportLoading" @click="export2Excel">导出信息</el-button>
          </div>
        </div>
        <tce-Search-bar>
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="车主" prop="driverName">
              <el-input v-model="searchForm.driverName" placeholder="车主" clearable></el-input>
            </el-form-item>
            <el-form-item label="车牌号" prop="vehiclePlate">
              <el-input v-model="searchForm.vehiclePlate" placeholder="车牌号" clearable></el-input>
            </el-form-item>
            <el-form-item label="地点" prop="areaIdArray">
              <el-cascader :options="options" v-model="searchForm.areaIdArray" clearable></el-cascader>
            </el-form-item>
            <template v-if="isInsider">
              <el-form-item label="所属园区/BU/部门" prop="depIds">
                <el-cascader
                  expand-trigger="hover"
                  :options="compOptions"
                  :show-all-levels="false"
                  :change-on-select="true"
                  v-model="depIds"
                  clearable
                ></el-cascader>
              </el-form-item>
            </template>
            <el-form-item label="出入类型" prop="eventType">
              <el-select v-model="searchForm.eventType" placeholder="出入类型" clearable>
                <el-option label="进门" value="1"></el-option>
                <el-option label="出门" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="出入时间" prop="snapTime">
              <el-date-picker
                v-model="searchForm.snapTime"
                type="datetimerange"
                range-separator="-"
                value-format="yyyy-MM-dd HH:mm:ss"
                :default-time="['00:00:00', '23:59:59']"
                start-placeholder="起始时间"
                end-placeholder="截止时间"
                clearable
              ></el-date-picker>
            </el-form-item>
            <template v-if="!isInsider">
              <el-form-item label="手机号" prop="driverPhone">
                <el-input v-model="searchForm.driverPhone" placeholder="手机号" clearable></el-input>
              </el-form-item>
            </template>
          </el-form>
        </tce-Search-bar>
        <avue-crud
          ref="crud"
          :page="page"
          :data="mainData"
          :table-loading="tableLoading"
          @size-change="sizeChange"
          @current-change="currentChange"
          :option="mainOption"
        >
          <template slot-scope="scope" slot="driverName">{{scope.row.driverName || '未知'}}</template>
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index)"
            >详情</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, tree } from "@/api/platform/entrance/vehicle";
import { getCompTree } from "@/api/platform/_publicService";
import {
  insidertbOpt,
  outsidertbOpt
} from "@/const/crud/platform/entrance/vehicle";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";

export default {
  name: "vehicle",
  data() {
    return {
      exportLoading: false,
      searchForm: {
        //搜索菜单表单
        driverName: undefined,
        vehiclePlate: undefined,
        compId: undefined,
        depId: undefined,
        areaId: undefined,
        eventType: undefined,
        snapTime: undefined,
        driverPhone: undefined,
        areaId: undefined,
        areaIdArray: []
      },
      depIds: [],
      options: [], //地点列表
      compOptions: [],
      activeName: 'insider',
      isInsider: true,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      vehicleAscription: 2,
      tableLoading: false,
      mainData: [],
      insiderData: [],
      outsiderData: [],
      mainOption: insidertbOpt,
      insidertbOpt: insidertbOpt,
      outsidertbOpt: outsidertbOpt
    };
  },
  watch: {
    depIds(newVal, oldVal) {
      if (isArrayFn(newVal) && newVal.length > 0) {
        const depLength = newVal.length;
        if (depLength == 3) {
          this.searchForm.depId = this.depIds[2];
          this.searchForm.compId = this.depIds[1];
          this.searchForm.parkId = this.depIds[0];
        } else if (depLength >= 2) {
          this.searchForm.depId = undefined;
          this.searchForm.compId = this.depIds[1];
          this.searchForm.parkId = this.depIds[0];
        } else if (depLength >= 1) {
          this.searchForm.depId = undefined;
          this.searchForm.compId = undefined;
          this.searchForm.parkId = this.depIds[0];
        }
      } else {
        this.searchForm.depId = undefined;
        this.searchForm.compId = undefined;
        this.searchForm.parkId = undefined;
      }
    }
  },
  created() {
    tree().then(response => {
      this.options = response.data.data;
    });
    getCompTree().then(response => {
      this.compOptions = response.data.data;
    });

    this.$nextTick(() => {
      // 详情带参数返回
      if (this.$route.query.queryForm != undefined) {
        this.isInsider = this.$route.query.isInsider;

        if(this.isInsider=='true'||this.isInsider){
          this.activeName = 'insider'
          this.vehicleAscription = 2;
          this.mainOption = this.insidertbOpt;
        }else if(this.isInsider=='false'||!this.isInsider){
          this.activeName = 'outsider'
          this.vehicleAscription = 3;
          this.mainOption = this.outsidertbOpt;
        }
        let queryPage = this.$route.query.queryPage;
        let queryForm = this.$route.query.queryForm;
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {});
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {});
        }
        this.getList(this.page, this.searchForm);
      } else {
        this.getList(this.page);
      }
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),
    getMainData: function() {
      if (this.isInsider) {
        return this.insiderData;
      } else {
        return this.outsiderData;
      }
    },
    begainTime: function() {
      if (this.searchForm.snapTime) {
        return this.searchForm.snapTime[0];
      } else {
        return undefined;
      }
    },
    endTime: function() {
      if (this.searchForm.snapTime) {
        return this.searchForm.snapTime[1];
      } else {
        return undefined;
      }
    }
  },
  methods: {
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        let cName1 = '', cName2= ''
        if(this.vehicleAscription==2){
          cName1 = "BU"
          cName2 = "compName"
        }else{
          cName1 = "所属单位"
          cName2 = "company"
        }
        const tHeader = [
          "所属园区",
          "车主",
          "车牌号",
          "出入地点",
          "出入类型",
          "出入时间",
          cName1,
          "手机号",
          "方式",
          "权限"
        ];
        const filterVal = [
          "parkName",
          "driverName",
          "vehiclePlate",
          "areaName",
          "eventType",
          "snapTime",
          cName2,
          "driverPhone",
          "letPass",
          "authority"
        ];

        let params = Object.assign(
          {
            descs: "create_time",
            current: 1,
            size: 10000,
            vehicleAscription: this.vehicleAscription,
          },
          this.searchForm
        );
        if (isArrayFn(params.snapTime)) {
          //日期数组
          params.snapTime = params.snapTime.join();
        }
        if (isArrayFn(params.areaIdArray)) {
          //地点数组,后台那边只要最后一个值
          if (params.areaIdArray.length > 0) {
            params.areaIdArray = params.areaIdArray.slice(-1)[0];
            params.areaId = params.areaIdArray;
          }
        }
        fetchList(params).then(response => {
          const list = response.data.data.records;
          list.forEach(function(item) {

            item.eventType ===1? item.eventType = "进门":""
            item.eventType ===2? item.eventType = "出门":""

            item.letPass ===0? item.letPass = "手动":""
            item.letPass ===1? item.letPass = "自动":""

            item.authority ===0? item.authority = "有":""
            item.authority ===1? item.authority = "无":""

          });
          const data = this.formatJson(filterVal, list);
          let title = this.vehicleAscription==2?'内部车辆出入记录':'外部车辆出入记录'
          export_json_to_excel(tHeader, data, `${title}&(${this.searchForm.snapTime})`);
          this.exportLoading = false;
        }).catch(err => {
          this.exportLoading = false;
        });
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    tabChange(tab) {
      if(tab.name==='insider'){
        this.initInsider();
      }else if(tab.name==='outsider'){
        this.initOutsider();
      }
      this.resetFrom("searchForm");
      this.getList(this.page);
    },
    getList(page, params) {
      this.tableLoading = true;

      params = Object.assign(
        {
          descs: "create_time",
          current: page.currentPage,
          vehicleAscription: this.vehicleAscription,
          size: page.pageSize
        },
        params
      );

      if (isArrayFn(params.snapTime)) {
        //日期数组
        params.snapTime = params.snapTime.join();
      }

      if (isArrayFn(params.areaIdArray)) {
        //地点数组
        if (params.areaIdArray.length > 0) {
          params.areaIdArray = params.areaIdArray.slice(-1)[0];
          params.areaId = params.areaIdArray;
        }
      }

      fetchList(params).then(response => {
        this.mainData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
      this.tableLoading = false;
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
    initInsider() {
      this.isInsider = true;
      this.total = 0;
      this.currentPage = 1;
      this.pageSize = 20;
      this.vehicleAscription = 2;
      this.mainOption = this.insidertbOpt;
      this.getList(this.page);
    },
    initOutsider() {
      this.isInsider = false;
      this.total = 0;
      this.currentPage = 1;
      this.pageSize = 20;
      this.vehicleAscription = 3;
      this.mainOption = this.outsidertbOpt;
      this.getList(this.page);
    },

    handleDetail(row, index) {
      const src = `/platform/entrance/vehicle/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          isInsider: this.isInsider,
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
        this.page.currentPage = 1;
        this.depIds = [];
        this.getList(this.page);
      }
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
 .top-menu{
    border: none;
  }
  .tabs ::v-deep {
    flex: 1;
    .el-tabs__item{
      height: 50px;
    }
  }
</style>
