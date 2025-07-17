<script setup>
import { ref } from 'vue';
import GeneralHoursEditor from '../components/hours/GeneralHoursEditor.vue';
import SpecialHoursEditor from '../components/hours/SpecialHoursEditor.vue';

// 模態框/側邊欄的顯示狀態
const isSidebarVisible = ref(false);
const sidebarType = ref(''); // 'general' 或 'special'
const editingDay = ref(''); // 當編輯一般營業時間時，表示正在編輯哪一天

// 提示訊息的狀態
const showNotification = ref(false);
const notificationMessage = ref('');
const notificationType = ref('success'); // success, warning, danger 等

// 模擬一般營業時間數據
const generalHours = ref({
  'monday': '關閉',
  'tuesday': '10:00 - 14:00',
  'wednesday': '10:00 - 14:00',
  'thursday': '10:00 - 14:00',
  'friday': '關閉',
  'saturday': '關閉',
  'sunday': '關閉',
});

// 模擬特殊營業時間數據 (範例，可能需要更複雜的結構來存儲日期範圍等)
const specialHoursRecords = ref([
  // 範例數據，模擬圖片中的一個特殊營業時間
  {
    type: 'range',
    startDate: '28/05/2024',
    endDate: '31/05/2024',
    time: '10:00 至 20:00',
  },
]);

// 開啟一般營業時間編輯器
const openGeneralEditor = (day) => {
  sidebarType.value = 'general';
  editingDay.value = day; // 傳遞正在編輯的日期
  isSidebarVisible.value = true;
  hideNotification(); // 開啟側邊欄時隱藏提示
};

// 開啟特殊營業時間編輯器
const openSpecialEditor = () => {
  sidebarType.value = 'special';
  editingDay.value = ''; // 清空編輯日期
  isSidebarVisible.value = true;
  hideNotification(); // 開啟側邊欄時隱藏提示
};

// 關閉側邊欄
const closeSidebar = () => {
  isSidebarVisible.value = false;
  sidebarType.value = '';
  editingDay.value = '';
};

// 顯示提示訊息
const showToast = (message, type = 'success') => {
  notificationMessage.value = message;
  notificationType.value = type;
  showNotification.value = true;

  // 自動隱藏提示
  setTimeout(() => {
    hideNotification();
  }, 3000); // 3 秒後隱藏
};

// 隱藏提示訊息
const hideNotification = () => {
  showNotification.value = false;
  notificationMessage.value = '';
};

// 處理一般營業時間的保存
const handleSaveGeneralHours = (updatedHours) => {
  generalHours.value = { ...generalHours.value, ...updatedHours };
  closeSidebar();
  showToast('一般營業時間已更新！', 'success');
};

// 處理特殊營業時間的保存
const handleSaveSpecialHours = (newRecord) => {
  specialHoursRecords.value.push(newRecord); // 簡單添加，實際應用中可能需要更複雜的邏輯
  closeSidebar();
  showToast('特殊營業時間已新增！', 'success'); // 顯示成功提示
};

// 刪除特殊營業時間 (範例)
const deleteSpecialHours = (index) => {
  if (confirm('確定要刪除此特殊營業時間記錄嗎？')) {
    specialHoursRecords.value.splice(index, 1);
    showToast('特殊營業時間已刪除！', 'warning'); // 顯示刪除提示
  }
};
</script>

