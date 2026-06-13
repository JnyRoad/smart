<!--物品放行条-->
<template>
  <div class="page3">
    <div class="page3_cont">
      <div class="page3_block code_block">
        <!-- 已过期 -->
        <template v-if="detailData.expire">
          <div class="code_img">
            <tce-image :src="outFac" :width="0" :height="0"></tce-image>
          </div>
          <div class="code_tip">放行码已过期</div>
        </template>
        <!-- 未过期 -->
        <template v-else>
          <template v-if="detailData.status === 1">
            <div class="code_img">
              <tce-image :src="code_h2" :width="0" :height="0"></tce-image>
            </div>
            <div class="code_tip">放行码仍在审批中，请稍后</div>
          </template>
          <template v-if="detailData.status === 2">
            <div class="code_img">
              <tce-image :src="'data:image/jpg;base64,'+detailData.qrCodePic" :width="0" :height="0"></tce-image>
            </div>
            <div class="code_tip">扫描放行码以识别备案物进行放行</div>
          </template>
          <template v-if="detailData.status === 3">
            <div class="code_img">
              <tce-image :src="code_h1" :width="0" :height="0"></tce-image>
            </div>
            <div class="code_tip">审核失败</div>
          </template>
          <template v-if="detailData.status === 4">
            <div class="code_img">
              <tce-image :src="outFac" :width="0" :height="0"></tce-image>
            </div>
            <div class="code_tip">已出厂</div>
          </template>
          <template v-if="detailData.status === 5">
            <div class="code_img">
              <tce-image :src="code_h1" :width="0" :height="0"></tce-image>
            </div>
            <div class="code_tip">拒绝放行</div>
          </template>
        </template>
      </div>
      <div class="page3_block">
        <div class="page3_item">
          <div class="item_label">{{detailData.carrier}}</div>
          <div class="item_value" v-if="detailData.facePic">
            <div class="img_h">
              <tce-image :src="detailData.facePic" :width="0" :height="0"></tce-image>
            </div>
          </div>
        </div>
      </div>
      <div class="page3_block">
        <div class="page3_item page3_item3">
          <div class="t1">{{detailData.parkName}}</div>
          <div class="t2"><span>{{detailData.articlesTypeName}}</span></div>
          <div class="t3" v-if="detailData.dormitoryName">{{detailData.dormitoryName}}-{{detailData.floorName}}层-{{detailData.roomName}}号房-{{detailData.bedName}}床</div>
        </div>
        <div class="page3_item page3_item2">
          <div class="item_label" style="width: 112px;">预计离厂日期：</div>
          <div class="item_value">
            {{detailData.plannedDepartureTime}}
          </div>
        </div>
        <div class="page3_item page3_item2">
          <div class="item_label">物品描述：</div>
          <div class="item_value">
            {{detailData.remarks}}
          </div>
        </div>
        <div class="page3_item page3_item2">
          <div class="item_label">物品照片：</div>
          <div class="item_value">
            <div class="img_h" v-if="detailData.oneImg">
              <tce-image :src="detailData.oneImg" :width="0" :height="0"></tce-image>
            </div>
            <div class="img_h" v-if="detailData.twoImg">
              <tce-image :src="detailData.twoImg" :width="0" :height="0"></tce-image>
            </div>
            <div class="img_h" v-if="detailData.threeImg">
              <tce-image :src="detailData.threeImg" :width="0" :height="0"></tce-image>
            </div>
          </div>
        </div>
      </div>
      <!-- v-if="detailData.status == 5" -->
      <div class="page3_block" >
        <div class="page3_item page3_item2">
          <div class="item_label">状态：</div>
          <div class="item_value">
            {{detailData.statusName || '-'}}
          </div>
        </div>
        <!-- 扫码拒绝后 -->
        <div class="page3_item page3_item2" v-if="detailData.status == 5">
          <div class="item_label">拒绝原因：</div>
          <div class="item_value">
            {{detailData.remark}}
          </div>
        </div>
        <!-- 扫码同意或拒绝后 -->
        <div class="page3_item page3_item2" v-if="detailData.status == 4 || detailData.status == 5">
          <div class="item_label">放行人员：</div>
          <div class="item_value">
            {{detailData.securityStaff || '-'}}
          </div>
        </div>
        <!-- 扫码同意离厂后 -->
        <div class="page3_item page3_item2" v-if="detailData.status == 4">
          <div class="item_label">离厂时间：</div>
          <div class="item_value">
            {{detailData.departureTime || '-'}}
          </div>
        </div>
      </div>
      <!-- <div class="page3_block" v-if="flows && flows.length>0">
        <div class="record">
          <div class="record-inner">
            <template v-for="(item, index) in flows">
              <div class="record-item" :key="index">
                <div class="record-item__left">
                  <div class="num"></div>
                  <i class="line1"></i>
                </div>
                <div class="record-item__right">
                  <div>
                    {{ item.processDesc }}
                    <span style="font-weight: bold; padding-left: 10px">{{ item.createUser }}</span>
                  </div>
                  <div class="line2">
                    {{ item.processDate }}
                  </div>
                </div>
              </div>
            </template>
          </div>
          <a class="tel" :href="'tel:'+ detailData.phone"><i></i>电话联系</a>
        </div>
      </div> -->
      <div class="page3_block" v-if="flows && flows.length>0">
        <div class="record">
          <div class="record-inner">
            <template v-for="(item, index) in flows">
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
                    <!-- {{ item.processDesc }} -->
                    <!-- <span style="font-weight: bold; padding-left: 10px">{{ item.createUser }}</span> -->
                  </div>
                  <div v-for="(item2, index2) in item.staffInfos" :key="index2" style="margin-top: 10px">
                    <div style="font-size: 14px;">
                      {{item2.staffName}}-
                      <!-- result：0 待审批 1 通过 2 拒绝 3关闭 4 等待 -->
                      <span v-if="item2.result===0" class="pc_c0">{{item2.resultDesc}}</span>
                      <span v-if="item2.result===1" class="pc_c1">{{item2.resultDesc}}</span>
                      <span v-if="item2.result===2 || item2.result===3" class="pc_c2">{{item2.resultDesc}}</span>
                      <span v-if="item2.result===4" class="pc_c4">{{item2.resultDesc}}</span>
                    </div>
                    <div class="line2">
                      {{ item2.recordDate || item2.createDate}}
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>
          <a class="tel" :href="'tel:'+ detailData.phone"><i></i>电话联系</a>
        </div>
      </div>
    </div>
    <div class="g_logo"></div>
  </div>
