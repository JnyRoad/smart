<template>
  <div class="business-form-item clearance upload-image" v-if="!disable">
    <div class="business-form-item__label">
      <div class="-no-required" :class="{ '-required': required }">{{ label }}</div>
    </div>
    <div class="business-form-item__value">
      <div class="image-item" v-if="formData">
        <img :src="formData" mode="contain"/>
        <div class="option" @click="del(index)"></div>
      </div>
      <div class="image-item-camera" v-else>
        <componentCamera :framingImg="framingImg" @getImage="getImage" ref="camera" :uploadServer="uploadServer"></componentCamera>
      </div>
    </div>
  </div>
</template>

<script>
import { postImageSave } from '@/services/upload'
import framingImg from './_img/img-bg.jpg'
import mixin from './mixin.js'
import componentCamera from '@/components/camera/index'

export default {
  mixins: [mixin],
  components: {
    componentCamera
  },
  data() {
    return {
      uploadServer: postImageSave,
      framingImg: framingImg,
      formData: ''
    }
  },
  computed: {
    computedFormOption() {
      return Object.assign(
        {
          limit: this.limit
        },
        this.formOption
      )
    }
  },
  props: {
    placeholder: {
      type: String,
      default: '请输入'
    },
    limit: {
      type: [Number, String],
      default: 3
    }
  },
  methods: {
    reset() {
      this.formData = ''
    },
    getImage(data) {
      this.formData = data.resultData.base64
      // this.$nextTick(() => {
      //   this.$refs.camera && this.$refs.camera.cameraReset()
      // })
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
          that.formData = ''
        }
      }).show()
    }
  }
}
</script>

<style lang="scss" scoped>
.business-form-item {
  position: relative;
  display: flex;
  @include border-bottom();
  &.is-noborder {
    &::after {
      border-bottom-color: transparent;
    }
  }
  .business-form-item__label {
    margin-left: 10px;
    line-height: 45px;
    height: 45px;
    font-size: 14px;
    color: #666666;
    > div {
      display: inline-block;
      vertical-align: middle;
    }
  }
  .business-form-item__value {
    background: #fff;
    padding: 10px 12px 10px 18px;
    display: flex;
    justify-content: flex-end;
    &::after {
      content: '';
      display: table;
      clear: both;
    }
  }
  .business-input__inner {
    line-height: 45px;
    height: 45px;
    text-align: right;
    padding: 0 10px;
  }
  .limit {
    margin-left: 5px;
    color: #c0c0c0;
    font-size: 14px;
    line-height: 1;
    font-size: 12px;
  }
}

.image-item,
.image-item-camera {
  width: rem(100);
  height: rem(100);
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
.image-item + .image-item,
.image-item + .image-item-camera {
}
</style>
