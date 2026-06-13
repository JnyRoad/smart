<template>
  <div class="page3">
    <div class="page-header">
      <div class="label">问题详情</div>
    </div>
    <div class="page3-list">
      <div v-html="detailInfo.answerContent"></div>
      <!-- <uParse :content="contentStr"></uParse> -->
    </div>
  </div>
</template>
<script>
// import { getWaterElecData } from '@/services/dorm'
import { getDetail } from '@/services/help'
export default {
  data() {
    return {
      detailInfo: {},
      contentStr: '',
      imageUrl: null,
      imageUrlArry: []
    }
  },
  methods: {
    async getDetail() {
      this.$loading.show()
      const res = await getDetail(this.$route.query.id)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.detailInfo = res.data
        this.imgClick()
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    imgClick() {
      // 为img添加点击事件
      const _this = this
      setTimeout(function () {
        const oImg = document.getElementsByClassName('page3-list')[0].getElementsByTagName('img')
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
    this.getDetail()
  },
  /**
   * 生命周期 mounted
   */
  mounted() {},
  /**
   * 生命周期 beforeDestroy
   */
  beforeDestroy() {},
  computed: {},
  props: {},
  watch: {}
}
</script>
<style lang="scss" scoped>
.page3 {
  background: $TCE-Background-Grey;
  width: 100%;
  height: 100%;
  //   padding: 0 rem(20) rem(20);
  .page-header {
    height: 48px;
    line-height: 48px;
    background: #fff;
    border-bottom: 1px solid #ccc;
    display: flex;
    .label {
      width: 100%;
      text-align: center;
      color: #0f74f0;
    }
  }
  .page3-list {
    height: calc(100% - 50px);
    padding: 16px;
    .item {
      background: #fff;
      border-radius: 5px;
      padding: rem(30) rem(30) rem(20);
      margin-bottom: rem(20);
      .item-title {
        display: flex;
        justify-content: space-between;
        margin-bottom: rem(30);
        &_left {
          font-size: 16px;
          font-weight: bold;
        }
      }
      .item-info {
        &_row {
          display: flex;
          margin-bottom: rem(20);
        }
        &_label {
          color: #333;
          width: rem(240);
        }
        &_value {
          flex: 1;
          padding-left: rem(20);
          text-align: right;
        }
        .page3_item {
          margin: rem(16) 0;
          .item_label {
            color: #333;
            width: rem(240);
            float: left;
            font-weight: bold;
          }
          .item_value {
            -webkit-box-sizing: border-box;
            box-sizing: border-box;
            position: relative;
            text-align: right;
          }
        }
      }
    }
  }
}
</style>
