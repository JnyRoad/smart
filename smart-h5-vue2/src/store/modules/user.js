/**
 * 用户相关
 */
import { getStore, setStore } from '@/util/store'
const user = {
  state: {
    userInfo: getStore({
      name: 'userInfo'
    }),
    userInfoBase: getStore({
      name: 'userInfoBase'
    }),
    parkInfo: getStore({
      name: 'parkInfo'
    }),
    expires_in: getStore({
      name: 'expires_in'
    }) || '',
    access_token: getStore({
      name: 'access_token'
    }) || '',
    refresh_token: getStore({
      name: 'refresh_token'
    }) || ''
  },
  actions: {
    /**
     * 获取用户信息
     * @param {*} param0
     */
    GetUserInfo ({ commit }) {
      return new Promise((resolve, reject) => {
        // getUserInfo()
        //   .then(res => {
        //     const data = res.data.data || {}
        //     commit('SET_USERIFNO', data.userDTO)
        //     resolve(data)
        //   })
        //   .catch(err => {
        //     reject(err)
        //   })
      })
    }
  },
  mutations: {
    SET_ACCESS_TOKEN: (state, accessToken) => {
      state.access_token = accessToken
      setStore({ name: 'access_token', content: state.access_token })
    },
    SET_EXPIRES_IN: (state, expiresIn) => {
      state.expires_in = expiresIn
      setStore({ name: 'expires_in', content: state.expires_in })
    },
    SET_REFRESH_TOKEN: (state, rfToken) => {
      state.refresh_token = rfToken
      setStore({ name: 'refresh_token', content: state.refresh_token })
    },
    SET_USERIFNO: (state, userInfo) => {
      state.userInfo = userInfo
      setStore({ name: 'userInfo', content: state.userInfo })
    },
    SET_USERIFNOBASE: (state, userInfoBase) => {
      state.userInfoBase = userInfoBase
      setStore({ name: 'userInfoBase', content: state.userInfoBase })
    },
    SET_PARKINFO: (state, parkInfo) => {
      state.parkInfo = parkInfo
      setStore({ name: 'parkInfo', content: state.parkInfo })
    }
  }
}
export default user
