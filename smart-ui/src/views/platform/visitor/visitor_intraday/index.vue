<!--访客预约，当天预约  -->
<template>
  <div class="my-basic-container visitor-intraday mycard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <el-row type="flex" class="my-basic-inner">
        <el-col :lg="18" :md="16" class="box-outer box-left">
          <div class>
            <p class="box-orange">来访信息</p>
            <el-row :gutter="20">
              <el-col :span="6">
                <dl class="img-info">
                  <dt class="img-outer">
                    <i class="corner cn-top"></i>
                    <i class="corner cn-rit"></i>
                    <i class="corner cn-btm"></i>
                    <i class="corner cn-lft"></i>
                    <div class="img-inner">
                      <viewer>
                        <img
                          :src="visitorInfo.visitorPhoto || errorImgPeaple()"
                          :onerror="errorImgPeaple()"
                        />
                      </viewer>
                    </div>
                  </dt>
                  <dd class="desc-info">人脸底图</dd>
                </dl>
              </el-col>
              <el-col :span="6" v-if="!hasEntourage">
                <dl class="img-info">
                  <dt class="img-outer">
                    <i class="corner cn-top"></i>
                    <i class="corner cn-rit"></i>
                    <i class="corner cn-btm"></i>
                    <i class="corner cn-lft"></i>
                    <div class="img-inner">
                      <viewer>
                        <img
                          :src="visitorInfo.snapPhoto || errorImgPeapleCamera()"
                          :onerror="errorImgPeapleCamera()"
                        />
                      </viewer>
                    </div>
                  </dt>
                  <dd class="desc-info">人脸/车辆抓拍图</dd>
                </dl>
              </el-col>
              <el-col :span="12">
                <table class="dot-list" style="min-width:80%">
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="访客姓名"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{visitorInfo.visitorName}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="身份证号"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{visitorInfo.certNo}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="手机号"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{visitorInfo.visitorPhone}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="车牌号"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{visitorInfo.vehiclePlate}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="来访单位"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{visitorInfo.company}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="到访时间"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{visitorInfo.snapTime}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="来访事由"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{visitorInfo.causeDesc}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="来访区域"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{visitorInfo.areaName}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="来访园区"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{visitorInfo.parkName}}</td>
                  </tr>
                </table>
              </el-col>
            </el-row>
          </div>
          <avue-crud
            ref="crud"
            :page="page"
            :data="tableData"
            :table-loading="tableLoading"
            :option="personOption"
            @size-change="sizeChange"
            @current-change="currentChange"
          ></avue-crud>
        </el-col>
        <el-col :lg="6" :md="8">
          <div class="box-outer box-top" v-if="hasEntourage">
            <p class="box-orange">随行人员</p>
            <el-row :gutter="10">
              <el-col :span="12">
                <dl class="img-info">
                  <dt class="img-outer">
                    <i class="corner cn-top"></i>
                    <i class="corner cn-rit"></i>
                    <i class="corner cn-btm"></i>
                    <i class="corner cn-lft"></i>
                    <div class="img-inner">
                      <viewer>
                        <img
                          v-if="snapTodayFellowVisitorList.length>0"
                          :src="snapTodayFellowVisitorList[0].fellowPhoto"
                          :onerror="errorImgPeaple()"
                        />
                      </viewer>
                    </div>
                  </dt>
                  <dd class="desc-info">人脸底图</dd>
                </dl>
              </el-col>
              <el-col :span="12">
                <dl class="img-info">
                  <dt class="img-outer">
                    <i class="corner cn-top"></i>
                    <i class="corner cn-rit"></i>
                    <i class="corner cn-btm"></i>
                    <i class="corner cn-lft"></i>
                    <div class="img-inner">
                      <viewer>
                        <img
                          v-if="snapTodayFellowVisitorList.length>0"
                          :src="snapTodayFellowVisitorList[0].snapPhoto"
                          :onerror="errorImgPeapleCamera()"
                        />
                      </viewer>
                    </div>
                  </dt>
                  <dd class="desc-info">人脸抓拍图</dd>
                </dl>
              </el-col>
            </el-row>
          </div>
          <div class="box-outer">
            <p class="box-orange">被访人信息</p>
            <table class="lit-table">
              <tr>
                <td>姓名</td>
                <td>{{visitorInfo.receptionistName}}</td>
              </tr>
              <tr>
                <td>电话</td>
                <td>{{visitorInfo.receptionistPhone}}</td>
              </tr>
              <tr>
                <td>部门</td>
                <td>{{visitorInfo.receptionistDept}}</td>
              </tr>
            </table>
          </div>
        </el-col>
      </el-row>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList } from "@/api/platform/visitor/visitor_intraday";
