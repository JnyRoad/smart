<template>
  <el-select
    v-model="currVal"
    placeholder="请选择"
    @change="handleChange"
    :disabled="myDisabled"
    clearable
  >
    <template v-for="(item, index) in dataList">
      <el-option :label="item.authorityName" :value="item.id" :key="index"></el-option>
    </template>
  </el-select>
</template>

<script>
import { fetchAuthList } from "@/api/platform/area/park-set";
import { validatenull } from "@/util/validate";
export default {
  name: "parkSelect",
  data() {
    return {
      currVal: undefined,
      dataList: []
    };
  },
  props: {
    value: undefined,
    myDisabled: false,
    dataObj:Array
  },
  mounted() {
    this.currVal = this.value;
    this.dataList = this.dataObj
  },
  watch: {
    value(val, oldval) {
      this.currVal = val;
    },
    currVal(val) {
      this.$emit("input", val);
    },
    dataObj(val){
      this.dataList= val
    }
  },
  methods: {
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
