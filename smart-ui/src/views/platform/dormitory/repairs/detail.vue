<!--宿舍报修详情 -->
<template>
  <div class="my-basic-container vehicle">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-form
          ref="editform"
          :rules="replyRules"
          :model="detailInfo"
          size="mini"
          label-width="80px"
        >
          <p class="box-orange">报修信息</p>
          <el-row>
            <el-col :span="14">
              <table class="lit-table" border="false" v-if="!isEdit">
                <tr>
                  <td>状态</td>
                  <td>{{detailInfo.statusDesc}}</td>
                </tr>
                <tr>
                  <td>姓名</td>
                  <td>{{detailInfo.name}}</td>
                </tr>
                <tr>
                  <td>工号</td>
                  <td>{{detailInfo.staffBadge}}</td>
                </tr>
                <tr>
                  <td>BU</td>
                  <td>{{detailInfo.compName}}</td>
                </tr>
                <tr>
                  <td>部门</td>
                  <td>{{detailInfo.depName}}</td>
                </tr>
                <tr>
                  <td>维修区域</td>
                  <td>{{detailInfo.rangeTypeDesc}}</td>
                </tr>
                <tr>
                  <td>维修类别</td>
                  <td>{{detailInfo.repairTypeDesc}}</td>
                </tr>
                 <tr>
                  <td>维修位置</td>
                  <td>{{detailInfo.dormitoryName}}#{{detailInfo.roomName}}</td>
                </tr>
                 <tr>
                  <td>园区</td>
                  <td>{{detailInfo.parkName}}</td>
                </tr>
                <tr>
                  <td>故障描述</td>
                  <td>{{detailInfo.faultDesc}}</td>
                </tr>
                <tr>
                  <td>故障照片</td>
                  <td>
                    <template v-for="(item, index) in detailInfo.imgs">
                      <dl class="img-info" :key="index">
                        <dt class="img-outer">
                          <div class="img-inner">
                            <viewer>
                              <img :src="item || errorImgPeaple()" :onerror="errorImgPeaple()" />
                            </viewer>
                          </div>
                        </dt>
                        <dd class="desc-info"></dd>
                      </dl>
                    </template>
                  </td>
                </tr>
              </table>
            </el-col>
          </el-row>
          <el-row>
            <div class>
              <p class="box-orange">审批信息</p>
              <div class="record">
                <div class="record-inner">
                  <template v-for="(item, index) in detailInfo.approvalProcess">
                    <div class="record-item" :key="index">
                      <div class="record-item__left">
                        <div class="num"></div>
                        <i class="line1"></i>
                      </div>
                      <!-- 提交节点 -->
                      <div class="record-item__right" v-if="item.recordNode===0">
                        <div>
                          <span style="font-weight: bold;">{{ item.staffInfos[0].staffName }}-</span>
                          <span class="pc_c0">{{ item.staffInfos[0].resultDesc }}</span>
                        </div>
                        <div class="line2">
                          {{ item.staffInfos[0].createDate }}
                        </div>
                      </div>
                      <!-- 审批节点 -->
                      <div class="record-item__right" v-else>
                        <div>
                          处理人
                        </div>
                        <div v-for="(item2, index2) in item.staffInfos" :key="index2" style="margin-top: 10px">
                          <div style="font-size: 14px;">
                            {{item2.staffName}}-
                            <!-- result：0 待审批 1 通过 2 拒绝 3关闭 4 等待 -->
                            <span v-if="item2.result===0" class="pc_c0">{{item2.resultDesc}}</span>
                            <span v-if="item2.result===1" class="pc_c1">{{item2.resultDesc}}</span>
                            <span v-if="item2.result===2||item2.result===3" class="pc_c2">{{item2.resultDesc}}</span>
                            <span v-if="item2.result===4" class="pc_c4">{{item2.resultDesc}}</span>
                          </div>
                          <div v-if="item2.remark" style="padding: 10px 0;">
                            <span style="color: #999;">{{item2.remark}}</span>
                          </div>
                          <div class="line2">
                            {{ item2.recordDate || item2.createDate}}
                          </div>
                        </div>
                      </div>
                    </div>
                  </template>
                </div>
              </div>
            </div>
          </el-row>
          <div class="gray-line"></div>
          <p class="box-orange">回复记录</p>
          <el-row>
            <el-col :span="19" :key="item.replyName" v-for="item in detailInfo.repairReplyList">
              <table class="lit-table">
                <tr>
                  <td>{{item.replyName}}</td>
                  <td>{{item.replyTime}}</td>
                  <td>{{item.replyStatusDesc}}</td>
                </tr>
                <tr v-if="item.replyDesc!== null">
                  <td colspan="3">{{item.replyDesc}}</td>
                </tr>
              </table>
            </el-col>
          </el-row>
        </el-form>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import {
  checkDetail
} from "@/api/platform/dormitory/dormitory_repairs";
import { allPark } from "@/api/platform/_publicService";
import { mapGetters } from "vuex";
import { isArrayFn } from "@/util/util";
export default {
  data() {
    var driverLicenseIdValidator = (rule, value, callback) => {
      var driverLicenseIdImg = this.deviceInfo.driverLicenseId;
      if (
        driverLicenseIdImg == null ||
        driverLicenseIdImg == "" ||
        driverLicenseIdImg == undefined
      ) {
        callback(new Error("请选择驾驶证照片！"));
      } else {
        callback();
      }
    };
    return {
      isEdit: false, //是否是编辑状态
      detailInfo: {},
      stfBadge: undefined,  //查询用员工工号
      staffListInfo: {},   //员工列表信息
      changeFormVisible: false,   //转交Ta人弹窗
      parkData: [],
      replyRules: {
        replyDesc: [{ required: true, message: "请输入回复内容", trigger: "change" }]
      },
      changeForm:{
        id: undefined,  //记录标识
        changeBadge: undefined //转交员工工号
      }
    };
  },
  created() {
    checkDetail(this.$route.params.id).then(response => {
      let result = response.data.data;
      this.detailInfo = result;
    })
  },
  computed: {
  },
  methods: {
    async getParks() {
      const res = await allPark();
      this.parkData = res.data.data;
    },
    goBack() {
      this.$router.push({
        path: `/platform/dormitory/repairs/repairs`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    },
    editInfo() {
      this.isEdit = true;
      this.doClearValidate("editform");
    },
    resetEditform(formName) {
      this.$refs[formName].clearValidate();
    },
    resetchangeForm(formName) {
      this.stfBadge = '';
      this.staffListInfo = {};
       this.$refs[formName].resetFields();
    },
    saveCancel(formName) {
      this.isEdit = false;
      this.resetEditform(formName);
    },
  }
};
</script>
<style lang="scss" scoped>
  .record {
    position: relative;
    padding-left: 20px;
    .pc_c0{
      color: #508BFF;
    }
    .pc_c1{
      color: #74C288;
    }
    .pc_c2{
      color: #F25C19;
    }
    .pc_c4{
      color: #999
    }
    &-item {
      display: flex;
      &__left {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding-right: 8px;
        .num {
          width: 18px;
          height: 18px;
          text-align: center;
          line-height: 18px;
          background: url('/img/p_1.png');
          background-size: 100% 100%;
        }
        .line1 {
          flex: 1;
          width: 1px;
          border-left: 1px dashed #e0e0e0;
        }
      }
      &__right {
        .line2 {
          font-size: 12px;
          color: #666;
          margin: 5px 0 15px 0;
        }
      }
    }
    .record-inner{
      .record-item:last-child{
        .record-item__left {
          .line1 {
            display: none;
          }
        }
      }
    }
  }
</style>
<style lang="scss" scoped>
.noPading {
  padding: 12px 20px;
}
.img-info {
  width: 100px;
  margin: 0 20px 0 0;
  float: left;
  .img-inner {
    top: 5px;
    right: 5px;
    bottom: 5px;
    left: 5px;
  }
}
.lit-table {
  width: 550px;
  float: left;
}
.form-outer {
  max-width: 400px;
  margin-left: 30px;
}
.desc-info {
  color: #666;
  font-size: 12px;
}
.avatar-uploader-icon {
  width: 100%;
  height: 100%;
  font-size: 12px !important;
  font-style: normal;
  text-align: center;
  color: #999;
  // padding-top: 50%;
  display: inline-block;
  background-position: center;
  background-size: 100% 100%;
  background-repeat: no-repeat;
}
</style>
