import { defineStore } from 'pinia'
import { diningApi } from '@/api'

export const useDiningStore = defineStore('dining', {
  state: () => ({
    currentReservation: null,
    dishes: [],
    cart: {},
    tables: [],
    dateList: [],
    selectedDateIdx: 0,
    timeSlot: 'lunch'
  }),
  getters: {
    selectedDate: (state) => state.dateList[state.selectedDateIdx]?.value || '',
    cartCount: (state) => Object.values(state.cart).reduce((a, b) => a + b, 0),
    cartTotal: (state) => state.dishes.reduce((sum, d) => sum + (state.cart[d.id] || 0) * d.price, 0),
    hasValidReservation: (state) => {
      if (!state.currentReservation) return false
      if (state.currentReservation.status !== 'pending') return false
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const reservationDate = new Date(state.currentReservation.reservationDate)
      reservationDate.setHours(0, 0, 0, 0)
      return reservationDate >= today
    }
  },
  actions: {
    initDates() {
      const days = ['日', '一', '二', '三', '四', '五', '六']
      this.dateList = []
      for (let i = 0; i < 3; i++) {
        const d = new Date()
        d.setDate(d.getDate() + i)
        const yyyy = d.getFullYear()
        const mm = String(d.getMonth() + 1).padStart(2, '0')
        const dd = String(d.getDate()).padStart(2, '0')
        this.dateList.push({
          value: `${yyyy}-${mm}-${dd}`,
          day: `${d.getMonth() + 1}/${d.getDate()}`,
          label: i === 0 ? '今天' : i === 1 ? '明天' : '后天'
        })
      }
    },
    selectDate(idx) {
      this.selectedDateIdx = idx
    },
    async fetchTables() {
      try {
        this.tables = await diningApi.getTables(this.selectedDate, this.timeSlot)
      } catch (e) {
        console.error('fetchTables error:', e)
      }
    },
    async fetchDishes() {
      try {
        this.dishes = await diningApi.getDishes()
      } catch (e) {
        console.error('fetchDishes error:', e)
      }
    },
    async fetchReservation() {
      try {
        const reservations = await diningApi.getReservations('pending')
        const today = new Date()
        today.setHours(0, 0, 0, 0)
        this.currentReservation = reservations.find(r => {
          const d = new Date(r.reservationDate)
          d.setHours(0, 0, 0, 0)
          return d >= today
        }) || null
      } catch (e) {
        console.error('fetchReservation error:', e)
      }
    },
    async createReservation(tableId) {
      const reservation = await diningApi.createReservation(tableId, this.selectedDate, this.timeSlot)
      this.currentReservation = reservation
      return reservation
    },
    addToCart(dish) {
      if (dish.remainingStock !== null && dish.remainingStock !== undefined && dish.remainingStock <= 0) return
      this.cart[dish.id] = (this.cart[dish.id] || 0) + 1
    },
    removeFromCart(dish) {
      if (this.cart[dish.id] && this.cart[dish.id] > 0) {
        this.cart[dish.id]--
        if (this.cart[dish.id] === 0) {
          delete this.cart[dish.id]
        }
      }
    },
    clearCart() {
      this.cart = {}
    },
    async submitOrder() {
      if (!this.currentReservation) throw new Error('请先完成预约')
      const items = Object.entries(this.cart).map(([dishId, quantity]) => ({
        dish_id: Number(dishId),
        quantity
      }))
      if (items.length === 0) throw new Error('购物车为空')
      const order = await diningApi.createOrder({
        reservation_id: this.currentReservation.id,
        items
      })
      this.clearCart()
      return order
    },
    clearReservation() {
      this.currentReservation = null
    }
  }
})