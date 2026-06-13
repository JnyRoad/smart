<!--出入记录：物品方向 -->
<template>
  <div class="my-basic-container">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addInfo(searchForm)">添加终端</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="终端名称" prop="name">
            <el-input v-model="searchForm.name" placeholder="终端名称" clearable></el-input>
          </el-form-item>
        </el-form>
        <avue-crud ref="crud" :page="page" :data="tableData" :table-loading="tableLoading" :option="tableOption" @size-change="sizeChange" @current-change="currentChange">
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit" @click="handleDetail(scope.row, scope.$index, 1)">修改</el-button>
            <el-button type="text" icon="el-icon-edit" @click="deleteData(scope.row)">删除</el-button>
            <el-button type="text" icon="el-icon-link" @click="handleUrl(scope.row,scope.$index)">链接</el-button>
          </template>
        </avue-crud>
      </section>
      <!-- 新增 弹出框 -->
      <el-dialog :title="dialogFromData.id ? '编辑终端' : '新增终端'" @close="closeDialog" :visible.sync="dialogVisible">
        <el-scrollbar style="height: 400px">
          <el-form ref="form" :rules="rules" :model="dialogFromData" label-width="120px" v-if="dialogVisible">
            <el-form-item required label="终端名称" prop="name">
              <el-input v-model="dialogFromData.name" placeholder="请输入终端名称"></el-input>
            </el-form-item>
            <el-form-item label="发布资源" prop="infoId">
              <el-select v-model="dialogFromData.infoId" filterable  placeholder="发布资源(可搜索)" clearable>
                <el-option v-for="item in infoSelectData" :key="item.id" :value="item.id" :label="item.infoName"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item required label="IP地址" prop="ip">
              <el-input v-model="dialogFromData.ip" placeholder="请输入端口"></el-input>
            </el-form-item>
            <el-form-item label="备注" prop="remark">
              <el-input v-model="dialogFromData.remark" placeholder="请输入备注"></el-input>
            </el-form-item>
            <el-form-item required label="发布时效" prop="timeType">
              <el-radio v-model="dialogFromData.timeType" label="1">实时发布</el-radio>
              <el-radio v-model="dialogFromData.timeType" label="2">定时发布</el-radio>
            </el-form-item>
            <el-form-item label="" v-if="dialogFromData.timeType == 1"> 选择后及时发布到终端设备网页上，刷新显示 </el-form-item>
            <el-form-item required label="发布时间" v-else>
              <el-date-picker v-model="dialogFromData.startTime" type="datetime" value-format="yyyy-MM-dd HH:mm:ss " placeholder="请选择发布时间"> </el-date-picker>
            </el-form-item>
          </el-form>
        </el-scrollbar>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" plain @click="closeDialog">取 消</el-button>
          <el-button type="primary" @click="save" :loading="addLoading">保 存</el-button>
        </div>
      </el-dialog>
      <el-dialog title="链接" class="dialog_form" width="500px" :visible.sync="linkVisible">
          <div class="linkdv" style="padding:10px 0">
            <p>
              {{link}}
              <el-button
                class="copybtn"
                type="primary"
                v-clipboard:copy="link"
                v-clipboard:success="onCopy"
                v-clipboard:error="onError"
              >复制</el-button>
            </p>
          </div>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="linkVisible=false" plain>关闭</el-button>
          </div>
        </el-dialog>
    </el-scrollbar>
  </div>
</template>

<script>
import { returnApi } from './_service'
import { mapGetters } from 'vuex'

