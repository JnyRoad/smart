<template>
<div>
  <div class="tce-image" :class="{ error: loadError }" @click="viewImage" :previewImage="previewImage" :style="{ height: height === 0 ? '100%' : height + 'px', width: width === 0 ? '100%' : width + 'px' }">
    <img :src="imageUrl" :mode="mode" @load="load" @error="error"/>
  </div>
  <cube-popup type="my-popup" ref="myPopup">
    <img :src="imageUrl" :mode="mode" @load="load" @error="error" @click="hideImage"/>
  </cube-popup>
</div>
</template>

<script>
import placeholder from './_img/none.png'
import { GET_IMAGE_URL } from '@/conf'

/**
 * 获取浏览图片地址
 */
const getImgeUrl = function(id) {
  return `${GET_IMAGE_URL}/${id}`
}

const filter = function(src) {
  if (!src) {
    return '/_ERRORIMAGE'
  }
  if (src.indexOf('base64,') > -1 || src.indexOf('/') === 0 || src.indexOf('.') === 0 || src.indexOf('//') > -1 || src.indexOf('http') === 0) {
    return src
  } else {
    return getImgeUrl(src)
  }
}

export default {
  data() {
    return {
      imageUrl: placeholder,
      loadError: false
    }
  },
  props: {
    src: {
      type: String,
      default: ''
    },
    mode: {
      type: String,
      default: 'contain'
    },
    lazyLoad: {
      type: Boolean,
      default: true
    },
    placeholder: {
      type: String,
      default: ''
    },
    previewImage: {
      type: Boolean,
      default: false
    },
    width: {
      type: Number,
      default: 200
    },
    height: {
      type: Number,
      default: 200
    }
  },
  watch: {
    src() {
      this.loadError = false
      this.imageUrl = filter(this.src || this.placeholder)
    }
  },
  methods: {
    viewImage() {
      if (this.previewImage) {
        const component = this.$refs.myPopup
        component.show()
      }
    },
    hideImage() {
      const component = this.$refs.myPopup
      component.hide()
    },
    load() {
      this.$emit('load')
    },
    error() {
      this.loadError = true
      this.$emit('error')
    }
  },
  mounted() {
    this.imageUrl = filter(this.src || this.placeholder)
  }
}
</script>

<style lang="scss">
.tce-image {
  display: block;
  width: 100%;
  height: 100%;
  &.error {
    background-repeat: no-repeat;
    background-position: center center;
    background-size: auto auto;
  }

  img {
    width: 100%;
    height: 100%;
    display: block;
  }
}
</style>
