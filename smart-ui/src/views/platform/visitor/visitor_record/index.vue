<!--访客预约，预约记录  -->
<template>
  <div class="my-basic-container visitor">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu clear">
          <div class="top-right">
            <el-button type="primary" @click="visitorRecord()">核实访客记录</el-button>
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchForm)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchForm')"
              plain
            >清空</el-button>
            <el-tooltip class="item" effect="light" content="最多导出1000条" placement="bottom">
              <el-button
                type="primary"
                :loading="exportLoading"
                @click="export2Excel"
                icon="icon-yutong-download"
              >导出表格</el-button>
            </el-tooltip>
          </div>
        </div>
        <tce-Search-bar>
          <el-form ref="searchForm" :inline="true" :model="searchForm" class="topForm" size="mini">
            <div class="form-outer">
              <el-form-item label="访客姓名" prop="visitorName">
                <el-input v-model="searchForm.visitorName" placeholder="访客姓名" clearable></el-input>
              </el-form-item>
              <el-form-item label="手机号" prop="visitorPhone">
                <el-input v-model="searchForm.visitorPhone" placeholder="手机号" clearable></el-input>
              </el-form-item>
              <el-form-item label="是否有车" prop="isVehicle">
                <el-select v-model="searchForm.isVehicle" placeholder="所有" clearable>
                  <el-option label="有车" :value="1"></el-option>
                  <el-option label="无车" :value="0"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="车牌号" v-if="hasCar" prop="vehiclePlate">
                <el-input v-model="searchForm.vehiclePlate" clearable></el-input>
              </el-form-item>
              <el-form-item label="所属单位" prop="company">
                <el-input v-model="searchForm.company" clearable placeholder="所属单位"></el-input>
              </el-form-item>
              <el-form-item label="来访事由" prop="cause">
                <el-select v-model="searchForm.cause" placeholder="来访事由" clearable>
                  <el-option
                    v-for="item in causes"
                    :key="item.code"
                    :label="item.label"
                    :value="item.code"
                  ></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="被访人姓名" prop="receptionistName">
                <el-input v-model="searchForm.receptionistName" clearable placeholder="被访人姓名"></el-input>
              </el-form-item>
              <el-form-item label="所属园区/BU/部门" prop="depIds">
                <el-cascader
                  expand-trigger="hover"
                  :options="options"
                  :show-all-levels="false"
                  :change-on-select="true"
                  v-model="depIds"
                  clearable
                ></el-cascader>
              </el-form-item>
              <el-form-item label="来访状态" prop="status">
                <el-select v-model="searchForm.status" placeholder="请来访状态" clearable>
                  <el-option label="已通过" value="0"></el-option>
                  <el-option label="已拒绝" value="1"></el-option>
                  <el-option label="待审批" value="2"></el-option>
                  <el-option label="已到达" value="3"></el-option>
                  <el-option label="超时未到" value="4"></el-option>
                  <el-option label="已离开" value="5"></el-option>
                  <el-option label="预约超时" value="6"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="预约来访时间" prop="timeRange">
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
            </div>
          </el-form>
        </tce-Search-bar>
        <div v-loading="loading" element-loading-text="正在查询，请稍等！">
          <template v-if="hasData">
            <div class="vs-list clear" style="margin-top: 20px">
              <template v-for="(item, key, index) in visitor ">
                <div class="visitor-card" :key="item.id">
                  <div class="vs-inner" @click="handleDetail(item, index)">
                    <div class="vs-status" :class="item.status| visitorStatusClassFormat">
                      <p>{{item.status | visitorStatusFormat}}</p>
                    </div>
                    <div class="vs-info clear">
                      <div class="vs-left">
                        <img
                          :src="item.visitorPhoto || errorImgPeaple()"
                          :onerror="errorImgPeaple()"
                        />
                      </div>
                      <div class="vs-right">
                        <div class="vs-cell">
                          <p>
                            <i class="vs-name"></i>
                            {{item.visitorName}}
                          </p>
                          <p>
                            <i class="vs-phone"></i>
                            {{item.visitorPhone}}
                          </p>
                          <p v-if="item.vehiclePlate!==null && item.vehiclePlate!==''">
                            <i class="vs-vehicle-license"></i>
                            {{item.vehiclePlate}}
                          </p>
                          <p>
                            <i class="vs-firm"></i>
                            {{item.company}}
                          </p>
                          <p>
                            <i class="vs-firm"></i>
                            {{item.parkName}}
                          </p>
                          <p class="btns" @click.stop="test">
                            <!--status 审批通过0  超时未到4 两种状态下才可以重新下发 -->
                            <el-button  v-if="item.status===0 || item.status===4" type="text" size="mini" @click.stop="reSend(item)">重新下发 </el-button>
                            <el-button  v-else type="text" size="mini" @click.stop="reSend(item)" disabled>重新下发 </el-button>
                            <!-- hasAuth: 0 无权限  1有权限 -->
                            <el-button type="text" size="mini" @click.stop="delAuth(item)" :disabled="item.hasAuth!=1">删除通行权限 </el-button>
                          </p>
                        </div>
                      </div>
                    </div>
                    <div class="vs-btm">
                      <div class="vs-time">
                        <p>预约来访时间</p>
                        <p>{{item.startTime}}</p>
                      </div>
                      <div class="vs-time">
                        <p>预约离开时间</p>
                        <p>{{item.endTime}}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
            </div>
            <div class="page-outer">
              <el-pagination
                background
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="page.currentPage"
                :page-sizes="[8, 16, 24, 32, 40]"
                :page-size="page.pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="page.total"
              ></el-pagination>
            </div>
          </template>
          <div v-else class="noData">
            <i></i>
            暂无数据！
          </div>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/platform/visitor/visitor_record" as *;
