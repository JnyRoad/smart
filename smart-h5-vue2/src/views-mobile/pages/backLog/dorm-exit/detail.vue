<!--
- @name 退宿申请
-->

<template>
  <div class="page3-detail">
    <div class="-line-20 -line-background"></div>
    <div class="block">
      <div class="row name-info">
        <div class="name">{{ detailInfo.name }}</div>
        <div class="img">
          <tce-image :src="detailInfo.facePic ? detailInfo.facePic : holderPerson" :width="72" :height="72"></tce-image>
        </div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="房间信息:"></tce-label-justify>
        </div>
        <div class="value">
          <ul>
            <li class="roomLi" v-for="(_item, index) in detailInfo.dorDetailStr" :key="index">
              {{ _item }}
            </li>
          </ul>
        </div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="退宿原因:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.quitReasonDesc }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="预计离开时间:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.applyLeaveTime }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="备注:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.remark }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="照片:"></tce-label-justify>
        </div>
        <div class="value imgList">
          <div class="img" v-if="detailInfo.imgs && detailInfo.imgs[0]">
            <tce-image :src="detailInfo.imgs[0]" :width="72" :height="72" :previewImage="true"></tce-image>
          </div>
          <div class="img" v-if="detailInfo.imgs && detailInfo.imgs[1]">
            <tce-image :src="detailInfo.imgs[1]" :width="72" :height="72" :previewImage="true"></tce-image>
          </div>
          <div class="img" v-if="detailInfo.imgs && detailInfo.imgs[2]">
            <tce-image :src="detailInfo.imgs[2]" :width="72" :height="72" :previewImage="true"></tce-image>
          </div>
        </div>
      </div>
    </div>
    <div class="-line-20 -line-background"></div>
    <div class="block">
      <div class="row">
        <div style="font-weight: 600">审批流程</div>
      </div>
      <process :list="detailInfo.processRecord"></process>
    </div>
    <div class="-line-20 -line-background" v-if="curTabIndex == 0"></div>
    <div class="block" style="padding-bottom: 20px" v-if="curTabIndex == 0">
      <div class="row">
        <div style="font-weight: 600">描述(非必填)</div>
      </div>
      <cube-textarea v-model="remark" :placeholder="placeholder" :maxlength="maxlength" :autofocus="autofocus"></cube-textarea>
    </div>
    <page3Bottom v-if="detailInfo.status === 1 && curTabIndex == 0">
      <div slot="inner" class="btn-inner">
        <button @click="dealHandle(3)" class="tce-button tce-button--primary is-plain">拒绝</button>
        <button @click="dealHandle(2)" class="tce-button tce-button--primary">通过</button>
      </div>
    </page3Bottom>
    <page3Bottom v-if="detailInfo.status === 2 && curTabIndex == 0">
      <div slot="inner" class="btn-inner">
        <button @click="dealHandle(5)" class="tce-button tce-button--primary is-plain">拒绝</button>
        <button @click="dealHandle(4)" class="tce-button tce-button--primary">保安通过</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import process from './components/process/index'
import { getDetail, getScanDetail } from '@/services/dormExit'
import { confirmDormExit } from '@/services/backLog'
// confirmDormExit
import { GET_IMAGE_URL } from '../../../../conf'
import holderPerson from './img/user.png'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import store from '@/store'

export default {
  components: {
    process,
    page3Bottom
  },
  data() {
    return {
      baseInfo: {},
      holderPerson: holderPerson,
      detailInfo: {},
      remark: '',
      placeholder: '请输入内容',
      maxlength: 50,
      autofocus: true,
      curTabIndex: 1, // 是否展示审批按钮 1隐藏 0展示
      isScan: false // true 扫码进入 保安详情接口修改
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
      let res
      if (this.isScan) {
        res = await getScanDetail(this.$route.query.id)
        this.curTabIndex = 0
      } else {
        res = await getDetail(this.$route.query.id)
      }
      this.$loading.hide()
      if (res.code === 0 && res.data !== null) {
        this.detailInfo = res.data
        this.detailInfo['facePic'] = GET_IMAGE_URL + '/' + this.detailInfo['faceId']
        if (this.detailInfo.isApprove) {
          this.curTabIndex = 0
        }
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
        this.$router.push({
          path: '/xuchang/home'
        })
      }
    },
    async dealHandle(s) {
      const obj = {
        id: this.detailInfo.id,
        remark: this.remark,
        status: s,
        approveBadge: this.baseInfo.employeeBadge
      }
      this.$loading.show()
      const res = await confirmDormExit(obj)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        if (this.isScan) {
          this.$router.push({
            path: '/xuchang/home'
          })
        } else {
          this.$router.push({
            path: '/xuchang/backLog/dormExit',
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
    if (this.$route.query.isScan) {
      this.isScan = this.$route.query.isScan
    }
    this.baseInfo = store.getters.userInfoBase
    // this.curTabIndex = this.$route.query.curTabIndex

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
        text-align: right;
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
