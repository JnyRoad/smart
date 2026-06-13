<!--
- @name 个人信息
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-12
-->

<template>
  <div class="mine-info">
    <div class="block">
      <div class="row">
        <div class="label"> 姓名 </div>
        <div class="value -ellipsis"> {{ baseInfo.employeeName }} </div>
      </div>
      <div class="row">
        <div class="label"> 工号 </div>
        <div class="value -ellipsis"> {{ baseInfo.employeeBadge }} </div>
      </div>
      <div class="row">
        <div class="label"> 公司 </div>
        <div class="value -ellipsis"> {{ baseInfo.buName || '-' }} </div>
      </div>
    </div>
    <div class="-line-10 -line-background"></div>
    <div class="block">
      <div class="row">
        <div class="label"> 部门 </div>
        <div class="value -ellipsis"> {{ baseInfo.deptName || '-'  }} </div>
      </div>
      <div class="row">
        <div class="label"> 职务 </div>
        <div class="value -ellipsis"> {{ baseInfo.jobName || '-'  }} </div>
      </div>
      <div class="row">
        <div class="label"> 职层 </div>
        <div class="value -ellipsis"> {{ baseInfo.jcheName || '-'  }} </div>
      </div>
      <div class="row">
        <div class="label"> 福利层次 </div>
        <div class="value -ellipsis"> {{ baseInfo.welfareLevel || '-'  }} </div>
      </div>
    </div>
    <div class="-line-10 -line-background"></div>
    <div class="block">
      <div class="row">
        <div class="label"> 人员状态 </div>
        <div class="value -ellipsis"> {{ baseInfo.statusDes || '-'  }} </div>
      </div>
      <div class="row">
        <div class="label"> 人员类型 </div>
        <div class="value -ellipsis"> {{ baseInfo.empTypeDes || '-'  }} </div>
      </div>
      <div class="row">
        <div class="label"> 员工性质 </div>
        <div class="value -ellipsis"> {{ baseInfo.empAttribute || '-'  }} </div>
      </div>
    </div>
  </div>
</template>

<script>
import store from '@/store'

export default {
  components: {},
  data() {
    return {
      baseInfo: {}
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {},
  filters: {},
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
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
  .mine-info{
    background: $TCE-Background-Grey;
    width: 100%;
    min-height: 100%;
    .block{
      background: #fff;
      padding: 0 rem(30);
      .row{
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #eee;
        height: rem(100);
        .label{
          color: #666;
          width: rem(160);
          flex: none;
        }
      }
      & .row:last-child{
        border: none
      }
    }
  }
</style>
