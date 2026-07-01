<!--区域管理，考勤设备通关权限策略  -->
<template>
  <div class="my-basic-container limit">
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
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加通关权限</el-button>
          </div>
        </div>
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="searchForm.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="通关权限名称" prop="authorityName">
              <el-input v-model="searchForm.authorityName" placeholder="请输入通关权限名称" clearable></el-input>
            </el-form-item>
            <el-form-item label="类型" prop="areaType">
              <el-select v-model="searchForm.areaType" placeholder="请选择权限性质">
                <el-option label="公共区域" value="0"></el-option>
                <el-option label="保密区域" value="1"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
          @row-del="rowDel"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-edit"
              @click="handleEdit(scope.row,scope.$index)"
            >编辑</el-button>
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
            <el-button
              type="text"
              icon="el-icon-refresh"
              @click="handleAreaTypeSwitch(scope.row)"
            >变更性质</el-button>
            <el-button
              type="text"
              icon="el-icon-info"
              v-if="scope.row.type === 1"
              @click="handlePerson(scope.row,scope.row.$index)"
            >关联员工</el-button>
            <el-button
              type="text"
              icon="el-icon-info"
              v-else-if="scope.row.type === 3"
              @click="handleVehicle(scope.row,scope.row.$index)"
            >关联内部车辆</el-button>
          </template>
        </avue-crud>
        <AreaTypeSwitchDialog
          :visible.sync="areaTypeSwitchVisible"
          :authority="areaTypeSwitchTarget"
          @success="getList(page, searchForm)"
        />
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, delObj } from "@/api/platform/area/limit";
import { tableOption } from "@/const/crud/platform/area/limit";
import { mapGetters } from "vuex";
import AreaTypeSwitchDialog from './components/AreaTypeSwitchDialog.vue'

export default {
  name: "limit",
  components: { AreaTypeSwitchDialog },
  data() {
    return {
      searchForm: {
        parkId: "",
        authorityName: "",
        areaType: ""
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      areaTypeSwitchVisible: false,
      areaTypeSwitchTarget: {}
    };
  },
  created() {
    this.getList(this.page);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  watch: {
    $route() {
      this.getList();
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
            deviceUseType: 2 // 1-门禁，2-考勤
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
    handleAdd() {
      //点击添加
      const src = `/platform/area/limit/add`;
      this.$router.push({
        path: src,
        query: {
          deviceUseType: 2 // 1-门禁，2-考勤
        }
      });
    },
    handleEdit(row, index) {
      //点击编辑
      const src = `/platform/area/limit/edit/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          deviceUseType: 2 // 1-门禁，2-考勤
        }
      });
    },
    handleDel(row, index) {
      //删除
      //this.$refs.crud.rowDel(row, index);
      this.rowDel(row, index);
    },
    handleAreaTypeSwitch(row) {
      // 打开变更性质弹窗
      this.areaTypeSwitchTarget = row;
      this.areaTypeSwitchVisible = true;
    },
    handlePerson(row, index) {
      const src = `/platform/area/limit/personDetail/${row.id}/${row.type}`;
      this.$router.push({
        path: src,
        query: {
          name: row.authorityName,
          backPageTag: 'limitAttendance'
        }
      });
    },
    handleVehicle(row, index) {
      const src = `/platform/area/limit/vechileDetail/${row.id}/${row.type}`;
      this.$router.push({
        path: src
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
    rowDel: function(row, index) {
      let _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该权限策略信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj(row.id);
        })
        .then(dataResponse => {
          _this.getList(_this.page, _this.searchForm);
          if (dataResponse.data.data) {
            _this.$notify({
              title: "成功",
              message: "删除成功",
              type: "success",
              duration: 2000
            });
          } else {
            _this.$notify({
              title: "失败",
              message: dataResponse.data.msg,
              type: "fail",
              duration: 2000
            });
          }
          // _this.$notify({
          //   title: "成功",
          //   message:"删除成功",
          //   type: "success",
          //   duration: 2000
          // });
        })
        .catch(err => { console.error(err) });
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
        this.getList(this.page);
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.topForm ::v-deep {
  .el-form-item__label {
    width: 120px;
  }
}
</style>
