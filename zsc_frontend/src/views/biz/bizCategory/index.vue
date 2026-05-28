<template>
  <div class="app-container">
    <!-- 列表组件（含搜索表单和操作按钮） -->
    <CategoryTable
      :categoryList="categoryList"
      :loading="loading"
      :total="total"
      :queryParams="queryParams"
      @update:pageNum="queryParams.pageNum = $event"
      @update:pageSize="queryParams.pageSize = $event"
      @add="handleAdd"
      @update="handleUpdate"
      @delete="handleDelete"
      @query="handleQuery"
      @reset="handleReset"
      @pagination="getList"
    />

    <!-- 表单组件 -->
    <CategoryForm
      v-model="open"
      :title="title"
      :formData="form"
      ref="categoryFormRef"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup name="BizCategory">
import { listBizCategory, getBizCategory } from "@/api/biz/bizCategory"
import CategoryTable from "./components/CategoryTable.vue"
import CategoryForm from "./components/CategoryForm.vue"

const { proxy } = getCurrentInstance()

const categoryList = ref([])
const open = ref(false)
const loading = ref(true)
const total = ref(0)
const title = ref("")

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  categoryName: undefined,
  status: undefined
})

const form = ref({})

const categoryFormRef = ref(null)

/** 查询业务类别列表 */
function getList() {
  loading.value = true
  listBizCategory(queryParams.value).then(response => {
    categoryList.value = response.data.list
    total.value = response.data.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function handleReset() {
  queryParams.value.categoryName = undefined
  queryParams.value.status = undefined
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加业务类别"
}

/** 修改按钮操作 */
function handleUpdate(categoryId) {
  reset()
  getBizCategory(categoryId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改业务类别"
  })
}

/** 删除按钮操作 */
function handleDelete() {
  getList()
}

/** 表单提交成功回调 */
function handleFormSuccess() {
  getList()
}

/** 表单重置 */
function reset() {
  form.value = {
    categoryId: undefined,
    categoryName: undefined,
    sortOrder: 1,
    status: "0"
  }
  if (categoryFormRef.value) {
    categoryFormRef.value.reset()
  }
}

// 初始化加载数据
getList()
</script>
