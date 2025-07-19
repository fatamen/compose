import axios from '../plungins/axios.js';

const API_BASE = '/api/reservations';

class ReservationService {
    /**
     * 取得餐廳的訂位列表
     * @param {number} storeId - 餐廳ID
     * @param {Object} filters - 篩選條件
     * @returns {Promise<Array>}
     */
    async getStoreReservations(storeId, filters = {}) {
        try {
            const params = new URLSearchParams();

            if (filters.status && filters.status !== 'all') {
                params.append('status', filters.status);
            }

            if (filters.date) {
                params.append('date', filters.date);
            }

            if (filters.keyword) {
                params.append('keyword', filters.keyword);
            }

            const response = await axios.get(`${API_BASE}/store/${storeId}?${params}`);
            return response.data;
        } catch (error) {
            console.error('取得訂位列表失敗:', error);
            throw error;
        }
    }

    /**
     * 取得餐廳即將到來的訂位
     * @param {number} storeId - 餐廳ID
     * @returns {Promise<Array>}
     */
    async getStoreUpcomingReservations(storeId) {
        try {
            const response = await axios.get(`${API_BASE}/store/${storeId}/upcoming`);
            return response.data;
        } catch (error) {
            console.error('取得即將到來訂位失敗:', error);
            throw error;
        }
    }

    /**
     * 取得訂位詳情
     * @param {number} reservationId - 訂位ID
     * @returns {Promise<Object>}
     */
    async getReservationDetail(reservationId) {
        try {
            const response = await axios.get(`${API_BASE}/${reservationId}`);
            return response.data;
        } catch (error) {
            console.error('取得訂位詳情失敗:', error);
            throw error;
        }
    }

    /**
     * 更新訂位狀態
     * @param {number} reservationId - 訂位ID
     * @param {string} status - 新狀態
     * @returns {Promise<Object>}
     */
    async updateReservationStatus(reservationId, status) {
        try {
            const response = await axios.put(`${API_BASE}/${reservationId}/status`, {
                status
            });
            return response.data;
        } catch (error) {
            console.error('更新訂位狀態失敗:', error);
            throw error;
        }
    }

    /**
     * 確認訂位
     * @param {number} reservationId - 訂位ID
     * @returns {Promise<Object>}
     */
    async confirmReservation(reservationId) {
        try {
            const response = await axios.put(`${API_BASE}/${reservationId}/confirm`);
            return response.data;
        } catch (error) {
            console.error('確認訂位失敗:', error);
            throw error;
        }
    }

    /**
     * 取消訂位
     * @param {number} reservationId - 訂位ID
     * @returns {Promise<Object>}
     */
    async cancelReservation(reservationId) {
        try {
            const response = await axios.put(`${API_BASE}/${reservationId}/cancel`);
            return response.data;
        } catch (error) {
            console.error('取消訂位失敗:', error);
            throw error;
        }
    }

    /**
     * 完成訂位
     * @param {number} reservationId - 訂位ID
     * @returns {Promise<Object>}
     */
    async completeReservation(reservationId) {
        try {
            const response = await axios.put(`${API_BASE}/${reservationId}/complete`);
            return response.data;
        } catch (error) {
            console.error('完成訂位失敗:', error);
            throw error;
        }
    }

    /**
     * 更新訂位資訊
     * @param {number} reservationId - 訂位ID
     * @param {Object} updateData - 更新資料
     * @returns {Promise<Object>}
     */
    async updateReservation(reservationId, updateData) {
        try {
            const response = await axios.put(`${API_BASE}/${reservationId}`, updateData);
            return response.data;
        } catch (error) {
            console.error('更新訂位失敗:', error);
            throw error;
        }
    }

    /**
     * 刪除訂位
     * @param {number} reservationId - 訂位ID
     * @returns {Promise<void>}
     */
    async deleteReservation(reservationId) {
        try {
            await axios.delete(`${API_BASE}/${reservationId}`);
        } catch (error) {
            console.error('刪除訂位失敗:', error);
            throw error;
        }
    }

    /**
     * 搜尋訂位
     * @param {Object} searchParams - 搜尋參數
     * @returns {Promise<Array>}
     */
    async searchReservations(searchParams) {
        try {
            const params = new URLSearchParams(searchParams);
            const response = await axios.get(`${API_BASE}/search?${params}`);
            return response.data;
        } catch (error) {
            console.error('搜尋訂位失敗:', error);
            throw error;
        }
    }

    /**
     * 取得訂位統計
     * @param {number} storeId - 餐廳ID
     * @returns {Promise<Object>}
     */
    async getReservationStats(storeId) {
        try {
            const response = await axios.get(`${API_BASE}/store/${storeId}/stats`);
            return response.data;
        } catch (error) {
            console.error('取得訂位統計失敗:', error);
            throw error;
        }
    }

