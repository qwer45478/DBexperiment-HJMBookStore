<template>
  <div class="user-layout">
    <header class="header">
      <div class="container header-content">
        <div class="logo" @click="router.push('/user/home')">
          <h2>哈籍迷书城</h2>
        </div>
        
        <nav class="nav-menu">
          <router-link to="/user/home" class="nav-item">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link to="/user/rankings" class="nav-item">
            <el-icon><TrendCharts /></el-icon>
            <span>排行榜</span>
          </router-link>
          <router-link to="/user/search" class="nav-item">
            <el-icon><Search /></el-icon>
            <span>搜索</span>
          </router-link>
          <router-link to="/user/cart" class="nav-item">
            <el-icon><ShoppingCart /></el-icon>
            <span>购物车</span>
          </router-link>
          <router-link to="/user/orders" class="nav-item">
            <el-icon><List /></el-icon>
            <span>订单</span>
          </router-link>
        </nav>

        <div class="user-actions">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-icon><User /></el-icon>
              <span>{{ userStore.userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="coupons">
                  <el-icon><Ticket /></el-icon>
                  我的优惠券
                </el-dropdown-item>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>

    <footer class="footer">
      <div class="container">
        <p>&copy; 2024 哈籍迷书城. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { onMounted } from 'vue'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'coupons') {
    router.push('/user/coupons')
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

// Initialize Dify Chatbot
onMounted(() => {
  // Dify Chatbot Configuration
  window.difyChatbotConfig = {
    token: 'r3uNoJ8Ozn3dyzUF',
    isDev: false,
    baseUrl: 'https://udify.app',
    inputs: {
      // You can define the inputs from the Start node here
      // key is the variable name
      // e.g.
      // name: "NAME"
    },
    systemVariables: {
      // user_id: 'YOU CAN DEFINE USER ID HERE',
      // conversation_id: 'YOU CAN DEFINE CONVERSATION ID HERE, IT MUST BE A VALID UUID',
    },
    userVariables: {
      // avatar_url: 'YOU CAN DEFINE USER AVATAR URL HERE',
      // name: 'YOU CAN DEFINE USER NAME HERE',
    },
  }

  // Create and append the script tag for Dify Chatbot
  const script = document.createElement('script')
  script.src = 'https://udify.app/embed.min.js'
  script.id = 'r3uNoJ8Ozn3dyzUF'
  script.defer = true
  
  // Add error handling
  script.onload = () => {
    console.log('Dify chatbot script loaded successfully')
  }
  
  script.onerror = (error) => {
    console.error('Failed to load Dify chatbot script:', error)
  }
  
  document.head.appendChild(script)

  // Add custom styles for the chatbot
  const style = document.createElement('style')
  style.textContent = `
    #dify-chatbot-bubble-button {
      background-color: #1C64F2 !important;
      position: fixed !important;
      right: 20px !important;
      bottom: 20px !important;
      z-index: 9999 !important;
    }
    #dify-chatbot-bubble-window {
      position: fixed !important;
      right: 20px !important;
      bottom: 80px !important;
      width: 24rem !important;
      height: 40rem !important;
      z-index: 9999 !important;
    }
  `
  document.head.appendChild(style)
})
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
  color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
}

.logo {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.logo:hover {
  transform: scale(1.05);
}

.logo h2 {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.nav-menu {
  display: flex;
  gap: 30px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 5px;
  color: white;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.2);
}

.nav-item.router-link-active {
  background: rgba(255, 255, 255, 0.3);
}

.user-actions {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.2);
}

.main-content {
  flex: 1;
  background: #f5f7fa;
  padding: 20px 0;
}

.footer {
  background: #1f2937;
  color: white;
  padding: 20px 0;
  text-align: center;
}

.footer p {
  margin: 0;
  opacity: 0.8;
}
</style>
