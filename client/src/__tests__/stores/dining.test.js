import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { mockUni, clearUniCalls } from '../helpers/uni-mock'

const mockGetTables = vi.fn()
const mockGetDishes = vi.fn()
const mockGetReservations = vi.fn()
const mockCreateReservation = vi.fn()
const mockCreateOrder = vi.fn()

vi.mock('@/api', () => ({
  diningApi: {
    getTables: (...args) => mockGetTables(...args),
    getDishes: (...args) => mockGetDishes(...args),
    getReservations: (...args) => mockGetReservations(...args),
    createReservation: (...args) => mockCreateReservation(...args),
    createOrder: (...args) => mockCreateOrder(...args)
  }
}))

describe('dining store - initDates', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should generate 3 dates starting from today', () => {
    const store = useDiningStore()
    store.initDates()
    expect(store.dateList).toHaveLength(3)
    expect(store.dateList[0].label).toBe('今天')
    expect(store.dateList[1].label).toBe('明天')
    expect(store.dateList[2].label).toBe('后天')
  })

  it('should set first date index to 0', () => {
    const store = useDiningStore()
    store.initDates()
    expect(store.selectedDateIdx).toBe(0)
  })
})

describe('dining store - selectDate', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should update selectedDateIdx', () => {
    const store = useDiningStore()
    store.initDates()
    store.selectDate(1)
    expect(store.selectedDateIdx).toBe(1)
  })
})

describe('dining store - hasValidReservation', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should return false when currentReservation is null', () => {
    const store = useDiningStore()
    store.currentReservation = null
    expect(store.hasValidReservation).toBe(false)
  })

  it('should return false when status is not pending', () => {
    const store = useDiningStore()
    store.currentReservation = { status: 'cancelled', reservationDate: new Date().toISOString() }
    expect(store.hasValidReservation).toBe(false)
  })

  it('should return false when reservation date is in the past', () => {
    const store = useDiningStore()
    const pastDate = new Date()
    pastDate.setDate(pastDate.getDate() - 1)
    store.currentReservation = {
      status: 'pending',
      reservationDate: pastDate.toISOString().split('T')[0]
    }
    expect(store.hasValidReservation).toBe(false)
  })

  it('should return true when status is pending and date is today', () => {
    const store = useDiningStore()
    store.currentReservation = {
      status: 'pending',
      reservationDate: new Date().toISOString().split('T')[0]
    }
    expect(store.hasValidReservation).toBe(true)
  })

  it('should return true when status is pending and date is in the future', () => {
    const store = useDiningStore()
    const futureDate = new Date()
    futureDate.setDate(futureDate.getDate() + 2)
    store.currentReservation = {
      status: 'pending',
      reservationDate: futureDate.toISOString().split('T')[0]
    }
    expect(store.hasValidReservation).toBe(true)
  })
})

describe('dining store - addToCart', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should add dish to cart with quantity 1', () => {
    const store = useDiningStore()
    store.addToCart({ id: 1, name: '宫保鸡丁', price: 28, remainingStock: 10 })
    expect(store.cart[1]).toBe(1)
  })

  it('should increment quantity when dish already in cart', () => {
    const store = useDiningStore()
    store.cart[1] = 2
    store.addToCart({ id: 1, name: '宫保鸡丁', price: 28, remainingStock: 10 })
    expect(store.cart[1]).toBe(3)
  })

  it('should not add dish when remainingStock is 0', () => {
    const store = useDiningStore()
    store.addToCart({ id: 2, name: '售罄菜', price: 18, remainingStock: 0 })
    expect(store.cart[2]).toBeUndefined()
  })
})

describe('dining store - removeFromCart', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should decrement quantity', () => {
    const store = useDiningStore()
    store.cart[1] = 3
    store.removeFromCart({ id: 1 })
    expect(store.cart[1]).toBe(2)
  })

  it('should remove dish from cart when quantity reaches 0', () => {
    const store = useDiningStore()
    store.cart[1] = 1
    store.removeFromCart({ id: 1 })
    expect(store.cart[1]).toBeUndefined()
  })

  it('should do nothing when dish not in cart', () => {
    const store = useDiningStore()
    store.removeFromCart({ id: 99 })
    expect(store.cart[99]).toBeUndefined()
  })
})

describe('dining store - getters', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('cartCount should sum all quantities', () => {
    const store = useDiningStore()
    store.cart = { 1: 2, 2: 3 }
    expect(store.cartCount).toBe(5)
  })

  it('cartCount should be 0 when cart is empty', () => {
    const store = useDiningStore()
    expect(store.cartCount).toBe(0)
  })

  it('cartTotal should calculate total price', () => {
    const store = useDiningStore()
    store.cart = { 1: 2, 2: 3 }
    store.dishes = [
      { id: 1, price: 10 },
      { id: 2, price: 20 }
    ]
    expect(store.cartTotal).toBe(80)
  })

  it('cartTotal should be 0 when cart is empty', () => {
    const store = useDiningStore()
    expect(store.cartTotal).toBe(0)
  })
})

describe('dining store - clearCart', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should reset cart to empty object', () => {
    const store = useDiningStore()
    store.cart = { 1: 2, 2: 3 }
    store.clearCart()
    expect(store.cart).toEqual({})
  })
})

