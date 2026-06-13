<!--停车场管理，停车记录 -->
<template>
  <div class="my-basic-container parking">
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
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="车牌号" prop="vehiclePlate">
            <el-input v-model="searchForm.vehiclePlate" placeholder="车牌号" clearable></el-input>
          </el-form-item>
          <el-form-item label="车主" prop="driverName">
            <el-input v-model="searchForm.driverName" placeholder="车主" clearable></el-input>
          </el-form-item>
          <el-form-item label="出入类型" prop="eventType">
            <el-select v-model="searchForm.eventType" placeholder="出入类型" clearable>
              <el-option label="进" value="1"></el-option>
              <el-option label="出" value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="出入时间" prop="snapTime">
            <el-date-picker
              v-model="snapTime"
              type="daterange"
              range-separator="-"
              value-format="yyyy-MM-dd"
              start-placeholder="起始时间"
              end-placeholder="截止时间"
              clearable
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
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index)"
            >查看</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList } from "@/api/platform/parking/parking_record";
import { tableOption } from "@/const/crud/platform/parking/parking_record";
import { mapGetters } from "vuex";

export default {
  name: "parking",
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        vehiclePlate: "",
        driverName: "",
        eventType: "",
        startTime: "",
        endTime: "",
        AllFlag: 1,
        parkId: ""
      },
      snapTime: [],
      test: [
        {
          label: "已驶离车辆",
          value: "0"
        },
        {
          label: "当前车辆",
          value: "1"
        }
      ],
      parking_space: {
        total_count: 600,
        use_count: 200
      },
      progress_width: 25,
      stroke_width: 3,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption
    };
  },
  created: function() {
    this.getList(this.page);
  },
  mounted: function() {},
  computed: {
    unuse_count: function() {
      //剩余车位数量
      return this.parking_space.total_count - this.parking_space.use_count;
    },
    unuse_percent: function() {
      //剩余车位百分比
      return (this.unuse_count / this.parking_space.total_count) * 100;
    },
    use_percent: function() {
      //已用车辆百分比
      return (
        (this.parking_space.use_count / this.parking_space.total_count) * 100
      );
    },
    ...mapGetters(["permissions"])
  },
  methods: {
    getList(page, params) {
      if (this.snapTime.length == 2) {
        this.searchForm.startTime = this.snapTime[0];
        this.searchForm.endTime = this.snapTime[1];
      }
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            descs: "create_time",
            current: page.currentPage,
            size: page.pageSize,
            allFlag: 1
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
    handleDetail(row, index) {
      const src = `/platform/parking/present_parking/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {}
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
        this.snapTime = [];
        this.$refs[formName].resetFields();
        this.page.currentPage = 1;
        this.getList(this.page);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
