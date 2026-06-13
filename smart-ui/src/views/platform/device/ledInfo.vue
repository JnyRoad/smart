<!--设备管理，道闸管理，显示屏信息修改  -->
<template>
  <div class="my-basic-container ledInfo">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <el-form ref="form" :model="form" :inline="true" class="form">
          <el-form-item label="显示类型" prop="id" label-width="75px">
            <el-select v-model="displayScene" placeholder="请选择" @change="displaySceneChange">
              <el-option
                v-for="item in displays"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              ></el-option>
            </el-select>
          </el-form-item>
          <div class="item" :class="{'setted': line1Checked}">
            <p class="item__title">
              <span class="tip">当前未设置</span>
              第一排文字
              <el-checkbox class="ck1" v-model="line1Checked">本次是否设置</el-checkbox>
            </p>
            <div class="item__block">
              <div class="item__row">
                <span class="title">文字颜色</span>
                <div class="cont">
                  <el-radio-group v-model="form.line1.areaColor" class="rdColor">
                    <el-radio
                      v-for="(item, index) in colors"
                      :key="index"
                      :label="item.value"
                      :style="{'background':item.color,'borderColor':item.borderColor}"
                    ></el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="item__row">
                <span class="title">文字运动</span>
                <div class="cont">
                  <el-radio-group v-model="form.line1.areaAction" class="rdAction">
                    <el-radio
                      v-for="(item, index) in actions"
                      :key="index"
                      :label="item.value"
                    >{{ item.text }}</el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="item__row">
                <span class="title">内容类型</span>
                <div class="cont">
                  <el-radio
                    v-model="form.line1.areaType"
                    v-for="(item, index) in types"
                    :key="index"
                    :label="item.value"
                    @change="typeChange(form.line1)"
                  >{{ item.text }}</el-radio>
                </div>
              </div>
              <div class="item__row" v-if="form.line1.areaType==1">
                <span class="title">日期格式</span>
                <div class="cont dateCont">
                  <el-button
                    v-for="(item, index) in dates"
                    :key="index"
                    @click="insertDateVal(form.line1, item.value)"
                    plain
                  >{{item.text}}</el-button>
                </div>
              </div>
              <div class="item__row" v-if="form.line1.areaType==0">
                <span class="title">插入文字</span>
                <div class="cont dateCont">
                  <el-button
                    v-for="(item, index) in texts"
                    :key="index"
                    @click="insertVal(form.line1, item.value)"
                    plain
                  >{{item.text}}</el-button>
                </div>
              </div>
              <div class="block-btm">
                <!-- 0:文本，可编辑。 1: 时间，不可编辑 -->
                <el-input
                  :readonly="form.line1.areaType===1"
                  v-model="form.line1.areaContent"
                  type="textarea"
                  :placeholder="form.line1.areaType===1?'请选择日期格式':'请输入内容'"
                ></el-input>
              </div>
            </div>
          </div>
          <div class="item" :class="{'setted': line2Checked}">
            <p class="item__title">
              <span class="tip">当前未设置</span>
              第二排文字
              <el-checkbox class="ck1" v-model="line2Checked">本次是否设置</el-checkbox>
            </p>
            <div class="item__block">
              <div class="item__row">
                <span class="title">文字颜色</span>
                <div class="cont">
                  <el-radio-group v-model="form.line2.areaColor" class="rdColor">
                    <el-radio
                      v-for="(item, index) in colors"
                      :key="index"
                      :label="item.value"
                      :style="{'background':item.color,'borderColor':item.borderColor}"
                    ></el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="item__row">
                <span class="title">文字运动</span>
                <div class="cont">
                  <el-radio-group v-model="form.line2.areaAction" class="rdAction">
                    <el-radio
                      v-for="(item, index) in actions"
                      :key="index"
                      :label="item.value"
                    >{{ item.text }}</el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="item__row">
                <span class="title">内容类型</span>
                <div class="cont">
                  <el-radio
                    v-model="form.line2.areaType"
                    v-for="(item, index) in types"
                    :key="index"
                    :label="item.value"
                    @change="typeChange(form.line2)"
                  >{{ item.text }}</el-radio>
                </div>
              </div>
              <div class="item__row" v-if="form.line2.areaType==1">
                <span class="title">日期格式</span>
                <div class="cont dateCont">
                  <el-button
                    v-for="(item, index) in dates"
                    :key="index"
                    @click="insertDateVal(form.line2, item.value)"
                    plain
                  >{{item.text}}</el-button>
                </div>
              </div>
              <div class="item__row" v-if="form.line2.areaType==0">
                <span class="title">插入文字</span>
                <div class="cont dateCont">
                  <el-button
                    v-for="(item, index) in texts"
                    :key="index"
                    @click="insertVal(form.line2, item.value)"
                    plain
                  >{{item.text}}</el-button>
                </div>
              </div>
              <div class="block-btm">
                <!-- 0:文本，可编辑。 1: 时间，不可编辑 -->
                <el-input
                  :readonly="form.line2.areaType===1"
                  v-model="form.line2.areaContent"
                  type="textarea"
                  :placeholder="form.line2.areaType===1?'请选择日期格式':'请输入内容'"
                ></el-input>
              </div>
            </div>
          </div>
          <div class="item" :class="{'setted': line3Checked}">
            <p class="item__title">
              <span class="tip">当前未设置</span>
              第三排文字
              <el-checkbox class="ck1" v-model="line3Checked">本次是否设置</el-checkbox>
            </p>
            <div class="item__block">
              <div class="item__row">
                <span class="title">文字颜色</span>
                <div class="cont">
                  <el-radio-group v-model="form.line3.areaColor" class="rdColor">
                    <el-radio
                      v-for="(item, index) in colors"
                      :key="index"
                      :label="item.value"
                      :style="{'background':item.color,'borderColor':item.borderColor}"
                    ></el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="item__row">
                <span class="title">文字运动</span>
                <div class="cont">
                  <el-radio-group v-model="form.line3.areaAction" class="rdAction">
                    <el-radio
                      v-for="(item, index) in actions"
                      :key="index"
                      :label="item.value"
                    >{{ item.text }}</el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="item__row">
                <span class="title">内容类型</span>
                <div class="cont">
                  <el-radio
                    v-model="form.line3.areaType"
                    v-for="(item, index) in types"
                    :key="index"
                    :label="item.value"
                    @change="typeChange(form.line3)"
                  >{{ item.text }}</el-radio>
                </div>
              </div>
              <div class="item__row" v-if="form.line3.areaType==1">
                <span class="title">日期格式</span>
                <div class="cont dateCont">
                  <el-button
                    v-for="(item, index) in dates"
                    :key="index"
                    @click="insertDateVal(form.line3, item.value)"
                    plain
                  >{{item.text}}</el-button>
                </div>
              </div>
              <div class="item__row" v-if="form.line3.areaType==0">
                <span class="title">插入文字</span>
                <div class="cont dateCont">
                  <el-button
                    v-for="(item, index) in texts"
                    :key="index"
                    @click="insertVal(form.line3, item.value)"
                    plain
                  >{{item.text}}</el-button>
                </div>
              </div>
              <div class="block-btm">
                <!-- 0:文本，可编辑。 1: 时间，不可编辑 -->
                <el-input
                  :readonly="form.line3.areaType===1"
                  v-model="form.line3.areaContent"
                  type="textarea"
                  :placeholder="form.line3.areaType===1?'请选择日期格式':'请输入内容'"
                ></el-input>
              </div>
            </div>
          </div>
          <div class="item" :class="{'setted': line4Checked}">
            <p class="item__title">
              <span class="tip">当前未设置</span>
              第四排文字
              <el-checkbox class="ck1" v-model="line4Checked">本次是否设置</el-checkbox>
            </p>
            <div class="item__block">
              <div class="item__row">
                <span class="title">文字颜色</span>
                <div class="cont">
                  <el-radio-group v-model="form.line4.areaColor" class="rdColor">
                    <el-radio
                      v-for="(item, index) in colors"
                      :key="index"
                      :label="item.value"
                      :style="{'background':item.color,'borderColor':item.borderColor}"
                    ></el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="item__row">
                <span class="title">文字运动</span>
                <div class="cont">
                  <el-radio-group v-model="form.line4.areaAction" class="rdAction">
                    <el-radio
                      v-for="(item, index) in actions"
                      :key="index"
                      :label="item.value"
                    >{{ item.text }}</el-radio>
                  </el-radio-group>
                </div>
              </div>
              <div class="item__row">
                <span class="title">内容类型</span>
                <div class="cont">
                  <el-radio
                    v-model="form.line4.areaType"
                    v-for="(item, index) in types"
                    :key="index"
                    :label="item.value"
                    @change="typeChange(form.line4)"
                  >{{ item.text }}</el-radio>
                </div>
              </div>
              <div class="item__row" v-if="form.line4.areaType==1">
                <span class="title">日期格式</span>
                <div class="cont dateCont">
                  <el-button
                    v-for="(item, index) in dates"
                    :key="index"
                    @click="insertDateVal(form.line4, item.value)"
                    plain
                  >{{item.text}}</el-button>
                </div>
              </div>
              <div class="item__row" v-if="form.line4.areaType==0">
                <span class="title">插入文字</span>
                <div class="cont dateCont">
                  <el-button
                    v-for="(item, index) in texts"
                    :key="index"
                    @click="insertVal(form.line4, item.value)"
                    plain
                  >{{item.text}}</el-button>
                </div>
              </div>
              <div class="block-btm">
                <!-- 0:文本，可编辑。 1: 时间，不可编辑 -->
                <el-input
                  :readonly="form.line4.areaType===1"
                  v-model="form.line4.areaContent"
                  type="textarea"
                  :placeholder="form.line4.areaType===1?'请选择日期格式':'请输入内容'"
                ></el-input>
              </div>
            </div>
          </div>
          <div class="item">
            <p class="item__title">语音文字</p>
            <div class="item__block">
              <div class="item__row">
                <span class="title">插入文字</span>
                <div class="cont dateCont">
                  <el-button
                    v-for="(item, index) in voices"
                    :key="index"
                    @click="insertSoundVal(form, item.value)"
                    plain
                  >{{item.text}}</el-button>
                </div>
              </div>
              <div class="block-btm">
                <el-input v-model="form.soundText" type="textarea" placeholder="请输入内容"></el-input>
              </div>
            </div>
          </div>
          <div class="btns">
            <el-button type="primary" @click="cancel" plain>取消</el-button>
            <el-button type="primary" @click="save('form')" :loading="loading">保存</el-button>
          </div>
        </el-form>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { get, set, fetchList } from "@/api/platform/device/ledInfo";