</template>
<script>
import { getDetailApi } from '@/services/goodRreleaseLive'
import outFac from '../img/outFac.png'
import p1 from '../img/p_1.png'
import p2 from '../img/p_2.png'
import p3 from '../img/p_3.png'
import codeH1 from '../img/code_h1.png'
import codeH2 from '../img/code_h2.png'

export default {
  data() {
    return {
      outFac: outFac,
      p_1: p1,
      p_2: p2,
      p_3: p3,
      code_h1: codeH1,
      code_h2: codeH2,
      detailData: {
        status: 1, // 1审批中，2审批通过，3审批未通过, 4保安确认页面
        carrier: '',
        phone: '',
        employeeId: '',
        parkId: ''
      },
      flows: [],
      id: ''
    }
  },
  created: function () {
    this.id = this.$route.query.id
    if (this.id) {
      this.getDetail(this.id)
    }
  },
  mounted: function () {},
  methods: {
    async getDetail(id) {
      const res = await getDetailApi(id)
      if (Number(res.data.code) === 0) {
        if (Number(res.data.data.code) === 0) {
          this.detailData = res.data.data.data
        } else {
          this.$message.error(res.data.data.msg)
        }
      } else {
        this.$message.error(res.data.msg)
      }
      this.flows = this.detailData.approvalProcess
      // this.flows[0]= {
      //   processDesc: '提交',
      //   createUser: this.detailData.carrier,//h5端的详情 只有是非员工的能看到，所以这里的提交者直接为携带人
      //   processDate: this.detailData.createTime
      // }
      // let status2 = this.detailData.statusName
      // if (this.detailData.approver) {
      //   this.flows[1]= {
      //     processDesc: status2,
      //     createUser: this.detailData.approver,
      //     processDate: this.detailData.approveTime
      //   }
      // }
      // if (this.detailData.securityStaff) {
      //   this.flows[1].processDesc = '通过'
      //   this.flows[2]= {
      //     processDesc: status2,
      //     createUser: this.detailData.securityStaff,
      //     processDate: this.detailData.departureTime
      //   }
      // }
    }
  }
}
</script>
<style lang="scss" scoped>
.page3 ::v-deep {
  width: 100%;
  position: relative;
  height: 100%;
  overflow-y: auto;
  background: #fafafa;
  font-size: 16px;
  .noBorder {
    border: none !important;
  }
  .page3_block {
    background: #fff;
    margin-bottom: rem(15);
    padding: 0 rem(15);
    box-shadow: 0 0 3px 0 rgba(0, 0, 0, 0.15);
  }
  .code_img {
    width: rem(300);
    height: rem(300);
    border: 1px solid #fff;
    margin: 0 auto 5px;
    img {
      width: 100%;
      height: 100%;
    }
  }
  .code_block {
    padding: rem(15) 0;
  }
  .code_tip {
    text-align: center;
    font-size: 16px;
    color: #999;
  }
  .page3_item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    .item_label {
    }
    .item_value {
      flex: 1;
      overflow: hidden;
      text-align: right;
    }
    .img_h {
      width: rem(100);
      height: rem(100);
      display: inline-block;
      border: 1px solid #dbe0e5;
      background:  #dbe0e5;
      text-align: center;
      position: relative;
      img {
        width: auto;
        height: auto;
        max-width: 100%;
        max-height: 100%;
        position: absolute;
        top: 50%;
        left: 50%;
        transform:translate(-50%,-50%);
      }
    }
  }
  .page3_item2 {
    .item_label {
      color: #999;
    }
    .item_value {
      text-align: left;
      .img_h {
        margin-right: rem(20);
        width: rem(120);
        height: rem(120);
      }
    }
    border-top: 1px solid #eee;
  }
  .page3_item3 {
    flex-direction: column;
    align-items: flex-start;
    .t1 {
      font-size: 18px;
      margin-bottom: rem(10);
    }
    .t2 {
      font-size: 14px;
      margin-bottom: rem(10);
      span {
        color: #ed6d00;
        background: rgba(237, 109, 0, 0.1);
        padding: 2px 5px;
      }
    }
    .t3 {
      font-size: 16px;
    }
  }
  .img-item {
    padding: 0.4rem;
    .goodimg-outer {
      width: 2.4rem;
      height: 2.4rem;
      background: #dbe0e5;
      display: inline-block;
      margin: 0 0 0 0.32rem;
    }
  }
  .g_logo {
    background: url("../img/logo.png");
    background-repeat: no-repeat;
    background-position: center;
    background-size: rem(220) auto;
    height: rem(20);
    margin: rem(45) 0;
  }
  .record {
    position: relative;
    padding-top: 25px;
    .tel{
      position: absolute;
      right: 0;
      top: 25px;
      font-size: 12px;
      color: #ed6d00;
      i{
        width: 9px;
        height: 10px;
        display: inline-block;
        background: url('../img/tel.png');
        background-size: 100% 100%;
        vertical-align: middle;
        margin: -2px 5px 0 0;
      }
    }
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
          background: url('/img/goods/p_1.png');
          background-size: 100% 100%;
        }
        .num2{
          background-image: url('/img/goods/p_2.png');
        }
        .num3{
          background-image: url('/img/goods/p_3.png');
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
}
</style>
