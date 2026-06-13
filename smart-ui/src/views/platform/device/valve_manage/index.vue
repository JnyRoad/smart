<!--设备管理，门禁管理  -->
<template>
  <div class="my-basic-container device guard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-plus" @click="addData()">新增水表阀门</el-button>
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm', searchForm)" plain>清空</el-button>
          </div>
        </div>
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="searchForm.name" placeholder="设备名称" clearable></el-input>
            </el-form-item>
            <el-form-item label="本地/远程状态" prop="remoteStatus">
              <el-select v-model="searchForm.remoteStatus" placeholder="请选择本地/远程状态" clearable>
                <el-option label="本地" :value="0"></el-option>
                <el-option label="远程" :value="1"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="开关状态" prop="openStatus">
              <el-select v-model="searchForm.openStatus" placeholder="请选择开关状态" clearable>
                <el-option label="开" :value="1"></el-option>
                <el-option label="关" :value="0"></el-option>
              </el-select>
            </el-form-item>
             <!-- <el-form-item label="关联集中器" prop="concenId">
              <el-select v-model="searchForm.concenId" placeholder="请选择" clearable>
                <template v-for="(item, index) in meterList">
                  <el-option :label="item.name" :value="item.id" :key="index"></el-option>
                </template>
              </el-select>
            </el-form-item> -->
          </el-form>
        </div>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="remoteStatus">
            <el-switch
              v-model="scope.row.remoteStatus"
              :active-color="activeColor"
              :inactive-color="inactiveColor"
              :active-value="1"
              :inactive-value="0"
              @change="switchChangeRemoteStatus(scope.row)"
            ></el-switch>
          </template>
          <template slot-scope="scope" slot="isOpen">
            <el-switch
              v-model="scope.row.isOpen"
              :active-color="activeColor"
              :inactive-color="inactiveColor"
              :active-value="1"
              :inactive-value="0"
              @change="switchChange(scope.row)"
            ></el-switch>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="handleDetail(scope.row)">编辑</el-button>
            <el-button type="text" @click="logsList(scope.row)">操作记录</el-button>
          </template>
        </avue-crud>
      </section>
      <!-- 新增集中器 弹出框 -->
      <el-dialog :title="addForm.id ? '编辑水表阀门' : '新增水表阀门'" :close-on-click-modal="false" @close="closeDialog" :visible.sync="dialogVisible">
        <el-scrollbar>
          <el-form :rules="addRules" ref="addForm" :model="addForm">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="addForm.name" clearable></el-input>
            </el-form-item>
            <el-form-item label="集中器" prop="concentratorId">
              <el-select v-model="addForm.concentratorId"  placeholder="请选择" clearable>
                <template v-for="(item, index) in meterList">
                  <el-option :label="item.name" :value="item.id" :key="index"></el-option>
                </template>
              </el-select>
            </el-form-item>
            <el-form-item label="设备序号" prop="seq">
              <el-input type="number" v-model="addForm.seq"  clearable></el-input>
            </el-form-item>
            <el-form-item label="设备标签" prop="tagIds">
              <deviceTagSelect v-model="addForm.tagIds"></deviceTagSelect>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="addForm.remark" placeholder="请输入备注"></el-input>
            </el-form-item>
          </el-form>
        </el-scrollbar>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" plain @click="closeDialog">取 消</el-button>
          <el-button type="primary" @click="save" :loading="addLoading">保 存</el-button>
        </div>
      </el-dialog>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/device/camera" as *;
</style>

<script>
import { valveList, addValve, updateValve, getTagList, concentratorList, putValve, changeRemoteStatus} from './_service'
import deviceTagSelect from '../components/device-tag-select'

