
<template>
  <div class="wrap bg2" :class="{'bg1':!single && tab1,'bg3':!single && !tab1}">
    <div class="wrap_inner">
      <div class="wrap_top">
        <div class="wrap_top_t top_t1" @click="changeTab(1)">
          <el-tooltip effect="light" :content="dataObj.areaName+'-'+obj1.deviceName" placement="top">
            <span>{{dataObj.areaName}}-{{obj1.deviceName}}</span>
          </el-tooltip>
        </div>
        <div class="wrap_top_t top_t2" v-if="!single" @click="changeTab(2)">
          <el-tooltip effect="light" :content="dataObj.areaName+'-'+obj2.deviceName" placement="top">
            <span>{{dataObj.areaName}}-{{obj2.deviceName}}</span>
          </el-tooltip>
        </div>
      </div>
      <div class="wrap_cont">
        <div class="c_top">
          <div class="cimg-outer">
            <img
              :src="curentObj.snapPhotoUrl?curentObj.snapPhotoUrl:defaultImg"
              :onerror="defaultImgFun()"
            />
            <div class="img-btm">
              <span>现场抓拍</span>
            </div>
          </div>
          <div class="cimg-outer">
            <img
              :src="curentObj.personUrl?curentObj.personUrl:defaultImg"
              :onerror="defaultImgFun()"
            />
            <div class="img-btm">
              <span>人脸识别</span>
            </div>
          </div>
        </div>
        <div class="c_btm">
          <div class="info-line">
            <span class="label">姓<i class="ft1_zh"></i><i class="ft1_zh"></i>名</span>
            <span class="field">{{ curentObj.personName || "-" }}</span>
          </div>
          <div class="info-line info-line2">
            <div>
              <span class="label">身<i class="ft1_zh"></i><i class="ft1_zh"></i>份</span>
              <span class="field">{{ curentObj.personTypeDesc || "-"  }}</span>
            </div>
            <div>
              <span class="label">出入类型</span>
              <span class="field">{{ curentObj.eventTypeDesc || "-"  }}</span>
            </div>
          </div>
          <div class="info-line">
            <span class="label">出入时间</span>
            <span class="field">{{ curentObj.snapTime || "-"  }}</span>
          </div>
          <div class="info-line">
            <span class="label">单位名称</span>
            <span class="field">{{ curentObj.company || "-"  }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  data() {
    return {
      defaultImg: "/img/pn_plhd.png",
      tab1: true,
      obj1: {},
      obj2: {},
      curentObj: {},
      single: true
    }
  },
  props: {
    dataObj: {
      type: Object,
      default: () => ({}),
    }
  },
  watch:{
    dataObj:{
			handler: function(val){
        this.obj1 = {}
        this.obj2 = {}
        this.curentObj = {}

        if(val && val.snapDataList){
          if(val.snapDataList.length>1){
            this.single = false
          }
          else{
            this.single = true
          }
        }else{
          this.single = true
        }
        this.initData()
      },
			immediate: true
		}
  },
  mounted() {
    // this.initData()
  },
  methods: {
    defaultImgFun() {
      return 'this.onload=null;this.src=" ' + this.defaultImg + '";';
    },
    initData(){
      if(!this.single){
        this.obj1 = this.dataObj.snapDataList[0]
        this.obj2 = this.dataObj.snapDataList[1]
        if(this.tab1){
          this.curentObj = this.obj1
        }else{
          this.curentObj = this.obj2
        }
      }else{
        if(this.dataObj&&this.dataObj.snapDataList&&this.dataObj.snapDataList.length>0){
          this.obj1 = this.dataObj.snapDataList[0]
          this.curentObj = this.obj1
        }
      }
    },
    changeTab(type){
      this.curentObj = {}
      if(type===1){
        this.tab1 = true
        this.curentObj = this.obj1
      }
      if(type===2){
        this.tab1 = false
        this.curentObj = this.obj2
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.wrap{
  width: 369px;
  height: 296px;
  &_top{
    display: flex;
    height: 22px !important;
    line-height: 28px !important;
    &_t{
      width: 130px !important;
      height: 100%;
      padding-left: 16px;
      margin-right: 10px;
    }
  }
  .num{
    width: 62px;
  }
  &_cont{
    display: flex;
    flex-direction: column;
    padding: 10px 20px 8px;
    .c_top{
      display: flex;
      justify-content: space-between;
      .cimg-outer{
        position: relative;
        width: 136px;
        height: 136px;
        background: rgba(42, 93, 168, .3);
        overflow: hidden;
        text-align: center;
        img{
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          max-width: 100%;
          height: auto;
          max-height: 100%;
          width: auto;
        }
        .img-btm{
          position: absolute;
          left: 0;
          right: 0;
          bottom: 0;
          height: 15px;
          background: url('/img/panel_new/person/t1.png') no-repeat;
          text-align: left;
          span{
            display: inline-block;
            width: 60px;
            font-size: 12px;
            letter-spacing: 2px;
            padding-left: 2px;
            margin-top: -4px;
            vertical-align: middle;
            overflow: hidden;
          }
        }
      }
    }
    .c_btm{
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-around;
      padding-top: 5px;
      .info-line{
        display: flex;
        overflow: hidden;
        .label{
          color: #448fd5;
          padding-right: 15px;
          display: inline-block;
        }
        .field{
          flex: 1;
          color: #a6d4ff;
          line-height: 20px;
          display: inline-block;
          overflow: hidden;
          text-overflow:ellipsis;
          white-space: nowrap;
        }
      }
      .info-line2{
        display: flex;
        justify-content: space-between;
        >div{
          display: flex;
          flex: 1;
          overflow: hidden;
        }
      }
    }
  }
}
.bg2{
  background-image: url('/img/panel_new/person/bg_2.png');

}
.bg1{
  background-image: url('/img/panel_new/person/bg_1.png');
  .top_t1, .top_t2{
    cursor: pointer;
  }
  .top_t2{
    color: #102854;
  }
}
.bg3{
  background-image: url('/img/panel_new/person/bg_3.png');
  .top_t1, .top_t2{
    cursor: pointer;
  }
  .top_t1{
    color: #102854;
  }
}
</style>
