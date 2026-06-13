
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t">园区数据</div>
      </div>
      <div class="wrap_cont">
        <div class="w-item">
          接入园区<span class="num">108</span>
        </div>
        <div class="w-item">
          涉及数据<span class="num">10</span>
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
  props: {
    // list:{
    //   type: Object,
    //   default: {}
    // }
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
    height: 109px;
    background-image: url('/img/panel_new/park/bg_1.png');
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
