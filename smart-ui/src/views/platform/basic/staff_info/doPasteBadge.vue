<template>
  <el-dialog
    ref="dialog"
    :visible.sync="currVisible"
    width="600px"
    :show-close="true"
    @open="open"
    @close="close"
    :close-on-click-modal="false"
    :append-to-body="true"
    custom-class="paste-badge-dialog"
    title="批量添加工号">

    <!-- 主体内容 -->
    <el-form ref="form" class="form">
      <div class="tip-box">
        <i class="el-icon-info"></i>
        <span>请直接将员工工号粘贴到下面框里，每个一行，最多输入200个</span>
      </div>

      <el-form-item class="input-area">
        <el-input
          type="textarea"
          class="staffs"
          v-model="badges"
          @input="handleInput"
          @paste.native.prevent="handlePaste"
          placeholder="请输入工号，每行一个"
          :maxlength="4000"
          clearable>
        </el-input>
      </el-form-item>

      <!-- 进度条和警告提示 -->
      <div class="progress-container">
        <el-progress
          :percentage="progressPercentage"
          :status="rowsNum > 0 ? progressStatus : undefined"
          :stroke-width="8"
          :format="progressFormat">
        </el-progress>

        <!-- 超出行数警告 -->
        <div v-if="rowsNum > 200" class="warning-box">
          <i class="el-icon-warning"></i>
          <span>当前已输入 {{rowsNum}} 行，超出200行限制，请手动删除多余内容</span>
        </div>

        <!-- 错误提示框 -->
        <div v-if="invalidBadges.length > 0" class="error-box">
          <div class="error-title">
            <i class="el-icon-warning"></i>
            <span>以下工号格式有误（仅允许英文和数字）：</span>
          </div>
          <div class="error-content">
            <span
              v-for="(item, index) in invalidBadges"
              :key="index"
              class="error-item">
              <el-tag size="mini" type="danger">第{{item.line}}行</el-tag>
              {{item.value}}
            </span>
          </div>
        </div>
      </div>
    </el-form>

    <!-- 底部按钮 -->
    <div slot="footer" class="dialog-footer">
      <el-button @click="cancel" plain>取 消</el-button>
      <el-button
        @click="resetFrom()"
        :type="badges ? 'warning' : 'info'"
        :plain="!badges"
        :disabled="!badges">
        清空
      </el-button>
      <el-button
        type="primary"
        @click="formSumit()"
        :loading="btnLoading"
        :disabled="rowsNum === 0">
        确 定
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    return {
      btnLoading: false,
      currVisible: false,
      badges: '',
      rowsNum: 0,
      lastValidValue: '',
      inputTimer: null,
      invalidBadges: [] // 存储错误的工号信息
    }
  },
  props: {
    visible: Boolean,
    dataItem: String
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      }
    }
  },
  computed: {
    progressPercentage() {
      return Math.min(Math.floor((this.rowsNum / 200) * 100), 100);
    },

    progressStatus() {
      if (this.rowsNum === 0) return '';
      if (this.rowsNum > 200) return 'exception';
      if (this.rowsNum > 180) return 'warning';
      return 'success';
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
    /**
     * 验证工号格式
     */
    validateBadges(badges) {
      if (!badges) return { isValid: true, invalidBadges: [], cleanBadges: '' };

      const badgeList = badges.split('\n');
      const invalidBadges = [];
      const pattern = /^[A-Za-z0-9]+$/;

      badgeList.forEach((badge, index) => {
        if (badge && !pattern.test(badge)) {
          invalidBadges.push({
            line: index + 1,
            value: badge
          });
        }
      });

      this.invalidBadges = invalidBadges;

      return {
        isValid: invalidBadges.length === 0,
        invalidBadges,
        cleanBadges: badges
      };
    },
    /**
     * 提交前的验证
     */
    async formSumit() {
      // 提交时检查行数
      if (this.rowsNum > 200) {
        this.$message({
          message: '行数超出限制，请删除多余内容后再提交',
          type: 'error',
          duration: 3000
        });
        return;
      }

      const validateResult = this.validateBadges(this.badges);

      if (!validateResult.isValid) {
        const errorLines = validateResult.invalidBadges
          .map(item => `第${item.line}行: ${item.value}`)
          .join('\n');

        this.$message({
          message: '以下工号包含非法字符（只允许英文和数字）：\n' + errorLines,
          type: 'error',
          duration: 5000,
          showClose: true
        });
        return;
      }

      this.badges = validateResult.cleanBadges;
      this.addSubmit();
    },
    /**
     * 计算实际行数
     */
    getAddRow() {
      if (!this.badges) return 0;
      return this.badges.split('\n').length;
    },
    /**
     * 添加
     */
    async addSubmit() {
      // console.log(this.addform.badges)
      this.$emit('refresh', this.badges)
      this.currVisible = false
    },
    resetFrom(){
      this.badges = ''
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.badges = ''
      this.currVisible = false
    },
    open() {
      this.currVisible = true
      this.badges = this.dataItem
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.badges = ''
      this.currVisible = false
    },
    progressFormat(percentage) {
      // 显示实际行数，而不是百分比
      return `已输入: ${this.rowsNum}/200`
    },
    /**
     * 处理内容，去除空行并限制行数
     */
    processContent(content) {
      // 分割成数组并去除每行前后空格
      const lines = content.split('\n')
        .map(line => line.trim())
        .filter(line => line !== ''); // 过滤掉空行

      // 如果超过200行，只取前200行
      if (lines.length > 200) {
        this.$message({
          message: '已超过200行，仅保留前200行内容',
          type: 'warning',
          duration: 3000
        });
        return lines.slice(0, 200).join('\n');
      }

      return lines.join('\n');
    },

    /**
     * 处理输入事件
     */
    handleInput() {
      clearTimeout(this.inputTimer);
      this.inputTimer = setTimeout(() => {
        this.updateContent(this.badges);
      }, 300);
    },

    /**
     * 处理粘贴事件
     */
    handlePaste(e) {
      // 获取剪贴板数据
      const clipboardData = e.clipboardData || window.clipboardData;
      const pastedData = clipboardData.getData('text');

      // 处理粘贴的内容，只过滤空行
      const lines = pastedData
        .split('\n')
        .map(line => line.trim())
        .filter(line => line !== ''); // 过滤空行

      // 设置内容，不限制行数
      this.badges = lines.join('\n');

      // 如果超过200行，显示警告消息
      if (lines.length > 200) {
        this.$message({
          message: `当前共 ${lines.length} 行，超出200行限制，请手动删除多余内容`,
          type: 'warning',
          duration: 5000,
          showClose: true
        });
      }

      // 更新内容和验证
      this.$nextTick(() => {
        this.updateContent(this.badges);
      });
    },

    /**
     * 统一处理内容更新
     */
    updateContent(value) {
      if (!value) {
        this.rowsNum = 0;
        this.invalidBadges = [];
        return;
      }

      // 更新行数统计
      this.rowsNum = value.split('\n').length;

      // 验证格式
      const validateResult = this.validateBadges(value);
      this.lastValidValue = value;
    }
  },
  mounted() {},
  beforeDestroy() {
    // 清理定时器
    clearTimeout(this.inputTimer);
  }
}
</script>

