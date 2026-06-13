<template>
  <el-select
    v-model="currVal"
    placeholder="请选择"
    @change="handleChange"
    :disabled="myDisabled"
    clearable
    multiple
    collapse-tags
  >
    <template v-for="(item, index) in dataList">
      <el-option :label="item.label" :value="item.value" :key="index"></el-option>
    </template>
  </el-select>
</template>

<script>
import request from "axios";
const getList = function(parkId) {
  return request({
    url: `/platform/parkbu/getByPark/${parkId}`,
    method: "GET"
  });
};
export default {
  name: "buSelect",
  data() {
    return {
      currVal: undefined,
      dataList: []
    };
  },
  props: {
    value: undefined,
    parkId: undefined,
    myDisabled: false
  },
  mounted() {
    this.currVal = this.value;
    this.parkId && this.getDataList(this.parkId);
  },
  watch: {
    value(val, oldval) {
      this.currVal = val;
    },
    currVal(val) {
      this.$emit("input", val);
    },
    parkId(val) {
      val && this.getDataList(val);
    }
  },
  methods: {
    async getDataList(parkId) {
      let res = await getList(parkId);
      this.dataList = [];
      res.data.data.forEach(element => {
        if(element&& element.compid){
          this.dataList.push({ value: element.compid, label: element.compAbbr });
        }
      });
    },
    handleChange(id) {
      this.$emit("doChange", id);
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