import { mapGetters } from "vuex";
export default {
  data() {
    const displayOption = [
      { value: 0, label: "正常场景" },
      { value: 1, label: "有权限过车场景" },
      { value: 2, label: "无权限过车场景" }
    ];
    const colorOption = [
      { value: 0, color: "#ff5454", borderColor: "#ff5454" },
      { value: 1, color: "#5adf67", borderColor: "#5adf67" },
      { value: 2, color: "#ffd154", borderColor: "#ffd154" },
      { value: 3, color: "#54a4ff", borderColor: "#54a4ff" },
      { value: 4, color: "#b954ff", borderColor: "#b954ff" },
      { value: 5, color: "#36d9e8", borderColor: "#36d9e8" },
      { value: 6, color: "#FFF", borderColor: "#E6E6E6" }
    ];
    const actionOption = [
      { value: 0, text: "静止" },
      { value: 1, text: "向上移动" },
      { value: 2, text: "向下移动" },
      { value: 3, text: "向左移动" },
      { value: 4, text: "向右移动" },
      { value: 5, text: "闪烁" }
    ];
    const dateOption = [
      { value: "%Y4-%M3-%D2", text: "YYYY-MM-DD" },
      { value: "%Y4.%M3.%D2", text: "YYYY.MM.DD" },
      { value: "%h0:%m", text: "hh:mm" }
    ];
    const textOption = [
      { value: "余位：{ps}", text: "余位" },
      { value: "车牌号：{pn}", text: "车牌号" }
    ];
    const typeOption = [
      { value: 0, text: "文本" },
      { value: 1, text: "时间" }
    ];
    const voiceOption = [
      { value: "{pn}欢迎光临", text: "欢迎光临" },
      { value: "停车检查", text: "停车检查" },
      { value: "等待确认", text: "等待确认" }
    ];
    return {
      displays: displayOption,
      colors: colorOption,
      actions: actionOption,
      dates: dateOption,
      texts: textOption,
      types: typeOption,
      voices: voiceOption,
      line1Checked: false, //是否设置第一排
      line2Checked: false, //是否设置第二排
      line3Checked: false, //是否设置第三排
      line4Checked: false, //是否设置第四排
      displayScene: 0, //显示场景
      deviceCode: '',
      form: {},
      initData: {
        ledAreaList: [],
        soundText: "",
        line1: {
          areaRow: 1,
          areaColor: 0, //文字颜色
          areaAction: 0, //文字运动
          areaType: 0,
          areaContent: ""
        },
        line2: {
          areaRow: 2,
          areaColor: 0, //文字颜色
          areaAction: 0, //文字运动
          areaType: 0,
          areaContent: ""
        },
        line3: {
          areaRow: 3,
          areaColor: 0, //文字颜色
          areaAction: 0, //文字运动
          areaType: 0,
          areaContent: ""
        },
        line4: {
          areaRow: 4,
          areaColor: 0, //文字颜色
          areaAction: 0, //文字运动
          areaType: 0,
          areaContent: ""
        }
      },
      loading: false
    };
  },

  created() {
    this.deviceCode = this.$route.params.id;
    this.form = this.initData
    this.getDetails();
  },
  mounted() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    getDetails() {
      let obj = {
        deviceCode: this.deviceCode,
        displayScene : this.displayScene
      }
      get(obj).then(response => {
        if(response.data.data){
          this.form = response.data.data;
        }else{
          this.form = this.initData
        }
        if (this.validatenull(this.form.line1.areaContent)) {
          this.line1Checked = false;
        } else {
          this.line1Checked = true;
        }
        if (this.validatenull(this.form.line2.areaContent)) {
          this.line2Checked = false;
        } else {
          this.line2Checked = true;
        }
        if (this.validatenull(this.form.line3.areaContent)) {
          this.line3Checked = false;
        } else {
          this.line3Checked = true;
        }
        if (this.validatenull(this.form.line4.areaContent)) {
          this.line4Checked = false;
        } else {
          this.line4Checked = true;
        }
      });
    },
    displaySceneChange() {
      this.getDetails();
    },
    typeChange(obj) {
      obj.areaContent = "";
    },
    insertVal(obj, val) {
      obj.areaContent += val;
    },
    insertDateVal(obj, val) {
      obj.areaContent = val;
    },
    insertSoundVal(obj, val) {
      obj.soundText += val;
    },
    cancel() {
      const src = `/platform/device/automatic`;
      this.$router.push({
        path: src
      });
    },
    save() {
      this.form.ledAreaList = [];
      if (this.line1Checked) {
        if (this.validatenull(this.form.line1.areaContent)) {
          this.$message.error("请选择或输入第一排文字的内容！");
          return;
        } else {
          this.form.ledAreaList.push(this.form.line1);
        }
      }
      if (this.line2Checked) {
        if (this.validatenull(this.form.line2.areaContent)) {
          this.$message.error("请选择或输入第二排文字的内容！");
          return;
        } else {
          this.form.ledAreaList.push(this.form.line2);
        }
      }
      if (this.line3Checked) {
        if (this.validatenull(this.form.line3.areaContent)) {
          this.$message.error("请选择或输入第三排文字的内容！");
          return;
        } else {
          this.form.ledAreaList.push(this.form.line3);
        }
      }
      if (this.line4Checked) {
        if (this.validatenull(this.form.line4.areaContent)) {
          this.$message.error("请选择或输入第四排文字的内容！");
          return;
        } else {
          this.form.ledAreaList.push(this.form.line4);
        }
      }
      if (
        this.line1Checked ||
        this.line2Checked ||
        this.line3Checked ||
        this.line4Checked
      ) {
        // 成功后跳转
        let obj = Object.assign(
          {
            deviceCode: this.deviceCode,
            displayScene : this.displayScene
          },
          this.form
        )
        set(obj).then(response => {
          if (response.data.code == 0) {
            const src = `/platform/device/automatic`;
            this.$router.push({
              path: src
            });
          } else {
            this.$message.error(response.data.msg);
          }
        });
      } else {
        this.$message.error("请至少设置一排文字！");
        return;
      }
    }
  }
};
</script>

