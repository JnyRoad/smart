<template>
  <el-dialog
    ref="dialog"
    title="标记"
    :visible.sync="currVisible"
    width="700px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <div class="r_table">
      <div class="r_tr" v-for="(item, index) in dataList" :key="index">
        <div class="r_td r_type">{{item.reasonTypeDesc}}</div>
        <div class="r_td">{{ item.startTime }}</div>
        <div class="r_td">{{ item.endTime }}</div>
        <div class="r_td r_menu">
          <el-button type="text" @click="editRemark(item)">编辑</el-button>
          <el-button type="text" @click="doDel(item)">删除</el-button>
        </div>
      </div>
      <div v-if="!dataList || dataList.length===0" class="noData">暂无备注内容</div>
    </div>
    <div slot="footer" class="dialog-footer">
      <div style="float: left">
        <el-button type="primary" @click="addRemark" plain>添 加</el-button>
      </div>
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
    <!-- 添加备注 -->
    <addRemark ref="addremark" :checkInObj="dataItem" :remarkItem="curRemarkItem" @refresh="refreshList"/>
  </el-dialog>
</template>

<script>
import { remarkList, remarkDel } from "@/api/platform/dormitory/bed_mng"
import addRemark from './_add_remark'
export default {
  components:{
    addRemark
  },
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      dataList: [],
      curRemarkItem: {}
    }
  },
  props: {
    visible: Boolean,
    dataItem: Object
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      }
    },
    dataItem:{
			handler: function(val){
        if(val && val.id){
          this.getList(val.id)
        }
      },
			immediate: true
		}
  },
  methods: {
    //添加备注
    addRemark(row){
      this.$refs.addremark && this.$refs.addremark.open()
    },
    //编辑备注
    editRemark(row){
      this.curRemarkItem = row
      this.$refs.addremark && this.$refs.addremark.open()
    },
    doDel(row){
      var _this = this;
      const elm = this.$createElement;
      this.$msgbox({
        message: elm("p", { attrs: { class: "smallp" } }, [
          elm("i", { attrs: { class: "smallInfo delInfo" } }, ""),
          elm("span", null, "确认删除该备注信息？ ")
        ]),
        showCancelButton: true,
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        customClass: "small_dialog",
        center: true
      }).then(function() {
        _this.delRemark(row)
      })
    },
    //删除备注
    async delRemark(row){
      const res = await remarkDel(row.id)
      if(res.data.code===0){
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        this.refreshList()
      }else{
        this.$message.error('删除失败')
      }
    },
    refreshList(){
      this.getList(this.dataItem.id)
    },
    /**
     * 验证表单
     */
    validateForm() {
      if (this.$refs.form) {
        return this.$refs.form.validate()
      }
      return Promise.resolve()
    },
    //备注列表
    async getList(id) {
      const res = await remarkList(id)
      this.dataList = res.data.data
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
      this.btnLoading = false
      this.$emit('refresh')
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
      this.btnLoading = false
      this.dataList = []
      this.$emit('refresh')
    }
  }
}
</script>

<style lang="scss" scoped>
  .r_table{
    margin-bottom: 10px;
    min-height: 200px;
    .r_tr{
      display: flex;
      align-items: center;
      .r_td{
        flex: 1;
        text-align: left;
        text-align: center;
      }
      .r_type{
        flex: none;
        width: 50px;
        text-align: left;
      }
      .r_menu{
        flex: none;
        width: 80px;
        text-align: right;
      }
    }
    .noData{
      text-align: center;
      padding: 50px;
      color: #999;
      font-size: 12px;
    }
  }
</style>
