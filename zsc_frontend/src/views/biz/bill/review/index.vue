<template>
  <div class="app-container">
    <!-- 搜索表单 -->
    <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="关键字" prop="keywords">
        <el-input
          v-model="queryParams.keywords"
          placeholder="票据编号/标题"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery"><svg-icon icon-class="reset" /> 重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <span class="review-tip">
          <el-icon><InfoFilled /></el-icon>
          仅显示待审核的票据
        </span>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 审核列表 -->
    <el-table v-loading="loading" :data="billList">
      <el-table-column label="票据编号" align="center" prop="billNo" :show-overflow-tooltip="true" min-width="160" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="180" />
      <el-table-column label="类别" align="center" prop="categoryName" width="100" />
      <el-table-column label="金额" align="center" prop="amount" width="120">
        <template #default="scope">
          <span>{{ formatAmount(scope.row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提交人" align="center" prop="createBy" width="100" />
      <el-table-column label="提交时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-tooltip content="详情" placement="top"><el-button link type="primary" icon="Document" @click="handleDetail(scope.row)" v-hasPermi="['biz:bill:query']" /></el-tooltip>
          <el-tooltip content="审批" placement="top"><el-button link type="primary" icon="CircleCheck" @click="handleReview(scope.row)" v-hasPermi="['biz:bill:review']" /></el-tooltip>
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

    <!-- 审批弹窗 -->
    <el-dialog title="审批票据" v-model="reviewOpen" width="550px" append-to-body @close="cancelReview">
      <el-descriptions :column="2" border size="small" style="margin-bottom: 20px;">
        <el-descriptions-item label="票据编号">{{ currentRow.billNo }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentRow.title }}</el-descriptions-item>
        <el-descriptions-item label="类别">{{ currentRow.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ formatAmount(currentRow.amount) }}</el-descriptions-item>
        <el-descriptions-item label="提交人">{{ currentRow.createBy }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ parseTime(currentRow.createTime) }}</el-descriptions-item>
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

<script setup name="BizBillReview">
import { listBill, getBill, reviewBill } from "@/api/biz/bill"
import BillForm from "@/views/biz/bill/components/BillForm.vue"

const { proxy } = getCurrentInstance()

const billList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])
const detailOpen = ref(false)
const reviewOpen = ref(false)
const detailData = ref({})
const currentRow = ref({})

const queryParams = ref({
  currentPage: 1,
  pageSize: 10,
  keywords: undefined,
  status: "1",
  startTime: undefined,
  endTime: undefined
})

const reviewForm = ref({
  billId: undefined,
  action: "1",
  comment: undefined
})

const reviewRules = {
  action: [{ required: true, message: "请选择审批结果", trigger: "change" }]
}

function formatAmount(amount) {
  if (amount == null) return "-"
  return "¥" + Number(amount).toLocaleString("zh-CN", { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getList() {
  loading.value = true
  queryParams.value.startTime = dateRange.value ? dateRange.value[0] : undefined
  queryParams.value.endTime = dateRange.value ? dateRange.value[1] : undefined
  listBill(queryParams.value).then(response => {
    billList.value = response.data.list
    total.value = response.data.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.currentPage = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryFormRef")
  handleQuery()
}

function handleDetail(row) {
  getBill(row.id).then(response => {
    detailData.value = response.data
    detailOpen.value = true
  })
}

function handleReview(row) {
  currentRow.value = row
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

getList()
</script>
