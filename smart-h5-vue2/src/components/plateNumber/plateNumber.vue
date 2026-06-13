<template>
  <compoentModal v-model="visible" ref="compoentModal" @load="load" @cancel="cancel" @ok="success" :customOpenAfter="true">
    <template slot="header" v-if="cutomHeader">
      <div class="plate-custom-header">
        <button class="tce-button tce-button--textbtn" @click="doAnther">按员工信息搜索</button>
        <button class="tce-button tce-button--textbtn textbtn2" @click="successCustom">确定</button>
      </div>
    </template>
    <div class="wrap tce-platenumber" slot="content">
      <div class="tce-platenumber-inner">
        <div>
          <div style="margin: 0.1rem 0 0; padding: 0.1rem">
            <label class="radio" :class="{ active: formData.commonCard !== '2' }" style="margin-right: 0.2rem" @click="plate('1')">普通车牌</label>
            <label class="radio" :class="{ active: formData.commonCard === '2' }" @click="plate('2')">新能源车牌</label>
          </div>
          <div style="text-align: right; margin-right: 0.1rem; margin-top: -0.29rem;" @click="clear">清空</div>
        </div>
        <div class="num-box" style="padding: 0.2rem">
          <div class="num0" @click="clickFirstWrap()" :class="activeIndex === 0 ? 'active' : ''">{{ formData.num0 }}</div>
          <div class="num1" @click="clickKeyWordWrap(1)" :class="activeIndex === 1 ? 'active' : ''">{{ formData.num1 }}</div>
          <div class="spot">·</div>
          <div class="num1" @click="clickKeyWordWrap(2)" :class="activeIndex === 2 ? 'active' : ''">{{ formData.num2 }}</div>
          <div class="num1" @click="clickKeyWordWrap(3)" :class="activeIndex === 3 ? 'active' : ''">{{ formData.num3 }}</div>
          <div class="num1" @click="clickKeyWordWrap(4)" :class="activeIndex === 4 ? 'active' : ''">{{ formData.num4 }}</div>
          <div class="num1" @click="clickKeyWordWrap(5)" :class="activeIndex === 5 ? 'active' : ''">{{ formData.num5 }}</div>
          <div class="num1" @click="clickKeyWordWrap(6)" :class="activeIndex === 6 ? 'active' : ''">{{ formData.num6 }}</div>
          <div v-if="formData.commonCard == '2'" class="num1" @click="clickKeyWordWrap(7)" :class="activeIndex === 7 ? 'active' : ''">{{ formData.num7 }}</div>
        </div>
        <!-- <div class="submit-box"><button @click="submitFn()">确认</button></div> -->
        <div class="keyboard-box">
          <div class="first-word-wrap" v-show="firstWrapStatus">
            <div class="first-word" @click="selectFirstWord($event)">
              <div class="word">
                <span data-value="京">京</span>
              </div>
              <div class="word">
                <span data-value="湘">湘</span>
              </div>
              <div class="word">
                <span data-value="津">津</span>
              </div>
              <div class="word">
                <span data-value="鄂">鄂</span>
              </div>
              <div class="word">
                <span data-value="沪">沪</span>
              </div>
              <div class="word">
                <span data-value="粤">粤</span>
              </div>
              <div class="word">
                <span data-value="渝">渝</span>
              </div>
              <div class="word">
                <span data-value="琼">琼</span>
              </div>
              <div class="word">
                <span data-value="冀">冀</span>
              </div>
              <div class="word">
                <span data-value="川">川</span>
              </div>
            </div>
            <div class="first-word" @click="selectFirstWord($event)">
              <div class="word">
                <span data-value="晋">晋</span>
              </div>
              <div class="word">
                <span data-value="贵">贵</span>
              </div>
              <div class="word">
                <span data-value="辽">辽</span>
              </div>
              <div class="word">
                <span data-value="云">云</span>
              </div>
              <div class="word">
                <span data-value="吉">吉</span>
              </div>
              <div class="word">
                <span data-value="陕">陕</span>
              </div>
              <div class="word">
                <span data-value="黑">黑</span>
              </div>
              <div class="word">
                <span data-value="甘">甘</span>
              </div>
              <div class="word">
                <span data-value="苏">苏</span>
              </div>
              <div class="word">
                <span data-value="青">青</span>
              </div>
            </div>
            <div class="first-word" @click="selectFirstWord($event)">
              <div class="word">
                <span data-value="浙">浙</span>
              </div>
              <div class="word">
                <span data-value="皖">皖</span>
              </div>
              <div class="word">
                <span data-value="藏">藏</span>
              </div>
              <div class="word">
                <span data-value="闽">闽</span>
              </div>
              <div class="word">
                <span data-value="蒙">蒙</span>
              </div>
              <div class="word">
                <span data-value="赣">赣</span>
              </div>
              <div class="word">
                <span data-value="桂">桂</span>
              </div>
              <div class="word">
                <span data-value="鲁">鲁</span>
              </div>
              <div class="word">
                <span data-value="宁">宁</span>
              </div>
              <div class="word">
                <span data-value="豫">豫</span>
              </div>
            </div>
            <div class="first-word" @click="selectFirstWord($event)">
              <div class="word">
                <span data-value="新">新</span>
              </div>
              <div class="word bordernone">
                <!-- <img src="../assets/images/icon-switch.png" alt=""> -->
              </div>
              <div class="word bordernone">
                <!-- <img src="../assets/images/icon-switch.png" alt=""> -->
              </div>
              <div class="word bordernone">
                <!-- <img src="../assets/images/icon-switch.png" alt=""> -->
              </div>
              <div class="word bordernone">
                <!-- <img src="../assets/images/icon-switch.png" alt=""> -->
              </div>
              <div class="word bordernone">
                <!-- <img src="../assets/images/icon-switch.png" alt=""> -->
              </div>
            </div>
          </div>
          <div class="keyboard-wrap" v-show="keyBoardStatus === true">
            <div class="keyboard" v-if="activeKeyWordIndex !== 1">
              <span v-for="(item, index) in allKeyWord._1" :key="index" @click="clickKeyBoard(item)">{{ item }}</span>
            </div>
            <div class="keyboard">
              <span v-for="(item, index) in allKeyWord._3" :key="index" @click="clickKeyBoard(item)">{{ item }}</span>
            </div>
            <div class="keyboard">
              <span v-for="(item, index) in allKeyWord._4" :key="index" @click="clickKeyBoard(item)">{{ item }}</span>
            </div>
            <div class="keyboard">
              <span v-for="(item, index) in allKeyWord._5" :key="index" @click="clickKeyBoard(item)">{{ item }}</span>
              <span class="bordernone"></span>
              <span class="bordernone"></span>
              <span class="bordernone"></span>
              <span class="bordernone"></span>
              <span class="bordernone"></span>
              <span class="bordernone"></span>
            </div>
            <div class="keyboard" v-if="activeKeyWordIndex !== 1" style="position: relative">
              <span v-for="(item, index) in allKeyWord._7" :key="index" @click="clickKeyBoard(item)">{{ item }}</span>
              <span class="bordernone"></span>
              <span class="bordernone"></span>
              <span class="bordernone"></span>
              <span class="bordernone"></span>
              <span class="bordernone"></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </compoentModal>
