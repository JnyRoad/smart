<template>
  <el-upload class="tce-upload-img" action accept="image/png, image/jpeg" :auto-upload="true" ref="upload" list-type="picture-card" :http-request="httpRequest">
    <i class="el-icon-plus"></i>
  </el-upload>
</template>

<script>
import loadLrz from '@/util/load-lrz'
export default {
  name: 'tce-upload-img',
  data() {
    return {
      files: []
    }
  },
  props: {
    width: {
      type: Number,
      default: 1000
    },
    height: {
      type: Number,
      default: 1000
    },
    filter: {
      type: Function,
      default: function (data) {
        return data
      }
    }
  },
  methods: {
    async httpRequest(file) {
      const that = this
      let imageData = {}
      try {
        that.$refs.upload.clearFiles()
        that.files = []
        const lrz = await loadLrz()
        const rst = await lrz(file.file, {
          width: this.width,
          height: this.height,
          fieldName: 'file'
        })

        imageData = {
          status: 1,
          url: rst.base64,
          prefix: rst.base64.split(',')[0],
          base64: rst.base64.split(',')[1]
        }
        that.files.push(imageData)

        this.$emit('input', that.filter(that.files))
        this.$emit('uploadAfter', that.files)
      } catch (error) {
        this.$message.warning(`图片无法处理`)
      }
    },
    getImageList() {
      return this.files
    }
  }
}
</script>

<style lang="scss" scoped>
.tce-upload-img ::v-deep {
  line-height: 1;
  display: inline-block;
  .el-upload-list--picture-card .el-upload-list__item {
    width: 85px;
    height: 85px;
    // border: 1px solid red;
  }
  .el-upload--picture-card {
    width: 85px;
    height: 85px;
    line-height: 90px;
    // border: 1px solid blue;
  }
}
</style>
