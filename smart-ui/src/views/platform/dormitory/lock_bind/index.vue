<!--
- @name 门锁绑定
-->
<template>
  <div class="my-basic-container lock_bind">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <!-- 操作菜单 -->
        <div class="top-menu clear" style="min-height: 48px">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom" plain>清空</el-button>
          </div>
        </div>

        <!-- 搜索条件 -->
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="门锁名称" prop="name">
            <el-input v-model="searchForm.deviceName" placeholder="门锁名称" clearable></el-input>
          </el-form-item>
        </el-form>

        <!-- 列表 -->
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" size="mini" @click="bindRoom(scope.row)">绑定房间 </el-button>
          </template>
        </avue-crud>
      </section>
       <!-- 选择床位 -->
       <BindRoom ref="bindRoom" :row="listItem" @done="selectBed"/>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList
} from "./_service.js";
import { tableOption } from '@/const/crud/platform/dormitory/lock_bind'
import BindRoom from "./bind_room";
export default {
  components:{
    BindRoom
  },
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        deviceName: undefined
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      params: null,
      listItem:{},
    }
  },
  created() {
    this.getList(this.page)
  },
  mounted: function() {},
  computed: {},
  watch: {},
  methods: {
    async getList(page, params) {
      var _this = this
      this.tableLoading = true
      fetchList(
         Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize,
            deviceName: _this.searchForm.deviceName
          },
          params
        )
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false
      });
      this.tableLoading = false
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
    selectBed(){
      this.searchSubmit()
    },
    bindRoom(val) {
      this.listItem = val
      this.$refs.bindRoom && this.$refs.bindRoom.open()
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.page.currentPage = 1
      this.getList(this.page, this.params)
    },
    /**
     * 清空搜索
     */
    resetFrom() {
      this.searchForm.deviceName = null
      this.page.currentPage = 1
      this.getList(this.page, this.params)
    }
  }
}
</script>
<style lang="scss" scoped>
.lock_bind {
  min-width: 1120px;
}
</style>