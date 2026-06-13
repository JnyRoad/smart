<!--
- @name 个人中心
- @author qingqing.he <qingqing.he@bjtce.com>
- @date 2021-08-11
-->

<template>
  <div class="mine">
    <div class="info" @click="toDetail">
      <div class="left">
        <div class="img">
          <img :src="baseInfo.employeePhoto" style="border-radius: 50%;" mode="contain" width="100%" height="100%"/>
          <!-- <tce-image :src="baseInfo.employeePhoto" :placeholder="holerPerson" :width="0" :height="0"></tce-image> -->
        </div>
        <div class="name">
          <div>
            <span class="staffName">{{baseInfo.employeeName}}</span>
            <span class="m_icon icon_boy" v-if="baseInfo.employeeSex === 0"></span>
            <span class="m_icon icon_girl" v-if="baseInfo.employeeSex === 1"></span>
          </div>
          <div class="num">{{baseInfo.mobile}}</div>
        </div>
      </div>
      <span class="-arrow"></span>
    </div>
    <div class="bg">
      <div>
        <span class="park">{{parkInfo.parkName}}</span>
      </div>
    </div>
    <div class="dept">
      <div>
        <div class="value">{{baseInfo.deptName || '-'}}</div>
        <div class="label">所属部门</div>
      </div>
      <div>
        <div class="value">{{baseInfo.jobName || '-'}}</div>
        <div class="label">担任职务</div>
      </div>
      <div>
        <div class="value">{{baseInfo.statusDes || '-'}}</div>
        <div class="label">人员状态</div>
      </div>
    </div>
    <!-- list组件 -->
    <menuList :list="menuList"></menuList>
    <div class="btn">
      <button @click="goBack" class="tce-button tce-button--primary is-round is-plain">返回</button>
    </div>
  </div>
</template>

<script>
import iconHelp from './img/icon_help.png'
import iconDorm from './img/icon_dorm.png'
import holderPerson from './img/person.png'
import iconWechat from './img/icon_wechat.png'
import menuList from './components/menu-list'
import store from '@/store'

export default {
  components: {
    menuList
  },
  data() {
    return {
      holerPerson: holderPerson,
      menuList: [
        {
          imgSrc: iconHelp,
          title: '我的宿舍',
          pageSrc: '/xuchang/dorm'
        },
        {
          imgSrc: iconDorm,
          title: '帮助中心',
          pageSrc: '/xuchang/help'
        },
        {
          imgSrc: iconWechat,
          title: '微信解绑'
        }
      ],
      baseInfo: {},
      parkInfo: {}
    }
  },
  computed: {},
  props: {},
  watch: {},
  methods: {
    toDetail () {
      this.$router.push({
        path: '/xuchang/mine/detail'
      })
    },
    goBack () {
      this.$router.push({
        path: '/xuchang/home'
      })
    }
  },
  /**
   * 生命周期 created
   */
  created() {
    this.baseInfo = store.getters.userInfoBase
    this.parkInfo = store.getters.parkInfo
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
  .mine{
    width: 100%;
    min-height: 100%;
    padding: rem(50);
    .info{
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 rem(20);
      margin-bottom: rem(20);
      .left{
        display: flex;
        align-items: center;
      }
      .img{
        width: rem(150);
        height: rem(150);
        margin-right: rem(30);
      }
      .staffName{
        font-size: 20px;
        margin-right: rem(15);
      }
      .num{
        font-size: 16px;
        margin-top: rem(20);
      }
      .m_icon{
        display: inline-block;
        width: rem(50);
        height: rem(36);
        background-repeat: no-repeat;
        background-size: 100% 100%;
        vertical-align: middle;
        margin-top: -6px;
      }
      .icon_boy{
        background-image: url('./img/boy.png');
      }
      .icon_girl{
        background-image: url('./img/girl.png');
      }
    }
    .bg{
      position: relative;
      margin-bottom: -1px;
      width: 100%;
      padding: 0 rem(20);
      >div{
        width: 100%;
        height: rem(137);
        background: url('./img/bg.png') no-repeat;
        background-size: 100% auto;
        .park{
          position: absolute;
          left: rem(50);
          bottom: rem(30);
          color: #fff;
          font-size: 16px;
        }
      }
    }
    .dept{
      height: rem(170);
      margin-bottom: rem(40);
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 rem(70);
      border-radius: 5px;
      overflow: hidden;
      box-shadow: -2px 0 11px rgba(0, 0, 0, 0.2 );
      text-align: center;
      .value{
        margin-bottom: rem(20);
      }
      .label{
        color: #999;
        font-size: 12px;
      }
    }
    .btn{
      margin-top: rem(110);
    }
  }
</style>
