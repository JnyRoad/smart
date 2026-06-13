<!--设备管理，考勤机管理  -->
<template>
  <div class="my-basic-container device guard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm',searchForm)"
              plain
            >清空</el-button>
            <el-button type="primary" @click="settingVisible = true">设备设置</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加考勤机</el-button>
            <el-button type="primary" icon="el-icon-edit" @click="editLimit">更新设备权限</el-button>
            <el-button type="primary" @click="handelSetTag" plain>设置标签</el-button>

          </div>
        </div>
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="设备名称" prop="deviceName">
              <el-input v-model="searchForm.deviceName" placeholder="设备名称" clearable></el-input>
            </el-form-item>
            <el-form-item label="所在区域" prop="areaIdArray">
              <areaCascader v-model="searchForm.areaIdArray" :changeOnSelect="true" placeholder="所在区域"></areaCascader>
            </el-form-item>
            <el-form-item label="接通状态" prop="connectStatus">
              <el-select v-model="searchForm.connectStatus" placeholder="请选择接通状态" clearable>
                <el-option label="未连线" value="0"></el-option>
                <el-option label="离线" value="1"></el-option>
                <el-option label="在线" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="设备标签" prop="tagsArray">
              <deviceTagSelect v-model="searchForm.tagsArray"></deviceTagSelect>
            </el-form-item>
          </el-form>
        </div>
        <el-checkbox-group v-model="ckItem" class="list-group">
          <div class="device-list">
            <template v-for="(item, key, index) in datalist ">
              <div class="device-outer" :key="index">
                <dl class="device-item clear">
                  <div class="device-status" :class="item.connectStatus | deviceStatusClassFormat">
                    <span>{{item.connectStatus | deviceStatusFormat}}</span>
                  </div>
                  <dt></dt>
                  <dd>
                    <el-checkbox class="ck1" :label="item.id">1</el-checkbox>
                    <div class="info-tbl">
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">设备名称</div>
                        <div class="tbl_cell" :title="item.deviceName">{{item.deviceName || '-'}}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">所属区域</div>
                        <div class="tbl_cell" :title="item.areaName">{{item.areaName || '-'}}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">设备标签</div>
                        <div class="tbl_cell" :title="item.tagNames">{{item.tagNames || '-'}}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell_label">卡机类型</div>
                        <div class="tbl_cell">{{deviceCapabilityText(item.deviceCapability)}}</div>
                      </div>
                      <div class="tbl_row">
                        <div class="tbl_cell tbl_cell2">
                          <div class="circle-btns">
                            <el-button type="info" @click="handleEdit(item)" icon="el-icon-edit" class="edit-btn"></el-button>
                            <el-button type="info" @click="handleDel(item)" icon="el-icon-delete" class="del-btn" ></el-button>
                          </div>
                          <div>
                            <el-button type="primary" @click="handleClear(item)" class="perm-btn" plain round>清空</el-button>
                            <el-button type="primary" @click="handleReissue(item)" class="perm-btn" plain round >重新下发</el-button>
                            <el-button type="primary" @click="permittedList(item)" class="perm-btn" plain round >通关人员</el-button>
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
            :page-sizes="[10, 20, 300, 40]"
            :page-size="page.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="page.total"
          ></el-pagination>
        </div>
        <el-dialog title="设备设置" class="dialog_form" width="800px" :visible.sync="settingVisible">
          <el-form
            :inline="true"
            ref="addForm"
            :model="addForm"
            label-position="left"
            label-width="100px"
          >
            <el-form-item label="园区" prop="parkId">
              <parkSelect
                :defaultSelected="true"
                v-model="settingForm.parkId"
                @doChange="selectPark"
                @defaultHandle="defaultHandle">
              </parkSelect>
            </el-form-item>
            <div class="row" v-for="(item, index) in settingForm.list" :key="index">
              <div class="c1">{{item.deviceName}}</div>
              <div class="c2">
                <el-form-item label="开启体温检测" prop="thermalEnable">
                  <el-switch
                    v-model="item.thermalEnable"
                    active-color="#13ce66"
                    inactive-color="#ff4949"
                    :active-value="1"
                    :inactive-value="0"
                  >
                  </el-switch>
                </el-form-item>
              </div>
              <div class="c3">
                <el-form-item label="体温阈值" prop="thermalThreshold">
                  <el-input v-model="item.thermalThreshold"></el-input>
                </el-form-item>
              </div>
            </div>
            <div v-if="settingForm.list.length===0" class="noDate">
              当前园区下无设备信息。
            </div>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="settingVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="settingSubmit('settingForm')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog title="添加考勤机" class="dialog_form" width="700px" :visible.sync="addFormVisible">
          <el-form
            :rules="addRules"
            :inline="true"
            ref="addForm"
            :model="addForm"
            label-position="top"
          >
            <el-form-item label="考勤机名称" prop="deviceName">
              <el-input v-model="addForm.deviceName" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备厂家" prop="deviceVendor">
              <el-select v-model="addForm.deviceVendor" clearable>
                <el-option label="海康" value="1"></el-option>
                <el-option label="大华" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="卡机类型" prop="deviceCapability">
              <deviceCapabilitySelect v-model="addForm.deviceCapability"></deviceCapabilitySelect>
            </el-form-item>
            <el-form-item label="登录账号" prop="deviceUsername">
              <el-input v-model="addForm.deviceUsername" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备序列号" prop="deviceCode">
              <el-input v-model="addForm.deviceCode" clearable></el-input>
            </el-form-item>
            <el-form-item label="登录密码" prop="devicePassword">
              <el-input type="password" v-model="addForm.devicePassword" clearable></el-input>
            </el-form-item>
            <el-form-item label="考勤机IP" prop="deviceIp">
              <el-input v-model="addForm.deviceIp" clearable></el-input>
            </el-form-item>
            <el-form-item label="区域" prop="areaIds">
              <areaCascader v-model="addForm.areaIds" :changeOnSelect="false" placeholder="区域"></areaCascader>
            </el-form-item>
            <el-form-item label="进出类型" prop="eventType">
              <el-select v-model="addForm.eventType" clearable>
                <el-option label="进" :value="1"></el-option>
                <el-option label="出" :value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="端口" prop="devicePort">
              <el-input v-model="addForm.devicePort" clearable></el-input>
            </el-form-item>
            <el-form-item label="开启体温检测" prop="devicePort">
              <el-switch
                v-model="addForm.value"
                active-color="#13ce66"
                inactive-color="#ff4949">
              </el-switch>
            </el-form-item>
            <el-form-item label="体温阈值" prop="thermalThreshold">
              <el-input v-model="addForm.thermalThreshold" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备标签" prop="tagIds">
              <deviceTagSelect v-model="editForm.tagIds"></deviceTagSelect>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addFormVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog title="编辑考勤机" class="dialog_form" width="700px" :visible.sync="editFormVisible">
          <el-form
            :rules="rules"
            :inline="true"
            ref="editForm"
            :model="editForm"
            label-position="top"
          >
            <el-form-item label="考勤机名称" prop="deviceName">
              <el-input v-model="editForm.deviceName" clearable :disabled="editDisabled"></el-input>
            </el-form-item>
            <el-form-item label="设备厂家" prop="deviceVendor">
              <el-select v-model="editForm.deviceVendor" clearable>
                <el-option label="海康" value="1"></el-option>
                <el-option label="大华" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="卡机类型" prop="deviceCapability">
              <deviceCapabilitySelect v-model="editForm.deviceCapability"></deviceCapabilitySelect>
            </el-form-item>
            <el-form-item v-if="!isIscDevice(editForm)" label="登录账号" prop="deviceUsername">
              <el-input v-model="editForm.deviceUsername" clearable :disabled="editDisabled"></el-input>
            </el-form-item>
            <el-form-item v-if="isIscDevice(editForm)" label="设备ID" prop="id">
              <el-input v-model="editForm.id" disabled></el-input>
            </el-form-item>
            <el-form-item v-if="!isIscDevice(editForm)" label="设备序列号" prop="deviceCode">
              <el-input v-model="editForm.deviceCode" clearable :disabled="editDisabled"></el-input>
            </el-form-item>
            <el-form-item v-if="!isIscDevice(editForm)" label="登录密码" prop="devicePassword">
              <el-input type="password" v-model="editForm.devicePassword" clearable :disabled="editDisabled"></el-input>
            </el-form-item>
            <el-form-item label="考勤机IP" prop="deviceIp">
              <el-input v-model="editForm.deviceIp" clearable :disabled="editDisabled"></el-input>
            </el-form-item>
            <el-form-item label="区域" prop="areaIds">
              <areaCascader v-model="editForm.areaIds" :changeOnSelect="false" placeholder="区域"></areaCascader>
            </el-form-item>
            <el-form-item label="进出类型" prop="eventType">
              <el-select v-model="editForm.eventType" clearable>
                <el-option label="进" :value="1"></el-option>
                <el-option label="出" :value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="端口" prop="devicePort">
              <el-input v-model="editForm.devicePort" clearable :disabled="editDisabled"></el-input>
            </el-form-item>
            <el-form-item label="设备标签" prop="tagIds">
              <deviceTagSelect v-model="editForm.tagIds"></deviceTagSelect>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="editFormVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="editSubmit('editForm')" :loading="editLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
    <setTag ref="setTag" :deviceIds="ckItem" @refresh="refresh"></setTag>

  </div>
