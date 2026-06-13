
<template>
  <div class="chart-wrap" ref="chartPie"></div>
</template>

<script>
import echarts from "echarts";
import { getProportion } from "@/util/util";

export default {
  data() {
    return {
      myChart: null
    };
  },
  props: {
    dataObj:{
      type: Object,
      default: function(){
        return {}
      }
    }
  },
  mounted() {
    //已入住，未入住
    // this.initChart([700,300], 1000);
    this.initChart(this.dataObj.nums, this.dataObj.total);
  },
  methods: {
    initChart(nums, total) {
      this.myChart = echarts.init(this.$refs.chartPie);
      let rate = 0
      if(nums&&nums[0]!==0){
        rate = getProportion(nums[0], total)
      }
      let rtColors = [
        "#ffa811", //入住的颜色
        "#e6e9f0"
      ];
      let option = {
        animation: false,
        title: [
          {
            text:
              "{val|" + rate + "%}\n{name|" + "入住率" + "}",
            top: "center",
            left: "center",
            textStyle: {
              rich: {
                name: {
                  fontSize: 14,
                  fontWeight: "bold",
                  color: "#ffa811",
                  padding: [10, 0]
                },
                val: {
                  fontSize: 22,
                  fontWeight: "bold",
                  color: "#ffa811"
                },
              },
            },
          },
        ],
        series: [
          {
            name: "inrate",
            type: "pie",
            center: ["50%", "50%"],
            radius: ["75%", "100%"],
            itemStyle: {
              normal: {
                label: {
                  show: false,
                },
                labelLine: {
                  show: false,
                },
                borderColor: "#fff",
                borderWidth: "3",
              },
            },
            data: nums,
          },
        ],
        color: rtColors,
      };
      this.myChart.setOption(option);
    },
    updateChart(){
      this.initChart(this.dataObj.nums, this.dataObj.total)
    }
  },
};
</script>

<style lang="scss" scoped>
.chart-wrap {
  width: 100%;
  height: 100%;
}
</style>
