<!--业务监控：请假列表，详情 -->
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
            <table class="dot-list dotlist2">
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
                <td>{{staffResult.createDate}}</td>
              </tr>
            </table>
            <p class="box-orange">班次信息</p>
            <table class="dot-list dotlist2">
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="班次名称"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.classDesc}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="二入"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.secondEnter}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="二出"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.secondOut}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="四入"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fourthEnter}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="四出"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fourthOut}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="五入"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fifthEnter}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="五出"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fifthOut}}</td>
              </tr>
            </table>
          </el-col>
          <el-col :span="2"></el-col>
          <el-col :span="10">
            <p class="box-orange">加班信息</p>
            <table class="dot-list dotlist2">
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="工号"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.employeeBadge}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="姓名"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.employeeName}}</td>
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
                <td>{{staffResult.deptName}}</td>
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
                    <tce-label-justify label="加班时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.extraworkDate}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="班别"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.extraworkClassDesc}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="加班类型"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.extraworkTypeDesc}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="开始时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.startDate2?staffResult.startDate2:'-'}}【第二段】</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="结束时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.endDate2?staffResult.endDate2:'-'}}【第二段】</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="开始时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.startDate4?staffResult.startDate4:'-'}}【第四段】</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="结束时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.endDate4?staffResult.endDate4:'-'}}【第四段】</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="开始时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.startDate5?staffResult.startDate5:'-'}}【第五段】</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="结束时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.endDate5?staffResult.endDate5:'-'}}【第五段】</td>
              </tr>

              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="加班时长"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.extraworkCount}}小时</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="加班原因"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.extraworkDesc}}</td>
              </tr>
            </table>
          </el-col>
        </el-row>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { getById } from "@/api/platform/work/over_time";
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
        path: `/platform/work/overTime`,
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
</style>
