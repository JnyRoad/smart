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
            <el-button type="primary" icon="el-icon-delete" @click="handleDelBatch()">批量删除</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="handleClear()">清空权限</el-button>
            <el-button type="primary" @click="byPaste()">批量粘贴</el-button>
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
  </div>
</template>

<script>
import { getDetailPage, delObj, batchDel, clearAll } from '@/api/platform/area/limit'
import { tableOption } from '@/const/crud/platform/area/limit_person'
import DoPasteDialog from './doPaste'
import DoPasteDialogs from './doPasteBadge'
import { mapGetters } from 'vuex'
import { staffStatusInit } from '@/filters/index'
export default {
  name: 'limit',
  components: {
    DoPasteDialog,
    DoPasteDialogs
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
        pageSize: 20 // 每页显示多少条
      },
      deleteForm: {
        delIds: []
      },
      badges: '',
      badgeArry: [],
      authorityName: null
    }
  },
  created() {
    this.page.authId = this.$route.params.id
    this.page.type = this.$route.params.type
    this.authorityName = this.$route.query.name
    this.deleteForm.authId = this.$route.params.id
    this.deleteForm.type = this.$route.params.type
    this.getList(this.page, this.searchForm)
  },
  mounted: function () {},
  computed: {
    ...mapGetters(['permissions'])
  },
  watch: {
    $route() {
      this.getList()
    }
  },
  methods: {
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
    getList(page, params) {
      this.tableLoading = true
      getDetailPage(
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
      ).then((response) => {
        this.tableData = response.data.data.records
        this.page.total = response.data.data.total
        this.tableLoading = false
      })
      this.tableLoading = false
    },
    handleDelBatch() {
      var _this = this
      const elm = this.$createElement
      if (this.deleteForm.delIds.length == 0) {
        _this.$notify.error({
          title: '提示信息',
          message: '请选择要删除的人员',
          type: 'success',
          duration: 2000
        })
        return
      }
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除所选人员信息？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(() => {
          return batchDel(this.deleteForm)
        })
        .then((dataResponse) => {
          if (dataResponse.data.data) {
            _this.getList(this.page)
            _this.$notify({
              title: '删除成功',
              message: '删除成功',
              type: 'success',
              duration: 2000
            })
          } else {
            _this.$notify.error({
              title: '删除失败',
              message: '删除失败',
              type: 'error',
              duration: 2000
            })
          }
        })
        .catch(err => { console.error(err) })
    },
    handleClear() {
      const _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认清空所有人员权限信息？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(() => {
          return clearAll(this.deleteForm.authId)
        })
        .then((dataResponse) => {
          if (dataResponse.data.data) {
            _this.getList(this.page)
            _this.$notify({
              title: '删除成功',
              message: '删除成功',
              type: 'success',
              duration: 2000
            })
          } else {
            _this.$notify.error({
              title: '删除失败',
              message: '删除失败',
              type: 'error',
              duration: 2000
            })
          }
        })
        .catch(err => { console.error(err) })
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