<style lang="scss" scoped>
.ledInfo ::v-deep {
  .form {
    width: 800px;
    margin: 0 auto;
  }
  .item {
    margin-bottom: 30px;
    .tip {
      float: right;
      color: #999;
      font-size: 12px;
      font-weight: normal;
      padding-left: 30px;
    }
    .ck1 {
      margin-left: 30px;
    }
    &__title {
      margin-bottom: 15px;
      font-size: 16px;
      font-weight: bold;
    }
    &__row {
      font-size: 12px;
      padding: 0 15px;
      margin: 10px 0;
      display: flex;
      line-height: 25px;
      .cont {
        padding-left: 20px;
      }
    }
    &__block {
      background: #fafafa;
      border: 1px solid #dcdfe6;
      .block-btm {
        height: 70px;
        padding: 5px 0;
        background: #fff;
        border-top: 1px solid #dcdfe6;
      }
      .el-textarea {
        height: 100%;
        background: #fff;
      }
      .el-textarea__inner {
        height: 100%;
        border: none;
      }
      .el-textarea__inner:read-only {
        color: #999;
      }
    }
  }
  .setted {
    .tip {
      display: none;
    }
    .item__block {
      border-color: #ed6d00;
    }
  }
  .rdColor {
    .el-radio__input {
      display: none;
    }
    .el-radio {
      margin: 0 5px;
      width: 20px;
      height: 20px;
      display: inline-block;
      border: 1px solid transparent;
    }
    .el-radio__label {
      display: none;
    }
    .el-radio.is-checked {
      width: 22px;
      height: 22px;
      border: 2px solid #000 !important;
    }
  }
  .rdAction {
    .el-radio__input {
      display: none;
    }
    .el-radio {
      margin: 0 5px;
      display: inline-block;
      background: #e8ebf0;
      padding: 3px 10px;
      border-radius: 10px;
    }
    .el-radio__label {
      font-size: 12px;
      padding-left: 0;
    }
    .el-radio.is-checked {
      background: #ed6d00;
    }
    .el-radio__input.is-checked + .el-radio__label {
      color: #fff;
    }
  }
  .dateCont {
    .el-button {
      font-size: 12px;
      padding: 3px 5px 2px 5px;
      margin: 0 5px;
    }
    .el-button.is-plain:hover {
      background-color: #fff;
      border-color: #409eff;
      color: #409eff;
    }
  }
  .btns {
    padding-top: 30px;
    text-align: center;
    .el-button {
      padding: 10px 25px;
      margin: 0 20px;
    }
  }
}
</style>
