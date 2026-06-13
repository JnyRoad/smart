<template>
  <el-dialog
    ref="dialog"
    title="家属"
    :visible.sync="currVisible"
    width="1000px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <div style="min-height: 200px;padding-bottom: 30px;">
      <dlgFamilyList :row="row"/>
    </div>
    <div slot="footer" class="footer">
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import dlgFamilyList from "./dlg_family_list";
export default {
  components: {
    dlgFamilyList
  },
  data() {
    return {
      currVisible: false,
      curFamiley: {}
    }
  },
  props: {
    visible: Boolean,
    row: undefined
  },
  created() {},
  watch: {
    row:{
      handler:function(newVal, oldVal) {
      },
      immediate: true
    },
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      }
    }
  },
  methods: {
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.$emit('refresh')
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped></style>
