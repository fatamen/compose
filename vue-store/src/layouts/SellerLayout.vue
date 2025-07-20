<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import logoUrl from '../assets/logo.png'
import { useStore } from '@/composables/useStore.js' // 🔥 NEW: 導入 useStore
import { useUserStore } from '@/stores/user.js' // 🔥 NEW: 導入 userStore
import { useRouter } from 'vue-router'
import { clearAllStoreState } from '@/composables/useStore.js' // 🔥 NEW: 導入清除方法

const userStore = useUserStore() // 🔥 NEW: 獲取 userStore 實例

const router = useRouter();
// 🔥 NEW: 使用 store composable
const {
    currentUser,
    stores,
    selectedStore,
    isLoading: isStoreLoading,
    isLoggedIn,
    switchStore
} = useStore()

// 響應式資料
const iconDropdownRef = ref(null)
const showDropdown = ref(false)

// 方法
const onUserIconClick = () => {
    console.log('點擊用戶圖示:', isLoggedIn.value)
    if (isLoggedIn.value) {
        showDropdown.value = !showDropdown.value
        console.log('showDropdown:', showDropdown.value)
    }
}

const handleClickOutside = (event) => {
    if (iconDropdownRef.value && !iconDropdownRef.value.contains(event.target)) {
        showDropdown.value = false
    }
}

const logout = () => {
    console.log('🚪 開始執行登出流程...')
    
    try {
        // 🔥 1. 清除 useStore 的全域狀態（最重要！）
        console.log('🧹 清除 useStore 全域狀態...')
        clearAllStoreState()
        
        // 🔥 2. 清除 Pinia store 狀態
        if (userStore.logoutAll && typeof userStore.logoutAll === 'function') {
            console.log('🔄 使用 userStore.logoutAll() 清除狀態')
            userStore.logoutAll()
        } else {
            console.log('🔄 手動清除 userStore 狀態')
            
            // 手動清除各項狀態
            if (userStore.ownerLogout && typeof userStore.ownerLogout === 'function') {
                userStore.ownerLogout()
            }
            
            // 清除其他狀態
            if (userStore.setStoreProfile && typeof userStore.setStoreProfile === 'function') {
                userStore.setStoreProfile({})
            }
            
            if (userStore.setStoreId && typeof userStore.setStoreId === 'function') {
                userStore.setStoreId('')
            }
        }
        
        // 🔥 3. 手動清除 localStorage（三重保險）
        const localStorageKeys = [
            'ownerId', 'storeFullName', 'storeEmail', 'storeId', 'storeProfile',
            'userFullName', 'userId', 'userEmail', 'token',
            'storeName', 'address', 'storePhone'
        ]
        
        localStorageKeys.forEach(key => {
            localStorage.removeItem(key)
        })
        
        console.log('🗑️ 已清除所有相關 localStorage 項目')
        
        // 🔥 4. 觸發全域重新載入事件
        window.dispatchEvent(new CustomEvent('userLoggedOut'))
        
    } catch (error) {
        console.error('❌ 登出過程中發生錯誤:', error)
        
        // 🔥 如果出錯，強制清除所有可能的 localStorage 項目
        Object.keys(localStorage).forEach(key => {
            if (key.includes('store') || key.includes('owner') || key.includes('user')) {
                localStorage.removeItem(key)
            }
        })
        
        // 強制清除 useStore 狀態
        clearAllStoreState()
    }
    
    // 🔥 5. 重設本地 UI 狀態
    showDropdown.value = false
    
    // 🔥 6. 跳轉到首頁（稍微延遲確保清除完成）
    setTimeout(() => {
        console.log('🏠 跳轉到首頁')
        router.push('/home')
        console.log('✅ 登出流程完成')
    }, 100)
}

// 🔥 NEW: 處理店家切換
const handleStoreChange = (event) => {
    const newStoreId = parseInt(event.target.value)
    console.log('🔄 [SellerLayout] 用戶切換店家到:', newStoreId)
    switchStore(newStoreId)
}

