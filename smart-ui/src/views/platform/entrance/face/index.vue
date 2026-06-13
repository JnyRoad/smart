<!--出入记录：人员出入 -->
<template>
  <div class="my-basic-container face">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-tabs v-model="activeName" @tab-click="tabChange" class="tabs">
            <el-tab-pane label="内部人员" name="insider"></el-tab-pane>
            <el-tab-pane label="外部人员" name="outsider"></el-tab-pane>
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
            <el-form-item label="姓名" prop="personName">
              <el-input v-model="searchForm.personName" placeholder="姓名" clearable></el-input>
            </el-form-item>
            <el-form-item label="工号" prop="badge" v-if="isInsider">
              <el-input v-model="searchForm.badge" placeholder="工号" clearable></el-input>
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
            <el-form-item label="设备名称" prop="deviceId">
              <el-select
                v-model="searchForm.deviceId"
                placeholder="请输入设备名称进行搜索"
                clearable
                filterable
                remote
                reserve-keyword
                :remote-method="remoteSearchDevice"
                :loading="deviceLoading"
                @clear="handleDeviceClear"
                @visible-change="handleDeviceVisibleChange"
                no-match-text="未找到匹配的设备"
                no-data-text="暂无设备数据">
                <el-option
                  v-for="item in filteredDeviceOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="体温" prop="isNormal">
              <el-select v-model="searchForm.isNormal" placeholder="体温是否正常" clearable>
                <el-option label="正常" :value="1"></el-option>
                <el-option label="异常" :value="0"></el-option>
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
          <template slot-scope="scope" slot="compName">
            <el-tooltip placement="bottom">
              <div slot="content" style="line-height: 25px">
                部门：{{scope.row.depName}}<br/>
                岗位：{{scope.row.jobName}}<br/>
                职层：{{scope.row.jcheName}}<br/>
              </div>
              <div style="cursor: pointer">
                <!-- <i class="el-icon-info"></i> -->
                <span class="ft-blue">{{ scope.row.compName }}</span>
              </div>
            </el-tooltip>
          </template>
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
import { fetchList, tree } from "@/api/platform/entrance/face";
import { getCompTree } from "@/api/platform/_publicService";
import { fetchList as fetchDeviceList } from "@/api/platform/device/camera";
import {
  insidertbOpt,
  outsidertbOpt
} from "@/const/crud/platform/entrance/face";
import { mapGetters } from "vuex";
import { isArrayFn, dateFormat} from "@/util/util";

