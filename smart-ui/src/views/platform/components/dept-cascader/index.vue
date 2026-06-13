<template>
  <el-cascader
    expand-trigger="hover"
    :options="dataList"
    v-model="currVal"
    :placeholder="placeholder"
    :change-on-select="changeOnSelect"
    clearable
  ></el-cascader>
</template>

<script>
import { getCompTree } from "@/api/platform/_publicService";
export default {
  data() {
    return {
      currVal: [],
      dataList: []
    };
  },
  props: {
    value: {
      type: Array,
      default: function(){
        return []
      }
    },
    changeOnSelect: {
      type: Boolean,
      default: true
    },
    placeholder: {
      type: String,
      default: '请选择'
    }
  },
  mounted() {
    this.currVal = this.value
    this.getDataList()
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
      let res = await getCompTree()
      this.dataList = res.data.data
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
