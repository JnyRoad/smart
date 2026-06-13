<!--
- @name 首页-服务列表
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-10
-->

<template>
  <div class="nav-list">
    <div class="nav-item" @click="toPage(item)" v-for="(item, index) in list" :key="index">
      <div class="img">
        <tce-image :src="item.imgSrc" :width="0" :height="0"></tce-image>
        <span class="num-out">
          <span class="num" v-if="item.num">{{item.num | flt_num}}</span>
        </span>
      </div>
      <div class="title">{{item.title}}</div>
      <div class="tip" v-if="item.tip">({{item.tip}})</div>
    </div>
  </div>
</template>

<script>
import hm1 from '../img/hm_1.png'
import { isNull } from '@tce/tce-util'
import wx from 'weixin-js-sdk'
import { getWxSignature } from '@/services/backLog'
import { APPIP } from '@/conf'
export default {
  components: {},
  data() {
    return {
      img1: hm1
    }
  },
  computed: {},
  filters: {
    flt_num(val) {
      if (!isNull.isNull(val)) {
        if (Number(val) > 99) {
          return '99+'
        } else {
          return val
        }
      } else {
        return ''
      }
    }
  },
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
    toPage(item) {
      // debugger
      if (item.title === '扫码放行') {
        this.onScan()
      } else {
        this.$router.push({
          path: item.pageSrc
        })
      }
    },
    toExitDetail(id) {
      this.$router.push({
        path: '/xuchang/backLog/dormExit/detail',
        query: {
          id: id,
          curTabIndex: 0,
          isScan: true
          // sort: item.sort
        }
      })
    },
    toWorkDetail(id) {
      this.$router.push({
        path: '/xuchang/backLog/goodReleaseWork/detail',
        query: {
          id: id,
          curTabIndex: 0,
          isScan: true
        }
      })
    },
    toLiveDetail(id) {
      this.$router.push({
        path: '/xuchang/backLog/goodReleaseLive/detail',
        query: {
          id: id,
          curTabIndex: 0,
          sort: 3,
          isScan: true
        }
      })
    },
    /**
     * 扫一扫
     */
    onScan() {
      this.getCofig()
      const me = this
      wx.ready(function () {
        wx.checkJsApi({
          // 需要使用的JS接口列表，在这里只需要用到scanQRCode
          jsApiList: ['scanQRCode'],
          success: function (res1) {
            if (res1.checkResult.scanQRCode) {
              // 当scanQRCode可使用时
              wx.scanQRCode({
                needResult: 1, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
                scanType: ['qrCode', 'barCode'], // 可以指定扫二维码还是一维码，默认二者都有
                success: function (res2) {
                  let result = res2.resultStr
                  const obj = JSON.parse(result)
                  if (obj.type === '6') {
                    me.toExitDetail(obj.id)
                  } else {
                    if (obj.type === '3-5') {
                      me.toWorkDetail(obj.id)
                    } else {
                      me.toLiveDetail(obj.id)
                    }
                  }
                  // {id: '', type:''}
                  // a = {id:'1451429288591634434',type:'6'}
                  // this.toScan(result);
                  // window.location.href = result;
                  // 也可以对扫描结果处理过之后再使用
                  // 比如可以这样使用：
                  // window.location.href = result.split('?')[0] + '/detail?' + result.split('?')[1]
                },
                error: function (response) {
                  me.$tceMobile.toast('扫码失败')
                }
              })
            }
          }
        })
      })
    },
    async getCofig() {
      let url = ''
      let ua = navigator.userAgent.toLowerCase()
      if (/iphone|ipad|ipod/.test(ua)) {
        url = window.location.href.split('#')[0]
      } else if (/android/.test(ua)) {
        url = window.location.href
      }
      // GetWeixinScan 后端提供
      // let WxAuthUrl = GetWeixinScan + '?url=' + url;
      let WxAuthUrl = url
      try {
        const res = await getWxSignature({ url: WxAuthUrl })
        if (res.code === 0) {
          // console.log(res.data.data, '这个是获取调用扫描返回的参数？~~');
          // 微信的配置~~
          this.wxConfig(res.data.timestamp, res.data.nonceStr, res.data.signature)
        } else {
          this.$tceMobile.toast(res.message || '微信配置失败')
        }
      } catch (e) {
        this.$tceMobile.toast('微信配置失败')
      }
    },
    /**
     * wx.config的配置
     */
    wxConfig(timestamp, nonceStr, signature) {
      wx.config({
        debug: false, // 开启调试模式,
        appId: APPIP, // 必填，企业号的唯一标识
        timestamp: timestamp, // 必填，生成签名的时间戳
        nonceStr: nonceStr, // 必填，生成签名的随机串
        signature: signature, // 必填，签名
        jsApiList: ['scanQRCode', 'checkJsApi'] // 必填，需要使用的JS接口列表
      })
      const me = this
      wx.error(function (res) {
        me.$tceMobile.toast(`微信配置错误：${res.errMsg}`)
      })
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
  .nav-list{
    display: flex;
    flex-wrap: wrap;
    padding: rem(30) rem(15) 0 rem(15);
    .nav-item{
      width: 25%;
      text-align: center;
      margin: 0 0 rem(30);
    }
    .img{
      position: relative;
      width: rem(82);
      height: rem(82);
      display: inline-block;
    }
    .num-out{
      position: absolute;
      top: -2px;
      right: 11px;
    }
    .num{
      position: absolute;
      top: 0;
      left: 0;
      display: inline-block;
      color: #fff;
      background: #E7292E;
      font-size: 12px;
      min-width: 17px;
      height: 17px;
      line-height: 13px;
      border-radius: 8px;
      text-align: center;
      padding: 0 4px 2px;
      border: 1px solid #fff;
      transform: scale(.9);
    }
    .title{
      margin: rem(20) 0;
      font-size: 12px;
    }
    .tip{
      color: #999;
      font-size: 12px;
    }
  }
</style>
