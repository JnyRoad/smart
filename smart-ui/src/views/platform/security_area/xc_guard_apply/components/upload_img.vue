<template>
  <el-dialog
    ref="dialog"
    :title="title"
    :visible.sync="currVisible"
    width="500px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog- xc_guard_apply'"
  >
    <div class="img-dv">
      <div class="staff-img-outer">
        <imgUpload
          ref="imgUpload1"
          @complete="imgUpload"
        />
      </div>
      <div class="tips">建议上传200K大小以内的jpg，jpeg 照片</div>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="addSubmit" :loading="btnLoading" :disabled="!curObj.detailId || !curObj.faceBase64">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { xcGuardApplyApi } from '../_service'
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      curObj: {
        detailId: '',
	      faceBase64: ''
      }
    }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Object
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      }else{
        if(this.itemObj && this.itemObj.id){
          this.curObj.detailId = this.itemObj.id
        }
      }
    },
    itemObj:{
      handler(){},
      immediate: true
    }
  },
  methods: {
    /**
     * 验证表单
     */
    validateForm() {
      if (this.$refs.form) {
        return this.$refs.form.validate()
      }
      return Promise.resolve()
    },
    /*
     * 选择上传照片
     */
    imgUpload(e) {
      this.curObj.faceBase64 = e
    },
    /**
     * 提交
     */
    async addSubmit(){
      await this.executeOnceSubmit({
        promise: xcGuardApplyApi.reUploadImg(this.curObj)
      })
      this.refresh()
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.imgUpload1 && this.$refs.imgUpload1.clearFiles()
      this.curObj = {
        detailId: '',
	      faceBase64: ''
      }
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.imgUpload1 && this.$refs.imgUpload1.clearFiles()
      this.curObj = {
        detailId: '',
	      faceBase64: ''
      }
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
  .form{
    margin-bottom: 40px;
  }
  .img-dv ::v-deep {
    text-align: center;
    .tips{
      color: #999;
      font-size: 12px;
      margin-top: 10px;
      margin-bottom: 30px;
    }
    .el-upload--picture-card i{
      width: 100%;
      height: 100%;
      background: url('/img/print_peaple.png') no-repeat;
      background-size: 100% 100%;
      line-height: auto;
      &::before{
        display: none;
      }
    }
  }
  .staff-img-outer{
    width: 200px;
    height: 200px;
    margin: 0 auto;
    border: 1px solid #e0e0e0;
  }
</style>
