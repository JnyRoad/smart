<template>
  <el-dialog
    ref="dialog"
    :title="title"
    :visible.sync="currVisible"
    width="700px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <div class="cont">
      <div>
        <el-button type="primary" @click="byPaste">粘贴人员</el-button>
        <el-button type="primary" @click="byDept">根据部门筛选</el-button>
        <el-button type="primary" @click="handelDelBatch" plain>批量移除</el-button>
      </div>
      <div v-if="errorArr && !isFirst" class="tip">
        <el-tag type="danger">
          {{errorArr}}
        </el-tag>
      </div>
      <avue-crud
        ref="crud"
        :data="tableData"
        :table-loading="tableLoading"
        :option="listOption"
        @selection-change="selectChange"
      >
      </avue-crud>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading" :disabled="!tableData||tableData.length===0">保 存</el-button>
    </div>
    <DoPasteDialog ref="doPasteDialog" @refresh="getStaffsByBadge"/>
    <DoDeptDialog ref="doDeptDialog" @refresh="getStaffsById"/>
  </el-dialog>
</template>

<script>
import { xcProjectApi } from '../_service'
import DoPasteDialog from './doPaste'
import DoDeptDialog from './doDept'

export default {
  mixins: [tce.mixins.executeOnce],
  components: {
    DoPasteDialog,
    DoDeptDialog
  },
  data() {
    return {
      isFirst: true,
      btnLoading: false,
      currVisible: false,
      tableData: [],
      listOption: listOption(),
      tableLoading: false,
      authIds: [],
      authList: [],
      curAuth: {},
      staffIdArr: [],
    }
  },
  props: {
    visible: Boolean,
    title: String,
    securityId: [String, Number],
    parkId: [String, Number]
  },
  created() {
  },
  computed: {
    errorArr(){
      //没有查到的工号信息集合
      if(this.tableData && this.tableData.length>0){
        let arr = this.tableData[0].errorBadge
        if(arr && arr.length>0){
          return arr.toString() + '。未查到对应工号信息，请检查工号是否正确'
        }else{
          return ''
        }
      }else{
        return '未查询到员工'
      }
    }
  },
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      }
    }
  },
  methods: {
    /**
     * 粘贴人员
     */
    byPaste(){
      this.$refs.doPasteDialog && this.$refs.doPasteDialog.open()
    },
    /**
     * 粘贴人员-查询列表
     */
    async getStaffsByBadge(staffs){
      this.isFirst = false
      const res = await xcProjectApi.searchStaff( { staffBadges: staffs } )
      if(res.data.code===0){
        this.tableData = res.data.data
      }
    },
    /**
     * 根据部门筛选-查询列表
     */
    async getStaffsById(staffs){
      this.isFirst = false
      const res = await xcProjectApi.searchStaff( { staffIds: staffs } )
      if(res.data.code===0){
        this.tableData = res.data.data
      }
    },
    /**
     * 根据部门筛选
     */
    byDept(){
      this.$refs.doDeptDialog && this.$refs.doDeptDialog.open()
    },
    /**
     * 批量删除
     */
    async handelDelBatch(row) {
      if(this.staffIdArr.length===0){
        this.$message.error('请选择要删除的员工信息')
        return
      }
      this.staffIdArr.forEach((el, index)=>{
        let objIndex = this.tableData.indexOf(el)
        if(objIndex>-1){
          this.tableData.splice(objIndex, 1)
        }
      })
    },
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
     * 提交
     */
    async formSumit() {
      this.addSubmit()
    },
    /**
     * 添加
     */
    async addSubmit(){
      let arr = []
      this.tableData.forEach(el=>{
        arr.push(
          {
            securityId: [this.securityId],
            staffBadge: el.badge,
            staffId: el.id,
            staffName: el.name,
            parkId: this.parkId
          }
        )
      })
      await this.executeOnceSubmit({
        promise: xcProjectApi.addPersonToProject(arr)
      })
      this.refresh()
    },
    /**
     * 多选事件
     */
    selectChange(val) {
      this.staffIdArr = val
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.tableData = []
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.tableData = []
      this.currVisible = false
    }
  },
  mounted() {}
}
const listOption = function () {
  return {
    index: true,
    indexLabel: '序号',
    addBtn: false,
    delBtn: false,
    editBtn: false,
    viewBtn: false,
    border: false,
    refreshBtn: false,
    columnBtn: false,
    stripe: false,
    page: true,
    align: 'center',
    menuAlign: 'center',
    menu: false,
    menuWidth: 120,
    selection: true,
    tip: false,
    column: [
      {
        label: '工号',
        prop: 'badge'
      },
      {
        label: '姓名',
        prop: 'name'
      }
    ]
  }
}
</script>

<style lang="scss" scoped>
  .form{
    margin-bottom: 40px;
  }
  .cont{
    padding-bottom: 50px;
    .tip{
      padding-top: 20px;
      .el-tag{
        width: 100%;
        height: auto;
      }
    }
  }
</style>
