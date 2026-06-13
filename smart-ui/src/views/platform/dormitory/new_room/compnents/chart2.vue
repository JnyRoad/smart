
<template>
  <div class="wrap-outer">
    <template v-if="noData">
      <div class="noData">
        <span>暂无统计信息</span>
      </div>
    </template>
    <template v-else>
      <swiper :options="swiperOption" v-if="arrData.length>0">
        <swiper-slide ref="mySwiper" class="swiper-slide" v-for="(item,index) in arrData" :key="index">
          <div class="wrap">
            <div class="wrap_left">
              <chart :dataObj="item.chartData" :ref="'myChart'+index" />
            </div>
            <div class="wrap_right">
              <div class="c_top">
                {{item.dormName}}
              </div>
              <div class="c_btm">
                <div class="cbtm_i">
                  <div class="t2">总床位</div>
                  <div class="t1" :title="item.total">{{item.total|formatNumber}}</div>
                </div>
                <div class="cbtm_i">
                  <div class="t2">已入住</div>
                  <div class="t1" :title="item.usedNum">{{item.usedNum|formatNumber}}</div>
                </div>
                <div class="cbtm_i">
                  <div class="t2">未入住</div>
                  <div class="t1" :title="item.freeNum">{{item.freeNum|formatNumber}}</div>
                </div>
              </div>
            </div>
          </div>
        </swiper-slide>
      </swiper>
      <div class="swiper-button-prev"></div>
      <div class="swiper-button-next"></div>
    </template>
  </div>
</template>

<script>
import { swiper, swiperSlide } from "vue-awesome-swiper";
import "swiper/dist/css/swiper.css";
import chart from "./chart-item";

export default {
  components: {
    chart,
    swiper,
    swiperSlide,
  },
  data() {
    return {
      noData: true,
      swiperOption: {
        slidesPerView: 'auto',
        spaceBetween: 20,
        observer: true,
        observeParents: true,
        loop: false,
        navigation: {
          nextEl: ".swiper-button-next",
          prevEl: ".swiper-button-prev"
        },
        // autoplay: {
        //   delay: 1500,
        //   disableOnInteraction: false,
        // },
      },
      arrData: [],
    };
  },
  props: {
    dataObj: {
      type: Array,
      default: function(){
        return []
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
      if (this.dataObj && this.dataObj.length>0) {
        this.noData = false
        this.arrData = []
        this.dataObj.forEach(el => {
          let usedNum = el.totalUseNumber
          let freeNum = el.total - el.totalUseNumber
          let chartData = {
            nums: [usedNum, freeNum], //已入住，未入住
            total: el.total,
          }
          let obj = {
            total: el.total,
            usedNum: usedNum,
            freeNum: freeNum,
            dormName: el.dormitoryName,
            chartData: chartData
          }
          this.arrData.push(obj)
        });
      }else{
        this.noData = true
      }
    },
    updateChart() {
      // this.arrData.forEach((el, index) => {
      //   this.$refs['myChart'+index].updateChart();
      // });
    }
  }
};
</script>

<style lang="scss" scoped>
.wrap-outer{
  position: relative;
  padding: 0 94px 0 84px;
  display: flex;
  justify-items: center;
  height: 100%;
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
  .swiper-container{
    width: 100%;
    padding: 0 20px;
  }
  .swiper-slide{
    width: 340px;
  }
  .swiper-button-prev, .swiper-container-rtl .swiper-button-next{
    background-image: none;
    left: 27px;
  }
  .swiper-button-next, .swiper-container-rtl .swiper-button-prev{
    background-image: none;
    right: 25px;
  }
  .swiper-button-prev, .swiper-button-next{
    margin-top: -16px;
    padding: 20px;
  }
  .swiper-button-prev::before{
    position: absolute;
    left: 4px;
    top: 16px;
    content: '';
    display: inline-block;
    border: 10px solid #ed6d00;
    border-top: 6px solid transparent;
    border-left: 10px solid transparent;
    border-bottom: 6px solid transparent;
  }
  .swiper-button-next::before{
    position: absolute;
    left: 17px;
    top: 16px;
    content: '';
    display: inline-block;
    border: 10px solid #ed6d00;
    border-top: 6px solid transparent;
    border-right: 10px solid transparent;
    border-bottom: 6px solid transparent;
  }
  .swiper-button-prev.swiper-button-disabled,
  .swiper-button-next.swiper-button-disabled{
    opacity: 1;
  }
  .swiper-button-prev.swiper-button-disabled::before{
    border-right: 10px solid #c0c0c0;
  }
  .swiper-button-next.swiper-button-disabled::before{
    border-left: 10px solid #c0c0c0;
  }
}
.wrap {
  width: 340px;
  height: 100%;
  padding: 20px 10px 20px 0;
  display: flex;
  &_left {
    width: 180px;
  }
  &_right {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    overflow: hidden;
    padding: 0 20px 0 10px;
    .c_top{
      font-size: 18px;
      color: #333;
      font-weight: bold;
    }
    .c_btm{
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      .cbtm_i{
        display: flex;
        align-items: baseline;
        overflow: hidden;
      }
      .t1{
        font-size: 20px;
        padding-bottom: 3px;
        flex: 1;
        text-align: right;
        overflow: hidden;
        text-overflow:ellipsis;
        white-space: nowrap;
      }
      .t2{
        font-size: 14px;
        margin-right: 10px;
        width: 46px;
      }
    }
  }
}
</style>
