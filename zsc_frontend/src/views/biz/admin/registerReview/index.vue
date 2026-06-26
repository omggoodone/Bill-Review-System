<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <span class="header-title">注册申请审核</span>
        <el-tag type="warning" size="large" effect="plain">待审核</el-tag>
      </div>
    </el-card>

    <!-- 注册申请列表 -->
    <el-table v-loading="loading" :data="requestList" style="width: 100%">
      <el-table-column label="邮箱" prop="email" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="申请角色" prop="roleKey" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.roleKey === 'reviewer'" type="warning" size="small">审核员</el-tag>
          <el-tag v-else-if="scope.row.roleKey === 'user'" type="primary" size="small">普通用户</el-tag>
          <el-tag v-else size="small">{{ scope.row.roleKey }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请说明" prop="note" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="申请时间" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="success" icon="Select" size="small" @click="handleApprove(scope.row)">通过</el-button>
          <el-button link type="danger" icon="CloseBold" size="small" @click="handleReject(scope.row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && !requestList.length" description="暂无待审核的注册申请" :image-size="100" />
  </div>
</template>

<script setup name="AdminRegisterReview">
import { ref, onMounted, getCurrentInstance } from "vue"
import { ElMessage, ElMessageBox } from "element-plus"
import { listRegisterRequests, approveRegisterRequest, rejectRegisterRequest } from "@/api/biz/admin"

const { proxy } = getCurrentInstance()
const requestList = ref([])
const loading = ref(false)

function parseTime(time) {
  if (!time) return '-'
  return proxy.parseTime(time)
}

function getList() {
  loading.value = true
  listRegisterRequests().then(res => {
    requestList.value = res.data || []
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function handleApprove(row) {
  ElMessageBox.prompt('审批意见（可选）', '通过注册申请', {
    confirmButtonText: '确认通过',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '输入审批意见...'
  }).then(({ value }) => {
    approveRegisterRequest(row.id, value || '').then(() => {
      ElMessage.success('已通过')
      getList()
    })
  }).catch(() => {})
}

function handleReject(row) {
  ElMessageBox.prompt('请输入拒绝原因', '拒绝注册申请', {
    confirmButtonText: '确认拒绝',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入拒绝原因...',
    inputValidator: (val) => val ? true : '拒绝原因不能为空'
  }).then(({ value }) => {
    rejectRegisterRequest(row.id, value).then(() => {
      ElMessage.success('已拒绝')
      getList()
    })
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.header-card { margin-bottom: 16px; }
.header-content { display: flex; align-items: center; gap: 12px; }
.header-title { font-size: 18px; font-weight: 600; color: var(--el-text-color-primary); }
</style>
