<!--
- @name 物品放行-办公区-详情-物品列表-详情
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-09-10
-->
<template>
<div class="page3-detail">
    <div class="block">
      <div class="row">
        <div class="label">
          <tce-label-justify label="资产编码:"></tce-label-justify>
        </div>
        <div class="value"> {{itemInfo.wpbm}} </div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="名称:"></tce-label-justify>
        </div>
        <div class="value"> {{itemInfo.wpmc}} </div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="单位:"></tce-label-justify>
        </div>
        <div class="value"> {{itemInfo.wpdw}} </div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="数量:"></tce-label-justify>
        </div>
        <div class="value"> {{itemInfo.wpsl}}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="接收单位:"></tce-label-justify>
        </div>
        <div class="value"> {{itemInfo.jsdw}}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="放行日期:"></tce-label-justify>
        </div>
        <div class="value"> {{itemInfo.fxrq}}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="备注(原因):"></tce-label-justify>
        </div>
        <div class="value"> {{itemInfo.bz}}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="运输方式:"></tce-label-justify>
        </div>
        <div class="value"> {{itemInfo.ysfsDesc}}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="姓名:"></tce-label-justify>
        </div>
        <div class="value"> {{itemInfo.xm}}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="车牌号:"></tce-label-justify>
        </div>
        <div class="value"> {{itemInfo.cph}}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { other } from '@tce/tce-util'
import { transportTypeOption } from './const'

export default {
  components: {},
  data() {
    return {
      itemInfo: {}
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {},
  /**
   * 生命周期 created
   */
  created() {
    this.itemInfo = JSON.parse(this.$route.query.itemObj)
    this.itemInfo.ysfsDesc = other.findByvalue(transportTypeOption, parseInt(this.itemInfo.ysfs))
  },
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
        .personOuter{
          position: relative;
          .personList ::v-deep{
            justify-content: flex-start;
            >div{
              margin: 0 rem(20) rem(20) 0;
            }
          }
          .-arrow{
            position: absolute;
            top: rem(50);
            right: rem(30);
          }
        }
        .img{
          width: rem(100);
          height: rem(100);
        }
      }
      & .row:last-child{
        border: none
      }
      .name-info{
        display: block;
        padding-bottom: rem(10);
        .name{
          font-size: 16px;
          font-weight: bold;
        }
        .status-tag--gray{
          display: inline-block;
          margin-top: rem(20);
        }
      }
    }
  }
</style>
