<!--业务设置，访客设置  -->
<template>
  <div class="my-basic-container parking_lot">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-left">
            <el-button type="primary" icon="el-icon-plus" @click="handleDetail()">添加访客设置</el-button>
          </div>
        </div>
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
              @click="handleDetail(scope.row,scope.$index)"
            >编辑</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
  import { fetchList, getObj, editObj, delObj } from './visitor_service'
  import { tableOption } from '@/const/crud/platform/business/visitor_config'
  import { mapGetters } from 'vuex'

  export default {
    name: "badgeConfig",
    data() {
      return {
        addLoading: false,
        addform: {
          parkId: "",
          price: ""
        },
        parkId:0,
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 10 // 每页显示多少条
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
      getList(page) {
        this.tableLoading = true;
        fetchList(
          Object.assign(
            {
              current: page.currentPage,
              size: page.pageSize
            }
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
      handleDetail(row) {
        if(row != null) {
          this.parkId = row.parkId
        }
        const src = `/platform/business/visitor/detail/${this.parkId}`;
        this.$router.push({
          path: src
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
        this.getList(this.page);
      },
      currentChange(val) {
        this.page.currentPage = val;
        this.getList(this.page);
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
