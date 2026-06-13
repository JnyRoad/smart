<!--
- @name 公告-列表
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-09-13
-->

<template>
  <div class="page3">
    <div class="page3-list">
      <div class="item" @click="toDetail(item)" v-for="(item, index) in dataList" :key="index">
        <div class="item-img">
          <tce-image :src="item.bbsImg" :width="60" :height="60"></tce-image>
        </div>
        <div class="item-info -ellipsis">
          <div class="item-info_value -ellipsis">{{item.bbsTitle}}</div>
        </div>
      </div>
      <tce-empty v-if="!dataList || dataList.length===0"></tce-empty>
    </div>
  </div>
</template>

<script>
import { getBbsList } from '@/services/home'
import store from '@/store'

export default {
  components: {},
  data() {
    return {
      dataList: [],
      parkInfo: {}
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    toDetail(item) {
      let isPdf = false
      if (item.contentLinkType === 1) { // 列表字段：bbsUrl 外部链接
        window.location.href = item.bbsUrl
        return
      } else if (item.contentLinkType === 2) { // 详情字段：bbsContent 文本
      } else if (item.contentLinkType === 4) { // 详情字段：previewUrl pdf
        isPdf = true
      }
      this.$router.push({
        path: '/xuchang/home/bbs/detail',
        query: {
          id: item.bbsId,
          isPdf: isPdf
        }
      })
    },
    /**
     * 获取列表
     */
    async getList() {
      this.$loading.show()
      const res = await getBbsList(
        {
          badge: this.parkInfo.id,
          current: 1,
          size: 100
        }
      )
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        this.dataList = res.data.records
      } else {
        this.$tceMobile.toast(res.message || '网络错误')
      }
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.parkInfo = store.getters.parkInfo
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
  .page3{
    background: $TCE-Background-Grey;
    width: 100%;
    min-height: 100%;
    padding: rem(20);
    .page3-list{
      .item{
        background: #fff;
        border-radius: 5px;
        padding: rem(30) rem(30) rem(20);
        margin-bottom: rem(20);
        display: flex;
        .item-img{
          width: rem(100);
          height: rem(100);
        }
        .item-info{
          flex: 1;
          display: flex;
          justify-content: center;
          flex-direction: column;
          padding-left: rem(20);
        }
      }
    }
  }
</style>
