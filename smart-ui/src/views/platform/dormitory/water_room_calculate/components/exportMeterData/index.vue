<template>
  <el-dialog
    ref="dialog"
    title="导出抄表数据"
    :visible.sync="currVisible"
    width="500px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <section class="my-basic-inner">
      <div class="step1">
        <div class="step1Info">
          <el-form ref="form" :inline="false" :model="searchform" :rules="rules" size="mini" label-width="60px">
            <el-form-item label="园区" prop="parkId">
              <parkSelect v-model="searchform.parkId" :defaultSelected="true" @doChange="parkChange" @defaultHandle="defaultHandle"></parkSelect>
            </el-form-item>
            <el-form-item label="楼栋" prop="dormitoryIds">
              <dormMultiSelect
                :parkId="searchform.parkId"
                v-model="searchform.dormitoryIds"
                :placeholder="'可多选'"
              ></dormMultiSelect>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </section>
    <div slot="footer">
      <el-button type="primary" @click="download">导 出</el-button>
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import dormMultiSelect from "@/views/platform/components/dorm-multi-select/index";
import  { exportData } from '../../_service'
export default {
  components: {
    dormMultiSelect
  },
  data() {
    return {
      searchform: {
        parkId: '',
        dormitoryIds: [],
        meterMonth: '',
      },
      rules: {
        parkId: { required: true, message: '请选择园区', trigger: 'change' },
        dormitoryIds: { required: true, message: '请选择楼栋', trigger: 'change' },
      },
      currVisible: false,
    }
  },
  props: {
    visible: Boolean,
    meterMonth: String
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        this.doClearValidate("form");
      }
    },
    meterMonth:{
      handler( val){
        this.searchform.meterMonth = val
      },
      immediate: true
    }
  },
  methods: {
    parkChange(){
      this.searchform.dormitoryIds = []
    },
    //导出表格
    async download() {
      await this.validateForm('form')
      let obj = {
        dormitoryIds: this.searchform.dormitoryIds.toString(),
        meterMonth: this.searchform.meterMonth
      }
      require.ensure([], () => {
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "楼栋",
          "房间号",
          "(电)上月",
          "(电)本月",
          "(电)实用",
          "(电)标准",
          "(电)超标",
          "(冷水)上月",
          "(冷水)本月",
          "(热水)上月",
          "(热水)本月",
          "(水)实用",
          "(水)标准",
          "(水)超标",
          "(电)单价",
          "(水)单价",
          "超标金额",
          "入住总天数",
          "日平均金额",
          "抄表月份",
          "备注"
        ];
        const filterVal = [
          "dormitoryName",
          "roomName",
          "elePreMonthNum",
          "eleCurMonthNum",
          "eleUse",
          "eleQty",
          "eleOverUse",

          "coldPreMonthNum",
          "coldCurMonthNum",
          "hotPreMonthNum",
          "hotCurMonthNum",

          "coldUse",
          "coldQty",
          "coldOverUse",

          "eleOverFee",
          "coldOverFee",

          "totalAmount",
          "inDays",
          "avgAmount",

          "meterMonth",
          "remark"
        ];
        exportData( obj )
          .then(response => {
            const list = response.data.data;
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, `房间水电计算表信息&(${this.searchform.meterMonth})`);
          })
          .catch(err => { console.error(err) });
      });
      this.close()
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    defaultHandle(e){
      this.searchform.parkId = e.value
    },
    /**
     * 验证表单
     */
    validateForm(formName) {
      if (this.$refs[formName]) {
        return this.$refs[formName].validate()
      }
      return Promise.resolve()
    },
    refresh() {
      this.$emit('refresh')
      this.close()
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
  .my-basic-inner{
    padding: 10px 0 30px;
  }
  .step1Info{
    margin-bottom: 20px;
  }
  .box-orange{
    margin-bottom: 20px;
  }
  .fileInfo{
    padding-bottom: 20px;
  }
  .tips{
    color: #999;
    font-size: 12px;
  }
</style>
