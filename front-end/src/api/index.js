import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

// 认证相关API
export const authAPI = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data)
}

// 书籍相关API
export const bookAPI = {
  getList: () => request.get('/books/list'),
  getById: (id) => request.get(`/books/${id}`),
  search: (data) => request.post('/books/search', data),
  getRankings: () => request.get('/books/rankings'),
  getRecommendations: (userId) => request.get(`/books/recommend/${userId}`),
  save: (data) => request.post('/books/save', data),
  remove: (id) => request.delete(`/books/remove/${id}`),
  removeBatch: (ids) => request.post('/books/remove/batch', ids),
  restore: (id) => request.put(`/books/restore/${id}`),
  delete: (id) => request.delete(`/books/delete/${id}`),
  deleteBatch: (ids) => request.post('/books/delete/batch', ids),
  uploadImage: (file, bookId) => {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('bookId', bookId)
    return request.post('/books/upload-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  saveWithImage: (bookData, imageFile) => {
    const formData = new FormData()
    formData.append('bookData', JSON.stringify(bookData))
    if (imageFile) {
      formData.append('imageFile', imageFile)
    }
    return request.post('/books/save-with-image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  importFromExcel: (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/books/import-excel', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

// 购物车相关API
export const cartAPI = {
  getList: (userId) => request.get(`/cart/${userId}`),
  add: (data) => request.post('/cart/add', data),
  update: (data) => request.put('/cart/update', data),
  remove: (id) => request.delete(`/cart/remove/${id}`),
  clear: (userId) => request.delete(`/cart/clear/${userId}`)
}

// 订单相关API
export const orderAPI = {
  create: (data) => request.post('/orders/create', data),
  getList: (userId) => request.get(`/orders/${userId}`),
  getStatistics: () => request.get('/orders/statistics'),
  prepareConfirm: (data) => request.post('/orders/confirm', data),
  createConfirmed: (data) => request.post('/orders/create-confirmed', data),
  cancel: (orderId, data) => request.post(`/orders/cancel/${orderId}`, data),
  receive: (orderId, data) => request.post(`/orders/receive/${orderId}`, data)
}

// 用户相关API
export const userAPI = {
  getInfo: (userId) => request.get(`/users/${userId}`),
  update: (data) => request.put('/users/update', data),
  getAddresses: (userId) => request.get(`/users/${userId}/addresses`),
  addAddress: (data) => request.post('/users/addresses', data),
  updateAddress: (data) => request.put('/users/addresses', data),
  deleteAddress: (id) => request.delete(`/users/addresses/${id}`)
}

// 评分相关API
export const scoreAPI = {
  rate: (data) => request.post('/scores/rate', data),
  getUserScore: (userId, bookId) => request.get(`/scores/${userId}/${bookId}`)
}

// 轮播图相关API
export const carouselAPI = {
  getList: () => request.get('/carousel/list'),
  add: (data) => request.post('/carousel/add', data),
  remove: (id) => request.delete(`/carousel/remove/${id}`)
}

// 管理员相关API
export const adminAPI = {
  login: (data) => request.post('/auth/admin/login', data),
  getList: () => request.get('/admin/list'),
  add: (data) => request.post('/admin/add', data),
  remove: (id) => request.delete(`/admin/remove/${id}`),
  changePassword: (data) => request.put('/admin/change-password', data)
}

// 优惠券相关API
export const couponAPI = {
  getUserCoupons: (userId) => request.get(`/coupons/user/${userId}`),
  claim: (data) => request.post('/coupons/claim', data),
  claimDaily: (data) => request.post('/coupons/daily', data),
  getAvailable: (userId, orderAmount) => request.get(`/coupons/available/${userId}`, { 
    params: orderAmount ? { orderAmount } : {} 
  }),
  getExpiring: (userId) => request.get(`/coupons/expiring/${userId}`),
  calculateDiscount: (couponId, orderAmount) => request.get('/coupons/calculate-discount', {
    params: { couponId, orderAmount }
  })
}

// 用户等级相关API
export const userLevelAPI = {
  getInfo: (userId) => request.get(`/user-level/info/${userId}`),
  getDescriptions: () => request.get('/user-level/descriptions'),
  updateLevel: (userId) => request.post(`/user-level/update/${userId}`)
}

export default request
