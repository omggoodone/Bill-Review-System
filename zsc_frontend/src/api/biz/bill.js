import request from '@/utils/request'

// 各类别金额汇总
export function getCategorySummary() {
  return request({
    url: '/api/bill/category-summary',
    method: 'get'
  })
}

// 本月提交趋势
export function getTrend() {
  return request({
    url: '/api/bill/trend',
    method: 'get'
  })
}

// 查询票据列表（分页 + 条件）
export function listBill(query, signal) {
  return request({
    url: '/api/bill/query',
    method: 'post',
    data: query,
    headers: { repeatSubmit: false },
    signal
  })
}

// 查询票据详情（含附件列表 + 审批记录）
export function getBill(id) {
  return request({
    url: '/api/bill/' + id,
    method: 'get'
  })
}

// 新增票据
export function addBill(data) {
  return request({
    url: '/api/bill',
    method: 'post',
    data: data
  })
}

// 修改票据
export function updateBill(data) {
  return request({
    url: '/api/bill',
    method: 'put',
    data: data
  })
}

// 提交票据（草稿 → 已提交）
export function submitBill(id) {
  return request({
    url: '/api/bill/submit/' + id,
    method: 'post'
  })
}

// 删除票据（仅草稿可删）
export function delBill(id) {
  return request({
    url: '/api/bill/' + id,
    method: 'delete'
  })
}

// 审批票据（通过 / 退回）
export function reviewBill(data) {
  return request({
    url: '/api/bill/review',
    method: 'post',
    data: data
  })
}
