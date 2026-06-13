<template>
  <div class="mncont" style="padding: 100px;">
    <el-row>
      <el-col :span="11">
        <el-card @click.native="toUrl('/face/detect')" :body-style="cardStyle" shadow="hover">
          <div class="inner-card">
            <span>人脸识别</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="2">
        <div>&nbsp;</div>
      </el-col>
      <el-col :span="11">
        <el-card @click.native="toUrl('/ocr')" :body-style="cardStyle" shadow="hover">
          <div class="inner-card">
            <span>OCR识别</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row style="margin-top:50px;">
      <el-col :span="11">
        <el-card @click.native="toUrl('/liveness')" :body-style="cardStyle" shadow="hover">
          <div class="inner-card">
            <span>活体检测</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="2">
        <div>&nbsp;</div>
      </el-col>
      <el-col :span="11">
        <el-card @click.native="toUrl('/compare')" :body-style="cardStyle" shadow="hover">
          <div class="inner-card">
            <span>人像比对</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>

  export default {
    name: "Index",
    data: function () {
      return {
        cardStyle:{
          padding: '50px',
          cursor: 'pointer'
        },

      };
    },
    methods: {
      toUrl(url){
        this.$router.push(url);
      }
    },
    created() {
      //实例创建完成后,页面渲染前执行
    },

    mounted() {
    }
  };
</script>

<style scoped>
  .mncont {
    display: flex;
    flex-direction: column;
    background: transparent;
    box-shadow: none;
    padding: 0;
  }

</style>
