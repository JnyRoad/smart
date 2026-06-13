<template>
  <el-select
    v-model="currVal"
    placeholder="请选择"
    @change="handleChange"
    :disabled="myDisabled"
    :clearable="canClearable"
  >
    <template v-for="(item, index) in dataList">
      <el-option :label="item.label" :value="item.value" :key="index"></el-option>
    </template>
  </el-select>
</template>

<script>
import request from "axios";
import { validatenull } from "@/util/validate";
const getList = function(query) {
  return request({
    url: `/platform/admittance/area/type/security/factory/list`,
    method: "GET",
    params: query
  });
};
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
    defaultSelected: false,
    canClearable: false
  },
  mounted() {
    this.currVal = this.value
    this.getDataList()
  },
  watch: {
    value(val, oldval) {
      this.currVal = val
    },
    currVal(val) {
      this.$emit("input", val)
    }
  },
  methods: {
    async getDataList() {
      let res = await getList({type:1})
      res.data.data.forEach(element => {
        this.dataList.push({ value: element.areaOaId, label: element.areaTypeName })
      })
      if(this.defaultSelected){
        if(this.dataList.length>0){
          this.currVal = this.dataList[0].value
          this.$emit("defaultHandle", this.dataList[0])
          this.$emit("getItem", this.dataList[0])
        }
      }
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
          this.$emit("getItem", obj)
        }
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
