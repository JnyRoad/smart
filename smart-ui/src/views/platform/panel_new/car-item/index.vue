
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t">车位动态</div>
      </div>
      <div class="wrap_cont">
        <div class="bar">
          <div class="f1">
            <span>可用车位（个）</span>
            <div>
              <span class="num">{{dataObj.parkingFreeCount}}</span>
              <span class="num1">/ {{dataObj.parkingCount}}</span>
            </div>
          </div>
          <el-progress :percentage="percent" color="#63C1FF" :show-text="false" :stroke-width="10"></el-progress>
        </div>
        <div class="tb">
          <div class="tb_t">
            <span>位置</span>
            <span>时间</span>
            <span>车牌号</span>
            <span>类型</span>
          </div>
          <div class="tb_c">
            <template v-for="(item, index) in dataObj.inOutRecords">
              <div class="tb_i" :class="{'tb_i_in': item.typeDes=='进'}" :key="index">
                <span>
                  <el-tooltip class="item" effect="light" :content="item.areaName" placement="top">
                    <span>{{item.areaName}}</span>
                  </el-tooltip>
                </span>
                <span>{{item.snapTime | getTime}}</span>
                <span>{{item.vehiclePlate}}</span>
                <span>{{item.typeDes}}</span>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getProportion } from "@/views/platform/panel_new/util.js";
export default {
  data() {
    return {};
  },
  props: {
    dataObj: {
      type: Object,
      default: () => ({}),
    },
  },
  filters: {
    getTime(date) {
      return date.substring(11);
    },
  },
  computed: {
    percent() {
      let val = getProportion(this.dataObj.parkingFreeCount, this.dataObj.parkingCount)
      if(!isNaN(val)){
        return val;
      }else{
        return 0
      }
    },
    usedCount() {
      return this.dataObj.parkingCount - this.dataObj.parkingFreeCount;
    },
  },
  mounted() {},
  methods: {},
};
</script>

<style lang="scss" scoped>
.wrap {
  width: 389px;
  height: 323px;
  background-image: url("/img/panel_new/park/bg_6.png");
  .num {
    font-size: 20px;
    padding-right: 10px;
    margin-top: -5px;
  }
  .num1 {
    opacity: 0.3;
  }
  &_cont {
    padding: 20px 20px 10px;
    display: flex;
    flex-direction: column;
    .bar {
      margin-bottom: 15px;
      .f1 {
        display: flex;
        justify-content: space-between;
        margin-bottom: 10px;
      }
    }
    .tb {
      flex: 1;
      display: flex;
      flex-direction: column;
      &_t {
        height: 30px;
        line-height: 30px;
        background: rgba(37, 74, 127, 0.5);
        display: flex;
        text-align: center;
        color: #a6d4ff;
        span {
          flex: 1;
        }
      }
      &_c {
        padding-top: 5px;
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-around;
        overflow: hidden;
        .tb_i {
          display: flex;
          text-align: center;
          color: #39cbbe;
          overflow: hidden;
          span {
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            padding: 0 2px;
          }
        }
        .tb_i_in {
          color: #ffce80;
        }
      }
    }
  }
}
</style>
