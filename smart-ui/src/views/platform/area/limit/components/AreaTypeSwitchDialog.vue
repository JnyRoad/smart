<template>
  <el-dialog
    :visible.sync="innerVisible"
    title="变更通关权限性质"
    width="480px"
    @closed="reset"
  >
    <div class="area-type-switch">
      <p class="area-type-switch__row">
        <span>权限组</span>
        <strong>{{ authority.authorityName }}</strong>
      </p>
      <p class="area-type-switch__row">
        <span>性质变更</span>
        <strong>{{ areaTypeLabel(authority.areaType) }} → {{ areaTypeLabel(targetAreaType) }}</strong>
      </p>
      <div
        v-if="conflicts.length"
        class="area-type-switch__conflicts">
        <p class="area-type-switch__conflicts-title">以下设备已被其他权限组占用，无法切换：</p>
        <table>
          <tr
            v-for="item in conflicts"
            :key="item.deviceId">
            <td>{{ item.deviceName }}</td>
            <td>{{ item.conflictAuthorityName }}</td>
          </tr>
        </table>
        <p class="area-type-switch__conflicts-tip">
          请先到对应权限组的编辑页移除以上设备，再回来切换性质。
        </p>
      </div>
    </div>
    <div slot="footer">
      <el-button @click="innerVisible = false">取消</el-button>
      <el-button
        :loading="submitting"
        :disabled="conflicts.length > 0"
        type="primary"
        class="area-type-switch__confirm"
        @click="submit"
      >确定切换</el-button>
    </div>
  </el-dialog>
</template>

<script>
// 变更通关权限性质的独立入口：只改 area_type 一个字段，不加载、不展示设备树，
// 和「编辑」页的设备清单编辑物理隔离，避免重新踩“切换性质时设备树被换掉”的坑。
import { switchAreaType } from '@/api/platform/area/limit'

export default {
  name: 'AreaTypeSwitchDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    authority: {
      type: Object,
      default: () => ({})
    }
  },
  data () {
    return {
      submitting: false,
      conflicts: []
    }
  },
  computed: {
    innerVisible: {
      get () {
        return this.visible
      },
      set (val) {
        this.$emit('update:visible', val)
      }
    },
    // 性质只有 0/1 两种取值，切换必然是翻转到另一种，不需要让用户再挑一遍
    targetAreaType () {
      return this.authority.areaType === 1 ? 0 : 1
    }
  },
  methods: {
    areaTypeLabel (val) {
      return val === 1 ? '保密区域' : '公共区域'
    },
    submit () {
      this.submitting = true
      switchAreaType({ id: this.authority.id, areaType: this.targetAreaType }).then(response => {
        this.submitting = false
        const result = response.data.data
        if (result.success) {
          this.$message.success('权限性质已切换')
          this.$emit('success')
          this.innerVisible = false
        } else {
          this.conflicts = result.conflicts || []
        }
      }).catch(() => {
        this.submitting = false
      })
    },
    reset () {
      this.conflicts = []
      this.submitting = false
    }
  }
}
</script>

<style lang="scss" scoped>
.area-type-switch {
  &__row {
    display: flex;
    justify-content: space-between;
    font-size: 14px;
    margin: 0 0 12px;
  }
  &__conflicts {
    border: 1px solid #f5c6a5;
    border-radius: 4px;
    padding: 8px 12px;
    margin-top: 8px;
    table {
      width: 100%;
      font-size: 13px;
    }
  }
  &__conflicts-title {
    font-size: 13px;
    color: #e6a23c;
    margin: 0 0 6px;
  }
  &__conflicts-tip {
    font-size: 12px;
    color: #909399;
    margin: 6px 0 0;
  }
}
</style>
