<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>哈籍迷书城</h1>
        <p>发现好书，享受阅读</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="用户登录" name="userLogin">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="login-form">
            <el-form-item prop="account">
              <el-input
                v-model="loginForm.account"
                placeholder="请输入手机号"
                prefix-icon="Phone"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-button type="primary" size="large" class="login-btn" @click="handleLogin('user')" :loading="loading">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="用户注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input
                v-model="registerForm.phone"
                placeholder="请输入手机号"
                prefix-icon="Phone"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（6-24位，包含字母和数字）"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-button type="primary" size="large" class="login-btn" @click="handleRegister" :loading="loading">
              注册
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="管理员登录" name="adminLogin">
          <el-form :model="adminLoginForm" :rules="loginRules" ref="adminLoginFormRef" class="login-form">
            <el-form-item prop="account">
              <el-input
                v-model="adminLoginForm.account"
                placeholder="请输入管理员ID"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="adminLoginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-button type="primary" size="large" class="login-btn" @click="handleLogin('admin')" :loading="loading">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('userLogin')
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: ''
})

const adminLoginForm = reactive({
  account: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  phone: '',
  password: ''
})

const loginRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { max: 16, message: '用户名长度不能超过16个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,24}$/, message: '密码必须包含字母和数字，长度6-24位', trigger: 'blur' }
  ]
}

const loginFormRef = ref(null)
const adminLoginFormRef = ref(null)
const registerFormRef = ref(null)

const handleLogin = async (type) => {
  const formRef = type === 'admin' ? adminLoginFormRef.value : loginFormRef.value
  const form = type === 'admin' ? adminLoginForm : loginForm

  await formRef.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await authAPI.login({
          ...form,
          loginType: type
        })
        
        userStore.setToken(res.data.token)
        userStore.setUserInfo(res.data)
        userStore.setUserType(type)
        
        ElMessage.success('登录成功')
        
        if (type === 'admin') {
          router.push('/admin/dashboard')
        } else {
          router.push('/user/home')
        }
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = async () => {
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await authAPI.register(registerForm)
        
        userStore.setToken(res.data.token)
        userStore.setUserInfo(res.data)
        userStore.setUserType('user')
        
        ElMessage.success('注册成功')
        router.push('/user/home')
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 20px;
  padding: 40px;
  width: 100%;
  max-width: 450px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 32px;
  color: #7c3aed;
  margin-bottom: 10px;
  font-weight: 700;
}

.login-header p {
  color: #6b7280;
  font-size: 14px;
}

.login-tabs {
  margin-bottom: 20px;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
  border: none;
  font-size: 16px;
  font-weight: 600;
}

.login-btn:hover {
  background: linear-gradient(135deg, #6d28d9, #8b5cf6);
}
</style>
