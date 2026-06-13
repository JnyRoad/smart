<!--区域管理，区域管理 ,通关人员 -->
<template>
  <div class="my-basic-container area">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          :option="personOption"
          @size-change="sizeChange"
          @current-change="currentChange"
        ></avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList } from "@/api/platform/area/area";
import { personOption } from "@/const/crud/platform/area/area";
import { mapGetters } from "vuex";

export default {
  name: "area",
  data() {
    return {
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [
        {
          // 'workid':'1001',
          // 'staff_name':'张三',
          // 'bu':'深圳裕同',
          // 'department':'研发部',
          // 'layer':'部长层',
          // 'position':'研发工程师',
        }
      ],
      personOption: personOption
    };
  },
  created: function() {
    this.getList(this.page);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    getList(page, params) {
      // this.tableLoading = true
      // fetchList(Object.assign({
      //   descs: 'create_time',
      //   current: page.currentPage,
      //   size: page.pageSize
      // }, params)).then(response => {
      //   this.insiderData = response.data.data.records
      //   this.page.total = response.data.data.total
      //   this.tableLoading = false
      // })

      this.tableLoading = false;
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page);
    },
    handleDetail(row, index) {
      const src = `/platform/vehicle/staff_vehicle/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {}
      });
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
