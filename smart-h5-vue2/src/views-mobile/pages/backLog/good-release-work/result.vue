<!--
- @name 待我审批-物品放行-审批结果
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-27
-->

<template>
  <div class="page3-detail">
    <div class="block block-code">
      <div class="codeImg">
        <tce-image :width="0" :height="0"></tce-image>
      </div>
      <div class="tip"> 您已通过物品放行审批 </div>
    </div>
    <div class="block">
      <div class="row name-info">
        <div class="name">张三<span style="padding: 0 10px;">|</span>研发部</div>
        <div class="status-tag status-tag--gray">不返厂</div>
      </div>
    </div>
    <div class="block">
      <div class="row" >
        <div class="label">
          <tce-label-justify label="放行事项:"></tce-label-justify>
        </div>
        <div class="value"> test </div>
      </div>
      </div>
    <div class="block">
      <div class="row" style="padding-top: 0px;">
        <div class="label">
          <tce-label-justify label="放行时间:"></tce-label-justify>
        </div>
        <div class="value"> test </div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="OA节点:"></tce-label-justify>
        </div>
        <div class="value"> test </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
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
  .page3-detail{
    background: $TCE-Background-Grey;
    width: 100%;
    min-height: 100%;
    padding: rem(20) rem(20) rem(200) rem(20);
    .block{
      background: #fff;
      padding: 0 rem(30);
      .row{
        display: flex;
        border-bottom: 1px solid #eee;
        padding: rem(30) 0;
        .label{
          color: #999;
          width: rem(130);
          flex: none;
        }
        .value{
          flex: 1;
          padding-left: rem(30);
        }
      }
      & .row:last-child{
        border: none
      }
      .name-info{
        display: flex;
        justify-content: space-between;
        padding-bottom: rem(10);
        .name{
          font-size: 16px;
          font-weight: bold;
        }
        .status-tag--gray{
          display: inline-block;
        }
      }
    }
    .block-code{
      background: transparent;
      padding: rem(70) 0 rem(50) 0;
      .codeImg{
        width: rem(500);
        height: rem(500);
        border: 1px solid red;
        margin: 0 auto;
      }
      .tip{
        margin-top: rem(30);
        color: #999;
        text-align: center;
      }
    }
  }
</style>
