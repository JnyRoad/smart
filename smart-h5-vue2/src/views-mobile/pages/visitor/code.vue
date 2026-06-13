<!--访客码-->
<template>
  <div class="page3">
    <div class="page3_cont">
      <div class="page3_block code_block">
        <div class="top">裕同科技许昌园区欢迎您</div>
        <template v-if="delFlag === 0">
          <div class="code_img">
            <tce-image :src="'data:image/jpg;base64,'+detailData.qrCode" :width="0" :height="0"></tce-image>
          </div>
          <div class="code_num">{{detailData.smsCode}}</div>
          <div class="t3"><span>首次扫码，打印有效</span></div>
          <div class="tip1">请截图保存该页面，入园接受检查时可出此凭证，以便核实</div>
        </template>
        <template v-if="delFlag === 1">
          <div class="code_img">
            <tce-image :src="outFac" :width="0" :height="0"></tce-image>
          </div>
          <div class="code_tip">二维码已失效</div>
        </template>
        <template v-if="delFlag === 2">
          <div class="code_img">
            <tce-image :src="outFac" :width="0" :height="0"></tce-image>
          </div>
          <div class="code_tip">二维码已失效</div>
        </template>
      </div>
      <div class="page3_block" v-if="delFlag === 0 || delFlag === 1">
        <div class="page3_item page3_item3">
          <div class="t1">{{detailData.parkName}}</div>
          <div class="t2"><span>{{detailData.causeDesc}}</span></div>
        </div>
        <div class="page3_item page3_item2">
          <div class="item_label">{{detailData.receptionistName}}<span class="itip">（被访人）</span></div>
          <div class="item_value">
            {{detailData.receptionistPhone}}
          </div>
        </div>
        <div class="page3_item page3_item2">
          <div class="item_label">{{detailData.visitorName}}<span class="itip">（访客）</span></div>
          <div class="item_value">
            {{detailData.visitorPhone}}
          </div>
        </div>
        <div class="page3_item page3_item2">
          <div class="item_label">区域类型：</div>
          <div class="item_value">
            {{detailData.permitFactoryTypeDesc}}
          </div>
        </div>
        <div class="page3_item page3_item2" v-if="newAreaType !== ''">
          <div class="item_label">新工厂：</div>
          <div class="item_value">
            {{newAreaType}}
          </div>
        </div>
        <div class="page3_item page3_item2" v-if="oldAreaType !== ''">
          <div class="item_label">老工厂：</div>
          <div class="item_value">
            {{oldAreaType}}
          </div>
        </div>
        <div class="page3_item page3_item2">
          <div class="item_label">预约到访时间：</div>
          <div class="item_value">
            {{detailData.startTime}}
          </div>
        </div>
        <div class="page3_item page3_item2">
          <div class="item_label">预约离开时间：</div>
          <div class="item_value">
            {{detailData.endTime}}
          </div>
        </div>
      </div>
      <div class="page3_block" v-if="delFlag === 0">
        <div class="bt_block">
          <div class="bt-item bt1">
            <div class="hd"></div>
            <div class="bt_n">01</div>
            <div>填写预约码</div>
          </div>
          <div class="bt-item bt2">
            <div class="hd"></div>
            <div class="bt_n">02</div>
            <div>打印凭条</div>
          </div>
          <div class="bt-item bt3">
            <div class="hd"></div>
            <div class="bt_n">03</div>
            <div>粘贴至胸前</div>
          </div>
        </div>
      </div>
    </div>
    <div class="g_logo"></div>
  </div>
</template>
<script>
import outFac from './img/outFac.png'
import { getCode, getAreaType } from '@/services/visitor'

