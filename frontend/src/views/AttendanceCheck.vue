<template>
  <div class="page-container">
    <el-row :gutter="20">
      <!-- 人脸识别打卡（独占整行） -->
      <el-col :span="24">
        <div class="content-card check-card">
          <h3>人脸识别打卡</h3>

          <!-- 今日打卡状态提醒 -->
          <div class="today-status">
            <el-alert v-if="todayStatus.checkedIn && todayStatus.checkedOut"
              title="今日已完成签到签退" type="success" :closable="false" show-icon />
            <el-alert v-else-if="todayStatus.checkedIn"
              title="已签到，请记得签退" type="warning" :closable="false" show-icon />
            <el-alert v-else title="今日尚未签到" type="info" :closable="false" show-icon />
          </div>

          <!-- 当前时间 -->
          <div class="current-time">
            <div class="time-display">{{ currentTime }}</div>
            <div class="date-display">{{ currentDate }}</div>
          </div>

          <!-- 打卡状态信息 -->
          <div class="check-info" v-if="todayStatus.checkInTime">
            <p>签到时间: {{ todayStatus.checkInTime }}</p>
            <p v-if="todayStatus.checkOutTime">签退时间: {{ todayStatus.checkOutTime }}</p>
            <p>状态: <el-tag :type="statusTagType">{{ statusText }}</el-tag></p>
          </div>

          <!-- 人脸识别区域 -->
          <div class="face-checkin-area">
            <!-- 摄像头区域：始终保留video元素避免销毁，用v-show切换显示 -->
            <div class="face-camera-box">
              <!-- 摄像头实时画面 -->
              <video ref="videoRef" autoplay playsinline
                     style="width:100%;max-height:320px;border-radius:8px;background:#1a1a2e"
                     v-show="cameraReady && !capturedImage" />
              <!-- 拍照预览 -->
              <img v-show="capturedImage" :src="capturedImage"
                   style="width:100%;max-height:320px;border-radius:8px" />
              <!-- 加载中 -->
              <div v-if="!cameraReady && !cameraError && !capturedImage" class="camera-placeholder">
                <el-icon :size="60" color="#409eff"><Camera /></el-icon>
                <p>正在启动摄像头...</p>
              </div>
              <!-- 错误状态 -->
              <div v-if="cameraError && !capturedImage" class="camera-placeholder camera-error-box">
                <el-icon :size="60" color="#f56c6c"><WarningFilled /></el-icon>
                <p class="error-text">{{ cameraError }}</p>
                <el-button type="primary" size="small" @click="startCamera" style="margin-top:12px">
                  重新连接
                </el-button>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div style="margin-top:16px;display:flex;gap:12px;justify-content:center">
              <el-button v-if="cameraReady && !capturedImage"
                         type="primary" @click="captureFrame" :icon="Camera" size="large">
                拍照
              </el-button>
              <el-button v-if="capturedImage" @click="retakePhoto" :icon="Refresh" size="large">
                重新拍照
              </el-button>
              <el-button v-if="capturedImage" type="success" size="large"
                         @click="submitFaceCheckIn" :loading="faceChecking"
                         :disabled="todayStatus.checkedIn && todayStatus.checkedOut">
                {{ todayStatus.checkedIn ? '确认签退' : '确认打卡' }}
              </el-button>
            </div>

            <!-- 结果提示 -->
            <div v-if="faceResult" style="margin-top:16px;text-align:center">
              <el-tag :type="faceResult.matched ? 'success' : 'danger'" size="large">
                {{ faceResult.message }}
              </el-tag>
              <p v-if="!faceResult.matched" style="color:#909399;margin-top:8px;font-size:13px">
                比对未通过不会记录考勤，请调整角度和光线后重新拍照
              </p>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera, WarningFilled, Refresh } from '@element-plus/icons-vue'
import { faceCheckIn as faceCheckInApi, getTodayStatus } from '@/api'

const faceChecking = ref(false)
const faceResult = ref(null)
const todayStatus = reactive({ checkedIn: false, checkedOut: false, checkInTime: '', checkOutTime: '', status: '' })

const currentTime = ref('')
const currentDate = ref('')

// 摄像头相关
const videoRef = ref(null)
const cameraReady = ref(false)
const cameraError = ref('')
const capturedImage = ref(null)
let mediaStream = null

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
}

