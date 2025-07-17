<!-- 歷史訂單點進去後的詳細 -->

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from '@/plungins/axios.js';

// 引入 Bootstrap 5 CSS
import 'bootstrap/dist/css/bootstrap.min.css';
// 引入 Bootstrap 5 JS (可選，這裡主要用於佈局，JS 功能如彈窗等可能需要完整引入)
// import 'bootstrap/dist/js/bootstrap.bundle.min.js';

const props =defineProps(['restaurantId'])

const route = useRoute();
// 訂單數據
const order = ref({});

// 計算總計
const total = computed(() => {
  return order.value.subtotal + order.value.deliveryFee + order.value.platformFee;
});

// 重新訂購按鈕點擊事件
const reorder = () => {
  alert('您點擊了「重新訂購最新訂購項目」！');
  // 這裡可以加入重新訂購的邏輯
};

// 需要協助按鈕點擊事件
const needHelp = () => {
  alert('您點擊了「與我們協助」！');
  // 這裡可以加入尋求協助的邏輯
};

const findOrderById = (id)=>{
  axios.get(`/api/orders/${id}`).then((res)=>{
    console.log(res)
    order.value =res.data
    console.log('order內容',order.value)
  })
}

onMounted(() => {
  // 在組件掛載後執行的邏輯
  console.log('取得id',props.restaurantId   )
  console.log('取得id2',route.params.id   )
  findOrderById(props.restaurantId)
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
              <h5 class="card-title fw-bold">訂單記錄 & 最新訂購</h5>
              <p class="text-muted small">
                <a href="#" class="text-decoration-none" style="color: #e20261;">訂單記錄</a> &gt; <a href="#" class="text-decoration-none" style="color: #e20261;">訂單詳情</a>
              </p>

              <div class="d-flex align-items-center mb-3">
                <div class="me-3">
                  <img src="" alt="餐廳圖標" class="rounded-circle border" style="width: 70px; height: 70px; object-fit: cover;">
                </div>
                <div>
                  <!-- <h5 class="mb-0 fw-bold">{{ order.store.name }}</h5> -->
                  <!-- <p class="mb-0 text-muted small">外送於 {{ order.restaurant.address }}</p> -->
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
                  <img :src="item.imageUrl" :alt="item.name" class="rounded-circle" style="width: 50px; height: 50px; object-fit: cover;">
                </div>
                <div class="flex-grow-1">
                  <p class="mb-0 fw-bold">{{ item.name }}</p>
                  <!-- <p class="mb-0 text-muted small">{{ item.description }}</p> -->
                </div>
                <span class="fw-bold text-end">NT$ {{ item.price.toFixed(0) }}</span>
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
              <button @click="reorder" class="btn btn-block btn-lg mt-3 w-100 fw-bold rounded" style="background-color: #e20261; color: white;">
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

