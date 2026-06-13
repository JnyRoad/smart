<!--保密区供应商人员模板  -->
<template>
  <div class="my-basic-container mycard2 room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <div class="box-outer box-left">
        <supplierList ref="supplierList" @doSearch="doSearch"></supplierList>
      </div>
      <div class="my-basic-inner">
        <div class="box-outer">
          <div class="top-menu">
            <div class="top-right">
              <el-button type="primary" @click="supplierImportDialog.visible = true">导入供应商</el-button>
              <el-button type="primary" @click="exportExcelDialog.visible = true">导出供应商</el-button>
              <el-button type="primary" @click="importSupplierPerson">导入人员</el-button>
              <el-button type="primary" @click="export2Excel">导出人员</el-button>
              <el-button type="primary" @click="dlg1Visible = true">更新授权项目</el-button>
              <el-button type="primary" @click="dlg2Visible = true">到期设置</el-button>
              <el-button type="primary" @click="batchDel">批量删除</el-button>
              <!-- <el-button type="primary" @click="addFormVisible = true">添加人员</el-button> -->
            </div>
          </div>
          <el-form ref="searchform" :inline="true" :model="searchform" size="mini" class="topForm">
            <el-form-item label="授权人员" prop="personName">
              <el-input v-model="searchform.personName" placeholder="模糊查询" clearable></el-input>
            </el-form-item>
            <div style="display: inline-block">
              <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchform)">搜索</el-button>
              <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchform')" plain>重置</el-button>
            </div>
          </el-form>
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
            <template slot-scope="scope" slot="menu">
              <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row, scope.$index)">编辑</el-button>
              <el-button type="text" icon="el-icon-delete" @click="handleDel(scope.row, scope.$index)">删除</el-button>
            </template>
          </avue-crud>
        </div>
      </div>
    </el-scrollbar>
    <!-- 添加保密区供应商人员弹框 -->
    <el-dialog title="添加保密区供应商人员" class="dialog_form" width="500px" @close="resetAddForm('addForm')" :visible.sync="addFormVisible">
      <el-form :rules="addRules" ref="addForm" :model="addForm" label-width="120px">
        <el-form-item label="园区" prop="parkId">
          <parkSelect v-model="addForm.parkId" @doChange="getParkSupplierList"></parkSelect>
        </el-form-item>
        <el-form-item label="单位名称" prop="supplierId">
          <el-select v-model="addForm.supplierId" placeholder="请选择">
            <el-option v-for="item in parkSupplierList" :key="item.id" :label="item.companyName" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="人员姓名" prop="personName">
          <el-input v-model="addForm.personName" maxlength="5" clearable></el-input>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="addForm.idCard" maxlength="20" clearable></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="addForm.phone" maxlength="15" clearable></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="addForm.remark" maxlength="50" clearable></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="resetAddForm('addForm')" plain>取 消</el-button>
        <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">保 存</el-button>
      </div>
    </el-dialog>
    <!-- 编辑弹出模板 -->
    <el-dialog title="编辑保密区供应商人员" class="dialog_form config_form" width="600px" @close="resetEditForm('editForm')" :visible.sync="editFormVisible">
      <el-form :rules="addRules" ref="editForm" :model="editForm" label-width="80px">
        <el-form-item label="园区" prop="parkName">
          <el-input v-model="editForm.parkName" readonly></el-input>
        </el-form-item>
        <el-form-item label="单位名称" prop="companyName">
          <el-input v-model="editForm.companyName" readonly clearable></el-input>
        </el-form-item>
        <el-form-item label="人员姓名" prop="personName">
          <el-input v-model="editForm.personName" clearable></el-input>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="editForm.idCard" clearable></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" clearable></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="editForm.remark" clearable></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="resetEditForm('editForm')" plain>取 消</el-button>
        <el-button type="primary" @click="editSubmit('editForm')" :loading="editLoading">保 存</el-button>
      </div>
    </el-dialog>
    <!-- 批量导入供应商 -->
    <el-dialog title="导入供应商" class="dialog_form config_form" width="500px" @close="resetExportExcelForm('uploadSupplierForm')" :visible.sync="supplierImportDialog.visible">
      <el-form :rules="supplierImportDialog.rules" ref="uploadSupplierForm" :model="supplierImportDialog.formData" label-width="120px">
        <el-form-item label="园区" prop="parkId">
          <parkSelect v-model="supplierImportDialog.formData.parkId"></parkSelect>
        </el-form-item>
        <el-form-item label="分类" prop="supplierType">
          <el-select v-model="supplierImportDialog.formData.supplierType" placeholder="请选择">
            <el-option v-for="item in supplierTypeList" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="供应商信息">
          <a class="linkT" href="/resource/supplier-temp.xlsx" target="__blank">下载模板</a>
        </el-form-item>
        <el-form-item label="导入文件" prop="personDetails">
          <input type="file" ref="fileSupplier" @change="fileChangeSupplier" style="display: none" />
          <el-button type="primary" @click="uploadSupplier" :plain="hasFileSupplier" icon="icon-yutong-upload">选择文件</el-button>
        </el-form-item>
      </el-form>
      <div v-if="hasFileSupplier" class="fileInfo" style="margin-left: 120px">
        <i class="el-icon-document"></i>
        {{ myFileSupplier.name }}
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="resetExportExcelForm('uploadSupplierForm')" plain>取 消</el-button>
        <el-button type="primary" @click="uploadSupplierSubmit('uploadSupplierForm')" :loading="supplierImportDialog.loading">保 存</el-button>
      </div>
    </el-dialog>

    <!-- 导出供应商 -->
    <el-dialog title="导出供应商" class="dialog_form config_form" width="500px" @close="exportExcelDialog.visible = false" :visible.sync="exportExcelDialog.visible">
      <el-form :rules="exportExcelDialog.rules" ref="exportExcelForm" :model="exportExcelDialog.formData" label-width="120px">
        <el-form-item label="园区" prop="parkId">
          <parkSelect v-model="exportExcelDialog.formData.parkId"></parkSelect>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="exportExcelDialog.visible = false" plain>取 消</el-button>
        <el-button type="primary" @click="exportExcelSubmit('exportExcelForm')" :loading="exportExcelDialog.loading">导 出</el-button>
      </div>
    </el-dialog>

    <dlgSupplierPerson ref="dlgSupplierPerson" @refresh="refreshSupplierPerson" />
    <dlgAuthProject :visible="dlg1Visible" @dlgdo="dlg1do" @dlgdoSuccess="dlgdo1Success" />
    <dlgNoticeSet :visible="dlg2Visible" @dlgdo="dlg2do" @dlgdoSuccess="dlgdo2Success" />
  </div>
