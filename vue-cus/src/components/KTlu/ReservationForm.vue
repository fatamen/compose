<template>
    <div class="reservation-form-container goldenbowl-restaurant-theme">
        <div class="guest-date-section">
            <div class="guest-selection">
                <label class="form-label">用餐人數</label>
                <div class="select-group">
                    <Select v-model="selectedGuest" :options="guests" optionLabel="name" placeholder="請選擇用餐人數" checkmark
                        :highlightOnSelect="false" class="guest-select" />
                    <Select v-model="selectChild" :options="children" optionLabel="name" placeholder="請選擇用餐人數" checkmark
                        :highlightOnSelect="false" class="child-select" />
                </div>

                <div class="date-selection">
                    <label class="form-label">用餐日期</label>
                    <DatePicker v-model="date" placeholder="請選擇" dateFormat="yy/mm/dd" :minDate="minDate"
                        :maxDate="maxDate" :disabledDates="disabledDates" class="date-picker" />
                </div>

                <div class="date-hint">
                    <small class="hint-text">
                        <i class="pi pi-info-circle"></i>
                        僅顯示有時間段的可預約日期・可接受1-6位訂位（含大人與小孩）・超過6人請電話預約
                        <a href="tel:0227845677" class="phone-link">02-2784-5677</a>
                    </small>
                </div>
            </div>
        </div>

        <hr class="divider" />

        <div class="time-section">
            <h5 class="section-title">選擇用餐時間</h5>
            <div v-if="loading" class="loading-state">
                <i class="pi pi-spinner pi-spin"></i> 載入時間段中...
            </div>
            <div v-else-if="!date" class="empty-state">
                請先選擇用餐日期
            </div>
            <div v-else-if="isClosedDay(date)" class="empty-state">
                <i class="pi pi-calendar-times"></i> 當日為公休日，暫不提供預約服務
            </div>
            <div v-else-if="!timeSections || timeSections.length === 0" class="empty-state">
                <i class="pi pi-info-circle"></i> 當日暫無可用時間段
            </div>
            <TimePickerSectioned v-else v-model="selectedTime" :sections="timeSections"
                :disabledSlots="disabledTimeSlots || []" :selectedDate="date" />
        </div>

        <h4 class="section-title">預約訂位</h4>

        <!-- 姓名 -->
        <div class="form-group">
            <label class="form-label">姓名</label>
            <InputText v-model="name" class="form-input" placeholder="請輸入姓名" />
        </div>

        <!-- 電話 -->
        <div class="form-group">
            <label class="form-label">電話</label>
            <InputText v-model="phone" class="form-input" placeholder="請輸入電話" />
        </div>

        <!-- 備註 -->
        <div class="form-group">
            <label class="form-label">備註</label>
            <InputText v-model="note" class="form-input" placeholder="(選填)" />
        </div>

        <Button label="送出預約" icon="pi pi-check" class="submit-btn" @click="submit" />
    </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import Swal from 'sweetalert2'
import InputText from 'primevue/inputtext'
import Button from 'primevue/button'
import DatePicker from 'primevue/datepicker'
import Select from 'primevue/select'
import TimePickerSectioned from './TimePickerSectioned.vue'
import '@/assets/css/restaurant-theme.css'
import {
    getTimeSlotsForDate,
    groupTimeSlotsByPeriod,
    formatDateToString,
    formatTimeToString
} from '@/utils/timeSlotUtils.js'
import {
    fetchRestaurantTimeSlots,
    fetchBookedTimeSlots,
    createReservation,
    checkTimeAvailability,
    fetchStoreHours,
    checkStoreClosedDay
} from '@/services/timeSlotService.js'

// 定義 props 接收餐廳 ID
const props = defineProps({
    restaurantId: {
        type: String,
        required: true
    }
})

const date = ref(new Date())
const name = ref('')
const phone = ref('')
const note = ref('')
const selectedTime = ref('')
const minDate = ref(new Date()) // 今日起始
const maxDate = ref(null) // 時段資料最新日期
const disabledDates = ref([]) // 禁用日期列表

const selectedGuest = ref();
const selectChild = ref();
const guests = ref([
    { name: '1 位大人' },
    { name: '2 位大人' },
    { name: '3 位大人' },
    { name: '4 位大人' },
    { name: '5 位大人' }
]);
const children = ref([
    { name: '0 位小孩' },
    { name: '1 位小孩' },
    { name: '2 位小孩' },
    { name: '3 位小孩' },
    { name: '4 位小孩' }
]);

