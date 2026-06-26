<template>
  <el-dialog
    :title="title"
    :visible="visible"
    class="dialog_form"
    width="550px"
    @close="$emit('close')"
  >
    <el-form
      ref="dormForm"
      :rules="rules"
      :model="form"
      label-width="100px"
      label-position="left"
    >
      <el-form-item 
        label="宿舍楼名称" 
        prop="dormitoryName">
        <el-input
          :value="form.dormitoryName"
          clearable
          @input="$emit('update-form-field', { field: 'dormitoryName', value: $event })"
        />
      </el-form-item>
    </el-form>
    <div 
      slot="footer" 
      class="dialog-footer">
      <el-button 
        type="primary" 
        plain 
        @click="$emit('close')">取 消</el-button>
      <el-button 
        :loading="loading" 
        type="primary" 
        @click="$emit('submit')">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: 'RoomDormitoryDialog',
  props: {
    title: {
      type: String,
      default: ''
    },
    visible: {
      type: Boolean,
      default: false
    },
    form: {
      type: Object,
      default: () => ({})
    },
    rules: {
      type: Object,
      default: () => ({})
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    validate(callback) {
      this.$refs.dormForm.validate(callback)
    },
    resetFields() {
      if (this.$refs.dormForm) {
        this.$refs.dormForm.resetFields()
      }
    }
  }
}
</script>