const statusText = computed(() => {
  const m = { NORMAL: '正常', LATE: '迟到', EARLY: '早退', ABSENT: '缺勤' }
  return m[todayStatus.status] || todayStatus.status
})
const statusTagType = computed(() => {
  const m = { NORMAL: 'success', LATE: 'warning', EARLY: 'warning', ABSENT: 'danger' }
  return m[todayStatus.status] || 'info'
})

async function loadStatus() {
  try {
    const res = await getTodayStatus()
    Object.assign(todayStatus, res.data)
  } catch (e) { }
}

// 启动摄像头
async function startCamera() {
  cameraError.value = ''
  cameraReady.value = false
  capturedImage.value = null

  try {
    mediaStream = await navigator.mediaDevices.getUserMedia({
      video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' }
    })
    if (videoRef.value) {
      videoRef.value.srcObject = mediaStream
      cameraReady.value = true
    }
  } catch (err) {
    if (err.name === 'NotAllowedError') {
      cameraError.value = '摄像头权限被拒绝，请在浏览器设置中允许摄像头访问'
    } else if (err.name === 'NotFoundError') {
      cameraError.value = '未检测到摄像头设备，请确认摄像头已连接'
    } else if (err.name === 'NotReadableError') {
      cameraError.value = '摄像头被其他应用占用，请关闭其他程序后重试'
    } else {
      cameraError.value = '摄像头启动失败: ' + err.message
    }
  }
}

// 拍照：video → canvas → base64
function captureFrame() {
  if (!videoRef.value) return
  const canvas = document.createElement('canvas')
  const vw = videoRef.value.videoWidth || 640
  const vh = videoRef.value.videoHeight || 480
  canvas.width = Math.min(vw, 800)
  canvas.height = Math.min(vh, 600)
  const ctx = canvas.getContext('2d')
  ctx.drawImage(videoRef.value, 0, 0, canvas.width, canvas.height)
  capturedImage.value = canvas.toDataURL('image/jpeg', 0.85)
}

// 重新拍照
function retakePhoto() {
  capturedImage.value = null
  faceResult.value = null
}

// 人脸识别打卡
async function submitFaceCheckIn() {
  if (!capturedImage.value) {
    ElMessage.warning('请先拍照')
    return
  }
  faceChecking.value = true
  faceResult.value = null
  try {
    // 去掉 data:image/jpeg;base64, 前缀
    const base64Data = capturedImage.value.replace(/^data:image\/\w+;base64,/, '')
    const res = await faceCheckInApi({
      type: 'FACE',
      employeeId: null,
      faceData: base64Data
    })
    faceResult.value = res.data
    if (res.data.matched) {
      ElMessage.success(res.data.message || '人脸识别打卡成功')
      capturedImage.value = null
      loadStatus()
    } else {
      ElMessage.error(res.data.message || '人脸识别失败')
    }
  } catch (e) {
    // Axios 拦截器已处理错误提示
  } finally {
    faceChecking.value = false
  }
}

onMounted(() => {
  updateTime()
  loadStatus()
  setInterval(updateTime, 1000)
  startCamera()
})

// 组件卸载时释放摄像头
onBeforeUnmount(() => {
  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop())
  }
})
</script>

<style scoped>
.check-card {
  min-height: 400px;
}
.check-card h3 {
  margin-top: 0;
  margin-bottom: 20px;
}
.today-status {
  margin-bottom: 20px;
}
.current-time {
  text-align: center;
  margin: 20px 0;
}
.time-display {
  font-size: 48px;
  font-weight: bold;
  color: #303133;
}
.date-display {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.check-info {
  text-align: center;
  color: #606266;
  margin-bottom: 20px;
}
.check-info p {
  margin: 4px 0;
}
.face-checkin-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}
.face-camera-box {
  width: 100%;
  max-width: 500px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}
.camera-placeholder {
  text-align: center;
  padding: 50px 20px;
}
.camera-placeholder p {
  color: #909399;
  margin-top: 16px;
}
.camera-error-box {
  padding: 30px 20px;
}
.error-text {
  color: #f56c6c !important;
  font-size: 14px;
  max-width: 350px;
  margin: 16px auto 0;
}
</style>