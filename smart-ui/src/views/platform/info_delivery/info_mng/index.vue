<!--出入记录：物品方向 -->
<template>
  <div class="my-basic-container">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addInfo(searchForm)">添加信息资源</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="资源名称" prop="infoName">
            <el-input v-model="searchForm.infoName" placeholder="资源名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="发布类型" prop="type">
            <el-select v-model="searchForm.type" placeholder="发布类型" clearable>
              <el-option v-for="item in typeSelectData" :key="item.value" :value="item.code" :label="item.desc"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="发布状态" prop="status">
            <el-select v-model="searchForm.status" placeholder="发布状态" clearable>
              <el-option v-for="item in statusSelectData" :key="item.label" :value="item.value" :label="item.label"></el-option>
            </el-select>
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
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row, scope.$index, 1)">修改</el-button>
            <el-button type="text" icon="el-icon-delete" @click="deleteData(scope.row)">删除</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { returnApi } from './_service'
import { mapGetters } from 'vuex'

export default {
  name: 'article',
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        status: null,
        infoName: null,
        type: null,
        timeSlot: null
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
      tableOption: tableOption,
      statusSelectData: [
        {label: '已发布', value: 0},
        {label: '未发布', value: 2},
        ],
      typeSelectData: [],
    }
  },
  watch: {},
  created() {
    // this.getSelectStatus()
    this.getSelectType()
    this.$nextTick(() => {
      // 详情带参数返回
      if (this.$route.query.queryForm != undefined) {
        let queryPage = this.$route.query.queryPage
        let queryForm = this.$route.query.queryForm
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {})
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {})
        }
        this.getList(this.page, this.searchForm)
      } else {
        this.getList(this.page, this.searchForm)
      }
    })
  },
  mounted: function () {},
  computed: {
    ...mapGetters(['permissions'])
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true
      returnApi
      .getList({
            current: page.currentPage,
            size: page.pageSize
          },
          params
      )
        .then((response) => {
          this.tableData = response.data.data.records
          this.page.total = response.data.data.total
          this.tableLoading = false
        })
      this.tableLoading = false
    },
    async getSelectStatus(){
      const res = await returnApi.getSelectStatus()
      this.statusSelectData = res.data.data
    },
    async getSelectType(){
      const res = await returnApi.getSelectType()
      this.typeSelectData = res.data.data
    },
    addInfo(){
      this.$router.push({
        path: `/platform/info_delivery/info_mng/add`,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    handleEdit(row, index, isDetail) {
      const src = `/platform/info_delivery/info_mng/edit/${row.id}`
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      })
    },
    deleteData(row) {
      var _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除该资源信息？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          return returnApi.deleteData(row.id)
        })
        .then((dataResponse) => {
          _this.getList(_this.page, _this.searchForm)
          _this.$notify({
            title: '删除成功',
            message: '删除成功',
            type: 'success',
            duration: 2000
          })
        })
        .catch(error => { console.error(error) })
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1
      if(form.timeSlot && form.timeSlot != null){
        form.startTime = form.timeSlot[0]
        form.endTime = form.timeSlot[1]
      }else{
        form.startTime = null
        form.endTime = null
      }
      this.getList(this.page, form)
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page, this.searchForm)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.searchForm)
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields()
        this.page.currentPage = 1
        this.getList(this.page, this.searchForm)
      }
    }
  }
}
const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 200,
  labelWidth: 100,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  searchSize: 'mini',
  dialogWidth: '600px',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [
    {
      label: '资源名称',
      prop: 'infoName'
    },
    {
      label: '发布类型',
      prop: 'typeDesc'
    },
    {
      label: '发布状态',
      prop: 'statusDesc'
    },
    {
      label: '发布终端',
      prop: 'terminalName'
    },
    {
      label: '创建时间',
      prop: 'createTime',
      width: 170
    },
    {
      label: '创建人',
      prop: 'creator',
    }
  ]
}
</script>

<style lang="scss" scoped>
.topForm ::v-deep {
  .el-form-item__label {
    width: 130px;
  }
}
.tabs ::v-deep {
  flex: 1;
  .el-tabs__item {
    height: 50px;
  }
}
</style>
