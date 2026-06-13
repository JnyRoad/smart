<template>
  <el-dialog
    ref="dialog"
    :visible.sync="currVisible"
    width="700px"
    :show-close="false"
    @open="open"
    @close="close"
    :append-to-body="true"
    :custom-class="'my-dialog-'"
  >
    <div class="cks" style="padding-bottom: 30px;">
      <div style="margin: 10px 0">
        <el-input placeholder="输入关键字进行过滤" clearable v-model="filterText" size="mini">
          <el-button type="info" slot="append" icon="el-icon-search"></el-button>
        </el-input>
      </div>
      <el-scrollbar class="my-lit-scrollbar" :native="false" v-loading="loading">
        <el-tree
          :data="treeData"
          ref="depttree1"
          node-key="value"
          :load="loadNode"
          lazy
          show-checkbox
          :filter-node-method="filterNode"
          :highlight-current="true"
          :check-strictly="false"
          :props="defaultProps"
        ></el-tree>
      </el-scrollbar>
    </div>
    <div slot="footer">
      <el-button type="primary" plain @click="cancel">取 消</el-button>
      <el-button type="primary" @click="formSumit()" :loading="btnLoading">保 存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { xcProjectApi } from '../_service'
export default {
  mixins: [tce.mixins.executeOnce],
  data() {
    return {
      loading: false,
      btnLoading: false,
      currVisible: false,
      personList: [],
      treeData: [],
      filterText: '',
      defaultProps: {
        children: "children",
        label: "label"
      }
    }
  },
  props: {
    visible: Boolean,
    title: String,
    itemObj: Object,
    parkId: [String, Number]
  },
  created() {},
  watch: {
    visible() {
      this.currVisible = this.visible
    },
    currVisible() {
      if (this.currVisible === false) {
        this.$emit('update:visible', false)
      } else {
        this.getPersonList()
      }
    },
    filterText(val) {
      this.$refs.depttree1.filter(val);
    }
  },
  methods: {
    async loadNode(node, resolve) {
      if(node.level < 3){
        let obj = { arr: [] }
        this.getChild(node.data.value, this.personList, obj)
        return resolve(obj.arr)
      }
      if (node.level === 3) {
        const res2 = await xcProjectApi.getPersonTreeByDeptId(node.data.value)
        if(res2.data.data && res2.data.data.length>0){
          let arr = []
          res2.data.data.forEach(el=>{
            arr.push({
              value: el.staffId,
              label: el.staffInfo,
              isStaff: true
            })
          })
          return resolve(arr)
        }else{
          return resolve([])
        }
      }else{
        return resolve([])
      }
    },
    /**
     * 提交
     */
    async formSumit() {
      let nodes = this.$refs.depttree1.getCheckedNodes()
      let staffIds = []
      nodes.forEach(el=>{
        if(el.isStaff){
          staffIds.push(el.value)
        }
      })
      if(staffIds.length===0){
        this.$message.error('请选择员工信息');
        return
      }
      this.$emit('refresh', staffIds)
      this.close()
    },
    /**
     * 获取列表 getPersonTreeByDeptId
     */
    async getPersonList() {
      this.loading = true
      const res = await xcProjectApi.getDeptTree()
      this.personList = res.data.data
      this.treeData = res.data.data
      this.loading = false
    },
    getChild(fatherId, arr, obj){
      let fond = false
      arr.forEach(el=>{
        if(el.value === fatherId){
          fond = true
          if(!el.children) return
          obj.arr = el.children
        }else{
          if(!fond){
            if(el.children && el.children.length>0){
              this.getChild(fatherId, el.children, obj)
            }
          }
        }
      })
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    cancel() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    },
    open() {
      this.currVisible = true
    },
    close() {
      this.$refs.form && this.$refs.form.resetFields()
      this.currVisible = false
    }
  },
  mounted() {}
}
</script>

<style lang="scss" scoped>
  .form{
    margin-bottom: 40px;
  }
  .cks ::v-deep {
    .my-lit-scrollbar{
      height: 400px;
    }
    .el-scrollbar__wrap{
      overflow-x: hidden;
    }
    .el-checkbox+.el-checkbox{
      margin-left: 0;
    }
    .el-checkbox{
      margin-right: 20px;
    }
  }
</style>
