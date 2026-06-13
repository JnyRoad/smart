<!--
- @name 审批流程
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-09-08
-->

<template>
  <div class="record">
    <div class="record-inner">
      <template v-for="(item, index) in list">
        <div class="record-item" :key="index">
          <div class="record-item__left">
            <div class="num"></div>
            <i class="line1"></i>
          </div>
          <!-- 提交节点 -->
          <div class="record-item__right" v-if="item.recordNode===0">
            <div>
              <span style="font-weight: bold;">{{ item.staffInfos[0].staffName }}-</span>
              <span class="pc_c0">{{ item.staffInfos[0].resultDesc }}</span>
            </div>
            <div class="line2">
              {{ item.staffInfos[0].createDate }}
            </div>
          </div>
          <!-- 审批节点 -->
          <div class="record-item__right" v-else>
            <div>
              {{item.statusName}}
            </div>
            <div v-for="(item2, index2) in item.staffInfos" :key="index2" style="margin-top: 10px">
              <div style="font-size: 14px;">
                {{item2.staffName}}-
                <!-- result：0 待审批 1 通过 2 拒绝 3关闭 4 等待 -->
                <span v-if="item2.result===0" class="pc_c0">{{item2.resultDesc}}</span>
                <span v-if="item2.result===1" class="pc_c1">{{item2.resultDesc}}</span>
                <span v-if="item2.result===2 || item2.result===3" class="pc_c2">{{item2.resultDesc}}</span>
                <span v-if="item2.result===4" class="pc_c4">{{item2.resultDesc}}</span>
              </div>
              <div class="line2" v-if="item2.remark && item2.remark != null">
                意见: {{ item2.remark}}
              </div>
              <div class="line2">
                {{ item2.recordDate || item2.createDate}}
              </div>
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script>

export default {
  components: {},
  data() {
    return {}
  },
  computed: {},
  props: {
    list: {
      type: Array,
      default: function() {
        return []
      }
    }
  },
  watch: {},
  methods: {},
  /**
   * 生命周期 created
   */
  created() {},
  /**
   * 生命周期 mounted
   */
  mounted() {},
  /**
   * 生命周期 beforeDestroy
   */
  beforeDestroy() {}
}
</script>

<style lang="scss" scoped>
  .record {
    position: relative;
    padding: rem(40) 0 rem(20);
    .pc_c0{
      color: #508BFF;
    }
    .pc_c1{
      color: #74C288;
    }
    .pc_c2{
      color: #F25C19;
    }
    .pc_c4{
      color: #999
    }
    &-item {
      display: flex;
      &__left {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding-right: 8px;
        .num {
          width: 18px;
          height: 18px;
          text-align: center;
          line-height: 18px;
          background: url('./img/p_1.png');
          background-size: 100% 100%;
        }
        .line1 {
          flex: 1;
          width: 1px;
          border-left: 1px dashed #e0e0e0;
        }
      }
      &__right {
        .line2 {
          font-size: 12px;
          color: #666;
          margin: 5px 0 15px 0;
        }
      }
    }
    .record-inner{
      .record-item:last-child{
        .record-item__left {
          .line1 {
            display: none;
          }
        }
      }
    }
  }
</style>
