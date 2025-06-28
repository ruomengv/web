import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

// 布局和通用视图
import LoginView from '@/views/LoginView.vue'
import MainLayout from '@/views/MainLayout.vue'
import ProfileView from '@/views/ProfileView.vue'

// 会议管理视图 (新增导入)
import MeetingManagement from '@/views/meeting-management/MeetingManagement.vue'
import AddMeeting from '@/views/meeting-management/AddMeeting.vue'
import EditMeeting from '@/views/meeting-management/EditMeeting.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView
  },
  {
    path: '/main',
    component: MainLayout,
    children: [
      {
        path: 'profile',
        component: ProfileView,
        meta: { requiresAuth: true }
      },
      {
        path: 'users',
        component: () => import('@/views/UserManagement.vue'),
        meta: { requiresAdmin: true }
      },
      {
        path: 'news',
        component: () => import('@/views/NewsManagement.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'courses',
        component: () => import('@/views/CourseManagement.vue'),
        meta: { requiresAuth: true }
      },
      {
        path: 'AI',
        component: () => import('@/views/AIView.vue'),
        meta: { requiresAuth: true }
      },
      {
        // --- 修改部分开始 ---
        // 将所有会议路由整合到这里
        path: 'meeting',
        // 当访问 /main/meeting 时，自动跳转到列表页
        redirect: '/main/meeting/list',
        children: [
          {
            path: 'list', // 列表页 URL: /main/meeting/list
            name: 'MeetingList',
            component: MeetingManagement,
            meta: { requiresAuth: true }
          },
          {
            path: 'add', // 新增页 URL: /main/meeting/add
            name: 'AddMeeting',
            component: AddMeeting,
            meta: { requiresAuth: true }
          },
          {
            path: 'edit/:id', // 编辑页 URL: /main/meeting/edit/:id
            name: 'EditMeeting',
            component: EditMeeting,
            meta: { requiresAuth: true }
          }
        ]
        // --- 修改部分结束 ---
      },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 (保持不变)
router.beforeEach((to, from, next) => {
  // 从localStorage获取用户信息
  const userJson = localStorage.getItem('user')
  const user = userJson ? JSON.parse(userJson) : null

  // 检查用户是否有效
  if (user && user.status === 0) {
    ElMessage.error('您的账号已被禁用')
    localStorage.removeItem('user')
    next('/login')
    return
  }

  // 检查是否需要登录
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!user || !user.uid) {
      ElMessage.warning('请先登录')
      next({ path: '/login' })
      return
    }
  }

  // 检查管理员权限
  if (to.matched.some(record => record.meta.requiresAdmin)) {
    if (!user || user.role !== 0) {
      ElMessage.error('无权限访问')
      // 重定向到用户有权限的页面
      next(user && user.uid ? '/main/profile' : '/login')
      return
    }
  }

  next()
})

export default router