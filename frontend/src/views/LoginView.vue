<template>
  <div class="login-container">
    <!-- 动态方块背景 -->
    <div class="dynamic-squares">
      <div
        v-for="(square, index) in squares"
        :key="index"
        class="square"
        :style="getSquareStyle(square)"
      ></div>
    </div>

    <!-- 登录卡片 -->
    <div class="login-card">
      <div class="login-title">
        <h1>企业管理系统</h1>
        <p>Welcome back! Please login to your account</p>
      </div>

      <el-form :model="form" class="login-form">
        <div class="input-group">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            clearable
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="input-group">
          <el-input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
            <template #suffix>
              <el-icon @click="showPassword = !showPassword">
                <View v-if="showPassword" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
        </div>

        <div class="remember">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        </div>

        <el-button
          type="success"
          @click="handleLogin"
          class="login-btn"
          :disabled="!isFormValid"
        >
          登录
        </el-button>

        <el-button
          type="primary"
          @click="showRegister = true"
          class="register-btn"
        >
          企业注册
        </el-button>
      </el-form>
    </div>

    <!-- 企业注册对话框 -->
    <el-dialog v-model="showRegister" title="企业注册" width="500px">
      <el-form :model="regForm" label-width="120px" :rules="regRules" ref="regFormRef">
        <el-form-item label="企业名称" prop="companyName">
          <input v-model="regForm.companyName"/>
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <input v-model="regForm.contact" />
        </el-form-item>
        <el-form-item label="营业执照" prop="license">
          <input v-model="regForm.license" />
        </el-form-item>
        <el-form-item label="管理员账号" prop="username">
          <input v-model="regForm.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <input v-model="regForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <input v-model="regForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/services/api'
import { ElMessage } from 'element-plus'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'

const router = useRouter()
const regFormRef = ref(null)

// 表单数据
const form = ref({ username: 'admin', password: '' })
const regForm = ref({
  companyName: '',
  contact: '',
  license: '',
  username: '',
  password: '',
  confirmPassword: '',
  captcha: ''
})
const showRegister = ref(false)
const rememberMe = ref(false)
const showPassword = ref(false)
const captchaCooldown = ref(0)

// 验证规则
const validatePassword = (rule, value, callback) => {
  if (value !== regForm.value.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const regRules = {
  companyName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' }
  ],
  contact: [
    { required: true, message: '请输入联系人', trigger: 'blur' }
  ],
  license: [
    { required: true, message: '请输入营业执照', trigger: 'blur' }
  ],
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 4, message: '用户名长度至少4位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 4, message: '验证码长度为4位', trigger: 'blur' }
  ]
}

// 表单验证
const isFormValid = computed(() => {
  return form.value.username.trim() !== '' && form.value.password.trim() !== ''
})

// 方块位置和大小数据
const squares = ref([
  { top: '15%', left: '75%', size: '100px', delay: '0s' },
  { top: '40%', left: '10%', size: '150px', delay: '1s' },
  { top: '70%', left: '80%', size: '60px', delay: '2s' },
  { top: '80%', left: '15%', size: '50px', delay: '3s' },
  { top: '10%', left: '15%', size: '50px', delay: '4s' },
  { top: '30%', left: '75%', size: '85px', delay: '5s' }
])

// 获取方块样式
const getSquareStyle = (square) => {
  return {
    top: square.top,
    left: square.left,
    width: square.size,
    height: square.size,
    animationDelay: square.delay
  }
}