describe('dining store - submitOrder', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    mockCreateOrder.mockReset()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should throw when no current reservation', async () => {
    const store = useDiningStore()
    store.currentReservation = null
    store.cart = { 1: 1 }
    await expect(store.submitOrder()).rejects.toThrow('请先完成预约')
  })

  it('should throw when cart is empty', async () => {
    const store = useDiningStore()
    store.currentReservation = { id: 1 }
    await expect(store.submitOrder()).rejects.toThrow('购物车为空')
  })

  it('should create order and clear cart on success', async () => {
    const store = useDiningStore()
    store.currentReservation = { id: 10 }
    store.cart = { 1: 2, 3: 1 }
    mockCreateOrder.mockResolvedValue({ id: 100, totalAmount: 50 })
    const order = await store.submitOrder()
    expect(mockCreateOrder).toHaveBeenCalledWith({
      reservation_id: 10,
      items: [
        { dish_id: 1, quantity: 2 },
        { dish_id: 3, quantity: 1 }
      ]
    })
    expect(order.id).toBe(100)
    expect(store.cart).toEqual({})
  })
})

describe('dining store - fetchReservation', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    mockGetReservations.mockReset()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should set currentReservation to pending reservation for today', async () => {
    const today = new Date().toISOString().split('T')[0]
    mockGetReservations.mockResolvedValue([
      { id: 1, status: 'pending', reservationDate: today }
    ])
    const store = useDiningStore()
    await store.fetchReservation()
    expect(store.currentReservation.id).toBe(1)
  })

  it('should filter out past reservations', async () => {
    const pastDate = new Date()
    pastDate.setDate(pastDate.getDate() - 1)
    mockGetReservations.mockResolvedValue([
      { id: 1, status: 'pending', reservationDate: pastDate.toISOString().split('T')[0] }
    ])
    const store = useDiningStore()
    await store.fetchReservation()
    expect(store.currentReservation).toBeNull()
  })

  it('should set null when no pending reservations', async () => {
    mockGetReservations.mockResolvedValue([])
    const store = useDiningStore()
    await store.fetchReservation()
    expect(store.currentReservation).toBeNull()
  })

  it('should handle fetch error gracefully', async () => {
    mockGetReservations.mockRejectedValue(new Error('Network error'))
    const store = useDiningStore()
    store.currentReservation = { id: 5, status: 'pending' }
    await store.fetchReservation()
    expect(store.currentReservation.id).toBe(5)
  })
})

describe('dining store - fetchTables', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    mockGetTables.mockReset()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should set tables on success', async () => {
    mockGetTables.mockResolvedValue([
      { id: 1, tableNumber: 'A1', capacity: 4 }
    ])
    const store = useDiningStore()
    await store.fetchTables()
    expect(store.tables).toHaveLength(1)
    expect(store.tables[0].tableNumber).toBe('A1')
  })

  it('should handle fetch error gracefully', async () => {
    mockGetTables.mockRejectedValue(new Error('Network error'))
    const store = useDiningStore()
    store.tables = [{ id: 99 }]
    await store.fetchTables()
    expect(store.tables).toHaveLength(1)
  })
})

describe('dining store - fetchDishes', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    mockGetDishes.mockReset()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should set dishes on success', async () => {
    mockGetDishes.mockResolvedValue([
      { id: 1, name: '宫保鸡丁', price: 28 }
    ])
    const store = useDiningStore()
    await store.fetchDishes()
    expect(store.dishes).toHaveLength(1)
    expect(store.dishes[0].name).toBe('宫保鸡丁')
  })

  it('should handle fetch error gracefully', async () => {
    mockGetDishes.mockRejectedValue(new Error('Network error'))
    const store = useDiningStore()
    store.dishes = [{ id: 99, name: 'x' }]
    await store.fetchDishes()
    expect(store.dishes).toHaveLength(1)
  })
})

describe('dining store - createReservation', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    mockCreateReservation.mockReset()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should create reservation and update state', async () => {
    const reservation = { id: 20, tableId: 5, reservationDate: '2026-05-11', timeSlot: 'lunch' }
    mockCreateReservation.mockResolvedValue(reservation)
    const store = useDiningStore()
    store.dateList = [{ value: '2026-05-11' }, { value: '2026-05-12' }, { value: '2026-05-13' }]
    store.selectedDateIdx = 0
    store.timeSlot = 'lunch'
    const result = await store.createReservation(5)
    expect(result.id).toBe(20)
    expect(store.currentReservation.id).toBe(20)
    expect(mockCreateReservation).toHaveBeenCalledWith(5, '2026-05-11', 'lunch')
  })

  it('should propagate API errors', async () => {
    mockCreateReservation.mockRejectedValue(new Error('Table already reserved'))
    const store = useDiningStore()
    await expect(store.createReservation(5)).rejects.toThrow('Table already reserved')
  })
})

describe('dining store - submitOrder error path', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    mockCreateOrder.mockReset()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should propagate API error and preserve cart', async () => {
    const store = useDiningStore()
    store.currentReservation = { id: 10 }
    store.cart = { 1: 2 }
    store.dishes = [{ id: 1, price: 10 }]
    mockCreateOrder.mockRejectedValue(new Error('下单失败，请重试'))
    await expect(store.submitOrder()).rejects.toThrow('下单失败，请重试')
    expect(store.cart).toEqual({ 1: 2 })
  })
})

describe('dining store - clearReservation', () => {
  let useDiningStore

  beforeEach(async () => {
    vi.resetModules()
    clearUniCalls()
    mockUni()
    setActivePinia(createPinia())
    const mod = await import('@/stores/dining')
    useDiningStore = mod.useDiningStore
  })

  it('should set currentReservation to null', () => {
    const store = useDiningStore()
    store.currentReservation = { id: 1 }
    store.clearReservation()
    expect(store.currentReservation).toBeNull()
  })
})