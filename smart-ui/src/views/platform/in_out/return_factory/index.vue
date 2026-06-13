<!--出入记录：物品方向 -->
<template>
  <div class="my-basic-container">
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
          </div>
        </div>
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="OA单号" prop="processId">
              <el-input v-model="searchForm.processId" placeholder="OA单号" clearable></el-input>
            </el-form-item>
            <!-- <el-form-item label="物品名称" prop="goodsName">
              <el-input v-model="searchForm.goodsName" placeholder="物品名称" clearable></el-input>
            </el-form-item> -->
            <el-form-item label="携带人姓名" prop="name">
              <el-input v-model="searchForm.name" placeholder="携带人姓名" clearable></el-input>
            </el-form-item>
          </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index, 1)"
            >查看</el-button>

            <el-button
              v-if="scope.row.backStatus === '未确认'"
              type="text"
              icon="el-icon-edit"
              @click="apply(scope.row)"
            >确认返厂</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { returnApi } from "./_service";
import { mapGetters } from "vuex";

export default {
  name: "article",
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        processId: null,
        name: null,
        goodsName: null
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
      tableOption: tableOption
    };
  },
  watch: {
  },
  created() {
    this.$nextTick(() => {
      // 详情带参数返回
      if (this.$route.query.queryForm != undefined) {
        let queryPage = this.$route.query.queryPage;
        let queryForm = this.$route.query.queryForm;
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {});
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {});
        }
        this.getList(this.page, this.searchForm);
      } else {
        this.getList(this.page);
      }
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),
  },
  methods: {
    getList(page, params) {
        this.tableLoading = true;
        returnApi.getList(
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
    async apply(row){ // 确认返厂
      await this.$confirm('是否确认返厂', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      returnApi.confirm({ releaseId: row.id }).then((response) => {
        if (response.data.code === 0) {
          this.$notify({
            title: '成功',
            message: '确认返厂成功',
            type: 'success'
          })
          this.getList(this.page, this.searchForm);
        } else {
          this.$notify.error({
            title: '失败',
            message: response.data.msg
          })
        }
      })
    },
    handleDetail(row, index, isDetail) {
      const src = `/platform/in_out/return_factory/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
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
  dialogWidth : '600px',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [
    {
      label: 'OA单号',
      prop: 'processId',
    },
    {
      label: '申请人姓名',
      prop: 'name',
    },
    {
      label: '放行事项',
      prop: 'releaseItemDesc'
    },
    {
      label: '物品放行类型',
      prop: 'releaseTypeDesc',
    }, {
      label: 'OA节点',
      prop: 'oaNode'
    }, {
      label: '申请时间',
      prop: 'createTime',
      width: 170
    },
    {
      label: '状态',
      prop: 'backStatus',
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
    .el-tabs__item{
      height: 50px;
    }
  }
</style>
