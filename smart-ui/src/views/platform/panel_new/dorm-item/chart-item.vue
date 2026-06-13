
<template>
  <div class="chart-wrap" ref="chartPie"></div>
</template>

<script>
import echarts from "echarts";
export default {
  data() {
    return {};
  },
  props: {
    dataObj:{
      type: Array,
      default: () => []
    }
  },
  watch: {
    dataObj: {
      handler: "updateChart",
      immediate: false,
      deep: true,
    },
  },
  mounted() {
    this.initChart([{name:'', value:0}]);
  },
  methods: {
    initChart(dataArr) {
      let myChart = echarts.init(this.$refs.chartPie);
      let option = {
        color: ["#FA645C", "#49D7D9"],
        tooltip: {
          trigger: "item",
          formatter: "{a} <br/>{b} : {c} ({d}%)",
          position: ['50%', '50%']
        },
        series: [
          {
            name: '宿舍动态',
            type: "pie",
            label: {
              show: false,
            },
            radius: [0, 60],
            // data: [
            //   { value: 20, name: "国宝" },
            //   { value: 30, name: "治安" },
            // ],
            data: dataArr
          },
        ],
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
