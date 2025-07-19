<!-- 歷史訂單點進去後的詳細 -->

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from '@/plungins/axios.js';
import { useCartStore } from '@/stores/cart'; // 引入 Pinia 購物車 Store
// 引入 Bootstrap 5 CSS
import 'bootstrap/dist/css/bootstrap.min.css';
import Swal from 'sweetalert2';
// 引入 Bootstrap 5 JS (可選，這裡主要用於佈局，JS 功能如彈窗等可能需要完整引入)
// import 'bootstrap/dist/js/bootstrap.bundle.min.js';
// 獲取購物車 Store 實例
const cartStore = useCartStore();



// 重新訂購按鈕點擊事件
const handleReorderClick = () => { // 將名稱從 reorder 改為 handleReorderClick，避免與 store 的 reorder 混淆
  console.log('重新訂購按鈕被點擊');
    if (order.value && order.value.orderDetails && order.value.orderDetails.length > 0) {
         // 呼叫購物車 Store 的 reorder 函式，並傳入當前訂單數據
        Swal.fire(
                '已加入購物車！', // 標題
                `${order.value.store.name} 的訂單已加入您的購物車。`, // 內文
                'success' // 圖標：success, error, warning, info, question
            ).then(() => {
              cartStore.reorder(order.value);
            })
            ; // 給用戶一個提示
    } else {
         Swal.fire({
            icon: 'warning', // 使用警告圖標
            title: '無法重新訂購',
            text: '該訂單沒有有效的店家或商品信息，無法加入購物車。',
            confirmButtonText: '確定'
        });
    }
};


const route = useRoute();
// 訂單數據
const order = ref({});

// 計算總計



const findOrderById = (id) => {
  axios.get(`/api/orders/${id}`)
    .then((res) => {
      if (res.status === 200 && res.data) {
        order.value = res.data;
        console.log('訂單內容載入成功:', order.value);
      } else {
        // 理論上，如果後端嚴格返回 404，這裡不會被觸發
        // 但以防萬一，處理非 200 但成功的響應（例如後端返回 200 空響應）
        console.log('訂單載入但無內容或狀態非 200:', res.status);
        order.value = {}; // 設置為空物件，避免 TypeError
      }
    })
    .catch((err) => {
      console.error('獲取訂單詳情錯誤:', err);
      // --- 關鍵修復：在錯誤發生時，明確處理 order 的狀態 ---
      if (err.response && err.response.status === 404) {
        console.log('訂單不存在 (404 Not Found)');
        order.value = {}; // 設置為空物件，表示沒有找到訂單
        // 你也可以在此處設置一個錯誤訊息，顯示在模板中
        // errorMessage.value = '您要查詢的訂單不存在。';
      } else {
        console.log('其他錯誤發生，無法載入訂單。');
        order.value = {}; // 設置為空物件，避免渲染錯誤
        // errorMessage.value = '載入訂單時發生未知錯誤。';
      }
    });
};

onMounted(() => {
  // 在組件掛載後執行的邏輯
  // console.log('取得id',props.restaurantId   )
  console.log('取得id2',route.params.id   )
  findOrderById(route.params.id)
})

