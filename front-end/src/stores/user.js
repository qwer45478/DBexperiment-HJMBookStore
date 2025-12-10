import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const userType = ref(localStorage.getItem('userType') || '')

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info) => {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  const setUserType = (type) => {
    userType.value = type
    localStorage.setItem('userType', type)
  }

  const logout = () => {
    token.value = ''
    userInfo.value = {}
    userType.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('userType')
  }

  return {
    token,
    userInfo,
    userType,
    setToken,
    setUserInfo,
    setUserType,
    logout
  }
})
