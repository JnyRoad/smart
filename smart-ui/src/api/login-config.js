const DEFAULT_LOGIN_TENANT_ID = '1'
const DEFAULT_LOGIN_AUTHORIZATION = 'Basic c21hcnQ6c21hcnQ='

function getRuntimeLoginConfig() {
  return typeof window !== 'undefined' ? window.__SMART_CONFIG__ || {} : {}
}

export function getLoginTenantId() {
  const runtimeConfig = getRuntimeLoginConfig()
  return runtimeConfig.loginTenantId || process.env.VUE_APP_LOGIN_TENANT_ID || DEFAULT_LOGIN_TENANT_ID
}

export function getLoginAuthorization() {
  const runtimeConfig = getRuntimeLoginConfig()
  return runtimeConfig.loginAuthorization || process.env.VUE_APP_LOGIN_AUTHORIZATION || DEFAULT_LOGIN_AUTHORIZATION
}

export function createLoginHeaders(extraHeaders = {}) {
  return {
    ...extraHeaders,
    TENANT_ID: getLoginTenantId(),
    Authorization: getLoginAuthorization()
  }
}
