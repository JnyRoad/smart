<!--
- @name 开门记录
-->
<template>
  <div class="my-basic-container door_open">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <!-- 操作菜单 -->
        <div class="top-menu clear" style="min-height: 48px">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom" plain>清空</el-button>
            <el-button type="primary" icon="el-icon-download" @click="download" plain>导出excel</el-button>
          </div>
        </div>

        <!-- 搜索条件 -->
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="门锁名称" prop="deviceName">
            <el-input v-model="searchForm.deviceName" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="personName">
            <el-input v-model="searchForm.personName" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="工号" prop="personNum">
            <el-input v-model="searchForm.personNum" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="开门方式" prop="openType">
            <el-select v-model="searchForm.openType" placeholder="请选择" clearable>
              <el-option
                v-for="(item, index) in openTypes"
                :key="index"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-form>

        <!-- 列表 -->
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { tableOption } from '@/const/crud/platform/dormitory/door_open_record'
import {
  fetchList,
  exportShareData
} from "./_service.js";
export default {
  data() {
    return {
      openTypes:[
        {value: 1, label: '密码'},
        {value: 2, label: '指纹'},
        {value: 3, label: '卡片'},
        {value: 4, label: '远程开门'}
      ],
      searchForm: {
        //搜索菜单表单
        parkId: null,
        deviceName: null,
        personName: null,
        personNum: null,
        openType: null
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      params: null
    }
  },
  created() {
    this.getList()
  },
  computed: {
  },
  methods: {
    async getList() {
      var _this = this
      _this.tableLoading = true
      fetchList(
         Object.assign(
          {
            current: _this.page.currentPage,
            size: _this.page.pageSize,
            deviceName: _this.searchForm.deviceName,
            parkId: _this.searchForm.parkId,
            personName:  _this.searchForm.personName,
            personNum:  _this.searchForm.personNum,
            openType:  _this.searchForm.openType
          }
        )
      ).then(response => {
        _this.tableData = response.data.data.records;
        _this.page.total = response.data.data.total;
        _this.tableLoading = false
      });
      _this.tableLoading = false
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page, this.params)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.params)
    },
    //导出表格
    async download() {
      const _this = this
      const obj = {
        current: 1,
        size: 99999,
        deviceName: _this.searchForm.deviceName,
        parkId: _this.searchForm.parkId,
        personName:  _this.searchForm.personName,
        personNum:  _this.searchForm.personNum,
        openType:  _this.searchForm.openType
      }
      require.ensure([], () => {
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "门锁名称",
          "绑定房间",
          "人员编号",
          "姓名",
          "开门方式",
          "开门时间"
        ];
        const filterVal = [
          "deviceName",
          "deviceArea",
          "personNum",
          "personName",
          "openTypeDesc",
          "openTime"
        ];
        fetchList(obj)
          .then(response => {
            // console.log('response',response.data)
            const list = response.data.data.records

            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, "开门记录统计表");

            // const fileDownload = require("js-file-download");
            // fileDownload(data, "开门记录统计表.xls");
          })
          .catch(err => { console.error(err) });
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.page.currentPage = 1
      this.getList()
    },
    /**
     * 清空搜索
     */
    resetFrom() {
      this.$refs['searchForm'].resetFields()
      this.page.currentPage = 1
      this.getList()
    }
  }
}
</script>
<style lang="scss" scoped>
.lock_bind {
  min-width: 1120px;
}
</style>