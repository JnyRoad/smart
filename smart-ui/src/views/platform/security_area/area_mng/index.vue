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
            <el-button type="primary" icon="el-icon-plus" @click="dialogVisible = true">新增区域</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="mixinSearchForm" class="topForm" size="mini">
          <el-form-item label="关键字" prop="keyword">
            <el-input v-model="mixinSearchForm.keyword" placeholder="请输入字段名或名称" clearable></el-input>
          </el-form-item>
          <el-form-item label="所属工厂" prop="factoryType">
            <el-select v-model="mixinSearchForm.factoryType" clearable>
              <el-option label="新工厂" :value="1"></el-option>
              <el-option label="老工厂" :value="2"></el-option>
            </el-select>
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
          <template slot-scope="scope" slot="factoryType">
            <template v-if="scope.row.factoryType==1">新工厂</template>
            <template v-else-if="scope.row.factoryType==2">老工厂</template>
            <template v-else>-</template>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-edit"  @click="handleEdit(scope.row,scope.$index)" >编辑</el-button>
            <el-button type="text" icon="el-icon-delete" @click="handleDel(scope.row,scope.$index)">删除</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <!-- 新增区域 弹出框 -->
    <el-dialog :title="dialogFromData.id ? '编辑区域' : '新增区域'" width="600px" :visible.sync="dialogVisible">
      <el-scrollbar>
        <el-form ref="form" :rules="rules" :model="dialogFromData" label-width="80px" v-if="dialogVisible">
          <el-row>
            <el-col :span="24">
              <el-form-item label="所属工厂" prop="factoryType">
                <el-select v-model="dialogFromData.factoryType" placeholder="请选择所属工厂" :disabled="!!dialogFromData.id">
                  <el-option label="新工厂" :value="1"></el-option>
                  <el-option label="老工厂" :value="2"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="编号" prop="code">
                <el-input v-model.trim="dialogFromData.code" placeholder="请输入编号" :maxlength="100" :disabled="!!dialogFromData.id"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="字段名" prop="type">
                <el-input v-model.trim="dialogFromData.type" placeholder="请输入字段名" :maxlength="100" :disabled="!!dialogFromData.id"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row style="margin-bottom: 30px;">
            <el-col :span="24">
              <el-form-item label="名称" prop="desc">
                <el-input v-model.trim="dialogFromData.desc" placeholder="请输入名称" :maxlength="100"></el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </el-scrollbar>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" plain @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { areaApi } from "./_service"

export default {
  mixins: [tce.mixins.list],
  components: {},
  data() {
    return {
      tableData: [],
      listOption: listOption(),
      mixinSearchForm: {
        keyword: '',
        factoryType: ''
      },
      rules: {
        code: [
          { required: true, message: '请输入编号', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '请输入字段名', trigger: 'blur' }
        ],
        desc: [
          { required: true, message: '请输入名称', trigger: 'blur' }
        ],
        factoryType: [
          { required: true, message: '请选择所属工厂', trigger: 'change' }
        ]
      },
      dialogFromData: {
        code: '',
        type: '',
        desc: '',
        factoryType: 1
      },
      dialogVisible: false,
    };
  },
  created() {
    this.refresh()
  },
  watch: {
     dialogVisible(value) {
      if (!value) {
        this.resetDialogFromData()
      }
    }
  },
  mounted: function() {},
  methods: {
    resetDialogFromData() {
      this.dialogFromData = {
        code: '',
        type: '',
        desc: '',
        factoryType: 1
      }
    },
    async handleEdit(row, index) {
      this.dialogFromData = {
        id: row.id,
        code: row.code,
        type: row.type,
        desc: row.desc,
        factoryType: row.factoryType
      }
      this.dialogVisible = true
    },
    async save() {
      await this.$refs['form'].validate()
      let res = null
      if(this.dialogFromData.id){
        res = await areaApi.editObj(this.dialogFromData)
      }else{
        res = await areaApi.addObj(this.dialogFromData)
      }
      if (res.data.data) {
        this.$message({
          showClose: true,
          message: '保存成功',
          type: 'success'
        })
        this.dialogVisible = false
        this.refresh()
      }
    },
    async handleDel(row, index) {
      await this.$confirm('是否确认删除', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const res = await areaApi.delObj(row.id)
      if (res.data.code === 0) {
        this.$message({
          showClose: true,
          message: '删除成功',
          type: 'success'
        })
        this.refresh()
      }
    },
    refresh(){
      this.getList(this.mixinPage, this.mixinSearchForm)
    },
    async getList(page, params) {
      this.mixinTableLoading = true
      const res = await areaApi.getList(
        Object.assign({
          current: page.currentPage,
          size: page.pageSize
        }, params)
      )
      this.tableData = res.data.data.records
      this.mixinPage.total = res.data.data.total
      this.mixinTableLoading = false;
    },
    mixinResetFrom(formName){
      this.$refs[formName] && this.$refs[formName].resetFields()
      this.mixinPage.currentPage = 1
      this.getList(this.mixinPage)
    }
  }
};
const listOption = function () {
  return {
    menu: true,
    menuWidth: 180,
    selection: true,
    column: [
     {
        label: '编号',
        prop: 'code'
      },{
        label: '字段名',
        prop: 'type'
      },{
        label: '名称',
        prop: 'desc'
      },{
        label: '所属工厂',
        prop: 'factoryType',
        solt: true
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
