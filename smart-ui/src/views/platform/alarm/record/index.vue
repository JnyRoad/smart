<!--安防中心，警报记录  -->
<template>
  <div class="my-basic-container alarm_record">
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
        <div class="form-outer">
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <el-form-item label="选择地点" prop="areaIdArray">
              <el-cascader
                :options="options"
                :change-on-select="true"
                v-model="searchForm.areaIdArray"
              ></el-cascader>
            </el-form-item>
            <el-form-item label="时间" prop="alarmTime" clearable>
              <el-date-picker
                v-model="searchForm.alarmTime"
                type="datetimerange"
                value-format="yyyy-MM-dd"
                format="yyyy-MM-dd"
                range-separator="-"
                start-placeholder
                end-placeholder
              ></el-date-picker>
            </el-form-item>
          </el-form>
        </div>
        <div class="alarm-list" id="alarmImages">
          <template v-for="item in alarm ">
            <div class="alarm-card" :key="item.id">
              <div class="al-img">
                <img
                  :src="item.snapId"
                  :data-original="`${item.snapId}`"
                  :onerror="errorImgPeaple()"
                />
              </div>
              <p class="c-info">{{item.alarmName}}</p>
              <p class="a-info">
                <span class="a-lbl">警报时间：</span>
                <span class="a-val">{{item.alarmTime}}</span>
              </p>
              <p class="a-info">
                <span class="a-lbl">警报地点：</span>
                <span class="a-val">{{item.areaName}}</span>
              </p>
              <!-- 目前阶段警报名称就是警报类型，暂时隐藏 -->
              <!-- <p class="a-info">
                <span class="a-lbl">警报类型：</span>
                <span class="a-val">{{item.alarmName}}</span>
              </p>-->
            </div>
          </template>
        </div>
        <div v-if="!hasData" class="noData">
          <i></i>
          暂无数据！
        </div>
        <div class="page-outer" v-show="hasData">
          <el-pagination
            background
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="page.currentPage"
            :page-sizes="[10, 20, 300, 40]"
            :page-size="page.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="page.total"
          ></el-pagination>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/alarm/record" as *;
</style>

<script>
import { fetchList, tree } from "@/api/platform/alarm/record";
import { mapGetters } from "vuex";
import Viewer from "viewerjs";
import "viewerjs/dist/viewer.css";

export default {
  name: "alarm",
  data() {
    return {
      page: {
        total: null, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      searchForm: {
        areaIdArray: [],
        alarmTime: []
      },
      alarm: [],
      options: [],
      viewer: null
    };
  },
  created() {
    this.getList(this.page);
    tree().then(response => {
      this.options = response.data.data;
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),
    hasData: function() {
      if (this.alarm) {
        if (this.alarm.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }
  },
  methods: {
    initImageTools() {
      //初始化 viewerjs
      var ViewerDom = document.getElementById("alarmImages");

      if (!this.viewer) {
        this.viewer = new Viewer(ViewerDom, {
          url: "data-original",
          ready() {
            // 2 methods are available here: "show" and "destroy".
          },
          shown() {
            // 9 methods are available here: "hide", "view", "prev", "next", "play", "stop", "full", "exit" and "destroy".
          },
          viewed() {
            // All methods are available here except "show".
          }
        });
      }
    },
    getList(page, params) {
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            descs: "create_time",
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then(response => {
        this.alarm = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;

        //重点：要在图片已经请求到再调用！！
        // this.$nextTick(() => {
        //   console.log(1111111111);
        //     this.initImageTools();
        // });
      });
    },

    // 目前详情页面展示的信息和列表一样，暂时隐藏
    handleDetail(item, index) {
      const src = `/platform/alarm/record/detail/${item.id}`;
      this.$router.push({
        path: src,
        query: {}
      });
    },
    /**
     * 搜索回调
     */
    searchSubmit(form) {
      this.getList(this.page, form);
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      if (this.$refs[formName] != undefined) {
        this.$refs[formName].resetFields();
      }
    },
    handleSizeChange(val) {
      this.page.pageSize = val;
      this.getList(this.page, this.searchForm);
    },
    handleCurrentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page, this.searchForm);
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
