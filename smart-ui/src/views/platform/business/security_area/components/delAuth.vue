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
    <div class="cks">
      <el-checkbox-group v-model="authIds">
        <el-checkbox v-for="(item, index) in authList" :label="item.authId" :key="index">
          {{item.authName}}
        </el-checkbox>
      </el-checkbox-group>
      <div class="noData" v-if="!authList || authList.length===0" style="height: 45px;">当前无内容</div>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">删 除</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  mixins: [tce.mixins.executeOnce],
  components: {
  },
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      authIds: [],
      authList: [],
    }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Object,
  },
  created() {
  },
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        this.getAuthList()
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
    /**
     * 提交
     */
    async formSumit() {
      this.addSubmit()
    },
    /**
     * 添加
     */
    async addSubmit(){
      if(this.authIds.length===0){
        this.$message.error('请选择要删除的权限')
        return
      }
      this.authIds.forEach(el1=>{
        let temp = this.authList.filter(el=>{
          return el1===el.authId
        })
        let index = this.authList.indexOf(temp[0])
        this.authList.splice(index, 1)
      })
      this.itemObj.authList = this.authList
      this.close()
    },
    /**
     * 获取权限列表
     */
    async getAuthList() {
      this.authList = this.itemObj.authList
    },
    cancel() {
      this.authIds = []
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.authIds = []
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
    min-height: 200px;
    .el-checkbox+.el-checkbox{
      margin-left: 0;
    }
    .el-checkbox{
      margin-right: 20px;
      margin-bottom: 15px;
    }
  }
</style>