<template>
  <!-- 根容器：佔滿整個視口，並設置背景色 -->
  <div class="h-100 w-100">
    <!-- 主要內容區塊的包裝器：佔據整個寬度，並提供內邊距 -->
    <div class="main-content-wrapper d-flex flex-column h-100 p-4">
      <!-- 移除 card 類別，保持 shadow-sm, flex-grow-1, rounded-4, p-4 並加上 bg-white -->
      <div class="  p-4">
        <div class="card-body">
          <h4 class="mb-4 fw-bold">營業時間</h4>
          <hr class="mb-4">

          <div class="row"> <!-- 這個 row 用來並排「一般營業時間」和「特殊營業時間」 -->
            <div class="col-12 col-md-6 mb-4"> <!-- 一般營業時間區塊 -->
              <div class="d-flex justify-content-between align-items-center mb-4">
                <h5 class="fw-bold mb-0">一般營業時間</h5>
                <button class="btn btn-success btn-sm px-3 py-2" @click="openGeneralEditor()">
                  <i class="bi bi-pencil me-2"></i>編輯
                </button>
              </div>
              <ul class="list-unstyled mb-0">
                <li v-for="(time, day) in generalHours" :key="day" class="d-flex justify-content-between align-items-center py-2 border-bottom">
                  <span class="fw-bold text-capitalize">{{ day === 'monday' ? '週一' : day === 'tuesday' ? '週二' : day === 'wednesday' ? '週三' : day === 'thursday' ? '週四' : day === 'friday' ? '週五' : day === 'saturday' ? '週六' : '週日' }}</span>
                  <span>{{ time }}</span>
                </li>
              </ul>
            </div>

            <div class="col-12 col-md-6 mb-4"> <!-- 特殊營業時間區塊 -->
              <div class="d-flex justify-content-between align-items-center mb-4">
                <h5 class="fw-bold mb-0">特殊營業時間 / 店休</h5>
                <button class="btn btn-primary btn-sm  px-3 py-2" @click="openSpecialEditor">
                  <i class="bi bi-plus-circle me-2"></i>新增
                </button>
              </div>

              <!-- <p class="text-muted small mb-4">
                如有欲臨時特殊營業時間與平時的營業時間不同時，請在此設定 (例如：特殊公休日等)。提醒您：外送與外帶將自動同步，恕無法修改。如有特殊需求請與線上客服聯繫。店休期間 foodpanda 將依約收取期租費。如需長期暫停營業，請透過經銷中心或線上客服告知，我們將盡速為您處理。
              </p> -->

              <div v-if="specialHoursRecords.length === 0" class="text-center py-5   ">
                <img src="https://placehold.co/150x150/E5E7EB/4B5563?text=No+Records" alt="No Records" class="mb-3">
                <p class="text-muted mb-4">尚未設定的店休紀錄</p>
                <p class="text-muted mb-4">您尚未設定任何特殊營業時間或店休日期，請點擊下方按鈕。</p>
                <button class="btn btn-outline-primary  px-4 py-2 fw-bold" @click="openSpecialEditor">
                  <i class="bi bi-plus-circle me-2"></i>設定特殊營業時間
                </button>
              </div>
              <div v-else>
                <!-- 顯示已設定的特殊營業時間列表 -->
                <div v-for="(record, index) in specialHoursRecords" :key="index" class="card mb-3 shadow-sm rounded-3">
                  <div class="card-body d-flex justify-content-between align-items-center">
                    <div>
                      <h6 class="mb-1">{{ record.type === 'specific' ? '特定日期' : '日期範圍' }}</h6>
                      <p class="mb-0 small text-muted">
                        {{ record.type === 'specific' ? record.date : `${record.startDate} 至 ${record.endDate}` }}
                      </p>
                      <p class="mb-0 small text-muted">{{ record.time === 'closed' ? '關閉' : record.time }}</p>
                    </div>
                    <button class="btn btn-outline-danger btn-sm" @click="deleteSpecialHours(index)">刪除</button>
                  </div>
                </div>
                <!-- <button class="btn btn-outline-primary  px-4 py-2 fw-bold w-100" @click="openSpecialEditor">
                  <i class="fas fa-plus-circle me-2"></i>新增特殊營業時間
                </button> -->
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右側側邊欄 -->
    <div
      class="bg-white shadow-lg border-start position-fixed end-0 h-100 detail-sidebar"
      :class="{ 'show': isSidebarVisible }"
    >
      <GeneralHoursEditor
        v-if="sidebarType === 'general'"
        :generalHours="generalHours"
        @close="closeSidebar"
        @save="handleSaveGeneralHours"
      />
      <SpecialHoursEditor
        v-if="sidebarType === 'special'"
        @close="closeSidebar"
        @save="handleSaveSpecialHours"
      />
    </div>

    <!-- 側邊欄疊加層 (點擊空白處關閉) -->
    <div
      class="overlay"
      :class="{ 'show': isSidebarVisible }"
      @click="closeSidebar"
    ></div>

    <!-- 底部提示訊息（Toast / Snackbar 樣式） -->
    <transition name="fade">
      <div
        v-if="showNotification"
        :class="['notification-toast', 'bg-' + notificationType, 'text-white']"
      >
        {{ notificationMessage }}
        <button type="button" class="btn-close btn-close-white ms-auto" @click="hideNotification" aria-label="Close"></button>
      </div>
    </transition>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

/* 自定義粉色邊框按鈕 */
.btn-pink-outline {
  color: #dc3545; /* 圖片中按鈕的顏色類似於 danger */
  border-color: #dc3545;
  background-color: transparent;
  transition: all 0.2s ease-in-out;
}

.btn-pink-outline:hover {
  color: white;
  background-color: #dc3545;
  border-color: #dc3545;
}

/* 側邊欄樣式 */
.detail-sidebar {
  width: 100%;
  max-width: 500px; /* 控制側邊欄最大寬度 */
  transform: translateX(100%);
  transition: transform 0.3s ease-in-out;
  z-index: 1050; /* 比 overlay 高 */
  top: 0; /* 確保從頂部開始 */
  height: 100vh !important; /* 確保側邊欄充滿整個視口高度 */
  overflow-y: auto; /* 允許側邊欄內容滾動 */
}

.detail-sidebar.show {
  transform: translateX(0);
}

/* 疊加層樣式 */
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1040; /* 在側邊欄之下 */
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease-in-out, visibility 0.3s ease-in-out;
}

.overlay.show {
  opacity: 1;
  visibility: visible;
}

/* 確保側邊欄在移動端全寬 */
@media (max-width: 1199.98px) { /* xl breakpoint */
  .detail-sidebar {
    position: fixed;
    max-width: 100%;
  }
}

/* 提示訊息 (Toast) 樣式 */
.notification-toast {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  min-width: 280px;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  z-index: 1100; /* 確保在最上層 */
  font-weight: bold;
}

/* 過渡動畫 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  transform: translateY(20px) translateX(-50%);
}
.fade-enter-to, .fade-leave-from {
  opacity: 1;
  transform: translateY(0) translateX(-50%);
}
</style>
