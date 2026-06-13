
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t">车辆进入时段分析</div>
        <div class="top_r">
          <div class="posi_in"><i class="block_i in_i"></i>进入园区</div>
          <div class="posi_out"><i class="block_i out_i"></i>离开园区</div>
        </div>
      </div>
      <div class="wrap_cont">
        <chart :dataObj="dataObj" ref="myChart"/>
      </div>
    </div>
  </div>
</template>

<script>
import chart from './chart-item'
export default {
  components: {
    chart
  },
  data() {
    return {
    }
  },
  props: {
    dataObj: {
      type: Object,
      default: () => ({}),
    },
  },
  mounted() {},
  methods: {
    updateChart() {
      this.$refs.myChart.updateChart();
    }
  }
}
</script>

<style lang="scss" scoped>
  .wrap{
    width: 524px;
    height: 297px;
    background-image: url('/img/panel_new/person/bg_7.png');
    &_top{
      display: flex;
      height: 38px !important;
      line-height: 38px !important;
      justify-content: space-between;
      .top_r{
        display: flex;
        padding-right: 20px;
        font-size: 14px;
        .posi_in{
          color: #FFCE80;
          margin-right: 40px;
          .in_i{
            background: #FFCE80;
          }
        }
        .posi_out{
          color: #3ACBBD;
          .out_i{
            background: #3ACBBD;
          }
        }
      }
      &_t{
        width: auto !important;
      }
    }
    .num{
      width: 62px;
    }
    &_cont{
      display: flex;
      flex-wrap: wrap;
      align-content: space-around;
      padding: 15px 0 5px 0;
    }
  }
</style>
