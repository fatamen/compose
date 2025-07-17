<script setup>
import { computed, watch, nextTick } from 'vue';
import { Collapse } from 'bootstrap'; // 導入 Bootstrap 的 Collapse 模組
import Swal from 'sweetalert2';
import axios from '@/plungins/axios.js';

const props = defineProps({
  order: {
    type: Object,
    default: null,
  },
});

const emit = defineEmits(['cancel-order','close-Sidebar','confirm-order','complete-order']);



// 新增的取消訂單方法
const cancelOrder = () => {
  // 這裡可以添加確認對話框，例如使用自定義模態框，而不是 alert
  Swal.fire({
  title: "確定取消訂單?",
  text: "取消後無法修改狀態",
  icon: "warning",
  showCancelButton: true,
  confirmButtonColor: "#d33" ,
  cancelButtonColor: "#3085d6",
  confirmButtonText: "取消",
  cancelButtonText: "再考慮",
  allowOutsideClick: false // 禁止点击外部区域关闭
  }).then((result) => {
    if (result.isConfirmed) {
      emit('cancel-order', props.order.id); // 發送事件到父組件，傳遞訂單ID
      Swal.fire({
        title: "取消成功",
        text: "訂單已取消",
        icon: "success",
        timer: 1200 
      }).then((timerResult) => {
        // 這個 .then() 會在 timer 結束後觸發
        if (timerResult.dismiss === Swal.DismissReason.timer||timerResult) {
          emit('close-Sidebar') // 計時器結束後，關閉詳情面板
        }


      })
    }
});
  
  
  
};

const confirmOrder = () => {
  emit('confirm-order', props.order.id)
 
};
const completeOrder = () => {

  emit('complete-order', props.order.id)
};

// 監聽 order 屬性的變化，用於手動控制手風琴狀態
watch(() => props.order, (newOrder) => {
  // 只有當 order 存在時才嘗試處理手風琴狀態
  if (newOrder) {
    // nextTick 確保 DOM 已經更新並渲染了手風琴元素
    nextTick(() => {
      const accordionElement = document.getElementById('flush-collapseOne');
      if (accordionElement) {
        const bsCollapse = Collapse.getInstance(accordionElement);
        // 如果手風琴實例已經存在，我們強制隱藏它。
        // 如果不存在，Bootstrap 的 data-bs-toggle 屬性會在使用者第一次點擊時自動創建並管理它。
        if (bsCollapse) {
          bsCollapse.hide(); // 隱藏（折疊）手風琴
        }
        // 重要：這裡不再進行 else { new Collapse(...) }
        // 我們完全依賴 Bootstrap 的 data-bs-toggle 來在使用者點擊時創建和管理實例。
      }
    });
  }
}, { immediate: true }); // immediate: true 確保在組件初次掛載時也會執行一次
</script>

