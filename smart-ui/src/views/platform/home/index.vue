<!--首页 -->
<template>
  <div class="my-basic-container">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="h_top">
          <div class="h_top_l">
            <div class="t1">您好，{{userInfo.username}}，欢迎登录！</div>
            <div class="t2">{{time}}</div>
          </div>
          <div class="h_top_r">
            <div class="t3">当前在线人数</div>
            <div class="t4">
              <template v-for="(item, index) in onLine">
                <span :key="index">{{item}}</span>
              </template>
            </div>
          </div>
        </div>
        <div class="bg"></div>
      </section>
    </el-scrollbar>
  </div>
</template>

<script>
import {
  loggedCount
} from "@/api/platform/home";
import { dateFormat } from '@/util/date.js';
import { mapGetters } from "vuex";
export default {
  name: "home",
  data() {
    return {
      onLine: 0,
      time: ''
    };
  },
  watch: {},
  created() {
    this.getTime();
    var taskInterval = setInterval(() => {
      this.getTime();
    }, 1000);
  },
  mounted: function() {
    loggedCount().then(response => {
        let count = response.data.data;
        this.initNum(count);
      });
  },
  computed: {
    ...mapGetters(["permissions", "userInfo"])
  },
  methods: {
    getTime() {
      this.time = dateFormat(new Date());
    },
    initNum(num){
      let len = num.toString().length
      //最少显示三位数，不够的进行补位
      if(len<3){
        this.onLine = this.PrefixZero(num, 3)
      }else{
        this.onLine = num.toString()
      }
    },
    PrefixZero(num, n) {
      return (Array(n).join(0) + num).slice(-n);
    }
  }
};
</script>

<style lang="scss" scoped>

  .my-basic-inner{
    display: flex;
    flex-direction: column;
    .h_top{
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 10px 50px;
      margin-bottom: 50px;
      &_l{
        .t1{
          font-size: 26px;
          margin-bottom: 5px;
        }
        .t2{
          font-size: 18px;
        }
      }
      &_r{
        display: flex;
        .t3{
          font-size: 26px;
          color: #666;
        }
        .t4{
          display: flex;
          align-items: center;
          margin-left: 15px;
          >span{
            background: $mainBg;
            width: 20px;
            text-align: center;
            color: #fff;
            margin-right: 6px;
            height: 25px;
            line-height: 25px;
          }
        }
      }
    }
    .bg{
      flex: 1;
      width: 45%;
      margin: 0 auto 50px;
      background: url('/img/home.png') bottom no-repeat;
      background-size: 100% auto;
    }
  }
</style>
