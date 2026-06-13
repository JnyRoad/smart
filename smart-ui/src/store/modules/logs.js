import { getStore, setStore } from '@/util/store'
import { dateFormat } from '@/filters/'
import { sendLogs } from '@/api/admin/log'

const logs = {
  state: {
    logsList: getStore({ name: 'logsList' }) || []
  },
  actions: {
    // 发送错误日志
    SendLogs ({ state, commit }) {
      return new Promise((resolve, reject) => {
        sendLogs(state.logsList).then(() => {
          commit('CLEAR_LOGS')
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    }
  },
  mutations: {
    ADD_LOGS: (state, { type, message, stack, info }) => {
      state.logsList.push(Object.assign({
        url: window.location.href,
        time: dateFormat(new Date())
      }, {
        type,
        message,
        stack,
        info: info.toString()
      }))
      // Cap the persisted list: the unhandledrejection listener (CR-C14-002) raises
      // volume, and re-persisting an unbounded array would hit the localStorage quota.
      if (state.logsList.length > 100) {
        state.logsList = state.logsList.slice(-100)
      }
      setStore({ name: 'logsList', content: state.logsList })
    },
    CLEAR_LOGS: (state) => {
      state.logsList = []
      setStore({ name: 'logsList', content: state.logsList })
    }
  }

}

export default logs