// 生命週期
onMounted(() => {
    document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
    document.removeEventListener('click', handleClickOutside)
})

</script>

<template>
    <div class="page-wrapper">
        <!-- Header: 在 Flex 容器中，它是一個獨立的區塊 -->
        <header class="navbar d-flex justify-content-between align-items-center px-4 py-2 shadow-sm"
            style="background-color: #ffba20;">
            <a class="navbar-brand d-flex align-items-center gap-3" style="cursor: pointer"
                @click="$router.push('/vendor/dashboard')">
                <img :src="logoUrl" alt="Logo" height="80" />
                <span class="brand-title">商家管理中心</span>
            </a>
            <!-- 右側使用者資訊 -->
            <div class="d-flex align-items-center gap-3">
                <span class="text-white fw-semibold">
                    {{ currentUser ? currentUser.ownerFullName || currentUser.ownerEmail || '商家' : '使用者' }}，您好！
                </span>
                <!-- 純 Vue 控 dropdown -->
                <div ref="iconDropdownRef" class="position-relative">
                    <i class="bi bi-person-circle text-white" style="font-size: 2rem; cursor:pointer"
                        @click="onUserIconClick"></i>
                    <ul v-if="isLoggedIn && showDropdown" class="dropdown-menu dropdown-menu-end show"
                        style="position: absolute; right: 0; top: 110%;">
                        <li>
                            <a class="dropdown-item" href="#" @click.prevent="logout">登出</a>
                        </li>
                    </ul>
                </div>
            </div>
        </header>

        <div class="main-container">
            <!-- 側邊欄連結使用 router-link 並透過 "to" 屬性指定目標路徑-->

            <!-- Sidebar -->
            <nav class="sidebar">
                <div class="sidebar-sticky">

                    <!-- 🔥 NEW: 店家選擇區域 -->
                    <div class="sidebar-section">
                        <h6 class="section-title">當前店家</h6>

                        <!-- 載入中狀態 -->
                        <div v-if="isStoreLoading" class="text-center p-2">
                            <div class="spinner-border spinner-border-sm" role="status">
                                <span class="visually-hidden">Loading...</span>
                            </div>
                            <small class="d-block mt-1">載入店家中...</small>
                        </div>

                        <!-- 店家選擇 -->
                        <div v-else-if="stores.length > 0" class="mb-3">
                            <!-- 多店家：下拉選單 -->
                            <select v-if="stores.length > 1" class="form-select form-select-sm" :value="selectedStore"
                                @change="handleStoreChange">
                                <option v-for="store in stores" :key="store.id" :value="store.id">
                                    🏪 {{ store.name }}
                                </option>
                            </select>

                            <!-- 單店家：顯示名稱 -->
                            <div v-else class="alert alert-info mb-0 py-2">
                                <small>🏪 {{ stores[0].name }}</small>
                            </div>
                        </div>

                        <!-- 無店家資料 -->
                        <div v-else class="alert alert-warning mb-0 py-2">
                            <small>⚠️ 無店家資料</small>
                        </div>
                    </div>

                    <!-- 商家資訊 -->
                    <div class="sidebar-section">
                        <h6 class="section-title">管理你的商家資訊</h6>
                        <ul class="nav flex-column">
                            <li class="nav-item">
                                <router-link to="/store/edit-owner" class="nav-link" active-class="active-link">
                                    <i class="fas fa-user-gear fa-fw me-2"></i> 商家資料
                                </router-link>
                            </li>
                            <li class="nav-item">
                                <router-link to="/store/menu" class="nav-link" active-class="active-link">
                                    <i class="fas fa-utensils fa-fw me-2"></i> 菜單管理
                                </router-link>
                            </li>
                            <li class="nav-item">
                                <router-link to="/store/edit-store" class="nav-link" active-class="active-link">
                                    <i class="fas fa-store fa-fw me-2"></i> 店鋪管理
                                </router-link>
                            </li>
                        </ul>
                    </div>

                    <!-- 主要功能 -->
                    <div class="sidebar-section">
                        <h6 class="section-title">主要功能設定</h6>
                        <ul class="nav flex-column mb-2">
                            <li class="nav-item">
                                <router-link to="/store/orders" class="nav-link" active-class="active-link">
                                    <i class="fas fa-file-invoice fa-fw me-2"></i> 訂單管理
                                </router-link>
                            </li>
                            <li class="nav-item">
                                <router-link to="/store/reservations" class="nav-link" active-class="active-link">
                                    <i class="fas fa-chair fa-fw me-2"></i> 訂位管理
                                </router-link>
                            </li>
                            <li class="nav-item">
                                <router-link to="/store/hours" class="nav-link" active-class="active-link">
                                    <i class="fas fa-clock fa-fw me-2"></i> 營業時間
                                </router-link>
                            </li>

                            <li class="nav-item">
                                <router-link to="/store/timeslots" class="nav-link" active-class="active-link">
                                    <i class="fas fa-calendar-alt fa-fw me-2"></i> 時段管理
                                </router-link>
                            </li>

                            <!-- <li class="nav-item">
                                <router-link to="#" class="nav-link" active-class="active-link">
                                    <i class="fas fa-comments fa-fw me-2"></i> 評論回覆
                                </router-link>
                            </li> -->
                        </ul>
                    </div>
                </div>
            </nav>

            <!-- 主內容區域 -->
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
    overflow-y: auto;
    border-right: 1px solid #dee2e6;
    background-color: #f7f7f7;
    padding: 2rem;
    position: relative;
    /* << 新增：讓它建立一個堆疊上下文 */
    /* z-index: 20; */
    /* << 新增：給它一個較高的層級，確保它在最上面 */
}

