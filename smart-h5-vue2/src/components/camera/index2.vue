<!--
- @name 图片上传
- @author yang.chuan <yang.chuan@bjtce.com>
- @date 2020-11-26
-->

<template>
  <div class="camera-view">
    <!-- 相机ai -->
    <div class="camera">
      <img class="camera-mask" v-if="!preview" :src="framingImg || iconCamera" />
      <img class="camera-img" mode="contain" model="scaleToFill" v-if="preview" :src="preview" />
      <!-- 选择相片 -->
      <div class="camera-cover-view">
        <!-- <div class="camera-cover-view-mask"></div> -->
        <!-- <image v-show="preview" class="camera-frame" :src="preview ? iconCamera : placeholderImg"></image> -->
        <input type="file" ref="file" accept="image/*" class="camera-cover__file" @change="fileChange" />
      </div>
      <div class="camera-complete" v-show="preview"></div>
    </div>
  </div>
</template>

<script>
import placeholderImg from './_img/none.png'
import iconCamera from './_img/icon-camera.png'
import lrz from 'lrz'
export default {
  components: {},
  data() {
    return {
      placeholderImg: placeholderImg,
      iconCamera: iconCamera,
      imageData: null
    }
  },
  computed: {
    preview() {
      if (this.imageData) {
        return this.imageData.base64
      }
      if (this.propImg) {
        return this.propImg
      }
      return ''
    }
  },
  props: {
    framingImg: String,
    isUseData64: {
      type: Boolean,
      default: true
    },
    scaleWidth: {
      type: Number,
      default: 800
    },
    scaleHeight: {
      type: Number,
      default: 800
    },
    /**
     * 图片获取方式
     * 0: 相框 1:拍照 2:[相框，拍照]
     */
    sourceType: {
      type: Number,
      default: 2
    },
    propImg: {
      type: String,
      default: ''
    },
    uploadServer: Function,
    isCompress: {
      type: Boolean,
      default: true
    }
  },
  watch: {},
  methods: {
    /**
     * 上传图片
     */
    async fileChange(file) {
      const res = await lrz(file.target.files[0], {
        width: this.scaleWidth,
        height: this.scaleHeight,
        quality: 0.7,
        fieldName: 'file'
      })

      if (this.uploadServer) {
        const toast = this.$createToast({
          time: 30000,
          txt: '图片正在上传'
        })
        toast.show()
        const uploadRes = await this.uploadServer(res)
        toast.hide()
        this.imageData = res
        if (uploadRes.code === 1) {
          this.$emit('input', uploadRes)
          this.$emit('getImage', {
            content: uploadRes,
            resultData: res
          })
        } else {
          file.target.value = ''
          this.cameraReset()
          this.$createToast({
            type: 'text',
            time: 1500,
            txt: `${uploadRes.message}`
          }).show()
        }
      } else {
        this.$emit('input', res)
        this.$emit('getImage', {
          content: res,
          resultData: res
        })
      }
    },
    cameraReset() {
      this.imageData = null
    }
  },
  filters: {},
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
  beforeDestroy() {
    this.imageData = null
  }
}
</script>

<style lang="scss" scoped>
.camera-view {
  width: 100%;
  height: 100%;
  background: #f1f1f1;
}

.camera {
  position: relative;
  width: 100%;
  height: 100%;
  margin: 0 auto;
  background: #f1f1f1;
}

.camera.active {
  .camera-mask {
    &::before,
    &::after {
      display: none;
    }
  }
}

.camera-img {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 10;
  width: 100%;
  height: 100%;
  background: #f1f1f1;
}

.camera-mask {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 10;
  width: 100%;
  height: 100%;
  background-size: 100% 100%;
  &.card {
  }
}

.camera-cover-view {
  position: absolute;
  z-index: 20;
  width: 100%;
  height: 100%;

  .camera-cover-view-mask {
    position: absolute;
    left: 0;
    top: 0;
    z-index: 10;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.4);
  }

  .camera-frame {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    margin: auto;
    z-index: 20;
    width: 160upx;
    height: 160upx;
  }
}

.camera {
  camera {
    position: absolute;
    z-index: 9;
    width: 100%;
    height: 100%;
  }
}

.camera-take__picture {
  position: relative;
  width: px2upx(130px);
  height: px2upx(130px);
  margin: 0 auto;
  background: #509eff;
  border-radius: 50%;

  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    z-index: 1;
    width: px2upx(69px);
    height: px2upx(52px);
    margin-top: px2upx(-52px/2);
    margin-left: px2upx(-69px/2);
    // background: url(~@/static/app/icons/icon-camera.png) no-repeat 0 0;
    background-size: 100% 100%;
  }
}

.camera-canvas {
  position: absolute;
  top: px2upx(-9900upx);
  left: px2upx(-9990upx);
}

.tip {
  text-align: center;
  margin: 56upx;

  text {
    color: #666;
    font-size: 24upx;
  }
}

.camera-complete {
  width: 20px;
  height: 20px;
  background: url(./_img/icon-camera-ok.png) no-repeat center center;
  position: absolute;
  top: -8px;
  right: -8px;
  background-size: 100%;
  z-index: 50;
}

.camera-cover__file {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  font-size: 0;
  opacity: 0;
}
</style>
