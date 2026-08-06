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
            <span class="custom-tree-node" slot-scope="{ node, data }">
              <span>{{ node.label }}</span>
              <span v-if="node.parent.parent">
                <i class="el-icon-edit" @click.stop="treeNodeOption(data, 'EDI', node)"></i>
                <i class="el-icon-circle-plus-outline" @click.stop="treeNodeOption(data, 'APP', node)"></i>
                <i class="el-icon-delete" @click.stop="treeNodeOption(data, 'DEL', node)"></i>
              </span>
              <span v-else>
                <i class="el-icon-circle-plus-outline" @click.stop="treeNodeOption(-1, 'APP')"></i>
              </span>
            </span>
          </el-tree>
          <!-- <el-button-group style="margin: 20px 0;">
            <el-button type="primary" icon="plus" @click="departmentDialog.visible = true">添加</el-button>
          </el-button-group> -->
        </el-scrollbar>
      </div>
      <div class="my-basic-inner">
        <div class="box-outer">
          <div class="top-menu clear">
            在职人员
            <div class="top-right">
              <el-button type="primary" icon="el-icon-search" @click="searchSubmit">搜索</el-button>
              <el-button type="primary" icon="el-icon-delete" @click="resetFrom" plain>清空</el-button>
              <el-button type="primary" icon="el-icon-plus" @click="addPerson">新增员工</el-button>
              <el-button type="primary" icon="el-icon-upload2" @click="importDialog.visible = true">导入员工</el-button>
              <el-button
                type="primary"
                :loading="selectLoading"
                class="importBtn"
                icon="el-icon-upload2"
                @click="importImg"
              >导入员工照片</el-button>
              <input class="fileBtn" ref="uploadBtn" type="file" multiple @change="myChange($event)" />
              <el-button type="primary" :loading="exportLoading" @click="export2Excel" icon>导出员工</el-button>
              <el-button type="primary" v-if="compId" @click="deleteVisible = true">批量离职</el-button>
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
            <el-form-item label="人脸照片" prop="isFace">
              <el-select v-model="searchForm.isFace" placeholder="是否上传人脸照片">
                <el-option :value="true" label="已上传"></el-option>
                <el-option :value="false" label="未上传"></el-option>
              </el-select>
            </el-form-item>
            <!-- <el-form-item label="状态" prop="status">
              <el-select v-model="searchForm.status" placeholder="在职状态">
                <el-option v-for="item in staffStatusOption" :key="item.value" :value="item.value" :label="item.label"></el-option>
              </el-select>
            </el-form-item> -->
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
          >
            <template slot-scope="scope" slot="status">{{ staffStatus[scope.row.status + ''] }} </template>
            <template slot-scope="scope" slot="faceImgUrl">
              <viewer v-if="scope.row.faceImgUrl" ><img class="el-image" style="width:80px;height:100px; object-fit: contain;" :src="scope.row.faceImgUrl" :onerror="errorImgPeaple()"/></viewer>
              <img v-else class="el-image" style="width:80px;height:100px; object-fit: contain;" :src="placeholderUrl" />
            </template>
            <template slot-scope="scope" slot="menu">
              <el-button type="text" icon="el-icon-edit" size="mini" @click="handleEdit(scope.row, scope.index)">编辑 </el-button>
              <!--<el-button type="text" icon="el-icon-delete" size="mini" @click="handleDel(scope.row, scope.index)">删除 </el-button>-->
            </template>
          </avue-crud>
        </div>
      </div>

      <!--导入人员 弹出框 -->
      <el-dialog title="导入人员" class="dialog_form" width="1200px" :visible.sync="importDialog.visible">
        <div class="import-dialog-inner">
          <componentImport v-if="importDialog.visible" @complete="importComplete"></componentImport>
          <!-- <p class="tt">1、下载导入模板，填写人员</p>
          <a class="down" href="" target="__blank">下载模板</a>
          <p class="tt">2、上传填好的人员</p>
          <el-upload class="upload-demo" accept=".xls,.xlsx" action="https://jsonplaceholder.typicode.com/posts/">
            <span class="upload">点击上传</span>
            <span slot="tip" class="el-upload__tip">仅支持xls，xlsx</span>
          </el-upload>
          <p class="tt-desc">导入会覆盖原有员工的信息，如需更新已存在的员工，请先导出员工，在导出表格里进行修改</p> -->
        </div>
      </el-dialog>

      <!-- 编辑人员信息 弹出框 -->
      <el-dialog title="编辑人员" :visible.sync="personnelDialog.visible">
        <el-scrollbar style="height:400px;">
          <el-form ref="personnelForm" :rules="personnelDialog.rules" :model="personnelDialog.fromData" label-width="120px" v-if="personnelDialog.visible">
            <!-- 数据及权限 -->
