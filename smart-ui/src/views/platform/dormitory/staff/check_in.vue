<!--宿舍管理，入住信息  -->
<template>
  <div class="box-outer">
    <div class="top-menu clear">
      <!-- 员工列表1 -->
      <div class="top-right">
        <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
        <el-button
          type="primary"
          icon="el-icon-delete"
          @click="resetFrom('searchForm')"
          plain
        >清空</el-button>
        <el-button
          type="primary"
          :loading="exportLoading"
          @click="export2Excel"
          icon="icon-yutong-download"
        >导出表格</el-button>
      </div>
    </div>
    <tce-Search-bar>
      <el-form
        ref="searchForm"
        :inline="true"
        :model="searchForm"
        class="topForm"
        size="mini"
      >
        <el-form-item label="工号" prop="staffBadge">
          <el-input v-model="searchForm.staffBadge" placeholder="工号" clearable></el-input>
        </el-form-item>
        <el-form-item label="员工姓名" prop="staffName">
          <el-input v-model="searchForm.staffName" placeholder="员工姓名" clearable></el-input>
        </el-form-item>
        <el-form-item label="BU" prop="compName">
          <el-input v-model="searchForm.compName" placeholder="BU" clearable></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-select v-model="searchForm.sex" clearable placeholder="性别">
            <el-option label="男" value="0"></el-option>
            <el-option label="女" value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="职层" prop="jcheId">
          <jcheSelect v-model="searchForm.jcheId"></jcheSelect>
        </el-form-item>
        <el-form-item label="入住时间" prop="rangTimeIn">
          <el-date-picker
            v-model="searchForm.rangTimeIn"
            type="datetimerange"
            range-separator="-"
            value-format="yyyy-MM-dd HH:mm:ss"
            :default-time="['00:00:00', '23:59:59']"
            start-placeholder="起始时间"
            end-placeholder="截止时间"
            clearable
          ></el-date-picker>
        </el-form-item>
      </el-form>
    </tce-Search-bar>
    <avue-crud
      ref="staffTable"
      :page="page"
      :data="tableData"
      :option="tableOption"
      @size-change="sizeChange"
      @current-change="currentChange"
    >
      <template slot-scope="scope" slot="compName">
        <template
          v-if="scope.row.compName!=null && scope.row.compName!=''"
        >{{scope.row.compName}}</template>
        <template v-else>{{scope.row.dorCompName}}</template>
      </template>

      <template slot-scope="scope" slot="depName">
        <template
          v-if="scope.row.depName!=null && scope.row.depName!=''"
        >{{scope.row.depName}}</template>
        <template v-else>{{scope.row.dorDepName}}</template>
      </template>

      <template slot-scope="scope" slot="jobName">
        <template
          v-if="scope.row.jobName!=null && scope.row.jobName!=''"
        >{{scope.row.jobName}}</template>
        <template v-else>{{scope.row.dorJobName}}</template>
      </template>
      <template slot-scope="scope" slot="createTime">
        <span
          class="ft-blue"
          @click="editTime(scope.row)"
        >{{dateFormat(scope.row.createTime)}}</span>
      </template>
    </avue-crud>
    <el-dialog title="修改入住时间" class="dialog_form" width="400px" :visible.sync="timeVisible">
      <el-form ref="timeForm" :model="timeForm" :rules="timeRules">
        <el-form-item label="请选择入住时间" prop="createTime">
          <el-date-picker
            v-model="timeForm.createTime"
            type="date"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            :picker-options="timePickerOptions"
            clearable
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="timeVisible = false" plain>取 消</el-button>
        <el-button type="primary" @click="handleEditTime('timeForm')" :loading="timeLoading">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<!--引用的room.scss-->
<style lang="scss">
@use "@/styles/platform/dormitory/room" as *;
</style>

<script>
import {
  fetchList,
  allList,
  updateDormitoryStaff
} from "@/api/platform/dormitory/staff";
import { tableOption } from "@/const/crud/platform/dormitory/staff";
import { mapGetters } from "vuex";
import { dateFormat2 } from "@/util/date";
import { isArrayFn } from "@/util/util";

