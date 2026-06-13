<!--设备标签 -->
<template>
  <div class="my-basic-container note_record">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="mixinSearchSubmit(mixinSearchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="mixinResetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="handleAdd">添加</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="mixinSearchForm" class="topForm" size="mini">
          <el-form-item label="标签名称" prop="tagName">
            <el-input v-model="mixinSearchForm.tagName" placeholder="标签名称" clearable></el-input>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="mixinPage"
          :data="tableData"
          :table-loading="mixinTableLoading"
          @size-change="mixinSizeChange"
          @current-change="mixinCurrentChange"
          :option="mixinListOptionConf"
        >
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="handleEdit(scope.row,scope.$index)" >编辑</el-button>
            <el-button type="text" icon="el-icon-delete" @click="handleDel(scope.row, scope.$index)">删除</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <AddDialog :title="dlgTitle" :itemObj="curObj" ref="addDialog" @refresh="refresh"/>

  </div>
</template>

<script>
import { xcDeviceTagApi } from "./_service"
import AddDialog from './components/AddDialog'

export default {
  mixins: [tce.mixins.list],
  components: {
    AddDialog
  },
  data() {
    return {
      tableData: [],
      listOption: listOption(),
      dlgTitle: '',
      curObj: {}
    };
  },
  created() {
    this.refresh()
  },
  mounted: function() {},
  methods: {
    handleAdd(){
      this.curObj = null
      this.dlgTitle = '添加'
      this.$refs.addDialog.open()
    },
    handleEdit(row){
      this.curObj = row
      this.dlgTitle = '编辑'
      this.$refs.addDialog.open()
    },
    refresh(){
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    async getList(page, params) {
      this.mixinTableLoading = true
      const res = await  xcDeviceTagApi.getList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      )
      this.tableData = res.data.data.records
      this.mixinPage.total = res.data.data.total
      this.mixinTableLoading = false;
    },
    /**
     * 删除
     */
    async handleDel(row, index) {
      await this.mixinMsgDel('确认删除该标签信息？')
      const res = await xcDeviceTagApi.delObj(row.id)
      if(res.data.code===0){
        this.refresh()
        this.$notify({
          title: "删除成功",
          message: "删除成功",
          type: "success",
          duration: 2000
        });
      }else{
        this.$message.error(res.data.message);
      }
    }
  }
};
const listOption = function () {
  return {
    menu: true,
    column: [
      {
        label: '标签名称',
        prop: 'tagName'
      }
    ]
  }
}
</script>

<style lang="scss" scoped>
::v-deep .el-scrollbar__wrap {
  overflow-x: auto;
}
</style>
