<script setup>
import { ref } from 'vue';

const props = defineProps({
  orders: {
    type: Array,
    required: true,
  },
});

const emit = defineEmits(['select-order']);

const selectOrder = (order) => {
  emit('select-order', order);
};




</script>

<template>
  <div class="order-list-wrapper">
    <div class="table-responsive">
      <table class="table table-hover table-borderless align-middle text-nowrap">
        <thead>
          <tr>
            <th scope="col" class="text-muted text-uppercase">訂單 ID</th>
            <th scope="col" class="text-muted text-uppercase">取餐時間</th>
            <th scope="col" class="text-muted text-uppercase">交易方式</th>
            <th scope="col" class="text-muted text-uppercase">客戶名稱</th>
            <th scope="col" class="text-muted text-uppercase">訂單狀態</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in orders" :key="order.id" @click="selectOrder(order)" class="cursor-pointer border-bottom rounded-3 overflow-hidden">
            <td>{{ order.id }}</td> 
            <td>{{ order.pickupTime }}</td>
            <td>{{ order.method }}</td>
            <td>{{ order.user.name }} </td>
            <td>{{ order.status }}</td>
          </tr> 
        
          <tr v-if="orders.length === 0">
            <td colspan="5" class="text-center py-4 text-muted">目前沒有訂單。</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}

/* 調整後的包裹層樣式 */
.order-list-wrapper {
  /* 移除 max-width 和 margin: auto; 讓它佔滿父容器可用空間 */
  padding: 1.5rem 1rem; /* 上下內邊距 1.5rem，左右內邊距 1rem */
  box-sizing: border-box; /* 確保 padding 不會導致溢出 */
}

/* 為行添加圓角和陰影效果 */
tbody tr {
  transition: background-color 0.2s ease, transform 0.2s ease;
  border-radius: 8px; /* 圆角 */
  overflow: hidden; /* 確保內容不會溢出圓角 */
}

tbody tr:hover {
  background-color: #f8f9fa; /* 懸停背景色 */
  transform: scale(0.99); /* 懸停時輕微放大 */
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* 懸停時添加陰影 */
}

/* 確保表格头部和單元格對齊 */
.table th, .table td {
  padding: 1rem 1rem; /* 調整內邊距 */
  border-color: #eee; /* 輕微邊框顏色 */
}

.table thead th {
  border-bottom: 2px solid #e9ecef; /* 头部底边框 */
}

/* 隱藏最後一行底部邊框，使卡片感覺更強 */
tbody tr:last-child {
  border-bottom: none !important;
}

/* 調整表格背景，使其融入卡片背景 */
.table {
  background-color: transparent;
}

/* 媒體查詢：現在這個媒體查詢變得不那麼重要了，因為預設就佔滿寬度 */
/* 但如果你希望在小螢幕上有不同的 padding，可以保留或調整 */
@media (max-width: 767.98px) { /* Bootstrap sm breakpoint */
  .order-list-wrapper {
    /* 這裡可以調整小螢幕下的 padding，例如更小的左右邊距 */
    padding-left: 0.75rem;
    padding-right: 0.75rem;
  }
}
</style>