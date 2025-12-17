<template>
  <div class="cart-page">
    <div class="container">
      <div class="page-header">
        <h1>购物车</h1>
      </div>

      <div v-if="cartItems.length > 0" class="cart-content">
        <el-card class="cart-list">
          <div class="cart-header">
            <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
            <span>商品信息</span>
            <span>单价</span>
            <span>数量</span>
            <span>小计</span>
            <span>操作</span>
          </div>

          <div v-for="item in cartItems" :key="item.cartId" class="cart-item">
            <el-checkbox v-model="item.selected" @change="updateTotal" />
            <div class="item-info">
              <img :src="item.bookImage || '/placeholder.jpg'" :alt="item.bookName" />
              <div class="item-details">
                <h4>{{ item.bookName }}</h4>
                <p>{{ item.author }}</p>
              </div>
            </div>
            <div class="item-price">¥{{ item.price }}</div>
            <div class="item-quantity">
              <el-input-number
                v-model="item.quantity"
                :min="1"
                :max="item.stock"
                @change="handleQuantityChange(item)"
              />
            </div>
            <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            <div class="item-actions">
              <el-button type="danger" text @click="handleRemove(item.cartId)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </el-card>

        <el-card class="cart-summary">
          <div class="summary-content">
            <div class="summary-info">
              <span>已选择 {{ selectedCount }} 件商品</span>
            </div>
            <div class="summary-total">
              <span>总计：</span>
              <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            </div>
            <el-button type="primary" size="large" @click="handleCheckout" :disabled="selectedCount === 0">
              结算
            </el-button>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="购物车是空的">
        <el-button type="primary" @click="router.push('/user/home')">去逛逛</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cartAPI, orderAPI, userAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const cartItems = ref([])
const selectAll = ref(false)

const selectedCount = computed(() => {
  return cartItems.value.filter(item => item.selected).length
})

const totalPrice = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const loadCartItems = async () => {
  try {
    const userId = userStore.userInfo.userId
    const res = await cartAPI.getList(userId)
    cartItems.value = res.data.map(item => ({
      ...item,
      selected: false
    }))
  } catch (error) {
    console.error('加载购物车失败', error)
  }
}

const handleSelectAll = (val) => {
  cartItems.value.forEach(item => {
    item.selected = val
  })
}

const updateTotal = () => {
  selectAll.value = cartItems.value.every(item => item.selected)
}

const handleQuantityChange = async (item) => {
  try {
    await cartAPI.update({
      cartId: item.cartId,
      quantity: item.quantity
    })
  } catch (error) {
    ElMessage.error('更新数量失败')
  }
}

const handleRemove = async (cartId) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await cartAPI.remove(cartId)
    ElMessage.success('删除成功')
    loadCartItems()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleCheckout = () => {
  try {
    const selectedItems = cartItems.value.filter(item => item.selected)
    
    if (selectedItems.length === 0) {
      ElMessage.warning('请选择要结算的商品')
      return
    }

    const items = selectedItems.map(item => ({
      bookId: item.bookId,
      quantity: item.quantity,
      unitPrice: item.price
    }))
    const cartIds = selectedItems.map(item => item.cartId)

    // 跳转到订单确认页面
    router.push({
      name: 'OrderConfirm',
      query: {
        items: JSON.stringify(items),
        cartIds: JSON.stringify(cartIds)
      }
    })
  } catch (error) {
    ElMessage.error('结算失败')
  }
}

onMounted(() => {
  loadCartItems()
})
</script>

<style scoped>
.cart-page {
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

.cart-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.cart-list {
  flex: 1;
}

.cart-header {
  display: grid;
  grid-template-columns: 50px 2fr 1fr 1fr 1fr 1fr;
  gap: 20px;
  align-items: center;
  padding: 15px 20px;
  background: #f9fafb;
  border-radius: 8px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 15px;
}

.cart-item {
  display: grid;
  grid-template-columns: 50px 2fr 1fr 1fr 1fr 1fr;
  gap: 20px;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-info {
  display: flex;
  gap: 15px;
  align-items: center;
}

.item-info img {
  width: 80px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.item-details h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.item-details p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.item-price,
.item-subtotal {
  font-size: 18px;
  font-weight: 600;
  color: #7c3aed;
}

.cart-summary {
  position: sticky;
  bottom: 20px;
}

.summary-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 30px;
}

.summary-info {
  font-size: 14px;
  color: #6b7280;
}

.summary-total {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
}

.total-price {
  font-size: 28px;
  font-weight: 700;
  color: #dc2626;
}
</style>
