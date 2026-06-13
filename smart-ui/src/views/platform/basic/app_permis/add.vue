<!--基础信息，APP权限，添加  -->
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
              <el-form ref="addform" :rules="rules" :model="addform" label-width="80px" label-position="left" class="addform">
                <div class="maxW">
                  <el-form-item label="所属园区" prop="parkId">
                    <!-- <parkSelect v-model="addform.parkId" @change="handleChange"></parkSelect> -->
                    <el-select v-model="addform.parkId" placeholder="请选择" @change="handleChange">
                      <template v-for="(item, index) in parkList">
                        <el-option :label="item.parkName" :value="item.id" :key="index"></el-option>
                      </template>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="权限名称" prop="authName">
                    <el-input v-model="addform.authName" placeholder="不超过10个字"></el-input>
                  </el-form-item>
                  <el-form-item label="备注" prop="authDesc">
                    <el-input type="textarea" :rows="3" v-model="addform.authDesc" placeholder="不超过50个字"></el-input>
                  </el-form-item>
                  <el-form-item label="允许查看APP的职层" label-width="150px" prop="jcheId">
                    <el-checkbox-group v-model="addform.jcheId" class="zclist">
                      <el-checkbox v-for="(item, index) in jcheListOption" :label="item.value" :key="index">{{ item.label }}</el-checkbox>
                    </el-checkbox-group>
                  </el-form-item>
                  <el-form-item label="是否为园区前端通用菜单" label-width="180px" prop="initFlag" v-if="!initFlag">
                    <el-switch active-color="#10CC8F" inactive-color="#e7292e" :active-value="0" :inactive-value="1" v-model="addform.initFlag"></el-switch>
                  </el-form-item>
                </div>
                <el-form-item class="btns">
                  <el-button type="primary" @click="setCheckedNodes">全选</el-button>
                  <!-- <el-button type="primary" @click="onSubmit" plain>反选</el-button> -->
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
                      :default-checked-keys="addform.hrAuthId"
                      :props="authProps"
                    ></el-tree>
                    <el-tree
                      :data="treeDataModule"
                      ref="moduletree"
                      node-key="id"
                      show-checkbox
                      default-expand-all
                      :default-checked-keys="addform.moduleId"
                      :props="moduleProps"
                    ></el-tree>
                  </div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="onSubmit('addform')" :loading="addLoading">保存</el-button>
                  <el-button type="primary" @click="cancelAdd" plain>取消</el-button>
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

<style lang="scss">
@use "@/styles/platform/area/limit" as *;
</style>

<script>
import { fetchModule, addObj, getHrAuth, getInitFlag, getList } from '@/api/platform/basic/app_permis'
import { mapGetters } from 'vuex'
import { getJchesObj } from '@/api/platform/_publicService'
export default {
  name: 'app_permis',
  data() {
    return {
      jcheListOption: [],
      addLoading: false,
      addform: {
        parkId: undefined,
        authName: '',
        authDesc: '',
        hrAuthId: [],
        moduleId: [],
        jcheId: [],
        initFlag: 1
      },
      rules: {
        parkId: [{ required: true, message: '请选择园区', trigger: 'change' }],
        authName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
        // authDesc: [
        //   { required: true, message: "请输入备注", trigger: "blur" }
        // ],
        jcheId: [{ required: true, message: '请选择至少一个职层', trigger: 'blur' }],
        moduleId: [{ required: true, message: '请选择至少一个服务模块', trigger: 'blur' }]
      },
      treeDataAuth: [
        {
          id: -1,
          authName: '招聘权限',
          children: []
        }
      ],
      treeDataModule: [
        {
          id: -1,
          moduleName: '服务模块',
          children: []
        }
      ],
      authProps: {
        children: 'children',
        label: 'authName'
      },
      moduleProps: {
        children: 'children',
        label: 'moduleName'
      },
      parkList: [],
      initFlag: true
    }
  },
  created() {
    this.getHr()
    this.getDataList()
    this.getModule()
    // 获取职层字典
    getJchesObj().then((response) => {
      let self = this
      response.data.data.forEach((item, $index) => {
        self.jcheListOption.push({ label: item.typeName, value: item.typeCode })
      })
      //console.log("self.jcheLis====================>"+JSON.stringify(self.jcheListOption));
    })
  },
  mounted: function () {},
  computed: {
    ...mapGetters(['permissions'])
  },
  methods: {
    async getDataList() {
      let res = await getList()
      this.parkList = res.data.data
    },
    getHr() {
      //获取HR权限列表
      getHrAuth().then((response) => {
        this.treeDataAuth[0].children = response.data.data
      })
    },
    getModule() {
      //获取权限模块列表
      var _this = this
      fetchModule().then((response) => {
        this.treeDataModule[0].children = response.data.data
      })
    },
    resetChecked() {
      //清空
      this.$refs.authtree.setCheckedKeys([])
      this.$refs.moduletree.setCheckedKeys([])
    },
    setCheckedNodes() {
      //全选
      this.$refs.authtree.setCheckedNodes(this.treeDataAuth)
      this.$refs.moduletree.setCheckedNodes(this.treeDataModule)
    },
    onSubmit(formName) {
      var _this = this

      this.addform.hrAuthId = this.$refs.authtree.getCheckedKeys()
      this.addform.moduleId = this.$refs.moduletree.getCheckedKeys()

      this.addform.hrAuthId.forEach(function (item, index) {
        if (item == -1) {
          _this.addform.hrAuthId.splice(index, 1)
        }
      })

      this.addform.moduleId.forEach(function (item, index) {
        if (item == -1) {
          _this.addform.moduleId.splice(index, 1)
        }
      })

      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.addLoading = true
          addObj(this.addform)
            .then((response) => {
              var msg = response.data.msg
              var dataResult = response.data.data
              if (dataResult === true) {
                this.resetAddFrom(formName)
                this.addFormVisible = false
                this.$notify({
                  title: '成功',
                  message: '添加成功',
                  type: 'success',
                  duration: 2000
                })
                this.cancelEdit()
              } else if (dataResult === false) {
                this.$notify({
                  title: '失败',
                  message: msg,
                  type: 'error',
                  duration: 2000
                })
              }
              this.addLoading = false
            })
            .catch((err) => {
              this.addLoading = false
            })
        } else {
          return false
        }
      })
    },
    cancelAdd() {
      this.$router.go(-1)
    },
    cancelEdit() {
      this.$router.go(-1)
    },
    resetAddFrom(formName) {
      this.$refs[formName].resetFields()
      this.resetChecked()
    },
    async handleChange(id) {
      const res = await getInitFlag(id)
      this.initFlag = res.data.data
    },
    // async getInitFlag(){

    // }
  }
}
</script>

<style lang="scss" scoped>
.zclist {
  padding-top: 0px;
  .el-checkbox {
    margin-right: 20px;
  }
  .el-checkbox + .el-checkbox {
    margin-left: 0;
  }
}
</style>