.sidebar-section {
    margin-bottom: 2rem;
}

.section-title {
    font-size: 16px;
    font-weight: bold;
    color: #212529;
    margin-bottom: 0.5rem;
    position: relative;
    padding-left: 1.25rem;
}

.section-title::before {
    content: '';
    display: inline-block;
    width: 6px;
    height: 6px;
    background-color: #212529;
    border-radius: 50%;
    position: absolute;
    left: 0;
    top: 6px;
}

.nav-link {
    display: flex;
    align-items: center;
    font-size: 15px;
    color: #6c757d;
    background-color: transparent;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 6px;
    transition: all 0.3s ease;
    transform: translateX(0);
}

/* 圖示顏色 */
.nav-link i {
    color: #6c757d;
    width: 18px;
    text-align: center;
}

.nav-link:hover i {
    color: #eca300;
    width: 18px;
    text-align: center;
}

/* 當滑鼠移到按鈕時改變背景色和文字顏色 */
.nav-link:hover {
    background-color: #fcebc1;
    color: #eca300;
    transform: translateX(4px);
}

/* 移除 router-link 的預設藍框與藍字 */
.nav-link:focus,
.nav-link:active {
    outline: none !important;
    box-shadow: none !important;
    color: #3e2723 !important;
    background-color: #e8d4ae !important;
    text-decoration: none !important;
}

.main-content {
    flex-grow: 1;
    /* overflow-y: auto; */
    background-color: white;
    position: relative;
    /* << 新增：也建立堆疊上下文，成為子頁面絕對定位的基準 */
    z-index: 10;
    /* << 新增：層級比 sidebar 低，但比預設高 */
}

/* 確保 header 和 footer 不會被壓縮 */
header,
footer {
    flex-shrink: 0;
}

/* 🔥 NEW: 店家選擇區域樣式 */
.form-select-sm {
    font-size: 0.875rem;
}

.alert {
    font-size: 0.875rem;
}

.spinner-border-sm {
    width: 1rem;
    height: 1rem;
}
</style>