
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t">园区描述</div>
      </div>
      <div class="wrap_cont">
        <div class="w-item">
          占地面积（亩）<span class="num">{{dataObj.parkArea}}</span>
        </div>
        <div class="w-item">
          生产厂房（栋）<span class="num">{{dataObj.workshopToal}}</span>
        </div>
        <div class="w-item">
          宿舍楼数（栋）<span class="num">{{dataObj.dormitoryToTal}}</span>
        </div>
        <div class="w-item">
          餐厅楼数（栋）<span class="num">{{dataObj.diningRoomTotal}}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  data() {
    return {}
  },
  props: {
    dataObj:{
      type: Object,
      default: function(){
        return {}
      }
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
    background-image: url('/img/panel_new/view/bg_1.png');
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
