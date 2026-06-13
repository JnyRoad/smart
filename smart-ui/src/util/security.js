export const getSecurityEncodeKey = () => {
  const runtimeConfig = typeof window !== 'undefined' ? window.__SMART_CONFIG__ : null
  const runtimeKey = runtimeConfig && runtimeConfig.securityEncodeKey
  const envKey = process.env.VUE_APP_SECURITY_ENCODE_KEY
  const key = runtimeKey || envKey
  if (!key) {
    throw new Error('securityEncodeKey is not configured in public/config.js or VUE_APP_SECURITY_ENCODE_KEY')
  }
  return key
}
