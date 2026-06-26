<!--可视化面板，人员出入 -->
<template>
  <div class="pn-at">
    <section class="pn-top">
      <div class="pn-top-item">
        <span class="pn-btn" @click="goPark()" :class="{'pn-btn-active': isPark}">园区概况</span>
        <span class="pn-btn" @click="goAccessto()" :class="{'pn-btn-active': !isPark}">人员出入</span>
      </div>
      <div class="pn-top-item">
        <p class="pn-title">智慧园区运营大数据</p>
      </div>
      <div class="pn-top-item pn-top-it-tm">
        <span class="pn-time" style="visibility: hidden;">{{time}}</span>
        <span class="pn-exit" @click="logout"></span>
        <!-- <el-button type="text"><span class="pn-exit" @click.native="logout"></span></el-button> -->
      </div>
    </section>
    <router-view></router-view>
  </div>
</template>

<style lang="scss" scoped>
@use "@/styles/platform/panel/accessto" as *;
</style>
<script>
export default {
  name: "panel",
  data() {
    return {
      time: "",
      isPark: true,
      taskInterval: null
    };
  },
  created: function() {
    this.getTime();
    this.taskInterval = setInterval(() => {
      this.getTime();
    }, 1000);
  },
  beforeDestroy: function() {
    clearInterval(this.taskInterval);
    this.taskInterval = null;
  },
  mounted: function() {
    if (this.$route.path == "/platform/panel/accessto") {
      this.isPark = false;
    } else {
      this.isPark = true;
    }
  },
  computed: {},
  methods: {
    goPark() {
      const src = `/platform/panel/bigdata`;
      this.$router.push({
        path: src
      });
      this.isPark = true;
    },
    goAccessto() {
      const src = `/platform/panel/accessto`;
      this.$router.push({
        path: src
      });
      this.isPark = false;
    },
    getTime() {
      this.time = this.dateFormat(new Date());
    },
    dateFormat(date) {
      let format = "yyyy-MM-dd hh:mm:ss";
      if (date != "Invalid Date") {
        var o = {
          "M+": date.getMonth() + 1, //month
          "d+": date.getDate(), //day
          "h+": date.getHours(), //hour
          "m+": date.getMinutes(), //minute
          "s+": date.getSeconds(), //second
          "q+": Math.floor((date.getMonth() + 3) / 3), //quarter
          S: date.getMilliseconds() //millisecond
        };
        if (/(y+)/.test(format))
          format = format.replace(
            RegExp.$1,
            (date.getFullYear() + "").substr(4 - RegExp.$1.length)
          );
        for (var k in o)
          if (new RegExp("(" + k + ")").test(format))
            format = format.replace(
              RegExp.$1,
              RegExp.$1.length == 1
                ? o[k]
                : ("00" + o[k]).substr(("" + o[k]).length)
            );
        return format;
      }
      return "";
    },
    logout() {
      this.$confirm("是否退出系统, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        this.$store.dispatch("LogOut").then(() => {
          this.$router.push({ path: "/login" });
        });
      });
    }
  }
};
</script>
