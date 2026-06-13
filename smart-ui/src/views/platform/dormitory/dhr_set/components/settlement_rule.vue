
<!--
- @name BU
-->
<template>
  <el-dialog ref="dialog" title="BU使用范围" :visible.sync="currVisible" width="680px" @open="open" @close="close" :close-on-click-modal="false" :append-to-body="true" :custom-class="'approve-detail-dialog'">
    <el-form label-width="200px" label-position="left">
      <el-form-item label="1、离职当天计算水电开关">
        <el-switch v-model="isCount" active-color="#13ce66" inactive-color="#ff4949" :active-value="0" :inactive-value="1"> </el-switch>
      </el-form-item>
      <el-form-item label="2、规则模板">
        <el-radio v-model="radio" label="1">集团石岩规则</el-radio>
      </el-form-item>
      <el-form-item label=""> 离职天数=上月自然日总天数-上月抄表日期+当月离职日期-1 </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">确 定</el-button>
    </div>
  </el-dialog>
</template>


<script>
import { ruleSet, getRule } from '../_service.js'
export default {
  data() {
    return {
      currVisible: false,
      btnLoading: false,
      isCount: 0,
      radio: '1'
    }
  },
  props: {
    tempId: [Number, String],
    parkId: [Number, String]
  },
  created() {},
  watch: {},
  mounted: function () {},
  computed: {},
  methods: {
    async getRuleSet() {
      const res = await getRule({
        parkId: this.parkId,
        tempId: this.tempId
      })
      const d = res.data.data
      this.isCount = d.isSettlementLast
    },
    async formSumit() {
      const str = JSON.stringify({
        isSettlementLast: this.isCount,
        tempId: this.tempId
      })
      const obj = {
        businessType: 4,
        configType: 5,
        parkId: this.parkId,
        value: str
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
      if (this.tempId) {
        this.getRuleSet()
      }
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