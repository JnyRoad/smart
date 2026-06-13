<!--人资行政管理，厂牌挂失记录  -->
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
            <el-button type="primary" icon="ali-icon-download" @click="export2Excel()">导出excel</el-button>
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
          <el-form-item label="工号">
            <el-input v-model="searchForm.badge" placeholder="请输入工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="挂失时间" >
                <el-date-picker
                  v-model="times"
                  format="yyyy-MM-dd HH:mm:ss"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  :default-time="['00:00:00', '23:59:59']"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
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
            >查看</el-button>
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
            <p class="box-orange">挂失详情</p>
            <table class="lit-table">
              <tr>
                <td>员工工号</td>
                <td>{{detailForm.badge}}</td>
              </tr>
              <tr>
                <td>员工姓名</td>
                <td>{{detailForm.name}}</td>
              </tr>
              <tr>
                <td>所属园区</td>
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
                <td>挂失时间</td>
                <td>{{detailForm.createTime}}</td>
              </tr>
            </table>
          </div>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="editCancel('editform')" >确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
  import { fetchList, exportList } from './badge_service'
  import { getCompTree } from "@/api/platform/_publicService";
  import { tableOption } from '@/const/crud/platform/personnel_manage/badge_loss'
  import { isArrayFn } from "@/util/util";
  import { mapGetters } from 'vuex'

  export default {
    name: "badgeLoss",
    data() {
      return {
        editFormVisible: false,
        compOptions:[],
        depIds:[],
        times: [],
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 10 // 每页显示多少条
        },
        searchForm: {
          parkId: undefined,
          depId:undefined,
          compId:undefined,
          badge:undefined,
          startTime:undefined,
          endTime:undefined
        },
        detailForm: {
          badge: undefined,
          name: undefined,
          compName: undefined,
          depName: undefined,
          parkName: undefined,
          startTime: undefined,
        },
        tableData: [],
        tableOption: tableOption
      };
    },
    watch: {
      times(val, oldVal) {
        if (!val || val.length === 0) {
          this.searchForm.startTime = undefined
          this.searchForm.endTime = undefined
        } else {
          this.searchForm.startTime = val[0]
          this.searchForm.endTime = val[1]
        }
      },
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
      this.getList(this.page,this.searchForm);
      getCompTree().then(response => {
        this.compOptions = response.data.data;
      });
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
      export2Excel() {
        require.ensure([], () => {
          this.exportLoading = true;
          const { export_json_to_excel } = require("@/vendor/Export2Excel");
          const tHeader = [
            "员工工号",
            "员工姓名",
            "所属园区",
            "BU",
            "部门",
            "挂失时间"
          ];
          const filterVal = [
            "badge",
            "name",
            "parkName",
            "compName",
            "depName",
            "createTime"
          ];
          let params = Object.assign({}, this.searchForm);
          fetchList(
            {
              current: 1,
              size: this.page.total,
            },
            params

          ).then(response => {
            const list = response.data.data.records;
            const data = this.formatJson(filterVal, list);
            // export_json_to_excel(tHeader, data, "厂牌挂失记录");

            export_json_to_excel(tHeader, data, `厂牌挂失记录&(${this.times})`);

            this.exportLoading = false;
          })
            .catch(err => {
              this.exportLoading = false;
            });
        });
      },
      //导出相关
      formatJson(filterVal, jsonData) {
        return jsonData.map(v => filterVal.map(j => v[j]));
      },
      handleEdit(row, index) {
        //点击编辑
        this.editFormVisible = true;
        this.detailForm = row;
      },
      editCancel(formName) {
        this.editFormVisible = false;
        this.resetEditFrom(formName);
      },
      resetEditFrom(formName) {
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
          this.times = []
          this.depIds = []
          this.searchForm.badge = null
          this.searchForm.startTime = null
          this.searchForm.endTime = null
          this.$refs[formName].resetFields();
          this.page.currentPage = 1;
          this.getList(this.page,this.searchForm);
        }
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
