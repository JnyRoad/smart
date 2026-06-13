<template>
  <div class="page3">
    <div class="page-title">刷新动态码</div>
    <div class="page-title">需完成人脸识别</div>
    <div class="business-form-item__value">
      <!-- <div class="image-item" v-if="imgSrc">
        <img :src="imgSrc" mode="contain" />
        <div class="option" @click="del(index)"></div>
      </div> -->
      <div class="image-item-camera">
        <componentCamera :framingImg="framingImg" @getImage="getImage" ref="camera" :uploadServer="uploadServer"></componentCamera>
      </div>
    </div>

    <div class="page-title" v-if="imgSrc">人脸对比成功</div>
    <button @click="Refresh" class="tce-button tce-button--primary is-plain" v-if="imgSrc" style="width: 50%; margin: 30px auto">生成动态码</button>
    <div class="page-title" v-else>请拍照</div>
  </div>
</template>

<script>
import store from '@/store'
import { postImageSave } from '@/services/upload'
import { updatePwd } from '@/services/dorm'
import framingImg from './_img/img-bg.jpg'
import componentCamera from '@/components/camera/index'
export default {
  components: {
    componentCamera
  },
  data() {
    return {
      imgSrc: null,
      uploadServer: postImageSave, // 通用
      // uploadServer: checkFaceAndCut, //人脸
      framingImg: framingImg,
      havePhoto: false,
      memberPhoto: '' // 随行人员base64
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    async Refresh() {
      const baseInfo = store.getters.userInfoBase
      const employeeBadge = baseInfo.employeeBadge
      const obj = {
        badge: employeeBadge,
        facePic: this.imgSrc
      }
      const res = await updatePwd(obj)
      if (res.code === 0) {
        this.$createDialog({
          type: 'alert',
          title: '',
          content: '刷新动态码成功！',
          icon: 'cubeic-alert',
          onConfirm: () => {
            this.$router.push({
              path: '/xuchang/dorm/lock'
            })
          }
        }).show()
      } else {
        this.$createDialog({
          type: 'alert',
          title: '错误',
          content: res.msg,
          icon: 'cubeic-alert'
        }).show()
      }
    },
    getImage(data) {
      this.imgSrc = data.resultData.base64
    },
    async del(index) {
      const that = this
      this.$createDialog({
        type: 'confirm',
        title: '',
        content: '是否移除此图片',
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
          that.imgSrc = ''
        }
      }).show()
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
.page3 {
  // background: $TCE-Background-Grey;
  width: 100%;
  min-height: 100%;
  padding: rem(180) rem(20) rem(190);
  text-align: center;
  .page-title {
    margin-top: rem(30);
    font-size: 18px;
  }
  .business-form-item__value {
    margin: rem(30) auto;
    background: #fff;
    width: rem(300);
    height: rem(300);
    // padding: rem(50);
    display: flex;
    justify-content: flex-end;
    &::after {
      content: '';
      display: table;
      clear: both;
    }
  }
  .image-item,
  .image-item-camera {
    width: rem(300);
    height: rem(300);
    float: left;
    position: relative;
    background: #f1f1f1;
    img {
      width: 100%;
      height: 100%;
      overflow: hidden;
    }
    .option {
      position: absolute;
      top: -8px;
      right: -8px;
      width: 20px;
      height: 20px;
      background-image: url(./_img/my-application-del.png);
      background-size: 100% 100%;
      overflow: hidden;
    }
  }
}
</style>
