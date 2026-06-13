<!--车辆管理：非员工车辆，添加车辆 -->
<template>
  <div class="my-basic-container vehicle_add">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <el-form
          ref="addform"
          :rules="addRules"
          :model="infoForm"
          label-width="80px"
          style="padding-top:20px;"
        >
          <el-row :gutter="20">
            <el-col :span="7" class="minWidth" :offset="1">
              <el-form-item label="证件照片" prop="drivinglLicenseId">
                <el-row class>
                  <el-col :span="12" style="padding-right:10px;">
                    <div class="img-info car-img">
                      <div class="img-outer">
                        <div class="img-inner">
                          <el-upload
                            class="avatar-uploader"
                            action
                            :before-upload="drivinglLicenseImgrUpload"
                            :show-file-list="false"
                          >
                            <img
                              v-if="infoForm.drivinglLicenseId"
                              :src="infoForm.drivinglLicenseId"
                              :onerror="errorImgPeaple()"
                              class="avatar"
                            />
                            <i v-else class="avatar-uploader-icon"></i>
                          </el-upload>
                        </div>
                      </div>
                      <p class="gray-font">行驶证</p>
                    </div>
                  </el-col>
                  <el-col :span="12" style="padding-left:10px;">
                    <div class="img-info car-img">
                      <div class="img-outer">
                        <div class="img-inner">
                          <el-upload
                            class="avatar-uploader"
                            action
                            :before-upload="driverLicenseImgUpload"
                            :show-file-list="false"
                          >
                            <img
                              v-if="infoForm.driverLicenseId"
                              :src="infoForm.driverLicenseId"
                              class="avatar"
                            />
                            <i v-else class="avatar-uploader-icon"></i>
                          </el-upload>
                        </div>
                      </div>
                      <p class="gray-font">驾驶证</p>
                    </div>
                  </el-col>
                </el-row>
              </el-form-item>
              <el-form-item label="所属园区" prop="parkId">
                <el-select v-model="infoForm.parkId" @change="selectChanged"  placeholder="所属园区">
                  <el-option
                    v-for="item in parkData"
                    :key="item.id"
                    :label="item.parkName"
                    :value="item.id"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="车牌号" prop="vehiclePlate">
                <el-input v-model="infoForm.vehiclePlate" clearable></el-input>
              </el-form-item>
              <el-form-item label="车类型" prop="vehicleType">
                <el-select v-model="infoForm.vehicleType" placeholder="请选择" clearable>
                  <el-option label="大型车" value="1"></el-option>
                  <el-option label="小型车" value="2"></el-option>
                  <el-option label="其他车" value="0"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="车品牌" prop="vehicleBrand">
                <el-input v-model="infoForm.vehicleBrand" clearable></el-input>
              </el-form-item>
              <el-form-item label="车颜色" prop="vehicleColor">
                <el-select v-model="infoForm.vehicleColor" placeholder="请选择" clearable>
                  <el-option label="白" value="1"></el-option>
                  <el-option label="银" value="2"></el-option>
                  <el-option label="灰" value="3"></el-option>
                  <el-option label="黑" value="4"></el-option>
                  <el-option label="红" value="5"></el-option>
                  <el-option label="深蓝" value="6"></el-option>
                  <el-option label="蓝" value="7"></el-option>
                  <el-option label="黄" value="8"></el-option>
                  <el-option label="绿" value="9"></el-option>
                  <el-option label="棕" value="10"></el-option>
                  <el-option label="粉" value="11"></el-option>
                  <el-option label="紫" value="12"></el-option>
                  <el-option label="深灰" value="13"></el-option>
                  <el-option label="青色" value="14"></el-option>
                  <el-option label="其他" value="0"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="对口人员" prop="name">
                <el-input v-model="infoForm.name" clearable></el-input>
              </el-form-item>
              <el-form-item label="电话" prop="phone">
                <el-input v-model="infoForm.phone" clearable></el-input>
              </el-form-item>
              <el-form-item label="车辆权限" prop="authorityId">
                <el-select v-model="infoForm.authorityId" placeholder="车辆权限">
                  <el-option
                    v-for="item in authorityData"
                    :key="item.id"
                    :label="item.authorityName"
                    :value="item.id"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="备注" prop="remark">
                <el-input type="textarea" v-model="infoForm.remark" clearable></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="7" class="minWidth" :offset="1"></el-col>
          </el-row>
          <div class="btns-bottom">
            <el-button type="primary" @click="saveInfor('addform')">提交</el-button>
            <el-button type="primary" @click="saveCancel('addform')" plain>取消</el-button>
          </div>
        </el-form>
      </section>
    </el-scrollbar>
  </div>
