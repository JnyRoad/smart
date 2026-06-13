<!--重置记录-->
<template>
  <el-dialog
    title="查看上月止度修改记录"
    class="dialog_form"
    width="700px"
    :visible.sync="setFormVisible"
  >
   <div class="d-table">
     <avue-crud
          ref="crud"
          class="crud"
          :data="tableData"
          :option="mainOption"
        >
        <template slot-scope="scope" slot="categoryId">
          <span>{{scope.row.categoryId | fl_getCategory}}</span>
        </template>
        <template slot-scope="scope" slot="preMonthNum">
          <span>{{scope.row.preMonthNum == -1 ? '无': scope.row.preMonthNum}}</span>
        </template>

      </avue-crud>
   </div>
  </el-dialog>
</template>

<script>
import { tableOption } from "./_config.js";
import {validatenull} from '@/util/validate';

export default {
  name: "",
  data() {
    return {
      setFormVisible: false,
      tableData: [],
      mainOption: tableOption
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    row: undefined
  },
  watch: {
    visible(newVal, oldVal) {
      this.setFormVisible = newVal;
    },
    setFormVisible(newVal, oldVal) {
      if (newVal === false) {
        this.$emit("dlgdo", newVal);
      }
    },
    row:{
			handler: function(val){
        if(!validatenull(val)){
          this.tableData = val
        }
      },
			immediate: true
		}
  },
  filters: {
    fl_getCategory: function(val) {
      if(validatenull(val)){
        return
      }
      const arr = ['热水表','冷水表','电表']
      return  arr[val-1]
    }
  },
  created() {
    this.initData();
  },
  mounted: function () {},
  computed: {},
  methods: {
    initData() {
      this.setFormVisible = this.visible;
    }
  }
};
</script>
<style lang="scss" scoped>
.dialog_form ::v-deep {
  .el-dialog__body{
    padding-bottom: 30px;
  }
}
</style>