// 後台數據狀態 - 確保初始值為空數組
const timeSlots = ref([])
const bookedSlots = ref([])
const storeHours = ref([])
const loading = ref(false)



// 分析時段數據中的日期
const analyzeTimeSlotDates = () => {
    if (!timeSlots.value || timeSlots.value.length === 0) {
        console.log('=== 時段數據分析 ===')
        console.log('❌ 無時段數據')
        return
    }

    console.log('=== 時段數據分析 ===')

    // 獲取所有唯一日期
    const uniqueDates = [...new Set(timeSlots.value.map(slot => slot.day || slot.date))]
        .filter(dateStr => dateStr)
        .sort()

    console.log('📅 來自後端的日期:', uniqueDates)
    console.log('📊 總日期數量:', uniqueDates.length)

    // 找出最新日期
    if (uniqueDates.length > 0) {
        const latestDate = uniqueDates[uniqueDates.length - 1]
        console.log('🆕 資料庫最新日期:', latestDate)
    }

    // 分析每個日期的時段數量
    const dateSlotCount = {}
    timeSlots.value.forEach(slot => {
        const dateStr = slot.day || slot.date
        if (dateStr) {
            dateSlotCount[dateStr] = (dateSlotCount[dateStr] || 0) + 1
        }
    })

    console.log('📈 各日期時段數量:', dateSlotCount)
}

// 分析公休日
const analyzeClosedDays = () => {
    if (!storeHours.value || storeHours.value.length === 0) {
        console.log('=== 公休日分析 ===')
        console.log('❌ 無營業時間設定')
        return
    }

    console.log('=== 公休日分析 ===')

    const closedDays = []
    const dayNames = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY']
    const dayNameMap = {
        'SUNDAY': '週日',
        'MONDAY': '週一',
        'TUESDAY': '週二',
        'WEDNESDAY': '週三',
        'THURSDAY': '週四',
        'FRIDAY': '週五',
        'SATURDAY': '週六'
    }

    storeHours.value.forEach(hour => {
        if (hour.openTime === null && hour.closeTime === null) {
            closedDays.push(dayNameMap[hour.dayOfWeek] || hour.dayOfWeek)
        }
    })

    console.log('🚫 公休日:', closedDays)
    console.log('📊 公休日數量:', closedDays.length)
}

// 獲取日曆元數據
const fetchCalendarMetadata = async () => {
    try {
        console.log('正在獲取日曆元數據，餐廳ID:', props.restaurantId)

        // 調用後端API
        const response = await fetch(`/api/booking/calendar-metadata/${props.restaurantId}`)

        if (!response.ok) {
            throw new Error(`API 請求失敗: ${response.status}`)
        }

        const data = await response.json()
        console.log('日曆元數據:', data)

        // 設定最大日期
        if (data.maxDate) {
            maxDate.value = new Date(data.maxDate)
            console.log('📅 最大可選日期:', maxDate.value.toISOString().split('T')[0])
        }

        // 設定禁用日期
        if (data.disabledDates && Array.isArray(data.disabledDates)) {
            disabledDates.value = data.disabledDates.map(dateStr => new Date(dateStr))
            console.log('🚫 禁用日期數量:', disabledDates.value.length)
            console.log('🚫 禁用日期:', data.disabledDates)
        }

    } catch (error) {
        console.error('獲取日曆元數據失敗:', error)
        // 發生錯誤時使用預設值
        maxDate.value = new Date()
        disabledDates.value = []
    }
}

// 顯示今天日期
const showTodayInfo = () => {
    const today = new Date()
    const todayString = today.toISOString().split('T')[0]
    console.log('=== 今天日期 ===')
    console.log('📅 今天:', todayString)
    console.log('📅 今天 (本地格式):', today.toLocaleDateString('zh-TW'))
}

// 檢查是否為公休日
const isClosedDay = (date) => {
    if (!date || !storeHours.value || storeHours.value.length === 0) {
        return false
    }

    try {
        const dayOfWeek = date.getDay() // 0=Sunday, 1=Monday, ..., 6=Saturday
        const dayNames = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY']
        const dayName = dayNames[dayOfWeek]

        // 查找對應的營業時間設定
        const dayHours = storeHours.value.find(hour => hour.dayOfWeek === dayName)

        if (dayHours) {
            // 如果 openTime 和 closeTime 都為 null，表示公休日
            return dayHours.openTime === null && dayHours.closeTime === null
        }

        return false
    } catch (error) {
        console.error('檢查公休日時發生錯誤:', error)
        return false
    }
}

