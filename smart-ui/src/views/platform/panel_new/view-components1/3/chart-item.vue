
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
      type: Object,
      default: () => ({})
    }
  },
  mounted() {
    this.initChart([0], 0);
  },
  methods: {
    initChart(nums, total) {
      let formatNumber = function (num) {
        let reg = /(?=(\B)(\d{3})+$)/g;
        return num.toString().replace(reg, ",");
      };
      this.myChart = echarts.init(this.$refs.chartPie);
      let rtColors = [
        "#FF7780",
        "#63CDFF",
        "#AC9EFF",
        "#FD8E6E",
        "#FFCE80",
        "#FE6FD0",
        "#A4FE80",
      ];
      let option = {
        animation: false,
        title: [
          {
            text:
              "{val|" + formatNumber(total) + "}\n{name|" + "员工总数" + "}",
            top: "center",
            left: "center",
            textStyle: {
              rich: {
                name: {
                  fontSize: 16,
                  fontWeight: "normal",
                  color: "#509fe8",
                  padding: [10, 0],
                },
                val: {
                  fontSize: 16,
                  color: "#a6d4ff",
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
