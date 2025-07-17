<script setup>
// 把 Logo 和 Avatar 的導入也移到這裡
import logoUrl from '../assets/logo.png';
import avataUrl from '../assets/avata.png';
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
                <span class="text-white fw-semibold">使用者，您好！</span>
                <img :src="avataUrl" alt="Avatar" class="rounded-circle"
                    style="height: 40px; width: 40px; object-fit: cover;" />
            </div>
        </header>

        <div class="main-container">
            <!-- 側邊欄連結使用 router-link 並透過 "to" 屬性指定目標路徑-->

            <!-- Sidebar -->
            <nav class="sidebar">
                <div class="sidebar-sticky">
                    <!-- 商家資訊 -->
                    <div class="sidebar-section">
                        <h6 class="section-title">管理你的商家資訊</h6>
                        <ul class="nav flex-column">
                            <li class="nav-item">
                                <router-link to="#" class="nav-link" active-class="active-link">
                                    <i class="fas fa-user-gear fa-fw me-2"></i> 商家資料
                                </router-link>
                            </li>
                            <li class="nav-item">
                                <router-link to="/menu" class="nav-link" active-class="active-link">
                                    <i class="fas fa-utensils fa-fw me-2"></i> 菜單管理
                                </router-link>
                            </li>
                            <li class="nav-item">
                                <router-link to="#" class="nav-link" active-class="active-link">
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
                                <router-link to="/orders" class="nav-link" active-class="active-link">
                                    <i class="fas fa-file-invoice fa-fw me-2"></i> 訂單管理
                                </router-link>
                            </li>
                            <li class="nav-item">
                                <router-link to="/reservations" class="nav-link" active-class="active-link">
                                    <i class="fas fa-chair fa-fw me-2"></i> 訂位管理
                                </router-link>
                            </li>
                            <li class="nav-item">
                                <router-link to="/hours" class="nav-link" active-class="active-link">
                                    <i class="fas fa-clock fa-fw me-2"></i> 營業時間
                                </router-link>
                            </li>
                            <li class="nav-item">
                                <router-link to="/time-setting-test" class="nav-link" active-class="active-link">
                                    <i class="fas fa-cog fa-fw me-2"></i> 時段設定
                                </router-link>
                            </li>
                            <li class="nav-item">
                                <router-link to="#" class="nav-link" active-class="active-link">
                                    <i class="fas fa-comments fa-fw me-2"></i> 評論回覆
                                </router-link>
                            </li>
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
    z-index: 20;
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
</style>