
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t">APP动态</div>
      </div>
      <div class="wrap_cont">
        <div class="w-item">
          累计安装人数<span class="num">108</span>
        </div>
        <div class="w-item">
          日均登录人数<span class="num">10</span>
        </div>
        <div class="w-item">
          应用业务种类<span class="num">2</span>
        </div>
        <div class="w-item">
          日业务记录量<span class="num">1</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  data() {
    return {
    }
  },
  components: {
  },
  methods: {
  },
  mounted() {
  }
}
</script>

<style lang="scss" scoped>
  .wrap{
    width: 389px;
    height: 170px;
    background-image: url('/img/panel_new/park/bg_2.png');
    .num{
      width: 62px;
      margin-left: 20px;
      margin-top: -3px;
    }
    &_cont{
      display: flex;
      flex-wrap: wrap;
      align-content: space-around;
      padding: 25px 0 25px 20px;
      .w-item{
        width: 50%;
        overflow: hidden;
      }
    }
  }
</style>
