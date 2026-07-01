<template>
  <el-dialog
    ref="dialog"
    title="导出部门分摊水电表"
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
      <el-button
        :loading="exporting"
        type="primary"
        @click="download"
      >导 出</el-button>
      <el-button type="primary" plain @click="cancel">关 闭</el-button>
    </div>
  </el-dialog>
</template>

<script>
import dormMultiSelect from "@/views/platform/components/dorm-multi-select/index"
import  { exportShareData } from '../../_service'
import { dateFormat2 } from "@/util/date"
import { staffStatusInit } from '@/filters/index'
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
      exporting: false,
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
    dateFormat(val) {
      if (!this.validatenull(val)) {
        return dateFormat2(new Date(val));
      }
    },
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
      this.exporting = true
      // 导出接口是全量联表查询，耗时明显长于普通接口，超时时长在 _service.js 里单独放宽，
      // 这里等接口真正返回/失败后再关弹窗、切 loading，不要一发起请求就关闭弹窗，
      // 否则请求失败时用户看不到任何反馈（历史 bug：只 console.error，界面上像是卡死无响应）。
      return exportShareData(obj)
        .then(response => {
          this.close()
          require.ensure([], () => {
            const { export_json_to_excel } = require("@/vendor/Export2Excel");
            const tHeader = [
              "工号",
              "姓名",
              "员工状态",
              "BU",
              "部门",
              "入住日期",
              "索引",
              "楼栋",
              "房间号",
              "费用承担BU",
              "房间用电金额",
              "房间用水金额",
              "住宿天数",
              "个人直接归属电费",
              "个人直接归属水费",
              "标记天数",
              "日平均金额",
              "水电超标金额"
            ];
            const filterVal = [
              "badge",
              "name",
              "status",
              "compName",
              "depName",
              "inTime",
              "index",
              "dormitoryName",
              "roomName",
              "bearBu",
              "electric",
              "water",
              "inDays",
              "realElectric",
              "realWater",
              "remarkDays",
              "avgFee",
              "fee"
            ];
            const list = response.data.data
            list.forEach(el=>{
              el.inTime = this.dateFormat(el.inTime)
              el.meterMonth = this.dateFormat(el.meterMonth)
              el.status = staffStatusInit(el.status)
            })
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, `部门分摊水电表&(${this.searchform.meterMonth})`);
          });
        })
        .catch(err => {
          console.error(err)
          this.$message.error('导出失败，请稍后重试')
        })
        .finally(() => {
          this.exporting = false
        })
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
