<!--访客预约，黑名单  -->
<template>
  <div class="my-basic-container park">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-radio-group v-model="blackType" @change="changeBlackType">
            <el-radio-button :label="0">园区黑名单</el-radio-button>
            <el-radio-button :label="1">EHR黑名单</el-radio-button>
          </el-radio-group>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button
              type="primary"
              v-if="blackType==0"
              icon="el-icon-plus"
              @click="addFormVisible = true"
            >添加黑名单</el-button>
            <el-button type="primary" v-if="blackType==0" @click="supplierImportDialog.visible = true">导入黑名单</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="姓名" prop="personName">
            <el-input v-model="searchForm.personName" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="身份证号" prop="cardNo">
            <el-input v-model="searchForm.cardNo" placeholder="身份证号" clearable></el-input>
          </el-form-item>
          <el-form-item label="园区" prop="parkId" v-if="blackType==0">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          v-if="blackType==0"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
          @row-del="rowDel"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>
          </template>
        </avue-crud>

        <avue-crud
          ref="crud"
          v-if="blackType==1"
          :page="page"
          :data="tableHrData"
          :table-loading="tableLoading"
          :option="tableHrOption"
          @size-change="sizeChange"
          @current-change="currentChange"
          @row-del="rowDel"
        ></avue-crud>
        <el-dialog
          title="添加黑名单"
          class="dialog_form"
          @close="resetAddFrom('addForm')"
          width="500px"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="rules" ref="addForm" :model="addform" label-width="120px">
            <el-form-item label="姓名" prop="personName">
              <el-input v-model="addform.personName" placeholder="请输入" clearable></el-input>
            </el-form-item>
            <el-form-item label="身份证号" prop="cardNo">
              <el-input v-model="addform.cardNo" placeholder="请输入" clearable></el-input>
            </el-form-item>
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="addform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="原因" prop="reason">
              <el-input v-model="addform.reason" placeholder="请输入" clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addCancel('addForm')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
    <!-- 批量导入黑名单 -->
    <el-dialog title="导入黑名单" class="dialog_form config_form" width="500px" @close="resetUploadSupplierForm('uploadSupplierForm')" :visible.sync="supplierImportDialog.visible">
      <el-form ref="uploadSupplierForm" :model="supplierImportDialog.formData" label-width="120px">
        <el-form-item label="园区" prop="parkId">
          <parkSelect v-model="supplierImportDialog.formData.parkId"></parkSelect>
        </el-form-item>
        <el-form-item label="黑名单信息">
          <a class="linkT" href="/resource/blacklist.xlsx" target="__blank">下载模板</a>
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
        <el-button type="primary" @click="resetUploadSupplierForm('uploadSupplierForm')" plain>取 消</el-button>
        <el-button type="primary" @click="uploadSupplierSubmit('uploadSupplierForm')" :loading="supplierImportDialog.loading">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {
  fetchList,
  addObj,
  delObj,
  fetchHrList,
  supplierImport
} from "@/api/platform/visitor/blacklist";
import { tableOption } from "@/const/crud/platform/visitor/blacklist";
import { tableHrOption } from "@/const/crud/platform/visitor/blacklist_hr";
import { mapGetters } from "vuex";
import { cardid } from "@/util/validate";
import XLSX from 'xlsx'