export default {
  name: 'article',
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        name: null
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [],
      depIds: [],
      compOptions: [],
      tableOption: tableOption,
      dialogVisible: false,
      dialogFromData: {
        name: null,
        infoId: null,
        ip: null,
        remark: null,
        timeType: '1',
        startTime: null
      },
      pickerOptions1: {
        disabledDate(time) {
          return time.getTime() <= Date.now()
        }
      },
      infoSelectData: [],
      addLoading: false,
      rules: {
        name: [{ required: true, message: '请输入终端名称', trigger: 'blur' }],
        ip: [{ required: true, message: '请输入IP地址', trigger: 'blur' }]
      },
      linkVisible: false,
      link: ''
    }
  },
  watch: {},
  created() {
    this.$nextTick(() => {
      // 详情带参数返回
      if (this.$route.query.queryForm != undefined) {
        let queryPage = this.$route.query.queryPage
        let queryForm = this.$route.query.queryForm
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {})
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {})
        }
        this.getList(this.page, this.searchForm)
      } else {
        this.getList(this.page)
      }
    })
  },
  mounted: function () {},
  computed: {
    ...mapGetters(['permissions'])
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true
      returnApi
        .getList(
          Object.assign(
            {
              current: page.currentPage,
              size: page.pageSize
            },
            params
          )
        )
        .then((response) => {
          this.tableData = response.data.data.records
          this.page.total = response.data.data.total
          this.tableLoading = false
        })
      this.tableLoading = false
    },
    async getInfoSelectData() {
      const res = await returnApi.getInfoSelect({
        current: 1,
        size: 999
      })
      this.infoSelectData = res.data.data.records
      // console.log(res, 'res')
    },
    async save() {
      await this.$refs['form'].validate()
      if (this.dialogFromData.timeType == 2 && !this.dialogFromData.startTime) {
        this.$message.error('发布时间不能为空')
        return
      }
      this.dialogFromData.timeType = parseInt(this.dialogFromData.timeType)
      this.saveLoading = true
      returnApi.saveSubmit(this.dialogFromData).then((response) => {
        this.saveLoading = false
        if (response.data.code === 0) {
          this.$message({
            showClose: true,
            message: '成功',
            type: 'success'
          })
          this.closeDialog()
          this.getList(this.page)
        } else {
          this.$notify({
            title: '失败',
            message: response.data.msg,
            type: 'error',
            duration: 2000
          })
        }
      })
    },
    addInfo() {
      this.getInfoSelectData()
      this.dialogVisible = true
    },
    handleDetail(row) {
      this.getInfoSelectData()
      this.dialogVisible = true
      this.dialogFromData = row
      if(typeof this.dialogFromData.timeType === 'number'){
          this.dialogFromData.timeType = JSON.stringify(this.dialogFromData.timeType)
      }
    },
    deleteData(row) {
      var _this = this
      const elm = this.$createElement
      this.$msgbox({
        message: elm('p', { attrs: { class: 'smallp' } }, [elm('i', { attrs: { class: 'smallInfo delInfo' } }, ''), elm('span', null, '确认删除该终端信息？ ')]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        customClass: 'small_dialog',
        center: true
      })
        .then(function () {
          return returnApi.deleteData(row.id)
        })
        .then((dataResponse) => {
          _this.getList(_this.page)
          _this.$notify({
            title: '删除成功',
            message: '删除成功',
            type: 'success',
            duration: 2000
          })
        })
        .catch(error => { console.error(error) })
    },
    closeDialog() {
      this.dialogVisible = false
      this.dialogFromData = {
        name: null,
        infoId: null,
        ip: null,
        remark: null,
        timeType: '1',
        startTime: null
      }
    },
    handleUrl(row, index) {
      this.linkVisible = true;
      const origin = window.location.origin
      this.link = origin + "/#/video?ip=" + row.ip;
    },
    onCopy(e) {
      this.$notify({
        title: "复制成功",
        message: "已复制到粘贴板,去粘贴吧！",
        type: "success",
        duration: 2000
      });
    },
    onError(e) {
      this.$notify({
        title: "复制失败",
        message: "复制失败，请尝试手动选择链接地址复制！",
        type: "success",
        duration: 2000
      });
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
      this.getList(this.page, this.searchForm)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.searchForm)
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
const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 200,
  labelWidth: 100,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  searchSize: 'mini',
  dialogWidth: '600px',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [
    {
      label: '终端名称',
      prop: 'name'
    },
    {
      label: '发布类型',
      prop: 'infoType'
    },
    {
      label: '资源名称',
      prop: 'infoName'
    },
    {
      label: 'IP地址',
      prop: 'ip',
      width: 120
    },
    {
      label: '备注',
      prop: 'remark'
    },
    {
      label: '创建时间',
      prop: 'createTime',
      width: 170
    },
    {
      label: '创建人',
      prop: 'creator'
    }
  ]
}
</script>

<style lang="scss" scoped>
.topForm ::v-deep {
  .el-form-item__label {
    width: 130px;
  }
}
.tabs ::v-deep {
  flex: 1;
  .el-tabs__item {
    height: 50px;
  }
}
</style>
