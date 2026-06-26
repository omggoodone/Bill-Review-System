<template>
  <div class="app-container dashboard">

    <el-row :gutter="20" class="dashboard-stats">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card clickable-card" shadow="hover" @click="goUserDetail('all')">
          <div class="stat-content">
            <div class="stat-icon total-icon"><el-icon :size="28"><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">总用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card clickable-card" shadow="hover" @click="goUserDetail('user')">
          <div class="stat-content">
            <div class="stat-icon user-icon"><el-icon :size="28"><UserFilled /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-label">普通用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card clickable-card" shadow="hover" @click="goUserDetail('reviewer')">
          <div class="stat-content">
            <div class="stat-icon reviewer-icon"><el-icon :size="28"><Checked /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.reviewerCount }}</div>
              <div class="stat-label">审核员</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card clickable-card" shadow="hover" @click="goUserDetail('admin')">
          <div class="stat-content">
            <div class="stat-icon admin-icon"><el-icon :size="28"><Setting /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.adminCount }}</div>
              <div class="stat-label">管理员</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card clickable-card" shadow="hover" @click="goBillDetail('all')">
          <div class="stat-content">
            <div class="stat-icon bill-icon"><el-icon :size="28"><Document /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalBills }}</div>
              <div class="stat-label">总票据</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card clickable-card" shadow="hover" @click="goBillDetail('pending')">
          <div class="stat-content">
            <div class="stat-icon pending-icon"><el-icon :size="28"><Clock /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value pending-value">{{ stats.pendingBills }}</div>
              <div class="stat-label">待审核</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card clickable-card" shadow="hover" @click="goBillDetail('approved')">
          <div class="stat-content">
            <div class="stat-icon approved-icon"><el-icon :size="28"><CircleCheck /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value approved-value">{{ stats.approvedBills }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card clickable-card" shadow="hover" @click="goBillDetail('rejected')">
          <div class="stat-content">
            <div class="stat-icon rejected-icon"><el-icon :size="28"><CircleClose /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value rejected-value">{{ stats.rejectedBills }}</div>
              <div class="stat-label">已退回</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-charts">
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header"><span>用户角色分布</span></div>
          </template>
          <div ref="roleChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header"><span>票据状态概览</span></div>
          </template>
          <div ref="billChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header"><span>快捷操作</span></div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" icon="User" size="large" @click="goUserManage">管理员管理</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="dashboard-charts">
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header"><span>审核员工作量</span></div>
          </template>
          <div ref="workloadChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header"><span>各类别金额总数</span></div>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header"><span>用户已通过金额</span></div>
          </template>
          <div ref="userAmountChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup name="SuperAdminDashboard">
