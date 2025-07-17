/**
 * 時間段處理工具函數
 */

/**
 * 將 LocalTime 對象轉換為 HH:mm 格式字符串
 * @param {Object|string} time - LocalTime 對象或時間字符串
 * @returns {string} HH:mm 格式的時間字符串
 */
export function formatTimeToString(time) {
    if (!time) return ''

    // 如果已經是字符串格式，處理 ISO 格式 (HH:mm:ss)
    if (typeof time === 'string') {
        // 如果是 ISO 格式 (14:30:00)，只取前5個字符 (14:30)
        if (time.includes(':') && time.length >= 5) {
            return time.substring(0, 5)
        }
        return time
    }

    // 如果是 LocalTime 對象（包含 hour, minute 屬性）
    if (time && typeof time === 'object') {
        try {
            const hour = String(time.hour || 0).padStart(2, '0')
            const minute = String(time.minute || 0).padStart(2, '0')
            return `${hour}:${minute}`
        } catch (error) {
            console.error('格式化時間時發生錯誤:', error, time)
            return ''
        }
    }

    console.error('無效的時間格式:', time)
    return ''
}

/**
 * 獲取可用日期（在當前月份內有時間段的日期）
 * @param {Array} timeSlots - 時間段數據數組
 * @returns {Set} 可用日期的 Set
 */
export function getAvailableDates(timeSlots) {
    if (timeSlots.length === 0) return new Set()

    const availableDateStrings = new Set()
    const today = new Date()

    // 只檢查當前月份內的日期
    timeSlots.forEach(slot => {
        const slotDate = slot.day || slot.date
        if (slotDate) {
            try {
                const slotDateObj = new Date(slotDate)
                // 只包含今天及以後的日期
                if (slotDateObj >= today) {
                    availableDateStrings.add(slotDate)
                }
            } catch (error) {
                console.error('處理日期時發生錯誤:', slotDate, error)
            }
        }
    })

    return availableDateStrings
}

/**
 * 獲取店休日（在當前月份內沒有時間段的日期）
 * @param {Array} timeSlots - 時間段數據數組
 * @returns {Array} 店休日數組
 */
export function getHolidayDates(timeSlots) {
    if (timeSlots.length === 0) return []

    const availableDateStrings = new Set(timeSlots.map(slot => slot.day || slot.date))
    const holidayDates = []
    const today = new Date()

    // 獲取當前月份的第一天和最後一天
    const currentYear = today.getFullYear()
    const currentMonth = today.getMonth()
    const firstDayOfMonth = new Date(currentYear, currentMonth, 1)
    const lastDayOfMonth = new Date(currentYear, currentMonth + 1, 0)

    // 只檢查當前月份內的日期
    for (let checkDate = new Date(Math.max(today, firstDayOfMonth)); checkDate <= lastDayOfMonth; checkDate.setDate(checkDate.getDate() + 1)) {

        // 修正時區問題：使用本地時間而不是 UTC 時間
        const year = checkDate.getFullYear()
        const month = String(checkDate.getMonth() + 1).padStart(2, '0')
        const day = String(checkDate.getDate()).padStart(2, '0')
        const dateString = `${year}-${month}-${day}`

        // 只有沒有時間段的日期才是真正的店休日
        if (!availableDateStrings.has(dateString)) {
            holidayDates.push(new Date(checkDate))
        }
    }

    return holidayDates
}

/**
 * 獲取不可選擇的日期（用於 DatePicker 的 disabledDates）
 * @param {Array} timeSlots - 時間段數據數組
 * @returns {Array} 不可選擇的日期數組
 */
export function getDisabledDates(timeSlots) {
    // 現在只返回店休日，因為 minDate 和 maxDate 已經處理了月份限制
    return getHolidayDates(timeSlots)
}

/**
 * 獲取指定日期的時間段
 * @param {Array} timeSlots - 時間段數據數組
 * @param {Date|string} selectedDate - 選中的日期（Date 對象或 YYYY-MM-DD 字符串）
 * @returns {Array} 當天的時間段數組
 */
