<!--保密区设置  -->
<template>
  <div class="my-basic-container parking_lot">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-left">
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加设置</el-button>
          </div>
        </div>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" @size-change="sizeChange" @current-change="currentChange" :option="tableOption">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="handleDetail(scope.row, scope.$index)">编辑</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <el-dialog title="添加" class="dialog_form" @close="resetDlgFrom('addform')" width="500px" :visible.sync="addFormVisible">
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
import { fetchList,bsSecurityAreaApi } from './_service'
import { tableOption } from '@/const/crud/platform/business/security_area'
import { mapGetters } from 'vuex'

export default {
  name: 'badgeConfig',
  data() {
    return {
      addLoading: false,
      addform: {
        parkId: '',
        parkName: ''
        // price: ''
      },
      addFormVisible: false,
      parkId: 0,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 10 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      rules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "blur" }],
      },
      whiteListForm: {
        parkId: '',
        isBusiness: '',
        isCompensatory: '',
        isHoliday: '',
        isLeave: '',
        isWhiteList: 0,
        whiteList: []
      },
    }
  },
  watch: {},
  created() {
    this.getList(this.page)
  },
  mounted: function () {},
  computed: {
    ...mapGetters(['permissions'])
  },
  methods: {
    getList(page) {
      this.tableLoading = true
      fetchList(
        Object.assign({
          current: page.currentPage,
          size: page.pageSize
        })
      ).then((response) => {
        this.tableData = response.data.data.records
        this.page.total = response.data.data.total
        this.tableLoading = false
      })
      this.tableLoading = false
    },
    parkChange(obj) {
      this.addform.parkName = obj.label
    },
    handleEdit(row, index) {
      //点击编辑
      this.editFormVisible = true
      this.editform = row
    },
    addSubmit(formName) {
      //添加内容确定
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.addLoading = true;
          this.whiteListForm.parkId = this.addform.parkId;
          bsSecurityAreaApi.editWhiteListObj(this.whiteListForm).then(dataResponse => {
            this.addFormVisible = false;
            this.addLoading = false;
            this.getList(this.page);
          }).catch(() => {
            this.addLoading = false;
          });
        } else {
          this.addLoading = true;
          return false;
        }
      })
    },
    handleDetail(row) {
      if (row != null) {
        this.parkId = row.parkId
      } else {
        this.parkId = 0
      }
      const src = `/platform/business/security_area/edit/${this.parkId}`
      this.$router.push({
        path: src
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
