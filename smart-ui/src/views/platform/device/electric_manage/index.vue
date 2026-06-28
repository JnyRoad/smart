<!--设备管理，闸机管理  -->
<template>
  <div class="my-basic-container device electric">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm', searchForm)" plain>清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加电表</el-button>
            <!-- <el-button type="primary" @click="handelSetTag" plain>设置标签</el-button> -->
            <el-button type="primary" @click="supplierImportDialog.visible = true">导入电表</el-button>
            <el-button type="primary" :loading="exportLoading" @click="export2Excel" icon>导出电表</el-button>
            <el-dropdown style="margin-left: 16px">
              <el-button type="primary" plain
                >批量操作
                <i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item>
                  <span @click="batchDelete">批量删除</span>
                </el-dropdown-item>
                <el-dropdown-item>
                  <span @click="meterRead">批量手动抄表</span>
                </el-dropdown-item>
                <el-dropdown-item>
                  <span @click="reDownload">批量下载档案</span>
                </el-dropdown-item>
                <el-dropdown-item>
                  <span @click="handelChange(1)">批量开闸</span>
                </el-dropdown-item>
                <el-dropdown-item>
                  <span @click="handelChange(0)">批量关闸</span>
                </el-dropdown-item>
                <el-dropdown-item>
                  <span @click="handelSetTag">批量设置标签</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="园区" prop="parkId">
              <parkSelect v-model="searchForm.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="设备位置" prop="place">
              <el-input v-model="searchForm.place" placeholder="请输入房间号/厂区地点" clearable></el-input>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="searchForm.status" placeholder="请选择接通状态" clearable>
                <el-option label="未连接" value="0"></el-option>
                <el-option label="离线" value="1"></el-option>
                <el-option label="在线" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="通信地址" prop="address">
              <el-input v-model="searchForm.address" placeholder="请输入通信地址" clearable></el-input>
            </el-form-item>
            <el-form-item label="关联集中器" prop="concenId">
              <el-select v-model="searchForm.concenId" placeholder="请选择" clearable>
                <template v-for="(item, index) in meterList">
                  <el-option :label="item.name" :value="item.id" :key="index"></el-option>
                </template>
              </el-select>
            </el-form-item>
            <el-form-item label="设备标签" prop="tagIds">
              <el-select v-model="searchForm.tagIds" placeholder="请选择" clearable>
                <template v-for="(item, index) in tagList">
                  <el-option :label="item.tagName" :value="item.id" :key="index"></el-option>
                </template>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-bottom: 20px;" v-if="datalist.length > 0">
          <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="checkAllChange">全选</el-checkbox>
        </div>
        <el-checkbox-group v-model="ckItem" class="list-group">
          <div class="device-list">
            <template v-for="(item, key, index) in datalist">
              <div class="device-outer" :key="index">
                <dl class="device-item clear">
                  <div class="device-status deviceStatus0" v-bind:class="returnColor(item.status)">
                    <span>{{ item.status }}</span>
                  </div>
                  <dt></dt>
                  <dd>
                    <el-checkbox class="ck1" :label="item.id">1</el-checkbox>
                    <div class="info-tbl">
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">设备名称</div>
                        <div class="tbl_cell" :title="item.name">{{ item.name || '-' }}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">通信地址</div>
                        <div class="tbl_cell" :title="item.address">{{ item.address || '-' }}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">绑定房间</div>
                        <div class="tbl_cell" :title="item.roomName">{{ item.roomName || '-' }}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">所在区域</div>
                        <div class="tbl_cell" :title="item.dormitoryName" v-if="item.placeType === 0">{{ item.dormitoryName || '-' }}</div>
                        <div class="tbl_cell" :title="item.areaName" v-if="item.placeType === 1">{{ item.areaName || '-' }}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">关联集中器</div>
                        <div class="tbl_cell" :title="item.concentratorName">{{ item.concentratorName || '-' }}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">设备标签</div>
                        <div class="tbl_cell" :title="item.tagName">{{ item.tagName || '-' }}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">当前读数</div>
                        <div class="tbl_cell" :title="item.currentReading">{{ item.currentReading || '-' }}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">闸门控制</div>
                        <div class="tbl_cell">
                          <el-switch
                            v-if="item.valveStatus === 0 || item.valveStatus === 1"
                            v-model="item.valveStatus"
                            :active-color="activeColor"
                            :inactive-color="inactiveColor"
                            :active-value="1"
                            :inactive-value="0"
                            @change="valveChange(item)"
                          ></el-switch>
                          <span v-if="item.valveStatus === 4">开启中</span>
                          <span v-if="item.valveStatus === 2">未关联闸门</span>
                          <span v-if="item.valveStatus === 3">关闭中</span>
                        </div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell2">
                          <div class="circle-btns">
                            <el-button type="info" @click="handleEdit(item, false)" icon="el-icon-edit" class="edit-btn" title="编辑"></el-button>
                            <el-button type="info" @click="handleEdit(item, true)" icon="el-icon-sort" class="edit-btn" title="更换设备"></el-button>
                            <el-button type="info" @click="handleDel(item)" icon="el-icon-delete" class="del-btn" title="删除"></el-button>
                          </div>
                          <div>
                            <el-button type="primary" @click="logsList(item)" class="perm-btn" plain round>操作记录</el-button>
                            <el-button type="primary" @click="permittedList(item)" class="perm-btn" plain round>历史度数</el-button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </dd>
                </dl>
              </div>
            </template>
          </div>
        </el-checkbox-group>
        <div v-if="!hasData" class="noData">
          <i></i>
          暂无数据！
        </div>
        <div class="page-outer" v-show="hasData">
          <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="page.currentPage"
            :page-sizes="[10, 20, 30, 40]"
            :page-size="page.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="page.total"
          ></el-pagination>
        </div>
        <el-dialog :title="addTitle" :close-on-click-modal="false" width="700px" :visible.sync="addFormVisible" @close="closeDialog">
          <el-form :rules="addRules" ref="addForm" :model="addForm" label-width="120px">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="addForm.name" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备序号" prop="seq">
              <el-input v-model="addForm.seq" clearable></el-input>
            </el-form-item>
            <el-form-item label="倍率" prop="ratio">
              <el-input v-model="addForm.ratio" clearable></el-input>
            </el-form-item>
            <el-form-item label="通信地址" prop="address">
              <el-input v-model="addForm.address" clearable></el-input>
            </el-form-item>
            <el-form-item label="通信端口号" prop="port">
              <el-select v-model="addForm.port" placeholder="请选择" clearable>
                <template v-for="(item, index) in portList">
                  <el-option :label="item" :value="item" :key="index"></el-option>
                </template>
              </el-select>
            </el-form-item>
            <!-- portList -->
            <el-form-item label="集中器" prop="concentratorId">
              <el-select v-model="addForm.concentratorId" placeholder="请选择" clearable>
                <template v-for="(item, index) in meterList">
                  <el-option :label="item.name" :value="item.id" :key="index"></el-option>
                </template>
              </el-select>
            </el-form-item>
            <el-form-item label="区域类型" prop="placeType" v-if="!isEdit">
              <el-radio v-model="addForm.placeType" label="0">宿舍</el-radio>
              <el-radio v-model="addForm.placeType" label="1">厂区</el-radio>
            </el-form-item>
            <el-form-item label="绑定宿舍" prop="roomName" v-if="addForm.placeType == 0 && !isEdit">
              <el-input v-model="addForm.roomName" placeholder="绑定宿舍" disabled>
                <template slot="append">
                  <el-button @click="bindRoom">绑定宿舍</el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="绑定区域" prop="areaIdArray" v-if="addForm.placeType == 1 && !isEdit">
              <areaCascader v-model="addForm.areaIdArray" :changeOnSelect="true" placeholder="所在区域"></areaCascader>
            </el-form-item>
            <el-form-item label="设备标签" prop="tagIds">
              <el-select v-model="addForm.tagIds" placeholder="请选择" multiple clearable>
                <template v-for="(item, index) in tagList">
                  <el-option :label="item.tagName" :value="item.id" :key="index"></el-option>
                </template>
              </el-select>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="closeDialog" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
    <!-- 选择房间 -->
    <BindRoom ref="bindRoom" @done="selectBed" />
    <setTag ref="setTag" :deviceIds="ckItem" @refresh="refresh"></setTag>
    <!-- 批量导入 -->
    <el-dialog title="导入电表" width="500px" @close="resetUploadSupplierForm('uploadSupplierForm')" :visible.sync="supplierImportDialog.visible">
      <el-form ref="uploadSupplierForm" :model="supplierImportDialog.formData" label-width="120px">
        <el-form-item label="电表模板">
          <a class="linkT" href="/resource/ele-temp.xls" target="__blank">下载模板</a>
        </el-form-item>
        <el-form-item label="导入文件" prop="personDetails">
          <input
            type="file"
            ref="fileSupplier"
            accept="application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            @change="fileChangeSupplier"
            style="display: none"
          />
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

