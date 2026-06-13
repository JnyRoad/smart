import request from "@/router/axios";

/**
 * 获取基本的统计信息
 * @param {id} query
 */
export function getStatistics(query = {}) {
  return request({
    url: `/platform/park/statistics/${query.id}`,
    method: "get"
  });
}

/**
 * 获取车辆统计数据
 * @param {*} query
 */
export function getVehicleCount(query = {}) {
  return request({
    url: "/platform/vehicle/count",
    method: "get",
    params: query
  });
}

/**
 * 获取车位统计数据
 * @param {*} query
 */
export function getCorrectionCount(query = {}) {
  return request({
    url: "/platform/parking/correction/count",
    method: "get",
    params: query
  });
}