<template>
  <div v-if="order" class="h-100 d-flex flex-column p-4">


    <div class="flex-grow-1 overflow-auto pe-2">
      <div class="d-flex align-items-center mb-4">
        <div class="rounded-circle bg-warning text-white d-flex align-items-center justify-content-center me-2" style="width: 40px; height: 40px;">
          <i class="fas fa-user"></i>
        </div>
        <div>
          <p class="mb-0 fw-bold">{{ order.user.name }}</p>
          <!-- <p class="mb-0 text-muted small">{{ order.timeline.length }} 份訂單</p> -->
        </div>
      </div>

      <hr>

      <h5 class="mb-3">訂單 {{ order.id }}</h5>
      <p class="text-muted small mb-4">取餐時間：{{ order.pickupTime }}</p>

      <div v-for="item in order.orderDetails" :key="item.name" class="d-flex justify-content-between align-items-start mb-3 border-bottom pb-2">
        <div class="d-flex align-items-baseline">
          <span class="badge bg-secondary rounded-pill me-2">{{ item.quantity }}</span>
          <div>
            <p class="mb-0 fw-bold">{{ item.food.name }}</p>
            <!-- <p v-if="item.note" class="mb-0 text-muted small">規格: {{ item.note }}</p> -->
          </div>
        </div>
        <p class="mb-0 fw-bold text-end">NT$ {{ item.price }}</p>
      </div>
      <!-- v-if="order.note" -->
      <div  class="mt-auto pt-3 border-top mb-3 p-3 bg-light rounded">
        <h6 class="mb-2 fw-bold">備註:</h6>
        <p class="mb-0 text-muted small">麻煩多給我一副餐具{{ order.content }}</p>
      </div>

    </div>

    <div class="mt-auto pt-3 border-top">
      <div class="d-flex justify-content-between mb-2">
        <span class="fw-bold">總計 ({{ order.orderDetails.length }} 項)</span>
        <span class="fw-bold">NT$ {{ order.total }}</span>
      </div>
      <div class="d-flex gap-2"> 
        <button v-if="order.status!='已取消' && order.status!='已完成' " class="btn btn-outline-danger w-100 py-3 rounded-pill fw-bold" @click="cancelOrder">取消訂單</button>
        <button v-if="order.status=='待確認'" class="btn btn-outline-primary w-100 py-3 rounded-pill fw-bold" @click="confirmOrder">確認訂單
        </button>
        <button v-if="order.status=='處理中'" class="btn btn-outline-primary w-100 py-3 rounded-pill fw-bold" @click="completeOrder">完成訂單，代取餐
        </button>
      </div>
    </div>
  </div>
  <div v-else class="h-100 d-flex flex-column justify-content-center align-items-center text-muted">
    <p>請從左側列表選擇一個訂單。</p>
  </div>
</template>

<style scoped>
/* 側邊欄整體內邊距和圓角 */
.h-100.d-flex.flex-column {
  background-color: white; /* 確保背景是白色 */
}

.btn-close {
  font-size: 1.25rem;
}
.my-swal-popup {
  z-index: 1060 !important;
}
.my-swal-backdrop {
  z-index: 1055 !important;
}


/* 時間線段 satart */
.timeline-list {
  position: relative;
  padding-left: 20px; /* 為垂直線和點留出空間 */
}

.timeline-list li {
  position: relative;
  margin-bottom: 15px;
  padding-left: 25px; /* 為點和內容之間留出空間 */
}

.timeline-list li:last-child {
  margin-bottom: 0;
}

.timeline-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 5px;
  width: 10px;
  height: 10px;
  background-color: #ccc; /* 預設點顏色 */
  border-radius: 50%;
  z-index: 1;
}
/* 如果是最後一個li項目 */
.timeline-list li.active::before {
  background-color: #28a745; /* 活動點的顏色 */
  box-shadow: 0 0 0 3px rgba(40, 167, 69, 0.3); /* 活動點光暈 */
}

.timeline-list::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 10px;
  bottom: 0;
  width: 2px;
  background-color: #eee; /* 垂直線顏色 */
}

.timeline-list li:last-child::after {
  height: 0; /* 最後一個點後面沒有線 */
}

.timeline-time {
  display: block;
  font-size: 0.85rem;
  color: #6c757d;
  margin-bottom: 3px;
}

.timeline-description {
  display: block;
  font-size: 0.95rem;
  font-weight: 500;
  color: #343a40;
}

/* 時間線end */


/* 訂單項目列表滾動條美化 */
/* 這裡的樣式現在將應用於整個可滾動區域 */
.flex-grow-1.overflow-auto {
  scrollbar-width: thin; /* Firefox */
  scrollbar-color: #ccc #f1f1f1; /* Firefox */
}

.flex-grow-1.overflow-auto::-webkit-scrollbar {
  width: 8px;
}

.flex-grow-1.overflow-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.flex-grow-1.overflow-auto::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}

.flex-grow-1.overflow-auto::-webkit-scrollbar-thumb:hover {
  background: #bbb;
}
</style>