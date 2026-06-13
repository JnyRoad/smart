<!--基础信息：工资签收管理 -->
<template>
  <div class="my-basic-container staff">
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
          <el-form-item label="工号" prop="badge">
            <el-input v-model="searchForm.badge" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="searchForm.name" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属园区/BU/部门" prop="depId">
            <el-cascader
              expand-trigger="hover"
              :options="options"
              :show-all-levels="false"
              :change-on-select="true"
              v-model="depIds"
              clearable
            ></el-cascader>
          </el-form-item>
          <el-form-item label="工资月份" prop="wageDate">
            <el-date-picker
              v-model="searchForm.wageDate"
              type="month"
              value-format="yyyy-MM"
              placeholder="工资月份"
              clearable
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="签收日期" prop="createTime">
            <el-date-picker
              v-model="searchForm.createTime"
              type="datetimerange"
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
import { fetchList } from "@/api/platform/basic/salary_sign";
import { getCompTree } from "@/api/platform/_publicService";
import { tableOption } from "@/const/crud/platform/basic/salary_sign";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
const salaryStatusOption = [
  { label: "未签收", value: 0 },
  { label: "已签收", value: 1 }
];
export default {
  name: "salary_sign",
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        badge: undefined,
        name: undefined,
        compId: undefined,
        depId: undefined,
        wageDate: undefined,
        createTime: undefined,
        parkId: undefined
      },
      salaryStatus: salaryStatusOption,
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      options: [],
      depIds: [],
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    };
  },
  created() {
    this.getList(this.page);
    getCompTree().then(response => {
      this.options = response.data.data;
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
    startTime: function() {
      if (this.searchForm.createTime) {
        return this.searchForm.createTime[0];
      } else {
        return undefined;
      }
    },
    endTime: function() {
      if (this.searchForm.createTime) {
        return this.searchForm.createTime[1];
      } else {
        return undefined;
      }
    }
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true;
      params = Object.assign(
        {
          descs: "create_time",
          current: page.currentPage,
          size: page.pageSize,
          startTime: this.startTime,
          endTime: this.endTime
        },
        params
      );

      if (isArrayFn(params.createTime)) {
        //日期数组
        params.createTime = params.createTime.join();
      }
      fetchList(params).then(response => {
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
      const src = `/platform/basic/salary_sign/detail/${row.id}`;
      this.$router.push({
        path: src
      });
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.page.currentPage = 1;
      this.getList(this.page, this.searchForm);
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      this.searchForm.compId = undefined;
      this.searchForm.depId = undefined;
      this.searchForm.badge = undefined;
      this.searchForm.name = undefined;
      this.searchForm.wageDate = undefined;
      this.searchForm.createTime = undefined;
      this.depIds = [];
      this.page.currentPage = 1;
      this.getList(this.page);
      // if(this.$refs[formName]!=undefined){
      //   this.$refs[formName].resetFields();
      //   alert(2);
      //   this.page.currentPage = 1;
      //   this.getList(this.page);
      // }
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
::v-deep .el-scrollbar__wrap {
  overflow-x: auto;
}
</style>
