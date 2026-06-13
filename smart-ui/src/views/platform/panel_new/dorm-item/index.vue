
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t">宿舍动态</div>
      </div>
      <div class="wrap_cont">
        <div class="t1">
          <div :class="{'t1n_1':!isRoom}" @click="changeTab(true)">
            <div class="t1n">
              <span class="num">{{ usedRoom }}</span>
              <span class="num1">/ {{ dataObj.roomCount}}</span>
            </div>
            <div>房间数量（间）</div>
          </div>
          <div :class="{'t1n_1':isRoom}" @click="changeTab(false)">
            <div class="t1n">
              <span class="num">{{ usedBed }}</span>
              <span class="num1">/ {{ dataObj.bedCount}}</span>
            </div>
            <div>床位数量（个）</div>
          </div>
        </div>
          <div class="t2">
            <div class="chart">
              <chart ref="myChart" :dataObj="chartData"></chart>
            </div>
            <template v-if="isRoom">
              <div class="t2_r">
                <div>
                  <div class="t2n">
                    <span class="num">{{ dataObj.roomFreeCount }}</span>
                    <span class="num1">{{ dataObj.roomFreeCount | getRate(dataObj.roomCount)}}%</span>
                  </div>
                  <div class="posi_c"><i class="block_i posi_i"></i>空置房间</div>
                </div>
                <div>
                  <div class="t2n">
                    <span class="num">{{ usedRoom }}</span>
                    <span class="num1">{{ usedRoom | getRate(dataObj.roomCount)}}%</span>
                  </div>
                  <div class="neg_c"><i class="block_i neg_i"></i>已使用房间</div>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="t2_r">
                <div>
                  <div class="t2n">
                    <span class="num">{{ dataObj.bedFreeCount }}</span>
                    <span class="num1">{{ dataObj.bedFreeCount | getRate(dataObj.bedCount)}}%</span>
                  </div>
                  <div class="posi_c"><i class="block_i posi_i"></i>空置床位</div>
                </div>
                <div>
                  <div class="t2n">
                    <span class="num">{{ usedBed }}</span>
                    <span class="num1">{{ usedBed | getRate(dataObj.bedCount)}}%</span>
                  </div>
                  <div class="neg_c"><i class="block_i neg_i"></i>已使用床位</div>
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
      chartData: [],
      chartDataBed: [],
      chartDataRoom: [],
      isRoom: true //默认显示房间信息
    }
  },
  props: {
    dataObj: {
      type: Object,
      default: () => ({}),
    },
  },
  watch: {
    dataObj: {
      handler: "initData",
      immediate: false,
      deep: true,
    }
  },
  computed:{
    usedBed(){
      return this.dataObj.bedCount - this.dataObj.bedFreeCount
    },
    usedRoom(){
      return this.dataObj.roomCount - this.dataObj.roomFreeCount
    }
  },
  filters: {
    getRate(num, total){
      return getProportion(num, total)
    }
  },
  mounted() {},
  methods: {
    changeTab(e){
      if(e){
        this.isRoom = true
        this.chartData = this.chartDataRoom
      }else{
        this.isRoom = false
        this.chartData = this.chartDataBed
      }
    },
    initData() {
      // 重构数据结构,易于处理
      if (this.dataObj) {
        this.chartDataBed = [{
          name: '已使用床位',
          value: this.usedBed
        },{
          name: '空置床位',
          value: this.dataObj.bedFreeCount
        }]

        this.chartDataRoom = [{
          name: '已使用房间',
          value: this.usedRoom
        },{
          name: '空置房间',
          value: this.dataObj.roomFreeCount
        }]

        this.chartData = this.chartDataRoom
      }
    },
    updateChart() {
      this.$refs.myChart? this.$refs.myChart.updateChart(): '';
    }
  }
}
</script>
<style lang="scss" scoped>
  @use "@/styles/platform/panel_new/variables" as *;
  .wrap{
    width: 389px;
    height: 277px;
    background-image: url('/img/panel_new/park/bg_5.png');
    &_cont{
      display: flex;
      flex-direction: column;
      padding: 15px 20px 15px;
      .t1{
        display: flex;
        margin-bottom: 15px;
        >div{
          flex: 1;
          text-align: center;
          cursor: pointer;
        }
      }
      .t2{
        display: flex;
        flex: 1;
        >div{
          flex: 1;
        }
        &_r{
          display: flex;
          flex-direction: column;
          justify-content: space-around;
          padding-left: 10px;
        }
      }
    }
    .t1n{
      margin-bottom: 5px;
      .num{
        font-size: 20px;
        margin-top: -5px;
      }
      .num1{
        font-size: 16px;
        padding: 0 0 0 10px;
        opacity: 0.3;
      }
    }
    .t1n_1{
      color: #0d3264;
      .num, .num1{
        color: #0d3264;
        opacity: 1;
      }
    }
    .posi_i{
      background: $posi_c;
    }
    .neg_i{
      background: $neg_c;
    }
    .t2n{
      margin-bottom: 5px;
      padding-left: 20px;
      .num{
        font-size: 16px;
        margin-top: -5px;
      }
      .num1{
        font-size: 16px;
        padding: 0 0 0 20px;
      }
    }
  }
</style>
