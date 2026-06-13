<!--
- @name 公告-详情
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-09-13
-->

<template>
  <div class="page3">
    <div v-html="detailInfo.bbsContent" v-if="!isPdf"></div>
    <pdf :src="detailInfo.previewUrl" v-else></pdf>
  </div>
</template>

<script>
import { getBbsDetail } from '@/services/home'
import pdf from 'vue-pdf'
export default {
  components: {
    pdf
  },
  data() {
    return {
      detailInfo: {},
      isPdf: false
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    /**
     * 获取详情
     */
    async getBbsDetail() {
      this.$loading.show()
      const res = await getBbsDetail(this.$route.query.id)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.detailInfo = res.data
        if (!this.isPdf) {
          this.imgClick()
        }
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    imgClick() {
      // 为img添加点击事件
      const _this = this
      setTimeout(function () {
        const oImg = document.getElementsByClassName('page3')[0].getElementsByTagName('img')
        if (oImg.length > 0) {
          for (let i = 0; i < oImg.length; i++) {
            _this.imageUrlArry.push(oImg[i].currentSrc)
            oImg[i].onclick = function () {
              _this.imageUrl = oImg[i].currentSrc
              _this.showImagePreview(i)
            }
          }
          // console.log('img222', _this.imageUrlArry)
        }
      }, 1000)
    },
    showImagePreview(index) {
      this.$createImagePreview({
        imgs: this.imageUrlArry,
        initialIndex: index
      }).show()
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.getBbsDetail(
      this.isPdf = this.$route.query.isPdf
    )
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
  .page3{
    background: $TCE-Background-Grey;
    width: 100%;
    min-height: 100%;
    padding: rem(20);
  }
</style>
