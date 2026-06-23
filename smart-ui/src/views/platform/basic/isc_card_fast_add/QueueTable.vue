<template>
  <div class="work-panel queue-panel">
    <div class="panel-title">
      <span>待提交队列</span>
      <span class="panel-count">共{{ rows.length }}条，可提交{{ readyCount }}条，异常{{ invalidCount }}条</span>
    </div>
    <el-table
      :data="rows"
      :row-class-name="rowClassName"
      size="mini"
      border
      max-height="360"
      empty-text="暂无待提交卡片"
    >
      <el-table-column
        label="状态"
        width="92"
      >
        <template slot-scope="scope">
          <el-tag
            :type="formatStatusType(scope.row.status)"
            size="mini"
          >{{ formatStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="badge"
        label="工号"
        width="100"
      />
      <el-table-column
        prop="name"
        label="姓名"
        width="90"
      />
      <el-table-column
        prop="parkName"
        label="园区"
        width="110"
        show-overflow-tooltip
      />
      <el-table-column
        prop="dispatcherParkName"
        label="ISC平台"
        width="120"
        show-overflow-tooltip
      />
      <el-table-column
        label="卡号 / 结果"
        min-width="210"
      >
        <template slot-scope="scope">
          <span class="card-no">{{ scope.row.cardNo || '-' }}</span>
          <div
            v-if="scope.row.message"
            class="row-message"
          >{{ scope.row.message }}</div>
        </template>
      </el-table-column>
      <el-table-column
        label="操作"
        width="92"
        fixed="right"
      >
        <template slot-scope="scope">
          <el-button
            :disabled="submitting || scope.row.status === 'saving'"
            type="text"
            size="mini"
            @click="$emit('remove-row', scope.$index)"
          >移除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="queue-footer">
      <div class="queue-note">保存成功后会自动创建ISC新增卡片同步任务；失败行会保留在队列中。</div>
      <div>
        <el-button
          :disabled="submitting"
          size="mini"
          plain
          @click="$emit('remove-finished')"
        >清除成功行</el-button>
        <el-button
          :disabled="submitting"
          size="mini"
          plain
          @click="$emit('clear')"
        >清空队列</el-button>
        <el-button
          :loading="submitting"
          :disabled="!canSubmit"
          type="primary"
          size="mini"
          @click="$emit('submit')"
        >提交队列</el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'IscCardFastAddQueueTable',
  props: {
    rows: {
      type: Array,
      default() {
        return []
      }
    },
    readyCount: {
      type: Number,
      default: 0
    },
    invalidCount: {
      type: Number,
      default: 0
    },
    submitting: {
      type: Boolean,
      default: false
    },
    canSubmit: {
      type: Boolean,
      default: false
    },
    formatStatusText: {
      type: Function,
      required: true
    },
    formatStatusType: {
      type: Function,
      required: true
    },
    rowClassName: {
      type: Function,
      required: true
    }
  }
}
</script>

<style lang="scss" scoped>
.work-panel {
  border: 1px solid #e8e9ed;
  border-radius: 4px;
  background: #fff;
}

.queue-panel {
  min-width: 0;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 40px;
  padding: 0 12px;
  border-bottom: 1px solid #eee;
  background: #fafafa;
  color: #333;
  font-size: 13px;
  font-weight: 600;
}

.panel-count {
  color: #999;
  font-size: 12px;
  font-weight: 400;
}

.card-no {
  font-family: Consolas, Menlo, monospace;
}

.row-message {
  margin-top: 3px;
  color: #e7292e;
  font-size: 12px;
}

.queue-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-top: 1px solid #eee;
  background: #fafafa;
}

.queue-note {
  color: #999;
  font-size: 12px;
}

::v-deep .queue-row-error td {
  background: #fff8f8 !important;
}

::v-deep .queue-row-success td {
  background: #f4fbf8 !important;
}
</style>
