<template>
  <div class="app-container">
    <!-- 新增管理员弹窗 -->
    <el-dialog v-model="createDialogVisible" title="新增管理员" width="420px" :close-on-click-modal="false">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="80px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="请输入管理员邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreate">确认创建</el-button>
      </template>
    </el-dialog>

    <!-- 工具栏行：批量操作 + 搜索表单 -->
    <div class="toolbar-row">
      <el-button v-if="isSuperAdmin" type="danger" icon="Plus" style="margin-right: 12px;" @click="openCreateDialog">新增管理员</el-button>
      <el-button type="success" plain icon="Select" :disabled="ids.length === 0" @click="handleBatchStatus('0')">批量启用</el-button>
      <el-button type="warning" plain icon="CloseBold" :disabled="ids.length === 0" @click="handleBatchStatus('1')">批量停用</el-button>
      <el-button type="danger" plain icon="Delete" :disabled="ids.length === 0" style="margin-right: 16px;" @click="handleBatchDelete">批量删除</el-button>
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="search-form">
        <el-form-item prop="userName">
          <template #label><svg-icon icon-class="search" /></template>
          <el-input v-model="queryParams.userName" placeholder="用户名" clearable style="width: 130px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item v-if="!isSuperAdmin" prop="roleKey">
          <template #label><svg-icon icon-class="peoples" /></template>
          <el-select v-model="queryParams.roleKey" placeholder="角色" clearable style="width: 100px">
            <el-option label="普通用户" value="user" />
            <el-option label="审核员" value="reviewer" />
          </el-select>
        </el-form-item>
        <el-form-item prop="status">
          <template #label><svg-icon icon-class="switch" /></template>
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 100px">
            <el-option v-for="d in sys_normal_disable" :key="d.value" :label="d.label" :value="d.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" circle native-type="button" @click="handleQuery" />
          <el-button type="primary" circle native-type="button" @click="resetQuery"><svg-icon icon-class="reset" /></el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="用户名" align="center" prop="userName" min-width="120" />
      <el-table-column label="邮箱" align="center" prop="email" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="手机" align="center" prop="phonenumber" width="130" />
      <el-table-column label="角色" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.roles?.[0]?.roleKey === 'admin_user'" type="danger" size="small">管理员</el-tag>
          <el-tag v-else-if="scope.row.roles?.[0]?.roleKey === 'reviewer'" type="success" size="small">审核员</el-tag>
          <el-tag v-else size="small">普通用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="80">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope"><span>{{ parseTime(scope.row.createTime) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-tooltip content="重置密码" placement="top"><el-button link type="primary" icon="Lock" @click="handleResetPwd(scope.row)" v-hasPermi="['system:user:resetPwd']" /></el-tooltip>
          <el-tooltip :content="scope.row.status === '0' ? '停用' : '启用'" placement="top"><el-button link :type="scope.row.status === '0' ? 'warning' : 'success'" :icon="scope.row.status === '0' ? 'VideoPause' : 'VideoPlay'" @click="handleStatus(scope.row)" v-hasPermi="['system:user:edit']" /></el-tooltip>
          <el-tooltip content="删除" placement="top"><el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:user:remove']" /></el-tooltip>
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

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import useUserStore from '@/store/modules/user'
import { listUser, createAdmin, resetUserPwd, changeUserStatus } from '@/api/biz/admin'
import { delUser } from '@/api/system/user'

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict('sys_normal_disable')
const userStore = useUserStore()

const isSuperAdmin = computed(() => userStore.roles.includes('admin'))

const userList = ref([])
const loading = ref(false)
const total = ref(0)
const ids = ref([])
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref(null)
const createForm = reactive({ email: '' })
const createRules = {
  email: [
    { required: true, trigger: 'blur', message: '请输入邮箱' },
    { pattern: /^\S+@\S+\.\S+$/, trigger: 'blur', message: '邮箱格式不正确' }
  ]
}

