<!--设备管理，门禁管理  -->
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
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加门禁</el-button>
            <el-button type="primary" @click="handelSetTag" plain>设置标签</el-button>
          </div>
        </div>
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="所在区域" prop="areaIdArray">
              <areaCascader v-model="searchForm.areaIdArray" :changeOnSelect="true" placeholder="所在区域"></areaCascader>
            </el-form-item>
            <!-- <el-form-item label="选择厂家">
              <el-select v-model="searchForm.deviceVendor" placeholder="请选择厂家">
                <el-option label="海康" value="1"></el-option>
                <el-option label="大华" value="2"></el-option>
              </el-select>
            </el-form-item>-->
            <el-form-item label="接通状态" prop="connectStatus">
              <el-select v-model="searchForm.connectStatus" placeholder="请选择接通状态" clearable>
                <el-option label="未连线" value="0"></el-option>
                <el-option label="离线" value="1"></el-option>
                <el-option label="在线" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="启用状态" prop="enableStatus">
              <el-select v-model="searchForm.enableStatus" placeholder="请选择区域状态" clearable>
                <el-option label="启用" value="1"></el-option>
                <el-option label="禁用" value="2"></el-option>
              </el-select>
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
                        <div class="tbl_cell tbl_cell_label">启停状态</div>
                        <div class="tbl_cell">
                          <el-switch
                            v-model="item.enableStatus"
                            :active-color="activeColor"
                            :inactive-color="inactiveColor"
                            :active-value="1"
                            :inactive-value="2"
                          ></el-switch>
                        </div>
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
        <el-dialog title="添加门禁" class="dialog_form" width="700px" :visible.sync="addFormVisible">
          <el-form
            :rules="rules"
            :inline="true"
            ref="addForm"
            :model="addForm"
            label-position="top"
          >
            <el-form-item label="门禁名称" prop="deviceName">
              <el-input v-model="addForm.deviceName" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备厂家" prop="deviceVendor">
              <el-select v-model="addForm.deviceVendor" clearable>
                <el-option label="海康" value="1"></el-option>
                <el-option label="大华" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="门禁账号" prop="deviceUsername">
              <el-input v-model="addForm.deviceUsername" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备序列号" prop="deviceCode">
              <el-input v-model="addForm.deviceCode" clearable></el-input>
            </el-form-item>
            <el-form-item label="门禁密码" prop="devicePassword">
              <el-input type="password" v-model="addForm.devicePassword" clearable></el-input>
            </el-form-item>
            <el-form-item label="门禁IP" prop="deviceIp">
              <el-input v-model="addForm.deviceIp" clearable></el-input>
            </el-form-item>
            <el-form-item label="区域" prop="areaIds">
              <areaCascader v-model="addForm.areaIds" :changeOnSelect="false" placeholder="区域"></areaCascader>
            </el-form-item>
            <el-form-item label="门禁端口" prop="devicePort">
              <el-input v-model="addForm.devicePort" clearable></el-input>
            </el-form-item>
            <el-form-item label="进出类型" prop="eventType">
              <el-select v-model="addForm.eventType" clearable>
                <el-option label="进" :value="1"></el-option>
                <el-option label="出" :value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="设备标签" prop="tagIds">
              <deviceTagSelect v-model="addForm.tagIds"></deviceTagSelect>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addFormVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog title="编辑门禁" class="dialog_form" width="700px" :visible.sync="editFormVisible">
          <el-form
            :rules="rules"
            :inline="true"
            ref="editForm"
            :model="editForm"
            label-position="top"
          >
            <el-form-item label="门禁名称" prop="deviceName">
              <el-input v-model="editForm.deviceName" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备厂家" prop="deviceVendor">
              <el-select v-model="editForm.deviceVendor" clearable>
                <el-option label="海康" value="1"></el-option>
                <el-option label="大华" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="门禁账号" prop="deviceUsername">
              <el-input v-model="editForm.deviceUsername" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备序列号" prop="deviceCode">
              <el-input v-model="editForm.deviceCode" clearable></el-input>
            </el-form-item>
            <el-form-item label="门禁密码" prop="devicePassword">
              <el-input type="password" v-model="editForm.devicePassword" clearable></el-input>
            </el-form-item>
            <el-form-item label="门禁IP" prop="deviceIp">
              <el-input v-model="editForm.deviceIp" clearable></el-input>
            </el-form-item>
            <el-form-item label="区域" prop="areaIds">
              <areaCascader v-model="editForm.areaIds" :changeOnSelect="false" placeholder="区域"></areaCascader>
            </el-form-item>
            <el-form-item label="门禁端口" prop="devicePort">
              <el-input v-model="editForm.devicePort" clearable></el-input>
            </el-form-item>
            <el-form-item label="进出类型" prop="eventType">
              <el-select v-model="editForm.eventType" clearable>
                <el-option label="进" :value="1"></el-option>
                <el-option label="出" :value="2"></el-option>
              </el-select>
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
  delObj
} from "@/api/platform/device/camera"
import { xcGuardApi } from './_service'
import { mapGetters } from "vuex";
import setTag from "./components/setTag"
import deviceTagSelect from "../components/device-tag-select"