export function getTimeSlotsForDate(timeSlots, selectedDate) {
    if (!selectedDate) return []

    let selectedDateStr;

    // 檢查 selectedDate 是 Date 對象還是字符串
    if (typeof selectedDate === 'string') {
        selectedDateStr = selectedDate;
    } else if (selectedDate instanceof Date) {
        // 修正時區問題：使用本地時間而不是 UTC 時間
        const year = selectedDate.getFullYear()
        const month = String(selectedDate.getMonth() + 1).padStart(2, '0')
        const day = String(selectedDate.getDate()).padStart(2, '0')
        selectedDateStr = `${year}-${month}-${day}`
    } else {
        console.error('selectedDate 必須是 Date 對象或日期字符串:', selectedDate)
        return []
    }

    // 處理後端返回的數據格式
    return timeSlots.filter(slot => {
        const slotDate = slot.day || slot.date
        return slotDate === selectedDateStr && slot.isActive !== false
    })
}

/**
 * 將時間段按時間分組
 * @param {Array} timeSlots - 當天的時間段數組
 * @returns {Array} 分組後的時間段數組
 */
export function groupTimeSlotsByPeriod(timeSlots) {
    if (timeSlots.length === 0) return []

    const sections = []
    // 使用 formatTimeToString 轉換時間格式
    const slots = timeSlots.map(slot => formatTimeToString(slot.startTime)).sort()

    // 按時間段分組
    const lunchSlots = slots.filter(time => {
        const hour = parseInt(time.split(':')[0])
        return hour >= 11 && hour < 15
    })

    const afternoonSlots = slots.filter(time => {
        const hour = parseInt(time.split(':')[0])
        return hour >= 15 && hour < 17
    })

    const dinnerSlots = slots.filter(time => {
        const hour = parseInt(time.split(':')[0])
        return hour >= 17 && hour < 22
    })

    if (lunchSlots.length > 0) {
        sections.push({
            label: '午餐時段',
            slots: lunchSlots
        })
    }

    if (afternoonSlots.length > 0) {
        sections.push({
            label: '下午時段',
            slots: afternoonSlots
        })
    }

    if (dinnerSlots.length > 0) {
        sections.push({
            label: '晚餐時段',
            slots: dinnerSlots
        })
    }

    return sections
}

/**
 * 檢查時間段是否已預訂
 * @param {Array} bookedSlots - 已預訂的時間段數組
 * @param {string} timeSlot - 要檢查的時間段
 * @param {Date|string} selectedDate - 選中的日期（Date 對象或 YYYY-MM-DD 字符串）
 * @returns {boolean} 是否已預訂
 */
export function isTimeSlotBooked(bookedSlots, timeSlot, selectedDate) {
    if (!bookedSlots || bookedSlots.length === 0) return false

    let selectedDateStr;

    // 檢查 selectedDate 是 Date 對象還是字符串
    if (typeof selectedDate === 'string') {
        selectedDateStr = selectedDate;
    } else if (selectedDate instanceof Date) {
        // 修正時區問題：使用本地時間而不是 UTC 時間
        const year = selectedDate.getFullYear()
        const month = String(selectedDate.getMonth() + 1).padStart(2, '0')
        const day = String(selectedDate.getDate()).padStart(2, '0')
        selectedDateStr = `${year}-${month}-${day}`
    } else {
        console.error('selectedDate 必須是 Date 對象或日期字符串:', selectedDate)
        return false
    }

    return bookedSlots.some(booking =>
        (booking.date === selectedDateStr || booking.day === selectedDateStr) &&
        formatTimeToString(booking.startTime) === timeSlot
    )
}

/**
 * 格式化日期為 YYYY-MM-DD 格式
 * @param {Date|string} date - 日期對象或日期字符串
 * @returns {string} 格式化後的日期字符串
 */
export function formatDateToString(date) {
    if (!date) return ''

    // 如果已經是字符串格式，直接返回（假設格式正確）
    if (typeof date === 'string') {
        return date;
    }

    // 如果是 Date 對象，進行格式化
    if (date instanceof Date) {
        try {
            // 修正時區問題：使用本地時間而不是 UTC 時間
            const year = date.getFullYear()
            const month = String(date.getMonth() + 1).padStart(2, '0')
            const day = String(date.getDate()).padStart(2, '0')
            return `${year}-${month}-${day}`
        } catch (error) {
            console.error('格式化日期時發生錯誤:', error, date)
            return ''
        }
    }

    console.error('無效的日期格式:', date)
    return ''
}

/**
 * 獲取時間段的顯示標籤
 * @param {string} startTime - 開始時間
 * @param {string} endTime - 結束時間
 * @returns {string} 顯示標籤
 */
export function getTimeSlotLabel(startTime, endTime) {
    return `${startTime} - ${endTime}`
}