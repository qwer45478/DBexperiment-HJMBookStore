<template>
  <div class="home-page">
    <div class="container">
      <!-- 轮播图 -->
      <div class="carousel-section">
        <el-carousel height="400px" :interval="5000">
          <el-carousel-item v-for="item in carouselItems" :key="item.carouselId">
            <div class="carousel-item" @click="goToBookDetail(item.bookId)">
              <img :src="getImageUrl(item.bookImage)" :alt="item.bookName" />
              <div class="carousel-overlay">
                <h3>{{ item.bookName }}</h3>
                <p>{{ item.author }}</p>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <!-- 个性化推荐 -->
      <div class="section">
        <div class="section-header">
          <h2><el-icon><Star /></el-icon> 为你推荐</h2>
          <span class="subtitle">基于你的阅读偏好</span>
        </div>
        <div class="book-grid">
          <div
            v-for="book in recommendations"
            :key="book.bookId"
            class="book-card"
            @click="goToBookDetail(book.bookId)"
          >
            <div class="book-cover">
              <img :src="getImageUrl(book.bookImage)" :alt="book.bookName" />
              <div class="book-overlay">
                <el-button type="primary" size="small" circle>
                  <el-icon><View /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="book-info">
              <h4 class="book-title">{{ book.bookName }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <div class="book-meta">
                <span class="price">¥{{ book.price }}</span>
                 <span class="rating">
                  <el-icon><StarFilled /></el-icon>
                  {{ book.rating }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 热销榜单 -->
      <div class="section">
        <div class="section-header">
          <h2><el-icon><TrendCharts /></el-icon> 热销榜单</h2>
          <span class="subtitle">大家都在看</span>
        </div>
        <div class="book-grid">
          <div
            v-for="book in topSales"
            :key="book.bookId"
            class="book-card"
            @click="goToBookDetail(book.bookId)"
          >
            <div class="book-cover">
              <img :src="getImageUrl(book.bookImage)" :alt="book.bookName" />
              <div class="book-overlay">
                <el-button type="primary" size="small" circle>
                  <el-icon><View /></el-icon>
                </el-button>
              </div>
            </div>
            <div class="book-info">
              <h4 class="book-title">{{ book.bookName }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <div class="book-meta">
                <span class="price">¥{{ book.price }}</span>
                <span class="sales">销量 {{ book.sales }}</span>
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
import { bookAPI, carouselAPI } from '@/api'
import { getImageUrl } from '@/utils/imageUtils'

const router = useRouter()

const carouselItems = ref([])
const recommendations = ref([])
const topSales = ref([])

const loadCarouselItems = async () => {
  try {
    const res = await carouselAPI.getList()
    carouselItems.value = res.data
  } catch (error) {
    console.error('加载轮播图失败', error)
  }
}

const loadRecommendedBooks = async () => {
  try {
    // 默认用户ID为1，实际应该从用户状态获取
    const res = await bookAPI.getRecommendations(1)
    recommendations.value = res.data
  } catch (error) {
    console.error('加载推荐书籍失败', error)
  }
}

const loadTopSalesBooks = async () => {
  try {
    const res = await bookAPI.getRankings()
    topSales.value = res.data.sales.slice(0, 5)
  } catch (error) {
    console.error('加载销量书籍失败', error)
  }
}

const goToBookDetail = (bookId) => {
  router.push(`/user/book/${bookId}`)
}

onMounted(() => {
  loadCarouselItems()
  loadRecommendedBooks()
  loadTopSalesBooks()
})
</script>

<style scoped>
.home-page {
  padding: 20px 0;
}

.carousel-section {
  margin-bottom: 40px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.carousel-item {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
  overflow: hidden;
}

.carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.carousel-item:hover img {
  transform: scale(1.05);
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
  color: white;
  padding: 30px;
}

.carousel-overlay h3 {
  font-size: 28px;
  margin: 0 0 10px 0;
}

.carousel-overlay p {
  font-size: 16px;
  margin: 0;
  opacity: 0.9;
}

.section {
  margin-bottom: 50px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
}

.section-header h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 28px;
  color: #1f2937;
  margin: 0;
}

.subtitle {
  color: #6b7280;
  font-size: 14px;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 24px;
}

.book-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.book-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 24px rgba(124, 58, 237, 0.2);
}

.book-cover {
  position: relative;
  width: 100%;
  padding-top: 140%;
  overflow: hidden;
  background: #f3f4f6;
}

.book-cover img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.book-card:hover .book-cover img {
  transform: scale(1.1);
}

.book-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.book-card:hover .book-overlay {
  opacity: 1;
}

.book-info {
  padding: 15px;
}

.book-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 18px;
  font-weight: 700;
  color: #7c3aed;
}

.rating,
.sales {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #f59e0b;
}

.sales {
  color: #6b7280;
}
</style>
