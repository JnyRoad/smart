<!--重置记录-->
<template>
  <el-dialog
    title="查看人天修改记录"
    class="dialog_form"
    width="900px"
    :visible.sync="setFormVisible"
  >
   <div class="d-table">
     <div class="d-tip">* 修改了入住天数，会产生修改记录</div>
     <avue-crud
          ref="crud"
          class="crud"
          :data="tableData"
          :option="mainOption"
        >
        <template slot-scope="scope" slot="staffName">
          <span>{{scope.row.staffBadge}}-{{scope.row.staffName}}</span>
        </template>
      </avue-crud>
   </div>
  </el-dialog>
</template>

<script>
import { queryStayModify } from "../_service";
import { tableOptionDay } from "./_config.js";
import {validatenull} from '@/util/validate';

export default {
  name: "",
  data() {
    return {
      setFormVisible: false,
      tableData: [],
      mainOption: tableOptionDay
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
      if (newVal) {
        this.getDetail(this.row.id)
      }
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
          this.getDetail(val.id)
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
    },
    async getDetail(id) {
      const res = await queryStayModify(id)
      this.tableData = res.data.data
    }
  }
};
</script>
<style lang="scss" scoped>
.dialog_form ::v-deep {
  .d-tip{
    font-size: 12px;
  }
  .el-dialog__body{
    padding-bottom: 30px;
  }
}
</style>