<style lang="scss" scoped>
.paste-badge-dialog {
  .form {
    margin-bottom: 20px;
  }

  .tip-box {
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: #f0f9eb;
    border-radius: 4px;
    margin-bottom: 12px;
    color: #67c23a;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 2px 12px rgba(0,0,0,0.1);
    }

    i {
      margin-right: 8px;
      font-size: 16px;
    }
  }

  .input-area {
    margin: 0;
  }

  .staffs {
    ::v-deep textarea {
      min-height: 200px !important;
      padding: 12px;
      font-size: 14px;
      line-height: 1.6;
      border-radius: 4px;
      transition: all 0.3s ease;

      &:focus {
        border-color: #409EFF;
        box-shadow: 0 0 0 2px rgba(64,158,255,0.2);
      }
    }
  }

  .progress-container {
    margin-top: 12px;
    padding: 0 4px;

    ::v-deep .el-progress-bar__inner {
      transition: all 0.3s ease;
    }

    ::v-deep .el-progress--line.is-exception .el-progress-bar__inner {
      background-color: #f56c6c;
    }
  }

  .error-box {
    margin-top: 12px;
    padding: 12px 16px;
    background: #fff3f3;
    border-radius: 4px;
    border: 1px solid #ffd4d4;
    transition: all 0.3s ease;

    .error-title {
      display: flex;
      align-items: center;
      color: #f56c6c;
      margin-bottom: 8px;
      font-weight: 500;

      i {
        margin-right: 8px;
        font-size: 16px;
      }
    }

    .error-content {
      line-height: 1.8;
      color: #666;
      font-size: 13px;
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .error-item {
        display: inline-flex;
        align-items: center;
        gap: 4px;

        .el-tag {
          margin-right: 4px;
        }
      }
    }
  }

  .dialog-footer {
    text-align: right;
    padding-top: 20px;
  }

  .warning-box {
    margin-top: 8px;
    padding: 8px 12px;
    background: #fdf6ec;
    border-radius: 4px;
    color: #e6a23c;
    font-size: 13px;
    display: flex;
    align-items: center;

    i {
      margin-right: 8px;
      font-size: 14px;
    }
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}
</style>
