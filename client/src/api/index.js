import { get, post, del, put, uploadFile } from './request'

export const authApi = {
  sendSmsCode: (phone) => post('/auth/sms-code', { phone }),
  register: (phone, code) => post('/auth/register', { phone, code }),
  login: (phone, code) => post('/auth/login', { phone, code }),
  adminLogin: (phone, password) => post('/auth/admin-login', { phone, password }),
  getMe: () => get('/auth/me')
}

export const diningApi = {
  getTables: (date, time_slot) => get('/dining/tables', { date, time_slot }),
  createReservation: (table_id, date, time_slot) => post('/dining/reservations', { table_id, date, time_slot }),
  cancelReservation: (id) => del(`/dining/reservations/${id}`),
  getReservations: () => get('/dining/reservations'),
  getActiveOrder: (table_qr) => get('/dining/orders/active', { table_qr }),
  getActiveOrders: () => get('/dining/orders/active'),
  createOrder: (table_qr, items) => post('/dining/orders', { table_qr, items }),
  settleOrder: (id) => post(`/dining/orders/${id}/settle`),
  getDishes: () => get('/dining/dishes'),
  staffCheckin: (id) => post(`/dining/staff/reservations/${id}/checkin`),
  staffCancel: (id) => post(`/dining/staff/reservations/${id}/cancel`),
  changeTable: (orderId, new_table_id) => post(`/dining/orders/${orderId}/change-table`, { new_table_id }),
  refundOrderItem: (id) => post(`/dining/order-items/${id}/refund`),
  getAllTables: () => get('/dining/all-tables'),
  createTable: (data) => post('/dining/tables', data),
  deleteTable: (id) => del(`/dining/tables/${id}`)
}

export const productApi = {
  getProducts: () => get('/products'),
  getProduct: (id) => get(`/products/${id}`),
  purchase: (id) => post(`/products/${id}/purchase`)
}

export const couponApi = {
  getCoupons: () => get('/coupons'),
  getCoupon: (id) => get(`/coupons/${id}`),
  transfer: (id, target_user_id) => post(`/coupons/${id}/transfer`, { target_user_id }),
  verify: (code) => post('/coupons/verify', { code }),
  createServiceReservation: (coupon_id, product_id, date) => post('/coupons/service-reservations', { coupon_id, product_id, date }),
  cancelServiceReservation: (id) => del(`/coupons/service-reservations/${id}`)
}

export const membershipApi = {
  getStatus: () => get('/membership/status'),
  purchase: () => post('/membership/purchase'),
  getConfig: () => get('/membership/config'),
  grant: (user_id) => post('/membership/grant', { user_id })
}

export const plantingApi = {
  getPlots: () => get('/planting/plots'),
  getPlot: (id) => get(`/planting/plots/${id}`),
  rentPlot: (id) => post(`/planting/plots/${id}/rent`),
  getGardenServices: () => get('/planting/garden-services'),
  createServiceOrder: (plot_id, service_id, coupon_id) => post('/planting/service-orders', { plot_id, service_id, coupon_id }),
  completeServiceOrder: (id) => post(`/planting/service-orders/${id}/complete`),
  getCameraStatus: (id) => get(`/planting/cameras/${id}/status`),
  controlCamera: (id, action, speed) => post(`/planting/cameras/${id}/control`, { action, speed }),
  getCameras: () => get('/planting/cameras'),
  createCamera: (data) => post('/planting/cameras', data),
  deleteCamera: (id) => del(`/planting/cameras/${id}`),
  bindCameraPlot: (cameraId, plotId) => post(`/planting/cameras/${cameraId}/bind-plot/${plotId}`)
}

export const journalApi = {
  getJournals: () => get('/journals'),
  getJournal: (id) => get(`/journals/${id}`),
  getSharedJournals: () => get('/journals/shared'),
  createJournal: (title, content, images) => post('/journals', { title, content, images }),
  updateJournal: (id, title, content, images) => put(`/journals/${id}`, { title, content, images }),
  shareJournal: (id) => post(`/journals/${id}/share`),
  unshareJournal: (id) => del(`/journals/${id}/share`)
}

export const adminApi = {
  getDashboard: () => get('/admin/dashboard'),
  getUsers: (role) => get('/admin/users', { role }),
  updateUserStatus: (id, data) => put(`/admin/users/${id}/status`, data),
  createStaff: (data) => post('/admin/staff', data),
  deleteStaff: (id) => del(`/admin/staff/${id}`),
  createDish: (data) => post('/admin/dishes', data),
  updateDish: (id, data) => put(`/admin/dishes/${id}`, data),
  deleteDish: (id) => del(`/admin/dishes/${id}`),
  createProduct: (data) => post('/admin/products', data),
  updateProduct: (id, data) => put(`/admin/products/${id}`, data),
  deleteProduct: (id) => del(`/admin/products/${id}`),
  createPlot: (data) => post('/admin/plots', data),
  deletePlot: (id) => del(`/admin/plots/${id}`),
  getAllTables: diningApi.getAllTables,
  createTable: diningApi.createTable,
  deleteTable: diningApi.deleteTable,
  getCameras: plantingApi.getCameras,
  createCamera: plantingApi.createCamera,
  deleteCamera: plantingApi.deleteCamera,
  bindCameraPlot: plantingApi.bindCameraPlot
}

export const uploadApi = {
  upload: uploadFile
}
