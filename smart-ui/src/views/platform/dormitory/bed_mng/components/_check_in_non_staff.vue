<template>
  <div>
    <el-form ref="form" :inline="true" :model="dataform" label-width="80px" :rules="rules">
      <el-form-item label="工号" prop="badge">
        <el-input v-model="dataform.badge" style="width:240px" placeholder="请输入工号" clearable></el-input>
      </el-form-item>
      <el-form-item label="姓名" prop="name">
        <el-input v-model="dataform.name" style="width:240px" placeholder="请输入姓名" clearable></el-input>
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-radio v-model="dataform.sex" :label="0" style="width:60px">男</el-radio>
        <el-radio v-model="dataform.sex" :label="1" style="width:150px">女</el-radio>
      </el-form-item>
      <el-form-item label="职务" prop="jobName">
        <el-input v-model="dataform.jobName" style="width:240px" placeholder="请输入职务" clearable></el-input>
      </el-form-item>
      <el-form-item label="入住日期" prop="createTime">
        <el-date-picker
        style="width:240px!important"
          v-model="dataform.createTime"
          type="date"
          value-format="yyyy-MM-dd HH:mm:ss"
          placeholder="请选择入住日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="备注" prop="simpleRemark">
        <el-input
        style="width:240px"
        type="textarea"
        maxlength="20"
        placeholder="20字以内"
        v-model="dataform.simpleRemark"
        show-word-limit="true"></el-input>
      </el-form-item>
    </el-form>
    <div class="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </div>
</template>

<script>
import { addDormitory } from "@/api/platform/dormitory/bed_mng";
export default {
  data() {
    return {
      dataform:{
        bedId: "",
        badge: "",
        name: "",
        createTime: "",
        simpleRemark: "",
        sex: 0
      },
      rules: {
        badge: [{ required: true, message: "请输入工号", trigger: "blur" }],
        name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
        sex: [{ required: true, message: "请选择性别", trigger: "change" }],
        createTime: [
          { required: true, message: "请选择入住日期", trigger: "change" }
        ]
      },
      btnLoading: false,
    }
  },
  props: {
    visible: Boolean,
    dataItem: Object
  },
  created() {},
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
      this.btnLoading = true
      this.dataform.bedId = this.dataItem.bedId
      const res = await addDormitory(this.dataform)
      this.btnLoading = false
      if(res.data.data){
        this.$message({
          message: '入住成功',
          type: 'success'
        });
        this.refresh()
      }else{
        this.$notify({
          title: '失败',
          message: res.data.message,
          type: "error",
          duration: 2000
        });
      }
    },
    refresh() {
      this.$refs.form && this.$refs.form.resetFields()
      this.btnLoading = false
      this.$emit('refresh')
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.btnLoading = false
      this.$emit('cancel')
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
  .footer{
    padding: 20px 0 30px 0;
    text-align: right;
  }
</style>
