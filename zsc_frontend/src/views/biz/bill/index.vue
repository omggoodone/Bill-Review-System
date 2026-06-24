<template>
  <div class="app-container">
    <!-- Tab 栏 -->
    <el-tabs v-model="activeStatus" @tab-change="handleStatusTabChange" class="status-tabs">
      <el-tab-pane label="全部" name="" />
      <el-tab-pane v-for="dict in biz_bill_status" :key="dict.value" :label="dict.label" :name="dict.value" />
    </el-tabs>

    <!-- 工具栏行：操作按钮 + 搜索表单 -->
    <div class="toolbar-row">
      <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['biz:bill:add']">新增</el-button>
      <el-button type="warning" plain icon="Upload" :disabled="ids.length === 0" @click="handleBatchSubmit" v-hasPermi="['biz:bill:add']">批量提交</el-button>
      <el-button type="danger" plain icon="Delete" :disabled="ids.length === 0" style="margin-right: 16px;" @click="handleBatchDelete" v-hasPermi="['biz:bill:remove']">批量删除</el-button>
      <el-form :model="queryParams" :inline="true" class="search-form">
        <el-form-item prop="keywords">
          <template #label><svg-icon icon-class="search" /></template>
          <el-input v-model="queryParams.keywords" placeholder="编号/标题" clearable style="width: 130px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item prop="categoryId">
          <template #label><svg-icon icon-class="category" /></template>
          <el-select v-model="queryParams.categoryId" placeholder="类别" clearable style="width: 100px">
            <el-option v-for="cat in categoryOptions" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item prop="dateRange">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="-" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" circle @click="handleQuery" />
          <el-button type="primary" circle @click="resetQuery"><svg-icon icon-class="reset" /></el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="billList" @selection-change="handleSelectionChange" @sort-change="handleSortChange">
      <el-table-column type="selection" width="55" align="center" :selectable="checkSelectable" />
      <el-table-column label="票据编号" align="center" prop="billNo" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="180" />
      <el-table-column label="类别" align="center" prop="categoryName" width="100" />
      <el-table-column label="金额" align="center" prop="amount" width="120" sortable="custom">
        <template #default="scope">
          <span>{{ formatAmount(scope.row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <dict-tag :options="biz_bill_status" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建人" align="center" prop="createBy" width="100" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" sortable="custom">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="审批人" align="center" prop="auditBy" width="100" />
      <el-table-column label="审批时间" align="center" prop="auditTime" width="160" sortable="custom">
        <template #default="scope">
          <span>{{ parseTime(scope.row.auditTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width" fixed="right">
        <template #default="scope">
          <el-tooltip content="详情" placement="top"><el-button link type="primary" icon="Document" @click="handleDetail(scope.row)" /></el-tooltip>
          <el-tooltip content="修改" placement="top"><el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-show="canEdit(scope.row)" v-hasPermi="['biz:bill:add']" /></el-tooltip>
          <el-tooltip content="提交" placement="top"><el-button link type="primary" icon="Upload" @click="handleSubmit(scope.row)" v-show="canSubmit(scope.row)" v-hasPermi="['biz:bill:add']" /></el-tooltip>
          <el-tooltip content="审批" placement="top"><el-button link type="primary" icon="CircleCheck" @click="handleReview(scope.row)" v-show="canReview(scope.row)" v-hasPermi="['biz:bill:review']" /></el-tooltip>
          <el-tooltip content="删除" placement="top"><el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-show="canDelete(scope.row)" v-hasPermi="['biz:bill:remove']" /></el-tooltip>
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

    <!-- 新增/编辑弹窗 -->
    <BillForm
      v-model="open"
      :title="title"
      :formData="form"
      :readonly="isDetail"
      ref="billFormRef"
      @success="handleFormSuccess"
    />

    <!-- 审批弹窗 -->
    <el-dialog title="审批票据" v-model="reviewOpen" width="550px" append-to-body @close="cancelReview">
      <el-descriptions :column="2" border size="small" style="margin-bottom: 20px;">
        <el-descriptions-item label="票据编号">{{ currentBill.billNo }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentBill.title }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ currentBill.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ formatAmount(currentBill.amount) }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ currentBill.createBy }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ parseTime(currentBill.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <el-form ref="reviewFormRef" :model="reviewForm" :rules="reviewRules" label-width="80px">
        <el-form-item label="审批结果" prop="action">
          <el-radio-group v-model="reviewForm.action">
            <el-radio value="1">通过</el-radio>
            <el-radio value="2">退回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见" prop="comment">
          <el-input
            v-model="reviewForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
            :maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancelReview">取 消</el-button>
          <el-button type="primary" @click="submitReview">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BizBill">
import { listBill, getBill, submitBill, delBill, reviewBill } from "@/api/biz/bill"
import { listBizCategory } from "@/api/biz/bizCategory"
import BillForm from "./components/BillForm.vue"

const { proxy } = getCurrentInstance()
const { biz_bill_status } = proxy.useDict("biz_bill_status")

const billList = ref([])
const open = ref(false)
const reviewOpen = ref(false)
const loading = ref(true)
const total = ref(0)
const title = ref("")
const isDetail = ref(false)
const activeStatus = ref("")
const dateRange = ref([])

const queryParams = ref({
  currentPage: 1,
  pageSize: 10,
  keywords: undefined,
  categoryId: undefined,
  status: undefined,
  startTime: undefined,
  endTime: undefined
})

const form = ref({})
const currentBill = ref({})
const categoryOptions = ref([])

const billFormRef = ref(null)
const reviewFormRef = ref(null)
const ids = ref([])
const sortField = ref(undefined)
const sortOrder = ref(undefined)

const reviewForm = ref({
  billId: undefined,
  action: "1",
  comment: undefined
})

const reviewRules = {
  action: [{ required: true, message: "请选择审批结果", trigger: "change" }]
}

/** 是否可编辑（草稿 / 退回） */
function canEdit(row) {
  return row.status === "0" || row.status === "3"
}

/** 是否可选中（草稿/退回） */
function checkSelectable(row) {
  return row.status === "0" || row.status === "3"
}

/** 是否可删除（仅草稿） */
function canDelete(row) {
  return row.status === "0"
}

/** 是否可提交（草稿 / 退回） */
function canSubmit(row) {
  return row.status === "0" || row.status === "3"
}

/** 是否可审批（已提交） */
function canReview(row) {
  return row.status === "1"
}

/** 金额格式化 */
function formatAmount(amount) {
  if (amount == null) return "-"
  return "¥" + Number(amount).toLocaleString("zh-CN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 加载类别选项 */
function loadCategories() {
  listBizCategory({ currentPage: 1, pageSize: 1000 }).then(res => {
    categoryOptions.value = res.data.list || []
  })
}

/** 查询票据列表 */
let abortController = null
function getList() {
  if (abortController) {
    abortController.abort()
  }
  abortController = new AbortController()
  loading.value = true
  const params = {
    currentPage: queryParams.value.currentPage,
    pageSize: queryParams.value.pageSize,
    keywords: queryParams.value.keywords || undefined,
    categoryId: queryParams.value.categoryId || undefined,
    status: queryParams.value.status || undefined,
    startTime: dateRange.value?.[0] || undefined,
    endTime: dateRange.value?.[1] || undefined,
    sortField: sortField.value || undefined,
    sortOrder: sortOrder.value || undefined
  }
  listBill(params, abortController.signal).then(response => {
    billList.value = response.data.list
    total.value = response.data.total
    ids.value = []
  }).catch((err) => {
    if (err?.name === 'CanceledError' || err?.code === 'ERR_CANCELED') return
  }).finally(() => {
    loading.value = false
  })
}

/** 搜索 */
function handleQuery() {
  queryParams.value.currentPage = 1
  getList()
}

/** 重置 */
function resetQuery() {
  dateRange.value = []
  queryParams.value.keywords = undefined
  queryParams.value.categoryId = undefined
  queryParams.value.currentPage = 1
  if (activeStatus.value !== '') {
    activeStatus.value = ''
    // 切 tab 会触发 query，不用手动调 getList
  } else {
    queryParams.value.status = undefined
    getList()
  }
}

/** Tab 切换状态筛选 */
function handleStatusTabChange(tabName) {
  queryParams.value.status = tabName || undefined
  handleQuery()
}

/** 排序变化 */
function handleSortChange({ prop, order }) {
  sortField.value = prop
  sortOrder.value = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : undefined
  getList()
}

/** 多选 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
}

/** 批量提交 */
function handleBatchSubmit() {
  const submitIds = ids.value.filter(id => {
    const row = billList.value.find(r => r.id === id)
    return row && (row.status === "0" || row.status === "3")
  })
  if (submitIds.length === 0) {
    proxy.$modal.msgWarning("所选票据无可提交项（仅草稿或已退回可提交）")
    return
  }
  proxy.$modal.confirm("确认提交选中的 " + submitIds.length + " 条票据吗？").then(() => {
    Promise.all(submitIds.map(id => submitBill(id))).then(() => {
      proxy.$modal.msgSuccess("批量提交成功")
      ids.value = []
      getList()
    })
  })
}

/** 批量删除 */
function handleBatchDelete() {
  const deleteIds = ids.value.filter(id => {
    const row = billList.value.find(r => r.id === id)
    return row && row.status === "0"
  })
  if (deleteIds.length === 0) {
    proxy.$modal.msgWarning("所选票据无可删除项（仅草稿可删除）")
    return
  }
  proxy.$modal.confirm("确认删除选中的 " + deleteIds.length + " 条草稿票据吗？").then(() => {
    Promise.all(deleteIds.map(id => delBill(id))).then(() => {
      proxy.$modal.msgSuccess("批量删除成功")
      ids.value = []
      getList()
    })
  })
}

/** 新增 */
function handleAdd() {
  resetForm()
  isDetail.value = false
  open.value = true
  title.value = "新增票据"
}

/** 修改 */
function handleUpdate(row) {
  resetForm()
  isDetail.value = false
  getBill(row.id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改票据"
  })
}

/** 查看详情 */
function handleDetail(row) {
  resetForm()
  isDetail.value = true
  getBill(row.id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "票据详情"
  })
}

/** 提交 */
function handleSubmit(row) {
  proxy.$modal.confirm("确认提交该票据吗？提交后将无法修改。").then(() => {
    submitBill(row.id).then(() => {
      proxy.$modal.msgSuccess("提交成功")
      getList()
    })
  })
}

/** 删除 */
function handleDelete(row) {
  proxy.$modal.confirm('确认删除票据编号为"' + row.billNo + '"的数据项吗？').then(() => {
    return delBill(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getList()
  }).catch(() => {})
}

/** 审批 */
function handleReview(row) {
  currentBill.value = row
  reviewForm.value = {
    billId: row.id,
    action: "1",
    comment: undefined
  }
  reviewOpen.value = true
}

function cancelReview() {
  reviewOpen.value = false
  proxy.resetForm("reviewFormRef")
}

function submitReview() {
  proxy.$refs["reviewFormRef"].validate((valid) => {
    if (!valid) return
    reviewBill(reviewForm.value).then(() => {
      proxy.$modal.msgSuccess("审批完成")
      reviewOpen.value = false
      getList()
    })
  })
}

/** 表单提交成功回调 */
function handleFormSuccess() {
  open.value = false
  reviewOpen.value = false
  getList()
}

/** 重置表单数据 */
function resetForm() {
  form.value = {
    id: undefined,
    title: undefined,
    categoryId: undefined,
    amount: undefined,
    description: undefined,
    attachment: undefined,
    status: "0"
  }
  if (billFormRef.value) {
    billFormRef.value.reset()
  }
}

loadCategories()
getList()
</script>

<style scoped>
.status-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}
.status-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}
.toolbar-row {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
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
