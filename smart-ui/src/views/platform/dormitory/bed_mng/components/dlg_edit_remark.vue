<!--修改备注：从 bed_mng/index.vue 抽出的内联弹窗。
契约沿用本目录既有子组件（dlg_note_pre / dlg_edit_time）：
父级传 :visible 与 :row，关闭时 emit('dlgdo', false)，保存成功后 emit('refresh') 让父级刷新列表。-->
<template>
  <el-dialog
    :visible.sync="setFormVisible"
    :close-on-click-modal="false"
    title="修改备注"
    class="dialog_form"
    width="400px"
  >
    <el-form 
      ref="simpleRemarkForm" 
      :model="form" 
      :rules="rules">
      <el-form-item 
        label="请输入备注" 
        prop="simpleRemark">
        <el-input 
          v-model="form.simpleRemark" 
          placeholder="请输入" 
          clearable />
      </el-form-item>
    </el-form>
    <div 
      slot="footer" 
      class="dialog-footer">
      <el-button 
        type="primary" 
        plain 
        @click="setFormVisible = false">取 消</el-button>
      <el-button 
        :loading="loading" 
        type="primary" 
        @click="handleSubmit('simpleRemarkForm')">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { updateSimpleRemark } from "@/api/platform/dormitory/bed_mng";

export default {
  name: "DlgEditRemark",
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    // 待编辑的行：{ id, simpleRemark }。父级须保证置 visible=true 前已填充 row，
    // 默认 {} 仅为满足 require-default-prop，并非有效的打开态。
    row: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      setFormVisible: false,
      loading: false,
      form: {
        id: "",
        simpleRemark: ""
      },
      rules: {
        simpleRemark: [
          { required: true, message: "请输入备注", trigger: "blur" }
        ]
      }
    };
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        // 打开时用父级行数据初始化表单（等价原 updateRemark 的赋值）
        this.form = { id: this.row.id, simpleRemark: this.row.simpleRemark };
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
    handleSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.loading = true;
          // 注意：表单字段 simpleRemark 映射成接口的 remark（沿用原 handleEditSimpleRemark）
          updateSimpleRemark({
            id: this.form.id,
            remark: this.form.simpleRemark
          })
            .then(() => {
              this.setFormVisible = false;
              this.loading = false;
              this.$emit("refresh");
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
