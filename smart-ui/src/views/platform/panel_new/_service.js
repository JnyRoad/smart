/**
 * API-LIST
 */
import request from "@/router/axios";
const baseUrl = '/platform'

export const api = {
  /**
   * 查询用户所有园区
   */
  allPark () {
    return request({
      url: `${baseUrl}/park/all`,
      method: 'get'
    })
  },
  /**
   * 园区概括---获取基本的统计信息
  */
  getStatistics(parkId) {
    return request({
      url: `${baseUrl}/park/statistics/${parkId}`,
      method: "get"
    });
  },
  /**
   * 园区概括---车位动态
   * @param {*} parkId
  */
  getCorrectionCount(parkId) {
    return request({
      url: `${baseUrl}/bigdata/park/parking/${parkId}`,
      method: "get"
    });
  },
   /**
   * 园区概括---宿舍动态
  */
 getDormitory(parkId) {
  return request({
    url: `${baseUrl}/bigdata/park/dormitory/${parkId}`,
    method: "get"
  });
},
  /**
   * 人员出入---设备显示
  */
  getVisitorNew (parkId) {
    return request({
      url: `${baseUrl}/bigdata/park/areadevice/snap/${parkId}`,
      method: 'get'
    })
  },
  /**
   * 人员出入---访客分析
  */
  getVisitorAnalysis (parkId) {
    return request({
      url: `${baseUrl}/visitor/searchVisitorAnalysisToday/${parkId}`,
      method: 'get'
    })
  },
  /**
   * 人员出入---车辆进入时段分析
  */
  getVehicleCount (parkId) {
    return request({
      url: `${baseUrl}/snap/vehicle/count/${parkId}`,
      method: 'get'
    })
  },
  /**
   * 人员出入---访客实时
  */
 getVisitorRealTime (parkId) {
    return request({
      url: `${baseUrl}/bigdata/park/visitor/${parkId}`,
      method: 'get'
    })
  }
}