</template>
<style lang="scss">
@use "@/styles/platform/vehicle/add" as *;
</style>
<script>
import EXIF from "exif-js";
import { addObj, plate } from "@/api/platform/vehicle/non_staff_vehicle";
import { authorityList } from "@/api/platform/vehicle/staff_vehicle_detail";
import { allPark } from "@/api/platform/_publicService";
import { mapGetters } from "vuex";
import { isMobile } from "@/util/validate";
import { isArrayFn } from "@/util/util";
export default {
  data() {
    var vehiclePlateValidator = (rule, value, callback) => {
      var temp = value.replace(/\s*/g, "");
      this.infoForm.vehiclePlate = temp.toLocaleUpperCase();
      var parten = /^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[A-Z])|([A-Z]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$/;
      if (!parten.test(this.infoForm.vehiclePlate)) {
        callback(new Error("车牌号输入错误"));
      } else {
        const { parkId, vehiclePlate, vehicleAscription, source } = this.infoForm;
        plate({ parkId, vehiclePlate, vehicleAscription, source }).then(response => {
          if (response.data.data) {
            callback(new Error("车牌号已绑定，请勿重复绑定"));
          } else {
            callback();
          }
        });
      }
    };
    var validatePhone = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error("请输入电话"));
      } else {
        if (!isMobile(value.replace(/(^\s*)|(\s*$)/g, ""))) {
          callback(new Error("请输入正确的电话"));
        } else {
          callback();
        }
      }
    };
    return {
      infoForm: {
        vehiclePlate: "",
        vehicleType: "",
        vehicleBrand: "",
        vehicleColor: "",
        drivinglLicenseId: "",
        driverLicenseId: "",
        vehicleAscription: 0,
        parkId: "",
        authorityId: ""
      },
      parkData: [],
      authorityData: [],
      addRules: {
        vehiclePlate: [
          { required: true, message: "请输入车牌号", trigger: "blur" },
          { validator: vehiclePlateValidator, trigger: "blur" }
        ],
        parkId: [
          { required: true, message: "请选择所属园区", trigger: "change" }
        ],
        authorityId: [
          { required: true, message: "请选择车辆权限", trigger: "change" }
        ],
        name: [
          { required: true, message: "请输入对口人员姓名", trigger: "blur" }
        ],
        phone: [
          { required: true, message: "请输入电话", trigger: "blur" },
          { validator: validatePhone, trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getParks();
    this.doClearValidate("addform");
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    async selectChanged(parkId) {
      const res = await authorityList(parkId);
      this.authorityData = res.data.data;
      this.infoForm.parkId = parkId;
    },
    async getParks() {
      const res = await allPark();
      this.parkData = res.data.data;
    },
    saveInfor(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          const params = Object.assign({}, this.infoForm);
          if (isArrayFn(params.parkId)) {
            params.parkId = params.parkId.join(",");
          }
          addObj(params)
            .then(response => {
              this.$router.go(-1);
            })
            .catch(err => { console.error(err) });
        } else {
          return false;
        }
      });
    },
    saveCancel() {
      this.$router.go(-1);
    },
    drivinglLicenseImgrUpload(file) {
      let _this = this;
      let fileSize;
      let fileName;
      let maxSize = 2 * 1024 * 1024; //2MB
      EXIF.getData(file, function() {
        let exifdata = file.exifdata;
        _this.imgDirection = file.exifdata.Orientation;
      });
      let reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = function(e) {
        //onload
        fileSize = file.size;
        fileName = file.name;
        //这是图片没压缩以前的 代码
        let image = new Image();
        image.src = reader.result;
        image.onload = function() {
          let canvas = document.createElement("canvas");
          let context = canvas.getContext("2d");
          let imageWidth;
          let imageHeight;

          if (fileSize > maxSize) {
            imageWidth = image.width / 5;
            imageHeight = image.height / 5;
          } else {
            imageWidth = image.width;
            imageHeight = image.height;
          }

          let data = "";
          if (imageWidth > imageHeight) {
            canvas.width = imageWidth;
            canvas.height = imageWidth;
          } else {
            canvas.width = imageHeight;
            canvas.height = imageHeight;
          }

          if (_this.imgDirection && _this.imgDirection != 1) {
            switch (_this.imgDirection) {
              case 6: // 旋转90度
                var x = 0;
                var y = -(imageWidth - 50);
                if (imageWidth > imageHeight) {
                  y = -imageWidth - (imageHeight - imageWidth) / 2;
                } else {
                  // y = ;
                }
                context.rotate((90 * Math.PI) / 180);
                context.drawImage(image, x, y, imageWidth, imageHeight);
                break;
              case 3: // 旋转180度
                var x = -imageWidth;
                var y = -imageHeight;
                if (imageWidth > imageHeight) {
                  x = -imageWidth;
                  y = -imageHeight - (imageWidth - imageHeight) / 2;
                } else {
                  // y = ;
                }
                context.rotate(Math.PI);
                context.drawImage(image, x, y, imageWidth, imageHeight);
                break;
              case 8: // 旋转-90度  //270
                var x = -imageWidth;
                var y = 70;
                if (imageHeight > imageWidth) {
                  x = -imageWidth;
                  y = -(imageHeight - imageWidth) / 2;
                } else {
                  // y = ;
                }
                context.rotate((3 * Math.PI) / 2);
                context.drawImage(image, x, y, imageWidth, imageHeight);
                break;
            }
          } else {
            var x = 0;
            var y = 0;
            if (imageHeight > imageWidth) {
              x = (imageHeight - imageWidth) / 2;
            } else {
              y = (imageWidth - imageHeight) / 2;
            }
            context.drawImage(image, x, y, imageWidth, imageHeight);
          }

          // 将canvas的透明背景设置成白色
          var imageData = context.getImageData(
            0,
            0,
            canvas.width,
            canvas.height
          );
          for (var i = 0; i < imageData.data.length; i += 4) {
            // 当该像素是透明的，则设置成白色
            if (imageData.data[i + 3] == 0) {
              imageData.data[i] = 255;
              imageData.data[i + 1] = 255;
              imageData.data[i + 2] = 255;
              imageData.data[i + 3] = 255;
            }
          }

          context.putImageData(imageData, 0, 0);
          data = canvas.toDataURL("image/jpeg");

          //压缩完毕
          _this.infoForm.drivinglLicenseId = data; //含有 base64 头的
        };
      };
    },
    driverLicenseImgUpload(file) {
      let _this = this;
      let fileSize;
      let fileName;
      let maxSize = 2 * 1024 * 1024; //2MB
      EXIF.getData(file, function() {
        let exifdata = file.exifdata;
        _this.imgDirection = file.exifdata.Orientation;
      });
      let reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = function(e) {
        //onload
        fileSize = file.size;
        fileName = file.name;
        //这是图片没压缩以前的 代码
        let image = new Image();
        image.src = reader.result;
        image.onload = function() {
          let canvas = document.createElement("canvas");
          let context = canvas.getContext("2d");
          let imageWidth;
          let imageHeight;

          if (fileSize > maxSize) {
            imageWidth = image.width / 5;
            imageHeight = image.height / 5;
          } else {
            imageWidth = image.width;
            imageHeight = image.height;
          }

          let data = "";
          if (imageWidth > imageHeight) {
            canvas.width = imageWidth;
            canvas.height = imageWidth;
          } else {
            canvas.width = imageHeight;
            canvas.height = imageHeight;
          }

          if (_this.imgDirection && _this.imgDirection != 1) {
            switch (_this.imgDirection) {
              case 6: // 旋转90度
                var x = 0;
                var y = -(imageWidth - 50);
                if (imageWidth > imageHeight) {
                  y = -imageWidth - (imageHeight - imageWidth) / 2;
                } else {
                  // y = ;
                }
                context.rotate((90 * Math.PI) / 180);
                context.drawImage(image, x, y, imageWidth, imageHeight);
                break;
              case 3: // 旋转180度
                var x = -imageWidth;
                var y = -imageHeight;
                if (imageWidth > imageHeight) {
                  x = -imageWidth;
                  y = -imageHeight - (imageWidth - imageHeight) / 2;
                } else {
                  // y = ;
                }
                context.rotate(Math.PI);
                context.drawImage(image, x, y, imageWidth, imageHeight);
                break;
              case 8: // 旋转-90度  //270
                var x = -imageWidth;
                var y = 70;
                if (imageHeight > imageWidth) {
                  x = -imageWidth;
                  y = -(imageHeight - imageWidth) / 2;
                } else {
                  // y = ;
                }
                context.rotate((3 * Math.PI) / 2);
                context.drawImage(image, x, y, imageWidth, imageHeight);
                break;
            }
          } else {
            var x = 0;
            var y = 0;
            if (imageHeight > imageWidth) {
              x = (imageHeight - imageWidth) / 2;
            } else {
              y = (imageWidth - imageHeight) / 2;
            }
            context.drawImage(image, x, y, imageWidth, imageHeight);
          }

          // 将canvas的透明背景设置成白色
          var imageData = context.getImageData(
            0,
            0,
            canvas.width,
            canvas.height
          );
          for (var i = 0; i < imageData.data.length; i += 4) {
            // 当该像素是透明的，则设置成白色
            if (imageData.data[i + 3] == 0) {
              imageData.data[i] = 255;
              imageData.data[i + 1] = 255;
              imageData.data[i + 2] = 255;
              imageData.data[i + 3] = 255;
            }
          }

          context.putImageData(imageData, 0, 0);
          data = canvas.toDataURL("image/jpeg");

          //压缩完毕
          _this.infoForm.driverLicenseId = data; //含有 base64 头的
        };
      };
    }
  }
};
</script>
<style lang="scss" scoped>
</style>
