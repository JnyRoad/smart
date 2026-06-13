<!--
- @name 授权管理
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
            <el-button type="primary" @click="handleAdd()">人员授权</el-button>
            <el-button type="primary" icon="el-icon-download" @click="download">导出excel</el-button>
          </div>
        </div>

        <!-- 搜索条件 -->
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="工号" prop="personNum">
            <el-input v-model="searchForm.personNum" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="personName">
            <el-input v-model="searchForm.personName" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="手机号" prop="personPhone">
            <el-input v-model="searchForm.personPhone" placeholder="手机号" clearable></el-input>
          </el-form-item>
          <el-form-item label="门锁名称" prop="deviceName">
            <el-input v-model="searchForm.deviceName" placeholder="门锁名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="searchForm.status" clearable placeholder="状态">
              <el-option label="待授权" :value="0"></el-option>
              <el-option label="已授权" :value="1"></el-option>
              <el-option label="授权失败" :value="2"></el-option>
              <el-option label="已失效" :value="3"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="授权开始时间" prop="time">
            <el-date-picker
              v-model="searchForm.time"
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

        <!-- 列表 -->
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="keyList" v-if="scope.row.keyList.length > 0">
            <div v-for="(item, index) in scope.row.keyList" :key="index">{{item.statusDesc}}-{{item.keyTypeDesc}}</div>
          </template>
          <template slot-scope="scope" slot="menu">
            <!-- <el-button type="text" icon="el-icon-view" size="mini" @click="bindRoom(scope.row)">查看</el-button> -->
            <el-button type="text" icon="el-icon-edit" v-if="scope.row.status === 1" @click="handleAuthCancel(scope.row, scope.$index)" :disabled="scope.row.isAvailable === 1"
              >取消授权</el-button
            >
            <el-button type="text" icon="el-icon-edit" v-if="scope.row.status !== 1" @click="handleAuthAdd(scope.row, scope.$index)" :disabled="scope.row.isAvailable === 1"
              >重新授权</el-button
            >
            <el-button type="text" icon="el-icon-edit" size="mini" @click="handleEdit(scope.row)" :disabled="scope.row.isAvailable === 1">编辑</el-button>
            <el-button type="text" icon="el-icon-delete" @click="handleDel(scope.row, scope.$index)" :disabled="scope.row.isAvailable === 1">删除</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <AddDialog :title="dlgTitle" :itemObj="curAuth" ref="addDialog" @refresh="refresh" />
    <AuthRoomsDialog title="授权房间" :itemObj="curRooms" ref="AuthRoomsDialog" @refresh="refresh" />
    <ReAuthDialog title="重新授权" :itemObj="curAuth" ref="reAuthDialog" @refresh="refresh" />
  </div>
</template>

