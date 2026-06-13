<!--基础信息：短信发送记录 -->
<template>
  <div class="my-basic-container note_record">
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
          <el-form-item label="手机号码" prop="phoneNo">
            <el-input v-model="searchForm.phoneNo" placeholder="手机号码" clearable></el-input>
          </el-form-item>
          <el-form-item label="短信模板" prop="tempId">
            <el-select v-model="searchForm.tempId" placeholder="短信模板" clearable>
              <el-option
                v-for="item in templates"
                :key="item.id"
                :label="item.tempName"
                :value="item.id"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="发送状态" prop="msgState">
            <el-select v-model="searchForm.msgState" placeholder="发送状态" clearable>
              <el-option
                v-for="item in states"
                :key="item.stateId"
                :label="item.state"
                :value="item.stateId"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="发送时间" prop="createTime">
            <el-date-picker
              v-model="searchForm.createTime"
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
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="tableOption"
          @size-change="sizeChange"
          @current-change="currentChange"
        >
          <template slot-scope="scope" slot="msgStateName">
            <span>{{scope.row.msgStateName}}</span>
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index)"
            >详情</el-button>
          </template>
        </avue-crud>
        <!-- 短信详情 -->
        <el-dialog title="短信详情" class="dialog_form" width="740px" :visible.sync="noteVisible">
          <div class="linkdv">
            <table class="note_detail">
              <tr>
                <td>短信模板</td>
                <td>{{ detailObj.tempName }}</td>
              </tr>
              <tr>
                <td>发送手机号码</td>
                <td>{{ detailObj.msgObject }}</td>
              </tr>
              <tr>
                <td>发送时间</td>
                <td>{{ detailObj.createTime }}</td>
              </tr>
              <tr>
                <td>发送内容</td>
                <td>{{ detailObj.msgContent }}</td>
              </tr>
              <tr>
                <td>发送状态</td>
                <td>{{ detailObj.msgStateName }}</td>
              </tr>
              <tr>
                <td>备注</td>
                <td>{{ detailObj.msgDesc }}</td>
              </tr>
            </table>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList,
  getById,
  getAllTemplate,
  getAllState
} from "@/api/platform/basic/note_record";
import { tableOption } from "@/const/crud/platform/basic/note_record";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
export default {
  name: "salary_sign",
  data() {
    return {
      noteVisible: false,
      searchForm: {
        //搜索菜单表单
        phoneNo: undefined,
        tempId: undefined, //模板id
        msgState: undefined,
        createTime: undefined
      },
      templates: [],
      states: [],
      detailObj: {},
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    };
  },
  created() {
    this.getList(this.page);
    this.getAllTemplate();
    this.getAllState();
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),
    startTime: function() {
      if (this.searchForm.createTime) {
        return this.searchForm.createTime[0];
      } else {
        return undefined;
      }
    },
    endTime: function() {
      if (this.searchForm.createTime) {
        return this.searchForm.createTime[1];
      } else {
        return undefined;
      }
    }
  },
  methods: {
    getList(page, params) {
      this.tableLoading = true;

      params = Object.assign(
        {
          startTime: this.startTime,
          endTime: this.endTime
        },
        params
      );

      if (isArrayFn(params.createTime)) {
        //日期数组
        params.createTime = params.createTime.join();
      }

      fetchList(
        {
          current: page.currentPage,
          size: page.pageSize
        },
        params
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });

      this.tableLoading = false;
    },
    getAllTemplate() {
      //所有模板
      getAllTemplate().then(response => {
        this.templates = response.data.data;
      });
    },
    getAllState() {
      //所有状态
      getAllState().then(response => {
        this.states = response.data.data;
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
    handleDetail(row, index) {
      // this.detailObj = row;
      getById(row.id).then(response => {
        this.detailObj = response.data.data;
      });
      this.noteVisible = true;
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.page.currentPage = 1;
      this.getList(this.page, this.searchForm);
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      this.page.currentPage = 1;
      this.$refs[formName].resetFields();
      this.getList(this.page);
    }
  }
};
</script>

<style lang="scss" scoped>
::v-deep .el-scrollbar__wrap {
  overflow-x: auto;
}
.note_record {
  .linkdv {
    padding-bottom: 30px;
  }
  .note_detail {
    width: 100%;
    border: 1px solid #e6ebf0;
    td {
      padding: 8px 10px 8px 20px;
      line-height: 25px;
    }
    td:first-child {
      width: 150px;
      text-align: center;
      border-right: 1px solid #e6ebf0;
    }
    tr:nth-child(2n + 1) td {
      background: #f5f7fa;
    }
  }
}
</style>
