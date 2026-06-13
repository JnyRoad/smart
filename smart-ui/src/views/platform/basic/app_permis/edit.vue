<!--基础信息，APP权限，编辑  -->
<template>
  <div class="my-basic-container limit">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="cancelEdit">返回</el-button>
        </div>
        <div class="limit-inner">
          <el-row class>
            <el-col :span="12">
              <el-form
                ref="editform"
                :rules="rules"
                :model="editform"
                label-width="80px"
                label-position="left"
                class="editform"
              >
                <div class="maxW">
                  <el-form-item label="所属园区" prop="parkId">
                    <!-- <parkSelect v-model="editform.parkId"></parkSelect> -->
                    <el-select v-model="editform.parkId" placeholder="请选择" @change="handleChange">
                      <template v-for="(item, index) in parkList">
                        <el-option :label="item.parkName" :value="item.id" :key="index"></el-option>
                      </template>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="权限名称" prop="authName">
                    <el-input v-model="editform.authName"></el-input>
                  </el-form-item>
                  <el-form-item label="备注" prop="authDesc">
                    <el-input
                      type="textarea"
                      :rows="3"
                      v-model="editform.authDesc"
                      placeholder="不超过60个字"
                    ></el-input>
                  </el-form-item>
                  <el-form-item label="允许查看APP的职层" label-width="150px" prop="jcheId">
                    <el-checkbox-group v-model="editform.jcheId" class="zclist">
                      <el-checkbox
                        v-for="(item, index) in jcheListOption"
                        :label="item.value"
                        :key="index"
                      >{{item.label}}</el-checkbox>
                    </el-checkbox-group>
                  </el-form-item>
                  <el-form-item label="是否为园区前端通用菜单" label-width="180px" prop="initFlag" v-if="!initFlag">
                    <el-switch active-color="#10CC8F" inactive-color="#e7292e" :active-value="0" :inactive-value="1" v-model="editform.initFlag"></el-switch>
                  </el-form-item>
                </div>
                <el-form-item class="btns">
                  <el-button type="primary" @click="setCheckedNodes">全选</el-button>
                  <el-button type="primary" @click="resetChecked" plain>清空</el-button>
                </el-form-item>
                <el-form-item label="选择权限" prop="moduleId">
                  <div class="qt-limit">
                    <el-tree
                      :data="treeDataAuth"
                      ref="authtree"
                      node-key="id"
                      show-checkbox
                      default-expand-all
                      :default-checked-keys="editform.hrAuthId"
                      :props="authProps"
                    ></el-tree>
                    <el-tree
                      :data="treeDataModule"
                      ref="moduletree"
                      node-key="id"
                      show-checkbox
                      default-expand-all
                      :default-checked-keys="editform.moduleId"
                      :props="moduleProps"
                    ></el-tree>
                  </div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="onSubmit('editform')" :loading="editLoading">保存</el-button>
                  <el-button type="primary" @click="cancelEdit" plain>取消</el-button>
                </el-form-item>
              </el-form>
            </el-col>
            <el-col :span="12"></el-col>
          </el-row>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>
<!--使用的区域管理内，权限策略的样式-->
<style lang="scss">
@use "@/styles/platform/area/limit" as *;
</style>

<script>
import {
  getById,
  fetchModule,
  getHrAuth,
  putObj,
  getList,
  getInitFlag
} from "@/api/platform/basic/app_permis";
import { mapGetters } from "vuex";
import { getJchesObj } from "@/api/platform/_publicService";

