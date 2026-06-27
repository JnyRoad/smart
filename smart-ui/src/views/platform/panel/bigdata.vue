<!--可视化面板，智慧园区运营大数据 -->
<template>
  <div class="big-screen">
    <div class="big-screen__inner">
      <div class="big-screen__left">
        <!-- 园区描述 -->
        <div class="screen-area park-describe">
          <div class="screen-area__title">园区描述</div>
          <div class="screen-area__content">
            <!-- 占地面积（亩） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon1"></i>
                    <div class="park-info__text">{{ statisticsData.parkArea || "-" }}</div>
                  </div>
                  <div class="park-info__title">占地面积（亩）</div>
                </div>
              </div>
            </div>
            <!-- 生产厂房（栋） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon2"></i>
                    <div class="park-info__text">{{ statisticsData.workshopToal || "-" }}</div>
                  </div>
                  <div class="park-info__title">生产厂房（栋）</div>
                </div>
              </div>
            </div>
            <!-- 宿舍楼数（栋） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon3"></i>
                    <div class="park-info__text">{{ statisticsData.dormitoryToTal || "-" }}</div>
                  </div>
                  <div class="park-info__title">宿舍楼数（栋）</div>
                </div>
              </div>
            </div>
            <!-- 餐厅楼数（栋） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon4"></i>
                    <div class="park-info__text">{{ statisticsData.diningRoomTotal || "-" }}</div>
                  </div>
                  <div class="park-info__title">餐厅楼数（栋）</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 园区员工 -->
        <div class="screen-area park-staff">
          <div class="screen-area__title">园区员工</div>
          <div class="screen-area__content">
            <!-- 入驻BU数（个） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon5"></i>
                    <div class="park-info__text">{{ statisticsData.compTotal || "-" }}</div>
                  </div>
                  <div class="park-info__title">入驻BU数（个）</div>
                </div>
              </div>
            </div>
            <!-- 部门数量（个） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon6"></i>
                    <div class="park-info__text">{{ statisticsData.deptToal || "-" }}</div>
                  </div>
                  <div class="park-info__title">部门数量（个）</div>
                </div>
              </div>
            </div>
            <!-- 岗位个数（个） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon7"></i>
                    <div class="park-info__text">{{ statisticsData.jobTotal || "-" }}</div>
                  </div>
                  <div class="park-info__title">岗位个数（个）</div>
                </div>
              </div>
            </div>
            <!-- 员工总数（人） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon8"></i>
                    <div class="park-info__text">{{ statisticsData.staffTotal || "-" }}</div>
                  </div>
                  <div class="park-info__title">员工总数（人）</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 车辆动态 -->
        <div class="screen-area park-staff">
          <div class="screen-area__title">车辆动态</div>
          <div class="screen-area__content">
            <!-- 员工车辆（辆） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon11"></i>
                    <div class="park-info__text">{{vehicleTrends.innerTotal||'-'}}</div>
                  </div>
                  <div class="park-info__title">员工车辆（辆）</div>
                </div>
              </div>
            </div>
            <!-- 外来车辆（辆） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon11"></i>
                    <div class="park-info__text">{{(vehicleTrends.foreignTotal)||'-'}}</div>
                  </div>
                  <div class="park-info__title">外来车辆（辆）</div>
                </div>
              </div>
            </div>
            <!-- 车位总数（个） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon9"></i>
                    <div class="park-info__text">{{kingPlace.totalCount||'-'}}</div>
                  </div>
                  <div class="park-info__title">车位总数（个）</div>
                </div>
              </div>
            </div>
            <!-- 当前空余（个） -->
            <div class="screen-area-item">
              <div class="screen-area-item__inner">
                <div class="park-info">
                  <div class="park-info__content">
                    <i class="park-info__icon icon10"></i>
                    <div class="park-info__text">{{kingPlace.freeCount||'-'}}</div>
                  </div>
                  <div class="park-info__title">当前空余（个）</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="big-screen__right">
        <!-- 右上部分 -->
        <div class="big-right__top">
          <!-- 地图 -->
          <div class="screen-map">
            <div class="animation-box">
              <canvas class="canva-line" ref="canvasLine" width="264.5" height="245"></canvas>
              <canvas class="canva-linemove" ref="canvasLineMove" width="264.5" height="245"></canvas>
              <div class="round"></div>
              <div class="round"></div>
              <div class="round"></div>
              <div class="round"></div>
              <div class="round"></div>
              <div class="round"></div>
              <div class="round"></div>
            </div>
          </div>

          <!-- 宿舍动态 -->
          <div class="screen-area dormitory-dynamics">
            <div class="screen-area__title">宿舍动态</div>
            <div class="screen-area__content">
              <div class="dormitory-dynamics__info">
                <!-- 房间数量（间） -->
                <div class="screen-area-item">
                  <div class="screen-area-item__inner">
                    <div class="park-info">
                      <div class="park-info__content">
                        <div class="park-info__text">{{ statisticsData.roomTotal || "-" }}</div>
                      </div>
                      <div class="park-info__title">房间数量（间）</div>
                    </div>
                  </div>
                </div>
                <!-- 床位数量（个） -->
                <div class="screen-area-item">
                  <div class="screen-area-item__inner">
                    <div class="park-info">
                      <div class="park-info__content">
                        <div class="park-info__text">{{ statisticsData.bedTotal || "-" }}</div>
                      </div>
                      <div class="park-info__title">床位数量（个）</div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="dormitory-dynamics__chart">
                <div class="dormitory-dynamics__chart-item chart-panel-box">
                  <div class="chart-panel" ref="chartPanel"></div>
                  <div class="chart-info">
                    <div>
                      入住人数
                      <span>{{ dormitoryDynamics.bedStaffTotal || "-" }}</span>
                    </div>
                    <div>
                      床位占用
                      <span>
                        {{
                        dormitoryDynamics.bedStaffTotalProportion || "-"
                        }}%
                      </span>
                    </div>
                  </div>
                </div>
                <div class="dormitory-dynamics__chart-item">
                  <div
                    class="chart-item"
                    :class="
                      dormitoryDynamics.gardeSectionIndex === 0 && 'active'
                    "
                  >
                    <i class="i1"></i>
                    <span>极高</span>
                    <span>90%-100%</span>
                  </div>
                  <div
                    class="chart-item i2"
                    :class="
                      dormitoryDynamics.gardeSectionIndex === 1 && 'active'
                    "
                  >
                    <i class="i2"></i>
                    <span>较高</span>
                    <span>60%-90%</span>
                  </div>
                  <div
                    class="chart-item i3"
                    :class="
                      dormitoryDynamics.gardeSectionIndex === 2 && 'active'
                    "
                  >
                    <i class="i3"></i>
                    <span>正常</span>
                    <span>30%-60%</span>
                  </div>
                  <div
                    class="chart-item i4"
                    :class="
                      dormitoryDynamics.gardeSectionIndex === 3 && 'active'
                    "
                  >
                    <i class="i4"></i>
                    <span>较低</span>
                    <span>0%-30%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 右下部分：组织构成与员工分布 -->
        <div class="big-right__bottom">
          <div class="screen-area bar-chart">
            <div class="screen-area__title">组织构成与员工分布</div>
            <div class="screen-area__content bar-chart__content">
              <!-- 组织构成 -->
              <div class="chart-pie" ref="chartPie"></div>
              <!-- 员工分布 -->
              <div class="chart-bar" ref="chartBar"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/platform/panel/accessto" as *;
