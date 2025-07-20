<script setup>
import { onMounted, ref } from 'vue';
import GeneralHoursEditor from '../components/hours/GeneralHoursEditor.vue';
import SpecialHoursEditor from '../components/hours/SpecialHoursEditor.vue';
import axios from '@/plungins/axios.js';
import Swal from 'sweetalert2';
import { useUserStore } from '@/stores/user.js'

const userStore = useUserStore();

const storeId = userStore.ownerId; //店家id
// const storeId = ref(7); //店家id

// 模態框/側邊欄的顯示狀態
const isSidebarVisible = ref(false);
const sidebarType = ref(''); // 'general' 或 'special'
const editingDay = ref(''); // 當編輯一般營業時間時，表示正在編輯哪一天

// 提示訊息的狀態
const showNotification = ref(false);
const notificationMessage = ref('');
const notificationType = ref('success'); // success, warning, danger 等
const generalHours = ref([]);//一般營業時間

//預設陣列
const defaultDayData = [
  { storeId: storeId, dayOfWeek: 'SUNDAY', dayName: '星期日', openTimeStr: null, closeTimeStr: null, isOpen: false },
  { storeId: storeId, dayOfWeek: 'MONDAY', dayName: '星期一', openTimeStr: null, closeTimeStr: null, isOpen: false },
  { storeId: storeId, dayOfWeek: 'TUESDAY', dayName: '星期二', openTimeStr: null, closeTimeStr: null, isOpen: false },
  { storeId: storeId, dayOfWeek: 'WEDNESDAY', dayName: '星期三', openTimeStr: null, closeTimeStr: null, isOpen: false },
  { storeId: storeId, dayOfWeek: 'THURSDAY', dayName: '星期四', openTimeStr: null, closeTimeStr: null, isOpen: false },
  { storeId: storeId, dayOfWeek: 'FRIDAY', dayName: '星期五', openTimeStr: null, closeTimeStr: null, isOpen: false },
  { storeId: storeId, dayOfWeek: 'SATURDAY', dayName: '星期六', openTimeStr: null, closeTimeStr: null, isOpen: false }, // 週末可以給不同預設值
];

const findOpenHours = (id) => {
  axios.get(`/api/stores/${id}/hours`).then((res) => {
    generalHours.value = res.data
    // //console.log(generalHours.value)
    // //console.log(generalHours.value)

  }).catch((error) => {
    //console.log(error);
  })

}
const findSpecialHours = (id) => {
  axios.get(`/api/stores/${id}/special/all`)
    .then((res) => {
      specialHoursRecords.value = res.data
      //console.log('特殊營業時間',res)


    }).catch((error) => {
      //console.log(error);
    })
}


// 模擬特殊營業時間數據 (範例，可能需要更複雜的結構來存儲日期範圍等)
// 模擬特殊營業時間數據 (符合您的新格式)
const specialHoursRecords = ref([]);

// 開啟一般營業時間編輯器
const openGeneralEditor = (day) => {
  if (generalHours.value.length == 0) {
    axios.put(`/api/stores/${storeId}/hours/saveAll`, defaultDayData)
      .then((res) => {
        // //console.log(res.data) 
        showToast('建立一般營業時間！', 'success');
        findOpenHours(storeId)
      }).catch((error) => {
        //console.log(error);
      })

  }
  // //console.log("陣列長度",generalHours.value.length)
  // //console.log("陣列長度",generalHours.value.length)

  sidebarType.value = 'general';
  // editingDay.value = day; // 傳遞正在編輯的日期
  isSidebarVisible.value = true;
  hideNotification(); // 開啟側邊欄時隱藏提示
};

// 開啟特殊營業時間編輯器
const openSpecialEditor = () => {
  sidebarType.value = 'special';
  // editingDay.value = ''; // 清空編輯日期
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
  // generalHours.value = { ...generalHours.value, ...updatedHours };
  axios.put(`/api/stores/${storeId}/hours/saveAll`, updatedHours)
    .then((res) => {
      // //console.log(res.data) 
      showToast('一般營業時間已更新！', 'success');
      findOpenHours(storeId)
    }).catch((error) => {
      //console.log(error);
    })

  closeSidebar();

};

