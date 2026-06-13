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
          <el-form-item label="姓名" prop="general">
            <el-input v-model="searchForm.general" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="员工号" prop="badge">
            <el-input v-model="searchForm.badge" placeholder="员工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="业务类型" prop="serviceType">
            <el-select v-model="searchForm.serviceType" placeholder="请选择" clearable>
              <el-option label="变更员工权限" value="1"></el-option>
              <el-option label="APP信息完善" value="2"></el-option>
              <el-option label="访客预约" value="3"></el-option>
              <el-option label="员工扫码登记" value="4"></el-option>
              <el-option label="招聘入职" value="5"></el-option>
              <el-option label="入厂申请" value="6"></el-option>
              <el-option label="导入员工照片" value="7"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="taskType">
            <el-select v-model="searchForm.taskType" placeholder="请选择" clearable>
              <el-option label="初始化" value="0"></el-option>
              <el-option label="成功" value="1"></el-option>
              <el-option label="失败" value="2"></el-option>
              <el-option label="处理中" value="3"></el-option>
              <el-option label="已取消" value="4"></el-option>
              <el-option label="权限已过期" value="5"></el-option>
              <el-option label="设备离线" value="6"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="任务类型" prop="action">
            <el-select v-model="searchForm.action" placeholder="请选择" clearable>
              <el-option label="下发" value="1"></el-option>
              <el-option label="删除" value="2"></el-option>
              <el-option label="修改" value="3"></el-option>
              <el-option label="延迟下发" value="11"></el-option>
              <el-option label="延迟删除" value="12"></el-option>
              <el-option label="延迟修改" value="13"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="设备" prop="deviceName">
            <el-input v-model="searchForm.deviceName" placeholder="设备名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="设备类型" prop="deviceType">
            <deviceTypeSelect v-model="searchForm.deviceType" placeholder="设备类型"></deviceTypeSelect>
          </el-form-item>
          <el-form-item label="所在区域" prop="areaIdArray">
            <areaCascader v-model="searchForm.areaIdArray" :changeOnSelect="true" placeholder="所在区域"></areaCascader>
            <!-- <el-cascader :options="options" v-model="searchForm.areaIdArray" clearable></el-cascader> -->
          </el-form-item>
          <el-form-item label="下发时间" prop="downTime" clearable>
            <el-date-picker
              v-model="searchForm.downTime"
              type="daterange"
              value-format="yyyy-MM-dd"
              format="yyyy-MM-dd"
              range-separator="-"
              start-placeholder="起始日期"
              end-placeholder="截止日期"
            ></el-date-picker>
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
          <!-- <template slot-scope="scope" slot="menu">
            <template v-if="scope.row.taskType===2">
              <el-button type="text" @click="deviceTask(scope.row,scope.$index)">再次下发</el-button>
            </template>
            <template v-if="scope.row.taskType!=2">
              <el-button type="text" disabled>重新下发</el-button>
            </template>
          </template> -->
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchISCList,
  getTree,
  updateTaskStauts
} from "@/api/platform/records/person";
import { tableOption } from "@/const/crud/platform/records/person_isc";
import deviceTypeSelect from "../../device/components/device-type-select"
import { dateFormat } from "@/util/util";

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
        action:"",
        deviceName: "",
        areaId: "",
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
    // 设置默认时间范围为最近7天
    const endTime = dateFormat(new Date(), 'yyyy-MM-dd')
    const startTime = this.setStartTime(endTime)
    this.searchForm.downTime = [startTime, endTime]
    this.searchForm.startTime = startTime
    this.searchForm.endTime = endTime

    this.getList(this.page, this.searchForm);
    // getTree(2).then(response => {
    //   this.options = response.data.data;
    // });
  },
  mounted: function() {},
  computed: {},
  methods: {
    /**
     * 设置开始时间为7天前
     */
    setStartTime(time) {
      var tm = new Date(time)
      tm.setDate(tm.getDate() - 7)  // 设置为7天前
      return dateFormat(tm, 'yyyy-MM-dd')
    },
    getList(page, params) {
      if (this.depIds.length == 2) {
        this.searchForm.depId = this.depIds[1];
      }
      this.tableLoading = true;
      let obj = params
      if(params && params.downTime){
        obj = JSON.parse(JSON.stringify(params))
        delete obj.downTime
        delete obj.areaIdArray
      }
      fetchISCList(
        Object.assign(
          {
            descs: "create_time",
            current: page.currentPage,
            size: page.pageSize
          },
          obj
        )
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });

      this.tableLoading = false;
    },
    deviceTask(row) {
      var params = { taskId: row.taskId, id: row.id };
      updateTaskStauts(params).then(response => {
        this.getList(this.page, this.searchForm);
      });
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
      if (this.searchForm.areaIdArray.length > 0) {
        form.areaId = this.searchForm.areaIdArray[this.searchForm.areaIdArray.length - 1];
      }else{
        form.areaId = null
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
        // 重新设置默认时间为最近7天
        const endTime = dateFormat(new Date(), 'yyyy-MM-dd')
        const startTime = this.setStartTime(endTime)
        this.searchForm.downTime = [startTime, endTime]
        this.searchForm.startTime = startTime
        this.searchForm.endTime = endTime
        this.getList(this.page, this.searchForm);
      }
    }
  }
};
</script>
