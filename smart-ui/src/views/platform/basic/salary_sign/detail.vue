<!--基础信息：工资签收详情 -->
<template>
  <div class="my-basic-container staff-info">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div id="imgDom">
          <p class="box-orange">工资信息</p>
          <table class="salary_table">
            <tr>
              <td>
                <span class="s_label">薪资期间：</span>
              </td>
              <td>{{personInfo.wageDate || '-'}}</td>
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
            <tr>
              <td>
                <span class="s_label">签收日期：</span>
              </td>
              <td>{{personInfo.createTime || '-'}}</td>
            </tr>
          </table>
          <p class="box-orange">本人电子签名</p>
          <dl class="img-info">
            <dt class="img-outer">
              <div class="img-inner">
                <viewer>
                  <img :src="'data:image/jpeg;base64,'+personInfo.signImg" />
                </viewer>
              </div>
            </dt>
          </dl>
        </div>
        <el-button type="primary" icon @click="downloadImg">下载签收凭证图片</el-button>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { getById } from "@/api/platform/basic/salary_sign";
import { mapGetters } from "vuex";
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
      }).then(function(canvas) {
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
          "_工资签收凭证"; // 设置图片名称
        a.href = pageData; // 将生成的URL设置为a.href属性
        a.dispatchEvent(event); // 触发a的单击事件
      });
    }
  }
};
</script>
<style lang="scss" scoped>
#imgDom {
  width: 350px;
  padding: 0 20px 20px 20px;
  border: 1px solid #e0e0e0;
  margin: 0 0 30px 0;
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
