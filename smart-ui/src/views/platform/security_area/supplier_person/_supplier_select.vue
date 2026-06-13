<!--
- @name 供应商管理
-->
<template>
  <el-scrollbar class="my-lit-scrollbar" :native="false">
    <el-tree class="my-menu-tree" :data="treeData" show-checkbox highlight-current :props="defaultProps" node-key="id" :expand-on-click-node="false" ref="tree" default-expand-all>
    </el-tree>
  </el-scrollbar>
</template>

<script>
import { supplierList } from './_service'
import { mapGetters } from 'vuex'
export default {
  data() {
    return {
      treeData: [],
      defaultProps: {
        children: 'children',
        label: 'companyName'
      }
    }
  },
  mounted: function () {
    this.getSupplierList()
  },
  computed: {
    ...mapGetters(['permissions'])
  },
  watch: {},
  methods: {
    getCheckedKeys() {
      let tmpA = this.$refs.tree.getCheckedNodes().filter((item) => {
        return item.id !== -1 && item.id !== -2
      })
      let tmpB = []
      tmpA.forEach((el) => {
        tmpB.push(el.id)
      })
      return tmpB
    },
    async getSupplierList() {
      const res = await supplierList()
      this.supplierList = res.data.data
      let arr1 = []
      let arr2 = []
      this.supplierList.forEach((el) => {
        if (arr1.indexOf(el.parkId) == -1) {
          let obj = {
            id: -1,
            parkId: el.parkId,
            companyName: el.parkName,
            companyCode: '',
            supplierType: '',
            children: el.children
          }
          arr1.push(el.parkId)
          arr2.push(obj)
        }
      })

      let arr3 = []
      arr2.forEach((el) => {
        let o2 = {
          a: {
            id: -2,
            parkId: el.parkId,
            companyName: 'A类',
            companyCode: '',
            supplierType: 1,
            children: []
          },
          na: {
            id: -2,
            parkId: el.parkId,
            companyName: '非A类',
            companyCode: '',
            supplierType: 2,
            children: []
          }
        }
        let tmpA = []
        let tmpNa = []
        if (el.children !== null) {
          tmpA = el.children.filter((item) => {
            return item.parkId === el.parkId && item.supplierType === 1
          })
          tmpNa = el.children.filter((item) => {
            return item.parkId === el.parkId && item.supplierType === 2
          })
        }
        let tmpA2 = []
        tmpA.forEach((el) => {
          el.companyName = '[' + el.companyCode + ']' + el.companyName
          tmpA2.push(el)
        })
        let tmpNa2 = []
        tmpNa.forEach((el) => {
          el.companyName = '[' + el.companyCode + ']' + el.companyName
          tmpNa2.push(el)
        })
        o2.a.children = tmpA2
        o2.na.children = tmpNa2
        arr3.push(o2)
      })
      arr2.forEach((el, index) => {
        el.children = [arr3[index].a, arr3[index].na]
      })
      this.treeData = arr2
    },
    clear() {
      this.$refs.tree.setCheckedKeys([])
    }
  }
}
</script>

<style lang="scss" scoped>
.personnel-dialog-from-item {
  padding: 0 20px;
  ::v-deep {
    .el-form-item {
      margin-bottom: 20px;
    }
  }
}
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
  span:nth-of-type(1) {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    word-break: break-all;
    width: 150px;
  }
  span:nth-of-type(2) {
    display: none;
  }
  &:hover {
    span:nth-of-type(2) {
      display: block;
    }
  }
}
</style>
