<!--设备管理，闸机管理，更新设备权限  -->
<template>
  <div class="my-basic-container limit">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="limit-inner">
          <p class="box-orange">设备权限管理</p>
          <el-form
            ref="form"
            :model="editform"
            label-width="80px"
            label-position="top"
            class="editform"
          >
            <el-row class>
              <el-col :span="5">
                <div class="block">
                  <p>人员列表</p>
                  <el-form-item label prop="general">
                    <el-input v-model="editform.general" placeholder="请输入姓名" clearable>
                      <el-button
                        slot="append"
                        icon="el-icon-search"
                        type="info"
                        @click="selectPerson"
                      ></el-button>
                    </el-input>
                  </el-form-item>
                  <el-form-item label prop="cardNo" class="list" v-if="hasData">
                    <el-radio-group v-model="cardNo" @change="selectLimit">
                      <el-radio
                        v-for="(item, index) in personList"
                        :label="item.cardNo"
                        :key="index"
                      >
                        <div class="img-block">
                          <div class="img-outer">
                            <div class="img-inner">
                              <viewer>
                                <img
                                  :src="item.faceImage || errorImgPeaple()"
                                  :onerror="errorImgPeaple()"
                                />
                              </viewer>
                            </div>
                          </div>
                        </div>
                        {{item.name}}
                      </el-radio>
                    </el-radio-group>
                  </el-form-item>
                </div>
                <div class="btns">
                  <el-button type="primary" @click="delLimit('form')" :loading="loading">删除</el-button>
                  <el-button type="primary" @click="cancel" plain>取消</el-button>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="block block2">
                  <p>人员拥有的设备权限组</p>
                  <el-form-item label prop="checkedlimits">
                    <div class="qt-limit">
                      <template v-if="treeVisible&&hasData">
                        <div class="tip">以下所选中的设备为可删除的设备，请根据自己的需求，来选择具体要删除的设备！</div>
                        <el-tree
                          :data="treeData"
                          ref="limitree"
                          node-key="id"
                          show-checkbox
                          default-expand-all
                          :highlight-current="false"
                          :check-strictly="false"
                          :default-checked-keys="editform.checkedlimits"
                          :props="defaultProps"
                        ></el-tree>
                      </template>
                      <div v-else class="tip2">请先搜索相关人员信息,点击查询结果来查看相关设备</div>
                    </div>
                  </el-form-item>
                </div>
              </el-col>
              <el-col :span="8"></el-col>
            </el-row>
          </el-form>
        </div>
      </section>
    </el-scrollbar>
  </div>
</template>

<style lang="scss">
@use "@/styles/platform/device/limit" as *;
</style>

<script>
import {
  getTree,
  getPersonList,
  getLimitByCode,
  getObj,
  delObj
} from "@/api/platform/device/limit";
import { mapGetters } from "vuex";

export default {
  name: "limit",
  data() {
    return {
      loading: false,
      cardNo: "",
      personList: [],
      treeDeviceType: 1, //2 是人员， 3是车辆
      limitDeviceType: 1, //1 是人员， 2是车辆
      hasData: false, //是否有查到相关人员信息
      treeVisible: false, //树形的显示和隐藏
      canSelNodes: [], //可被选择的节点
      editform: {
        general: "",
        remark: "",
        checkedlimits: [] //修改时，选中的权限列表
      },
      treeData: [],
      defaultProps: {
        children: "children",
        label: "label",
        disabled: this.disabledFn
      },
      num: 0,
      deviceType: undefined
    };
  },
  created() {
    this.deviceType = this.$route.query.deviceType;
    getTree(this.treeDeviceType).then(response => {
      this.treeData = response.data.data;
    });
  },
  mounted: function() {},
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    disabledFn(data, node) {
      if (this.canSelNodes.indexOf(data.id) > -1) {
        return false;
      } else {
        return true;
      }
    },
    //根据人员姓名查找人员信息
    selectPerson() {
      this.cardNo = "";
      this.treeVisible = false;
      this.personList = [];
      // this.editform.checkedlimits=[];
      this.editform.general = this.editform.general.replace(/(\s+)/g, "");
      if (this.validatenull(this.editform.general)) {
        this.$message({
          message: "请输入人员姓名！",
          type: "warning"
        });
        return;
      }
      getPersonList({ general: this.editform.general }).then(response => {
        const personList = response.data.data;
        if (personList.length > 0) {
          this.hasData = true;
          this.personList = personList;
        } else {
          this.hasData = false;
          this.$message({
            message: "没有查到该人员相关信息，请核对名称后重新搜索！",
            type: "warning"
          });
        }
      });
    },
    //根据查询到的人员姓名 查看相关权限
    selectLimit() {
      getLimitByCode({ deviceType: this.limitDeviceType, cardNo: this.cardNo })
        .then(response => {
          const list = response.data.data;
          if (list.length > 0) {
            this.treeVisible = true;
            this.canSelNodes = list;
            this.editform.checkedlimits = list;
          } else {
            this.$message({
              message: "此人当前没有绑定任何设备权限！",
              type: "warning"
            });
            this.treeVisible = false;
          }
        })
        .catch(err => { console.error(err) });
    },
    //删除选中权限
    delLimit(formName) {
      if (this.treeVisible && this.hasData) {
        this.editform.checkedlimits = this.$refs.limitree.getCheckedKeys();
        if (this.editform.checkedlimits.length > 0) {
          this.loading = true;
          delObj({
            cardNo: this.cardNo,
            deviceCode: this.$refs.limitree.getCheckedKeys()
          })
            .then(response => {
              const code = response.data.code;
              if (code === 0) {
                this.$notify({
                  title: "删除成功",
                  message: "删除成功",
                  type: "success",
                  duration: 2000
                });
                let src = `/platform/device/gate`;
                if(this.deviceType===1){
                  src = `/platform/device/gate`
                }else if(this.deviceType===3){
                  src = `/platform/device/attendance`
                }else if(this.deviceType===4){
                  src = `/platform/device/entrance_guard`
                }
                this.$router.push({
                  path: src
                });
              } else {
                this.$notify({
                  title: "删除失败",
                  message: "删除失败",
                  type: "error",
                  duration: 2000
                });
              }
              this.loading = false;
            })
            .catch(err => {
              this.loading = false;
            });
        } else {
          this.$message({
            message: "请先选择要删除的权限设备！",
            type: "warning"
          });
        }
      } else {
        this.$message({
          message: "请先输入人员姓名查找相关信息并选中需要删除的设备！",
          type: "warning"
        });
      }
    },
    cancel() {
      let src = `/platform/device/gate`;
      if(this.deviceType===1){
        src = `/platform/device/gate`
      }else if(this.deviceType===3){
        src = `/platform/device/attendance`
      }else if(this.deviceType===4){
        src = `/platform/device/entrance_guard`
      }
      this.$router.push({
        path: src
      });
    }
  }
};
</script>

<style lang="scss" scoped>
  .limit{
    min-width: 1000px;
  }
</style>
