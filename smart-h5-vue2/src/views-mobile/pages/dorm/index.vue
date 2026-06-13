<!--
- @name 我的宿舍
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-12
-->

<template>
  <div class="dorm">
    <page2Titel :info="titleInfo"></page2Titel>
    <page2List :list="listArr"></page2List>
  </div>
</template>

<script>
import iconLock from './img/icon_lock.png'
import iconWater from './img/icon_water.png'
import bg from './img/bg.png'
import page2Titel from '@/views-mobile/components/page2-title'
import page2List from '@/views-mobile/components/page2-list'

export default {
  components: {
    page2Titel,
    page2List
  },
  data() {
    return {
      titleInfo: {
        title: '我的宿舍',
        imgSrc: bg
      },
      listArr: [
        {
          imgSrc: iconLock,
          title: '门锁动态码',
          tip: '智能门锁动态码开门',
          pageSrc: '/xuchang/dorm/lock'
        },
        {
          imgSrc: iconWater,
          title: '水电扣费明细',
          tip: '查询每月宿舍水电扣费明细',
          pageSrc: '/xuchang/dorm/waterElec'
        }
      ]
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {

  },
  /**
   * 生命周期 created
   */
  created() {},
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
.dorm{
  background: $TCE-Background-Grey;
  width: 100%;
  min-height: 100%;
}
</style>
