<template>
  <div class="book-management-page">
    <div class="page-header">
      <h1>书籍管理</h1>
      <div class="header-actions">
        <el-button type="primary" @click="addDialogVisible = true">
          <el-icon><Plus /></el-icon>
          添加书籍
        </el-button>
        <el-button type="success" @click="showImportDialog">
          <el-icon><Upload /></el-icon>
          Excel导入
        </el-button>
        <el-button type="danger" @click="handleBatchRemove" :disabled="selectedBooks.length === 0">
          <el-icon><Delete /></el-icon>
          批量下架
        </el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedBooks.length === 0">
          <el-icon><Delete /></el-icon>
          批量删除
        </el-button>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card style="margin-bottom: 20px;">
      <el-form :model="searchForm" inline>
        <el-form-item label="书名">
          <el-input v-model="searchForm.bookName" placeholder="请输入书名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="searchForm.author" placeholder="请输入作者" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="searchForm.publisher" placeholder="请输入出版社" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.category" placeholder="选择分类" clearable style="width: 200px">
            <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 书籍列表 -->
    <el-card>
      <el-table :data="books" @selection-change="handleSelectionChange" stripe v-loading="loading">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="bookId" label="ID" width="80" />
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <img :src="getImageUrl(row.bookImage)" style="width: 60px; height: 80px; object-fit: cover; border-radius: 4px;" />
          </template>
        </el-table-column>
        <el-table-column prop="bookName" label="书名" min-width="150" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="publisher" label="出版社" width="150" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column prop="rating" label="评分" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" text size="small" @click="handleRemove(row.bookId)" v-if="row.status === 1">
              下架
            </el-button>
            <el-button type="success" text size="small" @click="handleRestore(row.bookId)" v-else>
              上架
            </el-button>
            <el-button type="danger" text size="small" @click="handleDelete(row.bookId)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-wrapper">
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
    </el-card>

    <!-- 添加/编辑书籍对话框 -->
    <el-dialog v-model="addDialogVisible" :title="editMode ? '编辑书籍' : '添加书籍'" width="600px">
      <el-form :model="bookForm" label-width="100px">
        <el-form-item label="书名">
          <el-input v-model="bookForm.bookName" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="bookForm.author" />
        </el-form-item>
        <el-form-item label="出版社">
          <el-input v-model="bookForm.publisher" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="bookForm.category" placeholder="选择分类">
            <el-option v-for="cat in categories" :key="cat.value" :label="cat.label" :value="cat.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="bookForm.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="bookForm.stock" :min="0" />
        </el-form-item>
        <el-form-item label="图片">
          <div class="image-upload-section">
            <el-upload
              ref="imageUpload"
              :auto-upload="false"
              :show-file-list="false"
              accept="image/*"
              :on-change="handleImageChange"
              class="image-uploader"
            >
              <template #trigger>
                <el-button type="primary">选择图片</el-button>
              </template>
            </el-upload>
            <div v-if="imagePreview" class="image-preview">
              <img :src="imagePreview" alt="预览" />
              <el-button type="danger" size="small" @click="clearImage">删除</el-button>
            </div>
            <div v-else-if="bookForm.bookImage" class="current-image">
              <img :src="getImageUrl(bookForm.bookImage)" alt="当前图片" />
              <p>当前图片</p>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="bookForm.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Excel导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="Excel导入书籍" width="600px">
      <div class="import-dialog-content">
        <el-alert
          title="导入说明"
          type="info"
          description="请上传.xlsx格式的Excel文件，第一行为表头：书本名、分类、作者、简介、出版社、价格、库存。分类必须是A-Z的单个字母。"
          show-icon
          :closable="false"
          style="margin-bottom: 20px;"
        />
        
        <el-upload
          ref="excelUpload"
          :auto-upload="false"
          :show-file-list="true"
          accept=".xlsx"
          :limit="1"
          :on-change="handleExcelChange"
          :on-remove="clearExcelFile"
          drag
          class="excel-uploader"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将Excel文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传.xlsx文件，且不超过10MB
            </div>
          </template>
        </el-upload>

        <!-- 导入结果显示 -->
        <div v-if="importResult" class="import-result">
          <el-divider>导入结果</el-divider>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总数">{{ importResult.totalCount }}</el-descriptions-item>
            <el-descriptions-item label="成功">{{ importResult.successCount }}</el-descriptions-item>
            <el-descriptions-item label="跳过">{{ importResult.skipCount }}</el-descriptions-item>
            <el-descriptions-item label="错误">{{ importResult.errorCount }}</el-descriptions-item>
          </el-descriptions>
          
          <!-- 错误信息列表 -->
          <div v-if="importResult.errorMessages && importResult.errorMessages.length > 0" class="error-messages">
            <h4>错误详情：</h4>
            <el-scrollbar height="200px">
              <ul>
                <li v-for="(error, index) in importResult.errorMessages" :key="index" class="error-item">
                  {{ error }}
                </li>
              </ul>
            </el-scrollbar>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="importDialogVisible = false">关闭</el-button>
        <el-button 
          type="primary" 
          @click="handleImportExcel" 
          :loading="importing"
          :disabled="!selectedExcelFile"
        >
          {{ importing ? '导入中...' : '开始导入' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Upload, UploadFilled } from '@element-plus/icons-vue'
import { bookAPI } from '@/api'

const books = ref([])
const selectedBooks = ref([])
const loading = ref(false)
const pagination = ref({
  page: 1,
  size: 10,
  total: 0,
  totalPages: 0
})
const searchForm = ref({
  bookName: '',
  author: '',
  publisher: '',
  category: '',
  minPrice: null,
  maxPrice: null,
  status: null,
  sortBy: 'createdAt',
  sortOrder: 'desc',
  page: 1,
  size: 10
})
const addDialogVisible = ref(false)
const editMode = ref(false)
const imageUpload = ref(null)
const imagePreview = ref('')
const selectedImageFile = ref(null)

// Excel导入相关
const importDialogVisible = ref(false)
const excelUpload = ref(null)
const selectedExcelFile = ref(null)
const importing = ref(false)
const importResult = ref(null)

const bookForm = reactive({
  bookId: null,
  bookName: '',
  author: '',
  publisher: '',
  category: '',
  price: 0,
  stock: 0,
  bookImage: '',
  description: ''
})

const categories = [
  { label: 'A - 马克思主义、列宁主义、毛泽东思想', value: 'A' },
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
  { label: 'X - 环境科学、安全科学', value: 'X' },
  { label: 'Z - 综合性图书', value: 'Z' }
]

const loadBooks = async () => {
  loading.value = true
  try {
    const res = await bookAPI.searchAdmin(searchForm.value)
    books.value = res.data.content
    pagination.value = {
      page: res.data.page,
      size: res.data.size,
      total: res.data.total,
      totalPages: res.data.totalPages
    }
  } catch (error) {
    console.error('加载书籍失败', error)
    ElMessage.error('加载书籍失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  searchForm.value.page = page
  loadBooks()
}

const handleSizeChange = (size) => {
  searchForm.value.size = size
  searchForm.value.page = 1
  loadBooks()
}

const handleSearch = () => {
  searchForm.value.page = 1
  loadBooks()
}

const handleReset = () => {
  searchForm.value = {
    bookName: '',
    author: '',
    publisher: '',
    category: '',
    minPrice: null,
    maxPrice: null,
    status: null,
    sortBy: 'createdAt',
    sortOrder: 'desc',
    page: 1,
    size: 10
  }
  loadBooks()
}

const handleSelectionChange = (selection) => {
  selectedBooks.value = selection
}

const handleEdit = (row) => {
  editMode.value = true
  Object.assign(bookForm, row)
  imagePreview.value = ''
  selectedImageFile.value = null
  addDialogVisible.value = true
}

const handleImageChange = (file) => {
  const isImage = file.raw.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能选择图片文件')
    return false
  }

  const isLt5M = file.raw.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过5MB')
    return false
  }

  selectedImageFile.value = file.raw
  imagePreview.value = URL.createObjectURL(file.raw)
  return true
}

const clearImage = () => {
  imagePreview.value = ''
  selectedImageFile.value = null
  if (imageUpload.value) {
    imageUpload.value.clearFiles()
  }
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

const handleSave = async () => {
  try {
    if (selectedImageFile.value) {
      // 有新图片，使用新的上传接口
      const bookData = { ...bookForm }
      delete bookData.bookImage // 删除旧的图片路径
      
      const res = await bookAPI.saveWithImage(bookData, selectedImageFile.value)
      ElMessage.success('保存成功')
      addDialogVisible.value = false
      resetForm()
      loadBooks()
    } else {
      // 没有新图片，使用原有接口
      await bookAPI.save(bookForm)
      ElMessage.success('保存成功')
      addDialogVisible.value = false
      resetForm()
      loadBooks()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleRemove = async (bookId) => {
  try {
    await ElMessageBox.confirm('确定要下架该书籍吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await bookAPI.remove(bookId)
    ElMessage.success('下架成功')
    loadBooks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('下架失败')
    }
  }
}

const handleRestore = async (bookId) => {
  try {
    await ElMessageBox.confirm('确定要上架该书籍吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await bookAPI.restore(bookId)
    ElMessage.success('上架成功')
    loadBooks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('上架失败')
    }
  }
}

const handleDelete = async (bookId) => {
  try {
    await ElMessageBox.confirm('确定要删除该书籍吗？删除后将从数据库中彻底移除且无法恢复！', '危险操作', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })

    await bookAPI.delete(bookId)
    ElMessage.success('删除成功')
    loadBooks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchRemove = async () => {
  try {
    await ElMessageBox.confirm(`确定要下架选中的 ${selectedBooks.value.length} 本书籍吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const bookIds = selectedBooks.value.map(b => b.bookId)
    await bookAPI.removeBatch(bookIds)
    ElMessage.success('批量下架成功')
    loadBooks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量下架失败')
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedBooks.value.length} 本书籍吗？删除后将从数据库中彻底移除且无法恢复！`, '危险操作', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })

    const bookIds = selectedBooks.value.map(b => b.bookId)
    await bookAPI.deleteBatch(bookIds)
    ElMessage.success('批量删除成功')
    loadBooks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
    }
  }
}

const resetForm = () => {
  editMode.value = false
  Object.assign(bookForm, {
    bookId: null,
    bookName: '',
    author: '',
    publisher: '',
    category: '',
    price: 0,
    stock: 0,
    bookImage: '',
    description: ''
  })
  clearImage()
}

// Excel导入相关方法
const showImportDialog = () => {
  importDialogVisible.value = true
  clearImportData()
}

const clearImportData = () => {
  selectedExcelFile.value = null
  importResult.value = null
  importing.value = false
  if (excelUpload.value) {
    excelUpload.value.clearFiles()
  }
}

const handleExcelChange = (file) => {
  // 验证文件类型
  if (!file.name.toLowerCase().endsWith('.xlsx')) {
    ElMessage.error('只能上传.xlsx格式的Excel文件')
    return false
  }

  // 验证文件大小（10MB）
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('Excel文件大小不能超过10MB')
    return false
  }

  selectedExcelFile.value = file.raw
  importResult.value = null // 清空之前的导入结果
  return true
}

const clearExcelFile = () => {
  selectedExcelFile.value = null
  importResult.value = null
}

const handleImportExcel = async () => {
  if (!selectedExcelFile.value) {
    ElMessage.error('请先选择Excel文件')
    return
  }

  try {
    importing.value = true
    importResult.value = null

    const res = await bookAPI.importFromExcel(selectedExcelFile.value)
    importResult.value = res.data

    // 根据导入结果显示不同的消息
    if (res.data.errorCount > 0) {
      ElMessage.warning(`导入完成！成功${res.data.successCount}本，跳过${res.data.skipCount}本，错误${res.data.errorCount}本`)
    } else {
      ElMessage.success(`导入成功！共导入${res.data.successCount}本书籍`)
    }

    // 刷新书籍列表
    await loadBooks()

  } catch (error) {
    console.error('Excel导入失败', error)
    ElMessage.error(error.response?.data?.message || 'Excel导入失败')
  } finally {
    importing.value = false
  }
}

onMounted(() => {
  loadBooks()
})
</script>

<style scoped>
.book-management-page {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  color: #1f2937;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.image-upload-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.image-uploader {
  display: flex;
  align-items: center;
}

.image-preview,
.current-image {
  display: flex;
  align-items: center;
  gap: 10px;
}

.image-preview img,
.current-image img {
  width: 80px;
  height: 100px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.current-image p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

/* Excel导入相关样式 */
.import-dialog-content {
  padding: 10px 0;
}

.excel-uploader {
  width: 100%;
  margin-bottom: 20px;
}

.import-result {
  margin-top: 20px;
}

.error-messages {
  margin-top: 15px;
}

.error-messages h4 {
  margin: 0 0 10px 0;
  color: #f56c6c;
  font-size: 14px;
}

.error-item {
  color: #f56c6c;
  font-size: 12px;
  margin-bottom: 5px;
  padding: 2px 0;
}

.error-item:last-child {
  margin-bottom: 0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px 0;
}
</style>
