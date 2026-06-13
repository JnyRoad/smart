<!--水电模板  -->
<template>
  <!-- 配置模板弹出框 -->
  <el-dialog
    title="配置水电分摊模板"
    class="dialog_form config_form"
    width="1200px"
    @close="resetSetForm('waterform')"
    :visible.sync="setFormVisible"
  >
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <el-form ref="waterform" :inline="true" :model="waterform" size="mini">
          <table class="tb2">
            <template v-for="(item, index) in waterform.rulesList">
              <tr :key="index" v-if="index===0">
                <td></td>
                <template v-for="(itemType, indexType) in item.rulesDataList">
                  <td :key="indexType">{{ itemType.monthNum }}月</td>
                </template>
                <td>操作</td>
              </tr>
              <tr :key="index">
                <td>
                  <div>{{getType(item.categoryId, 0)}}</div>
                  <div style="color: #999; padding-top: 5px;font-size: 12px;">{{getType(item.categoryId, 2)}}</div>
                </td>
                 <template v-for="(itemType, indexType) in item.rulesDataList">
                  <td :key="indexType">
                    <el-form-item
                      :prop="'rulesList.'+index+'.rulesDataList.'+indexType+'.standardQty'"
                      :rules="[
                        { required: true, message: '请输入', trigger: 'blur' },
                        { type: 'number', message: '请输入数字', trigger: 'blur' }
                      ]"
                    >
                      <el-input v-model.number="itemType.standardQty" placeholder="请输入" clearable></el-input>
                    </el-form-item>
                  </td>
                </template>
                <td>
                  <el-button type="text" @click="editAll(item.rulesDataList, 'standardQty', $event)">批量修改</el-button>
                </td>
              </tr>
              <tr :key="index">
                <td>{{getType(item.categoryId, 1)}}</td>
                 <template v-for="(itemType, indexType) in item.rulesDataList">
                  <td :key="indexType">
                    <el-form-item
                      :prop="'rulesList.'+index+'.rulesDataList.'+indexType+'.overFee'"
                      :rules="[
                        { required: true, message: '请输入', trigger: 'blur' },
                        { type: 'number', message: '请输入数字', trigger: 'blur' }
                      ]"
                    >
                      <el-input v-model.number="itemType.overFee" placeholder="请输入" clearable></el-input>
                    </el-form-item>
                  </td>
                </template>
                <td>
                  <el-button type="text" @click="editAll(item.rulesDataList, 'overFee', $event)">批量修改</el-button>
                </td>
              </tr>
            </template>
          </table>
        </el-form>
      </section>
    </el-scrollbar>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="resetSetForm('waterform')" plain>取 消</el-button>
      <el-button type="primary" @click="editSubmit('waterform')" :loading="setLoading">保 存</el-button>
    </div>
     <el-dialog
      :style="{'left': leftv}"
      width="300px"
      custom-class="minDialog"
      class="minDialog"
      :top="topv"
      @close="reseteditForm('editform')"
      :visible.sync="minVisible"
      :show-close="false"
      append-to-body>
      <el-form ref="editform" :model="editform" size="mini">
        <el-form-item prop="val"
          :rules="[
            { required: true, message: '请输入', trigger: 'blur' },
            { type: 'number', message: '请输入数字', trigger: 'blur' }
          ]"
        >
          <el-input v-model.number="editform.val" placeholder="请输入" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="text" @click="editSure">确定</el-button>
          <el-button type="text" @click="minVisible = false">取 消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </el-dialog>
</template>

