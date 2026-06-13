<template>
  <section class="person-section">
    <div class="person-head">
      <div class="section-heading">
        <p class="box-orange">添加人员名单</p>
        <span>添加后默认选中本次人员，可直接设置权限并提交。</span>
      </div>
      <div class="person-actions">
        <el-dropdown trigger="click" @command="handleAddCommand">
          <el-button type="primary" icon="el-icon-plus">
            添加人员<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="paste">粘贴工号</el-dropdown-item>
            <el-dropdown-item command="dept">根据部门筛选</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <template v-if="tableData && tableData.length>0">
          <el-button type="primary" icon="el-icon-edit" @click="batchAddAuth" plain>弹窗设置权限</el-button>
        </template>
      </div>
    </div>
    <div v-if="errorArr && !isFirst" class="tip">
      <el-tag type="danger">
        {{errorArr}}
      </el-tag>
    </div>

    <div v-if="tableData && tableData.length>0" class="person-summary">
      <div class="summary-item">
        <span>名单人数</span>
        <strong>{{ personStats.totalCount }}</strong>
      </div>
      <div class="summary-item">
        <span>已选人员</span>
        <strong>{{ personStats.selectedCount }}</strong>
      </div>
      <div class="summary-item success">
        <span>可提交</span>
        <strong>{{ personStats.readyCount }}</strong>
      </div>
      <div class="summary-item" :class="{ danger: personStats.noAuthCount > 0 }">
        <span>未设权限</span>
        <strong>{{ personStats.noAuthCount }}</strong>
      </div>
      <div class="summary-item" :class="{ danger: isNoneSecurityNum > 0 }">
        <span>未签协议</span>
        <strong>{{ isNoneSecurityNum }}</strong>
      </div>
      <div class="summary-item" :class="{ danger: isNoneImgNum > 0 }">
        <span>未采照片</span>
        <strong>{{ isNoneImgNum }}</strong>
      </div>
    </div>

    <div v-if="tableData && tableData.length>0" class="auth-assist-panel">
      <div class="auth-assist-main">
        <label>本次申请权限</label>
        <el-select
          v-model="inlineAuthIds"
          multiple
          collapse-tags
          filterable
          placeholder="选择权限后，一键应用到人员"
          :disabled="applyingAuth || authList.length === 0"
        >
          <el-option
            v-for="item in authList"
            :key="item.authId"
            :label="item.authName"
            :value="item.authId"
          ></el-option>
        </el-select>
      </div>
      <div class="auth-assist-actions">
        <el-button
          type="primary"
          @click="applyInlineAuth('selected')"
          :loading="applyingAuth"
          :disabled="applyingAuth || !canApplySelected"
          plain
        >应用到选中人员</el-button>
        <el-button
          type="primary"
          @click="applyInlineAuth('all')"
          :loading="applyingAuth"
          :disabled="applyingAuth || !canApplyAll"
        >应用到全部人员</el-button>
      </div>
    </div>

    <el-collapse v-if="sourceTableData && sourceTableData.length>0" v-model="filterPanel" class="filter-collapse">
      <el-collapse-item name="filters">
        <template slot="title">
          <span class="filter-title">筛选名单</span>
          <span class="filter-desc">按协议、照片、权限或入职时间缩小当前名单</span>
        </template>
        <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini" class="form2">
          <el-form-item label="签署保密协议" prop="isSecurity">
            <el-select v-model="searchForm.isSecurity" placeholder="请选择" clearable>
              <el-option label="是" :value="1"></el-option>
              <el-option label="否" :value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="有无照片" prop="isImg">
            <el-select v-model="searchForm.isImg" placeholder="请选择" clearable>
              <el-option label="有" :value="1"></el-option>
              <el-option label="无" :value="0"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="权限" prop="authIds">
            <authPersonMultiSelect
              :querObj="queryObj"
              :newAreaType="newAreaType"
              :oldAreaType="oldAreaType"
              v-model="searchForm.authIds"
            ></authPersonMultiSelect>
          </el-form-item>
          <el-form-item label="入职时间" prop="times">
            <el-date-picker
              v-model="times"
              type="datetimerange"
              range-separator="-"
              value-format="yyyy-MM-dd"
              start-placeholder="起始时间"
              end-placeholder="截止时间"
              clearable
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="" >
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">查询</el-button>
            <el-button type="primary" plain @click="resetSearch">重置</el-button>
            <el-button type="primary" @click="handelExport" icon="icon-yutong-download" plain :disabled="!this.tableData || this.tableData.length===0">导出</el-button>
          </el-form-item>
        </el-form>
      </el-collapse-item>
    </el-collapse>

    <div v-if="(!sourceTableData || sourceTableData.length===0) && isFirst" class="empty-person-state">
      <div class="empty-title">还没有添加人员</div>
      <div class="empty-copy">先选择上方园区和可进区域，再通过粘贴工号或部门筛选加入人员。</div>
      <div class="empty-actions">
        <el-button type="primary" icon="el-icon-plus" @click="byPaste">粘贴工号</el-button>
        <el-button type="primary" plain @click="byDept">根据部门筛选</el-button>
      </div>
    </div>
    <div v-if="sourceTableData && sourceTableData.length>0 && (!tableData || tableData.length===0)" class="empty-person-state">
      <div class="empty-title">没有匹配人员</div>
      <div class="empty-copy">当前筛选条件下没有人员，请调整条件或重置筛选。</div>
      <div class="empty-actions">
        <el-button type="primary" plain @click="resetSearch">清空筛选</el-button>
      </div>
    </div>
    <div v-if="tableData && tableData.length>0" class="person-list-crud">
      <avue-crud
        ref="crud"
        :data="tableData"
        :option="listOption"
        @selection-change="selectChange"
      >
        <template slot-scope="scope" slot="isSecurity">
          <i class="el-icon-error tag-icon" v-if="scope.row.isSecurity===0"></i>
          <i class="el-icon-success tag-icon" v-if="scope.row.isSecurity===1"></i>
        </template>
        <template slot-scope="scope" slot="isImg">
          <i class="el-icon-error tag-icon" v-if="scope.row.isImg===0"></i>
          <i class="el-icon-success tag-icon" v-if="scope.row.isImg===1"></i>
        </template>
        <template slot-scope="scope" slot="authList">
          <span class="ft-blue">{{ scope.row.authList | flt_getAuthLabel }}</span>
        </template>
        <template slot-scope="scope" slot="remark">
          <span class="ft-danger">{{ scope.row.remark || '-' }}</span>
        </template>
        <template slot-scope="scope" slot="menu">
          <el-button
            type="text"
            icon="el-icon-edit"
            @click="handleEditAuth(scope.row,scope.$index)"
          >修改权限</el-button>
        </template>
      </avue-crud>
    </div>
    <DoPasteDialog :itemObj="curObj" ref="doPasteDialog" @refresh="getStaffsByBadge"/>
    <DoDeptDialog :itemObj="curObj" ref="doDeptDialog" @refresh="getStaffsById"/>
    <AuthSelectMulti
      :selectArr="authTargetStaffs"
      :showAuth="showAuth"
      :querObj="queryObj"
      :newAreaType="newAreaType"
      :oldAreaType="oldAreaType"
      ref="authSelectMulti"
      @refresh="getAuth"
      @update:visible="handleAuthDialogVisibleChange"
    />
  </section>
