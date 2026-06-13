<!--业务监控：请假列表，详情 -->
<template>
  <div class="my-basic-container staff-info">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-row>
          <el-col :span="24">
            <p class="box-orange">离职主信息</p>
            <table class="dot-list dotlist2">
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="工号"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.badge}}</td>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="入职日期"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.joinTime | dateFormat2}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="姓名"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.name}}</td>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="离职日期"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.leaveTime | dateFormat2}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="BU"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.compName}}</td>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="离职类型"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.leaveTypeDesc}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="部门"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.depName}}</td>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="离职原因"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.leaveReasonDesc}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="岗位"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.jobName}}</td>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="创建时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.createTime}}</td>
              </tr>
            </table>
            <p class="box-orange">离职交接</p>
            <template>
              <el-table :data="tableData" style="width: 100%">
                <el-table-column prop="zrdepName" label="责任部分" ></el-table-column>
                <el-table-column prop="jjItem" label="交接项目" ></el-table-column>
                <el-table-column prop="jjr" label="交接人工号"></el-table-column>
                <el-table-column prop="jjrName" label="交接人姓名"></el-table-column>
                <el-table-column prop="je" label="金额"></el-table-column>
                <el-table-column prop="jjRemark" label="交接说明"></el-table-column>
              </el-table>
            </template>
          </el-col>
        </el-row>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { getById } from "@/api/platform/work/leaveApplication";
import { mapGetters } from "vuex";

export default {
  data() {
    return {
      staffResult: {},
      tableData: []
    };
  },
  created() {
    var id = this.$route.params.id;
    var params = { id };
    getById(id).then(response => {
      if (!this.validatenull(response.data.data)) {
        this.staffResult = response.data.data;
        this.tableData = response.data.data.items;
      }
    });
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/work/leaveApplication`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    }
  }
};
</script>
<style lang="scss" scoped>
.my-basic-inner {
  padding: 30px;
}
.img-info {
  width: 60%;
  margin: 0 auto;
}
.dot-list {
  width: 80%;
  margin: 0 50px 40px 0;
}
.dotlist2 {
  td:nth-child(2n+1) {
    width: 130px;
  }
}
</style>
