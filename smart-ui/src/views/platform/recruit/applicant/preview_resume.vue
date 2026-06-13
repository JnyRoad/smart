<!--招聘管理：应聘管理的预览简历-->
<template>
  <div class="my-basic-container center-card recruit_detail">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="center-conent">
          <el-row class="border-btm info2">
            <el-col :span="4">
              <dl class="img-lft">
                <dt>
                  <div>
                    <img :src="data_applicant.img1" :onerror="errorImgPeaple()" />
                  </div>
                </dt>
                <dd>人脸照片</dd>
              </dl>
            </el-col>
            <el-col :span="14">
              <p class="person-name">{{data_applicant.name1}}</p>
              <div class="person-info clear">
                <p>
                  <i class="psi psi-sex"></i>
                  {{data_applicant.name2}}
                </p>
                <p>
                  <i class="psi psi-date"></i>
                  {{data_applicant.time3}}
                </p>
                <p>
                  <i class="psi psi-age"></i>
                  {{data_applicant.name4}}
                </p>
                <p>
                  <i class="psi psi-nation"></i>
                  {{data_applicant.name5}}
                </p>
                <p>
                  <i class="psi psi-edu"></i>
                  {{data_applicant.name6}}
                </p>
                <p>
                  <i class="psi psi-tel"></i>
                  {{data_applicant.name7}}
                </p>
              </div>
              <p>
                <i class="psi psi-site"></i>
                {{data_applicant.name8}}
              </p>
            </el-col>
            <el-col :span="6">
              <dl class="img-rt">
                <dt>
                  <div>
                    <img :src="data_applicant.img2" :onerror="errorImgIdentity()" />
                  </div>
                </dt>
                <dd>身份证照片</dd>
              </dl>
            </el-col>
          </el-row>
          <div class="info3">
            <p class="box-orange">求职意向</p>
            <div class="info-inner clear">
              <p class="circle-white">应聘岗位：{{data_applytment.name1}}</p>
              <p class="circle-white">BU：{{data_applytment.name2}}</p>
              <p class="circle-white">部门：{{data_applytment.name3}}</p>
              <p class="circle-white">职层：{{data_applytment.name4}}</p>
              <p class="circle-white">投递时间：{{data_applytment.name1}}</p>
            </div>
          </div>
          <div class="info3">
            <p class="box-orange">教育经验</p>
            <div class="info-inner clear">
              <p class="circle-white">起始时间：{{data_education.time1}}</p>
              <p class="circle-white">截止时间：{{data_education.time2}}</p>
              <p class="circle-white">学校名称：{{data_education.name3}}</p>
              <p class="circle-white">专业：{{data_education.name4}}</p>
              <p class="circle-white">学历：{{data_education.name5}}</p>
            </div>
          </div>
          <div class="info3">
            <p class="box-orange">工作经验</p>
            <div class="info-inner clear">
              <p class="circle-white">起始时间：{{data_work.time1}}</p>
              <p class="circle-white">截止时间：{{data_work.time2}}</p>
              <p class="circle-white">服务单位：{{data_work.name3}}</p>
              <p class="circle-white">岗位：{{data_work.name4}}</p>
              <p class="circle-white">薪资：{{data_work.name5}}</p>
              <p class="circle-white">负责人：{{data_work.name6}}</p>
              <p class="circle-white">联系方式：{{data_work.name7}}</p>
            </div>
          </div>
          <div class="info3">
            <p class="box-orange">任职关系</p>
            <div class="info-inner clear">
              <p class="circle-white">姓名：{{data_relation.name1}}</p>
              <p class="circle-white">与本人关系：{{data_relation.name2}}</p>
              <p class="circle-white">公司名称：{{data_relation.name3}}</p>
              <p class="circle-white">部门：{{data_relation.name4}}</p>
            </div>
          </div>
        </div>
        <div class="download">
          <el-button type="primary" @click="downloadResume" round>下载简历</el-button>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/recruit/detail" as *;
</style>

<script>
import { fetchList } from "@/api/platform/recruit/applicant_detail";
import { mapGetters } from "vuex";

export default {
  name: "resume",
  data() {
    return {
      data_applytment: {
        name1: "运维工程师",
        name2: "XXXXX",
        name3: "运维管理部",
        name4: "职员层",
        time5: "2019-01-01 8:20:20",
        status: 3
      },
      data_applicant: {
        name1: "张丹丹",
        name2: "女",
        time3: "2019-01-01",
        name4: "22",
        name5: "汉",
        name6: "本科",
        name7: "-",
        name8: "北京市海淀区牡丹园旷怡大厦",
        img1: "img/placeholder_people.png",
        img2: "img/placeholder_identity.png"
      },
      data_education: {
        time1: "2019-01-01",
        time2: "2019-01-01",
        name3: "清华大学",
        name4: "计算机专业",
        name5: "本科"
      },
      data_work: {
        time1: "2019-01-01",
        time2: "2019-01-01",
        name3: "时代云英科技有限公司",
        name4: "运维管理部",
        name5: "15K",
        name6: "李四",
        name7: "-"
      },
      data_relation: {
        name1: "张三",
        name2: "朋友",
        name3: "时代云英科技有限公司",
        name4: "运维工程师1"
      }
    };
  },
  created() {},
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    getList(page, params) {
      // this.tableLoading = true
      // fetchList(Object.assign({
      //   descs: 'create_time',
      //   current: page.currentPage,
      //   size: page.pageSize
      // }, params)).then(response => {
      //   this.insiderData = response.data.data.records
      //   this.page.total = response.data.data.total
      //   this.tableLoading = false
      // })
    },
    downloadResume() {}
  }
};
</script>

<style lang="scss" scoped>
.download {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100%;
  height: 80px;
  background: #fff;
  box-shadow: 0 0 10px 1px #d4d5d9;
  text-align: center;
  padding-top: 25px;
  .el-button {
    width: 130px;
  }
}
</style>
