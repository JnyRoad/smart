<!-- 业务设置，ISC平台绑定配置 -->
<template>
  <div class="my-basic-container parking_lot">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" plain @click="resetFrom('searchForm')">清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="openAdd">添加ISC平台绑定</el-button>
          </div>
        </div>

        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
        </el-form>

        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
        >
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="openEdit(scope.row)">编辑</el-button>
            <el-button
              type="text"
              icon="el-icon-search"
              :disabled="scope.row.cardSyncEnabled !== 1"
              @click="handleDryRun(scope.row)"
            >预检</el-button>
            <el-button
              type="text"
              icon="el-icon-upload2"
              :disabled="scope.row.cardSyncEnabled !== 1"
              @click="handleImport(scope.row)"
            >初始化导入</el-button>
            <el-button type="text" icon="el-icon-document" @click="openImportRecord(scope.row)">同步记录</el-button>
            <el-button type="text" icon="el-icon-delete" @click="handleDel(scope.row, scope.$index)">删除</el-button>
          </template>
        </avue-crud>

        <el-dialog
          :title="dialogTitle"
          class="dialog_form"
          width="560px"
          :visible.sync="formVisible"
          @close="resetEditForm('editform')"
        >
          <el-form ref="editform" :rules="rules" :model="editform" label-width="120px">
            <el-form-item label="业务园区" prop="parkId">
              <parkSelect v-model="editform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="ISC调度园区" prop="dispatcherParkId">
              <parkSelect v-model="editform.dispatcherParkId"></parkSelect>
            </el-form-item>
            <el-form-item label="卡片同步" prop="cardSyncEnabled">
              <el-switch
                v-model="editform.cardSyncEnabled"
                :active-value="1"
                :inactive-value="0"
                active-text="启用"
                inactive-text="停用"
              ></el-switch>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="editform.remark"
                type="textarea"
                :rows="3"
                maxlength="512"
                show-word-limit
                clearable
              ></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" plain @click="cancelEdit('editform')">取 消</el-button>
            <el-button type="primary" :loading="submitLoading" @click="submitEdit('editform')">确 定</el-button>
          </div>
        </el-dialog>

        <el-dialog
          :title="staffScopeDialogTitle"
          class="dialog_form"
          width="520px"
          :visible.sync="staffScopeVisible"
          @close="resetStaffScopeDialog"
        >
          <el-form :model="staffScopeForm" label-width="100px">
            <el-form-item label="人员范围">
              <el-radio-group v-model="staffScopeForm.staffScope">
                <el-radio-button label="ALL">全部人员</el-radio-button>
                <el-radio-button label="ACTIVE">在职人员</el-radio-button>
                <el-radio-button label="RESIGNED">离职人员</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <div class="staff-scope-tip">{{ staffScopeTip }}</div>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" plain @click="staffScopeVisible = false">取 消</el-button>
            <el-button type="primary" :loading="staffScopeSubmitting" @click="submitStaffScope">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList, editObj, delObj, startCardImportDryRun, startCardImport } from './isc_park_config_service'
import { tableOption } from '@/const/crud/platform/business/isc_park_config'

const emptyForm = () => ({
  id: null,
  parkId: '',
  dispatcherParkId: '',
  cardSyncEnabled: 1,
  remark: ''
})

const emptyStaffScopeForm = () => ({
  staffScope: 'ACTIVE'
})

