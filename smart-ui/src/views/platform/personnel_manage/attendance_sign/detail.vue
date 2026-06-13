<!--基础信息：考勤汇总确认详情 -->
<template>
  <div class="my-basic-container staff-info">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner clear">
        <div id="imgDom">
          <p class="box-orange">签收信息</p>
          <table class="salary_table">
            <tr>
              <td>
                <span class="s_label">考勤期间：</span>
              </td>
              <td>{{personInfo.checkDate || '-'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">确认状态：</span>
              </td>
              <td>{{personInfo.signStatusDesc || '-'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">通知状态：</span>
              </td>
              <td>{{personInfo.noticeStatus || '-'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">
                  工
                  <i class="ft1_zh"></i>
                  <i class="ft1_zh"></i>号：
                </span>
              </td>
              <td>{{personInfo.badge || '-'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">
                  姓
                  <i class="ft1_zh"></i>
                  <i class="ft1_zh"></i>名：
                </span>
              </td>
              <td>{{personInfo.name || '-'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">
                  园
                  <i class="ft1_zh"></i>
                  <i class="ft1_zh"></i>区：
                </span>
              </td>
              <td>{{personInfo.parkName || '-'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">
                  B
                  <i class="ft1_zh"></i>
                  <i class="ft1_zh"></i>
                  <i class="ft1"></i>U：
                </span>
              </td>
              <td>{{personInfo.compName || '-'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">
                  部
                  <i class="ft1_zh"></i>
                  <i class="ft1_zh"></i>门：
                </span>
              </td>
              <td>{{personInfo.depName || '-'}}</td>
            </tr>


          </table>
          <p class="box-orange">本人电子签名</p>
          <dl class="img-info">
            <dt class="img-outer">
              <div class="img-inner">
                <viewer>
                  <img :src="'data:image/jpeg;base64,'+ personInfo.signImg" :onerror="errorImgSign()"/>
                </viewer>
              </div>
            </dt>
          </dl>
          <el-button type="primary" icon @click="downloadImg" style="margin-top: 20px;">下载签收凭证图片</el-button>
        </div>
        <div id="imgDom2">
          <p class="box-orange">考勤信息</p>
          <table class="salary_table">
            <tr>
              <td>
                <span class="s_label">应出勤（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a6 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">实出勤（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a7 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">平时加班（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a10 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">周末加班（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a11 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">法定节假日加班（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a12 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">事假（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a15 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">病假（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a16 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">待料（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a17 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">工伤假（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a18 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">婚假（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a19 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">丧假（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a20 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">产假（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a21 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">陪产假（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a22 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">产检假（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a23 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">放行（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a24 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">年休假（H）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a25 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">迟到累计（M）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a27 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">迟到次数：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a28 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">早退累计（M）：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a29 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">早退次数：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a30 || '0'}}</td>
            </tr>
            <tr>
              <td>
                <span class="s_label">夜班次数：</span>
              </td>
              <td>{{personInfo.avaGetskyPayYSHRDTO.a36 || '0'}}</td>
            </tr>


          </table>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
  import {getById} from "./attendance_sign.js";
  import {mapGetters} from "vuex";
  import html2Canvas from "html2canvas";

  export default {
    data() {
      return {
        personInfo: {}
      };
    },
    created() {
      var id = this.$route.params.id;

      getById(id).then(response => {
        if (!this.validatenull(response.data.data)) {
          this.personInfo = response.data.data;
        }
      });
    },
    computed: {
      ...mapGetters(["permissions"])
    },
    methods: {
      downloadImg() {
        var title = this.htmlTitle;
        let _this = this;
        html2Canvas(document.querySelector("#imgDom"), {
          allowTaint: true
        }).then(function (canvas) {
          let contentWidth = canvas.width;
          let contentHeight = canvas.height;
          let pageHeight = (contentWidth / 592.28) * 841.89;
          let leftHeight = contentHeight;
          let position = 0;
          let imgWidth = 595.28;
          let imgHeight = (592.28 / contentWidth) * contentHeight;
          let pageData = canvas.toDataURL("image/jpeg", 1.0);

          var a = document.createElement("a"); // 生成一个a元素
          var event = new MouseEvent("click"); // 创建一个单击事件
          a.download =
            _this.personInfo.badge +
            "_" +
            _this.personInfo.name +
            "_考勤汇总确认签收凭证"; // 设置图片名称
          a.href = pageData; // 将生成的URL设置为a.href属性
          a.dispatchEvent(event); // 触发a的单击事件
        });
      }
    }
  };
</script>
<style lang="scss" scoped>
  #imgDom {
    float: left;
    width: 350px;
    padding: 0 20px 20px 20px;
    border: 1px solid #e0e0e0;
    margin: 0 30px 30px 0;
  }

  #imgDom2 {
    float: left;
    width: 350px;
    padding: 0 20px 20px 20px;
    border: 1px solid #e0e0e0;
    margin: 0 30px 30px 0;
  }

  .box-orange {
    margin-top: 25px;
  }

  .img-outer {
    height: 200px;
    border: none;
    padding-top: 0;

    .img-inner {
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
      background: #E4E8EC;
    }
  }

  .salary_table {
    .s_label {
      margin-right: 20px;
    }

    td {
      height: 30px;
    }
  }
</style>
