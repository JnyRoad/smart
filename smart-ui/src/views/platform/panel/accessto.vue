<!--可视化面板，人员出入 -->
<template>
  <section class="pn-btm">
    <div class="pn-btm-lft">
      <section class="pn-btm-sq">
        <p class="pn-sec-titile">{{device1.deviceName?device1.deviceName:'暂无数据'}}</p>
        <div class="pn-sq-item">
          <div class="pn-sq-img">
            <div class="pn-img-outer">
              <div class="pn-img-inner">
                <img
                  :src="device1.snapPhotoUrl?device1.snapPhotoUrl:defaultImg"
                  :onerror="defaultImgFun()"
                />
                <span class="pn-img-bg">
                  <span>现场抓拍</span>
                </span>
              </div>
            </div>
          </div>
          <div class="pn-sq-img">
            <div class="pn-img-outer">
              <div class="pn-img-inner">
                <img
                  :src="device1.personUrl?device1.personUrl:defaultImg"
                  :onerror="defaultImgFun()"
                />
                <span class="pn-img-bg">
                  <span>人脸识别</span>
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="pn-sq-info pn-sq-item">
          <p>
            <span class="pn-label">
              姓
              <i class="ft-zn-cn2"></i>名
            </span>
            <span class="pn-value">{{device1.personName || "-"}}</span>
          </p>
          <div class="pn-sq-item-outer">
            <p>
              <span class="pn-label">
                身
                <i class="ft-zn-cn2"></i>份
              </span>
              <span class="pn-value">{{device1.personTypeDesc || "-"}}</span>
            </p>
            <p>
              <span class="pn-label">出入类型</span>
              <span class="pn-value">{{device1.eventTypeDesc || "-"}}</span>
            </p>
          </div>
          <p>
            <span class="pn-label">出入时间</span>
            <span class="pn-value">{{device1.snapTime || "-"}}</span>
          </p>
          <p>
            <span class="pn-label">单位名称</span>
            <span class="pn-value">{{device1.company || "-"}}</span>
          </p>
        </div>
      </section>
      <section class="pn-btm-sq">
        <p class="pn-sec-titile">{{device3.deviceName?device3.deviceName:'暂无数据'}}</p>
        <div class="pn-sq-item">
          <div class="pn-sq-img">
            <div class="pn-img-outer">
              <div class="pn-img-inner">
                <img
                  :src="device3.snapPhotoUrl?device3.snapPhotoUrl:defaultImg"
                  :onerror="defaultImgFun()"
                />
                <span class="pn-img-bg">
                  <span>现场抓拍</span>
                </span>
              </div>
            </div>
          </div>
          <div class="pn-sq-img">
            <div class="pn-img-outer">
              <div class="pn-img-inner">
                <img
                  :src="device3.personUrl?device3.personUrl:defaultImg"
                  :onerror="defaultImgFun()"
                />
                <span class="pn-img-bg">
                  <span>人脸识别</span>
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="pn-sq-info pn-sq-item">
          <p>
            <span class="pn-label">
              姓
              <i class="ft-zn-cn2"></i>名
            </span>
            <span class="pn-value">{{device3.personName || "-"}}</span>
          </p>
          <div class="pn-sq-item-outer">
            <p>
              <span class="pn-label">
                身
                <i class="ft-zn-cn2"></i>份
              </span>
              <span class="pn-value">{{device3.personTypeDesc || "-"}}</span>
            </p>
            <p>
              <span class="pn-label">出入类型</span>
              <span class="pn-value">{{device3.eventTypeDesc || "-"}}</span>
            </p>
          </div>
          <p>
            <span class="pn-label">出入时间</span>
            <span class="pn-value">{{device3.snapTime || "-"}}</span>
          </p>
          <p>
            <span class="pn-label">单位名称</span>
            <span class="pn-value">{{device3.company || "-"}}</span>
          </p>
        </div>
      </section>
    </div>
    <div class="pn-btm-md">
      <section class="pn-btm-sq">
        <p class="pn-sec-titile">{{device2.deviceName?device2.deviceName:'暂无数据'}}</p>
        <div class="pn-sq-item">
          <div class="pn-sq-img">
            <div class="pn-img-outer">
              <div class="pn-img-inner">
                <img
                  :src="device2.snapPhotoUrl?device2.snapPhotoUrl:defaultImg"
                  :onerror="defaultImgFun()"
                />
                <span class="pn-img-bg">
                  <span>现场抓拍</span>
                </span>
              </div>
            </div>
          </div>
          <div class="pn-sq-img">
            <div class="pn-img-outer">
              <div class="pn-img-inner">
                <img
                  :src="device2.personUrl?device2.personUrl:defaultImg"
                  :onerror="defaultImgFun()"
                />
                <span class="pn-img-bg">
                  <span>人脸识别</span>
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="pn-sq-info pn-sq-item">
          <p>
            <span class="pn-label">
              姓
              <i class="ft-zn-cn2"></i>名
            </span>
            <span class="pn-value">{{device2.personName || "-"}}</span>
          </p>
          <div class="pn-sq-item-outer">
            <p>
              <span class="pn-label">
                身
                <i class="ft-zn-cn2"></i>份
              </span>
              <span class="pn-value">{{device2.personTypeDesc || "-"}}</span>
            </p>
            <p>
              <span class="pn-label">出入类型</span>
              <span class="pn-value">{{device2.eventTypeDesc || "-"}}</span>
            </p>
          </div>
          <p>
            <span class="pn-label">出入时间</span>
            <span class="pn-value">{{device2.snapTime || "-"}}</span>
          </p>
          <p>
            <span class="pn-label">单位名称</span>
            <span class="pn-value">{{device2.company || "-"}}</span>
          </p>
        </div>
      </section>
      <section class="pn-btm-sq">
        <p class="pn-sec-titile">{{device4.deviceName?device4.deviceName:'暂无数据'}}</p>
        <div class="pn-sq-item">
          <div class="pn-sq-img">
            <div class="pn-img-outer">
              <div class="pn-img-inner">
                <img
                  :src="device4.snapPhotoUrl?device4.snapPhotoUrl:defaultImg"
                  :onerror="defaultImgFun()"
                />
                <span class="pn-img-bg">
                  <span>现场抓拍</span>
                </span>
              </div>
            </div>
          </div>
          <div class="pn-sq-img">
            <div class="pn-img-outer">
              <div class="pn-img-inner">
                <img
                  :src="device4.personUrl?device4.personUrl:defaultImg"
                  :onerror="defaultImgFun()"
                />
                <span class="pn-img-bg">
                  <span>人脸识别</span>
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="pn-sq-info pn-sq-item">
          <p>
            <span class="pn-label">
              姓
              <i class="ft-zn-cn2"></i>名
            </span>
            <span class="pn-value">{{device4.personName || "-"}}</span>
          </p>
          <div class="pn-sq-item-outer">
            <p>
              <span class="pn-label">
                身
                <i class="ft-zn-cn2"></i>份
              </span>
              <span class="pn-value">{{device4.personTypeDesc || "-"}}</span>
            </p>
            <p>
              <span class="pn-label">出入类型</span>
              <span class="pn-value">{{device4.eventTypeDesc || "-"}}</span>
            </p>
          </div>
          <p>
            <span class="pn-label">出入时间</span>
            <span class="pn-value">{{device4.snapTime || "-"}}</span>
          </p>
          <p>
            <span class="pn-label">单位名称</span>
            <span class="pn-value">{{device4.company || "-"}}</span>
          </p>
        </div>
      </section>
    </div>
    <div class="pn-btm-rt">
      <div class="pn-btm-rt-item">
        <section class="pn-btm-rt-sq2">
          <p class="pn-sec-titile">访客分析</p>
          <div class="pn-chart1">
            <div class="pn-ct1-item">
              <template v-for="(item, index) in visitorAnalysisChart">
                <p :key="index">
                  <i class="pn-fkfx-i pn-i-cl1" :style="{backgroundColor:item.color}"></i>
                  <span class="pn-fkfx-percent">{{item.value}}</span>
                  {{item.name}}
                </p>
              </template>
            </div>
            <div class="pn-ct1-item" id="inRate" ref="inRateChart"></div>
          </div>
        </section>
        <section class="pn-btm-rt-sq2">
          <p class="pn-sec-titile">访客实时</p>
          <div class="pn-rt-fkss">
            <div class="pn-fkss-item">
              <div class="pn-fkss-info">
                <i class="pn-fkss-i pn-fkss-in"></i>
                <span class="pn-fkss-num">{{visitorInNum}}</span>
              </div>
              <p>今日累计进厂（人）</p>
            </div>
            <div class="pn-fkss-item">
              <div class="pn-fkss-info">
                <i class="pn-fkss-i pn-fkss-out"></i>
                <span class="pn-fkss-num">{{visitorOutNum}}</span>
              </div>
              <p>今日累计出厂（人）</p>
            </div>
            <div class="pn-fkss-item pn-fkss-doors">
              <template v-for="(item, index) in visitorDeviceNum">
                <div class="pn-fk-dr-item" :key="index">
                  <p>
                    <span class="pn-fkss-dr-value">{{item.deviceName}}</span>
                  </p>
                  <p>
                    <span class="pn-fkss-dr-value">{{item.eventCount}}</span>
                    <span class="pn-fkss-dr-label">{{item.eventTypeDesc}}</span>
                  </p>
                </div>
              </template>
            </div>
          </div>
        </section>
      </div>
      <section class="pn-btm-rt-sq3">
        <p class="pn-sec-titile carTitile">
          <span>车辆进入时段分析</span>
          <span class="indoor">进门</span>
          <span class="outdoor">出门</span>
        </p>
        <div class="pn-chart2">
          <div class="carAnalysisChart" id="carAnalysisChart" ref="carAnalysisChart"></div>
        </div>
      </section>
    </div>
  </section>
