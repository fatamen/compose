<template>
  <div class="filters">
    <div class="recommendation-buttons">
      <button
        v-if="isMorning"
        @click="toggleSearchKeyword('早餐')"
        :class="{ 'filter-button': true, 'active': activeKeywords.includes('早餐') }"
      >
        早餐推薦
      </button>
      <button
        v-else-if="isBrunch"
        @click="toggleSearchKeyword('早午餐')"
        :class="{ 'filter-button': true, 'active': activeKeywords.includes('早午餐') }"
      >
        早午餐推薦
      </button>
      <button
        v-else-if="isSupper"
        @click="toggleSearchKeyword('宵夜')"
        :class="{ 'filter-button': true, 'active': activeKeywords.includes('宵夜') }"
      >
        宵夜推薦
      </button>

      <button
        v-if="isCold"
        @click="toggleSearchKeyword('熱')"
        :class="{ 'filter-button': true, 'active': activeKeywords.includes('熱') }"
      >
        天寒地凍？來點熱飲吧！
      </button>
      <button
        v-else-if="isHot"
        @click="toggleSearchKeyword('冰')"
        :class="{ 'filter-button': true, 'active': activeKeywords.includes('冰') }"
      >
        日頭炎炎？來點冰品吧！
      </button>

      <button v-for="item in randomKeywords" :key="item.searchKeyword" @click="toggleSearchKeyword(item.searchKeyword)" 
        :class="{ 'filter-button': true, 'active': activeKeywords.includes(item.searchKeyword) }">
        {{ item.displayText }} 
      </button>
    </div>

    <div class="sort-options">
      <label for="sort-select">排序：</label>
      <select id="sort-select" :value="modelValue" @change="handleSortChange">
        <option value="評分最高">評分最高</option>
        <option value="距離最近">距離最近</option>
        <option value="最受歡迎">最受歡迎</option>
      </select>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useLocationStore } from '@/stores/location';

const locationStore = useLocationStore();

const props = defineProps({
  modelValue: {
    type: String,
    default: '評分最高'
  }
});

const emit = defineEmits(['update:modelValue', 'search-keyword']);

const temperature = computed(() => locationStore.temperature);

const isMorning = computed(() => {
  const hour = new Date().getHours();
  // 早餐時間設定為早上 5 點到 10 點前
  return hour >= 5 && hour < 10;
});

const isBrunch = computed(() => {
  const hour = new Date().getHours();
  // 早午餐時間設定為早上 10 點到下午 2 點前 (14 點)
  return hour >= 10 && hour < 14;
});

const isSupper = computed(() => {
  const hour = new Date().getHours();
  // 宵夜時間設定為晚上 10 點到凌晨 4 點前
  return hour >= 22 || hour < 4;
});

const isCold = computed(() => {
  return temperature.value !== null && temperature.value < 18;
});

const isHot = computed(() => {
  return temperature.value !== null && temperature.value > 28;
});

const handleSortChange = (event) => {
  emit('update:modelValue', event.target.value);
};


const activeKeywords = ref([]);

const toggleSearchKeyword = (keyword) => { // 這裡的 keyword 就是 searchKeyword
  if (activeKeywords.value.includes(keyword)) {
    activeKeywords.value = [];
    emit('search-keyword', '');
  } else {
    activeKeywords.value = [keyword];
    emit('search-keyword', keyword);
  }
};


// --- 隨機推薦邏輯 ---
// 這裡可以新增您特別的顯示名稱和對應的搜索關鍵字
const allRecommendationKeywords = [
  { displayText: '毛小孩好去處', searchKeyword: '寵物友善' },
  { displayText: '小孩放電好所在', searchKeyword: '公園附近' },
  { displayText: '熱鬧看比賽!', searchKeyword: '觀看直播' },
  { displayText: '初一十五要吃菜', searchKeyword: '素食' },
  { displayText: '精神不濟?', searchKeyword: '咖啡' },
  { displayText: '想吃點特別的', searchKeyword: '異國料理' },
  { displayText: '飯後來點甜', searchKeyword: '甜點' },
  { displayText: '喝酒! 嚕串!', searchKeyword: '燒烤' },
  { displayText: '痛風也要吃', searchKeyword: '海鮮' },
  { displayText: '朋友家人聚餐', searchKeyword: '聚餐' },
  { displayText: '巷口銅板美食', searchKeyword: '小吃' },
  { displayText: '輕食無負擔', searchKeyword: '健康' },
  { displayText: '趕時間也能吃', searchKeyword: '快速' },
  { displayText: '氣氛超好拍美照', searchKeyword: '好拍照' },
  { displayText: '天啊 已經三點了', searchKeyword: '甜點飲品' },
  { displayText: '下雨天不想出門', searchKeyword: '外送' }
];
const randomKeywords = ref([]);

const selectRandomKeywords = () => {
  const shuffled = [...allRecommendationKeywords].sort(() => 0.5 - Math.random());
  randomKeywords.value = shuffled.slice(0, 3);
};
// --- 隨機推薦邏輯結束 ---

onMounted(() => {
  // 獲取溫度資訊 (這部分仍保留，因為按鈕需要溫度判斷)
  if (locationStore.coordinates && locationStore.coordinates.lat && locationStore.coordinates.lon && locationStore.temperature === null) {
    locationStore.getTemperature(locationStore.coordinates.lat, locationStore.coordinates.lon);
  }
  // 頁面載入時選擇隨機關鍵字
  selectRandomKeywords();
});

watch(() => locationStore.coordinates, (newCoords) => {
  if (newCoords && newCoords.lat && newCoords.lon && locationStore.temperature === null) {
    locationStore.getTemperature(newCoords.lat, newCoords.lon);
  }
}, { deep: true });
</script>

<style scoped>
.filters {
  padding: 15px 20px;
  background-color: #fff;
  margin: 20px;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.recommendation-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-start;
  flex-grow: 1;
}

.filter-button {
  padding: 8px 15px;
  background-color: white; /* 預設白色背景 */
  color: #333; /* 預設深色文字 */
  border: 1px solid #ccc; /* 預設淺灰色邊框 */
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s ease; /* 過渡效果 */
}

.filter-button:hover {
  background-color: #f0f0f0; /* 懸停時輕微變灰 */
}

/* 點擊後的樣式 */
.filter-button.active {
  background-color: #ffba20; /* 點擊後變黃色 */
  color: white; /* 黃色背景搭配白色文字 */
  border-color: #ffba20; /* 邊框也變黃色 */
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 5px;
  flex-shrink: 0;
}

.sort-options label {
  font-size: 14px;
  color: #333;
}

.sort-options select {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
  background-color: white;
  cursor: pointer;
}

@media (max-width: 768px) {
  .filters {
    flex-direction: column;
    align-items: flex-start;
  }

  .recommendation-buttons {
    width: 100%;
    justify-content: flex-start;
    margin-bottom: 10px;
  }

  .sort-options {
    width: 100%;
    justify-content: flex-start;
  }

  .filter-button {
    flex-grow: 0;
  }
}
</style>