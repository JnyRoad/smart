<!--可视化面板，人员出入 -->
<template>
  <section class="pn-top top1">
    <div class="pn-top-item item1">
      <div class="item1-park">
        <!-- <span>大岭山园区</span> -->
      </div>
      <div class="item1-btn">
        <span class="pn-btn" @click="goView()" :class="{'pn-btn-active': isView}">园区概况</span>
        <span class="pn-btn" @click="goPerson()" :class="{'pn-btn-active': !isView}">人员出入</span>
      </div>
    </div>
    <div class="pn-top-item top_bg">
      <p class="pn-title">智慧园区运营大数据</p>
    </div>
    <div class="pn-top-item pn-top-it-tm">
      <span class="pn-time">{{time}}</span>
      <span class="pn-exit-outer"  @click="changePark">
        <!-- <span class="pn-exit"></span> -->
        <!-- 首页 -->
        <el-select v-model="parkId" popper-class="panel_park" class="panel_park1" placeholder="请选择" @change="parkChange">
          <el-option
            v-for="item in parks"
            :key="item.id"
            :label="item.parkName"
            :value="item.id"
            >
          </el-option>
        </el-select>
      </span>
    </div>
  </section>
</template>

<style scoped lang="scss">
  @use "@/styles/platform/panel_new/top" as *;
</style>
<style lang="scss">
  .panel_park1{
    .el-input__inner{
      background: transparent;
      border: none;
      color: #5DB1FF;
    }
    .el-input.is-focus .el-input__inner{
      background: transparent;
    }
    .el-input .el-select__caret{
      color: #5DB1FF;
    }
    input:read-only{
      color: #5DB1FF;
    }
  }
  .panel_park{
    border: 1px solid #19325D;
    border-top-color: transparent;
    border-bottom-color: transparent;
    border-left-color: transparent;
    border-radius: 0;
    top: 35px !important;
    background: rgba(4, 18, 43, .6);
    .el-select-dropdown__item{
      color: #54a1ff;
    }
    .el-select-dropdown__item.selected{
      color: #fff;
      // background: #428be8;
      background: linear-gradient(to bottom, #4998fb 0%,#1e407c 100%);
    }
    .el-select-dropdown__item.hover, .el-select-dropdown__item:hover {
      color: #fff;
      // background-color: #428be8;
      background: linear-gradient(to bottom, #4998fb 0%,#1e407c 100%);

    }
    .popper__arrow{
      display: none;
    }
    .el-select-dropdown__empty{
      color: #54a1ff;
    }
  }
  .panel_park::before{
    content: '';
    display: inline-block;
    position: absolute;
    left: 0;
    right: 0;
    top: 0;
    width: 100%;
    height: 1px;
    background: linear-gradient(to right, #0b152e 0%,#5BB8FA 50%, #0b152e 100%);
  }
   .panel_park::after{
    content: '';
    display: inline-block;
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    width: 100%;
    height: 1px;
    background: linear-gradient(to right, #0b152e 0%,#5BB8FA 50%, #0b152e 100%);
  }
</style>
<script>
import { dateFormat } from '@/util/date.js';
import { api } from "@/views/platform/panel_new/_service";
export default {
  name: "panel",
  data() {
    return {
      time: "",
      taskInterval: '',
      isView: true,
      parks: [],
      parkId: ''
    };
  },
  created: function() {
    this.getPark()
    this.getTime();
    this.taskInterval = setInterval(() => {
      this.getTime();
    }, 1000);
  },
  mounted: function() {
    if (this.$route.path == "/platform/panel/view") {
      this.isView = true;
    } else {
      this.isView = false;
    }
  },
  beforeDestroy() {
    clearInterval(this.taskInterval);
  },
  computed: {},
  methods: {
    async getPark(){
      const res = await api.allPark()
      if(res.data.code==0){
        this.parks = res.data.data
        if(this.parks.length>0){
          this.parkId = this.parks[0].id
          this.$emit("defaultHandle", this.parkId);
        }
      }
    },
    parkChange(id){
      this.$emit('parkChange', id)
    },
    goView() {
      const src = `/platform/panel/view`;
      this.$router.push({
        path: src
      });
      this.isView = true;
    },
    goPerson() {
      const src = `/platform/panel/person`;
      this.$router.push({
        path: src
      });
      this.isView = false;
    },
    getTime() {
      this.time = dateFormat(new Date());
    },
    changePark(){
      const src = `/home/index`;
      this.$router.push({
        path: src
      });
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
