<template>
  <div class="business-form">
    <slot></slot>
    <template v-if="tips === 1">
      <div class="business-form__message" :class="{ visible: visibleMessage }">{{ messageText }}</div>
    </template>
  </div>
</template>

<script>
const queue = async function (arr) {
  for (let item of arr) {
    if (!item.disable) {
      if (item.required) {
        await item.verifyEmpty()
      }
      if (item.verification) {
        await item.verification()
      }
    }
  }
}
let clearVisibleMessage = false

export default {
  name: 'business-form',
  data() {
    return {
      formData: {},
      messageText: '',
      visibleMessage: false
    }
  },
  props: {
    tips: {
      type: Number,
      default: 1
    },
    duration: {
      type: Number,
      default: 1500
    }
  },
  methods: {
    setFormData(key, value) {
      this.formData[key] = value
      this.$emit('watch')
    },
    hideVisibleMessage() {
      clearVisibleMessage && clearTimeout(clearVisibleMessage)
      clearVisibleMessage = setTimeout(() => {
        this.messageText = ''
        this.visibleMessage = false
      }, this.duration)
    },
    showVisibleMessage(message) {
      clearVisibleMessage && clearTimeout(clearVisibleMessage)
      clearVisibleMessage = setTimeout(() => {
        if (this.tips === 1) {
          this.messageText = message
          this.visibleMessage = true
          this.hideVisibleMessage()
        } else {
          const toast = this.$createToast({
            txt: message,
            time: this.duration,
            mask: true
          })
          toast.show()
        }
        this.$emit('verification', message)
      }, 20)
    },
    pushRequiredNode(node) {
      this.requiredFieldNodes.push(node)
    },
    /**
     * 校验
     */
    verification() {
      return queue(this.requiredFieldNodes)
    },
    reset() {
      for (let item of this.requiredFieldNodes) {
        if (item.reset) {
          item.reset()
        } else {
          item.clearFormData()
        }
      }
    }
  },
  created() {
    this.requiredFieldNodes = []
  },
  beforeCreate() {
    clearVisibleMessage && clearTimeout(clearVisibleMessage)
    this.pushRequiredNode = null
  }
}
</script>

<style lang="scss" scoped>
.business-form {
}
.business-form__message {
  position: fixed;
  line-height: 30px;
  height: 30px;
  text-align: center;
  background: rgba(255, 0, 0, 0.64);
  top: -32px;
  left: 0;
  width: 100%;
  color: #fff;
  transition: all 0.5s;
  z-index: 9999;
  &.visible {
    top: 0;
  }
}
</style>
