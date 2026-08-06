<template>
  <div class="my-basic-container execution">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-button type="primary" v-if="permissions.sys_client_add" icon="el-icon-plus" @click="handleAdd">新 增</el-button>
        </div>
        <avue-crud ref="crud"
                  :page="page"
                  :data="tableData"
                  :table-loading="tableLoading"
                  :option="tableOption"
                  @on-load="getList"
                  @refresh-change="refreshChange"
                  @search-change="searchChange"
                  @row-update="handleUpdate"
                  @row-save="handleSave"
                  @row-del="rowDel">
          <template slot-scope="scope"
                    slot="menu">
            <el-button type="text"
                      v-if="permissions.sys_client_edit"
                      icon="el-icon-check"
                      size="small"
                      @click="handleEdit(scope.row,scope.index)">编辑
            </el-button>
            <el-button type="text"
                      v-if="permissions.sys_client_del"
                      icon="el-icon-delete"
                      size="small"
                      @click="handleDel(scope.row,scope.index)">删除
            </el-button>
            <el-button type="text"
                      v-if="permissions.sys_client_edit"
                      icon="el-icon-refresh"
                      size="small"
                      @click="handleRotateSecret(scope.row)">轮换密钥
            </el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
  import {addObj, delObj, fetchList, getObj, putObj, rotateSecret} from '@/api/admin/sys-social-details'
  import {tableOption} from '@/const/crud/admin/sys-social-details'
  import {mapGetters} from 'vuex'

  export default {
    name: 'sys-social-details',
    data() {
      return {
        tableData: [],
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 20 // 每页显示多少条
        },
        tableLoading: false,
        tableOption: tableOption
      }
    },
    created() {
    },
    mounted: function () {
    },
    computed: {
      ...mapGetters(['permissions'])
    },
    methods: {
      getList(page, params) {
        this.tableLoading = true
        fetchList(Object.assign({
          current: page.currentPage,
          size: page.pageSize
        }, params)).then(response => {
          this.tableData = response.data.data.records
          this.page.total = response.data.data.total
          this.tableLoading = false
        })
      },
      /**
       * @title 打开新增窗口
       * @detail 调用crud的handleadd方法即可
       *
       **/
      handleAdd: function () {
        this.$refs.crud.rowAdd()
      },
      handleEdit(row, index) {
        this.$refs.crud.rowEdit(row, index)
      },
      handleDel(row, index) {
        this.$refs.crud.rowDel(row, index)
      },
      rowDel: function (row, index) {
        var _this = this
        this.$confirm('是否确认删除ID为' + row.id, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(function () {
          return delObj(row.id)
        }).then(data => {
          _this.tableData.splice(index, 1)
          _this.$message({
            showClose: true,
            message: '删除成功',
            type: 'success'
          })
          this.refreshChange()
        }).catch(error => { console.error(error) })
      },
      /**
       * @title 数据更新
       * @param row 为当前的数据
       * @param index 为当前更新数据的行数
       * @param done 为表单关闭函数
       *
       **/
      handleUpdate: function (row, index, done) {
        const payload = Object.assign({}, row)
        delete payload.appSecret
        putObj(payload).then(data => {
          this.tableData.splice(index, 1, Object.assign({}, row))
          this.$message({
            showClose: true,
            message: '修改成功',
            type: 'success'
          })
          this.refreshChange()
          done()
        })
      },
      /**
       * @title 数据添加
       * @param row 为当前的数据
       * @param done 为表单关闭函数
       *
       **/
      handleSave: function (row, done) {
        if (!row.appSecret) {
          this.$message({ showClose: true, message: '新增三方账号必须填写 appSecret', type: 'warning' })
          return
        }
        addObj(row).then(() => {
          this.tableData.push(Object.assign({}, row))
          this.$message({
            showClose: true,
            message: '添加成功',
            type: 'success'
          })
          this.refreshChange()
          done()
        })
      },
      /** 密钥从不由查询接口返回；管理员必须在此独立操作中主动输入新值。 */
      handleRotateSecret (row) {
        this.$prompt('请输入新的 appSecret；保存后旧密钥立即失效', '轮换三方账号密钥', {
          confirmButtonText: '确认轮换',
          cancelButtonText: '取消',
          inputType: 'password',
          inputPattern: /\S+/,
          inputErrorMessage: 'appSecret 不能为空'
        }).then(({ value }) => rotateSecret(row.id, value)).then(() => {
          this.$message({ showClose: true, message: '密钥已轮换', type: 'success' })
        }).catch(() => {})
      },
      /**
       * 刷新回调
       */
      refreshChange() {
        this.getList(this.page)
      },
      /**
       * 搜索回调
       */
      searchChange(form) {
        this.getList(this.page, form)
      }
    }
  }
</script>

<style lang="scss" scoped>
</style>
