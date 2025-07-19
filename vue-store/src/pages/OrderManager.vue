<script setup>
import { ref, computed, onMounted } from 'vue';
import PageHeader from '../components/common/PageHeader.vue'; //導入頁面標頭公版
import SlideOutPanel from '../components/common/SlideOutPanel.vue'; //導入側邊欄公版
import OrderList from '../components/order/OrderList.vue';
import OrderDetail from '../components/order/OrderDetail.vue';

import axios from '@/plungins/axios.js';
import { useOrderNotifier } from '../composables/useOrderNotifier.js'; // 匯入 Composable


// 訂單列表資料 (模擬資料)
const orders = ref([]);
const selectedOrder = ref(null);
const isDetailSidebarVisible = ref(false); // 控制側邊欄可見性

const storeId =4  
// 使用 Composables，它會自動處理 WebSocket 連線和訂閱
// 從後端獲得資料
function fetchOrders (storeId)  {
  
  axios.get(`/api/orders/store/${storeId}`).then(function(response) {
    // console.log(response.data)
    orders.value=response.data
    console.log('orders',orders.value)
  }
).catch(function(error){
  console.log(error)
})

};
// 需要取得storeId和方法
const { isConnected } = useOrderNotifier(storeId, fetchOrders); 

// 組件首次載入時，也執行一次完整的訂單獲取，以確保數據是最新且完整的
onMounted(() => {
  fetchOrders(storeId);
});

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

// 模擬"重設"功能 (重置篩選/排序)
const resetFilters = () => {
  // 在這裡實現重置邏輯，例如重新載入原始訂單數據
  alert('重設功能待實現');
};

const updateStatus=(orderId, status) =>{
    const body = {
    id: orderId,
    status
  }
  axios.put(`/api/orders/status/${orderId}`,body)
    .then(function (response) {
    console.log('修改編號的訂單', orderId)
    console.log('修改編號的狀態', status)

  }).catch(function (error) {
    console.log(error)
  })

}

//取消訂單
const cancelOrder = (orderId) => {
  console.log('取消編號的訂單',orderId)
  updateStatus(orderId, 'cancelled')
}
const confirmOrder = (orderId) => {
  console.log('確認編號的訂單',orderId)
  updateStatus(orderId, 'perparing')
  
}
const completeOrder = (orderId) => {
  console.log('完成編號的訂單',orderId)
  updateStatus(orderId, 'completed')
}
</script>

<template>
<!-- 移除舊有外層佈局 div，只保留頁面自身需要的結構 -->
  <!-- 為整個頁面組件提供一個根元素，用 d-flex 和 h-100 來撐開佈局 -->
  <div class="d-flex flex-column h-100">
    <PageHeader title="訂單管理">
      <template #right-side>
        <div class="d-flex align-items-center">
          <h5 class="mb-0 text-muted me-3">找到 {{ totalOrders }} 項結果</h5>
          <span :class="{'text-success': isConnected, 'text-danger': !isConnected}" class="me-3">
            {{ isConnected ? '● 線上' : '● 離線' }}
          </span>
          <button class="btn btn-outline-secondary btn-sm rounded-pill px-3" @click="resetFilters">重設</button>
        </div>
      </template>
    </PageHeader>

    <div class="order-list-panel">
      <OrderList 
      :orders="orders" 
      @select-order="handleOrderSelected" 
      />
    </div>
    <!-- 左側訂單列表 -->

    <!-- 3. 使用新的 SlideOutPanel 組件 -->
    <!-- 測邊列 -->
    <SlideOutPanel 
      v-model:isOpen="isDetailSidebarVisible" 
      title="訂單詳情"
      @close="closeDetailSidebar"
    >
      <!-- 面板的主體內容放在預設插槽中 -->
      <OrderDetail 
        v-if="selectedOrder" 
        :order="selectedOrder" 
        @close-Sidebar="closeDetailSidebar"
        @cancel-order="cancelOrder"
        @confirm-order="confirmOrder"
        @complete-order="completeOrder"
      />
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
  position: relative; /* 重點：作為定位父層 */
}

/* 內容區塊 */
.content-wrapper {
  flex-grow: 1; /* 佔滿剩餘的垂直空間 */
  position: relative; /* 加上 relative，以防萬一內部有需要定位的元素 */
  display: flex; /* 為了讓 .order-list-panel 撐開，還是需要 */
}

/* 訂單列表面板 */
.order-list-panel {
  flex-grow: 1;   
  overflow-y: auto; /* 讓列表自己滾動 */
  background-color: white;
  box-shadow: 0 .125rem .25rem rgba(0,0,0,.075);
  border-radius: .25rem;
}

/* 行動端適應 */
@media (max-width: 1199.98px) { /* xl breakpoint */
  .detail-sidebar {
    position: fixed; /* 在行動端使用 fixed */
    max-width: 100%; /* 行動端全寬 */
  }
}
</style>
