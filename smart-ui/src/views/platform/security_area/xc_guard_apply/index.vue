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
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加保密门禁申请</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="mixinSearchForm" class="topForm" size="mini">
          <el-form-item label="OA单号" prop="processId">
            <el-input v-model="mixinSearchForm.processId" placeholder="请输入" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属园区/BU/部门" prop="depIds">
            <deptCascader v-model="depIds" :changeOnSelect="true" placeholder="请选择"></deptCascader>
          </el-form-item>
          <el-form-item label="申请时间" prop="times">
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
          <!-- <el-form-item label="OA状态" prop="">
            <el-select v-model="mixinSearchForm.s" clearable>
              <el-option label="待审批" :value="0"></el-option>
              <el-option label="通过" :value="1"></el-option>
              <el-option label="拒绝" :value="2"></el-option>
              <el-option label="关闭" :value="3"></el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item label="下发状态" prop="downStatus">
            <el-select v-model="mixinSearchForm.downStatus" clearable>
              <el-option label="待下发" :value="0"></el-option>
              <el-option label="已下发" :value="4"></el-option>
              <!-- <el-option label="下发成功" :value="1"></el-option>
              <el-option label="下发失败" :value="2"></el-option>
              <el-option label="下发中" :value="3"></el-option> -->
            </el-select>
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
            <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row,scope.$index)" >详情</el-button>
            <!--oaStatus oa状态 已通过 才可以手动下发 -->
            <el-button type="text" @click="handleSend(scope.row, scope.$index)" :disabled="scope.row.oaStatus!==1">手动下发</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>

  </div>
</template>

<script>
import { xcGuardApplyApi } from "./_service"

export default {
  mixins: [tce.mixins.list],
  data() {
    return {
      depIds: [],
      times: [],
      tableData: [],
      listOption: listOption(),
    };
  },
  created() {
    this.refresh()
  },
  watch: {
    depIds(val){
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
    refresh(){
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    async getList(page, params) {
      this.mixinTableLoading = true
      const res = await  xcGuardApplyApi.getList({
          current: page.currentPage,
          size: page.pageSize
        },params
      )
      this.tableData = res.data.data.records
      this.mixinPage.total = res.data.data.total
      this.mixinTableLoading = false;
    },
    /**
     * 手动下发
     */
    async handleSend(row){
      const res = await xcGuardApplyApi.doSend(row.id)
      if(res.data.code===0 && res.data.data){
        this.$message({
          message: '手动下发成功',
          type: 'success'
        });
      }
    },
    /**
     * 添加保密门禁申请
     */
    handleAdd(){
      this.$router.push({
        path: `/platform/security_area/securityGuardApply/add`
      });
    },
    /**
     * 详情
     */
    handleDetail(row) {
      this.$router.push({
        path: `/platform/security_area/securityGuardApply/detail/${row.id}`,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    /**
     * 重置搜索条件
     */
    mixinResetFrom(formName){
      this.$refs[formName] && this.$refs[formName].resetFields()
      this.depIds = []
      this.times = []
      this.mixinPage.currentPage = 1
      this.getList(this.mixinPage)
    }
  }
};
const listOption = function () {
  return {
    index: false,
    menu: true,
    menuWidth: 180,
    column: [
      // {
      //   label: '流水号',
      //   prop: 'serialNum'
      // },
      {
        label: '申请人工号',
        prop: 'badge',
        width: 100
      },
      {
        label: '申请人姓名',
        prop: 'name',
        width: 100
      },
      {
        label: '园区',
        prop: 'parkName'
      },
      {
        label: 'BU',
        prop: 'compName'
      },
      {
        label: '部门',
        prop: 'depName'
      },
      {
        label: 'OA单号',
        prop: 'processId'
      },
      {
        label: '申请时间',
        prop: 'createTime',
        width: 170
      },
      {
        label: 'OA状态',
        prop: 'oaStatusDesc'
      },
      {
        label: '下发状态',
        prop: 'deviceStatusDesc'
      },
      {
        label: '下发总人数',
        prop: 'totalNum'
      },
      {
        label: '下发成功数量',
        prop: 'successNum',
        width: 120
      },
      {
        label: '下发失败数量',
        prop: 'failNum',
        width: 120
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
