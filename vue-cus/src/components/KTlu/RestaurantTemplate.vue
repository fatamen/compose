<template><!--上一層是Restaurant.page-->
    <div class="restaurant-container goldenbowl-restaurant-theme">
        <!-- 餐廳橫幅 -->
        <RestaurantBanner :restaurant="restaurant" />

        <!-- 餐廳名稱區域 -->
        <div class="restaurant-header">
            <h1 class="restaurant-main-title">{{ restaurant.name }}</h1>
        </div>

        <!-- TabMenu 區域 -->
        <div class="tab-menu-section restaurant-light-bg">
            <div class="tab-menu-container">
                <div class="tab-menu-tabs">
                    <button class="tab-menu-tab" :class="{ active: activeTab === 'reservation' }"
                        @click="activeTab = 'reservation'">
                        <i class="pi pi-calendar"></i>
                        <span>預約訂位</span>
                    </button>
                    <button class="tab-menu-tab" :class="{ active: activeTab === 'order' }"
                        @click="activeTab = 'order'">
                        <i class="pi pi-shopping-cart"></i>
                        <span>線上訂餐</span>
                    </button>
                </div>
            </div>
        </div>

        <!-- 餐廳資訊 -->
        <RestaurantInfo :restaurant="restaurantWithHours" />

        <!-- Tab 內容區域 -->
        <div class="tab-content-section">
            <!-- 預約訂位內容 -->
            <div v-if="activeTab === 'reservation'" class="tab-content" id="reservation-content">
                <ReservationForm :restaurant-id="restaurant.id.toString()" :user-data="currentUserData" />
            </div>

            <!-- 線上訂餐內容 -->
            <div v-if="activeTab === 'order'" class="tab-content" id="order-content">
                <RestaurantMenu :restaurant="restaurant" @checkout="handleCheckout" />
            </div>
        </div>

        <!-- 評論摘要與地圖並排區域 -->
        <div class="comment-map-section">
            <div class="comment-map-container">
                <!-- 評論摘要 -->
                <div class="comment-wrapper">
                    <CommentSummary :comments="comments" :restaurant="restaurant" />
                </div>

                <!-- 地圖 -->
                <div class="map-wrapper">
                    <RestaurantMap :restaurant="restaurant" />
                </div>
            </div>
        </div>

        <!-- 頁腳 -->
        <RestaurantFooter :restaurant="restaurant" />
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue'
import RestaurantBanner from './RestaurantBanner.vue'
import RestaurantInfo from './RestaurantInfo.vue'
import RestaurantMenu from './RestaurantMenu.vue'
import RestaurantMap from './RestaurantMap.vue'
import ReservationForm from './ReservationForm.vue'
import RestaurantFooter from './RestaurantFooter.vue'
import CommentSummary from './CommentSummary.vue'
import { useUserStore } from '@/stores/user.js' // 引入用戶 store
import apiClient from '../../plungins/axios.js' // 引入 API 客戶端
import '@/assets/css/restaurant-theme.css'

// 用戶 store
const userStore = useUserStore()

// 接收餐廳資料
const props = defineProps({
    restaurant: {
        type: Object,
        required: true
    }
})

// 營業時間資料
const businessHours = ref(null)
const businessHoursLoading = ref(false)
const businessHoursError = ref(null)

// 評論資料
const comments = ref([])
const commentsLoading = ref(false)
const commentsError = ref(null)

// 整合後的餐廳資料（包含營業時間）
const restaurantWithHours = computed(() => {
    if (!props.restaurant) return null

    return {
        ...props.restaurant,
        businessHours: businessHours.value
    }
})

// 從 store 獲取用戶資料並映射到預約表單需要的格式
const currentUserData = computed(() => {
    const userData = {
        name: userStore.fullName || '',
        phone: userStore.phone || '',
        email: userStore.email || '',
        userId: userStore.userId || null
    }
    console.log('🏪 RestaurantTemplate currentUserData:', userData)
    return userData
})

// Tab狀態管理
const activeTab = ref('order') // 預設顯示線上訂餐以展示Hero風格菜單導航

// 獲取營業時間資料
const fetchBusinessHours = async () => {
    if (!props.restaurant?.id) {
        console.warn('餐廳 ID 不存在，無法獲取營業時間')
        return
    }

    businessHoursLoading.value = true
    businessHoursError.value = null

    try {
        console.log('正在獲取餐廳營業時間，餐廳ID:', props.restaurant.id)

        // 使用完整營業時間 API
        const response = await apiClient.get(`/api/stores/${props.restaurant.id}/hours/complete`)

        console.log('營業時間資料:', response.data)
        businessHours.value = response.data

    } catch (error) {
        console.error('獲取營業時間失敗:', error)
        businessHoursError.value = '無法載入營業時間資訊'

        // 如果 API 失敗，嘗試使用基本營業時間 API
        try {
            console.log('嘗試使用基本營業時間 API')
            const basicResponse = await apiClient.get(`/api/stores/${props.restaurant.id}/hours`)
            businessHours.value = basicResponse.data
        } catch (basicError) {
            console.error('基本營業時間 API 也失敗:', basicError)
            businessHours.value = null
        }
    } finally {
        businessHoursLoading.value = false
    }
}

// 獲取評論資料
const fetchComments = async () => {
    if (!props.restaurant?.id) {
        console.warn('餐廳 ID 不存在，無法獲取評論')
        return
    }

    commentsLoading.value = true
    commentsError.value = null

    try {
        console.log('正在獲取餐廳評論，餐廳ID:', props.restaurant.id)
        // 修正 API 路徑：移除 /api 前綴
        const response = await apiClient.get(`/comment/store/${props.restaurant.id}`)

        console.log('評論資料:', response.data)
        comments.value = Array.isArray(response.data) ? response.data : []

    } catch (error) {
        console.error('獲取評論失敗:', error)
        commentsError.value = '無法載入評論資訊'
        comments.value = []
    } finally {
        commentsLoading.value = false
    }
}

