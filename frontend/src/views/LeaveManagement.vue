<template>
  <div class="page-container">
    <!-- 管理员：待审批Tab -->
    <el-tabs v-if="isAdmin" v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="我的申请" name="own" />
      <el-tab-pane label="待审批" name="pending" />
    </el-tabs>
    <h3 v-else style="margin:0 0 16px">请假管理</h3>

    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px" v-if="activeTab === 'own' || !isAdmin">
      <span style="color:#909399;font-size:13px">共 {{ total }} 条记录</span>
      <el-button type="primary" @click="drawerVisible = true">申请请假</el-button>
    </div>

    <!-- 我的申请表格 -->
    <div class="content-card" v-if="activeTab === 'own' || !isAdmin">
      <el-table :data="tableData" stripe v-loading="loading" style="width:100%">
        <el-table-column prop="employeeName" label="申请人" width="100" />
        <el-table-column label="类型" width="80">
          <template #default="{row}"><el-tag size="small">{{ typeText(row.leaveType) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始" width="110" />
        <el-table-column prop="endDate" label="结束" width="110" />
        <el-table-column label="时长" width="80"><template #default="{row}">{{ row.duration }}天</template></el-table-column>
        <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{row}"><el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="approverName" label="审批人" width="80" />
        <el-table-column prop="approveComment" label="审批意见" width="120" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="申请时间" width="160" />
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size"
        @current-change="loadData" layout="total, prev, pager, next" style="margin-top:16px" />
    </div>

    <!-- 管理员：待审批表格 -->
    <div class="content-card" v-if="isAdmin && activeTab === 'pending'">
      <el-table :data="pendingData" stripe v-loading="pendingLoading" style="width:100%">
        <el-table-column prop="employeeName" label="申请人" width="100" />
        <el-table-column label="类型" width="80">
          <template #default="{row}"><el-tag size="small">{{ typeText(row.leaveType) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始" width="110" />
        <el-table-column prop="endDate" label="结束" width="110" />
        <el-table-column label="时长" width="80"><template #default="{row}">{{ row.duration }}天</template></el-table-column>
        <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="申请时间" width="160" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-input v-model="approveComments[row.id]" placeholder="审批意见" size="small" style="width:80px;margin-right:4px" />
            <el-button type="success" size="small" @click="doApprove(row.id, 'APPROVED')">批准</el-button>
            <el-button type="danger" size="small" @click="doApprove(row.id, 'REJECTED')">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pendingPage" :total="pendingTotal" :page-size="pendingSize"
        @current-change="loadPending" layout="total, prev, pager, next" style="margin-top:16px" />
    </div>

    <!-- 请假申请抽屉 -->
    <el-drawer v-model="drawerVisible" title="请假申请" size="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" style="width:100%">
            <el-option v-for="t in leaveTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="时长"><el-input-number v-model="form.duration" :min="0.5" :step="0.5" :precision="1" /> 天</el-form-item>
        <el-form-item label="请假原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请详细说明请假原因" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitLeave" :loading="submitting">提交申请</el-button>
          <el-button @click="drawerVisible = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLeaveList, submitLeave as submitLeaveApi, approveLeave } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isAdmin = computed(() => {
  const r = userStore.role
  return r === '系统管理员' || r === '管理员' || r === '部门经理'
})

const loading = ref(false), submitting = ref(false), drawerVisible = ref(false)
const tableData = ref([]), total = ref(0), page = ref(1), size = ref(10)
const formRef = ref(null)

// 待审批
const activeTab = ref('own')
const pendingLoading = ref(false)
const pendingData = ref([]), pendingTotal = ref(0), pendingPage = ref(1), pendingSize = ref(10)
const approveComments = reactive({})

const leaveTypes = [
  {value:'SICK',label:'病假'},{value:'PERSONAL',label:'事假'},{value:'ANNUAL',label:'年假'},
  {value:'MARRIAGE',label:'婚假'},{value:'MATERNITY',label:'产假'},{value:'OTHER',label:'其他'}
]

const form = reactive({ leaveType:'SICK', startDate:'', endDate:'', duration:1, reason:'' })
const rules = {
  leaveType:[{required:true,message:'请选择类型'}],
  startDate:[{required:true,message:'请选择开始日期'}],
  endDate:[{required:true,message:'请选择结束日期'}],
  reason:[{required:true,message:'请填写请假原因',trigger:'blur'}]
}

function typeText(t) { const m = {SICK:'病假',PERSONAL:'事假',ANNUAL:'年假',MARRIAGE:'婚假',MATERNITY:'产假',OTHER:'其他'}; return m[t]||t }
function statusTag(s) { return {PENDING:'warning',APPROVED:'success',REJECTED:'danger'}[s]||'info' }
function statusText(s) { return {PENDING:'待审批',APPROVED:'已批准',REJECTED:'已拒绝'}[s]||s }

function onTabChange(tab) {
  if (tab === 'pending') loadPending()
  else loadData()
}

async function loadData() {
  loading.value = true
  try {
    const res = await getLeaveList({ page: page.value, size: size.value })
    tableData.value = res.data?.list || []; total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function loadPending() {
  pendingLoading.value = true
  try {
    const res = await getLeaveList({ page: pendingPage.value, size: pendingSize.value, status: 'PENDING' })
    pendingData.value = res.data?.list || []; pendingTotal.value = res.data?.total || 0
  } finally { pendingLoading.value = false }
}

async function doApprove(id, status) {
  const comment = approveComments[id] || (status === 'APPROVED' ? '同意' : '不同意')
  const action = status === 'APPROVED' ? '批准' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定${action}该请假申请吗？`, '确认审批', { type: 'warning' })
    await approveLeave({ requestId: id, status, comment })
    ElMessage.success(`已${action}`)
    delete approveComments[id]
    loadPending()
  } catch (e) {}
}

async function submitLeave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await submitLeaveApi(form)
    ElMessage.success('请假申请已提交，等待审批')
    drawerVisible.value = false
    loadData()
  } catch(e) {} finally { submitting.value = false }
}

onMounted(loadData)
</script>