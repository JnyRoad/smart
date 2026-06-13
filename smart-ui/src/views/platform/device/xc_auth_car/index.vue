<!--授权车辆 -->
<template>
  <div class="my-basic-container note_record">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="mixinSearchSubmit(mixinSearchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="mixinResetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button
              type="primary"
              @click="handelExport"
              :loading="exportLoading"
              icon="icon-yutong-download"
            >导出Excel</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="mixinSearchForm" class="topForm" size="mini">
          <el-form-item label="设备名称" prop="deviceName">
            <el-input v-model="mixinSearchForm.deviceName" placeholder="设备名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="车牌号" prop="plate">
            <el-input v-model="mixinSearchForm.plate" placeholder="车牌号" clearable></el-input>
          </el-form-item>
          <el-form-item label="所在区域" prop="areaIdArray">
            <areaCascader v-model="mixinSearchForm.areaIdArray" :changeOnSelect="true" placeholder="所在区域"></areaCascader>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="mixinPage"
          :data="tableData"
          :table-loading="mixinTableLoading"
          @size-change="mixinSizeChange"
          @current-change="mixinCurrentChange"
          :option="mixinListOptionConf"
        >
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-delete" @click="handleDel(scope.row, scope.$index)">删除</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { xcAuthCarApi } from "./_service"
import { delObj } from "@/api/platform/device/vehicle_list";

export default {
  mixins: [tce.mixins.list],
  data() {
    return {
      exportLoading: false,
      mixinSearchForm: {
        areaIdArray: []
      },
      tableData: [],
      listOption: listOption()
    };
  },
  created() {
    this.refresh()
  },
  mounted: function() {},
  methods: {
    refresh(){
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    async getList(page, params) {
      this.mixinTableLoading = true
      const res = await  xcAuthCarApi.getList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
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
      xcAuthCarApi.exportApi().then(res => {
        var fileDownload = require("js-file-download");
        fileDownload(res.data, "授权车辆导出.xls");
      }).finally(f=>{
        this.exportLoading = false
      })
    },
    /**
     * 删除
     */
    async handleDel(row, index) {
      await this.mixinMsgDel('确认删除该授权信息？')
      const res = await delObj(
        {
          cardNo: row.cardNo,
          deviceCode: [row.deviceCode],
          serialNo: undefined
        }
      )
      if(res.data.code===0){
        this.refresh()
        this.$notify({
          title: "操作成功",
          message: "已加入删除队列中",
          type: "success",
          duration: 2000
        })
      }else{
        this.$message.error(res.data.message);
      }
    }
  }
};
const listOption = function () {
  return {
    menu: true,
    menuWidth: '100px',
    column: [
      {
        label: '设备名称',
        prop: 'deviceName',
      },
      {
        label: '所在区域',
        prop: 'areaName',
      },
      {
        label: '车牌号',
        prop: 'plate',
      },
      {
        label: '创建时间',
        prop: 'createTime',
        width: 170
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
