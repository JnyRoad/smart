<!--业务监控：补卡列表，详情 -->
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
            <p class="box-orange">补卡信息</p>
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
                    <tce-label-justify label="考勤月份"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.workMonth}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="补卡开始时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.startTime}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="补卡原因"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.causeDesc}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="二段进"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.secondEnter}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="二段出"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.secondOut}}</td>
              </tr>
              <!-- <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="二段进是否跨天"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.secondEnter}}</td>
              </tr>-->
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="二段出是否跨天"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.secondOutCover==1?'是':'否'}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="四段进"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fourthEnter}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="四段出"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fourthOut}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="四段进是否跨天"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fourthEnterCover==1?'是':'否'}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="四段出是否跨天"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fourthOutCover==1?'是':'否'}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="五段进"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fifthEnter}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="五段出"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fifthOut}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="五段进是否跨天"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fifthEnterCover==1?'是':'否'}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="五段出是否跨天"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.fifthOutCover==1?'是':'否'}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="备注"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.remark}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="附件"></tce-label-justify>
                  </div>
                </td>
                <td>
                  <viewer>
                    <div class="img-inner">
                      <img
                        :src="staffResult.photoUrl || errorImgPeaple()"
                        :onerror="errorImgPeaple()"
                      />
                    </div>
                  </viewer>
                </td>
              </tr>
            </table>
          </el-col>
        </el-row>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { getById } from "@/api/platform/work/attendance";
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
        this.staffResult = response.data.data;
      }
    });
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/work/attendance`,
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
.dotlist2{
  .my-dot-label{
    width: 170px;
  }
}
.my-basic-inner {
  padding: 30px;
}
.img-info {
  width: 60%;
  margin: 0 auto;
}
.img-inner {
  width: 300px;
  height: 180px;
  overflow: hidden;
  img {
    max-width: 100%;
    height: auto;
    max-height: 100%;
    width: auto;
    cursor: pointer;
  }
}
.dot-list {
  width: 80%;
  margin: 0 50px 40px 0;
}
</style>
