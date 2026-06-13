<!-- 停车场管理，当前车辆详情 -->
<template>
  <div class="my-basic-container vehicle-detail mycard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <el-row type="flex" class="my-basic-inner">
        <el-col :lg="8" :md="8" class="box-outer box-left">
          <div class="top-menu" style="margin-bottom:20px;">
            <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
          </div>
          <div class>
            <p class="box-orange">车辆信息</p>
            <table class="lit-table">
              <tr>
                <td>车牌号</td>
                <td>{{vehicleInfo.vehiclePlate}}</td>
              </tr>
              <!-- <tr>
                <td>车类型</td>
                <td>{{vehicleInfo.vehicleType}}</td>
              </tr> -->
              <tr>
                <td>车品牌</td>
                <td>{{vehicleInfo.vehicleBrand==0?'未知':vehicleInfo.vehicleBrand}}</td>
              </tr>
              <tr>
                <td>车颜色</td>
                <td>{{vehicleInfo.vehicleColorDesc}}</td>
              </tr>
            </table>
          </div>
          <div class>
            <p class="box-orange">车主信息</p>
            <table class="lit-table">
              <tr>
                <td>姓名</td>
                <td>{{vehicleInfo.driverName}}</td>
              </tr>
              <tr>
                <td>电话</td>
                <td>{{vehicleInfo.driverPhone}}</td>
              </tr>
              <tr>
                <td>所属园区</td>
                <td>{{vehicleInfo.parkName}}</td>
              </tr>
              <tr>
                <td>所属BU</td>
                <td>{{vehicleInfo.compName}}</td>
              </tr>
              <tr>
                <td>所属部门</td>
                <td>{{vehicleInfo.depName}}</td>
              </tr>
            </table>
          </div>
          <div class>
            <p class="box-orange">停车信息</p>
            <table class="lit-table">
              <tr>
                <td>出入园区</td>
                <td>{{vehicleInfo.parkName}}</td>
              </tr>
              <tr>
                <td>出入地点</td>
                <td>{{vehicleInfo.areaName}}</td>
              </tr>
              <tr>
                <td>出入类型</td>
                <td>{{vehicleInfo.eventTypeDesc}}</td>
              </tr>
              <tr>
                <td>出入时间</td>
                <td>{{vehicleInfo.snapTime}}</td>
              </tr>
            </table>
          </div>
        </el-col>
        <el-col :lg="16" :md="16" class="box-outer">
          <!-- 折线图片框 -->
          <div class="folder-outer">
            <div class="folder-line">
              <i class="cni cni-right">
                <i class="icon-yutong-squareRight bkcn cn3"></i>
              </i>
              <i class="cni cni-btm">
                <i class="icon-yutong-squareBottom bkcn cn4"></i>
              </i>
              <i class="cni cni-lft">
                <i class="icon-yutong-squareLeft bkcn cn5"></i>
              </i>
              <div class="img">
                <viewer>
                  <img :src="vehicleInfo.snapPhoto || errorImgPeaple()" :onerror="errorImgPeaple()" />
                </viewer>
              </div>
            </div>
            <span class="folder-top">
              抓拍照片
              <i class="skewi"></i>
              <i class="icon-yutong-squareTop1 bkcn cn2"></i>
            </span>
            <i class="cni cni-top">
              <i class="icon-yutong-squareTop bkcn cn1"></i>
            </i>
          </div>
        </el-col>
      </el-row>
    </el-scrollbar>
  </div>
</template>

<script>
import { getDetails } from "@/api/platform/entrance/vehicle_detail";
import { mapGetters } from "vuex";

export default {
  name: "parking",
  data() {
    return {
      vehicleInfo: {}
    };
  },
  created() {
    getDetails(this.$route.params.id).then(response => {
      this.vehicleInfo = response.data.data;
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    goBack() {
      let path = '/platform/parking/parking_record'
      this.$router.push({
        path: path,
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
</style>
