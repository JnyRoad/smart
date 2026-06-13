<template>
  <div class="remarks">
    <div class="remarks-top">
      <div class="top-left">
        {{ label }}
      </div>
      <div class="top-right">
        <input class="uni-input" placeholder="请输入" style="text-align: right" v-model="formData.valuablesRemark" />
      </div>
    </div>
    <div class="remarks-bottom">
      <div class="remarks-bottom-item" v-for="(item, index) in formData.imgBase64Arr" :key="index">
        <div class="del-img" @click="delImg(index)"></div>
        <img mode="aspectFill" :src="item" class="uni-image" />
      </div>
      <div class="remarks-bottom-item">
        <camera-uni :framingImg="bgImg" style="height: 100%" @getImage="getImage" ref="camera" :uploadServer="otherImageSave"></camera-uni>
      </div>
    </div>
  </div>
</template>

<script>
import mixin from './mixin.js'
import cameraUni from '@/components/camera/index.vue'
import bgImg from './_img/img-bg.jpg'
import { otherImageSave } from '@/services/uploadImage'

export default {
  mixins: [mixin],
  components: {
    cameraUni
  },
  props: {
    label: {
      type: String,
      default: '物品备注'
    }
  },
  data() {
    return {
      bgImg: bgImg,
      otherImageSave: otherImageSave,
      formData: {
        valuablesRemark: '',
        imgBase64Arr: [],
        idArr: []
      }
    }
  },
  watch: {
    'formData.valuablesRemark': function (val) {
      this.formData.valuablesRemark = val

      this.syncFormData(this.formData)
    }
  },
  methods: {
    getImage(data) {
      this.$refs.camera.cameraReset()
      let imgBase64 = data.resultData.base64

      let id = data.content.data
      this.formData.imgBase64Arr.push(imgBase64)
      this.formData.idArr.push(id)
      this.syncFormData(this.formData)
      this.$refs.camera.cameraReset()
    },
    delImg(index) {
      this.formData.imgBase64Arr.splice(index, 1)
      this.formData.idArr.splice(index, 1)
      this.syncFormData(this.formData)
    }
  }
}
</script>

<style lang="scss">
.remarks {
  // height: 310rpx;
  background-color: #fff;
  padding-top: 25px;
  padding-bottom: 25px;
  padding-left: 12px;

  .remarks-top {
    font-size: 14px;
    display: flex;
    justify-content: space-between;
    padding-left: 12px;
    padding-right: 12px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eeeeee;

    .top-left {
      color: #666;
    }

    .top-right {
      color: #c0c0c0;
    }
  }

  .remarks-bottom {
    margin-left: 12px;
    display: flex;
    flex-wrap: wrap;

    .remarks-bottom-item {
      position: relative;
      margin-top: 15px;
      margin-right: 15px;
      width: 70px;
      height: 70px;
      border-radius: 7px;
      background: #edf2f9;

      .del-img {
        background: url('../_img/my-application-del.png');
        width: 23px;
        height: 23px;
        background-size: 100%;
        position: absolute;
        right: -6px;
        top: -7px;
      }

      .uni-image {
        width: 70px;
        height: 70px;
        border-radius: 7px;
        background: #edf2f9;
      }
    }
  }
}
</style>
