
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t">访客分析</div>
      </div>
      <div class="wrap_cont">
        <div class="chart">
          <chart :dataObj="numArr" ref="myChart"/>
        </div>
        <div class="legend">
          <template v-for="(item, index) in lineData">
            <div :key="index">
              <div class="tip">
                <i class="block_i i_0" :class="'i_'+index"></i>
                <span>{{item.name}}</span>
              </div>
              <div>
                <span class="num">{{item.ratios}}%</span>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import chart from './chart-item'
import { getProportion } from "@/views/platform/panel_new/util.js";
export default {
  components: {
    chart
  },
  data() {
    return {
      nameArr: [],
      numArr: [],
      totalNum: "",
      chartData: [],
      lineData: [],
    }
  },
  props: {
    dataObj: {
      type: Array,
      default: () => [],
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
      if (this.dataObj) {
        this.nameArr = [];
        this.numArr = [];
        this.totalNum = 0;
        this.dataObj.forEach((item) => {
          this.nameArr.push(item.causeDesc);
          this.numArr.push(item.causeCount);
        });

        // 人工计算，未采用接口数据
        this.totalNum = this.numArr.reduce((acc, item) => {
          return acc + item;
        });

        this.chartData = this.numArr
        let ratioSum = 0;

        // 计算比例 最后一位比例为补位项
        let l = this.numArr.length
        this.lineData = []
        this.numArr.forEach((item, i) => {
          let ratios = 0;
          let _s = getProportion(item, this.totalNum) * 100; // 转为整数

          ratioSum += _s;
          ratios = _s / 100;

          // if (i == l - 1) {
          //   ratios = (100 * 100 - ratioSum) / 100; // 直接补位
          // } else {
          //   ratioSum += _s;
          //   ratios = _s / 100;
          // }
          this.lineData.push({
            ratios: ratios,
            value: item,
            name: this.nameArr[i],
          });
        });
      }
    },
    updateChart() {
      this.$refs.myChart.updateChart();
    }
  }
}
</script>

<style lang="scss" scoped>
  .wrap{
    width: 240px;
    height: 296px;
    background-image: url('/img/panel_new/person/bg_5.png');
    &_top{
      line-height: 32px !important;
    }
    .num{
      width: 62px;
    }
    &_cont{
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: 10px 0 15px 20px;
      .chart{
        height: 110px;
      }
      .legend{
        flex: 1;
        display: flex;
        flex-direction: column;
        // flex-wrap: wrap;
        padding-top: 5px;
        // border: 1px dashed red;
        .block_i{
          margin-top: 0;
        }
        >div{
          // width: 50%;
          display: flex;
          justify-content: space-between;
          margin-bottom: 3px;
          .num{
            font-weight: normal;
            color: #a6d4ff;
          }
          .tip{
            padding-left: 19px;
            font-size: 14px;
            color: #448fd5;
          }
          .i_0{
            background: #FF7780;
          }
          .i_1{
            background: #FFCE80;
          }
          .i_2{
            background: #63CDFF;
          }
          .i_3{
            background: #AC9EFF;
          }
          .i_4{
            background: #FD8E6E;
          }
          .i_5{
            background: #FE6FD0;
          }
          .i_6{
            background: #A4FE80;
          }
          // border: 1px solid blue;
        }
      }
    }
  }
</style>
