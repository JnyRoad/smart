<!--业务设置，厂牌设置  -->
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
            <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">添加厂牌补领设置</el-button>
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
            <!--<el-button
              type="text"
              icon="el-icon-delete"
              @click="handleDel(scope.row,scope.$index)"
            >删除</el-button>-->
          </template>
        </avue-crud>
        <el-dialog
          title="添加厂牌补领设置"
          class="dialog_form"
          @close="resetEditFrom('addform')"
          width="500px"
          :visible.sync="addFormVisible"
        >
          <el-form :rules="rules" ref="addform" :model="addform" label-width="100px">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="addform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="厂牌费用" prop="price">
              <el-input v-model="addform.price" clearable>
                <template slot="prepend">每张扣取</template>
                <template slot="append">元</template>
              </el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="addCancel('addform')" plain>取 消</el-button>
            <el-button type="primary" @click="addSubmit('addform')" :loading="addLoading">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog
          title="编辑厂牌补领设置"
          class="dialog_form"
          @close="resetEditFrom('editform')"
          width="500px"
          :visible.sync="editFormVisible"
        >
          <el-form :rules="rules" ref="editform" :model="editform" label-width="100px">
            <el-form-item label="所属园区" prop="parkId">
              <parkSelect v-model="editform.parkId"></parkSelect>
            </el-form-item>
            <el-form-item label="厂牌费用" prop="price">
              <el-input v-model="editform.price" clearable>
                <template slot="prepend">每张扣取</template>
                <template slot="append">元</template>
              </el-input>
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
  import { fetchList, getObj, editObj, delObj } from './badge_service'
  import { tableOption } from '@/const/crud/platform/business/badge_config'
  import { mapGetters } from 'vuex'

  export default {
    name: "badgeConfig",
    data() {
      return {
        addLoading: false,
        editLoading: false,
        addFormVisible: false,
        editFormVisible: false,
        addform: {
          parkId: "",
          price: ""
        },
        rules: {
          parkId: [{ required: true, message: "请选择园区", trigger: "blur" }],
          price: [{ required: true, message: "请输入费用", trigger: "blur" }]
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
          price: ""
        },
        tableLoading: false,
        tableData: [],
        tableOption: tableOption
      };
    },
    watch: {
    },
    created() {
      this.getList(this.page);
    },
    mounted: function() {},
    computed: {
      ...mapGetters(["permissions"])
    },
    methods: {
      getList(page, params) {
        this.tableLoading = true;
        fetchList(
          Object.assign(
            {
              current: page.currentPage,
              size: page.pageSize
            },params
          )
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
        this.editform = row;
      },
      editSubmit(formName) {
        //编辑内容确定
        this.$refs[formName].validate(valid => {
          if (valid) {
            this.editLoading = true;
            editObj(this.editform).then(dataResponse => {
              this.editFormVisible = false;
              this.editLoading = false;
              this.getList(this.page);
            }).catch(() => {
                this.editLoading = false;
              });;
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
              this.getList(this.page);
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
        this.$refs[formName].clearValidate();
      },
      handleDel(row, index) {
        //删除
        this.$refs.crud.rowDel(row, index);
      },
      rowDel: function(row, index) {
        var _this = this
        this.$confirm('是否确认删除ID为' + row.id, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(function() {
          return delObj(row.id)
        }).then(data => {
          _this.tableData.splice(index, 1)
          _this.$message({
            showClose: true,
            message: '删除成功',
            type: 'success'
          })
          this.getList(this.page)
        }).catch(function (error) { console.error(error) })
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
          this.getList(this.page);
        }
      }
    }
  };
</script>

<style lang="scss" scoped>
</style>
