<template>
  <el-container style="height: 100%">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar" style="transition: width 0.3s">
      <div class="logo">
        <span v-if="!isCollapse">📋 考勤管理系统</span>
        <span v-else>📋</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
        style="border-right: none"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/attendance/check">
          <el-icon><Camera /></el-icon>
          <template #title>人脸签到</template>
        </el-menu-item>
        <el-menu-item index="/attendance/records">
          <el-icon><Document /></el-icon>
          <template #title>打卡记录</template>
        </el-menu-item>
        <el-menu-item index="/leave">
          <el-icon><Notebook /></el-icon>
          <template #title>请假申请</template>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/attendance/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>考勤统计</template>
        </el-menu-item>
        <el-menu-item v-if="isAdmin" index="/employee">
          <el-icon><User /></el-icon>
          <template #title>员工管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button @click="isCollapse = !isCollapse" :icon="isCollapse ? 'Expand' : 'Fold'" text />
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" style="background: #409eff">{{ userStore.realName?.charAt(0) || 'U' }}</el-avatar>
              <span style="margin-left: 8px">{{ userStore.realName }}</span>
              <el-tag size="small" style="margin-left: 8px">{{ userStore.role }}</el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="refreshInfo">刷新信息</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main style="background: #f5f7fa; padding: 20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { Camera, Document, Notebook, DataAnalysis, User, HomeFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

// 管理员才显示员工管理
const isAdmin = computed(() => {
  const role = userStore.role
  return ['系统管理员', '管理员', 'ROLE_ADMIN'].includes(role)
})

const activeMenu = ref(route.path)
watch(() => route.path, (val) => { activeMenu.value = val })

const currentTitle = computed(() => {
  const matched = route.matched
  return matched.length > 1 ? matched[matched.length - 1].meta?.title : ''
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

function refreshInfo() {
  userStore.refreshUserInfo()
}
</script>

<style scoped>
.sidebar {
  background: #304156;
  overflow-y: auto;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background: #2b3a4a;
  white-space: nowrap;
  overflow: hidden;
}
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,0,0,.08);
  z-index: 10;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}
</style>