<!--            <div class="personnel-dialog-from-item">
              <p class="box-orange">数据及权限</p>
              <el-row>
                <el-col :span="12"> </el-col>
                <el-col :span="12">
                  <el-form-item label="APP权限" prop="accessAuthority">
                    <el-select multiple v-model="personnelDialog.fromData.appAuthority" placeholder="请选择APP权限" :disabled="true">
                      <el-option v-for="item in personnelDialog.appauthData" :key="item.id" :label="item.authName" :value="item.id"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>-->

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
                    <componentUpload v-if="personnelDialog.fromData.face" v-model="personnelDialog.fromData.face" :previewImage="'data:image/jpeg;base64,'+personnelDialog.fromData.face"></componentUpload>
                    <componentUpload v-else v-model="personnelDialog.fromData.face" ></componentUpload>
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
          <el-button type="primary" plain @click="personnelDialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="personnelSave" :loading="addPersonLoading">保 存</el-button>
        </div>
      </el-dialog>

      <!-- 编辑部门 弹出框 -->
      <el-dialog title="编辑部门" :visible.sync="departmentDialog.visible">
        <el-scrollbar style="height:400px;">
          <el-form ref="departmentForm" :rules="departmentDialog.rules" :model="departmentDialog.fromData" label-width="120px" v-if="departmentDialog.visible">
            <div class="personnel-dialog-from-item">
              <el-row>
                <el-col :span="12">
                  <el-form-item label="上级部门" prop="parentId">
                    <el-select v-model="departmentDialog.fromData.parentId" placeholder="请选择上级部门">
                      <el-option label="顶级部门" value=""></el-option>
                      <el-option v-for="item in deptList" :key="item.id" :label="item.deptName" :value="item.id"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="部门名称" prop="name">
                    <el-input v-model="departmentDialog.fromData.name" placeholder="请输入部门名称"></el-input>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row>
                <el-col :span="12">
                  <el-form-item label="部门主管" prop="director">
                    <el-select
                      v-model="departmentDialog.fromData.director"
                      filterable
                      remote
                      placeholder="请输入工号查询"
                      :remote-method="remoteMethod"
                      :loading="departmentDialog.directorLoading"
                    >
                      <el-option v-for="item in departmentDialog.directorDataList" :key="item.id" :label="item.name" :value="item.id"> </el-option>
                    </el-select> </el-form-item
                ></el-col>
                <el-col :span="12">
                  <el-form-item label="关联c6部门" prop="c6DeptNo" v-if="deptType == 2">
                    <el-select v-model="departmentDialog.fromData.c6DeptNo" filterable clearable placeholder="请选择">
                      <el-option-group
                        v-for="group in c6DeptList"
                        :key="group.value"
                        :label="group.label">
                        <el-option
                          v-for="item in group.children"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value">
                        </el-option>
                      </el-option-group>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </div>
          </el-form>
        </el-scrollbar>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" plain @click="departmentDialog.visible = false">取 消</el-button>
          <el-button type="primary" @click="depSave">保 存</el-button>
        </div>
      </el-dialog>

      <el-dialog title="批量离职" width="700px" @close="delBatchClose" :visible.sync="deleteVisible" :close-on-click-modal="false">
        <el-scrollbar style="height:400px;">
          <el-input v-model.trim="delBadges" placeholder="请输入工号,每个工号使用空格隔开" type="textarea"/>
          <el-button type="primary" @click="selectBadge" style="margin-top: 10px">查询员工信息</el-button>
          <div class="tb-outer">
            <div  v-if="nonBadges && nonBadges.length>0">
              <span style="color: red; font-weight: bold;">{{ nonBadges.toString() }} </span>
              这些工号没有查到相关信息，请核实
            </div>
            <avue-crud
              ref="crud"
              :data="backPerons"
              :option="tableOptionBatchDel"
              @selection-change="selectChange"
            >
            </avue-crud>
          </div>
        </el-scrollbar>
        <div slot="footer" class="dialog-footer" style="display: flex;justify-content: space-between">
          <div>
            <el-button type="primary" plain @click="delSelect" :disabled="selectBatchArr&&selectBatchArr.length==0">批量移除</el-button>
          </div>
          <div>
            <el-button type="primary" plain @click="deleteVisible = false">取 消</el-button>
            <el-button type="primary" @click="delSave" :disabled="selectBatchArr&&selectBatchArr.length==0" :loading="delLoading">确认离职</el-button>
          </div>
        </div>
      </el-dialog>

      <!-- 批量导入图片 -->
      <el-dialog
        title="批量导入图片"
        @close="importCancel"
        class="dialog_form import_img"
        width="600px"
        :visible.sync="importVisible"
      >
        <div class="importCont">
          <p>操作提示：</p>
          <p>1.照片名称以工号为命名。</p>
          <p>2.请核对一下要导入的照片，避免重复覆盖。</p>
          <p>3.一次性导入数量，不超过2000张，避免造成系统压力。</p>
          <div>
            本次共选择
            <span class="num">{{totalImgNum}}</span>个员工照片，
            其中
            <span class="num red">{{noneStaffs.length}}</span>个照片的工号不存在
            <template v-if="noneStaffs.length>0">
              ，请检查以下工号是否正确！
              <span class="num red">
                （
                <template v-for="(item, index) in noneStaffs">
                  {{item.staffBadge}}
                  <template v-if="(index+1) !=noneStaffs.length">，</template>
                </template>
                ）
              </span>
            </template>
          </div>
          <el-scrollbar style="height:400px;">
            <table class="fileTb">
              <tr>
                <td>工号</td>
                <td>姓名</td>
                <td>是否存在</td>
                <td>是否有照片</td>
                <td>操作</td>
              </tr>
              <template v-for="(item, index) in listInfo">
                <tr :key="index" :class="{'haveNo':item.status==2}">
                  <td>{{ item.staffBadge }}</td>
                  <td>{{ item.staffName || '-' }}</td>
                  <td>{{ item.status==0?'是':'否' }}</td>
                  <td>{{ item.status==0?'是':'否' }}</td>
                  <td>
                    <el-button type="text" @click="delImg(item.staffBadge)">移除</el-button>
                  </td>
                </tr>
              </template>
              <template v-if="listInfo.length == 0">
                <tr>
                  <td colspan="5">还未选择图片文件</td>
                </tr>
              </template>
            </table>
          </el-scrollbar>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="importCancel" plain>取 消</el-button>
          <el-button
            type="primary"
            @click="myUpload()"
            :loading="importLoading"
            :disabled="listInfo.length == 0"
          >确 定</el-button>
        </div>
      </el-dialog>
    </el-scrollbar>
  </div>
