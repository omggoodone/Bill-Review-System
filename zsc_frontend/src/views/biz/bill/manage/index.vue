<template>
  <div class="app-container dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="dashboard-stats">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon total-icon">
              <el-icon :size="36"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.total }}</div>
              <div class="stat-label">全部票据</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon draft-icon">
              <el-icon :size="36"><Edit /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.draft }}</div>
              <div class="stat-label">草稿中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon pending-icon">
              <el-icon :size="36"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pending }}</div>
              <div class="stat-label">审批中</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon approved-icon">
              <el-icon :size="36"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.approved }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" class="dashboard-charts">
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>票据状态分布</span>
            </div>
          </template>
          <div ref="statusChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>各类别金额汇总</span>
              <el-tag type="success" size="small" style="margin-left: 8px;">已通过</el-tag>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :md="12" :lg="8">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" icon="Plus" size="large" @click="goCreate">新增票据</el-button>
            <el-button type="success" icon="Document" size="large" @click="goMyBill">我的票据</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近票据 -->
    <el-row :gutter="20" class="dashboard-tables">
      <el-col :span="24">
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <span>最近创建的票据</span>
              <el-button type="primary" link @click="goMyBill">查看全部</el-button>
            </div>
          </template>
          <el-table v-loading="loading" :data="recentBills" style="width: 100%" max-height="340">
            <el-table-column label="票据编号" prop="billNo" :show-overflow-tooltip="true" min-width="160" />
            <el-table-column label="标题" prop="title" :show-overflow-tooltip="true" min-width="180" />
            <el-table-column label="类别" prop="categoryName" width="100" />
            <el-table-column label="金额" prop="amount" width="120">
              <template #default="scope">
                <span>{{ formatAmount(scope.row.amount) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" prop="status" width="90">
              <template #default="scope">
                <dict-tag :options="biz_bill_status" :value="scope.row.status" />
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="160">
              <template #default="scope">
                <span>{{ parseTime(scope.row.createTime) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="BillManage">
import { ref, onMounted, nextTick } from 'vue'
import { Document, Edit, Clock, CircleCheck } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useRouter } from 'vue-router'
import useSettingsStore from '@/store/modules/settings'
import { listBill, getCategorySummary } from '@/api/biz/bill'

const { proxy } = getCurrentInstance()
const settingsStore = useSettingsStore()
const { biz_bill_status } = proxy.useDict('biz_bill_status')
const router = useRouter()

const statusChartRef = ref(null)
const trendChartRef = ref(null)
const loading = ref(false)

const statistics = ref({
  total: 0,
  draft: 0,
  pending: 0,
  approved: 0,
  rejected: 0
})

const recentBills = ref([])

function formatAmount(amount) {
  if (amount == null) return '-'
  return '¥' + Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function parseTime(time) {
  if (!time) return '-'
  return proxy.parseTime(time)
}

function initStatusChart() {
  if (!statusChartRef.value) return
  const chart = echarts.init(statusChartRef.value)
  const isDark = settingsStore.isDark
  const option = {
    tooltip: { trigger: 'item', formatter: '{a} <br/>{b}: {c} ({d}%)' },
    legend: {
      orient: 'vertical', left: 'left', top: 'middle',
      textStyle: { color: isDark ? '#ccc' : '#333' }
    },
    series: [{
      name: '票据状态',
      type: 'pie',
      radius: '60%',
      center: ['60%', '50%'],
      label: { color: isDark ? '#ccc' : '#333' },
      data: [
        { value: statistics.value.draft, name: '草稿' },
        { value: statistics.value.pending, name: '审批中' },
        { value: statistics.value.approved, name: '已通过' },
        { value: statistics.value.rejected, name: '已退回' }
      ].filter(d => d.value > 0),
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' }
      }
    }]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

function initCategoryChart(data) {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  const labels = data.map(d => d.label)
  const values = data.map(d => d.count)
  const isDark = settingsStore.isDark

  const option = {
    tooltip: { trigger: 'axis', formatter: p => `${p[0].name}<br/>¥${p[0].value.toLocaleString()}` },
    grid: { left: '3%', right: '10%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'value',
      name: '金额 (¥)',
      nameTextStyle: { color: isDark ? '#ccc' : '#666' },
      axisLabel: { color: isDark ? '#aaa' : '#666' }
    },
    yAxis: {
      type: 'category',
      data: labels,
      axisLabel: { color: isDark ? '#ccc' : '#333', fontWeight: 'bold' },
      axisLine: { lineStyle: { color: isDark ? '#555' : '#ddd' } }
    },
    series: [{
      name: '金额',
      type: 'bar',
      data: values,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#409EFF' },
          { offset: 1, color: '#67C23A' }
        ])
      },
      label: {
        show: true,
        position: 'right',
        color: isDark ? '#ddd' : '#333',
        fontWeight: 'bold',
        formatter: p => '¥' + p.value.toLocaleString()
      }
    }]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

function loadData() {
  loading.value = true
  listBill({ currentPage: 1, pageSize: 10 }).then(res => {
    const list = res.data.list || []
    recentBills.value = list.slice(0, 5)
    const total = res.data.total || 0

    // 统计各状态数量（基于当前分页数据估算，完整统计需后端接口）
    statistics.value.total = total
    // 分状态调用查询获取准确数据
    Promise.all([
      listBill({ currentPage: 1, pageSize: 1, status: '0' }),
      listBill({ currentPage: 1, pageSize: 1, status: '1' }),
      listBill({ currentPage: 1, pageSize: 1, status: '2' }),
      listBill({ currentPage: 1, pageSize: 1, status: '3' })
    ]).then(([draftRes, pendingRes, approvedRes, rejectedRes]) => {
      statistics.value.draft = draftRes.data.total || 0
      statistics.value.pending = pendingRes.data.total || 0
      statistics.value.approved = approvedRes.data.total || 0
      statistics.value.rejected = rejectedRes.data.total || 0
      // 用总数减去已知的来计算其他状态
      if (statistics.value.draft + statistics.value.pending + statistics.value.approved < statistics.value.total) {
        // 可能有退回等状态
      }
    }).finally(() => {
      nextTick(() => {
        initStatusChart()
        getCategorySummary().then(res => {
          initCategoryChart(res.data || [])
        }).catch(() => {
          initCategoryChart([])
        })
      })
    })
  }).finally(() => {
    loading.value = false
  })
}

function goCreate() {
  router.push('/bill/myBill')
}
function goMyBill() {
  router.push('/bill/myBill')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.dashboard {
  padding: 20px;

  .dashboard-stats {
    margin-bottom: 20px;

    .stat-card {
      transition: all 0.3s;
      &:hover { transform: translateY(-5px); }

      .stat-content {
        display: flex;
        align-items: center;
        justify-content: space-around;
        padding: 10px 0;

        .stat-icon {
          width: 70px;
          height: 70px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;

          &.total-icon     { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; }
          &.draft-icon     { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: #fff; }
          &.pending-icon   { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); color: #fff; }
          &.approved-icon  { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); color: #fff; }
        }

        .stat-info {
          text-align: center;
          .stat-value { font-size: 32px; font-weight: bold; color: #303133; margin-bottom: 5px; }
          .stat-label { font-size: 14px; color: #909399; }
        }
      }
    }
  }

  .dashboard-charts {
    margin-bottom: 20px;

    .chart-card {
      height: 100%;
      .chart-container { height: 280px; width: 100%; }
    }

    .quick-actions {
      height: 280px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 20px;
    }
  }

  .dashboard-tables .table-card .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-weight: bold;
    font-size: 16px;
  }
}

:deep(.el-card__header) {
  padding: 15px 20px;
}
</style>