// 處理特殊營業時間的保存
const handleSaveSpecialHours = (newRecord) => {
  // specialHoursRecords.value.push(newRecord); // 簡單添加，實際應用中可能需要更複雜的邏輯
  axios.put(`/api/stores/${storeId}/special/saveAll`, newRecord)
    .then((res) => {
      // //console.log(res.data) 
      findSpecialHours(storeId)
      showToast('特殊營業時間已新增！', 'success'); // 顯示成功提示
      closeSidebar();
    }).catch((error) => {
      //console.log(error);
    })

  //console.log('接收到newRecord', newRecord)




};

// 刪除特殊營業時間 (範例)
const deleteSpecialHours = (index) => {
  Swal.fire({
    title: '確定要刪除此錄嗎？', // 確認彈窗的標題
    text: "此操作將無法復原！", // 確認彈窗的內容文字
    icon: 'warning', // 顯示警告圖標
    showCancelButton: true, // 顯示取消按鈕
    confirmButtonColor: '#d33', // 確認按鈕的顏色 (紅色)
    cancelButtonColor: '#6c757d', // 取消按鈕的顏色 (灰色)
    confirmButtonText: '是的，刪除它！', // 確認按鈕的文字
    cancelButtonText: '取消' // 取消按鈕的文字+

  }).then((result) => {
    // 檢查用戶是否點擊了「確認」按鈕
    if (result.isConfirmed) {
      axios.delete(`/api/stores/${storeId}/special/${index}`) // 確保這裡使用 idToDelete
        .then((res) => {
          // 成功刪除後，重新載入特殊營業時間列表
          findSpecialHours(storeId);
          // 顯示 SweetAlert2 成功提示
          Swal.fire({
            icon: 'success',
            title: '刪除成功！',
            text: '特殊營業時間已成功刪除。',
            showConfirmButton: false,
            timer: 1500
          });
          // 移除 showToast，避免重複提示
          // showToast('特殊營業時間已刪除！', 'danger'); 
        })
        .catch((error) => {
          //console.error("刪除特殊營業時間失敗:", error);

          // 根據錯誤類型設置提示訊息
          let errorMessage = '刪除失敗，請稍後再試。';
          if (error.response && error.response.status === 400) {
            errorMessage = '刪除失敗：請求無效或該記錄不存在。';
          } else if (error.response && error.response.data && error.response.data.message) {
            errorMessage = error.response.data.message;
          }

          // 顯示 SweetAlert2 錯誤提示
          Swal.fire({
            icon: 'error',
            title: '刪除失敗！',
            text: errorMessage,
            confirmButtonText: '確認'
          });
          // 移除 showToast，避免重複提示
          // showToast('刪除失敗！', 'danger');
        });
    }
  });
};



onMounted(() => {

  findOpenHours(storeId)
  findSpecialHours(storeId)
})
</script>

