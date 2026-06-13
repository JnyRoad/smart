<template>
<div class="info password-manager" :class="{'edit2':!isStrongPwd}">
    <div class="layout-nav-logo" v-if="!isStrongPwd"></div>
    <div class="my-basic-container app-container calendar-list-container">
      <el-scrollbar class="my-scrollbar" :native="false">
        <section class="my-basic-inner">
          <el-row>
            <el-col :span="24">
              <el-tabs @tab-click="switchTab" v-model="activeName">
                <el-tab-pane  label='信息管理' name='userManager' v-if="isStrongPwd"/>
                <el-tab-pane label='密码管理' name='passwordManager'/>
              </el-tabs>
              <div class="tip" v-if="!isStrongPwd">为保障你的账号安全，避免信息泄露，建议你设置新密码！</div>
              <div class="grid-content bg-purple">
                <el-form :model="ruleForm2"
                        :rules="rules2"
                        ref="ruleForm2"
                        label-width="100px"
                        label-position="top"
                        v-if="activeName==='userManager'"
                        class="demo-ruleForm">
                  <el-form-item label="用户名"
                                prop="username">
                    <el-input type="text"
                              v-model="ruleForm2.username"
                              disabled></el-input>
                  </el-form-item>
                  <el-form-item label="手机号" prop="phone">
                    <el-input v-model="ruleForm2.phone" placeholder="验证码登录使用"></el-input>
                  </el-form-item>
                  <!-- <el-form-item label="头像">
                    <el-upload
                      class="avatar-uploader"
                      action="/admin/file/upload"
                      :headers="headers"
                      :show-file-list="false"
                      :on-success="handleAvatarSuccess">
                      <img id="avatar" v-if="ruleForm2.avatar" :src="avatarUrl" class="avatar">
                      <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                    </el-upload>
                  </el-form-item> -->
                  <el-form-item>
                    <el-button type="primary"
                              @click="submitForm('ruleForm2')">提交
                    </el-button>
                    <el-button @click="resetForm('ruleForm2')">重置</el-button>
                  </el-form-item>
                </el-form>
                <el-form :model="ruleForm2"
                        :rules="rules2"
                        ref="ruleForm2"
                        label-width="100px"
                        label-position="top"
                        v-if="activeName==='passwordManager'"
                        class="demo-ruleForm">
                  <el-form-item label="原密码"
                                prop="password">
                    <el-input type="password"
                              v-model="ruleForm2.password"
                              auto-complete="off"></el-input>
                  </el-form-item>
                  <el-form-item label="新密码" prop="newpassword1">
                    <el-input type="password" v-model="ruleForm2.newpassword1" auto-complete="off"></el-input>
                  </el-form-item>
                  <div style="margin-bottom: 10px">
                    <p class="p1">1、密码不少于8位</p>
                    <p class="p1">2、包含数字、大写字母、小写字母、特殊符号</p>
                    <p class="p1">3、密码不能包含三个及以上相同数字或字母</p>
                    <p class="p1">4、密码不能包含三个及以上连续数字或字母</p>
                    <p class="p2">5、不可与原密码相同</p>
                  </div>
                  <el-form-item label="确认新密码" prop="newpassword2">
                    <el-input type="password" v-model="ruleForm2.newpassword2" auto-complete="off"></el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="submitForm('ruleForm2')">提交</el-button>
                    <el-button @click="resetForm('ruleForm2')" v-if="isStrongPwd">重置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-col>
          </el-row>
        </section>
      </el-scrollbar>
    </div>
</div>
</template>


