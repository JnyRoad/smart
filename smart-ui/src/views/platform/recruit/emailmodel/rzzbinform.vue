<!--招聘管理，邮件通知模板 入职准备通知模板  -->
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
        <el-col :span="10">
<!--          <el-form-item label="园区" prop="parkId">
            <parkSelect
              :defaultSelected="true"
              @defaultHandle="parkDefault"
              v-model="emailForm.parkId"
              @doChange="getInform"
            ></parkSelect>
          </el-form-item>-->
          <el-form-item label="邮件标题" prop="tempName">
            <el-input v-model="emailForm.tempName" placeholder="请输入邮件标题"></el-input>
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
          <!-- 保存模板，带接收人，目前仅开放管理员可见权限 -->
          <el-button  v-if="permissions.sys_recruit_model_edit" type="primary" icon @click="saveInfo('emailForm')">确认并保存</el-button>
        </el-col>
        <el-col :span="1">&nbsp;</el-col>
        <el-col :span="12">
          <p class="clear">
            <el-button
              type="text"
              class="float-right"
              icon="el-icon-plus"
              @click="addFormVisible = true"
            >新增接收人</el-button>邮件接收人
          </p>
          <table class="ps-table">
            <tr>
              <td>园区</td>
              <td>姓名</td>
              <td>手机</td>
              <td>邮箱</td>
              <td>管理</td>
            </tr>
            <template v-for="(item, index) in personList">
              <tr :key="index">
                <td>{{item.parkName}}</td>
                <td>{{item.name}}</td>
                <td>{{item.phone}}</td>
                <td>{{item.email}}</td>
                <td>
                  <el-button type="text" icon="el-icon-delete" @click="delPerson(index)">删除</el-button>
                </td>
              </tr>
            </template>
            <template v-if="!hasList">
              <tr>
                <td colspan="12">
                  <span class="noData">还未设置接收人</span>
                </td>
              </tr>
            </template>
          </table>
          <div style="text-align: center;padding-top: 30px;">
            <el-button type="primary" icon @click="savePerson()">保存接收人</el-button>
          </div>
        </el-col>
      </el-row>
    </el-form>
    <el-dialog
      title="新增接收人"
      class="dialog_form"
      width="550px"
      @close="resetAddForm('addFormRef')"
      :visible.sync="addFormVisible"
    >
      <el-form :rules="addRules" ref="addFormRef" :model="addForm" label-width="80px">
        <el-form-item label="所属园区" prop="parkId">
          <parkSelect v-model="addForm.parkId"></parkSelect>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="addForm.name" clearable></el-input>
        </el-form-item>
        <el-form-item label="手机" prop="phone">
          <el-input v-model="addForm.phone" clearable></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="addForm.email" clearable></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="resetAddForm('addFormRef')" plain>取 消</el-button>
        <el-button type="primary" @click="addSubmit('addFormRef')">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<style lang="scss">