</template>

<script>
import { getTemporaryStaffByBadgeBatch, delStaffBatch, getAppauth, getRecruitment, getDeptTree, postDeptSave, getDeptList, getSearchStaff, delDept, getDeptDetails, getStaffPage, getStaff, postAddStaff, postDelStaff, getDirector, getC6DeptList} from '@/api/platform/basic/personnel_manage'
import { getStaffImgInfo, importImgs } from "@/api/platform/basic/staff_info";
import { tableOption, tableOptionBatchDel } from '@/const/crud/platform/basic/personnel_manage'
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
      if(!this.editPerson){ //添加工号的时候，验证，编辑时不验证，编辑时是不可更改的状态
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
          if(len>5 && len <13){
            callback()
          }else{
            callback(new Error('工号长度在6~12位之间'))
          }
        }
      }else{
        callback()
      }
    }

    /**
     * 校验身份证号
     */
    var vCardId = (rule, value, callback) => {
      if(value.includes(' ')){
        callback(new Error('证件号码不能包含空格'))
      }
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
    return {
      exportLoading: false,
      totalImgNum: 0, //本次共选择了几项
      noneStaffs: [], //不存在的工号集合
      fileNames: [], //名称集合
      listInfo: [], //根据名称集合查询的图片信息
      uploadInfo: [], //要上传的文件列表
      importVisible: false,
      importLoading: false,
      selectLoading: false,

      editPerson: false, //编辑人员true
      compId: '',
      deleteVisible: false, //批量离职
      delBadges: '', //输入框内的工号字符串
      delBadgesArr: [], //输入框内的工号数组
      backPerons: [], //返回的员工信息集合
      backBadges: [], //返回结果内的工号数组
      nonBadges: [], //没有查到信息的工号数组
      selectBatchArr: [], //批量离职接口的工号数组
      delLoading: false,

      tableOptionBatchDel: tableOptionBatchDel,
      addPersonLoading: false,
      placeholderUrl: logoSrc,
      treeData: [],
      deptList: [],
      deptSaveData: {},
      genderOption: genderOption,
      staffStatusOption: staffStatusOption,
      staffStatus: staffStatus,
      pickerOptions1: {
        disabledDate(time) {
          return time.getTime() > Date.now();
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
          entryTime: '', //入职日期
          dispatch: '', //派遣单位
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
          entryTime: [
            { required: true, message: '入职时间不能为空', trigger: 'change' }
          ],
          // superior: [
          //   { required: true, message: '上传负责人不能为空', trigger: 'blur' }
          // ],
          post: [
            { required: true, message: '岗位不能为空', trigger: 'blur' }
          ]
        }
      },
      deptType: null,
      departmentDialog: {
        directorLoading: false,
        directorDataList: [],
        fromData: {
          id: '',
          parentId: '',// 上级
          name: '',// 部门名称
          director: '',// 主管
          c6DeptNo: ''
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
        isFace: '',
        status: 1 //在职
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
          entryTime: '', //入职日期
          dispatch: '', //派遣单位
          department: '',// 部门
          superior: '',// 上级负责人
          post: ''// 岗位
        }
      }
    }
  },
  methods: {
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
            export_json_to_excel(tHeader, data, "在职人员信息");
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
    importImg() {
      this.$refs.uploadBtn.click();
    },
    //选择图片
    myChange(e) {
      this.$message.closeAll();
      let files = e.target.files;
      let errorSuffix = []; //所选文件非指定规范格式(jpg)的图片 的集合 ,用来提示
      let errorSize = []; //所选文件非指定规范大小(60~200k)的图片 的集合 ,用来提示
      let arrName = []; //图片名称集合，用来查询是否存在图片信息
      let arrFile = []; //所选文件信息集合，上传使用
      this.totalImgNum = files.length;

      if (files.length > 0) {
        if (files.length < 2000) {
          //单次导入，数量不能超过200张
          this.selectLoading = true;
          for (var i = 0; i < files.length; i++) {
            var suffixPosition = files[i].name.lastIndexOf(".") + 1;
            var nums = files[i].name.length - suffixPosition;
            var suffix = files[i].name.substr(suffixPosition, nums); //文件后缀

            var namePosition = files[i].name.lastIndexOf(".");
            var badge = files[i].name.substr(0, namePosition); //文件名字

            //找出格式不规范的文件集合
            const isImg =
              suffix == "jpg" ||
              suffix == "JPG" ||
              suffix == "jpeg" ||
              suffix == "JPEG";
            if (!isImg) {
              //判断所选文件格式
              errorSuffix.push(badge);
            }

            //找出大小不规范的文件集合
            const fileSize = files[i].size / 1024;
            if (fileSize > 200) {
              errorSize.push(badge);
            }

            arrName.push({ staffBadge: badge });
          }
          if (errorSuffix.length > 0) {
            this.selectLoading = false;
            let str =
              '<div class="tips" style="max-width: 500px; word-break:break-all;line-height:20px;""><p>共选择了 ' +
              this.totalImgNum +
              " 个文件，有 " +
              errorSuffix.length +
              " 个文件格式不符合规范</p>";
            str +=
              '<p>照片仅支持 jpg 一种格式，请检查以下文件格式再重新导入！</p><p style="color:red">';

            for (var i = 0; i < errorSuffix.length; i++) {
              str += errorSuffix[i] + "，";
            }

            var dotPosition = str.lastIndexOf("，");
            str = str.substr(0, dotPosition);
            str += "</p></div>";

            this.$message({
              dangerouslyUseHTMLString: true,
              showClose: true,
              message: str,
              type: "warning",
              duration: 0
            });
          } else if (errorSize.length > 0) {
            this.selectLoading = false;
            let str =
              '<div class="tips" style="max-width: 500px; word-break:break-all;line-height:20px;""><p>共选择了 ' +
              this.totalImgNum +
              " 个文件，有 " +
              errorSize.length +
              " 个文件大小不符合规范</p>";
            str +=
              '<p>照片大小应在小于200KB，请检查以下文件大小再重新导入！</p><p style="color:red">';

            for (var i = 0; i < errorSize.length; i++) {
              str += errorSize[i] + "，";
            }

            var dotPosition = str.lastIndexOf("，");
            str = str.substr(0, dotPosition);
            str += "</p></div>";

            this.$message({
              dangerouslyUseHTMLString: true,
              showClose: true,
              message: str,
              type: "warning",
              duration: 0
            });
          } else {
            getStaffImgInfo({ facePicUpLoad: arrName, compId:this.compId})
              .then(response => {
                if (!this.validatenull(response.data.data)) {
                  var result = response.data.data;
                  for (var i = 0; i < result.length; i++) {
                    if (result[i].status == 2) {
                      this.noneStaffs.push(result[i]);
                    } else {
                      this.listInfo.push(result[i]);
                    }
                  }

                  [].forEach.call(files, this.readAndPreview);

                  this.importVisible = true;
                  this.selectLoading = false;
                }
              })
              .catch(err => {
                this.selectLoading = false;
              });
          }
        } else {
          this.$message.warning(
            "为避免造成系统压力,一次性导入数量，不能超过2000张!"
          );
          return;
        }
      }
    },
    readAndPreview(file) {
      //将对象转为base64和文件名称的组合
      let _this = this;
      let noneStaffs = this.noneStaffs;
      var namePosition = file.name.lastIndexOf(".");
      var badge = file.name.substr(0, namePosition);

      if (noneStaffs.length > 0) {
        for (var i = 0; i < noneStaffs.length; i++) {
          if (noneStaffs[i].staffBadge == badge) {
            return;
          }
        }
      }

      var reader = new FileReader();
      reader.addEventListener(
        "load",
        function() {
          let originalImgCode = this.result; //含有 base64 头的
          let baseIndex = originalImgCode.indexOf(",") + 1;
          let baseImg = originalImgCode.slice(baseIndex);

          _this.uploadInfo.push({ staffBadge: badge, facePic: baseImg });
        },
        false
      );
      reader.readAsDataURL(file);
    },
    delImg(badge) {
      //根据工号，移除上传列表中的照片文件

      let listInfo = this.listInfo; //列表展示的集合
      let uploadInfo = this.uploadInfo; //上传的集合

      //移除的同时，列表展示的集合和上传的集合都要移除
      for (var i = 0; i < listInfo.length; i++) {
        if (listInfo[i].staffBadge == badge) {
          this.listInfo.splice(i, 1);
        }
      }

      //移除的同时，列表展示的集合和上传的集合都要移除
      for (var i = 0; i < uploadInfo.length; i++) {
        if (uploadInfo[i].staffBadge == badge) {
          this.uploadInfo.splice(i, 1);
        }
      }
    },
    importCancel() {
      this.importVisible = false;
      this.noneStaffs = []; //不存在的工号集合
      this.fileNames = []; //名称集合
      this.listInfo = []; //根据名称集合查询的图片信息
      this.uploadInfo = []; //要上传的文件列表
      this.$refs.uploadBtn.value = "";
    },
    myUpload() {
      if (this.uploadInfo.length > 0) {
        this.importLoading = true;

        importImgs({ facePicUpLoad: this.uploadInfo })
          .then(response => {
            this.importLoading = false;
            if (response.data.data) {
              this.importVisible = false;
              this.$notify({
                title: "完成",
                message: "成功导入" + response.data.data + "张照片！",
                type: "success"
              });
              this.getList()
            } else {
              this.$notify.error({
                title: "失败",
                message: "员工照片导入失败！"
              });
            }
          })
          .catch(err => {
            //不要在此处提示失败
            // this.$notify.error({
            //   title: '失败',
            //   message: '员工照片导入失败！'
            // });
            this.importLoading = false;
          });
      }
    },
    importComplete(){
      this.importDialog.visible = false
      this.getList()
    },
    async selectBadge(){
      if(!this.delBadges){
        this.$message.error('请输入工号')
        return
      }
      //以逗号或者空格分隔
      this.delBadgesArr = this.delBadges.split(/[\s|,]+/)
      const res = await getTemporaryStaffByBadgeBatch({
        badges: this.delBadgesArr.join(',')
      })
      if(!res) return
      if(res.data.code==0){
        let resData = res.data.data
        if(resData && resData.length>0){
          this.backPerons = resData

          //找出没有查到信息的工号
          this.backBadges = []
          this.backPerons.forEach(el=>{
            this.backBadges.push(el.badge)
          })

          this.nonBadges = this.delBadgesArr.filter(items => {
            if (!this.backBadges.includes(items)) return items;
          })



        }else{
          this.$message.error('没有查询到这些工号的信息，请核对工号是否输入正确')
        }
      }else{
        this.$message.error(res.data.msg)
      }
    },
    selectChange(val) {
      //序号那边选择事件
      this.selectBatchArr = val;
    },
    //批量移除
    delSelect(){
      this.selectBatchArr.forEach(el=>{
        this.backPerons.forEach((el2, index)=>{
          if(el.id === el2.id){
            this.backPerons.splice(index, 1)
          }
        })
      })
    },
    //批量删除，保存
    async delSave(){
      try{
        this.delLoading = true
        const res = await delStaffBatch(this.selectBatchArr)
        if(!res) return
        if(res.data.code==0){
          if(res.data.data){
            this.$notify({
              title: '成功',
              message: '批量离职成功',
              type: 'success'
            });
          }

          this.deleteVisible = false
          this.getList()
        }else{
          this.$notify.error({
            title: '批量离职失败',
            message: res.data.msg
          });
        }
        this.delLoading = false
      }catch(err){
        this.delLoading = false
      }
    },
    initDelBatchData(){
      this.delBadges = ''
      this.delBadgesArr = []
      this.backPerons = []
      this.backBadges = []
      this.nonBadges = []
      this.selectBatchArr = []
    },
    delBatchClose(){
      this.initDelBatchData()
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
      return Promise.all([this.getDeptTree(), this.getDeptList(), this.getC6DeptList()])
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
      if(this.treeData.length>0){
        this.compId = this.treeData[0].value
        this.deptType = this.treeData[0].type
        // console.log('11111',this.treeData[0])
      }
    },
    async getC6DeptList() {
      const res = await getC6DeptList().then(res => {
        if (!res.data.data) {
          return []
        }
        return res.data.data
      })
      this.c6DeptList = res
      this.c6DeptList.forEach(element => {
        if(element.children.length === 0){
          element.children.push({
            label: element.label,
            value: element.value
          })
        }
      });
      // console.log('c6DeptList',this.c6DeptList)
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
        parentDept: this.departmentDialog.fromData.parentId,
        c6DeptNo: this.departmentDialog.fromData.c6DeptNo
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
      // console.log(this.deptType)
      this.getDeptList()
      this.getC6DeptList()
      switch (opt) {
        case 'EDI':
          const detailsRes = await getDeptDetails({ id: data.value })
          this.departmentDialog.fromData = {
            id: detailsRes.data.data.id,
            parentId: detailsRes.data.data.parentDept || "",// 上级
            name: detailsRes.data.data.deptName,// 部门名称
            directorBadge: detailsRes.data.data.directorBadge,
            directorName: detailsRes.data.data.directorName,
            director: detailsRes.data.data.director,
            c6DeptNo: detailsRes.data.data.c6DeptNo
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
      // this.deptInit()
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
        post: res.data.data.jobName,
        entryTime: res.data.data.entryTime,
        dispatch: res.data.data.dispatch
      }
      this.editPerson = true
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
    addPerson(){
      this.personnelDialog.visible = true
      this.editPerson = false
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
          entryTime: this.personnelDialog.fromData.entryTime,
          dispatch: this.personnelDialog.fromData.dispatch,
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
</script>

<style lang="scss" scoped>
@media screen and (max-width: 1560px) {
  .theme-yutong .el-button--primary {
    font-size: 12px;
  }
}
.mycard2 .box-left{
  width: 420px;
}
.mycard2 .my-scrollbar{
  padding: 0 0 0 420px;
}
.noData{
  text-align: center;
  padding-top: 50px;
}
.person-info-holder{
  margin-top: 30px;
  height: 150px;
  border: 1px dashed #e0e0e0;
}
.fileBtn {
  display: none;
}
.importBtn {
  margin-right: 20px;
}
.import_img{
  .tips {
    max-width: 500px;
    border: 1px solid red;
  }
  .num {
    padding: 0 5px;
    font-weight: bold;
  }
  .red {
    color: red;
  }
  .importCont {
    line-height: 25px;
    padding-bottom: 30px;
  }
  .fileTb {
    margin: 10px 0 0 0;
    width: 100%;
    .haveNo {
      color: red;
    }
    td {
      border: 1px solid #e0e0e0;
      text-align: center;
      padding: 0 10px;
      height: 35px;
    }
  }
}
.person-info{
  margin-top: 30px;
  display: flex;
  height: 150px;
  .info-img{
    position: relative;
    width: 130px;
    height: 150px;
    overflow: hidden;
    background: #F0F2F5;
    border: 1px solid #e0e0e0;
    img{
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      max-width: 100%;
      height: auto;
      max-height: 100%;
      width: auto;
    }
  }
  .info_right{
    display: flex;
    flex-wrap: wrap;
    margin-left: 5px;
    .info_row{
      width: 50%;
      display: flex;
      border: 1px solid #e0e0e0;
      align-items: stretch;
      border-right: none;
      border-bottom: none;
      .info_label, .info_value{
        line-height: 35px;
      }
      .info_label{
        width: 80px;
        padding-left: 8px;
        background: #f6f7fb;
      }
      .info_value{
        flex: 1;
        padding-left: 10px;
      }
    }
    .info_row:nth-child(7){
      border-bottom: 1px solid #e0e0e0;
    }
    .info_row:last-child{
      border-bottom: 1px solid #e0e0e0;
    }
    .info_row:nth-child(2n){
      border-right: 1px solid #e0e0e0;
    }
  }
}
.tb-outer{
  margin-top: 30px;
  .tb-row{
    display: flex;
    justify-content: space-between;
    .tb-cell{
      flex: 1;
      height: 45px;
      line-height: 45px;
      text-align: center;
    }
    .cell1{
      width: 50px;
    }
    .cell-opr{
      width: 60px;
    }
  }
  .tb-row:nth-child(2n+1){
    background: #f2f2f2;
  }
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
}
</style>
