<!--基础信息：考勤汇总确认管理 -->
<template>
  <div class="my-basic-container staff">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-left">
            <span>查询小计</span>
            <span>已确认:{{count.countSign}}</span>
            <span>未确认:{{count.countNotSign}}</span>
          </div>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空
            </el-button>
            <el-button
              type="primary"
              icon=""
              @click="sendMsgNum()"
            >发送短信提醒
            </el-button>
            <el-button
              type="primary"
              :loading="exportLoading"
              @click="exportExcel"
              icon="icon-yutong-download"
            >导出excel
            </el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="所属园区/BU/部门" prop="depId">
            <el-cascader
              expand-trigger="hover"
              :options="options"
              :show-all-levels="false"
              :change-on-select="true"
              v-model="depIds"
              clearable
            ></el-cascader>
          </el-form-item>
          <el-form-item label="工号" prop="badge">
            <el-input v-model="searchForm.badge" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="searchForm.name" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="考勤月份" prop="checkDate">
            <el-date-picker
              v-model="searchForm.checkDate"
              type="month"
              value-format="yyyy-MM"
              placeholder="考勤月份"
              clearable
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="确认状态">
            <el-select v-model="searchForm.signStatus" placeholder="签收状态">
              <template v-for="(item, index) in status">
                <el-option :label="item.desc" :value="item.code" :key="index"></el-option>
              </template>
            </el-select>
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
              @click="handleDetail(scope.row,scope.$index)"
            >查看
            </el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <el-dialog
      title="确认发送短信提醒"
      :visible.sync="sureVisible"
      width="600px">
      <p style="padding: 10px 0 30px">点击确认发送短信，将自动短信通知当前筛选条件下的 <span style="font-weight: bold; font-size: 18px;">{{msgNum}}</span> 位员工进行工资签收</p>
      <span slot="footer" class="dialog-footer">
        <el-button @click="sureVisible = false">不发送短信</el-button>
        <el-button type="primary" @click="sendMsg" :disabled="msgTime>0">确认无误，发送短信
          <template v-if="msgTime>0">
          （{{msgTime}}s）
          </template>
        </el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  import {fetchList, sendMsg, sendMsgNum} from "./attendance_sign.js";
  import {getCompTree} from "@/api/platform/_publicService";
  import {tableOption} from "@/const/crud/platform/personnel_manage/attendance_sign";
  import {mapGetters} from "vuex";
  import {isArrayFn, getDatePreMonth} from "@/util/util";

  export default {
    name: "salary_sign",
    data() {
      return {
        sureVisible: false,
        msgNum: '',
        msgTime: 5,
        searchForm: {
          //搜索菜单表单
          badge: undefined,
          name: undefined,
          compId: undefined,
          depId: undefined,
          checkDate: undefined,
          parkId: undefined,
          syncStatus: undefined
        },
        count: {
          countNotSign: 0,
          countSign: 0
        },
        tableLoading: false,
        tableData: [],
        exportLoading: false,
        tableOption: tableOption,
        options: [],
        status: [{
          code: 1,
          desc: '未确认'
        }, {
          code: 2,
          desc: '已确认'
        }],
        depIds: [],
        page: {
          total: 0, // 总页数
          currentPage: 1, // 当前页数
          pageSize: 20 // 每页显示多少条
        }
      };
    },
    created() {
      this.searchForm.checkDate = getDatePreMonth();
      this.getList(this.page, this.searchForm);
      getCompTree().then(response => {
        this.options = response.data.data;
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
    mounted: function () {
    },
    computed: {
      ...mapGetters(["permissions"]),
    },
    methods: {
      getList(page, params) {
        this.tableLoading = true;
        params = Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        );
        fetchList(params).then(response => {
          this.tableData = response.data.data.records;
          this.page.total = response.data.data.total;
          if (response.data.data.total !== 0) {
            this.count = response.data.data.records[0];
          }
          this.tableLoading = false;
        });
        this.tableLoading = false;
      },
      sizeChange(val) {
        this.page.currentPage = 1;
        this.page.pageSize = val;
        this.getList(this.page, this.searchForm);
      },
      exportExcel() {
        var _this = this;
        const elm = this.$createElement;
        this.$msgbox({
          message: elm("p", { attrs: { class: "smallp" } }, [
            elm("i", { attrs: { class: "smallInfo checkIn" } }, ""),
            elm("span", null, "等待时间较长，是否确认导出excel？")
          ]),
          showCancelButton: true,
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          customClass: "small_dialog",
          center: true
        }).then(async function() {
          return _this.export2Excel();
        })
      },
      export2Excel() {
        require.ensure([], () => {
          this.exportLoading = true;
          const {export_json_to_excel} = require("@/vendor/Export2Excel");
          const tHeader = [
            "工号",
            "姓名",
            "所属园区",
            "BU",
            "部门",
            "考勤月份",
            "签收状态",
            "签收时间"
          ];
          const filterVal = [
            "badge",
            "name",
            "parkName",
            "compName",
            "depName",
            "checkDate",
            "signStatusDesc",
            "createTime"
          ];
          let params = Object.assign({}, this.searchForm);
          fetchList(
            Object.assign(
              {
                current: 1,
                size: this.page.total,
              },
              params
            )
          ).then(response => {
            const list = response.data.data.records;
            list.forEach(function (item) {
              item.signStatus == 1 ? (item.signStatusDesc = "未确认") : (item.signStatusDesc = "已确认");
            });
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, `考勤汇总确认&(${this.searchForm.checkDate})`);

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
      currentChange(val) {
        this.page.currentPage = val;
        this.getList(this.page, this.searchForm);
      },
      handleDetail(row, index) {
        const src = `/platform/personnel_manage/attendance_sign/detail/${row.id}`;
        this.$router.push({
          path: src
        });
      },
      /**
       * 搜索回调
       */
      searchSubmit() {
        this.page.currentPage = 1;
        this.getList(this.page, this.searchForm);
      },
      sendMsgNum() {
        sendMsgNum(this.searchForm).then(res => {
          let _this = this
          this.msgNum = res.data.data
          this.sureVisible = true
          let s1 = setInterval(function() {
            _this.msgTime -= 1
            if(_this.msgTime==0){
              clearInterval(s1)
            }
          }, 1000);
        }).catch(err => {
          this.$notify({
            title: "失败",
            message: "获取发送短信条数失败",
            type: "warning",
            duration: 2000
          });
        });
      },
      sendMsg() {
        sendMsg(this.searchForm).then(response => {
          this.$notify({
            title: "发送短信提醒成功",
            message: "发送短信提醒成功",
            type: "success",
            duration: 2000
          });
          this.sureVisible = false
        }).catch(err => {
          this.$notify({
            title: "发送短信提醒失败",
            message: "发送短信提醒失败",
            type: "warning",
            duration: 2000
          });
        });
      },
      /**
       * 清空搜索
       */
      resetFrom(formName) {
        this.searchForm.compId = undefined;
        this.searchForm.depId = undefined;
        this.searchForm.badge = undefined;
        this.searchForm.name = undefined;
        this.searchForm.checkDate = undefined;
        this.searchForm.createTime = undefined;
        this.searchForm.signStatus = undefined;
        this.depIds = [];
        this.page.currentPage = 1;
        this.getList(this.page, this.searchForm);
      }
    }
  };
</script>

<style lang="scss" scoped>
  .topForm ::v-deep {
    .el-form-item__label {
      width: 130px;
    }
  }

  ::v-deep .el-scrollbar__wrap {
    overflow-x: auto;
  }
</style>
