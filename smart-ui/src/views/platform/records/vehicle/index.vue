<!--设备管理，闸机和门禁的通关人员 -->
<template>
  <div class="my-basic-container device">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="车牌号" prop="vehiclePlate">
            <el-input v-model="searchForm.general" placeholder="车牌号" clearable></el-input>
          </el-form-item>
          <el-form-item label="车主姓名" prop="name">
            <el-input v-model="searchForm.personName" placeholder="车主" clearable></el-input>
          </el-form-item>
          <el-form-item label="员工号" prop="badge">
            <el-input v-model="searchForm.badge" placeholder="员工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="业务类型" prop="serviceType">
            <el-select v-model="searchForm.serviceType" placeholder="请选择" clearable>
              <el-option label="员工车辆" value="1"></el-option>
              <el-option label="公司车辆" value="2"></el-option>
              <el-option label="非员工车辆" value="3"></el-option>
              <el-option label="访客预约" value="4"></el-option>
              <el-option label="物流车预约" value="5"></el-option>
              <el-option label="入厂申请" value="6"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="taskType">
            <el-select v-model="searchForm.taskType" placeholder="请选择" clearable>
              <el-option label="待处理" value="0"></el-option>
              <el-option label="已处理" value="1"></el-option>
              <el-option label="失败" value="2"></el-option>
              <el-option label="处理中" value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="下发时间" prop="downTime" clearable>
            <el-date-picker
              v-model="searchForm.downTime"
              type="daterange"
              range-separator="-"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              start-placeholder="起始日期"
              end-placeholder="截止日期"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="设备类型" prop="deviceType">
            <deviceTypeSelect v-model="searchForm.deviceType" placeholder="设备类型"></deviceTypeSelect>
          </el-form-item>
          <el-form-item label="所在区域" prop="areaIdArray">
            <el-cascader :options="options" v-model="searchForm.areaIdArray" clearable></el-cascader>
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
          <template slot-scope="scope" slot="deviceType">
            <template v-if="scope.row.deviceType === 1">闸机</template>
            <template v-if="scope.row.deviceType === 2">门禁</template>
            <template v-if="scope.row.deviceType === 3">道闸</template>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, getTree } from "@/api/platform/records/vehicle";
import { tableOption } from "@/const/crud/platform/records/vehicle";
import deviceTypeSelect from "../../device/components/device-type-select"

export default {
  name: "device",
  components: {
    deviceTypeSelect
  },
  data() {
    return {
      searchForm: {
        general: "",
        personName: "",
        depId: [],
        badge: "",
        serviceType: "",
        taskType: "",
        deviceCode: "",
        areaIdArray: [],
        downTime: [],
        startTime: "",
        endTime: ""
      },
      tableLoading: false,
      tableData: [],
      depIds: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      options: []
    };
  },
  created: function() {
    this.getList(this.page);
    getTree(3).then(response => {
      this.options = response.data.data;
    });
  },
  mounted: function() {},
  computed: {},
  methods: {
    getList(page, params) {
      if (this.depIds.length == 2) {
        this.searchForm.depId = this.depIds[1];
      }
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            descs: "create_time",
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then(response => {
        this.tableData = response.data.data.records;
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
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      if (this.searchForm.areaIdArray.length == 2) {
        form.deviceCode = this.searchForm.areaIdArray[1];
      }
      if (this.searchForm.downTime.length == 2) {
        form.startTime = this.searchForm.downTime[0];
        form.endTime = this.searchForm.downTime[1];
      }
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
        this.getList(this.page);
      }
    }
  }
};
</script>
