<!--园区服务：员工反馈 -->
<template>
  <div class="my-basic-container feed_back">
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
          <el-form-item label="反馈问题" prop="question">
            <el-input v-model="searchForm.question" placeholder="反馈问题" clearable></el-input>
          </el-form-item>
          <el-form-item label="处理状态" prop="status">
            <el-select v-model="searchForm.status" placeholder="处理状态" clearable>
              <el-option label="未处理" value="0"></el-option>
              <el-option label="已处理" value="1"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="反馈时间" prop="timeRange">
            <el-date-picker
              v-model="searchForm.timeRange"
              type="datetimerange"
              range-separator="-"
              value-format="yyyy-MM-dd HH:mm:ss"
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
          @size-change="sizeChange"
          @current-change="currentChange"
          :option="tableOption"
        >
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
import { fetchList } from "@/api/platform/park_service/feed_back";
import { tableOption } from "@/const/crud/platform/park_service/feed_back";

import { mapGetters } from "vuex";

export default {
  name: "feed_back",
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        question: "",
        timeRange: undefined,
        status: ""
      },
      reverve_status: 1,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableOption: tableOption,
      tableData: []
    };
  },
  created: function() {
    this.$nextTick(() => {
      if (this.$route.query.queryForm != undefined) {
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
  mounted: function() {},
  watch: {
    $route() {
      this.getList(this.page, this.searchForm);
    }
  },
  computed: {
    ...mapGetters(["permissions"]),

    startTime: function() {
      if (this.searchForm.timeRange) {
        return this.searchForm.timeRange[0];
      } else {
        return undefined;
      }
    },
    endTime: function() {
      if (this.searchForm.timeRange) {
        return this.searchForm.timeRange[1];
      } else {
        return undefined;
      }
    }
  },

  methods: {
    getList(page, params) {
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            descs: "create_time",
            current: page.currentPage,
            size: page.pageSize,
            startTime: this.startTime,
            endTime: this.endTime
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
      const src = `/platform/park_service/feed_back/detail/${row.id}`;
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
        this.$refs[formName].resetFields();
        this.page.currentPage = 1;
        this.getList(this.page, this.searchForm);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