export default {
  name: 'iscParkConfig',
  data() {
    return {
      submitLoading: false,
      formVisible: false,
      staffScopeVisible: false,
      staffScopeSubmitting: false,
      staffScopeAction: '',
      staffScopeRow: null,
      staffScopeForm: emptyStaffScopeForm(),
      dialogTitle: '添加ISC平台绑定',
      editform: emptyForm(),
      rules: {
        parkId: [{ required: true, message: '请选择业务园区', trigger: 'blur' }],
        dispatcherParkId: [{ required: true, message: '请选择ISC调度园区', trigger: 'blur' }],
        cardSyncEnabled: [{ required: true, message: '请选择卡片同步开关', trigger: 'change' }]
      },
      page: {
        total: 0,
        currentPage: 1,
        pageSize: 10
      },
      searchForm: {
        parkId: ''
      },
      tableLoading: false,
      tableData: [],
      tableOption
    }
  },
  computed: {
    staffScopeDialogTitle() {
      return this.staffScopeAction === 'IMPORT' ? '选择初始化导入范围' : '选择预检范围'
    },
    staffScopeTip() {
      const scopeText = {
        ALL: '全部人员',
        ACTIVE: '在职人员',
        RESIGNED: '离职人员'
      }[this.staffScopeForm.staffScope] || '全部人员'
      if (this.staffScopeAction === 'IMPORT') {
        return `将从海康ISC查询该园区${scopeText}的实体卡；离职人员卡片会立即作废并生成退卡任务，冲突数据不会自动覆盖。`
      }
      return `将从海康ISC查询该园区${scopeText}的实体卡并生成预检明细，不会写入本地卡表。`
    }
  },
  created() {
    this.getList(this.page)
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true
      fetchList(Object.assign({
        current: page.currentPage,
        size: page.pageSize
      }, params)).then(response => {
        const data = response.data.data || {}
        this.tableData = data.records || []
        this.page.total = data.total || 0
        this.tableLoading = false
      }).catch(() => {
        this.tableLoading = false
      })
    },
    openAdd() {
      this.dialogTitle = '添加ISC平台绑定'
      this.editform = emptyForm()
      this.formVisible = true
    },
    openEdit(row) {
      this.dialogTitle = '编辑ISC平台绑定'
      this.editform = Object.assign(emptyForm(), row)
      this.formVisible = true
    },
    submitEdit(formName) {
      this.$refs[formName].validate(valid => {
        if (!valid) {
          return false
        }
        this.submitLoading = true
        editObj(this.editform).then(() => {
          this.$message({
            showClose: true,
            message: '保存成功',
            type: 'success'
          })
          this.formVisible = false
          this.submitLoading = false
          this.getList(this.page, this.searchForm)
        }).catch(() => {
          this.submitLoading = false
        })
      })
    },
    cancelEdit(formName) {
      this.formVisible = false
      this.resetEditForm(formName)
    },
    resetEditForm(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].clearValidate()
      }
    },
    handleDel(row, index) {
      this.$confirm('是否确认删除该ISC平台绑定配置', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delObj(row.id)
      }).then(() => {
        this.$message({
          showClose: true,
          message: '删除成功',
          type: 'success'
        })
        this.getList(this.page, this.searchForm)
      }).catch(error => { console.error(error) })
    },
    handleDryRun(row) {
      this.openStaffScopeDialog(row, 'DRY_RUN')
    },
    handleImport(row) {
      this.openStaffScopeDialog(row, 'IMPORT')
    },
    openStaffScopeDialog(row, action) {
      this.staffScopeRow = row
      this.staffScopeAction = action
      this.staffScopeForm = emptyStaffScopeForm()
      this.staffScopeVisible = true
    },
    resetStaffScopeDialog() {
      this.staffScopeSubmitting = false
      this.staffScopeRow = null
      this.staffScopeAction = ''
      this.staffScopeForm = emptyStaffScopeForm()
    },
    submitStaffScope() {
      if (!this.staffScopeRow) {
        return
      }
      this.staffScopeSubmitting = true
      const request = this.staffScopeAction === 'IMPORT' ? startCardImport : startCardImportDryRun
      const successMessage = this.staffScopeAction === 'IMPORT' ? '初始化导入任务已提交' : '预检任务已提交'
      request({
        parkId: this.staffScopeRow.parkId,
        staffScope: this.staffScopeForm.staffScope
      }).then(response => {
        const batch = response.data.data || {}
        this.$message({
          showClose: true,
          message: successMessage,
          type: 'success'
        })
        const row = this.staffScopeRow
        this.staffScopeVisible = false
        this.openImportRecord(row, batch.id)
      }).catch(() => {
        this.staffScopeSubmitting = false
      })
    },
    openImportRecord(row, batchId) {
      this.$router.push({
        path: '/platform/records/isc_card_import/index',
        query: {
          parkId: row.parkId,
          batchId: batchId || ''
        }
      })
    },
    searchSubmit(form) {
      this.page.currentPage = 1
      this.getList(this.page, form)
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
    resetFrom(formName) {
      if (this.$refs[formName]) {
        this.$refs[formName].resetFields()
      }
      this.page.currentPage = 1
      this.getList(this.page)
    }
  }
}
</script>

<style scoped>
.staff-scope-tip {
  margin-left: 100px;
  line-height: 20px;
  color: #606266;
  font-size: 13px;
}
</style>
