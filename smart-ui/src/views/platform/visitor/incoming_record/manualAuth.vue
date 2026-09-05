<!--访客入厂申请手动下发 ISC 人员权限 -->
<template>
  <el-dialog
    title="通关权限分配"
    class="dialog_form auth-dialog mac-style"
    width="700px"
    :visible.sync="dialogVisible"
    top="5vh"
    :show-close="false"
    :close-on-press-escape="false"
    :close-on-click-modal="false"
    @close="handleDialogClose"
  >
    <div class="manual-auth-dialog">
      <div v-if="loading" class="manual-auth-loading">正在查询可下发对象和权限，请稍等！</div>
      <template v-else>
        <div class="selected-staff-info">
          <div class="info-content">
            <i class="el-icon-user"></i>
            <div class="subject-content">
              <div class="subject-heading">
                <span class="manual-auth-label">本单人员</span>
                <el-select
                  v-model="selectedTargetId"
                  placeholder="请选择本申请人员"
                  filterable
                  clearable
                  :disabled="fellows.length === 0 || submitLoading"
                  @change="handleTargetChange"
                >
                  <el-option
                    v-for="fellow in fellows"
                    :key="fellow.id"
                    :label="fellow.name"
                    :value="fellow.id"
                  />
                </el-select>
              </div>
              <span class="manual-auth-apply">申请单 {{ applyId || '未知' }}</span>
            </div>
          </div>
          <p class="manual-auth-notice">仅支持人员ISC权限，车辆暂不支持。</p>
        </div>

        <div v-if="fellows.length === 0" class="manual-auth-empty">
          当前申请没有可下发的人员。
        </div>

        <div v-else class="auth-container">
          <div class="auth-panel">
            <div class="panel-header">
              <span class="panel-title">待选权限</span>
              <el-input
                v-model="authSearchKey"
                placeholder="搜索权限"
                prefix-icon="el-icon-search"
                size="small"
                clearable
                :disabled="submitLoading"
              />
            </div>
            <div class="panel-content">
              <el-checkbox-group v-model="tempSelectedAuth">
                <el-checkbox
                  v-for="authority in filteredAuthList"
                  :key="authority.id"
                  :label="authority.id"
                  :disabled="submitLoading || isAuthorityDisabled(authority) || selectedAuth.includes(authority.id)"
                  class="auth-checkbox"
                  :aria-label="authority.authorityName"
                >
                  <span class="auth-name" :title="authority.authorityName">{{ authority.authorityName }}</span>
                  <span v-if="isAuthorityDisabled(authority)" class="auth-disabled-reason">
                    （保密考试校验暂未开通，暂不支持涉密权限下发）
                  </span>
                </el-checkbox>
              </el-checkbox-group>
              <div v-if="filteredAuthList.length === 0" class="auth-empty">当前对象没有可用权限组</div>
            </div>
          </div>

          <div class="auth-operations">
            <el-button
              type="primary"
              size="small"
              :disabled="submitLoading || tempSelectedAuth.length === 0"
              aria-label="添加权限"
              @click="addAuth"
            >
              <i class="el-icon-arrow-right"></i>
            </el-button>
            <el-button
              type="primary"
              size="small"
              :disabled="submitLoading || tempRemovedAuth.length === 0"
              aria-label="移除权限"
              @click="removeAuth"
            >
              <i class="el-icon-arrow-left"></i>
            </el-button>
          </div>

          <div class="auth-panel">
            <div class="panel-header">
              <span class="panel-title">已选权限 ({{ selectedAuth.length }})</span>
              <el-button type="text" :disabled="submitLoading || selectedAuth.length === 0" @click="clearSelected">清空</el-button>
            </div>
            <div class="panel-content">
              <el-checkbox-group v-model="tempRemovedAuth">
                <el-checkbox
                  v-for="authorityId in selectedAuth"
                  :key="authorityId"
                  :label="authorityId"
                  :disabled="submitLoading"
                  class="auth-checkbox"
                  :aria-label="getAuthorityName(authorityId)"
                >
                  <span class="auth-name" :title="getAuthorityName(authorityId)">{{ getAuthorityName(authorityId) }}</span>
                </el-checkbox>
              </el-checkbox-group>
              <div v-if="selectedAuth.length === 0" class="auth-empty">尚未选择权限组</div>
            </div>
          </div>
        </div>

        <div class="assign-validity">
          <span class="assign-validity__label">权限有效期</span>
          <div class="assign-validity__field">
            <span>开始</span>
            <el-input :value="options.startTime" readonly size="small" />
          </div>
          <div class="assign-validity__field">
            <span>结束</span>
            <el-input :value="options.endTime" readonly size="small" />
          </div>
          <span class="assign-validity__hint">有效期由申请单确定，不可修改。</span>
        </div>
      </template>
    </div>

    <div slot="footer" class="dialog-footer">
      <el-button size="small" plain :disabled="submitLoading" @click="closeDialog">取 消</el-button>
      <el-button
        type="primary"
        size="small"
        :loading="submitLoading"
        :disabled="!canSubmit"
        @click="submitAuth"
      >确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { buildManualAuthPayload, filterManualAuthAuthorities, xcIncomingRecordApi } from './_service'

