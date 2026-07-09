import request from './request'

// 登录认证
export const login = (data) => request.post('/login', data)
export const getUserInfo = () => request.get('/user/info')

// 员工管理
export const getEmployees = (params) => request.get('/employees', { params })
export const createEmployee = (data) => request.post('/employees', data)
export const updateEmployee = (id, data) => request.put(`/employees/${id}`, data)
export const deleteEmployee = (id) => request.delete(`/employees/${id}`)
export const batchUpdateEmployeeStatus = (ids, status) => request.put('/employees/batch-status', { ids, status })

// 人脸识别打卡
export const faceCheckIn = (data) => request.post('/attendance/face-check-in', data)
export const getTodayStatus = () => request.get('/attendance/today-status')
export const getAttendanceRecords = (data) => request.post('/attendance/records', data)
export const getAttendanceStatistics = () => request.get('/attendance/statistics')
export const exportAttendanceRecords = (params) => request.get('/attendance/export', { params, responseType: 'blob' })

// 请假管理
export const submitLeave = (data) => request.post('/leave/submit', data)
export const approveLeave = (data) => request.post('/leave/approve', data)
export const getLeaveList = (params) => request.get('/leave/list', { params })

// 部门（员工表单下拉用）
export const getDepartments = () => request.get('/departments')

// 文件上传
export const uploadFile = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
