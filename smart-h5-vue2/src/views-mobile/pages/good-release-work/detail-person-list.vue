<!--
- @name 物品放行-办公区-详情-人员列表
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-09-10
-->
<template>
  <div class="page3">
    <div class="page3-list" v-if="goodsPersonInfo && goodsPersonInfo.length>0">
      <div class="top-tip">放行人员（{{goodsPersonInfo.length}}人）</div>
      <div class="item" v-for="(item, index) in goodsPersonInfo" :key="index" @click="detail(item)">
        <div class="item-title">
          <div class="item-title_left">{{item.xm}} {{item.gh}}</div>
        </div>
        <div class="item-info">
          <div class="item-info_row">
            <div class="item-info_label">
              <tce-label-justify label="离厂事由:"></tce-label-justify>
            </div>
            <div class="item-info_value -ellipsis">{{item.lcsy}}</div>
          </div>
          <div class="item-info_row">
            <div class="item-info_label">
              <tce-label-justify label="离厂时间:"></tce-label-justify>
            </div>
            <div class="item-info_value -ellipsis">{{item.lcrq}} {{item.lcsj}}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  components: {},
  data() {
    return {
      goodsPersonInfo: []
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    /**
     * 详情
     */
    detail(item) {
      this.$router.push({
        path: '/xuchang/goodReleaseWork/detailPerson',
        query: {
          itemObj: JSON.stringify(item)
        }
      })
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.goodsPersonInfo = JSON.parse(this.$route.query.list)
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
  .page3{
    background: $TCE-Background-Grey;
    width: 100%;
    min-height: 100%;
    padding: rem(20);
    .page3-list{
      .top-tip{
        color: #999;
        padding-top: rem(10);
        margin-bottom: rem(20);
      }
      .item{
        background: #fff;
        border-radius: 5px;
        padding: rem(40) rem(30) rem(20);
        margin-bottom: rem(20);
        .item-title{
          display: flex;
          justify-content: space-between;
          margin-bottom: rem(30);
          &_left{
            font-size: 16px;
            font-weight: bold;
          }
        }
        .item-info{
          &_row{
            display: flex;
            margin-bottom: rem(20);
          }
          &_label{
            color: #999;
            width: rem(120);
          }
          &_value{
            flex: 1;
            padding-left: rem(20);
          }
        }
      }
    }
    .holder{
      text-align: center;
      padding-top: rem(267);
      .inner{
        width: rem(484);
        height: rem(255);
        margin: 0 auto 15px;
        background: url('./img/holder.png') no-repeat;
        background-size: 100% auto;
      }
      p{
        color: #c0c0c0;
        font-size: 16px;
        margin-bottom: 10px;
      }
    }
    .btn-inner{
      width: 100%;
      height: 100%;
      padding: 0 rem(52);
      display: flex;
      justify-content: center;
      align-items: center;
      button{
        margin: 0 rem(20);
      }
    }
    .tce-icons--del{
      margin-left: rem(40);
    }
  }
</style>
