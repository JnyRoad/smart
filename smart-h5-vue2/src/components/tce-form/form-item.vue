<template>
  <div class="">
    <template v-if="$slots.customize">
      <slot name="customize"></slot>
    </template>
    <!-- 预定义字段 -->
    <template v-else>
      <template v-if="type === 'input'">
        <formInput
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
          :readonly="readonly"
        ></formInput>
      </template>
      <template v-if="type === 'picker'">
        <formPicker
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
          :defaultProps="defaultProps"
          @pickerSelectHandle="pickerSelectHandle"
        ></formPicker>
      </template>
      <template v-if="type === 'phone'">
        <formPhone
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
        ></formPhone>
      </template>
      <template v-if="type === 'time-picker'">
        <formTimePicker
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
          @timeSelectHandle="timeSelectHandle"
        ></formTimePicker>
      </template>
      <template v-if="type === 'textarea'">
        <formTextarea
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
        ></formTextarea>
      </template>
      <template v-if="type === 'upload-image'">
        <formUploadImage
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
        ></formUploadImage>
      </template>
      <template v-if="type === 'upload-image-base64'">
        <formUploadImageBase64
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
        ></formUploadImageBase64>
      </template>
      <template v-if="type === 'upload-image-single'">
        <formUploadImageSingle
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
        ></formUploadImageSingle>
      </template>
      <template v-if="type === 'upload-image-single-face'">
        <formUploadImageSingleFace
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
        ></formUploadImageSingleFace>
      </template>
      <template v-if="type === 'upload-image-single-base64'">
        <formUploadImageSingleBase64
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
        ></formUploadImageSingleBase64>
      </template>
      <template v-if="type === 'plate-number'">
        <formPlateNumber
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
        ></formPlateNumber>
      </template>
      <template v-if="type === 'upload-file'">
        <formUploadFile
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
        ></formUploadFile>
      </template>
      <template v-if="type === 'item-holder'">
        <formItemHolder
          v-model="formData"
          :requiredMessage="requiredMessage"
          :field="field"
          :valueData="valueData"
          :required="required"
          :label="label"
          :placeholder="placeholder"
          :formOption="formOption"
          :disable="disable"
        >
          <template slot="itemholder">
            <slot name="itemholder"></slot>
          </template>
        </formItemHolder>
      </template>
    </template>
  </div>
</template>

<script>
import formInput from './form-input.vue'
import formPhone from './form-phone.vue'
import formPicker from './form-picker.vue'
import formTimePicker from './form-time-picker.vue'
import formTextarea from './form-textarea.vue'
import formUploadImage from './form-upload-image.vue'
import formUploadImageBase64 from './form-upload-image-base64.vue'
import formUploadImageSingle from './form-upload-image-single.vue'
import formUploadImageSingleFace from './form-upload-image-single-face.vue'
import formUploadImageSingleBase64 from './form-upload-image-single-base64.vue'
import formPlateNumber from './form-plate-number.vue'
import formUploadFile from './form-upload-file.vue'
import formItemHolder from './form-item-holder.vue'

const getParentNode = function(node) {
  if (!node.$parent) {
    return null
  }
  if (node.$parent.$options.name === 'business-form-group') {
    return getParentNode(node.$parent)
  }
  if (node.$parent.$options.name === 'business-form') {
    return node.$parent
  }
}
export default {
  name: 'business-form-item',
  provide() {
    return {
      formScope: {
        $formRoot: getParentNode(this),
        $scope: this
      }
    }
  },
  components: {
    formInput,
    formPhone,
    formPicker,
    formTimePicker,
    formTextarea,
    formUploadImage,
    formUploadImageBase64,
    formUploadImageSingle,
    formUploadImageSingleFace,
    formUploadImageSingleBase64,
    formPlateNumber,
    formUploadFile,
    formItemHolder
  },
  data() {
    return {
      formData: null
    }
  },
  props: {
    field: String,
    label: String,
    placeholder: String,
    required: Boolean,
    requiredMessage: String,
    formOption: Object,
    type: String,
    valueData: [Object, String, Number, Array],
    disable: Boolean,
    readonly: Boolean,
    defaultProps: Object
  },
  methods: {
    timeSelectHandle(time) {
      this.$emit('timeSelectHandle', time)
    },
    pickerSelectHandle(obj) {
      this.$emit('pickerSelectHandle', obj)
    }
  }
}
</script>

<style lang="scss" scoped></style>