// 從後台抓取營業時間設定
const fetchStoreHoursData = async () => {
    try {
        console.log('正在獲取營業時間設定，餐廳ID:', props.restaurantId)

        const result = await fetchStoreHours(props.restaurantId)
        console.log('營業時間設定:', result)

        if (Array.isArray(result)) {
            storeHours.value = result
            console.log('成功獲取營業時間設定:', storeHours.value.length, '筆')

            // 分析公休日
            analyzeClosedDays()
        } else {
            console.warn('營業時間 API 返回非數組數據:', result)
            storeHours.value = []
        }
    } catch (error) {
        console.error('獲取營業時間設定失敗:', error)
        storeHours.value = []
    }
}

// 從後台抓取時間段數據
const fetchTimeSlots = async (selectedDate = null) => {
    loading.value = true
    try {
        // 使用服務層來獲取數據
        // 如果沒有指定日期，使用今天的日期
        const dateParam = selectedDate ? formatDateToString(selectedDate) : formatDateToString(new Date())
        console.log('正在獲取時間段數據，餐廳ID:', props.restaurantId, '日期:', dateParam)

        const result = await fetchRestaurantTimeSlots(props.restaurantId, dateParam)
        console.log('API 原始響應:', result)

        // 確保 result 是數組
        if (Array.isArray(result)) {
            timeSlots.value = result
            console.log('成功獲取時間段數據:', timeSlots.value.length, '筆')
            console.log('時間段數據樣本:', timeSlots.value.slice(0, 2))

            // 分析時段數據中的日期
            analyzeTimeSlotDates()
        } else {
            console.warn('API 返回非數組數據:', result)
            timeSlots.value = []
        }

        // 獲取已預訂的時間段
        await fetchBookedSlots(selectedDate)
    } catch (error) {
        console.error('抓取時間段失敗:', error)
        timeSlots.value = []

        // 顯示錯誤訊息
        await Swal.fire({
            title: '載入失敗',
            text: '無法載入用餐時間，請重新整理頁面或聯繫客服',
            icon: 'error',
            confirmButtonText: '確定'
        })
    } finally {
        loading.value = false
    }
}

// 從後台抓取已預訂的時間段
const fetchBookedSlots = async (selectedDate = null) => {
    try {
        // 如果沒有指定日期，使用今天的日期
        const dateParam = selectedDate ? formatDateToString(selectedDate) : formatDateToString(new Date())
        console.log('正在獲取已預訂時間段，餐廳ID:', props.restaurantId, '日期:', dateParam)

        const result = await fetchBookedTimeSlots(props.restaurantId, dateParam)
        console.log('已預訂 API 原始響應:', result)

        // 確保 result 是數組
        if (Array.isArray(result)) {
            bookedSlots.value = result
            console.log('成功獲取已預訂時間段:', bookedSlots.value.length, '筆')
            console.log('已預訂時間段樣本:', bookedSlots.value.slice(0, 2))
        } else {
            console.warn('已預訂 API 返回非數組數據:', result)
            bookedSlots.value = []
        }
    } catch (error) {
        console.error('抓取已預訂時間段失敗:', error)
        bookedSlots.value = []
    }
}





const timeSections = computed(() => {
    if (!date.value || !timeSlots.value || timeSlots.value.length === 0) {
        console.log('timeSections: 缺少必要數據', {
            date: date.value,
            timeSlotsLength: timeSlots.value?.length
        })
        return []
    }

    try {
        const dateString = formatDateToString(date.value)
        if (!dateString) {
            console.error('無法格式化日期:', date.value)
            return []
        }

        // 檢查是否為公休日
        if (isClosedDay(date.value)) {
            console.log('當日為公休日，不顯示時段:', dateString)
            return []
        }

        console.log('處理時間段，日期:', dateString, '總時段數:', timeSlots.value.length)

        // 獲取當天的時段
        const daySlots = getTimeSlotsForDate(timeSlots.value, dateString)
        console.log('當天時段數:', daySlots.length, '樣本:', daySlots.slice(0, 2))

        if (!Array.isArray(daySlots)) {
            console.error('getTimeSlotsForDate 返回了非數組值:', daySlots)
            return []
        }

        const sections = groupTimeSlotsByPeriod(daySlots)
        console.log('分組後時段:', sections)

        return Array.isArray(sections) ? sections : []
    } catch (error) {
        console.error('處理時間段時發生錯誤:', error)
        return []
    }
})

