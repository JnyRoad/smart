<!--
- @name 入厂申请-随行人员列表
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-30
-->

<template>
  <div class="page3">
    <div class="page3-list" v-if="fellowInfo && fellowInfo.length>0">
      <div class="top-tip">已添加随行人员（{{fellowInfo.length}}人）</div>
      <div class="item" v-for="(item, index) in fellowInfo" :key="index">
        <div class="item-image">
          <tce-image :src="item.fellowPhotoId" :width="0" :height="0"></tce-image>
        </div>
        <div class="item-info">
          <div class="name">{{item.fellowName}}</div>
          <div>{{item.certNo}}</div>
        </div>
        <div class="item-btn">
          <span class="tce-icons tce-icons--edit" @click="edit(item, index)"></span>
          <span class="tce-icons tce-icons--del" @click="del(index)"></span>
        </div>
      </div>
    </div>
    <div v-else class="holder">
      <div class="inner"></div>
      <div>
        <p>暂无随行人员信息</p>
        <p>请点击下方按钮添加随行人员</p>
      </div>
    </div>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="add" class="tce-button tce-button--primary is-round is-plain">新增随行人员</button>
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
      fellowInfo: []
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
      if (this.$route.query.idHefei) {
        this.$router.push({
          path: '/xuchang/visitor/addPersonHefei',
          query: {
            idHefei: true,
            cause: this.$route.query.cause
          }
        })
      } else {
        this.$router.push({
          path: '/xuchang/visitor/addPerson'
        })
      }
    },
    /**
     * 确定
     */
    sure() {
      if (this.$route.query.idHefei) {
        this.$router.push({
          path: '/xuchang/visitor/visitorInfoHefei'
        })
      } else {
        this.$router.push({
          path: '/xuchang/visitor/visitorInfo'
        })
      }
    },
    /**
     * 编辑
     */
    edit(item, index) {
      if (this.$route.query.idHefei) {
        this.$router.push({
          path: '/xuchang/visitor/addPersonHefei',
          query: {
            itemInfo: JSON.stringify(item),
            itemIndex: index,
            isEdit: true,
            idHefei: true,
            cause: this.$route.query.cause
          }
        })
      } else {
        this.$router.push({
          path: '/xuchang/visitor/addPerson',
          query: {
            itemInfo: JSON.stringify(item),
            itemIndex: index,
            isEdit: true,
            cause: this.$route.query.cause
          }
        })
      }
    },
    /**
     * 删除
     */
    del(index) {
      this.fellowInfo.splice(index, 1)
      this.localStorageSave()
    },
    localStorageSave() {
      localStorage.setItem('fellowInfo', JSON.stringify(this.fellowInfo))
    },
    storageLoad() {
      let arr = JSON.parse(localStorage.getItem('fellowInfo')) // 随行人员信息
      if (arr && arr.length > 0) {
        this.fellowInfo = arr
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
        padding: rem(30) rem(30);
        margin-bottom: rem(20);
        display: flex;
        justify-content: space-between;
        .item-image{
          width: rem(100);
          height: rem(100);
        }
        .item-info{
          flex: 1;
          display: flex;
          justify-content: center;
          flex-direction: column;
          padding: 0 rem(20);
          .name{
            margin-bottom: rem(20);
          }
        }
      }
    }
    .holder{
      text-align: center;
      padding-top: rem(267);
      // .inner{
      //   width: rem(484);
      //   height: rem(255);
      //   margin: 0 auto 15px;
      //   background: url('./img/holder.png') no-repeat;
      //   background-size: 100% auto;
      // }
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
