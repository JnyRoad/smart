<!--
- @name 物品放行-办公区-添加人员-列表
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-26
-->

<template>
  <div class="page3">
    <div class="page3-list" v-if="releaseGoodsInfo && releaseGoodsInfo.length>0">
      <div class="top-tip">已添加放行物品（{{releaseGoodsInfo.length}}项）</div>
      <div class="item" v-for="(item, index) in releaseGoodsInfo" :key="index">
        <div class="item-title">
          <div class="item-title_left">{{item.wpmc}} {{item.wpbm}}</div>
          <div class="item-title_right">
            <span class="tce-icons tce-icons--edit" @click="edit(item, index)"></span>
            <span class="tce-icons tce-icons--del" @click="del(index)"></span>
          </div>
        </div>
        <div class="item-info">
          <div class="item-info_row">
            <div class="item-info_label">
              <tce-label-justify label="单位"></tce-label-justify>
            </div>
            <div class="item-info_value -ellipsis">{{item.wpdw}}</div>
          </div>
          <div class="item-info_row">
            <div class="item-info_label">
              <tce-label-justify label="放行日期"></tce-label-justify>
            </div>
            <div class="item-info_value -ellipsis">{{item.fxrq}}</div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="holder">
      <div class="inner"></div>
      <div>
        <p>暂无放行物品信息</p>
        <p>请点击下方按钮添加放行物品</p>
      </div>
    </div>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="add" class="tce-button tce-button--primary is-round is-plain">新增物品</button>
        <button @click="sure" class="tce-button tce-button--primary is-round">确 定</button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Bottom from '@/views-mobile/components/page3-bottom'

export default {
  components: {
    page3Bottom
  },
  data() {
    return {
      releaseGoodsInfo: [],
      itemInfo: {},
      itemIndex: '',
      isEdit: false
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    /**
     * 添加
     */
    add() {
      this.$router.push({
        path: '/xuchang/goodReleaseWork/addGoods'
      })
    },
    /**
     * 确定
     */
    sure() {
      this.$router.push({
        path: '/xuchang/goodReleaseWork'
      })
    },
    /**
     * 编辑
     */
    edit(item, index) {
      this.$router.push({
        path: '/xuchang/goodReleaseWork/addGoods',
        query: {
          itemInfo: JSON.stringify(item),
          itemIndex: index,
          isEdit: true
        }
      })
    },
    /**
     * 删除
     */
    del(index) {
      this.releaseGoodsInfo.splice(index, 1)
      this.localStorageSave()
    },
    localStorageSave() {
      localStorage.setItem('releaseGoodsInfo', JSON.stringify(this.releaseGoodsInfo))
    },
    storageLoad() {
      let arr = JSON.parse(localStorage.getItem('releaseGoodsInfo')) // 放行物品信息
      if (arr && arr.length > 0) {
        this.releaseGoodsInfo = arr
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.storageLoad()
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
    padding: rem(20) rem(20) rem(190);
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
