<!--协议到期通知设置-->
<template>
  <el-dialog
    :title="mTitle"
    class="dialog_form noticeSet_form"
    width="600px"
    @close="resetSetForm('dataform')"
    :visible.sync="setFormVisible"
  >
    <el-form ref="dataform" :model="dataform" :rules="addRule" label-width="90px">
      <el-form-item label="园区" prop="parkId">
        <parkSelect v-model="dataform.parkId" @doChange="parkChange"></parkSelect>
      </el-form-item>
      <el-form-item label="通知规则" prop="days">
        <div class="dv1">
          协议到期前
          <el-input v-model="dataform.days" />
          天通知，只通知一次
        </div>
      </el-form-item>
      <el-form-item label="通知方式" prop="notifyType">
        <el-radio v-model="dataform.notifyType" :label="1">邮件</el-radio>
      </el-form-item>
      <el-form-item label="通知内容" prop="template">
        <div class="dv2">
          <el-input v-model="dataform.template" type="textarea"/>
          <div>
            插入变量：
            <el-button
              type="primary"
              plain
              v-for="(item, index) in textOption"
              :key="index"
              @click="insertTextVal(dataform, item.value)"
            >{{item.text}}</el-button>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="通知名单" prop="accounts">
        <div>
          <div class="dv3">
            <div class="row">邮箱
              <el-button @click="addAccount" type="text" class="btn">添加</el-button>
            </div>
            <div class="row" v-for="(item, index) in emails" :key="index">
              <el-input v-model="item.el" placeholder=""/>
              <el-button @click="delAccount(index)" type="text" class="btn">删除</el-button>
            </div>
          </div>
        </div>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="resetSetForm('dataform')" plain
        >取 消</el-button
      >
      <el-button
        type="primary"
        @click="editSubmit('dataform')"
        :loading="setLoading"
        :disabled="dataform.statementStatus==1"
        >保 存</el-button
      >
    </div>
  </el-dialog>
</template>

<script>
import { saveNotifyConfig, getNotifyConfigByParkId } from "./_service.js";
const textOption = [
  { value: "{供应商名称}", text: "供应商名称" },
  { value: "{协议到期日期}", text: "协议到期日期" }
];
export default {
  name: "",
  data() {
    return {
      textOption: textOption,
      setFormVisible: false,
      setLoading: false, //是否正在设置
      emails: [],
      dataform: {
        accounts: [],
        days: '',
        notifyType: 1,
        parkId: '',
        template: ""
      },
      addRule: {
        days: [
          { required: true, message: '请输入', trigger: 'blur' }
        ],
        template: [
          { required: true, message: '请输入通知内容', trigger: 'blur' }
        ]
      }
    };
  },
  props: {
    mTitle: {
      type: String,
      default: function(){
        return '协议到期通知设置'
      }
    },
    visible: {
      type: Boolean,
    }
  },
  watch: {
    visible(newVal, oldVal) {
      this.setFormVisible = newVal;
    },
    setFormVisible(newVal, oldVal) {
      if (newVal === false) {
        this.$emit("dlgdo", newVal);
      }
    }
  },
  created() {
    this.initData();
  },
  mounted: function () {},
  computed: {},
  methods: {
    insertTextVal(obj, val){
      obj.template += val
    },
    parkChange(id){
      this.getNotifyConfigByParkId(id)
    },
    async getNotifyConfigByParkId(parkId){
      try{
        const res = await getNotifyConfigByParkId(parkId)
        let resData = res.data.data
        if(resData.days){
          this.dataform.accounts = resData.accounts
          this.dataform.days = resData.days
          this.dataform.notifyType = resData.notifyType
          this.dataform.template = resData.template
          this.emails = []
          this.dataform.accounts.forEach(el=>{
            this.emails.push({el: el})
          })
        }else{
          this.initFormData()
        }
      }catch(err){
        this.initFormData()
      }
    },
    initFormData(){
      this.dataform.accounts = []
      this.dataform.days = ''
      this.dataform.notifyType = 1
      this.dataform.template = ''
      this.emails = []
    },
    addAccount(){
      this.emails.push({el:''})
    },
    delAccount(index){
      this.emails.splice(index, 1)
    },
    initData() {
      this.setFormVisible = this.visible;
    },
    async getDetail() {

    },
    initForm(){
      this.dataform= {}
    },
    resetSetForm(formName) {
      this.setFormVisible = false;
      this.setLoading = false;
      this.initForm()
      // this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    async editSubmit(formName) {
      let arr = []
      this.emails.forEach(el=>{
        arr.push(el.el)
      })
      this.dataform.accounts = arr
      this.$refs[formName].validate(valid => {
        if (valid){
          saveNotifyConfig(this.dataform).then(res => {
            if(res.data.code==0){
              this.resetSetForm(formName)
              this.$emit("dlgdoSuccess");
              this.$notify({
                title: '成功',
                message: this.mTitle,
                type: 'success'
              });
            }
          });
        } else {
          return false
        }
      });
    },
  },
};
</script>
<style lang="scss" scoped>
.noticeSet_form ::v-deep {
  .el-form-item__label{
    text-align: left;
  }
  .dv1{
    display: flex;
    .el-input{
      width: 80px;
      margin: 0 10px;
    }
  }
  .dv2{
    textarea{
      height: 80px;
      margin-bottom: 10px;
    }
  }
  .dv3{
    width: 80%;
    .row{
      position: relative;
      text-align: center;
      border: 1px solid #e0e0e0;
      border-bottom: none;
      .btn{
        position: absolute;
        right: -50px;
        top: 5px;
      }
      .el-input__inner{
        border: none;
        text-align: center;
      }
    }
    .row:last-child{
      border-bottom: 1px solid #e0e0e0;
    }
  }
}
</style>