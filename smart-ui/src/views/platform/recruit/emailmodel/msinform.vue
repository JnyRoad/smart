<!--招聘管理，邮件通知模板 面试通知模板  -->
<template>
  <div class="content">
    <el-form
      ref="emailForm"
      :rules="emailRules"
      :model="emailForm"
      label-width="110px"
      label-position="top"
    >
      <el-row>
        <!-- <el-col :span="8">
          <el-form-item label="联系人" prop="t1">
            <el-input v-model="emailForm.t1"></el-input>
          </el-form-item>
          <el-form-item label="联系人电话" prop="t1">
            <el-input v-model="emailForm.t1"></el-input>
          </el-form-item>
          <el-form-item label="邮箱地址" prop="t1">
            <el-input v-model="emailForm.t1"></el-input>
          </el-form-item>
          <el-form-item label="乘车路线" prop="t1">
            <el-input v-model="emailForm.t1" type="textarea" :rows="5"></el-input>
          </el-form-item>
          <el-button type="primary" icon="" @click="saveInfo('emailForm')">确认并保存</el-button>
        </el-col>
        <el-col :span="2">&nbsp;</el-col>-->
        <el-col :span="13">
<!--          <el-form-item label="园区" prop="parkId">
            <parkSelect
              :defaultSelected="true"
              @defaultHandle="parkDefault"
              v-model="emailForm.parkId"
              @doChange="getInform"
            ></parkSelect>
          </el-form-item>-->
          <el-form-item label="邮件标题" prop="tempName">
            <el-input
              v-model="emailForm.tempName"
              placeholder="请输入邮件标题"
            ></el-input>
          </el-form-item>
          <el-form-item label="邮件内容" prop="tempContent">
            <!-- <div class="tags">
              <template v-for="(item, index) in tags">
                <el-tag type="info"  :key="index" @click.native="tagClick(item)">{{item}}</el-tag>
              </template>
            </div>-->
            <div id="wangeditor">
              <div ref="editorElem"></div>
            </div>
          </el-form-item>
          <el-button type="primary" icon @click="saveInfo('emailForm')"
            >确认并保存</el-button
          >
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>
<style lang="scss">
@use "@/styles/platform/recruit/emailmodel" as *;
</style>
<script>
import { getDetail, editModel } from "@/api/platform/recruit/emailmodel";
import { mapGetters } from "vuex";
import E from "wangeditor";

const tagsOption = ["姓名", "岗位", "面试时间", "面试地址"];
export default {
  name: "emailmodel",
  data() {
    return {
      emailForm: {
        tempCode: 2201,
      },
      emailRules: {
        //parkId: [{ required: true, message: "请选择园区", trigger: "blur" }],
        tempName: [
          { required: true, message: "请输入邮件标题", trigger: "blur" },
        ],
        tempContent: [
          { required: true, message: "请输入邮件内容", trigger: "blur" },
        ],
      },
      tags: tagsOption,
      editor: null,
    };
  },
  created() {
    this.getInform();
  },
  mounted: function () {
    this.initEditor();
  },
  computed: {
    ...mapGetters(["permissions"]),
  },
  methods: {
    //parkDefault(e){
    //  this.getInform(e.value);
    //},
    initEditor() {
      this.editor = new E(this.$refs.editorElem);
      // 编辑器的事件，每次改变会获取其html内容
      this.editor.customConfig.onchange = (html) => {
        this.emailForm.tempContent = html;
      };
      this.editor.customConfig.menus = [
        // 菜单配置
        "head", // 标题
        "bold", // 粗体
        "fontSize", // 字号
        "fontName", // 字体
        "italic", // 斜体
        "underline", // 下划线
        "strikeThrough", // 删除线
        "foreColor", // 文字颜色
        "backColor", // 背景颜色
        "link", // 插入链接
        "list", // 列表
        "justify", // 对齐方式
        "quote", // 引用
        "emoticon", // 表情
        "image", // 插入图片
        "table", // 表格
        //'code', // 插入代码
        "undo", // 撤销
        "redo", // 重复
      ];
      this.editor.create(); // 创建富文本实例
    },
    tagClick(item) {
      var txt = "<span>" + item + "</span>";
      this.editor.txt.append(txt);
    },
    getInform() {
      var tempCode = this.emailForm.tempCode;
      this.emailForm.id = null;
      getDetail(tempCode).then((response) => {
        if (response.data.code == 0) {
          this.emailForm = response.data.data.msgTemplate;
        } else {
          this.emailForm.tempName = "";
          this.emailForm.tempContent = "";
        }
        var txt = "<div>" + this.emailForm.tempContent + "</div>";
        this.editor.txt.clear();
        this.editor.txt.html(txt);
      });
    },
    saveInfo(formName) {
      let _this = this;
      this.$refs[formName].validate((valid) => {
        if (valid) {
          var obj = { msgTemplate: _this.emailForm };
          editModel(obj)
            .then((response) => {
              if (response.data.code == 0) {
                this.$notify({
                  title: "保存成功",
                  type: "success",
                });
                this.getInform();
              } else {
                this.$notify.error({
                  title: "保存失败",
                });
              }
            })
            .catch((err) => {
              this.$notify.error({
                title: "保存失败",
              });
            });
        } else {
          return false;
        }
      });
    },
    tbClick(e) {
    },
  },
};
</script>

<style lang="scss" scoped>
</style>
