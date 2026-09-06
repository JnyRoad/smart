import { printRequest } from './client'
const idPath = value => encodeURIComponent(value)
export const listPrinters = params => printRequest({ url: '/printer-profiles', method: 'get', params })
export const getPrinter = id => printRequest({ url: `/printer-profiles/${idPath(id)}`, method: 'get' })
export const savePrinter = (id, data, key) => printRequest({ url: `/printer-profiles${id ? '/' + idPath(id) : ''}`, method: id ? 'patch' : 'post', data, headers: { 'Idempotency-Key': key } })
export const disablePrinter = (id, data, key) => printRequest({ url: `/printer-profiles/${idPath(id)}/disable`, method: 'post', data, headers: { 'Idempotency-Key': key } })

export const listPrinterOptions = params => printRequest({ url: '/printer-profiles/options', method: 'get', params })
