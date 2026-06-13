<!--水表更换记录 -->
<template>
  <div>
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
          <el-form-item label="更换前设备通信地址" prop="beforeAddress">
            <el-input v-model="searchForm.beforeAddress" placeholder="更换前设备通信地址" clearable></el-input>
          </el-form-item>
          <el-form-item label="更换后设备通信地址" prop="afterAddress">
            <el-input v-model="searchForm.afterAddress" placeholder="更换后设备通信地址" clearable></el-input>
          </el-form-item>
          <el-form-item label="更换时间" prop="createTime">
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
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  electricRecordList
} from "../_service";
import { tableOption } from "@/const/crud/platform/device/electricRecord";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
export default {
  name: "electric_record",
  data() {
    return {
      searchForm: {
        //搜索菜单表单
        beforeAddress: undefined,
        afterAddress: undefined,
        createTime: undefined
      },
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
    this.getList();
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
    getList() {
      this.tableLoading = true;
      const obj = {
          current: this.page.currentPage,
          size: this.page.pageSize,
          beforeAddress: this.searchForm.beforeAddress,
          afterAddress: this.searchForm.afterAddress,
          startTime: this.startTime,
          endTime: this.endTime
      }
      electricRecordList(
        obj
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
      this.getList();
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList();
    },
    /**
     * 搜索回调
     */
    searchSubmit() {
      this.page.currentPage = 1;
      this.getList();
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      this.page.currentPage = 1;
      this.$refs[formName].resetFields();
      this.getList();
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
