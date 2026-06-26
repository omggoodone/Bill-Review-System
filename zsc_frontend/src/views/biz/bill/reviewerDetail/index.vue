<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <el-card class="header-card" shadow="never">
      <div class="header-content">
        <span class="header-title">{{ pageTitle }}</span>
        <el-tag :type="type === 'approved' ? 'success' : 'danger'" size="large" effect="plain">
          {{ type === 'approved' ? '已通过' : '已退回' }}
        </el-tag>
      </div>
    </el-card>

    <!-- 搜索表单 -->
    <div class="toolbar-row">
      <el-form :model="queryParams" ref="queryFormRef" :inline="true" class="search-form">
        <el-form-item prop="keywords">
          <template #label><svg-icon icon-class="search" /></template>
          <el-input v-model="queryParams.keywords" placeholder="编号/标题" clearable style="width: 130px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="createBy">
          <template #label><svg-icon icon-class="submitter" /></template>
          <el-input v-model="queryParams.createBy" placeholder="提交人" clearable style="width: 90px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="categoryId">
          <template #label><svg-icon icon-class="category" /></template>
          <el-select v-model="queryParams.categoryId" placeholder="类别" clearable style="width: 100px">
            <el-option v-for="cat in categoryOptions" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" circle @click="handleQuery" />
          <el-button type="primary" circle @click="resetQuery"><svg-icon icon-class="reset" /></el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 票据列表（只读） -->
    <el-table v-loading="loading" :data="billList" @sort-change="handleSortChange" style="width: 100%">
      <el-table-column label="票据编号" align="center" prop="billNo" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="180" />
      <el-table-column label="类别" align="center" prop="categoryName" width="100" />
      <el-table-column label="金额" align="center" prop="amount" width="120" sortable="custom">
        <template #default="scope">
          <span>{{ formatAmount(scope.row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提交人" align="center" prop="createBy" width="100" />
      <el-table-column label="提交时间" align="center" prop="createTime" width="160" sortable="custom">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审核人" align="center" prop="auditBy" width="100" />
      <el-table-column label="审核时间" align="center" prop="auditTime" width="160" sortable="custom">
        <template #default="scope">
          <span>{{ parseTime(scope.row.auditTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="80" class-name="small-padding fixed-width" fixed="right">
        <template #default="scope">
          <el-tooltip content="详情" placement="top">
            <el-button link type="primary" icon="Document" @click="handleDetail(scope.row)" v-hasPermi="['biz:bill:query']" />
          </el-tooltip>
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

    <!-- 详情弹窗 -->
    <BillForm
      v-model="detailOpen"
      title="票据详情"
      :formData="detailData"
      :readonly="true"
    />
  </div>
</template>

<script setup name="ReviewerDetail">
import { ref, computed, onMounted, getCurrentInstance } from "vue"
import { useRoute } from "vue-router"
import { listBill, getBill } from "@/api/biz/bill"
import { listBizCategory } from "@/api/biz/bizCategory"
import BillForm from "@/views/biz/bill/components/BillForm.vue"

const { proxy } = getCurrentInstance()
const route = useRoute()

// type: 'approved' | 'rejected'
const type = computed(() => route.query.type === 'rejected' ? 'rejected' : 'approved')
const pageTitle = computed(() => type.value === 'approved' ? '今日通过票据' : '今日退回票据')

const { biz_bill_status } = proxy.useDict('biz_bill_status')

const billList = ref([])
const loading = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const detailData = ref({})
const categoryOptions = ref([])
const sortField = ref(undefined)
const sortOrder = ref(undefined)

const queryParams = ref({
  currentPage: 1,
  pageSize: 10,
  keywords: undefined,
  createBy: undefined,
  categoryId: undefined,
  status: type.value === 'approved' ? '2' : '3',
  auditDate: getTodayStr()
})

function getTodayStr() {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatAmount(amount) {
  if (amount == null) return "-"
  return "¥" + Number(amount).toLocaleString("zh-CN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function parseTime(time) {
  if (!time) return "-"
  return proxy.parseTime(time)
}

function getList() {
  loading.value = true
  queryParams.value.status = type.value === 'approved' ? '2' : '3'
  queryParams.value.auditDate = getTodayStr()
  queryParams.value.sortField = sortField.value || undefined
  queryParams.value.sortOrder = sortOrder.value || undefined
  listBill(queryParams.value).then(response => {
    billList.value = response.data.list
    total.value = response.data.total
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function handleSortChange({ prop, order }) {
  sortField.value = prop
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : undefined
  getList()
}

function handleQuery() {
  queryParams.value.currentPage = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryFormRef")
  queryParams.value.keywords = undefined
  queryParams.value.createBy = undefined
  queryParams.value.categoryId = undefined
  queryParams.value.currentPage = 1
  getList()
}

function handleDetail(row) {
  getBill(row.id).then(response => {
    detailData.value = response.data
    detailOpen.value = true
  })
}

function loadCategories() {
  listBizCategory({ currentPage: 1, pageSize: 1000 }).then(res => {
    categoryOptions.value = res.data.list || []
  })
}

onMounted(() => {
  loadCategories()
  getList()
})
</script>

<style scoped>
.header-card {
  margin-bottom: 16px;
}
.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}
.toolbar-row {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
}
.search-form :deep(.el-form-item) {
  margin-right: 4px;
  margin-bottom: 0;
}
.search-form :deep(.el-form-item__label) {
  display: flex;
  align-items: center;
}
</style>
