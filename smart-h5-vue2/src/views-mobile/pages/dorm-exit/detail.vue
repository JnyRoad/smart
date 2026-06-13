<!--
- @name 退宿申请
-->

<template>
  <div class="page3-detail">
    <div class="block block-code" v-if="detailInfo.status == 2">
      <div class="codeImg">
        <tce-image :src="'data:image/jpg;base64,'+detailInfo.qrcode" :width="0" :height="0"></tce-image>
      </div>
       <div class="tip" v-if="expire !== ''">
        {{expire}}
      </div>
      <div class="tip">
        在门卫处出示放行码
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
    <div class="-line-20 -line-background"></div>
    <div class="block">
      <div class="row name-info">
        <div class="name">{{detailInfo.name}}</div>
        <div class="img">
          <tce-image :src="detailInfo.facePic? detailInfo.facePic: holderPerson" :width="72" :height="72"></tce-image>
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
        <div class="value"> {{detailInfo.quitReasonDesc}} </div>
      </div>
       <div class="row">
        <div class="label">
          <tce-label-justify label="预计离开时间:"></tce-label-justify>
        </div>
        <div class="value"> {{detailInfo.applyLeaveTime}} </div>
      </div>
       <div class="row">
        <div class="label">
          <tce-label-justify label="备注:"></tce-label-justify>
        </div>
        <div class="value"> {{detailInfo.remark}} </div>
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
        <div style="font-weight:600">审批流程</div>
      </div>
      <process :list="detailInfo.processRecord"></process>
    </div>
  </div>
</template>

<script>
import process from './components/process/index'
import { getDetail } from '@/services/dormExit'
import { GET_IMAGE_URL } from '../../../conf'
import holderPerson from './img/user.png'
import holderOutFac from './img/outFac.png'

export default {
  components: {
    process
  },
  data() {
    return {
      holderOutFac: holderOutFac,
      holderPerson: holderPerson,
      detailInfo: {},
      expire: ''
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
      const me = this
      this.$loading.show()
      const res = await getDetail(this.$route.query.id)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.detailInfo = res.data
        this.detailInfo['facePic'] = GET_IMAGE_URL + '/' + this.detailInfo['faceId']
        switch (this.detailInfo.status) {
          case 4:
            me.expire = '已同意出厂'
            break
          case 5:
            me.expire = '已拒绝出厂'
            break
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
    padding: rem(20);
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
          text-align: right;
        }
      }
      & .row:last-child{
        border: none
      }
    }
    .name-info{
      display: flex;
      align-items: center;
      justify-content: space-between;
      .name{
        font-size: 16px;
        font-weight: bold;
      }
      .img{
        width: rem(100);
        height: rem(100);
      }
    }
    .imgList{
      display: flex;
      .img{
        width: rem(120);
        height: rem(120);
        margin-right: rem(20);
      }
    }
    .block-code{
      padding: rem(70) 0 rem(50) 0;
      .codeImg{
        width: rem(500);
        height: rem(500);
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
