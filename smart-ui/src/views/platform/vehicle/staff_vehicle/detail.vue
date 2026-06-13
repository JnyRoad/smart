<!--车辆管理：员工车辆详情 -->
<template>
  <div class="my-basic-container vehicle">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-form
          ref="editform"
          :rules="editRules"
          :model="deviceInfo"
          size="mini"
          label-width="80px"
        >
          <p class="box-orange">车辆信息</p>
          <el-row>
            <el-col :span="5">
              <dl class="img-info">
                <el-form-item label prop="drivinglLicenseId" label-width="0">
                  <dt class="img-outer">
                    <i class="corner cn-top"></i>
                    <i class="corner cn-rit"></i>
                    <i class="corner cn-btm"></i>
                    <i class="corner cn-lft"></i>
                    <div class="img-inner">
                      <el-upload
                        v-if="isEdit"
                        :disabled="!isEdit"
                        class="avatar-uploader"
                        action
                        :before-upload="drivinglLicenseImgrUpload"
                        :show-file-list="false"
                      >
                        <template v-if="deviceInfo.drivinglLicenseId">
                          <img
                            :src="deviceInfo.drivinglLicenseId || errorImgDriving()"
                            :onerror="errorImgDriving()"
                            class="avatar"
                          />
                        </template>
                        <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                      </el-upload>
                      <viewer v-else>
                        <img :src="deviceInfo.drivinglLicenseId || errorImgDriving()" :onerror="errorImgDriving()" class="avatar" />
                      </viewer>
                    </div>
                  </dt>
                  <dd class="desc-info">行驶证</dd>
                </el-form-item>
              </dl>
            </el-col>
            <el-col :span="5">
              <dl class="img-info">
                <el-form-item label prop="driverLicenseId" label-width="0">
                  <dt class="img-outer">
                    <i class="corner cn-top"></i>
                    <i class="corner cn-rit"></i>
                    <i class="corner cn-btm"></i>
                    <i class="corner cn-lft"></i>
                    <div class="img-inner">
                      <el-upload
                        v-if="isEdit"
                        :disabled="!isEdit"
                        class="avatar-uploader"
                        action
                        :before-upload="driverLicenseImgUpload"
                        :show-file-list="false"
                      >
                        <template v-if="deviceInfo.driverLicenseId">
                          <img
                            :src="deviceInfo.driverLicenseId || errorImgDriver()"
                            :onerror="errorImgDriver()"
                            class="avatar"
                          />
                        </template>
                        <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                      </el-upload>
                      <viewer v-else>
                        <img :src="deviceInfo.driverLicenseId || errorImgDriver()" :onerror="errorImgDriver()" class="avatar" />
                      </viewer>
                    </div>
                  </dt>
                  <dd class="desc-info">驾驶证</dd>
                </el-form-item>
              </dl>
            </el-col>
            <el-col :span="14">
              <table class="lit-table" v-if="!isEdit">
                <tr>
                  <td>所属园区</td>
                  <td>
                    <el-select
                      v-model="curParkId"
                      placeholder="所属园区" >
                      <el-option
                        v-for="item in parkData"
                        :key="item.id"
                        :label="item.parkName"
                        :value="item.id"
                      ></el-option>
                    </el-select>
                  </td>
                </tr>
                <tr>
                  <td>车牌号</td>
                  <td>{{deviceInfo.vehiclePlate}}</td>
                </tr>
                <tr>
                  <td>车类型</td>
                  <td>{{deviceInfo.vehicleTypeName}}</td>
                </tr>
                <tr>
                  <td>车品牌</td>
                  <td>{{deviceInfo.vehicleBrand}}</td>
                </tr>
                <tr>
                  <td>车颜色</td>
                  <td>{{deviceInfo.vehicleColorName}}</td>
                </tr>
                <!-- 临时人员信息 -->
                <template v-if="pageType==3">
                  <tr>
                    <td>对口人员</td>
                    <td>{{deviceInfo.name}}</td>
                  </tr>
                  <tr>
                    <td>电话</td>
                    <td>{{deviceInfo.phone}}</td>
                  </tr>
                </template>
                <tr>
                  <td>车辆权限</td>
                  <td>
                    {{curAuthName}}
                  </td>
                </tr>
                <tr>
                  <td>授权设备</td>
                  <td>
                    <el-button type="text" @click="deviceVisible = true">查看授权设备</el-button>
                  </td>
                </tr>
                <tr v-if="pageType==3">
                  <td>备注</td>
                  <td>{{deviceInfo.remark}}</td>
                </tr>
              </table>
              <template v-else>
                <div class="form-outer">
                  <el-form-item label="所属园区" prop="parkId">
                    <el-select
                      v-model="curParkId"
                      placeholder="所属园区" >
                      <el-option
                        v-for="item in parkData"
                        :key="item.id"
                        :label="item.parkName"
                        :value="item.id"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="车牌号" prop="vehiclePlate">
                    <el-input v-model="deviceInfo.vehiclePlate" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="车类型" prop="vehicleType">
                    <el-select v-model="deviceInfo.vehicleType" placeholder="请选择">
                      <el-option label="大型车" :value="1"></el-option>
                      <el-option label="小型车" :value="2"></el-option>
                      <el-option label="其他车" :value="0"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="车品牌" prop="vehicleBrand">
                    <el-input v-model="deviceInfo.vehicleBrand"></el-input>
                  </el-form-item>
                  <el-form-item label="车颜色" prop="vehicleColor">
                    <el-select v-model="deviceInfo.vehicleColor" placeholder="请选择">
                      <el-option label="白" :value="1"></el-option>
                      <el-option label="银" :value="2"></el-option>
                      <el-option label="灰" :value="3"></el-option>
                      <el-option label="黑" :value="4"></el-option>
                      <el-option label="红" :value="5"></el-option>
                      <el-option label="深蓝" :value="6"></el-option>
                      <el-option label="蓝" :value="7"></el-option>
                      <el-option label="黄" :value="8"></el-option>
                      <el-option label="绿" :value="9"></el-option>
                      <el-option label="棕" :value="10"></el-option>
                      <el-option label="粉" :value="11"></el-option>
                      <el-option label="紫" :value="12"></el-option>
                      <el-option label="深灰" :value="13"></el-option>
                      <el-option label="青色" :value="14"></el-option>
                      <el-option label="其他" :value="0"></el-option>
                    </el-select>
                  </el-form-item>
                  <!-- 临时人员信息 -->
                  <template v-if="pageType==3">
                    <el-form-item label="对口人员" prop="name">
                      <el-input v-model="deviceInfo.name" clearable></el-input>
                    </el-form-item>
                    <el-form-item label="电话" prop="phone">
                      <el-input v-model="deviceInfo.phone" clearable></el-input>
                    </el-form-item>
                  </template>
                  <el-form-item label="车辆权限">
                    <el-select
                      v-model="curAuthId"
                      placeholder="车辆权限"
                    >
                      <el-option
                        v-for="item in authorityData"
                        :key="item.id"
                        :label="item.authorityName"
                        :value="item.id"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="授权设备">
                    <el-button type="text" @click="deviceVisible = true">查看授权设备</el-button>
                  </el-form-item>
                  <!-- 临时人员信息 -->
                  <el-form-item v-if="pageType==3" label="备注" prop="remark">
                    <el-input type="textarea" v-model="deviceInfo.remark" clearable></el-input>
                  </el-form-item>
                </div>
              </template>
            </el-col>
          </el-row>
          <!-- 临时人员不显示下方信息 -->
          <template v-if="pageType!=3">
            <div class="gray-line"></div>
            <p class="box-orange">人员信息</p>
            <el-row>
              <el-col :span="5">
                <dl class="img-info">
                  <dt class="img-outer">
                    <i class="corner cn-top"></i>
                    <i class="corner cn-rit"></i>
                    <i class="corner cn-btm"></i>
                    <i class="corner cn-lft"></i>
                    <div class="img-inner">
                      <!-- <el-upload
                        :disabled="!isEdit"
                        class="avatar-uploader"
                        action=""
                      :show-file-list="false">-->
                      <!-- <template v-if="deviceInfo.facePicId"> -->
                        <viewer>
                          <img :src="deviceInfo.facePicId || errorImgPeaple()" :onerror="errorImgPeaple()" class="avatar" />
                        </viewer>
                      <!-- </template>
                      <i v-else class="avatar-uploader-icon">
                        <span></span>
                      </i> -->
                      <!-- </el-upload> -->
                    </div>
                  </dt>
                  <dd class="desc-info">无需选择，由员工信息关联</dd>
                </dl>
              </el-col>
              <el-col :span="19">
                <table class="lit-table" v-if="!isEdit">
                  <tr>
                    <td>所属部门</td>
                    <td>{{deviceInfo.depName}}</td>
                  </tr>
                  <tr>
                    <td>对接人员</td>
                    <td>{{deviceInfo.name}}</td>
                  </tr>
                  <tr>
                    <td>身份证号</td>
                    <td>{{deviceInfo.certno}}</td>
                  </tr>
                  <tr>
                    <td>性别</td>
                    <td>{{deviceInfo.sex}}</td>
                  </tr>
                  <tr>
                    <td>手机号</td>
                    <td>{{deviceInfo.phone}}</td>
                  </tr>
                </table>
                <template v-else>
                  <div class="form-outer">
                    <el-form-item label="工号" prop="badge">
                      <el-input placeholder="请输入工号" v-model="deviceInfo.badge">
                        <el-button
                          type="info"
                          slot="append"
                          @click="selectStaffDetail('editform')"
                          class="noPading"
                          icon="el-icon-search"
                        ></el-button>
                      </el-input>
                    </el-form-item>
                    <el-form-item label="姓名" prop="name">
                      <el-input placeholder v-model="deviceInfo.name" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="所属BU" prop="compId">
                      <el-input placeholder v-model="deviceInfo.compName" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="所属部门" prop="depId">
                      <el-input placeholder v-model="deviceInfo.depName" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="身份证号" prop="certno">
                      <el-input v-model="deviceInfo.certno" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="性别" prop="sex">
                      <el-input v-model="deviceInfo.sex" disabled></el-input>
                    </el-form-item>
                    <el-form-item label="手机" prop="phone">
                      <el-input v-model="deviceInfo.phone" disabled></el-input>
                    </el-form-item>
                  </div>
                </template>
              </el-col>
            </el-row>
          </template>
          <div class="btns-bottom">
            <template v-if="!isEdit">
              <el-button type="primary" @click="editInfo">编辑</el-button>
            </template>
            <template v-else>
              <el-button type="primary" @click="saveInfor('editform')">保存</el-button>
              <el-button type="primary" @click="saveCancel('editform')" plain>取消</el-button>
            </template>
          </div>
        </el-form>
      </section>
    </el-scrollbar>
    <el-dialog
      title="授权设备"
      class="dialog_form"
      width="500px"
      :visible.sync="deviceVisible"
    >
      <div style="padding-bottom: 30px;">
        <el-tree
          class="lm-tree"
          :data="treeData"
          ref="limitree"
          node-key="id"
          show-checkbox
          default-expand-all
          :highlight-current="true"
          :check-strictly="true"
          :default-checked-keys="checkedlimits"
          :props="defaultProps"
        ></el-tree>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="deviceVisible = false" plain>关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import {
  getDetails,
  putObj,
  addObj,
  getStaffDetail,
  authorityList
} from "@/api/platform/vehicle/staff_vehicle_detail";
import { getObj, getTree } from "@/api/platform/area/limit";
import { allPark } from "@/api/platform/_publicService";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
export default {
  data() {
    var driverLicenseIdValidator = (rule, value, callback) => {
      var driverLicenseIdImg = this.deviceInfo.driverLicenseId;
      if (
        driverLicenseIdImg == null ||
        driverLicenseIdImg == "" ||
        driverLicenseIdImg == undefined
      ) {
        callback(new Error("请选择驾驶证照片！"));
      } else {
        callback();
      }
    };
    var vehiclePlateValidator = (rule, value, callback) => {
      var temp = value.replace(/\s*/g, "");
      this.deviceInfo.vehiclePlate = temp.toLocaleUpperCase();
      var parten = /^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[A-Z])|([A-Z]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$/;
      if (!parten.test(this.deviceInfo.vehiclePlate)) {
        callback(new Error("车牌号输入错误"));
      } else {
        callback();
      }
    };
    return {
      deviceVisible: false,
      pageType: 1, // 1员工车辆、2公司车辆 、3临时人员 共用此详情页
      treeData: [],
      isEdit: false, //是否是编辑状态
      // deviceInfo:{},
      deviceInfo: {
        vehiclePlate: "",
        vehicleType: "",
        vehicleBrand: "",
        vehicleColor: "",
        vehicleColorName: "",
        vehicleTypeName: "",
        driverLicenseId: "",
        drivinglLicenseId: "",
        name: "",
        certno: "",
        sex: "",
        phone: "",
        depName: "",
        parkName: "",
        facePicId: "",
        vehicleAscription: 1,
        id: "",
        staffId: "",
        depId: "",
        parkId: "",
        compId: "",
        authorityId: ""
      },
      defaultProps: {
        children: "children",
        label: "label"
      },
      authes: [],
      curAuthId: '',
      curParkId: '',
      curType: '',
      checkedlimits: [],
      parkData: [],
      authorityData: [],
      editRules: {
        vehiclePlate: [
          { required: true, message: "请输入车牌号", trigger: "blur" },
          { validator: vehiclePlateValidator, trigger: "blur" }
        ],
        parkId: [
          { required: true, message: "请选择所属园区", trigger: "change" }
        ],
        badge: [{ required: true, message: "请输入工号", trigger: "blur" }]
      }
    };
  },
  created() {
    this.pageType = this.$route.query.pageType
    this.getParks();
    this.getDetails();
  },
  computed: {
    ...mapGetters(["permissions"]),
    curAuthName() {
      let obj = this.authes.find(item => {
        if (item.parkId === this.curParkId) {
          return item
        }
      })
      if(obj){
        return obj.authorityName
      }else{
        return '无'
      }
    }
  },
  watch: {
    curParkId(newVal, oldVal) {
      this.selectAuthByParkId(newVal)
    },
    curAuthId(newVal, oldVal) {
      this.getTypeByAuthId(newVal)
    },
  },
  methods: {
    getDetails(){
      getDetails(this.$route.params.id).then(response => {
        let result = response.data.data;
        if (!this.validatenull(result)) {
          this.deviceInfo = result;
          this.authes = result.auths
          this.curParkId = Number(result.parkId)
          this.curAuthId = result.authorityId
          if (this.deviceInfo.sex == 0) {
            this.deviceInfo.sex = "男";
          } else if (this.deviceInfo.sex == 1) {
            this.deviceInfo.sex = "女";
          } else {
            this.deviceInfo.sex = "";
          }
        }
      });
    },
    //在所拥有的权限集合内 根据园区id 查询 是否有属于当前园区的权限，有则返回当前园区权限的id 并查询改园区权限集合
    selectAuthByParkId(parkId){
      let obj = this.authes.find(item => {
        if (item.parkId === parkId) {
          return item
        }
      })
      if(obj){
        this.getAuthorityData(parkId)
        this.curAuthId = obj.id
      }else{
        this.curAuthId = ''
        this.authorityData = []
      }
    },
    //获取园区对应 权限集合
    async getAuthorityData(parkId) {
      const res = await authorityList(parkId);
      this.authorityData = res.data.data;
      this.getTypeByAuthId(this.curAuthId)
    },
    // 根据车辆权限 获取对应type 进而获取 所有授权设备
    getTypeByAuthId(authId){
      let obj = this.authorityData.find(item => {
        if (item.id === authId) {
          return item
        }
      })
      this.treeData = []
      this.checkedlimits = []
      if(obj){
        this.curType = obj.type
        this.getDevices(this.curType, this.curParkId)
      }
    },
    //获取当前车辆权限 所有 授权设备
    getDevices(type ,parkId) {
      getTree(type ,parkId).then(response => {
        this.treeData = response.data.data;
        if(this.treeData && this.treeData.length>0){
          this.disabledDevice(this.treeData)
          this.getSelectedDevices(this.curAuthId)
        }
      });
    },
    //获取当前车辆权限 选中的 授权设备
    getSelectedDevices(authId){
      getObj(authId).then(response => {
        this.checkedlimits = response.data.data.checkedlimits;
      });
    },
    // 此处授权设备仅供展示，所有改为都不可编辑
    disabledDevice(arr) {
      for (let i = 0; i < arr.length; i++) {
        const item = arr[i]
        arr[i].disabled= true
        if (item.children && item.children.length !== 0) {
          this.disabledDevice(item.children)
        }
      }
    },
    async getParks() {
      const res = await allPark();
      this.parkData = res.data.data;
    },
    goBack() {
      let path = ''
      if(this.pageType == 1){
        path = '/platform/vehicle/staff_vehicle'
      }else if(this.pageType == 2){
        path = '/platform/vehicle/company_vehicle'
      }else if(this.pageType == 3){
        path = '/platform/vehicle/non_staff_vehicle'
      }
      this.$router.push({
        path: path,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    },
    editInfo() {
      this.isEdit = true;
      this.doClearValidate("editform");
    },
    saveInfor(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          /* 改权限集合的方式
            //在所有权限里 找到当前选中的权限对象
            let obj = this.authorityData.find(item => {
              if (item.id === this.curAuthId) {
                return item
              }
            })
            let tempArr = []
            //把原来园区对应的权限对象  替换为当前选中的
            this.authes.forEach(el=>{
              if(el.parkId == obj.parkId){
                tempArr.push(obj)
              }else{
                tempArr.push(el)
              }
            })
            this.deviceInfo.auths = tempArr
          */
          this.deviceInfo.parkId= this.curParkId
          this.deviceInfo.authorityId = this.curAuthId
          const params = Object.assign({}, this.deviceInfo);
          // if (isArrayFn(params.parkId)) {
          //   params.parkId = params.parkId.join(",");
          // }
          putObj(params)
            .then(response => {
              this.isEdit = false;
              this.$router.go(-1);
            })
            .catch(err => { console.error(err) });
        } else {
          return false;
        }
      });
    },
    resetEditform(formName) {
      this.$refs[formName].clearValidate();
    },
    saveCancel(formName) {
      this.isEdit = false;
      this.resetEditform(formName);
    },
    drivinglLicenseImgrUpload(file) {
      let isJPG = false;
      if (
        file.type === "image/jpeg" ||
        file.type === "image/png" ||
        file.type === "image/bmp"
      ) {
        isJPG = true;
      }
      // const isJPG = file.type === 'image/jpeg';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error("上传头像图片只能是 JPG、PNG、 BMP 格式!");
      }
      if (!isLt2M) {
        this.$message.error("上传头像图片大小不能超过 2MB!");
      }

      if (isJPG && isLt2M) {
        var reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = e => {
          this.deviceInfo.drivinglLicenseId = e.target.result;
        };
      }
      return isJPG && isLt2M;
    },
    driverLicenseImgUpload(file) {
      let isJPG = false;
      if (
        file.type === "image/jpeg" ||
        file.type === "image/png" ||
        file.type === "image/bmp"
      ) {
        isJPG = true;
      }
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error("上传头像图片只能是 JPG、PNG、 BMP 格式!");
      }
      if (!isLt2M) {
        this.$message.error("上传头像图片大小不能超过 2MB!");
      }

      if (isJPG && isLt2M) {
        var reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = e => {
          this.deviceInfo.driverLicenseId = e.target.result;
        };
      }
      return isJPG && isLt2M;
    },
    //根据工号查询员工详细信息
    selectStaffDetail(formName) {
      let _this = this;
      this.$refs[formName].validateField("badge", errorMessage => {
        if (!errorMessage) {
          getStaffDetail(this.deviceInfo.badge).then(response => {
            let result = response.data.data;
            if (!this.validatenull(result)) {
              let id = result.id;
              if (!this.validatenull(id)) {
                this.deviceInfo.staffId = result.id; //右侧部分，提交的时候只需要提交这个id
                this.deviceInfo.facePicId = result.facePicId;
                this.deviceInfo.name = result.name;
                this.deviceInfo.certno = result.certno;
                this.deviceInfo.sex = result.sex;
                this.deviceInfo.phone = result.phone;
                this.deviceInfo.parkName = result.parkName;
                this.deviceInfo.compName = result.compName;

                if (this.deviceInfo.sex == 0) {
                  this.deviceInfo.sex = "男";
                } else if (this.deviceInfo.sex == 1) {
                  this.deviceInfo.sex = "女";
                } else {
                  this.deviceInfo.sex = "";
                }
              } else {
                this.$notify.error({
                  title: "提示",
                  message: "暂无该工号对应员工信息，请检查工号是否正确！"
                });
              }
            }
          });
        } else {
          return false;
        }
      });
    }
  }
};
</script>
<style lang="scss" scoped>
.lm-tree ::v-deep {
  background: transparent;
  .el-checkbox__input.is-disabled.is-checked .el-checkbox__inner {
    background-color: #ed6d00;
    border-color: #ed6d00;
  }
}
.noPading {
  padding: 12px 20px;
}
.img-info {
  max-width: 200px;
  margin: 0 auto;
}
.img-info2 {
  float: left;
}
.lit-table {
  max-width: 400px;
  float: left;
}
.form-outer {
  max-width: 400px;
  margin-left: 30px;
}
.desc-info {
  color: #666;
  font-size: 12px;
}
.avatar-uploader-icon {
  width: 100%;
  height: 100%;
  font-size: 12px !important;
  font-style: normal;
  text-align: center;
  color: #999;
  // padding-top: 50%;
  display: inline-block;
  background-position: center;
  background-size: 100% 100%;
  background-repeat: no-repeat;
}
</style>
