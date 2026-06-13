<!--
- @name 返厂确认-详情
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-09-09
-->

<template>
  <div class="page3-detail">
    <div class="block">
      <div class="row name-info">
        <div class="name">{{detailInfo.name}}<span style="padding: 0 10px;">|</span>{{detailInfo.deptName}}</div>
        <div class="status-tag status-tag--gray">
          <template v-if="detailInfo.applyMain.sffcDesc==='是'">返厂</template>
          <template v-else>不返厂</template>
        </div>
      </div>
    </div>
    <div class="block">
      <div class="row">
        <div class="label">
          <tce-label-justify label="放行去处:"></tce-label-justify>
        </div>
        <div class="value"> {{detailInfo.applyMain.fxqcDesc}} </div>
      </div>
      <div class="row" >
        <div class="label">
          <tce-label-justify label="出发地点:"></tce-label-justify>
        </div>
        <div class="value"> {{detailInfo.applyMain.fxddDesc}} </div>
      </div>
      </div>
    <div class="block">
      <div class="row" style="padding-top: 0px;">
        <div class="label">
          <tce-label-justify label="到达地点:"></tce-label-justify>
        </div>
        <div class="value"> {{detailInfo.applyMain.ddddDesc}} </div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="放行事项:"></tce-label-justify>
        </div>
        <div class="value"> {{detailInfo.applyMain.fxsxDesc}} </div>
      </div>
    </div>
    <div class="block">
      <div class="row" style="padding-top: 0px;">
        <div class="label">
          <tce-label-justify label="放行类别:"></tce-label-justify>
        </div>
        <div class="value"> {{detailInfo.applyMain.wpfxlbDesc}} </div>
      </div>
    </div>
    <div class="block">
      <div class="row" style="padding-top: 0px;">
        <div class="label">
          <tce-label-justify label="放行人级别:"></tce-label-justify>
        </div>
        <div class="value"> {{detailInfo.applyMain.sqrjbDesc}} </div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="附件:"></tce-label-justify>
        </div>
        <div class="value">
          <div class="img" v-if="detailInfo.applyMain.fjsc">
            <tce-image :src="detailInfo.applyMain.fjsc" :width="0" :height="0"></tce-image>
          </div>
          <template v-else>
            -
          </template>
        </div>
      </div>
    </div>
    <template v-if="detailInfo.applyMain.fxsx === 0 || detailInfo.applyMain.fxsx===7">
      <div class="-line-20 -line-background"></div>
      <div class="block">
        <div class="row">
          <div class="label">
            <tce-label-justify label="放行人员:"></tce-label-justify>
          </div>
          <div class="value personOuter" @click="openPersonList">
            <personList :list="detailInfo.personDetailList"></personList>
          </div>
        </div>
      </div>
    </template>
    <template v-else>
      <div class="-line-20 -line-background"></div>
      <div class="block">
        <div class="row">
          <div class="label">
            <tce-label-justify label="放行物品:"></tce-label-justify>
          </div>
          <div class="value personOuter" @click="openGoodsList">
            <goodsList :list="detailInfo.thingDetailList"></goodsList>
          </div>
        </div>
      </div>
    </template>
    <div class="-line-20 -line-background"></div>
    <div class="block">
      <process :list="detailInfo.approvalProcess"></process>
    </div>
    <page3Bottom v-if="detailInfo.status === 4 && !detailInfo.backTime">
      <div slot="inner" class="btn-inner">
        <button @click="apply" class="tce-button tce-button--primary is-round">确认返厂</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import process from '../good-release-live/components/process/index'
import personList from '../good-release-work/components/person-tag-detail'
import goodsList from '../good-release-work/components/goods-tag'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { getDetailApi } from '@/services/goodRreleaseLive'
import { backConfirm } from '@/services/returnFactory'

export default {
  components: {
    process,
    personList,
    goodsList,
    page3Bottom
  },
  data() {
    return {
      detailInfo: {
        applyMain: {}
      }
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    /**
     * 详情
     */
    async getDetail() {
      this.$loading.show()
      const res = await getDetailApi(this.$route.query.id)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.detailInfo = res.data
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    /**
     * 确认返厂
     */
    async apply() {
      this.$loading.show('请稍后')
      const res = await backConfirm(this.$route.query.id)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.$router.push({
          path: '/xuchang/returnFactory',
          query: {
            curTabIndex: 1
          }
        })
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    /**
     * 人员列表
     */
    openPersonList() {
      this.$router.push({
        path: '/xuchang/goodReleaseWork/detailPersonList',
        query: {
          list: JSON.stringify(this.detailInfo.personDetailList)
        }
      })
    },
    /**
     * 物品列表
     */
    openGoodsList() {
      this.$router.push({
        path: '/xuchang/goodReleaseWork/detailGoodsList',
        query: {
          list: JSON.stringify(this.detailInfo.thingDetailList)
        }
      })
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.getDetail()
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
    .btn-inner{
      width: 100%;
      height: 100%;
      padding: 0 rem(52);
      display: flex;
      justify-content: center;
      align-items: center;
    }
    .personList{
      display: flex;
      .img{
        width: rem(120);
        height: rem(120);
        margin-right: rem(20);
      }
    }
  }
</style>
