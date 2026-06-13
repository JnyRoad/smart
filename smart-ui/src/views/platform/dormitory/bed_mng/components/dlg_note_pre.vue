<!--凭条预览-->
<template>
  <el-dialog
    title="凭条预览"
    class="dialog_form note_form"
    width="800px"
    :visible.sync="setFormVisible"
  >
    <div class="note-outer">
      <div class="note-inner" ref="print" id="printDom">
        <div class="n_logo">
          裕同科技
          <!-- <img src="/img/dorm/logo_d.png"/> -->
        </div>
        <p class="p1">欢迎您加入裕同科技</p>
        <div class="gray-line"></div>
        <p class="p2">{{row.name}}（{{row.sex|genderInit}}）</p>
        <p class="p3">{{row.roomName}}室{{row.bedNumber}}床</p>
        <div class="gray-line"></div>
        <p class="p4">{{row.dormitoryName}}</p>
        <p class="p5">{{row.createTime}}</p>
        <p class="p6">*入住前请向宿管出示入住凭条</p>
      </div>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="setFormVisible = false" plain
        >取 消</el-button
      >
      <el-button type="primary" id="printDomBtn" @click="editSubmit()" v-print="'#printDom'"
        >打印凭条</el-button
      >
      <!-- <el-button type="primary" @click="editSubmit()"
        >打印凭条</el-button
      > -->
    </div>
  </el-dialog>
</template>

<script>
import Vue from "vue";
import Print from "vue-print-nb";

Vue.use(Print);

export default {
  name: "",
  data() {
    return {
      setFormVisible: false,
    };
  },
  props: {
    visible: {
      type: Boolean,
    },
    row: undefined,
  },
  watch: {
    visible(newVal, oldVal) {
      if (newVal) {
        this.getDetail();
        // this.$nextTick(()=>{
        //   document.getElementById("printDomBtn")&&document.getElementById("printDomBtn").click()
        // })
      }
      this.setFormVisible = newVal;
    },
    setFormVisible(newVal, oldVal) {
      if (newVal === false) {
        this.$emit("dlgdo", newVal);
      }
    },
  },
  created() {
    this.initData();
  },
  mounted: function () {},
  computed: {},
  methods: {
    initData() {
      this.setFormVisible = this.visible;
    },
    async getDetail() {},
    editSubmit(formName) {
      this.setFormVisible = false;
    }
    // editSubmit(formName) {
    //   this.$print(this.$refs.print)
    //   this.setFormVisible = false;
    // }
  },
};
</script>
<style lang="scss" scoped>
.note_form ::v-deep {
  .el-dialog__body {
    padding: 0;
  }
  .note-outer {
    background: #dce0e5;
    padding: 20px 0;
  }
  .note-inner {
    width: 300px;
    height: 400px;
    background: #fff;
    margin: 0 auto;
    padding: 10px;
    text-align: center;
    .n_logo {
      height: 26px;
      font-size: 14px;
      // background-image: url("/img/dorm/logo_d.png");
      // background-repeat: no-repeat;
      // background-size: auto 100%;
    }
    .p1 {
      color: #000;
      font-size: 18px;
      margin-top: 30px;
      font-weight: bold;
    }
    .p2 {
      font-size: 22px;
      font-weight: bold;
      color: #333;
      margin-bottom: 10px;
    }
    .p3,
    .p4 {
      font-size: 18px;
    }
    .p5 {
      margin-top: 10px;
      margin-bottom: 30px;
    }
    .p6 {
      color: #999;
      font-size: 12px;
    }
  }
}
</style>
<style lang="scss" scoped media="print">
@page {
  size: 48mm portrait; /* auto is the initial value */
  margin: 3mm; /* this affects the margin in the printer settings */
}

html {
  width: 100%;
  // border: 1px solid black;
  background-color: #ffffff;
  margin: 0px; /* this affects the margin on the html before sending to printer */
}

body {
  width: 100%;
  //margin: 10mm 15mm 10mm 15mm; /* margin you want for the content */
}
//打印区样式
.note-inner {
  width: 100%;
  // border: 1px solid black;
  // height: 400px;
  background: #fff;
  margin: 0 auto;
  // padding: 10px;
  font-size: 12pt;
  text-align: center;
  .n_logo {
    height: 26px;
    font-size: 16pt;
    img{
      width: 100%;
      height: auto;
    }
    // background-image: url("/img/dorm/logo_d.png");
    // background-repeat: no-repeat;
    // background-size: auto 100%;
  }
  .p1 {
    color: #333;
    font-size: 12pt;
    margin-top: 30px;
    font-weight: bold;
  }
  .p2 {
    font-size: 16pt;
    font-weight: bold;
    color: #333;
    margin-bottom: 10px;
  }
  .p3,
  .p4 {
    font-size: 14pt;
  }
  .p5 {
    margin-top: 10px;
    margin-bottom: 30px;
  }
  .p6 {
    color: #999;
    font-size: 12pt;
  }
}
</style>