<template>
  <!-- 根容器：佔滿整個視口，並設置背景色 -->
  <div class=" flex-grow-1 w-100 d-flex flex-column">
    <!-- 主要內容區塊的包裝器：佔據整個寬度，並提供內邊距 -->
    <div class="main-content-wrapper flex-grow-1 p-4" style="overflow-y: auto;">
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
                <li v-for="(time) in generalHours" :key="time.id"
                  class="d-flex justify-content-between align-items-center py-2 border-bottom">
                  <span class="fw-bold text-capitalize">{{ time.dayName }}</span>
                  <div v-if="time.isOpen">
                    <span>{{ time.openTimeStr }} 至 {{ time.closeTimeStr }}</span>
                  </div>
                  <div v-else>
                    <span>未營業</span>
                  </div>

                </li>
              </ul>
            </div>
            <!-- 特殊營業時間區塊 -->
            <div class="col-12 col-md-6 mb-4">
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
                <i class="bi bi-calendar-plus" style="font-size: 4rem; color: #6c757d;"></i>
                <p class="text-muted mb-4">尚未設定的店休紀錄</p>
                <p class="text-muted mb-4">點擊新增建立特殊營業時間或店休日期。</p>
                <!-- <button class="btn btn-outline-primary  px-4 py-2 fw-bold" @click="openSpecialEditor">
                  <i class="bi bi-plus-circle me-2"></i>設定特殊營業時間
                </button> -->
              </div>
              <!-- 顯示特殊營業日 -->

              <div v-else class="special-hours-list-container">
                <div v-for="(record, index) in specialHoursRecords" :key="record.id"
                  class="card mb-3 shadow-sm rounded-3">
                  <div class="card-body d-flex justify-content-between align-items-center">
                    <div>

                      <p class="mb-0 small text-muted">
                        特殊日期：{{ record.date }}
                      </p>
                      <p class="mb-0 small text-muted">
                        <span v-if="record.isClose">營業時間：店休</span>
                        <span v-else>營業時間：{{ record.openTime }} 至 {{ record.closeTime }}</span>
                      </p>
                    </div>
                    <button class="btn btn-outline-danger btn-sm" @click="deleteSpecialHours(record.id)">刪除</button>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右側側邊欄 -->
    <div class="bg-white shadow-lg border-start position-fixed end-0 h-100 detail-sidebar"
      :class="{ 'show': isSidebarVisible }">
      <GeneralHoursEditor v-if="sidebarType === 'general'" :generalHours="generalHours" @close="closeSidebar"
        @save="handleSaveGeneralHours" />
      <SpecialHoursEditor v-if="sidebarType === 'special'" @close="closeSidebar" @save="handleSaveSpecialHours" />
    </div>

    <!-- 側邊欄疊加層 (點擊空白處關閉) -->
    <div class="overlay" :class="{ 'show': isSidebarVisible }" @click="closeSidebar"></div>

    <!-- 底部提示訊息（Toast / Snackbar 樣式） -->
    <transition name="fade">
      <div v-if="showNotification" :class="['notification-toast', 'bg-' + notificationType, 'text-white']">
        {{ notificationMessage }}
        <button type="button" class="btn-close btn-close-white ms-auto" @click="hideNotification"
          aria-label="Close"></button>
      </div>
    </transition>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.main-content-wrapper {
  overflow-y: auto;
  /*已經直接寫在 template inline style*/
  /* 如果你的佈局是 flex，flex-grow-1 會讓它填充可用空間 */
}


/* 自定義粉色邊框按鈕 */
.btn-pink-outline {
  color: #dc3545;
  /* 圖片中按鈕的顏色類似於 danger */
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
  max-width: 500px;
  /* 控制側邊欄最大寬度 */
  transform: translateX(100%);
  transition: transform 0.3s ease-in-out;
  z-index: 1050;
  /* 比 overlay 高 */
  top: 0;
  /* 確保從頂部開始 */
  height: 100vh !important;
  /* 確保側邊欄充滿整個視口高度 */
  overflow-y: auto;
  /* 允許側邊欄內容滾動 */
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
  z-index: 1040;
  /* 在側邊欄之下 */
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease-in-out, visibility 0.3s ease-in-out;
}

.overlay.show {
  opacity: 1;
  visibility: visible;
}

/* 確保側邊欄在移動端全寬 */
@media (max-width: 1199.98px) {

  /* xl breakpoint */
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
  z-index: 1100;
  /* 確保在最上層 */
  font-weight: bold;
}

/* 過渡動畫 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(20px) translateX(-50%);
}

.fade-enter-to,
.fade-leave-from {
  opacity: 1;
  transform: translateY(0) translateX(-50%);
}

/**特殊營業日的滾動條 */
.special-hours-list-container {
  max-height: 320px;
  /* 設定一個最大高度，您可以根據需求調整 */
  overflow-y: auto;
  /* 當內容超出 max-height 時顯示垂直滾輪 */
  padding-right: 15px;
  /* 為滾輪留出空間，避免內容被遮擋 */
}

/* 為了讓滾動條更好看 (可選) */
.special-hours-list-container::-webkit-scrollbar {
  width: 8px;
}

.special-hours-list-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.special-hours-list-container::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 10px;
}

.special-hours-list-container::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
