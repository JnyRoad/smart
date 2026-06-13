
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t">访客实时</div>
      </div>
      <div class="wrap_cont">
        <div class="c_top">
          <div class="w-item">
            今日累计进厂（人）
            <span class="num">{{ dataObj.inCount }}</span>
          </div>
          <div class="w-item">
            今日累计出厂（人）
            <span class="num">{{ dataObj.outCount }}</span>
          </div>
        </div>
        <div class="tb">
          <div class="tb_t">
            <span>位置</span>
            <span>进入园区</span>
            <span>离开园区</span>
          </div>
          <div class="tb_c">
            <template v-for="(item, index) in dateList">
              <div class="tb_i tb_i_in" :key="index">
                <span >
                  <el-tooltip effect="light" :content="item.areaName" placement="top">
                    <span>{{item.areaName}}</span>
                  </el-tooltip>
                </span>
                <span>{{item.inCount}}</span>
                <span>{{item.outCount}}</span>
              </div>
            </template>
            <template v-if="dateList&&dateList.length==0">
              <div class="tip">暂无访客实时数据</div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  data() {
    return {};
  },
  props: {
    dataObj: {
      type: Object,
      default: () => ({}),
    }
  },
  computed:{
    dateList(){
      let Arr = [];
      if(this.dataObj.inOutRecords){
        Arr = this.dataObj.inOutRecords
        if(Arr.length>4){
          return Arr.slice(0, 4)
        }else{
          return Arr
        }
      }
    }
  },
  mounted() {},
  methods: {}
};
</script>

<style lang="scss" scoped>
.wrap {
  width: 282px;
  height: 296px;
  background-image: url("/img/panel_new/person/bg_6.png");
  &_top{
    line-height: 32px !important;
  }
  .num {
    width: 80px;
    text-align: right;
  }
  &_cont {
    display: flex;
    flex-direction: column;
    padding: 23px 10px 10px 15px;
    .w-item {
      overflow: hidden;
      margin-bottom: 12px;
      display: flex;
      justify-content: space-between;
      padding-right: 15px;
    }
    .tb{
      flex: 1;
      display: flex;
      flex-direction: column;
      margin-top: 10px;
      &_t{
        height: 30px;
        line-height: 30px;
        background: rgba(37, 74, 127, .5);
        display: flex;
        text-align: center;
        color: #a6d4ff;
        span{
          flex: 1;
        }
      }
      &_c{
        padding-top: 5px;
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-around;
        overflow: hidden;
        .tb_i{
          display: flex;
          text-align: center;
          color: #39cbbe;
          overflow: hidden;
          span{
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            padding: 0 2px;
          }
        }
        .tb_i_in{
          color: #ffce80;
        }
      }
    }
    .tip{
      text-align: center;
      opacity: 0.7;
    }
  }
}
</style>
