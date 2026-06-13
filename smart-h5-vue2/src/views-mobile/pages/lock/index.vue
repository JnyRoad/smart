<template>
  <div class="page3">
    <div class="page-title">你的门锁动态码</div>
    <div class="page-num">{{ num | decryption }}</div>
    <button @click="editCode" class="tce-button tce-button--primary is-plain" style="width: 50%; margin: 0 auto">修改动态码</button>
    <editCode ref="editCode" :curCode="num | decryption" :cutomHeader="true" @successCustom="successCustom"></editCode>
  </div>
</template>

<script>
import { getPwd } from '@/services/lock'
import store from '@/store'
import { decryption } from '@/util/encryption'
import editCode from './edit-code.vue'
export default {
  components: {
    editCode
  },
  data() {
    return {
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
        return '******'
      }
    }
  },
  methods: {
    successCustom() {
      this.getPwd()
    },
    doAntherStaff() {
      this.searchIndex = 2
      this.$refs.plateNumber && this.$refs.plateNumber.start()
    },
    async getPwd() {
      const baseInfo = store.getters.userInfoBase
      const employeeBadge = baseInfo.employeeBadge
      const res = await getPwd({ badge: employeeBadge })
      if (res.data !== '') {
        this.num = res.data
      } else {
        this.$createDialog({
          type: 'alert',
          title: '',
          content: '您暂未入住智能宿舍，请联系宿管入住！',
          icon: 'cubeic-alert',
          onConfirm: () => {
            this.$router.push({
              path: '/xuchang/dorm'
            })
          }
        }).show()
      }
    },
    editCode() {
      this.$refs.editCode && this.$refs.editCode.start()
    },
    Refresh() {
      this.$router.push({
        path: '/xuchang/dorm/getCode'
      })
    }
  },
  /**
   * 生命周期 created
   */
  async created() {
    this.getPwd()
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
  padding: rem(120) rem(20) rem(190);
  text-align: center;
  .page-title {
    margin-top: rem(120);
    font-size: 18px;
  }
  .page-num {
    margin: rem(120) 0;
    letter-spacing: 2px;
    font-size: 36px;
    font-weight: bold;
  }
}
</style>
