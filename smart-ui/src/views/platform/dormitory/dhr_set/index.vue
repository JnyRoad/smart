<!--入住申请  -->
<template>
  <div class="my-basic-container">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini">
            <el-form-item label="园区">
              <parkSelect v-model="searchForm.parkId" @getItem="getAddPark"></parkSelect>
            </el-form-item>
          </el-form>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-plus" @click="addPark">新增</el-button>
          </div>
        </div>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" @click="addPark(scope.row)">编辑</el-button>
            <el-button type="text" @click="deductionSet(scope.row)">水电扣费金额</el-button>
            <el-button type="text" @click="setBus(scope.row)">BU使用范围</el-button>
            <el-button type="text" @click="bindRoom(scope.row)">使用宿舍</el-button>
            <el-button type="text" @click="settlementRule(scope.row, scope.$index)">结算规则</el-button>
            <el-button type="text" icon="el-icon-delete" @click="handleDel(scope.row, scope.$index)"></el-button>
          </template>
        </avue-crud>
        <el-dialog :title="addTitle" class="dialog_form" @close="addCancel('addform')" width="500px" :visible.sync="addFormVisible" :close-on-click-modal="false">
          <el-form :rules="rules" ref="addform" :model="addform" label-width="100px">
            <el-form-item label="模板名称" prop="templateName">
              <el-input v-model="addform.templateName" placeholder="请输入"></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addCancel('addform')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addform')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
        <!-- 选择宿舍 -->
        <BindRoom ref="bindRooms" :parkId="searchForm.parkId" :tempId="tempId"/>
        <!-- BU -->
        <SetBu ref="setBu" :parkId="searchForm.parkId" :tempId="tempId"/>
        <!-- 结算规则 -->
        <SettlementRule ref="settlementRule" :parkId="searchForm.parkId" :tempId="tempId"/>
        <!-- 水电扣费金额 -->
        <DeductionSet ref="deductionSet" :parkId="searchForm.parkId" :tempId="tempId"/>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList,addData,allPark,deleteData } from './_service.js'
import { tableOption } from '@/const/crud/platform/dormitory/dhr_set'
import BindRoom from './components/bind_room'
import SetBu from './components/setBu'
import SettlementRule from './components/settlement_rule'
import DeductionSet from './components/deduction_set'

export default {
  name: 'type',
  components: {
    BindRoom,
    SetBu,
    SettlementRule,
    DeductionSet
  },
  data() {
    return {
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      parkData:[],
      searchForm: {
        parkId: null
      },
      tempId:null,
      addFormVisible: false,
      addTitle: '新增模板',
      addLoading: false,
      addform: {
        templateName: null,
        id: null
      },
      rules: {
        templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }]
      }
    }
  },
  async created() {
    const res = await allPark();
    this.parkData = res.data.data;
    this.searchForm.parkId = this.parkData[0].id
    this.getList(this.page, this.searchForm.parkId)
  },
  filters: {},
  mounted: function () {},
  computed: {},
  methods: {
    addPark(row = {}){
      if(this.searchForm.parkId){
        if(row.id){
          this.addTitle = '编辑模板'
          this.addform = JSON.parse(JSON.stringify(row))
        }
        this.addFormVisible = true
      }else{
        this.$notify({
          title: "提示",
          message: '请选择园区',
          type: "error",
          duration: 2000
        });
      }
      // this.addFormVisible = true
    },
    setBus(row){
      this.tempId = row.id
      this.$refs.setBu.open()
    },
    bindRoom(row) {
      this.tempId = row.id
      this.$refs.bindRooms.open()
    },
    settlementRule(row){
      this.tempId = row.id
      this.$refs.settlementRule.open()
    },
    deductionSet(row){
      this.tempId = row.id
      this.$refs.deductionSet.open()
    },
    async handleDel(row, index) {
      await this.$confirm('是否确认删除', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const res = await deleteData(row.id)
      if (res.data.code === 0) {
        this.$message({
          showClose: true,
          message: '删除成功',
          type: 'success'
        })
        this.getList(this.page, this.searchForm.parkId)
      }
    },
    addSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.addLoading = true;
          const obj = {
            templateName: this.addform.templateName,
            parkId: this.searchForm.parkId
          }
          if(this.addform.id){
            obj['id'] = this.addform.id
          }
          addData(obj)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.getList(this.page, this.searchForm.parkId);
                this.addFormVisible = false;
                this.$notify({
                  title: "成功",
                  message: "成功",
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
    handleDetail() {},
    getAddPark(val){
      this.searchForm.parkId = val.value
      this.getList(this.page, val.value)
    },
    getList(page, parkId) {
      this.tableLoading = true
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          }
        ),
        parkId
      ).then((response) => {
        this.tableData = response.data.data.records
        this.page.total = response.data.data.total
        this.tableLoading = false
      })
      this.tableLoading = false
    },
    addCancel(formName) {
      this.addFormVisible = false
      this.resetAddFrom(formName)
    },
    resetAddFrom(formName) {
      this.addform.templateName = null
      this.addform.id = null
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page, this.searchForm.parkId)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.searchForm.parkId)
    }
  }
}
</script>