// 获取验证码
const getCaptcha = async () => {
  try {
    // 调用真实验证码接口
    await api.getCaptcha()
    
    // 启动倒计时
    captchaCooldown.value = 60
    const timer = setInterval(() => {
      captchaCooldown.value--
      if (captchaCooldown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
    
    ElMessage.success('验证码已发送')
  } catch (error) {
    console.error('获取验证码失败:', error)
    ElMessage.error('获取验证码失败')
  }
}

// 登录方法
const handleLogin = async () => {
  try {
    const response = await api.login({
      username: form.value.username,
      password: form.value.password
    })
    
    // 确保响应数据存在
    if (!response) {
      ElMessage.error('登录失败，请检查用户名和密码')
      return
    }
    
    // 存储用户数据和会话ID
    localStorage.setItem('user', JSON.stringify({
      ...response.user,
      sessionId: response.sessionId
    }));
    
    ElMessage.success('登录成功')
    
    // 根据角色跳转
    if (response.user.role === 0) {
      router.push('/main/users')
    } else {
      router.push('/main/profile')
    }
  } catch (error) {
    console.error('登录失败:', error)
    
    // 更详细的错误信息
    let errorMsg = '登录请求失败: '
    if (error.response) {
      // 请求已发出，服务器响应状态码非2xx
      errorMsg += `[${error.response.status}] ${error.response.data?.message || '未知错误'}`
    } else if (error.request) {
      // 请求已发出但无响应
      errorMsg += '服务器无响应'
    } else {
      // 请求未发出
      errorMsg += error.message || '未知错误'
    }
    
    ElMessage.error(errorMsg)
  }
}

// 注册方法
const handleRegister = async () => {
  if (!regFormRef.value) return
  
  try {
    await regFormRef.value.validate()
    
    await api.register(regForm.value)
    showRegister.value = false
    ElMessage.success('注册成功，等待审核')
    
    // 清空注册表单
    regForm.value = {
      companyName: '',
      contact: '',
      license: '',
      username: '',
      password: '',
      confirmPassword: '',
      captcha: ''
    }
  } catch (error) {
    console.error('注册失败:', error)
    if (error.response?.data?.message) {
      ElMessage.error('注册失败: ' + error.response.data.message)
    } else {
      ElMessage.error('请填写完整的注册信息')
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
}

/* 动态方块背景 */
.dynamic-squares {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 1;
}

.square {
  position: absolute;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  box-shadow: 0 25px 45px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 15px;
  animation: square 10s linear infinite;
}

@keyframes square {
  0%,100% {
    transform: translateY(-20px);
  }
  50% {
    transform: translateY(20px);
  }
}

/* 登录卡片样式 */
.login-card {
  position: relative;
  z-index: 10;
  padding: 40px;
  width: 380px;
  min-height: 450px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  box-shadow: 0 25px 45px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.login-card::after {
  content: '';
  position: absolute;
  top: 5px;
  right: 5px;
  bottom: 5px;
  left: 5px;
  border-radius: 10px;
  pointer-events: none;
  background: linear-gradient(to bottom, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.1) 2%);
}

.login-title {
  color: white;
  text-align: center;
  margin-bottom: 35px;
}

.login-title h1 {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.login-title p {
  font-size: 14px;
  opacity: 0.9;
}

/* 登录表单 */
.login-form {
  width: 100%;
}

/* 输入框样式 */
.input-group {
  position: relative;
  width: 100%;
  margin-bottom: 25px;
}

/* 修复输入框边框问题 */
:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.15) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  border-radius: 30px !important;
  box-shadow: none !important;
}

:deep(.el-input__inner) {
  color: white !important;
  padding-left: 40px !important;
  height: 46px;
}

:deep(.el-input__inner)::placeholder {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-input__inner):focus {
  background: rgba(255, 255, 255, 0.25) !important;
  border-color: rgba(255, 255, 255, 0.4) !important;
}

:deep(.el-input__prefix) {
  color: rgba(255, 255, 255, 0.7) !important;
  left: 12px !important;
}

:deep(.el-input__suffix) {
  color: rgba(255, 255, 255, 0.7) !important;
  cursor: pointer;
  right: 15px !important;
}

/* 记住我 */
.remember {
  display: flex;
  align-items: center;
  width: 100%;
  margin-bottom: 20px;
  color: white;
}

:deep(.el-checkbox__label) {
  color: white !important;
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 10px;
  transition: all 0.3s ease;
  border-radius: 30px !important;
  background: rgba(255, 255, 255, 0.9) !important;
  color: #2c3e50 !important;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1) !important;
}

.login-btn:hover {
  background: white !important;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2) !important;
}

.login-btn:disabled {
  background: rgba(255, 255, 255, 0.5) !important;
  cursor: not-allowed;
  transform: none;
  box-shadow: none !important;
}

/* 注册按钮 */
.register-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 15px;
  transition: all 0.3s ease;
  border-radius: 30px !important;
  background: rgba(56, 158, 255, 0.9) !important;
  color: white !important;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1) !important;
}

.register-btn:hover {
  background: rgba(64, 158, 255, 1) !important;
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2) !important;
}

/* 验证码容器 */
.captcha-container {
  display: flex;
  gap: 10px;
}

.captcha-btn {
  white-space: nowrap;
}

/* 背景动画 */
@keyframes gradientBG {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 30px;
  }
  
  .login-title h1 {
    font-size: 24px;
  }
  
  .login-title p {
    font-size: 13px;
  }
}
</style>