<!--
- @name 宿舍报修
-->

<template>
  <div class="page3-detail">
    <div class="-line-20 -line-background"></div>
    <div class="block">
      <div class="row">
        <div class="label">
          <tce-label-justify label="状态:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.statusDesc }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="报修时间:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.createTime }}</div>
      </div>
    </div>
    <div class="-line-20 -line-background"></div>
    <div class="block">
      <div class="row">
        <div style="font-weight: 600">报修信息</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="姓名:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.name }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="工号:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.staffBadge }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="BU:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.compName }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="部门:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.depName }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="维修区域:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.rangeTypeDesc }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="维修类别:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.repairTypeDesc }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="维修位置:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.dormitoryName }}#{{ detailInfo.roomName }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="所在园区:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.parkName }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="故障描述:"></tce-label-justify>
        </div>
        <div class="value">{{ detailInfo.faultDesc }}</div>
      </div>
      <div class="row">
        <div class="label">
          <tce-label-justify label="物品照片:"></tce-label-justify>
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
      <process :list="detailInfo.approvalProcess"></process>
    </div>
    <div class="-line-20 -line-background"></div>
    <div class="block" style="padding-bottom: 20px" v-if="curTabIndex == 0">
      <div class="row">
        <div style="font-weight: 600">描述(非必填)</div>
      </div>
      <cube-textarea v-model="remark" :placeholder="placeholder" :maxlength="maxlength" :autofocus="autofocus"></cube-textarea>
    </div>
    <div class="block" v-if="curTabIndex == 1 && detailInfo.repairReplyList !== null">
      <div class="row">
        <div style="font-weight: 600">维修结果</div>
      </div>
      <replyRecord :list="detailInfo.repairReplyList"></replyRecord>
    </div>
    <page3Bottom v-if="detailInfo.status === 0">
      <div slot="inner" class="btn-inner">
        <button @click="dealHandle(2)" class="tce-button tce-button--primary is-plain">不接单</button>
        <button @click="dealHandle(1)" class="tce-button tce-button--primary">接单</button>
      </div>
    </page3Bottom>
    <page3Bottom v-if="detailInfo.status === 1">
      <div slot="inner" class="btn-inner">
        <button @click="_dealHandle(4)" class="tce-button tce-button--primary is-plain">无法维修</button>
        <button @click="_dealHandle(3)" class="tce-button tce-button--primary">已安排维修</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import process from './components/process/index'
import replyRecord from './components/process/reply-record'
import { getDetail } from '@/services/dormRepairs'
import { confirmDormRepairs, _confirmDormRepairs } from '@/services/backLog'
import page3Bottom from '@/views-mobile/components/page3-bottom'
import store from '@/store'

export default {
  components: {
    process,
    replyRecord,
    page3Bottom
  },
  data() {
    return {
      baseInfo: {},
      detailInfo: {},
      remark: '',
      placeholder: '请输入内容',
      maxlength: 100,
      autofocus: true
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
      const res = await getDetail(this.$route.query.id)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.detailInfo = res.data
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    async dealHandle(s) {
      const obj = {
        approveBadge: this.baseInfo.employeeBadge,
        id: this.$route.query.id,
        remark: this.remark,
        status: s
      }
      this.$loading.show()
      const res = await confirmDormRepairs(obj)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.$router.push({
          path: '/xuchang/backLog/dormRepairs',
          query: {
            curTabIndex: 0
          }
        })
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    async _dealHandle(s) {
      const obj = {
        approveBadge: this.baseInfo.employeeBadge,
        id: this.$route.query.id,
        result: this.remark,
        status: s
      }
      this.$loading.show()
      const res = await _confirmDormRepairs(obj)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.$router.push({
          path: '/xuchang/backLog/dormRepairs',
          query: {
            curTabIndex: 1
          }
        })
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
    this.curTabIndex = this.$route.query.curTabIndex
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
