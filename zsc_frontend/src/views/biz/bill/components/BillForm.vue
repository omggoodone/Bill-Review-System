<template>
  <el-dialog v-model="open" width="520px" align-center append-to-body destroy-on-close @close="cancel" :show-header="false" :show-close="false">
    <!-- 详情模式 -->
    <div v-show="readonly">
    <div class="form-title">票据详情</div>
    <el-descriptions :column="2" border class="detail-descriptions">
      <el-descriptions-item label="标题" :span="2">{{ form.title || '-' }}</el-descriptions-item>
      <el-descriptions-item label="描述" :span="2">{{ form.description || '-' }}</el-descriptions-item>
      <el-descriptions-item label="附件" :span="2">
        <template v-if="form.files && form.files.length > 0">
          <div v-for="(file, idx) in form.files" :key="idx" class="file-link">
            <el-icon><Link /></el-icon>
            <a :href="file.filePath" target="_blank">{{ file.fileName }}</a>
          </div>
        </template>
        <span v-else>-</span>
      </el-descriptions-item>
      <el-descriptions-item label="票据编号">{{ form.billNo || '-' }}</el-descriptions-item>
      <el-descriptions-item label="状态">
        <dict-tag :options="biz_bill_status" :value="form.status" />
      </el-descriptions-item>
      <el-descriptions-item label="类别">{{ form.categoryName || categoryOptions.find(c => c.categoryId === form.categoryId)?.categoryName || '-' }}</el-descriptions-item>
      <el-descriptions-item label="金额">{{ formatAmount(form.amount) }}</el-descriptions-item>
      <el-descriptions-item label="创建人">{{ form.createBy || '-' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ parseTime(form.createTime) }}</el-descriptions-item>
      <el-descriptions-item label="审批人">{{ form.auditBy || '-' }}</el-descriptions-item>
      <el-descriptions-item label="审批时间">{{ parseTime(form.auditTime) }}</el-descriptions-item>
    </el-descriptions>

    <template v-if="form.auditLogs && form.auditLogs.length > 0">
      <el-divider content-position="left">审批记录</el-divider>
      <el-timeline>
        <el-timeline-item
          v-for="log in form.auditLogs"
          :key="log.id"
          :timestamp="parseTime(log.auditTime)"
          :type="log.action === '1' ? 'success' : 'danger'"
          placement="top"
        >
          <el-card shadow="never">
            <p><strong>{{ log.action === '1' ? '通过' : '退回' }}</strong> — {{ log.auditBy }}</p>
            <p v-if="log.comment" style="color: #909399; margin-top: 4px;">{{ log.comment }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </template>

    <div class="detail-footer">
      <el-button type="primary" @click="cancel">关 闭</el-button>
    </div>
    </div>

    <!-- 新增/编辑模式 -->
    <div v-show="!readonly">
    <div class="form-title">{{ title }}</div>
    <div class="edit-form-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" class="edit-form">
        <el-form-item label="票据标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入票据标题" :maxlength="200" />
        </el-form-item>
        <el-form-item label="类别" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择类别" clearable style="width: 100%">
            <el-option v-for="cat in categoryOptions" :key="cat.categoryId" :label="cat.categoryName" :value="cat.categoryId" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0"
            :precision="2"
            :controls="false"
            placeholder="请输入金额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" :maxlength="500" />
        </el-form-item>
        <el-form-item label="附件" prop="attachment">
          <FileUpload v-model="form.attachment" :limit="5" :fileSize="10" />
        </el-form-item>
      </el-form>
    </div>
    <div class="form-footer">
      <el-button @click="cancel">取 消</el-button>
      <el-button type="warning" @click="submitForm('0')">保存草稿</el-button>
      <el-button v-if="!form.id" type="primary" @click="submitForm('1')">直接提交</el-button>
    </div>
    </div>
  </el-dialog>
</template>

<script setup name="BizBillForm">
import { Link } from '@element-plus/icons-vue'
import { addBill, updateBill, submitBill } from "@/api/biz/bill"
import { listBizCategory } from "@/api/biz/bizCategory"

const { proxy } = getCurrentInstance()
const { biz_bill_status } = proxy.useDict("biz_bill_status")

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: "" },
  formData: { type: Object, default: () => ({}) },
  readonly: { type: Boolean, default: false }
})

const emit = defineEmits(["update:modelValue", "success"])

const formRef = ref(null)
const categoryOptions = ref([])

const open = computed({
  get: () => props.modelValue,
  set: (val) => emit("update:modelValue", val)
})

const form = ref({})

const rules = {
  title: [{ required: true, message: "票据标题不能为空", trigger: "blur" }],
  categoryId: [{ required: true, message: "请选择类别", trigger: "change" }],
  amount: [{ required: true, message: "金额不能为空", trigger: "blur" }]
}

watch(() => props.formData, (newVal) => {
  form.value = { ...newVal }
  if (!newVal.attachment && newVal.files && newVal.files.length > 0) {
    form.value.attachment = newVal.files.map(f => f.filePath).join(',')
  }
}, { immediate: true, deep: true })

function formatAmount(amount) {
  if (amount == null) return '-'
  return '¥' + Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function loadCategories() {
  listBizCategory({ currentPage: 1, pageSize: 1000 }).then(res => {
    categoryOptions.value = res.data.list || []
  })
}

loadCategories()

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    id: undefined,
    title: undefined,
    categoryId: undefined,
    amount: undefined,
    description: undefined,
    attachment: undefined,
    status: "0"
  }
  proxy.resetForm("formRef")
}

function submitForm(submitStatus) {
  proxy.$refs["formRef"].validate((valid) => {
    if (!valid) return

    form.value.status = submitStatus

    if (form.value.id != undefined) {
      updateBill(form.value).then(() => {
        if (submitStatus === "1") {
          return submitBill(form.value.id)
        }
      }).then(() => {
        proxy.$modal.msgSuccess(submitStatus === "1" ? "提交成功" : "保存成功")
        open.value = false
        emit("success")
      })
    } else {
      addBill(form.value).then(() => {
        proxy.$modal.msgSuccess(submitStatus === "1" ? "提交成功" : "保存成功")
        open.value = false
        emit("success")
      })
    }
  })
}

defineExpose({ formRef, reset })
</script>

<style scoped>
/* 详情模式 */
.detail-descriptions :deep(.el-descriptions__label) {
  width: 80px;
  white-space: nowrap;
}
.detail-descriptions :deep(.el-descriptions__content) {
  padding: 10px 12px;
}
.file-link {
  line-height: 24px;
}
.file-link a {
  margin-left: 4px;
  color: var(--el-color-primary);
  text-decoration: none;
}
.file-link a:hover {
  text-decoration: underline;
}
.detail-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 标题（详情/编辑共用） */
.form-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 20px;
  text-align: center;
}

/* 编辑模式 */
.edit-form-card {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  padding: 16px 20px 4px;
}
.edit-form :deep(.el-form-item__label) {
  font-weight: 500;
}
.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 20px;
}
</style>
