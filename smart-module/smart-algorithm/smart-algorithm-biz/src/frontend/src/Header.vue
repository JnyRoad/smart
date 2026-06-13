<template>
  <div class="header">
    <el-row>
      <el-col :span="8">
        <img src="./assets/img/index-logo.png" class="logo"/>
        <span class="sysName">算法演示</span>
      </el-col>
      <el-col :span="16">
        <el-menu :router="true" class="el-menu-demo" mode="horizontal">
          <el-menu-item index="/index">主页</el-menu-item>
          <el-menu-item index="/face/detect">人脸识别</el-menu-item>
          <el-menu-item index="/ocr">OCR识别</el-menu-item>
          <el-menu-item index="/liveness">活体检测</el-menu-item>
          <el-menu-item index="/compare">人像比对</el-menu-item>
        </el-menu>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  export default {
    name: 'Header',
    data: function () {
      return {
        activeIndex : '/index'
      }
    },
    methods: {
    },
    created() {
    },
    mounted() {
    }
  }
</script>
