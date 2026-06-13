<template>
  <div class="page3">
    <div class="page-header">
      <div class="label" @click="getAll()">查询全部</div>
      <div class="value">
        <!-- <cube-select v-model="date" :options="options"> </cube-select> -->
        <div class="date-select" @click="showDatePicker">
          {{ date }}
          <i class="select-icon"></i>
        </div>
      </div>
    </div>
    <div class="page3-list">
      <cube-scroll ref="scroll" :options="options" :data="dataList" @pulling-down="onPullingDown" @pulling-up="onPullingUp">
        <div class="item" v-for="(item, index) in dataList" :key="index">
          <div class="item-info">
            <div class="page3_item">
              <div class="item_label">{{ item.staffName }}-{{ item.staffBadge }}</div>
              <div class="item_value">{{ item.statementDate }}</div>
            </div>
            <div class="page3_item">
              <div class="item_label">抄表月份</div>
              <div class="item_value">{{ item.meterMonth }}</div>
            </div>
            <div class="page3_item" v-for="(_item, _index) in item.cateInfos" :key="_index">
              <div class="item_label">房间{{ _item.cateName }}费</div>
              <div class="item_value">{{ _item.fee }}</div>
            </div>
            <div class="page3_item">
              <div class="item_label">总计</div>
              <div class="item_value">{{ item.totalFee }}</div>
            </div>
          </div>
        </div>
        <tce-empty v-if="!dataList || dataList.length === 0"></tce-empty>
      </cube-scroll>
    </div>
  </div>
</template>
<script>
// getWaterElecData
import { getWaterElecData } from '@/services/dorm'
export default {
  data() {
    return {
      dataList: [],
      current: 1,
      pages: 1,
      options: {
        pullDownRefresh: {
          threshold: 40,
          stop: 44,
          stopTime: 1000,
          txt: '更新成功'
        },
        pullUpLoad: true
      },
      date: null
    }
  },
  methods: {
    async getAll() {
      this.dataList = []
      this.date = '未选择'
      this.getList(true)
    },
    async getList(s = null) {
      const me = this
      const obj = {
        current: this.current,
        size: 10,
        statementMonth: this.date
      }
      if (s) {
        obj.statementMonth = ''
      }
      me.$loading.show()
      const res = await getWaterElecData(obj)
      me.$loading.hide()
      if (res.code === 0) {
        let d = res.data.records
        // d = [{
        //   staffName:'测试',
        //   staffBadge: '1',
        //   statementDate: '6',
        //   meterMonth: '6',
        //   cateInfos:[
        //     {cateName: '热水',fee: 0},
        //     {cateName: '冷水',fee: 47},
        //     {cateName: '电',fee: 24},
        //     ],
        //     totalFee: 111
        // }]
        if (d.length > 0) {
          d.forEach(el => {
            el.cateInfos = el.cateInfos.filter(item => item.cateName !== '热水')
            const a = el.cateInfos.filter(item => item.cateName === '冷水')
            a[0].cateName = '水'
          })
        }
        this.dataList = this.dataList.concat(d)
        this.pages = res.data.pages
      } else {
        this.$tceMobile.toast(res.message)
      }
    },
    showDatePicker() {
      if (!this.datePicker) {
        this.datePicker = this.$createDatePicker({
          value: new Date(),
          max: new Date(),
          format: { year: 'YYYY年', month: 'MM月', date: null },
          onSelect: this.selectHandle
        })
      }
      this.datePicker.show()
    },
    selectHandle(date, selectedVal, selectedText) {
      this.date = selectedVal[0] + '-' + selectedVal[1]
      // console.log(d)
      this.dataList = []
      this.getList()
    },
    formatDate(timestamp) {
      const year = timestamp.getFullYear()
      const month = timestamp.getMonth() + 1
      return year + '-' + month
    },
    // 下拉刷新
    onPullingDown() {
      // debugger
      this.current = 1
      this.dataList = []
      if (this.date === '未选择') {
        this.getList(true)
      } else {
        this.getList()
      }
    }, // 上拉加载
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
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.date = this.formatDate(new Date())
    this.getList()
  },
  /**
   * 生命周期 mounted
   */
  mounted() {},
  /**
   * 生命周期 beforeDestroy
   */
  beforeDestroy() {},
  computed: {},
  props: {},
  watch: {}
}
</script>
<style lang="scss" scoped>
.page3 {
  background: $TCE-Background-Grey;
  width: 100%;
  height: 100%;
  //   padding: 0 rem(20) rem(20);
  .page-header {
    height: 48px;
    line-height: 48px;
    background: #fff;
    border-bottom: 1px solid #ccc;
    display: flex;
    .label {
      flex: none;
      color: #0f74f0;
      padding-left: rem(20);
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
    .date-select {
      -webkit-box-sizing: border-box;
      box-sizing: border-box;
      padding: 10px 30px 10px 10px;
      border-radius: 2px;
      font-size: 14px;
      height: 100%;
      line-height: rem(40);
      color: #666;
      background-color: #fff;
      position: relative;
      text-align: right;
    }
    .select-icon {
      position: absolute;
      right: 8px;
      top: 50%;
      -webkit-transform: translateY(-50%);
      -ms-transform: translateY(-50%);
      transform: translateY(-50%);
      border-style: solid;
      border-color: #999 transparent transparent;
      border-width: 4px 4px 0;
      -webkit-transition: -webkit-transform 0.3s ease-in-out;
      transition: -webkit-transform 0.3s ease-in-out;
      -o-transition: transform 0.3s ease-in-out;
      transition: transform 0.3s ease-in-out;
      transition: transform 0.3s ease-in-out, -webkit-transform 0.3s ease-in-out;
    }
  }
  .page3-list {
    height: calc(100% - 50px);
    padding: 16px;
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
          margin-bottom: rem(20);
        }
        &_label {
          color: #333;
          width: rem(240);
        }
        &_value {
          flex: 1;
          padding-left: rem(20);
          text-align: right;
        }
        .page3_item {
          margin: rem(16) 0;
          .item_label {
            color: #333;
            width: rem(240);
            float: left;
            font-weight: bold;
          }
          .item_value {
            -webkit-box-sizing: border-box;
            box-sizing: border-box;
            position: relative;
            text-align: right;
          }
        }
      }
    }
  }
}
</style>
