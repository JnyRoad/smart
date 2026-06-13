<template>
  <div class="qrCode password-manager" v-loading="loading" :element-loading-text="loadingText" element-loading-background="rgba(0, 0, 0, 0.6)">
    <div class="layout-nav-logo"></div>
    <div class="my-basic-container">
      <el-scrollbar class="my-scrollbar" :native="false">
        <section class="my-basic-inner qrCode-inner">
          <div class="cont">
            <div class="left">
              <div class="left_inner">
                <div class="t2">裕同科技欢迎您</div>
                <div class="t1">访客凭条打印</div>
                <div class="form-outer">
                  <el-form :model="codeForm" :inline="true" ref="codeForm">
                    <el-form-item prop="keyCods" class="key-item">
                      <el-input v-focus type="text" v-model.trim="keyCods" maxlength="6" @keyup.native="doCheck($event)"></el-input>
                      <div class="line-item">
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                      </div>
                    </el-form-item>
                  </el-form>
                  <div class="tip">
                    请将
                    <span style="color: #fa7b0c; font-size: 16px">{{ checkText }}</span>
                    靠近扫码器或通过下方键盘手动输入{{ checkRemark }}
                  </div>
                  <div class="tip">（请确保鼠标焦点在输入框内）</div>
                  <div class="checkCode" @click="doCheckCode">凭条打印提示</div>
                </div>
                <calculation ref="calculation" @doKeyPress="_confirmEvent"></calculation>
                <!-- <img id='previewArea'> -->
              </div>
            </div>
          </div>
        </section>
      </el-scrollbar>
    </div>
    <el-dialog title="凭条打印提示" :visible.sync="codeVisible" width="500px" center>
      <div class="btmInfo">
        <div class="b1">
          <div>第1步、在线填写访客信息发起预约</div>
          <div>第2步、预约通过后收到访客预约码，访客预约码将以微信发送给您</div>
          <div>第3步、键盘或扫码输入访客预约码，录入完成后自动打印访客凭条</div>
          <div>第4步、粘贴访客凭条，刷脸入园（完成）</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getInfoApi, getInfoApiCard } from '@/api/platform/visitor/qrCode'
import calculation from './keys'