// 處理結帳
const handleCheckout = (orderData) => {
    console.log('處理結帳:', orderData)
    // 這裡可以添加結帳處理邏輯
    // 例如：跳轉到支付頁面、顯示結帳表單等
    alert(`訂單總計：NT$${orderData.totalAmount}\n正在處理訂單...`)
}

function getStoreFoods() {

}

// 生命周期
onMounted(async () => {
    console.log('🏪 RestaurantTemplate 已載入')
    console.log('餐廳資料:', props.restaurant)
    console.log('用戶資料:', currentUserData.value)

    // 並行獲取營業時間和評論資料
    await Promise.all([
        fetchBusinessHours(),
        fetchComments()
    ])
})

// 監聽餐廳 ID 變化
watch(() => props.restaurant?.id, async (newId, oldId) => {
    if (newId && newId !== oldId) {
        console.log('餐廳 ID 變化，重新獲取資料:', newId)
        await Promise.all([
            fetchBusinessHours(),
            fetchComments()
        ])
    }
})
</script>

<style scoped>
.restaurant-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0;
    background: var(--restaurant-bg-primary);
    box-shadow: 0 0 20px var(--restaurant-shadow-light);
}

.restaurant-header {
    text-align: left;
    padding: 2rem 1rem;
    background: var(--restaurant-gradient-subtle);
    width: 100%;
    margin: 0 0;
    border-bottom: 2px solid var(--restaurant-border-light);
}

.restaurant-main-title {
    font-size: 2.5rem;
    font-weight: 700;
    color: var(--restaurant-text-primary);
    margin: 0;
    letter-spacing: 1px;
    text-shadow: 0 1px 2px var(--restaurant-shadow-neutral);
}

/* TabMenu 樣式 - 優雅主題 */
.tab-menu-section {
    background: var(--restaurant-bg-secondary);
    padding: 1rem;
    border-bottom: 2px solid var(--restaurant-border-light);
}

.tab-menu-container {
    max-width: 600px;
    margin: 0 auto;
    text-align: center;
}

.tab-menu-tabs {
    display: inline-flex;
    background: var(--restaurant-bg-primary);
    border: 1px solid var(--restaurant-border-medium);
    border-radius: 8px;
    padding: 0.25rem;
    gap: 0.25rem;
    box-shadow: 0 2px 8px var(--restaurant-shadow-neutral);
}

.tab-menu-tab {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.5rem;
    background: transparent;
    border: none;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 0.9rem;
    color: var(--restaurant-text-secondary);
    font-weight: 500;
}

.tab-menu-tab:hover {
    background: var(--restaurant-bg-accent);
    color: var(--restaurant-text-primary);
    transform: translateY(-1px);
}

.tab-menu-tab.active {
    background: var(--restaurant-text-secondary);
    color: var(--restaurant-bg-primary);
    box-shadow: 0 3px 12px var(--restaurant-shadow-medium);
    transform: translateY(-1px);
}

.tab-menu-tab i {
    font-size: 1.1rem;
}

.tab-menu-tab span {
    font-weight: 500;
}

/* Tab 內容區域 - 黃白主題 */
.tab-content-section {
    background: var(--restaurant-bg-primary);
    width: 100%;
    margin: 0;
    padding: 0;
    border-top: 1px solid var(--restaurant-border-light);
}

.tab-content {
    padding: 1.5rem 1rem;
    scroll-margin-top: 80px;
    background: var(--restaurant-bg-primary);
}

/* 評論摘要與地圖並排區域 */
.comment-map-section {
    padding: 2rem 1rem;
    background: var(--restaurant-bg-light);
    border-top: 1px solid var(--restaurant-border-light);
}

.comment-map-container {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
    max-width: 1200px;
    margin: 0 auto;
}

.comment-wrapper,
.map-wrapper {
    width: 100%;
}

/* 響應式設計 */
@media (max-width: 1024px) {
    .comment-map-container {
        gap: 1.5rem;
    }
}

@media (max-width: 768px) {
    .restaurant-main-title {
        font-size: 2rem;
    }

    .restaurant-header {
        padding: 1.5rem 1rem;
        width: 100%;
    }

    .tab-menu-section {
        padding: 0.75rem;
    }

    .tab-menu-title {
        font-size: 1.25rem;
    }

    .tab-menu-subtitle {
        font-size: 0.8rem;
    }

    .tab-menu-tabs {
        gap: 0.2rem;
        margin-top: 0.75rem;
    }

    .tab-menu-tab {
        padding: 0.65rem 1.25rem;
        font-size: 0.85rem;
    }

    .tab-menu-tab i {
        font-size: 1rem;
    }

    .tab-content {
        padding: 1.25rem 0.75rem;
    }

    .content-header h3 {
        font-size: 1.1rem;
    }

    /* 手機版評論摘要與地圖垂直排列 */
    .comment-map-section {
        padding: 1.5rem 0.75rem;
    }

    .comment-map-container {
        grid-template-columns: 1fr;
        gap: 1.5rem;
    }
}

@media (max-width: 480px) {
    .restaurant-main-title {
        font-size: 1.75rem;
    }

    .restaurant-header {
        padding: 1rem;
    }

    .tab-menu-section {
        padding: 0.5rem;
    }

    .tab-menu-tab {
        padding: 0.5rem 1rem;
        font-size: 0.8rem;
    }

    .tab-content {
        padding: 1rem 0.5rem;
    }

    .comment-map-section {
        padding: 1rem 0.5rem;
    }

    .comment-map-container {
        gap: 1rem;
    }
}
</style>