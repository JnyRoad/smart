<template>
  <el-dialog
    ref="dialog"
    :title="title"
    :visible.sync="currVisible"
    width="600px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <div class="list">
      <div class="row" v-for="(item, index) in authList" :key="index">
        {{item.authorityName}}
        <el-button type="text" @click="ckAuth(item)" style="margin-left: 15px;">查看</el-button>
      </div>
    </div>
    <div class="noData" v-if="!authList || authList.length===0" style="height: 45px;">当前园区无权限策略内容</div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
    <AuthesDialog v-if="itemObj" title="查看权限策略详情" :itemObj="curAuth" :parkId="itemObj.parkId" ref="AuthesDialog"/>

  </el-dialog>
</template>

<script>
import AuthesDialog from './authes'

export default {
  mixins: [tce.mixins.executeOnce],
  components: {
    AuthesDialog
  },
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      authList: [],
      curAuth: {}
    }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Object
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
        if(this.itemObj && this.itemObj.id){
          this.authList = []
          this.itemObj.authLists.forEach(el => {
            this.authList.push({
              id: el.authId,
              authorityName: el.authName
            })
          });
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
     * 查看权限
     */
    ckAuth(item){
      this.curAuth = item
      this.$refs.AuthesDialog && this.$refs.AuthesDialog.open()
    },
    cancel() {
      this.authList = []
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.authList = []
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
  .list ::v-deep {
    min-height: 200px;
    margin-bottom: 30px;
    .row{
      margin-bottom: 15px;
    }
  }
</style>
