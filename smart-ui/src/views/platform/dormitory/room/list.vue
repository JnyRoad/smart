<!--宿舍管理，房间管理，列表  -->
<template>
  <div class="my-basic-container mycard2 room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <room-tree-panel
        :tree-data="treeData"
        :default-props="defaultProps"
        @node-click="handleNodeClick"
        @node-action="treeNodeOption"
      />
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
        <room-grid-panel
          :has-data="hasData"
          :table-data="tableData"
          :checked-room="checkedRoom"
          :check-all="checkAll"
          :is-indeterminate="isIndeterminate"
          @update-checked-room="checkedRoom = $event"
          @update-check-all="checkAll = $event"
          @check-all-change="checkAllChange"
          @room-change="roomChange"
          @edit-room="handleEdit"
          @delete-room="rowDel"
        />
      </div>
      <room-edit-dialog
        ref="editForm"
        :visible="editFormVisible"
        :form="editForm"
        :rules="editRules"
        :is-dormitory-arr="isDormitoryArr"
        :is-count-arr="isCountArr"
        :park-dorm-type-list="parkDormTypeList"
        :sd-temp-list="sdTempList"
        :loading="editLoading"
        @update-form-field="updateEditFormField"
        @show-bed-num="showBedNum"
        @get-bed-num="getBedNum"
        @close="resetEditForm('editForm')"
        @submit="editSubmit('editForm')"
      />
      <!--- 批量编辑弹框 --->
      <room-batch-edit-dialog
        ref="batchEditForm"
        :title="editTitle"
        :visible="batchEditFormVisible"
        :form="batchEditForm"
        :rules="batchEditRules"
        :is-handel-s-d="isHandelSD"
        :is-dormitory-arr="isDormitoryArr"
        :is-count-arr="isCountArr"
        :park-dorm-type-list="parkDormTypeList"
        :sd-temp-list="sdTempList"
        :loading="editLoading"
        @update-form-field="updateBatchEditFormField"
        @close="resetBatchEditForm('batchEditForm')"
        @submit="batchEditSubmit('batchEditForm')"
      />
      <!-- 楼层添加、编辑 -->
      <room-floor-dialog
        ref="floorForm"
        :title="floorTitle"
        :visible="floorVisible"
        :form="floorForm"
        :rules="editFloor ? floorEditRules : floorAddRules"
        :edit-floor="editFloor"
        :has-start-num="hasStartNum"
        :loading="floorLoading"
        @update-form-field="updateFloorFormField"
        @close="resetFloorForm('floorForm')"
        @submit="floorSubmit('floorForm')"
      />
      <!-- 楼栋添加、编辑 -->
      <room-dormitory-dialog
        ref="dormForm"
        :title="dormTitle"
        :visible="dormVisible"
        :form="dormForm"
        :rules="dormRules"
        :loading="floorLoading"
        @update-form-field="updateDormFormField"
        @close="resetDormForm('dormForm')"
        @submit="dormSubmit('dormForm')"
      />
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
import RoomGridPanel from './components/RoomGridPanel.vue'
import RoomTreePanel from './components/RoomTreePanel.vue'
import RoomSearchToolbar from './components/RoomSearchToolbar.vue'
import RoomEditDialog from './components/RoomEditDialog.vue'
import RoomBatchEditDialog from './components/RoomBatchEditDialog.vue'
import RoomFloorDialog from './components/RoomFloorDialog.vue'
import RoomDormitoryDialog from './components/RoomDormitoryDialog.vue'
import {
  buildRoomListQuery,
  createBatchEditRules,
  createDormFormForPark,
  createDormRules,
  createEmptyBatchEditForm,
  createEmptyDormForm,
  createEmptyFloorForm,
  createEmptyRoomEditForm,
  createFloorAddRules,
  createFloorFormForDormitory,
  createFloorEditRules,
  createRoomExportConfig,
  createRoomEditRules,
  formatRoomExportRows,
  hasRoomListData,
  isEmptyRoomBatchEditForm,
  prepareRoomEditSubmitForm,
  roomCountOptions,
  roomDormitoryOptions,
  roomSelectionState,
  toCheckedRoomIds,
  toExportRows
} from './room-rules'
import { roomInitialTreeSelection, roomTreeScopeForNode } from './room-tree-rules'

