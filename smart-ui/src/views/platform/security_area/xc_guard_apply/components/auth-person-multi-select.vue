<template>
  <el-select
    v-model="currVal"
    placeholder="请选择"
    @change="handleChange"
    :disabled="myDisabled"
    multiple
    collapse-tags
    clearable
  >
    <template v-for="(item, index) in dataList">
      <el-option :label="item.label" :value="item.value" :key="index"></el-option>
    </template>
  </el-select>
</template>

<script>
import { xcGuardApplyApi } from "../_service"
export default {
  name: "authCarSelect",
  data() {
    return {
      currVal: undefined,
      dataList: []
    };
  },
  props: {
    value: undefined,
    querObj: undefined,
    myDisabled: false,
    newAreaType: Array,
    oldAreaType: Array
  },
  mounted() {
    // this.currVal = this.value;
    // this.querObj.parkId && this.querObj.permitFactoryType && this.getDataList();
  },
  watch: {
    value(val, oldval) {
      this.currVal = val;
    },
    currVal(val) {
      this.$emit("input", val);
    },
    querObj:{
			handler: function(val){
        const areaType = this.newAreaType.concat(this.oldAreaType)
        let str = ''
        if(areaType.length > 0){
          str = areaType.join(',')
        }
        val.parkId && str && this.getDataList(str);
      },
			immediate: true
		}
  },
  methods: {
    async getDataList(areaId) {
      let query = {
        areaId: areaId,
        parkId: this.querObj.parkId
      }
      let res = await xcGuardApplyApi.getAuthList(query);
      this.dataList = [];
      res.data.data.forEach(element => {
        this.dataList.push({ value: element.authId, label: element.authName });
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
