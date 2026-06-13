<!--个人水电明细模板  -->
<template>
  <div class="my-basic-container room">
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
            >重置</el-button>
            <el-button
              type="primary"
              :loading="exportLoading"
              @click="export2Excel"
              icon="icon-yutong-download"
            >导出表格</el-button>
            <!-- <el-button type="primary" icon="el-icon-plus" @click="addFormVisible = true">导出个人水电明细报表</el-button> -->
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" size="mini" class="topForm">
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
          <el-form-item label="工号" prop="badge">
            <el-input v-model="searchForm.badge" clearable></el-input>
          </el-form-item>
            <el-form-item label="姓名" prop="name">
            <el-input v-model="searchForm.name" clearable></el-input>
          </el-form-item>
          <el-form-item label="扣款月份" prop="meterMonth">
            <el-date-picker v-model="searchForm.meterMonth" type="month" value-format="yyyy-MM" placeholder="选择月份"></el-date-picker>
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
        <template slot-scope="scope" slot="inTime">
          <span
          >{{dateFormat(scope.row.inTime)}}</span>
        </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList
} from "@/api/platform/dormitory/staff_sdstatement";
import { getCompTree } from "@/api/platform/_publicService";
import { tableOption } from "@/const/crud/platform/dormitory/staff_sdstatement";
import { validatenum } from "@/util/validate";
import { isArrayFn } from "@/util/util";
import { dateFormat2 } from "@/util/date";
import { mapGetters } from "vuex";

export default {
  name: "dorm_mng",
  data() {
    var validateIsNum = (rule, value, callback) => {
      let regName = /^-?\d+$/;
      if (value === 0) {
        callback(new Error("请输入非0整数"));
      }
      if (!regName.test(value)) {
        callback(new Error("请输入整数"));
      } else {
        callback();
      }
    };
    var validateFloorNum = (rule, value, callback) => {
      let regName = /^\d+$/;
      if (value == 0) {
        callback(new Error("请输入大于0的正整数"));
      }

      if (!regName.test(value)) {
        callback(new Error("请输入正整数"));
      } else if (value > 14) {
        callback(new Error("楼层数量最大值为14"));
      } else {
        callback();
      }
    };

    var validateRoomNum = (rule, value, callback) => {
      let regName = /^\d+$/;
      // if(value==0)
      // {
      //    callback(new Error('请输入大于0的正整数'));
      // }

      if (!regName.test(value)) {
        callback(new Error("不能输入小数和负数"));
      } else if (value > 24) {
        callback(new Error("房间数量最大值为24"));
      } else {
        callback();
      }
    };
    return {
      exportLoading: false,
      searchForm: {
        parkId: undefined, //园区ID
        BUId: undefined, //BUID
        deptId: undefined, //部门ID
        badge: undefined, //工号
        name: undefined, //姓名
        meterMonth: undefined //抄表月份
      },
      depIds: [],
      tableLoading: false,
      compOptions: [],
      tableData: [],
      tableOption: tableOption,

      dormitoryData: [],
      floorData: [],
      roomDate: [],

      addDormitoryData: [],
      addFloorData: [],
      addRoomDate: [],

      defaultProps: {
        label: "parkName",
        value: "id"
      },
      dormiProps: {
        label: "dormitoryName",
        value: "id"
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    };
  },
  created() {
    this.getList(this.page);
    getCompTree().then(response => {
      this.compOptions = response.data.data;
    });
  },
  watch: {
    depIds(newVal, oldVal) {
      if (isArrayFn(newVal) && newVal.length > 0) {
        const depLength = newVal.length;
        if (depLength == 3) {
          this.searchForm.depId = this.depIds[2];
          this.searchForm.compId = this.depIds[1];
          this.searchForm.parkId = this.depIds[0];
        } else if (depLength >= 2) {
          this.searchForm.depId = undefined;
          this.searchForm.compId = this.depIds[1];
          this.searchForm.parkId = this.depIds[0];
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
  mounted: function() {},
  computed: {},
  methods: {
    dateFormat(val) {
      if (!this.validatenull(val)) {
        return dateFormat2(new Date(val));
      }
    },
    getList(page, params) {
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });

      this.tableLoading = false;
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page, this.searchform);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page, this.searchform);
    },
    resetMeterReadData(formName) {
      this.addMeterReadFormVisible = false;
      this.setLoading = false;
      this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    searchSubmit(form) {
      //搜索
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
    resetFrom(formName) {
      //清空
      this.$refs[formName].resetFields();
      this.dormitoryData = [];
      this.page.currentPage = 1;
      this.getList(this.page);
    },
    //导出表格
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "工号",
          "员工姓名",
          "BU",
          "部门",
          "入住日期",
          "房号",
          "住宿天",
          "日平均金额",
          "个人扣款",
          "扣款月份",
          "扣款来源园区"
        ];
        const filterVal = [
          "badge",
          "name",
          "compName",
          "depName",
          "inTime",
          "roomName",
          "inDays",
          "avgFee",
          "fee",
          "meterMonth",
          "parkName"
        ];
        let params = Object.assign({}, this.searchForm);
        fetchList(
          Object.assign(
            {
              current: 1,
              size: this.page.total
            },
            params
          )
          )
          .then(response => {
            const list = response.data.data.records;
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, `个人扣款信息&(${this.searchForm.meterMonth})`);
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
  }
};
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
