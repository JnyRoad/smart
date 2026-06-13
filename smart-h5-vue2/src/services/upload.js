// 图片上传
import http from '@/util/http'
// const authorization = true // 授权接口

/**
 * 通用图片上传
 * @param {Object} data
 */
export const postImageSave = function (uploadData) {
  let base64 = uploadData.base64.split(',')[1]
  let obj = { visitorPhoto: base64 }
  return http.request({
    type: 'post',
    module: 'app',
    url: '/wechat/visit/checkFace',
    data: obj
  }, {
    authorization: false
  })
}
/**
 * 人脸识别并裁剪
 * @param {Object} data
 */
export const checkFaceAndCut = function (uploadData) {
  let base64 = uploadData.base64.split(',')[1]
  let obj = { imageData: base64 }
  return http.request({
    type: 'post',
    module: 'algorithm',
    url: '/out/face/cut',
    data: obj
  }, {
    authorization: false
  })
}
/**
 * 人脸采集
 * @param {Object} data
 */
export const postFaceImageSave = function ({
  imgFile,
  employeeId
}) {
  const data = new FormData()
  data.append('file', imgFile)
  data.append('employeeId', employeeId)
  return http.request({
    type: 'post',
    module: 'workbench',
    url: '/employee/image/import',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: data
  })
}