const disabledTimeSlots = computed(() => {
    if (!date.value || !bookedSlots.value) {
        console.log('disabledTimeSlots: 日期或已預訂數據為空:', {
            date: date.value,
            bookedSlotsLength: bookedSlots.value?.length
        })
        return []
    }

    try {
        const dateString = formatDateToString(date.value)
        console.log('處理已預訂時段，日期:', dateString)

        const filtered = bookedSlots.value
            .filter(slot => {
                if (!slot) {
                    console.log('跳過無效的已預訂時段:', slot)
                    return false
                }
                const slotDate = slot.date || slot.day
                console.log('比較日期:', slotDate, 'vs', dateString)
                return slotDate === dateString
            })
            .map(slot => {
                const formattedTime = formatTimeToString(slot.startTime)
                console.log('格式化時間:', slot.startTime, '->', formattedTime)
                return formattedTime
            })
            .filter(time => time)

        console.log('最終禁用時間段:', filtered)
        return filtered
    } catch (error) {
        console.error('處理已預訂時間段時發生錯誤:', error)
        return []
    }
})

const submit = async () => {
    if (!name.value || !phone.value || !selectedTime.value || !date.value) {
        await Swal.fire({
            title: '請填寫完整資訊',
            text: '請確認姓名、電話、日期和時間都已填寫',
            icon: 'warning',
            confirmButtonText: '確定'
        })
        return
    }

    // 計算總人數
    const adultCount = selectedGuest.value ? parseInt(selectedGuest.value.name.split(' ')[0]) : 0
    const childCount = selectChild.value ? parseInt(selectChild.value.name.split(' ')[0]) : 0
    const totalGuests = adultCount + childCount

    if (totalGuests === 0) {
        await Swal.fire({
            title: '請選擇用餐人數',
            text: '請至少選擇一位用餐人數',
            icon: 'warning',
            confirmButtonText: '確定'
        })
        return
    }

    try {
        console.log('開始檢查時間可用性...')
        // 檢查時間可用性
        const availabilityCheck = await checkTimeAvailability(
            props.restaurantId,
            formatDateToString(date.value),
            selectedTime.value,
            totalGuests
        )

        console.log('可用性檢查結果:', availabilityCheck)

        if (!availabilityCheck.available) {
            await Swal.fire({
                title: '時間不可用',
                text: availabilityCheck.reason || '選擇的時間已被預訂，請選擇其他時間',
                icon: 'warning',
                confirmButtonText: '確定'
            })
            return
        }

        // 準備預約數據
        const reservationData = {
            userId: 1, // 暫時使用固定用戶ID，實際應該從登入狀態獲取
            storeId: parseInt(props.restaurantId),
            reservedDate: formatDateToString(date.value),
            reservedTime: selectedTime.value,
            guests: totalGuests,
            duration: 120, // 2小時
            content: note.value || `預約人: ${name.value}, 電話: ${phone.value}, 大人: ${adultCount}位, 小孩: ${childCount}位`
        }

        console.log('準備提交預約數據:', reservationData)

        // 提交預約
        const result = await createReservation(reservationData)

        console.log('預約結果:', result)

        if (result.success) {
            await Swal.fire({
                title: '預約成功！',
                text: `${name.value} 您好，已為您預約 ${formatDateToString(date.value)} ${selectedTime.value} 的位子`,
                icon: 'success',
                confirmButtonText: '確定'
            })

            // 重置表單
            name.value = ''
            phone.value = ''
            note.value = ''
            selectedTime.value = ''
            date.value = null
            selectedGuest.value = null
            selectChild.value = null

            // 重新載入時間段數據
            await fetchTimeSlots()
        } else {
            throw new Error(result.errorMessage || '預約失敗')
        }
    } catch (error) {
        console.error('預約失敗:', error)
        await Swal.fire({
            title: '預約失敗',
            text: error.message || '請稍後再試或聯繫客服',
            icon: 'error',
            confirmButtonText: '確定'
        })
    }
}

onMounted(async () => {
    console.log('組件載入，開始初始化...')

    // 顯示今天日期
    showTodayInfo()

    // 獲取日曆元數據
    await fetchCalendarMetadata()

    // 先獲取營業時間資料
    await fetchStoreHoursData()

    // 再獲取時段資料
    await fetchTimeSlots(new Date())
})

