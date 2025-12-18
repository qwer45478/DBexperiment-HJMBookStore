<template>
  <div class="admin-management-page">
    <div class="page-header">
      <h1>管理员管理</h1>
      <el-button type="primary" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建管理员
      </el-button>
    </div>

    <el-card>
      <el-table :data="admins" stripe>
        <el-table-column prop="adminId" label="管理员ID" width="150" />
        <el-table-column prop="adminLevel" label="等级" width="100">
          <template #default="{ row }">
            <el-tag :type="row.adminLevel === 2 ? 'danger' : 'primary'">
              {{ row.adminLevel === 2 ? '超级管理员' : '普通管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" v-if="isLevel2Admin">
          <template #default="{ row }">
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :disabled="row.adminLevel === 2 || row.adminId === currentUserAdminId"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建管理员对话框 -->
    <el-dialog v-model="createDialogVisible" title="创建管理员" width="500px">
      <el-alert
        title="系统将自动生成管理员ID和密码"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      />
      
      <el-form :model="adminForm" label-width="100px">
        <el-form-item label="管理员等级">
          <el-radio-group v-model="adminForm.adminLevel">
            <el-radio :label="1">普通管理员</el-radio>
            <el-radio :label="2">超级管理员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="newAdminInfo"
        title="管理员创建成功！请妥善保存以下信息："
        type="success"
        :closable="false"
      >
        <div style="margin-top: 10px;">
          <p><strong>管理员ID：</strong>{{ newAdminInfo.adminId }}</p>
          <p><strong>初始密码：</strong>{{ newAdminInfo.password }}</p>
        </div>
      </el-alert>

      <template #footer>
        <el-button @click="createDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleConfirmCreate" v-if="!newAdminInfo">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const admins = ref([])
const createDialogVisible = ref(false)
const newAdminInfo = ref(null)

const adminForm = reactive({
  adminLevel: 1
})

// 计算属性：判断当前用户是否为2级管理员
const isLevel2Admin = computed(() => userStore.userInfo?.adminLevel === 2)

// 计算属性：获取当前管理员ID
const currentUserAdminId = computed(() => userStore.userInfo?.adminId)

const loadAdmins = async () => {
  try {
    const res = await adminAPI.getList()
    admins.value = res.data
  } catch (error) {
    console.error('加载管理员列表失败', error)
  }
}

const handleCreate = () => {
  newAdminInfo.value = null
  adminForm.adminLevel = 1
  createDialogVisible.value = true
}

const handleConfirmCreate = async () => {
  try {
    const res = await adminAPI.add(adminForm)
    newAdminInfo.value = res.data
    ElMessage.success('创建成功')
    loadAdmins()
  } catch (error) {
    ElMessage.error('创建失败')
  }
}

const handleDelete = async (admin) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除管理员"${admin.adminId}"吗？此操作不可撤销。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminAPI.delete(admin.adminId, currentUserAdminId.value)
    ElMessage.success('删除成功')
    loadAdmins()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadAdmins()
})
</script>

<style scoped>
.admin-management-page {
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
