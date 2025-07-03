<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button 
            type="primary" 
            @click="addUser"
            v-permission="['admin']"
          >
            添加用户
          </el-button>
        </div>
      </template>
      
      <div class="search-bar">
        <el-input 
          v-model="searchQuery" 
          placeholder="搜索用户名/手机号" 
          clearable
          @clear="fetchUsers"
          @keyup.enter="searchUsers"
        >
          <template #append>
            <el-button icon="Search" @click="searchUsers" />
          </template>
        </el-input>
        
        <el-select 
          v-model="statusFilter" 
          placeholder="状态筛选" 
          clearable 
          @change="fetchUsers"
          style="margin-left: 15px; width: 120px"
        >
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </div>
      
      <el-table :data="users" border v-loading="loading">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="telephone" label="电话" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="角色">
          <template #default="{ row }">
            {{ row.role === 0 ? '超级管理员' : '企业用户' }}
          </template>
        </el-table-column>
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button 
              size="small" 
              @click="editUser(row)"
              v-permission="['admin']"
            >
              编辑
            </el-button>
            <el-button 
              size="small" 
              :type="row.status === 1 ? 'danger' : 'success'" 
              @click="toggleUserStatus(row)"
              v-permission="['admin']"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchUsers"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog v-model="showDialog" :title="dialogTitle">
      <el-form :model="currentUser" label-width="80px" ref="userForm">
        <el-form-item label="用户名" prop="username" required>
          <el-input v-model="currentUser.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!currentUser.uid" required>
          <el-input v-model="currentUser.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="currentUser.nickname" />
        </el-form-item>
        <el-form-item label="电话" prop="telephone">
          <el-input v-model="currentUser.telephone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="currentUser.email" />
        </el-form-item>
        <el-form-item label="角色" prop="role" v-if="currentUser.role !== 0">
          <el-select v-model="currentUser.role">
            <el-option label="企业用户" :value="1" />
            <el-option label="超级管理员" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="currentUser.status">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/services/api'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'

const users = ref([])
const currentUser = ref({})
const showDialog = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const userForm = ref(null)

const dialogTitle = computed(() => 
  currentUser.value.uid ? '编辑用户' : '添加用户'
)

onMounted(async () => {
  await fetchUsers()
})

const fetchUsers = async () => {
  try {
    loading.value = true
    const params = {
      username: searchQuery.value?.trim() || undefined,
      telephone: searchQuery.value?.trim() || undefined,
      status: statusFilter.value
    }
    
    const res = await api.getUsers(params)
    
    // 处理不同的响应格式
    if (Array.isArray(res)) {
      users.value = res
      total.value = res.length
    } else if (res?.data && Array.isArray(res.data)) {
      users.value = res.data
      total.value = res.data.length
    } else {
      users.value = []
      total.value = 0
      ElMessage.warning('没有用户数据')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const searchUsers = () => {
  currentPage.value = 1
  fetchUsers()
}

const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
  fetchUsers()
}

const addUser = () => {
  currentUser.value = {
    username: '',
    password: '',
    nickname: '',
    telephone: '',
    email: '',
    role: 1,
    status: 1
  }
  showDialog.value = true
}

const editUser = (user) => {
  currentUser.value = { ...user }
  showDialog.value = true
}

const toggleUserStatus = (user) => {
  const action = user.status === 1 ? '禁用' : '启用'
  const newStatus = user.status === 1 ? 0 : 1
  
  ElMessageBox.confirm(`确定要${action}该用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 显示加载状态
      const loadingInstance = ElLoading.service({
        lock: true,
        text: `${action}中...`,
        background: 'rgba(0, 0, 0, 0.7)'
      })
      
      // 调用API
      await api.updateUserStatus(user.uid, newStatus)
      
      // 更新本地数据
      user.status = newStatus
      
      // 关闭加载状态
      loadingInstance.close()
      
      ElMessage.success(`${action}成功`)
    } catch (error) {
      console.error(`${action}用户失败:`, error)
      
      // 详细的错误处理
      let errorMsg = `${action}失败`
      
      if (error.response) {
        // 从响应中提取错误信息
        const data = error.response.data
        if (typeof data === 'string') {
          errorMsg += `: ${data}`
        } else if (data && data.message) {
          errorMsg += `: ${data.message}`
        } else if (data && typeof data === 'object') {
          // 尝试解析对象中的错误信息
          errorMsg += ': ' + JSON.stringify(data)
        } else {
          errorMsg += ` (状态码: ${error.response.status})`
        }
      } else if (error.message) {
        errorMsg += `: ${error.message}`
      }
      
      ElMessage.error(errorMsg)
    }
  }).catch(() => {})
}

const saveUser = async () => {
  try {
    await userForm.value.validate()
    
    if (currentUser.value.uid) {
      await api.updateUser(currentUser.value)
    } else {
      await api.createUser(currentUser.value)
    }
    
    ElMessage.success('保存成功')
    showDialog.value = false
    await fetchUsers()
  } catch (error) {
    console.error('保存用户失败:', error)
    if (error.fields) {
      ElMessage.error('请填写必填字段')
    } else {
      ElMessage.error('保存失败: ' + (error.message || '未知错误'))
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.search-bar {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>