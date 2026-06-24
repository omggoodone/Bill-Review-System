<template>
  <div class="app-container">
    <el-row :gutter="10" class="toolbar">
      <el-col :xs="24" :sm="8" :md="4">
        <el-select v-model="currentFile" @change="loadLog" style="width:100%">
          <el-option label="应用日志 (app.log)" value="app.log" />
          <el-option label="Nginx 访问 (nginx-access.log)" value="nginx-access.log" />
          <el-option label="Nginx 错误 (nginx-error.log)" value="nginx-error.log" />
        </el-select>
      </el-col>
      <el-col :xs="12" :sm="6" :md="3">
        <el-select v-model="maxLines" @change="loadLog" style="width:100%">
          <el-option :value="100" label="100 行" />
          <el-option :value="200" label="200 行" />
          <el-option :value="500" label="500 行" />
          <el-option :value="1000" label="1000 行" />
        </el-select>
      </el-col>
      <el-col :xs="12" :sm="6" :md="3">
        <el-button type="primary" icon="Refresh" @click="loadLog" :loading="loading">
          刷新
        </el-button>
        <el-checkbox v-model="autoRefresh" style="margin-left:8px" @change="toggleAuto">
          自动
        </el-checkbox>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top:10px">
      <pre class="log-viewer" ref="logViewer">{{ content || '暂无日志' }}</pre>
    </el-card>
  </div>
</template>

<script setup>
import request from '@/utils/request'

const currentFile = ref('app.log')
const maxLines = ref(200)
const loading = ref(false)
const autoRefresh = ref(false)
const content = ref('')
const logViewer = ref(null)
let timer = null

function loadLog() {
  loading.value = true
  request({
    url: '/monitor/logs',
    method: 'get',
    params: { file: currentFile.value, lines: maxLines.value }
  }).then(res => {
    content.value = res.data.content
    // 滚到底部
    nextTick(() => {
      if (logViewer.value) {
        logViewer.value.scrollTop = logViewer.value.scrollHeight
      }
    })
  }).finally(() => {
    loading.value = false
  })
}

function toggleAuto(val) {
  if (val) {
    loadLog()
    timer = setInterval(loadLog, 5000)
  } else {
    clearInterval(timer)
  }
}

onMounted(() => loadLog())
onUnmounted(() => clearInterval(timer))
</script>

<style scoped lang="scss">
.toolbar {
  margin-bottom: 10px;
}

.log-viewer {
  background: #1e1e1e;
  color: #d4d4d4;
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 13px;
  line-height: 1.5;
  padding: 16px;
  margin: 0;
  min-height: 60vh;
  max-height: 75vh;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  border-radius: 4px;
}
</style>
