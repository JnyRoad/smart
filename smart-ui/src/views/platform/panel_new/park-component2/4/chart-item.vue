
<template>
  <div class="chart-wrap" ref="chartPie"></div>
</template>

<script>
import echarts from "echarts";
export default {
  data() {
    return {};
  },
  mounted() {
    let inN = [20, 30, 10];
    this.initChart(inN);
  },
  methods: {
    initChart(nums) {
      let myChart = echarts.init(this.$refs.chartPie);
      let rtColors = [
        "#FF7680",
        "#63CEFF",
        "#FFCD7F"
      ];
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
  },
};
</script>

<style lang="scss" scoped>
.chart-wrap {
  width: 100%;
  height: 100%;
}
</style>
