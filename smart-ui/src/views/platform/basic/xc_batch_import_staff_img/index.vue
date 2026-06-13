<!--批量导入照片 -->
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
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button type="primary" @click="addTask">添加导入任务</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="mixinSearchForm" class="topForm" size="mini">
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="mixinSearchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="任务名称" prop="taskName">
            <el-input v-model="mixinSearchForm.taskName" placeholder="请输入" clearable></el-input>
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
          <template slot-scope="scope" slot="failNum">
            <span class="ft-danger">{{scope.row.failNum}}</span>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row,scope.$index)" >查看</el-button>
            <el-button type="text" icon="el-icon-delete" @click="handleDel(scope.row, scope.$index)">删除</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <importImg ref="importImg" @refresh="refresh"></importImg>
  </div>
</template>

<script>
import { xcBatchImgApi } from "./_service"
import importImg from "./components/import"

export default {
  mixins: [tce.mixins.list],
  components: {
    importImg
  },
  data() {
    return {
      tableData: [],
      listOption: listOption()
    };
  },
  created() {
    this.refresh()
  },
  mounted: function() {},
  methods: {
    refresh(){
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    async getList(page, params) {
      this.mixinTableLoading = true
      const res = await xcBatchImgApi.getList(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
          )
      this.tableData = res.data.data.records
      this.mixinPage.total = res.data.data.total
      this.mixinTableLoading = false;
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      this.$refs[formName] && this.$refs[formName].resetFields()
      this.mixinPage.currentPage = 1
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    handleDetail(row){
      const src = `/platform/basic/xc_batchImportImg/detail`
      this.$router.push({
        path: src,
        query: {
          id: row.id,
          parkName: row.parkName,
          taskName: row.taskName
        }
      })
    },
    addTask(){
      this.$refs.importImg && this.$refs.importImg.open()
    },
    /**
     * 删除
     */
    async handleDel(row, index) {
      await this.mixinMsgDel('确认删除该任务？')
      const res = await xcBatchImgApi.delObj(row.id)
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
    menuWidth: '150px',
    column: [
      {
        label: '所属园区',
        prop: 'parkName',
        type: 'input'
      },
      {
        label: '任务名称',
        prop: 'taskName',
        type: 'input'
      },
      {
        label: '导入数量',
        prop: 'totalNum',
      },
      {
        label: '下发成功数量',
        prop: 'successNum',
        width: 120
      },
      {
        label: '下发失败数量',
        prop: 'failNum',
        width: 120,
        solt: true
      },
      {
        label: '创建时间',
        prop: 'createTime',
        width: 170
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
