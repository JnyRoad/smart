<template>
  <div class="box-outer box-left">
    <el-scrollbar
      :native="false"
      class="my-lit-scrollbar">
      <div class="cont-left">
        <span class="tiplbl">选择楼栋及楼层</span>
        <div class="lft-block">
          <div style="margin: 10px 0">
            <el-input
              v-model="filterText"
              placeholder="输入关键字进行过滤"
              clearable
              size="mini">
              <el-button
                slot="append"
                type="info"
                icon="el-icon-search"/>
            </el-input>
          </div>
          <el-tree
            ref="roomtree"
            :data="treeData"
            :props="defaultProps"
            :filter-node-method="filterNode"
            class="my-menu-tree"
            highlight-current
            node-key="id"
            accordion
            @node-click="emitNodeClick">
            <span
              slot-scope="{ node, data }"
              class="custom-tree-node">
              <span class="tree-label">{{ node.label }}{{ node.level == 3 ? 'F' : '' }} </span>
              <span
                v-if="node.parent.parent"
                class="tree-btn">
                <i
                  class="btni"
                  @click.stop="emitNodeAction(data, 'EDI', node)">编辑</i>
                <i
                  class="btni"
                  @click.stop="emitNodeAction(data, 'DEL', node)">删除</i>
                <template v-if="node.level == 1">
                  <i
                    class="btni"
                    @click.stop="emitNodeAction(data, 'APP', node)">新增楼栋</i>
                </template>
                <template v-if="node.level == 2">
                  <i
                    class="btni"
                    @click.stop="emitNodeAction(data, 'APP', node)">新增楼层</i>
                </template>
              </span>
              <span
                v-else
                class="tree-btn">
                <i
                  class="btni"
                  @click.stop="emitNodeAction(data, 'APP', node)">新增楼栋</i>
              </span>
            </span>
          </el-tree>
        </div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script>
import { shouldShowRoomTreeNode } from '../room-rules'

export default {
  name: 'RoomTreePanel',
  props: {
    treeData: {
      type: Array,
      required: true
    },
    defaultProps: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      filterText: ''
    }
  },
  watch: {
    filterText(val) {
      if (this.$refs.roomtree) {
        this.$refs.roomtree.filter(val)
      }
    }
  },
  methods: {
    filterNode(value, data) {
      return shouldShowRoomTreeNode(value, data)
    },
    emitNodeClick(data, node, treeNode) {
      this.$emit('node-click', data, node, treeNode)
    },
    emitNodeAction(data, action, node) {
      this.$emit('node-action', data, action, node)
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  i[class^='el-icon-'] {
    margin: 0 2px;
  }
  .btni {
    font-size: 12px;
    font-style: normal;
    margin-left: 8px;
  }
  span:nth-of-type(1) {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    word-break: break-all;
    width: 145px;
  }
  span:nth-of-type(2) {
    color: #ee6a00;
  }
}
</style>
