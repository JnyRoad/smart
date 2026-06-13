<!--设备管理，闸机和门禁的通关人员 -->
<template>
  <div class="my-basic-container device">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
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
            <el-input v-model="searchForm.plate" placeholder="车牌号" clearable></el-input>
          </el-form-item>
          <el-form-item label="车主姓名" prop="name">
            <el-input v-model="searchForm.name" placeholder="车主" clearable></el-input>
          </el-form-item>
          <!-- <el-form-item label="所属园区/BU/部门" prop="depIds">
            <el-cascader
                expand-trigger="hover"
                :options="options"
                :show-all-levels="false"
                :change-on-select="true"
                v-model="depIds">
              </el-cascader>
          </el-form-item>-->
        </el-form>
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
              icon="el-icon-delete"
              :loading="scope.row.status==1"
              @click="handleDel(scope.row,scope.$index)"
            >
              <template v-if="scope.row.status==1">删除中</template>
              <template v-if="scope.row.status==0">删除</template>
            </el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, delObj } from "@/api/platform/device/vehicle_list";
// import { getCompTree} from '@/api/platform/_publicService'
import { tableOption } from "@/const/crud/platform/device/vehicle_list";
import { mapGetters } from "vuex";
// import {isArrayFn} from '@/util/util'
export default {
  name: "device",
  data() {
    return {
      searchForm: {
        plate: "",
        name: "",
        depId: []
      },
      deviceId: "",
      tableLoading: false,
      tableData: [],
      depIds: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      options: [],
      serialNo: ''
    };
  },
  created: function() {
    this.deviceId = this.$route.params.id;
    this.serialNo = this.$route.query.serialNo;
    this.getList(this.page);
    // getCompTree().then(response => {
    //     this.options = response.data.data
    //     console.log(this.options);

    // })
  },
  watch: {
    depIds(newVal, oldVal) {
      // if( isArrayFn(newVal) && newVal.length > 0) {
      //   const depLength = newVal.length
      //   if( depLength == 3 ) {
      //     this.searchForm.depId = this.depIds[2];
      //     this.searchForm.compId = this.depIds[1];
      //     this.searchForm.parkId = this.depIds[0];
      //   }else if ( depLength >= 2 ) {
      //     this.searchForm.depId = undefined;
      //     this.searchForm.compId = this.depIds[1];
      //     this.searchForm.parkId = this.depIds[0];
      //   }else if ( depLength >= 1 ){
      //     this.searchForm.depId = undefined;
      //     this.searchForm.compId = undefined;
      //     this.searchForm.parkId = this.depIds[0];
      //   }
      // }else{
      //   this.searchForm.depId = undefined;
      //   this.searchForm.compId = undefined;
      //   this.searchForm.parkId = undefined;
      // }
    }
  },
  mounted: function() {},
  computed: {},
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/device/automatic`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    },
    getList(page, params) {
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            descs: "create_time",
            current: page.currentPage,
            size: page.pageSize,
            deviceId: this.deviceId
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
    handleDel(row, index) {
      //删除
      this.$refs.crud.rowDel(row, index);
    },
    rowDel: function(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该通关车辆信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj({
            cardNo: row.cardNo,
            deviceCode: [_this.deviceId],
            serialNo: _this.serialNo
          });
        })
        .then(dataResponse => {
          var msg = dataResponse.data.msg;
          var dataResult = dataResponse.data.code;
          if (dataResult == 0) {
            _this.getList(this.page, this.searchForm);
            _this.$notify({
              title: "操作成功",
              message: "已加入删除队列中",
              type: "success",
              duration: 2000
            });
          } else {
            _this.$notify({
              title: "操作失败",
              message: msg,
              type: "error",
              duration: 2000
            });
          }
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
        this.depIds = [];
        this.getList(this.page);
      }
    }
  }
};
</script>
