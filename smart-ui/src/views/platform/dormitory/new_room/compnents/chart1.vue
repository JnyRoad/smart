
<template>
  <div class="wrap">
    <template v-if="noData">
      <div class="noData">
        <span>暂无统计信息</span>
      </div>
    </template>
    <template v-else>
      <div class="wrap_left">
        <chart :dataObj="chartData" ref="myChart" />
      </div>
      <div class="wrap_right">
        <div class="c_top">
          总体统计
        </div>
        <div class="c_btm">
          <div class="cbtm_i">
            <div class="t1" :title="dataObj.total">{{dataObj.total|formatNumber}}</div>
            <div class="t2">总床位</div>
          </div>
          <div class="cbtm_i">
            <div class="t1" :title="freeNum">{{freeNum|formatNumber}}</div>
            <div class="t2">未入住</div>
          </div>
          <div class="cbtm_i">
            <div class="t1" :title="dataObj.actualNumber">{{dataObj.actualNumber|formatNumber}}</div>
            <div class="t2">已入住</div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import chart from "./chart-item";

export default {
  components: {
    chart,
  },
  data() {
    return {
      freeNum: 0, //未入住
      chartData: {},
      noData: true
    };
  },
  props: {
    dataObj: {
      type: Object,
      default: function(){
        return null
      }
    },
  },
  watch: {
    dataObj: {
      handler: "initData",
      immediate: false,
      deep: true,
    },
  },
  mounted() {},
  methods: {
    initData() {
      // 重构数据结构,易于处理
      if (this.dataObj&&this.dataObj.parkName) {
        this.noData = false
        this.freeNum = this.dataObj.total - this.dataObj.actualNumber
        let numArr = [this.dataObj.actualNumber, this.freeNum] //已入住，未入住
        this.chartData = {
          nums: numArr,
          total: this.dataObj.total,
        };
      }else{
        this.noData = true
      }
    },
    updateChart() {
      this.$refs.myChart.updateChart();
    }
  }
};
</script>

<style lang="scss" scoped>
.wrap {
  width: 100%;
  height: 100%;
  padding: 20px;
  display: flex;
  .noData{
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    span{
      display: inline-block;
      width: 200px;
      height: 180px;
      padding-top: 140px;
      background: url('/img/dorm_hd.png') no-repeat;
      background-position: top center;
    }
  }
  &_left {
    width: 180px;
  }
  &_right {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    overflow: hidden;
    padding: 0 15px 0 15px;
    .c_top{
      font-size: 18px;
      color: #333;
      font-weight: bold;
    }
    .c_btm{
      display: flex;
      justify-content: space-between;
      .cbtm_i{
        flex: 1;
        overflow: hidden;
        padding-right: 3px;
      }
      .t1{
        font-size: 24px;
        padding-bottom: 3px;
        overflow: hidden;
        text-overflow:ellipsis;
        white-space: nowrap;
      }
      .t2{
        font-size: 16px;
      }
    }
  }
}
</style>
