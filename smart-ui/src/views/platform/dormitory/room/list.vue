<!--宿舍管理，房间管理，列表  -->
<template>
  <div class="my-basic-container mycard2 room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <div class="box-outer box-left">
        <el-scrollbar class="my-lit-scrollbar" :native="false">
          <div class="cont-left">
            <span class="tiplbl">选择楼栋及楼层</span>
            <div class="lft-block">
              <!-- :current-node-key="defaultKey" -->
              <!-- :default-checked-keys="[defaultKey]" -->
              <!-- :default-expanded-keys="[26]" -->
              <div style="margin: 10px 0">
                <el-input placeholder="输入关键字进行过滤" clearable v-model="filterText" size="mini">
                  <el-button type="info" slot="append" icon="el-icon-search"></el-button>
                </el-input>
              </div>
              <el-tree
                class="my-menu-tree"
                :data="treeData"
                highlight-current
                node-key="id"
                :props="defaultProps"
                accordion
                :filter-node-method="filterNode"
                @node-click="handleNodeClick"
                ref="roomtree"
              >
                <span class="custom-tree-node" slot-scope="{ node, data }">
                  <span class="tree-label">{{ node.label }}{{ node.level == 3 ? 'F' : '' }} </span>
                  <span class="tree-btn" v-if="node.parent.parent">
                    <!-- <i class="el-icon-edit" title="编辑" @click.stop="treeNodeOption(data, 'EDI', node)"></i>
                  <template v-if="node.level==1||node.level==2">
                    <i class="el-icon-circle-plus-outline" @click.stop="treeNodeOption(data, 'APP', node)"></i>
                  </template>
                  <i class="el-icon-delete" @click.stop="treeNodeOption(data, 'DEL', node)"></i> -->
                    <i class="btni" @click.stop="treeNodeOption(data, 'EDI', node)">编辑</i>
                    <i class="btni" @click.stop="treeNodeOption(data, 'DEL', node)">删除</i>
                    <template v-if="node.level == 1">
                      <i class="btni" @click.stop="treeNodeOption(data, 'APP', node)">新增楼栋</i>
                    </template>
                    <template v-if="node.level == 2">
                      <i class="btni" @click.stop="treeNodeOption(data, 'APP', node)">新增楼层</i>
                    </template>
                    <!-- <el-dropdown placement="bottom" class="dropdown">
                    <span class="el-dropdown-link">
                      <i class="el-icon-more"></i>
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item @click="treeNodeOption(data, 'EDI', node)">编辑</el-dropdown-item>
                      <el-dropdown-item @click="treeNodeOption(data, 'APP', node)">
                        <template v-if="node.level==2">
                          添加楼层
                        </template>
                        <template v-if="node.level==3">
                          添加房间
                        </template>
                      </el-dropdown-item>
                      <el-dropdown-item @click="treeNodeOption(data, 'DEL', node)">删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown> -->
                  </span>
                  <span class="tree-btn" v-else>
                    <!-- <i class="el-icon-circle-plus-outline" @click.stop="treeNodeOption(data, 'APP', node)"></i> -->

                    <i class="btni" @click.stop="treeNodeOption(data, 'APP', node)">新增楼栋</i>
                    <!-- <el-dropdown placement="bottom" class="dropdown">
                    <span class="el-dropdown-link">
                      <i class="el-icon-more"></i>
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item @click="treeNodeOption(-1, 'APP')">添加楼栋</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown> -->
                  </span>
                </span>
              </el-tree>
            </div>
          </div>
        </el-scrollbar>
      </div>
      <div class="my-basic-inner">
        <div class="block1">
          <div class="box-outer">
            <room-search-toolbar
              ref="searchToolbar"
              :search-form="searchForm"
              :all-dorm-type-list="allDormTypeList"
              :has-data="hasData"
              :export-loading="exportLoading"
              @update-search-field="updateSearchField"
              @search="searchSubmit(searchForm)"
              @reset="resetFrom"
              @export="export2Excel"
              @batch-edit="handleBatchEdit"
              @sd-batch-edit="handleSDBatchEdit"
            />
          </div>
        </div>
        <div class="block1 block2">
          <div class="box-outer" v-if="hasData">
            <div style="margin-bottom: 20px;">
              <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="checkAllChange">全选</el-checkbox>
              <div class="tps">
                <span class="tp tp1"><i></i>男宿</span>
                <span class="tp tp2"><i></i>女宿</span>
                <span class="tp tp3"><i></i>夫妻/混住</span>
                <span class="tp tp4"><i class="lock"></i>不参与分配</span>
              </div>
            </div>
            <div style="margin: 15px 0"></div>
            <el-checkbox-group v-model="checkedRoom" @change="roomChange" class="room-list">
              <el-checkbox v-for="(item, index) in tableData" :label="item.id" :key="index">
                <i class="lock" v-if="item.isDormitoryRoom===1"></i>
                <div class="room-item" :class="item.roomSex | f_roomGenderClass">
                  <div>{{ item.roomName }}</div>
                  <div>{{ item.bedTotal }}人间</div>
                </div>
                <el-dropdown placement="bottom" class="dropdown">
                  <span class="el-dropdown-link">
                    <i class="el-icon-more"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item @click.native="handleEdit(item)">编辑房间</el-dropdown-item>
                    <el-dropdown-item @click.native="rowDel(item)">删除房间</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </el-checkbox>
            </el-checkbox-group>
          </div>
          <div v-else class="noData">当前条件下暂无住宿信息（请选择具体楼层）</div>
        </div>
      </div>
      <el-dialog title="编辑房间" class="dialog_form" width="600px" @close="resetEditForm('editForm')" :visible.sync="editFormVisible">
        <el-form :rules="editRules" ref="editForm" :model="editForm" label-width="120px">
          <el-form-item label="房间号" prop="roomName">
            <el-input v-model="editForm.roomName" disabled></el-input>
          </el-form-item>
          <el-form-item label="是否参与分配" prop="isDormitoryRoom">
            <el-select v-model="editForm.isDormitoryRoom" placeholder="请选择" @change="showBedNum">
              <el-option v-for="item in isDormitoryArr" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="是否参与计算" prop="isCount">
            <el-select v-model="editForm.isCount" placeholder="请选择">
              <el-option v-for="item in isCountArr" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="宿舍分类" prop="roomType">
            <el-select v-model="editForm.roomType" placeholder="请选择" @change="getBedNum">
              <el-option v-for="item in parkDormTypeList" :key="item.id" :label="item.typeName" :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="床位数" prop="bedTotal">
            <el-input v-model="editForm.bedTotal" disabled></el-input>
          </el-form-item>
          <!-- :disabled="editForm.usedBed>0" -->
          <el-form-item label="房间属性" prop="roomSex">
            <roomGenderSelect v-model="editForm.roomSex"></roomGenderSelect>
          </el-form-item>
          <el-form-item label="水电分摊模板" prop="sdTemplateId">
            <el-select v-model="editForm.sdTemplateId" placeholder="请选择">
              <el-option v-for="item in sdTempList" :key="item.id" :label="item.templateName" :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="离职结算模板" prop="leaveTempName">
            <el-input v-model="editForm.leaveTempName" disabled></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="resetEditForm('editForm')" plain>取 消</el-button>
          <el-button type="primary" @click="editSubmit('editForm')" :loading="editLoading">确 定</el-button>
        </div>
      </el-dialog>
      <!--- 批量编辑弹框 --->
      <el-dialog :title="editTitle" class="dialog_form" width="600px" @close="resetBatchEditForm('batchEditForm')" :visible.sync="batchEditFormVisible">
        <el-form :rules="batchEditRules" ref="batchEditForm" :model="batchEditForm" label-width="120px">
          <el-form-item label="是否参与分配" v-if="!isHandelSD" prop="isDormitoryRoom">
            <el-select v-model="batchEditForm.isDormitoryRoom" placeholder="请选择">
              <el-option v-for="item in isDormitoryArr" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="是否参与计算" v-if="!isHandelSD" prop="isCount">
            <el-select v-model="batchEditForm.isCount" placeholder="请选择">
              <el-option v-for="item in isCountArr" :key="item.value" :label="item.label" :value="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="宿舍分类" v-if="!isHandelSD" prop="roomType">
            <el-select v-model="batchEditForm.roomType" placeholder="请选择">
              <el-option v-for="item in parkDormTypeList" :key="item.id" :label="item.typeName" :value="item.id"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="房间属性" v-if="!isHandelSD" prop="roomSex">
            <roomGenderSelect v-model="batchEditForm.roomSex"></roomGenderSelect>
          </el-form-item>
          <el-form-item label="水电分摊模板" v-if="isHandelSD" prop="sdTemplateId">
            <el-select v-model="batchEditForm.sdTemplateId" placeholder="请选择">
              <el-option v-for="item in sdTempList" :key="item.id" :label="item.templateName" :value="item.id"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="resetBatchEditForm('batchEditForm')" plain>取 消</el-button>
          <el-button type="primary" @click="batchEditSubmit('batchEditForm')" :loading="editLoading">确 定</el-button>
        </div>
      </el-dialog>
      <!-- 楼层添加、编辑 -->
      <el-dialog :title="floorTitle" class="dialog_form" width="550px" @close="resetFloorForm('floorForm')" :visible.sync="floorVisible">
        <el-form :rules="editFloor ? floorEditRules : floorAddRules" ref="floorForm" :model="floorForm" label-width="80px">
          <template v-if="editFloor">
            <el-form-item label="楼层编号" prop="floorName">
              <el-input v-model="floorForm.floorName" disabled></el-input>
            </el-form-item>
            <el-form-item label="房间数量" prop="roomNum">
              <el-input v-model="floorForm.roomNum" clearable></el-input>
            </el-form-item>
          </template>
          <template v-else>
            <el-form-item label="起始编号" prop="startNum">
              <el-input v-model="floorForm.startNum" clearable :disabled="hasStartNum"></el-input>
            </el-form-item>
            <el-form-item label="楼层数量" prop="floorNum">
              <el-input v-model="floorForm.floorNum" clearable></el-input>
            </el-form-item>
          </template>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="resetFloorForm('floorForm')" plain>取 消</el-button>
          <el-button type="primary" @click="floorSubmit('floorForm')" :loading="floorLoading">确 定</el-button>
        </div>
      </el-dialog>
      <!-- 楼栋添加、编辑 -->
      <el-dialog :title="dormTitle" class="dialog_form" width="550px" @close="resetDormForm('dormForm')" :visible.sync="dormVisible">
        <el-form :rules="dormRules" ref="dormForm" :model="dormForm" label-width="100px" label-position="left">
          <el-form-item label="宿舍楼名称" prop="dormitoryName">
            <el-input v-model="dormForm.dormitoryName" clearable></el-input>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="resetDormForm('dormForm')" plain>取 消</el-button>
          <el-button type="primary" @click="dormSubmit('dormForm')" :loading="floorLoading">确 定</el-button>
        </div>
      </el-dialog>
    </el-scrollbar>
  </div>
