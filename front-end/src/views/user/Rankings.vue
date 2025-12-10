<template>
  <div class="rankings-page">
    <div class="container">
      <div class="page-header">
        <h1>排行榜</h1>
        <p>发现最受欢迎的书籍</p>
      </div>

      <div class="rankings-grid">
        <!-- 评分榜 -->
        <div class="ranking-section">
          <div class="ranking-header">
            <el-icon class="header-icon" color="#f59e0b"><StarFilled /></el-icon>
            <h2>评分榜</h2>
          </div>
          <div class="ranking-list">
            <div
              v-for="(book, index) in ratingRankings"
              :key="book.bookId"
              class="ranking-item"
              @click="goToBookDetail(book.bookId)"
            >
              <div class="rank-number" :class="getRankClass(index)">{{ index + 1 }}</div>
              <img :src="getImageUrl(book.bookImage)" :alt="book.bookName" class="book-thumbnail" />
              <div class="book-details">
                <h4>{{ book.bookName }}</h4>
                <p>{{ book.author }}</p>
                <div class="rating-score">
                  <el-icon color="#f59e0b"><StarFilled /></el-icon>
                  <span>{{ book.rating }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 历史销量榜 -->
        <div class="ranking-section">
          <div class="ranking-header">
            <el-icon class="header-icon" color="#7c3aed"><TrendCharts /></el-icon>
            <h2>历史销量榜</h2>
          </div>
          <div class="ranking-list">
            <div
              v-for="(book, index) in salesRankings"
              :key="book.bookId"
              class="ranking-item"
              @click="goToBookDetail(book.bookId)"
            >
              <div class="rank-number" :class="getRankClass(index)">{{ index + 1 }}</div>
              <img :src="getImageUrl(book.bookImage)" :alt="book.bookName" class="book-thumbnail" />
              <div class="book-details">
                <h4>{{ book.bookName }}</h4>
                <p>{{ book.author }}</p>
                <div class="sales-count">
                  <el-icon><ShoppingBag /></el-icon>
                  <span>{{ book.sales }} 销量</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 月销量榜 -->
        <div class="ranking-section">
          <div class="ranking-header">
            <el-icon class="header-icon" color="#ec4899"><Promotion /></el-icon>
            <h2>月销量榜</h2>
          </div>
          <div class="ranking-list">
            <div
              v-for="(book, index) in monthlySalesRankings"
              :key="book.bookId"
              class="ranking-item"
              @click="goToBookDetail(book.bookId)"
            >
              <div class="rank-number" :class="getRankClass(index)">{{ index + 1 }}</div>
              <img :src="getImageUrl(book.bookImage)" :alt="book.bookName" class="book-thumbnail" />
              <div class="book-details">
                <h4>{{ book.bookName }}</h4>
                <p>{{ book.author }}</p>
                <div class="sales-count">
                  <el-icon><Calendar /></el-icon>
                  <span>{{ book.monthlySales }} 月销</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bookAPI } from '@/api'
import { getImageUrl } from '@/utils/imageUtils'

const router = useRouter()

const ratingRankings = ref([])
const salesRankings = ref([])
const monthlySalesRankings = ref([])
const loading = ref(false)

const loadRankings = async () => {
  loading.value = true
  try {
    const res = await bookAPI.getRankings()
    ratingRankings.value = res.data.rating || []
    salesRankings.value = res.data.sales || []
    monthlySalesRankings.value = res.data.monthlySales || []
  } catch (error) {
    ElMessage.error('加载排行榜失败')
  } finally {
    loading.value = false
  }
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return ''
}

const goToBookDetail = (bookId) => {
  router.push(`/user/book/${bookId}`)
}

onMounted(() => {
  loadRankings()
})
</script>

<style scoped>
.rankings-page {
  padding: 20px 0;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 36px;
  color: #1f2937;
  margin: 0 0 10px 0;
}

.page-header p {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.rankings-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

@media (max-width: 1200px) {
  .rankings-grid {
    grid-template-columns: 1fr;
  }
}

.ranking-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.ranking-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f3f4f6;
}

.header-icon {
  font-size: 24px;
}

.ranking-header h2 {
  font-size: 22px;
  color: #1f2937;
  margin: 0;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f9fafb;
}

.ranking-item:hover {
  background: #f3f4f6;
  transform: translateX(5px);
}

.rank-number {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  border-radius: 8px;
  background: #e5e7eb;
  color: #6b7280;
  flex-shrink: 0;
}

.rank-gold {
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  color: white;
}

.rank-silver {
  background: linear-gradient(135deg, #d1d5db, #9ca3af);
  color: white;
}

.rank-bronze {
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: white;
}

.book-thumbnail {
  width: 60px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.book-details {
  flex: 1;
  min-width: 0;
}

.book-details h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 5px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-details p {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rating-score,
.sales-count {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  font-weight: 600;
  color: #7c3aed;
}

.rating-score {
  color: #f59e0b;
}
</style>
