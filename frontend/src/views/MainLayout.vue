<template>
  <div class="main-layout">
    <el-container>
      <el-aside width="200px">
        <div class="user-info">
          <div class="avatar">
            <el-avatar :size="60" :src="user.avatar || avatarPlaceholder" />
          </div>
          <div class="info">
            <div class="name">{{ userName }}</div>
            <div class="role">{{ userRole }}</div>
          </div>
        </div>

        <el-menu
          router
          :default-active="activeRoute"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/main/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>

          <el-menu-item index="/main/users" v-if="user.role === 0">
            <el-icon><Setting /></el-icon>
            <span>用户管理</span>
          </el-menu-item>

          <el-menu-item index="/main/news">
            <el-icon><Reading /></el-icon>
            <span>动态管理</span>
          </el-menu-item>

          <el-menu-item index="/main/courses" v-if="user.role === 0 || user.role === 1">
            <el-icon><VideoCamera /></el-icon>
            <span>课程管理</span>
          </el-menu-item>

          <el-menu-item index="/main/meeting" v-if="user.role === 0 || user.role === 1">
            <el-icon><Calendar /></el-icon>
            <span>会议管理</span>
          </el-menu-item>

          <el-menu-item index="/main/AI" v-if="user.role === 0 || user.role === 1">
            <el-icon><ChatDotRound /></el-icon>
            <span>AI助手</span>
          </el-menu-item>
        </el-menu>

        <!-- 底部退出按钮 -->
        <div class="logout-container">
          <el-button
            type="danger"
            class="logout-btn"
            @click="handleLogout"
            plain
          >
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </el-button>
        </div>
      </el-aside>

      <el-container>
        <el-header>
          <div class="header-right">
            <el-dropdown>
              <span class="user-name">
                {{ userName }}
                <el-icon class="el-icon--right">
                  <ArrowDown />
                </el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push('/main/profile')">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  User,
  Setting,
  Reading,
  VideoCamera,
  ArrowDown,
  SwitchButton, Calendar, ChatDotRound
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()

// 用户信息
const user = computed(() => {
  const userData = localStorage.getItem('user')
  return userData ? JSON.parse(userData) : {}
})

// 头像占位图（Base64编码的默认头像）
const avatarPlaceholder = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23ffffff'%3E%3Cpath d='M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z'/%3E%3C/svg%3E"

const userName = computed(() => user.value.nickname || user.value.username || '管理员')
const userRole = computed(() => user.value.role === 0 ? '超级管理员' : '企业用户')

// 当前激活的路由
const activeRoute = ref('/main/profile')
watch(() => route.path, (newPath) => {
  activeRoute.value = newPath
}, { immediate: true })

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('user')
    router.push('/login')
  }).catch(() => {})
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.el-header {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  z-index: 10;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.user-info {
  padding: 20px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid #eee;
  background: linear-gradient(120deg, #409EFF, #64b5f6);
  color: white;
}

.avatar {
  margin-right: 15px;
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  padding: 5px;
}

.info .name {
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 5px;
}

.info .role {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.el-aside {
  background-color: #fff;
  box-shadow: 2px 0 5px rgba(0,0,0,0.1);
  transition: width 0.3s;
  position: relative;
  display: flex;
  flex-direction: column;
}

.el-menu {
  border-right: none;
  flex: 1;
}

.el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 5px 0;
  border-radius: 4px;
}

.el-menu-item.is-active {
  background-color: #ecf5ff;
  color: #409EFF;
  font-weight: 600;
}

.el-menu-item:hover {
  background-color: #f5f7fa;
}

.el-main {
  padding: 20px;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.logout-container {
  padding: 15px;
  border-top: 1px solid #eee;
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn span {
  margin-left: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .el-aside {
    width: 64px !important;
  }

  .user-info .info,
  .el-menu-item span,
  .logout-btn span {
    display: none;
  }

  .user-info {
    justify-content: center;
    padding: 15px 5px;
  }

  .avatar {
    margin-right: 0;
  }

  .logout-btn {
    padding: 0;
    justify-content: center;
  }
}
</style>