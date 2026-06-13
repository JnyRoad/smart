<!--
- @name 人员管理
- @author yang.chuan <yang.chuan@bjtce.com>
- @date 2020-10-09
-->
<template>
  <div class="my-basic-container mycard2 personnel-manage">
    <el-scrollbar class="my-scrollbar" :native="false">
      <div class="box-outer box-left">
        <el-scrollbar class="my-lit-scrollbar" :native="false">
          <div style="margin:10px 0;">
            <el-input placeholder="请输入" clearable v-model="filterText" size="mini">
              <el-button slot="append" icon="el-icon-search" @click="filterTreeHandle(filterText)"></el-button>
            </el-input>
          </div>
          <el-tree
            class="my-menu-tree"
            :data="treeData"
            highlight-current
            :props="defaultProps"
            @node-click="handleNodeClick"
            :filter-node-method="filterNodeMethod"
            ref="roomtree"
            default-expand-all
          >
          </el-tree>
          <!-- <el-button-group style="margin: 20px 0;">
            <el-button type="primary" icon="plus" @click="departmentDialog.visible = true">添加</el-button>
          </el-button-group> -->
        </el-scrollbar>
      </div>
      <div class="my-basic-inner">
        <div class="box-outer">
          <div class="top-menu clear">
            离职人员
            <div class="top-right">
              <el-button type="primary" @click="reinstatementDialog.visible = true" icon>批量恢复在职</el-button>
              <el-button type="primary" :loading="exportLoading" @click="export2Excel" icon>导出员工信息</el-button>
              <el-button type="primary" icon="el-icon-search" @click="searchSubmit">搜索</el-button>
              <el-button type="primary" icon="el-icon-delete" @click="resetFrom" plain>清空</el-button>
            </div>
          </div>
          <!-- 搜索条件 -->
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="工号" prop="badge">
              <el-input v-model="searchForm.badge" placeholder="工号" clearable></el-input>
            </el-form-item>
            <el-form-item label="姓名" prop="name">
              <el-input v-model="searchForm.name" placeholder="姓名" clearable></el-input>
            </el-form-item>
          </el-form>
          <!-- 列表 -->
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
            <template slot-scope="scope" slot="status">{{ staffStatus[scope.row.status + ''] }} </template>
            <template slot-scope="scope" slot="faceImgUrl">
              <viewer v-if="scope.row.faceImgUrl" ><img class="el-image" style="width:80px;height:100px; object-fit: contain;" :src="scope.row.faceImgUrl" :onerror="errorImgPeaple()"/></viewer>
              <img v-else class="el-image" style="width:80px;height:100px; object-fit: contain;" :src="placeholderUrl" />
            </template>
            <template slot-scope="scope" slot="menu">
              <el-button type="text" icon="el-icon-view" size="mini" @click="handleEdit(scope.row, scope.index)">详情 </el-button>
              <el-button type="text" icon="el-icon-delete" size="mini" @click="handleDel(scope.row, scope.index)">删除 </el-button>
            </template>
          </avue-crud>
        </div>
      </div>
      <!-- Reinstatement -->
      <!-- 批量恢复在职  弹出框 -->
      <!-- reinstatementDialog -->
      <el-dialog title="批量恢复在职" @close="handleClose" :visible.sync="reinstatementDialog.visible" :close-on-click-modal="false">
        <el-scrollbar style="height:600px;">
          <div class="ft-danger">*请直接将员工工号粘贴到下面框里，每个使用空格隔开</div>
          <el-input style="margin:16px 0;" rows="6" type="textarea" class="staffs" v-model="reinstatementDialog.fromData.badges" placeholder="请输入" clearable></el-input>
          <el-button type="primary" @click="searchPerson" icon>查询员工信息</el-button>

          <div style="margin-top:32px">
            <div>
              <span style="margin-right:16px">当前表格已选择{{reinstatementDialog.checkNum}}项</span>
              <el-button type="text" @click="deletePersonList" size="mini">移除</el-button>
              <el-button type="text" @click="clearPersonList" size="mini">清空</el-button>
            </div>
            <avue-crud
              ref="crud"
              :data="reinstatementDialog.tableData"
              :option="reinstatementDialog.listOption"
              @selection-change="_selectChange"
            >
            </avue-crud>
          </div>
        </el-scrollbar>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" plain @click="handleClose">关 闭</el-button>
          <el-button type="primary" @click="personSave" :disabled="reinstatementDialog.checkNum==0" :loading="reinstatementDialog.saveLoading">恢复在职</el-button>
        </div>
      </el-dialog>
      <!-- 编辑人员信息 弹出框 -->
      <el-dialog title="查看详情" :visible.sync="personnelDialog.visible">
        <el-scrollbar style="height:400px;">
          <el-form ref="personnelForm" disabled :rules="personnelDialog.rules" :model="personnelDialog.fromData" label-width="120px" v-if="personnelDialog.visible">
            <!-- 基本信息 -->
            <div class="personnel-dialog-from-item">
              <p class="box-orange">基本信息</p>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="姓名" prop="name">
                    <el-input v-model="personnelDialog.fromData.name" placeholder="请输入姓名"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="性别" prop="sex">
                    <el-radio-group v-model="personnelDialog.fromData.sex">
                      <el-radio v-for="item in genderOption" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="personnelDialog.fromData.phone" maxlength="11" placeholder="请输入手机号"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份证号" prop="identity">
                    <el-input v-model="personnelDialog.fromData.identity" placeholder="请输入身份证号"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="相片" prop="face">
                    <img v-if="personnelDialog.fromData.face" class="el-image" style="width:138px;height:138px; object-fit: contain;" :src="'data:image/jpeg;base64,' +personnelDialog.fromData.face" :onerror="errorImgPeaple()"/>
                    <img v-else class="el-image" style="width:138px;height:138px; object-fit: contain;" :src="placeholderUrl" />
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
            <!-- 组织信息 -->
            <div class="personnel-dialog-from-item">
              <p class="box-orange">组织信息</p>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="员工工号" prop="jobNumber">
                    <el-input v-model="personnelDialog.fromData.jobNumber" placeholder="请输入员工工号"  v-if="personnelDialog.fromData.id" disabled></el-input>
                    <el-input
                      v-model="personnelDialog.fromData.jobNumber"
                      placeholder="请输入员工工号"
                      v-else
                    ></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="在职状态" prop="jobStatus">
                    <el-select v-model="personnelDialog.fromData.jobStatus" placeholder="请选择在职状态">
                      <el-option v-for="item in staffStatusOption" :key="item.value" :value="item.value" :label="item.label"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="职层" prop="rank">
                    <el-select v-model="personnelDialog.fromData.rank" placeholder="请选择职层">
                      <el-option v-for="item in personnelDialog.recruitmentData" :key="item.typeCode" :label="item.typeName" :value="item.typeCode"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="部门" prop="department">
                    <el-select v-model="personnelDialog.fromData.department" placeholder="请选择部门">
                      <el-option v-for="item in deptList" :key="item.id" :label="item.deptName" :value="item.id"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="上级负责人" prop="superior">
                    <el-input v-model="personnelDialog.fromData.superior" readonly placeholder="暂无上级负责人"></el-input>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="岗位" prop="post">
                    <el-input v-model="personnelDialog.fromData.post" placeholder="请输入岗位"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="入职日期" prop="entryTime">
                    <el-date-picker
                      v-model="personnelDialog.fromData.entryTime"
                      type="date"
                      :picker-options="pickerOptions1"
                      value-format="yyyy-MM-dd HH:mm:ss "
                      placeholder="请选择入职日期">
                    </el-date-picker>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="派遣单位" prop="dispatch">
                    <el-input v-model="personnelDialog.fromData.dispatch" placeholder="请输入派遣单位"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-form>
        </el-scrollbar>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" plain @click="personnelDialog.visible = false">关 闭</el-button>
        </div>
      </el-dialog>
    </el-scrollbar>
  </div>
