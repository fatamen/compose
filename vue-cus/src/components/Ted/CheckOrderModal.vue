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
              本店今日營業時間：<strong>{{ todayOpenTime }} - {{ todayCloseTime }}</strong>
            </div>
          </div>
          <div class="mb-4">
            <label for="pickupTime" class="form-label fw-bold">預計取餐時間</label>
            <div class="input-group ">
              <button class="btn btn-outline-secondary " type="button" @click="adjustTime(-5)">－</button>

              <input type="time" class="form-control text-center" v-model="currentTime"  @change="handleTimeInputChange"/>

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
              <span style="width: 35px;">{{ item.quantity }}</span>
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

          <!-- 優惠券選擇按鈕 -->
          <div class="d-flex justify-content-between align-items-center mt-3">
            <label class="fw-bold mb-0">優惠券</label>
            <button class="btn btn-sm btn-outline-warning" @click="openCouponModal">
              選擇優惠券
            </button>
          </div>
          <div v-if="selectedCoupon" class="d-flex justify-content-between align-items-center mt-2">
            <div class="fw-bold" style="color: #5f3300;">
              已選擇：{{ selectedCoupon.title }}
            </div>
            <div class="fw-bold" style="color: #5f3300;">
              折抵：-NT$ {{ subtotal - totalPayment }}
            </div>
          </div>
          <CouponSelectorModal v-model:show="showCouponModal" :promotions="promotionList" :cartAmount="subtotal"
            @selected="handleCouponSelected" />

          <!-- selected 是事件名稱 由CouponSelectorModal命名  -->
          <!-- 處理selected 事件的函數是 handleCouponSelected -->
          <!--handleCouponSelected 是由CheckOrderModal.vue命名 -->
          <!-- ！！物件事件處理函數的口訣務必記住！！  -->

          <div class="d-flex justify-content-between align-items-center mt-3 py-3 border-bottom">
            <h5 class="mb-0">總付款金額</h5>
            <h5 class="mb-0 text-primary">NT$ {{ totalPayment }}</h5>
          </div>

          <p class="text-muted text-center mt-4 small">
            您將在以下訂單選擇 {{ currentTime }} 取餐。
            <br>
          </p>
          <div class="mb-3">
            <label for="exampleFormControlTextarea1" class="form-label">備註</label>
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
import { ref, computed, watch, onMounted, onUnmounted ,nextTick} from 'vue';
import { Modal } from 'bootstrap';
import Swal from 'sweetalert2';

// 引入優惠券圖片
import axios from '@/plungins/axios.js'
import CouponSelectorModal from '@/components/Yifan/CouponSelectorModal.vue'//優惠券視窗

import globalImg from '@/assets/vouchers/global.png'
import restaurantImg from '@/assets/vouchers/restaurant.png'
import foodImg from '@/assets/vouchers/food.png'
import memberImg from '@/assets/vouchers/member.png'

const showCouponModal = ref(false)
const selectedCoupon = ref(null)
const promotionList = ref([])


// 統計 tag 消費金額（如果優惠券有綁定 tag_id，則應該用「該 tag 餐點的總金額」去比對 minSpend）
const tagSpendMap = computed(() => {
  const map = new Map();

  internalOrderItems.value.forEach(item => {
    //先判斷item.tags是否存在
    if (Array.isArray(item.tags)) {
      //計算總金額
      const total = item.price * item.quantity;
      item.tags.forEach(tag => {
        //這食物有多個 tag，所以需要判斷map中是否存在該tag，如果存在則累加，不存在則新增
        map.set(tag.id, (map.get(tag.id) || 0) + total);
      });
    }
  });

  const result = Object.fromEntries(map); // Map → Object
  console.log('✅ 轉換後 tagSpendMap:', result);
  return result;
});