const queryParams = ref({ currentPage: 1, pageSize: 10, userName: undefined, roleKey: undefined, status: undefined })

function parseTime(t) { return t ? proxy.parseTime(t) : '-' }

function getList() {
  loading.value = true
  const raw = queryParams.value
  const params = {}
  // 超管只看管理员账号，管理员排除管理员
  if (isSuperAdmin.value) {
    params.roleKey = 'admin_user'
  } else {
    params.excludeRoleKey = 'admin_user'
  }
  Object.keys(raw).forEach(k => {
    const v = raw[k]
    if (v !== undefined && v !== null && v !== '') {
      params[k] = v
    }
  })
  listUser(params).then(res => {
    userList.value = res.data.list || []
    total.value = res.data.total || 0
  }).finally(() => { loading.value = false })
}

function handleQuery() { queryParams.value.currentPage = 1; getList() }
function resetQuery() {
  queryParams.value = { currentPage: 1, pageSize: 10, userName: undefined, roleKey: undefined, status: undefined }
  handleQuery()
}

function handleResetPwd(row) {
  proxy.$prompt(`请输入「${row.userName}」的新密码（6~20位）`, '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValidator: (v) => (v && v.length >= 6 && v.length <= 20) ? true : '密码长度须在6~20位之间'
  }).then(({ value }) => {
    resetUserPwd(row.userId, value).then(() => { proxy.$modal.msgSuccess('密码重置成功'); getList() })
  })
}

function handleStatus(row) {
  const newStatus = row.status === '0' ? '1' : '0'
  const text = newStatus === '1' ? '停用' : '启用'
  proxy.$modal.confirm(`确认${text}用户「${row.userName}」？`).then(() => {
    changeUserStatus(row.userId, newStatus).then(() => { proxy.$modal.msgSuccess(text + '成功'); getList() })
  })
}

function openCreateDialog() {
  createForm.email = ''
  createDialogVisible.value = true
  createFormRef.value?.resetFields()
}

function handleCreate() {
  createFormRef.value.validate(valid => {
    if (!valid) return
    const email = createForm.email.trim()
    createLoading.value = true
    createAdmin({ email }).then(res => {
    createDialogVisible.value = false
    proxy.$alert(
      `<div style="line-height:2;">
        <p>用户名：<b>${res.data.userName}</b></p>
        <p>密码：<b>${res.data.password}</b></p>
        <p>邮箱：${res.data.email}</p>
      </div>`,
      '管理员账号已创建',
      { dangerouslyUseHTMLString: true, confirmButtonText: '已记录' }
    )
      getList()
    }).finally(() => { createLoading.value = false })
  })
}

function handleSelectionChange(selection) {
  ids.value = selection.map(s => s.userId)
}

function handleBatchStatus(status) {
  const label = status === '0' ? '启用' : '停用'
  proxy.$modal.confirm(`确认批量${label}选中的 ${ids.value.length} 名用户吗？`).then(() => {
    Promise.all(ids.value.map(id => changeUserStatus(id, status))).then(() => {
      proxy.$modal.msgSuccess(`批量${label}成功`)
      ids.value = []
      getList()
    })
  })
}

function handleBatchDelete() {
  proxy.$modal.confirm(`确认批量删除选中的 ${ids.value.length} 名用户吗？此操作不可恢复！`).then(() => {
    Promise.all(ids.value.map(id => delUser(id))).then(() => {
      proxy.$modal.msgSuccess('批量删除成功')
      ids.value = []
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm(`确认删除用户「${row.userName}」？`).then(() => {
    delUser(row.userId).then(() => { proxy.$modal.msgSuccess('删除成功'); getList() })
  })
}

onMounted(() => getList())
</script>

<style scoped lang="scss">
.toolbar-row {
  display: flex; align-items: center; margin-bottom: 10px;
}
.search-form :deep(.el-form-item) { margin-right: 4px; margin-bottom: 0; }
.search-form :deep(.el-form-item__label) { display: flex; align-items: center; }
</style>
