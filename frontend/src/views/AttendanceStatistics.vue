<template>
  <div class="page-container">
    <h3 style="margin:0 0 16px 0">考勤统计</h3>
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="6" v-for="c in statCards" :key="c.label">
        <div class="stat-card">
          <div class="label">{{ c.label }}</div>
          <div class="value" :style="{color:c.color}">{{ c.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <!-- 各部门出勤率 - 纯CSS柱状图 -->
      <el-col :span="12">
        <div class="content-card">
          <h4>各部门出勤率对比</h4>
          <div class="bar-chart">
            <div class="bar-row" v-for="d in deptAttendance" :key="d.name">
              <span class="bar-label">{{ d.name }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{width: d.value + '%', background: barColor(d.value)}" />
              </div>
              <span class="bar-value">{{ d.value }}%</span>
            </div>
            <div v-if="deptAttendance.length === 0" style="color:#909399;text-align:center;padding:40px;font-size:14px">
              暂无数据
            </div>
          </div>
        </div>
      </el-col>

      <!-- 近7天趋势 - 纯CSS折线图 -->
      <el-col :span="12">
        <div class="content-card">
          <h4>近7天出勤率趋势</h4>
          <div class="line-chart" v-if="dailyTrend.length > 0">
            <svg viewBox="0 0 500 250" style="width:100%;max-height:300px">
              <!-- 背景虚线 -->
              <line v-for="y in 5" :key="'h'+y" :x1="40" :y1="50*y-10" :x2="480" :y2="50*y-10"
                stroke="#ebeef5" stroke-dasharray="5,5" />
              <!-- Y轴标签 -->
              <text v-for="y in 5" :key="'yl'+y" x="35" :y="50*y-5" text-anchor="end"
                font-size="11" fill="#909399">{{ 120 - y * 20 }}%</text>
              <!-- 折线 -->
              <polyline
                :points="linePoints"
                fill="none" stroke="#67c23a" stroke-width="2.5" />
              <!-- 填充区域 -->
              <polygon
                :points="areaPoints"
                fill="rgba(103,194,58,0.12)" />
              <!-- 数据点 -->
              <circle v-for="(p, idx) in linePointsArr" :key="'c'+idx"
                :cx="p.x" :cy="p.y" r="4" fill="#67c23a" />
              <!-- X轴标签 -->
              <text v-for="(d, idx) in dailyTrend" :key="'xl'+idx"
                :x="linePointsArr[idx].x" :y="245"
                text-anchor="middle" font-size="11" fill="#909399">{{ d.name }}</text>
            </svg>
          </div>
          <div v-else style="color:#909399;text-align:center;padding:40px;font-size:14px">
            暂无数据
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getAttendanceStatistics } from '@/api'

const stats = reactive({totalEmployees:0,todayCheckIn:0,todayAbsent:0,todayLate:0,pendingLeaves:0,onDuty:0,attendanceRate:0})
const deptAttendance = ref([])
const dailyTrend = ref([])

const statCards = computed(() => [
  {label:'在职员工',value:stats.totalEmployees+'人',color:'#409eff'},
  {label:'今日已签到',value:stats.todayCheckIn+'人',color:'#67c23a'},
  {label:'今日迟到',value:stats.todayLate+'人',color:'#e6a23c'},
  {label:'出勤率',value:stats.attendanceRate+'%',color:'#67c23a'}
])

// 纯CSS柱状图颜色
function barColor(val) {
  if (val >= 80) return '#67c23a'
  if (val >= 60) return '#e6a23c'
  return '#f56c6c'
}

// SVG 折线图坐标计算
const linePointsArr = computed(() => {
  const data = dailyTrend.value
  if (data.length === 0) return []
  const maxVal = Math.max(...data.map(d => d.value), 100)
  const chartH = 200, chartW = 440, startX = 40, startY = 10
  return data.map((d, i) => ({
    x: startX + (i / Math.max(data.length - 1, 1)) * chartW,
    y: startY + chartH - (d.value / maxVal) * chartH
  }))
})

const linePoints = computed(() =>
  linePointsArr.value.map(p => `${p.x},${p.y}`).join(' ')
)

const areaPoints = computed(() => {
  const pts = linePointsArr.value
  if (pts.length === 0) return ''
  const first = `${pts[0].x},220`
  const last = `${pts[pts.length-1].x},220`
  const middle = pts.map(p => `${p.x},${p.y}`).join(' ')
  return first + ' ' + middle + ' ' + last
})

async function loadData() {
  try {
    const res = await getAttendanceStatistics()
    Object.assign(stats, res.data)
    deptAttendance.value = res.data.departmentAttendance || []
    dailyTrend.value = res.data.dailyTrend || []
  } catch(e) {}
}

onMounted(loadData)
</script>

<style scoped>
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.stat-card .label { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-card .value { font-size: 28px; font-weight: bold; }

/* 纯CSS柱状图 */
.bar-chart { padding: 12px 16px; }
.bar-row { display: flex; align-items: center; margin-bottom: 12px; }
.bar-label { width: 60px; font-size: 13px; color: #606266; flex-shrink: 0; text-align: right; margin-right: 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bar-track { flex: 1; height: 22px; background: #f0f2f5; border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; transition: width 0.4s ease; min-width: 2px; }
.bar-value { width: 45px; font-size: 13px; color: #303133; text-align: right; margin-left: 8px; flex-shrink: 0; }

.line-chart { padding: 8px; }
svg { display: block; margin: 0 auto; }
</style>