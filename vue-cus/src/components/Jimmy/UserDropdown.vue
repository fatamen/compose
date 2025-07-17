<template>
  <div class="user-dropdown-container">
    <a href="#" class="user-link" @click.prevent="toggleDropdown">
      <i class="bi bi-person-circle me-1"></i>
      <span v-if="!cUser">使用者*</span>
      <span v-else>{{ cUser }}</span>
    </a>
    <div class="dropdown-menu" v-if="showDropdown">
      <ul class="list-unstyled mb-0">
        <li @click="navigateTo('profile')" class="d-flex align-items-center gap-2 px-3 py-2">
          <i class="bi bi-person"></i> 個人資料
        </li>
        <li @click="navigateTo('VoucherWallet')" class="d-flex align-items-center gap-2 px-3 py-2">
          <i class="bi bi-ticket-perforated"></i> 優惠券
        </li>
        <li @click="navigateTo('subscription')" class="d-flex align-items-center gap-2 px-3 py-2">
          <i class="bi bi-box-seam"></i> 訂閱
        </li>
        <!-- <li @click="navigateTo('favorites')" class="d-flex align-items-center gap-2 px-3 py-2">
          <i class="bi bi-heart"></i> 收藏管理
        </li> -->
        <li @click="navigateTo('orderList')" class="d-flex align-items-center gap-2 px-3 py-2">
          <i class="bi bi-journal-text"></i> 歷史訂單
        </li>
        <li @click="navigateTo('reservation-records')" class="d-flex align-items-center gap-2 px-3 py-2">
          <i class="bi bi-calendar-check"></i> 訂位紀錄
        </li>
        <li @click="navigateTo('')" class="d-flex align-items-center gap-2 px-3 py-2">
          <i class="bi bi-house-door"></i> 回首頁*
        </li>        
        <!-- <li @click="navigateTo('WebRecom')" class="d-flex align-items-center gap-2 px-3 py-2">
          <i class="bi bi-house-door"></i> WebRecom*
        </li> 
        <li @click="navigateTo('ReportType')" class="d-flex align-items-center gap-2 px-3 py-2">
          <i class="bi bi-house-door"></i> ReportType*
        </li>  -->
        <li @click="logout" class="d-flex align-items-center gap-2 px-3 py-2">
          <i class="bi bi-box-arrow-right"></i> 登出
        </li>
      </ul>
    </div>
  </div>

</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user.js';

const showDropdown = ref(false);
const router = useRouter();

const userFullName = ref('');
const userEmail = ref('');

const userStore = useUserStore();
const cUser = computed(() => userStore.fullName);

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

// 導航到對應頁面
const navigateTo = (path) => {
  router.push(`/${path}`);
  showDropdown.value = false;
};

// 登出邏輯
const logout = () => {
  userStore.logout();
  showDropdown.value = false;
  router.push('/search');
};

// 點擊外部關閉下拉選單
const handleClickOutside = (event) => {
  if (!event.target.closest('.user-dropdown-container')) {
    showDropdown.value = false;
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside);

});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
});
</script>

<style scoped>
.user-dropdown-container {
  position: relative;
  display: inline-block;
}

.user-link {
  color: white;
  text-decoration: none;
  font-size: 16px;
  padding: 8px 12px;
  cursor: pointer;
}

.user-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  /* 確保 z-index 足夠高 */
  min-width: 160px;
  margin-top: 5px;
  display: block;
  /* 添加此行，確保顯示 */
}

.dropdown-menu ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

/* 游標滑到使用者按鈕會變色 */
.dropdown-menu li {
  padding: 12px 16px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
}

/* 游標滑到下拉式選單會變底色 */
.dropdown-menu li:hover {
  background-color: #fff3cd;
}

/* 游標滑到下拉式選單圖示會變色 */
.dropdown-menu li:hover i {
  color: #ffba20;
}

/* 預設為黃色文字 */
.dropdown-menu li:last-child {
  border-top: 1px solid #ddd;
  color: #ffba20;
  font-weight: 500;
}

/* 滑鼠移上去時反轉背景與文字顏色 */
.dropdown-menu li:last-child:hover {
  color: #5c3202;
}
</style>
