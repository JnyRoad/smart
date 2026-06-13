<!--园区服务：员工反馈 ：员工反馈详情  -->
<template>
  <div class="my-basic-container center-card feed_back">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-form ref="form" :model="editForm" :rules="edit_rules" label-width="120px">
          <div class="center-conent">
            <p class="box-orange">反馈信息</p>
            <el-row class="info-row">
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="反馈人：" prop="staffName">
                    <span>{{editForm.staffName}}</span>
                  </el-form-item>
                </el-col>
              </el-col>

              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="手机号：" prop="staffPhone">
                    <span>{{editForm.staffPhone}}</span>
                  </el-form-item>
                </el-col>
              </el-col>

              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="BU：" prop="compName">
                    <span>{{editForm.compName}}</span>
                  </el-form-item>
                </el-col>
              </el-col>

              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="部门：" prop="depName">
                    <span>{{editForm.depName}}</span>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="员工号：" prop="staffBadge">
                    <span>{{editForm.staffBadge}}</span>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="反馈时间：" prop="createTime">
                    <span>{{editForm.createTime}}</span>
                  </el-form-item>
                </el-col>
              </el-col>
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="反馈问题标签：" prop="question">
                    <span>{{editForm.question}}</span>
                  </el-form-item>
                </el-col>
              </el-col>
            </el-row>
            <p class="box-orange">处理信息</p>
            <el-row class="info-row">
              <el-col :span="24">
                <el-col :span="12">
                  <el-form-item label="处理回复：" prop="reply">
                    <el-input type="textarea" rows="6" v-model="editForm.reply" maxlength="100"></el-input>
                  </el-form-item>
                </el-col>
              </el-col>
              <template v-if="editForm.status==1">
                <el-col :span="24">
                  <el-col :span="12">
                    <el-form-item label="处理人：" prop="operator">
                      <span>{{editForm.operator}}</span>
                    </el-form-item>
                  </el-col>
                </el-col>
                <el-col :span="24">
                  <el-col :span="12">
                    <el-form-item label="处理状态：" prop="status">
                      <span>{{editForm.status==0?'未处理':'已处理'}}</span>
                    </el-form-item>
                  </el-col>
                </el-col>
                <el-col :span="24">
                  <el-col :span="12">
                    <el-form-item label="处理时间：" prop="operateTime">
                      <span>{{editForm.operateTime}}</span>
                    </el-form-item>
                  </el-col>
                </el-col>
              </template>
            </el-row>
          </div>
        </el-form>
        <div class="btns-bottom">
          <template>
            <el-button type="primary" @click="addSubmit()">保存</el-button>
          </template>
          <el-button type="primary" @click="goBack()" plain>取消</el-button>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/recruit/add" as *;
</style>

<script>
import { getById, putObj } from "@/api/platform/park_service/feed_back";
import { mapGetters } from "vuex";
export default {
  name: "feed_back",
  data() {
    return {
      editForm: {},
      edit_rules: {
        reply: [{ required: true, message: "请输入处理回复", trigger: "blur" }]
      }
    };
  },
  created() {
    var id = this.$route.params.id;
    var params = { id };
    var _this = this;
    getById(id).then(response => {
      this.editForm = response.data.data;
    });
  },
  mounted: function() {},
  watch: {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/park_service/feed_back`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    },

    addSubmit() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          putObj(this.editForm)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult == true) {
                this.$notify({
                  title: "成功",
                  message: msg,
                  type: "success",
                  duration: 2000
                });
                this.goBack();
              } else if (dataResult === false) {
                this.$notify({
                  title: "失败",
                  message: msg,
                  type: "error",
                  duration: 2000
                });
              }
            })
            .catch(() => {
              this.$notify({
                title: "失败",
                message: "修改失败",
                type: "error",
                duration: 2000
              });
            });
        } else {
          return false;
        }
      });
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
