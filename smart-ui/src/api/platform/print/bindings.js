import { printRequest } from './client'
const idPath = value => encodeURIComponent(value)
/** 绑定由系统维护，职级字典只读服务器已确认的数据。 */
export const listBindings = params => printRequest({ url: '/bindings', method: 'get', params })
export const employeeGrades = parkId => printRequest({ url: '/binding-options/employee-grades', method: 'get', params: { parkId } })
export const resolveBinding = params => printRequest({ url: '/bindings/resolve', method: 'get', params })
export const saveBinding = (id, data, key) => printRequest({ url: id ? `/bindings/${idPath(id)}` : '/bindings', method: id ? 'patch' : 'post', params: { parkId: data.parkId }, data, headers: { 'Idempotency-Key': key } })
export const disableBinding = (id, data, key) => printRequest({ url: `/bindings/${idPath(id)}/disable`, method: 'post', params: { parkId: data.parkId }, data, headers: { 'Idempotency-Key': key } })
