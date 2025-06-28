<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="profile-header">
          <h2>个人信息</h2>
          <el-button type="primary" @click="editMode = true" v-if="!editMode">编辑资料</el-button>
        </div>
      </template>
      
      <el-form :model="userForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" :disabled="!editMode" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.telephone" :disabled="!editMode" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" :disabled="!editMode" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="userForm.gender" :disabled="!editMode">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="创建日期">
          <el-input v-model="userForm.createTime" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-input :value="userForm.role === 0 ? '超级管理员' : '企业用户'" disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="userForm.status === 1 ? 'success' : 'danger'">
            {{ userForm.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-form-item>
        
        <el-form-item v-if="editMode">
          <el-button type="primary" @click="saveProfile">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </el-form-item>
      </el-form>
      
      <el-divider />
      
      <h3>修改密码</h3>
      <el-form :model="passwordForm" label-width="100px" class="password-form">
        <el-form-item label="旧密码" required>
          <el-input v-model="passwordForm.oldPassword" type="password" />
        </el-form-item>
        <el-form-item label="新密码" required>
          <el-input v-model="passwordForm.newPassword" type="password" />
        </el-form-item>
        <el-form-item label="确认密码" required>
          <el-input v-model="passwordForm.confirmPassword" type="password" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="changePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/services/api'
import { ElMessage } from 'element-plus'

const userForm = ref({
  uid: '',
  username: '',
  nickname: '',
  telephone: '',
  email: '',
  gender: 0,
  createTime: '',
  role: 1,
  status: 1
})

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const editMode = ref(false)
const originalUserData = ref({})

onMounted(() => {
  loadUserProfile()
})

const loadUserProfile = async () => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    if (user.uid) {
      const res = await api.getProfile(user.uid)
      userForm.value = res
      originalUserData.value = {...res}
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

const saveProfile = async () => {
  try {
    await api.updateProfile(userForm.value)
    ElMessage.success('个人信息更新成功')
    editMode.value = false
    
    // 更新本地存储的用户信息
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    user.nickname = userForm.value.nickname
    user.telephone = userForm.value.telephone
    user.email = userForm.value.email
    user.gender = userForm.value.gender
    localStorage.setItem('user', JSON.stringify(user))
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error('更新失败: ' + (error.response?.data?.message || '未知错误'))
  }
}

const cancelEdit = () => {
  userForm.value = {...originalUserData.value}
  editMode.value = false
}

const changePassword = async () => {
  if (!passwordForm.value.oldPassword || 
      !passwordForm.value.newPassword || 
      !passwordForm.value.confirmPassword) {
    ElMessage.warning('请填写所有密码字段')
    return
  }
  
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  if (passwordForm.value.newPassword.length < 6) {
    ElMessage.error('密码长度不能少于6位')
    return
  }
  
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    await api.changePassword({
      uid: user.uid,
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    
    ElMessage.success('密码修改成功')
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改失败: ' + (error.response?.data?.message || '旧密码不正确'))
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.password-form {
  margin-top: 20px;
  max-width: 600px;
}
</style>