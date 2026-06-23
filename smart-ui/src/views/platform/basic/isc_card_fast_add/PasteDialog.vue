<template>
  <el-dialog
    :visible="visible"
    :close-on-click-modal="!resolving"
    :close-on-press-escape="!resolving"
    :show-close="!resolving"
    title="批量粘贴录卡"
    width="680px"
    custom-class="isc-paste-dialog"
    append-to-body
    @update:visible="$emit('update:visible', $event)"
  >
    <div class="paste-dialog-body">
      <div class="paste-guide">
        <div class="paste-guide-main">
          <div class="paste-guide-title">
            <i class="el-icon-document-copy"/>
            <span>按行粘贴工号和卡号</span>
          </div>
          <div class="paste-desc">每行一条，工号在前、卡号在后；支持空格、Tab、逗号分隔，最多200行。</div>
        </div>
        <div class="paste-example">
          <div class="example-title">示例</div>
          <div>10288 1024388812</div>
          <div>10290 1024388845</div>
        </div>
      </div>

      <el-input
        :value="text"
        :rows="9"
        :disabled="resolving"
        :placeholder="placeholder"
        class="paste-input"
        type="textarea"
        resize="vertical"
        @input="$emit('update:text', $event)"
      />

      <div
        :class="{ ready: canSubmit, invalid: hasVisibleProblem }"
        class="paste-summary"
      >
        <div class="paste-stat">
          <span>已识别</span>
          <strong>{{ rows.length }}</strong>
          <span>/ 200行</span>
        </div>
        <div
          v-if="!rows.length"
          class="paste-status">
          <i class="el-icon-edit-outline"/>
          <span>等待粘贴数据</span>
        </div>
        <div
          v-else-if="isOverLimit"
          class="paste-status error">
          <i class="el-icon-warning-outline"/>
          <span>超出{{ rows.length - 200 }}行</span>
        </div>
        <div
          v-else-if="errors.length"
          class="paste-status error">
          <i class="el-icon-warning-outline"/>
          <span>发现{{ errors.length }}条问题</span>
        </div>
        <div
          v-else
          class="paste-status ok">
          <i class="el-icon-success"/>
          <span>格式校验通过</span>
        </div>
      </div>

      <div
        v-if="hasVisibleProblem"
        class="paste-errors">
        <div class="paste-errors-title">请先处理以下问题</div>
        <div v-if="isOverLimit">超过200行，请删除多余数据后再提交。</div>
        <div
          v-for="item in visibleErrors"
          :key="item.line">第{{ item.line }}行：{{ item.message }}</div>
        <div
          v-if="errors.length > visibleErrors.length"
          class="paste-errors-more">
          还有{{ errors.length - visibleErrors.length }}条问题未显示
        </div>
      </div>
    </div>

    <span
      slot="footer"
      class="paste-footer">
      <el-button
        :disabled="resolving"
        size="mini"
        @click="$emit('update:visible', false)">取消</el-button>
      <el-button
        :loading="resolving"
        :disabled="!canSubmit || resolving"
        type="primary"
        size="mini"
        @click="$emit('confirm')"
      >校验并加入队列</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'IscCardFastAddPasteDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    resolving: {
      type: Boolean,
      default: false
    },
    text: {
      type: String,
      default: ''
    },
    rows: {
      type: Array,
      default() {
        return []
      }
    },
    errors: {
      type: Array,
      default() {
        return []
      }
    },
    visibleErrors: {
      type: Array,
      default() {
        return []
      }
    },
    placeholder: {
      type: String,
      default: ''
    }
  },
  computed: {
    isOverLimit() {
      return this.rows.length > 200
    },
    hasVisibleProblem() {
      return this.errors.length > 0 || this.isOverLimit
    },
    canSubmit() {
      return this.rows.length > 0 && !this.hasVisibleProblem
    }
  }
}
</script>
