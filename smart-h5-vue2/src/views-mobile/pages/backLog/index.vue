<!--
- @name 待办事项
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-12
-->

<template>
  <div class="dorm">
    <page2Title :info="titleInfo"></page2Title>
    <page2List :list="listArr"></page2List>
  </div>
</template>

<script>
// import iconVisitor from './img/icon_visitor.png'
import iconGoods from './img/icon_goods.png'
// import iconLeave from './img/icon_leave.png'
// import iconAppeal from './img/icon_appeal.png'
import iconDormQuit from './img/icon_dorm_quit.png'
import iconRepair from './img/icon_repair.png'

import bg from './img/bg.png'
import page2Title from '@/views-mobile/components/page2-title'
import page2List from '@/views-mobile/components/page2-list'

export default {
  components: {
    page2Title,
    page2List
  },
  data() {
    return {
      titleInfo: {
        title: '待办事项',
        imgSrc: bg
      },
      listArr: [
        // {
        //   imgSrc: iconVisitor,
        //   title: '访客审批',
        //   tip: '',
        //   pageSrc: ''
        // },
        {
          imgSrc: iconGoods,
          title: '物品放行（生活区）',
          tip: '',
          pageSrc: '/xuchang/backLog/goodReleaseLive'
        },
        // {
        //   imgSrc: iconGoods,
        //   title: '物品放行（办公区）',
        //   tip: '',
        //   pageSrc: '/xuchang/backLog/goodReleaseWork'
        // },
        // {
        //   imgSrc: iconLeave,
        //   title: '离职交接审批',
        //   tip: '',
        //   pageSrc: ''
        // },
        // {
        //   imgSrc: iconAppeal,
        //   title: '员工申诉回复',
        //   tip: '',
        //   pageSrc: ''
        // },
        {
          imgSrc: iconDormQuit,
          title: '退宿审批',
          tip: '',
          pageSrc: '/xuchang/backLog/dormExit'
        },
        {
          imgSrc: iconRepair,
          title: '园区报修审批',
          tip: '',
          pageSrc: '/xuchang/backLog/dormRepairs'
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
