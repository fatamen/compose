<template>
  <div>
    <h3>篩選條件</h3>
    <div class="filter-group">
      <h4>美食類型</h4>
      <label><input type="checkbox" v-model="localFilters.category" value="中式料理"> 中式料理</label>
      <label><input type="checkbox" v-model="localFilters.category" value="西式料理"> 西式料理</label>
      <label><input type="checkbox" v-model="localFilters.category" value="日式料理"> 日式料理</label>
      <label><input type="checkbox" v-model="localFilters.category" value="韓式料理"> 韓式料理</label>
    </div>
    <div class="filter-group">
      <h4>最低星數</h4>
      <div class="star-rating">
        <span
          v-for="index in 5"
          :key="index"
          class="star"
          :class="{ 'half': isHalfStar(index), 'full': isFullStar(index) }"
          @click="setStarRating(index, $event)"
        >
          ★
        </span>
      </div>
      <div class="range-value">{{ localFilters.minscore.toFixed(1) }} 星</div>
    </div>
    <div class="filter-group">
      <h4>營業狀態</h4>
      <label><input type="checkbox" v-model="localFilters.isOpen"> 營業中</label>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, toRefs } from 'vue'; // 引入 toRefs

const props = defineProps({
  filters: Object,
  availableCategories: { // 假設您從父元件傳遞可用類別列表
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:filters', 'update-score']);

// 使用 toRefs 來保持 prop 的響應性連結，但仍可在 localFilters 中使用
// 這樣做能讓 prop.filters 的變動直接反映在 localFilters 上，而不需要單獨的 watch
const localFilters = ref({
  category: props.filters.category ? [...props.filters.category] : [], // 進行淺複製
  minscore: props.filters.minscore || 0,
  isOpen: props.filters.isOpen || false
});

// **只保留一個 watch，負責監聽 localFilters 的變化，並向上觸發事件**
watch(localFilters, (newFilters) => {
  // 當 localFilters 改變時，發送事件給父元件
  emit('update:filters', { ...newFilters }); // 確保發送的是一個新的物件，避免引用問題
}, { deep: true });

// **新增一個 watch 來監聽 props.filters 的變化，但要避免循環觸發**
// 這個 watch 的目的是為了處理父元件從外部重置篩選（例如按鈕）時，
// 能夠正確同步 localFilters 的狀態，而不觸發自身的 update:filters
watch(() => props.filters, (newVal) => {
  // 檢查是否與目前的 localFilters 值不同，避免不必要的更新
  // 這裡使用 JSON.stringify 是一個簡單但可能效率不高的方式，
  // 更好的方式是逐個屬性比較，或者確保外部觸發的更新邏輯只修改 props.filters
  // 並且不直接依賴 SidebarFilters 自身的 emit。

  // 最佳解法是確保 props.filters 更新時，不會被 localFilters 的 watch 反向觸發。
  // 我們可以透過比較值來減少循環，但更穩健的是在 `Home.vue` 中控制。
  // 這裡我們仍然讓它同步，因為 `Home.vue` 已經有 `ignoreFilterWatch` 來處理。
  // 這裡的邏輯主要是讓 SidebarFilters 能夠響應 Home.vue 的 `resetSidebarFilters`
  if (JSON.stringify(newVal) !== JSON.stringify(localFilters.value)) {
     localFilters.value = {
      category: newVal.category ? [...newVal.category] : [],
      minscore: newVal.minscore || 0,
      isOpen: newVal.isOpen || false
    };
  }
}, { deep: true });


const emitUpdatescore = () => {
  emit('update-score');
};

const setStarRating = (index, event) => {
  const rect = event.target.getBoundingClientRect();
  const clickX = event.clientX - rect.left;
  const starWidth = rect.width;
  const isHalf = clickX < starWidth / 2;
  localFilters.value.minscore = isHalf ? index - 0.5 : index;
  emitUpdatescore();
};

const isHalfStar = (index) => {
  return localFilters.value.minscore === index - 0.5;
};

const isFullStar = (index) => {
  return localFilters.value.minscore >= index;
};
</script>

<style scoped>
.sidebar {  
  width: 250px;
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.sidebar h3 {
  font-size: 18px;
  margin-bottom: 15px;
}

.filter-group {
  margin-bottom: 20px;
}

.filter-group label {
  display: block;
  margin-bottom: 10px;
  font-size: 14px;
  color: #333;
}

.filter-group input[type="checkbox"] {
  margin-right: 8px;
}

.star-rating {
  display: flex;
  font-size: 2.25rem; /* 原 1.5rem 放大 1.5 倍 = 2.25rem */
  cursor: pointer;
}

.star {
  color: #ccc; /* 未選中的星星顏色 */
  margin-right: 7.5px; /* 原 5px 放大 1.5 倍 = 7.5px */
}

.star.full {
  color: #f39c12; /* 整顆星的顏色 */
}

.star.half::before {
  content: '★';
  position: absolute;
  color: #f39c12; /* 半顆星的顏色 */
  width: 50%;
  overflow: hidden;
}

.star.half {
  position: relative;
  color: #ccc; /* 半顆星的背景顏色 */
}

.range-value {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}
</style>