<template>
  <div class="data_cont" ref="data_cont" v-bind:style="{ backgroundColor : textStyle.backgroundColor}">
    <div v-if="url && type === 0" ref="header">
      <video ref="homeVideo" class="home-video" controls autoplay="autoplay" loop="loop" :src="url" />
    </div>
    <div class="cont1" v-if="type === 1" style="width: 90%;height: 700px">
      <el-carousel :interval="4000" type="card" height="600px">
        <el-carousel-item v-for="item in images" :key="item.id">
          <img :src="item.imageUrl" width="100%" height="100%" />
        </el-carousel-item>
      </el-carousel>
    </div>
    <div v-if="type === 2">
      <div class="cont1" style="width:1366px;text-align:center;margin-top: 12px;">
        <pdf
          ref="pdf"
          :src="url"
          :page="pageNum"
          @progress="loadedRatio = $event"
          @num-pages="pageTotalNum=$event"
        ></pdf>
        <div style="color: #409EFF;margin: 10px 0;">{{ pageNum }} / {{ pageTotalNum }}</div>
        <el-button-group style="margin:0 auto">
          <el-button type="primary" icon="el-icon-arrow-left" size="mini" @click="prePage">上一页</el-button>
          <el-button type="primary" size="mini" @click="nextPage">下一页<i class="el-icon-arrow-right el-icon--right"></i></el-button>
        </el-button-group>
      </div>
    </div>
    <div v-if="type === 3 && msg">
      <template v-if="textMoveType==1">
        <div class="cont1" style="width: 100%;height: 240px;" v-bind:style="{ color: textStyle.fontColor}">
           <vue-seamless-scroll :data="msgList" class="seamless-warp2" :class-option="optionLeft">
            <ul class="item">
              <li v-for="(item, index) in msgList" :key="index" v-text="item"></li>
            </ul>
          </vue-seamless-scroll>
        </div>
      </template>
      <template v-else>
        <vue-seamless-scroll :data="msgList" class="seamless-warp" :class-option="optionHover">
          <ul class="item">
            <li v-for="(item, index) in msgList" :key="index" v-text="item"></li>
          </ul>
        </vue-seamless-scroll>
      </template>
    </div>
    <div v-if="type === 4" style="height: 100%">
      <iframe :src="url" width="100%" height="100%"></iframe>
    </div>
  </div>
</template>

<script>
import pdf from 'vue-pdf'
export default {
  name: 'VideoPlayer',
  components: {
    pdf
  },
  data() {
    return {
      url: '',
      type: 0, // 0 视频 1 图片 2 PDF
      images: [],
      pageNum: 1,
      pageTotalNum: 1, // 总页数
      loadedRatio: 0, // 当前页面的加载进度，范围是0-1 ，等于1的时候代表当前页已经完全加载完成了
      msg: null,
      msgList: [],
      textMoveType: 1,
      interval: null,
      ip: '',
      data: null,
      textStyle:{
        backgroundColor:'#f0f0f0',
        fontColor: '#333333'
      }
    }
  },
  computed: {
    optionHover() {
      return {
        step: 2,
        hoverStop: false
      }
    },
    optionLeft() {
      return {
        step: 2,
        hoverStop: false,
        direction: 2,
        limitMoveNum: 1
      }
    }
  },
  mounted() {
    this.type = null
    this.ip = this.$route.query.ip
    this.webSocket()
  },
  methods: {
    webSocket() {
      const me = this
      if ('WebSocket' in window) {
        var ws = new WebSocket('ws://'+window.location.hostname+'/websocket/' + this.ip)

        ws.onopen = function () {
          //当WebSocket创建成功时，触发onopen事件
        }
        ws.onmessage = function (e) {
          const data = JSON.parse(e.data)
          this.data = data
          me.type = data.type
          if (me.type === 0) {
            me.url = data.file.fileUrl
          } else if (me.type === 1) {
            me.images = data.images
          } else if (me.type === 2) {
            me.url = data.file.fileUrl
            this.keyDown()
          } else if (me.type === 3) {
            me.msg = data.content
            me.msg = me.msg + '\xa0\xa0'
            me.msgList = []
            for (let i in me.msg) {
              me.msgList.push(me.msg[i])
            }
            me.textMoveType = data.textMoveType
            me.textStyle = JSON.parse(data.textStyle)
          } else if (me.type === 4) {
            window.open(data.content)
          }
        }
        ws.onclose = function (e) {
          //当客户端收到服务端发送的关闭连接请求时，触发onclose事件
        }
      }
    },
    error: function (err) {
    },
    // 上一页
    prePage() {
      let page = this.pageNum
      page = page > 1 ? page - 1 : this.pageTotalNum
      this.pageNum = page
    },

    // 下一页
    nextPage() {
      let page = this.pageNum
      page = page < this.pageTotalNum ? page + 1 : 1
      this.pageNum = page
    },
    // 监听键盘
    keyDown() {
      document.onkeydown =  (e) => {
        //事件对象兼容
        let e1 = e || event || window.event || arguments.callee.caller.arguments[0]
        //键盘按键判断:左箭头-37;上箭头-38；右箭头-39;下箭头-40
        //左
        if (e1 && e1.keyCode == 37) {
          this.prePage()
          // 按下左箭头
        } else if (e1 && e1.keyCode == 39) {
          // 按下右箭头
          this.nextPage()
        }
      }
    },
  }
}
</script>
<style lang="scss" scoped>
.data_cont {
  width: 100%;
  height: 100%;
  position: relative;
  .cont1 {
    position: absolute;
    margin: auto;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
  }
  .seamless-warp {
    overflow: hidden;
    width: 100%;
    ul.item {
      width: 100%;
      li {
        width: 100%;
        height: 220px;
        font-size: 200px;
        text-align: center;
      }
    }
  }
  .seamless-warp2 {
      overflow: hidden;
      width: 100%;
      ul.item {
        white-space: nowrap;
        height: 200px;
        width: 100%;
          li {
              width: 200px;
              font-size: 200px;
              height: 200px;
              line-height: 200px;
              display:inline;
          }
      }
  }
}

.el-carousel__item:nth-child(2n) {
  background-color: #99a9bf;
}

.el-carousel__item:nth-child(2n + 1) {
  background-color: #d3dce6;
}
.home-video {
  z-index: 100;
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  object-fit: fill;
  width: auto;
  height: auto;
  -ms-transform: translateX(-50%) translateY(-50%);
  -webkit-transform: translateX(-50%) translateY(-50%);
  transform: translateX(-50%) translateY(-50%);
  background-size: cover;
}
</style>