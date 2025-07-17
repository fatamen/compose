<template>
  <div class="voucher-container mx-auto my-5 p-4 shadow-sm bg-white rounded-4 custom-width">
    <h2 class="mb-4 fw-bold text-center">
      <i class="bi bi-ticket-perforated me-3 text-warning"></i>我的優惠券
    </h2>

    <!-- 📌 Tabs -->
    <ul class="nav nav-tabs justify-content-center mb-4 border-bottom-yellow">
      <li class="nav-item" v-for="tab in tabs" :key="tab.value">
        <button
          class="nav-link tab-btn px-4 py-2 mx-1"
          :class="{ active: activeTab === tab.value }"
          @click="activeTab = tab.value"
        >
          <i :class="tab.icon" class="me-2"></i>{{ tab.label }}
        </button>
      </li>
    </ul>

    <!-- 📌 列表區：直接顯示 promotionList -->
    <div v-if="promotionList.length > 0" class="d-flex flex-column gap-3">
      <VoucherCard
        v-for="promotion in promotionList"
        :key="promotion.id"
        :promotion="promotion"
        :cartAmount="cartAmount"
        @use="handleUse"
      />
    </div>
    <div v-else class="text-muted text-center mt-5 fs-5">
      <i class="fas fa-ticket-alt me-2 text-warning"></i>
      此分類目前沒有可用優惠券
    </div>
  </div>
</template>

<script setup>
import VoucherCard from '@/components/Yifan/VoucherCard.vue'
//引入 axios 並撰寫 API 請求
import axios from '@/plungins/axios.js'
import { onMounted, ref, watch } from 'vue'

//引入優惠券圖片
import globalImg from '@/assets/vouchers/global.png'
import restaurantImg from '@/assets/vouchers/restaurant.png'
import foodImg from '@/assets/vouchers/food.png'
import memberImg from '@/assets/vouchers/member.png'

//初始資料
const cartAmount = ref(500)
const activeTab = ref('all')

//分類 tabs 資料
const tabs = [
  { label: '全部', value: 'all', icon: 'fas fa-folder-open' },
  { label: '全平台', value: 'global', icon: 'fas fa-globe' },
  { label: '餐廳限定', value: 'restaurant', icon: 'fas fa-utensils' },
  { label: '餐點限定', value: 'food', icon: 'fas fa-hamburger' },
  { label: '會員限定', value: 'member', icon: 'fas fa-crown' },
  { label: '歷史紀錄', value: 'history', icon: 'fas fa-clock' }
]

//優惠券資料
const promotionList = ref([]) //改連後端資料庫
// const filteredPromotions = computed(() => {
//   const current = activeTab.value
//   return promotionList.value.filter(p => {
//     if (current === 'history') return p.used
//     if (current === 'all') return !p.used
//     if (p.used) return false
//     return p.type === current
//   })
// })


// ✅ 點選 tab 時重新載入分類資料
watch(activeTab, async (tab) => {
  await loadPromotions(tab)
})

// ✅ 頁面載入時載入預設資料（全部可用券）
onMounted(async () => {
  await loadPromotions('all')
})

// ✅ 根據 tab 取得分類資料
const loadPromotions = async (tab) => {
  try {
    let url = ''

    if (tab === 'all') {
      url = '/promotions/all-available' // ✅ 顯示所有有效券（不是結帳篩選用）
    } else if (tab === 'history') {
      promotionList.value = [] // ✅ 尚未串 /used 資料，預設空清單
      return
    } else {
      url = `/promotions/by-type?type=${tab}` // ✅ 類型分類券
    }

    const response = await axios.get(url)


    // 將回傳的優惠券加上圖示與圖片（根據 type）
    promotionList.value = response.data.map(item => {
      let imageUrl = globalImg
      let iconClass = 'fas fa-globe'

      if (item.type === 'restaurant') {
        imageUrl = restaurantImg
        iconClass = 'fas fa-utensils'
      } else if (item.type === 'food') {
        imageUrl = foodImg
        iconClass = 'fas fa-hamburger'
      } else if (item.type === 'member') {
        imageUrl = memberImg
        iconClass = 'fas fa-crown'
      }

      return {
        ...item,
        iconClass,
        imageUrl,
      }
    })
  } catch (error) {
    console.error('載入優惠券失敗', error)
  }
}









//使用優惠券
const handleUse = (promo) => {
  console.log('使用優惠券：', promo.title)
}

// onMounted(async () => { //載入優惠券資料
//   try {
//     const response = await axios.get('/promotions/available') //先取得全部「可使用」的優惠券
//     console.log('載入優惠券成功', response)
//     promotionList.value = response.data.map(item => {
//       let imageUrl = globalImg
//       let iconClass = 'fas fa-globe'
//       let type = 'global'

//       if (item.store?.id) {
//         imageUrl = restaurantImg
//         iconClass = 'fas fa-utensils'
//         type = 'restaurant'
//       } else if (item.tag?.id) {
//         imageUrl = foodImg
//         iconClass = 'fas fa-hamburger'
//         type = 'food'
//       } else if (item.plan?.id) {
//         imageUrl = memberImg
//         iconClass = 'fas fa-crown'
//         type = 'member'
//       }

//       return {
//         ...item,
//         iconClass,
//         imageUrl,
//         type,
//       }
//     })
//   } catch (error) {
//     console.error('載入優惠券失敗', error)
//   }
// })


</script>

<style scoped>
h2 {
  font-size: 1.8rem;
  letter-spacing: 1px;
}

.custom-width {
  max-width: 1200px; /* 你可以自己調整為 700px 或 600px */
  width: 100%;
}

/* 主容器美化 */
.voucher-container {
  background-color: #fffbea;
  color: #5f3300;
}

/* Tabs 樣式 */
.nav-tabs {
  border-bottom: 2px solid #a06d50;
}

.nav-tabs .nav-link {
  border: 1px solid transparent;
  color: #666;
  font-weight: 500;
  padding: 10px 20px;
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

.tab-btn {
  width: 140px;
  background-color: transparent;
  color: #666;
  border: 1px solid transparent;
  transition: all 0.3s ease;
  border-radius: 10px 10px 0 0;
}

/* 無資料訊息 */
.text-muted i {
  font-size: 1.2rem;
}
</style>