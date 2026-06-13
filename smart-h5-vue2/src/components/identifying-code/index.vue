<template>
  <div>
    <div class="identifying-code" :class="{ disabled: !isSend }" @click="endmsg">
      <slot></slot>
      <cube-loading v-if="loading"></cube-loading> {{ isSend ? msgText : countDownText }}
    </div>
  </div>
</template>

<script>
import isMobile from '@/util/isMobile'
import executeOnce from '@/util/executeOnce'
import { getPhoneMessage } from '@/services/other'
const executeOnceSubmit = executeOnce().executeOnce
export default {
  data() {
    return {
      isSend: true,
      surplusTime: 60,
      countDownText: '',
      clearCountDown: null,
      loading: false
    }
  },
  props: {
    phone: {
      type: String,
      default: ''
    },
    msgText: {
      type: String,
      default: '获取验证码'
    },
    customCountDown: {
      type: Function,
      default: null
    },
    duration: {
      type: Number,
      default: 60
    }
  },
  methods: {
    setCountDownText() {
      if (this.customCountDown) {
        this.countDownText = this.customCountDown(this.surplusTime)
      } else {
        this.countDownText = `${this.surplusTime}s`
      }
    },
    /**
     * 发送短信
     */
    async endmsg() {
      // console.log('this.phone', this.phone)
      if (!this.phone) {
        executeOnceSubmit.error('请输入手机号')
        return
      }
      if (!isMobile(this.phone)) {
        executeOnceSubmit.error('手机号格式不正确')
      }
      if (this.isSend === false) {
        // executeOnceSubmit.error('请稍后再试');
        return
      }
      this.loading = true
      const awaitRes = await executeOnceSubmit.done(getPhoneMessage(this.phone))
      this.loading = false
      if (awaitRes.code === 0) {
        this.isSend = false
        this.surplusTime = this.duration
        this.setCountDownText()
        this.clearCountDown = setInterval(() => {
          this.surplusTime--
          this.setCountDownText()
          if (this.surplusTime === 0) {
            this.isSend = true
            clearInterval(this.clearCountDown)
            this.$emit('eventCustomCountDown', this.surplusTime)
          }
        }, 1000)
        await awaitRes.done('发送成功')
      } else {
        await awaitRes.done(awaitRes.message)
      }
    }
  }
}
</script>

<style lang="scss">
.identifying-code {
  display: flex;
  text-align: center;
  justify-content: center;
  align-items: center;

  .cube-loading {
  }
  .cube-loading-spinners {
    width: 16px;
    height: 16px;
    margin-right: 5px;
  }
}
</style>
