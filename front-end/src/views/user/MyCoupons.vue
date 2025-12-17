<template>
  <div class="my-coupons-page">
    <div class="container">
      <div class="page-header">
        <h1>我的优惠券</h1>
      </div>

      <!-- 统计信息 -->
      <el-card class="stats-card">
        <div class="stats-content">
          <div class="stat-item">
            <span class="stat-value">{{ totalCoupons }}</span>
            <span class="stat-label">总优惠券</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ availableCoupons }}</span>
            <span class="stat-label">可用优惠券</span>
          </div>
          <div class="stat-item">
            <span class="stat-value">{{ expiringCoupons }}</span>
            <span class="stat-label">即将过期</span>
          </div>
        </div>
      </el-card>

      <!-- 筛选和排序 -->
      <el-card class="filter-card">
        <div class="filter-content">
          <div class="filter-left">
            <el-select v-model="statusFilter" placeholder="状态筛选" @change="loadCoupons">
              <el-option label="全部" value="" />
              <el-option label="可用" value="available" />
              <el-option label="已过期" value="expired" />
              <el-option label="不可用" value="unusable" />
            </el-select>
          </div>
          <div class="filter-right">
            <el-select v-model="sortBy" placeholder="排序方式" @change="loadCoupons">
              <el-option label="获取时间" value="obtained" />
              <el-option label="优惠额度(大到小)" value="discount_desc" />
              <el-option label="即将过期" value="expiring" />
            </el-select>
          </div>
        </div>
      </el-card>

      <!-- 优惠券列表 -->
      <el-card class="coupons-list">
        <div v-if="coupons.length > 0" class="coupons-grid">
          <div 
            v-for="coupon in coupons" 
            :key="`${coupon.couponId}-${coupon.obtainedAt}`"
            class="coupon-card"
            :class="{ 
              'expired': coupon.isExpired,
              'unusable': !coupon.isUsable
            }"
          >
            <div class="coupon-header">
              <div class="coupon-value">
                <span class="currency">¥</span>
                <span class="amount">{{ coupon.discountAmount }}</span>
              </div>
              <div class="coupon-status">
                <el-tag v-if="coupon.isExpired" type="danger" size="small">已过期</el-tag>
                <el-tag v-else-if="!coupon.isUsable" type="warning" size="small">不可用</el-tag>
                <el-tag v-else type="success" size="small">可用</el-tag>
              </div>
            </div>
            
            <div class="coupon-body">
              <h3 class="coupon-name">{{ coupon.couponName }}</h3>
              <div class="coupon-info">
                <p v-if="coupon.minAmount > 0">满{{ coupon.minAmount }}元可用</p>
                <p v-else>无门槛</p>
                <p v-if="coupon.validDays > 0">有效期{{ coupon.validDays }}天</p>
                <p v-else>永久有效</p>
              </div>
            </div>
            
            <div class="coupon-footer">
              <div class="obtained-info">
                <p>获取时间：{{ formatDate(coupon.obtainedAt) }}</p>
                <p v-if="coupon.expiryDate">过期时间：{{ formatDate(coupon.expiryDate) }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <el-empty v-else description="暂无优惠券">
          <el-button type="primary" @click="claimDailyCoupon">领取每日优惠券</el-button>
        </el-empty>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { couponAPI, userLevelAPI } from '@/api'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const coupons = ref([])
const statusFilter = ref('')
const sortBy = ref('obtained')
const claimingDaily = ref(false)

// 计算统计数据
const totalCoupons = computed(() => coupons.value.length)
const availableCoupons = computed(() => 
  coupons.value.filter(c => !c.isExpired && c.isUsable).length
)
const expiringCoupons = computed(() => 
  coupons.value.filter(c => {
    if (!c.expiryDate) return false
    const now = new Date()
    const expiry = new Date(c.expiryDate)
    const diffTime = expiry - now
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    return diffDays <= 1 && diffDays > 0
  }).length
)

// 加载优惠券列表
const loadCoupons = async () => {
  try {
    const userId = userStore.userInfo.userId
    let res
    
    if (statusFilter.value === 'expiring') {
      res = await couponAPI.getExpiring(userId)
    } else {
      res = await couponAPI.getUserCoupons(userId)
    }
    
    let filteredCoupons = res.data || []
    
    // 状态筛选
    if (statusFilter.value === 'available') {
      filteredCoupons = filteredCoupons.filter(c => !c.isExpired && c.isUsable)
    } else if (statusFilter.value === 'expired') {
      filteredCoupons = filteredCoupons.filter(c => c.isExpired)
    } else if (statusFilter.value === 'unusable') {
      filteredCoupons = filteredCoupons.filter(c => !c.isUsable && !c.isExpired)
    }
    
    // 排序
    if (sortBy.value === 'discount_desc') {
      filteredCoupons.sort((a, b) => b.discountAmount - a.discountAmount)
    } else if (sortBy.value === 'expiring') {
      filteredCoupons.sort((a, b) => {
        if (!a.expiryDate) return 1
        if (!b.expiryDate) return -1
        return new Date(a.expiryDate) - new Date(b.expiryDate)
      })
    } else {
      filteredCoupons.sort((a, b) => new Date(b.obtainedAt) - new Date(a.obtainedAt))
    }
    
    coupons.value = filteredCoupons
  } catch (error) {
    console.error('加载优惠券失败', error)
    ElMessage.error('加载优惠券失败')
  }
}

// 领取每日优惠券
const claimDailyCoupon = async () => {
  try {
    claimingDaily.value = true
    
    // 检查用户等级
    const levelRes = await userLevelAPI.getInfo(userStore.userInfo.userId)
    const userLevel = levelRes.data.userLevel
    
    if (userLevel < 5) {
      ElMessage.warning('只有钻石会员才能领取每日优惠券')
      return
    }
    
    const res = await couponAPI.claimDaily({
      userId: userStore.userInfo.userId
    })
    
    ElMessage.success(res.message || '领取成功')
    loadCoupons()
  } catch (error) {
    console.error('领取每日优惠券失败', error)
    ElMessage.error(error.message || '领取失败')
  } finally {
    claimingDaily.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
}

onMounted(() => {
  loadCoupons()
})
</script>

<style scoped>
.my-coupons-page {
  padding: 20px 0;
  background: #f5f5f5;
  min-height: 100vh;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 32px;
  color: #1f2937;
  margin: 0;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-content {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 36px;
  font-weight: 700;
  color: #7c3aed;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.filter-left,
.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 下拉框宽度设置 */
.filter-left .el-select,
.filter-right .el-select {
  width: 140px;
}

.coupons-list {
  min-height: 400px;
}

.coupons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.coupon-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  border: 2px solid #e5e7eb;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.coupon-card:hover:not(.expired):not(.unusable) {
  border-color: #7c3aed;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(124, 58, 237, 0.15);
}

.coupon-card.expired {
  opacity: 0.6;
  background: #f9fafb;
}

.coupon-card.unusable {
  opacity: 0.7;
  background: #fef3c7;
}

.coupon-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.coupon-value {
  display: flex;
  align-items: baseline;
  color: #dc2626;
}

.currency {
  font-size: 18px;
  font-weight: 600;
  margin-right: 4px;
}

.amount {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.coupon-body {
  margin-bottom: 16px;
}

.coupon-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
}

.coupon-info p {
  margin: 4px 0;
  font-size: 14px;
  color: #6b7280;
}

.coupon-footer {
  border-top: 1px solid #f3f4f6;
  padding-top: 12px;
}

.obtained-info p {
  margin: 4px 0;
  font-size: 12px;
  color: #9ca3af;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .stats-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .filter-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .coupons-grid {
    grid-template-columns: 1fr;
  }
}
</style>
