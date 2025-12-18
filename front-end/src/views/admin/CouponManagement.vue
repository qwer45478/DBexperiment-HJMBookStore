<template>
  <div class="coupon-management-page">
    <div class="page-header">
      <h1>优惠券管理</h1>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        添加优惠券
      </el-button>
    </div>

    <!-- 优惠券列表 -->
    <el-card>
      <el-table :data="coupons" stripe v-loading="loading">
        <el-table-column prop="couponId" label="优惠券ID" width="100" />
        <el-table-column prop="couponName" label="优惠券名称" min-width="150" />
        <el-table-column prop="discountAmount" label="优惠额度" width="120">
          <template #default="{ row }">
            <span class="discount-amount">¥{{ row.discountAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="minAmount" label="使用门槛" width="120">
          <template #default="{ row }">
            {{ row.minAmount === 0 ? '无门槛' : `满${row.minAmount}元` }}
          </template>
        </el-table-column>
        <el-table-column prop="validDays" label="有效期" width="100">
          <template #default="{ row }">
            {{ row.validDays === 0 ? '永久' : `${row.validDays}天` }}
          </template>
        </el-table-column>
        <el-table-column prop="issuedCount" label="已发放" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleIssue(row)">
              发放
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              :disabled="row.issuedCount > 0"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加优惠券对话框 -->
    <el-dialog v-model="addDialogVisible" title="添加优惠券" width="500px">
      <el-form :model="couponForm" :rules="couponRules" ref="couponFormRef" label-width="100px">
        <el-form-item label="优惠券名称" prop="couponName">
          <el-input v-model="couponForm.couponName" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="优惠额度" prop="discountAmount">
          <el-input-number 
            v-model="couponForm.discountAmount" 
            :min="1" 
            :max="1000"
            placeholder="请输入优惠额度"
          />
          <span style="margin-left: 8px; color: #666;">元</span>
        </el-form-item>
        <el-form-item label="使用门槛" prop="minAmount">
          <el-input-number 
            v-model="couponForm.minAmount" 
            :min="0" 
            :max="10000"
            placeholder="0表示无门槛"
          />
          <span style="margin-left: 8px; color: #666;">元（0表示无门槛）</span>
        </el-form-item>
        <el-form-item label="有效期" prop="validDays">
          <el-input-number 
            v-model="couponForm.validDays" 
            :min="0" 
            :max="365"
            placeholder="0表示永久有效"
          />
          <span style="margin-left: 8px; color: #666;">天（0表示永久有效）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAdd" :loading="addLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 发放优惠券对话框 -->
    <el-dialog v-model="issueDialogVisible" title="发放优惠券" width="500px">
      <el-form :model="issueForm" :rules="issueRules" ref="issueFormRef" label-width="100px">
        <el-form-item label="优惠券">
          <el-input :value="selectedCoupon?.couponName" readonly />
        </el-form-item>
        <el-form-item label="发放数量" prop="quantity">
          <el-input-number 
            v-model="issueForm.quantity" 
            :min="1" 
            :max="10"
            placeholder="每人发放数量"
          />
          <span style="margin-left: 8px; color: #666;">张/人</span>
        </el-form-item>
        <el-form-item label="发放对象" prop="targetType">
          <el-radio-group v-model="issueForm.targetType">
            <el-radio label="level">按用户等级</el-radio>
            <el-radio label="user">指定用户ID</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="issueForm.targetType === 'level'" label="用户等级" prop="targetValue">
          <el-select v-model="issueForm.targetValue" placeholder="请选择用户等级">
            <el-option label="等级0 - 普通用户" :value="0" />
            <el-option label="等级1 - 铜牌会员" :value="1" />
            <el-option label="等级2 - 银牌会员" :value="2" />
            <el-option label="等级3 - 金牌会员" :value="3" />
            <el-option label="等级4 - 白金会员" :value="4" />
            <el-option label="等级5 - 钻石会员" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="issueForm.targetType === 'user'" label="用户ID" prop="targetValue">
          <el-input-number 
            v-model="issueForm.targetValue" 
            :min="100000"
            placeholder="请输入用户ID"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="issueDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmIssue" :loading="issueLoading">
          确定发放
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { couponAPI } from '@/api'

const coupons = ref([])
const loading = ref(false)
const addDialogVisible = ref(false)
const issueDialogVisible = ref(false)
const selectedCoupon = ref(null)
const addLoading = ref(false)
const issueLoading = ref(false)

const couponForm = reactive({
  couponName: '',
  discountAmount: null,
  minAmount: 0,
  validDays: 0
})

const issueForm = reactive({
  quantity: 1,
  targetType: 'level',
  targetValue: null
})

const couponFormRef = ref()
const issueFormRef = ref()

const couponRules = {
  couponName: [
    { required: true, message: '请输入优惠券名称', trigger: 'blur' },
    { max: 16, message: '优惠券名称不能超过16个字符', trigger: 'blur' }
  ],
  discountAmount: [
    { required: true, message: '请输入优惠额度', trigger: 'blur' },
    { type: 'number', min: 1, max: 1000, message: '优惠额度必须在1-1000之间', trigger: 'blur' }
  ],
  minAmount: [
    { required: true, message: '请输入使用门槛', trigger: 'blur' },
    { type: 'number', min: 0, max: 10000, message: '使用门槛必须在0-10000之间', trigger: 'blur' }
  ],
  validDays: [
    { required: true, message: '请输入有效期', trigger: 'blur' },
    { type: 'number', min: 0, max: 365, message: '有效期必须在0-365之间', trigger: 'blur' }
  ]
}

const issueRules = {
  quantity: [
    { required: true, message: '请输入发放数量', trigger: 'blur' },
    { type: 'number', min: 1, max: 10, message: '发放数量必须在1-10之间', trigger: 'blur' }
  ],
  targetValue: [
    { required: true, message: '请选择发放对象', trigger: 'blur' }
  ]
}

const loadCoupons = async () => {
  try {
    loading.value = true
    const res = await couponAPI.getAdminTypes()
    coupons.value = res.data
  } catch (error) {
    console.error('加载优惠券列表失败', error)
    ElMessage.error('加载优惠券列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  Object.assign(couponForm, {
    couponName: '',
    discountAmount: null,
    minAmount: 0,
    validDays: 0
  })
  addDialogVisible.value = true
}

const handleConfirmAdd = async () => {
  try {
    await couponFormRef.value.validate()
    addLoading.value = true
    
    await couponAPI.addAdminType(couponForm)
    ElMessage.success('优惠券添加成功')
    addDialogVisible.value = false
    loadCoupons()
  } catch (error) {
    if (error !== false) {
      console.error('添加优惠券失败', error)
      ElMessage.error(error.message || '添加优惠券失败')
    }
  } finally {
    addLoading.value = false
  }
}

const handleIssue = (coupon) => {
  selectedCoupon.value = coupon
  Object.assign(issueForm, {
    quantity: 1,
    targetType: 'level',
    targetValue: null
  })
  issueDialogVisible.value = true
}

const handleConfirmIssue = async () => {
  try {
    await issueFormRef.value.validate()
    issueLoading.value = true
    
    const request = {
      couponId: selectedCoupon.value.couponId,
      quantity: issueForm.quantity,
      targetType: issueForm.targetType,
      targetValue: issueForm.targetValue
    }
    
    await couponAPI.issueCoupons(request)
    ElMessage.success('优惠券发放成功')
    issueDialogVisible.value = false
    loadCoupons()
  } catch (error) {
    if (error !== false) {
      console.error('发放优惠券失败', error)
      ElMessage.error(error.message || '发放优惠券失败')
    }
  } finally {
    issueLoading.value = false
  }
}

const handleDelete = async (coupon) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除优惠券"${coupon.couponName}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await couponAPI.deleteAdminType(coupon.couponId)
    ElMessage.success('优惠券删除成功')
    loadCoupons()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除优惠券失败', error)
      ElMessage.error(error.message || '删除优惠券失败')
    }
  }
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadCoupons()
})
</script>

<style scoped>
.coupon-management-page {
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

.discount-amount {
  color: #dc2626;
  font-weight: 600;
}

.el-table {
  margin-top: 20px;
}

.el-form-item {
  margin-bottom: 20px;
}
</style>