</template>

<style lang="scss" scoped>
@use "@/styles/platform/panel/accessto" as *;
</style>

<script>
import {
  getVisitorIn,
  getVisitorOut,
  getVisitorDevice,
  getVisitorAnalysis,
  getVisitorNew,
  getVehicleCount
} from "@/api/platform/panel/accessto";
import { mapGetters } from "vuex";
import echarts from "echarts";
import { isArrayFn } from "@/util/util";
export default {
  name: "accessto",
  data() {
    return {
      defaultImg: "/img/pn_plhd.png",
      visitorInNum: "", //访客实时进厂数量
      visitorOutNum: "", //访客实时出厂数量
      visitorDeviceNum: [], //访客实时 设备进出场数据
      visitorAnalysisNum: [
        //访客分析
        { value: 0, name: "业务会谈" },
        { value: 0, name: "业务拜访" },
        { value: 0, name: "面试" },
        { value: 0, name: "其他" }
      ],
      rtColors: ["#FF7780", "#FFCE80", "#63CDFF", "#AC9EFF", "#FFCE80"],
      device1: {}, //第一个设备
      device2: {}, //第二个设备
      device3: {}, //第三个设备
      device4: {}, //第四个设备

      carAnalysisChart: null,
      indoorNums: [], //各指定时间段内，进门数量
      outdoorNums: [] //各指定时间段内，出门数量
    };
  },
  created: function() {
    if (this.timeOut) {
      clearTimeout(this.timeOut);
      this.timeOut = undefined;
    }
    this.getData();
  },
  mounted: function() {},
  beforeDestroy: function() {
    clearTimeout(this.timeOut);
    this.timeOut = undefined;
  },
  computed: {
    visitorAnalysisChart: function() {
      let total = 0;
      let tmp = [];
      let colors = this.rtColors;
      this.visitorAnalysisNum.forEach(function(element) {
        total += element.value;
      }, this);
      if (!this.validatenull(total)) {
        this.visitorAnalysisNum.forEach(function(element, index) {
          var tpv = "0%";
          if (total != 0) {
            tpv = ((element.value / total) * 100).toFixed(2) + "%";
          }
          // if(!this.validatenull(element.value)){
          //   // tpv =((element.value/total)*100).toFixed(2)+'%';
          //   tpv =(element.value/total)*100+'%';
          // }
          tmp.push({ value: tpv, name: element.name, color: colors[index] });
        }, this);
      }
      return tmp;
    },
    timeOut: {
      set(val) {
        this.$store.commit("SET_PANELTIMEOUT", val);
      },
      get() {
        return this.$store.state.panelTimeout;
      }
    }
  },
  methods: {
    getData() {
      //定时任务
      // 这里是一个http的异步请求
      this.init();
      if (this.$route.path == "/platform/panel/accessto") {
        let _this = this;
        this.timeOut = setTimeout(() => {
          _this.getData();
        }, 60000);
      } else {
        this.timeOut = undefined;
      }
    },
    init() {
      this.getVisitorIn(); //访客实时进厂数量
      this.getVisitorOut(); //访客实时出厂数量
      this.getVisitorDevice(); //访客实时 设备进出场数据
      this.getVisitorAnalysis(); //访客分析
      this.getVisitorNew(); //设备抓拍显示
      this.getVehicleCount(); //车辆进出时段分析
    },
    defaultImgFun() {
      return 'this.onload=null;this.src=" ' + this.defaultImg + '";';
    },
    getVisitorIn() {
      getVisitorIn()
        .then(response => {
          if (response.data.code == 0) {
            this.visitorInNum = response.data.data.count;
          }
        })
        .catch(err => { console.error(err) });
    },
    getVisitorOut() {
      getVisitorOut()
        .then(response => {
          if (response.data.code == 0) {
            this.visitorOutNum = response.data.data.count;
          }
        })
        .catch(err => { console.error(err) });
    },
    getVisitorDevice() {
      getVisitorDevice()
        .then(response => {
          if (response.data.code == 0) {
            this.visitorDeviceNum = response.data.data;
          }
        })
        .catch(err => { console.error(err) });
    },
    getVisitorAnalysis() {
      getVisitorAnalysis()
        .then(response => {
          if (response.data.code == 0) {
            let results = response.data.data;
            let tmp = [];
            results.forEach(function(element) {
              tmp.push({ value: element.causeCount, name: element.causeDesc });
            }, this);
            this.visitorAnalysisNum = tmp;
            this.initRateChart(this.visitorAnalysisNum);
          }
        })
        .catch(err => { console.error(err) });
    },
    getVisitorNew() {
      getVisitorNew()
        .then(response => {
          if (response.data.code == 0) {
            let results = response.data.data;
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
        })
        .catch(err => { console.error(err) });
    },
    getVehicleCount() {
      getVehicleCount()
        .then(response => {
          if (response.data.code == 0) {
            this.indoorNums = response.data.data.indoorNums;
            this.outdoorNums = response.data.data.outdoorNums;
            this.initAnalysisChart(this.indoorNums, this.outdoorNums);
          }
        })
        .catch(err => { console.error(err) });
    },
    dateFormat(date) {
      let format = "yyyy-MM-dd hh:mm:ss";
      if (date != "Invalid Date") {
        var o = {
          "M+": date.getMonth() + 1, //month
          "d+": date.getDate(), //day
          "h+": date.getHours(), //hour
          "m+": date.getMinutes(), //minute
          "s+": date.getSeconds(), //second
          "q+": Math.floor((date.getMonth() + 3) / 3), //quarter
          S: date.getMilliseconds() //millisecond
        };
        if (/(y+)/.test(format))
          format = format.replace(
            RegExp.$1,
            (date.getFullYear() + "").substr(4 - RegExp.$1.length)
          );
        for (var k in o)
          if (new RegExp("(" + k + ")").test(format))
            format = format.replace(
              RegExp.$1,
              RegExp.$1.length == 1
                ? o[k]
                : ("00" + o[k]).substr(("" + o[k]).length)
            );
        return format;
      }
      return "";
    },
    initAnalysisChart(inNums, outNums) {
      var carAnalysisChart = echarts.init(
        document.getElementById("carAnalysisChart")
      );
      var optionCarAnalysis = {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "shadow"
          }
        },
        grid: {
          top: "20",
          left: "0",
          right: "2%",
          bottom: "3%",
          containLabel: true
        },
        xAxis: [
          {
            type: "category",
            data: ["0-4时", "5-8时", "9-12时", "12-16时", "16-20时", "20-24时"],
            axisLine: {
              show: false,
              lineStyle: {
                color: "#063374",
                width: 1,
                type: "solid"
              }
            },
            axisTick: {
              show: false
            },
            axisLabel: {
              show: true,
              textStyle: {
                color: "#00c7ff"
              }
            }
          }
        ],
        yAxis: [
          {
            show: false,
            type: "value"
          }
        ],
        series: [
          {
            name: "进门",
            type: "bar",
            data: inNums,
            barWidth: 15, //柱子宽度
            barGap: 1, //柱子之间间距
            label: {
              show: true,
              position: "top",
              textStyle: {
                color: "#5DB1FF"
              }
            },
            itemStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {
                    offset: 0,
                    color: "#63C2FF"
                  },
                  {
                    offset: 1,
                    color: "#4B9BFF"
                  }
                ]),
                opacity: 1
              }
            }
          },
          {
            name: "出门",
            type: "bar",
            data: outNums,
            barWidth: 15,
            barGap: 1,
            label: {
              show: true,
              position: "top",
              textStyle: {
                color: "#9FA3FF"
              }
            },
            itemStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {
                    offset: 0,
                    color: "#9FA3FF"
                  },
                  {
                    offset: 1,
                    color: "#6985FF"
                  }
                ]),
                opacity: 1
              }
            }
          }
        ]
      };
      carAnalysisChart.setOption(optionCarAnalysis);
    },
    initRateChart(nums) {
      let _this = this;
      var inRateChart = echarts.init(document.getElementById("inRate"));
      var optionInRate = {
        animation: false,
        series: [
          {
            name: "inrate",
            type: "pie",
            center: ["50%", "50%"],
            radius: ["70%", "90%"],
            itemStyle: {
              normal: {
                label: {
                  show: false
                },
                labelLine: {
                  show: false
                },
                borderColor: "#102C63",
                borderWidth: "3"
              }
            },
            data: nums
          }
        ],
        color: this.rtColors
      };
      inRateChart.setOption(optionInRate);
    },
    initResize() {
      const self = this; //因为箭头函数会改变this指向，指向windows。所以先把this保存
      setTimeout(() => {
        window.onresize = function() {
          if (self.chart) {
            self.chart = echarts.init(self.$refs.inRateChart);
            self.chart = echarts.init(self.$refs.carAnalysisChart);
            self.chart.resize();
          }
        };
      }, 20);
    }
  }
};
</script>
