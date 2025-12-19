import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/user',
    name: 'UserLayout',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      {
        path: 'home',
        name: 'UserHome',
        component: () => import('@/views/user/Home.vue')
      },
      {
        path: 'rankings',
        name: 'Rankings',
        component: () => import('@/views/user/Rankings.vue')
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/user/Search.vue')
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/user/Cart.vue')
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/user/Orders.vue')
      },
      {
        path: 'order-confirm',
        name: 'OrderConfirm',
        component: () => import('@/views/user/OrderConfirm.vue')
      },
      {
        path: 'coupons',
        name: 'MyCoupons',
        component: () => import('@/views/user/MyCoupons.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue')
      },
      {
        path: 'book/:id',
        name: 'BookDetail',
        component: () => import('@/views/user/BookDetail.vue')
      }
    ]
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'books',
        name: 'BookManagement',
        component: () => import('@/views/admin/BookManagement.vue')
      },
      {
        path: 'coupons',
        name: 'CouponManagement',
        component: () => import('@/views/admin/CouponManagement.vue')
      },
      {
        path: 'carousel',
        name: 'CarouselManagement',
        component: () => import('@/views/admin/CarouselManagement.vue')
      },
      {
        path: 'admins',
        name: 'AdminManagement',
        component: () => import('@/views/admin/AdminManagement.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token

  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
