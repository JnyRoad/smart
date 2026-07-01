<!--区域管理，权限策略，添加  -->
<template>
  <div class="my-basic-container limit">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="limit-inner">
          <el-row class>
            <el-col :span="12">
              <el-form
                ref="form"
                :model="addform"
                :rules="rules"
                label-width="80px"
                label-position="left"
                class="addform"
              >
                <div class="maxW">
                  <el-form-item label="权限名称" prop="authorityName" clearable>
                    <el-input v-model="addform.authorityName"></el-input>
                  </el-form-item>
                  <el-form-item label="所属园区" prop="parkId">
                    <parkSelect v-model="addform.parkId"></parkSelect>
                  </el-form-item>
                  <el-form-item label="权限类型" prop="type">
                    <el-select v-model="addform.type" placeholder="策略类型">
                      <el-option label="人员" :value="1"></el-option>
                      <el-option label="车辆" :value="3"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="权限性质" prop="areaType">
                    <el-select v-model="addform.areaType" placeholder="权限性质" :disabled="areaTypeDisable">
                      <el-option label="公共区域" :value="0"></el-option>
                      <el-option label="保密区域" :value="1"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="备注" prop="remark">
                    <el-input
                      type="textarea"
                      :rows="3"
                      v-model="addform.remark"
                      placeholder="不超过60个字"
                    ></el-input>
                  </el-form-item>
                  <el-form-item label="设备类型" prop="deviceUseType">
                    <el-select v-model="addform.deviceUseType" :disabled="isAlone" placeholder="设备类型">
                      <el-option label="门禁" :value="1"></el-option>
                      <el-option label="考勤" :value="2"></el-option>
                    </el-select>
                  </el-form-item>
                </div>
                <el-form-item label="选择设备" prop="checkedlimits">
                  <DeviceTreePicker :tree-data="treeData" v-model="addform.checkedlimits" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="onSubmit('form')">保存</el-button>
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
import { getTree, getTreePersonNew, addObj } from "@/api/platform/area/limit";
import { mapGetters } from "vuex";
import DeviceTreePicker from "./components/DeviceTreePicker.vue";

export default {
  name: "limit",
  components: { DeviceTreePicker },
  data() {
    return {
      addform: {
        authorityName: "",
        type: "",
        remark: "",
        parkId: "",
        areaType: "",
        deviceUseType: "",
        checkedlimits: []
      },
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
      areaTypeDisable: false,
      treeData: [],
      isAlone: false // true: 指定是门禁或者考勤，false: 总的
    };
  },
  created() {
    if(this.$route.query.deviceUseType){
      this.isAlone = true
      this.addform.deviceUseType = this.$route.query.deviceUseType
    }
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  watch:{
    'addform.type':{
      handler(val){
        this.addform.areaType = 0
        if (val === 3) {
          this.areaTypeDisable = true
        } else {
          this.areaTypeDisable = false
        }
        this.selectDevice(val, this.addform.parkId)
      },
      immediate: true
    },
    'addform.parkId':{
      handler(val){
        this.selectDevice(this.addform.type, val)
      },
      immediate: true
    },
    'addform.areaType':{
      handler(val){
        this.selectDevice(this.addform.type, this.addform.parkId)
      },
      immediate: true
    }
  },
  methods: {
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
      getTreePersonNew(parkId, this.addform.areaType).then(response => {
        this.treeData = response.data.data;
      });
    },
    onSubmit(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          addObj(this.addform).then(response => {
            this.$router.go(-1);
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
