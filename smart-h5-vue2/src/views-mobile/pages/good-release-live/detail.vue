<!--
- @name 物品放行-生活区-记录详情
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-13
-->

<template>
  <div class="page3-detail">
    <div class="block block-code" v-if="detailInfo.expire && detailInfo.status < 4">
      <div class="codeImg">
        <tce-image :src="holderOutFac" :width="0" :height="0"></tce-image>
      </div>
      <div class="tip">放行码已过期</div>
    </div>
    <div class="block block-code" v-if="detailInfo.status === 4">
      <div class="codeImg">
        <tce-image :src="holderOutFac" :width="0" :height="0"></tce-image>
      </div>
      <div class="tip">已出厂</div>
    </div>
    <div class="block block-code" v-if="detailInfo.qrCodePic && detailInfo.status == 2 && !detailInfo.expire">
      <div class="codeImg">
        <tce-image :src="'data:image/jpg;base64,' + detailInfo.qrCodePic" :width="0" :height="0"></tce-image>
      </div>
      <div class="tip">【温馨提示】在门卫处出示放行码</div>
    </div>
    <div class="-line-20 -line-background"></div>
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
    <div class="block">
      <process :list="detailInfo.approvalProcess"></process>
    </div>
    <div class="-line-20 -line-background" v-if="detailInfo.status === 4 || detailInfo.status === 5"></div>
    <div class="block" v-if="detailInfo.status === 4 || detailInfo.status === 5">
      <div class="row">
        <div style="font-weight: 600">放行信息</div>
      </div>
      <div class="record-item">
        <div class="block" style="padding: 0">
          <div class="row">
            <div class="label">
              <tce-label-justify label="状态:"></tce-label-justify>
            </div>
            <div class="value">{{ detailInfo.statusName }}</div>
          </div>
          <div class="row">
            <div class="label">
              <tce-label-justify label="放行人员:"></tce-label-justify>
            </div>
            <div class="value">{{ detailInfo.securityStaff }}</div>
          </div>
          <div class="row">
            <div class="label">
              <tce-label-justify label="离场时间:"></tce-label-justify>
            </div>
            <div class="value">{{ detailInfo.departureTime }}</div>
          </div>
          <div class="row">
            <div class="label">
              <tce-label-justify label="备注:"></tce-label-justify>
            </div>
            <div class="value">{{ detailInfo.remark }}</div>
          </div>
        </div>
      </div>
    </div>
    <div class="-line-20 -line-background"></div>
  </div>
</template>

<script>
import holderPerson from './img/user.png'
import holderGoods from './img/holder_goods.png'
import holderOutFac from './img/outFac.png'
import process from './components/process/index'
import { getDetailApi } from '@/services/goodRreleaseLive'

export default {
  components: {
    process
  },
  data() {
    return {
      holderGoods: holderGoods,
      holderPerson: holderPerson,
      holderOutFac: holderOutFac,
      detailInfo: {}
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
.page3-detail {
  background: $TCE-Background-Grey;
  width: 100%;
  min-height: 100%;
  padding: rem(20);
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
}
</style>
