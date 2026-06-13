
import request from '@/router/axios'
export const returnApi = {
  getList (data) {
    return request({
      url: '/platform/news/terminal/page',
      method: 'get',
      params: data
    })
  },
  getInfoSelect (page) {
    return request({
      url: '/platform/news/details/page',
      method: 'post',
      params: page,
      data: {
        status: null,
        infoName: null,
        type: null
      }
    })
  },
  saveSubmit(obj){
    return request({
      url: '/platform/news/terminal/edit',
      method: 'post',
      data: obj
    })
  },
  deleteData(id){
    return request({
      url: `/platform/news/terminal/${id}`,
      method: 'post'
    })
  }
}