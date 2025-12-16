<template>
  <div class="profile-page">
    <div class="container">
      <div class="page-header">
        <h1>个人中心</h1>
      </div>

      <el-row :gutter="20">
        <!-- 用户信息 -->
        <el-col :span="12">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
                <el-button type="primary" text @click="editDialogVisible = true">编辑</el-button>
              </div>
            </template>

            <div class="info-list">
              <div class="info-item">
                <span class="label">用户ID：</span>
                <span class="value">{{ userInfo.userId }}</span>
              </div>
              <div class="info-item">
                <span class="label">用户名：</span>
                <span class="value">{{ userInfo.username }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机号：</span>
                <span class="value">{{ userInfo.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">用户等级：</span>
                <el-tag :type="getLevelType(userInfo.userLevel)">
                  LV{{ userInfo.userLevel }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="label">累计消费：</span>
                <span class="value highlight">¥{{ userInfo.totalSpending || 0 }}</span>
              </div>
            </div>
          </el-card>
        </el-col>

        <!-- 地址管理 -->
        <el-col :span="12">
          <el-card class="address-card">
            <template #header>
              <div class="card-header">
                <span>收货地址</span>
                <el-button type="primary" text @click="addAddressDialogVisible = true">添加地址</el-button>
              </div>
            </template>

            <div v-if="addresses.length > 0" class="address-list">
              <div v-for="addr in addresses" :key="addr.addressId" class="address-item">
                <div class="address-content">
                  <el-icon v-if="addr.isDefault === 1" color="#7c3aed"><Location /></el-icon>
                  <span>{{ addr.address }}</span>
                  <el-tag v-if="addr.isDefault === 1" type="success" size="small">默认</el-tag>
                </div>
                <div class="address-actions">
                  <el-button type="danger" text size="small" @click="handleDeleteAddress(addr.addressId)">
                    删除
                  </el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无收货地址" :image-size="100" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 等级说明 -->
      <el-card class="level-card">
        <template #header>
          <span>会员等级说明</span>
        </template>
        <div class="level-info">
          <div class="level-item">
            <div class="level-header">
              <el-tag type="info">LV0 普通会员</el-tag>
              <span class="level-requirement">消费 0-30元</span>
            </div>
            <div class="level-benefits">
              <p><el-icon><Close /></el-icon> 无折扣</p>
              <p><el-icon><Close /></el-icon> 无免运费</p>
              <p><el-icon><Close /></el-icon> 无每日优惠券</p>
            </div>
          </div>
          <div class="level-item">
            <div class="level-header">
              <el-tag type="success">LV1 铜牌会员</el-tag>
              <span class="level-requirement">消费 30-198元</span>
            </div>
            <div class="level-benefits">
              <p><el-icon><Check /></el-icon> 98折优惠</p>
              <p><el-icon><Close /></el-icon> 无免运费</p>
              <p><el-icon><Close /></el-icon> 无每日优惠券</p>
            </div>
          </div>
          <div class="level-item">
            <div class="level-header">
              <el-tag type="warning">LV2 银牌会员</el-tag>
              <span class="level-requirement">消费 198-328元</span>
            </div>
            <div class="level-benefits">
              <p><el-icon><Check /></el-icon> 97折优惠</p>
              <p><el-icon><Close /></el-icon> 无免运费</p>
              <p><el-icon><Close /></el-icon> 无每日优惠券</p>
            </div>
          </div>
          <div class="level-item">
            <div class="level-header">
              <el-tag type="danger">LV3 金牌会员</el-tag>
              <span class="level-requirement">消费 328-648元</span>
            </div>
            <div class="level-benefits">
              <p><el-icon><Check /></el-icon> 95折优惠</p>
              <p><el-icon><Close /></el-icon> 无免运费</p>
              <p><el-icon><Close /></el-icon> 无每日优惠券</p>
            </div>
          </div>
          <div class="level-item">
            <div class="level-header">
              <el-tag type="danger">LV4 白金会员</el-tag>
              <span class="level-requirement">消费 648-1998元</span>
            </div>
            <div class="level-benefits">
              <p><el-icon><Check /></el-icon> 95折优惠</p>
              <p><el-icon><Check /></el-icon> 免运费</p>
              <p><el-icon><Close /></el-icon> 无每日优惠券</p>
            </div>
          </div>
          <div class="level-item">
            <div class="level-header">
              <el-tag type="danger">LV5 钻石会员</el-tag>
              <span class="level-requirement">消费 1998元以上</span>
            </div>
            <div class="level-benefits">
              <p><el-icon><Check /></el-icon> 95折优惠</p>
              <p><el-icon><Check /></el-icon> 免运费</p>
              <p><el-icon><Check /></el-icon> 每日优惠券(¥5)</p>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 编辑信息对话框 -->
      <el-dialog v-model="editDialogVisible" title="编辑信息" width="500px">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="editForm.username" maxlength="16" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="editForm.phone" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpdateInfo">确定</el-button>
        </template>
      </el-dialog>

      <!-- 添加地址对话框 -->
      <el-dialog v-model="addAddressDialogVisible" title="添加地址" width="500px">
        <el-form :model="addressForm" label-width="80px">
          <el-form-item label="收货地址">
            <el-input v-model="addressForm.address" type="textarea" :rows="3" />
          </el-form-item>
          <el-form-item label="默认地址">
            <el-switch v-model="addressForm.isDefault" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="addAddressDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddAddress">确定</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { userAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const userInfo = ref(userStore.userInfo)
const addresses = ref([])
const editDialogVisible = ref(false)
const addAddressDialogVisible = ref(false)

const editForm = reactive({
  username: '',
  phone: ''
})

const addressForm = reactive({
  address: '',
  isDefault: false
})

const getLevelType = (level) => {
  const types = ['info', 'success', 'warning', 'danger', 'danger', 'danger']
  return types[level] || 'info'
}

const loadUserInfo = async () => {
  try {
    const res = await userAPI.getInfo(userInfo.value.userId)
    userInfo.value = res.data
    userStore.setUserInfo(res.data)
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

const loadAddresses = async () => {
  try {
    const res = await userAPI.getAddresses(userInfo.value.userId)
    addresses.value = res.data
  } catch (error) {
    console.error('加载地址失败', error)
  }
}

const handleUpdateInfo = async () => {
  try {
    await userAPI.update({
      userId: userInfo.value.userId,
      ...editForm
    })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    // 重新加载用户信息
    const res = await userAPI.getInfo(userInfo.value.userId)
    userInfo.value = res.data
    userStore.setUserInfo(res.data)
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleAddAddress = async () => {
  try {
    await userAPI.addAddress({
      userId: userInfo.value.userId,
      address: addressForm.address,
      isDefault: addressForm.isDefault ? 1 : 0
    })
    ElMessage.success('添加成功')
    addAddressDialogVisible.value = false
    addressForm.address = ''
    addressForm.isDefault = false
    loadAddresses()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const handleDeleteAddress = async (addressId) => {
  try {
    await userAPI.deleteAddress(addressId)
    ElMessage.success('删除成功')
    loadAddresses()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadUserInfo()
  loadAddresses()
  editForm.username = userInfo.value.username
  editForm.phone = userInfo.value.phone
})
</script>

<style scoped>
.profile-page {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.label {
  font-size: 14px;
  color: #6b7280;
  min-width: 80px;
}

.value {
  font-size: 16px;
  color: #1f2937;
  font-weight: 500;
}

.value.highlight {
  color: #7c3aed;
  font-weight: 700;
  font-size: 18px;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f9fafb;
  border-radius: 8px;
}

.address-content {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.level-card {
  margin-top: 20px;
}

.level-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.level-item {
  padding: 20px;
  background: #f9fafb;
  border-radius: 12px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s ease;
}

.level-item:hover {
  border-color: #7c3aed;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(124, 58, 237, 0.15);
}

.level-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e5e7eb;
}

.level-requirement {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.level-benefits {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.level-benefits p {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 14px;
  color: #374151;
}

.level-benefits .el-icon {
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .level-info {
    grid-template-columns: 1fr;
  }
  
  .level-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