    /**
     * 匯出訂位資料
     * @param {number} storeId - 餐廳ID
     * @param {Object} filters - 篩選條件
     * @returns {Promise<Blob>}
     */
    async exportReservations(storeId, filters = {}) {
        try {
            const params = new URLSearchParams();

            if (filters.status && filters.status !== 'all') {
                params.append('status', filters.status);
            }

            if (filters.date) {
                params.append('date', filters.date);
            }

            if (filters.keyword) {
                params.append('keyword', filters.keyword);
            }

            const response = await axios.get(`${API_BASE}/store/${storeId}/export`, {
                params,
                responseType: 'blob'
            });

            return response.data;
        } catch (error) {
            console.error('匯出訂位資料失敗:', error);
            throw error;
        }
    }

    /**
     * 取得時段管理相關API
     */
    getTimeSlotService() {
        return {
            /**
             * 取得餐廳的所有可用時段
             * @param {number} storeId - 餐廳ID
             * @param {string} date - 日期 (可選)
             * @returns {Promise<Array>}
             */
            async getTimeSlots(storeId, date = null) {
                try {
                    let url = `/api/stores/${storeId}/timeslots`;
                    if (date) {
                        url += `/date/${date}`;
                    }
                    const response = await axios.get(url);
                    return response.data;
                } catch (error) {
                    console.error('取得時段失敗:', error);
                    throw error;
                }
            },

            /**
             * 新增時段
             * @param {number} storeId - 餐廳ID
             * @param {string} day - 日期
             * @param {string} startTime - 開始時間
             * @param {string} endTime - 結束時間
             * @param {boolean} isActive - 是否啟用
             * @returns {Promise<Object>}
             */
            async addTimeSlot(storeId, day, startTime, endTime, isActive = true) {
                try {
                    const params = new URLSearchParams({
                        day,
                        startTime,
                        endTime,
                        isActive: isActive.toString()
                    });

                    const response = await axios.post(`/api/stores/${storeId}/timeslots?${params}`);
                    return response.data;
                } catch (error) {
                    console.error('新增時段失敗:', error);
                    throw error;
                }
            },

            /**
             * 更新時段
             * @param {number} storeId - 餐廳ID
             * @param {number} timeSlotId - 時段ID
             * @param {Object} updates - 更新內容
             * @returns {Promise<Object>}
             */
            async updateTimeSlot(storeId, timeSlotId, updates) {
                try {
                    const params = new URLSearchParams();
                    if (updates.startTime) params.append('startTime', updates.startTime);
                    if (updates.endTime) params.append('endTime', updates.endTime);
                    if (updates.isActive !== undefined) params.append('isActive', updates.isActive.toString());

                    const response = await axios.put(`/api/stores/${storeId}/timeslots/${timeSlotId}?${params}`);
                    return response.data;
                } catch (error) {
                    console.error('更新時段失敗:', error);
                    throw error;
                }
            },

            /**
             * 刪除時段
             * @param {number} storeId - 餐廳ID
             * @param {number} timeSlotId - 時段ID
             * @returns {Promise<void>}
             */
            async deleteTimeSlot(storeId, timeSlotId) {
                try {
                    await axios.delete(`/api/stores/${storeId}/timeslots/${timeSlotId}`);
                } catch (error) {
                    console.error('刪除時段失敗:', error);
                    throw error;
                }
            },

            /**
             * 生成時段資料
             * @param {number} storeId - 餐廳ID
             * @param {number} daysAhead - 未來天數
             * @returns {Promise<string>}
             */
            async generateTimeSlots(storeId, daysAhead = 30) {
                try {
                    const response = await axios.post(`/api/stores/${storeId}/timeslots/generate?daysAhead=${daysAhead}`);
                    return response.data;
                } catch (error) {
                    console.error('生成時段失敗:', error);
                    throw error;
                }
            }
        };
    }

    /**
     * 取得時段設定服務
     * @returns {Object} 時段設定服務
     */
    getTimeSettingService() {
        return {
            /**
             * 取得餐廳的時段設定
             * @param {number} storeId - 餐廳ID
             * @returns {Promise<Object>}
             */
            async getTimeSetting(storeId) {
                try {
                    const response = await axios.get(`/api/time-setting/${storeId}`);
                    return response.data;
                } catch (error) {
                    console.error('取得時段設定失敗:', error);
                    throw error;
                }
            },

            /**
             * 更新時段設定
             * @param {number} storeId - 餐廳ID
             * @param {number} interval - 時段間隔（分鐘）
             * @param {number} mealTime - 用餐時間（分鐘）
             * @returns {Promise<Object>}
             */
            async updateTimeSetting(storeId, interval, mealTime) {
                try {
                    const response = await axios.put(`/api/time-setting/${storeId}`, {
                        interval,
                        mealTime
                    });
                    return response.data;
                } catch (error) {
                    console.error('更新時段設定失敗:', error);
                    throw error;
                }
            }
        };
    }
}

// 建立並匯出服務實例
const reservationService = new ReservationService();
export default reservationService;