</script>

 <template>
  <div class="container-fluid py-4" style="background-color: #f5f5f5; min-height: 100vh;">
    <!-- 導航欄 - 簡化，僅為示意 -->
  

    <div class="container">
      <div class="row">
        <!-- 左側內容區 -->
         <h2 class="mb-4 fw-bold">訂單詳情</h2>
        <div class="col-lg-8">
          <div class="card shadow-sm mb-4 rounded-lg">
            <div class="card-body">
              <h5 class="card-title fw-bold"  v-if="order.status !== 'completed' && order.status !== 'canceled'">最新訂購</h5>
              <h5 class="card-title fw-bold"  v-else>訂單記錄</h5>
              <p class="text-muted small">
                
                <a href="#" class="text-decoration-none" style="color: #e20261;">歷史訂單</a> &gt; <a href="#" class="text-decoration-none" style="color: #e20261;">訂單詳情</a>
              </p>

              <div class="d-flex align-items-center mb-3">
                <div class="me-3">
                  <router-link :to="`/restaurant/${ order.store?.id }`">
                    <img :src="order.store?.photo" :alt="order.stor?.name" class="rounded-circle border" style="width: 70px; height: 70px; object-fit: cover;">
                  </router-link>
                </div>
                <div>
                  
                  <h5 class="mb-0 fw-bold">{{ order.store?.name }}</h5>
                  <p class="mb-0 text-muted small">取餐於 {{ order.store?.address }}</p>
                  <!-- <p class="mb-0 text-muted small">{{ order.restaurant.orderId }}</p> -->
                </div>
              </div>

              <div class="mb-3">
                <div class="d-flex align-items-center mb-2">
                  <i class="bi bi-geo-alt-fill me-2" style="color: #e20261;"></i>
                  <span class="fw-bold">訂購於</span>
                </div>
                <p class="ms-4 mb-0 text-muted">{{ order.pickupTime }}</p>
              </div>
            </div>
          </div>

          <div class="card shadow-sm mb-4 rounded-lg">
            <div class="card-body">
              <h5 class="card-title fw-bold">訂單</h5>
              <div v-for="(item, index) in order.orderDetails" :key="index" class="d-flex align-items-center mb-3 pb-3 border-bottom">
                <div class="me-3">
                  <img :src="item.food.image" :alt="item.food.name" class="rounded-circle" style="width: 50px; height: 50px; object-fit: cover;">
                </div>
                <div class="flex-grow-1">
                  <p class="mb-0 fw-bold">{{ item.food.name }}</p>
                  <!-- <p class="mb-0 text-muted small">{{ item.description }}</p> -->
                </div>
                <span class="fw-bold text-end">{{ item.quantity }}&nbsp;x&nbsp;</span>
                <span class="fw-bold text-end">&nbsp;NT$ {{ item.price }}</span>
              </div>

    

            
              <div class="d-flex justify-content-between mb-3">
                <h5 class="fw-bold mb-0">總計</h5>
                <h5 class="fw-bold mb-0">NT$ {{ order.total }}</h5>
              </div>
            </div>
          </div>

          <div class="card shadow-sm mb-4 rounded-lg">
            <div class="card-body">
              <h5 class="card-title fw-bold">付款方式</h5>
              <div class="d-flex justify-content-between mb-2">
                <span>信用卡</span>
                <!-- <span>NT$ {{ order.payment.creditCard.toFixed(0) }}</span> -->
              </div>
             
            </div>
          </div>
        </div>

        <!-- 右側內容區 -->
        <div class="col-lg-4">
          <div class="card shadow-sm mb-4 rounded-lg">
            <div class="card-body">
              <h5 class="card-title fw-bold">再次訂購</h5>
              <button @click="handleReorderClick" class="btn btn-block btn-lg mt-3 w-100 fw-bold rounded" style="background-color: #e20261; color: white;">
                重新訂購最新訂購項目
              </button>
            </div>
          </div>

          <div v-if="order.rated" class="card shadow-sm mb-4 rounded-lg">
            <div class="card-body">
              <p class="mb-0">你已評分 ⭐⭐⭐⭐⭐ import review_Modal</p>
            </div>
          </div>


        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 可以在這裡添加組件特有的樣式 */
.rounded-lg {
  border-radius: 0.75rem !important; /* 增加圓角 */
}

/* 確保按鈕在小螢幕下保持全寬 */
@media (max-width: 991.98px) {
  .btn-lg {
    font-size: 1rem; /* 調整按鈕字體大小 */
    padding: 0.75rem 1.25rem; /* 調整按鈕內邊距 */
  }
}
</style>

