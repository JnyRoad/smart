<!--可视化面板，人员出入 -->
<template>
  <div class="pn-at">
    <top @parkChange="doParkChange" @defaultHandle="defaultParkHandle"></top>
    <div class="pn-bt">
      <div class="pn-bt_left">
        <ps1-1 :dataObj="device1" />
        <ps1-1 :dataObj="device2" />
        <ps1-1 :dataObj="device3" />
        <ps1-1 :dataObj="device4" />
      </div>
      <div class="pn-bt_right">
        <div class="pn-bt_right_top">
          <ps1-3 :dataObj="dataFkfx" ref="v13" />
          <ps1-4 :dataObj="dataFkss" />
        </div>
        <div class="pn-bt_right_btm">
          <ps1-5 :dataObj="dataCljrsdfx" ref="v15" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import top from "./top";
import { api } from "@/views/platform/panel_new/_service";
import { isArrayFn } from "@/util/util";
import { dateFormat } from '@/util/date.js';

// 加载./components 下的所有组件
const context = require.context("./person-components", true, /\.vue/);
let components = {};
context.keys().forEach((item) => {
  components["ps1-" + item.split("/")[1]] = context(item).default;
});

export default {
  name: "panel",
  components: {
    top,
    ...components,
  },
  data() {
    return {
      dataFkfx: [], //访客分析
      dataCljrsdfx: {}, //车辆进入时段分析
      dataFkss: {}, //访客实时
      device1: {}, //第一个设备
      device2: {}, //第二个设备
      device3: {}, //第三个设备
      device4: {}, //第四个设备
      loading: null,
      parkId: '',
      timer: '', //定时器
      testTime: '' //间隔时间点
    };
  },
  created: function () {},
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
    initData(){
      Promise.all([
        this.getVisitorAnalysis(true),
        this.getVehicleCount(true),
        this.getVisitorRealTime(true),
        this.getVisitorNew(true)
      ]).then(arr=>{
        this.loading&&this.loading.close();
      });
    },
    refreshData(){
      Promise.all([
        this.getVisitorAnalysis(),
        this.getVehicleCount(),
        this.getVisitorRealTime(),
        this.getVisitorNew()
      ]).then(arr=>{
        this.loading&&this.loading.close();
      });
      this.getTime()
    },
    getTime() {//打印定时器间隔时间
      this.testTime = dateFormat(new Date());
    },
    showLoading() {
      this.loading = this.$loading({
        lock: true,
        text: "Loading",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)",
        customClass: "panelNew-loading",
      });
    },
    initDevice(){
      this.device1 = {}
      this.device2 = {}
      this.device3 = {}
      this.device4 = {}
    },
    //设备显示人员出入
    async getVisitorNew(doLoad) {
      this.initDevice()
      // doLoad?this.showLoading():''
      const res = await api.getVisitorNew(this.parkId);
      if (res.data.code == 0) {
        let results = res.data.data;
        if (isArrayFn(results)) {
          if (results.length > 0) {
            this.device1 = results[0];
          }
          if (results.length > 1) {
            this.device2 = results[1];
          }
          if (results.length > 2) {
            this.device3 = results[2];
          }
          if (results.length > 3) {
            this.device4 = results[3];
          }
        }
      }
    },
    //访客分析
    async getVisitorAnalysis(doLoad) {
      // doLoad?this.showLoading():''
      const res = await api.getVisitorAnalysis(this.parkId);
      if (res.data.code == 0) {
        this.dataFkfx = res.data.data;
        this.$nextTick(() => {
          this.$refs.v13&&this.$refs.v13.updateChart();
        });
      }
    },
    //车辆进入时段分析
    async getVehicleCount(doLoad) {
      // doLoad?this.showLoading():''
      const res = await api.getVehicleCount(this.parkId);
      if (res.data.code == 0) {
        this.dataCljrsdfx = {
          indoorNums: res.data.data.indoorNums,
          outdoorNums: res.data.data.outdoorNums,
        };
        this.$nextTick(() => {
          this.$refs.v15&&this.$refs.v15.updateChart();
        });
      }
    },
    //访客实时
    async getVisitorRealTime(doLoad) {
      // doLoad?this.showLoading():''
      const res = await api.getVisitorRealTime(this.parkId);
      if (res.data.code == 0) {
        this.dataFkss = res.data.data;
      }
    }
  },
};
</script>

<style lang="scss">
@use "@/styles/platform/panel_new/normal" as *;
</style>
<style scoped lang="scss">
@use "@/styles/platform/panel_new/person" as *;
</style>
