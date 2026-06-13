// 图片上传
import http from '@/util/http'
const authorization = false // 授权接口

/**
 * 通用图片上传
 * @param {Object} data
 */
export const postImageSave = function (uploadData) {
  return http.request({
    type: 'post',
    module: 'file',
    url: '/file/upload',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    data: uploadData.formData
  }, {
    authorization: authorization
  })
}

/**
 * 人脸照图片上传
 * @param {Object} data Face photo
 */
export const facePhotoImageSave = function (data) {
  return http.request({
    type: 'post',
    module: 'visitor',
    url: '/file/upload/face',
    headers: {
      'Content-Type': 'multipart/form-data'
    },

    // path: data.path
    data: data.formData

  }, {
    authorization: authorization
  })
}
/**
 * 其他图片图片上传
 * @param {Object} data
 */
export const otherImageSave = function (data) {
  return http.request({
    type: 'post',
    module: 'visitor',
    url: '/file/upload/file',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    // data: data.path
    data: data.formData

  }, {
    authorization: authorization
  })
}
/**
 * 证件图片图片上传
 * @param {Object} data
 */
export const cardImageSave = function (data) {
  const data2 = new FormData()
  data2.append('enterpriseId', data.enterpriseId)
  data2.append('parkId', data.parkId)
  data2.append('file', data.obj.file)
  return http.request({
    type: 'post',
    module: 'visitor',
    url: '/file/upload/card',
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    // data: data.path
    data: data2

  }, {
    authorization: authorization
  })
}
