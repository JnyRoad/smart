<template>
  <div class="key-container">
    <div class="key-row">
      <el-button class="key-cell" @click="_handleKeyPress('1')" :class="{'activeCell': activeIndex=='1'}">1</el-button>
      <el-button class="key-cell" @click="_handleKeyPress('2')" :class="{'activeCell': activeIndex=='2'}">2</el-button>
      <el-button class="key-cell" @click="_handleKeyPress('3')" :class="{'activeCell': activeIndex=='3'}">3</el-button>
    </div>
    <div class="key-row">
      <el-button class="key-cell" @click="_handleKeyPress('4')" :class="{'activeCell': activeIndex=='4'}">4</el-button>
      <el-button class="key-cell" @click="_handleKeyPress('5')" :class="{'activeCell': activeIndex=='5'}">5</el-button>
      <el-button class="key-cell" @click="_handleKeyPress('6')" :class="{'activeCell': activeIndex=='6'}">6</el-button>
    </div>
    <div class="key-row">
      <el-button class="key-cell" @click="_handleKeyPress('7')" :class="{'activeCell': activeIndex=='7'}">7</el-button>
      <el-button class="key-cell" @click="_handleKeyPress('8')" :class="{'activeCell': activeIndex=='8'}">8</el-button>
      <el-button class="key-cell" @click="_handleKeyPress('9')" :class="{'activeCell': activeIndex=='9'}">9</el-button>
    </div>
    <div class="key-row">
      <el-button class="key-cell" @click="_handleKeyPress('0')" :class="{'activeCell': activeIndex=='0'}">0</el-button>
      <el-button class="key-cell key-cell2" @click="_handleKeyPress('D')">删除</el-button>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {
      codeNum: '',
      keyNum: '',
      activeIndex: ''
    }
  },
  mounted() {},
  created() {},
  computed: {},
  methods: {
    //处理按键
    _handleKeyPress(e) {
      // console.log(e)
      //不同按键处理逻辑
      switch (String(e)) {
        //删除键
        case 'D':
          this._handleDeleteKey()
          break
        default:
          this._handleNumberKey(e)
          break
      }
    },
    //处理删除键
    _handleDeleteKey() {
      let S = this.codeNum
      // console.log(this.codeNum)
      //如果没有输入，直接返回
      if (!S.length) return false
      //否则删除最后一个
      this.codeNum = S.substring(0, S.length - 1)
      this.activeIndex = ''
      this.$emit('doKeyPress', this.codeNum)
      // console.log('删除键---')
      // console.log(this.codeNum)
      // console.log('删除键---end')
    },
    _handleNumberKey(num) {
      if(['1','2','3','4','5','6','7','8','9','0'].indexOf(num)==-1){
        return
      }
      this.activeIndex = num
      let S = this.codeNum
      if(S.length>6||S.length==6){
        return
      }
      this.codeNum = S + num
      this.$emit('doKeyPress', this.codeNum)
    },
    _clearCode(){
      this.codeNum = ''
      this.$emit('doKeyPress', this.codeNum)
    },
  }
}
</script>
<style lang="scss" scoped>
.key-container ::v-deep {
  width: 470px;
  margin: 0 auto;
  .key-row {
    display: flex;
    justify-content: space-around;
  }
  .key-cell {
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.49);
    border-radius: 3px;
    width: 170px;
    height: 70px;
    line-height: 70px;
    text-align: center;
    font-size: 30px;
    margin: 15px;
    cursor: pointer;
    overflow: hidden;
  }
  .el-button--default {
    color: #333;
    background: #fff;
    border-color: #fff;
    border: none;
    padding:0 ;
  }
  .activeCell{
    color: #fff;
    background: #ED6C00;
  }
  .el-radio-button__inner{
    width: 100%;
    height: 100%;
    border: none;
    padding: 0;
    line-height: 46px;
    font-size: 18px;
    text-align: center;
  }
  .el-radio-button__orig-radio:checked+.el-radio-button__inner{
    background-color: #ED6C00;
  }
  .el-radio-button__orig-radio:hover+.el-radio-button__inner{
    color: #ED6C00;
  }
  .el-radio-button.is-active .el-radio-button__orig-radio:hover+.el-radio-button__inner{
    color: #fff;
  }
  .key-cell2 {
    width: 385px;
    font-size: 20px;
    background: rgb(128, 126, 126);
    color: #fff;
  }
}
</style>
