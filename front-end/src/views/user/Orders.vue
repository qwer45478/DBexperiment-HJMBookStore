<template>
  <div class="orders-page">
    <div class="container">
      <div class="page-header">
        <h1>我的订单</h1>
      </div>

      <div v-if="orders.length > 0" class="orders-list">
        <el-card v-for="order in orders" :key="order.orderId" class="order-card">
          <div class="order-header">
            <div class="order-info">
              <span class="order-id">订单号：{{ order.orderId }}</span>
              <span class="order-time">{{ formatDate(order.createdAt) }}</span>
            </div>
            <el-tag :type="order.orderStatus === 1 ? 'success' : 'info'">
              {{ order.orderStatus === 1 ? '已完成' : '已取消' }}
            </el-tag>
          </div>

          <div class="order-content">
            <div class="book-info">
              <img :src="order.bookImage || '/placeholder.jpg'" :alt="order.bookName" />
              <div class="book-details">
                <h4>{{ order.bookName }}</h4>
                <p>{{ order.author }}</p>
                <div class="quantity">数量：{{ order.quantity }}</div>
              </div>
            </div>

            <div class="order-price">
              <div class="price-item">
                <span>单价：</span>
                <span>¥{{ order.unitPrice }}</span>
              </div>
              <div class="price-item total">
                <span>总计：</span>
                <span class="total-price">¥{{ order.totalPrice }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="暂无订单记录">
        <el-button type="primary" @click="router.push('/user/home')">去购物</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { orderAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const orders = ref([])

const loadOrders = async () => {
  try {
    const userId = userStore.userInfo.userId
    const res = await orderAPI.getList(userId)
    orders.value = res.data
  } catch (error) {
    console.error('加载订单失败', error)
  }
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.orders-page {
  padding: 20px 0;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  color: #1f2937;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.order-card {
  transition: all 0.3s ease;
}

.order-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 15px;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.order-id {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.order-time {
  font-size: 13px;
  color: #6b7280;
}

.order-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.book-info {
  display: flex;
  gap: 15px;
  flex: 1;
}

.book-info img {
  width: 80px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.book-details h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.book-details p {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.quantity {
  font-size: 13px;
  color: #9ca3af;
}

.order-price {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
}

.price-item {
  display: flex;
  gap: 10px;
  font-size: 14px;
  color: #6b7280;
}

.price-item.total {
  font-size: 16px;
  font-weight: 600;
}

.total-price {
  font-size: 20px;
  font-weight: 700;
  color: #7c3aed;
}
</style>