</template>
<script>
import isPlateNumber from '@/util/isPlateNumber'
import compoentModal from '../modal/index'
export default {
  data() {
    return {
      visible: false,
      formData: {
        commonCard: '1',
        num0: '',
        num1: '',
        num2: '',
        num3: '',
        num4: '',
        num5: '',
        num6: '',
        num7: ''
      },
      allKeyWord: {
        _1: [1, 2, 3, 4, 5, 6, 7, 8, 9, 0],
        _2: [],
        _3: ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K'],
        _4: ['L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V'],
        _5: ['W', 'X', 'Y', 'Z'],
        _6: [],
        _7: ['港', '澳', '学', '领', '警']
      },
      activeIndex: 0,
      activeKeyWordIndex: 1, // 当前车牌号
      keyBoardStatus: false,
      firstWrapStatus: true, // 选择弹窗
      confirmTitle: '',
      submitConfirm: false,
      submitConfirmFalse: false,
      submitConfirmText: ''
    }
  },
  components: {
    compoentModal
  },
  props: {
    value: {
      type: String,
      default: ''
    },
    cutomHeader: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    'formData.commonCard': function () {
      if (this.formData.commonCard === '2' && this.activeIndex >= 7) {
        this.formData.num7 = ''
        this.keyBoardStatus = true
        this.activeKeyWordIndex = 7
        this.activeIndex = 7
      }
      if (this.formData.commonCard === '1' && this.activeIndex >= 7) {
        this.formData.num7 = ''
        this.keyBoardStatus = false
        this.activeIndex = 8
      }
    }
  },
  methods: {
    edit(str) {
      str.split('').forEach((s, index) => {
        this.formData[`num${index}`] = s
      })
      this.formData.commonCard = '1'
      if (str.length === 8) {
        this.formData.commonCard = '2'
      }
      this.keyBoardStatus = false
      this.firstWrapStatus = false
      this.activeIndex = -1
    },
    getLicense() {
      let plateLicense
      if (this.formData.commonCard === '1') {
        plateLicense = this.plate_license_1
        plateLicense = this.palindrome(plateLicense)
      }
      if (this.formData.commonCard === '2') {
        plateLicense = this.plate_license_2
        plateLicense = this.palindrome(plateLicense)
      }
      return plateLicense
    },
    verification() {
      let plateLicense
      if (this.formData.commonCard === '1') {
        plateLicense = this.plate_license_1
        plateLicense = this.palindrome(plateLicense)
        if (plateLicense.length < 7) {
          return false
        }
      }
      if (this.formData.commonCard === '2') {
        plateLicense = this.plate_license_2
        plateLicense = this.palindrome(plateLicense)
        if (plateLicense.length < 8) {
          return false
        }
      }
      if (!isPlateNumber(plateLicense)) {
        this.$tceMobile.toast('车牌号不正确')
        this.$emit('getPlateLicenseError', '车牌号不正确')
        return false
      }
      return true
    },
    doAnther() {
      this.$emit('doAnther')
      this.$refs.compoentModal.cancel()
    },
    successCustom() {
      this.$refs.compoentModal.ok()
    },
    success(next) {
      this.firstWrapStatus = false
      this.keyBoardStatus = false
      this.activeIndex = -1
      this.submitFn(next)
    },
    start() {
      this.clear()
      this.visible = true
    },
    cancel(next) {
      this.clear()
      next()
    },
    clear() {
      this.formData.num0 = ''
      this.formData.num1 = ''
      this.formData.num2 = ''
      this.formData.num3 = ''
      this.formData.num4 = ''
      this.formData.num5 = ''
      this.formData.num6 = ''
      this.formData.num7 = ''
      this.keyBoardStatus = false
      this.firstWrapStatus = true
      this.activeIndex = 0
      this.$emit('clear', '')
    },
    plate(value) {
      this.formData.commonCard = value
    },
    clickFirstWrap() {
      // 点击第一个输入框
      this.firstClickStatus = true
      this.firstWrapStatus = true
      this.keyBoardStatus = false
      // this.formData.num0 = '';
      this.activeIndex = 0
    },
    selectFirstWord(event) {
      // console.log(event)
      if (!event.target.dataset.value) {
        return
      }
      this.formData.num0 = event.target.dataset.value
      this.firstSelectStatus = true
      this.firstWrapStatus = false
      this.firstClickStatus = false
      this.keyBoardStatus = true
      this.activeKeyWordIndex = 1
      this.activeIndex = 1
      this.isPlateLicense()
      // this.$refs.num1.focus()
      // document.getElementById('num1').focus()
    },
    clickKeyBoard(item) {
      // 点击自定义键盘
      // console.log(item);
      this.formData['num' + this.activeKeyWordIndex] = item
      if (this.formData.commonCard === '1') {
        this.activeKeyWordIndex++
        this.activeIndex++
        if (this.activeKeyWordIndex > 6) {
          this.keyBoardStatus = false
          if (!this.formData.num0) {
            this.activeIndex = 0
          }
        }
      } else {
        this.activeKeyWordIndex++
        this.activeIndex++
        if (this.activeKeyWordIndex > 7) {
          this.keyBoardStatus = false
          if (!this.formData.num0) {
            this.activeIndex = 0
          }
        }
      }
      this.isPlateLicense()
    },
    deleteWord() {
      // 退格
      // console.log(this.activeKeyWordIndex)
      // console.log(this.formData['num' + (this.activeKeyWordIndex - 1)])
      if (this.activeKeyWordIndex > 1) {
        this.formData['num' + this.activeKeyWordIndex] = ''
        // this.activeKeyWordIndex--;
      }
      if (this.activeIndex > 0) {
        // this.activeIndex--
      }
    },
    clickKeyWordWrap(activeKeyWordIndex) {
      this.firstWrapStatus = false
      this.keyBoardStatus = true
      this.activeKeyWordIndex = activeKeyWordIndex
      // this.formData['num' + this.activeKeyWordIndex] = '';
      this.activeIndex = activeKeyWordIndex || 0
    },
    isPlateLicense() {
      let plateLicense
      // let isssub
      if (this.formData.commonCard === '1') {
        plateLicense = this.plate_license_1
        plateLicense = this.palindrome(plateLicense)
        if (plateLicense.length === 7) {
          // isssub = 1
        }
      }
      if (this.formData.commonCard === '2') {
        plateLicense = this.plate_license_2
        plateLicense = this.palindrome(plateLicense)
        if (plateLicense.length === 8) {
          // isssub = 1
        }
      }

      // if (isssub) {
      //   this.submitFn()
      // }
    },
    submitFn(next) {
      let plateLicense
      if (this.formData.commonCard === '1') {
        plateLicense = this.plate_license_1
        plateLicense = this.palindrome(plateLicense)
        if (plateLicense.length > 0 && plateLicense.length < 7) {
          this.$tceMobile.toast('车牌号不正确')
          this.$emit('getPlateLicenseError', '车牌号不正确')
          return
        }
      }
      if (this.formData.commonCard === '2') {
        plateLicense = this.plate_license_2
        plateLicense = this.palindrome(plateLicense)
        if (plateLicense.length > 0 && plateLicense.length < 8) {
          this.$tceMobile.toast('车牌号不正确')
          this.$emit('getPlateLicenseError', '车牌号不正确')
          return
        }
      }
      if (plateLicense.length > 0 && !isPlateNumber(plateLicense)) {
        this.$tceMobile.toast('车牌号不正确')
        this.$emit('getPlateLicenseError', '车牌号不正确')
        return false
      }
      this.keyBoardStatus = false
      this.firstWrapStatus = false
      this.activeIndex = -1
      next()
      this.$emit('getPlateLicense', plateLicense)
      this.$emit('input', plateLicense)
      // console.log(plateLicense);
      // alert(plateLicense);
    },
    palindrome(str) {
      var arr = str.split('')
      arr = arr.filter(function (val) {
        return (
          val !== ' ' &&
          val !== ',' &&
          val !== '.' &&
          val !== '?' &&
          val !== ':' &&
          val !== ';' &&
          val !== '`' &&
          val !== "'" &&
          val !== '_' &&
          val !== '/' &&
          val !== '-' &&
          val !== '\\' &&
          val !== '' &&
          val !== '(' &&
          val !== ')'
        )
      })
      return arr.join('')
    },
    checkIsHasSpecialStr(str) {
      var flag = false
      var arr = str.split('')
      arr.forEach((val) => {
        if (
          val === '!' ||
          val === '}' ||
          val === '{' ||
          val === ']' ||
          val === '[' ||
          val === '&' ||
          val === '$' ||
          val === '@' ||
          val === ' ' ||
          val === ',' ||
          val === '.' ||
          val === '?' ||
          val === ':' ||
          val === ';' ||
          val === '`' ||
          val === "'" ||
          val === '_' ||
          val === '/' ||
          val === '-' ||
          val === '\\' ||
          val === '' ||
          val === '(' ||
          val === ')'
        ) {
          flag = true
        }
      })
      return flag
    },
    checkIsHasChineseStr(str) {
      var Reg = /.*[\u4e00-\u9fa5]+.*/
      if (Reg.test(str)) {
        return true
      }
      return false
    },
    load() {
      if (this.value) {
        this.edit(this.value)
      }
    }
  },
  computed: {
    plate_license_1() {
      return this.formData.num0 + this.formData.num1 + this.formData.num2 + this.formData.num3 + this.formData.num4 + this.formData.num5 + this.formData.num6
    },
    plate_license_2() {
      return this.formData.num0 + this.formData.num1 + this.formData.num2 + this.formData.num3 + this.formData.num4 + this.formData.num5 + this.formData.num6 + this.formData.num7
    }
  },
  mounted() {}
}
</script>
<style lang="scss">
@use 'sass:list';
@use 'sass:math';