export default {
  name: 'room',
  components: {
    RoomGridPanel,
    RoomTreePanel,
    RoomSearchToolbar,
    RoomEditDialog,
    RoomBatchEditDialog,
    RoomFloorDialog,
    RoomDormitoryDialog
  },
  data() {
    return {
      floorTitle: '添加楼层',
      dormTitle: '添加楼栋',
      editFloor: false,
      editDorm: false,
      floorVisible: false,
      dormVisible: false,
      floorLoading: false,
      dormLoading: false,
      floorForm: createEmptyFloorForm(),
      floorAddRules: createFloorAddRules(),
      floorEditRules: createFloorEditRules(),
      hasStartNum: false, //添加楼层时标记， 起始编号是否是读出来的，默认false
      dormForm: createEmptyDormForm(),
      dormRules: createDormRules(),

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
      isDormitoryArr: roomDormitoryOptions,
      isCountArr: roomCountOptions,
      batchEditForm: createEmptyBatchEditForm(),
      batchEditRules: createBatchEditRules(),
      editForm: createEmptyRoomEditForm(),
      editRules: createRoomEditRules(),
      sdTempList: [], //可选水电模板列表
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
  methods: {
    //添加楼栋
    dormAdd(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.dormLoading = true
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
            .catch(() => {
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
            .catch(() => {
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
            this.dormForm = createDormFormForPark(this.parkId)
          }
          if (node.level == 2) {
            //楼栋，点击新增是新增楼层

            this.floorVisible = true
            this.editFloor = false
            this.floorTitle = '新增楼层'
            this.floorForm = createFloorFormForDormitory(this.parkId, this.dormitoryId)
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
      // this.dormitoryId = null;
      // this.floorId = null;
      // this.parkId = null;
      this.tableData = []
      const selection = roomTreeScopeForNode(data, node)
      Object.assign(this, selection.scope)
      if (selection.shouldQueryRooms) {
        //只有选择楼层的时候进行查询
        this.getList(this.searchForm)
      }
    },
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true
        const { export_json_to_excel } = require('@/vendor/Export2Excel')
        const exportConfig = createRoomExportConfig()
        fetchRoomList(buildRoomListQuery({
          parkId: this.parkId,
          dormitoryId: this.dormitoryId,
          floorId: this.floorId
        }, this.searchForm))
          .then((response) => {
            const list = response.data.data
            formatRoomExportRows(list)
            const data = this.formatJson(exportConfig.fields, list)
            export_json_to_excel(exportConfig.headers, data, '房间列表')
            this.exportLoading = false
          })
          .catch(() => {
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
        Object.assign(this, roomInitialTreeSelection(this.treeData))
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
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.getList(form)
    },
    updateSearchField({ field, value }) {
      this.searchForm[field] = value
    },
    updateEditFormField({ field, value }) {
      this.editForm[field] = value
    },
    updateBatchEditFormField({ field, value }) {
      this.batchEditForm[field] = value
    },
    updateFloorFormField({ field, value }) {
      this.floorForm[field] = value
    },
    updateDormFormField({ field, value }) {
      this.dormForm[field] = value
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
              .catch(() => {
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
              .catch(() => {
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
          prepareRoomEditSubmitForm(this.editForm)
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
            .catch(() => {
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
  .box-left {
    width: 375px;
  }
  .my-scrollbar {
    padding: 0 0 0 380px;
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
}
.topForm ::v-deep {
  .el-form-item__label {
    width: 120px;
  }
}
</style>
