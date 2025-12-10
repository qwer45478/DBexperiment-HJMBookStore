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
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
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

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}
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