import { ref, onMounted, nextTick } from 'vue'
import { User, UserFilled, Checked, Setting, Document, Clock, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import useSettingsStore from '@/store/modules/settings'
import { getAdminStats, getReviewerWorkload, getUserAmountSummary } from '@/api/biz/admin'
import { getCategorySummary } from '@/api/biz/bill'

const settingsStore = useSettingsStore()
const router = useRouter()

const roleChartRef = ref(null)
const billChartRef = ref(null)
const workloadChartRef = ref(null)
const categoryChartRef = ref(null)
const userAmountChartRef = ref(null)

const stats = ref({
  totalUsers: 0, userCount: 0, reviewerCount: 0, adminCount: 0,
  totalBills: 0, pendingBills: 0, approvedBills: 0, rejectedBills: 0
})

function isDark() { return settingsStore.isDark }

function initRoleChart() {
  if (!roleChartRef.value) return
  const chart = echarts.init(roleChartRef.value)
  const dark = isDark()
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'middle', textStyle: { color: dark ? '#ccc' : '#333' } },
    series: [{
      name: '用户角色', type: 'pie', radius: ['45%', '70%'], center: ['60%', '50%'],
      label: { color: dark ? '#ccc' : '#333' },
      data: [
        { value: stats.value.userCount, name: '普通用户', itemStyle: { color: '#4facfe' } },
        { value: stats.value.reviewerCount, name: '审核员', itemStyle: { color: '#43e97b' } },
        { value: stats.value.adminCount, name: '管理员', itemStyle: { color: '#fa709a' } }
      ].filter(d => d.value > 0),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function initBillChart() {
  if (!billChartRef.value) return
  const chart = echarts.init(billChartRef.value)
  const dark = isDark()
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left', top: 'middle', textStyle: { color: dark ? '#ccc' : '#333' } },
    series: [{
      name: '票据状态', type: 'pie', radius: ['45%', '70%'], center: ['60%', '50%'],
      label: { color: dark ? '#ccc' : '#333' },
      data: [
        { value: stats.value.pendingBills, name: '待审核', itemStyle: { color: '#e6a23c' } },
        { value: stats.value.approvedBills, name: '已通过', itemStyle: { color: '#67c23a' } },
        { value: stats.value.rejectedBills, name: '已退回', itemStyle: { color: '#f56c6c' } }
      ].filter(d => d.value > 0),
      emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function initWorkloadChart(data) {
  if (!workloadChartRef.value || !data.length) return
  const chart = echarts.init(workloadChartRef.value)
  const dark = isDark()
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: p => {
      const d = data[p[0].dataIndex]
      return `${d.reviewerName}<br/>通过: ${d.approvedCount} | 退回: ${d.rejectedCount} | 合计: ${d.totalCount}`
    }},
    legend: { data: ['通过', '退回'], textStyle: { color: dark ? '#ccc' : '#333' } },
    grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', axisLabel: { color: dark ? '#aaa' : '#666' } },
    yAxis: { type: 'category', data: data.map(d => d.reviewerName), axisLabel: { color: dark ? '#ccc' : '#333', fontWeight: 'bold' } },
    series: [
      { name: '通过', type: 'bar', stack: 'total', data: data.map(d => d.approvedCount), itemStyle: { color: '#67c23a' }, label: { show: true, position: 'insideRight', color: '#fff', formatter: p => p.value || '' } },
      { name: '退回', type: 'bar', stack: 'total', data: data.map(d => d.rejectedCount), itemStyle: { color: '#f56c6c' }, label: { show: true, position: 'insideRight', color: '#fff', formatter: p => p.value || '' } }
    ]
  })
  window.addEventListener('resize', () => chart.resize())
}

function initCategoryChart(data) {
  if (!categoryChartRef.value || !data.length) return
  const chart = echarts.init(categoryChartRef.value)
  const dark = isDark()
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>¥${p[0].value.toLocaleString()}` },
    grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', name: '金额 (¥)', nameTextStyle: { color: dark ? '#ccc' : '#666' }, axisLabel: { color: dark ? '#aaa' : '#666' } },
    yAxis: { type: 'category', data: data.map(d => d.label), axisLabel: { color: dark ? '#ccc' : '#333', fontWeight: 'bold' } },
    series: [{
      name: '金额', type: 'bar', data: data.map(d => d.count),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#409EFF' }, { offset: 1, color: '#67C23A' }]) },
      label: { show: true, position: 'right', color: dark ? '#ddd' : '#333', fontWeight: 'bold', formatter: p => '¥' + p.value.toLocaleString() }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function initUserAmountChart(data) {
  if (!userAmountChartRef.value || !data.length) return
  const chart = echarts.init(userAmountChartRef.value)
  const dark = isDark()
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>¥${p[0].value.toLocaleString()}` },
    grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', name: '金额 (¥)', nameTextStyle: { color: dark ? '#ccc' : '#666' }, axisLabel: { color: dark ? '#aaa' : '#666' } },
    yAxis: { type: 'category', data: data.map(d => d.userName), axisLabel: { color: dark ? '#ccc' : '#333', fontWeight: 'bold' } },
    series: [{
      name: '金额', type: 'bar', data: data.map(d => d.totalAmount),
      itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#409EFF' }, { offset: 1, color: '#67C23A' }]) },
      label: { show: true, position: 'right', color: dark ? '#ddd' : '#333', fontWeight: 'bold', formatter: p => '¥' + p.value.toLocaleString() }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function loadData() {
  getAdminStats().then(res => {
    stats.value = res.data
    nextTick(() => { initRoleChart(); initBillChart() })
  })
  getReviewerWorkload().then(res => nextTick(() => initWorkloadChart(res.data || [])))
  getCategorySummary().then(res => nextTick(() => initCategoryChart(res.data || [])))
  getUserAmountSummary().then(res => nextTick(() => initUserAmountChart(res.data || [])))
}

function goUserManage() { router.push('/admin/users') }
function goBillDetail(type) { router.push('/admin/super/bill-detail?type=' + type) }
function goUserDetail(type) { router.push('/admin/super/user-detail?type=' + type) }

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.dashboard { padding: 20px; }

.dashboard-stats {
  margin-bottom: 20px;
  .el-col { margin-bottom: 12px; }
  .stat-card {
    transition: all 0.3s;
    &:hover { transform: translateY(-3px); }
    &.clickable-card { cursor: pointer; }
    .stat-content {
      display: flex; align-items: center; justify-content: space-around; padding: 6px 0;
      .stat-icon {
        width: 56px; height: 56px; border-radius: 50%;
        display: flex; align-items: center; justify-content: center;
        &.total-icon     { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
        &.user-icon      { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: #fff; }
        &.reviewer-icon  { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: #fff; }
        &.admin-icon     { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); color: #fff; }
        &.bill-icon      { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: #fff; }
        &.pending-icon   { background: linear-gradient(135deg, #f7971e 0%, #ffd200 100%); color: #fff; }
        &.approved-icon  { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
        &.rejected-icon  { background: linear-gradient(135deg, #f56c6c 0%, #e6a23c 100%); color: #fff; }
      }
      .stat-info {
        text-align: center;
        .stat-value { font-size: 24px; font-weight: bold; color: var(--el-text-color-primary); margin-bottom: 2px; }
        .stat-value.pending-value { color: #e6a23c; }
        .stat-value.approved-value { color: #67c23a; }
        .stat-value.rejected-value { color: #f56c6c; }
        .stat-label { font-size: 12px; color: var(--el-text-color-secondary); }
      }
    }
  }
}

.dashboard-charts {
  margin-bottom: 20px;
  .chart-card {
    height: 100%;
    .card-header { display: flex; align-items: center; font-weight: bold; font-size: 16px; }
    .chart-container { height: 320px; width: 100%; }
    .quick-actions {
      height: 320px; display: flex; align-items: center; justify-content: center;
    }
  }
}

:deep(.el-card__header) { padding: 15px 20px; }
</style>
