<!--通关权限  -->
<template>
  <div class="my-basic-container limit">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <div class="limit-inner">
          <el-row class>
            <el-col :span="12">
              <el-form
                ref="form"
                :model="editform"
                label-width="80px"
                label-position="left"
                class="editform"
              >
                <div class="maxW">
                  <el-form-item label="策略名称" prop="authorityName">
                    <el-select v-model="authIndex" placeholder="请选择">
                      <el-option
                        v-for="(item, index) in authObjArr"
                        :key="index"
                        :label="item.authName"
                        :value="index">
                      </el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="策略类型" prop="type">
                    <el-input v-model="editform.type" readonly></el-input>
                  </el-form-item>
                  <el-form-item label="备注" prop="remark">
                    <el-input type="textarea" :rows="3" v-model="editform.remark" placeholder="-" readonly></el-input>
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
                      default-expand-all
                      :highlight-current="true"
                      :check-strictly="true"
                      :default-checked-keys="editform.checkedlimits"
                      :props="defaultProps"
                    ></el-tree>
                  </div>
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
import { getAuth } from "@/api/platform/basic/staff_info";
import { mapGetters } from "vuex";

export default {
  name: "limit",
  data() {
    return {
      authIndex: '',
      authObjArr: [],
      editform: {
        authorityName: "",
        type: "",
        remark: "",
        checkedlimits: [] //修改时，选中的权限列表
      },
      defaultCheckedlimits: [3, 9], //默认展示选中的集合
      rules: {
        authorityName: [
          { required: true, message: "请输入策略名称", trigger: "blur" }
        ],
        type: [
          { required: true, message: "请选择策略类型", trigger: "change" }
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
      treeData: [],
      defaultProps: {
        children: "children",
        label: "label"
      }
    };
  },
  created() {
    getAuth(this.$route.params.id).then(response => {
      this.authObjArr = response.data.data
      if(this.authObjArr.length>0){
        this.authIndex = 0
      }
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  watch: {
    authIndex(val){
      if(this.authObjArr.length>0){
        this.getAuthInfo(val)
      }
    }
  },
  methods: {
    getAuthInfo(index){
      let auth = this.authObjArr[index]
      this.editform.type = auth.typeName;
      this.editform.remark = auth.remark;
      this.treeData = auth.children;
    },
    goBack() {
      this.$router.push({
        path: `/platform/basic/staff_info`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    },
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
    saveCancel() {
      this.$router.go(-1);
    }
  }
};
</script>

<style lang="scss" scoped>
</style>
