<template>
  <el-dialog
    :title="title"
    :visible="visible"
    class="dialog_form"
    width="550px"
    @close="$emit('close')"
  >
    <el-form
      ref="floorForm"
      :rules="rules"
      :model="form"
      label-width="80px"
    >
      <template v-if="editFloor">
        <el-form-item
          label="楼层编号"
          prop="floorName">
          <el-input
            :value="form.floorName"
            disabled
          />
        </el-form-item>
        <el-form-item
          label="房间数量"
          prop="roomNum">
          <el-input
            :value="form.roomNum"
            clearable
            @input="$emit('update-form-field', { field: 'roomNum', value: $event })"
          />
        </el-form-item>
      </template>
      <template v-else>
        <el-form-item
          label="起始编号"
          prop="startNum">
          <el-input
            :value="form.startNum"
            :disabled="hasStartNum"
            clearable
            @input="$emit('update-form-field', { field: 'startNum', value: $event })"
          />
        </el-form-item>
        <el-form-item
          label="楼层数量"
          prop="floorNum">
          <el-input
            :value="form.floorNum"
            clearable
            @input="$emit('update-form-field', { field: 'floorNum', value: $event })"
          />
        </el-form-item>
      </template>
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
  name: 'RoomFloorDialog',
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
    editFloor: {
      type: Boolean,
      default: false
    },
    hasStartNum: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    validate(callback) {
      this.$refs.floorForm.validate(callback)
    },
    resetFields() {
      if (this.$refs.floorForm) {
        this.$refs.floorForm.resetFields()
      }
    }
  }
}
</script>
