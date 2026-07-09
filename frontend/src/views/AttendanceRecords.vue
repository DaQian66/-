<template>
  <div class="page-container">
    <div class="search-bar">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-select v-model="query.departmentId" placeholder="按部门筛选" clearable style="width:100%">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="query.status" placeholder="考勤状态" clearable style="width:100%">
            <el-option label="正常" value="NORMAL" /><el-option label="迟到" value="LATE" />
            <el-option label="早退" value="EARLY" /><el-option label="缺勤" value="ABSENT" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期" format="YYYY-MM-DD"
            value-format="YYYY-MM-DD" @change="loadData" style="width:100%" />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="exportExcel">导出Excel</el-button>
        </el-col>
      </el-row>
    </div>
    <div class="content-card">
      <el-table :data="tableData" stripe v-loading="loading" style="width:100%">
        <el-table-column prop="employeeName" label="员工" width="100" />
        <el-table-column prop="employeeNo" label="工号" width="100" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="attendanceDate" label="日期" width="110" />
        <el-table-column prop="checkInTime" label="签到时间" width="100" />
        <el-table-column prop="checkOutTime" label="签退时间" width="100" />
        <el-table-column label="打卡方式" width="100">
          <template #default="{row}">
            <el-tag :type="row.checkInType==='FACE'?'success':'info'" size="small">
              {{ row.checkInType==='FACE'?'人脸':'密码' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
      </el-table>
      <el-pagination v-model:current-page="query.page" :total="total" :page-size="query.size"
        @current-change="loadData" layout="total, prev, pager, next" style="margin-top:16px" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAttendanceRecords, getDepartments, exportAttendanceRecords } from '@/api'

const loading = ref(false), tableData = ref([]), total = ref(0), departments = ref([])
const dateRange = ref([])
const query = reactive({ departmentId: null, status: null, startDate: null, endDate: null, page: 1, size: 10 })

function statusType(s) { return {NORMAL:'success',LATE:'warning',EARLY:'warning',ABSENT:'danger'}[s] || 'info' }
function statusText(s) { return {NORMAL:'正常',LATE:'迟到',EARLY:'早退',ABSENT:'缺勤'}[s] || s }

async function loadData() {
  loading.value = true
  try {
    if (dateRange.value && dateRange.value.length === 2) {
      query.startDate = dateRange.value[0]; query.endDate = dateRange.value[1]
    } else { query.startDate = null; query.endDate = null }
    const res = await getAttendanceRecords(query)
    tableData.value = res.data?.list || []; total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function exportExcel() {
  if (dateRange.value && dateRange.value.length === 2) {
    query.startDate = dateRange.value[0]
    query.endDate = dateRange.value[1]
  } else {
    query.startDate = null
    query.endDate = null
  }

  const res = await exportAttendanceRecords({
    departmentId: query.departmentId,
    status: query.status,
    startDate: query.startDate,
    endDate: query.endDate
  })
  const blob = new Blob([res.data], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `attendance-records-${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

async function loadDepts() {
  try { const res = await getDepartments(); departments.value = res.data || [] } catch(e) {}
}

onMounted(() => { loadData(); loadDepts() })
</script>
