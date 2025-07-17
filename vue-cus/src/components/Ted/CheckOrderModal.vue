<template><!--定錨 大修改-->
  <div class="modal fade" ref="checkOrderModal" tabindex="-1" aria-labelledby="checkOrderModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title" id="checkOrderModalLabel">您的訂單</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <h4 class="card-title text-center mb-4">您的訂單</h4>

            <div class="alert alert-info d-flex align-items-center gap-2 mb-4" role="alert">
              <i class="bi bi-clock"></i>
              <div>
                本店今日營業時間：<strong>今日營業時間為...</strong>
              </div>
            </div>
            <div class="mb-4">
              <label for="pickupTime" class="form-label fw-bold">預計取餐時間</label>
              <div class="input-group ">
                <button class="btn btn-outline-secondary " type="button" @click="adjustTime(-5)">－</button>

                <input type="time" class="form-control text-center" v-model="currentTime" />

                <button class="btn btn-outline-secondary" type="button" @click="adjustTime(5)">＋</button>
              </div>
            </div>

            <hr class="my-4">

            <h5 class="mb-3">您的訂單</h5>
            <div v-for="item in orderItems" :key="item.id"
              class="d-flex align-items-center justify-content-between mb-3 py-2 border-bottom">
              <div>
                <div class="fw-bold">{{ item.food.name }}a</div>
                <!-- 配料選校 -->
                <!-- <small class="text-muted d-block" style="font-size: 0.85rem;">
                  {{ item.spec || '無選項' }}
                </small> -->
                <!-- <small class="text-muted">NT$ {{ item.price }}</small> -->
              </div>
              <div class="d-flex align-items-center gap-2">
                <!-- <div class="d-flex align-items-center border overflow-hidden flex-shrink-0 rounded-2"> -->
                  <!-- <button class="btn btn-sm px-2" @click="decreaseQuantity(item.id)" :disabled="item.quantity === 1"
                    :class="{ 'btn-secondary text-white': item.quantity === 1, 'btn-light text-dark': item.quantity !== 1 }">
                    －
                  </button> -->
                  <span  style="width: 35px;">{{ item.quantity }}</span>
                  <!-- <button class="btn btn-sm px-2 btn-light text-dark" @click="increaseQuantity(item.id)">
                    ＋
                  </button> -->
                <!-- </div> -->
                <div class="fw-bold text-end" style="width: 80px;">NT$ {{ item.total }}</div>
                <!-- <button class="btn btn-sm btn-outline-danger rounded-circle p-0"
                  style="width: 28px; height: 28px; font-size: 0.8rem;" @click="removeItem(item.id)">
                  <i class="bi bi-trash"></i>
                </button> -->
              </div>
            </div>


            <!-- <div class="d-flex justify-content-between align-items-center mt-4">
              <span class="fw-bold">小計</span>
              <span class="fw-bold">NT$ {{ subtotal }}</span>
            </div> -->

            <div class="d-flex justify-content-between align-items-center mt-3 py-3 border-bottom">
              <h5 class="mb-0">總付款金額</h5>
              <h5 class="mb-0 text-primary">NT$ {{ totalPayment }}</h5>
            </div>

            <p class="text-muted text-center mt-4 small">
              您將在以下訂單選擇二、六月 24 日, 預約 12:30 取餐的外帶訂單。
              <br>取餐地址為...
            </p>
            <div class="mb-3">
              <label for="exampleFormControlTextarea1" class="form-label" >備註</label>
              <textarea class="form-control" id="exampleFormControlTextarea1" rows="3" v-model="content"></textarea>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-3">
              <label class="fw-bold">付款方式：</label>
              <label>
                <input type="radio" name="payment" value="cash" v-model="paymentMethod" /> 現金支付
              </label>
              <label class="ms-3">
                <input type="radio" name="payment" value="credit" v-model="paymentMethod" /> 信用卡支付
              </label>


            </div>
            <button type="button" class="btn btn-primary py-2 fw-bold w-100 mt-4" @click="emitAddToCart">
              確定結帳 NT$ {{ totalPayment }}
            </button>

          </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue';
import { Modal } from 'bootstrap';
import Swal from 'sweetalert2';

// 定義發射的事件
const emits = defineEmits(['add-to-cart', 'close']);

// 接收來自父組件的 props
const props = defineProps({
  orderItems: {
    type: Array,
    default: () => []
  },
  isVisible: {
    type: Boolean,
    default: false
  },
  restId: {
      type: Number,
      required: true
  }
});

  
// 模態框實例和 DOM 元素引用

let bsModal = null;
const checkOrderModal = ref(null);

const currentTime = ref(getNowTime());
// 設定時間
const paymentMethod = ref('cash');
// 設定付款方式
const content = ref('');
// 設定備註

