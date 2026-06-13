<template>
  <el-container class="outer">
    <el-header height="70px">
      <!-- 头部 -->
      <Header></Header>
    </el-header>
    <el-container class="second-outer">
      <el-container>
        <el-main id="el-main">
          <!-- 主体 -->
          <router-view></router-view>
        </el-main>
      </el-container>
    </el-container>
  </el-container>
</template>

<script>
  import Header from './Header';

  export default {
    name: 'App',
    components: {Header}
  }


</script>
