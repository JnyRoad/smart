<template>
  <div
    v-anim="animationBackGroundData"
    :class="direction"
    class="yc-modal"
    @click.stop="maskCancel(isWindowClick)"
    v-if="visible"
  >
    <div v-anim="animationData" class="yc-modal-inner" @click.stop @transitionend="transitionend">
      <div class="yc-modal-header" v-if="header">
        <slot name="header">
          <div class="yc-modal-header-left" v-if="headerLeftTitle===false"></div>
          <div v-else class="yc-modal-header-left" @click="cancel">{{headerLeftTitle || '取消'}}</div>
          <div class="yc-modal-header-title">{{title || ''}}</div>
          <div class="yc-modal-header-left" v-if="headerRightTitle===false"></div>
          <div class="yc-modal-header-right" @click="ok" v-else>{{headerRightTitle || '确定'}}</div>
        </slot>
      </div>
      <div class="yc-modal-content">
        <slot name="content"></slot>
      </div>
      <div class="yc-modal-footer" v-if="$slots.footer">
        <slot name="footer"></slot>
      </div>
    </div>
  </div>
</template>

<script>
import CreateAnimation from '@/util/transitn-2.0'

export default {
  data() {
    return {
      visible: false,
      animationData: {},
      animationBackGroundData: {},
      transitionStart: false
    }
  },
  props: {
    title: {
      type: String,
      default: ''
    },
    direction: {
      type: String,
      default: 'bottom' // bottom top left right center
    },
    headerLeftTitle: {},
    headerRightTitle: {},
    value: {
      type: Boolean,
      default: false
    },
    header: {
      type: Boolean,
      default: true
    },
    isWindowClick: {
      type: Boolean,
      default: true
    },
    customCloseAfter: {
      type: Boolean,
      default: false
    },
    customOpenAfter: {
      type: Boolean,
      default: false
    }
  },
  directives: {
    anim: {
      inserted: function(el, binding) {},
      update(el, binding) {
        // console.log('update')
      },
      componentUpdated(el, binding) {
        const { value } = binding
        // console.log(value)
        if (value.anim) {
          value.anim.set({
            element: el
          })
          value.anim.start()
        }
      }
    }
  },
  watch: {
    transitionStart() {
      // console.log(this.transitionStart)
      let anim = new CreateAnimation({
        timingFunction: 'ease',
        duration: '.2s'
      })
      let anim2 = new CreateAnimation({
        timingFunction: 'ease',
        duration: '.2s'
      })
      if (this.transitionStart) {
        switch (this.direction) {
          case 'top':
            anim.set({
              to: {
                transform: 'translateY(0)'
              }
            })
            break
          case 'center':
            anim.set({
              to: {
                opacity: 1,
                transform: 'translateY(-50%) scale(1, 1)'
              }
            })
            break
          default:
            anim.set({
              to: {
                opacity: 1,
                transform: 'translateY(0)'
              }
            })
            break
        }
        anim2.set({
          to: {
            'background-color': 'rgba(0, 0, 0, .5)'
          }
        })
      } else {
        switch (this.direction) {
          case 'top':
            anim.set({
              to: {
                opacity: 0,
                transform: 'translateY(-100%)'
              }
            })
            break
          case 'center':
            anim.set({
              to: {
                opacity: 0,
                transform: 'translateY(-50%) scale(0.8, 0.8)'
              }
            })
            break
          default:
            anim.set({
              to: {
                opacity: 0,
                transform: 'translateY(100%)'
              }
            })
            break
        }
        anim2.set({
          to: {
            'background-color': 'rgba(0, 0, 0, 0)'
          }
        })
      }

      setTimeout(() => {
        this.animationData = Object.assign({}, { anim })
        this.animationBackGroundData = Object.assign({}, { anim: anim2 })
      }, 20)
    },
    value() {
      if (this.value) {
        this.visible = true
        this.$nextTick(() => {
          this.transitionStart = true
          this.$emit('load')
        })
      }
    }
  },
  methods: {
    transitionend() {
      if (!this.transitionStart) {
        this.hide()
      } else {
      }
    },
    before() {
      this.$emit('before')
    },
    hide() {
      this.visible = false
      this.$emit('input', false)
    },
    maskCancel(isWindowClick) {
      if (isWindowClick === false) {
        return
      }
      this.transitionStart = false
    },
    cancel() {
      const that = this
      if (this.customCloseAfter) {
        this.$emit('cancel', () => {
          that.transitionStart = false
        })
      } else {
        that.transitionStart = false
      }
    },
    ok() {
      const that = this
      if (this.customOpenAfter) {
        this.$emit('ok', () => {
          that.transitionStart = false
        })
      } else {
        that.transitionStart = false
      }
    }
  }
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
.yc-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 99;
  background-color: rgba(0, 0, 0, 0);
  $radiusSize: px2rem(10px);

  &.bottom &-inner {
    left: 0;
    right: 0;
    bottom: 0;
    border-top-left-radius: $radiusSize;
    border-top-right-radius: $radiusSize;
    transform: translateY(100%);
  }

  &.center &-inner {
    left: px2rem(50px);
    right: px2rem(50px);
    top: 50%;
    border-radius: $radiusSize;
    transform: translateY(-50%) scale(0.8);
    opacity: 0;
  }

  &.top &-inner {
    left: 20%;
    right: 20%;
    top: 0;
    border-bottom-left-radius: $radiusSize;
    border-bottom-right-radius: $radiusSize;
    transform: translateY(-100%);
  }

  &-inner {
    position: absolute;
    background: #fff;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  &-content {
    flex: 1;
    overflow: hidden;
  }

  &-header {
    height: px2rem(100px);
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-content: center;

    &-left {
      line-height: px2rem(100px);
      min-width: px2rem(100px);
      height: 100%;
      color: #333333;
      padding: 0 px2rem(20px);
      text-align: center;
    }

    &-title {
      line-height: px2rem(100px);
      color: #333;
      font-size: px2rem(34px);
      text-align: center;
      flex: 1;
    }

    &-right {
      line-height: px2rem(100px);
      min-width: px2rem(100px);
      text-align: center;
      height: 100%;
      color: #ec6c00;
      padding: 0 px2rem(20px);
    }
  }

  &-footer {
    height: px2rem(100px);
    border-top: 1px solid #eee;
  }
}
</style>