export default {
  name: '',
  data() {
    return {
      outFac: outFac,
      detailData: {},
      delFlag: '', // 0期限内未打印, 1期限内已删除, 2已过期
      id: '',
      factoryList: [],
      areaNewType: [],
      areaOldType: [],
      newAreaType: '',
      oldAreaType: ''
    }
  },
  created: function () {
    this.id = this.$route.query.id
    this.getAreaType(0)
    this.getAreaType(1)
    if (this.id) {
      this.getDetail(this.id)
    }
  },
  mounted: function () {},
  methods: {
    async getDetail(id) {
      const res = await getCode(id)
      if (Number(res.code) === 0) {
        this.detailData = res.data
        this.delFlag = this.detailData.delFlag
        const areaType = this.detailData.areaType
        const newAreaTypeList = []; const oldAreaTypeList = []
        areaType.forEach((element) => {
          const ant = this.areaNewType.filter((item) => String(item.code) === String(element))
          if (ant.length > 0) {
            newAreaTypeList.push(ant[0].desc)
          }
          const aot = this.areaOldType.filter((item) => String(item.code) === String(element))
          if (aot.length > 0) {
            oldAreaTypeList.push(aot[0].desc)
          }
        })
        if (this.detailData.permitArea !== '') {
          newAreaTypeList.push(this.detailData.permitArea)
        }
        if (this.detailData.permitOldArea !== '') {
          oldAreaTypeList.push(this.detailData.permitOldArea)
        }
        if (newAreaTypeList.length > 0) {
          this.newAreaType = newAreaTypeList.join(',')
        }
        if (oldAreaTypeList.length > 0) {
          this.oldAreaType = oldAreaTypeList.join(',')
        }
      } else {
        this.$message.error(res.msg)
      }
    },
    // async getFactoryList() {
    //   const res = await getFactoryList()
    //   if (res.code==0) {
    //     this.factoryList = res.data.data
    //   } else {
    //     this.$message.error(res.msg);
    //   }
    // },
    async getAreaType(type) {
      const res = await getAreaType({ flag: type })
      if (Number(res.code) === 0) {
        if (Number(type) === 1) { // 1是新工厂，0是老工厂
          this.areaNewType = res.data
        } else {
          this.areaOldType = res.data
        }
      } else {
        this.$message.error(res.msg)
      }
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
  background: #f0f2f5;
  font-size: 16px;
  .top{
    padding: rem(30) 0 rem(30);
    text-align: center;
  }
  .tip1{
    font-size: 12px;
    color: #999;
    text-align: center;
  }
  .code_num{
    font-size: 30px;
    letter-spacing: 2px;
    text-align: center;
  }
  .t3 {
    font-size: 12px;
    margin: rem(10) 0;
    text-align: center;
    span {
      color: #ee7713;
      background: rgba(238, 117, 19, 0.1);
      padding: 2px 5px;
    }
  }
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
    color: #ff1c1c;
    margin: rem(20) 0 rem(15);
  }
  .page3_item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: rem(20) 0;
    .item_label {
    }
    .item_value {
      flex: 1;
      overflow: hidden;
      text-align: right;
    }
  }
  .page3_item2 {
    color: #333;
    .item_label {
      .itip{
        font-size: 12px;
        color: #999;
      }
    }
    .item_value {
    }
    border-bottom: 1px solid #eee;
  }
  .page3_item1{
    color: #666;
    padding: 0 0 5px 0;
    margin-bottom: rem(10);
    .item_label {
    }
  }
  .page3_item3 {
    flex-direction: column;
    align-items: flex-start;
    border-bottom: 1px solid #eee;
    .t1 {
      font-size: 18px;
      margin-bottom: rem(10);
    }
    .t2 {
      font-size: 14px;
      margin-bottom: rem(10);
      span {
        color: #6d7c8e;
        background: rgba(109, 124, 142, 0.1);
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
    background: url("./img/logo.png");
    background-repeat: no-repeat;
    background-position: center;
    background-size: rem(220) auto;
    height: rem(20);
    margin: rem(45) 0;
  }
  .bt_block{
    display: flex;
    padding: rem(30) 0;
    .bt-item{
      flex: 1;
      text-align: center;
      font-size: 14px;
      color: #333;
      .hd{
        width: rem(80);
        height: rem(120);
        margin: 0 auto rem(10);
        background-repeat: no-repeat;
        background-position: center;
        background-size: 100% auto;
      }
      .bt_n{
        margin-bottom: rem(10);
      }
    }
    .bt1 .hd{
      width: rem(60);
      background-image: url('./img/fk_1.png');
    }
    .bt2 .hd{
      background-image: url('./img/fk_2.png');
    }
    .bt3 .hd{
      background-image: url('./img/fk_3.png');
    }
  }
}
</style>
