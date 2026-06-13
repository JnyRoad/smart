<!--出入记录，车辆出入，详情  -->
<template>
  <div class="my-basic-container vehicle-detail mycard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <el-row type="flex" class="my-basic-inner">
        <el-col :lg="8" :md="8" class="box-outer box-left">
          <div class>
            <p class="box-orange">车辆信息</p>
            <table class="lit-table">
              <tr>
                <td>车牌号</td>
                <td>{{carInfo.vehiclePlate}}</td>
              </tr>
              <!-- <tr>
                <td>车类型</td>
                <td>{{carInfo.vehiclePlate}}</td>
              </tr>-->
              <!-- <tr>
                <td>车品牌</td>
                <td>{{carInfo.vehicleBrand}}</td>
              </tr>
              <tr>
                <td>车颜色</td>
                <td>{{carInfo.vehicleColorDesc}}</td>
              </tr>-->
            </table>
          </div>
          <div class>
            <p class="box-orange">出入信息</p>
            <table class="lit-table">
              <tr>
                <td>出入园区</td>
                <td>{{carInfo.parkName}}</td>
              </tr>
              <tr>
                <td>出入地点</td>
                <td>{{carInfo.areaName}}</td>
              </tr>
              <tr>
                <td>出入类型</td>
                <td>{{carInfo.eventTypeDesc}}</td>
              </tr>
              <tr>
                <td>出入时间</td>
                <td>{{carInfo.snapTime}}</td>
              </tr>
            </table>
          </div>
          <div class>
            <p class="box-orange">车主信息</p>
            <table class="lit-table">
              <tr>
                <td>姓名</td>
                <td>{{carInfo.driverName}}</td>
              </tr>
              <tr>
                <td>所属单位</td>
                <td>{{carInfo.supplier}}</td>
              </tr>
              <tr>
                <td>手机号</td>
                <td>{{carInfo.driverPhone}}</td>
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
                  <img :src="carInfo.snapPhoto || errorImgPeaple()" :onerror="errorImgPeaple()" />
                </viewer>
              </div>
            </div>
            <span class="folder-top">
              现场抓拍照片
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
import { getDetails } from "@/api/platform/logistics_vehicle/reserve_record";
import { mapGetters } from "vuex";

export default {
  name: "vehicle",
  data() {
    return {
      carInfo: {}
    };
  },
  created() {
    getDetails(this.$route.params.id + "").then(response => {
      this.carInfo = response.data.data;
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {}
};
</script>

<style lang="scss" scoped>
</style>
