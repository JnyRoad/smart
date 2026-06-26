<template>
  <el-dialog
    :title="title"
    :visible="visible"
    class="dialog_form"
    width="600px"
    @close="$emit('close')"
  >
    <el-form
      ref="batchEditForm"
      :rules="rules"
      :model="form"
      label-width="120px"
    >
      <el-form-item
        v-if="!isHandelSD"
        label="是否参与分配"
        prop="isDormitoryRoom">
        <el-select
          :value="form.isDormitoryRoom"
          placeholder="请选择"
          @input="$emit('update-form-field', { field: 'isDormitoryRoom', value: $event })"
        >
          <el-option
            v-for="item in isDormitoryArr"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="!isHandelSD"
        label="是否参与计算"
        prop="isCount">
        <el-select
          :value="form.isCount"
          placeholder="请选择"
          @input="$emit('update-form-field', { field: 'isCount', value: $event })"
        >
          <el-option
            v-for="item in isCountArr"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="!isHandelSD"
        label="宿舍分类"
        prop="roomType">
        <el-select
          :value="form.roomType"
          placeholder="请选择"
          @input="$emit('update-form-field', { field: 'roomType', value: $event })"
        >
          <el-option
            v-for="item in parkDormTypeList"
            :key="item.id"
            :label="item.typeName"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        v-if="!isHandelSD"
        label="房间属性"
        prop="roomSex">
        <roomGenderSelect
          :value="form.roomSex"
          @input="$emit('update-form-field', { field: 'roomSex', value: $event })"
        />
      </el-form-item>
      <el-form-item
        v-if="isHandelSD"
        label="水电分摊模板"
        prop="sdTemplateId">
        <el-select
          :value="form.sdTemplateId"
          placeholder="请选择"
          @input="$emit('update-form-field', { field: 'sdTemplateId', value: $event })"
        >
          <el-option
            v-for="item in sdTempList"
            :key="item.id"
            :label="item.templateName"
            :value="item.id"
          />
        </el-select>
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
  name: 'RoomBatchEditDialog',
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
    isHandelSD: {
      type: Boolean,
      default: false
    },
    isDormitoryArr: {
      type: Array,
      default: () => []
    },
    isCountArr: {
      type: Array,
      default: () => []
    },
    parkDormTypeList: {
      type: Array,
      default: () => []
    },
    sdTempList: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    validate(callback) {
      this.$refs.batchEditForm.validate(callback)
    },
    resetFields() {
      if (this.$refs.batchEditForm) {
        this.$refs.batchEditForm.resetFields()
      }
    }
  }
}
</script>
