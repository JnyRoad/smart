<!--宿舍管理，床位管理， 入住  -->
<template>
  <div class="my-basic-container check_in">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu">
          <el-button type="primary" icon="el-icon-back" @click="back" plain>返回</el-button>
          <div class="top-right">
            <el-button type="primary" icon="el-icon-search" @click="searchSubmit(searchform)">搜索</el-button>
            <el-button
              type="primary"
              icon="el-icon-delete"
              @click="resetFrom('searchform')"
              plain
            >清空</el-button>
          </div>
        </div>
        <el-form ref="searchform" :inline="true" :model="searchform" class="topForm" size="mini">
          <el-form-item label="工号" prop="badge">
            <el-input
              v-model="searchform.badge"
              placeholder="工号"
              @keyup.enter.native="searchSubmit(searchform)"
            ></el-input>
          </el-form-item>
          <el-form-item label="姓名" prop="name">
            <el-input v-model="searchform.name" placeholder="姓名"></el-input>
          </el-form-item>
          <el-form-item label="所属园区" prop="parkId">
            <parkSelect v-model="searchform.parkId" @doChange="parkChange"></parkSelect>
          </el-form-item>
          <el-form-item label="所属BU" prop="compId">
            <buSelect v-model="searchform.compId" :parkId="searchform.parkId" @doChange="buChange"></buSelect>
          </el-form-item>
          <el-form-item label="所属部门" prop="depId">
            <deptSelect v-model="searchform.depId" :compId="searchform.compId"></deptSelect>
          </el-form-item>
        </el-form>
        <avue-crud
          ref="crud"
          :page="page"
          :data="staffs"
          @size-change="sizeChange"
          @current-change="currentChange"
          :option="tableOption"
        >
          <template slot-scope="scope" slot="menu">
            <el-button
              type="text"
              icon="el-icon-plus"
              @click="selectStaff(scope.row,scope.$index)"
            >确认添加</el-button>
          </template>
        </avue-crud>
      </section>
    </el-scrollbar>
    <!-- 办理入住确认 -->
    <el-dialog
      title
      class="dialog_form"
      label-position="top"
      width="450px"
      :visible.sync="checkInVisible"
    >
      <div class="tips">
        <template v-if="checkInfo">
          <div class="f1" style="margin-bottom: 20px; font-size: 18px;">{{ currentStaff.badge }} - {{ currentStaff.name }} ，入住检查</div>
          <div><span class="label">是否已申请过外宿补贴：</span><span class="f1">{{checkInfo.isOutDormitory===0?'否':'是'}}</span></div>
          <div class="tip_row">
            <span class="label">是否拥有多套宿舍：</span>
            <span class="f1">
              <template v-if="!checkInfo.rooms || checkInfo.rooms.length===0">
                否
              </template>
              <template v-else>是
                <div v-for="(item, index) in checkInfo.rooms" :key="index" style="margin-top: 5px;">
                  {{item}}
                </div>
              </template>
            </span>
          </div>
        </template>
        <template v-else>
          <div class="f1" style="margin-bottom: 20px; font-size: 18px;">{{ currentStaff.badge }} - {{ currentStaff.name }} ，入住检查失败！</div>
        </template>
      </div>
      <el-form ref="checkInForm" :model="checkInForm" :rules="checkInRules">
        <el-form-item label prop="createTime">
          <el-date-picker
            v-model="checkInForm.createTime"
            type="date"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="请选择入住日期"
          ></el-date-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="resetcheckInForm('checkInForm')" plain>取 消</el-button>
        <el-button
          type="primary"
          @click="handleCheckIn('checkInForm')"
          :loading="checkInLoading"
        >确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/dormitory/bed_mng" as *;
</style>

