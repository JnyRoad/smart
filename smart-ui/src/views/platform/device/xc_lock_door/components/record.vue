<template>
  <div class="my-basic-container device">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button type="primary" icon="el-icon-delete" @click="resetFrom('searchForm')" plain>清空</el-button>
            <el-button type="primary" icon="el-icon-download" @click="download" plain>导出excel</el-button>
          </div>
        </div>
        <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
          <el-form-item label="下发时间" prop="downTime" clearable>
            <el-date-picker
              v-model="searchForm.downTime"
              type="datetimerange"
              value-format="yyyy-MM-dd HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
              range-separator="-"
              start-placeholder="起始日期"
              end-placeholder="截止日期"
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
  recordList
} from "../_service";
export default {
  components: {},
  data() {
    return {
      deviceId: null,
      searchForm: {
        downTime: [],
        startTime: '',
        endTime: ''
      },
      tableLoading: false,
      tableData: [],
      tableOption: {
        border: false,
        index: true,
        indexLabel: '序号',
        stripe: true,
        menu: false,
        menuAlign: 'center',
        menuWidth: 150,
        labelWidth: 100,
        align: 'center',
        refreshBtn: false,
        columnBtn: false,
        searchBtn: false,
        showClomnuBtn: false,
        searchSize: 'mini',
        dialogWidth: '600px',
        addBtn: false,
        editBtn: false,
        delBtn: false,
        viewBtn: false,
        column: [
          {
            label: '姓名',
            prop: 'personName'
          },
          {
            label: '下发时间',
            prop: 'createTime'
          }
        ]
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      }
    }
  },
  created: function () {
    this.deviceId = this.$route.params.id;
    this.getList(this.page)
  },
  mounted: function () {},
  computed: {},
  methods: {
    getList(page, params) {
      this.tableLoading = true;
      const obj = {
          current: page.currentPage,
          size: page.pageSize,
          deviceId: this.deviceId,
          startTime: this.searchForm.startTime,
          endTime: this.searchForm.endTime
      }
      recordList(
        Object.assign(obj),params
      ).then(response => {
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });
    },
    //导出表格
    async download() {
      const _this = this
      const obj = {
        current: 1,
        size: 99999,
        deviceId: this.deviceId,
        startTime: this.searchForm.startTime,
        endTime: this.searchForm.endTime
      }
      require.ensure([], () => {
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "姓名",
          "下发时间",
        ];
        const filterVal = [
          "personName",
          "createTime",
        ];
        recordList(obj)
          .then(response => {
            const list = response.data.data.records
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, "门锁下发记录");
          })
          .catch(err => { console.error(err) });
      });
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
    },
    sizeChange(val) {
      this.page.currentPage = 1
      this.page.pageSize = val
      this.getList(this.page, this.searchForm)
    },
    currentChange(val) {
      this.page.currentPage = val
      this.getList(this.page, this.searchForm)
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      if (this.searchForm.downTime.length == 2) {
        form.startTime = this.searchForm.downTime[0]
        form.endTime = this.searchForm.downTime[1]
      }
      this.page.currentPage = 1
      this.getList(this.page, form)
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields()
        this.searchForm.startTime = null
        this.searchForm.endTime = null
        this.page.currentPage = 1
        this.getList(this.page)
      }
    }
  }
}
</script>