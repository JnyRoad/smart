<!--
- @name 首页-服务列表
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-10
-->

<template>
  <div class="menu-list">
    <div class="menu-item" @click="toPage(item)" v-for="(item, index) in list" :key="index">
      <div class="left">
        <div class="img">
          <tce-image :src="item.imgSrc" :width="0" :height="0"></tce-image>
        </div>
        <div class="title">{{item.title}}</div>
      </div>
      <span class="-arrow"></span>
    </div>
  </div>
</template>

<script>
import { APPIP } from '@/conf'
import { unbind } from '@/services/mine'
import store from '@/store'
export default {
  components: {},
  data() {
    return {
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
    toBadge () {
      let url = encodeURIComponent(`${window.location.origin}/#/xuchang/login/logon_badge`)
      window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?appid=${APPIP}&redirect_uri=${url}&response_type=code&scope=snsapi_base&state=123#wechat_redirect`
    },
    async toPage(item) {
      const me = this
      if (item.title === '微信解绑') {
        this.$createDialog({
          type: 'confirm',
          title: '',
          content: '是否确认解除微信绑定？',
          confirmBtn: {
            text: '确定',
            active: true,
            disabled: false,
            href: 'javascript:;'
          },
          cancelBtn: {
            text: '取消',
            active: false,
            disabled: false,
            href: 'javascript:;'
          },
          onConfirm: () => {
            me.unbind()
          }
        }).show()
      } else {
        this.$router.push({
          path: item.pageSrc
        })
      }
    },
    async unbind() {
      const res = await unbind()
      if (res.code === 0 && res.data) {
        this.$tceMobile.toast('解绑成功')
        store.commit('SET_ACCESS_TOKEN', '')
        this.toBadge()
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    }
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
  .menu-list{
    .menu-item{
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin: 0 0 rem(40);
      height: rem(110);
      background: #f1f3f5;
      border-radius: 5px;
      padding: 0 rem(30);
    }
    .left{
      display: flex;
      align-items: center;
    }
    .img{
      position: relative;
      width: rem(45);
      height: rem(45);
      display: inline-block;
      margin-right: rem(50);
    }
    .title{
      color: #333;
      font-size: 16px;
      margin: rem(20) 0;
    }
    .tip{
      color: #999;
      font-size: 12px;
    }
  }
</style>
