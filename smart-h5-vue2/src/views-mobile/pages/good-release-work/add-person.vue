<!--
- @name 物品放行-办公区-添加人员
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-26
-->
<template>
  <div class="page3">
    <tce-form ref="tceForm">
      <tce-form-group>
        <!-- <tce-form-item required field="badge" :valueData="itemInfo.badge" requiredMessage="请输入工号" type="input" label="工号" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="xm" :valueData="itemInfo.xm" requiredMessage="请输入姓名" type="input" label="姓名" placeholder="请输入"></tce-form-item> -->
        <tce-form-item  type="item-holder" label="工号" placeholder="请输入">
          <template slot="itemholder">
            <div @click="search" class="personOuter">
              <div style="text-align: right;" v-if="itemInfo.gh">{{itemInfo.gh}}</div>
              <div v-else>
                <div style="text-align: right; color: #999">请输入</div>
              </div>
            </div>
          </template>
        </tce-form-item>
        <tce-form-item  type="item-holder" label="姓名" placeholder="请输入">
          <template slot="itemholder">
            <div @click="search" class="personOuter">
              <div style="text-align: right;" v-if="itemInfo.name">{{itemInfo.name}}</div>
              <div v-else>
                <div style="text-align: right; color: #999">请输入</div>
              </div>
            </div>
          </template>
        </tce-form-item>
        <tce-form-item required field="lcsy" :valueData="itemInfo.lcsy" requiredMessage="请输入离厂事由" type="input" label="离厂事由" placeholder="请输入"></tce-form-item>
        <tce-form-item required field="lcDate" :valueData="itemInfo.lcDate" type="time-picker" label="离厂日期" placeholder="请输入"></tce-form-item>
      </tce-form-group>
    </tce-form>
    <searchByStaff ref="searchByStaff" :cutomHeader="true" @successCustom="successCustom"></searchByStaff>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="apply" class="tce-button tce-button--primary is-round is-plain">
          <template v-if="isEdit">确认修改放行人员</template>
          <template v-else>确认添加放行人员</template>
        </button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Bottom from '@/views-mobile/components/page3-bottom'
import searchByStaff from './components/search-by-staff.vue'
import { getPersonData } from '@/services/goodRreleaseOffice'
export default {
  components: {
    page3Bottom,
    searchByStaff
  },
  data() {
    return {
      goodsPersonInfo: [],
      itemInfo: {
        gh: null,
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
      await this.$refs.tceForm.verification()
      let formData = this.$refs.tceForm.formData
      formData['gh'] = this.itemInfo.gh
      formData['name'] = this.itemInfo.name
      formData['xm'] = this.itemInfo.xm
      if (this.isEdit) {
        this.goodsPersonInfo.splice(this.itemIndex, 1, formData)
      } else {
        this.goodsPersonInfo.push(formData)
      }
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/goodReleaseWork/addPersonList'
      })
    },
    localStorageSave() {
      localStorage.setItem('goodsPersonInfo', JSON.stringify(this.goodsPersonInfo))
    },
    storageLoad() {
      let arr = JSON.parse(localStorage.getItem('goodsPersonInfo')) // 放行人员信息
      if (arr && arr.length > 0) {
        this.goodsPersonInfo = arr
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
        this.itemInfo.gh = obj.gh
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
  .personOuter{
      position: relative;
      padding: rem(30) rem(24) rem(10) rem(30);
      .-arrow{
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
