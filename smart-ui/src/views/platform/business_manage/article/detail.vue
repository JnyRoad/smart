<template>
  <div class="my-basic-container visitor-detail mycard">
    <el-scrollbar class="my-scrollbar" :native="false">
      <el-row type="flex" class="my-basic-inner" v-loading="loading" element-loading-text="正在查询，请稍等！">
        <el-col :lg="20" :md="20" class="box-outer">
          <div class="top-menu" style="margin-bottom: 20px">
            <el-button type="primary" icon="el-icon-back" plain @click="goBack">返回</el-button>
          </div>
          <div class="box-top-header">
            <span>申请时间：{{ visitorInfo.createTime }}</span>
            <span>确认状态：{{ visitorInfo.statusName }}</span>
          </div>
          <div v-if="visitorInfo.applyMain">
            <table class="my-table">
              <tr>
                <td class="label">申请人</td>
                <td>{{ visitorInfo.applyMain.sqr || '-' }}</td>
                <td class="label">放行去处</td>
                <td>{{ visitorInfo.applyMain.fxqcDesc || '-' }}</td>
                <td class="label">是否返厂</td>
                <td>{{ visitorInfo.applyMain.sffcDesc || '-' }}</td>
              </tr>
              <tr>
                <td class="label">申请部门</td>
                <td>{{ visitorInfo.applyMain.sqbm || '-' }}</td>
                <td class="label">出发地点</td>
                <td>{{ visitorInfo.applyMain.fxddDesc || '-' }}</td>
                <!-- <td class="label">是否拍照</td> -->
                <td colspan="3">{{ visitorInfo.applyMain.fxddxq || '-' }}</td>
              </tr>
              <tr>
                <td class="label">放行人级别</td>
                <td>{{ visitorInfo.applyMain.sqrjbDesc || '-' }}</td>
                <td class="label">到达地点</td>
                <td>{{ visitorInfo.applyMain.ddddDesc || '-' }}</td>
                <td colspan="3">{{ visitorInfo.applyMain.ddddxq || '-' }}</td>
              </tr>
              <tr>
                <td class="label">放行事项</td>
                <td>{{ visitorInfo.applyMain.fxsxDesc || '-' }}</td>
                <td class="label">物品放行类别</td>
                <td>{{ visitorInfo.applyMain.wpfxlbDesc || '-' }}</td>
                <td class="label">注解</td>
                <td>{{ visitorInfo.remark || '-' }}</td>
              </tr>
              <tr>
                <td class="label">附件</td>
                <td colspan="7">
                  <viewer v-if="visitorInfo.applyMain.fjsc"
                    ><img class="el-image" style="width: 50px; height: 50px; object-fit: contain" :src="visitorInfo.applyMain.fjsc"
                  /></viewer>
                </td>
              </tr>
            </table>
          </div>
          <div v-if="visitorInfo.personDetailList && visitorInfo.personDetailList.length > 0">
            <p class="box-orange">人员放行</p>
            <table class="my-table2">
              <tr class="header">
                <td>序号</td>
                <td>姓名</td>
                <td>工号</td>
                <td>离场事由</td>
                <td>离场日期</td>
                <td>离场时间</td>
                <td>返厂日期</td>
                <td>返厂时间</td>
              </tr>
              <tr v-for="(item, index) in visitorInfo.personDetailList" :key="index">
                <td>{{ index + 1 }}</td>
                <td>{{ item.xm || '-' }}</td>
                <td>{{ item.gh || '-' }}</td>
                <td>{{ item.lcsy || '-' }}</td>
                <td>{{ item.lcrq || '-' }}</td>
                <td>{{ item.lcsj || '-' }}</td>
                <td>{{ item.fcrq || '-' }}</td>
                <td>{{ item.fcsj || '-' }}</td>
              </tr>
            </table>
          </div>
          <div v-if="visitorInfo.thingDetailList && visitorInfo.thingDetailList.length > 0">
            <p class="box-orange">物品放行</p>
            <table class="my-table2">
              <tr class="header">
                <td>序号</td>
                <td>资产编号</td>
                <td>名称</td>
                <td>单位</td>
                <td>数量</td>
                <td>接收单位</td>
                <td>放行日期</td>
                <td>备注(原因)</td>
                <td>返厂日期</td>
                <td>返厂时间</td>
                <td>运输方式</td>
                <td>姓名</td>
                <td>车牌号</td>
              </tr>
              <template>
                <tr v-for="(item, index) in visitorInfo.thingDetailList" :key="index">
                  <td>{{ index + 1 }}</td>
                  <td>{{ item.wpbm || '-' }}</td>
                  <td>{{ item.wpmc || '-' }}</td>
                  <td>{{ item.wpdw || '-' }}</td>
                  <td>{{ item.wpsl || '-' }}</td>
                  <td>{{ item.jsdw || '-' }}</td>
                  <td>{{ item.fxrq || '-' }}</td>
                  <td>{{ item.bz || '-' }}</td>
                  <td>{{ item.wpfcrq || '-' }}</td>
                  <td>{{ item.wpfcsj || '-' }}</td>
                  <td>{{ item.ysfsDesc || '-' }}</td>
                  <td>{{ item.xm || '-' }}</td>
                  <td>{{ item.cph || '-' }}</td>
                </tr>
              </template>
            </table>
          </div>
          <p class="box-orange">放行信息</p>
          <table class="lit-table" style="margin: 0; width: 50%">
            <tr>
              <td>状态</td>
              <td>{{ visitorInfo.statusName || '-' }}</td>
            </tr>
            <tr v-if="visitorInfo.status === 5">
              <td>拒绝原因</td>
              <td>{{ visitorInfo.remark }}</td>
            </tr>
            <tr>
              <td>放行人员</td>
              <td>{{ visitorInfo.securityStaff || '-' }}</td>
            </tr>
            <tr>
              <td>离厂时间</td>
              <td>{{ visitorInfo.departureTime || '-' }}</td>
            </tr>
          </table>
        </el-col>
        <el-col :lg="4" :md="4" class="box-outer">
          <p class="box-orange">审批记录</p>
          <div class="record">
            <div class="record-inner">
              <template v-for="(item, index) in visitorInfo.approvalProcess">
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
                  <div class="record-item__right" v-if="item.recordNode === 1">
                    <div>处理人</div>
                    <div v-for="(item2, index2) in item.staffInfos" :key="index2" style="margin-top: 10px">
                      <div style="font-size: 14px">
                        {{ item2.staffName }}-
                        <!-- result：0 待审批 1 通过 2 拒绝 3关闭 4 等待 -->
                        <span v-if="item2.result === 0" class="pc_c0">{{ item2.resultDesc }}</span>
                        <span v-if="item2.result === 1" class="pc_c1">{{ item2.resultDesc }}</span>
                        <span v-if="item2.result === 2 || item2.result === 3" class="pc_c2">{{ item2.resultDesc }}</span>
                        <span v-if="item2.result === 4" class="pc_c4">{{ item2.resultDesc }}</span>
                      </div>
                      <div class="line2">
                        {{ item2.recordDate || item2.createDate }}
                      </div>
                    </div>
                  </div>
                  <div class="record-item__right" v-if="item.recordNode === 2">
                    <div>{{item.staffInfos[0].resultDesc}}</div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-scrollbar>
  </div>
