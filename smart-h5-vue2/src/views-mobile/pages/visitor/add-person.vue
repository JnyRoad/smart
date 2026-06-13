<!--
- @name 入厂申请-添加随行人员
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-30
-->
<template>
  <div class="page3">
    <tce-form ref="tceForm">
      <tce-form-group>
       <tce-form-item required field="fellowName" :valueData="itemInfo.fellowName" requiredMessage="请输入" type="input" label="访客姓名" placeholder="请输入"></tce-form-item>
        <!-- 检测人脸 -->
        <tce-form-item required field="fellowPhotoId" :valueData="itemInfo.fellowPhotoId" type="upload-image-single-face" label="访客照片" placeholder="请输入"></tce-form-item>
        <!-- 普通上传 -->
        <!-- <tce-form-item required field="fellowPhotoId" :valueData="itemInfo.fellowPhotoId" type="upload-image-single" label="访客照片" placeholder="请输入"></tce-form-item> -->
        <tce-form-item required field="certNo" :valueData="itemInfo.certNo" requiredMessage="请输入" type="input" label="证件号码" placeholder="请输入"></tce-form-item>
      </tce-form-group>
    </tce-form>
    <page3Bottom>
      <div slot="inner" class="btn-inner">
        <button @click="apply" class="tce-button tce-button--primary is-round is-plain">
          <template v-if="isEdit">确认修改随行人员</template>
          <template v-else>确认添加随行人员</template>
        </button>
      </div>
    </page3Bottom>
  </div>
</template>

<script>
import page3Bottom from '@/views-mobile/components/page3-bottom'
import { enumPersonCertApi } from '@/services/visitor'
import { cardid } from '@/util/validate'

export default {
  components: {
    page3Bottom
  },
  data() {
    return {
      personCertList: [],
      fellowInfo: [],
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
     * 提交
     */
    async apply() {
      let formData = this.$refs.tceForm.formData
      const res = cardid(formData.certNo)
      if (res[0]) {
        this.$tceMobile.toast(res[1])
        return
      }
      await this.$refs.tceForm.verification()
      if (this.isEdit) {
        this.fellowInfo.splice(this.itemIndex, 1, formData)
      } else {
        this.fellowInfo.push(formData)
      }
      this.localStorageSave()
      this.$router.push({
        path: '/xuchang/visitor/addPersonList'
      })
    },
    /**
     * OA人员证件类型枚举
     */
    async getEnumPersonCert() {
      const res = await enumPersonCertApi()
      this.personCertList = res.data
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
    // localStorage.setItem("fellowInfo", JSON.stringify([]))
    this.getEnumPersonCert()
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
  .page3{
    background: $TCE-Background-Grey;
    width: 100%;
    min-height: 100%;
    padding: rem(20) rem(20) rem(190);
    .business-form-group{
      margin-bottom: rem(20);
    }
    .btn-inner{
      width: 100%;
      height: 100%;
      padding: 0 rem(52);
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }
</style>