</template>
<style lang="scss">
@use "@/styles/platform/dormitory/room" as *;
</style>

<script>
import { fetchRoomList, floorList, delObj, putObj, putBatchObj, putSDBatchObj, dormTypeApi, allDormitoryType, getBedNum, fetchSDTempList } from '@/api/platform/dormitory/room'
import { putDormObj, addObj, delDormObj, getDormObj } from '@/api/platform/dormitory/dormitory'
import { delFloor, addFloor, getFloor, updateDormitoryFloor, getFloorStartNum } from '@/api/platform/dormitory/floor'
import { tableOption } from '@/const/crud/platform/dormitory/room'
import { excel } from '@/util/excel'
import { mapGetters } from 'vuex'
import echarts from 'echarts'
import RoomSearchToolbar from './components/RoomSearchToolbar.vue'
import {
  buildRoomListQuery,
  formatRoomExportRows,
  hasRoomListData,
  isEmptyRoomBatchEditForm,
  roomGenderClass,
  roomSelectionState,
  shouldShowRoomTreeNode,
  toCheckedRoomIds,
  toExportRows
} from './room-rules'

// import echarts from "./";
//是否为寝室，默认0， 0-是，1-否
const isDormitoryOption = [
  { label: '是', value: 0 },
  { label: '否', value: 1 }
]
const isCountOption = [
  { label: '是', value: 1 },
  { label: '否', value: 0 }
]
export default {
  name: 'room',
  components: {
    RoomSearchToolbar
  },
  data() {
    var validateIsNum = (rule, value, callback) => {
      let regName = /^-?\d+$/
      if (value === 0) {
        callback(new Error('请输入非0整数'))
      }
      if (!regName.test(value)) {
        callback(new Error('请输入整数'))
      } else {
        callback()
      }
    }
    var validateFloorNum = (rule, value, callback) => {
      let regName = /^\d+$/
      if (value == 0) {
        callback(new Error('请输入大于0的正整数'))
      }

      if (!regName.test(value)) {
        callback(new Error('请输入正整数'))
      } else if (value > 14) {
        callback(new Error('楼层数量最大值为14'))
      } else {
        callback()
      }
    }
    var validateRoomNum = (rule, value, callback) => {
      let regName = /^\d+$/
      // if(value==0)
      // {
      //    callback(new Error('请输入大于0的正整数'));
      // }

      if (!regName.test(value)) {
        callback(new Error('不能输入小数和负数'))
      } else {
        callback()
      }

      // if (!regName.test(value)) {
      //   callback(new Error("不能输入小数和负数"));
      // } else if (value > 24) {
      //   callback(new Error("房间数量最大值为24"));
      // } else {
      //   callback();
      // }
    }
    return {
      floorTitle: '添加楼层',
      dormTitle: '添加楼栋',
      filterText: "",
      editFloor: false,
      editDorm: false,
      floorVisible: false,
      dormVisible: false,
      floorLoading: false,
      dormLoading: false,
      floorForm: {
        parkId: undefined, //所属园区
        dormitoryId: undefined, //所属楼栋
        startNum: undefined, //起始编号
        floorNum: undefined //楼层数量
      },
      floorAddRules: {
        startNum: [
          { required: true, message: '请输入起始编号', trigger: 'blur' },
          { validator: validateIsNum, trigger: 'blur' }
        ],
        floorNum: [
          { required: true, message: '请输入楼层数量', trigger: 'blur' },
          { validator: validateFloorNum, trigger: 'blur' }
        ]
      },
      floorEditRules: {
        roomNum: [
          { required: true, message: '请输入房间数量', trigger: 'blur' },
          { validator: validateRoomNum, trigger: 'blur' }
        ]
      },
      hasStartNum: false, //添加楼层时标记， 起始编号是否是读出来的，默认false
      dormForm: {
        parkId: undefined,
        dormitoryName: undefined
      },
      dormRules: {
        dormitoryName: [{ required: true, message: '请输入楼栋名称', trigger: 'blur' }]
      },

      defaultKey: undefined, //树形，默认选中的id
      checkAll: false,
      checkedRoom: [],
      isIndeterminate: false,
      editLoading: false, //是否正在编辑
      editFormVisible: false, //编辑房间是否显示
      batchEditFormVisible: false, //批量编辑弹框是否显示
      editTitle: '批量设置房间类型',
      isHandelSD: false, //是否批量修改水电
      searchForm: {
        //搜索菜单表单
        isDormitoryRoom: undefined,
        isCount: undefined, //是否参与计算
        roomType: undefined,
        roomSex: undefined
      },
      isDormitoryArr: isDormitoryOption,
      isCountArr: isCountOption,
      batchEditForm: {
        roomIds: [],
        isDormitoryRoom: undefined, //是否为寝室
        isCount: undefined, //是否参与计算
        roomType: undefined, //宿舍分类
        roomSex: undefined, //房间属性
        sdTemplateId: undefined //水电模板ID
      },
      batchEditRules: {
        sdTemplateId: [{ required: true, message: '请选择水电模板', trigger: 'change' }]
      },
      editForm: {
        roomName: undefined, //房间号
        isDormitoryRoom: undefined, //是否为寝室
        isCount: undefined, //是否参与计算
        roomType: undefined, //宿舍分类
        bedTotal: undefined, //床位数
        roomSex: undefined, //房间属性
        sdTemplateId: undefined, //水电模板ID
        leaveTempName: undefined,
        leaveTempId: undefined
      },
      editRules: {
        roomName: [{ required: true, message: '请输入房间号', trigger: 'blur' }],
        isDormitoryRoom: [{ required: true, message: '请选择是否参与分配', trigger: 'change' }],
        isCount: [{ required: true, message: '请选择是否参与计算', trigger: 'change' }],
        roomType: [{ required: true, message: '请选择宿舍分类', trigger: 'change' }],
        bedTotal: [{ required: true, message: '请输入床位数', trigger: 'blur' }],
        roomSex: [{ required: true, message: '请选择房间属性', trigger: 'change' }],
        sdTemplateId: [{ required: true, message: '请选择水电模板', trigger: 'change' }]
      },
      sdTempList: [], //可选水电模板列表
      tableOption: tableOption,
      exportLoading: false,
      tableData: [],
      parkDormTypeList: [],
      allDormTypeList: [],
      parkId: null, //树形的园区Id
      dormitoryId: null, //树形的楼栋Id
      floorId: null, //树形的楼层Id
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'label'
      }
    }
  },
  created() {
    this.getTree()
    this.getAllDormType()
  },
  mounted: function () {},
  computed: {
    hasData() {
      return hasRoomListData(this.parkId, this.tableData)
    }
  },
  watch: {
    filterText(val) {
      this.$refs.roomtree.filter(val);
    }
  },
  filters: {
    f_roomGenderClass(val) {
      return roomGenderClass(val)
    }
  },
  methods: {
    //添加楼栋
    dormAdd(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.dormLoading = true
          let _this = this
          addObj(this.dormForm)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true || dataResult === 1) {
                this.dormVisible = false
                this.getTree()
                this.$notify({
                  title: '成功',
                  message: '添加楼栋成功',
                  type: 'success',
                  duration: 2000
                })
              } else if (dataResult === false) {
                this.$notify({
                  title: '添加楼栋失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
              this.dormLoading = false
            })
            .catch((err) => {
              this.dormLoading = false
            })
        } else {
          return false
        }
      })
    },
    //编辑楼栋
    dormEdit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.dormLoading = true
          putDormObj(this.dormForm)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true || dataResult === 1) {
                this.dormVisible = false
                this.getTree()
                this.$notify({
                  title: '成功',
                  message: '编辑楼栋成功',
                  type: 'success',
                  duration: 2000
                })
              } else if (dataResult === false) {
                this.$notify({
                  title: '编辑楼栋失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
              this.dormLoading = false
            })
            .catch((err) => {
              this.dormLoading = false
            })
        } else {
          return false
        }
      })
    },
    //添加、编辑楼栋，确定
    dormSubmit(formName) {
      if (this.editDorm) {
        this.dormEdit(formName)
      } else {
        this.dormAdd(formName)
      }
    },
    //添加、编辑楼栋，重置表单
    resetDormForm(formName) {
      this.dormVisible = false
      this.dormLoading = false
      this.hasStartNum = false //将起始编号状态恢复默认设置
      this.$refs[formName] && this.$refs[formName].resetFields()
    },
    //添加楼层
    floorAdd(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.floorLoading = true
          addFloor(this.floorForm)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true) {
                this.floorVisible = false
                this.getTree()
                this.$notify({
                  title: '成功',
                  message: '添加楼层成功',
                  type: 'success',
                  duration: 2000
                })
              } else if (dataResult === false) {
                this.$notify({
                  title: '添加楼层失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
              this.floorLoading = false
            })
            .catch(() => {
              this.floorLoading = false
            })
        } else {
          return false
        }
      })
    },
    //编辑楼层
    floorEdit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.floorLoading = true
          updateDormitoryFloor(this.floorForm)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true || dataResult === 1) {
                this.floorVisible = false
                this.getTree()
                this.$notify({
                  title: '成功',
                  message: '编辑楼层成功',
                  type: 'success',
                  duration: 2000
                })
              } else if (dataResult === false) {
                this.$notify({
                  title: '编辑楼层失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
              this.floorLoading = false
            })
            .catch(() => {
              this.floorLoading = false
            })
        } else {
          return false
        }
      })
    },
    //添加、编辑楼层，确定
    floorSubmit(formName) {
      if (this.editFloor) {
        this.floorEdit(formName)
      } else {
        this.floorAdd(formName)
      }
    },
    //添加、编辑楼层，重置表单
    resetFloorForm(formName) {
      this.floorVisible = false
      this.floorLoading = false
      this.hasStartNum = false //将起始编号状态恢复默认设置
      this.$refs[formName] && this.$refs[formName].resetFields()
    },
    //编辑楼层的时候调用
    getFloorStartNum(dormitoryId) {
      if (dormitoryId == undefined || dormitoryId == '') return
      getFloorStartNum(dormitoryId).then((response) => {
        if (response.data.data) {
          this.hasStartNum = true //表明起始编号是读出来的
          this.floorForm.startNum = response.data.data
        } else {
          this.hasStartNum = false //表明起始编号是读出来的
          this.floorForm.startNum = undefined
        }
      })
    },
    //全选房间
    checkAllChange(val) {
      // this.checkedRoom = val ? this.tableData : [];
      this.checkedRoom = toCheckedRoomIds(val, this.tableData)
      this.isIndeterminate = false
    },
    //多选房间
    roomChange(value) {
      const selectionState = roomSelectionState(value, this.tableData)
      this.checkAll = selectionState.checkAll
      this.isIndeterminate = selectionState.isIndeterminate
    },
    /**
     * tree节点操作
     */
    async treeNodeOption(data, opt, node) {
      switch (opt) {
        case 'EDI': //编辑

          if (node.level == 2) {
            //楼栋

            this.dormVisible = true
            this.editDorm = true
            this.dormTitle = '编辑楼栋'
            const res = await getDormObj(data.id)
            this.dormForm = res.data.data
          }
          if (node.level == 3) {
            //楼层

            this.floorVisible = true
            this.editFloor = true
            this.floorTitle = '编辑楼层'
            const res = await getFloor(data.id)
            this.floorForm = res.data.data
          }
          break
        case 'APP': //新增

          if (node.level == 1) {
            //园区，点击新增是新增楼栋
            this.dormVisible = true
            this.editDorm = false
            this.dormTitle = '新增楼栋'
            this.dormForm = {
              parkId: this.parkId,
              dormitoryName: undefined
            }
          }
          if (node.level == 2) {
            //楼栋，点击新增是新增楼层

            this.floorVisible = true
            this.editFloor = false
            this.floorTitle = '新增楼层'
            this.floorForm = {
              parkId: this.parkId,
              dormitoryId: this.dormitoryId,
              startNum: undefined,
              floorNum: undefined
            }
            this.getFloorStartNum(this.dormitoryId)
          }
          if (node.level == 3) {
            //楼层，点击新增是新增房间

            // this.floorVisible = true
            // this.editFloor = false
            // this.floorTitle = '新增楼层'
            // this.floorForm = {
            //   parkId: undefined,
            //   dormitoryId: undefined,
            //   startNum: undefined,
            //   floorNum: undefined
            // }
          }
          break
        case 'DEL': //删除

          if (node.level == 2) {
            //楼栋

            await this.$confirm('是否确认移除此楼栋', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            })
            const res = await delDormObj(data.id)
            if (res.data.code === 0) {
              if (!res.data.data) {
                this.$notify({
                  title: '失败',
                  message: res.data.msg,
                  type: 'error',
                  duration: 2000
                })
              } else {
                this.getTree()
                this.$notify({
                  title: '成功',
                  message: '删除楼栋成功',
                  type: 'success',
                  duration: 2000
                })
              }
            } else {
              this.$notify({
                title: '失败',
                message: res.data.msg,
                type: 'error',
                duration: 2000
              })
            }
          }
          if (node.level == 3) {
            //楼层
            await this.$confirm('是否确认移除此楼层', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            })
            const res = await delFloor(data.id)
            if (res.data.code === 0) {
              if (!res.data.data) {
                this.$notify({
                  title: '失败',
                  message: res.data.msg,
                  type: 'error',
                  duration: 2000
                })
              } else {
                this.getTree()
                this.$notify({
                  title: '成功',
                  message: '删除楼层成功',
                  type: 'success',
                  duration: 2000
                })
              }
            } else {
              this.$notify({
                title: '失败',
                message: res.data.msg,
                type: 'error',
                duration: 2000
              })
            }
          }
          break
      }
      // this.deptInit()
    },
    /**
     * 节点点击事件
     */
    handleNodeClick(data, node) {
      // console.log(data)
      // console.log(node)
      var level = node.level
      // this.dormitoryId = null;
      // this.floorId = null;
      // this.parkId = null;
      this.tableData = []
      if (level == 1) {
        // if(node.expanded){
        //   this.parkId = data.id;
        // }
        this.parkId = data.id
      } else if (level == 2) {
        this.parkId = node.parent.data.id
        this.dormitoryId = data.id
      } else if (level == 3) {
        //只有选择楼层的时候进行查询
        this.parkId = node.parent.parent.data.id
        this.dormitoryId = node.parent.data.id
        this.floorId = data.id
        this.getList(this.searchForm)
      }
    },
    goBedMng(roomName) {
      const src = `/platform/dormitory/bed_mng`
      this.$router.push({
        path: src,
        query: {
          roomname: roomName
        }
      })
    },
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true
        const { export_json_to_excel } = require('@/vendor/Export2Excel')
        const tHeader = ['房间号', '是否参与分配','是否参与计算', '宿舍分类', '床位数', '实住人数', '差异人数', '房间属性', '所属园区']
        const filterVal = ['roomName', 'isDormitoryRoom','isCount', 'typeName', 'bedTotal', 'usedBed', 'freeBed', 'roomSex', 'parkName']
        fetchRoomList(buildRoomListQuery({
          parkId: this.parkId,
          dormitoryId: this.dormitoryId,
          floorId: this.floorId
        }, this.searchForm))
          .then((response) => {
            const list = response.data.data
            formatRoomExportRows(list)
            const data = this.formatJson(filterVal, list)
            export_json_to_excel(tHeader, data, '房间列表')
            this.exportLoading = false
          })
          .catch((err) => {
            this.exportLoading = false
          })
      })
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return toExportRows(filterVal, jsonData)
    },
    //跟登录账号所属园区相关的宿舍分类（可能关联多个园区）
    getAllDormType() {
      allDormitoryType().then((response) => {
        this.allDormTypeList = response.data.data
      })
    },
    //根据具体的园区获取对应的宿舍分类
    getParkDormType(parkId) {
      dormTypeApi({ parkId }).then((response) => {
        this.parkDormTypeList = response.data.data
      })
    },
    //根据具体的园区获取获取可选择的水电模板列表
    getSDTempList(parkId) {
      fetchSDTempList(parkId).then((response) => {
        this.sdTempList = response.data.data
      })
    },
    //获取树形数据
    getTree() {
      var _this = this
      floorList().then((response) => {
        this.treeData = response.data.data
        this.parkId = this.treeData[0].id
        this.defaultKey = this.parkId
        if (this.treeData) {
          if (this.parkId) {
            if (this.treeData[0].children && this.treeData[0].children.length > 0) {
              this.dormitoryId = this.treeData[0].children[0].id
              this.defaultKey = this.dormitoryId
              if (this.dormitoryId) {
                if (this.treeData[0].children[0].children[0] && this.treeData[0].children[0].children[0].length > 0) {
                  this.floorId = this.treeData[0].children[0].children[0].id
                  this.defaultKey = this.floorId
                }
              }
            }
          }
        }
        if (this.floorId) {
          _this.getList()
        }
      })
    },
    showBedNum() {
      if (this.editForm.isDormitoryRoom == 1) {
        this.editForm.bedTotal = 0
      }
    },
    getBedNum() {
      if (this.editForm.isDormitoryRoom == 1) {
        return
      }
      var id = this.editForm.roomType
      this.editForm.bedTotal = null
      getBedNum(id).then((response) => {
        this.editForm.bedTotal = response.data.data.bedTotal
      })
    },
    filterNode(value, data) {
      return shouldShowRoomTreeNode(value, data)
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.getList(form)
    },
    updateSearchField({ field, value }) {
      this.searchForm[field] = value
    },
    /**
     * 清空搜索
     */
    resetFrom() {
      this.getList()
      if (this.$refs.searchToolbar != undefined) {
        this.$refs.searchToolbar.resetFields()
      }
    },
    //查询房间列表
    getList(params) {
      this.checkedRoom = []
      this.isIndeterminate = false
      this.tableLoading = true
      fetchRoomList(buildRoomListQuery({
        parkId: this.parkId,
        dormitoryId: this.dormitoryId,
        floorId: this.floorId
      }, params)).then((response) => {
        this.tableData = response.data.data
      })
    },
    //删除房间
    rowDel(row) {
      var _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除该房间信息？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          return delObj(row.id)
        })
        .then((data) => {
          if (data.data.data == false) {
            _this.$notify({
              title: '失败',
              message: data.data.msg,
              type: 'error'
            })
          } else {
            _this.getList(_this.searchForm)
            _this.$notify({
              title: '成功',
              message: '删除房间成功',
              type: 'success'
            })
          }
        })
        .catch(error => { console.error(error) })
    },
    //编辑房间
    handleEdit(row) {
      this.editFormVisible = true
      this.editForm = Object.assign({}, row)
      this.getParkDormType(this.parkId)
      if (this.editForm.isDormitoryRoom == 0) {
        if (!this.validatenull(this.editForm.roomType)) {
          getBedNum(this.editForm.roomType).then((response) => {
            this.editForm.bedTotal = response.data.data.bedTotal
          })
        }
      }
      this.getSDTempList(this.parkId)
    },
    //批量设置房间类型
    handleBatchEdit() {
      if (this.checkedRoom && this.checkedRoom.length > 0) {
        this.isHandelSD = false
        this.getParkDormType(this.parkId)
        this.batchEditForm.roomIds = this.checkedRoom
        this.editTitle = '批量设置房间类型'
        this.batchEditFormVisible = true
      } else {
        this.$message({
          message: '请先选择要设置的房间',
          type: 'warning'
        })
      }
    },
    //批量设置房间水电模板
    handleSDBatchEdit() {
      if (this.checkedRoom && this.checkedRoom.length > 0) {
        this.isHandelSD = true
        this.batchEditForm.roomIds = this.checkedRoom
        this.editTitle = '批量设置房间水电模板'
        this.getSDTempList(this.parkId)
        this.batchEditFormVisible = true
      } else {
        this.$message({
          message: '请先选择要设置的房间',
          type: 'warning'
        })
      }
    },
    //批量修改，确定
    batchEditSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (this.isHandelSD) {
            this.editLoading = true
            this.batchEditForm.parkId = this.parkId
            putSDBatchObj(this.batchEditForm)
              .then((response) => {
                var msg = response.data.msg
                var result = response.data.success
                this.editLoading = false
                if (result === true) {
                  this.batchEditFormVisible = false
                  this.getList(this.searchForm)
                  this.$notify({
                    title: '成功',
                    message: '批量设置房间水电模板成功',
                    type: 'success',
                    duration: 2000
                  })
                } else if (result === false) {
                  this.$notify({
                    title: '失败',
                    message: msg,
                    type: 'error',
                    duration: 2000
                  })
                }
              })
              .catch((err) => {
                this.editLoading = false
              })
          } else {

            if (isEmptyRoomBatchEditForm(this.batchEditForm)) {
              this.$message.error('请在 否参与分配、否参与计算、宿舍分类、房间属性 中至少选择一项')
              return
            }
            this.editLoading = true
            putBatchObj(this.batchEditForm)
              .then((response) => {
                var msg = response.data.msg
                var result = response.data.success
                this.editLoading = false
                if (result === true) {
                  this.batchEditFormVisible = false
                  this.getList(this.searchForm)
                  this.$notify({
                    title: '成功',
                    message: '批量设置房间类型成功',
                    type: 'success',
                    duration: 2000
                  })
                } else if (result === false) {
                  this.$notify({
                    title: '失败',
                    message: msg,
                    type: 'error',
                    duration: 2000
                  })
                }
              })
              .catch((err) => {
                this.editLoading = false
              })
          }
        } else {
          return false
        }
      })
    },
    //重置批量修改表单
    resetBatchEditForm(formName) {
      this.batchEditFormVisible = false
      this.$refs[formName].resetFields()
    },
    //编辑房间，确定
    editSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.editLoading = true
          this.editForm['aliasName'] = this.editForm.roomName
          delete this.editForm.roomName
          putObj(this.editForm)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.code
              this.editLoading = false
              if (dataResult === 0) {
                this.editFormVisible = false
                this.getList(this.searchForm)
                this.$notify({
                  title: '成功',
                  message: '编辑房间成功',
                  type: 'success',
                  duration: 2000
                })
              } else {
                this.$notify({
                  title: '编辑房间失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
            })
            .catch((err) => {
              this.editLoading = false
            })
        } else {
          return false
        }
      })
    },
    //重置编辑房间表单
    resetEditForm(formName) {
      this.editFormVisible = false
      this.editLoading = false
    }
  }
}
</script>

