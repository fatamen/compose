<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import PageHeader from '../components/common/PageHeader.vue'; //導入頁面標頭公版
import SlideOutPanel from '../components/common/SlideOutPanel.vue'; //導入側邊欄公版
import OrderList from '../components/order/OrderList.vue';
import OrderDetail from '../components/order/OrderDetail.vue';

import axios from '@/plungins/axios.js';
import { useOrderNotifier } from '../composables/useOrderNotifier.js'; // 匯入 Composable
import { useStore } from '../composables/useStore.js'; // 🔥 NEW: 導入共享的 store

// 🔥 NEW: 使用共享的 store 狀態
const { selectedStore, currentStoreName, isLoggedIn } = useStore()

// 訂單列表資料
const orders = ref([]);
const selectedOrder = ref(null);
const isDetailSidebarVisible = ref(false); // 控制側邊欄可見性
const isLoading = ref(false);

// const storeId =4  
// // 使用 Composables，它會自動處理 WebSocket 連線和訂閱
// // 從後端獲得資料
// function fetchOrders (storeId)  {

//   axios.get(`/api/orders/store/${storeId}`).then(function(response) {
//     // console.log(response.data)
//     orders.value=response.data
//     console.log('orders',orders.value)
//   }
// ).catch(function(error){
//   console.log(error)
// })

// };
// // 需要取得storeId和方法
// const { isConnected } = useOrderNotifier(storeId, fetchOrders); 

// 🔥 NEW: 動態的 fetchOrders 函數
function fetchOrders(storeId) {
  if (!storeId) {
    console.warn('⚠️ [OrderManager] 沒有提供 storeId')
    orders.value = []
    return
  }

  console.log(`🚀 [OrderManager] 載入店家 ${storeId} 的訂單資料`)
  isLoading.value = true

  axios.get(`/api/orders/store/${storeId}`)
    .then(function (response) {
      orders.value = response.data
      console.log('✅ [OrderManager] orders:', orders.value)
    })
    .catch(function (error) {
      console.error('❌ [OrderManager] 載入訂單失敗:', error)
      orders.value = []
    })
    .finally(() => {
      isLoading.value = false
    })
}

// 🔥 NEW: 監聽 selectedStore 變化
watch(selectedStore, (newStoreId, oldStoreId) => {
  console.log(`👀 [OrderManager] selectedStore 變化: ${oldStoreId} → ${newStoreId}`)
  if (newStoreId && newStoreId !== oldStoreId) {
    fetchOrders(newStoreId)
  }
}, { immediate: true }) // immediate: true 表示立即執行一次

// 🔥 NEW: WebSocket 連線也需要動態 storeId
let notifierCleanup = null

const setupOrderNotifier = (storeId) => {
  // 清理舊的連線
  if (notifierCleanup) {
    notifierCleanup()
    notifierCleanup = null
  }

  if (storeId) {
    console.log(`🔌 [OrderManager] 建立 WebSocket 連線 for store ${storeId}`)
    const { isConnected, cleanup } = useOrderNotifier(storeId, fetchOrders)
    notifierCleanup = cleanup
    return isConnected
  }

  return ref(false)
}

// 動態設定 WebSocket 連線
const isConnected = ref(false)
watch(selectedStore, (newStoreId) => {
  if (newStoreId) {
    const connected = setupOrderNotifier(newStoreId)
    isConnected.value = connected.value
    watch(connected, (val) => { isConnected.value = val })
  }
}, { immediate: true })

// 組件首次載入
onMounted(() => {
  console.log('🎬 [OrderManager] 組件掛載')

  // 如果已經有選中的店家，立即載入
  if (selectedStore.value) {
    fetchOrders(selectedStore.value)
  }
});

// 清理事件監聽器
onBeforeUnmount(() => {
  console.log('🧹 [OrderManager] 組件卸載，清理資源')

  if (notifierCleanup) {
    notifierCleanup()
  }
})

// 計算屬性：訂單總數
const totalOrders = computed(() => orders.value.length);

// 處理選中訂單
const handleOrderSelected = (order) => {
  selectedOrder.value = order;
  isDetailSidebarVisible.value = true; // 顯示側邊欄
};

// 關閉訂單詳情側邊欄
const closeDetailSidebar = () => {
  console.log('關閉側邊')
  isDetailSidebarVisible.value = false;
  // selectedOrder 會在面板關閉後再清空，體驗更好
  setTimeout(() => {
    if (!isDetailSidebarVisible.value) {
      selectedOrder.value = null;
    }
  }, 300); // 300ms 是我們動畫的時間
};

// // 模擬"重設"功能 (重置篩選/排序)
// const resetFilters = () => {
//   // 在這裡實現重置邏輯，例如重新載入原始訂單數據
//   alert('重設功能待實現');
// };

// const updateStatus=(orderId, status) =>{
//     const body = {
//     id: orderId,
//     status
//   }
//   axios.put(`/api/orders/status/${orderId}`,body)
//     .then(function (response) {
//     console.log('修改編號的訂單', orderId)
//     console.log('修改編號的狀態', status)

//   }).catch(function (error) {
//     console.log(error)
//   })

// }

