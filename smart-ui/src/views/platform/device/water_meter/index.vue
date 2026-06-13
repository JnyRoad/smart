<!--设备管理，门禁管理  -->
<template>
  <div class="my-basic-container device guard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-plus" @click="addData()">新增集中器</el-button>
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm', searchForm)" plain>清空</el-button>
          </div>
        </div>
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="集中器名称" prop="name">
              <el-input v-model="searchForm.name" placeholder="请输入名称" clearable></el-input>
            </el-form-item>
            <el-form-item label="通信地址" prop="address">
              <el-input v-model="searchForm.address" placeholder="请输入通信地址"></el-input>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select v-model="searchForm.status" placeholder="请选择接通状态" clearable>
                <el-option label="未连接" value="0"></el-option>
                <el-option label="离线" value="1"></el-option>
                <el-option label="在线" value="2"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="handleDetail(scope.row)">编辑</el-button>
            <el-button type="text" @click="viewDevice(scope.row)">查看设备</el-button>
            <el-button type="text" icon="el-icon-delete" @click="deleteData(scope.row)">删除</el-button>
          </template>
        </avue-crud>
      </section>
      <!-- 新增集中器 弹出框 -->
      <el-dialog :title="dialogFromData.id ? '编辑集中器' : '新增集中器'" @close="closeDialog" :visible.sync="dialogVisible" :close-on-click-modal="false">
        <el-scrollbar style="height: 400px">
          <el-form ref="form" :rules="rules" :model="dialogFromData" label-width="120px" v-if="dialogVisible">
            <el-form-item required label="所属园区" prop="parkId">
              <parkSelect v-model="dialogFromData.parkId"></parkSelect>
            </el-form-item>
            <el-form-item required label="IP" prop="ip">
              <el-input v-model="dialogFromData.ip" placeholder="请输入IP"></el-input>
            </el-form-item>
            <el-form-item required label="端口" prop="port">
              <el-input v-model="dialogFromData.port" placeholder="请输入端口"></el-input>
            </el-form-item>
            <el-form-item required label="名称" prop="name">
              <el-input v-model="dialogFromData.name" placeholder="请输入名称"></el-input>
            </el-form-item>
            <el-form-item required label="通信地址" prop="address">
              <el-input v-model="dialogFromData.address" placeholder="请输入通信地址"></el-input>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="dialogFromData.remark" placeholder="请输入备注"></el-input>
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
import { concentratorList, addConcentrator, updateConcentrator, delConcentrator} from './_service'

export default {
  mixins: [tce.mixins.list],
  name: 'alarm',
  components: {},
  data() {
    return {
      tableOption: tableOption,
      page: {
        total: null, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      searchForm: {
        name: '',
        status: '',
        address: ''
      },
      tableData: [],
      tableLoading: false,
      addLoading: false,
      dialogFromData: {
        // id: '',
        parkId: null,
        ip: null,
        port: null,
        address: null,
        name: null,
        remark: null
      },
      dialogVisible: false,
      rules: {
        parkId: [{ required: true, message: '请选择所在园区', trigger: 'change' }],
        IP: [{ required: true, message: '请输入IP', trigger: 'blur' }],
        port: [{ required: true, message: '请输入端口', trigger: 'blur' }],
        address: [{ required: true, message: '请输入通信地址', trigger: 'blur' }],
        name: [{ required: true, message: '请输入名称', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList(this.page, this.searchForm)
  },
  mounted: function () {},
  computed: {},
  methods: {
    async save() {
      await this.$refs['form'].validate()
      if (this.dialogFromData.id) {
        this.addLoading = true
        delete this.dialogFromData.$index
        delete this.dialogFromData.status
        updateConcentrator(this.dialogFromData).then((dataResponse) => {
          // console.log('11111111', dataResponse)
          this.dialogVisible = false
          this.$notify({
            title: '修改成功',
            message: '修改成功',
            type: 'success',
            duration: 2000
          })
          this.getList(this.page, this.searchForm)
        })
        this.addLoading = false
      } else {
        this.addLoading = true
        addConcentrator(this.dialogFromData).then((dataResponse) => {
          this.dialogVisible = false
          this.$notify({
            title: '添加成功',
            message: '添加成功',
            type: 'success',
            duration: 2000
          })
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
      this.dialogFromData = {
        parkId: null,
        ip: null,
        port: null,
        name: null,
        address: null,
        remark: null
      }
    },
    handleDetail(row) {
      this.dialogVisible = true
      this.dialogFromData = row
    },
    deleteData(row) {
      var _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除该集中器？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          return delConcentrator(row.id)
        })
        .then((dataResponse) => {
          _this.getList(_this.page)
          _this.$notify({
            title: '删除成功',
            message: '删除成功',
            type: 'success',
            duration: 2000
          })
        })
        .catch(error => { console.error(error) })
    },
    getList(page, params) {
      this.tableLoading = true
      const obj = {
        current: page.currentPage,
        size: page.pageSize
      }
      concentratorList(Object.assign(obj, params)).then((response) => {
        this.tableData = response.data.data.records
        this.page.total = response.data.data.total
        this.tableLoading = false
      })
    },
    viewDevice(item){
      const src = `/platform/device/waterManage`
      this.$router.push({
        path: src,
        query: {
          concenId: item.id
        }
      })
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
  menuWidth: 240,
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
      label: '园区名称',
      prop: 'parkName'
    },
    {
      label: '集中器名称',
      prop: 'name'
    },
    {
      label: 'IP',
      prop: 'ip'
    },
    // {
    //   label: '端口',
    //   prop: 'port'
    // },
     {
      label: '通信地址',
      prop: 'address'
    },
    {
      label: '状态',
      prop: 'status'
    },
    {
      label: '备注',
      prop: 'remark'
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