</template>

<style lang="scss">
@use "@/styles/platform/device/camera" as *;
</style>

<script>
  import {
    fetchList,
    getObj,
    addObj,
    putObj,
    delObj,
    setDeviceFT,
    getDeviceFT
  } from "@/api/platform/device/camera"
  import { xcGuardApi } from './xc_guard/_service'
  import { mapGetters } from "vuex"
  import setTag from "./xc_guard/components/setTag"
  import deviceTagSelect from "./components/device-tag-select"
  import deviceCapabilitySelect from "./components/device-capability-select"
  export default {
    mixins: [tce.mixins.list],
    name: "alarm",
    components: {
      setTag,
      deviceTagSelect,
      deviceCapabilitySelect
    },
    data() {
      var validateIP = (rule, value, callback) => {
        var parten = /^(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])\.(\d{1,2}|1\d\d|2[0-4]\d|25[0-5])$/;
        if (!parten.test(value)) {
          callback(new Error("IP输入错误"));
        } else {
          callback();
        }
      };
      var validatePort = (rule, value, callback) => {
        var parten = /^(\d)+$/g;
        if (
          !(
            parten.test(value) &&
            parseInt(value) <= 65535 &&
            parseInt(value) >= 0
          )
        ) {
          callback(new Error("端口输入错误"));
        } else {
          callback();
        }
      };
      return {
        settingLoading: false,
        settingForm: {
          list: []
        },
        settingVisible: false,
        addLoading: false,
        editLoading: false,
        addFormVisible: false, //添加考勤机
        editFormVisible: false, //编辑考勤机
        editDisabled: false,
        activeColor: "#10CC8F",
        inactiveColor: "#C6CAD3",
        ckItem: [],
        page: {
          total: null, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 20 // 每页显示多少条
        },
        searchForm: {
          parkId: "",
          enableStatus: "",
          connectStatus: "",
          areaIdArray: [],
          deviceType: 1,
          deviceName: '',
          tagsArray: [],
          deviceTag: 1
        },
        addForm: {
          channelNo: "",
          deviceName: "",
          deviceCode: "",
          channelManager: "",
          deviceIp: "",
          devicePort: "",
          protocolType: "",
          deviceUsername: "",
          devicePassword: "",
          areaIds: [],
          deviceType: 1,
          isSync: 0,
          deviceCapability: "",
          deviceVendor: "",
          eventType: "",
          deviceTag: "1"
        },
        editForm: {
          deviceName: "",
          deviceCode: "",
          deviceIp: "",
          devicePort: "",
          protocolType: "",
          deviceUsername: "",
          devicePassword: "",
          areaIds: [],
          deviceType: 1,
          deviceCapability: "",
          deviceVendor: "",
          eventType: "",
          deviceTag: "1"
        },
        addRules: {
          deviceName: [
            { required: true, message: "请输入考勤机名称", trigger: "blur" }
          ],
          deviceIp: [{ required: true, validator: validateIP, trigger: "blur" }],
          areaIds: [{ required: true, message: "请选择园区", trigger: "change" }],
          devicePort: [
            { required: true, validator: validatePort, trigger: "blur" }
          ],
          deviceVendor: [
            { required: true, message: "请选择设备厂家", trigger: "change" }
          ],
          eventType: [
            { required: true, message: "请选择出入类型", trigger: "change" }
          ],
          deviceCapability: [
            { required: true, message: "请选择卡机类型", trigger: "change" }
          ],
          deviceUsername: [
            { required: true, message: "请输入登录账号", trigger: "blur" }
          ],
          devicePassword: [
            { required: true, message: "请输入登录密码", trigger: "blur" }
          ],
          deviceCode: [
            { required: true, message: "请输入设备序列号", trigger: "blur" }
          ]
        },
        rules: {},
        rules1: {
          deviceName: [
            { required: true, message: "请输入考勤机名称", trigger: "blur" }
          ],
          deviceIp: [{ required: true, validator: validateIP, trigger: "blur" }],
          areaIds: [{ required: true, message: "请选择园区", trigger: "change" }],
          devicePort: [
            { required: true, validator: validatePort, trigger: "blur" }
          ],
          deviceVendor: [
            { required: true, message: "请选择设备厂家", trigger: "change" }
          ],
          eventType: [
            { required: true, message: "请选择出入类型", trigger: "change" }
          ],
          deviceCapability: [
            { required: true, message: "请选择卡机类型", trigger: "change" }
          ]
        },
        rules2: {
          deviceUsername: [
            { required: true, message: "请输入登录账号", trigger: "blur" }
          ],
          devicePassword: [
            { required: true, message: "请输入登录密码", trigger: "blur" }
          ],
          deviceCode: [
            { required: true, message: "请输入设备序列号", trigger: "blur" }
          ]
        },
        datalist: []
      };
    },
    created() {
      this.$nextTick(() => {
        if (this.$route.query.queryForm != undefined) {
          let queryPage = this.$route.query.queryPage;
          let queryForm = this.$route.query.queryForm;
          if (queryPage && queryPage.constructor === Object) {
            this.page = Object.assign(queryPage, {});
          }
          if (queryForm && queryForm.constructor === Object) {
            this.searchForm = Object.assign(queryForm, {});
          }
        }
        this.getList(this.page, this.searchForm);
      });
    },
    mounted: function() {},
    computed: {
      ...mapGetters(["permissions"]),
      hasData: function() {
        if (this.datalist) {
          if (this.datalist.length > 0) {
            return true;
          } else {
            return false;
          }
        } else {
          return false;
        }
      }
    },
    methods: {
      isIscDevice(device) {
        return Number(device && device.isSync) === 1
      },
      deviceCapabilityText(deviceCapability) {
        const option = {
          1: '刷脸机',
          2: '刷卡机',
          3: '刷脸+刷卡'
        }
        return option[deviceCapability] || '-'
      },
      handelSetTag(){
        if(this.ckItem && this.ckItem.length>0){
          this.$refs.setTag && this.$refs.setTag.open()
        }else{
          this.$message.error('请先勾选要设置的设备')
        }
      },
      refresh(){
        this.ckItem = []
        this.getList(this.page, this.searchForm)
      },
      //一键清空
      async handleClear(row){
        await this.mixinMsgDel('确认要清空该设备相关的授权信息？')
        const res = await xcGuardApi.authClear(row.id)
        if(res.data.code===0){
          this.refresh()
          this.$notify({
            title: "操作成功",
            message: "已加入清空队列中",
            type: "success",
            duration: 2000
          });
        }else{
          this.$message.error(res.data.message);
        }
      },
      //重新下发
      handleReissue(item){
        const _this = this
        const elm = this.$createElement;
        this.$msgbox({
          message: elm("p", { attrs: { class: "smallp" } }, [
            elm("div", null, [
              elm("p", null, "是否将所有人脸记录重新下发？"),
            ])
          ]),
          showCancelButton: true,
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          customClass: "small_dialog",
          center: true
        }).then(function() {
          _this.pageLoading = _this.$loading({
            lock: true,
            text: "正在执行，请稍后…",
            spinner: "el-icon-loading",
            background: "rgba(0, 0, 0, 0.7)",
            customClass: "bigdata-loading"
          });
          return xcGuardApi.equipReissue(item.id);
        })
          .then(data => {
            _this.pageLoading.close();
            _this.getList(this.page, this.searchForm)
            if (data.data.code == 0) {
              _this.$message({
                message: '重新下发成功',
                type: 'success'
              });
            }
          })
          .catch((e) => {
            _this.pageLoading.close();
          });
      },
      selectPark(parkId){
        this.getDeviceFT(parkId)
      },
      defaultHandle(e){
        this.settingForm.parkId = e.value
        this.getDeviceFT(e.value)
      },
      async getDeviceFT(parkId){
        const res = await getDeviceFT(parkId)
        this.settingForm.list = res.data.data
      },
      getList(page, params) {
        this.tableLoading = true;
        fetchList(
          Object.assign(
            {
              descs: "create_time",
              current: page.currentPage,
              size: page.pageSize
            },
            params
          )
        ).then(response => {
          this.datalist = response.data.data.records;
          this.datalist.forEach(el=>{
            if(el.deviceTagList && el.deviceTagList.length>0){
              let arr = []
              el.deviceTagList.forEach(el2=>{
                arr.push(el2.tagName)
              })
              el.tagNames = arr.toString()
            }
          })
          this.page.total = response.data.data.total;
          this.tableLoading = false;
        });
      },
      updateStatus(item) {
        putObj(item);
      },
      /**
       * 设备设置
       */
      settingSubmit(formName) {
        // this.$refs[formName].validate(valid => {
        //   if (valid) {
        this.settingLoading = true;
        setDeviceFT(this.settingForm.list).then(dataResponse => {
          this.settingVisible = false;
          this.settingLoading = false;
        });
        //   } else {
        //     console.log("error submit!!");
        //     return false;
        //   }
        // });
      },
      /**
       * 顶部搜索
       */
      searchSubmit(form) {
        this.getList(this.page, form);
      },
      /**
       * 清空搜索
       */
      resetFrom(formName, form) {
        if (this.$refs[formName] != undefined) {
          this.$refs[formName].resetFields();
        }
        this.getList(this.page, form);
      },
      /**
       * 添加考勤机确定
       */
      addSubmit(formName) {
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.addLoading = true;
            addObj(this.addForm).then(dataResponse => {
              this.addFormVisible = false;
              this.addLoading = false;
              this.getList(this.page, this.searchForm);
            });
          } else {
            return false;
          }
        });
      },
      /**
       * 编辑考勤机打开
       */
      handleEdit(item) {
        this.editForm.isSync = item.isSync
        if (item.isSync !== 1) {
          this.rules = Object.assign({}, this.rules1, this.rules2)
          this.editDisabled = false
        } else {
          this.rules = Object.assign({}, this.rules1)
          this.editDisabled = true
        }
        getObj(item.id).then(response => {
          this.editForm = response.data.data;
          this.editForm.areaIds = [
            response.data.data.parkId,
            response.data.data.pid,
            response.data.data.areaId
          ];
          this.editForm.deviceVendor = response.data.data.deviceVendor + "";
          if(!this.editForm.tagIds){
            this.editForm.tagIds = []
          }
        });
        this.editFormVisible = true;
      },
      /**
       * 编辑考勤机确定
       */
      editSubmit(formName) {
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.editLoading = true;
            putObj(this.editForm).then(dataResponse => {
              this.editFormVisible = false;
              this.editLoading = false;
              this.getList(this.page, this.searchForm);
            });
          } else {
            return false;
          }
        });
      },
      handleDel: function(row) {
        var _this = this;
        const elm = this.$createElement;
        this.$msgbox({
          message: elm("p", { attrs: { class: "smallp" } }, [
            elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
            elm("span", null, "确认删除该考勤机信息？ ")
          ]),
          showCancelButton: true,
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          customClass: "small_dialog",
          center: true
        })
          .then(function() {
            return delObj(row.id);
          })
          .then(dataResponse => {
            _this.getList(_this.page, _this.searchForm);
            _this.$notify({
              title: "删除成功",
              message: "删除成功",
              type: "success",
              duration: 2000
            });
          })
          .catch(error => { console.error(error) });
      },
      /**
       * 通关人员
       */
      permittedList(item) {
        const src = `/platform/device/person_list/${item.id}`;
        this.$router.push({
          path: src,
          query: {
            queryPage: this.page,
            queryForm: this.searchForm,
            serialNo: item.serialNo,
            deviceType: 3
          }
        });
      },
      /**
       * 更新设备权限
       */
      editLimit() {
        const src = `/platform/device/gate_limit`;
        this.$router.push({
          path: src,
          query: {
            deviceType: 3
          }
        });
      },
      /**
       * 搜索回调
       */
      searchChange(form) {
        this.getList(this.page, form);
      },
      handleSizeChange(val) {
        this.page.currentPage = 1;
        this.page.pageSize = val;
        this.getList(this.page, this.searchForm);
      },
      handleCurrentChange(val) {
        this.page.currentPage = val;
        this.getList(this.page, this.searchForm);
      }
    }
  };
</script>

<style lang="scss" scoped>
  .device .dialog_form .el-dialog__body .row .el-form-item {
    width: 100%;
  }
  .noDate{
    text-align: center;
    padding: 50px 0;
    color: #999;
  }
  .row ::v-deep {
    display: flex;
    .el-form-item{
      display: flex;
      align-items: center;
    }
    .el-form--label-left .el-form-item__label{
      text-align: right;
    }
    .el-switch{
      margin: -9px 0 0 0;
    }
    .c1{
      flex: 1;
      padding-top: 7px;
    }
    .c2{
      width: 250px;
    }
    .c3{
      width: 300px;
      .el-input{
        width: 80px;
        padding: 0;
      }
    }
  }
</style>
