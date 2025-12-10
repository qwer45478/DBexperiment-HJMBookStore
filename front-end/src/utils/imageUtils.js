/**
 * 图片URL处理工具函数
 */

/**
 * 获取完整的图片URL
 * @param {string} imagePath - 图片路径
 * @returns {string} 完整的图片URL
 */
export const getImageUrl = (imagePath) => {
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