watch(() => props.restaurantId, async () => {
    console.log('餐廳ID變化，重新初始化...')

    // 先獲取營業時間資料
    await fetchStoreHoursData()

    // 再獲取時段資料
    await fetchTimeSlots(new Date())
})



// 監聽日期變化
watch(date, (newDate) => {
    if (newDate) {
        fetchTimeSlots(newDate)
    }
})
</script>

<style scoped>
.reservation-form-container {
    width: 100%;
    margin: 0 0;
    padding: 0;
}

.guest-date-section {
    margin-bottom: 2rem;
}

.guest-selection {
    background: var(--restaurant-bg-light);
    border: 1px solid var(--restaurant-border-light);
    padding: 1.5rem;
    border-radius: 12px;
    margin-bottom: 1rem;
    box-shadow: 0 2px 8px var(--restaurant-shadow-light);
}

.form-label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 600;
    color: var(--restaurant-text-primary);
}

.select-group {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
}

.guest-select,
.child-select {
    flex: 1;
}

.date-selection {
    margin-bottom: 1rem;
}

.date-picker {
    width: 100%;
}

.date-hint {
    margin-top: 0.5rem;
}

.hint-text {
    color: var(--restaurant-text-secondary);
    font-size: 0.9rem;
    line-height: 1.4;
    padding: 0.75rem;
    background: var(--restaurant-bg-secondary);
    border-radius: 6px;
    border: 1px solid var(--restaurant-border-light);
}

.phone-link {
    color: var(--restaurant-primary);
    text-decoration: none;
    font-weight: 500;
}

.phone-link:hover {
    text-decoration: underline;
    color: var(--restaurant-primary-hover);
}

.divider {
    border: none;
    border-top: 2px solid var(--restaurant-border-light);
    margin: 2rem 0;
}

.time-section {
    margin-bottom: 2rem;
}

.section-title {
    color: var(--restaurant-primary);
    margin-bottom: 1rem;
    font-size: 1.25rem;
    font-weight: 600;
    text-shadow: 0 1px 2px var(--restaurant-shadow-light);
}

.loading-state,
.empty-state {
    text-align: center;
    padding: 1.5rem;
    color: var(--restaurant-text-secondary);
    background: var(--restaurant-bg-light);
    border-radius: 8px;
    border: 1px solid var(--restaurant-border-light);
}

.form-group {
    margin-bottom: 1.5rem;
}

.form-input {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid var(--restaurant-border-medium);
    background: var(--restaurant-bg-primary);
    border-radius: 8px;
    font-size: 1rem;
    color: var(--restaurant-text-primary);
    transition: all 0.2s ease;
}

.form-input:focus {
    outline: none;
    border-color: var(--restaurant-primary);
    box-shadow: 0 0 0 2px var(--restaurant-shadow-light);
}

.submit-btn {
    background: var(--restaurant-gradient-primary) !important;
    border: 1px solid var(--restaurant-primary-light) !important;
    padding: 0.75rem 2rem;
    font-size: 1rem;
    font-weight: 600;
    border-radius: 8px;
    width: 100%;
    color: var(--restaurant-text-primary) !important;
    box-shadow: 0 2px 8px var(--restaurant-shadow-light);
    transition: all 0.3s ease !important;
}

.submit-btn:hover {
    background: var(--restaurant-primary-hover) !important;
    border-color: var(--restaurant-primary) !important;
    transform: translateY(-2px);
    box-shadow: 0 4px 16px var(--restaurant-shadow-medium) !important;
}

/* 響應式設計 */
@media (max-width: 768px) {
    .reservation-form-container {
        width: 100%;
        padding: 15px;
    }

    .select-group {
        flex-direction: column;
    }
}

@media (max-width: 480px) {
    .reservation-form-container {
        width: 100%;
    }
}

/* 預約表單專用的日期選擇器樣式 */
.reservation-form-container .p-datepicker .p-datepicker-day:not(.p-datepicker-day-disabled):not(.p-datepicker-day-outside-month) {
    position: relative !important;
    border: 2px solid var(--restaurant-primary) !important;
    background-color: var(--restaurant-primary-light) !important;
    color: var(--restaurant-text-primary) !important;
    font-weight: 600 !important;
    border-radius: 8px !important;
    transition: all 0.3s ease !important;
    margin: 2px !important;
    min-width: 36px !important;
    height: 36px !important;
    display: flex !important;
    align-items: center !important;
    justify-content: center !important;
}

