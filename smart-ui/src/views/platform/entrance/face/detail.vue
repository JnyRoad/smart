<!--出入记录，人员出入，详情  -->
<template>
  <div class="my-basic-container face-detail mycard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <el-row type="flex" class="my-basic-inner">
        <el-col :lg="8" :md="8" class="box-outer box-left">
          <div class="top-menu" style="margin-bottom:20px;">
            <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
          </div>
          <div class="wPercent">
            <dl class="img-info">
              <dt class="img-outer">
                <i class="corner cn-top"></i>
                <i class="corner cn-rit"></i>
                <i class="corner cn-btm"></i>
                <i class="corner cn-lft"></i>
                <div class="img-inner">
                  <viewer>
                    <img :src="personInfo.photo || errorImgPeaple()" :onerror="errorImgPeaple()" />
                  </viewer>
                </div>
              </dt>
              <dd class="desc-info">人员照片</dd>
            </dl>
          </div>
          <div class>
            <p class="box-orange">人员信息</p>
            <table class="lit-table">
              <tr>
                <td>姓名</td>
                <td>{{personInfo.personName}}</td>
              </tr>
              <template v-if="personInfo.personType==1">
                <tr>
                  <td>工号</td>
                  <td>{{personInfo.staffBadge}}</td>
                </tr>
                <tr>
                  <td>BU</td>
                  <td>{{personInfo.compName}}</td>
                </tr>
                <tr>
                  <td>部门</td>
                  <td>{{personInfo.depName}}</td>
                </tr>
                <tr>
                  <td>岗位</td>
                  <td>{{personInfo.jobName}}</td>
                </tr>
                <tr>
                  <td>职层</td>
                  <td>{{personInfo.jcheName}}</td>
                </tr>
                <tr>
                  <td>员工状态</td>
                  <td>{{personInfo.staffStatusDesc}}</td>
                </tr>
                <tr>
                  <td>手机号</td>
                  <td>{{personInfo.personPhone}}</td>
                </tr>
              </template>
              <template v-if="personInfo.personType==2">
                <tr>
                  <td>所属单位</td>
                  <td>{{personInfo.company}}</td>
                </tr>
                <tr>
                  <td>手机号</td>
                  <td>{{personInfo.personPhone}}</td>
                </tr>
              </template>
            </table>
          </div>
          <div class>
            <p class="box-orange">出入信息</p>
            <table class="lit-table">
              <tr>
                <td>出入园区</td>
                <td>{{personInfo.parkName}}</td>
              </tr>
              <tr>
                <td>出入地点</td>
                <td>{{personInfo.areaName}}</td>
              </tr>
              <tr>
                <td>设备名称</td>
                <td>{{personInfo.deviceName||'-'}}</td>
              </tr>
              <tr>
                <td>出入类型</td>
                <td>{{personInfo.eventTypeDesc}}</td>
              </tr>
              <tr>
                <td>出入时间</td>
                <td>{{personInfo.snapTime}}</td>
              </tr>
              <tr>
                <td>体温</td>
                <td>{{personInfo.faceTemperature||'-'}}</td>
              </tr>
              <tr v-if="personInfo.faceTemperature">
                <td>体温是否正常</td>
                <td>{{personInfo.isNormal===1?'正常':'异常'}}</td>
              </tr>
            </table>
          </div>
        </el-col>
        <el-col :lg="16" :md="16" class="box-outer">
          <!-- 折线图片框 -->
          <div class="folder-outer">
            <div class="folder-line">
              <i class="cni cni-right">
                <i class="icon-yutong-squareRight bkcn cn3"></i>
              </i>
              <i class="cni cni-btm">
                <i class="icon-yutong-squareBottom bkcn cn4"></i>
              </i>
              <i class="cni cni-lft">
                <i class="icon-yutong-squareLeft bkcn cn5"></i>
              </i>
              <div class="img">
                <viewer>
                  <img :src="personInfo.snapPhoto || errorImgPeaple()" :onerror="errorImgPeaple()" />
                </viewer>
              </div>
            </div>
            <span class="folder-top">
              现场抓拍照片
              <i class="skewi"></i>
              <i class="icon-yutong-squareTop1 bkcn cn2"></i>
            </span>
            <i class="cni cni-top">
              <i class="icon-yutong-squareTop bkcn cn1"></i>
            </i>
          </div>
        </el-col>
      </el-row>
    </el-scrollbar>
  </div>
</template>

<script>
import { getDetails } from "@/api/platform/entrance/face_detail";
import { mapGetters } from "vuex";

export default {
  name: "face",
  data() {
    return {
      personInfo: {
        // 'vs_name':'张三',
        // 'workid': '1001',
        // 'bu': 'XXXX',
        // 'department':'研发部',
        // 'work':'研发工程师',
        // 'layer':'职员',
        // 'status':'警报地点1',
        // 'img':'/img/placeholder_people.png',
      }
      // entrynfo:{
      //   'site':'张三',
      //   'type': '外来人员',
      //   'time':'2019-01-01 5:20:20',
      //   'img':'/img/placeholder_people.png'
      // },
    };
  },
  created() {
    getDetails(this.$route.params.id).then(response => {
      this.personInfo = response.data.data;
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    goBack() {
      let path = '/platform/entrance/face'
      this.$router.push({
        path: path,
        query: {
          isInsider: this.$route.query.isInsider,
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
