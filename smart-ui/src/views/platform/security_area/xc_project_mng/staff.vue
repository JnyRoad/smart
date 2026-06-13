<!--设备标签 -->
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
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加员工</el-button>
            <el-button type="primary" @click="handelImport" :loading="importLoading" icon="icon-yutong-upload">导入excel</el-button>
            <el-button type="primary" @click="handelDelBatch" icon="el-icon-delete" plain>批量删除</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="mixinSearchForm" class="topForm" size="mini">
          <el-form-item label="工号" prop="staffBadge">
            <el-input v-model="mixinSearchForm.staffBadge" placeholder="请输入" clearable @keyup.enter.native="mixinSearchSubmit(mixinSearchForm)"></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="staffName">
            <el-input v-model="mixinSearchForm.staffName" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属园区/BU/部门" prop="deptIds">
            <deptCascader v-model="deptIds" :changeOnSelect="true" placeholder="请选择"></deptCascader>
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
        </avue-crud>
      </section>
    </el-scrollbar>
    <AddStaffDialog title="添加员工" :securityId="id" :parkId="parkId" ref="addStaffDialog" @refresh="refresh"/>
    <DlgImportPerson :securityId="id" :parkId="parkId" ref="dlgImportPerson" @refresh="refresh"/>
  </div>
</template>

<script>
import { xcProjectApi } from "./_service"
import AddStaffDialog from './components/AddStaffDialog'
import DlgImportPerson from './components/dlg_importPerson'

export default {
  mixins: [tce.mixins.list],
  components: {
    AddStaffDialog,
    DlgImportPerson
  },
  data() {
    return {
      importLoading: false,
      tableData: [],
      listOption: listOption(),
      curObj: {},
      deptIds: [],
      staffIdArr: [],
      id: undefined,
      parkId: ''
    };
  },
  created() {
    this.id = this.$route.query.id
    this.parkId = this.$route.query.parkId
    this.refresh()
  },
  watch: {
    deptIds(val){
      this.mixinSearchForm.parkId = undefined
      this.mixinSearchForm.buId = undefined
      this.mixinSearchForm.depId = undefined
      if(val && val.length===3){
        this.mixinSearchForm.parkId = val[0]
        this.mixinSearchForm.buId = val[1]
        this.mixinSearchForm.depId = val[2]
      }else if(val && val.length===2){
        this.mixinSearchForm.parkId = val[0]
        this.mixinSearchForm.buId = val[1]
      }else if(val && val.length===1){
        this.mixinSearchForm.parkId = val[0]
      }
    }
  },
  mounted: function() {},
  methods: {
    /**
     * 返回
     */
    goBack() {
      this.$router.push({
        path: `/platform/security_area/securityProjectMng`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    },
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
    handleAdd(){
      this.$refs.addStaffDialog.open()
    },
    refresh(){
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    async getList(page, params) {
      this.mixinTableLoading = true
      const res = await  xcProjectApi.getPersonList({
        current: page.currentPage,
        size: page.pageSize,
        },
        Object.assign({
          securityId: this.id
        }, params)
      )
      this.tableData = res.data.data.records
      this.mixinPage.total = res.data.data.total
      this.mixinTableLoading = false;
    },
    /**
     * 批量删除
     */
    async handelDelBatch(row) {
      // if(this.staffIdArr.length===0){
      //   this.$message.error('请选择要删除的员工信息')
      //   return
      // }
      //批量删除时，可根据筛选条件删除，或者根据复选框，复选框优先级高
      let obj = Object.assign(
        { relationId: this.staffIdArr },
        this.mixinSearchForm
      )
      await this.mixinMsgDel('确认要批量删除人员信息？')
      const res = await xcProjectApi.delPersonFromProjectBatch( obj )
      if(res.data.code===0){
        this.$refs.searchForm && this.$refs.searchForm.resetFields()
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
     * 导入excel
     */
    handelImport(){
      this.$refs.dlgImportPerson && this.$refs.dlgImportPerson.open()
    },
    /**
     * 重置搜索条件
     */
    mixinResetFrom(formName){
      this.$refs[formName] && this.$refs[formName].resetFields()
      this.deptIds = []
      this.mixinPage.currentPage = 1
      this.getList(this.mixinPage)
    }
  }
};
const listOption = function () {
  return {
    menu: false,
    selection: true,
    column: [
      {
        label: '工号',
        prop: 'staffBadge'
      },{
        label: '姓名',
        prop: 'staffName'
      },{
        label: 'BU',
        prop: 'compName'
      },{
        label: '部门',
        prop: 'depName'
      },{
        label: '岗位',
        prop: 'jobName'
      },{
        label: '入职时间',
        prop: 'inTime'
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
