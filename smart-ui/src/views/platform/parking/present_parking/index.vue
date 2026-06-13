<!--停车场管理，当前车辆 -->
<template>
  <div class="my-basic-container parking">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="text-rigth">
            <span class="pgitem">
              <el-progress
                type="circle"
                :stroke-width="stroke_width"
                :width="progress_width"
                :percentage="curDepotInfo.freeCount|freePerc(curDepotInfo.totalCount)"
                status="exception"
                :show-text="false"
              ></el-progress>剩余车位
              <span>{{ curDepotInfo.freeCount }}</span>
            </span>
            <span class="pgitem">
              <el-progress
                type="circle"
                :stroke-width="stroke_width"
                :width="progress_width"
                :percentage="curDepotInfo.useCount|usePerc(curDepotInfo.totalCount)"
                status="success"
                :show-text="false"
              ></el-progress>已用车位
              <span>{{curDepotInfo.useCount }}</span>
            </span>
            <span class="pgitem">
              <el-progress
                type="circle"
                :stroke-width="stroke_width"
                :width="progress_width"
                :percentage="100"
                :show-text="false"
              ></el-progress>总车位
              <span>{{curDepotInfo.totalCount}}</span>
            </span>
            <el-button type="primary" @click="handelCheck">车位校对</el-button>
            <div class="lot-sel">
              <el-select
                class="button-select"
                v-model="searchForm.parkingId"
                size="mini"
                @change="lotChange"
              >
                <el-option
                  v-for="item in depLots"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                ></el-option>
              </el-select>
            </div>
          </div>
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
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchForm.parkId"></parkSelect>
          </el-form-item>
          <el-form-item label="车牌号" prop="vehiclePlate">
            <el-input v-model="searchForm.vehiclePlate" placeholder="车牌号" clearable></el-input>
          </el-form-item>
          <el-form-item label="车主" prop="driverName">
            <el-input v-model="searchForm.driverName" placeholder="车主" clearable></el-input>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="tableData"
          :table-loading="tableLoading"
          @size-change="sizeChange"
          @current-change="currentChange"
          :option="tableOption"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              @click="handleDetail(scope.row,scope.$index)"
            >查看</el-button>
          </template>
        </avue-crud>
        <el-dialog title="车位校对" class="dialog_form" width="500px" :visible.sync="checkVisible">
          <el-form ref="checkForm" :rules="checkRules" :model="checkForm" label-width="80px">
            <el-form-item label="总车位" prop="totalCount">
              <el-input v-model="checkForm.totalCount" disabled></el-input>
            </el-form-item>
            <el-form-item label="已用车位" prop="useCount">
              <el-input v-model="checkForm.useCount"></el-input>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="primary" @click="checkVisible = false" plain>取 消</el-button>
            <el-button type="primary" @click="checkEdit('checkForm')" :loading="checkLoading">确 定</el-button>
          </div>
        </el-dialog>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  fetchList,
  getObj,
  parking,
  check
} from "@/api/platform/parking/present_parking";
import { tableOption } from "@/const/crud/platform/parking/present_parking";
import { mapGetters } from "vuex";

