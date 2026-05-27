import request from '@/utils/request'

// 获取管理员统计数据
export function getAdminStats() {
  return request({ url: '/api/admin/stats', method: 'get' })
}

// 查询用户列表（含角色信息，分页）
export function listUser(query) {
  return request({ url: '/api/admin/users', method: 'get', params: query })
}

// 修改用户（角色分配）
export function updateUser(data) {
  return request({ url: '/system/user', method: 'put', data })
}

// 重置密码
export function resetUserPwd(userId, password) {
  return request({ url: '/system/user/resetPwd', method: 'put', data: { userId, password } })
}

// 修改用户状态
export function changeUserStatus(userId, status) {
  return request({ url: '/system/user/changeStatus', method: 'put', data: { userId, status } })
}

// 获取角色列表（用于下拉选择）
export function listRole() {
  return request({ url: '/api/admin/roles', method: 'get' })
}

// 获取待审核注册申请
export function listRegisterRequests() {
  return request({ url: '/api/admin/register-requests', method: 'get' })
}

// 通过注册申请
export function approveRegisterRequest(id, comment) {
  return request({ url: `/api/admin/register-requests/${id}/approve`, method: 'post', data: { comment } })
}

// 审核员工作量统计
export function getReviewerWorkload() {
  return request({ url: '/api/admin/reviewer-workload', method: 'get' })
}

// 普通用户已通过金额汇总
export function getUserAmountSummary() {
  return request({ url: '/api/admin/user-amount-summary', method: 'get' })
}

// 创建管理员账号（超管直接创建）
export function createAdmin(data) {
  return request({ url: '/api/admin/create-admin', method: 'post', data })
}

// 拒绝注册申请
export function rejectRegisterRequest(id, comment) {
  return request({ url: `/api/admin/register-requests/${id}/reject`, method: 'post', data: { comment } })
}
