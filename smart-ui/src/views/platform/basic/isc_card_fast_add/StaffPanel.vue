<template>
  <div class="work-panel">
    <div class="panel-title">
      <span>当前录入</span>
      <el-tag 
        v-if="selectedStaff" 
        :type="selectedStaff.status === 0 ? 'danger' : 'success'" 
        size="mini">{{ staffStatusText(selectedStaff.status) }}</el-tag>
    </div>

    <div 
      v-if="selectedStaff" 
      class="staff-card">
      <div class="staff-main">
        <div class="staff-avatar">{{ staffAvatarText }}</div>
        <div>
          <div class="staff-name">{{ selectedStaff.name || '-' }} <span>{{ selectedStaff.badge || '-' }}</span></div>
          <div class="staff-meta">{{ selectedStaff.compName || '-' }} / {{ selectedStaff.depName || '-' }}</div>
        </div>
      </div>
      <el-row 
        :gutter="8" 
        class="staff-kv">
        <el-col :span="8">园区</el-col>
        <el-col :span="16">{{ selectedStaff.parkName || '-' }}</el-col>
        <el-col :span="8">岗位</el-col>
        <el-col :span="16">{{ selectedStaff.jobName || '-' }}</el-col>
        <el-col :span="8">入职日期</el-col>
        <el-col :span="16">{{ selectedStaff.createTime || '-' }}</el-col>
      </el-row>
      <div class="staff-actions">
        <el-button 
          type="text" 
          size="mini" 
          @click="$emit('open-detail', selectedStaff)">查看人员详情</el-button>
      </div>
    </div>
    <div 
      v-else 
      class="empty-state">先输入工号或姓名定位员工</div>

    <div 
      v-if="staffCandidates.length" 
      class="candidate-list">
      <div class="candidate-title">匹配人员</div>
      <el-table 
        :data="staffCandidates" 
        size="mini" 
        border 
        max-height="180">
        <el-table-column 
          prop="badge" 
          label="工号" 
          width="90"/>
        <el-table-column 
          prop="name" 
          label="姓名" 
          width="90"/>
        <el-table-column 
          prop="depName" 
          label="部门" 
          min-width="120" 
          show-overflow-tooltip/>
        <el-table-column 
          label="操作" 
          width="70" 
          fixed="right">
          <template slot-scope="scope">
            <el-button 
              type="text" 
              size="mini" 
              @click="$emit('select-staff', scope.row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="staff-card-list">
      <div class="candidate-title">已有ISC卡片</div>
      <el-table 
        v-loading="staffCardLoading" 
        :data="staffCards" 
        size="mini" 
        border 
        empty-text="暂无ISC实体卡">
        <el-table-column 
          prop="cardNo" 
          label="卡号" 
          min-width="120"/>
        <el-table-column 
          prop="dispatcherParkName" 
          label="ISC平台" 
          min-width="120"/>
        <el-table-column 
          label="同步状态" 
          width="92">
          <template slot-scope="scope">
            <el-tag 
              :type="cardSyncStatusType(scope.row.syncStatus)" 
              :title="scope.row.lastSyncRemark || ''" 
              size="mini">
              {{ cardSyncStatusText(scope.row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column 
          label="操作" 
          width="80" 
          fixed="right">
          <template slot-scope="scope">
            <el-button
              :loading="staffCardDeleting === scope.row.id"
              :disabled="!!staffCardDeleting && staffCardDeleting !== scope.row.id"
              type="text"
              size="mini"
              class="danger-text-button"
              @click="$emit('remove-card', scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
export default {
  name: 'IscCardFastAddStaffPanel',
  props: {
    selectedStaff: {
      type: Object,
      default: null
    },
    staffCandidates: {
      type: Array,
      required: true
    },
    staffCards: {
      type: Array,
      required: true
    },
    staffCardLoading: {
      type: Boolean,
      required: true
    },
    staffCardDeleting: {
      type: [Number, String],
      default: null
    },
    staffAvatarText: {
      type: String,
      required: true
    },
    staffStatusText: {
      type: Function,
      required: true
    },
    cardSyncStatusText: {
      type: Function,
      required: true
    },
    cardSyncStatusType: {
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

.staff-card {
  padding: 12px;
}

.staff-main {
  display: flex;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px dashed #eee;
}

.staff-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  margin-right: 10px;
  border-radius: 4px;
  background: #f0f2f5;
  color: #666;
  font-size: 16px;
  font-weight: 600;
}

.staff-name {
  color: #333;
  font-size: 15px;
  font-weight: 600;

  span {
    margin-left: 8px;
    color: #999;
    font-size: 12px;
    font-weight: 400;
  }
}

.staff-meta {
  margin-top: 4px;
  color: #666;
  font-size: 12px;
}

.staff-kv {
  padding-top: 10px;
  color: #666;
  font-size: 12px;
  line-height: 26px;

  .el-col:nth-child(2n + 1) {
    color: #999;
    text-align: right;
  }
}

.staff-actions {
  padding-top: 6px;
  text-align: right;
}

.candidate-list,
.staff-card-list {
  padding: 0 12px 12px;
}

.staff-card-list {
  .danger-text-button {
    color: #e7292e;

    &:hover,
    &:focus {
      color: #c11d22;
    }
  }
}

.candidate-title {
  padding: 8px 0;
  color: #666;
  font-size: 12px;
}
</style>