<style lang="scss">
@use "@/styles/platform/device/camera" as *;
</style>

<script>
import { concentratorList, getTagList, addObj, fetchList, batchDelete, putObj,changeObj, supplierImport, flushProgress, putValve, putValves, meterRead, reDownload} from './_service'
import { returnColor, formatJson, placeTypeDesc } from './electric-manage-rules'
import { mapGetters } from 'vuex'
import setTag from './components/setTag'
import deviceTagSelect from '../components/device-tag-select'
import BindRoom from './components/bind_room'
import XLSX from 'xlsx'
export default {
  mixins: [tce.mixins.list],
  name: 'alarm',
  components: {
    setTag,
    deviceTagSelect,
    BindRoom
  },
  data() {
    return {
      settingLoading: false,
      settingForm: {
        list: []
      },
      addLoading: false,
      editLoading: false,
      addFormVisible: false, //添加闸机
      editFormVisible: false, //编辑闸机
      editDisabled: false,
      activeColor: '#10CC8F',
      inactiveColor: '#C6CAD3',
      ckItem: [],
      page: {
        total: null, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      searchForm: {
        parkId: null,
        place: null,
        status: null,
        address: null,
        concenId: null,
        tagIds: null
      },
      addTitle:'新增电表',
      addForm: {
        name: '',
        concentratorId: null,
        address: '',
        port: '',
        seq: '',
        ratio: '1',
        roomId: null,
        roomName: '',
        tagIds: [],
        placeType: '0',
        areaIdArray: []
      },
      addRules: {
        name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
        address: [{ required: true, message: '请输入通信地址', trigger: 'blur' }],
        port: [{ required: true, message: '请选择通信端口号', trigger: 'change' }],
        concentratorId: [{ required: true, message: '请选择集中器', trigger: 'change' }],
        seq: [{ required: true, message: '请输入设备序号', trigger: 'blur' }],
        ratio: [{ required: true, message: '请输入倍率', trigger: 'blur' }],
        placeType: [{ required: true, message: '请选择区域类型', trigger: 'blur' }],
        roomName: [{ required: true, message: '请选择绑定房间', trigger: 'blur' }],
        areaIdArray: [{ required: true, message: '请选择绑定区域', trigger: 'blur' }]
      },
      meterList: [],
      tagList: [],
      datalist: [],
      isEdit: false,
      portList: ['1', '2', '3', '4'],
      hasFileSupplier: false,
      myFileSupplier: {},
      supplierImportDialog: {
        visible: false,
        loading: false,
        formData: {}
      },
      checkAll: false,
      isIndeterminate: false,
      exportLoading: false
    }
  },
  created() {
    this.$nextTick(() => {
      if (this.$route.query.queryForm != undefined) {
        let queryPage = this.$route.query.queryPage
        let queryForm = this.$route.query.queryForm
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {})
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {})
        }
      }
      if (this.$route.query.concenId != undefined) {
        const concenId = this.$route.query.concenId
        this.searchForm.concenId = concenId
      }
      this.getTagList()
      this.getMeter()
      this.getList(this.page, this.searchForm)
    })
  },
  computed: {
    ...mapGetters(['permissions']),
    hasData: function () {
      if (this.datalist) {
        if (this.datalist.length > 0) {
          return true
        } else {
          return false
        }
      } else {
        return false
      }
    }
  },
  methods: {
    //全选
    checkAllChange(val) {
      if (val) {
        this.ckItem = []
        this.datalist.forEach((el) => {
          this.ckItem.push(el.id)
        })
      } else {
        this.ckItem = []
      }
      this.isIndeterminate = false
    },
    async uploadSupplierSubmit() {
      const loading = this.$loading({
        lock: true,
        text: '导入中',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.3)',
        customClass: 'my-scheduling-submit-loading'
      })
      this.progressLoad(loading)
      const formdata = new FormData()
      formdata.append('file', this.myFileSupplier)
      const res = await supplierImport(formdata)
      let resBlob = new Blob([res.data])
      let reader = new FileReader()
      reader.readAsText(resBlob, 'utf-8')
      reader.onload = () => {
        try {
          let res2 = JSON.parse(reader.result)
          if (res2.code !== 0) {
            this.$message.error(res2.message)
            this.resetUploadSupplierForm('uploadSupplierForm')
          } else {
            this.$message({
              message: '导入成功',
              type: 'success'
            })
            this.resetUploadSupplierForm('uploadSupplierForm')
            this.getList(this.page, this.searchForm)
          }
        } catch (e) {
          if (res.data.byteLength > 0) {
            // 导入接口成功后，有可能返回失败名单
            this.$message.error('导入数据存在错误信息，请查看返回文件，重新检查数据！')
            var fileDownload = require('js-file-download')
            fileDownload(res.data, '电表错误表格.xls')
          } else {
            this.$message({
              message: '导入成功',
              type: 'success'
            })
            this.resetUploadSupplierForm('uploadSupplierForm')
            this.getList(this.page, this.searchForm)
          }
        }
        loading.close()
      }
    },
    progressLoad(loading) {
      let isInit = false
      let loop = setInterval(function () {
        flushProgress().then((res) => {
          let data = res.data.data
          if (data.maxSize > 0 && data.remainSize === 0 && isInit) {
            clearInterval(loop) //停止定时任务
          } else {
            isInit = true
            if (data.maxSize > 0) {
              loading.text = '导入中，总共' + data.maxSize + '，剩余' + data.remainSize
            }
          }
        })
      }, 1000) //单位毫秒  注意：如果导出页面很慢时，建议循环时间段稍长一点
    },
    uploadSupplier() {
      this.$refs.fileSupplier.click()
    },
    //重置 导入
    resetUploadSupplierForm(formName) {
      this.supplierImportDialog.visible = false
      this.supplierImportDialog.loading = false
      this.myFileSupplier = {}
      this.hasFileSupplier = false
      this.$refs['fileSupplier'].value = null
      this.$refs[formName].resetFields()
    },
    valveChange(item) {
      let str = ''
      if (item.valveStatus === 1) {
        str = '确认打开该电表闸门？'
      } else {
        str = '确认关闭该电表闸门？'
      }
      var _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, str)]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          const obj = {
            status: item.valveStatus,
            eleMeterId: item.id
          }
          return putValve(obj)
        })
        .then((dataResponse) => {
          _this.getList(_this.page, _this.searchForm)
          _this.$notify({
            title: '修改成功',
            message: '修改成功',
            type: 'success',
            duration: 2000
          })
        })
        .catch(() => {
          _this.getList(_this.page, _this.searchForm)
        })
    },
    async meterRead(){
      if (this.ckItem && this.ckItem.length > 0) {
        const res = await meterRead({meterIds: this.ckItem})
        if(res.data.code === 0){
          this.$notify({
            title: '提示',
            message: '抄表成功！',
            type: 'success',
            duration: 2000
          })
        }
      }else {
        this.$message.error('请先勾选需要抄表的设备')
      }
    },
    async reDownload() {
      if (this.ckItem && this.ckItem.length > 0) {
        const res = await reDownload({ meterIds: this.ckItem })
        if (res.data.code === 0) {
          this.$notify({
            title: '提示',
            message: '下载档案成功！',
            type: 'success',
            duration: 2000
          })
        }
      } else {
        this.$message.error('请先勾选需要下载档案的设备')
      }
    },
    handelChange(item) {
      if (this.ckItem && this.ckItem.length > 0) {
        let str = ''
        if (item === 1) {
          str = '确认打开电表闸门？'
        } else {
          str = '确认关闭电表闸门？'
        }
        var _this = this
        const elm = this.$createElement
        this.$msgbox({
          message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, str)]),
          showCancelButton: true,
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          customClass: 'small_dialog',
          center: true
        })
          .then(function () {
            const obj = {
              status: item,
              meterIds: _this.ckItem
            }
            return putValves(obj)
          })
          .then((dataResponse) => {
            _this.getList(_this.page, _this.searchForm)
            _this.$notify({
              title: '修改成功',
              message: '修改成功',
              type: 'success',
              duration: 2000
            })
          })
          .catch(() => {
            _this.getList(_this.page, _this.searchForm)
          })
      } else {
        this.$message.error('请先勾选要设置的设备')
      }
    },
    batchDelete(){
      if (this.ckItem && this.ckItem.length > 0) {
        const _this = this
        const str =  '确认要删除这些设备?'
        const elm = this.$createElement
        this.$msgbox({
          message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, str)]),
          showCancelButton: true,
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          customClass: 'small_dialog',
          center: true
        })
          .then(function () {
            const obj = {
              meterIds: _this.ckItem
            }
            return batchDelete(obj)
          })
          .then((dataResponse) => {
            _this.getList(_this.page, _this.searchForm)
            _this.$notify({
              title: '提示',
              message: dataResponse.data.data === true?'删除成功！':dataResponse.data.data,
              type: 'success',
              // duration: 2000
            })
          })
          .catch(() => {
            _this.getList(_this.page, _this.searchForm)
          })
      } else {
        this.$message.error('请先勾选要删除的设备')
      }
    },
    handelSetTag() {
      if (this.ckItem && this.ckItem.length > 0) {
        this.$refs.setTag && this.$refs.setTag.open()
      } else {
        this.$message.error('请先勾选要设置的设备')
      }
    },
    refresh() {
      this.ckItem = []
      this.getList(this.page, this.searchForm)
    },
    bindRoom() {
      this.$refs.bindRoom && this.$refs.bindRoom.open()
    },
    selectBed(item) {
      this.addForm.roomId = item.id
      this.addForm.roomName = item.dormitoryName + item.roomName
    },
    getMeter() {
      const obj = {
        current: 1,
        size: 999
      }
      concentratorList(Object.assign(obj)).then((response) => {
        this.meterList = response.data.data.records
      })
    },
    async getTagList() {
      const res = await getTagList()
      this.tagList = res.data.data
    },
    getList(page, params) {
      this.tableLoading = true
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then((response) => {
        this.datalist = response.data.data.records
        this.datalist.forEach((el) => {
          el.tagName = ''
          el.tagIds = []
          if (el.tagList !== null && el.tagList.length > 0) {
            const arr1 = []
            el.tagList.forEach((element) => {
              arr1.push(element.tagName)
              el.tagIds.push(element.id)
            })
            el.tagName = arr1.join(',')
          }
        })
        this.page.total = response.data.data.total
        this.tableLoading = false
      })
    },
    // updateStatus(item) {
    //   putObj(item)
    // },
    /**
     * 顶部搜索
     */
    searchSubmit(form) {
      this.page.currentPage = 1
      this.getList(this.page, form)
    },
    /**
     * 清空搜索
     */
    resetFrom(formName, form) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields()
      }
      this.getList(this.page, form)
    },
    /**
     * 添加闸机确定
     */
    addSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.addLoading = true
          this.addForm.seq = parseInt(this.addForm.seq)
          this.addForm.ratio = parseInt(this.addForm.ratio)
          if (this.addForm.id) {
            const obj = {
              name: this.addForm.name,
              address: this.addForm.address,
              concentratorId: this.addForm.concentratorId,
              port: this.addForm.port,
              seq: this.addForm.seq,
              ratio: this.addForm.ratio,
              // roomId: this.addForm.roomId,
              // roomName: this.addForm.roomName,
              tagIds: this.addForm.tagIds,
              id: this.addForm.id,
              placeType: parseInt(this.addForm.placeType)
            }
            switch (this.addForm.placeType) {
              case '1':
                const al = this.addForm.areaIdArray.length
                obj['areaId'] = this.addForm.areaIdArray[al - 1]
                break
              case '0':
                obj['roomId'] = this.addForm.roomId
                break
            }
            if (this.isEdit) {
              changeObj(obj)
              .then((dataResponse) => {
                this.addFormVisible = false
                this.addLoading = false
                if(dataResponse.data.code === 0){
                  this.$notify({
                    title: '更换成功',
                    message: '更换成功',
                    type: 'success',
                    duration: 2000
                  })
                  this.closeDialog()
                  this.getList(this.page, this.searchForm)
                }
              })
              .catch((err) => {
                this.addLoading = false
              })
            }else{
              putObj(obj)
              .then((dataResponse) => {
                this.addFormVisible = false
                this.addLoading = false
                if (dataResponse.data.code === 0) {
                  this.$notify({
                    title: '编辑成功',
                    message: '编辑成功',
                    type: 'success',
                    duration: 2000
                  })
                }
                this.closeDialog()
                this.getList(this.page, this.searchForm)
              })
              .catch((err) => {
                this.addLoading = false
              })
            }
          } else {
            this.addForm.placeType = parseInt(this.addForm.placeType)
            switch (this.addForm.placeType) {
              case 1:
                const al = this.addForm.areaIdArray.length
                this.addForm['areaId'] = this.addForm.areaIdArray[al - 1]
                break
              case 0:
                this.addForm['roomId'] = this.addForm.roomId
                break
            }
            addObj(this.addForm)
              .then((dataResponse) => {
                this.addFormVisible = false
                this.addLoading = false
                this.$notify({
                  title: '添加成功',
                  message: '添加成功',
                  type: 'success',
                  duration: 2000
                })
                this.closeDialog()
                this.getList(this.page, this.searchForm)
              })
              .catch((err) => {
                this.addLoading = false
              })
          }
        } else {
          return false
        }
      })
    },
    /**
     * 编辑闸机打开
     */
    handleEdit(item, type) {
      this.addFormVisible = true
      this.isEdit = type
      if (type) {
        this.addTitle = '更换设备'
      } else {
        this.addTitle = '编辑电表'
      }
      this.addForm = { ...item }
      this.addForm.roomName = item.dormitoryName + item.roomName
      this.addForm.placeType = JSON.stringify(this.addForm.placeType)
      if(this.addForm['areaIds'] !== null){
        this.addForm.areaIdArray = this.addForm['areaIds']
      }
    },
    closeDialog() {
      this.addFormVisible = false
      this.addForm = {
        name: '',
        concentratorId: null,
        seq: null,
        ratio: 1,
        roomId: null,
        address: '',
        roomName: '',
        tagIds: []
      }
    },
    handleDel: function (row) {
      var _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除该电表信息？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          return batchDelete({meterIds: [row.id]})
        })
        .then((dataResponse) => {
          _this.getList(_this.page, _this.searchForm)
          _this.$notify({
            title: '提示',
            message: dataResponse.data.data === true?'删除成功！':dataResponse.data.data,
            type: 'success',
            // duration: 2000
          })
        })
        .catch(error => { console.error(error) })
    },
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "设备名称",
          "关联集中器",
          "通信端口号",
          "设备序号",
          "通信地址",
          "区域类型",
          "所在区域",
          "当前度数",
          "状态"
        ];
        const filterVal = [
          "name",
          "concentratorName",
          "port",
          "seq",
          "address",
          "placeTypeDesc",
          "dormitoryName",
          "currentReading",
          "status"
        ];
        let params =  {
          current: 1,
          size: 10000
        }
        var _this = this;
        fetchList(params, this.searchForm)
          .then(response => {
            const list = response.data.data.records;
            list.forEach(function(item) {
              item.placeTypeDesc = placeTypeDesc(item.placeType)
            });
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, "电表信息");
            this.exportLoading = false;
          })
          .catch(err => {
            this.exportLoading = false;
          });
      });
    },
    //导出相关：委托给抽出的纯函数（见 electric-manage-rules.js）。
    formatJson(filterVal, jsonData) {
      return formatJson(filterVal, jsonData)
    },
    /**
     * 操作记录
     */
    logsList(item) {
      const src = `/platform/device/electricManage/log/${item.id}`
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm,
          name: item.name
        }
      })
    },
    /**
     * 历史度数
     */
    permittedList(item) {
      const src = `/platform/device/electricManage/record/${item.id}`
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm,
          name: item.name
        }
      })
    },
    // 委托给抽出的纯函数（见 electric-manage-rules.js）；模板绑定 returnColor(item.status) 不变。
    returnColor(s) {
      return returnColor(s)
    },
    /**
     * 搜索回调
     */
    searchChange(form) {
      this.getList(this.page, form)
    },
    handleSizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page, this.searchForm)
    },
    handleCurrentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.searchForm)
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
        _this.hasFileSupplier = true
        _this.myFileSupplier = file
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.device .dialog_form .el-dialog__body .row .el-form-item {
  width: 100%;
}
.noDate {
  text-align: center;
  padding: 50px 0;
  color: #999;
}
.row ::v-deep {
  display: flex;
  .el-form-item {
    display: flex;
    align-items: center;
  }
  .el-form--label-left .el-form-item__label {
    text-align: right;
  }
  .el-switch {
    margin: -9px 0 0 0;
  }
  .c1 {
    flex: 1;
    padding-top: 7px;
  }
  .c2 {
    width: 250px;
  }
  .c3 {
    width: 300px;
    .el-input {
      width: 80px;
      padding: 0;
    }
  }
  .deviceStatus1 {
    background: #e7282d;
    border-color: #e7282d;
  }
  .deviceStatus2 {
    color: #10cc8f;
    background: #d9fff2;
    border-color: #10cc8f;
  }
}
.my-scheduling-submit-loading {
  .el-loading-spinner {
    width: 240px;
    background: rgba(255, 255, 255, 0.6);
    padding: 40px 0 30px;
    border-radius: 10px;
    overflow: hidden;
    left: 50%;
    margin-left: -120px;
  }
  .el-icon-loading {
    font-size: 24px;
    margin-bottom: 10px;
  }
  // .el-icon-loading,
  // .el-loading-text {

  // }
}
</style>
