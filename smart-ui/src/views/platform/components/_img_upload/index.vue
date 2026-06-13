<!--
- @name 图片上传
- @author yang.chuan <yang.chuan@bjtce.com>
- @date 2020-10-15
-->

<template>
  <div class="upload-image" v-loading="uploadLoading">
    <el-upload
      ref="upload"
      action=""
      list-type="picture-card"
      accept="image/png, image/jpeg"
      :show-file-list="false"
      :http-request="httpRequest"
    >
      <div class="face" v-if="photo">
        <img :src="photo" />
      </div>
      <i v-else class="el-icon-plus avatar-uploader-icon"></i>
    </el-upload>
    <canvas
      ref="canvas"
      class="canvas"
      :width="canvasWidth"
      :height="canvasHeight"
      :style="{ width: canvasWidth + 'px', height: canvasHeight + 'px' }"
    ></canvas>
  </div>
</template>

<script>
import loadLrz from '@/util/load-lrz'
const MAX_WIDTH = 1000
const MAX_HEIGHT = 1000
export default {
  components: {},
  data() {
    return {
      uploadLoading: false,
      face: '',
      prefix: 'data:image/jpeg;base64,',
      canvasWidth: 1000,
      canvasHeight: 1000,
      showCachePreview: true
    }
  },
  computed: {
    photo() {
      if (!this.previewImage && !this.face) {
        return ''
      }
      if (this.showCachePreview && this.previewImage) {
        return this.previewImage
      }
      return `${this.prefix}${this.face}`
    }
  },
  props: {
    previewImage: String,
    needCheckFace: {
      type: Boolean,
      default: function () {
        return true
      }
    },
  },
  watch: {
    face(val) {
      this.showCachePreview = false
    }
  },
  methods: {
    /**
     * 清除列表
     */
    clearFiles() {
      this.$refs.upload.clearFiles()
      this.face = ''
    },
    async httpRequest(file) {
      try {
        this.uploadLoading = true
        const lrz = await loadLrz()
        const res = await lrz(file.file, { width: MAX_WIDTH, height: MAX_HEIGHT, quality: 1 })
        let base64 = await this.anyImageToJPG(res.base64)
        base64 = base64.split(',')[1]
        this.face = base64
        this.$emit('input', base64)
        this.$emit('complete',base64)
        this.uploadLoading = false
      } catch (error) {
        this.$message({
          message: error.message,
          type: "error"
        })
        this.face = ''
      }
      this.uploadLoading = false
    },
    anyImageToJPG(base64Str) {
      const that = this
      if (base64Str.indexOf('data:image/jpeg') > -1) {
        return Promise.resolve(base64Str)
      }
      return new Promise((resolve, reject) => {
        let canvas = this.$refs.canvas
        let context = canvas.getContext("2d")
        let image = new Image()
        image.src = base64Str
        image.onload = function () {
          that.canvasWidth = image.width
          that.canvasHeight = image.height
          context.drawImage(image, 0, 0, that.canvasWidth, that.canvasHeight);
          const data = canvas.toDataURL("image/jpeg", 0.7);
          resolve(data)
        }
      })
    }
  },
  filters: {},
  /**
   * 生命周期 created
   */
  created() { },
  /**
   * 生命周期 mounted
   */
  mounted() { },
  /**
   * 生命周期 beforeDestroy
   */
  beforeDestroy() { }
}
</script>

<style lang="scss" scoped>
.upload-image {
  width: 100%;
  height: 100%;
  > div {
    width: 100%;
    height: 100%;
  }
  ::v-deep {
    .el-upload--picture-card {
      width: 100%;
      height: 100%;
      background-color: transparent;
      border: none;
    }
  }
}
.canvas {
  position: fixed;
  top: -9999px;
  left: -9999px;
}

.face {
  width: 100%;
  height: 100%;
  > img {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain;
    background: #fff;
  }
}
</style>
