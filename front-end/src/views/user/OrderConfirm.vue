<template>
  <div class="order-confirm-page">
    <div class="container">
      <div class="page-header">
        <h1>订单确认</h1>
      </div>

      <el-card class="order-items">
        <template #header>
          <div class="card-header">
            <span>商品清单</span>
          </div>
        </template>
        
        <div v-for="item in orderData.items" :key="item.bookId" class="order-item">
          <img :src="item.bookImage || '/placeholder.jpg'" :alt="item.bookName" />
          <div class="item-info">
            <h4>{{ item.bookName }}</h4>
            <p>{{ item.author }}</p>
            <p class="price">¥{{ item.unitPrice }}</p>
          </div>
          <div class="item-quantity">
            <span>× {{ item.quantity }}</span>
          </div>
          <div class="item-subtotal">
            <span>¥{{ item.totalPrice }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="address-section">
        <template #header>
          <div class="card-header">
            <span>收货地址</span>
            <el-button type="text" @click="showAddressDialog = true">
              选择地址
            </el-button>
          </div>
        </template>
        
        <div v-if="selectedAddress" class="address-content">
          <p>{{ selectedAddress }}</p>
        </div>
        <div v-else class="no-address">
          <p>请选择收货地址</p>
        </div>
      </el-card>

      <el-card class="coupon-section">
        <template #header>
          <div class="card-header">
            <span>优惠券</span>
            <el-button type="text" @click="showCouponDialog = true">
              选择优惠券
            </el-button>
          </div>
        </template>
        
        <div v-if="selectedCoupon" class="coupon-content">
          <div class="coupon-info">
            <h4>{{ selectedCoupon.couponName }}</h4>
            <p>优惠额度：¥{{ selectedCoupon.discountAmount }}</p>
            <p v-if="selectedCoupon.minAmount > 0">满{{ selectedCoupon.minAmount }}元可用</p>
          </div>
          <div class="coupon-discount">
            <span class="discount-amount">-¥{{ orderData.couponDiscount }}</span>
          </div>
        </div>
        <div v-else class="no-coupon">
          <p>未选择优惠券</p>
        </div>
      </el-card>

      <el-card class="price-summary">
        <template #header>
          <div class="card-header">
            <span>费用明细</span>
          </div>
        </template>
        
        <div class="price-item">
          <span>商品原价：</span>
          <span>¥{{ orderData.originalPrice }}</span>
        </div>
        <div class="price-item">
          <span>用户等级({{ orderData.userLevelName }})折扣：</span>
          <span class="discount">-¥{{ orderData.discountAmount }}</span>
        </div>
        <div class="price-item">
          <span>运费：</span>
          <span>¥{{ orderData.shippingFee }}</span>
        </div>
        <div v-if="orderData.couponDiscount > 0" class="price-item">
          <span>优惠券：</span>
          <span class="discount">-¥{{ orderData.couponDiscount }}</span>
        </div>
        <div class="price-total">
          <span>实付金额：</span>
          <span class="final-price">¥{{ orderData.finalPrice }}</span>
        </div>
      </el-card>

      <div class="order-actions">
        <el-button size="large" @click="handleCancel">取消</el-button>
        <el-button 
          type="primary" 
          size="large" 
          @click="handleSubmit"
          :disabled="!selectedAddress || submitting"
          :loading="submitting"
        >
          确认下单
        </el-button>
      </div>
    </div>

    <!-- 地址选择对话框 -->
    <el-dialog v-model="showAddressDialog" title="选择收货地址" width="500px">
      <div class="address-list">
        <div 
          v-for="address in userAddresses" 
          :key="address.addressId"
          class="address-item"
          :class="{ active: selectedAddress === address.address }"
          @click="selectAddress(address.address)"
        >
          <p>{{ address.address }}</p>
          <el-tag v-if="address.isDefault" type="success" size="small">默认</el-tag>
        </div>
      </div>
      <div class="address-actions">
        <el-button @click="showAddressDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAddress">确定</el-button>
      </div>
    </el-dialog>

    <!-- 优惠券选择对话框 -->
    <el-dialog v-model="showCouponDialog" title="选择优惠券" width="600px">
      <div class="coupon-list">
        <div 
          v-for="coupon in availableCoupons" 
          :key="coupon.couponId"
          class="coupon-item"
          :class="{ 
            active: selectedCoupon?.couponId === coupon.couponId,
            disabled: !coupon.isUsable 
          }"
          @click="selectCoupon(coupon)"
        >
          <div class="coupon-main">
            <div class="coupon-value">
              <span class="amount">¥{{ coupon.discountAmount }}</span>
            </div>
            <div class="coupon-info">
              <h4>{{ coupon.couponName }}</h4>
              <p v-if="coupon.minAmount > 0">满{{ coupon.minAmount }}元可用</p>
              <p v-if="coupon.expiryDate">有效期至：{{ formatDate(coupon.expiryDate) }}</p>
            </div>
          </div>
          <div v-if="coupon.isExpired" class="coupon-status expired">已过期</div>
          <div v-else-if="!coupon.isUsable" class="coupon-status unusable">不可用</div>
        </div>
      </div>
      <div class="coupon-actions">
        <el-button @click="showCouponDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCoupon">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderAPI, couponAPI, userAPI, cartAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const orderData = ref({})