.reservation-form-container .p-datepicker .p-datepicker-day:not(.p-datepicker-day-disabled):not(.p-datepicker-day-outside-month):hover {
    background-color: var(--restaurant-primary) !important;
    color: white !important;
    transform: scale(1.05) !important;
    box-shadow: 0 4px 12px var(--restaurant-shadow-medium) !important;
    border-color: var(--restaurant-primary-dark) !important;
}

/* 選中的日期樣式 */
.reservation-form-container .p-datepicker .p-datepicker-day.p-datepicker-day-selected {
    background-color: var(--restaurant-primary) !important;
    color: white !important;
    border: 2px solid var(--restaurant-primary-dark) !important;
    box-shadow: 0 4px 12px var(--restaurant-shadow-medium) !important;
    font-weight: bold !important;
}

/* 今天的日期樣式 */
.reservation-form-container .p-datepicker .p-datepicker-day.p-datepicker-day-today:not(.p-datepicker-day-disabled) {
    border: 2px solid var(--restaurant-primary-dark) !important;
    background-color: var(--restaurant-primary-light) !important;
    color: var(--restaurant-text-primary) !important;
    font-weight: bold !important;
    position: relative !important;
}

.reservation-form-container .p-datepicker .p-datepicker-day.p-datepicker-day-today:not(.p-datepicker-day-disabled)::after {
    content: '今天' !important;
    position: absolute !important;
    top: -8px !important;
    right: -8px !important;
    background: var(--restaurant-primary-dark) !important;
    color: white !important;
    font-size: 8px !important;
    padding: 2px 4px !important;
    border-radius: 4px !important;
    font-weight: bold !important;
}

/* 禁用日期的樣式 */
.reservation-form-container .p-datepicker .p-datepicker-day.p-datepicker-day-disabled {
    background-color: var(--restaurant-bg-secondary) !important;
    color: var(--restaurant-text-light) !important;
    border: 1px solid var(--restaurant-border-light) !important;
    cursor: not-allowed !important;
    opacity: 0.5 !important;
}

/* 月份外日期的樣式 */
.reservation-form-container .p-datepicker .p-datepicker-day.p-datepicker-day-outside-month {
    background-color: transparent !important;
    color: var(--restaurant-text-light) !important;
    border: none !important;
    opacity: 0.3 !important;
}

/* 日期選擇器整體樣式 */
.reservation-form-container .p-datepicker {
    border: 2px solid var(--restaurant-border-light) !important;
    border-radius: 12px !important;
    box-shadow: 0 8px 24px var(--restaurant-shadow-light) !important;
    background: white !important;
}

/* 日期選擇器標題樣式 */
.reservation-form-container .p-datepicker .p-datepicker-header {
    background: var(--restaurant-gradient-primary) !important;
    border-bottom: 2px solid var(--restaurant-primary-dark) !important;
    border-radius: 10px 10px 0 0 !important;
    padding: 12px !important;
}

/* 日期選擇器導航按鈕樣式 */
.reservation-form-container .p-datepicker .p-datepicker-header .p-datepicker-prev,
.reservation-form-container .p-datepicker .p-datepicker-header .p-datepicker-next {
    background: var(--restaurant-primary-dark) !important;
    color: white !important;
    border-radius: 50% !important;
    width: 32px !important;
    height: 32px !important;
    transition: all 0.3s ease !important;
    border: none !important;
}

.reservation-form-container .p-datepicker .p-datepicker-header .p-datepicker-prev:hover,
.reservation-form-container .p-datepicker .p-datepicker-header .p-datepicker-next:hover {
    background: var(--restaurant-primary) !important;
    transform: scale(1.1) !important;
}

/* 日期選擇器月份/年份顯示 */
.reservation-form-container .p-datepicker .p-datepicker-header .p-datepicker-title {
    color: var(--restaurant-text-primary) !important;
    font-weight: 600 !important;
}

/* 星期標題樣式 */
.reservation-form-container .p-datepicker .p-datepicker-header .p-datepicker-title .p-datepicker-year,
.reservation-form-container .p-datepicker .p-datepicker-header .p-datepicker-title .p-datepicker-month {
    color: var(--restaurant-text-primary) !important;
    font-weight: 600 !important;
}
</style>