<script>
import {
  fetchStaffList,
  callOwanceDetails,
  addDormitoryStaff
} from "@/api/platform/dormitory/bed_mng";
import { tableOption } from "@/const/crud/platform/dormitory/check_in";
import { mapGetters } from "vuex";
import { dateFormat } from "@/util/date";
const dateNow = dateFormat(new Date());
export default {
  name: "check_in",
  data() {
    return {
      checkInVisible: false,
      checkInLoading: false,
      checkInForm: {
        createTime: dateNow
      },
      currentStaff: "",
      searchform: {
        parkId: null,
        depId: "",
        jcheId: null,
        sex: 0
      },
      checkInfo: null, //入住检查结果
      checkInRules: {
        createTime: [
          { required: true, message: "请选择入住日期", trigger: "change" }
        ]
      },
      page: {
        total: 0, // 总页数
        currentPage: 1, // 当前页数
        pageSize: 20 // 每页显示多少条
      },
      tableOption: tableOption,
      theStaff: "", //当前选中的员工
      thsStaffBadge: "", //当前员工号
      staffs: [],
      bedId: null,
      rules: {
        name: [{ required: true, message: "请输入策略名称", trigger: "blur" }]
      }
    };
  },
  created() {
    this.bedId = this.$route.query.bedId;
    this.searchform.sex = this.$route.query.roomSex;
    this.searchform.jcheId = this.$route.query.jcheId;
    this.getStaffList(this.page, this.searchform);
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    parkChange() {
      this.searchform.compId = undefined;
      this.searchform.depId = undefined;
    },
    buChange() {
      this.searchform.depId = undefined;
    },
    getStaffList(page, params) {
      fetchStaffList(
        Object.assign(
          {
            current: page.currentPage,
            size: page.pageSize
          },
          params
        )
      ).then(response => {
        this.staffs = response.data.data.records;
        this.page.total = response.data.data.total;
      });
    },
    selectStaff(row, index) {
      var _this = this;
      this.theStaff = row.id;
      this.thsStaffBadge = row.badge;
      this.currentStaff = row;
      this.$refs.crud.setCurrentRow(row);

      callOwanceDetails({ staffBadge: this.thsStaffBadge }).then(res => {
        if (res.data.code === 0) {
          this.checkInfo = res.data.data
        } else {
          // _this.$message.error('入住检查失败！');
        }
      }).catch(err => {
        console.error(err)
      }).finally(()=>{
        _this.checkInVisible = true;
      });
    },
    back() {
      if(this.$route.query.fromNew){
        const src = `/platform/dormitory/new_room`;
        this.$router.push({
          path: src,
          query: {
          }
        });
      }else{
        const src = `/platform/dormitory/bed_mng`;
        this.$router.push({
          path: src,
          query: {
            queryDefaultExpandedKey: this.$route.query.queryDefaultExpandedKey,
            params: this.$route.query.params,
            queryPage: this.$route.query.queryPage,
            queryForm: this.$route.query.queryForm
          }
        });
      }
    },
    handleCheckIn(formName) {
      //确认添加
      let _this = this;
      var add = {
        bedId: this.bedId,
        staffId: this.theStaff,
        createTime: this.checkInForm.createTime
      };
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.checkInLoading = true;
          addDormitoryStaff(add)
            .then(response => {
              this.checkInVisible = false;
              this.checkInLoading = false;
              this.back();
            })
            .catch(err => {
              this.checkInLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    sizeChange(val) {
      this.page.currentPage = 1;
      this.page.pageSize = val;
      this.getStaffList(this.page, this.searchform);
    },
    currentChange(val) {
      this.page.currentPage = val;
      this.getStaffList(this.page, this.searchform);
    },
    searchSubmit(form) {
      //搜索提交
      this.getStaffList(this.page, form);
      this.theStaff = "";
    },
    resetcheckInForm(formName) {
      this.$refs[formName].resetFields();
      this.checkInVisible = false;
    },
    /**
     * 清空搜索
     */
    resetFrom(formName) {
      this.$refs[formName].resetFields();
      this.getStaffList(this.page);
    }
  }
};
</script>

<style lang="scss" scoped>
.tips {
  margin-bottom: 20px;
  .f1{
    color: #ed6d00;
  }
  div{
    margin-bottom: 8px;
  }
  .label{
    width: 160px;
    display: inline-block;
  }
  .tip_row{
    display: flex;
    // justify-content: space-between;
  }
}
</style>
