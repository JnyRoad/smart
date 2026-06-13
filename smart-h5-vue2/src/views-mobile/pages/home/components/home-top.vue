<!--
- @name 首页-顶部
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-10
-->

<template>
  <div class="home_top">
    <!-- <div class="park-info" @click="showPark">
      <span class="">{{parkInfo.parkName}}</span>
      <span class="-arrow-solid_bottom_gray"></span>
    </div> -->
    <div class="park-info">
      <span class="">{{parkInfo.parkName}}</span>
    </div>
    <div class="weather-info" v-if="weatherInfo.city">{{weatherInfo.city}} {{weatherInfo.forecast[0].type}} {{weatherInfo.wendu}}°C {{weatherInfo.forecast[0].fengxiang}}</div>
    <div class="staff-info" @click="toMine">
      <span>{{info.employeeId}}</span>
      <span>{{info.employeeName}}</span>
      <span class="-arrow-solid_right_fff"></span>
    </div>
    <parkList ref="parkList" @done="changePark"></parkList>
  </div>
</template>

<script>
import hm1 from '../img/hm_1.png'
import parkList from './park-list'
import store from '@/store'
import { getWeather } from '@/services/home'

export default {
  components: {
    parkList
  },
  data() {
    return {
      img1: hm1,
      weatherInfo: {},
      parkInfo: {}
    }
  },
  computed: {},
  props: {
    list: {
      type: Array,
      default: function() {
        return []
      }
    },
    info: {
      type: Object,
      default: function() {
        return {}
      }
    }
  },
  watch: {
    list: {
      handler() {},
      immediate: true
    }
  },
  methods: {
    /**
     * 个人中心
     */
    toMine() {
      this.$router.push({
        path: '/xuchang/mine'
      })
    },
    async getWeather() {
      const res = await getWeather({
        city: '许昌'
      })
      if (res.status === 1000) {
        this.weatherInfo = res.data
      }
    },
    /**
     * 切换园区-显示
     */
    showPark() {
      this.$refs.parkList && this.$refs.parkList.open()
    },
    /**
     * 切换园区-确定
     */
    changePark(obj) {
      this.parkInfo = obj
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.parkInfo = store.getters.parkInfo
    this.getWeather()
  },
  /**
   * 生命周期 mounted
   */
  mounted() {},
  /**
   * 生命周期 beforeDestroy
   */
  beforeDestroy() {}
}
</script>

<style lang="scss" scoped>
  .home_top{
    height: rem(290);
    background: url('../img/hm_top.png') no-repeat;
    background-position: right bottom;
    background-size: auto 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-around;
    align-items: flex-start;
    padding: rem(30) 0;
    .park-info{
      font-size: 20px;
      padding-left: rem(30);
      display: flex;
      align-items: center;
      .-arrow-solid_bottom_gray{
        width: 1px;
        vertical-align: middle;
        margin: 0 0 rem(-25) rem(20);
      }
    }
    .weather-info{
      color: #abb2c4;
      padding-left: rem(30);
    }
    .staff-info{
      display: inline-block;
      height: rem(60);
      line-height: rem(60);
      padding: 0 rem(30);
      color: #fff;
      border-radius: 0 rem(30) rem(30) 0;
      background: $TCE-Color;
      span{
        padding-right: rem(30);
      }
    }
  }
</style>
