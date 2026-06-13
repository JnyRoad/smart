<!--业务设置，园区报修  -->
<template>
  <div class="my-basic-container parking_lot">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-left">
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加</el-button>
          </div>
        </div>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" @size-change="sizeChange" @current-change="currentChange" :option="tableOption">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="handleDetail(scope.row, scope.$index)">编辑</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <el-dialog
      title="添加"
      class="dialog_form"
      @close="resetDlgFrom('addform')"
      width="500px"
      :visible.sync="addFormVisible"
    >
      <el-form :rules="rules" ref="addform" :model="addform" label-width="100px">
        <el-form-item label="所属园区" prop="parkId">
          <parkSelect v-model="addform.parkId" @getItem="parkChange"></parkSelect>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="addFormVisible = false" plain>取 消</el-button>
        <el-button type="primary" @click="addSubmit('addform')" :loading="addLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { fetchList, addObj } from '../release/_service'
import { tableOption } from '@/const/crud/platform/business/repair'

export default {
  data() {
    return {
      addLoading: false,
      addform: {
        parkId: '',
        parkName: '',
        eventCode: 5
      },
      eventCode: 5, //3、物品放行 5、园区报修
      rules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "blur" }],
      },
      addFormVisible: false,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 10 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption
    }
  },
  watch: {},
  created() {
    this.getList(this.page)
  },
  mounted: function () {},
  methods: {
    //列表查询
    getList(page) {
      this.tableLoading = true
      fetchList(
        {
          current: page.currentPage,
          size: page.pageSize,
          eventCode: this.eventCode
        }
      ).then((response) => {
        this.tableData = response.data.data.records
        this.page.total = response.data.data.total
        this.tableLoading = false
      })
      this.tableLoading = false
    },
    parkChange(obj){
      this.addform.parkName = obj.label
    },
    //添加内容确定
    addSubmit(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.addLoading = true
          addObj(this.addform).then(() => {
            this.addFormVisible = false
            this.addLoading = false
            this.getList(this.page)
          }).catch(error => { console.error(error) }).finally(()=>{
            this.addLoading = false
          })
        } else {
          return false
        }
      })
    },
    //编辑
    handleDetail(row) {
      const src = `/platform/business/repair/detail`
      this.$router.push({
        path: src,
        query: {
          id: row.id,
          parkId: row.parkId,
          parkName: row.parkName
        }
      })
    },
    resetDlgFrom(formName) {
      this.addFormVisible = false
      this.$refs[formName] && this.$refs[formName].resetFields()
      this.$refs[formName] && this.$refs[formName].clearValidate()
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1
      this.getList(this.page, form)
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page)
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields()
        this.page.currentPage = 1
        this.getList(this.page)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
</style>
