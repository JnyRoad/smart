<!--设备管理，摄像头管理  -->
<template>
  <div class="my-basic-container device">
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
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加摄像头</el-button>
          </div>
        </div>
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="选择园区" prop="areaIdArray">
              <el-cascader
                :options="options"
                :change-on-select="true"
                v-model="searchForm.areaIdArray"
                clearable
              ></el-cascader>
            </el-form-item>
            <el-form-item label="接通状态" prop="connectStatus">
              <el-select v-model="searchForm.connectStatus" placeholder="请选择接通状态" clearable>
                <el-option label="未连线" value="0"></el-option>
                <el-option label="离线" value="1"></el-option>
                <el-option label="在线" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="启用状态" prop="enableStatus">
              <el-select v-model="searchForm.enableStatus" placeholder="请选择启用状态" clearable>
                <el-option label="启用" value="1"></el-option>
                <el-option label="禁用" value="2"></el-option>
              </el-select>
            </el-form-item>
          </el-form>
        </div>
        <div class="device-list">
          <template v-for="(item, key, index) in datalist ">
            <div class="device-outer" :key="index">
              <dl class="device-item clear">
                <div class="device-status" :class="item.connectStatus | deviceStatusClassFormat">
                  <span>{{item.connectStatus | deviceStatusFormat}}</span>
                </div>
                <dt></dt>
                <dd>
                  <table class="info-tbl">
                    <tr>
                      <td>通道名称</td>
                      <td>{{item.deviceName}}</td>
                    </tr>
                    <tr>
                      <td>所属区域</td>
                      <td>{{item.areaName}}</td>
                    </tr>
                    <tr class="tr3">
                      <td>启停状态</td>
                      <td>
                        <el-switch
                          @change="updateStatus(item)"
                          v-model="item.enableStatus"
                          :active-color="activeColor"
                          :inactive-color="inactiveColor"
                          :active-value="1"
                          :inactive-value="2"
                        ></el-switch>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <div class="circle-btns">
                          <el-button
                            type="info"
                            @click="handleEdit(item)"
                            icon="el-icon-edit"
                            class="edit-btn"
                          ></el-button>
                          <el-button
                            type="info"
                            @click="handleDel(item)"
                            icon="el-icon-delete"
                            class="del-btn"
                          ></el-button>
                        </div>
                      </td>
                      <td>&nbsp;</td>
                    </tr>
                  </table>
                </dd>
              </dl>
            </div>
          </template>
        </div>
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
        <el-dialog title="添加摄像头" class="dialog_form" width="700px" :visible.sync="addFormVisible">
          <el-form
            :rules="rules"
            :inline="true"
            ref="addForm"
            :model="addForm"
            label-position="top"
          >
            <el-form-item label="通道" prop="channelNo">
              <el-input v-model="addForm.channelNo" clearable></el-input>
            </el-form-item>
            <el-form-item label="通道名称" prop="deviceName">
              <el-input v-model="addForm.deviceName" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备厂家" prop="deviceVendor">
              <el-select v-model="addForm.deviceVendor" clearable>
                <el-option label="海康" value="1"></el-option>
                <el-option label="大华" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="通道管理号" prop="channelManager">
              <el-input v-model="addForm.channelManager" clearable></el-input>
            </el-form-item>
            <el-form-item label="IP地址" prop="deviceIp">
              <el-input v-model="addForm.deviceIp" clearable></el-input>
            </el-form-item>
            <el-form-item label="管理端口" prop="devicePort">
              <el-input v-model="addForm.devicePort" clearable></el-input>
            </el-form-item>
            <el-form-item label="协议类型" prop="protocolType">
              <el-input v-model="addForm.protocolType" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备账号" prop="deviceUsername">
              <el-input v-model="addForm.deviceUsername" clearable></el-input>
            </el-form-item>
            <el-form-item label="登录密码" prop="devicePassword">
              <el-input type="password" v-model="addForm.devicePassword" clearable></el-input>
            </el-form-item>
            <el-form-item label="所属区域" prop="areaIds">
              <el-cascader :options="options" v-model="addForm.areaIds"></el-cascader>
              <!-- <el-select v-model="addForm.areaId"  clearable>
                <el-option label="区域一" value="1"></el-option>
                <el-option label="区域二" value="2"></el-option>
              </el-select>-->
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addFormVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addForm')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog title="编辑摄像头" class="dialog_form" width="700px" :visible.sync="editFormVisible">
          <el-form
            :rules="rules"
            :inline="true"
            ref="editForm"
            :model="editForm"
            label-position="top"
          >
            <el-form-item label="通道" prop="channelNo">
              <el-input v-model="editForm.channelNo" clearable></el-input>
            </el-form-item>
            <el-form-item label="通道名称" prop="deviceName">
              <el-input v-model="editForm.deviceName" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备厂家" prop="deviceVendor">
              <el-select v-model="editForm.deviceVendor" clearable>
                <el-option label="海康" value="1"></el-option>
                <el-option label="大华" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="通道管理号" prop="channelManager">
              <el-input v-model="editForm.channelManager" clearable></el-input>
            </el-form-item>
            <el-form-item label="IP地址" prop="deviceIp">
              <el-input v-model="editForm.deviceIp" clearable></el-input>
            </el-form-item>
            <el-form-item label="管理端口" prop="devicePort">
              <el-input v-model="editForm.devicePort" clearable></el-input>
            </el-form-item>
            <el-form-item label="协议类型" prop="protocolType">
              <el-input v-model="editForm.protocolType" clearable></el-input>
            </el-form-item>
            <el-form-item label="设备账号" prop="deviceUsername">
              <el-input v-model="editForm.deviceUsername" clearable></el-input>
            </el-form-item>
            <el-form-item label="登录密码" prop="devicePassword">
              <el-input type="password" v-model="editForm.devicePassword" clearable></el-input>
            </el-form-item>
            <el-form-item label="所属区域" prop="areaIds">
              <el-cascader :options="options" v-model="editForm.areaIds"></el-cascader>
              <!-- <el-select v-model="editForm.areaId"  clearable>
                <el-option label="区域一" value="1"></el-option>
                <el-option label="区域二" value="2"></el-option>
              </el-select>-->
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="editFormVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="editSubmit('editForm')" :loading="editLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
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
  tree
} from "@/api/platform/device/camera";
import { mapGetters } from "vuex";

