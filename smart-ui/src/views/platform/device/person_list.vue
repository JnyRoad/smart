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
          <el-form-item label="姓名" prop="name">
            <el-input v-model="searchForm.name" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="工号" prop="badge">
            <el-input v-model="searchForm.badge" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="创建时间" prop="timeSlot">
            <el-date-picker
              v-model="searchForm.timeSlot"
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
          @size-change="sizeChange"
          @current-change="currentChange"
          @row-del="rowDel"
          :option="tableOption"
        >
          <template slot-scope="scope" slot="faceImage">
            <div class="img-outer">
              <div class="img-inner">
                <viewer>
                  <img :src="scope.row.faceImage || errorImgPeaple()" :onerror="errorImgPeaple()" />
                </viewer>
              </div>
            </div>
          </template>
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
            <viewer style="display: none;" :options="vOptions">
              <img ref="curImg" :src="curImgUrl || errorImgPeaple()" :onerror="errorImgPeaple()" />
            </viewer>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, delObj } from "@/api/platform/device/person_list";
import { tableOption } from "@/const/crud/platform/device/person_list";
import { mapGetters } from "vuex";

export default {
  name: "device",
  data() {
    return {
      vOptions: {
        inline: false,
        button: true,
        navbar: false,
        title: false,
        toolbar: false,
        tooltip: false,
        movable: true,
        zoomable: true,
        rotatable: true,
        scalable: true,
        transition: true,
        fullscreen: false,
        keyboard: true,
        url: "data-source"
      },
      curImgUrl: "",
      searchForm: {
        name: "",
        badge: "",
        timeSlot:null,
        startTime:null,
        endTime: null
      },
      deviceId: "",
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      serialNo: '',
      deviceType: undefined
    };
  },
  created: function() {
    this.deviceId = this.$route.params.id;
    this.serialNo = this.$route.query.serialNo;
    this.deviceType = this.$route.query.deviceType;
    this.getList(this.page);
  },
  mounted: function() {},
  computed: {},
  methods: {
    goBack() {
      let path = ''
      if(this.deviceType===1){
        path = `/platform/device/gate`
      }else if(this.deviceType===2){
        path = `/platform/device/device_guard`
      }else if(this.deviceType===3){
        path = `/platform/device/attendance`
      }else if(this.deviceType===4){
        path = `/platform/device/entrance_guard`
      }
      this.$router.push({
        path: path,
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
          elm("span", null, "确认删除该通关人员信息？ ")
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
          // console.log(dataResult);
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
      if(form.timeSlot && form.timeSlot != null){
        form.startTime = form.timeSlot[0]
        form.endTime = form.timeSlot[1]
      }else{
        form.startTime = null
        form.endTime = null
      }
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
.img-outer {
  width: 80px;
  height: 80px;
  padding-top: 0;
  background: #f7f7f7;
  .img-inner {
    background: #f7f7f7;
    top: 2px;
    right: 2px;
    bottom: 2px;
    left: 2px;
  }
}
</style>
