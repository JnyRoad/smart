<!--区域管理，权限策略，编辑  -->
<template>
  <div class="my-basic-container limit">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="limit-inner">
          <el-row class>
            <el-col :span="12">
              <el-form
                ref="form"
                :model="editform"
                :rules="rules"
                label-width="80px"
                label-position="left"
                class="editform"
              >
                <div class="maxW">
                  <el-form-item label="权限名称" prop="authorityName">
                    <el-input v-model="editform.authorityName" clearable></el-input>
                  </el-form-item>
                  <el-form-item label="所属园区" prop="parkId">
                    <parkSelect v-model="editform.parkId"></parkSelect>
                  </el-form-item>
                  <el-form-item label="权限类型" prop="type">
                    <el-select v-model="editform.type" placeholder="策略类型" :disabled="true">
                      <el-option label="人员" :value="1"></el-option>
                      <el-option label="车辆" :value="3"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="权限性质" prop="areaType">
                    <el-select v-model="editform.areaType" placeholder="权限性质" :disabled="areaTypeDisable">
                      <el-option label="公共区域" :value="0"></el-option>
                      <el-option label="保密区域" :value="1"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="备注" prop="remark">
                    <el-input
                      type="textarea"
                      :rows="3"
                      v-model="editform.remark"
                      placeholder="不超过60个字"
                    ></el-input>
                  </el-form-item>
                  <el-form-item label="设备类型" prop="deviceUseType">
                    <el-select v-model="editform.deviceUseType" :disabled="isAlone" placeholder="设备类型">
                      <el-option label="门禁" :value="1"></el-option>
                      <el-option label="考勤" :value="2"></el-option>
                    </el-select>
                  </el-form-item>
                </div>
                <!-- <el-form-item class="btns">
                  <el-button type="primary" @click="setCheckedNodes">全选</el-button>
                  <el-button type="primary" @click="resetChecked" plain>清空</el-button>
                </el-form-item>-->
                <el-form-item label="选择设备" prop="checkedlimits">
                  <div class="qt-limit">
                    <el-tree
                      :data="treeData"
                      ref="limitree"
                      node-key="id"
                      show-checkbox
                      default-expand-all
                      :highlight-current="true"
                      :check-strictly="true"
                      :default-checked-keys="editform.checkedlimits"
                      :props="defaultProps"
                    ></el-tree>
                  </div>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="onSubmit('form')" :loading="setLoading">保存</el-button>
                  <el-button type="primary" @click="saveCancel()" plain>取消</el-button>
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
import { getTree, getTreePersonNew, getObj, putObj } from "@/api/platform/area/limit";
import { mapGetters } from "vuex";

export default {
  name: "limit",
  data() {
    return {
      editform: {
        authorityName: "",
        type: "",
        remark: "",
        areaType: "",
        deviceUseType: "",
        checkedlimits: [] //修改时，选中的权限列表
      },
      defaultCheckedlimits: [3, 9], //默认展示选中的集合
      rules: {
        parkId: [
          { required: true, message: "请选择所属园区", trigger: "blur" }
        ],
        authorityName: [
          { required: true, message: "请输入策略名称", trigger: "blur" }
        ],
        type: [
          { required: true, message: "请选择策略类型", trigger: "change" }
        ],
        areaType: [
          { required: true, message: "请选择权限性质", trigger: "change" }
        ],
        deviceUseType: [
          { required: true, message: "请选择设备类型", trigger: "change" }
        ],
        remark: [{ max: 60, message: "不超过60个字", trigger: "blur" }],
        checkedlimits: [
          {
            type: "array",
            required: true,
            message: "请选择设备",
            trigger: "change"
          }
        ]
      },
      areaTypeDisable: true,
      setLoading: false,
      treeData: [],
      defaultProps: {
        children: "children",
        label: "label"
      },
      isAlone: false // true: 指定是门禁或者考勤，false: 总的
    };
  },
  created() {
    if(this.$route.query.deviceUseType){
      this.isAlone = true
    }
    getObj(this.$route.params.id).then(response => {
      this.editform = response.data.data;
      this.editform.type = response.data.data.type;
      if (this.editform.type === 3) {
        this.areaTypeDisable = true
      } else {
        this.areaTypeDisable = false
      }
      this.editform.areaType = response.data.data.areaType ? response.data.data.areaType : 0;
      this.editform.parkId = response.data.data.parkId;
      this.editform.deviceUseType = response.data.data.deviceUseType;
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  watch:{
    'editform.type':{
      handler(val){
        this.selectDevice(val, this.editform.parkId)
      },
      immediate: true
    },
    'editform.parkId':{
      handler(val){
        this.selectDevice(this.editform.type, val)
      },
      immediate: true
    },
    'editform.areaType':{
      handler(val){
        this.selectDevice(this.editform.type, this.editform.parkId)
      },
      immediate: true
    }
  },
  methods: {
    handleChange(value) {
    },
    resetChecked() {
      //清空
      this.$refs.limitree.setCheckedKeys([]);
    },
    setCheckedNodes() {
      //全选
      this.$refs.limitree.setCheckedNodes(this.treeData);
    },
    selectDevice(type, parkId) {
      if(!this.validatenull(type)&&!this.validatenull(parkId)){
        if(type===3){
          this.getTree(type, parkId)
        }else if(type===1){
          this.getTreePerson(parkId)
        }
      }else{
        this.treeData = []
      }
    },
    getTree(type, parkId){
      getTree(type, parkId).then(response => {
        this.treeData = response.data.data;
      });
    },
    getTreePerson(parkId){
      getTreePersonNew(parkId, this.editform.areaType).then(response => {
        this.treeData = response.data.data;
      });
    },
    onSubmit(formName) {
      this.editform.checkedlimits = this.$refs.limitree.getCheckedKeys();
      this.$refs[formName].validate(valid => {
        if (valid) {
          this.setLoading = true
          putObj(this.editform).then(response => {
            this.setLoading = false
            this.$router.go(-1);
          }).catch(err=>{
            this.setLoading = false
          });
        } else {
          return false;
        }
      });
    },
    saveCancel() {
      this.$router.go(-1);
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
