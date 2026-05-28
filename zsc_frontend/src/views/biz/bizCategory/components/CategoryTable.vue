<template>
  <div>
    <el-form :model="queryParams" :inline="true" class="search-form">
      <el-form-item>
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['biz:category:add']"
        >新增</el-button>
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['biz:category:remove']"
        >删除</el-button>
      </el-form-item>
      <el-form-item prop="categoryName">
        <template #label><svg-icon icon-class="search" /></template>
        <el-input
          v-model="queryParams.categoryName"
          placeholder="类别名称"
          clearable
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item prop="status">
        <template #label><svg-icon icon-class="switch" /></template>
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          style="width: 100px"
        >
          <el-option
            v-for="dict in sys_normal_disable"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" circle @click="handleQuery" />
        <el-button type="primary" circle @click="resetQuery"><svg-icon icon-class="reset" /></el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="categoryList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" :selectable="row => row.categoryName !== '其他'" />
      <el-table-column label="类别ID" align="center" prop="categoryId" width="100" />
      <el-table-column label="类别名称" align="center" prop="categoryName" :show-overflow-tooltip="true" />
      <el-table-column label="排序号" align="center" prop="sortOrder" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template #default="scope">
          <template v-if="scope.row.categoryName === '其他'">
            <span class="default-tip">默认类别</span>
          </template>
          <template v-else>
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:category:edit']">修改</el-button>
            <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:category:remove']">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page="queryParams.pageNum"
      :limit="queryParams.pageSize"
      @update:page="$emit('update:pageNum', $event)"
      @update:limit="$emit('update:pageSize', $event)"
      @pagination="handlePagination"
    />
  </div>
</template>

<script setup name="BizCategoryTable">
import { delBizCategory } from "@/api/biz/bizCategory"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict("sys_normal_disable")

const props = defineProps({
  categoryList: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  total: {
    type: Number,
    default: 0
  },
  queryParams: {
    type: Object,
    required: true
  },
})

const emit = defineEmits(['add', 'update', 'delete', 'query', 'reset', 'pagination', 'update:pageNum', 'update:pageSize'])

const ids = ref([])
const multiple = ref(true)

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.categoryId)
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  emit('add')
}

/** 修改按钮操作 */
function handleUpdate(row) {
  const categoryId = row.categoryId || ids.value[0]
  emit('update', categoryId)
}

/** 删除按钮操作 */
function handleDelete(row) {
  const categoryIds = row.categoryId || ids.value
  proxy.$modal.confirm('是否确认删除类别编号为"' + categoryIds + '"的数据项？').then(function() {
    return delBizCategory(categoryIds)
  }).then(() => {
    emit('delete')
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 搜索按钮操作 */
function handleQuery() {
  emit('query')
}

/** 重置按钮操作 */
function resetQuery() {
  emit('reset')
}

/** 分页操作 */
function handlePagination() {
  emit('pagination')
}
</script>

<style scoped>
.default-tip { color: var(--el-text-color-secondary); font-size: 12px; }
.search-form {
  margin-bottom: 16px;
}
.search-form :deep(.el-form-item) {
  margin-right: 8px;
  margin-bottom: 0;
}
.search-form :deep(.el-form-item__label) {
  display: flex;
  align-items: center;
}
</style>
