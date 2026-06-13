<!--业务监控：外宿补贴列表，详情 -->
<template>
  <div class="my-basic-container">
    <el-scrollbar class="my-scrollbar" :native="false">
      <section class="my-basic-inner">
        <div class="top-menu" style="margin-bottom:20px;">
          <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
        </div>
        <el-row>
          <el-col :span="16">
            <p class="box-orange">退宿信息</p>
            <table class="dot-list dotlist2" style="float: none">
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="园区"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.parkName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="宿舍信息"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.dormitoryName}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="退宿原因"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.quitReasonDesc}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="预计离开时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.applyLeaveTime}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="备注"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.remark}}</td>
              </tr>

              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="申请人工号"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.badge}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="申请人姓名"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.name}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="申请时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.createTime}}</td>
              </tr>
              <tr>
                  <td>
                    <div class="my-dot-label">
                    <tce-label-justify label="附件"></tce-label-justify>
                  </div>
                  </td>
                  <td>
                    <template v-for="(item, index) in imgs">
                      <dl :key="index">
                        <viewer
                          ><img class="el-image" style="width: 100px; height: 100px; object-fit: contain;float:left;margin-right:10px" :src="item"
                        /></viewer>
                      </dl>
                    </template>
                  </td>
                </tr>
            </table>
            <p class="box-orange" v-if="staffResult.status > 3" style="margin-top:20px">放行信息</p>
            <table class="dot-list dotlist2" style="float: none" v-if="staffResult.status > 3">
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="放行人"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.securityStaff}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="放行时间"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.leaveTime}}</td>
              </tr>
              <tr>
                <td>
                  <div class="my-dot-label">
                    <tce-label-justify label="状态"></tce-label-justify>
                  </div>
                </td>
                <td>{{staffResult.statusDesc}}</td>
              </tr>
            </table>
          </el-col>
          <el-col :span="8" class="box-outer">
            <p class="box-orange">审批信息</p>
            <div class="record">
                <div class="record-inner">
                <template v-for="(item, index) in staffResult.processRecord">
                    <div class="record-item" :key="index">
                    <div class="record-item__left">
                        <div class="num"></div>
                        <i class="line1"></i>
                    </div>
                    <!-- 提交节点 -->
                    <div class="record-item__right" v-if="item.recordNode === 0">
                        <div>
                        <span style="font-weight: bold">{{ item.staffInfos[0].staffName }}-</span>
                        <span class="pc_c0">{{ item.staffInfos[0].resultDesc }}</span>
                        </div>
                        <div class="line2">
                        {{ item.staffInfos[0].createDate }}
                        </div>
                    </div>
                    <!-- 审批节点 -->
                    <div class="record-item__right" v-if="item.recordNode === 1 || item.recordNode === 2">
                        <div>{{item.statusName}}</div>
                        <div v-for="(item2, index2) in item.staffInfos" :key="index2" style="margin-top: 10px">
                          <div style="font-size: 14px">
                              {{ item2.staffName }}-
                              <!-- result：0 待审批 1 通过 2 拒绝 3关闭 4 等待 -->
                              <span v-if="item2.result === 0" class="pc_c0">{{ item2.resultDesc }}</span>
                              <span v-if="item2.result === 1" class="pc_c1">{{ item2.resultDesc }}</span>
                              <span v-if="item2.result === 2 || item2.result === 3" class="pc_c2">{{ item2.resultDesc }}</span>
                              <span v-if="item2.result === 4" class="pc_c4">{{ item2.resultDesc }}</span>
                          </div>
                          <div style="font-size: 14px" v-if="item2.remark!== null">
                            备注：{{item2.remark}}
                          </div>
                          <div class="line2">
                              {{ item2.recordDate || item2.createDate }}
                          </div>
                        </div>
                    </div>
                    <!-- <div class="record-item__right" v-if="item.recordNode === 2">
                        <div>{{item.staffInfos[0].resultDesc}}</div>
                    </div> -->
                    </div>
                </template>
                </div>
            </div>
          </el-col>
        </el-row>
      </section>
    </el-scrollbar>
  </div>
</template>
<script>
import { getById } from "@/api/platform/work/checkedOut";
import { mapGetters } from "vuex";

export default {
  data() {
    return {
      staffResult: {},
      imgs: []
    };
  },
  created() {
    var id = this.$route.params.id;
    // var params = { id };

    getById(id).then(response => {
      if (!this.validatenull(response.data.data)) {
        this.staffResult = response.data.data;
        const p = this.staffResult.dorDetailStr[0].split('-')
        this.staffResult['parkName'] = p[0]
        this.staffResult['dormitoryName'] = p[1] + p[2]
        if(this.staffResult.imgs !== null && this.staffResult.imgs.length > 0){
          this.imgs = []
          this.staffResult.imgs.forEach(element => {
            this.imgs.push(`platform/image/view/${element}`)
          });
        }
      }
    });
  },
  computed: {
    ...mapGetters(["permissions"])
  },
  methods: {
    goBack() {
      this.$router.push({
        path: `/platform/work/checkedOut`,
        query: {
          queryPage: this.$route.query.queryPage,
          queryForm: this.$route.query.queryForm
        }
      });
    }
  }
};
</script>
<style lang="scss" scoped>
.dotlist2{
  .my-dot-label{
    width: 150px;
  }
}
.img-info {
  width: 60%;
  margin: 0 auto;
}
.dot-list {
  float: left;
  width: 80%;
  margin-right: 50px;
}
.record {
  position: relative;
  padding-left: 20px;
  .pc_c0 {
    color: #508bff;
  }
  .pc_c1 {
    color: #74c288;
  }
  .pc_c2 {
    color: #f25c19;
  }
  .pc_c4 {
    color: #999;
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
  .record-inner {
    .record-item:last-child {
      .record-item__left {
        .line1 {
          display: none;
        }
      }
    }
  }
}
</style>