</template>

<script>
import { fetchList, delRecord, addRecord, getParkSupplierList } from '@/api/platform/security_area/supplier_person'
import { supplierImport, delPersonBatch, exportExcel } from './_service'
import { tableOption } from '@/const/crud/platform/security_area/supplier_person'
import supplierList from './_supplier'
import dlgAuthProject from './dlg_authProject'
import dlgNoticeSet from './dlg_noticeSet'
import dlgSupplierPerson from './dlg_supplierPerson'
import XLSX from 'xlsx'

const supplierTypeList = [
  { label: 'A类', value: 1 },
  { label: '非A类', value: 2 }
]
export default {
  name: 'dorm_mng',
  components: {
    supplierList,
    dlgAuthProject,
    dlgNoticeSet,
    dlgSupplierPerson
  },
  data() {
    return {
      dlg2Visible: false,
      dlg1Visible: false,
      exportLoading: false,
      supplierTypeList: supplierTypeList,
      selectStaffs: [], //当前选中员工的集合
      editForm: {},
      setLoading: false,
      addLoading: false, //是否正在添加
      editLoading: false, //是否正在编辑
      addFormVisible: false, //添加弹框
      editFormVisible: false, //编辑弹框
      parkSupplierList: [], //园区关联的可用供应商列表
      hasFileSupplier: false,
      myFileSupplier: {},
      supplierImportDialog: {
        visible: false,
        loading: false,
        formData: {},
        rules: {
          parkId: [{ required: true, message: '请选择园区', trigger: 'change' }],
          supplierType: [{ required: true, message: '请选择分类', trigger: 'change' }]
        }
      },
      exportExcelDialog: {
        visible: false,
        loading: false,
        formData: {},
        rules: {
          parkId: [{ required: true, message: '请选择园区', trigger: 'change' }]
        }
      },
      addForm: {
        parkId: undefined, //所属园区
        supplierId: undefined, //供应商记录标识
        personName: undefined, //人员姓名
        idCard: undefined, //身份证号
        phone: undefined, //联系电话
        remark: undefined //备注
      },
      addRules: {
        parkId: [{ required: true, message: '请选择园区', trigger: 'change' }],
        supplierId: [{ required: true, message: '请选择供应商', trigger: 'change' }],
        personName: [{ required: true, message: '请填写人员姓名', trigger: 'change' }],
        idCard: [{ required: true, message: '请填写身份证号', trigger: 'change' }],
        phone: [{ required: true, message: '请填写手机号码', trigger: 'blur' }]
      },
      searchform: {
        parkId: undefined, //园区ID
        personName: undefined, //人员名称
        supplierId: undefined //供应商记录标识
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      defaultProps: {
        label: 'parkName',
        value: 'id'
      },
      dormiProps: {
        label: 'dormitoryName',
        value: 'id'
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    }
  },
  created() {
    this.getList(this.page)
  },
  mounted: function () {},
  computed: {},
  methods: {
    importSupplierPerson() {
      this.$refs.dlgSupplierPerson && this.$refs.dlgSupplierPerson.open()
    },
    refreshSupplierPerson() {
      this.getList(this.page, this.searchform)
    },
    doSearch(id) {
      this.searchform.supplierId = id
      this.page = {
        total: 0,
        currentPage: 1,
        pageSize: 20
      }
      this.getList(this.page, this.searchform)
    },
    dlg1do(val) {
      this.dlg1Visible = val
    },
    dlgdo1Success() {},
    dlg2do(val) {
      this.dlg2Visible = val
    },
    dlgdo2Success() {},
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true
        const { export_json_to_excel } = require('@/vendor/Export2Excel')
        const tHeader = ['供应商名称', '授权人', '身份证号码']
        const filterVal = ['companyName', 'personName', 'idCard']
        let params = {
          current: 1,
          size: 10000
        }
        var _this = this
        fetchList(params)
          .then((response) => {
            let list = response.data.data.records
            const data = this.formatJson(filterVal, list)
            export_json_to_excel(tHeader, data, '供应商人员信息','供应商人员信息')
            this.exportLoading = false
          })
          .catch((err) => {
            this.exportLoading = false
          })
      })
    },
    // export3Excel

    async exportExcelSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          require.ensure([], () => {
            this.exportExcelDialog.formData.loading = true
            const params = {
              parkId: this.exportExcelDialog.formData.parkId
            }
            exportExcel(params)
              .then((response) => {
                var fileDownload = require('js-file-download')
                fileDownload(response.data, '供应商表格.xls')
                this.exportExcelDialog.formData.loading = false
              })
              .catch((err) => {
                this.exportExcelDialog.formData.loading = false
              })
          })
        }
      })
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map((v) => filterVal.map((j) => v[j]))
    },
    formatDate(numb, format) {
      const time = new Date((numb - 1) * 24 * 3600000 + 1)
      time.setYear(time.getFullYear() - 70)
      const year = time.getFullYear() + ''
      const month = time.getMonth() + 1 + ''
      const date = time.getDate() - 1 + ''
      if (format && format.length === 1) {
        return year + format + month + format + date
      }
      return year + (month < 10 ? '0' + month : month) + (date < 10 ? '0' + date : date)
    },
    selectChange(val) {
      //序号那边选择事件
      var idArr = []
      if (val.length > 0) {
        val.forEach(function (element) {
          idArr.push(element.id)
        }, this)
        this.selectStaffs = idArr
      }
    },
    uploadSupplier() {
      this.$refs.fileSupplier.click()
    },
    getList(page, params) {
      this.tableLoading = true
      fetchList(
        Object.assign(
          {
            asc: 'id',
            current: page.currentPage,
            size: page.pageSize
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
    getParkSupplierList(parkId) {
      this.parkSupplierList = []
      this.addForm.supplierId = ''
      getParkSupplierList({ parkId: parkId }).then((response) => {
        this.parkSupplierList = response.data.data
      })
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page, this.searchform)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.searchform)
    },

    addSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.addLoading = true
          addRecord(this.addForm)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true) {
                this.addFormVisible = false
                this.addLoading = false
                this.getList(this.page, this.searchform)
                this.$notify({
                  title: '成功',
                  message: msg,
                  type: 'success',
                  duration: 2000
                })
              } else if (dataResult === false) {
                this.addLoading = false
                this.$notify({
                  title: '失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
            })
            .catch(() => {
              this.addLoading = false
            })
        } else {
          return false
        }
      })
    },
    editSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.editLoading = true
          addRecord(this.editForm)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true) {
                this.editFormVisible = false
                this.editLoading = false
                this.getList(this.page, this.searchform)
                this.$notify({
                  title: '成功',
                  message: msg,
                  type: 'success',
                  duration: 2000
                })
              } else if (dataResult === false) {
                this.editLoading = false
                this.$notify({
                  title: '失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
            })
            .catch(() => {
              this.editLoading = false
            })
        } else {
          return false
        }
      })
    },
    //导入供应商，确定
    uploadSupplierSubmit(formName) {
      // console.log(this.myFileSupplier)
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.supplierImportDialog.loading = true
          supplierImport(this.supplierImportDialog.formData)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true) {
                this.resetExportExcelForm('uploadSupplierForm')
                // this.getList(this.page, this.searchform);
                this.$notify({
                  title: '成功',
                  message: msg,
                  type: 'success',
                  duration: 2000
                })
                this.$refs.supplierList.getSupplierList()
              } else if (dataResult === false) {
                this.supplierImportDialog.loading = false
                this.$notify({
                  title: '失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
            })
            .catch(() => {
              this.supplierImportDialog.loading = false
            })
        } else {
          return false
        }
      })
    },
    batchDel() {
      //批量删除
      if (this.selectStaffs.length == 0) {
        this.$message({
          message: '请先选中想要删除的人员信息！',
          type: 'warning'
        })
        return
      }
      var _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除所有选中的人员信息？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          // return delPersonBatch({ ids: _this.selectStaffs });

          return delPersonBatch(_this.selectStaffs)
        })
        .then((dataResponse) => {
          var msg = dataResponse.data.msg
          var dataResult = dataResponse.data.data
          if (dataResult == true) {
            _this.getList(_this.page, _this.searchform)
            _this.$notify({
              title: '删除成功',
              message: '删除成功',
              type: 'success',
              duration: 2000
            })
          } else if (dataResult === false) {
            _this.$notify({
              title: '删除失败',
              message: msg,
              type: 'error',
              duration: 2000
            })
          }
        })
        .catch(err => { console.error(err) })
    },
    resetAddForm(formName) {
      this.addFormVisible = false
      this.addLoading = false
      this.hasStartNum = false //将起始编号状态恢复默认设置
      this.$refs[formName].resetFields()
    },
    resetEditForm(formName) {
      this.editFormVisible = false
      this.editLoading = false
      this.$refs[formName].clearValidate()
    },
    resetExportExcelForm(formName) {
      this.supplierImportDialog.visible = false
      this.supplierImportDialog.loading = false
      this.myFileSupplier = {}
      this.hasFileSupplier = false
      this.$refs[formName].resetFields()
    },
    //重置 导入供应商
    resetUploadSupplierForm(formName) {
      this.exportExcelDialog.visible = false
      this.exportExcelDialog.loading = false
      this.$refs[formName].resetFields()
    },
    handleEdit(row, index, done, loading) {
      this.editFormVisible = true
      this.editForm = Object.assign({}, row)
    },
    handleDel(row, index) {
      //删除
      var _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除该供应商人员信息？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          return delRecord(row.id)
        })
        .then((data) => {
          if (data.data.data == false) {
            _this.$notify({
              title: '失败',
              message: data.data.msg,
              type: 'error'
            })
          } else {
            this.getList(this.page, this.searchform)
            _this.$notify({
              title: '成功',
              message: '删除成功',
              type: 'success'
            })
          }
        })
        .catch(error => { console.error(error) })
    },
    searchSubmit(form) {
      //搜索
      this.page.currentPage = 1
      this.getList(this.page, form)
    },
    resetFrom(formName) {
      //清空
      this.$refs[formName].resetFields()
      this.dormitoryData = []
      this.page.currentPage = 1
      this.getList(this.page)
    },
    fileChangeSupplier(e) {
      let _this = this
      const isNum = (value) => {
        const age = /^[0-9]*$/
        if (!age.test(value)) {
          return false
        } else {
          return true
        }
      }
      if (e.target.files.length > 0) {
        let file = e.target.files[0]

        var suffixPosition = file.name.lastIndexOf('.') + 1
        var nums = file.name.length - suffixPosition
        var suffix = file.name.substr(suffixPosition, nums) //文件后缀

        //1、判断所选文件格式
        const isExcel = suffix == 'xlsx' || suffix == 'xls'
        if (!isExcel) {
          //
          _this.$message({
            message: '请上传excel文件，文件后缀为 "xlsx" 或 "xls"！',
            type: 'warning'
          })
          return
        }

        var fileReader = new FileReader()
        fileReader.onload = function (ev) {
          try {
            var data = ev.target.result,
              workbook = XLSX.read(data, {
                type: 'binary'
              }), // 以二进制流方式读取得到整份excel表格对象
              suppliers = [] // 存储获取到的数据
          } catch (e) {
            return
          }
          // 表格的表格范围，可用于判断表头数量是否正确
          var fromTo = ''

          //2、确保上传文件内只有一张工作表
          if (workbook.Props.Worksheets > 1) {
            _this.$message({
              message: '请确保上传文件内只有一张工作表！',
              type: 'warning'
            })
            return
          }

          // 遍历每张表读取
          for (var sheet in workbook.Sheets) {
            if (workbook.Sheets.hasOwnProperty(sheet)) {
              fromTo = workbook.Sheets[sheet]['!ref']
              suppliers = suppliers.concat(XLSX.utils.sheet_to_json(workbook.Sheets[sheet]))
              break // 如果只取第一张表，就取消注释这行
            }
          }

          // 3、确保上传文件内存在有效数据
          if (_this.validatenull(suppliers)) {
            _this.$message({
              message: '请确保上传文件内存在有效数据！',
              type: 'warning'
            })
            return
          }
          let arr = []
          for (var {
            ['供应商名称']: companyName,
            ['协议签订日期']: beginEffectTime,
            ['协议到期日期']: endEffectTime,
            ['申请理由']: remark,
            ['联系人']: contactPerson,
            ['授权项目']: authorList,
            ['授权区域']: authorizedArea,
            ['授权人数']: authPersonNum
          } of suppliers) {
            if (_this.validatenull(companyName)) {
              _this.$message({
                message: '"供应商名称"列的标题应为 "供应商名称"，且不能为空！',
                type: 'warning'
              })
              return
            }

            if (_this.validatenull(beginEffectTime)) {
              _this.$message({
                message: '"协议签订日期"列的标题应为 "协议签订日期"，且不能为空！',
                type: 'warning'
              })
              return
            }

            if (_this.validatenull(endEffectTime)) {
              _this.$message({
                message: '"协议到期日期"列的标题应为 "协议到期日期"，且不能为空！',
                type: 'warning'
              })
              return
            }

            if (_this.validatenull(remark)) {
              _this.$message({
                message: '"申请理由"列的标题应为 "申请理由"，且不能为空！',
                type: 'warning'
              })
              return
            }

            if (_this.validatenull(contactPerson)) {
              _this.$message({
                message: '"联系人"列的标题应为 "联系人"，且不能为空！',
                type: 'warning'
              })
              return
            }
            if (authPersonNum && !isNum(authPersonNum)) {
              _this.$message({
                message: '"授权人数"列的标题应为 "授权人数"，且必须为数字！',
                type: 'warning'
              })
              return
            }

            if (typeof beginEffectTime !== 'string') {
              beginEffectTime = _this.formatDate(beginEffectTime, '-')
            } else {
              if (beginEffectTime.indexOf('/') > -1) {
                beginEffectTime = beginEffectTime.replace(/\//g, '-')
              }
            }

            if (typeof endEffectTime !== 'string') {
              endEffectTime = _this.formatDate(endEffectTime, '-')
            } else {
              if (endEffectTime.indexOf('/') > -1) {
                endEffectTime = endEffectTime.replace(/\//g, '-')
              }
            }

            arr.push({
              companyName,
              beginEffectTime,
              endEffectTime,
              remark,
              contactPerson,
              authorList,
              authorizedArea,
              authPersonNum
            })
          }

          _this.hasFileSupplier = true
          _this.myFileSupplier = file
          _this.supplierImportDialog.formData.supplierInfoList = arr
        }
        // 以二进制方式打开文件
        fileReader.readAsBinaryString(file)
      }
    }
  }
}
</script>
<style lang="scss" scoped>
@media screen and (max-width: 1366px) {
  .theme-yutong .el-button--primary {
    font-size: 12px;
  }
}
.mycard2 .box-left {
  width: 400px;
}
.mycard2 .my-scrollbar {
  padding: 0 0 0 405px;
}
</style>
