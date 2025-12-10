<template>
  <div class="carousel-management-page">
    <div class="page-header">
      <h1>轮播图管理</h1>
      <el-button type="primary" @click="addDialogVisible = true">
        <el-icon><Plus /></el-icon>
        添加轮播图
      </el-button>
    </div>

    <el-card>
      <el-table :data="carouselItems" stripe>
        <el-table-column prop="carouselId" label="ID" width="80" />
        <el-table-column label="书籍封面" width="150">
          <template #default="{ row }">
            <img :src="getImageUrl(row.bookImage)" style="width: 100px; height: 60px; object-fit: cover; border-radius: 4px;" />
          </template>
        </el-table-column>
        <el-table-column prop="bookId" label="书籍ID" width="100" />
        <el-table-column prop="bookName" label="书籍名称" min-width="200" />
        <el-table-column prop="author" label="作者" width="150" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="danger" text size="small" @click="handleRemove(row.carouselId)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加轮播图对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加轮播图" width="500px">
      <el-form :model="carouselForm" label-width="100px">
        <el-form-item label="书籍ID">
          <el-input-number v-model="carouselForm.bookId" :min="1" />
        </el-form-item>
        <el-form-item label="排序顺序">
          <el-input-number v-model="carouselForm.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { carouselAPI } from '@/api'
import { getImageUrl } from '@/utils/imageUtils'

const carouselItems = ref([])
const addDialogVisible = ref(false)

const carouselForm = reactive({
  bookId: null,
  sortOrder: 0
})

const loadCarouselItems = async () => {
  try {
    const res = await carouselAPI.getList()
    carouselItems.value = res.data
  } catch (error) {
    console.error('加载轮播图失败', error)
  }
}

const handleAdd = async () => {
  try {
    await carouselAPI.add(carouselForm)
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    carouselForm.bookId = null
    carouselForm.sortOrder = 0
    loadCarouselItems()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const handleRemove = async (carouselId) => {
  try {
    await ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await carouselAPI.remove(carouselId)
    ElMessage.success('删除成功')
    loadCarouselItems()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadCarouselItems()
})
</script>

<style scoped>
.carousel-management-page {
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
</style>
