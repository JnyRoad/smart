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
              <el-option label="下发中" :value="3"></el-option>
              <el-option label="下发成功" :value="1"></el-option>
              <el-option label="部分失败/失败" :value="2"></el-option>
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
            <!--oaStatus oa状态 已通过 才可以手动下发；按钮权限码需在菜单管理配置并绑定角色（见 runbook） -->
            <el-button type="text" v-if="permissions['platform_security_auth_down']" @click="handleSend(scope.row, scope.$index)" :disabled="scope.row.oaStatus!==1 || isDispatchPending(scope.row.id)">手动下发</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>

  </div>
</template>

<script>
import { xcGuardApplyApi } from "./_service"
import { mapGetters } from 'vuex'

export default {
  mixins: [tce.mixins.list],
  data() {
    return {
      depIds: [],
      times: [],
      tableData: [],
      listOption: listOption(),
      dispatchingIds: {},
      dispatchPollingTimer: null,
      activeDispatch: null,
      dispatchPollingGeneration: 0,
      isDestroyed: false
    };
  },
  computed: {
    // 按钮权限：platform_security_auth_down 由菜单管理配置、登录时随用户权限下发
    ...mapGetters(['permissions'])
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
  beforeDestroy() {
    this.isDestroyed = true
    this.stopDispatchProgressPolling()
  },
  methods: {
    refresh(){
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    async getList(page, params) {
      this.mixinTableLoading = true
      try {
        const res = await xcGuardApplyApi.getList({
          current: page.currentPage,
          size: page.pageSize
        }, params)
        this.tableData = res.data.data.records
        this.mixinPage.total = res.data.data.total
      } finally {
        this.mixinTableLoading = false
      }
    },
    /**
     * 手动下发
     */
    async handleSend(row){
      if (this.isDispatchPending(row.id)) {
        return
      }
      if (this.$set) {
        this.$set(this.dispatchingIds, row.id, true)
      } else {
        this.dispatchingIds[row.id] = true
      }
      try {
        const res = await xcGuardApplyApi.doSend(row.id)
        const accepted = res.data && res.data.data
        if (res.status !== 202 || !res.data || res.data.code !== 0 || !accepted || !accepted.batchId) {
          throw new Error('下发命令未被受理')
        }
        this.$message({
          message: `已受理，正在下发（批次 ${accepted.batchId}）`,
          type: 'success'
        })
        this.refresh()
        this.startDispatchProgressPolling(row.id, accepted.batchId)
      } catch (error) {
        this.$message({
          message: '下发命令未受理，请稍后重试',
          type: 'error'
        })
      } finally {
        if (this.$set) {
          this.$set(this.dispatchingIds, row.id, false)
        } else {
          this.dispatchingIds[row.id] = false
        }
      }
    },
    /**
     * 当前申请单的受理请求尚未返回时禁止重复提交。
     */
    isDispatchPending(applyId) {
      return this.dispatchingIds[applyId] === true
    },
    /**
     * 每次只保留一个当前批次轮询，防止旧批次结果覆盖新批次展示。
     */
    async startDispatchProgressPolling(applyId, batchId) {
      this.stopDispatchProgressPolling()
      const token = ++this.dispatchPollingGeneration
      this.activeDispatch = { applyId, batchId, token }
      await this.pollDispatchProgress(token)
    },
    async pollDispatchProgress(token) {
      if (!this.isCurrentDispatch(token)) {
        return
      }
      const activeDispatch = this.activeDispatch
      try {
        const res = await xcGuardApplyApi.getDispatchProgress(activeDispatch.applyId, activeDispatch.batchId)
        if (!this.isCurrentDispatch(token)) {
          return
        }
        const progress = res.data && res.data.data
        if (!res.data || res.data.code !== 0 || !progress || progress.batchId !== activeDispatch.batchId) {
          throw new Error('下发进度返回异常')
        }
        this.applyDispatchProgress(activeDispatch.applyId, progress)
        if (this.isDispatchTerminal(progress)) {
          this.stopDispatchProgressPolling(token)
          this.refresh()
          return
        }
        if (!this.isCurrentDispatch(token)) {
          return
        }
        this.dispatchPollingTimer = window.setTimeout(() => {
          if (!this.isCurrentDispatch(token)) {
            return
          }
          this.dispatchPollingTimer = null
          this.pollDispatchProgress(token)
        }, 3000)
      } catch (error) {
        if (!this.isCurrentDispatch(token)) {
          return
        }
        this.stopDispatchProgressPolling(token)
        this.$message({
          message: '下发进度查询失败，请刷新后重试',
          type: 'error'
        })
      }
    },
    /**
     * HTTP 失败、组件销毁和批次终态都必须停止定时器。
     */
    stopDispatchProgressPolling(token) {
      if (token !== undefined && !this.isCurrentDispatch(token)) {
        return
      }
      if (this.dispatchPollingTimer) {
        window.clearTimeout(this.dispatchPollingTimer)
      }
      this.dispatchPollingTimer = null
      this.activeDispatch = null
      this.dispatchPollingGeneration++
    },
    /**
     * 仅当前组件、当前批次和当前代际可写入轮询状态。
     */
    isCurrentDispatch(token) {
      return !this.isDestroyed && this.activeDispatch && this.activeDispatch.token === token
    },
    /**
     * canceledCount 已包含在 failCount 中，终态不能再次相加。
     */
    isDispatchTerminal(progress) {
      const totalCount = Number(progress.totalCount) || 0
      const pendingCount = (Number(progress.waitingCount) || 0) + (Number(progress.inWorkCount) || 0)
      const completedCount = (Number(progress.successCount) || 0) + (Number(progress.failCount) || 0)
      return totalCount > 0 && pendingCount === 0 && completedCount >= totalCount
    },
    /**
     * 轮询期间用当前批次的真实 ISC 聚合结果更新列表行。
     */
    applyDispatchProgress(applyId, progress) {
      const row = this.tableData.find(item => item.id === applyId)
      if (!row) {
        return
      }
      row.currentDispatchBatchId = progress.batchId
      row.totalNum = progress.totalCount
      row.pendingNum = (Number(progress.waitingCount) || 0) + (Number(progress.inWorkCount) || 0)
      row.successNum = progress.successCount
      row.failNum = progress.failCount
      row.cancelNum = progress.canceledCount
      row.deviceStatusDesc = this.isDispatchTerminal(progress)
        ? (Number(progress.failCount) > 0 ? '部分失败/失败' : '下发成功')
        : '下发中'
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
        label: '当前批次',
        prop: 'currentDispatchBatchId',
        width: 130
      },
      {
        label: '待处理数量',
        prop: 'pendingNum',
        width: 110
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
      },
      {
        label: '已取消数量',
        prop: 'cancelNum',
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
