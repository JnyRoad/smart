<!--编辑非员工入住：从 bed_mng/index.vue 抽出的内联弹窗。
契约沿用本目录既有子组件（dlg_edit_time / dlg_edit_remark）：父级传 :visible 与 :row，
关闭时 emit('dlgdo', false)，入住成功后 emit('refresh') 让父级刷新列表。
注意：原弹窗未设 close-on-click-modal，此处保持一致；取消按钮沿用 resetFields + 关闭。-->
<template>
  <el-dialog 
    :visible.sync="setFormVisible" 
    title="编辑非员工入住" 
    class="dialog_form" 
    width="550px">
    <el-form 
      ref="editCheckInForm" 
      :model="form" 
      :rules="rules" 
      label-width="80px">
      <el-form-item 
        label="工号" 
        prop="staffBadge">
        <el-input 
          v-model="form.staffBadge" 
          placeholder="请输入工号" 
          clearable/>
      </el-form-item>
      <el-form-item 
        label="姓名" 
        prop="staffName">
        <el-input 
          v-model="form.staffName" 
          placeholder="请输入姓名" 
          clearable/>
      </el-form-item>
      <el-form-item 
        label="性别" 
        prop="staffSex">
        <el-radio 
          v-model="form.staffSex" 
          :label="0">男</el-radio>
        <el-radio 
          v-model="form.staffSex" 
          :label="1">女</el-radio>
      </el-form-item>
      <el-form-item 
        label="职务" 
        prop="jobName">
        <el-input 
          v-model="form.jobName" 
          placeholder="请输入职务" 
          clearable/>
      </el-form-item>
    </el-form>
    <div 
      slot="footer" 
      class="dialog-footer">
      <el-button 
        type="primary" 
        plain 
        @click="handleCancel">取 消</el-button>
      <el-button 
        :loading="loading" 
        type="primary" 
        @click="handleSubmit('editCheckInForm')">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { editCheckInDormitory } from "@/api/platform/dormitory/bed_mng";

// 表单初值（与原 index.vue data.editCheckInForm 一致，供 resetFields 复位用）
function emptyForm() {
  return {
    id: "",
    bedId: "",
    staffBadge: "",
    staffName: "",
    jobName: "",
    staffSex: undefined
  };
}

export default {
  name: "DlgEditCheckIn",
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    // 待编辑的行（已由父级 editCheckInInfo 做好字段映射）：
    // { id, bedId, staffBadge, staffName, jobName, staffSex }。
    // 父级须保证置 visible=true 前已填充 row，默认 {} 仅为满足 require-default-prop。
    row: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      setFormVisible: false,
      loading: false,
      form: emptyForm(),
      rules: {
        staffBadge: [
          { required: true, message: "请输入工号", trigger: "blur" }
        ],
        staffName: [{ required: true, message: "请输入姓名", trigger: "blur" }],
        staffSex: [{ required: true, message: "请选择性别", trigger: "change" }]
      }
    };
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        // 打开时用父级传入的（已映射好的）行数据初始化表单
        this.form = { ...emptyForm(), ...this.row };
      }
      this.setFormVisible = newVal;
    },
    setFormVisible(newVal) {
      if (newVal === false) {
        this.$emit("dlgdo", newVal);
      }
    }
  },
  created() {
    this.setFormVisible = this.visible;
  },
  methods: {
    // 取消：沿用原 resetEditCheckInForm，先 resetFields 再关闭
    handleCancel() {
      this.$refs.editCheckInForm.resetFields();
      this.setFormVisible = false;
    },
    handleSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.loading = true;
          editCheckInDormitory(this.form)
            .then(response => {
              const data = response.data.data;
              this.loading = false;
              if (data) {
                this.setFormVisible = false;
                this.$notify({
                  title: "成功",
                  message: "入住成功",
                  type: "success",
                  duration: 2000
                });
                this.$emit("refresh");
              } else {
                this.$notify({
                  title: "失败",
                  message: response.data.message,
                  type: "error",
                  duration: 2000
                });
              }
            })
            .catch(() => {
              this.loading = false;
            });
        } else {
          return false;
        }
      });
    }
  }
};
</script>
