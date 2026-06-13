<template>
  <el-dialog
    ref="dialog"
    title="导入供应商人员"
    :visible.sync="currVisible"
    width="900px"
    @open="open"
    @close="close"
    top="30px"
    :append-to-body="true"
    :custom-class="'supplier-person-dialog'"
  >
    <el-form :rules="uploadRules" ref="form" :model="uploadForm" label-width="120px" class="supplier-person-form">
      <el-form-item label="供应商名称" prop="supplierIds">
        <el-cascader :options="treeData" v-model="uploadForm.supplierIds" :show-all-levels="false" @change="supplierChange" change-on-select filterable placeholder="请选择" clearable></el-cascader>
        <!-- <el-select
          filterable
          v-model="uploadForm.supplierIds"
          placeholder="请选择"
          @change="supplierChange"
        >
          <el-option
            v-for="item in supplierList"
            :key="item.id"
            :label="item.companyName"
            :value="item.id"
          ></el-option>
        </el-select> -->
      </el-form-item>
      <el-form-item label="导入方式">
        <el-radio-group v-model="importType">
          <el-radio-button :label="1">excel导入</el-radio-button>
          <el-radio-button :label="2">从访客记录导入</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <template v-if="importType === 1">
        <el-form-item label="供应商人员信息">
          <a class="linkT" href="/resource/supplierPerson-temp.xlsx" download="供应商人员信息导入模板.xls" target="__blank">下载模板</a>
        </el-form-item>
        <el-form-item label="导入文件" prop="personDetails">
          <input type="file" ref="file" @change="fileChange" style="display: none" />
          <el-button type="primary" @click="upload" :plain="hasFile" icon="icon-yutong-upload">选择文件</el-button>
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item label="访客单位">
          <el-input v-model="company" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="访客来访时间">
          <div class="time-outer">
            <el-date-picker
              v-model="timeRange"
              type="datetimerange"
              range-separator="-"
              value-format="yyyy-MM-dd HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
              start-placeholder="起始时间"
              end-placeholder="截止时间"
              clearable
            ></el-date-picker>
            <el-radio-group v-model="time2" @change="getList()">
              <el-radio-button :label="7">近7天</el-radio-button>
              <el-radio-button :label="14">近14天</el-radio-button>
              <el-radio-button :label="30">近30天</el-radio-button>
            </el-radio-group>
          </div>
        </el-form-item>
        <el-form-item label=" ">
          <el-button type="primary" @click="getList()">查 询</el-button>
          <el-button type="primary" plain @click="clearSearch">清 空</el-button>
        </el-form-item>
        <el-form-item label=" ">
          <div class="visitInfo" v-if="visitRecord && visitRecord.length > 0">
            <div class="visitInfo_left">
              <el-scrollbar class="my-lit-scrollbar" style="width: 100%; height: 100%" :native="false">
                <div style="padding: 10px">
                  <div v-for="(item, index) in visitRecord" :key="index" class="info_card">
                    <div class="row">
                      <div class="row_label">访客单位：</div>
                      <div class="row_value">{{ item.company || '-' }}</div>
                    </div>
                    <div class="row">
                      <div class="row_label">访客姓名：</div>
                      <div class="row_value">{{ item.visitorName }}</div>
                    </div>
                    <div class="row">
                      <div class="row_label">身份证号：</div>
                      <div class="row_value">{{ item.visitorCertNo || '-' }}</div>
                    </div>
                    <div class="row">
                      <div class="row_label">手机号码：</div>
                      <div class="row_value">{{ item.visitorPhone }}</div>
                    </div>
                    <el-checkbox class="visit_checkbox" v-model="item.selected" @change="visitorChange(item)"></el-checkbox>
                  </div>
                </div>
              </el-scrollbar>
              <!-- <template v-if="!visitRecord || visitRecord.length===0">
                暂无数据
              </template> -->
            </div>
            <div class="visitInfo_right">
              <el-scrollbar class="my-lit-scrollbar" style="width: 100%; height: 100%" :native="false">
                <div style="padding: 10px">
                  <div v-for="(item2, index2) in selectedVisitRecord" :key="index2" class="info_card">
                    <div class="row">
                      <div class="row_left">
                        <div>
                          {{ item2.company }}
                          {{ item2.visitorName }}
                        </div>
                        <div>
                          <span :class="{ c_danger: !item2.visitorCertNo }">
                            {{ item2.visitorCertNo || '无证件号' }}
                          </span>
                        </div>
                        <div>
                          {{ item2.visitorPhone }}
                        </div>
                      </div>
                      <div class="row_right">
                        <template v-if="item2.noCertNo">
                          <span class="c_danger">不可添加</span>
                        </template>
                        <template v-else>
                          <template v-if="item2.isExist === true">
                            <span class="c_danger">已存在</span>
                          </template>
                          <template v-if="item2.isExist === false">
                            <span class="c_success">可添加</span>
                          </template>
                        </template>
                        <i class="close_i" @click="delPerson(item2, index2)"></i>
                      </div>
                    </div>
                  </div>
                  <template v-if="!selectedVisitRecord || selectedVisitRecord.length === 0">
                    <div class="holder">请在左侧勾选</div>
                  </template>
                </div>
              </el-scrollbar>
            </div>
          </div>
        </el-form-item>
      </template>
    </el-form>
    <div v-if="hasFile" class="fileInfo">
      <i class="el-icon-document"></i>
      {{ myFile.name }}
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="uploadLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getSupplierList, addUploadRecord, visitorFind } from '@/api/platform/security_area/supplier_person'
import { fetchList } from '@/api/platform/visitor/visitor_record'