<script>
import { querySDTemplateRule,addSDTemplateRule } from "@/api/platform/dormitory/sd_templates";
import { validatenum } from "@/util/validate";
import { mapGetters } from "vuex";
const MONTH_OPTION = [
  '1月',
  '2月',
  '3月',
  '4月',
  '5月',
  '6月',
  '7月',
  '8月',
  '9月',
  '10月',
  '11月',
  '12月'
]
let rulesList = []
for(let i = 1; i < 4; i++){
  let rulesDataList = []
  for(let j = 1; j < 13; j++){
    rulesDataList.push(
      {
        standardQty:'',
        overFee:'',
        monthNum: j
      }
    )
  }
  rulesList.push(
    {
      categoryId: i,
      rulesDataList: rulesDataList
    }
  )
}
export default {
  name: "",
  data() {
    return {
      topv: '',
      leftv: '',
      minVisible: false,
      setFormVisible: false, //配置模板
      setLoading: false, //是否正在设置
      curObj: [],
      editform:{
        val: ''
      },
      waterform: {
        tempId: '',
        rulesList: rulesList
      }
    };
  },
  props: {
    visible: {
      type: Boolean
    },
    row: undefined
  },
  watch: {
    visible(newVal, oldVal) {
      if(newVal){
        this.getDetail()
      }
      this.setFormVisible = newVal;
    },
    setFormVisible(newVal, oldVal) {
      if (newVal === false) {
        this.$emit("configdo", newVal);
      }
    },
  },
  created() {
    this.initData();
  },
  mounted: function() {},
  computed: {},
  methods: {
    getType(type, no){
      const obj = [
        [],
        ['热水基本配额（吨）', '热水超出单价（元/吨）', '--人月标准'],
        ['冷水基本配额（吨）', '冷水超出单价（元/吨）', '--人月标准'],
        ['电量基本配额（度）', '电量超出单价（元/度）', '--房月标准'],
      ]
      return obj[type][no]
    },
    initData() {
      this.setFormVisible = this.visible;
    },
    async getDetail() {
      if(this.row.id){
        const res = await querySDTemplateRule( this.row.id )
        let data = res.data.data
        if(data.rulesList.length>0){
          this.waterform = data
        }
      }
    },
    editAll(obj, attr, e){
      this.topv = e.y + 10 + 'px';
      this.leftv = e.x - 270 + 'px';
      this.minVisible = true
      this.curObj = obj
      this.attr = attr
    },
    resetSetForm(formName) {
      this.setFormVisible = false;
      this.minVisible = false;
      this.setLoading = false;
      this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
      this.waterform.rulesList = rulesList;
    },
    reseteditForm(formName) {
      this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    async editSubmit(formName) {
      await this.$refs['waterform'].validate()
      this.waterform.tempId = this.row.id
      addSDTemplateRule(this.waterform)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.setFormVisible = false;
                this.waterform.rulesList = rulesList;
                this.setLoading = false;
                this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
                this.$notify({
                  title: "成功",
                  message: msg,
                  type: "success",
                  duration: 2000
                });
              } else if (dataResult === false) {
                this.setLoading = false;
                this.$notify({
                  title: "失败",
                  message: msg,
                  type: "error",
                  duration: 2000
                });
              }
            })
            .catch(() => {
              this.addLoading = false;
            });
    },
    async editSure() {
      await this.$refs['editform'].validate()
      this.curObj.forEach(el=>{
        el[this.attr] = this.editform.val;
      })
      this.minVisible = false
    }
  }
};
</script>
<style lang="scss" scoped>
  .minDialog ::v-deep {
    width: 300px !important;
    overflow: hidden;
    .el-form--inline .el-form-item{
      margin-right: 0;
    }
    .el-form-item--mini.el-form-item,
    .el-form-item--small.el-form-item{
      margin-bottom: 10px;
    }
    .el-dialog__header{
      padding: 0;
    }
    .el-dialog__body{
      padding: 20px 20px 5px;
    }
  }
  .config_form ::v-deep {
    .my-basic-inner{
      padding: 10px 0 30px 0;
      min-height: 200px;
    }
    .tb2{
      td{
        border: 1px solid #e0e0e0;
        padding: 5px 0 12px 0;
        text-align: center;
      }
      td:first-child{
        width: 160px;
      }
      .el-button{
        font-size: 12px;
      }
      td:last-child{
        padding: 0 10px;
      }
    }
    .el-form--inline .el-form-item{
      margin: 0;
      width: 100%;
      height: 100%;
    }
    .el-form--inline .el-form-item__content{
      width: 100%;
    }
    .el-input--mini .el-input__inner{
      text-align: center;
      border: none;
      padding: 0
    }
  }
</style>
