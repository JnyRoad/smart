const getters = {
  userInfo: state => state.user.userInfo,
  userInfoBase: state => state.user.userInfoBase,
  parkInfo: state => state.user.parkInfo,
  access_token: state => state.user.access_token,
  refresh_token: state => state.user.refresh_token,
  expires_in: state => state.user.expires_in
}
export default getters
