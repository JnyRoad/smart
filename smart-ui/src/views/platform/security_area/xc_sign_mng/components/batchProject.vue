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
      <el-checkbox-group v-model="projectIds">
        <el-checkbox v-for="(item, index) in projectList" :label="item.id" :key="index">
          {{item.securityName}}
        </el-checkbox>
      </el-checkbox-group>
      <div class="noData" v-if="!projectList || projectList.length===0" style="height: 45px;">当前园区无权限策略内容</div>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { xcProjectApi } from '../../xc_project_mng/_service'
export default {
  mixins: [tce.mixins.executeOnce],
  components: {
  },
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      projectIds: [],
      projectList: [],
    }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Array
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
        this.getProjectList()
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
      if(this.projectIds.length===0){
        this.$message.error('请选择要设置的保密项目')
        return
      }
      let arr = []
      this.itemObj.forEach(el=>{
        arr.push(
          {
            securityId: this.projectIds,
            staffBadge: el.badge,
            staffId: el.staffId,
            staffName: el.name
          }
        )
      })
      await this.executeOnceSubmit({
        promise: xcProjectApi.addPersonToProject(arr)
      })
      this.refresh()
    },
    /**
     * 获取权限列表
     */
    async getProjectList() {
      const res = await xcProjectApi.getListAll()
      if(res.data.data && res.data.data.length>0){
        this.projectList = res.data.data
      }else{
        this.projectList = []
      }
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.projectIds = []
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.projectIds = []
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
