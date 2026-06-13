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
            <el-button type="primary" @click="handleBatchSet">批量设置保密项目</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="mixinSearchForm" class="topForm" size="mini">
          <el-form-item label="工号" prop="badges">
            <el-input v-model="mixinSearchForm.badges" placeholder="多个工号请用空格分隔" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="staffName">
            <el-input v-model="mixinSearchForm.staffName" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属园区/BU/部门" prop="deptIds">
            <deptCascader v-model="deptIds" :changeOnSelect="true" placeholder="请选择"></deptCascader>
          </el-form-item>
          <el-form-item label="状态" prop="signStatus">
            <el-select v-model="mixinSearchForm.signStatus" clearable>
              <el-option label="已签署" :value="1"></el-option>
              <el-option label="未签署" :value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="入职时间" prop="times">
            <el-date-picker
              v-model="times"
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
          :page="mixinPage"
          :data="tableData"
          :table-loading="mixinTableLoading"
          @size-change="mixinSizeChange"
          @current-change="mixinCurrentChange"
          :option="mixinListOptionConf"
          @selection-change="selectChange"
        >
          <template slot-scope="scope" slot="createTime">
            {{scope.row.createTime|dateFormat2}}
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-view" @click="handleCheckProject(scope.row,scope.$index)" >查看项目</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <BatchProjectDialog title="批量设置保密项目" :itemObj="staffArr" ref="batchProjectDialog" @refresh="refresh"/>
    <CheckProjectDialog title="查看项目" :itemObj="curObj" ref="checkProjectDialog" @refresh="refresh"/>

  </div>
</template>

<script>
import { xcSignMngApi } from "./_service"
import BatchProjectDialog from './components/batchProject'
import CheckProjectDialog from './components/checkProject'

export default {
  mixins: [tce.mixins.list],
  components: {
    BatchProjectDialog,
    CheckProjectDialog
  },
  data() {
    return {
      deptIds: [],
      times: [],
      tableData: [],
      listOption: listOption(),
      curObj: {},
      staffIdArr: [],
      staffArr: []
    };
  },
  created() {
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
    },
    times(val){
      this.mixinSearchForm.startDate = undefined
      this.mixinSearchForm.endDate = undefined
      if(val && val.length>0){
        this.mixinSearchForm.startDate = val[0]
        this.mixinSearchForm.endDate = val[1]
      }
    }
  },
  mounted: function() {},
  methods: {
    /**
     * 多选事件
     */
    selectChange(val) {
      this.staffIdArr = []
      if (val.length > 0) {
        this.staffArr = val
        val.forEach(function(element) {
          this.staffIdArr.push(element.id)
        }, this)
      }
    },
    handleBatchSet(){
      if(this.staffArr.length===0){
        this.$message.error('请选择要设置的员工信息')
        return
      }
      this.$refs.batchProjectDialog.open()
    },
    handleCheckProject(row){
      this.curObj = row
      this.$refs.checkProjectDialog.open()
    },
    refresh(){
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    async getList(page, params) {
      let staffBadges = []
      if(params.badges){
        let arr = params.badges.split(/[\s\n,]/)
        staffBadges = arr.filter(el=>{
          return el !=""
        })
        params.badges = ""
      }
      this.mixinTableLoading = true
      const res = await  xcSignMngApi.getList(
        {
          current: page.currentPage,
          size: page.pageSize
        },
        Object.assign({
          staffBadges: staffBadges
        },params)
      )
      this.tableData = res.data.data.records
      this.mixinPage.total = res.data.data.total
      this.mixinTableLoading = false;
    },
    mixinResetFrom(formName){
      this.$refs[formName] && this.$refs[formName].resetFields()
      this.deptIds = []
      this.times = []
      this.mixinPage.currentPage = 1
      this.getList(this.mixinPage)
    }
  }
};
const listOption = function () {
  return {
    menu: true,
    menuWidth: 120,
    selection: true,
    column: [
      {
        label: '工号',
        prop: 'badge'
      },{
        label: '姓名',
        prop: 'name'
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
        prop: 'createTime',
        solt: true
      },{
        label: '状态',
        prop: 'signStatusDesc'
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
