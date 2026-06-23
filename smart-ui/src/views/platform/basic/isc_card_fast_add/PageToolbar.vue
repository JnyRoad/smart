<template>
  <div class="top-menu clear">
    <div class="page-head">
      <div class="page-title">
        <span class="title-bar"/>
        <span>ISC卡片快速维护</span>
      </div>
      <div class="page-desc">先选择园区和员工，再刷卡或批量粘贴卡号；确认无误后提交，系统会自动同步到ISC。</div>
    </div>
    <div class="top-right">
      <el-button
        :disabled="!selectedPark || staffLoading"
        type="primary"
        icon="el-icon-search"
        @click="$emit('search-staff')">搜索员工</el-button>
      <el-button
        type="primary"
        icon="el-icon-delete"
        plain
        @click="$emit('reset')">清空</el-button>
      <el-button
        :disabled="!selectedPark"
        type="primary"
        icon="el-icon-document"
        plain
        @click="$emit('open-paste')">批量粘贴</el-button>
      <el-button
        :loading="submitting"
        :disabled="!canSubmit"
        type="primary"
        icon="el-icon-check"
        @click="$emit('submit')">批量提交{{ submitCountText }}</el-button>
      <el-button
        icon="el-icon-refresh"
        @click="$emit('open-tasks')">查看同步任务</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'IscCardFastAddPageToolbar',
  props: {
    selectedPark: {
      type: Object,
      default: null
    },
    staffLoading: {
      type: Boolean,
      required: true
    },
    submitting: {
      type: Boolean,
      required: true
    },
    canSubmit: {
      type: Boolean,
      required: true
    },
    submitCountText: {
      type: String,
      required: true
    }
  }
}
</script>

<style lang="scss" scoped>
.top-menu {
  min-height: 64px;
  position: relative;
}

.page-head {
  padding-right: 580px;
}

.page-title {
  display: flex;
  align-items: center;
  color: #333;
  font-size: 16px;
  font-weight: 600;
  line-height: 24px;
}

.title-bar {
  width: 3px;
  height: 16px;
  margin-right: 8px;
  border-radius: 2px;
  background: #ed6d00;
}

.page-desc {
  margin-top: 6px;
  color: #999;
  font-size: 12px;
  line-height: 18px;
}
</style>
