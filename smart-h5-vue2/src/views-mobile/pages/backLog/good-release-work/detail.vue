<!--
- @name 待我审批-物品放行(生活区)-申请详情
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-09-09
-->

<template>
  <div class="page3-detail">
    <div class="block">
      <div class="row name-info">
        <div class="name">{{ detailInfo.name }}<span style="padding: 0 10px">|</span>{{ detailInfo.deptName }}</div>
        <div class="status-tag status-tag--gray">
          <template v-if="detailInfo.applyMain.sffcDesc === '是'">返厂</template>
          <template v-else>不返厂</template>
        </div>
      </div>
    </div>
    <div class="block">
      <div class="row">
        <div class="label">
          <tce-label-justify label="放行去处:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.applyMain.fxqcDesc }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="出发地点:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.applyMain.fxddDesc }}</div>
      </div>
    </div>
    <div class="block">
      <div class="row">
        <div class="label">
          <tce-label-justify label="到达地点:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.applyMain.ddddDesc }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="放行事项:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.applyMain.fxsxDesc }}</div>
      </div>
    </div>
    <div class="block">
      <div class="row">
        <div class="label">
          <tce-label-justify label="放行类别:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.applyMain.wpfxlbDesc }}</div>
      </div>
    </div>
    <div class="block">
      <div class="row">
        <div class="label">
          <tce-label-justify label="放行人级别:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.applyMain.sqrjbDesc }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="附件:"></tce-label-justify>
        </div>
        <div class="value">
          <div class="img" v-if="detailInfo.applyMain.fjsc">
            <tce-image :src="detailInfo.applyMain.fjsc" :width="72" :height="72" :previewImage="true"></tce-image>
          </div>
          <template v-else> - </template>
        </div>
      </div>
    </div>
    <template v-if="detailInfo.applyMain.fxsx === 0 || detailInfo.applyMain.fxsx === 7">
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
    <tce-form ref="tceForm">
      <tce-form-group v-if="detailInfo.isUploadImg === 0">
        <tce-form-item field="imgs" type="upload-image-base64" label="上传物品照片" placeholder="请输入"></tce-form-item>
      </tce-form-group>
      <div class="-line-20 -line-background"></div>
      <div class="block">
        <process :list="detailInfo.approvalProcess"></process>
      </div>
    </tce-form>
    <page3Bottom v-if="detailInfo.status === 2">
      <div slot="inner" class="btn-inner">
        <button @click="dealHandle(5)" class="tce-button tce-button--primary is-plain">拒 绝</button>
        <button @click="dealHandle(4)" class="tce-button tce-button--primary">通 过</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import process from '../../good-release-live/components/process/index'
import personList from '../../good-release-work/components/person-tag-detail'
import goodsList from '../../good-release-work/components/goods-tag'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { getDetailApi } from '@/services/goodRreleaseLive'
import { securityStatusUpdate } from '@/services/backLog'
import store from '@/store'

export default {
  components: {
    process,
    personList,
    goodsList,
    page3Bottom
  },
  data() {
    return {
      isScan: false, // 是否是扫码进来的
      detailInfo: {
        applyMain: {}
      },
      baseInfo: {},
      parkInfo: {},
      id: '',
      curTabIndex: 0
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
      if (res.code === 0 && res.data !== null) {
        this.detailInfo = res.data
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
        this.$router.push({
          path: '/xuchang/home'
        })
      }
    },
    dealHandle(status) {
      this.securityDeal(status)
    },
    /**
     * 保安审批
     */
    async securityDeal(status) {
      let formData = this.$refs.tceForm.formData
      let guardOneImg = ''
      let guardTwoImg = ''
      let guardThreeImg = ''
      // 需要上传照片
      if (this.detailInfo.isUploadImg === 0) {
        if (formData.imgs && formData.imgs.length > 0) {
          guardOneImg = formData.imgs[0] ? formData.imgs[0] : ''
          guardTwoImg = formData.imgs[1] ? formData.imgs[1] : ''
          guardThreeImg = formData.imgs[2] ? formData.imgs[2] : ''
        } else {
          this.$tceMobile.toast('请至少上传一张照片')
          return
        }
      }
      let obj = {
        guardOneImg: guardOneImg,
        guardTwoImg: guardTwoImg,
        guardThreeImg: guardThreeImg,
        id: this.id,
        parkId: this.parkInfo.id,
        status: status,
        badge: this.baseInfo.employeeBadge,
        remark: formData.remark
      }
      this.$loading.show('请稍后')
      const res = await securityStatusUpdate(obj)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        if (this.isScan) {
          this.$router.push({
            path: '/xuchang/home'
          })
        } else {
          this.$router.push({
            path: '/xuchang/backLog/goodReleaseLive',
            query: {
              curTabIndex: 1
            }
          })
        }
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
    if (this.$route.query.isScan) {
      this.isScan = this.$route.query.isScan
    }
    this.id = this.$route.query.id
    this.curTabIndex = this.$route.query.curTabIndex
    this.baseInfo = store.getters.userInfoBase
    this.parkInfo = store.getters.parkInfo
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
.page3-detail {
  background: $TCE-Background-Grey;
  width: 100%;
  min-height: 100%;
  padding: rem(20) rem(20) rem(200) rem(20);
  .block {
    background: #fff;
    padding: 0 rem(30);
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
      }
      .personOuter {
        position: relative;
        .personList ::v-deep {
          justify-content: flex-start;
          > div {
            margin: 0 rem(20) rem(20) 0;
          }
        }
        .-arrow {
          position: absolute;
          top: rem(50);
          right: rem(30);
        }
      }
      .img {
        width: rem(100);
        height: rem(100);
      }
    }
    // & .row:last-child {
    //   border: none;
    // }
    .name-info {
      display: block;
      padding-bottom: rem(10);
      .name {
        font-size: 16px;
        font-weight: bold;
      }
      .status-tag--gray {
        display: inline-block;
        margin-top: rem(20);
      }
    }
  }
  .page3-bottom {
    display: flex;
    justify-content: center;
    align-items: center;
  }
  .btn-inner {
    width: 90%;
    border-radius: rem(50);
    background: #fff;
    margin-top: rem(10);
    display: flex;
    justify-content: center;
    align-items: center;
    button {
      margin: 0 rem(20);
    }
  }
  .personList {
    display: flex;
    .img {
      width: rem(120);
      height: rem(120);
      margin-right: rem(20);
    }
  }
}
</style>
