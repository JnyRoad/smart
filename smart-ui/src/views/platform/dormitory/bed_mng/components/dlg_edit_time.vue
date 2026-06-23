<!--修改入住时间：从 bed_mng/index.vue 抽出的内联弹窗。
契约沿用本目录既有子组件（dlg_note_pre / dlg_dorm_change）：
父级传 :visible 与 :row，关闭时 emit('dlgdo', false)，保存成功后 emit('refresh') 让父级刷新列表。-->
<template>
  <el-dialog
    :visible.sync="setFormVisible"
    :close-on-click-modal="false"
    title="修改入住时间"
    class="dialog_form"
    width="400px"
  >
    <el-form 
      ref="timeForm" 
      :model="form" 
      :rules="rules">
      <el-form-item 
        label="请选择入住时间" 
        prop="createTime">
        <el-date-picker
          v-model="form.createTime"
          :picker-options="pickerOptions"
          type="date"
          format="yyyy-MM-dd"
          value-format="yyyy-MM-dd"
          clearable
        />
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
        @click="handleSubmit('timeForm')">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { updateDormitoryStaff } from "@/api/platform/dormitory/bed_mng";

export default {
  name: "DlgEditTime",
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    // 待编辑的行：{ id, createTime }。父级须保证置 visible=true 前已填充 row，
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
        createTime: ""
      },
      rules: {
        createTime: [
          { required: true, message: "请输入入住时间", trigger: "blur" }
        ]
      },
      pickerOptions: {
        // 不允许选择今天之后的日期（沿用原 timePickerOptions）
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      }
    };
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        // 打开时用父级行数据初始化表单（等价原 editTime 的赋值）
        this.form = { id: this.row.id, createTime: this.row.createTime };
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
          updateDormitoryStaff(this.form)
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
