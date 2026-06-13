<template>
  <el-dialog
    ref="dialog"
    title=""
    :visible.sync="currVisible"
    width="800px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <div slot="title" class="dialog-footer">
      <el-tabs v-model="activeName" @tab-click="tabHandleClick">
        <el-tab-pane label="员工入住" name="staffCheckIn"></el-tab-pane>
        <el-tab-pane label="非员工入住" name="nonStaffCheckIn"></el-tab-pane>
      </el-tabs>
    </div>
    <checkInNonStaff :dataItem="dataItem" @cancel="cancel" @refresh="refresh" v-if="activeName==='nonStaffCheckIn'"/>
    <checkInStaff :dataItem="dataItem" @cancel="cancel" @refresh="refresh" v-if="activeName==='staffCheckIn'"/>
  </el-dialog>
</template>

<script>
// import { managementApi } from '../api/management'
import checkInNonStaff from './_check_in_non_staff'
import checkInStaff from './_check_in_staff'

export default {
  components: {
    checkInNonStaff,
    checkInStaff
  },
  data() {
    return {
      activeName: 'staffCheckIn',
      btnLoading: false,
      currVisible: false,
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
    }
  },
  methods: {
    tabHandleClick(){

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
    async formSumit() {
      await this.validateForm()

      this.refresh()
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
    },
    cancel() {
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss">

</style>