import { searchNewSnapVisitor } from "@/api/platform/visitor/visitor_intraday";
import { personOption } from "@/const/crud/platform/visitor/visitor_intraday";
import { mapGetters } from "vuex";
import { constants } from "crypto";

export default {
  name: "visitor",
  data() {
    return {
      hasEntourage: true, //是否有随行人员,false:没有，true:有
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 10 // 每页显示多少条
      },
      snapTodayFellowVisitorList: [],
      visitorInfo: {
        // 'vs_name':'张三',
        // 'tel': '13525145212',
        // 'carid': '渝A123456',
        // 'vs_firm':'时代云英科技有限公司',
        // 'time1':'2019-01-01 5:20:20',
        // 'reason':'测试来访事由来访事由来访',
        // 'img':'/img/placeholder_people.png',
        // 'snapTodayFellowVisitorList':[
        // ]
      },

      tableLoading: false,
      tableData: [],
      personOption: personOption
    };
  },
  created() {
    // this.getList(this.page);
    // this.getSearchNewSnapVisitor();
    if (this.timeOut) {
      clearTimeout(this.timeOut);
    }
    this.getVisitData();
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),
    timeOut: {
      set(val) {
        this.$store.commit("SET_VISITERTIMEOUT", val);
      },
      get() {
        return this.$store.state.visiterTimeout;
      }
    }
  },
  watch: {
    visitorInfo(val) {
      if (val != null || val != "" || val != undefined) {
        if (this.tableData.length > 0) {
          for (let i = 0; i < this.tableData.length; i++) {
            //判断id是否一样，一样则删除数组的id
            if (this.tableData[i].visitorId == val.id) {
              this.tableData.splice(i, 1);
              //  this.$set(this.tableData,i,null);
            }
          }
          this.tableData.sort;
          this.tableData.unshift(val);
          //判断数组的长度是否大于一页显示条数
          if (this.tableData.length >= this.page.pageSize) {
            this.tableData.pop();
          }
        }
      }
    }
  },
  methods: {
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page);
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
        this.tableData = response.data.data.records;
        this.page.total = response.data.data.total;
        this.tableLoading = false;
      });

      this.tableLoading = false;
    },
    getSearchNewSnapVisitor(params) {
      this.tableLoading = true;
      searchNewSnapVisitor(
        Object.assign(
          {
            descs: "create_time"
          },
          params
        )
      ).then(response => {
        this.visitorInfo = response.data.data;
        if (
          this.snapTodayFellowVisitorList != null &&
          this.snapTodayFellowVisitorList.length > 0
        ) {
          this.hasEntourage = true;
        } else {
          this.hasEntourage = false;
        }
        this.tableLoading = false;
      });
      this.tableLoading = false;
    },
    getVisitData() {
      //定时任务
      // 这里是一个http的异步请求
      this.getList(this.page);
      this.getSearchNewSnapVisitor();
      if (this.$route.path == "/platform/visitor/visitor_intraday") {
        let _this = this;
        this.timeOut = setTimeout(() => {
          _this.getVisitData();
        }, 300000);
      } else {
        this.timeOut = "";
      }
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
