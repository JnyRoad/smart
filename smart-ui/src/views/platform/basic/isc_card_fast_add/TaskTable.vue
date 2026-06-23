<template>
  <div class="task-block">
    <div class="panel-title">
      <span>最近录卡结果 / 同步任务追踪</span>
      <div>
        <el-button
          size="mini"
          icon="el-icon-refresh"
          @click="$emit('refresh')"
        >刷新</el-button>
      </div>
    </div>
    <el-table
      v-loading="loading"
      :data="rows"
      size="mini"
      border
      empty-text="暂无同步任务"
    >
      <el-table-column
        prop="parkName"
        label="园区"
        width="110"
      />
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
        prop="cardNo"
        label="卡 ID"
        width="140"
      />
      <el-table-column
        label="任务动作"
        width="100"
      >
        <template slot-scope="scope">{{ formatAction(scope.row) }}</template>
      </el-table-column>
      <el-table-column
        prop="remark"
        label="执行结果"
        min-width="220"
        show-overflow-tooltip
      />
      <el-table-column
        prop="createTime"
        label="创建时间"
        width="165"
      />
      <el-table-column
        prop="optUser"
        label="操作人"
        width="100"
      />
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'IscCardFastAddTaskTable',
  props: {
    loading: {
      type: Boolean,
      default: false
    },
    rows: {
      type: Array,
      default() {
        return []
      }
    },
    formatAction: {
      type: Function,
      required: true
    }
  }
}
</script>

<style lang="scss" scoped>
.task-block {
  margin: 0 20px 20px;
  border: 1px solid #e8e9ed;
  border-radius: 4px;
  background: #fff;
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
</style>
