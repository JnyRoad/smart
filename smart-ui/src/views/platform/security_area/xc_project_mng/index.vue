<!--设备标签 -->
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
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加保密项目</el-button>
            <el-button type="primary" @click="handelDelBatch" icon="el-icon-delete" plain>批量删除</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="mixinSearchForm" class="topForm" size="mini">
          <el-form-item label="项目名称" prop="securityName">
            <el-input v-model="mixinSearchForm.securityName" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="mixinSearchForm.parkId" :canClearable="true"></parkSelect>
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
          @selection-change="selectChange"
        >
          <template slot-scope="scope" slot="authNameList">
            <span v-if="scope.row.authNameList">{{scope.row.authNameList.toString()}}</span>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row,scope.$index)" >修改项目</el-button>
            <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row,scope.$index)" >查看权限</el-button>
            <el-button type="text" @click="handleStaff(scope.row, scope.$index)">授权员工</el-button>

          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <AddDialog :title="dlgTitle" :itemObj="curObj" ref="addDialog" @refresh="refresh"/>
    <CkAuth title="查看权限" :itemObj="curObj" ref="ckAuth"/>

  </div>
</template>

<script>
import { xcProjectApi } from "./_service"
import AddDialog from './components/AddDialog'
import CkAuth from './components/ckAuth'

import projectName from './components/project_name'

export default {
  mixins: [tce.mixins.list],
  components: {
    AddDialog,
    CkAuth,
    projectName
  },
  data() {
    return {
      tableData: [],
      listOption: listOption(),
      dlgTitle: '',
      curObj: {},
      staffIdArr: []
    };
  },
  created() {
    this.refresh()
  },
  mounted: function() {},
  methods: {
    /**
     * 多选事件
     */
    selectChange(val) {
      this.staffIdArr = []
      if (val.length > 0) {
        val.forEach(function(element) {
          this.staffIdArr.push(element.id)
        }, this)
      }
    },
    /**
     * 批量删除
     */
    async handelDelBatch(row) {
      if(this.staffIdArr.length===0){
        this.$message.error('请选择要删除的员工信息')
        return
      }
      await this.mixinMsgDel('确认删除选中信息？')
      const res = await xcProjectApi.delObjProjectBatch({
        id: this.staffIdArr
      })
      if(res.data.code===0){
        this.refresh()
        this.$notify({
          title: "批量删除成功",
          message: "批量删除成功",
          type: "success",
          duration: 2000
        });
      }else{
        this.$message.error(res.data.message);
      }
    },
    /**
     * 查看权限
     */
    handleDetail(row){
      this.curObj = Object.assign(row)
      this.$refs.ckAuth && this.$refs.ckAuth.open()
    },
    /**
     * 添加保密项目
     */
    handleAdd(){
      this.curObj = null
      this.dlgTitle = '添加'
      this.$refs.addDialog.open()
    },
    /**
     * 修改项目
     */
    handleEdit(row){
      this.curObj = Object.assign(row)
      this.dlgTitle = '编辑'
      this.$refs.addDialog.open()
    },
    refresh(){
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    async getList(page, params) {
      this.mixinTableLoading = true
      if(!params){
        params = {}
      }
      const res = await  xcProjectApi.getList( params,
        {
          current: page.currentPage,
          size: page.pageSize
        }
      )
      this.tableData = res.data.data.records
      this.mixinPage.total = res.data.data.total
      this.mixinTableLoading = false;
    },
     /**
     * 授权员工
     */
    handleStaff(row) {
      this.$router.push({
        path: `/platform/security_area/securityProjectMng/staff/${row.id}`,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm,
          id: row.id,
          parkId: row.parkId
        }
      });
    }
  }
};
const listOption = function () {
  return {
    menu: true,
    menuWidth: 280,
    selection: true,
    column: [
      {
        label: '所属园区',
        prop: 'parkName'
      },
      {
        label: '保密项目名称',
        prop: 'securityName'
      },{
        label: '保密项目代码',
        prop: 'securityCode'
      },{
        label: '项目门禁授权',
        prop: 'authNameList',
        solt: true
      },{
        label: '创建时间',
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
