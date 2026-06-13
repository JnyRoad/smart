
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t">园区员工</div>
      </div>
      <div class="wrap_cont">
        <div class="w-item">
          入住BU数（个）<span class="num">{{dataObj.compTotal}}</span>
        </div>
        <div class="w-item">
          部门数量（个）<span class="num">{{dataObj.deptToal}}</span>
        </div>
        <div class="w-item">
          岗位个数（个）<span class="num">{{dataObj.jobTotal}}</span>
        </div>
        <div class="w-item">
          员工总数（人）<span class="num">{{dataObj.staffTotal}}</span>
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
    dataObj:{
      type: Object,
      default: () => ({})
    }
  },
  mounted() {},
  methods: {}
}
</script>

<style lang="scss" scoped>
  .wrap{
    width: 389px;
    height: 181px;
    background-image: url('/img/panel_new/view/bg_2.png');
    .num{
      width: 62px;
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
