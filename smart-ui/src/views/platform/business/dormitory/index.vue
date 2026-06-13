<!--业务设置，宿舍管理员设置  -->
<template>
  <div class="my-basic-container parking_lot">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加配置</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          @size-change="sizeChange"
          @current-change="currentChange"
          :option="tableOption"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-edit"
              @click="handleEdit(scope.row,scope.$index)"
            >编辑</el-button>
          </template>
        </avue-crud>
        <el-dialog
          title="添加宿舍管理员"
          class="dialog_form"
          @close="resetAddFrom('addform')"
          width="600px"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="rules" ref="addform" :model="addform" label-width="110px">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="addform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="宿舍管理员1" prop="badgeOne">
              <el-input v-model="addform.badgeOne" placeholder="请输入工号" clearable></el-input>
            </el-form-item>
            <el-form-item label="宿舍管理员2" prop="badgeTwo">
              <el-input v-model="addform.badgeTwo" placeholder="请输入工号" clearable></el-input>
            </el-form-item>
            <el-form-item label="宿舍管理员3" prop="badgeThree">
              <el-input v-model="addform.badgeThree" placeholder="请输入工号" clearable></el-input>
            </el-form-item>
            <el-form-item label="宿舍管理员4" prop="badgeFour">
              <el-input v-model="addform.badgeFour" placeholder="请输入工号" clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addCancel('addform')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addform')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog
          title="编辑宿舍管理员"
          class="dialog_form"
          @close="resetEditFrom('editform')"
          width="600px"
          :visible.sync="editFormVisible"
        >
          <el-form :rules="rules" ref="editform" :model="editform" label-width="110px">
            <el-form-item label="宿舍管理员1" prop="badgeOne">
                <el-input v-model="editform.badgeOne" clearable></el-input>
            </el-form-item>
            <el-form-item label="宿舍管理员2" prop="badgeTwo">
              <el-input v-model="editform.badgeTwo" clearable></el-input>
            </el-form-item>
            <el-form-item label="宿舍管理员3" prop="badgeThree">
              <el-input v-model="editform.badgeThree" clearable></el-input>
            </el-form-item>
            <el-form-item label="宿舍管理员4" prop="badgeFour">
              <el-input v-model="editform.badgeFour" clearable></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="editCancel('editform')" plain>取 消</el-button>
            <el-button type="primary" @click="editSubmit('editform')" :loading="editLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
  import { fetchList,editObj } from './_service'
  import { tableOption } from '@/const/crud/platform/business/dormitory_config'
  import { mapGetters } from 'vuex'
  export default {
    name: "dormitoryConfig",
    data() {
      return {
        addLoading: false,
        editLoading: false,
        addFormVisible: false,
        editFormVisible: false,
        addform: {
          parkId: "",
          badgeOne: "",
          badgeTwo: "",
          badgeThree: "",
          badgeFour: ""
        },
        rules: {
          parkId: [{ required: true, message: "请选择园区", trigger: "blur" }]
        },
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 10 // 每页显示多少条
        },
        searchForm: {
          parkId: ""
        },
        editform: {
          parkId: "",
          badgeOne: "",
          badgeTwo: "",
          badgeThree: "",
          badgeFour: ""
        },
        tableLoading: false,
        tableData: [],
        tableOption: tableOption
      };
    },
    watch: {
    },
    created() {
      this.getList(this.page, this.searchForm);
    },
    mounted: function() {},
    computed: {
      ...mapGetters(["permissions"])
    },
    methods: {
      getList(page, params) {
        this.tableLoading = true;
        fetchList(
            {
              current: page.currentPage,
              size: page.pageSize
            },params
        ).then(response => {
          this.tableData = response.data.data.records;
          this.page.total = response.data.data.total;
          this.tableLoading = false;
        });
        this.tableLoading = false;
      },
      handleEdit(row, index) {
        //点击编辑
        this.editFormVisible = true;
        this.editform =Object.assign({},row);
      },
      editSubmit(formName) {
        //编辑内容确定
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.editLoading = true;
            editObj(this.editform).then(dataResponse => {
              this.editFormVisible = false;
              this.editLoading = false;
              this.getList(this.page, this.searchForm);
            }).catch(() => {
                this.editLoading = false;
              });
          } else {
            return false;
          }
        });
      },
      addSubmit(formName) {
        //添加内容确定
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.addLoading = true;
            editObj(this.addform).then(dataResponse => {
              this.addFormVisible = false;
              this.addLoading = false;
              this.getList(this.page, this.searchForm);
            }).catch(() => {
              this.addLoading = false;
            });
          } else {
            this.addLoading = true;
            return false;
          }
        });
      },
      editCancel(formName) {
        this.editFormVisible = false;
        this.resetEditFrom(formName);
      },
      addCancel(formName) {
        this.addFormVisible = false;
        this.resetAddFrom(formName);
      },
      resetAddFrom(formName) {
        this.$refs[formName].resetFields();
        this.$refs[formName].clearValidate();
      },
      resetEditFrom(formName) {
        this.$refs[formName].resetFields();
        this.$refs[formName].clearValidate();
      },
      /**
       * 搜索回调
       */
      searchSubmit(form) {
        this.page.currentPage = 1;
        this.getList(this.page, form);
      },
      sizeChange(val) {
        this.page.currentPage = 1;
        this.page.pageSize = val;
        this.getList(this.page, this.searchForm);
      },
      currentChange(val) {
        this.page.currentPage = val;
        this.getList(this.page, this.searchForm);
      },
      /**
       * 清空搜索
       */
      resetFrom(formName) {
        if (this.$refs[formName] != undefined) {
          this.$refs[formName].resetFields();
          this.page.currentPage = 1;
          this.getList(this.page, this.searchForm);
        }
      }
    }
  };
</script>

<style lang="scss" scoped>
  .tip1{
    font-size: 12px;
    color: #666;
  }
  .content1{
    display: flex;
    >div{
      flex: 1;
      padding: 0 10px;
    }
  }
</style>
