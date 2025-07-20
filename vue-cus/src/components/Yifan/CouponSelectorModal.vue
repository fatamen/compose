<template>
  <Teleport to="body">
    <transition name="fade">
      <div v-if="show" class="overlay" @click.self="cancel">
        <transition name="popup">
          <div class="popup-box">
            <!-- 標題 -->
            <div class="popup-header">
              <h5 class="d-flex align-items-center">
                <i class="bi bi-ticket-perforated me-2 text-warning fs-3"></i>
                選擇優惠券
              </h5>
              <button class="btn-close" @click="cancel">✕</button>
            </div>

            <!-- Tabs -->
            <ul class="nav nav-tabs mb-3 justify-content-center">
              <li class="nav-item" v-for="tab in tabs" :key="tab.value">
                <button
                  class="nav-link"
                  :class="{ active: activeTab === tab.value }"
                  @click="activeTab = tab.value"
                >
                  <i :class="tab.iconClass" class="me-2"></i>{{ tab.label }}
                </button>
              </li>
            </ul>




            <!-- 優惠券清單 -->
            <transition-group name="list-slide" tag="div" :key="activeTab">
              <div
                v-for="promotion in filteredPromotions"
                :key="promotion.id"
                class="card mb-2 shadow-sm voucher-card"
                :class="{ 'opacity-50': promotion.minSpend > cartAmount }"
              >
                <div class="card-body d-flex align-items-stretch">
                  <!-- 左圖 -->
                  <div class="voucher-left me-3 d-flex align-items-center justify-content-center">
                    <img :src="promotion.imageUrl" alt="icon" class="voucher-img" />
                  </div>


                  <!-- 中間內容 -->
                  <div class="voucher-content flex-grow-1">
                    <h5>{{ promotion.title }}</h5>
                    <p>{{ getDiscountText(promotion) }}</p>
                    <p>
                      有效期限：
                      {{ formatDate(promotion.startTime) }} ~ {{ formatDate(promotion.endTime) }}
                    </p>
                    <p class="small text-muted">{{ promotion.description }}</p>
                    <small v-if="promotion.minSpend > cartAmount" class="text-danger">
                      未達到最低消費門檻
                    </small>
                  </div>


                  <!-- 右側按鈕 -->
                  <div class="d-flex align-items-center ms-3">
                    <button
                      class="btn btn-yellow"
                      :disabled="promotion.minSpend > cartAmount"
                      @click="selectPromotion(promotion)"
                    >
                      使用
                    </button>
                  </div>
                </div>
              </div>
            </transition-group>


            <!-- 沒有資料時顯示 -->
            <div v-if="filteredPromotions.length === 0">
              <p class="text-muted text-center mt-4 fs-5">
                <i class="fas fa-ticket-alt me-2 text-warning"></i>
                此分類沒有符合條件的優惠券
              </p>
            </div>
          </div>
        </transition>
      </div>
    </transition>
  </Teleport>
</template>


<script setup>
import { ref, computed, watch, onMounted } from 'vue'


const props = defineProps({
  show: Boolean,
  promotions: Array,
  cartAmount: Number
})


const emits = defineEmits(['update:show', 'selected'])


const filteredPromotions = computed(() => {
  return activeTab.value === 'all'
    ? props.promotions
    : props.promotions.filter(p => p.type === activeTab.value)
})



const tabs = [
  { label: '全部', value: 'all', iconClass: 'fas fa-folder-open' },
  { label: '全平台', value: 'global', iconClass: 'fas fa-globe' },
  { label: '餐廳限定', value: 'restaurant', iconClass: 'fas fa-utensils' },
  { label: '餐點限定', value: 'food', iconClass: 'fas fa-hamburger' },
  // { label: '會員限定', value: 'member', iconClass: 'fas fa-crown' }
]
const activeTab = ref('all')




const getDiscountText = (p) => {
  if (p.discountType === 'amount') return `滿 $ ${p.minSpend} 折 ${p.discountValue} 元`
  if (p.discountType === 'percent') return `滿 $ ${p.minSpend} 打 ${p.discountValue} 折`
  return '優惠活動'
}


const formatDate = (datetimeStr) => {
  const date = new Date(datetimeStr)
  return isNaN(date) ? '格式錯誤' : `${date.getFullYear()}/${date.getMonth() + 1}/${date.getDate()}`
}




watch(() => props.show, (newVal) => {
  if (newVal) {
    setTimeout(() => {
      document.querySelectorAll('[aria-hidden="true"]').forEach(el => {
        el.removeAttribute('aria-hidden')
      })
    }, 50)
    activeTab.value = 'all'
  }
})






watch(() => props.promotions, (val) => {
  console.log('🧾 傳入的優惠券資料：', val)
})


const selectPromotion = (promotion) => {
  emits('selected', promotion)
  //函數名稱 selected
  //promotion 傳入的參數
  emits('update:show', false)
}


const cancel = () => emits('update:show', false)




</script>


<style scoped>
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 5000;
}


.popup-box {
  background: #fff;
  width: 100%;
  max-width: 600px;
  border-radius: 12px;
  padding: 20px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}


.popup-header {
  color: #5f3300;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}


.btn-close {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}


.nav-tabs {
  border-bottom: 2px solid #a06d50;
}


.nav-tabs .nav-link {
  width: 130px;
  color: #666;
  font-weight: 500;
  border: 1px solid transparent;
  background-color: transparent;
  transition: all 0.3s ease;
}


.nav-tabs .nav-link:hover {
  background-color: #fff3cd;
  color: #a06d50;
  border-color: #ffe58f;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
}


.nav-tabs .nav-link.active {
  background-color: #a06d50;
  color: white;
  font-weight: bold;
  border-color: #ffc94d #ffc94d #fff;
}


.voucher-card {
  position: relative;
  min-height: 160px;
  border: none;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}


.voucher-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}


.voucher-left {
  width: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}


.voucher-img {
  width: 120px;
  object-fit: contain;
}


.voucher-content {
  padding-left: 20px;
  line-height: 0.5;
}


.btn-yellow {
  background-color: #ffc94d;
  color: #ffffff;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  padding: 6px 16px;
}


.btn-yellow:hover {
  background-color: #f7ba1e;
  color: #5f3300;
}


/* 進出動畫 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}


.popup-enter-active,
.popup-leave-active {
  transition: transform 0.3s;
}
.popup-enter-from,
.popup-leave-to {
  transform: scale(0.8);
}


/* 券卡片滑動動畫 */
.list-slide-enter-from {
  opacity: 0;
  transform: translateY(20px);
}
.list-slide-enter-active {
  transition: all 0.4s ease;
}
.list-slide-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}
  .list-slide-leave-active {
  transition: all 0.3s ease;
}
</style>

