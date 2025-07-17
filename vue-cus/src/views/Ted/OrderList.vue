<!-- 歷史訂單 -->
<script setup>
import { ref ,onMounted} from 'vue';
import axios from '@/plungins/axios.js';
import RatingModal from '@/components/Ted/ReviewModal.vue';
import { useUserStore } from '@/stores/user.js'; // 引入 Pinia userStore
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';

const orders=ref([])
const id = ref(1)
const userStore = useUserStore(); // 實例化 userStore
const router = useRouter();
const userId = ref(null); // 用於存儲從 Pinia 獲取的用戶 ID

// 格式化日期時間顯示
const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return '';
    const date = new Date(dateTimeString);
    return date.toLocaleString(); // 根據用戶本地設置格式化日期時間
};



/**
 * 從後端獲取用戶訂單列表
 * @param {number} id - 用戶 ID
 */
 async function fetchOrders(id) {
  try {
    const response = await axios.get(`/api/orders/user/${id}`);
    orders.value = response.data.map(order => {
      order.comment = order.comment || null;
      if (order.orderDetails) {
        order.orderDetails = order.orderDetails.map(detail => {
          detail.likedFood = detail.likedFood || null;
          // 確保 detail.food 存在，如果不存在則給一個預設的空物件
          detail.food = detail.food || { name: '未知餐點' }; // 或者你希望的預設值
          return detail;
        });
      }
      return order;
    });
    console.log("訂單數據加載成功:", orders.value);
  } catch (error) {
    console.error("加載訂單失敗:", error);
  }
}

// 監聽 RatingModal 發出的更新事件，然後重新獲取訂單數據
const handleRatingUpdated = () => {
    console.log("收到 RatingModal 的更新事件，重新加載訂單...");
    if (userId.value) {
        fetchOrders(userId.value); // 重新呼叫 fetchOrders 刷新數據
    }
};

onMounted(() => {
  // 獲取用戶 ID 從 Pinia
  userId.value = userStore.userId; // 假設您的 Pinia store 中有 userId 屬性
  if (userId.value) {
    // 改為呼叫 fetchOrders，它包含了對 comment 和 likedFood 的預設值處理
    fetchOrders(userId.value);
  } else {
    console.warn("用戶 ID 未定義，無法加載訂單。請確保用戶已登入。");
    // 您可以導向登入頁面或顯示提示
  }
});
function findorder(id) {
  axios.get(`/api/orders/user/${id}`)
    .then(function (response) {

      console.log("訂單數據:", response.data);
      orders.value = response.data
      console.log(orders.value)
      // response.data.forEach(element => {
      //   console.log(element)
      //   orders.value.push(element);
      // });
      // console.log(`ordersValue:${orders.value}`)
  }).catch(function (error) {
    console.error("加載訂單失敗:", error);
  })

}

// 重新訂購功能
const reorder = (order) => {
  Swal.fire({
    icon: 'info',
    title: '重新訂購',
    text: `您選擇重新訂購：${order.store.name} 的訂單。`, // 使用 order.store.name 獲取店家名稱
    confirmButtonText: '確定'
  });
};

//頁面跳轉 點擊訂單詳情後跳轉
const goToOrderDetail = (orderId) => {
  router.push({ name: 'OrderDetail', params: { id: orderId } });
};

</script>

<template>
  <div class="order-history-container">
    <h4 class="mb-4 text-center">
      <strong>歷史訂單</strong>
    </h4>
    <div
      v-for="order in orders"
      :key="order.id"
      class="order-item-card d-flex align-items-start p-3 mb-3 rounded-lg shadow-sm"
      @click="goToOrderDetail(order.id)" 
    >
      <img
        :src="order.store.photo"
        alt="店家圖片"
        class="me-3 rounded-circle border border-light"
        style="width: 70px; height: 70px; object-fit: cover;"
      >
      <div class="flex-grow-1">
        <div class="d-flex w-100 justify-content-between align-items-center mb-2">
          <h5 class="mb-0 text-primary">
            {{ order.store.name }}<!--店家名稱-->
          </h5>
          <h4 class="mb-0 text-danger fw-bold">
            ${{ order.total }}<!--訂單總價-->
          </h4>
        </div>
        <p class="mb-2 text-muted small">
          訂購時間: {{ formatDateTime(order.createTime) }}
        </p>

        <div class="mb-3">
          <p v-for="detail in order.orderDetails" :key="detail.id" class="mb-1 fw-medium">
            <span class="text-dark">{{ detail.food.name }}  x  {{ detail.quantity }}</span>
            <!-- <span v-if="food.spec" class="text-secondary small"> ({{  }})</span> -->
          </p>
        </div>

        <div class="d-flex justify-content-end align-items-center mt-3">
          <button class="btn btn-outline-danger btn-sm rounded-pill px-3" @click.stop="reorder(order)">
            選擇想要重新訂購的項目
          </button>
        </div>

        <div >
          <RatingModal :order="order" @ratingUpdated="handleRatingUpdated" />
        </div>
      </div>
    </div>
  </div>
</template>


<style scoped>
/* 外部容器，保持與 RestaurantTemplate 中 tab-menu-container 相似的寬度限制 */
.order-history-container {
  max-width: 600px; /* 與 tab-menu-container 保持一致的 max-width */
  margin: 2rem auto; /* 水平居中，上下留一些空間 */
  padding: 0 1rem; /* 左右內邊距，防止內容貼邊 */
}

/* 訂單卡片樣式 */
.order-item-card {
  background-color: #fff; /* 使用白色背景 */
  border: 1px solid #e0e0e0; /* 淺灰色邊框 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08); /* 柔和的陰影 */
  transition: transform 0.2s ease, box-shadow 0.2s ease; /* 添加過渡效果 */
}


/* 圓角類，Bootstrap 預設的 rounded-lg 已經很不錯 */
.rounded-lg {
  border-radius: 0.5rem !important; /* 確保圓角較明顯 */
}

/* 圖片圓形 */
.rounded-circle {
  border-radius: 50% !important;
}

/* 重新訂購按鈕樣式 */
.btn-outline-danger {
  color: var(--bs-danger);
  border-color: var(--bs-danger);
}

.btn-outline-danger:hover {
  background-color: var(--bs-danger);
  color: #fff;
}

/* 覆蓋部分 Bootstrap 文本顏色 */
.text-primary {
  color: #333 !important; /* 可以根據你的主題調整主要文本顏色 */
}
</style>