const selectedAddress = ref('')
const selectedCoupon = ref(null)
const availableCoupons = ref([])
const userAddresses = ref([])
const submitting = ref(false)
const cartIds = ref([])

const showAddressDialog = ref(false)
const showCouponDialog = ref(false)

const tempAddress = ref('')
const tempCoupon = ref(null)

// 加载订单确认数据
const loadOrderData = async () => {
  try {
    const { items, address, couponId, cartIds: cartIdsParam } = route.query
    
    // 解析商品数据
    const parseJson = (value) => {
      if (!value) return []
      try {
        return JSON.parse(value)
      } catch (error) {
        console.error('解析参数失败', error)
        return []
      }
    }

    const orderItems = parseJson(items)
    
    const request = {
      userId: userStore.userInfo.userId,
      items: orderItems,
      address: address || '',
      couponId: couponId || ''
    }
    
    const res = await orderAPI.prepareConfirm(request)
    orderData.value = res.data
    availableCoupons.value = res.data.availableCoupons || []
    
    // 设置初始地址和优惠券
    selectedAddress.value = address || ''
    if (couponId) {
      selectedCoupon.value = res.data.availableCoupons.find(c => c.couponId === couponId)
    }

    cartIds.value = parseJson(cartIdsParam)
    
    // 加载用户地址
    await loadUserAddresses()
    
  } catch (error) {
    console.error('加载订单数据失败', error)
    ElMessage.error('加载订单数据失败')
    router.back()
  }
}

// 加载用户地址
const loadUserAddresses = async () => {
  try {
    const res = await userAPI.getAddresses(userStore.userInfo.userId)
    userAddresses.value = res.data || []
  } catch (error) {
    console.error('加载地址失败', error)
  }
}

// 选择地址
const selectAddress = (address) => {
  tempAddress.value = address
  // 立即更新selectedAddress以显示选中状态
  selectedAddress.value = address
}

// 确认地址选择
const confirmAddress = () => {
  selectedAddress.value = tempAddress.value
  showAddressDialog.value = false
  // 重新计算价格
  recalculatePrice()
}

// 选择优惠券
const selectCoupon = (coupon) => {
  if (coupon.isUsable) {
    if (tempCoupon.value?.couponId === coupon.couponId) {
      tempCoupon.value = null
      selectedCoupon.value = null
    } else {
      tempCoupon.value = coupon
      // 立即更新selectedCoupon以显示选中状态
      selectedCoupon.value = coupon
    }
  }
}

