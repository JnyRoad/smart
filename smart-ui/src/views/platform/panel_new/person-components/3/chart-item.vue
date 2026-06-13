
<template>
  <div class="chart-wrap" ref="chartPie"></div>
</template>

<script>
import echarts from "echarts";
export default {
  data() {
    return {
      myChart: null
    };
  },
  props: {
    dataObj:{
      type: Array,
      default: () => []
    }
  },
  mounted() {
    this.initChart([0]);
  },
  methods: {
    initChart(nums) {
      let myChart = echarts.init(this.$refs.chartPie);
      let rtColors = ["#FF7780", "#FFCE80", "#63CDFF", "#AC9EFF","#FD8E6E", "#FE6FD0","#A4FE80"]
      let option = {
        animation: false,
        series: [
          {
            name: "inrate",
            type: "pie",
            center: ["50%", "50%"],
            radius: ["65%", "90%"],
            itemStyle: {
              normal: {
                label: {
                  show: false,
                },
                labelLine: {
                  show: false,
                },
                borderColor: "#102C63",
                borderWidth: "3",
              },
            },
            data: nums,
          },
        ],
        color: rtColors,
      };
      myChart.setOption(option);
    },
    updateChart(){
      this.initChart(this.dataObj)
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
