<!-- 優惠券卡片元件 -->

<template>
  <div
    class="voucher-card d-flex shadow-sm"
    :class="{ 'used-card': promotion.used || promotion.min_spend > cartAmount }"
  >
    <!-- 左側圖片區塊 -->
    <div class="voucher-label">
      <img :src="promotion.imageUrl" alt="icon" class="voucher-img" />
    </div>

    <!-- 右側內容區域 -->
    <div class="voucher-content flex-grow-1">
      <div class="d-flex justify-content-between align-items-start">
        <!-- 優惠券標題 -->
        <h5 class="voucher-title">
          {{ promotion.title }}
        </h5>

        <!-- 使用按鈕 -->
        <button
          class="btn btn-use"
          :disabled="promotion.min_spend > cartAmount || promotion.used"
          @click="$emit('use', promotion)"
        >
          使用
        </button>
      </div>

      <!-- 優惠券資訊 -->
      <p class="mb-1 text-secondary">{{ getDiscountText(promotion) }}</p>
      <p class="mb-1 text-muted">
        有效期限：{{ formatDate(promotion.startTime) }} ~ {{ formatDate(promotion.endTime) }}
      </p>
      <small class="text-muted">{{ promotion.description }}</small>
    </div>
  </div>
</template>

<script setup>
// 接收 props 與 emits
const props = defineProps({
  promotion: Object,
  cartAmount: Number,
})
const emit = defineEmits(['use'])

//折扣類型判別(%或金額)
const getDiscountText = (p) => {
  if (p.discountType === 'amount') {
    return `滿 $ ${p.minSpend} 折 ${p.discountValue} 元`
  }
  if (p.discountType === 'percent') {
    return `滿 $ ${p.minSpend} 打 ${p.discountValue} 折`
  }
  return '優惠活動'
}

// 格式化日期顯示
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-TW') // 顯示為 2025/7/6
}

</script>

<style scoped>
/* 卡片區塊 */
.voucher-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  margin-bottom: 20px;
  border-left: 10px solid #e8b212; /* 左側強調色條 */
  transition: box-shadow 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 滑鼠經過卡片區塊會滑動 */
.voucher-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
}

/* 已使用或未達門檻 */
.used-card {
  opacity: 0.6;
  filter: grayscale(80%);
  pointer-events: none; /* 無法點選使用 */
}

/* 左側圖片區域 */
.voucher-label {
  width: 150px;
  background: #ffc94d;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.voucher-img {
  max-width: 150px;
  max-height: 150px;
  object-fit: contain;
}

/* 右側文字內容區 */
.voucher-content {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.voucher-title {
  font-size: 1.3rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

/* 使用按鈕樣式 */
.btn-use {
  background-color: #ffc94d;
  border: none;
  padding: 8px 18px;
  font-weight: bold;
  color: #ffffff;
  border-radius: 6px;
  transition: all 0.3s ease;
}
.btn-use:hover:enabled {
  background-color: #f7ba1e;
  color: #5f3300;
}
.btn-use:disabled {
  background-color: #e0e0e0;
  color: #888;
  cursor: not-allowed;
  box-shadow: none;
}
</style>