import XLSX from 'xlsx'
import { isMobile } from '@/util/validate'
import { dateFormat } from '@/util/date'

export default {
  data() {
    return {
      currVisible: false,
      uploadLoading: false,
      supplierList: [], //可用供应商列表
      treeData: [],
      importType: 1,
      company: undefined,
      timeRange: undefined,
      time2: undefined,
      startTime: undefined,
      endTime: undefined,
      visitRecord: [], //访客记录
      selectedVisitRecord: [], //选中的访客集合
      finalVisitRecord: [], // 可添加的访客集合
      uploadForm: {
        supplierIds: undefined, //供应商记录标识
        personDetails: [] //人员数据
      },
      uploadRules: {
        supplierIds: [{ required: true, message: '请选择供应商', trigger: 'change' }],
        persons: [{ required: true, message: '请选择文件', trigger: 'change' }]
      },
      hasFile: false,
      myFile: {}
    }
  },
  props: {
    visible: Boolean
  },
  created() {
    this.getSupplierList()
  },
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      }
    },
    time2(val) {
      if (val) {
        this.timeRange = undefined
        const end = new Date()
        const start = new Date()
        if (val === 7) {
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
        } else if (val === 14) {
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 14)
        } else if (val === 30) {
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
        }
        this.startTime = dateFormat(start).split(' ')[0] + ' 00:00:00'
        this.endTime = dateFormat(end).split(' ')[0] + ' 23:59:59'
      }
    },
    timeRange(val) {
      if (val && val.length > 0) {
        this.time2 = undefined
        this.startTime = val[0]
        this.endTime = val[1]
      } else {
        if (!this.time2) {
          this.startTime = undefined
          this.endTime = undefined
        }
      }
    },
    selectedVisitRecord(val) {
      if (!val || val.length === 0) {
        this.visitRecord.forEach((el) => {
          el.selected = false
        })
      }
    }
  },
  methods: {
    /**
     * 验证表单
     */
    validateForm() {
      if (this.$refs.form) {
        return this.$refs.form.validate()
      }
      return Promise.resolve()
    },
    /**
     * 清空表单
     */
    clearForm() {
      this.myFile = {}
      this.hasFile = false
      this.visitRecord = []
      this.selectedVisitRecord = []
      this.finalVisitRecord = []
      this.company = undefined
      this.timeRange = undefined
      this.time2 = undefined
      this.startTime = undefined
      this.endTime = undefined
      this.uploadForm.supplierIds = undefined
      // this.$refs.form && this.$refs.form.resetFields()
    },
    async formSumit() {
      await this.validateForm()
      if (this.importType === 1) {
        if (!this.hasFile) {
          this.$message({
            message: '请选择文件！',
            type: 'warning'
          })
          return
        }
        if (!this.uploadForm.supplierIds || this.uploadForm.supplierIds.length < 3) {
          this.$message({
            message: '请先选中供应商！',
            type: 'warning'
          })
          return
        }
      } else if (this.importType === 2) {
        if (this.visitRecord.length === 0) {
          this.$message({
            message: '请先查询访客信息并进行勾选！',
            type: 'warning'
          })
          return
        }
        if (this.selectedVisitRecord.length === 0) {
          this.$message({
            message: '请选择要添加的访客信息！',
            type: 'warning'
          })
          return
        }
        this.finalVisitRecord = []
        let idCards = [] //所有不重复的身份证号
        let sameIdCards = [] //重复的身份证号
        //右侧列表
        this.selectedVisitRecord.forEach((el) => {
          if (el.isExist === false) {
            if (idCards.indexOf(el.visitorCertNo) !== -1) {
              sameIdCards.push(el.visitorCertNo)
            } else {
              idCards.push(el.visitorCertNo)
            }
          }
        })
        if (sameIdCards.length > 0) {
          this.$message({
            message: '可添加访客信息里，有重复证件号，请先删除不需要的访客信息！',
            type: 'error'
          })
          return
        }
        this.selectedVisitRecord.forEach((el) => {
          if (el.isExist === false) {
            this.finalVisitRecord.push(el)
          }
        })

        //右侧列表，可添加项目数量
        if (this.finalVisitRecord.length === 0) {
          this.$message({
            message: '当前选中，没有可添加的访客信息！',
            type: 'warning'
          })
          return
        }
        let temp = []
        this.finalVisitRecord.forEach((el) => {
          temp.push({
            idCard: el.visitorCertNo,
            name: el.visitorName,
            phone: el.visitorPhone
          })
        })
        this.uploadForm.personDetails = temp
      }
      this.uploadLoading = true
      this.uploadForm.supplierId = this.uploadForm.supplierIds[2]
      addUploadRecord(this.uploadForm)
        .then((response) => {
          var msg = response.data.msg
          var dataResult = response.data.data
          if (dataResult === true) {
            this.refresh()
            this.uploadLoading = false
            this.$notify({
              title: '成功',
              message: msg,
              type: 'success',
              duration: 2000
            })
          } else if (dataResult === false) {
            this.uploadLoading = false
            this.$notify({
              title: '失败',
              message: msg,
              type: 'error',
              duration: 2000
            })
          }
        })
        .catch(() => {
          this.uploadLoading = false
        })
    },
    supplierChange() {
      this.selectedVisitRecord = []
      this.finalVisitRecord = []
    },
    visitorChange(item) {
      //选中列表内 是否有 当前选中元素
      let obj = this.selectedVisitRecord.find((el) => {
        if (el.id === item.id) {
          return el
        }
      })

      //当前元素被选中
      if (item.selected) {
        if (!this.uploadForm.supplierIds) {
          this.$message({
            message: '请先选中供应商！',
            type: 'warning'
          })
          item.selected = false
          return
        }
        //被选中列表内，没有当前元素，则添加
        if (!obj) {
          this.visitorFind(item)
        }

        //当前元素取消选中
      } else {
        //被选中列表内，有当前元素，则删除
        if (obj) {
          for (let i = 0; i < this.selectedVisitRecord.length; i++) {
            if (this.selectedVisitRecord[i].id == item.id) {
              this.selectedVisitRecord.splice(i, 1)
              return
            }
          }
        }
      }
    },
    delPerson(item, index) {
      this.selectedVisitRecord.splice(index, 1)
      for (let i = 0; i < this.visitRecord.length; i++) {
        if (this.visitRecord[i].id == item.id) {
          this.visitRecord[i].selected = false
          return
        }
      }
    },
    async visitorFind(item) {
      if (item.visitorCertNo) {
        if (!this.uploadForm.supplierIds) {
          this.$message({
            message: '请先选中供应商！',
            type: 'warning'
          })
          return
        }
        let obj = {
          supplierId: this.uploadForm.supplierIds[2],
          idCard: item.visitorCertNo
        }
        const res = await visitorFind(obj)
        if (res.data.data === true || res.data.data === false) {
          this.$set(item, 'isExist', res.data.data)
          this.selectedVisitRecord.push(item)
        }
      } else {
        this.$set(item, 'noCertNo', true)
        this.selectedVisitRecord.push(item)
      }
    },
    //查询访客记录
    getList() {
      this.loading = true
      let params = {
        descs: 'create_time',
        current: 1,
        size: 10000,
        startTime: this.startTime,
        endTime: this.endTime,
        company: this.company
      }
      fetchList(params)
        .then((response) => {
          response.data.data.records.forEach((item) => {
            item.selected = false
          })
          this.visitRecord = response.data.data.records
          this.selectedVisitRecord = []
          this.loading = false
        })
        .catch((err) => {
          this.visitRecord = []
          this.selectedVisitRecord = []
        })
    },
    clearSearch() {
      this.company = undefined
      this.timeRange = undefined
      this.startTime = undefined
      this.endTime = undefined
      this.time2 = undefined
      this.visitRecord = []
      this.selectedVisitRecord = []
      this.finalVisitRecord = []
    },
    getSupplierList() {
      getSupplierList().then((response) => {
        this.supplierList = response.data.data
        let arr1 = []
        let arr2 = []
        this.supplierList.forEach((el) => {
          if (arr1.indexOf(el.parkId) == -1) {
            let obj = {
              value: el.parkName,
              parkId: el.parkId,
              label: el.parkName,
              companyCode: '',
              supplierType: '',
              children: el.children
            }
            arr1.push(el.parkId)
            arr2.push(obj)
          }
        })

        let arr3 = []
        arr2.forEach((el) => {
          let o2 = {
            a: {
              value: 'A类',
              parkId: el.parkId,
              label: 'A类',
              companyCode: '',
              supplierType: 1,
              children: []
            },
            na: {
              value: '非A类',
              parkId: el.parkId,
              label: '非A类',
              companyCode: '',
              supplierType: 2,
              children: []
            }
          }
          let tmpA = []
          let tmpNa = []
          if (el.children !== null) {
            tmpA = el.children.filter((item) => {
              return item.parkId === el.parkId && item.supplierType === 1
            })
            tmpNa = el.children.filter((item) => {
              return item.parkId === el.parkId && item.supplierType === 2
            })
          }
          let tmpA2 = []
          tmpA.forEach((el) => {
            el.label = el.companyName
            el.value = el.id
            el.children = null
            tmpA2.push(el)
          })
          let tmpNa2 = []
          tmpNa.forEach((el) => {
            el.label = el.companyName
            el.value = el.id
            el.children = null
            tmpNa2.push(el)
          })
          o2.a.children = tmpA2
          o2.na.children = tmpNa2
          arr3.push(o2)
        })
        arr2.forEach((el, index) => {
          el.children = [arr3[index].a, arr3[index].na]
        })
        this.treeData = arr2
      })
    },
    upload() {
      this.$refs.file.click()
    },
    fileChange(e) {
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
              persons = [] // 存储获取到的数据
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
              persons = persons.concat(XLSX.utils.sheet_to_json(workbook.Sheets[sheet]))
              break // 如果只取第一张表，就取消注释这行
            }
          }

          // 3、确保上传文件内存在有效数据
          if (_this.validatenull(persons)) {
            _this.$message({
              message: '请确保上传文件内存在有效数据！',
              type: 'warning'
            })
            return
          }
          let arr = []
          let certnoArr = []
          let certnoSameArr = []
          for (var { ['授权人员']: name, ['身份证号码']: idCard, ['手机号码']: phone } of persons) {
            if (_this.validatenull(name)) {
              _this.$message({
                message: '"授权人员"列的标题应为 "授权人员"，且前后和中间不要有空格！',
                type: 'warning'
              })
              return
            }

            if (_this.validatenull(idCard)) {
              _this.$message({
                message: '"身份证号码"列的标题应为 "身份证号码"，且前后和中间不要有空格！',
                type: 'warning'
              })
              return
            }

            if (_this.validatenull(phone)) {
              _this.$message({
                message: '"手机号码"列的标题应为 "手机号码"，且前后和中间不要有空格！',
                type: 'warning'
              })
              return
            }

            idCard = idCard.toUpperCase().replace(/(\s+)/g, '') //转大写、去除全部空格

            if (certnoArr.indexOf(idCard) == -1) {
              certnoArr.push(idCard)
            } else {
              certnoSameArr.push(idCard)
            }

            if (!isMobile(Number(phone))) {
              _this.$message({
                message: `${phone} 手机号格式错误`,
                type: 'warning'
              })
              throw new Error('NOT VERIFICATION PHONE')
            }

            arr.push({
              name: name,
              idCard: idCard,
              phone: phone
            })
          }
          // 4、如果存在相同证件号，则需删除重复项后重新上传
          if (certnoSameArr.length > 0) {
            _this.$message({
              message: '检测到有相同证件号的数据，请删除重复项再重新上传！',
              type: 'warning'
            })
            return
          }
          _this.hasFile = true
          _this.myFile = file
          _this.uploadForm.personDetails = arr
        }

        // 以二进制方式打开文件
        fileReader.readAsBinaryString(file)
      }
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.clearForm()
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.clearForm()
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
.supplier-person-form ::v-deep {
  $mColor: #ed6d00;
  $color-success: #10cc8e;
  $color-danger: #e7292e;
  .el-scrollbar__wrap {
    overflow-x: hidden;
  }
  .linkT {
    color: $mColor;
    text-decoration: underline;
  }
  .el-radio-button__inner {
    height: 36px;
    padding: 0 10px;
    text-align: center;
    line-height: 35px;
  }
  .el-radio-button__orig-radio:checked + .el-radio-button__inner {
    color: $mColor;
    background-color: transparent;
    border-color: $mColor;
    -webkit-box-shadow: -1px 0 0 0 $mColor;
    box-shadow: -1px 0 0 0 $mColor;
  }
  .el-radio-button__inner:hover {
    color: $mColor;
    border-color: $mColor;
    -webkit-box-shadow: -1px 0 0 0 $mColor;
    box-shadow: -1px 0 0 0 $mColor;
  }
  .holder {
    text-align: center;
    color: #999;
    padding-top: 30px;
  }
  .time-outer {
    display: flex;
    align-items: center;
    .el-range-editor {
      flex: 1;
      margin-right: 5px;
    }
    .el-radio-button__inner {
      width: 90px;
    }
  }
  .visitInfo {
    // border: 1px solid red;
    display: flex;
    justify-content: space-between;
    max-height: 385px;
    &_left {
      flex: 1;
      border: 1px solid #e0e0e0;
      margin-right: 15px;
      .info_card {
        position: relative;
        margin-bottom: 5px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        padding: 5px 10px;
        cursor: pointer;
        .visit_checkbox {
          position: absolute;
          top: 8px;
          right: 8px;
        }
        .row {
          display: flex;
          line-height: 25px;
          align-items: center;
          &_label {
            width: 70px;
          }
          &_value {
            flex: 1;
          }
        }
      }
    }
    &_right {
      flex: 1;
      border: 1px solid #e0e0e0;
      .info_card {
        margin-bottom: 5px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        padding: 5px 10px;
        .row {
          display: flex;
          line-height: 25px;
          align-items: center;
          .c_success {
            color: $color-success;
          }
          .c_danger {
            color: $color-danger;
          }
          &_left {
            flex: 1;
          }
          &_right {
            width: 88px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            span {
              display: inline-block;
              text-align: right;
            }
            .close_i {
              width: 25px;
              height: 25px;
              display: inline-block;
              background: url('/img/rm_unpass.png') center no-repeat;
              background-size: 100% 100%;
              cursor: pointer;
            }
          }
        }
      }
    }
  }
}
</style>