// 确认优惠券选择
const confirmCoupon = () => {
  selectedCoupon.value = tempCoupon.value
  showCouponDialog.value = false
  // 重新计算价格
  recalculatePrice()
}

// 重新计算价格
const recalculatePrice = async () => {
  try {
    const orderItems = route.query.items ? JSON.parse(route.query.items) : []
    
    const request = {
      userId: userStore.userInfo.userId,
      items: orderItems,
      address: selectedAddress.value,
      couponId: selectedCoupon.value?.couponId || ''
    }
    
    const res = await orderAPI.prepareConfirm(request)
    orderData.value = res.data
  } catch (error) {
    console.error('重新计算价格失败', error)
  }
}

// 提交订单
const handleSubmit = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  
  try {
    await ElMessageBox.confirm('确认提交订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    submitting.value = true
    
    const orderItems = route.query.items ? JSON.parse(route.query.items) : []
    
    const request = {
      userId: userStore.userInfo.userId,
      items: orderItems,
      address: selectedAddress.value,
      couponId: selectedCoupon.value?.couponId || ''
    }
    
    await orderAPI.createConfirmed(request)
    
    // 清理已结算的购物车项
    if (cartIds.value.length > 0) {
      await Promise.all(
        cartIds.value.map(async (cartId) => {
          try {
            await cartAPI.remove(cartId)
          } catch (err) {
            console.warn(`删除购物车项${cartId}失败`, err)
          }
        })
      )
    }
    
    ElMessage.success('下单成功')
    router.push('/user/orders')
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交订单失败', error)
      ElMessage.error(error.message || '提交订单失败')
    }
  } finally {
    submitting.value = false
  }
}

// 取消订单
const handleCancel = () => {
  router.back()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString()
}

onMounted(() => {
  loadOrderData()
})
</script>

<style scoped>
.order-confirm-page {
  padding: 20px 0;
  background: #f5f5f5;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  color: #1f2937;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.order-items {
  margin-bottom: 20px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 0;
  border-bottom: 1px solid #e5e7eb;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item img {
  width: 80px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.item-info p {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 4px 0;
}

.item-info .price {
  color: #7c3aed;
  font-weight: 600;
}

.item-quantity {
  font-size: 16px;
  color: #6b7280;
}

.item-subtotal {
  font-size: 18px;
  font-weight: 600;
  color: #dc2626;
}

.address-section,
.coupon-section {
  margin-bottom: 20px;
}

.address-content,
.coupon-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no-address,
.no-coupon {
  color: #9ca3af;
  font-style: italic;
}

.coupon-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
}

.coupon-info p {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.discount-amount {
  color: #dc2626;
  font-weight: 600;
  font-size: 18px;
}

.price-summary {
  margin-bottom: 20px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 16px;
}

.discount {
  color: #dc2626;
}

.price-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-top: 2px solid #e5e7eb;
  font-size: 18px;
  font-weight: 600;
}

.final-price {
  color: #dc2626;
  font-size: 24px;
}

.order-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 20px 0;
}

.address-list,
.coupon-list {
  max-height: 400px;
  overflow-y: auto;
}

.address-item {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.address-item:hover,
.address-item.active {
  border-color: #7c3aed;
  background: #f3f4f6;
}

.coupon-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.coupon-item:hover:not(.disabled),
.coupon-item.active:not(.disabled) {
  border-color: #7c3aed;
  background: #f3f4f6;
}

.coupon-item.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.coupon-main {
  display: flex;
  align-items: center;
  flex: 1;
  gap: 16px;
}

.coupon-value {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  text-align: center;
  min-width: 80px;
}

.amount {
  font-size: 20px;
  font-weight: 700;
}

.coupon-info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
}

.coupon-info p {
  margin: 0;
  font-size: 14px;
  color: #6b7280;
}

.coupon-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.coupon-status.expired {
  background: #fecaca;
  color: #dc2626;
}

.coupon-status.unusable {
  background: #e5e7eb;
  color: #6b7280;
}

.address-actions,
.coupon-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}
</style>
