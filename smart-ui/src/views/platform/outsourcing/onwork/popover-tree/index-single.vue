<template>
  <div class="popover-tree">
    <el-select
      ref="elSelect"
      :value="selectTags.length ? selectTags[0] : ''"
      v-popover:popover
      popper-class="select-option"
      placeholder="请选择"
      @visible-change="visibleChange"
      :popper-append-to-body="false"
    ></el-select>
    <el-popover ref="popover" trigger="focus" :width="290" popper-class="popover-tree-popper" v-model="popover" placement="bottom-start">
      <div class="popover-tree-inner" v-loading="loading">
        <div class="popover-tree-search">
          <el-input placeholder="请输入工号" prefix-icon="ali-icon-search" v-model="searchKey" @input="changeInput"></el-input>
        </div>
        <div class="popover-tree-body" v-if="searchData.length">
          <div class="popover-tree-block">
            <el-scrollbar tag="div" wrap-class="popover-tree-block__wrap" view-class="popover-tree-block__list" style="height:100%">
              <template v-for="(subItem, subIndex) in searchData">
                <div class="popover-tree-item children-level" :key="subIndex" :class="{ disable: subItem.disable, active: subItem.active }" @click="checkItem(subItem)">
                  <label>{{ subItem.label }}</label>
                  <i></i>
                </div>
              </template>
            </el-scrollbar>
          </div>
        </div>
        <div class="result-nodata">
          —— 无相关人员 ——
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script>
import { getSearchStaff } from '@/api/platform/basic/personnel_manage'

/**
 * 递归搜索item
 */
const recursionItem = function (val, data) {
  let item = null
  for (let i = 0; i < data.length; i++) {
    const dataItem = data[i]
    if (val === dataItem.id) {
      item = dataItem
      break
    }
    if (dataItem.children && dataItem.children.length) {
      item = recursionItem(val, dataItem.children)
      if (item) {
        break
      }
    }
  }
  return item
}
export default {
  data() {
    return {
      searchKey: '',
      pushTreeDatas: [],
      selectValues: [],
      selectTags: [],
      selectItems: [],
      popover: false,
      treeData: [],
      searchData: [],
      loading: false
    }
  },
  props: {
    value: Array
  },
  watch: {
    value(val, old) {
      if (val === this.selectValues) {
        this.selectValues = val
        this.initWithData(this.treeData)
      }
    },
    treeData() {
      if (this.selectValues.length !== this.selectTags.length) {
        this.initWithData(this.treeData)
      }
    },
    searchData(val) {
      if (!val.length) {
        this.initWithData(this.treeData)
      }
    }
  },
  methods: {

    /**
     * 初始化数据
     */
    initWithData(data) {
      if (this.treeData) {
        const selectTags = []
        const selectItems = []
        this.selectTags = this.selectValues.forEach(val => {
          const item = recursionItem(val, data)
          item.active = true
          selectTags.push(item.label)
          selectItems.push(item)
        })
        this.selectTags = selectTags
        this.selectItems = selectItems
      }
    },
    emitValues() {
      this.$emit('input', this.selectValues)
      this.$nextTick(() => {
        this.$refs.elSelect.blur()
      })
    },
    /**
     * 刷新选择项
     */
    refreshSelectValues(item, isRemoveTag = false) {
      if (this.selectValues.length) {
        this.selectValues.splice(0, 1)
        this.selectTags.splice(0, 1)
        this.selectItems[0].active = false
        this.selectItems.splice(0, 1)
      }

      this.selectValues.push(item.id)
      this.selectTags.push(item.label)
      this.selectItems.push(item)

      this.emitValues()
    },
    /**
     * 选中
     */
    checkItem(item) {
      if (this.searchData.length) {
        this.searchData.forEach(item => {
          item.active = false
        })
      }
      if (item.disable) {
        return
      }
      if (item.active) {
        item.active = false
      } else {
        item.active = true
      }
      this.refreshSelectValues(item, true)
    },
    /**
     * 推入
     */
    pushChildren(subItem, children, parentList, index) {
      parentList.forEach(item => {
        item.active = false
      })
      subItem.active = true
      this.pushTreeDatas.splice(index + 1)
      if (!children.length) {
        this.pushTreeDatas.push([{
          label: '无相关人员',
          type: 1,
          disable: true,
          id: ''
        }])
      } else {
        children.forEach(item => {
          if (item.type === 0) {
            item.active = false
          }
        })
        this.pushTreeDatas.push(children)
      }

      this.$nextTick(() => {
        this.$nextTick(() => {
          this.$refs.popover && this.$refs.popover.updatePopper()
        })
      })
    },
    /**
     * 搜索
     */
    async changeInput(value) {
      if (value) {
        const res = await getSearchStaff({
          badge: value
        })
        this.searchData = res
        this.$nextTick(() => {
          this.$nextTick(() => {
            this.$refs.popover && this.$refs.popover.updatePopper()
          })
        })
      } else {
        this.searchData = []
      }
    },
    async getTreePeopleAll() {
      if (this.loading) {
        return
      }
      try {
        this.loading = true
        const res = await getSearchStaff()
        this.treeData = res
        // console.log(this.treeData)
        this.pushTreeDatas = [this.treeData]
      } catch (error) {
        // 忽略异常：树数据解析失败时保持现状
      }
      this.loading = false
    },
    /**
     * 下拉框出现/隐藏时触发
     */
    async visibleChange(val) {
      if (val && !this.treeData.length) {
        this.getTreePeopleAll()
      }
    }
  },
  mounted() {
    // this.getTreePeopleAll()
  }
}
</script>

<style lang="scss">
.popover-tree-popper {
  padding: 0;
  .el-popper {
    padding: 0;
  }
  .el-input__inner {
    background: #fff;
    border: 0;
    border-bottom: 1px solid #f1f1f1;
    padding-left: 10px;
  }
}
</style>

<style lang="scss" scoped>
.popover-tree ::v-deep {
  .select-option {
    display: none !important;
  }
}
.popover-tree-body {
  display: flex;
  .popover-tree-block {
    min-width: 200px;
    overflow: hidden;
    height: 36px * 5;
    & + .popover-tree-block {
      border-left: 1px solid #f2f2f2;
    }
  }
}
.popover-tree-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #333;
  max-width: 200px;
  padding: 0 10px;
  line-height: 36px;
  cursor: pointer;
  * {
    pointer-events: none;
  }
  &:hover {
    background: #f6f9fe;
  }
  &.disable {
    color: #999;
    font-size: 12px;
    cursor: not-allowed;
  }
  &.active {
    &.parent-level {
      background: #f6f9fe;
    }
    &.children-level {
      i {
        opacity: 1;
      }
    }
  }
  i {
    display: block;
    width: 14px;
    height: 14px;
  }
  &.parent-level {
    i {
      background: url(./image/1.png) no-repeat center;
    }
  }
  &.children-level {
    i {
      opacity: 0;
      background: url(./image/2.png) no-repeat center;
    }
  }
}

.result-nodata {
  color: #cccccc;
  text-align: center;
  padding: 40px;
}
</style>
