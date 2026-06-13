<!--安防中心，警报记录，详情  -->
<template>
  <div class="my-basic-container alarm-detail mycard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <el-row type="flex" class="my-basic-inner">
        <el-col :lg="6" :md="8" class="box-outer box-left">
          <div class="wPercent">
            <dl class="img-info">
              <dt class="img-outer">
                <i class="corner cn-top"></i>
                <i class="corner cn-rit"></i>
                <i class="corner cn-btm"></i>
                <i class="corner cn-lft"></i>
                <div class="img-inner">
                  <img :src="alarmInfo.img" :onerror="errorImgPeaple()" />
                </div>
              </dt>
              <dd class="desc-info">人员照片</dd>
            </dl>
          </div>
          <div class>
            <p class="box-orange">警报信息</p>
            <table class="lit-table">
              <tr>
                <td>姓名</td>
                <td>{{alarmInfo.vs_name}}</td>
              </tr>
              <tr>
                <td>身份</td>
                <td>{{alarmInfo.vs_name1}}</td>
              </tr>
              <tr>
                <td>警报名称</td>
                <td>{{alarmInfo.alarm_name}}</td>
              </tr>
              <tr>
                <td>警报时间</td>
                <td>{{alarmInfo.alarm_time}}</td>
              </tr>
              <tr>
                <td>警报地点</td>
                <td>{{alarmInfo.alarm_site}}</td>
              </tr>
            </table>
          </div>
        </el-col>
        <el-col :lg="18" :md="16" class="box-outer">
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
                <img :src="alarmInfo.img" :onerror="errorImgPeaple()" />
              </div>
            </div>
            <span class="folder-top">
              发生警报现场图片
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
import { fetchList } from "@/api/platform/alarm/record_detail";
import { mapGetters } from "vuex";

export default {
  name: "visitor",
  data() {
    return {
      alarmInfo: {
        vs_name: "张三",
        vs_name1: "外来人员",
        alarm_name: "非法闯入",
        alarm_time: "2019-01-01 5:20:20",
        alarm_site: "警报地点1",
        img: "/img/placeholder_people.png"
      }
    };
  },
  created() {},
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {}
};
</script>

<style lang="scss" scoped></style>