const loadCoupons = async () => {
  document.querySelector('#app')?.removeAttribute('aria-hidden');
  console.log("🧩 props.orderItems:", props.orderItems);
  // ✅ 強制同步 props.orderItems → internalOrderItems（保險起見）
  internalOrderItems.value = JSON.parse(JSON.stringify(props.orderItems));
  console.log('🧾 internalOrderItems', internalOrderItems.value);

  try {
    // ✅ 這裡從 localStorage 抓登入的 userId
    const userId = parseInt(localStorage.getItem('userId')) || null;

    if (!userId) {
      console.warn('⚠️ 尚未登入，無法查詢優惠券');
      return;
    }

    // ✅ Console 確認傳送內容
    console.log('🧾 internalOrderItems:', internalOrderItems.value);
    console.log('🔖 tagIds:', tagIds.value);
    console.log('💰 tagSpendMap:', tagSpendMap.value);
    console.log('📦 amount (subtotal):', subtotal.value);
    console.log('🚀 傳送的優惠券查詢資料:', {
      userId: userId,
      storeId: props.restId,
      amount: subtotal.value,
      tagIds: tagIds.value,
      tagSpendMap: JSON.stringify(tagSpendMap.value)
    });

    // ✅ 發送 POST 請求
    const res = await axios.post('/promotions/available', {
      userId: userId,
      storeId: props.restId,
      amount: subtotal.value,
      tagIds: tagIds.value,
      tagSpendMap: JSON.stringify(tagSpendMap.value)
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    });

    // ✅ 資料加工：分類與圖片設定
    promotionList.value = res.data.map(item => {
      let type = 'global';
      let imageUrl = globalImg;

      if (item.storeId) {
        type = 'restaurant';
        imageUrl = restaurantImg;
      }
      if (item.tagName) {
        type = 'food';
        imageUrl = foodImg;
      }
      if (item.planId) {
        type = 'member';
        imageUrl = memberImg;
      }

      return {
        ...item,
        type,
        imageUrl
      };
    });

    console.log('✅ 載入優惠券成功，共', promotionList.value.length, '張');
    showCouponModal.value = true;

  } catch (error) {
    console.error('❌ 載入優惠券失敗', error);
  }
};

// ✅ 補上：用來接住從 modal 子元件選擇的優惠券
const handleCouponSelected = (coupon) => {
  // cpupon就是剛剛 emits('selected', promotion)傳過來的 promotion只是我們這邊改變名稱叫做coupon
  selectedCoupon.value = coupon;
  showCouponModal.value = false;
  console.log('🎟️ 已選擇優惠券：', coupon.title);
};


const openCouponModal = () => {
  document.querySelector('#app')?.removeAttribute('aria-hidden');
  loadCoupons();
};


// 定義發射的事件
const emits = defineEmits(['add-to-cart', 'close']);

// 接收來自父組件的 props
const props = defineProps({
  orderItems: {
    type: Array,
    default: () => []
  },
  restId: {
    type: Number,
    required: true
  }
});

// 建立可修改的本地訂單項目
const internalOrderItems = ref(JSON.parse(JSON.stringify(props.orderItems)));

// 監聽父層 props，如果有變化就同步更新本地資料
watch(() => props.orderItems, (newItems) => {
  internalOrderItems.value = JSON.parse(JSON.stringify(newItems));
}, { deep: true });



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
  console.log('--- onMounted 進入點 (CheckOrderModal) ---');

  // ✨ 關鍵修改：將所有依賴 DOM 元素的操作放入 nextTick
  nextTick(() => {
    console.log('--- nextTick 內部執行，DOM 已更新 (CheckOrderModal) ---');
    if (checkOrderModal.value) {
      bsModal = new Modal(checkOrderModal.value); // 實例化 Bootstrap Modal
      console.log('--- Bootstrap Modal 實例化成功 (CheckOrderModal) ---');

      // 監聽模態框完全隱藏的事件
      checkOrderModal.value.addEventListener('hidden.bs.modal', () => {
        console.log('--- HIDDEN EVENT (CheckOrderModal) --- 模態框關閉'); // 模態框關閉時顯示
        emits('close'); // 通知父組件模態框已關閉
        resetModalState(); // 重置模態框內部狀態
      });

      // 如果需要，也可以監聽模態框完全顯示的事件
      checkOrderModal.value.addEventListener('shown.bs.modal', () => {
        console.log('--- SHOWN EVENT (CheckOrderModal) --- 模態框顯示');
        // 在這裡可以執行模態框顯示後才需要執行的邏輯，例如載入數據
      });

      // ✨ 在 onMounted 且 DOM 準備就緒後立即顯示模態框
      //    因為父組件已經透過 v-if 控制了掛載，所以這裡可以確保它顯示
      bsModal.show(); // 顯示模態框

    } else {
      console.log('--- nextTick 內部：checkOrderModal.value 是 null 或 undefined (CheckOrderModal) ---');
    }
  });

  // 非同步操作（如 findOpenHour）可以獨立於 nextTick，但確保其結果會更新響應式變數
  findOpenHour(props.restId);
  // 開啟時候渲染
  selectedCoupon.value = null;


});