</template>

<script>
import { getDetail } from './_service'
export default {
  data() {
    return {
      id: null,
      loading: false,
      btnLoading: false,
      visitorInfo: {}
    }
  },
  created() {},
  mounted: function () {
    this.loading = true
    getDetail(this.$route.params.id).then((response) => {
      this.visitorInfo = response.data.data
      if (this.visitorInfo.applyMain.fjsc) {
        this.visitorInfo.applyMain.fjsc = `platform/image/view/${this.visitorInfo.applyMain.fjsc}`
      }
      const thingDetailList = this.visitorInfo.thingDetailList
      if (thingDetailList !== null && thingDetailList.length > 0) {
        this.visitorInfo.thingDetailList.forEach((element) => {
          switch (element.ysfs) {
            case '0':
              element.ysfsDesc = '人工'
              break
            case '1':
              element.ysfsDesc = '货车'
              break
            case '2':
              element.ysfsDesc = '叉车'
              break
            case '3':
              element.ysfsDesc = '三轮'
              break
          }
        })
      }
      this.loading = false
    })
  },
  methods: {
    goBack() {
      this.$router.go(-1)
    }
  }
}
</script>

<style lang="scss">
@use "@/styles/platform/visitor/visitor_detail" as *;
</style>
<style lang="scss" scoped>
.box-top-header {
  margin-bottom: 20px;
  span {
    margin-right: 80px;
  }
}
.my-table {
  width: 100%;
  margin-bottom: 30px;
  td {
    padding: 10px 15px;
    border: 1px solid #e0e0e0;
  }
  .label {
    background: #e7f3fc;
  }
}
.my-table2 {
  width: 100%;
  margin-bottom: 30px;
  text-align: center;
  td {
    padding: 10px 15px;
    border: 1px solid #e0e0e0;
  }
  .header {
    background: #e7f3fc;
    text-align: center;
  }
}
.btns {
  text-align: center;
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
