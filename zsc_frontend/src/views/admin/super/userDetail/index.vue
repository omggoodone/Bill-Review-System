<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <span class="header-title">{{ pageTitle }}</span>
        <el-tag type="primary" size="large" effect="plain">{{ roleLabel }}</el-tag>
      </div>
    </el-card>

    <!-- 搜索表单 -->
    <div class="toolbar-row">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="search-form">
        <el-form-item prop="userName">
          <template #label><svg-icon icon-class="user" /></template>
          <el-input v-model="queryParams.userName" placeholder="用户名" clearable style="width: 120px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="status">
          <template #label><svg-icon icon-class="status" /></template>
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 100px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" circle @click="handleQuery" />
          <el-button type="primary" circle @click="resetQuery"><svg-icon icon-class="reset" /></el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 用户列表（只读） -->
    <el-table v-loading="loading" :data="userList" style="width: 100%">
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="用户名" align="center" prop="userName" width="120" />
      <el-table-column label="昵称" align="center" prop="nickName" width="120" :show-overflow-tooltip="true" />
      <el-table-column label="邮箱" align="center" prop="email" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="手机号" align="center" prop="phonenumber" width="130" />
      <el-table-column label="角色" align="center" width="120">
        <template #default="scope">
          <el-tag v-if="scope.row.roles" v-for="role in scope.row.roles" :key="role.roleId" :type="getRoleTagType(role.roleKey)" size="small" style="margin: 2px">
            {{ role.roleName }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'danger'" size="small">
            {{ scope.row.status === '0' ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.currentPage"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="AdminUserDetail">
import { ref, computed, onMounted, getCurrentInstance } from "vue"
import { useRoute } from "vue-router"
import { listUser } from "@/api/biz/admin"

const { proxy } = getCurrentInstance()
const route = useRoute()

// type: 'all' | 'user' | 'reviewer' | 'admin'
const type = computed(() => {
  const t = route.query.type
  return ['user', 'reviewer', 'admin'].includes(t) ? t : 'all'
})

const pageTitle = computed(() => {
  const map = { all: '全部用户', user: '普通用户', reviewer: '审核员', admin: '管理员' }
  return map[type.value] || '用户列表'
})

const roleLabel = computed(() => {
  const map = { all: '全部', user: '普通用户', reviewer: '审核员', admin: '管理员' }
  return map[type.value] || '全部'
})

const userList = ref([])
const loading = ref(true)
const total = ref(0)

const queryParams = ref({
  currentPage: 1,
  pageSize: 10,
  userName: undefined,
  roleKey: undefined,
  status: undefined
})

function getRoleTagType(roleKey) {
  const map = { user: '', reviewer: 'success', admin_user: 'danger' }
  return map[roleKey] || 'info'
}

function parseTime(time) {
  if (!time) return "-"
  return proxy.parseTime(time)
}

function getList() {
  loading.value = true
  // Set roleKey based on type
  if (type.value === 'user') queryParams.value.roleKey = 'user'
  else if (type.value === 'reviewer') queryParams.value.roleKey = 'reviewer'
  else if (type.value === 'admin') queryParams.value.roleKey = 'admin_user'
  else queryParams.value.roleKey = undefined

  listUser(queryParams.value).then(response => {
    userList.value = response.data.list
    total.value = response.data.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.currentPage = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryFormRef")
  queryParams.value.userName = undefined
  queryParams.value.status = undefined
  queryParams.value.currentPage = 1
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.header-card { margin-bottom: 16px; }
.header-content { display: flex; align-items: center; gap: 12px; }
.header-title { font-size: 18px; font-weight: 600; color: var(--el-text-color-primary); }
.toolbar-row { margin-bottom: 10px; display: flex; align-items: center; }
.search-form :deep(.el-form-item) { margin-right: 4px; margin-bottom: 0; }
.search-form :deep(.el-form-item__label) { display: flex; align-items: center; }
</style>