// 重設功能
const resetFilters = () => {
  if (selectedStore.value) {
    fetchOrders(selectedStore.value)
  } else {
    alert('請先選擇店家')
  }
};

// 🔥 修改：訂單狀態更新後重新載入當前店家的訂單
const updateStatus = (orderId, status) => {
  const body = {
    id: orderId,
    status
  }
  axios.put(`/api/orders/status/${orderId}`, body)
    .then(function (response) {
      console.log('修改編號的訂單', orderId)
      console.log('修改編號的狀態', status)
      // 🔥 更新狀態後重新載入當前店家的訂單
      if (selectedStore.value) {
        fetchOrders(selectedStore.value)
      }
    }).catch(function (error) {
      console.log(error)
    })
}

//取消訂單
const cancelOrder = (orderId) => {
  console.log('取消編號的訂單', orderId)
  updateStatus(orderId, 'cancelled')
}
const confirmOrder = (orderId) => {
  console.log('確認編號的訂單', orderId)
  updateStatus(orderId, 'perparing')

}
const completeOrder = (orderId) => {
  console.log('完成編號的訂單', orderId)
  updateStatus(orderId, 'completed')
}
</script>

<template>
  <!-- 移除舊有外層佈局 div，只保留頁面自身需要的結構 -->
  <!-- 為整個頁面組件提供一個根元素，用 d-flex 和 h-100 來撐開佈局 -->
  <div class="d-flex flex-column h-100">
    <PageHeader title="訂單管理">
      <!-- 🔥 NEW: 在 actions slot 中顯示當前店家 -->
      <template #actions>
        <div v-if="currentStoreName" class="badge bg-primary fs-6">
          🏪 {{ currentStoreName }}
        </div>
        <div v-else-if="!isLoggedIn" class="badge bg-warning fs-6">
          ⚠️ 未登入
        </div>
        <div v-else class="badge bg-secondary fs-6">
          📍 請選擇店家
        </div>
      </template>

      <template #right-side>
        <div class="d-flex align-items-center">
          <!-- 🔥 NEW: 載入狀態 -->
          <div v-if="isLoading" class="me-3">
            <div class="spinner-border spinner-border-sm text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
            <span class="ms-2 text-muted">載入中...</span>
          </div>

          <h5 v-else class="mb-0 text-muted me-3">找到 {{ totalOrders }} 項結果</h5>

          <span :class="{ 'text-success': isConnected, 'text-danger': !isConnected }" class="me-3">
            {{ isConnected ? '● 線上' : '● 離線' }}
          </span>
          <button class="btn btn-outline-secondary btn-sm rounded-pill px-3" @click="resetFilters">重設</button>
        </div>
      </template>
    </PageHeader>

    <!-- 沒有選中店家時的提示 -->
    <div v-if="!selectedStore" class="alert alert-info mx-3">
      <h5>📋 請選擇店家</h5>
      <p class="mb-0">請在左側邊欄選擇要管理的店家，然後就能查看該店家的訂單資料。</p>
    </div>

    <!-- 訂單列表 -->
    <div v-else class="order-list-panel"> <!-- 🔥 加上 v-else -->
      <OrderList :orders="orders" @select-order="handleOrderSelected" />
    </div>

    <!-- 訂單詳情側邊欄 -->
    <SlideOutPanel v-model:isOpen="isDetailSidebarVisible" title="訂單詳情" @close="closeDetailSidebar">
      <!-- 面板的主體內容放在預設插槽中 -->
      <OrderDetail v-if="selectedOrder" :order="selectedOrder" @close-Sidebar="closeDetailSidebar"
        @cancel-order="cancelOrder" @confirm-order="confirmOrder" @complete-order="completeOrder" />
      <!--  注意：OrderDetail 內部的關閉按鈕，我們不再需要監聽它的 @close-detail 事件了，
            因為 SlideOutPanel 自己的關閉按鈕和遮罩點擊會處理關閉邏輯。
            當然，保留它也沒問題，可以提供多一種關閉方式。
      -->
    </SlideOutPanel>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

/* 頁面根元素，設定 relative 來作為定位父層 */
.order-manager-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
  /* 重點：作為定位父層 */
}

/* 內容區塊 */
.content-wrapper {
  flex-grow: 1;
  /* 佔滿剩餘的垂直空間 */
  position: relative;
  /* 加上 relative，以防萬一內部有需要定位的元素 */
  display: flex;
  /* 為了讓 .order-list-panel 撐開，還是需要 */
}

/* 訂單列表面板 */
.order-list-panel {
  flex-grow: 1;
  overflow-y: auto;
  /* 讓列表自己滾動 */
  background-color: white;
  box-shadow: 0 .125rem .25rem rgba(0, 0, 0, .075);
  border-radius: .25rem;
}

/* 行動端適應 */
@media (max-width: 1199.98px) {

  /* xl breakpoint */
  .detail-sidebar {
    position: fixed;
    /* 在行動端使用 fixed */
    max-width: 100%;
    /* 行動端全寬 */
  }
}

/* 🔥 NEW: 載入狀態樣式 */
.spinner-border-sm {
  width: 1rem;
  height: 1rem;
}

.badge {
  font-size: 0.85rem !important;
}
</style>
