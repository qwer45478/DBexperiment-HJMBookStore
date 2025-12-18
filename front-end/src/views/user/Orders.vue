<template>
  <div class="orders-page">
    <div class="container">
      <div class="page-header">
        <h1>我的订单</h1>
      </div>

      <!-- 订单状态筛选 -->
      <div class="filter-section">
        <el-card>
          <div class="filter-tabs">
            <el-button 
              :type="searchForm.orderStatus === null ? 'primary' : 'default'"
              @click="handleStatusFilter('all')"
            >
              全部订单 ({{ statusCounts.all }})
            </el-button>
            <el-button 
              :type="searchForm.orderStatus === 1 ? 'primary' : 'default'"
              @click="handleStatusFilter(1)"
            >
              运送中 ({{ statusCounts.shipping }})
            </el-button>
            <el-button 
              :type="searchForm.orderStatus === 2 ? 'primary' : 'default'"
              @click="handleStatusFilter(2)"
            >
              已完成 ({{ statusCounts.completed }})
            </el-button>
            <el-button 
              :type="searchForm.orderStatus === 0 ? 'primary' : 'default'"
              @click="handleStatusFilter(0)"
            >
              已取消 ({{ statusCounts.cancelled }})
            </el-button>
          </div>
        </el-card>
      </div>

      <div v-if="orders.length > 0" class="orders-list" v-loading="loading">
        <el-card v-for="order in orders" :key="order.orderId" class="order-card">
          <div class="order-header">
            <div class="order-info">
              <span class="order-id">订单号：{{ order.orderId }}</span>
              <span class="order-time">{{ formatDate(order.createdAt) }}</span>
            </div>
            <el-tag :type="getStatusType(order.orderStatus)">
              {{ getStatusText(order.orderStatus) }}
            </el-tag>
          </div>

          <div class="order-content">
            <div class="book-info">
              <img :src="order.bookImage || '/placeholder.jpg'" :alt="order.bookName" />
              <div class="book-details">
                <h4>{{ order.bookName }}</h4>
                <p>{{ order.author }}</p>
                <div class="quantity">数量：{{ order.quantity }}</div>
                <div class="address">收货地址：{{ order.address || '无' }}</div>
              </div>
            </div>

            <div class="order-price">
              <div class="price-item">
                <span>单价：</span>
                <span>¥{{ order.unitPrice }}</span>
              </div>
              <div class="price-item">
                <span>总计：</span>
                <span>¥{{ order.totalPrice }}</span>
              </div>
              <div class="price-item actual-pay">
                <span>实付：</span>
                <span class="actual-pay-price">¥{{ order.actualPay || order.totalPrice }}</span>
              </div>
            </div>

            <div class="order-actions">
              <el-button 
                v-if="order.orderStatus === 1" 
                type="danger" 
                size="small" 
                @click="handleCancelOrder(order)"
              >
                取消订单
              </el-button>
              <el-button 
                v-if="order.orderStatus === 1" 
                type="success" 
                size="small" 
                @click="handleReceiveOrder(order)"
              >
                确认签收
              </el-button>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 分页组件 -->
      <div v-if="orders.length > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          :prev-text="'上一页'"
          :next-text="'下一页'"
          :pager-count="7"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const orders = ref([])
const loading = ref(false)
const pagination = ref({
  page: 1,
  size: 10,
  total: 0,
  totalPages: 0
})
const statusCounts = ref({
  all: 0,
  shipping: 0,  // 运送中
  completed: 0, // 已完成
  cancelled: 0  // 已取消
})
const searchForm = ref({
  userId: null,
  orderStatus: null,
  sortBy: 'createdAt',
  sortOrder: 'desc',
  page: 1,
  size: 10
})

const loadOrders = async () => {
  loading.value = true
  try {
    searchForm.value.userId = userStore.userInfo.userId
    const res = await orderAPI.search(searchForm.value)
    orders.value = res.data.content
    pagination.value = {
      page: res.data.page,
      size: res.data.size,
      total: res.data.total,
      totalPages: res.data.totalPages
    }
  } catch (error) {
    console.error('加载订单失败', error)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
  }
}

const loadStatusCounts = async () => {
  try {
    const userId = userStore.userInfo.userId
    
    // 获取各状态的订单数量
    const allRequest = { userId, page: 1, size: 1 }
    const shippingRequest = { userId, orderStatus: 1, page: 1, size: 1 }
    const completedRequest = { userId, orderStatus: 2, page: 1, size: 1 }
    const cancelledRequest = { userId, orderStatus: 0, page: 1, size: 1 }
    
    const [allRes, shippingRes, completedRes, cancelledRes] = await Promise.all([
      orderAPI.search(allRequest),
      orderAPI.search(shippingRequest),
      orderAPI.search(completedRequest),
      orderAPI.search(cancelledRequest)
    ])
    
    statusCounts.value = {
      all: allRes.data.total,
      shipping: shippingRes.data.total,
      completed: completedRes.data.total,
      cancelled: cancelledRes.data.total
    }
  } catch (error) {
    console.error('加载订单状态统计失败', error)
  }
}

const handlePageChange = (page) => {
  searchForm.value.page = page
  loadOrders()
}

const handleSizeChange = (size) => {
  searchForm.value.size = size
  searchForm.value.page = 1
  loadOrders()
}

const handleStatusFilter = (status) => {
  searchForm.value.orderStatus = status === 'all' ? null : status
  searchForm.value.page = 1
  loadOrders()
}

const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'danger'  // 已取消
    case 1: return 'warning' // 运送中
    case 2: return 'success' // 已完成
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '已取消'
    case 1: return '运送中'
    case 2: return '已完成'
    default: return '未知状态'
  }
}

const handleCancelOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消订单 ${order.orderId} 吗？取消后将恢复库存。`,
      '取消订单',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '再想想',
        type: 'warning'
      }
    )

    await orderAPI.cancel(order.orderId, {
      userId: userStore.userInfo.userId
    })
    
    ElMessage.success('订单取消成功')
    loadOrders() // 重新加载订单列表
    loadStatusCounts() // 重新加载状态统计
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败', error)
      ElMessage.error(error.message || '取消订单失败')
    }
  }
}

const handleReceiveOrder = async (order) => {
  try {
    await ElMessageBox.confirm(
      `确定已收到订单 ${order.orderId} 的商品吗？`,
      '确认签收',
      {
        confirmButtonText: '确认签收',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    await orderAPI.receive(order.orderId, {
      userId: userStore.userInfo.userId
    })
    
    ElMessage.success('签收成功')
    loadOrders() // 重新加载订单列表
    loadStatusCounts() // 重新加载状态统计
  } catch (error) {
    if (error !== 'cancel') {
      console.error('签收失败', error)
      ElMessage.error(error.message || '签收失败')
    }
  }
}

onMounted(() => {
  loadOrders()
  loadStatusCounts()
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

.filter-section {
  margin-bottom: 20px;
}

.filter-tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
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
  align-items: flex-start;
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

.address {
  font-size: 13px;
  color: #6b7280;
  margin-top: 8px;
  padding: 8px 12px;
  background: #f9fafb;
  border-radius: 4px;
  border-left: 3px solid #7c3aed;
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

.price-item.actual-pay {
  font-size: 16px;
  font-weight: 600;
  color: #059669;
}

.total-price {
  font-size: 20px;
  font-weight: 700;
  color: #7c3aed;
}

.actual-pay-price {
  font-size: 20px;
  font-weight: 700;
  color: #059669;
}

.order-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
  min-width: 100px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 20px 0;
}
</style>
