<template>
  <div class="container my-5">
    <h2>結帳頁</h2>
    <p>原價：{{ cartAmount }} 元</p>
    <p v-if="selected">折扣後金額：{{ discountedTotal }} 元</p>

    <!-- <button class="btn btn-primary mb-3" @click="show = true"> -->
    <button class="btn btn-primary mb-3" @click="openCouponModal()">
      選擇優惠券
    </button>

    <p>已選擇：{{ selected?.title || '尚未選擇' }}</p>
    <!-- ✅ 訂單明細區塊 -->

    <div class="mt-4 p-4 border rounded-3 bg-light">
      <h5 class="fw-bold mb-3">訂單明細</h5>

      <div v-if="selected" class="d-flex align-items-center mb-3">
        <img :src="selected.imageUrl" alt="券圖片" style="width: 60px; height: auto;" class="me-3" />
        <div>
          <p class="mb-1 fw-bold">{{ selected.title }}</p>
          <p class="mb-1">折扣類型：{{ selected.discountType }}</p>
          <p class="mb-1">折扣數值：{{ selected.discountValue }}</p>
          <p class="mb-1">使用門檻：{{ selected.minSpend }}</p>
          <p class="mb-1">
            有效期限：
            {{ selected.startTime }} ～ {{ selected.endTime }}
          </p>
        </div>
      </div>
      <div v-else class="text-muted mb-3">尚未選擇優惠券</div>

      <p class="mb-1">原價：{{ cartAmount }} 元</p>
      <p class="mb-1 text-success fw-bold">
        折扣金額：
        {{ selected ? '-' + (cartAmount - discountedTotal) : '0' }} 元
      </p>
      <p class="mb-0 text-danger fw-bold">應付金額：{{ discountedTotal }} 元</p>
    </div>


    <!-- 彈窗元件 -->
    <CouponSelectorModal v-model:show="show" :promotions="promotionList" :cartAmount="cartAmount"
      @selected="handleSelected" />
    <!-- 結帳按鈕 -->
    <button class="btn btn-success" @click="submitOrder">
      模擬送出訂單（含優惠券）
    </button>
    <p v-if="selected">折扣：{{ cartAmount - discountedTotal }} 元</p>

  </div>
</template>

<script setup>

import { ref, computed } from 'vue'
import axios from '@/plungins/axios.js'
import CouponSelectorModal from '@/components/Yifan/CouponSelectorModal.vue'
import globalImg from '@/assets/vouchers/global.png'
import restaurantImg from '@/assets/vouchers/restaurant.png'
import foodImg from '@/assets/vouchers/food.png'
import memberImg from '@/assets/vouchers/member.png'
import Swal from 'sweetalert2'

// 購物車金額
const discountedTotal = computed(() => {
  if (!selected.value) return cartAmount.value

  const discount = selected.value.discountType === 'amount'
    ? selected.value.discountValue
    : cartAmount.value * (1 - selected.value.discountValue / 10)

  return Math.max(0, Math.floor(cartAmount.value - discount))
})


// 彈窗顯示控制
const show = ref(false)
// 已選擇的券
const selected = ref(null)
// ‼️‼️‼️ 優惠券的使用門檻
const cartAmount = ref(1000)         // ✅ 測試門檻用的消費金額
const currentUser = { id: 5 }        // ✅ 測試用的會員 ID
const currentStore = { id: 2 }       // ✅ 測試用的店家 ID
// const currentTag = { id: 4 }         // ✅ 餐點類別 ID（如果後端有支援）
// const currentPlan = { id: 1 }        // ✅ 會員方案 ID（例如黃金會員）



// 優惠券列表（從後端載入）
const promotionList = ref([])


const openCouponModal = async () => {
  try {
    const response = await axios.get('/promotions/available', {
      params: {
        userId: currentUser.id,
        storeId: currentStore.id,
        amount: cartAmount.value
      }
    })
    console.log('🎯 後端回傳幾筆券：', response.data.length)
    console.log('🎯 回傳資料：', response.data)


    promotionList.value = response.data.map(item => {
      const types = []
      let imageUrl = globalImg
      let iconClass = 'fas fa-globe'

      if (item.storeId) {
        types.push('restaurant')
        imageUrl = restaurantImg
        iconClass = 'fas fa-utensils'
      }
      if (item.tagName) {
        types.push('food')
        imageUrl = foodImg
        iconClass = 'fas fa-hamburger'
      }
      if (item.planId) {
        types.push('member')
        imageUrl = memberImg
        iconClass = 'fas fa-crown'
      }
      if (types.length === 0) {
        types.push('global')
        imageUrl = globalImg
        iconClass = 'fas fa-globe'
      }

      return {
        ...item,
        iconClass,
        imageUrl,
        types // 確保這裡是陣列（分類用）
      }
    })


    // 🔍 debug 印出每張券的 types 分類
    console.log('🧾 每張券分類 types：')
    promotionList.value.forEach(p => {
      console.log(`📌 ${p.title}：`, p.types)
    })

    show.value = true
  } catch (error) {
    console.error('載入結帳可用優惠券失敗', error)
  }
}



const handleSelected = (promotion) => {
  selected.value = promotion
  console.log('已選擇：', promotion)
}


//訂單結帳
const submitOrder = async () => {
  if (!selected.value) {
    Swal.fire({
      icon: 'warning',
      title: '請先選擇優惠券',
      confirmButtonText: '了解'
    })
    return
  }

  const orderPayload = {
    userId: currentUser.id,
    storeId: currentStore.id,
    promotionId: selected.value ? selected.value.id : null,
    total: discountedTotal.value,
    status: 'COMPLETED',
  }

  try {
    // 送出訂單
    const res = await axios.post('/api/orders', orderPayload, {
      headers: { 'Content-Type': 'application/json' }
    })

    const orderId = res.data.id
    console.log('✅ 訂單送出成功，訂單編號：', orderId)

    // 🎯 測試：查詢這筆訂單可用的優惠券
    const availableRes = await axios.get('/promotions/available', {
      params: {
        userId: currentUser.id,
        storeId: currentStore.id,
        amount: discountedTotal.value // 或 res.data.total
      }
    })

    console.log('🎟️ 此訂單可用的優惠券清單：', availableRes.data)

    // ✅ 成功彈窗
    Swal.fire({
      icon: 'success',
      title: '訂單送出成功！',
      html: `
        訂單編號：<strong>${orderId}</strong><br/>
        查詢可用優惠券：<br/>
        共 ${availableRes.data.length} 張
      `,
      confirmButtonText: '太好了！'
    })

  } catch (err) {
    console.error('❌ 訂單送出失敗', err)
    Swal.fire({
      icon: 'error',
      title: '訂單送出失敗',
      text: '請稍後再試一次',
      confirmButtonText: '關閉'
    })
  }
}


</script>

<style scoped>
.container {
  max-width: 600px;
}
</style>
