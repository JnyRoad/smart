<!--区域管理，权限策略, 关联员工  -->
<template>
  <div class="my-basic-container limit">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
          <span style="font-size: 16px;margin-left: 10px;line-height: 30px;">当前操作权限：{{authorityName}}</span>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>重置</el-button>
            <el-button
              :loading="batchDeleting"
              :disabled="batchDeleting || clearing"
              type="primary"
              icon="el-icon-delete"
              @click="handleDelBatch()"
            >批量删除</el-button>
            <el-button
              :loading="clearing"
              :disabled="batchDeleting || clearing"
              type="primary"
              icon="el-icon-delete"
              @click="handleClear()"
            >清空权限</el-button>
            <el-button plain :disabled="batchDeleting || clearing" @click="retryPendingIntake">重试未确认提交</el-button>
            <el-button type="primary" @click="byPaste()">批量粘贴</el-button>
            <el-button
              plain
              icon="el-icon-time"
              @click="operationProgressVisible = true"
            >权限任务</el-button>
          </div>
        </div>
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <!-- <el-form-item label="工号" prop="badge">
              <el-input v-model="searchForm.badge" placeholder="请输入工号" clearable></el-input>
            </el-form-item> -->
            <el-form-item label="工号" prop="badge">
              <div class="badgeTemplate" @click="pasteBadge">
                <span v-if="badgeArry.length === 0" style="color: rgb(192, 196, 204)">点击批量粘贴工号</span>
                <div style="padding: 2px 0" v-else>
                  <el-tag type="info">{{ badgeArry[0] }}</el-tag>
                  <el-tag type="info" v-if="badgeArry.length > 1" style="margin-left: 4px">+ {{ badgeArry.length - 1 }}</el-tag>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="姓名" prop="personName">
              <el-input v-model="searchForm.personName" placeholder="请输入姓名" clearable></el-input>
            </el-form-item>
          </el-form>
        </div>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
          @selection-change="selectChange"
        >
          <template slot-scope="scope" slot="staffStatus">
            {{ scope.row.staffStatus | staffStatusInit}}
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <DoPasteDialogs ref="DoPasteDialogs" :dataItem="badges" @refresh="badgeChange" />
    <DoPasteDialog ref="doPasteDialog" :dataItem="deleteForm" @refresh="resetFrom('searchForm')"/>
    <AuthOperationProgress :key="routeContextVersion" v-model="operationProgressVisible" :operation-key="acceptedOperationKey" />
  </div>
</template>

