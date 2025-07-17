import axios from '@/plungins/axios'

// 獲取餐廳的營業時間設定
export const fetchStoreHours = async (storeId) => {
    try {
        console.log('fetchStoreHours - 餐廳ID:', storeId)

        const response = await axios.get(`/api/stores/${storeId}/hours`)

        console.log('fetchStoreHours - API 響應狀態:', response.status)
        console.log('fetchStoreHours - API 響應數據:', response.data)

        return response.data
    } catch (error) {
        console.error('獲取營業時間失敗:', error)
        console.error('錯誤詳情:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status
        })
        throw error
    }
}

// 檢查特定日期是否為公休日
export const checkStoreClosedDay = async (storeId, dayOfWeek) => {
    try {
        console.log('checkStoreClosedDay - 餐廳ID:', storeId, '星期:', dayOfWeek)

        const response = await axios.get(`/api/stores/${storeId}/hours/closed/${dayOfWeek}`)

        console.log('checkStoreClosedDay - API 響應狀態:', response.status)
        console.log('checkStoreClosedDay - API 響應數據:', response.data)

        return response.data
    } catch (error) {
        console.error('檢查公休日失敗:', error)
        console.error('錯誤詳情:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status
        })
        throw error
    }
}

// 檢查特定日期和時間是否營業
export const checkStoreOpenStatus = async (storeId, date, time) => {
    try {
        console.log('checkStoreOpenStatus - 餐廳ID:', storeId, '日期:', date, '時間:', time)

        const response = await axios.get(`/api/stores/${storeId}/hours/check/${date}`, {
            params: {
                time
            }
        })

        console.log('checkStoreOpenStatus - API 響應狀態:', response.status)
        console.log('checkStoreOpenStatus - API 響應數據:', response.data)

        return response.data
    } catch (error) {
        console.error('檢查營業狀態失敗:', error)
        console.error('錯誤詳情:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status
        })
        throw error
    }
}

// 獲取餐廳的時間段數據
export const fetchRestaurantTimeSlots = async (restaurantId, date = null) => {
    try {
        // 確保日期參數正確
        const dateParam = date || new Date().toISOString().split('T')[0]
        console.log('fetchRestaurantTimeSlots - 餐廳ID:', restaurantId, '日期:', dateParam)

        const params = {
            date: dateParam
        }

        console.log('發送請求到:', `/api/booking/all-slots/${restaurantId}`, '參數:', params)

        const response = await axios.get(`/api/booking/all-slots/${restaurantId}`, {
            params
        })

        console.log('fetchRestaurantTimeSlots - API 響應狀態:', response.status)
        console.log('fetchRestaurantTimeSlots - API 響應數據:', response.data)

        return response.data
    } catch (error) {
        console.error('獲取餐廳時間段失敗:', error)
        console.error('錯誤詳情:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status
        })
        throw error
    }
}

// 獲取已預訂的時間段
export const fetchBookedTimeSlots = async (restaurantId, date = null) => {
    try {
        // 確保日期參數正確
        const dateParam = date || new Date().toISOString().split('T')[0]
        console.log('fetchBookedTimeSlots - 餐廳ID:', restaurantId, '日期:', dateParam)

        const params = {
            date: dateParam
        }

        console.log('發送請求到:', `/api/booking/booked-slots/${restaurantId}`, '參數:', params)

        const response = await axios.get(`/api/booking/booked-slots/${restaurantId}`, {
            params
        })

        console.log('fetchBookedTimeSlots - API 響應狀態:', response.status)
        console.log('fetchBookedTimeSlots - API 響應數據:', response.data)

        return response.data
    } catch (error) {
        console.error('獲取已預訂時間段失敗:', error)
        console.error('錯誤詳情:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status
        })
        throw error
    }
}

// 檢查特定時間的可用性
export const checkTimeAvailability = async (restaurantId, date, time, guests) => {
    try {
        console.log('checkTimeAvailability - 參數:', {
            restaurantId,
            date,
            time,
            guests
        })

        const response = await axios.get('/api/booking/check', {
            params: {
                storeId: restaurantId,
                date: date,
                time: time,
                guests: guests,
                duration: 120 // 2小時
            }
        })

        console.log('checkTimeAvailability - API 響應:', response.data)
        return response.data
    } catch (error) {
        console.error('檢查時間可用性失敗:', error)
        console.error('錯誤詳情:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status
        })
        throw error
    }
}

// 創建預約
export const createReservation = async (reservationData) => {
    try {
        console.log('createReservation - 預約數據:', reservationData)

        const response = await axios.post('/api/reservations', reservationData)

        console.log('createReservation - API 響應:', response.data)

        // 檢查新的 ApiResponse 格式
        if (response.data && typeof response.data === 'object') {
            if (response.data.success !== undefined) {
                // 新的 ApiResponse 格式
                return response.data
            } else {
                // 舊格式，包裝成新格式
                return {
                    success: true,
                    data: response.data,
                    errorMessage: null
                }
            }
        }

        return response.data
    } catch (error) {
        console.error('創建預約失敗:', error)
        console.error('錯誤詳情:', {
            message: error.message,
            response: error.response?.data,
            status: error.response?.status
        })

        // 返回錯誤格式
        if (error.response?.data && error.response.data.errorMessage) {
            return {
                success: false,
                data: null,
                errorMessage: error.response.data.errorMessage
            }
        }

        throw error
    }
}

// 獲取用戶預約記錄
export const getUserReservations = async (userId) => {
    try {
        const response = await axios.get(`/api/reservations/user/${userId}`)
        return response.data
    } catch (error) {
        console.error('獲取用戶預約記錄失敗:', error)
        throw error
    }
}

// 取消預約
export const cancelReservation = async (reservationId, userId) => {
    try {
        const response = await axios.delete(`/api/reservations/${reservationId}`, {
            params: {
                userId: userId
            }
        })
        return response.data
    } catch (error) {
        console.error('取消預約失敗:', error)
        throw error
    }
}