<!--
- @name 待我审批-物品放行(生活区)-申请详情
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-09-09
-->

<template>
  <div class="page3-detail">
    <!-- <div class="block block-code" v-if="detailInfo.expire && detailInfo.status < 4">
      <div class="codeImg">
        <tce-image :src="holderOutFac" :width="0" :height="0"></tce-image>
      </div>
      <div class="tip">
        放行码已过期
      </div>
    </div>
    <div class="block block-code" v-if="detailInfo.status===4">
      <div class="codeImg">
        <tce-image :src="holderOutFac" :width="0" :height="0"></tce-image>
      </div>
      <div class="tip">
        已出厂
      </div>
    </div>
    <div class="-line-20 -line-background"></div> -->
    <div class="block">
      <div class="row name-info">
        <div class="name">{{ detailInfo.carrier }}</div>
        <div class="img">
          <tce-image :src="detailInfo.facePic ? detailInfo.facePic : holderPerson" :width="72" :height="72"></tce-image>
        </div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="物品类型:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.articlesTypeName }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="物品名称:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.articlesDesc }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="房间信息:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.dormitoryName }}{{ detailInfo.roomName }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="离厂时间:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.plannedDepartureTime }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="车牌号:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.licensePlate || '无' }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="备注信息:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.remarks || '无' }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="物品照片:"></tce-label-justify>
        </div>
        <div class="value imgList">
          <div class="img" v-if="detailInfo.oneImg">
            <tce-image :src="detailInfo.oneImg" :width="72" :height="72" :previewImage="true"></tce-image>
          </div>
          <div class="img" v-if="detailInfo.twoImg">
            <tce-image :src="detailInfo.twoImg" :width="72" :height="72" :previewImage="true"></tce-image>
          </div>
          <div class="img" v-if="detailInfo.threeImg">
            <tce-image :src="detailInfo.threeImg" :width="72" :height="72" :previewImage="true"></tce-image>
          </div>
        </div>
      </div>
    </div>
    <div class="-line-20 -line-background"></div>
    <tce-form ref="tceForm">
      <!-- 保安放行且配置需要上传图片时，才上传照片 isUploadImg：0 是，1 否-->
      <tce-form-group v-if="sort == 3 && detailInfo.isUploadImg === 0 && curTabIndex == 0 && detailInfo.status < 4">
        <tce-form-item field="imgs" type="upload-image-base64" label="上传物品照片" placeholder="请输入"></tce-form-item>
      </tce-form-group>
      <div class="block">
        <process :list="detailInfo.approvalProcess"></process>
      </div>
      <div class="-line-20 -line-background" v-if="curTabIndex == 0 && detailInfo.status < 4"></div>
      <tce-form-group v-if="curTabIndex == 0 && detailInfo.status < 4">
        <tce-form-item field="remark" requiredMessage="请输入" type="input" label="审批意见（选填）" placeholder="请输入"></tce-form-item>
      </tce-form-group>
    </tce-form>
    <div class="-line-20 -line-background"></div>
    <!-- 室友和宿管审批 -->
    <page3Bottom v-if="(sort == 1 || sort == 2) && curTabIndex == 0">
      <div slot="inner" class="btn-inner">
        <button @click="dealHandle(3)" class="tce-button tce-button--primary is-plain">拒 绝</button>
        <button @click="dealHandle(2)" class="tce-button tce-button--primary">通 过</button>
      </div>
    </page3Bottom>
    <!-- 保安审批 -->
    <page3Bottom v-if="sort == 3 && curTabIndex == 0 && detailInfo.status < 4">
      <div slot="inner" class="btn-inner">
        <button @click="dealHandle(5)" class="tce-button tce-button--primary is-plain">拒绝放行</button>
        <button @click="dealHandle(4)" class="tce-button tce-button--primary">确认放行</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import holderPerson from '../../good-release-live/img/user.png'
import holderGoods from '../../good-release-live/img/holder_goods.png'
import holderOutFac from '../../good-release-live/img/outFac.png'
import process from '../../good-release-live/components/process/index'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { getDetailApi } from '@/services/goodRreleaseLive'
import { staffStatusUpdate, securityStatusUpdate } from '@/services/backLog'
import store from '@/store'

export default {
  components: {
    process,
    page3Bottom
  },
  data() {
    return {
      isScan: false, // 是否是扫码进来的
      holderGoods: holderGoods,
      holderPerson: holderPerson,
      holderOutFac: holderOutFac,
      detailInfo: {},
      baseInfo: {},
      parkInfo: {},
      id: '',
      sort: 1, // 1当前室友审批节点，2当前是宿管审批节点，3当前是保安审批节点
      curTabIndex: 0 // 是否展示审批按钮 1隐藏 0展示
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
      let res = null
      try {
        res = await getDetailApi(this.id)
        this.$loading.hide()
        if (res.code === 0 && res.data !== null) {
          this.detailInfo = res.data
        } else {
          this.$tceMobile.toast(res.message || '网络错误')
          this.$router.push({
            path: '/xuchang/home'
          })
        }
      } catch (e) {
        this.$loading.hide()
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    dealHandle(status) {
      if (status === 2 || status === 3) {
        // 室友/宿管审批
        this.staffDeal(status)
      } else if (status === 4 || status === 5) {
        // 保安审批
        this.securityDeal(status)
      }
    },
    /**
     * 室友/宿管审批
     */
    async staffDeal(status) {
      let formData = this.$refs.tceForm.formData
      let obj = {
        approveBadge: this.baseInfo.employeeBadge,
        id: this.id,
        status: status,
        remark: formData.remark
      }
      this.$loading.show('请稍后')
      const res = await staffStatusUpdate(obj)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.$router.push({
          path: '/xuchang/backLog/goodReleaseLive',
          query: {
            curTabIndex: 1
          }
        })
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
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
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.sort = this.$route.query.sort
    this.id = this.$route.query.id
    if (this.$route.query.isScan) {
      this.isScan = this.$route.query.isScan
    }
    this.curTabIndex = this.$route.query.curTabIndex
    this.baseInfo = store.getters.userInfoBase
    this.parkInfo = store.getters.parkInfo
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
    }
    & .row:last-child {
      border: none;
    }
  }
  .name-info {
    display: flex;
    align-items: center;
    justify-content: space-between;
    .name {
      font-size: 16px;
      font-weight: bold;
    }
    .img {
      width: rem(100);
      height: rem(100);
    }
  }
  .imgList {
    display: flex;
    .img {
      width: rem(120);
      height: rem(120);
      margin-right: rem(20);
    }
  }
  .block-code {
    padding: rem(70) 0 rem(50) 0;
    .codeImg {
      width: rem(500);
      height: rem(500);
      margin: 0 auto;
    }
    .tip {
      margin-top: rem(30);
      color: #999;
      text-align: center;
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
}
</style>
