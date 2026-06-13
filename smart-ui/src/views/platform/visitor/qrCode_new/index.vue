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
                    <el-form-item prop="keyCods" class="key-item" v-if="checkTypeDesc == 0">
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
                    <el-form-item prop="cardCods" class="card-item" v-else>
                      <el-input v-focus type="text" v-model.trim="cardCods" maxlength="18" @keyup.native="doCheck($event)"></el-input>
                      <div class="line-item">
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                        <i></i>
                      </div>
                    </el-form-item>
                  </el-form>
                  <div class="checkCode checkType">
                    <el-button :class="{ check: checkTypeDesc == 0 }" type="info" @click="checkType(0, '二维码')">扫 码</el-button>
                    <el-button :class="{ check: checkTypeDesc == 1 }" type="info" @click="checkType(1, '身份证')">识别身份证</el-button>
                  </div>
                  <div class="tip">
                    请将
                    <span style="color: #fa7b0c; font-size: 16px">{{ checkText }}</span>
                    靠近扫码器或通过下方键盘手动输入{{checkRemark}}
                  </div>
                  <div class="tip">（请确保鼠标焦点在输入框内）</div>
                  <div class="checkCode" @click="doCheckCode">凭条打印提示</div>
                </div>
                <calculation ref="calculation" @doKeyPress="_confirmEvent" :checkTypeDesc="checkTypeDesc"></calculation>
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
import { getInfoApi, getInfoApiNew, getInfoApiCard, delSmsCode, getImage, getAreaType} from '@/api/platform/visitor/qrCode'
import * as bpac from '@/util/bpac'
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
      checkTypeDesc: 0,
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
  mounted() {
    getAreaType({ flag: 1 }).then((response) => {
      this.areaNewType = response.data.data
    })
    getAreaType({ flag: 0 }).then((response) => {
      this.areaOldType = response.data.data
    })
  },
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
    checkType(t, s) {
      this.checkTypeDesc = t
      this.checkText = s
      if (t === 0) {
        this.keyCods = ''
        this.checkRemark = '预约码'
        this.readQRcode()
      } else {
        this.cardCods = ''
        this.checkRemark = '身份证'
        this.readIDCard()
      }
    },
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
      if (this.checkTypeDesc == 0) {
        this.keyCods = res
      } else {
        this.cardCods = res
      }
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
      if (this.checkTypeDesc == 0) {
        getInfoApiNew(this.keyCods)
          .then((res) => {
            if (res.data.code == 0) {
              if (res.data === null) {
                this.$message.error(res.data.msg)
                this.loading = false
                return
              }
              this.visitorData = res.data.data
              this.memberList = this.visitorData.fellowVisitorList
              this.doPrint()
              // if (this.visitorData.delFlag === 1 || this.visitorData.delFlag === 2) {
              //   this.$message.error('该访客码已失效')
              //   this.loading = false
              // } else if (this.visitorData.delFlag === 0) {
              //   // this.getImage(this.visitorData.id)
              //   this.doPrint()
              // } else {
              //   this.$message.error('获取访客码信息异常')
              //   this.loading = false
              // }
            } else {
              this.$message.error(res.data.msg)
              this.loading = false
            }
          })
          .catch((err) => {
            this.$message.error(err)
            this.loading = false
          })
      } else {
        // console.log(this.cardCods)
        // getInfoApiCard
        getInfoApiCard(this.cardCods)
          .then((res) => {
            // console.log(res)
            if (res.data.code == 0) {
              if (res.data === null) {
                this.$message.error(res.data.msg)
                this.loading = false
                return
              }
              this.visitorData = res.data.data
              this.memberList = this.visitorData.fellowVisitorList
              // this.doPrint()
              if (this.visitorData.delFlag === 1 || this.visitorData.delFlag === 2) {
                this.$message.error('该访客码已失效')
                this.loading = false
              } else if (this.visitorData.delFlag === 0) {
                // this.getImage(this.visitorData.id)
                this.doPrint()
              } else {
                this.$message.error('获取访客码信息异常')
                this.loading = false
              }
            } else {
              this.$message.error(res.data.msg)
              this.loading = false
            }
          })
          .catch((err) => {
            this.$message.error(err)
            this.loading = false
          })
      }
    },
    async getImage(visitorId) {
      this.loading = true
      this.loadingText = '访客照片同步中…'
      try {
        const res = await getImage(visitorId)
        if (!res) return
        if (res.data.code == 0) {
          let result = res.data.data
          if (result === null) {
            this.$message.error(res.data.msg)
            this.loading = false
          }
        } else {
          this.$message.error(res.data.msg)
          this.loading = false
        }
      } catch (err) {
        // console.log('图片上传异常---end')
        this.$message.error(err)
        this.loading = false
      } finally {
        this.doPrint()
      }
    },
    async delSmsCode() {
      const res = await delSmsCode(this.visitorData.id)
    },
    async doPrint(strExport) {
      // this.visitorData
      //授权区域
      const areaType = this.visitorData.areaType
      const newAreaTypeList = [],
        oldAreaTypeList = [],
        strArry = []
      areaType.forEach((element) => {
        const ant = this.areaNewType.filter((item) => item.code == element)
        if (ant.length > 0) {
          newAreaTypeList.push(ant[0].desc)
        }
        const aot = this.areaOldType.filter((item) => item.code == element)
        if (aot.length > 0) {
          oldAreaTypeList.push(aot[0].desc)
        }
      })
      if (newAreaTypeList.length > 0) {
        this.newAreaType = newAreaTypeList.join(',')
        this.newAreaType = '新工厂：' + this.newAreaType
        strArry.push('新工厂')
      }
      if (oldAreaTypeList.length > 0) {
        this.oldAreaType = oldAreaTypeList.join(',')
        this.oldAreaType = '老工厂：' + this.oldAreaType
        strArry.push('老工厂')
      }
      if(strArry.length > 0){
        this.permitAreaDesc = strArry.join('+')
      }

      // debugger
      if (bpac.IsExtensionInstalled() == false) {
        // debugger
        this.loading = false
        this.$message({
          message: '打印异常，请检查打印插件',
          type: 'error',
          duration: 30000
        })
        // const agent = window.navigator.userAgent.toLowerCase()
        // const ischrome = agent.indexOf('chrome') !== -1 && agent.indexOf('edge') === -1 && agent.indexOf('opr') === -1
        // if (ischrome) window.open('https://chrome.google.com/webstore/detail/ilpghlfadkjifilabejhhijpfphfcfhb', '_blank')
        return
      }

      try {
        this.loading = true
        this.loadingText = '准备打印中…'
        let templateId = ''
        if (this.visitorData.isVip) {
          templateId = 'visitor-VIP.lbx'
        } else {
          templateId = 'visitor.lbx'
        }
        const strPath = window.location.origin + '/resource/bpacTemplates/red/' + templateId
        const objDoc = bpac.IDocument
        const ret = await objDoc.Open(strPath)

        if (ret == true) {
          objDoc
            .StartPrint('', 0)
            .then((res) => {
            })
            .catch(err => { console.error(err) })

          if (this.memberList) {
            //循环打印随行访客
            for (let i = 0; i < this.memberList.length; i++) {
              let member = this.memberList[i]

              //随行访客照片
              const objImg = await objDoc.GetObject('objImage')
              if (objImg) {
                if (!this.validatenull(member.fellowPhotoId)) {
                  const imgUrls = 'D:\\visitor\\' + member.fellowPhotoId + '.png'
                  objImg.SetData(0, imgUrls, 4)
                }
              }

              //随行访客姓名
              const objVisitorName = await objDoc.GetObject('visitorName')
              if (objVisitorName) {
                if (this.validatenull(member.fellowName)) {
                  objVisitorName.Text = ''
                } else {
                  objVisitorName.Text = member.fellowName
                }
              }

              //跟主访客人员same-------------------
              //园区名字
              const objParkName = await objDoc.GetObject('parkName')
              if (objParkName) {
                if (this.validatenull(this.visitorData.parkName)) {
                  objParkName.Text = ''
                } else {
                  objParkName.Text = this.visitorData.parkName
                }
              }

              //公司名字
              const objCompany = await objDoc.GetObject('company')
              if (objCompany) {
                if (this.validatenull(this.visitorData.company)) {
                  objCompany.Text = ''
                } else {
                  objCompany.Text = this.visitorData.company
                }
              }

              //被访人姓名
              const objReceptionistName = await objDoc.GetObject('receptionistName')
              if (objReceptionistName) {
                if (this.validatenull(this.visitorData.receptionistName)) {
                  objReceptionistName.Text = ''
                } else {
                  objReceptionistName.Text = this.visitorData.receptionistName
                }
              }

              //来访事由
              const objCause = await objDoc.GetObject('cause')
              if (objCause) {
                if (this.validatenull(this.visitorData.causeDesc)) {
                  objCause.Text = ''
                } else {
                  objCause.Text = this.visitorData.causeDesc
                }
              }

              //区域类别
              const objPermitAreaType = await objDoc.GetObject('permitAreaTypeDesc')
              if (objPermitAreaType) {
                if (this.validatenull(this.permitAreaDesc)) {
                  objPermitAreaType.Text = ''
                } else {
                  objPermitAreaType.Text = this.permitAreaDesc
                }
              }


               //授权区域新工厂
              const objNewArea = await objDoc.GetObject('newAreaType')
              if (objNewArea) {
                if (this.validatenull(this.newAreaType)) {
                  objNewArea.Text = ''
                } else {
                  objNewArea.Text = this.newAreaType
                }
              }

               //授权区域老工厂
              const objOldArea = await objDoc.GetObject('oldAreaType')
              if (objOldArea) {
                if (this.validatenull(this.oldAreaType)) {
                  objOldArea.Text = ''
                } else {
                  objOldArea.Text = this.oldAreaType
                }
              }

              //来访时间
              const objStartTime = await objDoc.GetObject('startTime')
              if (objStartTime) {
                if (this.validatenull(this.visitorData.startTime)) {
                  objStartTime.Text = ''
                } else {
                  objStartTime.Text = this.visitorData.startTime
                }
              }

              //结束时间
              const objEndTime = await objDoc.GetObject('endTime')
              if (objEndTime) {
                if (this.validatenull(this.visitorData.endTime)) {
                  objEndTime.Text = ''
                } else {
                  objEndTime.Text = this.visitorData.endTime
                }
              }
              objDoc.PrintOut(1, 0)
              // const image = await objDoc.GetImageData(4, 0, 240);
              // const img = document.getElementById("previewArea");
              // img.src = image;
            }
          }
          setTimeout(() => {
            objDoc.EndPrint()
            objDoc.Close()
            this.delSmsCode()
            this.loading = false
            this.loadingText = '加载中…'
            this.$refs['calculation']._clearCode()
          }, 4000)
        }
      } catch (e) {
        this.loading = false
        this.$message.error('打印异常')
      }
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
