<!--业务监控：外宿补贴列表，详情 -->
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

          <el-col :span="10">
            <p class="box-orange">外宿补贴信息</p>
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
                <td>{{staffResult.name}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="BU"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.compName}}</td>
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
                    <tce-label-justify label="外宿地址"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.outAddress}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="补贴开始时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.startTime}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="补贴结束时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.endTime}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="补贴类型"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.allowanceType}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="计算规则"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.computaionRule}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="补贴说明"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.explain}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="补贴金额"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.amount}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="备注"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.remark}}</td>
              </tr>
            </table>
          </el-col>
        </el-row>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { getById } from "@/api/platform/work/out_dormitory";
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
        path: `/platform/work/outDormitory`,
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
    width: 150px;
  }
}
.img-info {
  width: 60%;
  margin: 0 auto;
}
.dot-list {
  float: left;
  width: 80%;
  margin-right: 50px;
}
</style>