/**
 * px 转换为rem
 * @param {Object} $size
 */
@function px2rem($sizes, $unitDefault: 1rem) {
  $result: null;
  @each $size in $sizes {
    $unit: math.unit($size);
    $size: math.div(math.div(math.div($size, $size * 0 + 1), 100), 2);
    $result: list.append($result, $size * $unitDefault);
  }
  @return $result;
}
.plate-custom-header{
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding-left: rem(20);
  padding-right: rem(20);
  .tce-button{
    width: auto;
    font-weight: bold;
  }
  .textbtn2{
    color: #ec6c01;
  }
}
.flex-items-center {
  display: flex;
  align-items: center;
}

.wrap {
  border-radius: px2rem(5px);
  .radio.active {
    color: rgb(36, 134, 255);
  }
  // input输入框
  .num-box {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .spot {
      width: px2rem(20px);
      border: none;
    }

    & > div {
      width: px2rem(60px);
      height: px2rem(60px);
      text-align: center;
      line-height: px2rem(60px);
      border: 1px solid #c0c0c0;

      &.active {
        border: 1px solid rgb(36, 134, 255);

        &:after {
          border-bottom: 0.5rem solid #4a90e2;
        }
      }
    }
  }
}

$key-height: px2rem(400px);

.keyboard-box {
  height: $key-height;
}

