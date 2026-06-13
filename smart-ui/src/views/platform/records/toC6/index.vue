<!--日志管理：c6同步照片 -->
<template>
  <div class="my-basic-container staff">
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
          <el-form-item label="工号" prop="empNo">
            <el-input v-model="searchForm.empNo" placeholder="工号" clearable></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="searchForm.name" placeholder="姓名" clearable></el-input>
          </el-form-item>
          <el-form-item label="创建时间" prop="timeRange">
            <el-date-picker
              v-model="searchForm.timeRange"
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
          <template slot-scope="scope" slot="menu">
            <viewer style="display: none;">
              <img
                ref="curImg"
                :src="'data:image/jpeg;base64,/'+scope.row.photo || errorImgPeaple()"
                :onerror="errorImgPeaple()"
              />
            </viewer>
            <el-button type="text" icon="el-icon-view" @click="ckImg(scope.row,scope.$index)">查看照片</el-button>
          </template>
        </avue-crud>
        <!-- <img class="testimg"  :src="testImg" /> -->
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList } from "@/api/platform/records/toC6";
import { fetchMenuTree } from "@/api/platform/limit/index";
import { tableOption } from "@/const/crud/platform/records/toC6";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";

export default {
  name: "toC6",
  data() {
    return {
      exportLoading: false,
      myFiles: null,
      testImg: "",
      importVisible: false,
      selectLoading: false,
      importLoading: false,
      searchForm: {
        //搜索菜单表单
        name: undefined,
        empNo: undefined,
        timeRange: undefined
      },
      hasSelect: false,
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
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),

    startTime: function() {
      if (this.searchForm.timeRange) {
        return this.searchForm.timeRange[0];
      } else {
        return undefined;
      }
    },
    endTime: function() {
      if (this.searchForm.timeRange) {
        return this.searchForm.timeRange[1];
      } else {
        return undefined;
      }
    }
  },
  watch: {},
  methods: {
    ckImg(row, index) {
      this.$refs.curImg.click();
    },
    getList(page, params) {
      this.tableLoading = true;
      params = Object.assign(
        {
          descs: "create_time",
          current: page.currentPage,
          size: page.pageSize,
          startTime: this.startTime,
          endTime: this.endTime
        },
        params
      );

      if (isArrayFn(params.timeRange)) {
        //虽然后台没有用到这个值，但是传参时会报错，所以修改一下格式
        params.timeRange = params.timeRange.join();
      }

      fetchList(params).then(response => {
        this.tableData = response.data.data.records;
        // this.testImg = 'data:image/jpeg;base64,/'+ this.tableData[0].photos;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
      this.tableLoading = false;
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
.testimg {
  width: 100px;
  height: 100px;
  border: 1px solid red;
}
.upload-demo {
  display: inline-block;
  margin-right: 20px;
}
.tips {
  max-width: 500px;
  border: 1px solid red;
}
.num {
  padding: 0 5px;
  font-weight: bold;
}
.red {
  color: red;
}
.importBtn {
  margin-right: 20px;
}
.fileBtn {
  display: none;
}
.importCont {
  line-height: 25px;
  padding-bottom: 30px;
}
.fileTb {
  margin: 10px 0 0 0;
  width: 100%;
  .haveNo {
    color: red;
  }
  td {
    border: 1px solid #e0e0e0;
    text-align: center;
    padding: 0 10px;
    height: 35px;
  }
}
::v-deep .el-scrollbar__wrap {
  overflow-x: auto;
}
</style>
