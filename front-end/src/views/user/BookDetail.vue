<template>
  <div class="book-detail-page">
    <div class="container">
      <el-card v-if="book" class="detail-card">
        <el-row :gutter="40">
          <el-col :span="8">
            <div class="book-cover">
              <img :src="getImageUrl(book.bookImage)" :alt="book.bookName" />
            </div>
          </el-col>

          <el-col :span="16">
            <div class="book-info">
              <h1 class="book-title">{{ book.bookName }}</h1>
              
              <div class="book-meta">
                <div class="meta-item">
                  <span class="label">作者：</span>
                  <span class="value">{{ book.author }}</span>
                </div>
                <div class="meta-item">
                  <span class="label">出版社：</span>
                  <span class="value">{{ book.publisher }}</span>
                </div>
                <div class="meta-item">
                  <span class="label">分类：</span>
                  <el-tag>{{ getCategoryName(book.category) }}</el-tag>
                </div>
              </div>

              <div class="book-stats">
                <div class="stat-item">
                  <el-icon color="#f59e0b"><StarFilled /></el-icon>
                  <span class="stat-value">{{ book.rating }}</span>
                  <span class="stat-label">评分</span>
                </div>
                <div class="stat-item">
                  <el-icon color="#7c3aed"><ShoppingBag /></el-icon>
                  <span class="stat-value">{{ book.sales }}</span>
                  <span class="stat-label">销量</span>
                </div>
                <div class="stat-item">
                  <el-icon color="#10b981"><Box /></el-icon>
                  <span class="stat-value">{{ book.stock }}</span>
                  <span class="stat-label">库存</span>
                </div>
              </div>

              <div class="book-price">
                <span class="price-label">价格：</span>
                <span class="price-value">¥{{ book.price }}</span>
              </div>

              <div class="book-description">
                <h3>内容简介</h3>
                <p>{{ book.description || '暂无简介' }}</p>
              </div>

              <div class="book-actions">
                <el-input-number v-model="quantity" :min="1" :max="book.stock" />
                <el-button type="primary" size="large" @click="handleAddToCart" :disabled="book.stock === 0">
                  <el-icon><ShoppingCart /></el-icon>
                  加入购物车
                </el-button>
                <el-button type="success" size="large" @click="handleBuyNow" :disabled="book.stock === 0">
                  <el-icon><Sell /></el-icon>
                  立即购买
                </el-button>
              </div>

              <!-- 评分区域 -->
              <div class="rating-section">
                <h3>给这本书评分</h3>
                <el-rate
                  v-model="userRating"
                  :max="10"
                  show-score
                  @change="handleRate"
                />
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-skeleton v-else :rows="10" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bookAPI, cartAPI, orderAPI, scoreAPI, userAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const book = ref(null)
const quantity = ref(1)
const userRating = ref(0)

const categoryMap = {
  'A': '马克思主义',
  'B': '哲学',
  'C': '社会科学',
  'D': '政治、法律',
  'E': '军事',
  'F': '经济',
  'G': '文化、教育',
  'H': '语言、文字',
  'I': '文学',
  'J': '艺术',
  'K': '历史、地理',
  'N': '自然科学',
  'O': '数理化学',
  'P': '天文地球',
  'Q': '生物科学',
  'R': '医药卫生',
  'S': '农业科学',
  'T': '工业技术',
  'U': '交通运输',
  'V': '航空航天',
  'X': '环境科学',
  'Z': '综合图书'
}

const getCategoryName = (code) => {
  return categoryMap[code] || code
}

const getImageUrl = (imagePath) => {
  if (!imagePath) {
    return '/placeholder.jpg'
  }
  // 如果是完整URL，直接返回
  if (imagePath.startsWith('http')) {
    return imagePath
  }
  // 如果是相对路径，添加API前缀
  if (imagePath.startsWith('uploads/')) {
    return `/api/${imagePath}`
  }
  // 其他情况，添加uploads前缀
  return `/api/uploads/${imagePath}`
}

const loadBookDetail = async () => {
  try {
    const bookId = route.params.id
    const res = await bookAPI.getById(bookId)
    book.value = res.data

    // 加载用户评分
    const userId = userStore.userInfo.userId
    try {
      const scoreRes = await scoreAPI.getUserScore(userId, bookId)
      if (scoreRes.data) {
        userRating.value = scoreRes.data.score
      }
    } catch (error) {
      // 用户未评分
    }
  } catch (error) {
    ElMessage.error('加载书籍详情失败')
  }
}

const handleAddToCart = async () => {
  try {
    const userId = userStore.userInfo.userId
    await cartAPI.add({
      userId,
      bookId: book.value.bookId,
      quantity: quantity.value
    })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  }
}

const handleBuyNow = () => {
  try {
    if (book.value.stock === 0) {
      ElMessage.warning('商品库存不足')
      return
    }

    // 构造商品信息
    const items = [{
      bookId: book.value.bookId,
      quantity: quantity.value,
      unitPrice: book.value.price
    }]

    // 跳转到订单确认页面
    router.push({
      name: 'OrderConfirm',
      query: {
        items: JSON.stringify(items),
        cartIds: JSON.stringify([]) // 立即购买没有购物车ID
      }
    })
  } catch (error) {
    ElMessage.error('跳转失败')
  }
}

const handleRate = async (value) => {
  try {
    const userId = userStore.userInfo.userId
    await scoreAPI.rate({
      userId,
      bookId: book.value.bookId,
      score: value
    })
    ElMessage.success('评分成功')
  } catch (error) {
    ElMessage.error('评分失败')
  }
}

onMounted(() => {
  loadBookDetail()
})
</script>

<style scoped>
.book-detail-page {
  padding: 20px 0;
}

.detail-card {
  margin-bottom: 20px;
}

.book-cover {
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.book-cover img {
  width: 100%;
  height: auto;
  display: block;
}

.book-info {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.book-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.book-meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
  font-size: 14px;
  color: #6b7280;
  min-width: 60px;
}

.value {
  font-size: 16px;
  color: #1f2937;
}

.book-stats {
  display: flex;
  gap: 40px;
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.book-price {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: linear-gradient(135deg, #f3e7ff, #e9d5ff);
  border-radius: 12px;
}

.price-label {
  font-size: 18px;
  color: #1f2937;
}

.price-value {
  font-size: 36px;
  font-weight: 700;
  color: #7c3aed;
}

.book-description h3 {
  font-size: 18px;
  color: #1f2937;
  margin: 0 0 15px 0;
}

.book-description p {
  font-size: 15px;
  line-height: 1.8;
  color: #4b5563;
  margin: 0;
}

.book-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.rating-section {
  padding-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.rating-section h3 {
  font-size: 16px;
  color: #1f2937;
  margin: 0 0 15px 0;
}
</style>
