import CryptoJS from 'crypto-js'
import AES from 'crypto-js/aes'
import PadZeroPadding from 'crypto-js/pad-zeropadding'

export const getSecurityEncodeKey = () => {
  const runtimeConfig = typeof window !== 'undefined' ? window.__SMART_CONFIG__ : null
  const runtimeKey = runtimeConfig && runtimeConfig.securityEncodeKey
  const envKey = process.env.VUE_APP_SECURITY_ENCODE_KEY
  const key = runtimeKey || envKey
  if (!key) {
    throw new Error('VUE_APP_SECURITY_ENCODE_KEY is not configured')
  }
  return key
}

/**
 *加密处理
 */
export default params => {
  let { data, type, param, key } = params
  const result = JSON.parse(JSON.stringify(data))
  if (type === 'Base64') {
    param.forEach(ele => {
      result[ele] = window.btoa(result[ele])
    })
  } else {
    const encodeKey = key || getSecurityEncodeKey()
    param.forEach(ele => {
      var data = result[ele]
      const parsedKey = CryptoJS.enc.Latin1.parse(encodeKey)
      var iv = parsedKey
      // 加密
      var encrypted = AES.encrypt(data, parsedKey, {
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: PadZeroPadding
      })
      result[ele] = encrypted.toString()
    })
  }
  return result
}

/**
 *解密处理，跟上面加密处理，不是对应的
 */
export const decryption = (str) => {
  const key = CryptoJS.enc.Utf8.parse(getSecurityEncodeKey())
  const options = {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7
  }
  const decrypted = CryptoJS.AES.decrypt(CryptoJS.enc.Base64.stringify(CryptoJS.enc.Hex.parse(str)), key, options).toString(CryptoJS.enc.Utf8)
  return decrypted.toString()
}
