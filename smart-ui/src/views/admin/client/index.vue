<template>
  <div class="my-basic-container execution">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-button type="primary"  icon="el-icon-plus" @click="handleAdd">添加 App</el-button>
        </div>
        <avue-crud ref="crud"
                  :page="page"
                  :data="tableData"
                  :table-loading="tableLoading"
                  :option="tableOption"
                  v-model="form"
                  :before-open="handleOpenBefore"
                  @on-load="getList"
                  @refresh-change="refreshChange"
                  @row-update="handleUpdate"
                  @row-save="handleSave"
                  @row-del="rowDel">
          <!-- 授权园区不是数据库原生字段（写入 additionalInformation.allowedParkIds），
               用 avue-crud-select 多选组件承接，数据源复用现有 allPark() 接口 -->
          <template slot="allowedParkIdsForm" slot-scope="scope">
            <avue-crud-select v-model="allowedParkIds" multiple placeholder="请选择授权园区" :dic="parkOptions" :props="parkProps"></avue-crud-select>
          </template>
          <template slot-scope="scope"
                    slot="menu">
            <el-button type="text"
                      v-if="permissions.sys_client_edit"
                      icon="el-icon-check"
                      size="mini"
                      @click="handleEdit(scope.row,scope.index)">编辑
            </el-button>
            <el-button type="text"
                      v-if="permissions.sys_client_edit"
                      icon="el-icon-refresh"
                      size="mini"
                      @click="handleResetSecret(scope.row)">重置 App Secret
            </el-button>
            <el-button type="text"
                      v-if="permissions.sys_client_del"
                      icon="el-icon-delete"
                      size="mini"
                      @click="handleDel(scope.row,scope.index)">删除
            </el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <!-- 重置 App Secret 结果弹窗：明文只在这一次响应里返回，关闭后无法再次查看，
         所以弹窗强调该提示并提供一键复制，降低用户忘记保存的风险 -->
    <el-dialog title="App Secret 已重置"
              width="480px"
              :close-on-click-modal="false"
              :visible.sync="secretDialogVisible">
      <el-alert type="warning" :closable="false" show-icon title="关闭后不可再查看，请立即保存"></el-alert>
      <p class="secret-text">{{ newSecret }}</p>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary"
                  v-clipboard:copy="newSecret"
                  v-clipboard:success="onSecretCopySuccess"
                  v-clipboard:error="onSecretCopyError">复制</el-button>
        <el-button plain @click="secretDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import {addObj, delObj, fetchList, mergeAllowedParkIds, putObj, resetSecret} from '@/api/admin/client'
  import {allPark} from '@/api/platform/_publicService'
  import {tableOption} from '@/const/crud/admin/client'
  import {mapGetters} from 'vuex'

  export default {
    name: 'client',
    data() {
      return {
        tableData: [],
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 20 // 每页显示多少条
        },
        tableLoading: false,
        tableOption: tableOption,
        form: {},
        // 授权园区多选框当前选中值；与 form.allowedParkIds 保持同步（见 watch）
        allowedParkIds: [],
        parkOptions: [],
        parkProps: {
          label: 'parkName',
          value: 'id'
        },
        // 重置 App Secret 结果弹窗
        secretDialogVisible: false,
        newSecret: ''
      }
    },
    created() {
    },
    mounted: function () {
    },
    computed: {
      ...mapGetters(['permissions'])
    },
    watch: {
      allowedParkIds() {
        this.form.allowedParkIds = this.allowedParkIds
      }
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
      /**
       * 表单弹窗打开前的准备：拉取园区下拉数据源，并把当前行的 scope/授权园区
       * 从后端存储格式（逗号字符串 / additionalInformation JSON）反解成表单可编辑的数组。
       */
      handleOpenBefore(show, type) {
        allPark().then(response => {
          this.parkOptions = response.data.data
        })
        if (['edit', 'view'].includes(type)) {
          // scope 落库是逗号分隔字符串，多选下拉需要数组
          this.form.scope = this.form.scope ? this.form.scope.split(',') : []
          this.allowedParkIds = this.parseAllowedParkIds(this.form.additionalInformation)
        } else {
          this.form.scope = []
          this.allowedParkIds = []
        }
        show()
      },
      /**
       * 从 additionalInformation JSON 文本里读出 allowedParkIds，解析失败时静默按空数组处理
       * （编辑表单打开阶段不弹错，真正的防御性提示在保存时的 mergeAllowedParkIds 里）。
       */
      parseAllowedParkIds(rawAdditionalInformation) {
        if (!rawAdditionalInformation) {
          return []
        }
        try {
          const parsed = JSON.parse(rawAdditionalInformation)
          return Array.isArray(parsed.allowedParkIds) ? parsed.allowedParkIds : []
        } catch (e) {
          return []
        }
      },
      rowDel: function (row, index) {
        var _this = this
        this.$confirm('是否确认删除ID为' + row.clientId, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(function () {
          return delObj(row.clientId)
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
       * 把表单里的 scope 数组 / allowedParkIds 数组转换回后端存储格式：
       * scope → 逗号分隔字符串；allowedParkIds → 防御性 merge 进 additionalInformation JSON。
       * 解析失败（parseError）时提示用户，但仍按“仅本次授权园区生效”的结果继续保存，
       * 不做静默覆盖以外的隐藏行为。
       */
      buildSubmitPayload(row) {
        const payload = Object.assign({}, row)
        payload.scope = Array.isArray(row.scope) ? row.scope.join(',') : row.scope
        const { text, parseError } = mergeAllowedParkIds(row.additionalInformation, this.allowedParkIds)
        if (parseError) {
          this.$message({
            showClose: true,
            message: '扩展信息原内容不是合法 JSON，仅保留授权园区字段，请检查后再保存',
            type: 'warning'
          })
        }
        payload.additionalInformation = text
        return payload
      },
      /**
       * @title 数据更新
       * @param row 为当前的数据
       * @param index 为当前更新数据的行数
       * @param done 为表单关闭函数
       *
       **/
      handleUpdate: function (row, index, done) {
        putObj(this.buildSubmitPayload(row)).then(data => {
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
        addObj(this.buildSubmitPayload(row)).then(data => {
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
      /**
       * 重置 App Secret：二次确认 → 调用重置接口 → 用弹窗展示一次性明文。
       * 明文只存在于本次响应，组件不落地保存，用户必须自己复制保存。
       */
      handleResetSecret(row) {
        this.$confirm('重置后旧 App Secret 立即失效，且新密钥只会展示一次，是否继续？', '重置 App Secret', {
          confirmButtonText: '确定重置',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          return resetSecret(row.clientId)
        }).then(response => {
          this.newSecret = response.data.data
          this.secretDialogVisible = true
        }).catch(error => { if (error !== 'cancel') console.error(error) })
      },
      onSecretCopySuccess() {
        this.$notify({
          title: '复制成功',
          message: '已复制到粘贴板，请妥善保存',
          type: 'success',
          duration: 2000
        })
      },
      onSecretCopyError() {
        this.$notify({
          title: '复制失败',
          message: '复制失败，请手动选中文本复制',
          type: 'warning',
          duration: 2000
        })
      },
      /**
       * 刷新回调
       */
      refreshChange() {
        this.getList(this.page)
      }
    }
  }
</script>

<style lang="scss" scoped>
.secret-text {
  margin: 16px 0;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
  word-break: break-all;
  font-family: monospace;
}
</style>