@use "@/styles/platform/panel/bigdata" as *;
</style>
<style lang="scss">
.bigdata-loading {
  font-size: 36px !important;
  .el-icon-loading {
    font-size: 36px;
  }
  .el-loading-text {
    color: #409eff !important;
  }
  .el-loading-spinner i {
    color: #409eff;
  }
}
</style>


<script>
import {
  getStatistics,
  getVehicleCount,
  getCorrectionCount
} from "@/api/platform/panel/bigdata";
import { allPark } from "@/api/platform/_publicService";
import { mapGetters } from "vuex";
import echarts from "echarts";
import { clearPanelRefresh, schedulePanelRefresh } from "./panel-refresh";

/**
 * 利用贝塞尔曲线公式计算出曲线上某点坐标
 */
const get_bezier_dot = function(t, obj) {
  var x =
    (1 - t) * (1 - t) * obj.p0_x +
    2 * t * (1 - t) * obj.p1_x +
    t * t * obj.p2_x;
  var y =
    (1 - t) * (1 - t) * obj.p0_y +
    2 * t * (1 - t) * obj.p1_y +
    t * t * obj.p2_y;
  return { x: x, y: y };
};

/**
 * 获取当前分数段索引
 */
const getGardeSectionIndex = (function() {
  // 宿舍动态阶梯
  const gardeSection = [
    {
      text: "极高",
      start: 90,
      end: 100
    },
    {
      text: "较高",
      start: 60,
      end: 90
    },
    {
      text: "正常",
      start: 30,
      end: 60
    },
    {
      text: "较低",
      start: 0,
      end: 30
    }
  ];
  return function(grade = 0) {
    let index = 0;
    gardeSection.some((item, i) => {
      if (grade >= item.start && grade < item.end) {
        index = i;
        return true;
      }
    });
    return index;
  };
})();

