<template>
  <div class="device-tree-picker">
    <div class="device-tree-picker__col device-tree-picker__col--tree">
      <el-input
        v-model="filterText"
        placeholder="输入设备名称、楼栋或楼层搜索"
        size="small"
        clearable
        class="device-tree-picker__search"
      >
        <i slot="prefix" class="el-icon-search"></i>
      </el-input>
      <el-tree
        :key="treeRenderKey"
        ref="tree"
        class="device-tree-picker__tree"
        :data="treeData"
        node-key="id"
        show-checkbox
        check-strictly
        :props="elProps"
        :filter-node-method="filterNode"
        :default-expanded-keys="expandedKeys"
        :default-checked-keys="value"
        @check="handleCheck"
      ></el-tree>
    </div>
    <div class="device-tree-picker__col device-tree-picker__col--selected">
      <div class="device-tree-picker__selected-head">
        <span>已选设备（{{ selectedList.length }}）</span>
        <el-button type="text" size="mini" @click="clearAll">清空</el-button>
      </div>
      <ul class="device-tree-picker__selected-list">
        <li v-for="item in selectedList" :key="item.id">
          <span>{{ item.label }}</span>
          <i class="el-icon-close" @click="removeSelected(item.id)"></i>
        </li>
        <li v-if="selectedList.length === 0" class="device-tree-picker__empty">
          还没有选中设备，从左侧勾选或搜索添加
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
// 通关权限设备绑定选择器：左侧可搜索设备树，右侧平铺展示已选设备。
// 替换原来 add.vue/edit.vue 里 default-expand-all 的裸 el-tree，
// 解决“页面被撑得很长”和“找不到已选设备”两个体验问题。
export default {
  name: 'DeviceTreePicker',
  props: {
    treeData: {
      type: Array,
      default: () => []
    },
    value: {
      type: Array,
      default: () => []
    }
  },
  data () {
    return {
      filterText: '',
      leafNameById: {},
      expandedKeys: [],
      treeRenderKey: 0,
      elProps: {
        children: 'children',
        label: 'label'
      }
    }
  },
  watch: {
    treeData: {
      immediate: true,
      handler () {
        this.leafNameById = this.buildLeafNameMap(this.treeData)
        const checkedIdSet = new Set(this.value || [])
        const expandedKeys = []
        this.collectExpandedKeys(this.treeData, checkedIdSet, expandedKeys)
        this.expandedKeys = expandedKeys
        // el-tree 的 default-expanded-keys/default-checked-keys 只在节点创建时生效一次，
        // 树数据变化后必须强制重建组件实例才能让新的默认值重新应用。
        this.treeRenderKey += 1
      }
    },
    filterText (val) {
      this.$nextTick(() => {
        if (this.$refs.tree) {
          this.$refs.tree.filter(val)
        }
      })
    }
  },
  computed: {
    selectedList () {
      return (this.value || []).map(id => ({
        id: id,
        label: this.leafNameById[id] || id
      }))
    }
  },
  methods: {
    filterNode (value, data) {
      if (!value) {
        return true
      }
      return data.label.indexOf(value) !== -1
    },
    // 收集树里所有叶子节点（真正的设备，没有 children 的节点）的 id -> label 映射
    buildLeafNameMap (nodes, out) {
      out = out || {}
      ;(nodes || []).forEach(node => {
        const isLeaf = !node.children || node.children.length === 0
        if (isLeaf) {
          out[node.id] = node.label
        } else {
          this.buildLeafNameMap(node.children, out)
        }
      })
      return out
    },
    // 递归收集“包含已选设备”的非叶子节点 id，用于 default-expanded-keys；
    // 返回值表示这一层节点里是否存在命中的叶子设备（让上一层也跟着展开）。
    collectExpandedKeys (nodes, checkedIdSet, expandedKeys) {
      let anyChecked = false
      ;(nodes || []).forEach(node => {
        const isLeaf = !node.children || node.children.length === 0
        if (isLeaf) {
          if (checkedIdSet.has(node.id)) {
            anyChecked = true
          }
          return
        }
        const childHasChecked = this.collectExpandedKeys(node.children, checkedIdSet, expandedKeys)
        if (childHasChecked) {
          expandedKeys.push(node.id)
          anyChecked = true
        }
      })
      return anyChecked
    },
    handleCheck (data, checkedInfo) {
      this.$emit('input', checkedInfo.checkedKeys)
    },
    removeSelected (id) {
      const nextIds = (this.value || []).filter(existingId => existingId !== id)
      this.$emit('input', nextIds)
      this.$nextTick(() => {
        if (this.$refs.tree) {
          this.$refs.tree.setCheckedKeys(nextIds)
        }
      })
    },
    clearAll () {
      this.$emit('input', [])
      this.$nextTick(() => {
        if (this.$refs.tree) {
          this.$refs.tree.setCheckedKeys([])
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.device-tree-picker {
  display: flex;
  gap: 16px;
  &__col {
    flex: 1;
    min-width: 0;
    border: 1px solid #e5e7eb;
    border-radius: 4px;
  }
  &__search {
    padding: 8px;
  }
  &__tree {
    max-height: 320px;
    overflow-y: auto;
    padding: 0 8px 8px;
  }
  &__selected-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    border-bottom: 1px solid #e5e7eb;
    font-size: 13px;
    color: #606266;
  }
  &__selected-list {
    max-height: 320px;
    overflow-y: auto;
    margin: 0;
    padding: 6px 8px;
    list-style: none;
    li {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 5px 8px;
      font-size: 13px;
      .el-icon-close {
        cursor: pointer;
        color: #909399;
      }
    }
  }
  &__empty {
    color: #909399;
    font-size: 13px;
  }
}
</style>
