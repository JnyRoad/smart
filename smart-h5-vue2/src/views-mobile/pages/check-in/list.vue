<!--
- @name 物品放行-生活区-查看数据
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-13
-->

<template>
  <div class="page3">
    <page3Tab :list="tabList" :curIndex="1" @doApply="doApply" @checkList="checkList"></page3Tab>
    <div class="page3-list">
      <cube-scroll ref="scroll" :options="options" :data="dataList" @pulling-down="onPullingDown">
        <div class="item" v-for="(item, index) in dataList" :key="index">
          <div class="item-info">
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="宿舍楼栋:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis" style="text-align: right">{{ item.dormitoryName }}</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="房间号:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis" style="text-align: right">{{ item.roomName }}房间</div>
            </div>
            <div class="item-info_row">
              <div class="item-info_label">
                <tce-label-justify label="床位:"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis" style="text-align: right">{{ item.bedNumber }}号床</div>
            </div>
            <div class="item-info_row" style="border: 1px solid #ccc; padding: 12px">
              <div class="item-info_label">
                <tce-label-justify label="身份特征检查" style="color: #000"></tce-label-justify>
              </div>
              <div class="item-info_value -ellipsis" style="margin-top: 30px">
                <div style="height: 32px; line-height: 32px">
                  <div>
                    <div style="float: left; width: 60px">指纹:</div>
                    <div v-bind:class="returnColor(item.lockPwd.fingerprintCode)"></div>
                    <div style="float: left; width: 80px; margin-left: 12px">{{ item.lockPwd.fingerprintDesc }}</div>
                  </div>
                </div>
                <div style="height: 36px; line-height: 36px; margin-top: 8px">
                  <div>
                    <div style="float: left; width: 60px">动态码:</div>
                    <div v-bind:class="returnColor(item.lockPwd.dynamicCode)"></div>
                    <div style="float: left; width: 80px; margin-left: 12px" v-if="item.lockPwd.dynamicCode === 3">已录入</div>
                    <div style="float: left; width: 80px; margin-left: 12px" v-else>{{ item.lockPwd.dynamicDesc }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div class="page-num" v-if="item.lockPwd.fingerprintCode!==0&&num">{{num}}</div>
          </div>
        </div>
        <tce-empty v-if="!dataList || dataList.length === 0"></tce-empty>
      </cube-scroll>
    </div>
  </div>
</template>

<script>
import page3Tab from '@/views-mobile/components/page3-tab'
import { getRoomDetail } from '@/services/checkIn'
import { getPwd } from '@/services/lock'
import store from '@/store'
import { decryption } from '@/util/encryption'

export default {
  components: {
    page3Tab
  },
  data() {
    return {
      tabList: [
        {
          label: '发起提交',
          value: 0
        },
        {
          label: '查看数据',
          value: 1
        }
      ],
      current: 1,
      pages: 1,
      dataList: [],
      options: {
        pullDownRefresh: {
          threshold: 60,
          stop: 44,
          stopTime: 1000,
          txt: '更新成功'
        },
        pullUpLoad: true
      },
      baseInfo: {},
      num: null
    }
  },
  computed: {},
  props: {},
  watch: {},
  filters: {
    decryption(str) {
      if (str) {
        return decryption(str)
      } else {
        return null
      }
    }
  },
  methods: {
    /**
     * 获取列表
     */
    async getList() {
      this.$loading.show()
      const res = await getRoomDetail(this.baseInfo.employeeBadge)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.dataList = res.data
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    },
    async getPwd() {
      const baseInfo = store.getters.userInfoBase
      const employeeBadge = baseInfo.employeeBadge
      const res = await getPwd({ badge: employeeBadge })
      if (res.data !== '') {
        this.num = this.decryption(res.data)
      } else {
        this.num = null
      }
    },
    returnColor(s) {
      switch (s) {
        case 0:
          return 'fail'
        case 1:
          return 'success'
        case 2:
          return 'fail'
        case 3:
          return 'success'
        case 4:
          return 'fail'
      }
    },
    // 下拉刷新
    onPullingDown() {
      // debugger
      this.current = 1
      this.dataList = []
      this.getList()
    },
    // 上拉加载
    onPullingUp() {
      // debugger
      this.clearPullingUp && clearTimeout(this.clearPullingUp)
      this.clearPullingUp = setTimeout(() => {
        if (this.current < this.pages) {
          this.current++
          this.getList()
        } else {
          this.$refs.scroll.forceUpdate(false, true)
        }
      }, 1000)
    },
    /**
     * 发起提交
     */
    doApply() {
      this.$router.push({
        path: '/xuchang/checkIn'
      })
    },
    /**
     * 查看数据
     */
    checkList() {
      this.$router.push({
        path: '/xuchang/checkIn/detail'
      })
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
    this.getList()
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
.page3 {
  background: $TCE-Background-Grey;
  width: 100%;
  height: 100%;
  padding: rem(120) rem(20) rem(20);
  .page3-list {
    height: 100%;
    .item {
      background: #fff;
      border-radius: 5px;
      padding: rem(30) rem(30) rem(20);
      margin-bottom: rem(20);
      .item-title {
        display: flex;
        justify-content: space-between;
        margin-bottom: rem(30);
        &_left {
          font-size: 16px;
          font-weight: bold;
        }
      }
      .item-info {
        &_row {
          display: flex;
          margin-bottom: rem(15);
        }
        &_label {
          color: #999;
          width: rem(120);
        }
        &_value {
          flex: 1;
          padding-left: rem(20);
        }
      }
      .features-cont {
        border: 1px solid #ccc;
        padding: rem(16);
      }
      .form-title {
        width: 100%;
      }
      .form-block {
        background: #fff;
        height: 45px;
        line-height: 45px;
        padding-left: 10px;
        .form-row {
          display: flex;
          height: 100%;
          border-bottom: 1px solid #eeeeee;
          .label {
            flex: none;
            color: #666666;
          }
          .value {
            flex: 1;
            padding-left: rem(30);
            .form-btn {
              height: 75%;
              width: 40%;
              margin: 0 4%;
            }
          }
        }
      }
      .success {
        width: 32px;
        height: 32px;
        text-align: center;
        line-height: 32px;
        background: url('./img/p_1.png');
        background-size: 100% 100%;
        // position: relative;
        margin-left: rem(20);
        float: left;
      }
      .fail {
        width: 32px;
        height: 32px;
        text-align: center;
        line-height: 32px;
        background: url('./img/p_3.png');
        background-size: 100% 100%;
        // position: relative;
        margin-left: rem(20);
        float: left;
      }
    }
  }
}
.page-num {
  text-align: center;
  letter-spacing: 2px;
  font-size: 18px;
  font-weight: bold;
}
.item-info_label {
  min-width: 70px;
}
.tag-green {
  color: #07c160;
}
.tag-red {
  color: red;
}
</style>