/**
 * 创建手动授权弹窗的空选项结构。
 * @returns {Object} 可安全渲染的默认申请单授权选项。
 */
const EMPTY_OPTIONS = () => ({
  applyId: '',
  startTime: '',
  endTime: '',
  fellows: [],
  vehicles: [],
  authorities: []
})

export default {
  name: 'VisitorManualAuth',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    record: {
      type: Object,
      default: null
    }
  },
  /**
   * 初始化弹窗查询、对象和权限选择状态。
   * @returns {Object} Vue 响应式数据。
   */
  data () {
    return {
      dialogVisible: false,
      loading: false,
      submitLoading: false,
      requestSerial: 0,
      options: EMPTY_OPTIONS(),
      selectedTargetId: null,
      authSearchKey: '',
      selectedAuth: [],
      tempSelectedAuth: [],
      tempRemovedAuth: []
    }
  },
  computed: {
    /**
     * 返回当前查询结果中的申请单 ID。
     * @returns {string|number} 后端返回的申请单 ID，缺失时回退到列表记录 ID。
     */
    applyId () {
      return this.options.applyId || (this.record && this.record.id) || ''
    },
    /**
     * 返回申请单中的人员候选，车辆不进入可提交对象。
     * @returns {Array<Object>} 本申请可供 ISC 人员权限操作的人员列表。
     */
    fellows () {
      return Array.isArray(this.options.fellows) ? this.options.fellows : []
    },
    /**
     * 按当前对象和搜索词筛选可展示的权限组。
     * @returns {Array<Object>} 类型匹配的权限组，涉密项保留以展示禁用原因。
     */
    filteredAuthList () {
      const matched = filterManualAuthAuthorities(this.options.authorities)
      const key = (this.authSearchKey || '').trim().toLowerCase()
      if (!key) return matched
      return matched.filter(authority => {
        const name = String(authority.authorityName || '').toLowerCase()
        const description = String(authority.description || '').toLowerCase()
        return name.includes(key) || description.includes(key)
      })
    },
    /**
     * 判断当前表单是否具备一次普通人员权限下发所需的选择。
     * @returns {boolean} 只有人员、权限和非涉密权限均已选择时才允许提交。
     */
    canSubmit () {
      if (this.selectedTargetId === null || this.selectedTargetId === undefined || this.selectedTargetId === '') {
        return false
      }
      if (!this.selectedAuth.length || this.loading || this.submitLoading) return false
      return this.selectedAuth.every(id => {
        const authority = this.options.authorities.find(item => item.id === id)
        return authority && !this.isAuthorityDisabled(authority)
      })
    }
  },
  watch: {
    visible: {
      immediate: true,
      handler (value) {
        this.dialogVisible = value
        if (value) {
          this.loadOptions()
        } else {
          this.invalidateRequest()
          this.resetState()
        }
      }
    },
    record () {
      if (this.visible) this.loadOptions()
    }
  },
  methods: {
    /**
     * 查询当前申请的人员、有效期和权限组，并忽略已关闭弹窗的旧响应。
     * @returns {Promise<void>} 请求完成后更新当前弹窗；网络或业务错误仅提示用户。
     */
    async loadOptions () {
      const requestSerial = ++this.requestSerial
      this.loading = true
      this.options = EMPTY_OPTIONS()
      this.selectedTargetId = null
      this.resetSelection()

      const applyId = this.record && (this.record.id || this.record.applyId)
      if (!applyId) {
        this.loading = false
        this.showError('当前申请单缺少 ID，无法查询授权选项')
        return
      }

      try {
        const response = await xcIncomingRecordApi.getManualAuthOptions(applyId)
        if (!this.isCurrentRequest(requestSerial)) return
        const result = response && response.data ? response.data : {}
        if (result.code !== 0) {
          this.showError(result.msg || '查询授权选项失败')
          return
        }
        this.options = this.normalizeOptions(result.data)
        this.selectFirstFellow()
      } catch (error) {
        if (this.isCurrentRequest(requestSerial)) this.showError(this.getErrorMessage(error, '查询授权选项失败'))
      } finally {
        if (this.isCurrentRequest(requestSerial)) this.loading = false
      }
    },
    /**
     * 将接口响应补齐为弹窗可安全读取的完整结构。
     * @param {Object|null} data 后端返回的授权选项数据。
     * @returns {Object} 包含申请单、有效期、人员、车辆和权限组的规范对象。
     */
    normalizeOptions (data) {
      const value = data && typeof data === 'object' ? data : {}
      return {
        ...EMPTY_OPTIONS(),
        ...value,
        fellows: Array.isArray(value.fellows) ? value.fellows : [],
        vehicles: Array.isArray(value.vehicles) ? value.vehicles : [],
        authorities: Array.isArray(value.authorities) ? value.authorities : []
      }
    },
    /**
     * 选择首个人员作为默认授权对象，并清空任何不兼容的权限选择。
     * @returns {void} 更新对象选择和权限选择状态，不发起请求。
     */
    selectFirstFellow () {
      this.resetSelection()
      this.selectedTargetId = this.fellows.length ? this.fellows[0].id : null
    },
    /**
     * 在人员切换时清除旧权限，防止把上一对象的选择提交给当前对象。
     * @returns {void} 清空权限选择，不改变申请单有效期。
     */
    handleTargetChange () {
      this.resetSelection()
    },
    /**
     * 将待选权限移入已选区域，同时排除涉密权限和重复 ID。
     * @returns {void} 更新本地权限选择，不发起请求。
     */
    addAuth () {
      const addable = this.tempSelectedAuth.filter(id => {
        const authority = this.options.authorities.find(item => item.id === id)
        return authority && !this.isAuthorityDisabled(authority)
      })
      this.selectedAuth = [...new Set([...this.selectedAuth, ...addable])]
      this.tempSelectedAuth = []
    },
    /**
     * 从已选区域移除用户勾选的权限组。
     * @returns {void} 更新本地权限选择，不发起请求。
     */
    removeAuth () {
      this.selectedAuth = this.selectedAuth.filter(id => !this.tempRemovedAuth.includes(id))
      this.tempRemovedAuth = []
    },
    /**
     * 清空当前对象的全部权限选择。
     * @returns {void} 清空左右栏临时和已选状态。
     */
    clearSelected () {
      this.selectedAuth = []
      this.tempSelectedAuth = []
      this.tempRemovedAuth = []
    },
    /**
     * 读取权限组名称供已选区域展示。
     * @param {number|string} id 权限组 ID。
     * @returns {string} 权限组名称，找不到时返回空字符串。
     */
    getAuthorityName (id) {
      const authority = this.options.authorities.find(item => item.id === id)
      return authority ? authority.authorityName : ''
    },
    /**
     * 判断权限组是否因涉密考试依据缺失而不可下发。
     * @param {Object} authority 权限组候选。
     * @returns {boolean} areaType 为 1 时返回 true。
     */
    isAuthorityDisabled (authority) {
      return Number(authority && authority.areaType) === 1
    },
    /**
     * 按契约构造并提交单个人员的手动授权任务。
     * @returns {Promise<void>} 成功提示任务已提交并通知父列表刷新；失败保留当前选择。
     */
    async submitAuth () {
      if (this.submitLoading) return
      if (!this.canSubmit) {
        this.showWarning('请选择人员和普通权限组')
        return
      }

      const payload = buildManualAuthPayload({
        applyId: this.applyId,
        fellowId: this.selectedTargetId,
        authIds: this.selectedAuth
      })
      const submitSerial = ++this.requestSerial
      this.submitLoading = true
      try {
        const response = await xcIncomingRecordApi.submitManualAuth(payload)
        if (!this.isCurrentRequest(submitSerial)) return
        const result = response && response.data ? response.data : {}
        if (result.code !== 0) {
          this.showError(result.msg || '下发任务提交失败')
          return
        }
        this.$message({ message: '下发任务已提交', type: 'success' })
        this.$emit('submitted', result.data)
        this.closeDialog(true)
      } catch (error) {
        if (this.isCurrentRequest(submitSerial)) this.showError(this.getErrorMessage(error, '下发任务提交失败'))
      } finally {
        if (this.isCurrentRequest(submitSerial)) this.submitLoading = false
      }
    },
    /**
     * 关闭弹窗并使当前未完成查询失效，避免响应污染下一次打开。
     * @param {boolean} force 成功提交后允许关闭；普通用户操作在提交期间会被忽略。
     * @returns {void} 发出关闭事件并重置表单状态。
     */
    closeDialog (force = false) {
      if (this.submitLoading && !force) return
      this.dialogVisible = false
      this.invalidateRequest()
      this.resetState()
      this.$emit('update:visible', false)
    },
    /**
     * 响应 Element Dialog 的关闭回调，统一走可失效请求的关闭流程。
     * @returns {void} 关闭并清空当前弹窗状态。
     */
    handleDialogClose () {
      if (this.submitLoading) {
        this.dialogVisible = true
        return
      }
      if (this.dialogVisible || this.visible) {
        this.closeDialog()
      } else {
        this.invalidateRequest()
        this.resetState()
      }
    },
    /**
     * 使所有当前异步查询失效。
     * @returns {void} 递增请求序列号，不发起外部请求。
     */
    invalidateRequest () {
      this.requestSerial += 1
    },
    /**
     * 判断异步响应是否仍属于当前打开的弹窗。
     * @param {number} requestSerial 发起请求时记录的序列号。
     * @returns {boolean} 弹窗仍可见且序列号未被关闭或新查询取代时返回 true。
     */
    isCurrentRequest (requestSerial) {
      return this.dialogVisible && requestSerial === this.requestSerial
    },
    /**
     * 清除对象和权限选择但保留申请单有效期与候选列表。
     * @returns {void} 用于重新查询和对象切换时重置选择。
     */
    resetSelection () {
      this.selectedAuth = []
      this.tempSelectedAuth = []
      this.tempRemovedAuth = []
      this.authSearchKey = ''
    },
    /**
     * 清空弹窗所有请求结果和表单状态。
     * @returns {void} 关闭后恢复初始状态，不发起请求。
     */
    resetState () {
      this.loading = false
      this.submitLoading = false
      this.options = EMPTY_OPTIONS()
      this.selectedTargetId = null
      this.resetSelection()
    },
    /**
     * 从接口异常中提取可展示的错误信息。
     * @param {Object|string} error 请求异常对象或错误文本。
     * @param {string} fallback 无具体错误信息时的兜底文案。
     * @returns {string} 不包含敏感信息的用户提示。
     */
    getErrorMessage (error, fallback) {
      return (error && error.response && error.response.data && error.response.data.msg) || (error && error.message) || fallback
    },
    /**
     * 展示业务或网络错误。
     * @param {Object|string} error 错误文本或异常对象。
     * @returns {void} 调用页面消息提示，不改变用户当前选择。
     */
    showError (error) {
      const message = typeof error === 'string' ? error : this.getErrorMessage(error, '操作失败')
      if (this.$message && this.$message.error) {
        this.$message.error(message)
      } else if (this.$message) {
        this.$message({ message, type: 'error' })
      }
    },
    /**
     * 展示表单校验提示。
     * @param {string} message 用户需要补齐的选择说明。
     * @returns {void} 调用页面消息提示，不发起请求。
     */
    showWarning (message) {
      if (this.$message && this.$message.warning) {
        this.$message.warning(message)
      } else if (this.$message) {
        this.$message({ message, type: 'warning' })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.auth-dialog {
  ::v-deep .el-dialog {
    overflow: hidden;
    border-radius: 12px;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
  }

  ::v-deep .el-dialog__header {
    padding: 16px 20px;
    margin: 0;
    border-bottom: 1px solid #f0f0f0;
  }

  ::v-deep .el-dialog__title {
    font-size: 16px;
    font-weight: 500;
  }

  ::v-deep .el-dialog__body {
    box-sizing: border-box;
    max-height: calc(90vh - 150px);
    padding: 20px;
    overflow-y: auto;
  }
}

.manual-auth-dialog {
  color: #303133;
  padding: 0;
}

.selected-staff-info {
  padding: 12px 16px;
  margin-bottom: 16px;
  border-radius: 8px;
  background: #f9f9f9;

  .info-content {
    display: flex;
    align-items: flex-start;
    gap: 8px;

    > i {
      flex-shrink: 0;
      margin-top: 5px;
      color: #ed6d00;
    }
  }
}

.subject-content {
  flex: 1;
  min-width: 0;
}

.subject-heading {
  display: flex;
  align-items: center;
  gap: 12px;

  ::v-deep .el-select {
    width: 260px;
  }
}

.manual-auth-apply {
  display: block;
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}

.manual-auth-notice {
  margin: 8px 0 0 24px;
  color: #909399;
  font-size: 12px;
}

.manual-auth-label {
  font-weight: 500;
}

.manual-auth-loading,
.manual-auth-empty,
.auth-empty {
  padding: 24px 0;
  color: #909399;
  text-align: center;
}

.auth-container {
  display: flex;
  gap: 8px;
  height: 230px;
  margin: 16px 0 12px;
  min-width: 0;
}

.auth-panel {
  display: flex;
  flex: 1 1 0;
  flex-direction: column;
  height: 100%;
  min-width: 0;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #fff;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 0;
  padding: 10px 12px;
  border-bottom: 1px solid #ebeef5;

  ::v-deep .el-input {
    width: 130px;
  }
}

.panel-title {
  min-width: 0;
  color: #303133;
  font-size: 13px;
  font-weight: 500;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.auth-checkbox {
  display: flex;
  align-items: flex-start;
  box-sizing: border-box;
  width: 100%;
  margin: 4px 0;
  padding: 8px 12px;
  border-radius: 4px;

  &:hover {
    background-color: #f5f7fa;
  }

  ::v-deep .el-checkbox__label {
    display: block;
    flex: 1;
    min-width: 0;
    padding-left: 8px;
    white-space: normal;
  }
}

.auth-name {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.auth-disabled-reason {
  display: block;
  margin-top: 4px;
  color: #f56c6c;
  font-size: 12px;
  line-height: 18px;
  white-space: normal;
  overflow-wrap: anywhere;
}

.auth-operations {
  display: flex;
  align-items: center;
  flex: 0 0 40px;
  flex-direction: column;
  justify-content: center;
  gap: 12px;
  width: 40px;

  .el-button {
    width: 36px;
    height: 36px;
    margin: 0;
    padding: 0;

    &[disabled],
    &.is-disabled {
      color: #c0c4cc;
      border-color: #e4e7ed;
      background-color: #f5f7fa;
    }
  }
}

.assign-validity {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 14px;
  border: 1px solid #f5c6a5;
  border-radius: 8px;
  background: #fff9f2;
  flex-wrap: wrap;

  &__label {
    color: #5a2600;
    font-size: 13px;
    font-weight: 500;
    white-space: nowrap;
  }

  &__field {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 0;

    > span {
      color: #606266;
      font-size: 13px;
      white-space: nowrap;
    }

    ::v-deep .el-input {
      width: 155px;
    }
  }

  &__hint {
    flex: 1;
    min-width: 160px;
    color: #909399;
    font-size: 12px;
  }
}
</style>
