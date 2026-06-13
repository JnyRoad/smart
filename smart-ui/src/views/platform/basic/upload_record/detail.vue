<!--基础信息：员工信息，详情 -->
<template>
  <div class="my-basic-container staff-info">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <!-- <div class="top-menu clear">
          <div class="top-right">
            <el-button type="primary" icon="" @click="demission">确认员工离职</el-button>
          </div>
        </div>-->
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <p class="box-orange">员工信息</p>
        <el-row>
          <el-col :span="6">
            <dl class="img-info">
              <dt class="img-outer">
                <i class="corner cn-top"></i>
                <i class="corner cn-rit"></i>
                <i class="corner cn-btm"></i>
                <i class="corner cn-lft"></i>
                <div class="img-inner">
                  <viewer>
                    <img
                      :src="staffResult.facePicUrl || errorImgPeaple()"
                      :onerror="errorImgPeaple()"
                    />
                  </viewer>
                </div>
              </dt>
              <dd class="desc-info"></dd>
            </dl>
          </el-col>
          <el-col :span="14">
            <table class="dot-list dotlist2">
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="工号"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.badge}}</td>
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
                    <tce-label-justify label="所属园区"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.parkName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="导入时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.createTime}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="导入状态"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.status==0?'导入失败':'导入成功'}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="操作人"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.createUser}}</td>
              </tr>
            </table>
          </el-col>
        </el-row>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { getById } from "@/api/platform/basic/upload_record";
import { mapGetters } from "vuex";

export default {
  data() {
    return {
      personInfo: {},
      staffResult: {},
      smtStaffEmergency: {}
    };
  },
  created() {
    var id = this.$route.params.id;
    var params = { id };
    getById(id).then(response => {
      if (!this.validatenull(response.data.data)) {
        this.staffResult = response.data.data;
        //this.personInfo=response.data.data.smtStaff;
        if (!this.validatenull(response.data.data.smtStaffEmergency)) {
          this.smtStaffEmergency = response.data.data.smtStaffEmergency[0];
        }
      }
    });
  },
  computed: {
    ...mapGetters(["permissions"])
  },

  methods: {
    demission() {},
    goBack() {
      let path = '/platform/basic/upload_record'
      this.$router.push({
        path: path,
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
.img-info {
  width: 60%;
  margin: 0 auto;
}
.dot-list {
  float: left;
  width: 80%;
  margin-right: 50px;
}
.dotlist2 {
  td:first-child {
    width: 120px;
  }
  .circle-white {
    width: 110px;
  }
}
</style>