</style>

<script>
import { fetchList, delAuth, reSend } from "@/api/platform/visitor/visitor_record";
import { getCompTree } from "@/api/platform/_publicService";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";

export default {
  name: "visitor",
  data() {
    return {
      loading: true,
      exportLoading: false,
      causes: [
        //来访事由
        { code: 1, label: "业务会谈" },
        { code: 2, label: "面试" },
        { code: 3, label: "拜访" },
        { code: 5, label: "园区驻厂" },
        { code: 6, label: "入职" },
        { code: 4, label: "其他" },
        { code: 7, label: "家属住宿" },
      ],
      tabOption: {
        column: [
          {
            label: "有车",
            prop: "withCar"
          },
          {
            label: "无车",
            prop: "noCar"
          }
        ]
      },
      default_img: "img/placeholder_people.png",
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 8 // 每页显示多少
      },
      searchForm: {
        visitorName: undefined,
        visitorPhone: undefined,
        vehiclePlate: undefined,
        company: undefined,
        status: undefined,
        isVehicle: undefined,
        timeRange: undefined,
        compId: undefined,
        depId: undefined
      },
      depIds: [],
      options: [],
      visitor: []
    };
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
  created() {
    this.$nextTick(() => {
      if (this.$route.query.queryForm != undefined) {
        let queryPage = this.$route.query.queryPage;
        let queryForm = this.$route.query.queryForm;
        if (queryPage && queryPage.constructor === Object) {
          this.page = Object.assign(queryPage, {});
        }
        if (queryForm && queryForm.constructor === Object) {
          this.searchForm = Object.assign(queryForm, {});
        }
      }
      this.getList(this.page, this.searchForm);
    });
    getCompTree().then(response => {
      this.options = response.data.data;
    });
    // this.getList(this.page,this.searchForm);
    // 页面加载的时候会走tabChange事件，tabChange事件里有执行this.getList(this.page,this.searchForm);
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
    },
    hasData: function() {
      if (this.visitor) {
        if (this.visitor.length > 0) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    },
    hasCar: function() {
      if (this.searchForm.isVehicle == 1) {
        return true;
      } else {
        return false;
      }
    }
  },
  methods: {
    test(){},
    async reSend(item){
      const res = await reSend({id: item.id})
      if(res.data.code===0){
        this.$message({
          message: '重新下发成功',
          type: 'success'
        })
      }
    },
    async delAuth(item){
      const res = await delAuth({id: item.id})
      if(res.data.code===0){
        this.$message({
          message: '删除权限成功',
          type: 'success'
        })
      }
    },
    getList(page, params) {
      this.loading = true;
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

      fetchList(params)
        .then(response => {
          this.visitor = response.data.data.records;
          this.page.total = response.data.data.total;
          this.loading = false;
        })
        .catch(err => { console.error(err) });
    },
    handleDetail(item, index) {
      const src = `/platform/visitor/visitor_record/detail/${item.id}`;
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    //导出
    export2Excel() {
      require.ensure([], () => {
        this.exportLoading = true;
        const { export_json_to_excel } = require("@/vendor/Export2Excel");
        const tHeader = [
          "访客姓名",
          "访客手机号",
          "车牌号",
          "所属单位",
          "来访状态",
          "预约来访时间",
          "预约离开时间",
          "被访人工号",
          "被访人姓名",
          "被访人手机",
          "被访人BU",
          "被访人部门",
          "被访人岗位",
          "健康码",
          "行程码",
        ];
        const filterVal = [
          "visitorName",
          "visitorPhone",
          "vehiclePlate",
          "company",
          "status",
          "startTime",
          "endTime",
          "receptionistBadge",
          "receptionistName",
          "receptionistPhone",
          "compName",
          "depName",
          "jobName",
          "healthcode",
          "tripCode"
        ];

        let params = Object.assign(
          {
            descs: "create_time",
            current: 1,
            size: 10000,
            startTime: this.startTime,
            endTime: this.endTime
          },
          this.searchForm
        );

        if (isArrayFn(params.timeRange)) {
          //虽然后台没有用到这个值，但是传参时会报错，所以修改一下格式
          params.timeRange = params.timeRange.join();
        }

        fetchList(params)
          .then(response => {
            const list = response.data.data.records;
            list.forEach(function(item) {
              if (item.status == 0) {
                item.status = "审批通过";
              } else if (item.status == 1) {
                item.status = "已拒绝";
              } else if (item.status == 2) {
                item.status = "待审批";
              } else if (item.status == 3) {
                item.status = "已到达";
              } else if (item.status == 4) {
                item.status = "超时未到";
              } else if (item.status == 5) {
                item.status = "已离开";
              } else if (item.status == 6) {
                item.status = "预约超时";
              }
            });
            const data = this.formatJson(filterVal, list);
            export_json_to_excel(tHeader, data, `预约记录&(${this.searchForm.timeRange})`);
            this.exportLoading = false;
          })
          .catch(err => {
            this.exportLoading = false;
          });
      });
    },
    // 核实访客记录
    visitorRecord(){
      const src = `/#/visitor/qrCode`
      window.open(src, '_blank')
    },
    //导出相关
    formatJson(filterVal, jsonData) {
      return jsonData.map(v => filterVal.map(j => v[j]));
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
        this.depIds = [];
        this.getList(this.page, this.searchForm);
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
.topForm ::v-deep {
  .el-form-item__label {
    width: 130px;
  }
}
.btns{
  display: flex;
  justify-content: space-between;
  padding-bottom: 10px;
}
</style>
