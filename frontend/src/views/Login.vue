<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-shape shape1"></div>
      <div class="bg-shape shape2"></div>
      <div class="bg-shape shape3"></div>
    </div>
    <div class="login-card">
      <div class="login-header">
        <h1>员工考勤管理系统</h1>
        <p>Employee Attendance Management System</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"
            prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin"
            style="width: 100%; height: 44px; font-size: 16px">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>默认账号: admin / zhangsan / lisi | 密码: 123456</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    router.push('/')
  } catch (e) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}
.login-bg {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
}
.bg-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}
.shape1 {
  width: 600px; height: 600px;
  background: #409eff;
  top: -200px; right: -100px;
  animation: float 8s ease-in-out infinite;
}
.shape2 {
  width: 400px; height: 400px;
  background: #67c23a;
  bottom: -100px; left: -100px;
  animation: float 10s ease-in-out infinite reverse;
}
.shape3 {
  width: 300px; height: 300px;
  background: #e6a23c;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  animation: float 12s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(30px, -30px) rotate(10deg); }
}
.login-card {
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 48px 40px;
  width: 420px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
  z-index: 1;
  animation: slideUp 0.5s ease-out;
}
@keyframes slideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
.login-header {
  text-align: center;
  margin-bottom: 40px;
}
.login-header h1 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
}
.login-header p {
  font-size: 13px;
  color: #909399;
  margin: 0;
}
.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 12px;
  color: #c0c4cc;
}
</style>