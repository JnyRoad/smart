<!--
- @name 回复记录
-->

<template>
  <div class="record">
    <div class="record-inner">
      <template v-for="(item, index) in list">
        <div class="record-item" :key="index">
          <div class="block">
            <div class="row">
              <div class="label">
                <tce-label-justify label="维修结果:"></tce-label-justify>
              </div>
              <div class="value">{{ item.replyStatusDesc }}</div>
            </div>
             <div class="row">
                <div class="label">
                <tce-label-justify label="维修时间:"></tce-label-justify>
                </div>
                <div class="value">{{ item.replyTime }}</div>
            </div>
            <div class="row">
              <div class="label">
                <tce-label-justify label="回复人:"></tce-label-justify>
              </div>
              <div class="value">{{ item.replyName }}</div>
            </div>
            <div class="row">
                <div class="label">
                <tce-label-justify label="回复内容:"></tce-label-justify>
                </div>
                <div class="value">{{ item.replyDesc }}</div>
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
      default: function () {
        return []
      }
    }
  },
  watch: {},
  methods: {
    // returnColor(s) {
    //   switch (s) {
    //     case '维修成功':
    //       return 'tag-green'
    //       break
    //     case '无法维修':
    //       return 'tag-red'
    //       break
    //   }
    // }
    returnColor(s) {
      const colorMap = {
        '维修成功': 'tag-green',
        '无法维修': 'tag-red'
      }
      return colorMap[s] || 'tag-unknown' // 假设默认颜色为 'tag-unknown'
    }
  },
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
  .pc_c1 {
    color: #74c288;
  }
  .pc_c2 {
    color: #f25c19;
  }
  .pc_c4 {
    color: #999;
  }
  .block {
    background: #fff;
    // padding: 0 rem(30);
    .row {
      display: flex;
      border-bottom: 1px solid #eee;
      padding: rem(30) 0;
      .label {
        color: #999;
        width: rem(130);
        flex: none;
      }
      .value {
        flex: 1;
        padding-left: rem(30);
        text-align: right;
      }
    }
    & .row:last-child {
      border: none;
    }
  }
  .tag-green {
    color: #07c160;
  }
  .tag-red {
    color: red;
  }
  .tag-unknown {
    color: #000;
  }
}
</style>