export default {
  name: "app_permis",
  data() {
    return {
      jcheListOption: [],
      editLoading: false,
      editform: {
        parkId: undefined,
        authName: "",
        authDesc: "",
        hrAuthId: [],
        moduleId: [],
        jcheId: [],
        initFlag: 1
      },
      parkList: [],
      initFlag: true,
      appauthId: "",
      rules: {
        parkId: [
          { required: true, message: "请选择园区", trigger: "change" }
        ],
        authName: [
          { required: true, message: "请输入权限名称", trigger: "blur" }
        ],
        // authDesc: [
        //   { required: true, message: "请输入备注", trigger: "blur" }
        // ],
        jcheId: [
          { required: true, message: "请选择至少一个职层", trigger: "blur" }
        ],
        moduleId: [
          { required: true, message: "请选择至少一个服务模块", trigger: "blur" }
        ]
      },
      treeAuthChild: [],
      treeModuleChild: [],
      authProps: {
        children: "children",
        label: "authName"
      },
      moduleProps: {
        children: "children",
        label: "moduleName"
      }
    };
  },
  created() {
    this.appauthId = this.$route.params.id;
    this.getDataList()
    this.getHr();
    this.getModule();
    // 获取职层字典
    getJchesObj().then(response => {
      let self = this;
      response.data.data.forEach((item, $index) => {
        self.jcheListOption.push({ label: item.typeName, value: item.typeCode });
      });
      // console.log("self.jcheLis====================>"+JSON.stringify(self.jcheListOption));
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"]),
    treeDataAuth: function() {
      var treeData = [
        {
          id: -1,
          authName: "招聘权限",
          children: this.treeAuthChild
        }
      ];
      return treeData;
    },
    treeDataModule: function() {
      var treeData = [
        {
          id: -1,
          moduleName: "服务模块",
          children: this.treeModuleChild
        }
      ];
      return treeData;
    }
  },
  methods: {
    async getDataList() {
      let res = await getList()
      this.parkList = res.data.data
    },
    getDetail(id) {
      //详情
      getById(id).then(response => {
        if (response.data.data) {
          this.editform = response.data.data;
          this.getInitFlag(this.editform.parkId)
          if(!this.editform.jcheId) {
            this.editform.jcheId = []
          }
        }
      });
    },
    getHr() {
      //获取HR权限列表
      getHrAuth().then(response => {
        this.treeAuthChild = response.data.data;
      });
    },
    getModule() {
      //获取权限模块列表
      var _this = this;
      fetchModule().then(response => {
        this.treeModuleChild = response.data.data;
        _this.getDetail(_this.appauthId);
      });
    },
    resetChecked() {
      //清空
      this.$refs.authtree.setCheckedKeys([]);
      this.$refs.moduletree.setCheckedKeys([]);
    },
    setCheckedNodes() {
      //全选
      this.$refs.authtree.setCheckedNodes(this.treeDataAuth);
      this.$refs.moduletree.setCheckedNodes(this.treeDataModule);
    },
    onSubmit(formName) {
      var _this = this;

      this.editform.hrAuthId = this.$refs.authtree.getCheckedKeys();
      this.editform.moduleId = this.$refs.moduletree.getCheckedKeys();

      this.editform.hrAuthId.forEach(function(item, index) {
        if (item == -1) {
          _this.editform.hrAuthId.splice(index, 1);
        }
      });

      this.editform.moduleId.forEach(function(item, index) {
        if (item == -1) {
          _this.editform.moduleId.splice(index, 1);
        }
      });

      this.$refs[formName].validate(valid => {
        if (valid) {
          this.editLoading = true;
          putObj(this.editform)
            .then(response => {
              var msg = response.data.msg;
              var dataResult = response.data.data;
              if (dataResult === true) {
                this.editFormVisible = false;
                this.$notify({
                  title: "成功",
                  message: "修改成功",
                  type: "success",
                  duration: 2000
                });
                this.cancelEdit()
              } else if (dataResult === false) {
                this.$notify({
                  title: "失败",
                  message: msg,
                  type: "error",
                  duration: 2000
                });
              }
              this.editLoading = false;
            })
            .catch(err => {
              this.editLoading = false;
            });
        } else {
          return false;
        }
      });
    },
    cancelEdit() {
      this.$router.go(-1);
    },
    async getInitFlag(id) {
      const res = await getInitFlag(id)
      this.initFlag = res.data.data
    },
    async handleChange(id) {
      const res = await getInitFlag(id)
      this.initFlag = res.data.data
    },
  }
};
</script>

<style lang="scss" scoped>
  .zclist {
    padding-top: 0px;
    .el-checkbox{
      margin-right: 20px;
    }
    .el-checkbox+.el-checkbox{
      margin-left: 0;
    }
  }
</style>
