
<template>
  <div class="chart-wrap" ref="chartBar"></div>
</template>

<script>
import echarts from "echarts";
export default {
  data() {
    return {};
  },
  props: {
    dataObj:{
      type: Object,
      default: () => ({})
    }
  },
  mounted() {
    let inN = [0, 0, 0, 0, 0, 0]
    let inO = [0, 0, 0, 0, 0, 0]
    this.initChart(inN, inO);
  },
  methods: {
    initChart(inNums, outNums) {
      let myChart = echarts.init(this.$refs.chartBar);
      let option = {
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "shadow",
          },
        },
        grid: {
          top: "20",
          left: "0",
          right: "2%",
          bottom: "3%",
          containLabel: true,
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
                type: "solid",
              },
            },
            axisTick: {
              show: false,
            },
            axisLabel: {
              show: true,
              textStyle: {
                color: "#448fd5",
              },
            },
          },
        ],
        yAxis: [
          {
            show: false,
            type: "value",
          },
        ],
        series: [
          {
            name: "进入园区",
            type: "bar",
            data: inNums,
            barWidth: 15, //柱子宽度
            barGap: 1, //柱子之间间距
            label: {
              show: true,
              position: "top",
              textStyle: {
                color: "#FFCE80",
              },
            },
            itemStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {
                    offset: 0,
                    color: "#FFCE80",
                  },
                  {
                    offset: 1,
                    color: "#FFCE80",
                  },
                ]),
                opacity: 1,
              },
            },
          },
          {
            name: "离开园区",
            type: "bar",
            data: outNums,
            barWidth: 15,
            barGap: 1,
            label: {
              show: true,
              position: "top",
              textStyle: {
                color: "#38CABD",
              },
            },
            itemStyle: {
              normal: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  {
                    offset: 0,
                    color: "#38CABD",
                  },
                  {
                    offset: 1,
                    color: "#3ACBBD",
                  },
                ]),
                opacity: 1,
              },
            },
          },
        ],
      };
      myChart.setOption(option);
    },
    updateChart(){
      this.initChart(this.dataObj.indoorNums, this.dataObj.outdoorNums)
    }
  }
};
</script>

<style lang="scss" scoped>
.chart-wrap {
  width: 100%;
  height: 100%;
}
</style>
