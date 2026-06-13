
<!--
- @name BU
-->
<template>
  <el-dialog ref="dialog" title="日志存储策略" :visible.sync="currVisible" width="500px" @open="open" @close="close" :append-to-body="true" :custom-class="'approve-detail-dialog'">
    <el-form label-width="10px">
      <el-form-item label="">
        <span>调用该记录，系统只保存最近</span>
        <el-input v-model="times" placeholder="" style="width: 60px;margin: 0 4px"></el-input>
        <span>天内的日志记录，其余的将被删除</span>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">确 定</el-button>
    </div>
  </el-dialog>
</template>


<script>
import { ruleSet,getLeaveDate } from '../_service.js'
export default {
  data() {
    return {
      currVisible: false,
      btnLoading: false,
      times: null
    }
  },
  props: {
  },
  created() {},
  watch: {},
  mounted: function () {

  },
  computed: {},
  methods: {
    async getLeaveDate() {
      const res = await getLeaveDate()
      if(res.data.data){
        this.times = res.data.data.deleteDays
      }else{
        this.times = null
      }
    },
    async formSumit() {
       const obj = {
        businessType: 4,
        configType: 6,
        value: JSON.stringify({
          deleteDays: this.times
        })
      }
      this.btnLoading = true
      const res = await ruleSet(obj)
      this.btnLoading = false
      if (res.data.code === 0) {
        this.$notify({
          title: '成功',
          message: '设置成功',
          type: 'success'
        })
        this.close()
      }
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    },
    open() {
      this.getLeaveDate()
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    }
  }
}
</script>

<style lang="scss" scoped>
.top_dv {
  padding: 0 20px;
}
.my-lit-scrollbar {
  height: 100%;
}
.el-dialog__body {
  padding: 10px 0 0 0;
}
.sdcb_cont {
  display: flex;
  margin-bottom: 10px;
}
.sdcb_tp {
  width: 100%;
  padding: 0 20px;
}
.row1 {
  margin-bottom: 15px;
  .el-tooltip {
    display: inline-block;
    margin-left: 20px;
    font-weight: bold;
  }
}
</style>