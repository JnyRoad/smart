<template>
  <el-dialog
    ref="dialog"
    :title="title"
    :visible.sync="currVisible"
    width="800px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <div class="cks">
      <div class="item" v-for="(item, index) in projectList" :key="index">
        <div class="row1">
          <div>{{item.securityCode}}</div>
          <div>{{item.securityName}}</div>
          <div>{{item.createTime}}</div>
        </div>
        <div class="row2" v-if="item.authNameList && item.authNameList.length>0">
          <el-tag v-for="(item2, index2) in item.authNameList" :key="index2">{{item2}}</el-tag>
        </div>
        <div class="btns">
          <el-button type="text" @click="handelDel(item)" icon="el-icon-delete"></el-button>
        </div>
      </div>
      <div class="noData" v-if="!projectList || projectList.length===0" style="height: 45px;">该员工无保密项目信息</div>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { xcSignMngApi } from "../_service"
import { xcProjectApi } from '../../xc_project_mng/_service'
export default {
  mixins: [tce.mixins.executeOnce, tce.mixins.list],
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
     * 获取权限列表
     */
    async getProjectList() {
      const res = await xcSignMngApi.getProjectsByStaffId(this.itemObj.staffId)
      if(res.data.data && res.data.data.length>0){
        this.projectList = res.data.data
      }else{
        this.projectList = []
      }
    },
    /**
     * 删除
     */
    async handelDel(row) {
      let obj =  {
        staffBadge: this.itemObj.badge,
        securityId: row.id
      }
      await this.mixinMsgDel('确认要移除该保密项目信息？')
      const res = await xcProjectApi.delPersonFromProjectBatch( obj )
      if(res.data.code===0){
        this.getProjectList()
        this.$emit('refresh')
        this.$notify({
          title: "批量删除成功",
          message: "批量删除成功",
          type: "success",
          duration: 2000
        });
      }else{
        this.$message.error(res.data.message);
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
  ::v-deep .el-dialog__body {
    padding: 20px;
  }
  .cks ::v-deep {
    .item{
      position: relative;
      box-shadow: 0 0 6px rgba(0,0,0,.1);
      padding: 15px 20px;
      margin-bottom: 15px;
      .row1{
        display: flex;
        >div{
          margin-right: 30px;
        }
      }
      .row2{
        padding-top: 20px;
        .el-tag{
          margin: 0 10px 10px 0;
        }
      }
      .btns{
        position: absolute;
        top: 5px;
        right: 10px;
      }
    }
  }
</style>
