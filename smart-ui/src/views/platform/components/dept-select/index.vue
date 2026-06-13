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
import request from "axios";
import { validatenull } from "@/util/validate";
// 删除了 /platform/vehicle/dep/ +id 接口， 统一为/platform/recruitment/getDep/ +id
const baseUrl = "/platform/recruitment";
const getList = function(compId) {
  return request({
    url: `${baseUrl}/getDep/${compId}`,
    method: "GET"
  });
};
export default {
  name: "deptSelect",
  data() {
    return {
      currVal: undefined,
      dataList: []
    };
  },
  props: {
    value: undefined,
    compId: undefined,
    myDisabled: false
  },
  mounted() {
    this.currVal = this.value;
    this.compId && this.getDataList(this.compId);
  },
  watch: {
    value(val, oldval) {
      this.currVal = val;
    },
    currVal(val) {
      this.$emit("input", val);
    },
    compId(val) {
      val && this.getDataList(val);
    }
  },
  methods: {
    async getDataList(compId) {
      let res = await getList(compId);
      this.dataList = [];
      res.data.data.forEach(element => {
        this.dataList.push({ value: element.depid, label: element.depname });
      });
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
