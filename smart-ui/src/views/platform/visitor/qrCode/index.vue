<template>
  <div class="qrCode password-manager"
    v-loading="loading"
    :element-loading-text="loadingText"
    element-loading-background="rgba(0, 0, 0, 0.6)"
  >
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
                    <el-form-item prop="keyCods">
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
                  <div class="tip">请将二维码靠近扫码器或通过下方键盘手动输入预约码</div>
                  <div class="tip">（请确保鼠标焦点在输入框内）</div>
                  <div class="checkCode" @click="doCheckCode">凭条打印提示</div>
                </div>
                <calculation ref="calculation" @doKeyPress="_confirmEvent"></calculation>
                <!-- <div class="btm">
                  <div class="">*凭条打印提示</div>
                  <div class="b1">
                    <div>第1步、在线填写访客信息发起预约</div>
                    <div>第2步、预约通过后收到访客预约码，访客预约码将以短信发送给您</div>
                    <div>第3步、键盘或扫码输入访客预约码，录入完成后自动打印访客凭条</div>
                    <div>第4步、粘贴访客凭条，刷脸入园（完成）</div>
                  </div>
                </div> -->
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
import { getInfoApi, delSmsCode, getImage } from '@/api/platform/visitor/qrCode'
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
      memberList: [],
      visitorData: {}, //访客信息
      hasErr: false,
      errMsg: ''
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

  },
  computed: {},
  watch: {
    keyCods(val) {
      if (val && val.length == 6) {
        this.getInfo()
      }
    }
  },
  methods: {
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
      this.loadingText="获取访客信息中…"
      getInfoApi(this.keyCods)
        .then((res) => {
          if (res.data.code == 0) {
            if (res.data === null) {
              this.$message.error(res.data.msg)
            }
            this.visitorData = res.data.data
            this.memberList = this.visitorData.fellowVisitorList
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
    },
    async getImage(visitorId) {
      this.loading = true
      this.loadingText="访客照片同步中…"
      try{
        const res = await getImage(visitorId)
        if(!res)return
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
      }catch(err){
        // console.log('图片上传异常---end')
        this.$message.error(err)
        this.loading = false
      }finally{
        this.doPrint()
      }
    },
    async delSmsCode() {
      const res = await delSmsCode(this.visitorData.id)
    },
    async doPrint(strExport) {
      if (bpac.IsExtensionInstalled() == false) {
        this.loading = false
        this.$message({
          message: '打印异常，请检查打印插件',
          type: 'error',
          duration: 30000
        });
        const agent = window.navigator.userAgent.toLowerCase()
        const ischrome = agent.indexOf('chrome') !== -1 && agent.indexOf('edge') === -1 && agent.indexOf('opr') === -1
        if (ischrome) window.open('https://chrome.google.com/webstore/detail/ilpghlfadkjifilabejhhijpfphfcfhb', '_blank')
        return
      }

      try {
        this.loading = true
        this.loadingText="准备打印中…"
        let templateId = ''
        if (this.visitorData.isVip) {
          templateId = 'visitor-VIP.lbx'
        } else {
          templateId = 'visitor.lbx'
        }
        const strPath = window.location.origin + '/resource/bpacTemplates/white/' + templateId
        const objDoc = bpac.IDocument
        const ret = await objDoc.Open(strPath)
        // console.log(objDoc)
        // let pt = objDoc.Printer
        // let pt = bpac.IPrinter
        // pt.IsPrinterOnline('Brother QL-800')
        // console.log(pt)
        // console.log(pt.prototype)
        // console.log(pt.Name)
        // console.log(pt.portName)
        // console.log(pt.ErrorCode)
        // console.log(pt.ErrorString)
        // console.log(pt.ErrorString)
        // console.log(pt.GetInstalledPrinters)
        // console.log(pt.IsPrinterOnline)
        // console.log('open------end')
        // if (doc.Printer.IsPrinterOnline(printerName)) {
        //   status.Text = "Online";
        // }
        // else {
        //   status.Text = "Offline";
        // }

        // objDoc
        //   .GetPrinterName()
        //   .then((res) => {
        //     console.log('GetPrinterName-then------')
        //     console.log(res)
        //     // console.log(bpac.IPrinter.IsPrinterOnline(res))

        //     console.log('GetPrinterName-then------end')
        //   })
        //   .catch((err) => {
        //     console.log('GetPrinterName-catch------')
        //     console.log(err)
        //     console.log('GetPrinterName-catch------end')
        //   })

        if (ret == true) {
          objDoc
            .StartPrint('', 0)
            .then((res) => {
            })
            .catch(err => { console.error(err) })

          //打印主访客-------------
          //主访客照片
          // const objImg = await objDoc.GetObject('objImage')
          // if (objImg) {
          //   if (!this.validatenull(this.visitorData.remotePath) && !this.validatenull(this.visitorData.visitorPhoto)) {
          //     let imgUrl = this.visitorData.remotePath + this.visitorData.visitorPhoto + '.jpg'
          //     objImg.SetData(0, imgUrl, 4)
          //   }
          // } else {
          //   console.log('模板对象--主访客照片模板为空')
          // }

          //主访客姓名
          const objVisitorName = await objDoc.GetObject('visitorName')
          if (objVisitorName) {
            if (this.validatenull(this.visitorData.visitorName)) {
              objVisitorName.Text = ''
            } else {
              objVisitorName.Text = this.visitorData.visitorName
            }
          }

          //跟随行人员same-------------------
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

          //same-----------end

          if (this.memberList) {
            //循环打印随行访客
            for (let i = 0; i < this.memberList.length; i++) {
              let member = this.memberList[i]

              //随行访客照片
              // const objImg = await objDoc.GetObject('objImage')
              // if (objImg) {
              //   if (!this.validatenull(this.visitorData.remotePath) && !this.validatenull(member.fellowPhoto)) {
              //     let imgUrl = this.visitorData.remotePath + member.fellowPhotoId + '.jpg'
              //     objImg.SetData(0, imgUrl, 4)
              //   }
              // } else {
              //   console.log('模板对象--随行访客照片模板为空')
              // }

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
            }
          }
          setTimeout(() => {
            objDoc.EndPrint()
            objDoc.Close()
            this.delSmsCode()
            this.loading = false
            this.loadingText = '加载中…'
            this.$refs['calculation']._clearCode()
          }, 10)
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
    .v1 {
      margin-top: 20px;
    }
  }
}
</style>