const resetModalState = () => {
  content.value = '';
}

// watch(() => props.isVisible, (newVal) => {
//   if (bsModal) {
//     if (newVal) {
//       bsModal.show();
//     } else {
//       bsModal.hide();
//     }
//   }
// });


//---新增時間------------------------------------------
const todayOpenTime = ref('');
const todayCloseTime = ref('');
const shwoOpenTime = ref('');
const showCloseTime = ref('');
// todayOpenHourText.value = findOpenHour(props.restId);
const findOpenHour=(id)=>{
  axios.get(`/stores/${id}/hours/todayOpenHour`).then((response) => {
    const openHour = response.data;
    
    console.log('營業時間',openHour);
    todayOpenTime.value = openHour.openTime.slice(0,5);
    todayCloseTime.value = openHour.closeTime.slice(0,5);
    currentTime.value = validateAndClampTime(getNowTime());
  }).catch((error) => {
    console.error(error);
  });
}
function parseTime(timeString) {
  const [hour, minute] = timeString.split(':').map(Number);
  const now = new Date(); // 使用當前日期，只關注時間部分
  now.setHours(hour, minute, 0, 0); // 設置小時、分鐘、秒、毫秒
  return now;
}
function formatTimeToAmPm(time24h) {
  if (!time24h) return '';
  const [hour, minute] = time24h.split(':').map(Number);
  const ampm = hour >= 12 ? 'PM' : 'AM';
  const displayHour = hour % 12 || 12; // 處理 0 點和 12 點
  return ` ${ampm} ${displayHour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
}
shwoOpenTime.value=formatTimeToAmPm(todayOpenTime.value);
showCloseTime.value=formatTimeToAmPm(todayCloseTime.value);
//--------------------------------------------------------------



// 訂單內容相關的響應式數據和方法
// const orderSummaryCard = ref(null); // <-- 移除此行，因為 card 元素已經不存在


function getNowTime() {
  const now = new Date();
  return now.toTimeString().slice(0, 5);
}

// function adjustTime(minutes) {
//   const [hour, minute] = currentTime.value.split(':').map(Number);
//   const time = new Date();
//   time.setHours(hour);
//   time.setMinutes(minute + minutes);
//   currentTime.value = time.toTimeString().slice(0, 5);
// }
//調整時間----------------------------------------------------------------
function adjustTime(minutes) {
  const [currentHour, currentMinute] = currentTime.value.split(':').map(Number);
  const proposedTimeDate = new Date();
  proposedTimeDate.setHours(currentHour);
  proposedTimeDate.setMinutes(currentMinute + minutes);
  proposedTimeDate.setSeconds(0);
  proposedTimeDate.setMilliseconds(0);

  const originalTime = currentTime.value; // 記錄調整前時間

  // ✨ 呼叫核心驗證函數
  const clampedTime = validateAndClampTime(proposedTimeDate.toTimeString().slice(0, 5));
  currentTime.value = clampedTime; // 更新 currentTime
}

// ✨ 新增一個核心函數來驗證並限制時間範圍
function validateAndClampTime(timeToValidate) {
  // 檢查營業時間是否已載入
  if (!todayOpenTime.value || !todayCloseTime.value) {
    Swal.fire({
      icon: 'warning',
      title: '營業時間未載入',
      text: '請等待營業時間載入或重新整理頁面。',
      confirmButtonText: '確定',
    });
    return getNowTime(); // 返回當前時間作為 fallback
  }

  const inputTime = parseTime(timeToValidate);
  const openLimit = parseTime(todayOpenTime.value);
  const closeLimit = parseTime(todayCloseTime.value);

  let finalTimeDate = inputTime;

  if (inputTime < openLimit) {
    Swal.fire({
      icon: 'info',
      title: '時間已自動調整',
      text: `您選擇的時間已自動調整為營業時間範圍內的 ${currentTime.value}。`,
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true,
    });
    finalTimeDate = openLimit;
  } else if (inputTime > closeLimit) {
    Swal.fire({
      icon: 'info',
      title: '時間已自動調整',
      text: `您選擇的時間已自動調整為營業時間範圍內的 ${currentTime.value}。`,
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true,
    });
    finalTimeDate = closeLimit;
  }

  return finalTimeDate.toTimeString().slice(0, 5); // 返回修正後的 HH:mm 字串
}


function handleTimeInputChange() {
  const originalTime = currentTime.value; // 記錄調整前時間 (v-model 已更新)

  // ✨ 呼叫核心驗證函數
  const clampedTime = validateAndClampTime(originalTime); // 這裡傳入 v-model 已經更新的值
   currentTime.value = clampedTime; // 更新 currentTime
}

//--------------------------------------------------------------------
watch(() => props.orderItems, (newItems) => {
  internalOrderItems.value = JSON.parse(JSON.stringify(newItems));
}, { deep: true });

const subtotal = computed(() => {
  return internalOrderItems.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
});


const tagIds = computed(() => {
  const allTags = new Set();
  internalOrderItems.value.forEach(item => {
    console.log("item", item)
    if (Array.isArray(item.tags)) {
      item.tags.forEach(tag => allTags.add(tag.id));
    }
  });
  console.log("allTags", allTags)
  return Array.from(allTags);
});


// 原本總金額的寫法
// const totalPayment = computed(() => {
//   return subtotal.value;
// });

// 改成有使用優惠券，計算後總金額的寫法


const totalPayment = computed(() => {
  if (!selectedCoupon.value) return subtotal.value;

  const coupon = selectedCoupon.value;
  // 現金折抵優惠券
  if (coupon.discountType === 'amount') {
    return Math.max(0, subtotal.value - coupon.discountValue);
  }
  // 打折優惠券
  if (coupon.discountType === 'percent') {
    const discountRate = coupon.discountValue / 10; // 例如 9 ➜ 0.9 
    const discountAmount = subtotal.value * (1 - discountRate);
    return Math.max(0, Math.floor(subtotal.value - discountAmount));
  }

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
    //建立訂單時間即日期
    // formattedTime= formattedTime.replace(" ", "T");
    console.log("格式化後的台灣時間:", formattedTime.toString().replace(" ", "T").replace(/\//g, "-"));
    // console.log(new Date().toTimeString)
    // console.log(new Date().toTimeString)
    const body = {

      content: content.value,
      status: 'Pending',
      create_time: createTime,
      method: paymentMethod.value,
      pickup_time: createTime.slice(0, 11) + currentTime.value,
      // 設定取餐時間
      promotionId: selectedCoupon.value?.id || null, //  有選優惠券就放ID，沒選就 null
      total: totalPayment.value // 傳折扣後總金額
    }
    //增加備註、訂單狀態、付款方式、取餐時間
    emits('add-to-cart', props.restId, body);
    console.log('🎟️ emit 出去的資料：', body);
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

// 取得台灣時間
function getTaiwanISOTime() {
  const offsetMs = 8 * 60 * 60 * 1000;
  const taiwanTime = new Date(Date.now() + offsetMs);
  return taiwanTime.toISOString().slice(0, 19);
}

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
  z-index: 1060;
}

.modal-backdrop {
  z-index: 999 !important;
  /* 或其他你想要的值 */
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
  padding: 20px;
  /* 根據您的喜好調整內邊距 */
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