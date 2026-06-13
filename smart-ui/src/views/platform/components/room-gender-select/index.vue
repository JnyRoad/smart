<template>
  <el-select
    v-model="currVal"
    placeholder="请选择"
    @change="handleChange"
    :disabled="myDisabled"
    clearable
  >
    <template v-for="(item, index) in dataList">
      <el-option :label="item.label" :value="item.value" :key="index"></el-option>
    </template>
  </el-select>
</template>

<script>
import { validatenull } from "@/util/validate";

export default {
  name: "jcheSelect",
  data() {
    return {
      currVal: undefined,
      dataList: []
    };
  },
  props: {
    value: undefined,
    myDisabled: false
  },
  mounted() {
    this.currVal = this.value;
    this.getDataList();
  },
  watch: {
    value(val, oldval) {
      this.currVal = val;
    },
    currVal(val) {
      this.$emit("input", val);
    }
  },
  methods: {
    async getDataList() {
      this.dataList = [
        { label: '男', value: 0 },
        { label: '女', value: 1 },
        { label: '夫妻/家属', value: 2 },
        { label: '其他', value: 3 }
      ]
    },
    handleChange(id) {
      this.$emit("doChange", id);
      if (!validatenull(id)) {
        let obj = this.dataList.find(item => {
          if (item.value === id) {
            return item;
          }
        });
        if (obj !== null) {
          // 父组件通过getItem方法获取选中的item
          this.$emit("getItem", obj);
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
