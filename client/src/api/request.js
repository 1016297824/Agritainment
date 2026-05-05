const BASE_URL = import.meta.env.DEV ? '/api/v1' : 'http://localhost:8080/api/v1'

function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data && res.data.code === 0) {
          resolve(res.data.data)
        } else if (res.data && (res.data.code === 40101 || res.statusCode === 401)) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
          setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/index' })
          }, 1500)
          reject(res.data)
        } else if (res.data && res.data.code === 40301) {
          uni.showToast({ title: '权限不足', icon: 'none' })
          reject(res.data)
        } else {
          const msg = (res.data && res.data.message) || '请求失败'
          uni.showToast({ title: msg, icon: 'none' })
          reject(res.data || { code: -1, message: '请求失败' })
        }
      },
      fail: (err) => {
        uni.showToast({ title: '网络错误，请检查网络连接', icon: 'none' })
        reject(err)
      }
    })
  })
}

export function get(url, data) {
  return request({ url, method: 'GET', data })
}

export function post(url, data) {
  return request({ url, method: 'POST', data })
}

export function put(url, data) {
  return request({ url, method: 'PUT', data })
}

export function del(url, data) {
  return request({ url, method: 'DELETE', data })
}

export function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.uploadFile({
      url: BASE_URL + '/upload',
      filePath,
      name: 'file',
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        const data = JSON.parse(res.data)
        if (data.code === 0) {
          resolve(data.data)
        } else {
          uni.showToast({ title: data.message || '上传失败', icon: 'none' })
          reject(data)
        }
      },
      fail: (err) => {
        uni.showToast({ title: '上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default request
