<!--退换宿-->
<template>
  <div>
    <el-dialog
      title="退换宿"
      class="dialog_form change_out_form"
      width="1000px"
      :visible.sync="setFormVisible"
    >
      <div>
        <div>
          <avue-crud
            :data="tableData"
            :option="tableOption"
          >
            <!-- 没有工号就是空床位 -->
            <template slot-scope="scope" slot="staffName">
              <template v-if="scope.row.staffBadge">
                <el-tooltip placement="bottom">
                  <div slot="content" style="line-height: 25px">
                    姓名：{{scope.row.staffName}}<br/>
                    工号：{{scope.row.staffBadge}}<br/>
                    性别：{{scope.row.sex | genderInit}}<br/>
                    部门：{{scope.row.depName}}<br/>
                    职级：{{scope.row.jobName}}<br/>
                  </div>
                  <div style="cursor: pointer">
                    <i class="person_i"></i><span>{{ scope.row.staffName }}</span>
                  </div>
                </el-tooltip>
              </template>
              <template v-else> - </template>
            </template>
            <template slot-scope="scope" slot="status">
              <template v-if="scope.row.staffBadge">
                <span :class="scope.row.status|fl_statusClass">{{ scope.row.status|staffStatusInit }}</span>
              </template>
              <template v-else> - </template>
            </template>
            <template slot-scope="scope" slot="inDate">
              <template v-if="scope.row.staffBadge">{{ scope.row.inDate}}</template>
              <template v-else> - </template>
            </template>
            <template slot-scope="scope" slot="menu">
              <template v-if="scope.row.staffBadge">
                <el-button
                  type="text"
                  @click="handleChange(scope.row, scope.$index)"
                  ><i class="change_dorm_i"></i>换宿</el-button
                >
                <el-button
                  type="text"
                  class="check_out_btn"
                  @click="handleCheckOut(scope.row, scope.$index)"
                  ><i class="check_out_i"></i>退宿</el-button
                >
              </template>
              <template v-else>
                <el-button
                  v-if="!scope.row.status"
                  type="text"
                  class="check_in_btn"
                  @click="handleCheckIn(scope.row, scope.$index)"
                  ><i class="check_in_i"></i>入住</el-button
                >
              </template>
            </template>
          </avue-crud>
          <!-- 夫妻/家属 房的时候 可以添加家属 -->
          <div v-if="isMix && mainStaff" style="margin-top: 20px;">
            <p class="box-orange" style="margin-bottom: 0;">家属信息</p>
            <dlgFamilyList :row="mainStaff"/>
          </div>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="setFormVisible = false" plain
          >取 消</el-button
        >
        <el-button type="primary" @click="editSubmit()" :loading="setLoading"
          >保 存</el-button
        >
      </div>
    </el-dialog>
    <dlgCheckOutDorm :visible="dlg2Visible" :row="curDlg2Obj" @dlgdo="dlg2do" @dlgdoSuccess="dlg2doSuccess"/>
  </div>
</template>

<script>

import { bedDetail } from "../_service.js";
import { tableOption } from "@/const/crud/platform/dormitory/change_check_out";
import dlgCheckOutDorm from "./dlg_check_out_dorm";
import dlgFamilyList from "./dlg_family_list";

export default {
  name: "",
  components:{
    dlgCheckOutDorm,
    dlgFamilyList
  },
  data() {
    return {
      dlg2Visible: false, //退宿
      curDlg2Obj: {},
      tableOption: tableOption,
      tableData: [],
      isMix: undefined, //房间属性：'男','女','夫妻/家属','其他' 2是夫妻/家属房，可以添加家属
      mainStaff: undefined, //夫妻房时，主入住人
      setFormVisible: false, //配置模板
      setLoading: false, //是否正在设置
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    row: undefined,
  },
  watch: {
    visible(newVal, oldVal) {
      this.setFormVisible = newVal;
    },
    setFormVisible(newVal, oldVal) {
      if (newVal === false) {
        this.$emit("dlgdo", newVal);
      }
    },
    row:{
      handler:function(newVal, oldVal) {
        this.tableData = []
        if(newVal&&newVal.roomId){
          this.isMix = newVal.sex
          newVal.sex==2?this.isMix=true:this.isMix=false
          this.getDetail(newVal.roomId);
        }
      },
      immediate: true
    }
  },
  filters: {
    fl_statusClass: function(val) {
      //空床位，就是没有工号
      //0-离职，1-在职，null-未入职
      if (val===0) {
        return "st_1";
      } else if (val===1) {
        return "st_2";
      } else if (val===null) {
        return "st_3";
      }
    },
  },
  created() {
    this.initData();
  },
  mounted: function () {},
  computed: {},
  methods: {
    initData() {
      this.setFormVisible = this.visible;
    },
    dlg2do(val){
      this.dlg2Visible = val
    },
    //换宿
    handleChange(row){
      this.$emit("dlgChangeDorm", row);
    },
    //退宿
    handleCheckOut(row, index){
      this.dlg2Visible = true
      this.curDlg2Obj = row
    },
    dlg2doSuccess(){
      this.dlg2Visible = false
      this.$emit("dlgdoSuccess");
    },
    //入住
    handleCheckIn(row, index){
      const src = `/platform/dormitory/bed_mng/check_in/${row.id}`;
      this.$router.push({
        path: src,
        query: {
          bedId: row.id,
          fromNew: true
        }
      });
    },
    async getDetail(roomId) {
      const res = await bedDetail(roomId)
      this.tableData = res.data.data
      // 夫妻/家属 房的时候，如果有员工入住，存储为主入住人信息
      if(this.isMix&&this.tableData.length>0&&this.tableData[0].staffBadge){
        this.mainStaff = this.tableData[0]
      }else{
        this.mainStaff = undefined
      }
    },
    async editSubmit() {},
  },
};
</script>
<style lang="scss" scoped>
.change_out_form ::v-deep {
  $c1: #e7292e;
  $c2: #0dbc82;
  $c3: #678fd1;
  .btns{
    text-align: center;
    margin-bottom: 15px;
  }
  .person_i{
    width: 14px;
    height: 14px;
    display: inline-block;
    vertical-align: middle;
    margin-right: 5px;
    margin-top: -4px;
    background-image: url('/img/dorm/person_i.png');
    background-size: 100% 100%;
  }
  .change_dorm_i{
    width: 13px;
    height: 13px;
    display: inline-block;
    vertical-align: middle;
    margin-right: 5px;
    margin-top: -3px;
    background-image: url('/img/dorm/hs_i.png');
    background-size: 100% 100%;
  }
  .check_out_i{
    width: 13px;
    height: 13px;
    display: inline-block;
    vertical-align: middle;
    margin-right: 5px;
    margin-top: -3px;
    background-image: url('/img/dorm/ts_i.png');
    background-size: 100% 100%;
  }
  .check_in_i{
    width: 13px;
    height: 13px;
    display: inline-block;
    vertical-align: middle;
    margin-right: 5px;
    margin-top: -3px;
    background-image: url('/img/dorm/rz_i.png');
    background-size: 100% 100%;
  }
  .check_out_btn{
    color: $c1;
  }
  .check_in_btn{
    color: $c2;
  }
  .st_1{
    color: $c1;
    padding: 4px 15px;
    background: rgba(231, 41, 46, .1);
  }
  .st_2{
    color: $c2;
    padding: 4px 15px;
    background: rgba(13, 188, 130, .1);
  }
  .st_3{
    color: $c3;
    padding: 4px 15px;
    background: rgba(103, 143, 209, .1);
  }
}
</style>