<!--人资行政管理，考勤异常统计  -->
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
            >清空
            </el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <template>
            <el-form-item label="所属园区/BU/部门" prop="depIds">
              <el-cascader
                expand-trigger="hover"
                :options="compOptions"
                :show-all-levels="false"
                :change-on-select="true"
                v-model="depIds"
                clearable
              ></el-cascader>
            </el-form-item>
          </template>
          <el-form-item label="补卡日期" prop="startTime">
            <el-date-picker
              v-model="searchForm.startTime"
              type="date"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              placeholder="补卡日期"
              clearable
            ></el-date-picker>
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
            >查看
            </el-button>
          </template>
        </avue-crud>


        <el-dialog
          title=""
          class="dialog_form"
          @close="resetEditFrom('editform')"
          width="600px"
          :visible.sync="editFormVisible"
        >
          <div class="box-outer">
            <p class="box-orange">统计记录</p>
            <table class="lit-table">
              <tr>
                <td>补卡日期</td>
                <td>{{detailForm.startTime}}</td>
              </tr>
              <tr>
                <td>园区</td>
                <td>{{detailForm.parkName}}</td>
              </tr>
              <tr>
                <td>BU</td>
                <td>{{detailForm.compName}}</td>
              </tr>
              <tr>
                <td>部门</td>
                <td>{{detailForm.depName}}</td>
              </tr>
              <tr>
                <td>补卡原因</td>
                <td>{{detailForm.cause}}</td>
              </tr>
              <tr>
                <td>补卡人数</td>
                <td>{{detailForm.statistics}}</td>
              </tr>
            </table>
          </div>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="editCancel('editform')">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
  import {fetchList} from './abnormal_attendance'
  import {getCompTree} from "@/api/platform/_publicService";
  import {tableOption} from '@/const/crud/platform/personnel_manage/abnormal_attendance'
  import {isArrayFn, getDatePreDay} from "@/util/util";
  import {mapGetters} from 'vuex'

  export default {
    name: "badgeLoss",
    data() {
      return {
        editFormVisible: false,
        compOptions: [],
        depIds: [],
        times: [],
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 10 // 每页显示多少条
        },
        detailForm: {
          startTime: undefined,
          parkName: undefined,
          compName: undefined,
          depName: undefined,
          cause: undefined,
          statistics: undefined,
        },
        searchForm: {
          parkId: undefined,
          depId: undefined,
          compId: undefined,
          startTime: undefined,
        },
        tableData: [],
        tableOption: tableOption
      };
    },
    watch: {
      depIds(newVal, oldVal) {
        if (isArrayFn(newVal) && newVal.length > 0) {
          const depLength = newVal.length;
          if (depLength == 3) {
            this.searchForm.depId = this.depIds[2];
            this.searchForm.compId = undefined;
            this.searchForm.parkId = undefined;
          } else if (depLength >= 2) {
            this.searchForm.depId = undefined;
            this.searchForm.compId = this.depIds[1];
            this.searchForm.parkId = undefined;
          } else if (depLength >= 1) {
            this.searchForm.depId = undefined;
            this.searchForm.compId = undefined;
            this.searchForm.parkId = this.depIds[0];
          }
        } else {
          this.searchForm.depId = undefined;
          this.searchForm.compId = undefined;
          this.searchForm.parkId = undefined;
        }
      }
    },
    created() {
      // this.searchForm.startTime = getDatePreDay();
      this.getList(this.page, this.searchForm);
      getCompTree().then(response => {
        this.compOptions = response.data.data;
      });
    },
    mounted: function () {
    },
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
          }, params
        ).then(response => {
          this.tableData = response.data.data.records;
          this.page.total = response.data.data.total;
          this.tableLoading = false;
        });
        this.tableLoading = false;
      },
      editCancel(formName) {
        this.editFormVisible = false;
        this.resetEditFrom(formName);
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
      handleEdit(row, index) {
        //点击编辑
        this.editFormVisible = true;
        this.detailForm = row;
      },
      /**
       * 清空搜索
       */
      resetFrom(formName) {
        this.$refs[formName].resetFields();
        this.depIds = []
        this.searchForm.startTime = undefined;
        this.page.currentPage = 1;
        this.getList(this.page, this.searchForm);
      }
    }
  }
</script>

<style lang="scss" scoped>
  .topForm ::v-deep {
    .el-form-item__label {
      width: 130px;
    }
  }

  .record {
    padding-left: 30px;
    margin-top: 20px;

    &-item {
      display: flex;

      &__left {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding-right: 8px;

        .num {
          width: 20px;
          height: 20px;
          border-radius: 50%;
          border: 1px solid #e0e0e0;
          text-align: center;
          line-height: 18px;
        }

        .line1 {
          flex: 1;
          width: 1px;
          border-left: 1px solid #e0e0e0;
        }
      }

      &__right {
        .line2 {
          margin: 5px 0;
        }

        padding-bottom: 25px;
      }
    }

    &-item:last-child {
      .record-item__left {
        .line1 {
          display: none;
        }
      }
    }
  }
</style>