</template>

<script>
import { getAppauth, getRecruitment, getDeptTree, postDeptSave, getDeptList, getSearchStaff, delDept, getDeptDetails, getStaffPage, getStaff, postAddStaff, postDelStaff, getDirector, searchPersonList, reinstatementSave} from '@/api/platform/basic/personnel_manage'
import { tableOption } from '@/const/crud/platform/basic/personnel_manage_leave'
import { mapGetters } from 'vuex'
import { staffStatusOption, staffStatus, genderOption } from './enum'
import popoveTree from './popover-tree/index-single'
import { isMobile, cardid } from '@/util/validate'
import componentUpload from './_upload'
import componentImport from './import/index'
import logoSrc from './_img/holder_02.png'
export default {
  components: {
    popoveTree,
    componentUpload,
    componentImport
  },
  data() {
    /**
     * 校验手机号
     */
    var vMobile = (rule, value, callback) => {
      let r = isMobile(value)
      if (!r) {
        callback(new Error('手机格式不正确'))
      } else {
        callback()
      }
    }
    /**
     * 校验工号
     */
    var vJobNumber = (rule, value, callback) => {
      let codeReg = new RegExp("[A-Za-z0-9]+") //正则 英文+数字；
      let len = value.length,
      str='';
      for(var i=0;i<len;i++){
        if(!codeReg.test(value[i])){
          str+=value[i];
        }
      }
      if(str.length>0){
        callback(new Error('工号只能输入字母和数字'))
      }else{
        if(len>10){
          callback(new Error('工号最多为10位'))
        }else{
          callback()
        }
      }
    }

    /**
     * 校验身份证号
     */
    var vCardId = (rule, value, callback) => {
      let codeReg = new RegExp("[\u4E00-\u9FA5]+") //正则 不能输入汉字  ；
      let len = value.length,
      str='';
      for(var i=0;i<len;i++){
        if(codeReg.test(value[i])){
          str+=value[i];
        }
      }
      if(str.length>0){
        callback(new Error('证件号码不能包含汉字'))
      }else{
        if(len>7 && len<20){
          callback()
        }else{
          callback(new Error('证件号码需在8~20位之间'))
        }
      }
    }

    // var vCardId = (rule, value, callback) => {
    //   /(^\d{17}(\d|X|x)$)/.test(code)
    //   let r = cardid(value)
    //   if (r[0]) {
    //     callback(new Error(r[1]))
    //   } else {
    //     callback()
    //   }
    // }
    return {
      exportLoading: false,
      addPersonLoading: false,
      placeholderUrl: logoSrc,
      treeData: [],
      deptList: [],
      deptSaveData: {},
      genderOption: genderOption,
      staffStatusOption: staffStatusOption,
      staffStatus: staffStatus,
      reinstatementDialog: {
        visible: false,
        badgeArry: [],
        saveLoading: false,
        checkNum: 0,
        checkArr: [],
        tableData: [],
        listOption: listOption(),
        fromData: {
          badges: ''
        }
      },
      personnelDialog: {
        appauthData: [],
        recruitmentData: [],
        visible: false,
        fromData: {
          id: '',
          accessAuthority: '',// 通行权限
         // appAuthority: '',// app权限
          name: '',// 姓名
          sex: '',// 性别
          phone: '',// 手机号
          identity: '',// 身份证
          face: '',// 人脸
          jobNumber: '',// 工号
          jobStatus: '',// 在职状态
          bU: '', // 企业/BU
          rank: '',// 职层
          department: '',// 部门
          superior: '',// 上级负责人
          post: ''// 岗位
        },
        rules: {
          name: [
            { required: true, message: '姓名不能为空', trigger: 'blur' }
          ],
          sex: [
            { required: true, message: '性别不能为空', trigger: 'blur' }
          ],
          phone: [
            { required: true, message: '手机号不能为空', trigger: 'blur' },
            { validator: vMobile, trigger: 'blur' }
          ],
          identity: [
            { required: true, message: '身份证号不能为空', trigger: 'blur' },
            { validator: vCardId, trigger: 'blur' }
          ],
          //face: [
          //  { required: true, message: '相片不能为空', trigger: 'blur' }
          //],
          jobNumber: [
            { required: true, message: '工号不能为空', trigger: 'blur' },
            { validator: vJobNumber, trigger: 'blur' }
          ],
          jobStatus: [
            { required: true, message: '在职状态不能为空', trigger: 'change' }
          ],
          rank: [
            { required: true, message: '职层不能为空', trigger: 'change' }
          ],
          department: [
            { required: true, message: '部门不能为空', trigger: 'change' }
          ],
          // superior: [
          //   { required: true, message: '上传负责人不能为空', trigger: 'blur' }
          // ],
          post: [
            { required: true, message: '岗位不能为空', trigger: 'blur' }
          ]
        }
      },
      departmentDialog: {
        directorLoading: false,
        directorDataList: [],
        fromData: {
          id: '',
          parentId: '',// 上级
          name: '',// 部门名称
          director: ''// 主管
        },
        rules: {
          name: [
            { required: true, message: '请输入部门名称', trigger: 'blur' }
          ]
        },
        visible: false
      },
      importDialog: {
        visible: false
      },
      selectStaffs: [],
      hasSelect: false,
      filterText: '',
      defaultProps: {
        children: 'children',
        label: 'label'
      },
      searchForm: {//搜索菜单表单
        depId: '',
        badge: '',
        name: '',
        status: 0 //已离职
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    }
  },
  mounted: function () {
    this.deptInit()
    this.getList()
  },
  computed: {
    ...mapGetters(['permissions']),
  },
  watch: {
    selectStaffs: function(val) {
      val.length > 0 ? (this.hasSelect = true) : (this.hasSelect = false);
    },
    'personnelDialog.fromData.department'(value) {
      if (value) {
        this.getDirector(value).then(res => {
          const resData = res.data.data.split('-')
          if(!this.validatenull(resData[1])){
            this.personnelDialog.fromData.superior = resData[1]
          }else{
            this.personnelDialog.fromData.superior = null
          }
        })
      }
    },
    //'personnelDialog.fromData.face'(value) {
    //  this.$refs['personnelForm'] && this.$refs['personnelForm'].validateField(['face'])
    //},
    'personnelDialog.visible'(value) {
      if (value) {
        this.getFromInit()
      }
      if (!value) {
        this.personnelDialog.fromData = {
          id: '',
          accessAuthority: '',// 通行权限
          //appAuthority: '',// app权限
          name: '',// 姓名
          sex: '',// 性别
          phone: '',// 手机号
          identity: '',// 身份证
          face: '',// 人脸
          jobNumber: '',// 工号
          jobStatus: '',// 在职状态
          bU: '', // 企业/BU
          rank: '',// 职层
          department: '',// 部门
          superior: '',// 上级负责人
          post: ''// 岗位
        }
      }
    }
  },
  methods: {
    searchPerson(){
      if(this.reinstatementDialog.fromData.badges !== ''){
        const arr = this.reinstatementDialog.fromData.badges.split(/\s+/)
        if(arr.length > 0){
          const str = arr.join(',')
          // searchPersonList
          searchPersonList({badges: str,status: 0})
              .then((response) => {
                if (response.data.data) {
                  this.reinstatementDialog.tableData = response.data.data.records
                }
              })
              .catch(err => { console.error(err) })
        }
      }else{
        this.$message.error('请直接将员工工号粘贴到下面框里，每个使用空格隔开');
      }
    },
    clearPersonList(){ //清空
      this.reinstatementDialog.tableData = []
    },
    deletePersonList(){ //移除
      // console.log(this.reinstatementDialog.tableData,this.reinstatementDialog.checkArr)
      for(let i in this.reinstatementDialog.checkArr){
        this.reinstatementDialog.tableData.splice(this.reinstatementDialog.checkArr[i].$index,1)
      }
    },
    _selectChange(val) {
      this.reinstatementDialog.checkArr = val
      this.reinstatementDialog.checkNum = val.length
    },
    personSave(){
      const arr = []
      this.reinstatementDialog.checkArr.forEach(element => {
        arr.push(element.id)
      });
      reinstatementSave({ids: arr})
            .then((response) => {
              this.$message({
                showClose: true,
                message: '恢复在职成功',
                type: 'success'
              })
              this.handleClose()
              this.getList()
            })
            .catch(err => { console.error(err) })
      // const ids =
      // reinstatementSave
    },
    handleClose(){
      this.reinstatementDialog.tableData = []
      this.reinstatementDialog.fromData.badges = ''
      this.reinstatementDialog.visible = false
    },
     //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "工号",
          "姓名",
          "证件号",
          "部门",
          "职层",
          "岗位",
          "入职日期",
          "员工状态"
        ];
        const filterVal = [
          "badge",
          "name",
          "certno",
          "depName",
          "jcheName",
          "jobName",
          "entryTime",
          "status"
        ];
        let params =  {
          current: 1,
          size: 10000
        }
        var _this = this;
        getStaffPage(params, this.searchForm)
          .then(response => {
            const list = response.data.data.records;
            list.forEach(function(item) {
              item.status = item.status==1?'在职':'离职'
            });
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, "离职人员信息");
            this.exportLoading = false;
          })
          .catch(err => {
            this.exportLoading = false;
          });
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    accountInput(val){//账号的实时输入
      // len=val.length,
      // str='';
      // for(var i=0;i<len;i++){
      // if(codeReg.test(val[i])){
      //   str+=val[i];
      // }
      // }
      // this.accountVal=str;
    },
    getDirector(id) {
      return getDirector({ id })
    },
    selectChange(val) {
      //序号那边选择事件
      this.selectStaffs = val;
      var idArr = [];
      if (val.length > 0) {
        val.forEach(function(element) {
          idArr.push(element.id);
        }, this);
      }
      this.entryForm.ids = idArr;
      this.appForm.staffId = idArr;
    },
    /**
     * 远程搜索主管列表
     */
    async remoteMethod(query) {
      if (query) {
        this.departmentDialog.directorLoading = true
        const res = await getSearchStaff({
          badge: query
        })
        this.departmentDialog.directorDataList = res.data.data
        this.departmentDialog.directorLoading = false
      } else {
        this.departmentDialog.directorDataList = []
        this.departmentDialog.directorLoading = false
      }
    },
    deptInit() {
      return Promise.all([this.getDeptTree(), this.getDeptList()])
    },
    async getDeptList() {
      const res = await getDeptList().then(res => {
        if (!res.data.data) {
          return []
        }
        return res.data.data
      })
      this.deptList = res
    },
    async getDeptTree() {
      const res = await getDeptTree()
      this.treeData = res.data.data
    },
    async postDeptSave() {
      let name = ''
      if(this.departmentDialog.fromData.director !== null && this.departmentDialog.fromData.director.length > 0){
        const checkItem = this.departmentDialog.directorDataList.find(item => {
          return item.id === this.departmentDialog.fromData.director
        })
        name = checkItem.name
      }

      const res = await postDeptSave({
        compId: '',
        deptName: this.departmentDialog.fromData.name,
        director: this.departmentDialog.fromData.director,
        directorName: name,
        id: this.departmentDialog.fromData.id,
        parentDept: this.departmentDialog.fromData.parentId
      })
      if (res.data.code === 0) {
        this.$message({
          showClose: true,
          message: '保存成功',
          type: 'success'
        })
        this.departmentDialog.visible = false
        this.getDeptTree()
      }
    },
    async getFromInit() {
      const res = await Promise.all([getAppauth(), getRecruitment(), this.getDeptList()])
      this.personnelDialog.appauthData = res[0].data.data.records
      this.personnelDialog.recruitmentData = res[1].data.data
    },
    /**
     * 保存部门
     */
    async depSave() {
      await this.$refs.departmentForm.validate()
      this.postDeptSave()
    },
    /**
     * tree节点操作
     */
    async treeNodeOption(data, opt, node) {
      switch (opt) {
        case 'EDI':
          const detailsRes = await getDeptDetails({ id: data.value })
          this.departmentDialog.fromData = {
            id: detailsRes.data.data.id,
            parentId: detailsRes.data.data.parentDept || "",// 上级
            name: detailsRes.data.data.deptName,// 部门名称
            directorBadge: detailsRes.data.data.directorBadge,
            directorName: detailsRes.data.data.directorName,
            director: detailsRes.data.data.director
          }
          await this.remoteMethod(detailsRes.data.data.directorBadge)
          this.departmentDialog.visible = true
          break
        case 'APP':
          this.departmentDialog.fromData = {
            id: '',
            parentId: data.value,// 上级
            name: '',// 部门名称
            directorBadge: '',
            directorName: '',
            director: ''// 主管
          }
          if (data === -1) {
            this.departmentDialog.fromData = {
              id: '',
              parentId: '',// 上级
              name: '',// 部门名称
              directorBadge: '',
              directorName: '',
              director: ''// 主管
            }
          }
          this.departmentDialog.visible = true
          break
        case 'DEL':
          await this.$confirm('是否确认移除此节点', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          const res = await delDept({
            id: data.value
          })
          if (res.data.code === 0) {
            this.$message({
              showClose: true,
              message: '删除成功',
              type: 'success'
            })
          }
          break
      }
      this.deptInit()
    },
    /**
     * 节点点击事件
     */
    handleNodeClick(data, node) {
      if (node.parent.parent) {
        this.searchForm.depId = data.value
      } else {
        this.searchForm.depId = ''
      }
      this.getList()
    },
    /**
     * 搜索tree
     */
    filterTreeHandle(value) {
      this.$refs.roomtree.filter(value)
    },
    /**
     * 筛选tree
     */
    filterNodeMethod(value, data, node) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    async handleEdit(row, index) {
      const res = await getStaff({ staffId: row.id })
      this.personnelDialog.fromData = {
        id: res.data.data.id,
        accessAuthority: '',
        //appAuthority: res.data.data.appAuth || [],
        name: res.data.data.name,
        sex: res.data.data.sex,
        phone: res.data.data.phone || '',
        identity: res.data.data.certno,
        face: res.data.data.faceImg,
        jobNumber: res.data.data.badge,
        jobStatus: res.data.data.status,
        bU: '',
        rank: res.data.data.jcheId,
        department: res.data.data.depId,
        superior: res.data.data.reportToName,
        post: res.data.data.jobName
      }
      this.personnelDialog.visible = true
    },
    async handleDel(row, index) {
      await this.$confirm('是否确认删除', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const res = await postDelStaff({
        staffId: row.id
      })
      if (res.data.code === 0) {
        this.$message({
          showClose: true,
          message: '删除成功',
          type: 'success'
        })
        this.getList()
      }
    },
    async personnelSave() {
      await this.$refs['personnelForm'].validate()
      this.addPersonLoading = true
      const checkItem = this.deptList.find(item => {
        return item.id === this.personnelDialog.fromData.department
      })
      try {
        const res = await postAddStaff({
          //appAuth: this.personnelDialog.fromData.appAuthority,
          badge: this.personnelDialog.fromData.jobNumber,
          certno: this.personnelDialog.fromData.identity,
          depId: this.personnelDialog.fromData.department,
          depName: checkItem.deptName,
          faceImg: this.personnelDialog.fromData.face,
          id: this.personnelDialog.fromData.id,
          jcheId: this.personnelDialog.fromData.rank,
          jcheName: '',
          jobName: this.personnelDialog.fromData.post,
          name: this.personnelDialog.fromData.name,
          phone: this.personnelDialog.fromData.phone,
          sex: this.personnelDialog.fromData.sex,
          status: this.personnelDialog.fromData.jobStatus
        })
        if (res.data.code === 0) {
          this.$message({
            showClose: true,
            message: '保存成功',
            type: 'success'
          })
          this.personnelDialog.visible = false
          this.getList()
        }
        this.addPersonLoading = false
      } catch (error) {
        this.addPersonLoading = false
      }
    },
    /**
     * 获取list
     */
    async getList() {
      this.tableLoading = true
      const response = await getStaffPage({
        current: this.page.currentPage,
        size: this.page.pageSize
      }, this.searchForm)
      this.tableData = response.data.data.records
      this.page.total = response.data.data.total
      this.tableLoading = false
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList();
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList();
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.page.currentPage = 1;
      this.getList()
    },
    /**
     * 清空搜索
     */
    resetFrom() {
      this.$refs['searchForm'].resetFields();
      this.page.currentPage = 1;
      this.getList();
    }
  }
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
.mycard2 .box-left{
  width: 330px;
}
.mycard2 .my-scrollbar{
  padding: 0 0 0 335px;
}
// 导入dialog
.import-dialog-inner {
  padding: 10px 0;
  > p {
    font-size: 14px;
    line-height: 24px;
  }
  .down {
    color: #ed6d00;
    margin: 20px 0;
    margin-left: 2em;
    display: inline-block;
  }
  .upload {
    color: #ed6d00;
    margin: 20px;
    margin-left: 2em;
    display: inline-block;
  }
  .tt-desc {
    color: #999;
  }
}
.personnel-dialog-from-item {
  padding: 0 20px;
  ::v-deep {
    .el-form-item {
      margin-bottom: 20px;
    }
  }
}
.box-orange {
  padding: 0;
  padding-bottom: 15px;
  border-bottom: 1px solid #ddd;
  margin: 0;
  margin-bottom: 15px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  i[class^='el-icon-'] {
    margin: 0 2px;
  }
  span:nth-of-type(1) {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    word-break: break-all;
    width: 150px;
  }
  span:nth-of-type(2) {
    display: none;
  }
  &:hover {
    span:nth-of-type(2) {
      display: block;
    }
  }
  .reinstatement-cont{
    textarea{
      min-height: 150px !important;
    }
  }
}
</style>
