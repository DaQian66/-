import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'attendance/check', name: 'AttendanceCheck', component: () => import('@/views/AttendanceCheck.vue'), meta: { title: '人脸签到' } },
      { path: 'attendance/records', name: 'AttendanceRecords', component: () => import('@/views/AttendanceRecords.vue'), meta: { title: '打卡记录' } },
      { path: 'attendance/statistics', name: 'AttendanceStatistics', component: () => import('@/views/AttendanceStatistics.vue'), meta: { title: '考勤统计' } },
      { path: 'leave', name: 'LeaveManage', component: () => import('@/views/LeaveManagement.vue'), meta: { title: '请假申请' } },
      { path: 'employee', name: 'Employee', component: () => import('@/views/EmployeeList.vue'), meta: { title: '员工管理' } },
      { path: ':pathMatch(.*)*', redirect: '/dashboard' }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    if (token) { next('/'); return }
    next()
  } else {
    if (!token) { next('/login'); return }
    next()
  }
})

export default router