export default {
  name: "room",
  data() {
    return {
      timeVisible: false,
      timeLoading: false,
      timePickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
      timeForm: {
        id: "",
        createTime: ""
      },
      timeRules: {
        createTime: [
          { required: true, message: "请输入入住时间", trigger: "blur" }
        ]
      },
      exportLoading: false,
      searchForm: {
        //搜索菜单表单
        compName: undefined,
        staffBadge: undefined,
        staffName: undefined,
        sex: undefined,
        jcheId: undefined,
        startTime: undefined,
        endTime: undefined
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableOption: tableOption,
      tableData: []
    };
  },
  created() {},
  mounted: function() {},
  computed: {},
  watch: {
    roomInfo: {
      handler:function(newV,oldV){
        this.parentSearch()
      },
      immediate: true,
      deep: true
    }
  },
  props:{
    roomInfo: null //(parkId,dormitoryId,floorId,roomId)
  },
  methods: {
    dateFormat(val) {
      if (!this.validatenull(val)) {
        return dateFormat2(new Date(val));
      }
    },
    editTime(row) {
      this.timeVisible = true;
      this.timeForm = { id: row.id, createTime: row.createTime };
    },
    handleEditTime(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.timeLoading = true;
          updateDormitoryStaff(this.timeForm)
            .then(response => {
              this.timeVisible = false;
              this.timeLoading = false;
              this.getList(this.page, this.searchForm);
            })
            .catch(err => {
              this.timeLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "所属园区",
          "楼栋",
          "房间号",
          "床位号",
          "工号",
          "员工姓名",
          "性别",
          "BU",
          "单位组织",
          "岗位名称",
          "职层",
          "入住时间"
        ];
        const filterVal = [
          "parkName",
          "dormitoryName",
          "roomName",
          "bedNumber",
          "staffBadge",
          "staffName",
          "sex",
          "compName",
          "depName",
          "jobName",
          "jcheName",
          "createTime"
        ];
        let params = Object.assign({}, this.searchForm);
        if (isArrayFn(params.rangTimeIn)) {
          params.startTime = params.rangTimeIn[0];
          params.endTime = params.rangTimeIn[1];
          params.rangTimeIn = undefined;
        }
        fetchList(
          Object.assign(
            {
              current: 1,
              size: this.page.total,
              parkId: this.roomInfo.parkId,
              dormitoryId: this.roomInfo.dormitoryId,
              floorId: this.roomInfo.floorId,
              roomId: this.roomInfo.roomId
            },
            params
          )
        )
          .then(response => {
            const list = response.data.data.records;
            list.forEach(function(item) {
              item.sex == 1 ? (item.sex = "女") : (item.sex = "男");

              if (item.compName == null || item.compName == "") {
                item.compName = item.dorCompName;
              }
              if (item.depName == null || item.depName == "") {
                item.depName = item.dorDepName;
              }
              if (item.jobName == null || item.jobName == "") {
                item.jobName = item.dorJobName;
              }
            });
            const data = this.formatJson(filterVal, list);
            // export_json_to_excel(tHeader, data, "入住信息");

            export_json_to_excel(tHeader, data, `入住信息&(${this.searchForm.rangTimeIn})`);
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
    parentSearch(){
      this.page.currentPage = 1;
      this.getList(this.page, this.searchForm)
    },
    getList(page, formParams) {
      this.tableLoading = true;
      let params = Object.assign({}, formParams);
      if (isArrayFn(params.rangTimeIn)) {
        params.startTime = params.rangTimeIn[0];
        params.endTime = params.rangTimeIn[1];
        params.rangTimeIn = undefined;
      }
      fetchList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          this.roomInfo,
          params
        )
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
      });
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
     * 搜索回调
     */
    searchSubmit(form) {
      this.page.currentPage = 1;
      this.getList(this.page, form);
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
.top-menu {
  border-bottom: none;
}
</style>
