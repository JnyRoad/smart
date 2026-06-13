<!--出入记录：物品方向 -->
<template>
  <div class="my-basic-container vehicle">
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
            <el-form-item label="申请人" prop="name">
              <el-input v-model="searchForm.name" placeholder="申请人" clearable></el-input>
            </el-form-item>
            <el-form-item label="车牌号" prop="licensePlate">
              <el-input v-model="searchForm.licensePlate" placeholder="车牌号" clearable></el-input>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="searchForm.status" placeholder="状态" clearable>
                <el-option label="待审批" value="1"></el-option>
                <el-option label="通过" value="2"></el-option>
                <el-option label="拒绝" value="3"></el-option>
                <el-option label="已出厂" value="4"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="申请时间" prop="queryTime">
              <el-date-picker
                v-model="snapTime"
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
            >详情</el-button>

            <el-button
              v-if="scope.row.status === 2"
              type="text"
              icon="el-icon-edit"
              @click="handleStatus(scope.row,scope.$index)"
            >确认出厂</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, updateStatus } from "@/api/platform/entrance/article";
import { tableOption } from "@/const/crud/platform/entrance/article";
import { getCompTree } from "@/api/platform/_publicService";
import { isArrayFn } from "@/util/util";
import { mapGetters } from "vuex";

export default {
  name: "article",
  data() {
    return {
      snapTime: [],
      searchForm: {
        //搜索菜单表单
        parkId: "",
        compId:"",
        depId:"",
        name: "",
        licensePlate: "",
        status: "",
        startTime: "",
        endTime: ""
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [],
      depIds: [],
      compOptions: [],
      tableOption: tableOption
    };
  },
  watch: {
    depIds(newVal) {
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
    getCompTree().then(response => {
      this.compOptions = response.data.data;
    });
    this.$nextTick(() => {
      // 详情带参数返回
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
        this.getList(this.page);
      }
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),
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
              current: page.currentPage,
              size: page.pageSize
            },params
          )
        ).then(response => {
          this.tableData = response.data.data.records;
          this.page.total = response.data.data.total;
          this.tableLoading = false;
        });
        this.tableLoading = false;
    },
    handleDetail(row, index) {
      const src = `/platform/entrance/article/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    handleStatus: function(row, index) {
      let _this = this
      this.$confirm('是否确认已经出厂', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        return updateStatus({id: row.id, status: 4})
      }).then(data => {
        _this.$message({
          showClose: true,
          message: '确认成功',
          type: 'success'
        })
        this.getList(this.page)
      }).catch(function (error) { console.error(error) })
    },
      /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, form);
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