<script>
import { fetchList, authApi } from './_service.js'
import { tableOption } from '@/const/crud/platform/dormitory/grant_auth'
import AddDialog from './components/AddDialog'
import ReAuthDialog from './components/ReAuthDialog'
import AuthRoomsDialog from './components/AuthRoomsDialog'
export default {
  components: {
    AddDialog,
    ReAuthDialog,
    AuthRoomsDialog
  },
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        parkId: null,
        personNum: null,
        personName: null,
        personPhone: null,
        deviceName: null,
        time: [],
        validTimeStart: null,
        validTimeEnd: null,
        status: null
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
      dlgTitle: '添加',
      curAuth: null, //当前授权
      curRooms: [], //授权设备
      curPerson: {} //人员
    }
  },
  created() {
    this.getList(this.page)
  },
  methods: {
    async getList(page, params) {
      var _this = this
      this.tableLoading = true
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize,
            deviceName: this.searchForm.deviceName,
            parkId: this.searchForm.parkId,
            personNum: this.searchForm.personNum,
            personName: this.searchForm.personName,
            personPhone: this.searchForm.personPhone,
            validTimeStart: this.searchForm.validTimeStart,
            validTimeEnd: this.searchForm.validTimeEnd,
            status: this.searchForm.status
          },
          params
        )
      ).then((response) => {
        this.tableData = response.data.data.records
        this.page.total = response.data.data.total
        if (this.tableData.length > 0) {
          this.tableData.forEach((element) => {
            switch (element.status) {
              case 0:
                element['statusDesc'] = '待授权'
                break
              case 1:
                element['statusDesc'] = '已授权'
                break
              case 2:
                element['statusDesc'] = '授权失败'
                break
              case 3:
                element['statusDesc'] = '已失效'
                break
            }
            if (element.validTimeStart === null) {
              element['validTime'] = '长期有效'
            } else {
              element['validTime'] = element.validTimeStart + '至' + element.validTimeEnd
            }
            // validTime
          })
          this.tableLoading = false
        }
      })
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
    handleAdd() {
      this.curAuth = null
      this.dlgTitle = '添加'
      this.$refs.addDialog.open()
    },
    handleEdit(row) {
      this.dlgTitle = '编辑'
      this.curAuth = Object.assign(row)
      this.$refs.addDialog.open()
    },
    /**
     * 删除
     */
    async handleDel(row, index) {
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除该授权信息？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(() => {
          return authApi.delObj(row.id)
        })
        .then((dataResponse) => {
          if (dataResponse.data.code === 200) {
            this.getList(this.page, this.params)
            this.$notify({
              title: '成功',
              message: '删除成功',
              type: 'success',
              duration: 2000
            })
          } else {
            this.$message.error(dataResponse.data.message)
          }
        })
        .catch(err => { console.error(err) })
    },
    /**
     * 取消授权,点击
     */
    handleAuthCancel(row) {
      let _this = this
      this.$confirm('确认取消后将在数秒后移除设备的授权信息，移除后该人员将无法开门', '确认取消授权', {
        confirmButtonText: '确认取消授权',
        cancelButtonText: '不取消',
        type: 'warning'
      })
        .then(() => {
          //此处调用取消授权接口
          _this.cancelAuth(row)
        })
        .catch(error => { console.error(error) })
    },
    /**
     * 取消授权
     */
    async cancelAuth(row) {
      const res = await authApi.cancelAuth(row.id)
      if (res.data.code === 200) {
        this.getList(this.page, this.params)
        this.$notify({
          title: '成功',
          message: '取消成功',
          type: 'success',
          duration: 2000
        })
      } else {
        this.$message.error(res.data.message)
      }
    },
    /**
     * 重新授权
     */
    handleAuthAdd(row) {
      this.curAuth = Object.assign(row)
      this.$refs.reAuthDialog.open()
    },
    //导出表格
    async download() {
      const _this = this
      const obj = {
        current: 1,
        size: 99999,
        deviceName: this.searchForm.deviceName,
        deviceArea: this.searchForm.deviceArea,
        validTimeStart: this.searchForm.validTimeStart,
        validTimeEnd: this.searchForm.validTimeEnd,
        status: this.searchForm.status
      }
      require.ensure([], () => {
        const { export_json_to_excel } = require('@/vendor/Export2Excel')
        const tHeader = ['门锁名称', '人员编号', '授权人员', '授权时间', '状态', '备注']
        const filterVal = ['deviceName', 'personNum', 'personName', 'validTime', 'statusDesc', 'remark']
        fetchList(obj)
          .then((response) => {
            const list = response.data.data.records
            if (list.length > 0) {
              list.forEach((element) => {
                switch (element.status) {
                  case 0:
                    element['statusDesc'] = '待授权'
                    break
                  case 1:
                    element['statusDesc'] = '已授权'
                    break
                  case 2:
                    element['statusDesc'] = '授权失败'
                    break
                  case 3:
                    element['statusDesc'] = '已失效'
                    break
                }
                if (element.validTimeStart === null) {
                  element['validTime'] = '长期有效'
                } else {
                  element['validTime'] = element.validTimeStart + '至' + element.validTimeEnd
                }
                // validTime
              })
            }
            const data = this.formatJson(filterVal, list)
            export_json_to_excel(tHeader, data, `授权记录表&(${this.searchForm.time})`);

          })
          .catch(err => { console.error(err) })
      })
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map((v) => filterVal.map((j) => v[j]))
    },
    refresh() {
      this.getList(this.page, this.params)
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.page.currentPage = 1
      if (this.searchForm.time && this.searchForm.time != null) {
        this.searchForm.validTimeStart = this.searchForm.time[0]
        this.searchForm.validTimeEnd = this.searchForm.time[1]
      }
      this.getList(this.page, this.params)
    },
    /**
     * 清空搜索
     */
    resetFrom() {
      // this.$refs['searchForm'].resetFields()
      this.searchForm = {}
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