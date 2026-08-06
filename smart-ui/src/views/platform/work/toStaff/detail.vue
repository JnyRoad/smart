<!--业务监控：请假列表，详情 -->
<template>
  <div class="my-basic-container staff-info">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <div class="info-cont">
          <div class="right-menu">
            <el-button type="primary" plain>
              <a href="#rzzxx">入职主信息</a>
            </el-button>
            <el-button type="primary" plain>
              <a href="#jyjl">教育经历</a>
            </el-button>
            <el-button type="primary" plain>
              <a href="#gzjy">工作经验</a>
            </el-button>
            <el-button type="primary" plain>
              <a href="#jtcy">家庭成员</a>
            </el-button>
            <el-button type="primary" plain>
              <a href="#rzgx">任职关系</a>
            </el-button>
          </div>
          <div class="info3" id="rzzxx">
            <p class="box-orange">入职主信息</p>
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
                          :src="staffResult.facePic || errorImgPeaple()"
                          :onerror="errorImgPeaple()"
                        />
                      </viewer>
                    </div>
                  </dt>
                  <dd class="desc-info"></dd>
                </dl>
              </el-col>
              <el-col :span="17">
                <table class="dot-list dotlist2">
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="工号"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{personInfo.badge}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="姓名"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{personInfo.name}}</td>
                  </tr>

                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="公司"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{personInfo.compName}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="部门"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{personInfo.depName}}</td>
                  </tr>
                  <tr>
                    <td>
                      <div class="my-dot-label">
                        <tce-label-justify label="岗位"></tce-label-justify>
                      </div>
                    </td>
                    <td>{{personInfo.jobName}}</td>
                  </tr>
                </table>
              </el-col>
            </el-row>
          </div>
          <div class="info3" id="jyjl">
            <p class="box-orange">教育经历</p>
            <template>
              <div
                class="info-inner clear"
                v-for="data_education in education"
                :key="data_education.educationHisId"
              >
                <p class="circle-white">起始时间：{{data_education.startTime}}</p>
                <p class="circle-white">截止时间：{{data_education.endTime}}</p>
                <p class="circle-white">学校名称：{{data_education.schoolName}}</p>
                <p class="circle-white">专业：{{data_education.major}}</p>
                <p class="circle-white">学历：{{data_education.education}}</p>
                <p class="circle-white">是否为最高学历：{{data_education.isHighEduType==1 ? '是':'否'}}</p>
                <p class="circle-white">学位：{{data_education.degree}}</p>
                <p class="circle-white">是否为最高学位：{{data_education.isHighDegreeType==1 ? '是':'否'}}</p>
              </div>
            </template>
          </div>
          <div class="info3" id="gzjy">
            <p class="box-orange">工作经验</p>
            <template>
              <div class="info-inner clear" v-for="work in work" :key="work.id">
                <p class="circle-white">起始时间：{{work.startTime}}</p>
                <p class="circle-white">截止时间：{{work.endTime}}</p>
                <p class="circle-white">服务单位：{{work.company}}</p>
                <p class="circle-white">岗位：{{work.jobName}}</p>
                <p class="circle-white">负责人：{{work.personLiable}}</p>
                <p class="circle-white">联系方式：{{work.phone}}</p>
              </div>
            </template>
          </div>
          <div class="info3" id="jtcy">
            <p class="box-orange">家庭成员</p>
            <template>
              <div class="info-inner clear" v-for="work in family" :key="work.id">
                <p class="circle-white">亲属姓名：{{work.name}}</p>
                <p class="circle-white">亲属关系：{{work.relation}}</p>
                <p class="circle-white">亲属性别：{{work.sex==1?'男':'女'}}</p>
                <p class="circle-white">生日：{{work.birth}}</p>
                <p class="circle-white">电话：{{work.phone}}</p>
                <p class="circle-white">所在单位：{{work.company}}</p>
                <p class="circle-white">担任职务：{{work.job}}</p>
              </div>
            </template>
          </div>
          <div class="info3" id="rzgx">
            <p class="box-orange">任职关系</p>
            <template>
              <div class="info-inner clear" v-for="work in relation" :key="work.id">
                <p class="circle-white">亲属工号：{{work.badge}}</p>
                <p class="circle-white">姓名：{{work.name}}</p>
                <p class="circle-white">任职关系：{{work.relation}}</p>
                <p class="circle-white">详细关系：{{work.relationDetail}}</p>
                <p class="circle-white">公司：{{work.compName}}</p>
                <p class="circle-white">部门：{{work.deptName}}</p>
                <p class="circle-white">岗位：{{work.jobName}}</p>
              </div>
            </template>
          </div>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { getById } from "@/api/platform/work/to_staff";
import { mapGetters } from "vuex";

export default {
  data() {
    return {
      personInfo: {},
      staffResult: {},
      education: [],
      family: [],
      work: [],
      relation: []
    };
  },
  created() {
    var id = this.$route.params.id;
    var params = { id };

    getById(id).then(response => {
      if (!this.validatenull(response.data.data)) {
        this.staffResult = response.data.data;
        this.personInfo = response.data.data.smtStaff;
        if (!this.validatenull(response.data.data.education)) {
          this.education = response.data.data.education;
        }
        if (!this.validatenull(response.data.data.family)) {
          this.family = response.data.data.family;
        }
        if (!this.validatenull(response.data.data.work)) {
          this.work = response.data.data.work;
        }
        if (!this.validatenull(response.data.data.relation)) {
          this.relation = response.data.data.relation;
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
        path: `/platform/work/toStaff`,
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
.info-cont {
  padding: 20px 250px 0 20px;
}
.right-menu {
  position: absolute;
  top: 20px;
  right: 0;
  bottom: 20px;
  width: 250px;
  padding: 40px;
  border-left: 1px solid #e0e0e0;
  .el-button + .el-button {
    margin: 0 0 15px 0;
  }
  .el-button {
    display: block;
    width: 115px;
    margin-bottom: 15px;
  }
  a {
    color: #ed6d00;
  }
  .el-button:hover a {
    color: #fff;
  }
}
.info3 {
  margin-bottom: 30px;
}
.info-inner {
  padding: 0 35px 20px;
  p {
    width: 33.3%;
    float: left;
    margin-bottom: 10px;
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
.dotlist2{
  .my-dot-label{
    width: 100px;
  }
}
</style>
