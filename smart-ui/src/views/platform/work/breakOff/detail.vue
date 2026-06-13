<!--业务监控：调休列表，详情 -->
<template>
  <div class="my-basic-container staff-info">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-row>
          <el-col :span="9">
            <p class="box-orange">工单信息</p>
            <table class="dot-list">
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="流程编号"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.processId}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="创建时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.createTime}}</td>
              </tr>
            </table>
          </el-col>
          <el-col :span="2"></el-col>
          <el-col :span="10">
            <p class="box-orange">调休信息</p>
            <table class="dot-list dotlist2">
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="工号"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.staffBadge}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="姓名"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.staffName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="BU"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.buName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="部门"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.depName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="岗位"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.jobName}}</td>
              </tr>

              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="出勤时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.workDate}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="调休时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.restDate}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="要调休天数"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.restCount}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="可调休天数"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.restAbleCount}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="调休原因"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.restDesc}}</td>
              </tr>
            </table>
          </el-col>
        </el-row>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { getById } from "@/api/platform/work/break_off";
import { mapGetters } from "vuex";

export default {
  data() {
    return {
      staffResult: {}
    };
  },
  created() {
    var id = this.$route.params.id;
    var params = { id };
    getById(id).then(response => {
      if (!this.validatenull(response.data.data)) {
        if (!this.validatenull(response.data.data.employee)) {
          this.staffResult = response.data.data.employee;
        }
      }
    });
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/work/breakOff`,
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
.dotlist2{
  .my-dot-label{
    width: 140px;
  }
}
</style>