<style lang="scss" scoped>
.room ::v-deep {
  $c_man: #70a9ff;
  $c_woman: #ff98c4;
  $c_mix: #c4a7ff;
  .box-left {
    width: 375px;
  }
  .my-scrollbar {
    padding: 0 0 0 380px;
  }
  .tps{
    display: inline-block;
    margin-left: 60px;
    .tp{
      margin-right: 20px;
      i{
        width: 10px;
        height: 10px;
        display: inline-block;
        vertical-align: middle;
        margin: -2px 4px 0 0;
      }
    }
    .tp1{
      i{
        background-color: $c_man;
      }
    }
    .tp2{
      i{
        background-color: $c_woman;
      }
    }
    .tp3{
      i{
        background-color: $c_mix;
      }
    }
  }
  .noData {
    color: #999;
    text-align: center;
    padding-top: 100px;
  }
  .my-basic-inner {
    display: flex;
    flex-direction: column;
    background: transparent;
    .block1 {
      margin-bottom: 20px;
      background: #fff;
    }
    .block2 {
      flex: 1;
      margin-right: 0;
      margin-left: 0;
      margin-bottom: 0;
    }
  }
  .lock {
    width: 0px;
    height: 0px;
    border: 7px solid red;
    border-top-color: transparent;
    border-left-color: transparent;
    display: inline-block;
    border-radius: 3px;
  }
  .room-list {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    .el-checkbox + .el-checkbox {
      margin-left: 0;
    }
    .el-checkbox {
      position: relative;
      margin: 0 20px 20px 0;
      .el-checkbox__label {
        padding-left: 0;
      }
      .el-checkbox__input {
        position: absolute;
        left: 5px;
        top: 5px;
      }
    }
    .dropdown {
      position: absolute;
      top: 3px;
      right: 6px;
      .el-icon-more {
        color: #fff;
      }
    }
    .lock {
      position: absolute;
      bottom: 0.7px;
      right: 0.5px;
    }
    .room-item {
      width: 130px;
      height: 60px;
      border-radius: 3px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 15px;
      background: #e0e0e0;
      color: #fff;
      > div {
        margin-top: 14px;
      }
      .room-num {
        text-align: left;
      }
      .room-type {
        text-align: right;
      }
    }
    .man {
      background: $c_man;
    }
    .woman {
      background: $c_woman;
    }
    .mix {
      background: $c_mix;
    }
  }
}
.topForm ::v-deep {
  .el-form-item__label {
    width: 120px;
  }
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
  .btni {
    font-size: 12px;
    font-style: normal;
    margin-left: 8px;
  }
  span:nth-of-type(1) {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    word-break: break-all;
    width: 145px;
  }
  span:nth-of-type(2) {
    color: #ee6a00;
  }
  // span:nth-of-type(2) {
  //   display: none;
  // }
  // &:hover {
  //   span:nth-of-type(2) {
  //     display: block;
  //   }
  // }
}
</style>
