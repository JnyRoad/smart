
<template>
  <div class="personList">
    <div v-for="(item, index) in list" :key="index">{{item.name}}</div>
  </div>
</template>

<script>
export default {
  data() {
    return {}
  },
  computed: {},
  props: {
    list: {
      type: Array,
      default: function () {
        return []
      }
    }
  },
  watch: {},
  methods: {
    /**
     * 查看数据
     */
    checkList() {
      this.$router.push({
        path: '/xuchang/goodReleaseWork/list'
      })
    }
  },
  /**
   * 生命周期 created
   */
  created() {},
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
  .personList{
    display: flex;
    justify-content: flex-end;
    flex-wrap: wrap;
    >div{
      color: #687a95;
      font-size: 12px;
      background: #EDEFF5;
      margin-left: rem(30);
      margin-bottom: rem(15);
      padding: 5px 10px;
    }
  }
</style>
