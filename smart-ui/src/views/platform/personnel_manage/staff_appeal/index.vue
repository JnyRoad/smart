<!--员工申诉管理模板  -->
<template>
  <div class="my-basic-container room">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchform)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchform')"
              plain
            >重置</el-button>
          </div>
        </div>
        <el-form ref="searchform" :inline="true" :model="searchform" size="mini" class="topForm">
          <el-form-item label="反馈问题" prop="appealType">
            <el-select v-model="searchform.appealType" placeholder="请选择">
              <el-option
                v-for="item in servicesArr"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="searchform.status" placeholder="请选择">
              <el-option
                v-for="item in statusArr"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="反馈时间" prop="snapTime" clearable>
            <el-date-picker
              v-model="snapTime"
              type="datetimerange"
              value-format="yyyy-MM-dd HH:mm:ss"
              format="yyyy-MM-dd HH:mm:ss"
              range-separator="至"
              :default-time="['00:00:00', '23:59:59']"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
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
          <!-- <template slot-scope="scope" slot="statusDesc">
            {{scope.row.status==2?'已回复':scope.row.ischange?'已转交待回复':scope.row.statusDesc}}
          </template> -->
          <template slot-scope="scope" slot="ischange">
            {{scope.row.ischange?'已转交':'未转交'}}
          </template>
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-view"
              v-if="scope.row.status === 1"
              @click="handleReply(scope.row,scope.$index)"
            >回复</el-button>

            <el-button
              type="text"
              icon="el-icon-edit"
               v-if="scope.row.status === 2"
              @click="handleReply(scope.row, scope.$index)"
            >查看</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import { fetchList} from './staff_appeal'
import { tableOption } from "@/const/crud/platform/personnel_manage/staff_appeal";
import { validatenum } from "@/util/validate";
import { mapGetters } from "vuex";

const statusArr = [
  { label: "已申诉", value: 1 },
  { label: "已回复", value: 2 }
];

const servicesArr = [
  { label: "人事服务", value: 1 },
  { label: "宿舍服务", value: 2 },
  { label: "车间管理", value: 3 },
];

let meterReadDetailList = [];
for (let i = 1; i < 4; i++) {
  meterReadDetailList.push({
    preMonthNum: "",
    curMonthNum: "",
    categoryId: i
  });
}
export default {
  name: "dorm_mng",
  data() {
    var validateIsNum = (rule, value, callback) => {
      let regName = /^-?\d+$/;
      if (value === 0) {
        callback(new Error("请输入非0整数"));
      }
      if (!regName.test(value)) {
        callback(new Error("请输入整数"));
      } else {
        callback();
      }
    };
    var validateFloorNum = (rule, value, callback) => {
      let regName = /^\d+$/;
      if (value == 0) {
        callback(new Error("请输入大于0的正整数"));
      }

      if (!regName.test(value)) {
        callback(new Error("请输入正整数"));
      } else if (value > 14) {
        callback(new Error("楼层数量最大值为14"));
      } else {
        callback();
      }
    };

    var validateRoomNum = (rule, value, callback) => {
      let regName = /^\d+$/;
      // if(value==0)
      // {
      //    callback(new Error('请输入大于0的正整数'));
      // }

      if (!regName.test(value)) {
        callback(new Error("不能输入小数和负数"));
      } else if (value > 24) {
        callback(new Error("房间数量最大值为24"));
      } else {
        callback();
      }
    };
    return {
      configform: {
        mrId:"",
        roomId: "",
        meterMonth: "",
        meterReadDetailList: meterReadDetailList
      },
      snapTime: [],
      statusArr: statusArr,
      servicesArr: servicesArr,
      setLoading: false,
      isRead: true,
      isStatement: false, //是否已生成明细
      addLoading: false, //是否正在添加
      editLoading: false, //是否正在编辑
      addFormVisible: false, //添加楼层
      addMeterReadFormVisible: false, //抄表弹窗
      addForm: {
        parkId: undefined, //所属园区
        dormitoryId: undefined, //楼栋ID
        floorId: undefined, //楼层ID
        roomId: undefined, //房间号
        meterMonth: undefined //月份
      },
      addRules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "change" }],
        dormitoryId: [
          { required: true, message: "请选择楼栋", trigger: "change" }
        ],
        floorId: [{ required: true, message: "请选择楼层", trigger: "change" }],
        meterMonth: [{ required: true, message: "请选月份", trigger: "change" }]
      },
      searchform: {
        appealType: undefined, //反馈类型
        beginTime: undefined, //开始时间
        endTime: undefined,  //结束时间
        status: undefined //状态
      },
      tableLoading: false,
      tableData: [],
      tableOption: tableOption,

      dormitoryData: [],
      floorData: [],
      roomDate: [],

      addDormitoryData: [],
      addFloorData: [],
      addRoomDate: [],

      defaultProps: {
        label: "parkName",
        value: "id"
      },
      dormiProps: {
        label: "dormitoryName",
        value: "id"
      },
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
    begainTime: function() {
      if (this.snapTime) {
        return this.snapTime[0];
      } else {
        return undefined;
      }
    },
    endTime: function() {
      if (this.snapTime) {
        return this.snapTime[1];
      } else {
        return undefined;
      }
    }
  },
  methods: {
    getType(type) {
      const obj = ["", "热水", "冷水", "电"];
      return obj[type];
    },
    getList(page, params) {
      this.tableLoading = true;
      this.searchform.beginTime = this.begainTime;
      this.searchform.endTime = this.endTime;
      fetchList(
        Object.assign(
          {
            asc: "id",
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
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getList(this.page, this.searchform);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getList(this.page, this.searchform);
    },
    resetAddForm(formName) {
      this.addFormVisible = false;
      this.addLoading = false;
      this.hasStartNum = false; //将起始编号状态恢复默认设置
      this.$refs[formName].resetFields();
    },
    resetEditForm(formName) {
      this.editFormVisible = false;
      this.editLoading = false;
      this.$refs[formName].clearValidate();
    },
    handleReply(row, index) {
      const src = `/platform/personnel_manage/staff_appeal/detail/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          queryPage: this.page,
          queryForm: this.searchForm
        }
      });
    },
    resetMeterReadData(formName) {
      this.addMeterReadFormVisible = false;
      this.setLoading = false;
      this.$refs[formName] ? this.$refs[formName].resetFields() : "";
      this.$refs[formName] ? this.$refs[formName].clearValidate() : "";
    },
    searchSubmit(form) {
      //搜索
      this.page.currentPage = 1;
      this.getList(this.page, form);
    },
    resetFrom(formName) {
      //清空
      this.snapTime = [];
      this.$refs[formName].resetFields();
      this.page.currentPage = 1;
      this.getList(this.page);
    }
  }
};
</script>