const constImg = './img/print_peaple.png'
export default {
  components: {
    calculation
  },
  data() {
    return {
      loading: false,
      loadingText: '加载中…',
      codeVisible: false,
      codeForm: {},
      keyCods: '',
      cardCods: '',
      memberList: [],
      visitorData: {}, //访客信息
      hasErr: false,
      errMsg: '',
      socket: null,
      checkText: '二维码',
      checkRemark: '预约码',
      areaNewType: [],
      areaOldType: [],
      newAreaType: '',
      oldAreaType: '',
      permitAreaDesc: ''
    }
  },
  directives: {
    focus: {
      // 指令的定义
      inserted: function (el) {
        el.children[0].focus()
      },
      update: function (el) {
        el.children[0].focus()
      }
    }
  },
  mounted() {},
  created() {
    this.openReader()
  },
  computed: {},
  watch: {
    keyCods(val) {
      // console.log(this.keyCods)
      if (val && val.length == 6) {
        this.getInfo()
      }
    },
    cardCods(val) {
      // console.log(val)
      if (val && val.length == 18) {
        this.getInfo()
      }
    }
  },
  methods: {
    // 读写器初始化
    openReader() {
      const host = 'ws://127.0.0.1:33666' //客户端电脑本地IP，非服务器IP，无需修改
      const me = this
      if (me.socket == null) {
        me.socket = new WebSocket(host)
      }
      try {
        me.socket.onopen = function () {
          // me.autoReadCard();
          me.readQRcode()
        }
        me.socket.onclose = function () {
        }
        me.socket.onerror = function () {
        }
        me.socket.onmessage = function (msg) {
          if (typeof msg.data == 'string') {
            var msgM = msg.data + ''
            var msgJson = JSON.parse(msgM)
            // console.log(msgM)
            switch (msgJson.fun) {
              case 'EST_Reader_ReadIDCard#':
                if (msgJson.rCode == '0') {
                  //身份证信息
                  me.cardCods = msgJson.certNo //身份证号码
                } else if (msgJson.rCode == '1') {
                  // 此状态无需处理；保留分支避免落入下方 else 弹窗
                } else if (msgJson.rCode == '-2') {
                  // 此状态无需处理；保留分支避免落入下方 else 弹窗
                } else {
                  alert(msgJson.errMsg)
                }
                break
              case 'EST_ScanQRcode#':
                if (msgJson.rCode == '0') {
                  // console.log(msgJson)
                  me.keyCods = msgJson.QRCode
                } else {
                  alert(msgJson.errMsg)
                }
                break
            }
          }
        }
      } catch (ex) {
        alert('连接异常,请检查是否成功安装控件.')
      }
    },
    readQRcode() {
      // 自动读取 EST_Reader_ReadIDCard 身份证  EST_ScanQRcode 二维码
      try {
        if (this.socket.readyState == 1) {
          this.socket.send('EST_ScanQRcode#')
        } else {
          alert('未找到控件，请检查控件是否安装.')
        }
      } catch (ex) {
        alert('连接异常,请检查是否成功安装控件.')
      }
    },
    readIDCard() {
      try {
        if (this.socket.readyState == 1) {
          this.socket.send('EST_Reader_ReadIDCard#')
        } else {
          alert('未找到控件，请检查控件是否安装.')
        }
      } catch (ex) {
        alert('连接异常,请检查是否成功安装控件.')
      }
    },
    doCheckCode() {
      this.codeVisible = true
    },
    _confirmEvent(res) {
      this.keyCods = res
    },
    trimLR(e) {
      e.target.value = e.target.value.replace(/^\s+|\s+$/gm, '')
      if (!e.target.value) {
        return false
      }
      return true
    },
    doCheck(e) {
      if (!this.trimLR(e)) {
        return
      }
    },
    showErr(msg) {
      this.$message.closeAll()
      this.hasErr = true
      this.errMsg = msg
    },
    getInfo() {
      this.loading = true
      this.loadingText = '获取访客信息中…'
      getInfoApi(this.keyCods)
        .then((res) => {
          this.loading = false
          if (res.data.code == 0) {
            if (res.data === null) {
              this.$message.error(res.data.msg)
              return
            }
            // this.visitorData = res.data.data
            const src = `/platform/visitor/visitor_record/detail/${res.data.data.id}`;
              this.$router.push({
                path: src,
                query: {
                  queryPage: 1,
                  queryForm: {}
                }
              });
          } else {
            this.$message.error(res.data.msg)
          }
        })
        .catch((err) => {
          this.$message.error(err)
          this.loading = false
        })
    }
  }
}
</script>
<style lang="scss" scoped>
.qrCode ::v-deep {
  width: 100%;
  height: 100%;
  background: url('/img/qrCode_bg.png') center;
  background-repeat: no-repeat;
  background-size: 100% 100%;
  .my-basic-container {
    padding: 0;
  }
  .my-basic-inner {
    background: transparent;
  }
  .btmInfo {
    line-height: 30px;
  }
  .tipError {
    color: red;
    margin-bottom: 10px;
  }
  .checkCode {
    color: #ed6c00;
    text-decoration: underline;
    margin-top: 15px;
    cursor: pointer;
  }
  .checkType a:first-child {
    margin-right: 12px;
  }
  .check {
    background-color: #fa7b0c;
    border-color: #fa7b0c;
  }
  .qrCode-inner {
    display: flex;
    align-items: center;
  }
  .tip {
    color: #999;
    margin-top: 5px;
  }

  .cont {
    padding-top: 20px;
    flex: 1;
    display: flex;
    justify-content: center;
    .left {
      flex: 1;
      display: flex;
      // padding-right: 80px;
      justify-content: center;
      &_inner {
        // float: right;
        text-align: center;
        width: 530px;
      }
      .btm {
        color: #999;
        text-align: left;
      }
    }
    .right {
      flex: 1;
      text-align: left;
      padding-left: 80px;
      border-left: 1px solid #e0e0e0;
    }
    .t1 {
      font-size: 26px;
      font-weight: bold;
      margin-bottom: 25px;
    }
    .t2 {
      font-size: 16px;
      margin-bottom: 20px;
    }
    .b1 {
      margin-bottom: 25px;
      line-height: 25px;
    }
    .form-outer {
      margin-top: 20px;
      margin-bottom: 30px;
    }
    .form-outer {
      .el-form-item__content {
        position: relative;
      }
      .key-item {
        .el-input__inner {
          width: 500px;
          height: 70px;
          padding: 0;
          font-size: 50px;
          background: transparent;
          color: #333;
          // text-align: center;
          border: none;
          // border-bottom: 2px solid blue;
          border-radius: 0;
          letter-spacing: 46px;
          padding-left: 52px;
        }
        .line-item {
          position: absolute;
          left: 40px;
          right: 40px;
          bottom: 0;
          display: flex;
          justify-content: space-between;
          // border-bottom: 1px solid red;
          i {
            display: inline-block;
            width: 50px;
            border-bottom: 2px solid #000;
          }
        }
      }
      .card-item {
        .el-input__inner {
          width: 500px;
          // height: 70px;
          padding: 0;
          font-size: 10px;
          background: transparent;
          color: #333;
          // text-align: center;
          border: none;
          // border-bottom: 2px solid blue;
          border-radius: 0;
          letter-spacing: 17px;
          padding-left: 42px;
        }
        .line-item {
          position: absolute;
          left: 40px;
          right: 40px;
          bottom: 0;
          display: flex;
          justify-content: space-between;
          // border-bottom: 1px solid red;
          i {
            display: inline-block;
            width: 10px;
            border-bottom: 2px solid #000;
          }
        }
      }
    }
    .v1 {
      margin-top: 20px;
    }
  }
}
</style>
