<!--车辆管理：入园审批详情 -->
<template>
  <div class="my-basic-container vehicle">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <div class="top-menu clear">
          <div class="top-status">
            <p class="circle-white">申请入园：{{deviceInfo.parkName}}</p>
            <p class="circle-white">申请时间：{{deviceInfo.createTime}}</p>
            <p class="circle-white">申请状态：{{deviceInfo.status|entryExamineFormat}}</p>
            <p class="circle-white">描述：{{deviceInfo.reason}}</p>
            <p class="circle-white">审批人：{{deviceInfo.approver}}</p>
            <p class="circle-white">审批时间：{{deviceInfo.updateTime}}</p>
          </div>
        </div>
        <p class="box-orange box-p">车辆信息</p>
        <el-row>
          <el-col :span="5">
            <dl class="img-info">
              <dt class="img-outer img-outer-square">
                <i class="corner cn-top"></i>
                <i class="corner cn-rit"></i>
                <i class="corner cn-btm"></i>
                <i class="corner cn-lft"></i>
                <div class="img-inner">
                  <viewer>
                    <img :src="vehicle.drivinglLicenseId || errorImgDriving()" :onerror="errorImgDriving()" />
                  </viewer>
                </div>
              </dt>
              <dd class="desc-info">行驶证</dd>
            </dl>
          </el-col>
          <el-col :span="5">
            <dl class="img-info img-info2">
              <dt class="img-outer img-outer-square">
                <i class="corner cn-top"></i>
                <i class="corner cn-rit"></i>
                <i class="corner cn-btm"></i>
                <i class="corner cn-lft"></i>
                <div class="img-inner">
                  <viewer>
                    <img :src="vehicle.driverLicenseId || errorImgDriving()" :onerror="errorImgDriver()" />
                  </viewer>
                </div>
              </dt>
              <dd class="desc-info">驾驶证</dd>
            </dl>
          </el-col>
          <el-col :span="14">
            <table class="lit-table">
              <tr>
                <td>车牌号</td>
                <td>{{vehicle.vehiclePlate}}</td>
              </tr>
              <tr>
                <td>车类型</td>
                <td>{{vehicle.vehicleTypeName}}</td>
              </tr>
              <tr>
                <td>车品牌</td>
                <td>{{vehicle.vehicleBrand}}</td>
              </tr>
              <tr>
                <td>车颜色</td>
                <td>{{vehicle.vehicleColorName}}</td>
              </tr>
            </table>
          </el-col>
        </el-row>
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
                  <viewer>
                    <img :src="vehicle.facePicId || errorImgPeaple()" :onerror="errorImgPeaple()" />
                  </viewer>
                </div>
              </dt>
              <dd class="desc-info"></dd>
            </dl>
          </el-col>
          <el-col :span="19">
            <table class="lit-table">
              <tr>
                <td>车主姓名</td>
                <td>{{vehicle.name}}</td>
              </tr>
              <tr>
                <td>身份证号</td>
                <td>{{vehicle.certno}}</td>
              </tr>
              <tr>
                <td>性别</td>
                <td>{{vehicle.sex|genderInit}}</td>
              </tr>
              <tr>
                <td>手机号</td>
                <td>{{vehicle.phone}}</td>
              </tr>
              <tr>
                <td>所属部门</td>
                <td>{{vehicle.depName}}</td>
              </tr>
            </table>
          </el-col>
        </el-row>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { fetchDetail } from "@/api/platform/vehicle/entry_examine_detail";
import { mapGetters } from "vuex";
export default {
  data() {
    return {
      deviceInfo: {},
      vehicle: {}
    };
  },
  created() {
    fetchDetail(this.$route.params.id).then(response => {
      this.deviceInfo = response.data.data;
      this.vehicle = response.data.data.vehicle;
    });
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/vehicle/entry_examine`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    }
  }
};
</script>
<style lang="scss" scoped>
.box-p {
  margin-top: 20px;
}
.img-info {
  width: 60%;
  margin: 0 auto;
}
.top-status {
  p {
    float: left;
    margin-right: 60px;
  }
}
.img-info2 {
  float: left;
}
.lit-table {
  max-width: 400px;
  float: left;
}
</style>
