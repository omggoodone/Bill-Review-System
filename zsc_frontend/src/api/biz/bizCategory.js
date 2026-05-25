import request from '@/utils/request'

// 查询业务类别列表
export function listBizCategory(query) {
  return request({
    url: '/api/category/query',
    method: 'post',
    data: query,
    headers: { repeatSubmit: false }
  })
}

// 查询业务类别详细
export function getBizCategory(categoryId) {
  return request({
    url: '/api/category/' + categoryId,
    method: 'get'
  })
}

// 新增业务类别
export function addBizCategory(data) {
  return request({
    url: '/api/category',
    method: 'post',
    data: data
  })
}

// 修改业务类别
export function updateBizCategory(data) {
  return request({
    url: '/api/category',
    method: 'put',
    data: data
  })
}

// 删除业务类别
export function delBizCategory(categoryId) {
  return request({
    url: '/api/category/' + categoryId,
    method: 'delete'
  })
}
