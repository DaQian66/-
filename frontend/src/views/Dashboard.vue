<template>
  <div class="page-container">
    <div class="dashboard-head">
      <div>
        <h2>考勤仪表盘</h2>
        <p>查看今日出勤、迟到、请假待审和部门出勤趋势。</p>
      </div>
      <el-button type="primary" @click="loadData" :loading="loading">刷新</el-button>
    </div>

    <el-row :gutter="16" class="metric-row">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in cards" :key="card.label">
        <div class="metric-card">
          <span>{{ card.label }}</span>
          <strong :style="{ color: card.color }">{{ card.value }}</strong>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <div class="content-card chart-card">
          <h3>部门出勤率</h3>
          <div v-if="deptAttendance.length" class="bar-list">
            <div class="bar-row" v-for="item in deptAttendance" :key="item.name">
              <span>{{ item.name }}</span>
              <div class="bar-track"><div class="bar-fill" :style="{ width: item.value + '%' }" /></div>
              <b>{{ item.value }}%</b>
            </div>
          </div>
          <el-empty v-else description="暂无部门出勤数据" />
        </div>
      </el-col>

      <el-col :xs="24" :md="12">
        <div class="content-card chart-card">
          <h3>近 7 天出勤趋势</h3>
          <svg v-if="trendPoints.length" viewBox="0 0 520 260" class="line-chart">
            <line v-for="i in 5" :key="i" x1="44" :y1="i * 42" x2="500" :y2="i * 42" stroke="#e5e7eb" />
            <polyline :points="linePoints" fill="none" stroke="#409eff" stroke-width="3" />
            <circle v-for="(p, idx) in trendPoints" :key="idx" :cx="p.x" :cy="p.y" r="4" fill="#409eff" />
            <text v-for="(item, idx) in dailyTrend" :key="item.name" :x="trendPoints[idx].x" y="245" text-anchor="middle" fill="#606266" font-size="12">
              {{ item.name }}
            </text>
          </svg>
          <el-empty v-else description="暂无趋势数据" />
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getAttendanceStatistics } from '@/api'

const loading = ref(false)
const stats = reactive({
  totalEmployees: 0,
  todayCheckIn: 0,
  todayAbsent: 0,
  todayLate: 0,
  pendingLeaves: 0,
  attendanceRate: 0
})
const deptAttendance = ref([])
const dailyTrend = ref([])

const cards = computed(() => [
  { label: '在职员工', value: `${stats.totalEmployees} 人`, color: '#409eff' },
  { label: '今日签到', value: `${stats.todayCheckIn} 人`, color: '#67c23a' },
  { label: '今日迟到', value: `${stats.todayLate} 人`, color: '#e6a23c' },
  { label: '待审请假', value: `${stats.pendingLeaves} 条`, color: '#f56c6c' }
])

const trendPoints = computed(() => {
  const data = dailyTrend.value
  if (!data.length) return []
  const max = Math.max(100, ...data.map(item => item.value))
  return data.map((item, index) => ({
    x: 50 + (index / Math.max(data.length - 1, 1)) * 440,
    y: 220 - (item.value / max) * 180
  }))
})

const linePoints = computed(() => trendPoints.value.map(point => `${point.x},${point.y}`).join(' '))

async function loadData() {
  loading.value = true
  try {
    const res = await getAttendanceStatistics()
    Object.assign(stats, res.data || {})
    deptAttendance.value = res.data?.departmentAttendance || []
    dailyTrend.value = res.data?.dailyTrend || []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.dashboard-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.dashboard-head h2 {
  margin: 0 0 6px;
  font-size: 24px;
}
.dashboard-head p {
  margin: 0;
  color: #606266;
}
.metric-row {
  margin-bottom: 16px;
}
.metric-card {
  background: #fff;
  border-radius: 8px;
  padding: 18px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.metric-card span {
  display: block;
  color: #606266;
  margin-bottom: 8px;
}
.metric-card strong {
  font-size: 28px;
}
.chart-card {
  min-height: 330px;
}
.chart-card h3 {
  margin-top: 0;
}
.bar-row {
  display: grid;
  grid-template-columns: 88px 1fr 52px;
  align-items: center;
  gap: 10px;
  margin: 14px 0;
}
.bar-row span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
}
.bar-track {
  height: 22px;
  background: #eef2f7;
  border-radius: 4px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: #67c23a;
}
.bar-row b {
  color: #303133;
}
.line-chart {
  width: 100%;
  height: 260px;
}
</style>
