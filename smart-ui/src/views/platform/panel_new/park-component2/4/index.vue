
<template>
  <div class="wrap">
    <div class="wrap_inner">
      <div class="wrap_cont">
        <div class="c_left">
          <chart/>
        </div>
        <div class="c_right">
          <div class="c_top">
            员工总数
            <span class="num">200</span>
          </div>
          <div class="c_btm">
            <div class="c_i">
              <i class="block_i posi_0"></i>
              <span class="c_i_title">石岩</span>
              <div class="c_i_p">
                <el-progress :percentage="80" color="#63C1FF" :show-text="false" :stroke-width="10"></el-progress>
              </div>
              <span class="c_i_num num">52</span>
              <span class="c_i_num2">55.5%</span>
            </div>
            <div class="c_i">
              <i class="block_i posi_1"></i>
              <span class="c_i_title">龙岗</span>
              <div class="c_i_p">
                <el-progress :percentage="80" color="#63C1FF" :show-text="false" :stroke-width="10"></el-progress>
              </div>
              <span class="c_i_num num">52</span>
              <span class="c_i_num2">55.5%</span>
            </div>
            <div class="c_i">
              <i class="block_i posi_2"></i>
              <span class="c_i_title">大岭山</span>
              <div class="c_i_p">
                <el-progress :percentage="80" color="#63C1FF" :show-text="false" :stroke-width="10"></el-progress>
              </div>
              <span class="c_i_num num">52</span>
              <span class="c_i_num2">55.5%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import chart from './chart-item'
export default {
  data() {
    return {};
  },
  components: {
    chart
  },
  mounted() {},
  methods: {}
};
</script>

<style lang="scss" scoped>
.wrap {
  width: 880px;
  height: 221px;
  background-image: url("/img/panel_new/park/bg_4.png");
  &_top {
    &_t {
      width: auto !important;
    }
  }
  &_cont{
    display: flex;
    padding: 0 20px 20px 20px;
    overflow: hidden;
  }
  .c_left{
    width: 165px;
    padding-top: 10px;
  }
  .c_right{
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    padding-top: 30px;
    padding-left: 20px;
    .c_top{
      color: #a6d4ff;
      font-size: 16px;
      margin-bottom: 20px;
      .num{
        font-size: 20px;
        margin-left: 20px;
      }
    }
    .c_btm{
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }
    .c_i{
      display: flex;
      align-items: center;
      padding: 0 10px;
      margin-bottom: 8px;
      &_title{
        width: 60px;
        font-size: 16px;
        margin-right: 8px;
        overflow: hidden;
        text-overflow:ellipsis;
        white-space: nowrap;
      }
      &_p{
        flex: 1;
        margin-right: 3px;
      }
      &_num{
        margin-right: 8px;
        width: 55px;
        text-align: right;
        font-weight: normal;
      }
      &_num2{
        width: 43px;
        text-align: right;
      }
      .block_i{
        margin-top: 0;
      }
      .posi_0{
        background: #FF7680;
      }
      .posi_1{
        background: #63CEFF;
      }
      .posi_2{
        background: #FFCD7F;
      }
    }
  }
}
</style>
