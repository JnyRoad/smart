<template>
  <transition name="vue-toast" @after-leave="handleAfterLeave">
    <div class="toast-wraper" v-show="visible">
      <div class="toast-background" />
      <div class="toast-frame">{{ message }}</div>
    </div>
  </transition>
</template>

<script>
export default {
  name: 'VueToastHolder',
  data() {
    return {
      visible: false,
      message: '',
      timer: null,
      duration: 2000
    }
  },
  watch: {
    message() {
      this.clearTimer()
      this.startTimer()
    }
  },
  methods: {
    handleAfterLeave() {
      this.$destroy(true)
      this.$el.parentNode.removeChild(this.$el)
    },
    close() {
      this.visible = false
    },
    clearTimer() {
      clearTimeout(this.timer)
    },
    startTimer() {
      if (this.duration > 0) {
        this.timer = setTimeout(() => {
          this.onClose()
          if (!this.closed) {
            this.close()
          }
        }, this.duration)
      }
    }
  },
  mounted() {
    this.startTimer()
  }
}
</script>

<style lang="scss">
// structure
.toast-wraper {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 99999999;
  display: flex;
  align-items: center;
}
.toast-background {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 0;
}
.toast-frame {
  flex-grow: 0;
  flex-shrink: 0;
  margin-left: auto;
  margin-right: auto;
  position: relative;
  z-index: 1;
}
@media all and (min-width: 769px) {
  .toast-wraper {
    padding: 1em;
    align-items: flex-start;
  }
  .toast-frame {
    max-width: 50vw;
  }
}
// theme
.toast-wraper {
}
.toast-background {
}
.toast-frame {
  max-width: 220px;
  padding: 12px 24px;
  background: rgba(58, 58, 58, 0.8);
  border-radius: 4px;
  overflow: hidden;
  font-family: sans-serif;
  font-size: 13px;
  line-height: 16px;
  color: #ffffff;
  text-align: center;
  word-break: break-word;
  &::before {
    display: block;
    width: 24px;
    height: 24px;
    margin-top: 2px;
    margin-bottom: 8px;
    margin-left: auto;
    margin-right: auto;
    background-repeat: no-repeat;
    background-position: center center;
    background-size: contain;
  }
}
.toast-frame-success {
  &::before {
    content: '';
    background-image: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTMuNDgyIDMuNTM2QzUuODQgMS4xNzkgOC42OCAwIDEyIDBjMy4zMjEgMCA2LjE1MiAxLjE3IDguNDkxIDMuNTA5QzIyLjgzMSA1Ljg0OSAyNCA4LjY3OSAyNCAxMmMwIDMuMzIxLTEuMTcgNi4xNTItMy41MDkgOC40OTFDMTguMTUxIDIyLjgzMSAxNS4zMjEgMjQgMTIgMjRjLTMuMzIxIDAtNi4xNTItMS4xNy04LjQ5MS0zLjUwOUMxLjE2OSAxOC4xNTEgMCAxNS4zMjEgMCAxMmMwLTMuMzIxIDEuMTYtNi4xNDMgMy40ODItOC40NjR6bTE0LjY3OSA0LjQ0NmMuMDM1LS4wMzYuMDUzLS4wOS4wNTMtLjE2IDAtLjEwOC0uMDE4LS4xOC0uMDUzLS4yMTVMMTYuNSA2LjMyMWMtLjAzNi0uMDM1LS4xMDctLjA1My0uMjE0LS4wNTMtLjA3MiAwLS4xMjUuMDE4LS4xNjEuMDUzbC01Ljk0NiA3LjY2MWExMjQ2LjgxIDEyNDYuODEgMCAwIDEtMi40MTEtMi4zMDNjLS4xMDctLjEwOC0uMjE0LS4xNjEtLjMyMi0uMTYxLS4wMzUgMC0uMTA3LjA1My0uMjE0LjE2TDUuODQgMTMuMDcyYy0uMDM1LjAzNi0uMDUzLjEwOC0uMDUzLjIxNSAwIC4wNzEuMDE4LjEyNS4wNTMuMTZsLjEwNy4wNTQgNC4yMzMgNC4wNzFjLjEwNy4xMDguMTk2LjE2MS4yNjcuMTYxLjA3MiAwIC4xNjEtLjA1My4yNjgtLjE2bDcuNDQ3LTkuNTl6IiBmaWxsPSIjRkZGIi8+PC9zdmc+');
  }
}
.toast-frame-info {
  &::before {
    content: '';
    background-image: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTI0IDExLjg0Yy4wMzUgMy4zMi0xLjA5OSA2LjE2LTMuNDAyIDguNTE3LTIuMzA0IDIuMzU3LTUuMTE2IDMuNTctOC40MzcgMy42NDItMy4zMjIuMDM2LTYuMTYtMS4wOTgtOC41MTgtMy40MDFDMS4yODYgMTguMjk0LjA3MyAxNS40ODIuMDAxIDEyLjE2LS4wMzUgOC44MzkgMS4wOTkgNiAzLjQwMiAzLjY0MyA1LjcwNiAxLjI4NiA4LjUxOC4wNzMgMTEuODQuMDAxYzMuMzIyLS4wMzYgNi4xNiAxLjA5OCA4LjUxOCAzLjQwMSAyLjM1NyAyLjMwNCAzLjU3IDUuMTE2IDMuNjQyIDguNDM3ek0xMy4yMzEgNC43NjdjLS4zMjEtLjMyMS0uNzMyLS40ODItMS4yMzItLjQ4MnMtLjkxLjE2LTEuMjMyLjQ4MmMtLjMyMS4zMjItLjQ4Mi43MzItLjQ4MiAxLjIzMnMuMTYuOTExLjQ4MiAxLjIzMmMuMzIxLjMyMi43MzIuNDgzIDEuMjMyLjQ4M3MuOTEtLjE2MSAxLjIzMi0uNDgzYy4zMjEtLjMyLjQ4Mi0uNzMyLjQ4Mi0xLjIzMnMtLjE2LS45MS0uNDgyLTEuMjMyem0xLjEyNSAxNC42Nzh2LS41OWgtMS4xNzlWOS42NDRIOS42NDN2LjY0M2gxLjE3OXY4LjU3SDkuNjQzdi41OWg0LjcxNHoiIGZpbGw9IiNGRkYiLz48L3N2Zz4=');
  }
}
.toast-frame-warn {
  &::before {
    content: '';
    background-image: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTMuNTIgMy41MkM1Ljg2NCAxLjE3MiA4LjY5MSAwIDEyIDBjMy4zMDggMCA2LjEzNSAxLjE3MyA4LjQ4IDMuNTJDMjIuODI4IDUuODY0IDI0IDguNjkxIDI0IDEyYzAgMy4zMDgtMS4xNzMgNi4xMzUtMy41MiA4LjQ4QzE4LjEzNiAyMi44MjggMTUuMzA5IDI0IDEyIDI0Yy0zLjMwOCAwLTYuMTM1LTEuMTczLTguNDgtMy41MkMxLjE3MiAxOC4xMzYgMCAxNS4zMDkgMCAxMmMwLTMuMzA4IDEuMTczLTYuMTM1IDMuNTItOC40OHpNMTMuMzg0IDE4di0yLjMwOGgtMi43N1YxOGgyLjc3em0wLTUuMDc3VjUuNTM4aC0yLjc3djcuMzg1aDIuNzd6IiBmaWxsPSIjRkZGIi8+PC9zdmc+');
  }
}
.toast-frame-error {
  &::before {
    content: '';
    background-image: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHBhdGggZD0iTTMuNDgyIDMuNTM2QzUuODQgMS4xNzkgOC42OCAwIDEyIDBjMy4zMjEgMCA2LjE1MiAxLjE3IDguNDkxIDMuNTA5QzIyLjgzMSA1Ljg0OSAyNCA4LjY3OSAyNCAxMmMwIDMuMzIxLTEuMTcgNi4xNTItMy41MDkgOC40OTFDMTguMTUxIDIyLjgzMSAxNS4zMjEgMjQgMTIgMjRjLTMuMzIxIDAtNi4xNTItMS4xNy04LjQ5MS0zLjUwOUMxLjE2OSAxOC4xNTEgMCAxNS4zMjEgMCAxMmMwLTMuMzIxIDEuMTYtNi4xNDMgMy40ODItOC40NjR6bTE0LjMwNCAxMi40ODJMMTMuNzY4IDEybDQuMDcxLTQuMDE4Yy4yMTUtLjIxNC4yMTUtLjQyOCAwLS42NDNsLTEuMTc4LTEuMTI1Yy0uMTA3LS4xMDctLjIxNS0uMTYtLjMyMi0uMTYtLjA3MSAwLS4xNi4wNTMtLjI2OC4xNkwxMiAxMC4xOCA3LjkyOSA2LjIxNGMtLjEwOC0uMTA3LS4xOTctLjE2LS4yNjgtLjE2LS4xMDcgMC0uMjE1LjA1My0uMzIyLjE2TDYuMjE0IDcuMzRjLS4yMTQuMjE1LS4yMTQuNDI5IDAgLjY0M0wxMC4yODYgMTJsLTQuMDcyIDQuMDcxYy0uMDcxLjAzNi0uMTA3LjEyNS0uMTA3LjI2OCAwIC4xNDMuMDM2LjI1LjEwNy4zMjJsMS4xMjUgMS4xNzhjLjA3Mi4wNzIuMTc5LjEwNy4zMjIuMTA3LjE0MyAwIC4yNS0uMDM1LjMyMS0uMTA3TDEyIDEzLjc2OGw0LjA3MSA0LjAxOGMuMTA4LjEwNy4xOTcuMTYuMjY4LjE2LjEwNyAwIC4yMTUtLjA1My4zMjItLjE2bDEuMTI1LTEuMTI1YS4zNi4zNiAwIDAgMCAuMTYtLjMyMmMwLS4xMDctLjA1My0uMjE0LS4xNi0uMzIxeiIgZmlsbD0iI0ZGRiIgZmlsbC1ydWxlPSJub256ZXJvIi8+PC9zdmc+');
  }
}
@media all and (min-width: 769px) {
  .toast-frame {
    text-align: initial;
    display: flex;
    align-items: center;
    &::before {
      flex-grow: 0;
      flex-shrink: 0;
      margin: 0 1em 0 0;
    }
  }
}
.vue-toast-enter-active,
.vue-toast-leave-active {
  transition-property: visibility, opacity;
  transition-duration: 0.25s;
  .toast-frame {
    transition-property: transform;
    transition-duration: 0.25s;
    transition-timing-function: ease-in-out;
  }
}
.vue-toast-enter {
  opacity: 0;
  visibility: hidden;
  .toast-frame {
    transform: scale(1.1);
  }
}
.vue-toast-leave-to {
  opacity: 0;
  visibility: hidden;
  .toast-frame {
    transform: scale(0.9);
  }
}
.vue-toast-leave,
.vue-toast-enter-to {
  opacity: 1;
  visibility: visible;
  .toast-frame {
    transform: scale(1);
  }
}
</style>