export default {
  name: "parking",
  data() {
    var validateUseCount = (rule, value, callback) => {
      var useNum = /^(0|[1-9][0-9]*)$/;
      if (!useNum.test(value)) {
        callback(new Error("请输入大于或等于0的正整数"));
      } else if (value > this.curDepotInfo.totalCount) {
        callback(new Error("请输入小于或等于总车位数量的数字"));
      } else {
        callback();
      }
    };
    return {
      searchForm: {
        //搜索菜单表单
        vehiclePlate: "",
        driverName: "",
        AllFlag: 2,
        parkingId: "" //停车场id
      },
      checkLoading: false,
      checkVisible: false, //是否显示车位校对弹框
      parking_space: {
        // 'total_count': 600,
        // 'use_count': 200,
      },
      depLots: [], //所有停车场集合
      curDepotInfo: {}, //当前选中的停车场信息
      checkForm: {
        freeCount: "",
        totalCount: "",
        useCount: "",
        parkingId: "" //停车场id
      },
      checkRules: {
        useCount: [
          { required: true, message: "请输入已用车位", trigger: "blur" },
          { validator: validateUseCount, trigger: "blur" }
        ]
      },
      progress_width: 25,
      stroke_width: 3,
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption
    };
  },
  created() {
    if (this.timeOut) {
      clearTimeout(this.timeOut);
    }
    this.getDepots();
  },
  mounted: function() {},
  filters: {
    usePerc: function(use, total) {
      //已用车位数量图表百分比
      if (total > 0) {
        return (use / total) * 100;
      }
    },
    freePerc: function(free, total) {
      //剩余车位数量图表百分比
      if (total > 0) {
        return (free / total) * 100;
      }
    }
  },
  computed: {
    ...mapGetters(["permissions"]),
    timeOut: {
      set(val) {
        this.$store.commit("SET_PARKINGTIMEOUT", val);
      },
      get() {
        return this.$store.state.parkingTimeout;
      }
    }
  },
  methods: {
    getList(page, params) {
      //获取当前停车列表信息
      this.tableLoading = true;
      fetchList(
        Object.assign(
          {
            descs: "create_time",
            current: page.currentPage,
            size: page.pageSize,
            allFlag: 2,
            parkingId: this.searchForm.parkingId
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
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page, this.searchForm);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page, this.searchForm);
    },
    getDepots() {
      //获取停车场
      parking()
        .then(response => {
          this.depLots = response.data.data;
          // 默认获取选择第一个停车场，根据该停车场id查询对应的当前停车列表，和当前停车场车位数量情况
          this.searchForm.parkingId = this.depLots[0].id;
          this.getList(this.page, this.searchForm);
          this.getCurDepotCount(this.searchForm.parkingId);
          this.getVisitData();
        })
        .catch(error => { console.error(error) });
    },
    handleDetail(row, index) {
      //当前停车 详情
      const src = `/platform/parking/present_parking/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    handelCheck() {
      //校对车位，打开
      this.checkVisible = true;
      this.checkForm = Object.assign({}, this.curDepotInfo);
    },
    checkEdit(formName) {
      //车位校对确定
      let _this = this;
      this.checkForm.parkingId = this.searchForm.parkingId;
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.checkLoading = true;
          check(this.checkForm)
            .then(response => {
              this.checkVisible = false;
              this.checkLoading = false;
              if (this.timeOut) {
                clearTimeout(this.timeOut);
              }
              this.getCurDepotCount(this.checkForm.parkingId);
              this.getVisitData();
            })
            .catch(() => {
              this.checkLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    getCurDepotCount(parkingId) {
      //获取当前停车场车位数量信息
      getObj(parkingId).then(response => {
        if (response.data.data) {
          this.curDepotInfo = response.data.data;
        } else {
          this.curDepotInfo = {};
        }
      });
    },
    getVisitData() {
      let parkingId = this.searchForm.parkingId;
      this.getCurDepotCount(parkingId);
      if (this.$route.path == "/platform/parking/present_parking") {
        let _this = this;
        this.timeOut = setTimeout(() => {
          _this.getVisitData();
        }, 600000);
      } else {
        this.timeOut = "";
      }
    },
    lotChange() {
      //停车场切换
      if (this.timeOut) {
        clearTimeout(this.timeOut);
      }
      this.getVisitData();
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
  .text-rigth {
    text-align: right;
    ::v-deep .el-button {
      margin-left: 30px;
    }
  }
  .lot-sel {
    display: inline-block;
    width: 150px;
    margin-left: 30px;
    ::v-deepinput::-webkit-input-placeholder {
      color: #fff;
    }
    ::v-deepinput:-moz-placeholder {
      color: #fff;
    }
    ::v-deepinput::-moz-placeholder {
      color: #fff;
    }
    ::v-deepinput:-ms-input-placeholder {
      color: #fff;
    }
  }
  .pgitem {
    color: #666;
    font-size: 12px;
    margin-right: 20px;
    span {
      color: #333;
      font-size: 18px;
      padding-left: 8px;
      vertical-align: middle;
      margin-top: -1px;
    }
    .el-progress {
      vertical-align: middle;
    }
  }
}
</style>