@use "@/styles/platform/recruit/emailmodel" as *;
</style>
<script>
import { getDetail, editModel, editPerson, getPark  } from "@/api/platform/recruit/emailmodel";
import { mapGetters } from "vuex";
import E from "wangeditor";
import { isMobile } from "@/util/validate";
const tagsOption = ["姓名", "岗位", "面试时间", "面试地址"];
// sys_recruit_model_edit
export default {
  name: "emailmodel",
  data() {
    var validatePhone = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error("请输入电话"));
      } else {
        if (!isMobile(value.replace(/(^\s*)|(\s*$)/g, ""))) {
          callback(new Error("请输入正确的电话"));
        } else {
          callback();
        }
      }
    };
    var validateEmail = (rule, value, callback) => {
      if (this.validatenull(value)) {
        callback(new Error("请输入邮箱"));
      } else {
        if (
          !/^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((.[a-zA-Z0-9_-]{2,3}){1,2})$/.test(
            value.replace(/(^\s*)|(\s*$)/g, "")
          )
        ) {
          callback(new Error("请输入正确的邮箱"));
        } else {
          callback();
        }
      }
    };
    return {
      addFormVisible: false,
      personList: [],
      emailForm: {
        tempCode: 5001
      },
      emailRules: {
        //parkId: [{ required: true, message: "请选择园区", trigger: "blur" }],
        tempName: [
          { required: true, message: "请输入邮件标题", trigger: "blur" }
        ],
        tempContent: [
          { required: true, message: "请输入邮件内容", trigger: "blur" }
        ]
      },
      addForm: {
        parkId: "",
        parkName: "",
        name: "",
        phone: "",
        email: ""
      },
      addRules: {
        parkId: [{ required: true, message: "请选择园区", trigger: "blur" }],
        name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
        phone: [
          { required: true, message: "请输入电话", trigger: "blur" },
          { validator: validatePhone, trigger: "blur" }
        ],
        email: [
          { required: true, message: "请输入邮箱", trigger: "blur" },
          { validator: validateEmail, trigger: "blur" }
        ]
      },
      tags: tagsOption,
      editor: null
    };
  },
  created() {
    this.getInform();
  },
  mounted: function() {
    this.initEditor();
  },
  computed: {
    ...mapGetters(["permissions"]),
    hasList: function() {
      if (this.personList.length > 0) {
        return true;
      } else {
        return false;
      }
    }
  },
  methods: {
    parkDefault(e){
      this.getInform(e.value);
    },
    initEditor() {
      this.editor = new E(this.$refs.editorElem);
      // 编辑器的事件，每次改变会获取其html内容
      this.editor.customConfig.onchange = html => {
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
        "redo" // 重复
      ];
      this.editor.create(); // 创建富文本实例
    },
    tagClick(item) {
      var txt = "<span>" + item + "</span>";
      this.editor.txt.append(txt);
    },
    delPerson(index) {
      this.personList.splice(index, 1);
    },
    getInform() {
      //获取详情
      let tempCode = this.emailForm.tempCode;
      getDetail(tempCode).then(response => {
        if (response.data.code == 0) {
          this.emailForm = response.data.data.msgTemplate;
          this.personList = response.data.data.receiveList;
          // this.emailForm = response.data.data;
        }
        let txt = "<div>" + this.emailForm.tempContent + "</div>";
        this.editor.txt.clear();
        this.editor.txt.html(txt);
      });
    },
    //保存模板，带接收人，目前仅开放管理员可见权限
    saveInfo(formName) {
      let _this = this;
      this.$refs[formName].validate(valid => {
        if (valid) {
          let obj = {
            msgTemplate: _this.emailForm
          };
          editModel(obj)
            .then(response => {
              if (response.data.code == 0) {
                this.$notify({
                  title: "保存成功",
                  type: "success"
                });
                this.getInform();
              } else {
                this.$notify.error({
                  title: "保存失败"
                });
              }
            })
            .catch(err => {
              this.$notify.error({
                title: "保存失败"
              });
            });
        } else {
          return false;
        }
      });
    },
    //仅保存接收人，用户开放
    savePerson(){
      let obj = {
        msgTemplate: this.emailForm,
        receiveList: this.personList
      };
      editPerson(obj)
        .then(response => {
          if (response.data.code == 0) {
            this.$notify({
              title: "保存成功",
              type: "success"
            });
            this.getInform();
          } else {
            this.$notify.error({
              title: "保存失败"
            });
          }
        })
        .catch(err => {
          this.$notify.error({
            title: "保存失败"
          });
        });
    },
    addSubmit(formName) {
      //添加接收人
      this.$refs[formName].validate(valid => {
        if (valid) {
          getPark(this.addForm.parkId).then(response => {
            this.addForm.parkName =  response.data.data.parkName
            this.personList.push(Object.assign({}, this.addForm));
            this.addFormVisible = false;
          });
        } else {
          return false;
        }
      });
    },
    resetAddForm(formName) {
      this.addFormVisible = false;
      this.$refs[formName].clearValidate();
      this.$refs[formName].resetFields();
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