onMounted(() => {
 
  if (checkOrderModal.value) {
    bsModal = new Modal(checkOrderModal.value);
    // 初始化元素
    // 監聽 Bootstrap 的隱藏事件
    
    checkOrderModal.value.addEventListener('hidden.bs.modal', () => {
      emits('close'); // 模態框完全隱藏後才通知父組件
      resetModalState();
      // 這裡可以進行任何模態框內部數據的重置
      // 例如：
      console.log('關閉')
    });
    // 監聽 Bootstrap 的顯示事件（用於額外的調試或邏輯）
    checkOrderModal.value.addEventListener('shown.bs.modal', () => {
      console.log('Modal shown and focus handled by Bootstrap');
    });
  }
});


const resetModalState = () => {
  content.value = '';
  
}

watch(() => props.isVisible, (newVal) => {
  if (bsModal) {
    if (newVal) {
      bsModal.show();
    } else {
      bsModal.hide();
    }
  }
});


// 訂單內容相關的響應式數據和方法
// const orderSummaryCard = ref(null); // <-- 移除此行，因為 card 元素已經不存在


function getNowTime() {
  const now = new Date();
  return now.toTimeString().slice(0, 5);
}

function adjustTime(minutes) {
  const [hour, minute] = currentTime.value.split(':').map(Number);
  const time = new Date();
  time.setHours(hour);
  time.setMinutes(minute + minutes);
  currentTime.value = time.toTimeString().slice(0, 5);
}

const internalOrderItems = ref(JSON.parse(JSON.stringify(props.orderItems)));

watch(() => props.orderItems, (newItems) => {
  internalOrderItems.value = JSON.parse(JSON.stringify(newItems));
}, { deep: true });

const subtotal = computed(() => {
  return internalOrderItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
});

const totalPayment = computed(() => {
  return subtotal.value;
});

const removeItem = (id) => {
  internalOrderItems.value = internalOrderItems.value.filter(item => item.id !== id);
};

const emitAddToCart = () => {
  if (internalOrderItems.value.length > 0) {
    const now = new Date()
    const formattedTime = now.toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false, // 顯示 24 小時制
      timeZone: 'Asia/Taipei' // 明確指定時區為台灣 (UTC+8)
    });
    
    const createTime = formattedTime.toString().replace(" ", "T").replace(/\//g, "-")
    //建立訂單時間即日期
    // formattedTime= formattedTime.replace(" ", "T");
  console.log("格式化後的台灣時間:", formattedTime.toString().replace(" ", "T").replace(/\//g, "-"));
      // console.log(new Date().toTimeString)
      // console.log(new Date().toTimeString)
   const body={

      content:content.value,
      status:'Pending',
      create_time:createTime ,
      method:paymentMethod.value,
      pickup_time:createTime.slice(0, 11)+currentTime.value
      // 設定取餐時間
    }
    //增加備註、訂單狀態、付款方式、取餐時間
    emits('add-to-cart',props.restId,body);
    // 送出事件清空函數
    internalOrderItems.value = [];
    bsModal.hide();
  } else {
    Swal.fire({
      icon: 'warning', // 警告圖示，也可以是 'error', 'success', 'info', 'question'
      title: '無法結帳', // 標題
      text: '該餐廳購物車是空的，無法結帳！', // 內容文字
      confirmButtonText: '確定', // 確認按鈕的文字
      customClass: {
        confirmButton: 'my-swal-confirm-button' // 可以為按鈕添加自定義 CSS 類別
      }
    });
  }
};

const closeModal = () => {
  bsModal.hide();
};

// const scrollToTop = () => { // <-- 移除此函數，因為 orderSummaryCard 不再存在
//   if (orderSummaryCard.value) {
//     orderSummaryCard.value.scrollIntoView({ behavior: 'smooth' });
//   }
// };

// defineExpose({
//   scrollToTop // <-- 移除 expose
// });
</script>

<style scoped>
/* 確保 Bootstrap Modal 的 z-index 高於 Navigation.vue 的 3000 */
.modal {
  z-index: 4000;
}

.modal-backdrop {
  z-index: 3999 !important; /* 或其他你想要的值 */
}

/* 移除所有關於 .order-summary-card 的樣式 */
/* .order-summary-card {
  width: 100%;
  border: none;
  box-shadow: none;
  padding: 0 !important;
} */

/* 如果 modal-body 預設 padding 不符合期望，可以在這裡調整 */
.modal-body {
  padding: 20px; /* 根據您的喜好調整內邊距 */
}


/* 確保輸入框圓角與按鈕組一致 */
.input-group .form-control:first-child {
  border-top-left-radius: var(--bs-border-radius-pill) !important;
  border-bottom-left-radius: var(--bs-border-radius-pill) !important;
}
.input-group .form-control:last-child {
  border-top-right-radius: 0 !important;
  border-bottom-right-radius: 0 !important;
}

/* 調整 .card-body 的內邊距，使其填滿 modal-body */
/* .modal-body .card-body { <-- 移除此選擇器，因為 card-body 不存在了
  padding: 0 !important;
} */

/* 針對 Bootstrap 的 modal-header 和 modal-footer 如果有需要微調 */
.modal-header {
  border-bottom: none;
  /* 如果不喜歡預設底部邊框可以移除 */
  /* padding-bottom: 0; */
}
</style>