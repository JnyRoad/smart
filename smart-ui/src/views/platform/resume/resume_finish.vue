<!--简历登记，完成  -->
<template>
  <div class="my-basic-container resume">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top">
          <div class="top-logo">
            <img src="img/logo.png" />
            <span class="top-titile">
              裕同集团员工入职简历登记
              <span class="steps">【完成】</span>
            </span>
          </div>
          <!-- <el-row class="flex"  justify="center">
            <el-col :lg="12" :xs="24" :sm="24">
              <div class="top-logo">
                <img src="img/logo.png"/>
              </div>
            </el-col>
            <el-col :lg="12" :xs="24" :sm="24">
              <div class="top-titile">
                裕同集团员工入职简历登记
                <span class="steps">【完成】</span>
              </div>
            </el-col>
          </el-row>-->
        </div>
        <div class="cont">
          <el-row class="flex" justify="center">
            <el-col :span="24">
              <div class="img">
                <!-- <img src="img/rm_finish.jpg"/> -->
              </div>
              <p>谢谢您的配合</p>
              <p>记得准时来报道</p>
            </el-col>
          </el-row>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/resume/index" as *;
</style>

<script>
export default {
  name: "resume",
  data() {
    return {};
  },
  created: function() {},
  mounted: function() {},
  methods: {}
};
</script>

<style lang="scss" scoped>
.cont {
  text-align: center;
  color: #999;
  line-height: 30px;
  .img {
    width: 300px;
    height: 300px;
    margin: 0 auto 20px;
    background: url("/img/rm_finish.jpg") center no-repeat;
    background-size: 100% 100%;
  }
}
::v-deep .my-basic-container {
  padding-bottom: 0px;
}
::v-deep .my-basic-inner {
  padding: 0;
}
</style>