export default {
  name: "face",
  data() {
    return {
      exportLoading: false,
      searchForm: {
        //搜索菜单表单
        personName: undefined,
        eventType: undefined,
        snapTime: undefined,
        parkId: undefined,
        compId: undefined,
        depId: undefined,
        jobId: undefined,
        jcheId: undefined,
        areaId: undefined,
        areaIdArray: [],
        deviceId: undefined
      },
      depIds: [],
      options:[],
      compOptions: [],
      deviceOptions: [],
      filteredDeviceOptions: [],
      deviceLoading: false,
      isInsider: true,
      activeName: 'insider',
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      personType: 1,
      tableLoading: false,
      mainData: [],
      insiderData: [],
      outsiderData: [],
      mainOption: insidertbOpt,
      insidertbOpt: insidertbOpt,
      outsidertbOpt: outsidertbOpt
    };
  },
  created() {
    const endTime = dateFormat(new Date(), 'yyyy-MM-dd')
    const startTime = this.setStartTime(endTime)
    this.searchForm.snapTime = [startTime+' 00:00:00',endTime+' 23:59:59']
    // dateFormat(new Date(), 'yyyy-MM-dd hh:mm')
    tree().then(response => {
      this.options = response.data.data;
    });
    getCompTree().then(response => {
      this.compOptions = response.data.data;
    });
    // 获取设备列表
    this.getDeviceList();
    this.$nextTick(() => {
      // 详情带参数返回
      if (this.$route.query.queryForm != undefined) {
        this.isInsider = this.$route.query.isInsider;

        if(this.isInsider=='true'||this.isInsider){
          this.activeName = 'insider'
          this.personType = 1;
          this.mainOption = this.insidertbOpt;
        }else if(this.isInsider=='false'||!this.isInsider){
          this.activeName = 'outsider'
          this.personType = 2;
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
        this.getList(this.page, this.searchForm);
      }
    });
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
        if(this.personType==1){
          cName1 = "BU"
          cName2 = "compName"
        }else{
          cName1 = "所属单位"
          cName2 = "company"
        }
        const tHeader = [
          "所属园区",
          "姓名",
          "工号",
          "出入地点",
          "设备名称",
          "出入类型",
          "出入时间",
          cName1,
          "体温"
        ];
        const filterVal = [
          "parkName",
          "personName",
          "badge",
          "areaName",
          "deviceName",
          "eventType",
          "snapTime",
          cName2,
          "faceTemperature"
        ];

        let params = Object.assign(
          {
            descs: "create_time",
            current: 1,
            size: 10000,
            personType: this.personType,
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

            item.eventType ===1? item.eventType = "进":""
            item.eventType ===2? item.eventType = "出":""

          });
          const data = this.formatJson(filterVal, list);
          let title = this.personType==1?'内部人员出入记录':'外部人员出入记录'
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
          personType: this.personType,
          size: page.pageSize
        },
        params
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
      this.page.total = 0;
      this.page.currentPage = 1;
      this.page.pageSize = 20;
      this.personType = 1;
      this.mainOption = this.insidertbOpt;
    },
    initOutsider() {
      this.isInsider = false;
      this.page.total = 0;
      this.page.currentPage = 1;
      this.page.pageSize = 20;
      this.personType = 2;
      this.mainOption = this.outsidertbOpt;
    },
    handleDetail(row, index) {
      const src = `/platform/entrance/face/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          isInsider: this.isInsider,
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    setStartTime(time) {
      var tm = new Date(time)
      tm.setDate(tm.getDate() - 7)  // 设置为7天前
      return dateFormat(tm, 'yyyy-MM-dd')
    },
    // 获取设备列表
    getDeviceList() {
      this.deviceLoading = true;
      fetchDeviceList({
        current: 1,
        size: 1000  // 获取所有设备
      }).then(response => {
        if (response.data.code === 0 && response.data.data.records) {
          this.deviceOptions = response.data.data.records.map(device => ({
            label: device.deviceName,
            value: device.id  // 使用设备ID作为value
          }));
          // 初始化时显示前50个设备，避免下拉框过长
          this.filteredDeviceOptions = this.deviceOptions.slice(0, 50);
        }
        this.deviceLoading = false;
      }).catch(() => {
        this.deviceLoading = false;
      });
    },
    // 远程搜索设备
    remoteSearchDevice(query) {
      if (query !== '') {
        this.deviceLoading = true;
        setTimeout(() => {
          this.filteredDeviceOptions = this.deviceOptions.filter(item => {
            return item.label.toLowerCase().indexOf(query.toLowerCase()) > -1;
          });
          this.deviceLoading = false;
        }, 200);
      } else {
        // 如果搜索为空，恢复显示前50个设备
        this.filteredDeviceOptions = this.deviceOptions.slice(0, 50);
      }
    },
    // 处理设备选择框清空事件
    handleDeviceClear() {
      // 清空时恢复显示前50个设备
      this.filteredDeviceOptions = this.deviceOptions.slice(0, 50);
    },
    // 处理设备选择框显示/隐藏事件
    handleDeviceVisibleChange(visible) {
      if (visible && this.filteredDeviceOptions.length === 0) {
        // 当下拉框打开且没有选项时，显示前50个设备
        this.filteredDeviceOptions = this.deviceOptions.slice(0, 50);
      }
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
        // 重新设置默认时间为最近7天
        const endTime = dateFormat(new Date(), 'yyyy-MM-dd')
        const startTime = this.setStartTime(endTime)
        this.searchForm.snapTime = [startTime+' 00:00:00',endTime+' 23:59:59']
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
