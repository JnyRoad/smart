<!--
- @name 首页-消息
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-09-13
-->

<template>
  <div class="home_msg" @click="toList">
    <div class="msg-icon"></div>
    <div class="msg-info -ellipsis">{{ bbsObj.bbsTitle }}</div>
    <div class="msg-time">
      <!-- <span>2021年08月10日1</span> -->
      <span class="-arrow"></span>
    </div>
    <!-- <span class="new-msg"></span> -->
  </div>
</template>

<script>
import { getBbsList } from '@/services/home'
import hm1 from '../img/hm_1.png'
import store from '@/store'

export default {
  components: {},
  data() {
    return {
      img1: hm1,
      parkInfo: {},
      bbsObj: {
        bbsObj: '暂无公告'
      }
    }
  },
  computed: {},
  props: {
    list: {
      type: Array,
      default: function() {
        return []
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
    toList() {
      this.$router.push({
        path: '/xuchang/home/bbs/list'
      })
    },
    async getBbsList() {
      let obj = {
        current: 1,
        size: 1,
        parkId: this.parkInfo.id
      }
      const res = await getBbsList(obj)
      if (res.code === 0 && res.data.total > 0) {
        this.bbsObj = res.data.records[0]
      } else {
        this.bbsObj = {
          bbsObj: '暂无公告'
        }
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.parkInfo = store.getters.parkInfo
    this.getBbsList()
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
  .home_msg{
    position: relative;
    display: flex;
    justify-content: space-between;
    padding: rem(20) rem(30);
    .msg-icon{
      width: rem(80);
      background: url('../img/hm_msg.png') no-repeat;
      background-size: auto 100%;
    }
    .msg-time{
      flex: none;
      width: rem(30);
      color: #c0c0c0;
      text-align: right;
    }
    .new-msg{
      position: absolute;
      top: rem(10);
      right: rem(10);
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: #ff4734;
    }
  }
</style>
