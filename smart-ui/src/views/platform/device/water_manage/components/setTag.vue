<template>
  <el-dialog
    ref="dialog"
    title="设置标签"
    :visible.sync="currVisible"
    width="600px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <div class="tagList">
      <el-checkbox-group v-model="ckTags">
        <el-checkbox v-for="item in tagList" :label="item.id" :key="item.id" border>{{item.tagName}}</el-checkbox>
      </el-checkbox-group>
      <tceEmpty v-if="!tagList || tagList.length === 0" style="padding-top: 50px;"></tceEmpty>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading" :disabled="!ckTags || ckTags.length===0">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getTagList, setTagApi} from '../_service'
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      tagList: [],
      ckTags: []
    }
  },
  props: {
    visible: Boolean,
    deviceIds: Array
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        this.getList()
      }
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
    async getList() {
      const res = await getTagList()
      this.tagList = res.data.data
    },
    async formSumit() {
      await this.validateForm()
      await this.executeOnceSubmit({
        promise: setTagApi({
          meterIds: this.deviceIds,
          tagIds: this.ckTags
        })
      })
      this.refresh()
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.ckTags = []
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.ckTags = []
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
  .tagList{
    min-height: 200px;
    margin-bottom: 20px;
  }
  .el-checkbox ::v-deep {
    margin-bottom: 10px;
    margin-right: 10px;
    .el-checkbox__input{
      display: none;
    }
  }
  .el-checkbox.is-bordered+.el-checkbox.is-bordered{
    margin-left: 0;
    margin-right: 10px;
  }
  .el-checkbox.is-bordered.is-checked{
    border-color: #ed6d00;
  }
</style>
