
import request from '@/router/axios'
export const returnApi = {
  getFileStream(id){ //文件流
    return request({
      url: `/platform/news/file/stream/${id}`,
      method: 'get',
      responseType: 'blob',
      headers: {
        'Content-Type': 'multipart/form-data',
        Authorization: localStorage.userToken
      },
      withCredentials: true
    })
  },
  getFileDownload(id){ //文件下载
    return request({
      url: `/platform/news/file/download/${id}`,
      method: 'get'
    })
  },
}