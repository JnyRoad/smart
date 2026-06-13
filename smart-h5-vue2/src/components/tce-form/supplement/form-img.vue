<template>
  <div class="item-img">
    <div class="left-box">
      <div class="left-top">
        <div :class="{ '-required': required }">{{ label }}</div>
      </div>
      <div class="left-info">
        {{ info }}
      </div>
    </div>
    <div class="right-img">
      <camera-uni ref="refCamera" :framingImg="bgImg" style="height: 100%" @getImage="getImage" :propImg="getImgeUrl(valueData)" :uploadServer="getType"></camera-uni>
    </div>
  </div>
</template>

<script>
import mixin from './mixin.js'
import cameraUni from '@/components/camera/index2.vue'
import { facePhotoImageSave, otherImageSave, cardImageSave } from '@/services/uploadImage'
import { GET_IMAGE_URL } from '@/conf'
import bgImg from './_img/img-bg.jpg'
/**
 * 获取浏览图片地址
 */
const getImgeUrl = function (id) {
  // return `${GET_IMAGE_URL}/${id}?access_token=${getToken()}`;
  return `${GET_IMAGE_URL}/${id}`
}

export default {
  mixins: [mixin],
  components: {
    cameraUni
  },
  props: {
    // 描述信息
    info: {
      type: String,
      required: true
    },
    imgType: {
      type: String
      // required: true
    },
    idObj: {
      type: Object,
      default: function () {
        return {}
      }
    }
  },
  data() {
    return {
      bgImg: bgImg
    }
  },
  computed: {
    getType: function () {
      if (this.imgType === 'other') {
        // 其他图片
        return otherImageSave
      }
      if (this.imgType === 'card') {
        // 其他图片
        let idObj = this.idObj
        return function (obj) {
          return cardImageSave({ enterpriseId: idObj.enterpriseId, parkId: idObj.parkId, obj: obj })
        }
      } else {
        // 人脸图片
        return facePhotoImageSave
      }
    }
  },
  methods: {
    postImgFun(imgType) {},
    getImgeUrl(id) {
      if (!id) {
        return ''
      }
      return getImgeUrl(id)
    },

    getImage(data) {
      // 获取图片的base64
      if (this.imgType === 'card') {
        this.formData = {
          fileId: data.content.data.fileId,
          idCardData: data.content.data.idCardData
        }
        this.$emit('updataCard', this.formData)
        return
      }
      let id = data.content.data
      this.formData = id
    }
  }
  // mounted() {
  //   if (this.imgType === 'other') {
  //     // 其他图片
  //     this.$refs.refCamera.uploadServer = otherImageSave
  //   } else {
  //     // 人脸图片
  //     this.$refs.refCamera.uploadServer = facePhotoImageSave
  //   }
  // }
}
</script>

<style lang="scss" scoped>
.item-img {
  height: 100px;
  background: #fff;
  padding-left: 10px;
  padding-right: 25px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .left-box {
    .left-top {
      font-size: 14px;
      color: #333;
      font-weight: 600;
    }

    .left-info {
      font-size: 11px;
      color: #999;
      margin-top: 10px;
      margin-left: 12px;
    }
  }

  .right-img {
    width: 70px;
    height: 70px;
    border-radius: 7px;
    background: #edf2f9;
  }
}
</style>
