
import request from '@/router/axios'
export const returnApi = {
  getList (page, query) {
    return request({
      url: '/platform/news/details/page',
      method: 'post',
      params: page,
      data: query
    })
  },
  getSelectStatus(){
    return request({
      url: `/platform/news/details/time/type`,
      method: 'get'
    })
  },
  getSelectType(){
    return request({
      url: `/platform/news/details/info/type`,
      method: 'get'
    })
  },
  getDetail(id){
    return request({
      url: `/platform/news/details/${id}`,
      method: 'get'
    })
  },
  importImgs (obj) {
    return request({
      url: '/platform/image/save',
      method: 'post',
      data: obj
    })
  },
  fileUpload(data){
    return request({
      url: '/platform/news/file/upload',
      method: 'post',
      data: data,
      headers: { 'content-type': 'multipart/form-data' },
      responseType: 'arraybuffer',
      timeout: 1000*60*5
    })
  },
  saveSubmit(obj){
    return request({
      url: '/platform/news/details/edit',
      method: 'post',
      data: obj
    })
  },
  deleteData(id){
    return request({
      url: `/platform/news/details/${id}`,
      method: 'post'
    })
  }

}