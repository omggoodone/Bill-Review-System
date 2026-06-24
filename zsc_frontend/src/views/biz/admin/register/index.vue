<template>
  <div class="app-container">
    <el-table v-loading="loading" :data="requestList">
      <el-table-column label="邮箱" align="center" prop="email" min-width="180" />
      <el-table-column label="申请说明" align="center" prop="note" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="申请角色" align="center" prop="roleKey" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.roleKey === 'reviewer' ? 'success' : ''" size="small">
            {{ scope.row.roleKey === 'reviewer' ? '审核员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="申请时间" align="center" prop="createTime" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-tooltip content="通过" placement="top">
            <el-button link type="success" icon="Select" @click="handleApprove(scope.row)" />
          </el-tooltip>
          <el-tooltip content="拒绝" placement="top">
            <el-button link type="danger" icon="CloseBold" @click="handleReject(scope.row)" />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <!-- 通过结果展示 -->
    <el-dialog v-model="resultOpen" title="审批通过" width="450px" append-to-body :close-on-click-modal="false">
      <el-alert title="请记录以下信息并转交用户" type="success" :closable="false" style="margin-bottom: 16px;" />
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ resultCreds.username }}</el-descriptions-item>
        <el-descriptions-item label="初始密码">{{ resultCreds.password }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="resultOpen = false">已记录</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因 -->
    <el-dialog v-model="rejectOpen" title="拒绝申请" width="400px" append-to-body>
      <el-form>
        <el-form-item label="拒绝原因（选填）">
          <el-input v-model="rejectComment" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectOpen = false">取消</el-button>
        <el-button type="danger" @click="submitReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listRegisterRequests, approveRegisterRequest, rejectRegisterRequest } from '@/api/biz/admin'

const { proxy } = getCurrentInstance()
const requestList = ref([])
const loading = ref(false)
const resultOpen = ref(false)
const rejectOpen = ref(false)
const rejectComment = ref('')
const currentRow = ref({})
const resultCreds = ref({ username: '', password: '' })

function parseTime(t) {
  if (!t) return '-'
  return proxy.parseTime(t)
}

function loadList() {
  loading.value = true
  listRegisterRequests().then(res => {
    requestList.value = res.data || []
  }).finally(() => { loading.value = false })
}

function handleApprove(row) {
  currentRow.value = row
  approveRegisterRequest(row.id, '').then(res => {
    resultCreds.value = { username: res.data.generatedUsername, password: res.data.generatedPassword }
    resultOpen.value = true
    loadList()
  })
}

function handleReject(row) {
  currentRow.value = row
  rejectComment.value = ''
  rejectOpen.value = true
}

function submitReject() {
  rejectRegisterRequest(currentRow.value.id, rejectComment.value).then(() => {
    proxy.$modal.msgSuccess('已拒绝')
    rejectOpen.value = false
    loadList()
  })
}

onMounted(() => loadList())
</script>
