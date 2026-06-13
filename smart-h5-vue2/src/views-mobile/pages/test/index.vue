<!--
- @name 测试
- @author yang.chuan <yang.chuan@bjtce.com>
- @date 2020-11-26
-->

<template>
  <div class="container">
    <tce-form ref="tceForm">
      <tce-form-item required field="e" type="phone" label="访客类型" placeholder="请输入手机号"></tce-form-item>
      <tce-form-item required field="a" requiredMessage="访客类型不能为空" type="input" label="访客类型" placeholder="请输入"></tce-form-item>
      <tce-form-item
        required
        field="b"
        type="picker"
        :valueData="{ label: 'abc3', value: 'abc3' }"
        :formOption="{
          opt: [
            { label: 'abc', value: 'abc' },
            { label: 'abc2', value: 'abc2' },
            { label: 'abc3', value: 'abc3' }
          ]
        }"
        label="访客类型"
        placeholder="请输入"
      ></tce-form-item>
      <tce-form-group>
        <tce-form-item required field="c" type="input" label="访客类型2" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="d" type="picker" :formOption="{ opt: [{ label: 'abc', value: 'abc' }] }" label="访客类型2" placeholder="请输入"></tce-form-item>
      </tce-form-group>
      <tce-form-group>
        <tce-form-item required field="f" type="picker" :formOption="{ opt: [{ label: 'abc', value: 'abc' }] }" label="访客类型" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="g" type="input" label="访客类型" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="h" type="input" label="访客类型" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="j" type="time-picker"  valueData="2020-12-30 20:20" label="访客类型" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="o" type="textarea" label="访客类型" placeholder="请输入"></tce-form-item>
         <tce-form-item required field="o" type="upload-image" label="访客类型" placeholder="请输入"></tce-form-item>
          <tce-form-item required field="o" type="plate-number" label="车牌号" placeholder="请输入"></tce-form-item>
      </tce-form-group>
    </tce-form>
    <button @click="submit" class="tce-button tce-button--">提交</button>
  </div>
</template>

<script>
export default {
  components: {},
  data() {
    return {}
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    async submit() {
      await this.$refs.tceForm.verification()
    }
  },
  filters: {},
  /**
   * 生命周期 created
   */
  created() {},
  /**
   * 生命周期 mounted
   */
  mounted() {},
  /**
   * 生命周期 beforeDestroy
   */
  beforeDestroy() {}
}
</script>

<style lang="scss" scoped>
</style>
