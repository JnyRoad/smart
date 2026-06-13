<!--
- @name 物品放行-办公区-添加物品
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-26
-->
<template>
  <div class="page3">
    <tce-form ref="tceForm">
      <tce-form-group>
        <tce-form-item required field="wpbm" :valueData="itemInfo.wpbm" requiredMessage="请输入资产编码" type="input" label="资产编码" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="wpmc" :valueData="itemInfo.wpmc" requiredMessage="请输入名称" type="input" label="名称" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="wpdw" :valueData="itemInfo.wpdw" requiredMessage="请输入单位" type="input" label="单位" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="wpsl" :valueData="itemInfo.wpsl" requiredMessage="请输入数量" type="input" label="数量" placeholder="请输入"></tce-form-item>
      </tce-form-group>
      <tce-form-group>
        <tce-form-item required field="jsdw" :valueData="itemInfo.jsdw" requiredMessage="请输入接收单位" type="input" label="接收单位" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="fxrq" :valueData="itemInfo.fxrq" type="time-picker" label="放行日期" placeholder="请输入"></tce-form-item>
        <tce-form-item field="bz" :valueData="itemInfo.bz" requiredMessage="请输入" type="input" label="备注(原因)" placeholder="请输入"></tce-form-item>
        <tce-form-item
          required
          field="ysfs"
          type="picker"
          :formOption="{
            opt: transportType
          }"
          :valueData="itemInfo.ysfs"
          label="运输方式"
          placeholder="请选择"
        ></tce-form-item>
      </tce-form-group>
      <tce-form-group>
        <!-- <tce-form-item required field="xm" :valueData="itemInfo.xm" requiredMessage="请输入姓名" type="input" label="姓名" placeholder="请输入"></tce-form-item> -->
        <tce-form-item type="item-holder" label="姓名" placeholder="请选择">
          <template slot="itemholder">
            <div @click="search" class="personOuter">
              <div style="text-align: right" v-if="itemInfo.name">{{ itemInfo.name }}</div>
              <div v-else>
                <div style="text-align: right; color: #999">请输入</div>
              </div>
            </div>
          </template>
        </tce-form-item>
        <tce-form-item field="cph" :valueData="itemInfo.cph" type="plate-number" label="车牌号" placeholder="请输入"></tce-form-item>
      </tce-form-group>
    </tce-form>
    <searchByStaff ref="searchByStaff" :cutomHeader="true" @successCustom="successCustom"></searchByStaff>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="apply" class="tce-button tce-button--primary is-round is-plain">
          <template v-if="isEdit">确认修改放行物品</template>
          <template v-else>确认添加放行物品</template>
        </button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { transportTypeOption } from './const'
import searchByStaff from './components/search-by-staff.vue'
import { getPersonData } from '@/services/goodRreleaseOffice'
// getPersonData
export default {
  components: {
    page3Bottom,
    searchByStaff
  },
  data() {
    return {
      transportType: transportTypeOption,
      releaseGoodsInfo: [],
      itemInfo: {
        name: null,
        xm: null
      },
      itemIndex: '',
      isEdit: false
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    /**
     * 提交
     */
    async apply() {
      let formData = this.$refs.tceForm.formData
      formData['name'] = this.itemInfo.name
      formData['xm'] = this.itemInfo.xm
      await this.$refs.tceForm.verification()
      if (this.isEdit) {
        this.releaseGoodsInfo.splice(this.itemIndex, 1, formData)
      } else {
        this.releaseGoodsInfo.push(formData)
      }
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/goodReleaseWork/addGoodsList'
      })
    },
    localStorageSave() {
      localStorage.setItem('releaseGoodsInfo', JSON.stringify(this.releaseGoodsInfo))
    },
    storageLoad() {
      let arr = JSON.parse(localStorage.getItem('releaseGoodsInfo')) // 放行物品信息
      if (arr && arr.length > 0) {
        this.releaseGoodsInfo = arr
      }
    },
    async successCustom(obj) {
      this.$loading.show()
      const res = await getPersonData(obj.gh)
      this.$loading.hide()
      if (res.code === 0 && res.data) {
        const personData = res.data
        this.itemInfo.name = personData.name
        this.itemInfo.xm = personData.id
      } else {
        this.$tceMobile.toast(res.message)
      }
      // getPersonData
    },
    search() {
      this.$refs.searchByStaff && this.$refs.searchByStaff.start()
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.storageLoad()
    if (this.$route.query.itemInfo) {
      this.itemInfo = JSON.parse(this.$route.query.itemInfo)
    }
    this.itemIndex = this.$route.query.itemIndex
    this.isEdit = this.$route.query.isEdit
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
  min-height: 100%;
  padding: rem(20) rem(20) rem(190);
  .business-form-group {
    margin-bottom: rem(20);
  }
  .personOuter {
    position: relative;
    padding: rem(30) rem(28) rem(10) rem(30);
    .-arrow {
      position: absolute;
      top: rem(44);
      right: rem(30);
    }
  }
  .btn-inner {
    width: 100%;
    height: 100%;
    padding: 0 rem(52);
    display: flex;
    justify-content: center;
    align-items: center;
  }
}
</style>