export default {
  mixins: [tce.mixins.list],
  name: "alarm",
  components: {
    setTag,
    deviceTagSelect
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
      addLoading: false,
      editLoading: false,
      addFormVisible: false, //添加门禁
      editFormVisible: false, //编辑门禁
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
        deviceType: 2
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
        deviceType: 2,
        deviceVendor: "",
        eventType: "",
        tagIds: []
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
        deviceType: 2,
        deviceVendor: "",
        eventType: "",
        tagIds: []
      },
      rules: {
        deviceName: [
          { required: true, message: "请输入道闸名称", trigger: "blur" }
        ],
        deviceUsername: [
          { required: true, message: "请输入登录账号", trigger: "blur" }
        ],
        deviceCode: [
          { required: true, message: "请输入设备序列号", trigger: "blur" }
        ],
        devicePassword: [
          { required: true, message: "请输入登录密码", trigger: "blur" }
        ],
        deviceIp: [{ required: true, validator: validateIP, trigger: "blur" }],
        areaIds: [{ required: true, message: "请选择园区", trigger: "change" }],
        devicePort: [
          { required: true, validator: validatePort, trigger: "blur" }
        ],
        eventType: [
          { required: true, message: "请选择出入类型", trigger: "change" }
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
     * 添加门禁确定
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
     * 编辑门禁打开
     */
    handleEdit(item) {
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
     * 编辑门禁确定
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
      // 删除前先查询该设备的注销影响预览（关联的权限组、受影响人数/车辆数等）
      xcGuardApi.getDecommissionPlan(row.id).then(response => {
        const plan = response.data.data || { affectedAuthorities: [] };
        _this.confirmDecommission(row, plan);
      }).catch(error => {
        console.error(error);
        _this.$message.error("查询设备关联权限组失败，请稍后重试");
      });
    },
    /**
     * 根据注销影响预览渲染确认弹窗，确认后再真正执行删除
     */
    confirmDecommission(row, plan) {
      var _this = this;
      const elm = this.$createElement;
      const affected = plan.affectedAuthorities || [];
      const summaryChildren = [
        elm("p", null, "确认删除该门禁信息？"),
      ];
      if (affected.length === 0) {
        summaryChildren.push(elm("p", { attrs: { class: "smallInfo" } }, "该设备当前未绑定任何权限组。"));
      } else {
        summaryChildren.push(elm("p", { attrs: { class: "smallInfo" } }, `该设备绑定在以下 ${affected.length} 个权限组下：`));
        const listItems = affected.map(item => {
          const parts = [`${item.authorityName}（影响 ${item.staffCount} 名员工 / ${item.vehicleCount} 辆车）`];
          if (item.willCascadeDelete) {
            parts.push("—— 权限组将因变空被自动删除");
          } else if (item.protectedAuthority) {
            parts.push("—— 区域默认/系统内置权限组，仅解绑设备，权限组保留为空壳，请自行检查配置");
          }
          return elm("li", null, parts.join(" "));
        });
        summaryChildren.push(elm("ul", { attrs: { class: "smallInfo" } }, listItems));
      }
      this.$msgbox({
        message: elm("div", { attrs: { class: "smallp" } }, summaryChildren),
        showCancelButton: true,
        confirmButtonText: "确定删除",
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
          deviceType: 2
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
    },
    handleCurrentChange(val) {
    }
  }
};
</script>

<style lang="scss" scoped></style>
