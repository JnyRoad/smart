<!--
- @name 审批流程
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-13
-->

<template>
  <div class="process">
    <div class="process_item succeed">
      <div class="block">
        <div class="row1">
          <div class="title">提交审批</div>
          <div class="name">张三</div>
        </div>
        <div class="row2">
          <div class="time">2020/08/13 17:50:00</div>
        </div>
      </div>
      <div class="line">
        <div class="line_dot"></div>
        <div class="line_line"></div>
      </div>
    </div>
    <div class="process_item failed">
      <div class="block">
        <div class="row1">
          <div class="title">室友确认</div>
          <div class="name">李四</div>
          <div class="status">
            <div class="status-tag status-tag--fail">已拒绝</div>
          </div>
        </div>
        <div class="row2">
          <div class="time">2020/08/13 17:50:00</div>
        </div>
      </div>
      <div class="line">
        <div class="line_dot"></div>
        <div class="line_line"></div>
      </div>
    </div>
    <div class="process_item notReach">
      <div class="block">
        <div class="row1">
          <div class="title">宿管审批</div>
          <!-- <div class="name">李四</div>
          <div class="status">
            <div class="status-tag status-tag--fail">已拒绝</div>
          </div> -->
        </div>
        <!-- <div class="row2">
          <div class="time">2020/08/13 17:50:00</div>
        </div> -->
      </div>
      <div class="line">
        <div class="line_dot"></div>
        <!-- <div class="line_line"></div> -->
      </div>
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
  props: {},
  watch: {},
  methods: {

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
  .process{
    padding: rem(40) 0;
    .process_item{
      position: relative;
      padding-left: rem(50);
      .block{
        .row1{
          display: flex;
          font-size: 16px;
          margin-bottom: rem(30);
          .title{
            margin-right: rem(20);
          }
          .name{
            font-weight: bold;
            margin-right: rem(20);
          }
        }
        .row2{
          padding-bottom: rem(60);
          .time{
            color: #999;
          }
        }
      }
      .line{
        position: absolute;
        top: 2px;
        left: 0;
        bottom: 0;
        text-align: center;
        width: rem(30);
        display: flex;
        flex-direction: column;
        align-items: center;
        .line_dot{
          width: rem(15);
          height: rem(15);
          border-radius: 50%;
          background: #ddd;
        }
        .line_line{
          flex: 1;
          border-left: 1px dashed $TCE-Color-Success;
        }
      }
      &.succeed{
        .line{
          .line_dot{
            width: rem(30);
            height: rem(30);
            background: url('./img/icon_succeed.png') no-repeat;
            background-size: 100% 100%;
          }
          .line_line{
            border-color: $TCE-Color-Success;
          }
        }
      }
      &.failed{
        .line{
          .line_dot{
            width: rem(30);
            height: rem(30);
            background: url('./img/icon_failed.png') no-repeat;
            background-size: 100% 100%;
          }
          .line_line{
            border-color: $TCE-Color-Danger;
          }
        }
      }
      &.notReach{
        .block{
          .row1{
            .title{
              color: #999;
            }
          }
        }
        .line{
          // top: rem(15);
        }
      }
    }
  }
</style>
