<template>
  <div class="page-container">
    <div class="search-bar">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input v-model="search.keyword" placeholder="搜索姓名/工号" clearable @clear="loadData" />
        </el-col>
        <el-col :span="6">
          <el-select v-model="search.departmentId" placeholder="选择部门" clearable @change="loadData" style="width:100%">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="search.status" placeholder="状态" clearable @change="loadData" style="width:100%">
            <el-option label="在职" :value="1" />
            <el-option label="离职" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button type="success" @click="showAddDialog" v-if="isAdmin">新增员工</el-button>
        </el-col>
      </el-row>
    </div>

    <div class="content-card">
      <div v-if="isAdmin" style="display:flex;gap:8px;margin-bottom:12px">
        <el-button size="small" type="success" :disabled="selectedRows.length===0" @click="batchStatus(1)">批量设为在职</el-button>
        <el-button size="small" type="warning" :disabled="selectedRows.length===0" @click="batchStatus(0)">批量设为离职</el-button>
      </div>
      <el-table :data="tableData" stripe v-loading="loading" style="width:100%" @selection-change="selectedRows = $event">
        <el-table-column type="selection" width="45" v-if="isAdmin" />
        <el-table-column prop="employeeNo" label="工号" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="departmentName" label="部门" width="140" />
        <el-table-column prop="position" label="职位" width="120" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="entryDate" label="入职日期" width="110" />
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':'info'" size="small">{{row.status===1?'在职':'离职'}}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link @click="showEditDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="search.page" :total="total" :page-size="search.size"
        @current-change="loadData" layout="total, prev, pager, next" style="margin-top:16px" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑员工':'新增员工'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="工号" prop="employeeNo"><el-input v-model="form.employeeNo" :disabled="isEdit" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="性别"><el-select v-model="form.gender" style="width:100%"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="部门"><el-select v-model="form.departmentId" style="width:100%"><el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="职位"><el-input v-model="form.position" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="入职日期"><el-date-picker v-model="form.entryDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16" v-if="!isEdit">
          <el-col :span="12">
            <el-form-item label="登录账号" prop="username" :rules="[{required:true,message:'请输入登录账号',trigger:'blur'}]">
              <el-input v-model="form.username" placeholder="员工登录用" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="登录密码" prop="password" :rules="[{required:true,message:'请输入密码',trigger:'blur'},{min:6,message:'密码至少6位',trigger:'blur'}]">
              <el-input v-model="form.password" type="password" show-password placeholder="最少6位" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="人脸照片">
              <div style="display:flex;align-items:center;gap:12px">
                <el-upload
                  :show-file-list="false"
                  :before-upload="handleFaceUpload"
                  accept="image/*"
                  :disabled="faceUploading">
                  <el-button :loading="faceUploading" :icon="Upload">
                    {{ faceUploading ? '上传中...' : '选择照片' }}
                  </el-button>
                </el-upload>
                <el-tag v-if="form.faceImage" type="success" size="small">
                  已上传人脸照片
                  <el-button type="danger" link size="small" @click="form.faceImage=''"
                    style="margin-left:4px">删除</el-button>
                </el-tag>
                <span v-else style="color:#909399;font-size:12px">用于人脸识别打卡，请上传清晰正面照</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="form.faceImage">
          <el-col :span="24">
            <el-form-item label="预览">
              <img :src="form.faceImage" style="max-width:200px;max-height:200px;border-radius:4px;border:1px solid #dcdfe6" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { getEmployees, createEmployee, updateEmployee, deleteEmployee, getDepartments, uploadFile, batchUpdateEmployeeStatus } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isAdmin = computed(() => ['系统管理员', '管理员', 'ROLE_ADMIN'].includes(userStore.role))
const loading = ref(false), dialogVisible = ref(false), isEdit = ref(false)
const tableData = ref([]), departments = ref([]), total = ref(0)
const formRef = ref(null)
const selectedRows = ref([])

const search = reactive({ keyword:'', departmentId:null, status:null, page:1, size:10 })
const form = reactive({ id:null, employeeNo:'', realName:'', gender:'男', departmentId:null, position:'', phone:'', email:'', entryDate:'', faceImage:'', username:'', password:'' })
const faceUploading = ref(false)

const rules = {
  employeeNo: [{ required:true, message:'请输入工号', trigger:'blur' }],
  realName: [{ required:true, message:'请输入姓名', trigger:'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getEmployees(search)
    tableData.value = res.data?.list || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function loadDepts() {
  const res = await getDepartments()
  departments.value = res.data || []
}

function showAddDialog() {
  isEdit.value = false
  Object.assign(form, { id:null, employeeNo:'', realName:'', gender:'男', departmentId:null, position:'', phone:'', email:'', entryDate:'', faceImage:'', username:'', password:'' })
  dialogVisible.value = true
}

function showEditDialog(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(()=>false)
  if (!valid) return
  try {
    if (isEdit.value) await updateEmployee(form.id, form)
    else await createEmployee(form)
    ElMessage.success(isEdit.value?'更新成功':'新增成功')
    dialogVisible.value = false
    loadData()
  } catch(e) {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除员工 ${row.realName} 吗？`, '确认', { type:'warning' })
  try { await deleteEmployee(row.id); ElMessage.success('删除成功'); loadData() } catch(e) {}
}

async function batchStatus(status) {
  const ids = selectedRows.value.map(row => row.id)
  if (ids.length === 0) {
    ElMessage.warning('请先选择员工')
    return
  }
  const text = status === 1 ? '在职' : '离职'
  await ElMessageBox.confirm(`确定将选中的 ${ids.length} 名员工设为${text}吗？`, '批量状态变更', { type:'warning' })
  await batchUpdateEmployeeStatus(ids, status)
  ElMessage.success('批量状态更新成功')
  selectedRows.value = []
  loadData()
}

async function handleFaceUpload(file) {
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('人脸照片大小不能超过5MB')
    return false
  }
  faceUploading.value = true
  try {
    const res = await uploadFile(file)
    form.faceImage = res.data.url
    ElMessage.success('人脸上传成功')
  } catch (e) {
    // error handled by interceptor
  } finally {
    faceUploading.value = false
  }
  return false // prevent default upload behavior
}

onMounted(() => { loadData(); loadDepts() })
</script>
