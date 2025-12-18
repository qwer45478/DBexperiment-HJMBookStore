<template>
  <div class="search-page">
    <div class="container">
      <div class="search-header">
        <h1>搜索书籍</h1>
      </div>

      <!-- 搜索表单 -->
      <div class="search-form">
        <el-card>
          <el-form :model="searchForm" label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="书籍名称">
                  <el-input v-model="searchForm.bookName" placeholder="输入书籍名称" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="作者">
                  <el-input v-model="searchForm.author" placeholder="输入作者名" clearable />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="价格范围">
                  <el-input-number v-model="searchForm.minPrice" :min="0" placeholder="最低价" style="width: 45%" />
                  <span style="margin: 0 10px">-</span>
                  <el-input-number v-model="searchForm.maxPrice" :min="0" placeholder="最高价" style="width: 45%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="评分范围">
                  <el-input-number v-model="searchForm.minRating" :min="0" :max="10" placeholder="最低分" style="width: 45%" />
                  <span style="margin: 0 10px">-</span>
                  <el-input-number v-model="searchForm.maxRating" :min="0" :max="10" placeholder="最高分" style="width: 45%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="书籍分类">
                  <el-select v-model="searchForm.categories" multiple placeholder="选择分类" style="width: 100%">
                    <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="排序方式">
                  <el-select v-model="searchForm.sortBy" placeholder="选择排序" style="width: 100%">
                    <el-option label="销量排序" value="sales" />
                    <el-option label="评分排序" value="rating" />
                    <el-option label="价格排序" value="price" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item>
              <el-button type="primary" @click="handleSearch" :loading="loading">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">
                <el-icon><RefreshLeft /></el-icon>
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 搜索结果 -->
      <div class="search-results" v-if="searchResults.length > 0">
        <div class="results-header">
          <h3>找到 {{ pagination.total }} 本书籍</h3>
        </div>
        <div class="book-grid">
          <div
            v-for="book in searchResults"
            :key="book.bookId"
            class="book-card"
            @click="goToBookDetail(book.bookId)"
          >
            <div class="book-cover">
              <img :src="getImageUrl(book.bookImage)" :alt="book.bookName" />
            </div>
            <div class="book-info">
              <h4 class="book-title">{{ book.bookName }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-publisher">{{ book.publisher }}</p>
              <div class="book-meta">
                <span class="price">¥{{ book.price }}</span>
                <span class="rating">
                  <el-icon><StarFilled /></el-icon>
                  {{ book.rating }}
                </span>
              </div>
              <div class="book-stats">
                <span>销量: {{ book.sales }}</span>
                <span>库存: {{ book.stock }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页组件 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[12, 24, 48, 96]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            :prev-text="'上一页'"
            :next-text="'下一页'"
            :pager-count="7"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <!-- 无结果提示 -->
      <el-empty v-else description="未找到相关书籍" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bookAPI } from '@/api'
import { getImageUrl } from '@/utils/imageUtils'

const router = useRouter()

const searchForm = ref({
  bookName: '',
  author: '',
  categories: [],
  minSales: null,
  maxSales: null,
  minPrice: null,
  maxPrice: null,
  minRating: null,
  maxRating: null,
  sortBy: 'sales',
  sortOrder: 'desc',
  page: 1,
  size: 12
})

const books = ref([])
const loading = ref(false)
const pagination = ref({
  page: 1,
  size: 12,
  total: 0,
  totalPages: 0
})

const searchBooks = async () => {
  loading.value = true
  try {
    const res = await bookAPI.search(searchForm.value)
    books.value = res.data.content
    pagination.value = {
      page: res.data.page,
      size: res.data.size,
      total: res.data.total,
      totalPages: res.data.totalPages
    }
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const searchResults = computed(() => books.value)

const categories = [
  { label: 'A - 马克思主义', value: 'A' },
  { label: 'B - 哲学', value: 'B' },
  { label: 'C - 社会科学总论', value: 'C' },
  { label: 'D - 政治、法律', value: 'D' },
  { label: 'E - 军事', value: 'E' },
  { label: 'F - 经济', value: 'F' },
  { label: 'G - 文化、科学、教育、体育', value: 'G' },
  { label: 'H - 语言、文字', value: 'H' },
  { label: 'I - 文学', value: 'I' },
  { label: 'J - 艺术', value: 'J' },
  { label: 'K - 历史、地理', value: 'K' },
  { label: 'N - 自然科学总论', value: 'N' },
  { label: 'O - 数理科学和化学', value: 'O' },
  { label: 'P - 天文学、地球科学', value: 'P' },
  { label: 'Q - 生物科学', value: 'Q' },
  { label: 'R - 医药、卫生', value: 'R' },
  { label: 'S - 农业科学', value: 'S' },
  { label: 'T - 工业技术', value: 'T' },
  { label: 'U - 交通运输', value: 'U' },
  { label: 'V - 航空、航天', value: 'V' },
  { label: 'X - 环境科学', value: 'X' },
  { label: 'Z - 综合性图书', value: 'Z' }
]

const handleSearch = () => {
  searchBooks()
}

const handleReset = () => {
  searchForm.value = {
    bookName: '',
    author: '',
    categories: [],
    minSales: null,
    maxSales: null,
    minPrice: null,
    maxPrice: null,
    minRating: null,
    maxRating: null,
    sortBy: 'sales',
    sortOrder: 'desc',
    page: 1,
    size: 12
  }
  searchBooks()
}

const handlePageChange = (page) => {
  searchForm.value.page = page
  searchBooks()
}

const handleSizeChange = (size) => {
  searchForm.value.size = size
  searchForm.value.page = 1
  searchBooks()
}

const goToBookDetail = (bookId) => {
  router.push(`/user/book/${bookId}`)
}

onMounted(() => {
  // 初始加载所有书籍
  searchBooks()
})
</script>

<style scoped>
.search-page {
  padding: 20px 0;
}

.search-header {
  text-align: center;
  margin-bottom: 30px;
}

.search-header h1 {
  font-size: 32px;
  color: #1f2937;
}

.search-form {
  margin-bottom: 40px;
}

.results-header {
  margin-bottom: 20px;
}

.results-header h3 {
  font-size: 20px;
  color: #1f2937;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
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
  width: 100%;
  padding-top: 140%;
  position: relative;
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

.book-author,
.book-publisher {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.price {
  font-size: 18px;
  font-weight: 700;
  color: #7c3aed;
}

.rating {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #f59e0b;
}

.book-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #9ca3af;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 20px 0;
}
</style>
