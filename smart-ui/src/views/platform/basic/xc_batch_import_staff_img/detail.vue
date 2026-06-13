<!--导入照片，详情 -->
<template>
  <div class="my-basic-container note_record">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-left">
            <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
          </div>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="mixinSearchSubmit(mixinSearchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="mixinResetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button type="primary" icon="icon-yutong-download" @click="handelExport" :loading="exportLoading">导出Excel</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="mixinSearchForm" class="topForm" size="mini">
          <el-form-item label="状态" prop="status">
            <el-select v-model="mixinSearchForm.status" placeholder="请选择" clearable>
              <el-option
                v-for="item in staffStatus"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div>
          <span style="padding-right: 20px;">所属园区：{{ parkName }}</span>
          <span>任务名称：{{ taskName }}</span>
        </div>
        <avue-crud
          ref="crud"
          :page="mixinPage"
          :data="tableData"
          :table-loading="mixinTableLoading"
          @size-change="mixinSizeChange"
          @current-change="mixinCurrentChange"
          :option="mixinListOptionConf"
        >
        </avue-crud>
      </section>
    </el-scrollbar>
    <importImg ref="importImg"></importImg>
  </div>
</template>

<script>
import { xcBatchImgApi } from "./_service"
import importImg from "./components/import"
const staffStatusOption = [
  { label: "下发成功", value: 1 },
  { label: "下发失败", value: 2 },
  { label: "下发中", value: 3 }
];
export default {
  mixins: [tce.mixins.list],
  components: {
    importImg
  },
  data() {
    return {
      staffStatus: staffStatusOption,
      tableData: [],
      listOption: listOption(),
      exportLoading: false,
      taskId: undefined,
      parkName: undefined,
      taskName: undefined
    };
  },
  created() {
    this.taskId = this.$route.query.id
    this.parkName = this.$route.query.parkName
    this.taskName = this.$route.query.taskName
    this.refresh()
  },
  mounted: function() {},
  methods: {
    refresh(){
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    async getList(page, params) {
      this.mixinTableLoading = true
      const res = await xcBatchImgApi.getObj(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize,
            taskId: this.taskId
          },
          params
        )
      )
      this.tableData = res.data.data.records
      this.mixinPage.total = res.data.data.total
      this.mixinTableLoading = false;
    },
    handelExport(){
      this.exportLoading = true
      let obj = {
        taskId: this.taskId,
        status: this.mixinSearchForm.status
      }
      xcBatchImgApi.exportDetail( obj ).then(res => {
        var fileDownload = require("js-file-download");
        fileDownload(res.data, "照片导入.xls");
      }).finally(f=>{
        this.exportLoading = false
      })
    },
    goBack() {
      let path = '/platform/basic/xc_batchImportImg'
      this.$router.push({
        path: path,
        query: {}
      })
    }
  }
};
const listOption = function () {
  return {
    menu: false,
    column: [
      {
        label: '照片名称',
        prop: 'imgName'
      },
      {
        label: '状态',
        prop: 'statusDesc'
      },
      {
        label: '描述',
        prop: 'remark'
      },
      {
        label: '下发时间',
        prop: 'createTime'
      }
    ]
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-scrollbar__wrap {
  overflow-x: auto;
}
</style>
