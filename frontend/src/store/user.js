import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo } from '@/api'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    menus: JSON.parse(localStorage.getItem('menus') || '[]')
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    realName: (state) => state.userInfo?.realName || '',
    role: (state) => state.userInfo?.role || '',
    permissions: (state) => state.userInfo?.permissions || []
  },
  actions: {
    async login(username, password) {
      const res = await loginApi({ username, password })
      this.token = res.data.token
      localStorage.setItem('token', res.data.token)

      const infoRes = await getUserInfo()
      this.userInfo = infoRes.data
      this.menus = infoRes.data.menus || []
      localStorage.setItem('userInfo', JSON.stringify(infoRes.data))
      localStorage.setItem('menus', JSON.stringify(infoRes.data.menus || []))

      ElMessage.success('登录成功，欢迎 ' + (infoRes.data.realName || username))
      return true
    },
    async refreshUserInfo() {
      const infoRes = await getUserInfo()
      this.userInfo = infoRes.data
      this.menus = infoRes.data.menus || []
      localStorage.setItem('userInfo', JSON.stringify(infoRes.data))
      localStorage.setItem('menus', JSON.stringify(infoRes.data.menus || []))
    },
    logout() {
      this.token = ''
      this.userInfo = null
      this.menus = []
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('menus')
    }
  }
})