<template>
  <el-dialog
    ref="dialog"
    :title="mTitle"
    :visible.sync="currVisible"
    width="1000px"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'approve-detail-dialog'"
  >
    <el-form ref="form" :model="dataform" :rules="addRule" label-width="80px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="dataform.name" placeholder="请输入"/>
      </el-form-item>
      <el-form-item label="工号" prop="badge">
        <el-input v-model="dataform.badge" placeholder="请输入"/>
      </el-form-item>
      <el-form-item label="身份证号" prop="certno">
        <el-input v-model="dataform.certno" placeholder="请输入"/>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="dataform.phone" placeholder="请输入"/>
      </el-form-item>
      <el-form-item label="家属关系" prop="relation">
        <el-select v-model="dataform.relation" placeholder="请选择">
          <el-option
            v-for="item in relations"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <div class="family_tips">
        <p>家属关系说明</p>
        <p>直系血亲：包括祖父母、外祖父母、父母、子女、孙子女、外孙子女等</p>
        <p>三代以内旁系血亲：包括兄弟姐妹、堂兄弟姐妹、表兄弟姐妹，叔伯姑舅姨、侄子女、甥子女等</p>
        <p>近姻亲：配偶的父母、配偶的兄弟姐妹及其配偶、子女的配偶及子女配偶的父母、三代以内旁系血亲的配偶</p>
      </div>
    </el-form>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { addFamily } from "../_service.js";
import { isMobile } from "@/util/validate";

const RELATIONSHIPS = [
  {label: '夫妻', value: 1},
  {label: '直系血亲', value: 2},
  {label: '旁系血亲', value: 3},
  {label: '近姻亲', value: 4},
  {label: '其他', value: 5}
]
export default {
  data() {
    /**
     * 校验手机号
     */
    var vMobile = (rule, value, callback) => {
      let r = isMobile(value)
      if (!r) {
        callback(new Error('手机格式不正确'))
      } else {
        callback()
      }
    }
    /**
     * 校验身份证号
     */
    var vCardId = (rule, value, callback) => {
      let codeReg = new RegExp("[\u4E00-\u9FA5]+") //正则 不能输入汉字  ；
      let len = value.length,
      str='';
      for(var i=0;i<len;i++){
        if(codeReg.test(value[i])){
          str+=value[i];
        }
      }
      if(str.length>0){
        callback(new Error('证件号码不能包含汉字'))
      }else{
        if(len>7 && len<20){
          callback()
        }else{
          callback(new Error('证件号码需在8~20位之间'))
        }
      }
    }
    return {
      btnLoading: false,
      currVisible: false,
      relations: RELATIONSHIPS, //亲属关系
      dataform: {
        certno: '',
        name: '',
        badge: '',
        phone: '',
        relation: ''
      },
      addRule: {
        certno: [
          { required: true, message: '请输入身份证号', trigger: 'blur' },
          { validator: vCardId, trigger: 'blur' }
        ],
        name: [
          { required: true, message: '请输入姓名', trigger: 'blur' },
        ],
        phone: [
          { required: true, message: '请输入联系方式', trigger: 'blur' },
          { validator: vMobile, trigger: 'blur' }
        ],
        relation: [
          { required: true, message: '请选择亲属关系', trigger: 'change' },
        ]
      }
    }
  },
  props: {
    visible: Boolean,
    mTitle: {
      type: String,
      default: function(){
        return '添加家属'
      }
    },
    row: undefined,
    curFamiley: undefined
  },
  created() {},
  watch: {
    row:{
      handler:function(newVal, oldVal) {},
      immediate: true
    },
    curFamiley:{
      handler:function(newVal, oldVal) {},
      immediate: true
    },
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        if( this.curFamiley && this.curFamiley.id){
          this.dataform = Object.assign({}, this.curFamiley)
        }else{
          this. dataform = {
            certno: '',
            name: '',
            badge: '',
            phone: '',
            relation: ''
          }
        }
      }
    }
  },
  methods: {
    /**
     * 验证表单
     */
    validateForm() {
      if (this.$refs.form) {
        return this.$refs.form.validate()
      }
      return Promise.resolve()
    },
    async formSumit() {
      await this.validateForm()
      if(!this.row.staffBadge){
        this.$message.error('获取员工信息失败！');
        return
      }
      this.dataform.staffBadge = this.row.staffBadge
      this.btnLoading = true
      const res = await addFamily(this.dataform)
      if(res.data.code==0){
        this.$notify({
          title: '成功',
          message: this.mTitle,
          type: 'success'
        })
        this.refresh()
      }
      this.btnLoading = false
    },
    refresh() {
      this.$emit('refresh')
      this.currVisible = false
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
      this.btnLoading = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss">

</style>