export default {
  mixins: [tce.mixins.list],
  name: 'alarm',
  components: {
    deviceTagSelect
  },
  data() {
    const isNum = (rule, value, callback) => {
      const age= /^[0-9]*$/
      if (!age.test(value)) {
        callback(new Error('只能输入数字'))
      }else{
        callback()
      }
    }
    return {
      tableOption: tableOption,
      page: {
        total: null, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      searchForm: {
        name: '',
        concentratorId: null
      },
      tableData: [],
      tableLoading: false,
      addLoading: false,
      addForm: {
        // id: '',
        name: null,
        concentratorId: null,
        seq: null,
        tagIds: [],
        remark: null
      },
      dialogVisible: false,
      addRules: {
        name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
        concentratorId: [{ required: true, message: '请选择集中器', trigger: 'change' }],
        seq: [
          { required: true, message: '请输入设备序号', trigger: 'blur' },
          { validator: isNum, trigger: "blur" }
        ]
      },
      meterList: [],
      tagList: [],
      isEdit: false,
      activeColor: '#10CC8F',
      inactiveColor: '#C6CAD3',
    }
  },
  created() {
    this.getTagList()
    this.getMeter()
    this.getList(this.page, this.searchForm)
  },
  mounted: function () {},
  computed: {},
  methods: {
    switchChangeRemoteStatus(row){
      // 0: 本地， 1: 远程
      let str = ''
      if (row.remoteStatus === 1) {
        str = '确认切换到远程？'
      } else {
        str = '确认切换到本地？'
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
            status: row.remoteStatus,
            valveId: row.id
          }
          return changeRemoteStatus(obj)
        })
        .then((dataResponse) => {
          if(dataResponse.data.code === 0){
            _this.getList(_this.page, _this.searchForm)
            _this.$notify({
              title: '切换成功',
              message: '切换成功',
              type: 'success',
              duration: 2000
            })
          }
        })
        .catch(() => {
          _this.getList(this.page, this.searchForm)
        })
    },
    switchChange(row){
      let str = ''
      if (row.isOpen === 1) {
        str = '确认打开该水表阀门？'
      } else {
        str = '确认关闭该水表阀门？'
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
            status: row.isOpen,
            valveId: row.id
          }
          return putValve(obj)
        })
        .then((dataResponse) => {
          if(dataResponse.data.code === 0){
            _this.getList(_this.page, _this.searchForm)
            _this.$notify({
              title: '修改成功',
              message: '修改成功',
              type: 'success',
              duration: 2000
            })
          }
        })
        .catch(() => {
          _this.getList(this.page, this.searchForm)
        })
    },
    /**
     * 操作记录
     */
    logsList(item) {
      const src = `/platform/device/valveManage/log/${item.id}`
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm,
          name: item.name
        }
      })
    },
    async save() {
      await this.$refs['addForm'].validate()
      if (this.addForm.id) {
        this.addLoading = true
        delete this.addForm.$index
        delete this.addForm.status
        updateValve(this.addForm).then((dataResponse) => {
          this.dialogVisible = false
          if (dataResponse.data.code === 0) {
            this.$notify({
              title: '修改成功',
              message: '修改成功',
              type: 'success',
              duration: 2000
            })
          }
          this.getList(this.page, this.searchForm)
        })
        this.addLoading = false
      } else {
        this.addLoading = true
        addValve(this.addForm).then((dataResponse) => {
          this.dialogVisible = false
          if (dataResponse.data.code === 0) {
            this.$notify({
              title: '添加成功',
              message: '添加成功',
              type: 'success',
              duration: 2000
            })
          }
          this.getList(this.page, this.searchForm)
        })
        this.addLoading = false
      }
    },
    addData() {
      this.dialogVisible = true
    },
    closeDialog() {
      this.dialogVisible = false
      this.isEdit = false
      this.addForm = {
        name: null,
        concentratorId: null,
        seq: null,
        tagIds: [],
        remark: null
      }
    },
    handleDetail(row) {
      this.dialogVisible = true
      this.isEdit = true
      this.addForm =  {...row}
    },
    getList(page, params) {
      this.tableLoading = true
      const obj = {
        current: page.currentPage,
        size: page.pageSize
      }
      // debugger
      valveList(Object.assign(obj, params)).then((response) => {
        this.tableData = response.data.data.records
        this.tableData.forEach((el) => {
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
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields()
        this.page.currentPage = 1
        this.getList(this.page)
      }
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
    }
  }
}

const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 220,
  labelWidth: 100,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  searchSize: 'mini',
  dialogWidth: '600px',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [
    {
      label: '设备名称',
      prop: 'name'
    },
    {
      label: '集中器名称',
      prop: 'concentratorName'
    },
    {
      label: '标签',
      prop: 'tagName'
    },
    {
      label: '备注',
      prop: 'remark'
    },
    {
      label: '本地/远程状态',
      prop: 'remoteStatus',
      solt: true
    },
    {
      label: '开关状态',
      prop: 'isOpen',
      solt: true
    }
  ]
}
</script>

<style lang="scss" scoped>
.num-bar {
  height: 21px;
  border-radius: 10px;
  width: 70%;
  // margin-bottom: 13px;
  // margin-top: 7px;
  float: left;
  overflow: hidden;
  background: #eaeef4;
  i {
    display: inline-block;
    float: left;
    height: 100%;
    border-radius: 10px;
  }
}
.m-bar {
  background: #10cc8f;
}
.f-bar {
  background: #ff698c;
}
.other-bar {
  background: #c1bdbd;
}
</style>