export default {
  name: "alarm",
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
      addFormVisible: false, //添加摄像头
      editFormVisible: false, //编辑摄像头
      activeColor: "#10CC8F",
      inactiveColor: "#C6CAD3",
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
        deviceType: 4
      },
      addForm: {
        channelNo: "",
        deviceName: "",
        channelManager: "",
        deviceIp: "",
        devicePort: "",
        protocolType: "",
        deviceUsername: "",
        devicePassword: "",
        areaIds: [],
        deviceType: 4,
        deviceVendor: ""
      },
      editForm: {
        channelNo: "",
        deviceName: "",
        channelManager: "",
        deviceIp: "",
        devicePort: "",
        protocolType: "",
        deviceUsername: "",
        devicePassword: "",
        areaIds: [],
        deviceType: 4,
        deviceVendor: ""
      },
      options: [],
      rules: {
        channelNo: [
          { required: true, message: "请输入通道号", trigger: "blur" }
        ],
        deviceName: [
          { required: true, message: "请输入道闸名称", trigger: "blur" }
        ],
        deviceUsername: [
          { required: true, message: "请输入登录账号", trigger: "blur" }
        ],
        channelManager: [
          { required: true, message: "请输入通道管理号", trigger: "blur" }
        ],
        protocolType: [
          { required: true, message: "请输入协议类型", trigger: "blur" }
        ],
        devicePassword: [
          { required: true, message: "请输入登录密码", trigger: "blur" }
        ],
        deviceIp: [{ required: true, validator: validateIP, trigger: "blur" }],
        areaIds: [{ required: true, message: "请选择园区", trigger: "change" }],
        devicePort: [
          { required: true, validator: validatePort, trigger: "blur" }
        ],
        deviceVendor: [
          { required: true, message: "请选择设备厂家", trigger: "change" }
        ]
      },
      datalist: []
    };
  },
  created() {
    this.getList(this.page, this.searchForm);
    tree().then(response => {
      this.options = response.data.data;
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
     * 添加摄像头确定
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
     * 编辑摄像头打开
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
      });
      this.editFormVisible = true;
    },
    /**
     * 编辑摄像头确定
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
          elm("span", null, "确认删除该摄像头信息？ ")
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
    handleSizeChange(val) {
    },
    handleCurrentChange(val) {
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
