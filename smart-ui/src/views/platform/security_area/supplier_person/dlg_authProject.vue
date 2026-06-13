<!--授权项目-->
<template>
  <el-dialog
    :title="mTitle"
    class="dialog_form authProject_form"
    width="700px"
    @close="resetSetForm('dataform')"
    :visible.sync="setFormVisible"
  >
    <el-form ref="dataform" :model="dataform" :rules="addRule" label-position="top">
      <el-row>
        <el-col :span="12">
          <el-form-item label="选择供应商" prop="ids">
            <div class="dv1 dv2">
              <supplierList ref="supplierList"/>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="10" :offset="2">
          <el-form-item label="输入授权项目" prop="authorList">
            <el-input v-model="dataform.authorList" placeholder="授权项目以'/'分割，例如：Y12020/Y12024/Y12026"  class="dv1" type="textarea"/>
          </el-form-item>
        </el-col>
      </el-row>
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
import { supplierBatchAuth } from "./_service.js";
import supplierList from "./_supplier_select";
export default {
  name: "",
  components: {
    supplierList
  },
  data() {
    return {
      setFormVisible: false,
      setLoading: false, //是否正在设置
      dataform: {
        ids: [],
        authorList:''
      },
      addRule: {
        authorList: [
          { required: true, message: '请输入授权项目', trigger: 'blur' }
        ]
      }
    };
  },
  props: {
    mTitle: {
      type: String,
      default: function(){
        return '设置授权项目'
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
      this.$refs.supplierList.clear();
      this.initForm()
      // this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    async editSubmit(formName) {
      let ids = this.$refs.supplierList.getCheckedKeys()
      if(ids.length===0){
        this.$message.error('请选择供应商');
        return
      }else{
        this.dataform.ids = ids
      }
      this.$refs[formName].validate(valid => {
        if (valid){
          supplierBatchAuth(this.dataform).then(res => {
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
.authProject_form ::v-deep {
  .dv1{
    height: 350px;
    textarea{
      height: 100%;
    }
  }
  .dv2{
    padding: 10px 0;
    border: 1px solid #eee;
  }
}
</style>