<script>
  import {handleDown} from "@/api/admin/user";
  import {handleImg, openWindow} from '@/util/util'
  import { validatePwd } from "@/util/password";
  import {mapState} from 'vuex'
  import store from "@/store";
  import request from '@/router/axios'

  export default {
    data() {
      var vldPassword1 = (rule, value, callback) => {
        let r = validatePwd(value);
        if (r[0]) {
          callback(new Error(r[1]))
        } else {
          if(value === this.ruleForm2.password){
            callback(new Error('新密码不可与原密码相同！'))
          }else{
            callback();
          }
        }
      }
      var vldPassword2 = (rule, value, callback) => {
        if (value !== this.ruleForm2.newpassword1) {
          callback(new Error('确认新密码应该与新密码保持一致!'))
        } else {
          callback()
        }
      }

      return {
        activeName: 'userManager',
        avatarUrl: '',
        show: false,
        headers: {
          Authorization: 'Bearer ' + store.getters.access_token
        },
        ruleForm2: {
          username: '',
          password: '',
          newpassword1: '',
          newpassword2: '',
          avatar: '',
          phone: ''
        },
        rules2: {
         password: [
            { required: true, message: '请输入原密码', trigger: 'blur' },
            // { validator: vldPassword, trigger: "blur" }
          ],
          newpassword1: [
            { required: true, message: '请输入新密码', trigger: 'blur' },
            { validator: vldPassword1, trigger: "blur" }
          ],
          newpassword2: [
            { required: true, message: '请输入新密码进行确认', trigger: 'blur' },
            { validator: vldPassword2, trigger: "blur" }
          ]

        },
        isStrongPwd: true,
      }
    },
    mounted() {
    },
    created() {
      // Missing key means strong password; only an explicit "false" (weak-password 401)
      // should force the password tab (CR-C14-002).
      this.isStrongPwd = localStorage.getItem("isStrongPwd") !== "false";
      if(!this.isStrongPwd){
        this.ruleForm2.username = localStorage.getItem("tempUserName");
      }else{
        this.ruleForm2.username = this.userInfo.username
      }
      this.ruleForm2.phone = this.userInfo.phone
      this.isStrongPwd?this.activeName = 'userManager':this.activeName = 'passwordManager'
      handleImg(this.userInfo.avatar, 'avatar')
    },
    computed: {
      ...mapState({
        userInfo: state => state.user.userInfo,
      }),
    },
    methods: {
      logOutHandle(){
        this.$store.dispatch('LogOut').then(() => {
          //location.reload() // 为了重新实例化vue-router对象 避免bug
          this.$router.push({path: "/login"}); //弱密码修改后，跳转到登录页
        })
      },
      switchTab(tab, event) {
        if (tab.name === 'userManager') {
          handleImg(this.ruleForm2.avatar, 'avatar')
        }
        this.doClearValidate('ruleForm2')
      },
      submitForm(formName) {
        this.$refs[formName].validate(valid => {
          if (valid) {
            request({
              url: '/admin/user/edit',
              method: 'post',
              data: this.ruleForm2
            }).then(response => {
              if (response.data.data) {
                this.$notify({
                  title: '成功',
                  message: '修改成功',
                  type: 'success',
                  duration: 2000
                })
                // 修改密码之后强制重新登录
                if (this.activeName === 'passwordManager') {
                  this.logOutHandle()
                }
              } else {
                this.$notify({
                  title: '失败',
                  message: response.data.msg,
                  type: 'error',
                  duration: 2000
                })
              }
            }).catch(() => {
              this.$notify({
                title: '失败',
                message: '修改失败',
                type: 'error',
                duration: 2000
              })
            })
          } else {
            return false
          }
        })
      },
      resetForm(formName) {
        this.$refs[formName].resetFields()
      },
      handleAvatarSuccess(res, file) {
        this.avatarUrl = URL.createObjectURL(file.raw);
        this.ruleForm2.avatar = res.data.bucketName + "-" + res.data.fileName;
      }
    }
  }
</script>
<style lang="scss" scoped>

.password-manager ::v-deep {
  .avatar-uploader .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
  }
  .myTab{
    padding: 0 20px;
  }
  .avatar-uploader .el-upload:hover {
    border-color: #409eff;
  }

  .avatar-uploader-icon {
    font-size: 28px !important;
    color: #8c939d !important;
    width: 178px !important;
    height: 178px !important;
    line-height: 178px !important;
    text-align: center !important;
  }

  .avatar {
    width: 178px;
    height: 178px;
    display: block;
  }
  .grid-content{
    width: 400px;
    padding: 0 30px;
  }
  .el-form--label-top .el-form-item__label{
    padding: 0;
    line-height: 30px;
  }
  .el-form-item{
    margin-bottom: 15px;
  }
  .p1, .p2{
    font-size: 12px;
    line-height: 20px;
    color: $tipColor;
  }
}
.edit2 ::v-deep {
  height: 100%;
  align-items: center;
  background: #081740 url('/img/login_stars.png') left bottom no-repeat;
  background-size: cover;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
  .my-basic-inner{
    padding: 0;
  }
  .layout-nav-logo {
    position: fixed;
    left: 50px;
    top: 0;
    width: 204px;
    height: 50px;
    margin: 0 auto;
    margin-top: 26px;
    overflow: hidden;
    background: url(/img/yuTongLogo.png) no-repeat 50%;
    background-size: 100% 100%;
  }
  .my-basic-container {
    height: 510px;
    margin: 0 auto;
    padding: 0;
    align-items: center;
    border-radius: 5px;
    overflow: hidden;
  }
  .my-scrollbar{
    border-radius: 5px;
  }
  .grid-content{
    margin-bottom: 30px;
  }
  .el-tabs__header{
    margin-bottom: 0;
  }
  .el-tabs__item{
    font-size: 16px;
    height: 50px;
    line-height: 50px;
  }
  .el-tabs__active-bar{
    display: none;
  }
  .el-tabs__nav-wrap::after{
    display: none;
  }
  .el-tabs__nav-scroll{
    padding: 0 30px;
  }
  .tip{
    color: $color-danger;
    padding: 0 20px;
    background-color: #fdeaea;
    border-color: #fad4d5;
    font-size: 12px;
    line-height: 25px;
    margin-bottom: 15px;
  }
}
</style>