export default {
  name: "blacklist",
  data() {
    let _this = this;
    var validateUsername = (rule, value, callback) => {
      if (/^[0-9a-zA-Z\u4e00-\u9fa5_\s]{2,20}$/.test(value) == false) {
        this.$message.closeAll();
        this.$message({
          message:
            "姓名应包含2-20个字符，可为汉字、数字、字母（大小写）、下划线!",
          type: "warning"
        });
      } else {
        callback();
      }
    };
    var validateCertNo = (rule, value, callback) => {
      let val = value.replace(/(\s+)/g, ""); //去除全部空格
      let r = cardid(val);
      if (r[0]) {
        this.$message.closeAll();
        this.$message({
          message: r[1],
          type: "warning"
        });
      } else {
        this.addform.cardNo = val;
        callback();
      }
    };
    return {
      addLoading: false,
      addFormVisible: false, //添加园区，显示切换
      searchForm: {
        //搜索菜单表单
        cardNo: undefined,
        personName: undefined,
        parkId: undefined
      },
      blackType: 0,
      addform: {
        //添加园区，表单
        cardNo: "",
        personName: "",
        parkId: null,
        reason: ""
      },
      rules: {
        personName: [
          { required: true, message: "请输入姓名", trigger: "blur" },
          { validator: validateUsername, trigger: "blur" }
        ],
        cardNo: [
          { required: true, message: "请输入身份证号", trigger: "blur" },
          { validator: validateCertNo, trigger: "blur" }
        ]
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },

      tableLoading: false,
      tableData: [],
      tableHrData: [],
      tableOption: tableOption,
      tableHrOption: tableHrOption,
      hasFileSupplier: false,
      myFileSupplier: {},
      supplierImportDialog: {
        visible: false,
        loading: false,
        formData: {},
      },
      fileList: []
    };
  },
  created: function() {
    this.getList(this.page);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    //导入供应商，确定
    uploadSupplierSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.supplierImportDialog.loading = true
          supplierImport(this.supplierImportDialog.formData.parkId, this.fileList)
            .then((response) => {
              var dataResult = response.data.data
              if (dataResult.length === 0) {
                this.supplierImportDialog.visible = false
                this.supplierImportDialog.loading = false
                // this.getList(this.page, this.searchform);
                this.$notify({
                  title: '成功',
                  message: '导入成功',
                  type: 'success',
                  duration: 2000
                })
                this.getList(this.page);
              } else{
                this.supplierImportDialog.loading = false
                const arr = []
                dataResult.forEach(element => {
                  arr.push(element.cardNo + element.failReason)
                });
                const msg = arr.join(',')
                // const msg =
                this.$notify({
                  title: '失败',
                  message: msg,
                  type: 'error',
                  duration: 5000
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
    uploadSupplier() {
      this.$refs.fileSupplier.click()
    },
    //重置 导入供应商
    resetUploadSupplierForm(formName) {
      this.supplierImportDialog.visible = false
      this.supplierImportDialog.loading = false
      this.myFileSupplier = {}
      this.hasFileSupplier = false
      this.fileList = []
      this.$refs[formName].resetFields()
    },
    changeBlackType() {
      this.page.currentPage = 1;
      if (this.blackType == 0) {
        this.getList(this.page);
      } else if (this.blackType == 1) {
        this.getHrList(this.page);
      }
    },
    getList(page, params) {
      if (this.blackType == 1) {
        this.getHrList(page, params);
        return
      }
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
      this.tableLoading = false;
    },
    getHrList(page, params) {
      this.tableLoading = true;
      fetchHrList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then(response => {
        this.tableHrData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
      this.tableLoading = false;
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page);
    },
    addSubmit(formName) {
      //添加内容确定
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addLoading = true;
          addObj(this.addform)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.getList(this.page);
                this.addFormVisible = false;
                this.$notify({
                  title: "成功",
                  message: msg,
                  type: "success",
                  duration: 2000
                });
              } else if (dataResult === false) {
                this.$notify({
                  title: "失败",
                  message: msg,
                  type: "error",
                  duration: 2000
                });
              }
              this.addLoading = false;
            })
            .catch(err => {
              this.addLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    addCancel(formName) {
      this.addFormVisible = false;
      this.resetAddFrom(formName);
    },
    resetAddFrom(formName) {
      this.$refs[formName].resetFields();
    },
    handleDel(row, index) {
      //删除
      this.$refs.crud.rowDel(row, index);
    },
    rowDel: function(row, index) {
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该黑名单信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      })
        .then(function() {
          return delObj(row.id);
        })
        .then(dataResponse => {
          var msg = dataResponse.data.msg;
          var dataResult = dataResponse.data.data;
          if (dataResult == true) {
            _this.getList(this.page);
            _this.$notify({
              title: "删除成功",
              message: "删除成功",
              type: "success",
              duration: 2000
            });
          } else if (dataResult === false) {
            _this.$notify({
              title: "删除失败",
              message: msg,
              type: "error",
              duration: 2000
            });
          }
        })
        .catch(err => { console.error(err) });
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
        this.page.currentPage = 1;
        this.getList(this.page);
      }
    },
    fileChangeSupplier(e) {
      let _this = this
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
            ['姓名']: personName,
            ['身份证号']: cardNo,
            ['原因']: reason
          } of suppliers) {
            if (_this.validatenull(personName)) {
              _this.$message({
                message: '"姓名"列不应该存在空值！',
                type: 'warning'
              })
              return
            }

            if (_this.validatenull(cardNo)) {
              _this.$message({
                message: '"身份证号"列不应该存在空值！',
                type: 'warning'
              })
              return
            }

            if (_this.validatenull(reason)) {
              _this.$message({
                message: '"原因"列不应该存在空值！',
                type: 'warning'
              })
              return
            }

            arr.push({
              personName,
              cardNo,
              reason
            })
          }

          _this.hasFileSupplier = true
          _this.myFileSupplier = file
          _this.fileList = arr
        }
        // 以二进制方式打开文件
        fileReader.readAsBinaryString(file)
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
