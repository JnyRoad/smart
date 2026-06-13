<template>
  <el-dialog
    ref="dialog"
    :title="title"
    :visible.sync="currVisible"
    width="700px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <div class="cks" style="padding-bottom: 30px;">
      <el-tree
        :data="authList"
        ref="limitree"
        node-key="id"
        show-checkbox
        default-expand-all
        :highlight-current="true"
        :check-strictly="true"
        :default-checked-keys="authIds"
        :props="defaultProps"
      ></el-tree>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getTreePerson, getObj } from "@/api/platform/area/limit";
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      authIds: [],
      authList: [],
      defaultProps: {
        children: "children",
        label: "label"
      }
    }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Object,
    parkId: [String, Number]
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
        if(this.itemObj && this.itemObj.id){
          this.getAuthList(this.parkId)
          this.getDetail(Number(this.itemObj.id))
        }
      }
    },
    itemObj:{
      handler(){},
      immediate: true
    },
  },
  methods: {
    /**
     * 获取权限策略详情
     */
    async getDetail(id){
      const res = await getObj(id)
      if(res.data.data){
        this.authIds = res.data.data.checkedlimits
      }else{
        this.authIds = []
      }
    },
    /**
     * 获取权限列表
     */
    async getAuthList(parkId) {
      const res = await getTreePerson(parkId)
      this.authList = res.data.data
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
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
  .cks ::v-deep {
    .el-checkbox+.el-checkbox{
      margin-left: 0;
    }
    .el-checkbox{
      margin-right: 20px;
    }
  }
</style>