.first-word-wrap {
  height: $key-height;
  background-color: #fff;
  padding-bottom: px2rem(20px);

  .first-word {
    display: flex;
    justify-content: space-between;
    padding: px2rem(20px) px2rem(20px) 0;

    .word {
      box-sizing: border-box;
      width: px2rem(56px);
      height: px2rem(56px);
      // border: 1px solid #9cbce2;
      box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.1);
      // border-radius: 10px;
      text-align: center;

      &.bordernone {
        border: none;
        box-shadow: none;
      }

      span {
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        width: 100%;
        height: 100%;
        background-color: #fff;
        color: #000;
        // border: 1px solid #fff;
        border-radius: 2px;
      }
    }

    &:nth-last-of-type(1) {
      margin-bottom: 0;
    }
  }
}

.keyboard-wrap {
  height: $key-height;
  padding-bottom: px2rem(20px);

  .keyboard {
    display: flex;
    justify-content: space-between;
    align-items: center;
    justify-content: space-between;
    padding: px2rem(20px) px2rem(20px) 0;

    span {
      text-align: center;
      display: flex;
      width: px2rem(56px);
      height: px2rem(56px);
      align-items: center;
      justify-content: center;
      margin: 0;
      box-shadow: 0px 1px 4px rgba(0, 0, 0, 0.1);
      background-color: #fff;
      border-radius: 2px;

      &:active {
        // background-color: #e4e4e4;
      }

      &.bordernone {
        border: none;
        box-shadow: none;
        // background-color: #d2d5db;

        &:active {
          // background-color: rgba(0, 0, 0, .2);
        }
      }
    }
  }
}

.tce-platenumber {
  width: 100%;

  &-inner {
    width: 100%;
  }
}
</style>
