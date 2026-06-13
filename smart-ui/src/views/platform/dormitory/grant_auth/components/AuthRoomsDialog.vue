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
    <div class="roomOuter">
      <div v-for="(item, index) in itemObj" :key="index" class="roomItem">
        {{item}}室
      </div>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      currVisible: false
    }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Array
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
    itemObj:{
      handler(newV,oldV){

      },
      immediate: true
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
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>

  .roomOuter{
    display: flex;
    padding-bottom: 50px;
    .roomItem{
      color: #fff;
      width: 80px;
      padding: 5px 0;
      background: $mainBg;
      text-align: center;
      margin: 0 10px 10px 0;
    }
  }
</style>
