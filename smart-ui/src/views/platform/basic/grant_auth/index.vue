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
          </div>
        </div>

        <!-- 搜索条件 -->
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="姓名" prop="personName">
            <el-input v-model="searchForm.personName" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="工号" prop="personNum">
            <el-input v-model="searchForm.personNum" placeholder="请输入" clearable></el-input>
          </el-form-item>
        </el-form>

        <!-- 列表 -->
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" size="mini" @click="handleDelete(scope.row, scope.index)">解除授权</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, authDelete } from './_service.js'
export default {
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        parkId: null,
        personName: null,
        personNum: null
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
  computed: {},
  methods: {
    async getList() {
      var _this = this
      _this.tableLoading = true
      fetchList(
        Object.assign({
          current: _this.page.currentPage,
          size: _this.page.pageSize,
          parkId: _this.searchForm.parkId,
          name: _this.searchForm.personName,
          badge: _this.searchForm.personNum
        })
      ).then((response) => {
        _this.tableData = response.data.data.records
        _this.page.total = response.data.data.total
        _this.tableLoading = false
      })
      _this.tableLoading = false
    },
    handleDelete(row) {
      var _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '是否确认解除授权？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          return authDelete(row.id)
        })
        .then((response) => {
          var msg = response.data.msg
          var dataResult = response.data.data
          if (dataResult === true) {
            _this.getList(this.page)
            _this.$notify({
              title: '成功',
              message: '解除授权成功',
              type: 'success',
              duration: 2000
            })
          } else if (dataResult === false) {
            _this.$notify({
              title: '解除授权失败',
              message: msg,
              type: 'error',
              duration: 2000
            })
          }
        })
        .catch(error => { console.error(error) })
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
const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menu: true,
  menuAlign: 'center',
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  searchSize: 'mini',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  selection: false,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '授权平台',
      prop: 'from'
    },
    {
      label: '园区',
      prop: 'parkName'
    },
    {
      label: '工号',
      prop: 'badge'
    },
    {
      label: '姓名',
      prop: 'staffName'
    },
    {
      label: '授权ID',
      prop: 'openId'
    },
    {
      label: '创建时间',
      prop: 'createTime'
    }
  ]
}
</script>
<style lang="scss" scoped>
.lock_bind {
  min-width: 1120px;
}
</style>