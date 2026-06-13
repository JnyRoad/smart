
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t">组织构成与员工分布</div>
      </div>
      <div class="wrap_cont">
        <div class="c_left">
          <chart :dataObj="chartData" ref="myChart" />
        </div>
        <div class="c_right">
          <swiper :options="swiperOption" v-if="lineData.length>0">
            <swiper-slide ref="mySwiper" class="swiper-slide" v-for="(item,index) in lineData" :key="index">
              <div class="c_i" :key="index">
                <i class="block_i posi_0" :class="'posi_'+index"></i>
                <span class="c_i_title">{{item.name}}</span>
                <div class="c_i_p">
                  <el-progress
                    :percentage="item.ratios"
                    color="#63C1FF"
                    :show-text="false"
                    :stroke-width="10"
                  ></el-progress>
                </div>
                <span class="c_i_num num">{{item.value}}</span>
                <span class="c_i_num2">{{item.ratios}}%</span>
              </div>
            </swiper-slide>
          </swiper>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { swiper, swiperSlide } from "vue-awesome-swiper";
import "swiper/dist/css/swiper.css";
import chart from "./chart-item";
import { getProportion } from "@/views/platform/panel_new/util.js";

export default {
  components: {
    chart,
    swiper,
    swiperSlide,
  },
  data() {
    return {
      nameArr: [],
      numArr: [],
      totalNum: "",
      chartData: {},
      lineData: [],
      swiperOption: {
        direction: "vertical",
        slidesPerView: 6,
        spaceBetween: 10,
        observer: true,
        observeParents: true,
        loop: true,
        autoplay: {
          delay: 1500,
          disableOnInteraction: false,
        },
      }
    };
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
          this.nameArr.push(item.compName);
          this.numArr.push(item.vlaue);
        });

        // 人工计算，未采用接口数据
        this.totalNum = this.numArr.reduce((acc, item) => {
          return acc + item;
        });

        this.chartData = {
          nums: this.numArr,
          total: this.totalNum,
        };

        let ratioSum = 0;

        // 计算比例 最后一位比例为补位项
        let l = this.numArr.length
        this.lineData = []
        this.numArr.forEach((item, i) => {
          let ratios = 0;
          let _s = getProportion(item, this.totalNum) * 100; // 转为整数
          if (i == l - 1) {
            ratios = (100 * 100 - ratioSum) / 100; // 直接补位
          } else {
            ratioSum += _s;
            ratios = _s / 100;
          }
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
};
</script>

<style lang="scss" scoped>
.wrap {
  width: 880px;
  height: 250px;
  background-image: url("/img/panel_new/view/bg_3.png");
  &_top {
    &_t {
      width: auto !important;
    }
  }
  &_cont {
    display: flex;
    padding: 0 10px 10px 10px;
    overflow: hidden;
  }
  .c_left {
    width: 165px;
  }
  .c_right {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    padding: 0 20px 5px;
    .c_i {
      display: flex;
      align-items: center;
      padding: 0 10px;
      margin-bottom: 5px;
      &_title {
        width: 120px;
        margin-right: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      &_p {
        flex: 1;
        margin-right: 3px;
      }
      &_num {
        margin-right: 8px;
        width: 55px;
        text-align: right;
        font-weight: normal;
      }
      &_num2 {
        width: 55px;
        text-align: right;
      }
      .block_i {
        margin-top: 0;
      }
      .posi_0,
      .posi_7,
      .posi_14 {
        background: #ff7780;
      }
      .posi_1,
      .posi_8,
      .posi_15 {
        background: #63cdff;
      }
      .posi_2,
      .posi_9,
      .posi_16 {
        background: #ac9eff;
      }
      .posi_3,
      .posi_10,
      .posi_17 {
        background: #fd8e6e;
      }
      .posi_4,
      .posi_11,
      .posi_18 {
        background: #ffce80;
      }
      .posi_5,
      .posi_12,
      .posi_19 {
        background: #fe6fd0;
      }
      .posi_6,
      .posi_13,
      .posi_20 {
        background: #a4fe80;
      }
    }
  }
}
::v-deep .swiper-container {
  width: 100%;
}
</style>
