<template>
    <div class="page-wrapper">
        <!-- Top Navbar，完全取代 header -->
        <TopNavbar />

        <div class="main-container">
            <!-- Sidebar -->
            <nav class="sidebar">
                <div class="sidebar-sticky">
                    <div class="sidebar-section">
                        <ul class="list-group list-group-flush">
                            <li>
                                <router-link to="/account" class="list-group-item parent" :class="{ active: isAccountActive }">
                                    <i class="bi bi-person-gear me-2"></i> 管理帳號
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/restaurant" class="list-group-item parent" :class="{ active: isRestaurantActive }">
                                    <i class="bi bi-clipboard-check me-2"></i> 審核餐廳
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/review" class="list-group-item parent" :class="{ active: isReviewActive }">
                                    <i class="bi bi-chat-left-dots me-2"></i> 評價檢舉
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/promotionList" class="list-group-item parent" :class="{ active: isPromotionListActive }">
                                    <i class="bi bi-gift me-2"></i> 優惠活動
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/subscription/plan" class="list-group-item parent" :class="{ active: isSubscriptionPlanActive }">
                                    <i class="bi bi-bag-check me-2"></i> 訂閱方案
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/subscription/record" class="list-group-item parent" :class="{ active: isSubscriptionRecordActive }">
                                    <i class="bi bi-clock-history me-2"></i> 訂閱紀錄
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/restaurantTag" class="list-group-item parent" :class="{ active: isRestaurantTagActive }">
                                    <i class="bi bi-tags me-2"></i> 餐廳標籤
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/foodTag" class="list-group-item parent" :class="{ active: isFoodTagActive }">
                                    <i class="bi bi-egg-fried me-2"></i> 食物標籤
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/recommendation" class="list-group-item parent" :class="{ active: isRecommendationActive }">
                                    <i class="bi bi-star me-2"></i> 網站推薦
                                </router-link>
                            </li>
                            <li>
                                <router-link to="/reportType" class="list-group-item parent" :class="{ active: isReportTypeActive }">
                                    <i class="bi bi-star me-2"></i> 檢舉項目管理
                                </router-link>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
            <main class="main-content p-4">
                <router-view />
            </main>
        </div>

        <!-- Footer -->
        <footer class="bg-warning text-white text-center p-3 flex-shrink-0">
            Footer
        </footer>
    </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TopNavbar from '@/components/TopNavbar.vue'

const route = useRoute()

const isAccountActive            = computed(() => route.path.startsWith('/account'))
const isRestaurantActive         = computed(() => route.path.startsWith('/restaurant'))
const isReviewActive             = computed(() => route.path.startsWith('/review'))
// const isDiscountActive           = computed(() => route.path.startsWith('/discount'))
const isPromotionListActive = computed(() => route.path.startsWith('/promotionList'))
const isSubscriptionPlanActive   = computed(() => route.path.startsWith('/subscription/plan'))
const isSubscriptionRecordActive = computed(() => route.path.startsWith('/subscription/record'))
const isRestaurantTagActive      = computed(() => route.path.startsWith('/restaurantTag'))
const isFoodTagActive            = computed(() => route.path.startsWith('/foodTag'))
const isRecommendationActive     = computed(() => route.path.startsWith('/recommendation'))
const isReportTypeActive     = computed(() => route.path.startsWith('/reportType'))
</script>

<style scoped>
.brand-title {
color: #5c3203;
font-weight: bold;
font-size: 1.5rem;
}
.page-wrapper {
display: flex;
flex-direction: column;
height: 100vh;
background-color: #fdfaf5;
}
.main-container {
display: flex;
flex-grow: 1;
overflow: hidden;
}
.sidebar {
flex-shrink: 0;
width: 250px;
min-width: 200px;
overflow-y: auto;
border-right: 1px solid #dee2e6;
background-color: #f7f7f7;
padding: 2rem 1rem 2rem 1rem;
height: 100%;
}
.sidebar.open {
transform: translateX(0);
}
.sidebar-sticky {
position: sticky;
top: 0;
}
.sidebar-section {
margin-bottom: 2rem;
}

    /* 去除 sidebar ul/li 的圓點與縮排 */
    .sidebar .list-group {
    list-style: none;
    padding-left: 0;
    }
    .sidebar .list-group li {
    list-style: none;
    padding-left: 0;
    }

    /* 去除 router-link(a) 被點選的 outline 藍邊與 hover 背景 */
    .sidebar .list-group-item:focus,
    .sidebar .list-group-item:active {
    outline: none !important;
    box-shadow: none !important;
    }

    /* 視覺再美化：hover 高亮/active 狀態可自訂 */
    .sidebar .list-group-item:hover,
    .sidebar .list-group-item.active {
    background: #fff8e1;
    color: #ff9800;
    border: none;
    }

.list-group-item {
background: none;
border: none;
font-size: 15px;
color: #6c757d;
border-radius: 6px;
padding: 0.75rem 1rem;
display: flex;
align-items: center;
cursor: pointer;
transition: all 0.2s;
}
.list-group-item.parent {
font-weight: bold;
}
.list-group-item.child {
padding-left: 2.5rem;
font-weight: normal;
}
.list-group-item.active,
.list-group-item:hover {
background-color: #fcebc1;
color: #eca300;
}
.list-group-item.active i,
.list-group-item:hover i {
color: #eca300;
}
.list-group-item i {
color: #6c757d;
min-width: 18px;
}
.main-content {
flex-grow: 1;
overflow-y: auto;
background-color: white;
}
.breadcrumb-bar {
background: #f0f0f0;
padding: .75rem 1rem;
margin: 0 0 1.5rem 0;
border-radius: 0.5rem;
}
header,
footer {
flex-shrink: 0;
}
</style>