</template>
<script>
import authPersonMultiSelect from "./auth-person-multi-select"
import DoPasteDialog from '../../xc_project_mng/components/doPaste'
import DoDeptDialog from '../../xc_project_mng/components/doDept'
import AuthSelectMulti from './authSelectMulti'
import { xcGuardApplyApi } from '../_service'
import { xcProjectApi } from '../../xc_project_mng/_service'

export default {
  components: {
    authPersonMultiSelect,
    DoPasteDialog,
    DoDeptDialog,
    AuthSelectMulti
  },
  data() {
    return {
      searchForm: {
        authIds: []
      },
      filterPanel: [],
      inlineAuthIds: [],
      applyingAuth: false,
      restoringSelection: false,
      isFirst: true,
      times: [],
      tableData: [],
      sourceTableData: [], //初始的查询结果
      listOption: listOption(),
      curObj: {},
      staffIdArr: [],
      staffArr: [],
      authTargetStaffs: [],
      authRestoreStaffs: null,
      remarks: [], // 列表描述
      selectAuthes: [], //列表权限
      showAuth: [], //修改时回显的列表
      authList: []
    };
  },
  props: {
    queryObj: Object,
    newAreaType: Array,
    oldAreaType: Array
  },
  computed: {
    personStats(){
      const table = this.tableData || []
      const selected = this.staffArr || []
      const noAuthCount = table.filter(el => !el.authList || el.authList.length === 0).length
      const errorRemarkCount = table.filter(el => !!el.remark).length
      const selectedNoAuthCount = selected.filter(el => !el.authList || el.authList.length === 0).length
      const selectedErrorRemarkCount = selected.filter(el => !!el.remark).length
      return {
        totalCount: this.sourceTableData ? this.sourceTableData.length : 0,
        visibleCount: table.length,
        selectedCount: selected.length,
        readyCount: selected.filter(el => el.authList && el.authList.length > 0 && !el.remark).length,
        noAuthCount,
        errorRemarkCount,
        selectedNoAuthCount,
        selectedErrorRemarkCount,
        blockedCount: selectedNoAuthCount + selectedErrorRemarkCount
      }
    },
    canApplySelected(){
      return this.inlineAuthIds.length > 0 && this.staffArr.length > 0 && this.authList.length > 0
    },
    canApplyAll(){
      return this.inlineAuthIds.length > 0 && this.sourceTableData.length > 0 && this.authList.length > 0
    },
    errorArr(){
      //没有查到的工号信息集合
      if(this.sourceTableData && this.sourceTableData.length>0){
        let arr = this.sourceTableData[0].errorBadge
        if(arr && arr.length>0){
          return arr.toString() + '。未查到对应工号信息，请检查工号是否正确'
        }else{
          return ''
        }
      }else{
        return '当前未查询到员工'
      }
    },
    isNoneSecurityNum(){
      if(this.tableData && this.tableData.length>0){
        let num = 0
        this.tableData.forEach(el=>{
          if(el.isSecurity===0){
            num = num +1
          }
        })
        return num
      }else{
        return 0
      }
    },
    isNoneImgNum(){
      if(this.tableData && this.tableData.length>0){
        let num = 0
        this.tableData.forEach(el=>{
          if(el.isImg===0){
            num = num +1
          }
        })
        return num
      }else{
        return 0
      }
    }
  },
  filters: {
    flt_getAuthLabel(arr){
      if( arr&& arr.length>0){
        let str = ''
        arr.forEach(el=>{
          str = str + el.authName + '/'
        })
        return str.substring(0,str.length-1)
      }else{
        return '-'
      }
    }
  },
  created() {},
  watch: {
    times(val){
      this.searchForm.startDate = undefined
      this.searchForm.endDate = undefined
      if(val && val.length>0){
        this.searchForm.startDate = val[0]
        this.searchForm.endDate = val[1]
      }
    },
    sourceTableData(val){
      if(!val || val.length===0){
        this.remarks = []
        this.selectAuthes = []
        this.inlineAuthIds = []
      }
    },
    personStats:{
      handler(val){
        this.$emit('summaryChange', Object.assign({}, val, {
          isNoneSecurityNum: this.isNoneSecurityNum,
          isNoneImgNum: this.isNoneImgNum
        }))
      },
      immediate: true,
      deep: true
    },
    queryObj:{
      handler(val){
        // val.areaId = val.permitFactoryType
      },
      immediate: true
    }
  },
  methods: {
    getAuthLabel(arr){
      if( arr&& arr.length>0){
        let str = ''
        arr.forEach(el=>{
          str = str + el.authName + '/'
        })
        return str.substring(0,str.length-1)
      }else{
        return '-'
      }
    },
    handleAddCommand(command){
      if(command === 'paste'){
        this.byPaste()
      }
      if(command === 'dept'){
        this.byDept()
      }
    },
    /**
     * 粘贴人员
     */
    byPaste(){
      const areaType = this.newAreaType.concat(this.oldAreaType)
      if(this.queryObj.permitFactoryType && areaType.length > 0 && this.queryObj.parkId){
        this.$refs.doPasteDialog && this.$refs.doPasteDialog.open()
      }else{
        this.$message.error('请选择园区和可进区域');
      }
    },
    /**
     * 根据部门筛选
     */
    byDept(){
      const areaType = this.newAreaType.concat(this.oldAreaType)
      if(this.queryObj.permitFactoryType && areaType.length > 0 && this.queryObj.parkId){
        this.$refs.doDeptDialog && this.$refs.doDeptDialog.open()
      }else{
        this.$message.error('请选择园区和可进区域');
      }
    },
    /**
     * 粘贴人员-查询列表
     */
    async getStaffsByBadge(staffs){
      this.isFirst = false
      const res = await xcProjectApi.searchStaff( { staffBadges: staffs } )
      if(res.data.code===0){
        this.tableData = res.data.data
        this.sourceTableData = JSON.parse(JSON.stringify(this.tableData))
        this.selectCurrentStaffs()
        await this.getAuthList()
      }else{
        this.tableData = []
        this.sourceTableData = []
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
        this.sourceTableData = JSON.parse(JSON.stringify(this.tableData))
        this.selectCurrentStaffs()
        await this.getAuthList()
      }else{
        this.tableData = []
        this.sourceTableData = []
      }
    },
    /**
     * 查询人员权限
     */
    async getAuthList() {
      const areaType = this.newAreaType.concat(this.oldAreaType)
      let str = ''
      if(areaType.length > 0){
        str = areaType.join(',')
      }
      let query = {
        parkId: this.queryObj.parkId,
        areaId: str
      }
      const res = await xcGuardApplyApi.getAuthList(query)
      if(res.data.code===0){
        if(res.data.data.length > 0){
          this.authList = res.data.data
          // this.getRemark()
        }else{
          this.$message.error('所选可进区域权限为空，请在业务设置中配置可进区域权限！');
        }
      }else{
        this.$message.error('网络错误！');
      }
    },
    selectCurrentStaffs(){
      const staffs = (this.tableData || []).filter(el => el && el.id)
      this.staffArr = staffs
      this.staffIdArr = staffs.map(el => el.id)
      this.$nextTick(() => {
        if(this.$refs.crud && staffs.length > 0){
          this.toggleSelection(staffs)
        }
      })
    },
    restoreStaffSelection(staffs){
      const staffIds = (staffs || []).filter(el => el && el.id).map(el => el.id)
      const nextStaffs = []
      staffIds.forEach(id => {
        const row = (this.tableData || []).find(el => el.id === id) || (this.sourceTableData || []).find(el => el.id === id)
        if(row){
          nextStaffs.push(row)
        }
      })
      this.staffArr = nextStaffs
      this.staffIdArr = nextStaffs.map(el => el.id)
      this.restoringSelection = true
      this.$nextTick(() => {
        const visibleStaffs = (this.tableData || []).filter(el => staffIds.includes(el.id))
        if(this.$refs.crud && visibleStaffs.length > 0){
          this.toggleSelection(visibleStaffs)
        }
        this.$nextTick(() => {
          this.restoringSelection = false
        })
      })
    },
    toggleSelection(val){
      if(this.$refs.crud){
        this.$refs.crud.toggleSelection(val)
      }
    },
    /**
     * 添加默认权限
     */
    async getRemark() {
      const areaType = this.newAreaType.concat(this.oldAreaType)
      let str = ''
      if(areaType.length > 0){
        str = areaType.join(',')
      }
      let authIds = []
      this.authList.forEach(element => {
        authIds.push(element.authId)
      });
      let arr = []
      this.sourceTableData.forEach(el=>{
        arr.push({
          areaId: str,
          authList: authIds,
          badge: el.badge,
          staffId: el.id
        })
      })
      const res = await xcGuardApplyApi.queryRemark(arr)
      if(res.data.code===0){
        let remarks = []
        if(res.data.data && res.data.data.length>0){
          remarks = res.data.data
        }
        let obj = {
          staffs: this.sourceTableData,
          remarks: remarks,
          authList: this.authList
        }
        this.getAuth(obj)
      }
    },
    /**
     * 批量添加权限
     */
    batchAddAuth(){
      this.showAuth = []
      const areaType = this.newAreaType.concat(this.oldAreaType)
      if(this.queryObj.permitFactoryType && areaType.length > 0 && this.queryObj.parkId){
        if(!this.staffIdArr||this.staffIdArr.length===0){
          this.$message.error('请选择人员信息');
          return
        }
        this.authTargetStaffs = this.staffArr.slice()
        this.authRestoreStaffs = this.staffArr.slice()
        this.queryObj.staffIds = this.staffIdArr
        this.$refs.authSelectMulti && this.$refs.authSelectMulti.open()
      }else{
        this.$message.error('请选择园区和可进区域');
      }
    },
    async applyInlineAuth(type){
      if(this.applyingAuth){
        return
      }
      if(!this.inlineAuthIds || this.inlineAuthIds.length===0){
        this.$message.error('请选择权限');
        return
      }
      const authIds = this.inlineAuthIds.slice()
      const selectedAuthList = this.getSelectedAuthList(authIds)
      const staffs = type === 'all' ? this.sourceTableData : this.staffArr
      if(!staffs || staffs.length===0){
        this.$message.error(type === 'all' ? '暂无可设置人员' : '请选择人员信息');
        return
      }
      const areaId = this.getAreaId()
      if(!areaId){
        this.$message.error('请选择园区和可进区域');
        return
      }
      this.applyingAuth = true
      let query = []
      staffs.forEach(el=>{
        query.push({
          areaId: areaId,
          authList: authIds,
          badge: el.badge,
          staffId: el.id
        })
      })
      try {
        const res = await xcGuardApplyApi.queryRemark(query)
        if(res.data.code===0){
          let remarks = []
          if(res.data.data && res.data.data.length>0){
            remarks = res.data.data
          }
          this.getAuth({
            staffs: staffs,
            remarks: remarks,
            authList: selectedAuthList,
            restoreStaffs: this.staffArr.slice()
          })
        }
      } finally {
        this.applyingAuth = false
      }
    },
    getAreaId(){
      const areaType = this.newAreaType.concat(this.oldAreaType)
      if(areaType.length > 0){
        return areaType.join(',')
      }
      return ''
    },
    getSelectedAuthList(authIds){
      let selectAuthes = []
      ;(authIds || this.inlineAuthIds).forEach(authId=>{
        for(let i = 0; i < this.authList.length; i ++){
          if(this.authList[i].authId === authId){
            selectAuthes.push(this.authList[i])
            break
          }
        }
      })
      return selectAuthes
    },
    /**
     * 初始化列表
     */
    initListAll(){
      this.tableData = []
      this.sourceTableData = []
      this.staffIdArr = []
      this.staffArr = []
      this.remarks = []
      this.selectAuthes = []
      this.inlineAuthIds = []
      this.authTargetStaffs = []
      this.authRestoreStaffs = null
      this.toggleSelection()
    },
    /**
     * 初始化列表的权限数据
     */
    initAuthList(){
      this.staffArr = []
      this.staffIdArr = []
      this.remarks = []
      this.inlineAuthIds = []
      this.authTargetStaffs = []
      this.authRestoreStaffs = null
      this.sourceTableData.forEach(el=>{
        el.authList = []
        el.remark = ''
      })
      this.tableData.forEach(el=>{
        el.authList = []
        el.remark = ''
      })
      this.toggleSelection()
    },
    /**
     * 对应列表权限和描述
     */
    getAuth(obj){
      //清空选择
      this.restoringSelection = true
      this.toggleSelection()

      this.selectAuthes = obj.authList
      this.remarks = obj.remarks
      let staffs = obj.staffs
      const hasRestoreStaffs = Object.prototype.hasOwnProperty.call(obj, 'restoreStaffs')
      const restoreStaffs = hasRestoreStaffs ? obj.restoreStaffs : (this.authRestoreStaffs !== null ? this.authRestoreStaffs : staffs)

      //对应权限
      staffs.forEach(el=>{
        for(let i = 0; i < this.tableData.length; i ++){
          if(this.tableData[i].id === el.id){
            this.$set(this.tableData[i],'authList',this.selectAuthes)
            this.$set(this.tableData[i], 'remark', '')
            break
          }
        }
        for(let i = 0; i < this.sourceTableData.length; i ++){
          if(this.sourceTableData[i].id === el.id){
            this.$set(this.sourceTableData[i],'authList',this.selectAuthes)
            this.$set(this.sourceTableData[i], 'remark', '')
            break
          }
        }
      })
      //对应备注
      this.remarks.forEach(el=>{
        for(let i = 0; i < this.tableData.length; i ++){
          if(this.tableData[i].id === el.staffId){
            this.$set(this.tableData[i], 'remark', el.remark)
            break
          }
        }
        for(let i = 0; i < this.sourceTableData.length; i ++){
          if(this.sourceTableData[i].id === el.staffId){
            this.$set(this.sourceTableData[i], 'remark', el.remark)
            break
          }
        }
      })
      this.restoreStaffSelection(restoreStaffs)
      this.authTargetStaffs = []
      this.authRestoreStaffs = null
    },
    /**
     * 修改权限
     */
    handleEditAuth(row){
      this.authTargetStaffs = [row]
      this.authRestoreStaffs = this.staffArr.slice()
      this.showAuth = []
      if(row.authList && row.authList.length>0){
        row.authList.forEach(el=>{
          this.showAuth.push(el.authId)
        })
      }
      this.$refs.authSelectMulti && this.$refs.authSelectMulti.open()
    },
    handleAuthDialogVisibleChange(visible){
      if(visible === false){
        this.authTargetStaffs = []
        this.authRestoreStaffs = null
      }
    },
    /**
     * 查询
     */
    async searchSubmit(){
      const selectedStaffs = this.staffArr
      let queryObj = Object.assign(
        {
          staffInfos: this.sourceTableData
        },
        this.searchForm
      )
      const res = await xcGuardApplyApi.getStaffAgain(queryObj)
      if(res.data.code===0 && res.data.data){
        this.tableData = res.data.data
      }else{
        this.tableData = []
      }
      this.restoreStaffSelection(selectedStaffs)
    },
    resetSearch(){
      const selectedStaffs = this.staffArr
      this.searchForm = {
        authIds: []
      }
      this.times = []
      this.tableData = JSON.parse(JSON.stringify(this.sourceTableData || []))
      this.restoreStaffSelection(selectedStaffs)
      this.$nextTick(() => {
        if(this.$refs.searchForm){
          this.$refs.searchForm.clearValidate()
        }
      })
    },
    getPersonList(){
      if(!this.staffIdArr||this.staffIdArr.length===0){
        this.$message.error('请选择人员信息');
        return false
      }else{
        return this.staffArr
      }
    },
    /**
     * 导出
     */
    handelExport() {
      require.ensure([], () => {
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "工号",
          "姓名",
          "部门",
          "入职日期",
          "是否已签署保密承诺书",
          "是否有照片",
          "权限",
          "描述"
        ];
        const filterVal = [
          "badge",
          "name",
          "depName",
          "createTime",
          "isSecurity",
          "isImg",
          "authList",
          "remark"
        ];
        let dataArr = JSON.parse(JSON.stringify(this.tableData))
        dataArr.forEach( el=> {
          el.isSecurity = el.isSecurity===1 ? "是" : "否"
          el.isImg = el.isImg===1 ? "有" : "无"
          el.authList = this.getAuthLabel(el.authList)
        })
        const data = this.formatJson(filterVal, dataArr);
        export_json_to_excel(tHeader, data, "员工信息");
      })
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    /**
     * 多选事件
     */
    selectChange(val) {
      if(this.restoringSelection){
        return
      }
      this.staffIdArr = []
      this.staffArr = []
      if (val.length > 0) {
        this.staffArr = val
        val.forEach(function(element) {
          this.staffIdArr.push(element.id)
        }, this)
      }
    }
  }
};
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
    menu: true,
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
      },
      {
        label: '部门',
        prop: 'depName'
      },
      {
        label: '入职时间',
        prop: 'createTime'
      },
      {
        label: '是否已签署保密承诺书',
        prop: 'isSecurity',
        solt: true,
        width: 160,
      },
      {
        label: '是否有照片',
        prop: 'isImg',
        width: 90,
        solt: true
      },
      {
        label: '权限',
        prop: 'authList',
        solt: true,
        overHidden: true
      },
      {
        label: '描述',
        prop: 'remark',
        solt: true,
        overHidden: true
      }
    ]
  }
}
</script>
<style lang="scss" scoped>
.person-section{
  margin-top: 10px;
}
.person-head{
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  margin-bottom: 16px;
}
.section-heading{
  display: flex;
  align-items: center;
  gap: 14px;
  .box-orange{
    margin-bottom: 0;
    white-space: nowrap;
  }
  span{
    color: #8a9099;
    font-size: 13px;
  }
}
.person-actions{
  display: flex;
  align-items: center;
  gap: 10px;
}
.person-summary{
  display: grid;
  grid-template-columns: repeat(6, minmax(96px, 1fr));
  gap: 1px;
  overflow: hidden;
  margin: 16px 0;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #ebeef5;
}
.summary-item{
  min-height: 58px;
  padding: 10px 12px;
  background: #fafbfc;
  color: #6b7280;
  span{
    display: block;
    margin-bottom: 5px;
    font-size: 12px;
  }
  strong{
    color: #303133;
    font-size: 22px;
    font-variant-numeric: tabular-nums;
    line-height: 1;
  }
  &.success strong{
    color: #16a34a;
  }
  &.danger{
    background: #fff7ed;
    span{
      color: #b45309;
    }
    strong{
      color: #ea580c;
    }
  }
}
.auth-assist-panel{
  display: grid;
  grid-template-columns: minmax(260px, 1fr) auto;
  align-items: end;
  gap: 14px;
  margin-bottom: 14px;
  padding: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fbfcfe;
}
.auth-assist-main{
  label{
    display: block;
    margin-bottom: 8px;
    color: #4b5563;
    font-weight: 600;
  }
  .el-select{
    width: 100%;
  }
}
.auth-assist-actions{
  display: flex;
  gap: 10px;
}
.filter-collapse{
  margin-bottom: 14px;
  border-top: 1px solid #ebeef5;
  border-bottom: 1px solid #ebeef5;
}
.filter-collapse ::v-deep {
  .el-collapse-item__header{
    height: 42px;
    line-height: 42px;
    background: #ffffff;
  }
  .el-collapse-item__content{
    padding-bottom: 12px;
  }
}
.filter-title{
  color: #303133;
  font-weight: 600;
}
.filter-desc{
  margin-left: 10px;
  color: #9aa0a8;
  font-size: 12px;
}
.empty-person-state{
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  min-height: 210px;
  margin-top: 10px;
  padding: 28px;
  border: 1px dashed #d8dee8;
  border-radius: 6px;
  background: #fbfcfe;
}
.empty-title{
  margin-bottom: 8px;
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}
.empty-copy{
  max-width: 520px;
  margin-bottom: 18px;
  color: #8a9099;
  line-height: 1.7;
}
.empty-actions{
  display: flex;
  gap: 12px;
}
.ipt1{
  width: 300px !important;
  margin-right: 30px;
}
.tag-icon{
  font-size: 26px;
}
.el-icon-error{
  color: $color-danger;
}
.el-icon-success{
  color: $color-success;
}
.person-list-crud ::v-deep {
  .el-card__body{
    padding: 0 !important;
  }
}
.row1{
  display: flex;
  .el-tag{
    margin-right: 10px;
  }
}
.form2{
  padding: 4px 0 0;
}
.tip{
  margin-bottom: 20px;
  .el-tag{
    width: 100%;
    height: auto;
  }
}
.row2{
  display: flex;
  align-items: center;
  padding-left: 10px;
  margin-bottom: 10px;
  .ft-danger{
    padding-right: 5px;
    font-weight: bold;
  }
  >div{
    padding-right: 20px;
  }
}
@media (max-width: 1200px){
  .person-head,
  .auth-assist-panel{
    align-items: stretch;
    grid-template-columns: 1fr;
  }
  .person-head{
    flex-direction: column;
  }
  .person-summary{
    grid-template-columns: repeat(2, minmax(120px, 1fr));
  }
  .person-actions,
  .auth-assist-actions,
  .empty-actions{
    flex-wrap: wrap;
  }
}
</style>
