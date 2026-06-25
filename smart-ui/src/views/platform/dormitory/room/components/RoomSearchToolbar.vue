<template>
  <div>
    <div class="top-menu">
      房间
      <div class="top-right">
        <el-button
          type="primary"
          icon="el-icon-search"
          @click="$emit('search')">搜索</el-button>
        <el-button
          type="primary"
          icon="el-icon-delete"
          plain
          @click="$emit('reset')">清空</el-button>
        <template v-if="hasData">
          <el-button
            :loading="exportLoading"
            type="primary"
            icon="icon-yutong-download"
            @click="$emit('export')">导出表格</el-button>
          <el-button
            type="primary"
            icon="el-icon-edit"
            @click="$emit('batch-edit')">批量设置房间类型</el-button>
          <el-button
            type="primary"
            icon="el-icon-edit"
            @click="$emit('sd-batch-edit')">批量设置房间水电模板</el-button>
        </template>
      </div>
    </div>
    <el-form
      ref="searchForm"
      :inline="true"
      :model="searchForm"
      class="topForm"
      size="mini">
      <el-form-item
        label="是否参与分配"
        prop="isDormitoryRoom">
        <el-select
          :value="searchForm.isDormitoryRoom"
          clearable
          placeholder="是否参与分配"
          @input="updateSearchField('isDormitoryRoom', $event)">
          <el-option
            label="是"
            value="0"/>
          <el-option
            label="否"
            value="1"/>
        </el-select>
      </el-form-item>
      <el-form-item
        label="是否参与计算"
        prop="isCount">
        <el-select
          :value="searchForm.isCount"
          clearable
          placeholder="是否参与计算"
          @input="updateSearchField('isCount', $event)">
          <el-option
            label="是"
            value="1"/>
          <el-option
            label="否"
            value="0"/>
        </el-select>
      </el-form-item>
      <el-form-item
        label="宿舍分类"
        prop="roomType">
        <el-select
          :value="searchForm.roomType"
          clearable
          placeholder="请选择宿舍分类"
          @input="updateSearchField('roomType', $event)">
          <el-option
            v-for="item in allDormTypeList"
            :key="item.id"
            :label="item.typeName"
            :value="item.id"/>
        </el-select>
      </el-form-item>
      <el-form-item
        label="房间属性"
        prop="roomSex">
        <el-select
          :value="searchForm.roomSex"
          clearable
          placeholder="房间属性"
          @input="updateSearchField('roomSex', $event)">
          <el-option
            label="男"
            value="0"/>
          <el-option
            label="女"
            value="1"/>
          <el-option
            label="夫妻/家属"
            value="2"/>
          <el-option
            label="其他"
            value="3"/>
        </el-select>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'RoomSearchToolbar',
  props: {
    searchForm: {
      type: Object,
      required: true
    },
    allDormTypeList: {
      type: Array,
      required: true
    },
    hasData: {
      type: Boolean,
      required: true
    },
    exportLoading: {
      type: Boolean,
      required: true
    }
  },
  methods: {
    updateSearchField(field, value) {
      this.$emit('update-search-field', { field, value })
    },
    resetFields() {
      if (this.$refs.searchForm) {
        this.$refs.searchForm.resetFields()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.topForm ::v-deep {
  .el-form-item__label {
    width: 120px;
  }
}
</style>