/**
 * 获取小数后两位的百分比,不带'%'
 */
const getProportion = function(num, sum) {
  if (sum <= 0) {
    sum = 1;
  }
  return Math.round((num / sum) * 100 * 100) / 100;
};

export default {
  name: "bigdata",
  data() {
    return {
      statisticsData: {}, // 接口源数据
      compNames: [], // 组织名称数组
      compNamesStatisticss: [], // 对应 compNames 的人员数
      dormitoryDynamics: {
        // 宿舍动态
        bedStaffTotal: 0, // 床位数
        bedStaffTotalProportion: 0, // 入住比例
        gardeSectionIndex: 0 // 入信比例对应的状态 【0:极高 1：较高 2：正常 3：较低】
      },
      kingPlace: {}, // 车位剩余
      vehicleTrends: {}, //车辆动态
      loading: null
    };
  },
  created: function() {},
  mounted: function() {
    this.getParks();
    this.getCorrectionCount();
    this.getVehicleCount();
    this.mapEffect();
    clearPanelRefresh(this);
    this.getData();
  },
  beforeDestroy: function() {
    clearPanelRefresh(this);
  },
  computed: {
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

      this.getParks();
      this.getCorrectionCount();
      this.getVehicleCount();
      this.mapEffect();
      schedulePanelRefresh(this, "/platform/panel/bigdata", () => {
        this.getData();
      });
    },
    winClose() {
      this.$message("未实现...");
    },
    async getCorrectionCount() {
      this.loading = this.$loading({
        lock: true,
        text: "Loading",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)",
        customClass: "bigdata-loading"
      });
      const correctionCount = await getCorrectionCount();
      this.kingPlace = correctionCount.data.data;
    },
    async getVehicleCount() {
      this.loading = this.$loading({
        lock: true,
        text: "Loading",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)",
        customClass: "bigdata-loading"
      });
      const vehicleCount = await getVehicleCount();
      this.vehicleTrends = vehicleCount.data.data;
    },
    async getParks() {
      this.loading = this.$loading({
        lock: true,
        text: "Loading",
        spinner: "el-icon-loading",
        background: "rgba(0, 0, 0, 0.7)",
        customClass: "bigdata-loading"
      });
      const parks = await allPark();
      this.getStatistics(parks.data.data[0].id);
    },
    async getStatistics(id) {
      let statistics;
      try {
        statistics = await getStatistics({
          id: id
        });
      } catch {
        this.$message.error("网络连接，请刷新页面重试");
      }

      this.loading.close();
      this.statisticsData = statistics.data.data;

      // 处理宿舍动态数据
      this.dormitoryDynamics.bedStaffTotal = this.statisticsData.bedStaffTotal;
      this.dormitoryDynamics.bedStaffTotalProportion = getProportion(
        this.statisticsData.bedStaffTotal,
        this.statisticsData.bedTotal
      );
      this.dormitoryDynamics.gardeSectionIndex = getGardeSectionIndex(
        this.dormitoryDynamics.bedStaffTotalProportion
      );

      // 重构数据结构,易于处理
      if (this.statisticsData.compStatistics) {
        this.compNames = [];
        this.compNamesStatisticss = [];
        this.statisticsData.compStatistics.forEach(item => {
          this.compNames.push(item.compName);
          this.compNamesStatisticss.push(item.vlaue);
        });
      }
      this.$nextTick(() => {
        this.chartPanel();
        this.chartPie();
        this.chartBar();
      });
    },
    /**
     * 宿舍动态半圆环图表
     */
    chartPanel() {
      var index = this.dormitoryDynamics.gardeSectionIndex;
      // BEGIN chart
      // 渐变色
      const colors = [
        {
          type: "linear",
          x: 0.5,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: "#fe756e" // 0% 处的颜色
            },
            {
              offset: 1,
              color: "#f9bebb" // 100% 处的颜色
            }
          ],
          global: false // 缺省为 false
        },
        {
          type: "linear",
          x: 0.5,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: "#6e7ffe" // 0% 处的颜色
            },
            {
              offset: 1,
              color: "#a7b0f6" // 100% 处的颜色
            }
          ],
          global: false // 缺省为 false
        },
        {
          type: "linear",
          x: 0.5,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: "#6e75fe" // 0% 处的颜色
            },
            {
              offset: 1,
              color: "#5db1ff" // 100% 处的颜色
            }
          ],
          global: false // 缺省为 false
        },
        {
          type: "linear",
          x: 0.5,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            {
              offset: 0,
              color: "#5db1ff" // 0% 处的颜色
            },
            {
              offset: 1,
              color: "#a5d1fa" // 100% 处的颜色
            }
          ],
          global: false // 缺省为 false
        }
      ];
      let myChart = echarts.init(this.$refs.chartPanel);
      // 指定图表的配置项和数据
      let option = {
        series: [
          {
            type: "pie",
            startAngle: 0,
            hoverAnimation: false,
            radius: ["180%", "155%"],
            center: ["50%", "100%"],
            legendHoverLink: false,
            hoverAnimation: false,
            avoidLabelOverlap: false,
            labelLine: {
              show: false
            },
            data: [
              {
                value: this.statisticsData.bedTotal,
                itemStyle: {
                  normal: {
                    color: "rgba(80,150,224,0)"
                  }
                }
              },
              {
                value: this.statisticsData.bedStaffTotal,
                itemStyle: {
                  normal: {
                    color: colors[index]
                  }
                }
              },
              {
                value:
                  this.statisticsData.bedTotal -
                  this.statisticsData.bedStaffTotal,
                itemStyle: {
                  normal: {
                    color: "#17387a"
                  }
                }
              }
            ]
          }
        ]
      };
      myChart.setOption(option);
    },
    /**
     * 组织构成饼图
     */
    chartPie() {
      const compNames = this.compNames;
      const statistics = this.compNamesStatisticss;
      const ratios = [];
      const data = [];
      let ratioSum = 0;
      let sum = 0;

      // 人工计算，未采用接口数据
      sum = statistics.reduce((acc, item) => {
        return acc + item;
      });

      // 计算比例 最后一位比例为补位项
      statistics.forEach((item, i) => {
        // 构建echart 数据项
        data.push({
          value: item,
          name: compNames[i]
        });
        let _s = getProportion(item, sum) * 100; // 转为整数
        if (i == statistics.length - 1) {
          ratios.push((100 * 100 - ratioSum) / 100 + "% "); // 直接补位
        } else {
          ratioSum += _s;
          ratios.push(_s / 100 + "% ");
        }
      });

      // BEGIN echart
      let myChart = echarts.init(this.$refs.chartPie);
      let option = {
        color: ["#fe8e6e", "#ffce80", "#63cdff", "#ac9eff", "#ff7780"],
        tooltip: {
          trigger: "item",
          formatter: "{a} <br/>{b}: {c} ({d}%)"
        },
        legend: {
          // orient: 'vertical',
          itemWidth: 8,
          itemHeight: 8,
          x: "15%",
          y: "center",
          padding: [10, 15],
          textStyle: {
            color: "#448fd5",
            fontSize: 14,
            rich: {
              title: {
                color: "#448fd5",
                fontSize: 14,
                width: 66,
                align: "right"
              },
              abg: {
                color: "#448fd5",
                fontSize: 14
              }
            }
          },
          formatter: function(name) {
            let index = compNames.indexOf(name);
            return "{title|" + ratios[index] + "} {abg|" + name + "}";
          },
          data: compNames
        },
        series: [
          {
            name: "",
            type: "pie",
            radius: ["50%", "70%"],
            center: ["6%", "50%"],
            avoidLabelOverlap: false,
            label: {
              normal: {
                show: false,
                position: "center"
              },
              emphasis: {
                show: true,
                position: "bottom",
                textStyle: {
                  fontSize: "14",
                  fontWeight: "bold"
                }
              }
            },
            labelLine: {
              normal: {
                show: false
              }
            },
            data: data
          }
        ]
      };
      myChart.setOption(option);
      // END
    },
    /**
     * 员工分布柱状图
     */
    chartBar() {
      let myCharat = echarts.init(this.$refs.chartBar);
      let option = {
        color: ["#3398DB"],
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "shadow"
          }
        },
        grid: {
          left: "2%",
          right: "2%",
          bottom: "6%",
          containLabel: true
        },
        xAxis: [
          {
            type: "category",
            data: this.compNames,
            axisLabel: {
              fontSize: 14,
              interval: "0",
              color: "#448fd5"
            },
            axisLine: {
              show: false
            },
            splitLine: {
              show: false
            },
            axisTick: {
              show: false
            }
          }
        ],
        yAxis: [
          {
            axisLabel: {
              show: false
            },
            axisTick: {
              show: false
            },
            axisLine: {
              show: false
            },
            splitLine: {
              show: false
            },
            type: "value"
          }
        ],
        series: [
          {
            name: "",
            type: "bar",
            barWidth: "25%",
            label: {
              show: true,
              fontSize: 14,
              color: "#5db1ff",
              position: "top"
            },

            itemStyle: {
              color: {
                type: "linear",
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  {
                    offset: 0,
                    color: "#63c2ff" // 0% 处的颜色
                  },
                  {
                    offset: 1,
                    color: "#4a9bff" // 100% 处的颜色
                  }
                ],
                global: false // 缺省为 false
              }
            },
            data: this.compNamesStatisticss
          }
        ]
      };
      myCharat.setOption(option);
    },
    /**
     * map 画图
     */
    mapEffect() {
      // 添加所有线条
      let ctx = this.$refs.canvasLine.getContext("2d");
      let origin = [263 / 2, 397 / 2];
      let coordinate = [
        [257 / 2, 16 / 2, 224 / 2, 194 / 2],
        [62 / 2, 209 / 2, 162 / 2, 249 / 2],
        [360 / 2, 232 / 2, 304 / 2, 299 / 2],
        [304 / 2, 268 / 2, 261 / 2, 316 / 2],
        [138 / 2, 295 / 2, 197 / 2, 328 / 2],
        [15 / 2, 387 / 2, 130 / 2, 372 / 2]
      ];
      let brokenPoint = [322 / 2, 490 / 2, 529 / 2, 490 / 2];
      ctx.beginPath();
      coordinate.forEach(item => {
        ctx.moveTo(origin[0], origin[1]);
        ctx.quadraticCurveTo(item[2], item[3], item[0], item[1]);
      });
      ctx.strokeStyle = "#5db1ff";
      ctx.stroke();
      ctx.moveTo(origin[0], origin[1]);
      ctx.lineTo(brokenPoint[0], brokenPoint[1]);
      ctx.lineTo(brokenPoint[2], brokenPoint[3]);
      ctx.setLineDash([5, 5]);
      ctx.strokeStyle = "#5db1ff";
      ctx.stroke();

      //线条滚动代码
      let canvasLineMove = this.$refs.canvasLineMove;
      let ctxLineMove = this.$refs.canvasLineMove.getContext("2d");
      let t = 0.1;
      let draw = function() {
        if (t >= 1) {
          t = 0.01;
        }
        t += 0.01;
        ctxLineMove.clearRect(
          0,
          0,
          canvasLineMove.width,
          canvasLineMove.height
        );
        ctxLineMove.beginPath();
        coordinate.forEach(item => {
          ctxLineMove.moveTo(item[0], item[1]);
          ctxLineMove.quadraticCurveTo(item[2], item[3], origin[0], origin[1]);
        });
        ctxLineMove.strokeStyle = "rgba(255,255,255,.5)";
        ctxLineMove.setLineDash([10 * t]);
        ctxLineMove.stroke();
        // coordinate.forEach((item) => {
        //   let _origin = get_bezier_dot(t, {
        //     p0_x: item[0],
        //     p1_x: item[2],
        //     p2_x: origin[0],
        //     p0_y: item[1],
        //     p1_y: item[3],
        //     p2_y: origin[1],
        //   })
        //   ctxLineMove.arc(_origin.x, _origin.y, 1, 0, 2 * Math.PI);
        // });
        // let item = coordinate[0];
        // let _origin = get_bezier_dot(t, {
        //   p0_x: item[0],
        //   p1_x: item[2],
        //   p2_x: origin[0],
        //   p0_y: item[1],
        //   p1_y: item[3],
        //   p2_y: origin[1],
        // })
        // ctxLineMove.fillStyle = 'rgba(25,255,255,1)'
        // ctxLineMove.beginPath()
        // ctxLineMove.arc(_origin.x, _origin.y, 1, 0, 2 * Math.PI);
        // ctxLineMove.fill();
        // ctxLineMove.closePath()
        //ctxLineMove.clearRect(0, 0, canvasLineMove.width, canvasLineMove.height);
        window.requestAnimationFrame(draw);
      };
      draw();
    }
  }
};
</script>