<script>
import { pendingEmployeeIntake, submitEmployeeIntake } from './employee-intake-request'
import { getDetailPage, delObj, batchDelPersonWithReceipt, clearPersonWithReceipt, personIntakeCapability } from '@/api/platform/area/limit'
import { tableOption } from '@/const/crud/platform/area/limit_person'
import DoPasteDialog from './doPaste'
import DoPasteDialogs from './doPasteBadge'
import AuthOperationProgress from './AuthOperationProgress'
import { mapGetters } from 'vuex'
import { staffStatusInit } from '@/filters/index'
export default {
  name: 'limit',
  components: {
    DoPasteDialog,
    DoPasteDialogs,
    AuthOperationProgress
  },
  data() {
    return {
      searchForm: {
        badges: '',
        personName: ''
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20, // 每页显示多少条
        authId: '',
        type: ''
      },
      deleteForm: {
        delIds: [],
        authId: '',
        type: ''
      },
      badges: '',
      badgeArry: [],
      authorityName: null,
      operationProgressVisible: false,
      acceptedOperationKey: '',
      batchDeleting: false,
      clearing: false,
      routeContextVersion: 0,
      listRequestSequence: 0
    }
  },
  created() {
    this.syncRouteContext(this.$route)
    this.loadRouteList()
  },
  mounted: function () {},
  computed: {
    ...mapGetters(['permissions'])
  },
  watch: {
    $route(route) {
      this.syncRouteContext(route)
      this.loadRouteList()
    }
  },
  methods: {
    syncRouteContext(route) {
      this.operationProgressVisible = false
      this.acceptedOperationKey = ''
      this.routeContextVersion += 1
      this.listRequestSequence += 1
      this.page.authId = route.params.id
      this.page.type = route.params.type
      this.page.currentPage = 1
      this.page.total = 0
      this.deleteForm.authId = route.params.id
      this.deleteForm.type = route.params.type
      this.deleteForm.delIds = []
      this.authorityName = route.query.name
      this.tableData = []
    },
    loadRouteList() {
      return this.getList().catch(error => {
        this.$notify.error({
          title: '列表加载失败',
          message: this.errorMessage(error, '权限明细加载失败'),
          type: 'error',
          duration: 3000
        })
      })
    },
    pasteBadge() {
      this.$refs.DoPasteDialogs && this.$refs.DoPasteDialogs.open()
    },
    badgeChange(val) {
      this.badges = val
      let arr = val.split(/[\s\n,]/)
      this.badgeArry = arr.filter((el) => {
        return el != ''
      })
    },
    async getList(page = this.page, params = this.searchForm) {
      const requestSequence = ++this.listRequestSequence
      const routeContextVersion = this.routeContextVersion
      this.tableLoading = true
      try {
        const response = await getDetailPage(
          Object.assign(
            {
              descs: 'create_time',
              current: page.currentPage,
              size: page.pageSize,
              authId: page.authId,
              type: page.type
            },
            params
          )
        )
        if (requestSequence !== this.listRequestSequence || routeContextVersion !== this.routeContextVersion) return
        this.tableData = response.data.data.records
        this.page.total = response.data.data.total
      } catch (error) {
        if (requestSequence !== this.listRequestSequence || routeContextVersion !== this.routeContextVersion) return
        throw error
      } finally {
        if (requestSequence === this.listRequestSequence) this.tableLoading = false
      }
    },
    operationContext() {
      return {
        version: this.routeContextVersion,
        actorId: this.$store.getters.userInfo && this.$store.getters.userInfo.id,
        authId: this.deleteForm.authId,
        type: this.deleteForm.type,
        name: this.authorityName || (this.$route.query && this.$route.query.name) || ''
      }
    },
    isOperationContextCurrent(context) {
      return context.actorId === (this.$store.getters.userInfo && this.$store.getters.userInfo.id) &&
        context.version === this.routeContextVersion &&
        context.authId === this.deleteForm.authId &&
        context.type === this.deleteForm.type
    },
    async refreshAfterAccepted(context, receipt) {
      if (!this.isOperationContextCurrent(context)) return
      try {
        await this.getList()
      } catch (error) {
        if (!this.isOperationContextCurrent(context)) return
        if (receipt && receipt.mode === 'NO_CHANGE') {
          this.$notify({ title: '列表刷新失败', message: `权限组${this.operationLabel(context)}本次没有产生新批次，请手动刷新列表。`, type: 'warning' })
        } else this.notifyAcceptedRefreshFailed(error, context)
      }
    },
    operationLabel(context) {
      const name = context.name ? `“${context.name}”` : '未命名权限组'
      return `${name}（ID：${context.authId}）`
    },
    notifyAcceptedRefreshFailed(error, context) {
      this.$notify({
        title: '请求已受理，列表刷新失败',
        message: `权限组${this.operationLabel(context)}的设备结果仍待确认，请手动刷新列表后再操作。${this.errorMessage(error, '')}`,
        type: 'warning',
        duration: 5000
      })
    },
    notifyRouteChanged(context) {
      this.$notify({
        title: '权限组已切换',
        message: `权限组${this.operationLabel(context)}的本次操作已取消，请在当前权限组重新选择后提交。`,
        type: 'warning',
        duration: 3000
      })
    },
    async handleDelBatch() {
      const elm = this.$createElement
      if (this.batchDeleting || this.clearing) return
      if (this.deleteForm.delIds.length == 0) {
        this.$notify.error({
          title: '提示信息',
          message: '请选择要删除的人员',
          type: 'error',
          duration: 2000
        })
        return
      }
      const context = this.operationContext()
      const request = {
        authId: context.authId,
        type: context.type,
        delIds: [...this.deleteForm.delIds]
      }
      this.batchDeleting = true
      try {
        await this.$msgbox({
          message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除所选人员信息？ ')]),
          showCancelButton: true,
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          customClass: 'small_dialog',
          center: true
        })
        if (!this.isOperationContextCurrent(context)) {
          this.notifyRouteChanged(context)
          return
        }
        const dataResponse = await this.submitPersonIntake(context, { kind: 'REMOVE_ROWS', authId: request.authId, rowIds: request.delIds })
        if (this.applyOperationReceipt(dataResponse.data.data, context)) {
          if (this.isOperationContextCurrent(context)) {
            this.deleteForm.delIds = []
            await this.refreshAfterAccepted(context, dataResponse.data.data)
          }
        } else {
          this.notifyDeleteFailed('删除请求提交失败', context)
        }
      } catch (error) {
        if (!this.isConfirmCanceled(error)) {
          this.notifyDeleteFailed(this.submissionError(error), context)
        }
      } finally {
        this.batchDeleting = false
      }
    },
    async handleClear() {
      if (this.batchDeleting || this.clearing) return
      const elm = this.$createElement
      const context = this.operationContext()
      this.clearing = true
      try {
        await this.$msgbox({
          message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认清空所有人员权限信息？ ')]),
          showCancelButton: true,
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          customClass: 'small_dialog',
          center: true
        })
        if (!this.isOperationContextCurrent(context)) {
          this.notifyRouteChanged(context)
          return
        }
        const dataResponse = await this.submitPersonIntake(context, { kind: 'CLEAR_AUTHORITY', authId: context.authId, rowIds: [] })
        if (this.applyOperationReceipt(dataResponse.data.data, context)) {
          if (this.isOperationContextCurrent(context)) await this.refreshAfterAccepted(context, dataResponse.data.data)
        } else {
          this.notifyDeleteFailed('删除请求提交失败', context)
        }
      } catch (error) {
        if (!this.isConfirmCanceled(error)) {
          this.notifyDeleteFailed(this.submissionError(error), context)
        }
      } finally {
        this.clearing = false
      }
    },
    submitPersonIntake(context, intent) {
      return submitEmployeeIntake({
        actorId: context.actorId, intent, capability: personIntakeCapability,
        isCurrent: () => this.isOperationContextCurrent(context),
        send: (saved, key) => saved.kind === 'CLEAR_AUTHORITY'
          ? (key === undefined ? clearPersonWithReceipt(saved.authId) : clearPersonWithReceipt(saved.authId, key))
          : (key === undefined ? batchDelPersonWithReceipt({ authId: saved.authId, type: context.type, delIds: saved.rowIds })
            : batchDelPersonWithReceipt({ authId: saved.authId, type: 1, delIds: saved.rowIds }, key))
      })
    },
    async retryPendingIntake() {
      if (this.batchDeleting || this.clearing) return
      const context = this.operationContext()
      this.batchDeleting = true
      try {
        const saved = await pendingEmployeeIntake(context.actorId)
        if (!saved) { this.$notify({ title: '没有待重试请求', message: '当前登录用户没有保存的未确认提交。', type: 'info' }); return }
        if (String(saved.intent.authId) !== String(context.authId)) throw new Error(`请返回权限组 ${saved.intent.authId} 重试上次未确认提交`)
        const response = await this.submitPersonIntake(context, saved.intent)
        if (this.applyOperationReceipt(response.data.data, context) && this.isOperationContextCurrent(context)) {
          this.deleteForm.delIds = []
          await this.refreshAfterAccepted(context, response.data.data)
        }
      } catch (error) { this.notifyDeleteFailed(this.submissionError(error), context) }
      finally { this.batchDeleting = false }
    },
    applyOperationReceipt(receipt, context) {
      // 兼容旧 Boolean 响应，但绝不把 truthy 对象或 NO_CHANGE 当成批次。
      if (receipt === true) {
        this.clearReceiptFocus(context)
        this.notifyDeleteAccepted(context)
        return true
      }
      if (!receipt || typeof receipt !== 'object') return false
      if (receipt.mode === 'LEGACY' && receipt.submitted === true && receipt.operationKey == null) {
        this.clearReceiptFocus(context)
        this.notifyDeleteAccepted(context)
        return true
      }
      if (receipt.mode === 'NO_CHANGE' && receipt.submitted === false && receipt.operationKey == null) {
        this.clearReceiptFocus(context)
        this.$notify({ title: '本次没有变化', message: `权限组${this.operationLabel(context)}没有产生新批次。`, type: 'info' })
        return true
      }
      if (receipt.mode !== 'RELIABLE' || receipt.submitted !== true || typeof receipt.operationKey !== 'string' ||
          !receipt.operationKey.trim() || receipt.operationKey === 'NO_CHANGE') return false
      if (this.isOperationContextCurrent(context)) {
        this.acceptedOperationKey = receipt.operationKey
        this.operationProgressVisible = true
      }
      this.$notify({
        title: '删除请求已提交',
        message: `权限组${this.operationLabel(context)}的请求已受理，设备结果仍待确认。操作键：${receipt.operationKey}`,
        type: 'info', duration: 5000
      })
      return true
    },
    clearReceiptFocus(context) {
      if (!this.isOperationContextCurrent(context)) return
      this.operationProgressVisible = false
      this.acceptedOperationKey = ''
    },
    submissionError(error) {
      const status = error && error.response && error.response.status
      if (status >= 400 && status < 500) return this.errorMessage(error, '删除请求未受理')
      return `提交结果未确认，请先核对权限任务再操作。${this.errorMessage(error, '')}`
    },
    notifyDeleteAccepted(context) {
      this.$notify({
        title: '删除请求已提交',
        message: `权限组${this.operationLabel(context)}的请求已受理，设备结果仍待确认。旧链路暂不能自动定位批次，请手动查看可访问园区的权限任务。`,
        type: 'info',
        duration: 5000
      })
    },
    notifyDeleteFailed(message, context) {
      this.$notify.error({
        title: '删除失败',
        message: `权限组${this.operationLabel(context)}：${message}`,
        type: 'error',
        duration: 3000
      })
    },
    isConfirmCanceled(error) {
      return error === 'cancel' || error === 'close'
    },
    errorMessage(error, fallback) {
      return (error && error.response && error.response.data && error.response.data.msg) || (error && error.message) || fallback
    },
    selectChange(val) {
      //序号那边选择事件
      this.deleteForm.delIds = []
      if (val.length > 0) {
        val.forEach(function (element) {
          this.deleteForm.delIds.push(element.id)
        }, this)
      }
    },
    handleDel(row, index) {
      //删除
      //this.$refs.crud.rowDel(row, index);
      this.rowDel(row, index)
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
    rowDel: function (row, index) {
      let _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除该权限策略信息？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          return delObj(row.id)
        })
        .then((dataResponse) => {
          _this.getList(_this.page, _this.searchForm)
          if (dataResponse.data.data) {
            _this.$notify({
              title: '成功',
              message: '删除成功',
              type: 'success',
              duration: 2000
            })
          } else {
            _this.$notify({
              title: '失败',
              message: dataResponse.data.msg,
              type: 'fail',
              duration: 2000
            })
          }
          // _this.$notify({
          //   title: "成功",
          //   message:"删除成功",
          //   type: "success",
          //   duration: 2000
          // });
        })
        .catch(err => { console.error(err) })
    },
    goBack(){
      let path = `/platform/area/${this.$route.query.backPageTag}`
      this.$router.push({
        path: path,
        query: {}
      })
    },
    /**
     * 粘贴人员
     */
    byPaste() {
      this.$refs.doPasteDialog && this.$refs.doPasteDialog.open()
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1
      form['badges'] = this.badges
      this.getList(this.page, form)
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.badgeArry = []
        this.badges = ''
        this.$refs[formName].resetFields()
        this.page.currentPage = 1
        this.getList(this.page)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.topForm ::v-deep {
  .el-form-item__label {
    width: 120px;
  }
}
.badgeTemplate {
  width: 180px;
  height: 32px;
  border: 1px solid rgb(220, 223, 230);
  border-radius: 4px;
  padding: 0 30px 0 15px;
  cursor: pointer;
  .el-tag {
    height: 24px;
    line-height: 24px;
  }
}
</style>
