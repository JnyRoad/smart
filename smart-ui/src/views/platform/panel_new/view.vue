<!--可视化面板，园区概括 -->
<template>
  <div class="pn-at">
    <top @parkChange="doParkChange" @defaultHandle="defaultParkHandle"></top>
    <div class="pn-bt">
      <div class="pn-bt_left">
        <div class="pn-bt_left_top">
          <div class="pn-bt_left_top_l">
            <div class="pn-bt_left_top_l_t">
              <v1-1 :dataObj="dataYqms" />
            </div>
            <div class="pn-bt_left_top_l_b">
              <v1-2 :dataObj="dataYqyg" />
            </div>
          </div>
          <div class="pn-bt_left_top_r">
            <mapItem />
          </div>
        </div>
        <div class="pn-bt_left_btm">
          <v1-3 :dataObj="dataOrg" ref="v13" />
        </div>
      </div>
      <div class="pn-bt_right">
        <div class="pn-bt_right_top">
          <dormItem :dataObj="dataSsdt" ref="refDorm"/>
        </div>
        <div class="pn-bt_right_btm">
          <carItem  :dataObj="kingPlace"/>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import top from "./top";
import mapItem from "./map";
import dormItem from "./dorm-item";
import carItem from "./car-item";

import { api } from "@/views/platform/panel_new/_service";
import { dateFormat } from '@/util/date.js';
// 加载./components 下的所有组件
const context = require.context("./view-components1", true, /\.vue/);
let components = {};
context.keys().forEach((item) => {
  components["v1-" + item.split("/")[1]] = context(item).default;
});

export default {
  name: "panel",
  components: {
    top,
    mapItem,
    carItem,
    dormItem,
    ...components,
  },
  data() {
    return {
      dataYqms: {}, //园区描述
      dataYqyg: {}, //园区员工
      dataOrg: [], //组织结构与员工分布
      kingPlace: {}, //车位信息
      dataSsdt: {}, //宿舍动态
      loading: null,
      timer: '', //定时器
      parkId: '',
      testTime: '' //间隔时间点
    };
  },
  created: function () {
  },
  mounted: function () {
    this.timer = setInterval(this.refreshData, 60000);
  },
  beforeDestroy() {
    clearInterval(this.timer);
  },
  computed: {},
  methods: {
    defaultParkHandle(id){
      this.parkId = id
      this.initData()
    },
    doParkChange(id){
      this.parkId = id
      this.initData()
    },
    showLoading(){
      this.loading = this.$loading({
        lock: true,
        text: "Loading",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)",
        customClass: "panelNew-loading"
      });
    },
    getTime() {//测试定时器时间
      this.testTime = dateFormat(new Date());
    },
    refreshData(){
      Promise.all([
        this.getCorrectionCount(),
        this.getDormitory()
      ]).then(arr=>{
        this.loading&&this.loading.close();
      });
      this.getTime()
    },
    initData(){
      Promise.all([
        this.getStatistics(true),
        this.getCorrectionCount(true),
        this.getDormitory(true)
      ]).then(arr=>{
        this.loading&&this.loading.close();
      });
    },
    async getStatistics(doLoad) {
      // doLoad?this.showLoading():''
      const res = await api.getStatistics(this.parkId);
      const resData = res.data.data;
      //园区描述
      this.dataYqms = {
        parkArea: resData.parkArea,
        workshopToal: resData.workshopToal,
        dormitoryToTal: resData.dormitoryToTal,
        diningRoomTotal: resData.diningRoomTotal,
      };
      //园区员工
      this.dataYqyg = {
        compTotal: resData.compTotal,
        deptToal: resData.deptToal,
        jobTotal: resData.jobTotal,
        staffTotal: resData.staffTotal,
      };
      //组织结构与员工分布
      this.dataOrg = resData.compStatistics;
      this.$nextTick(() => {
        this.$refs.v13.updateChart();
      });
    },
    async getCorrectionCount(doLoad) {
      // doLoad ? this.showLoading() : ''
      const res = await api.getCorrectionCount(this.parkId);
      this.kingPlace = res.data.data;
    },
    //宿舍动态
    async getDormitory(doLoad){
      // doLoad ? this.showLoading() : ''
      const res = await api.getDormitory(this.parkId);
      this.dataSsdt = res.data.data;
      this.$nextTick(() => {
        this.$refs.refDorm.updateChart();
      });
    }
  }
};
</script>

<style lang="scss">
  @use "@/styles/platform/panel_new/normal" as *;
</style>
<style scoped lang="scss">
  @use "@/styles/platform/panel_new/view" as *;
</style>
