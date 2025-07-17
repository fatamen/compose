<!-- src/components/Jimmy/SearchSection.vue -->
<template>
  <section class="hero-section">
    <div class="search-container">
      <input
        type="text"
        placeholder="金碗 您 想來點甚麼呢?"
        v-model="searched"
        @focus="handleFocus"
        @blur="hideDropdownWithDelay"
        @input="filterSuggestions"
        @keydown.enter="handleSearch"
        @click="handleClickInInput" 
      />
      <button @click="handleSearch">搜尋</button>
      <div class="search-dropdown" v-show="showDropdown" @mousedown.prevent>
        <div class="search-section" v-if="searchHistory.length > 0">
          <h4>最近搜尋</h4>
          <ul>
            <li v-for="(item, index) in filteredHistory" :key="item" @click="selectSuggestion(item)" class="search-item">
              {{ item }}
              <button class="clear-history" @click.stop.prevent="removeHistoryItem(item)">✕</button>
            </li>
          </ul>
        </div>
        <div class="search-section">
          <h4>熱門搜尋</h4>
          <ul>
            <li v-for="(item, index) in filteredHotSearches" :key="item.id" @click="selectSuggestion(item.tag)" class="search-item">
              {{ item.tag }}
            </li>
          </ul>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue';
import axios from 'axios';

// API 基礎 URL
const API_URL = import.meta.env.VITE_API_URL;

// 定義 Props
const props = defineProps({
  initialSearch: {
    type: String,
    default: '',
  },
});

// 定義 Emits
const emit = defineEmits(['update:search', 'search']);

// 搜索相關狀態
const searched = ref(props.initialSearch);
const searchHistory = ref(JSON.parse(localStorage.getItem('searchHistory')) || []);
const hotSearches = ref([]);
const showDropdown = ref(false);
const filteredHistory = ref([]);
const filteredHotSearches = ref([]);

let preventBlur = false; // **新增：防止 blur 事件觸發的旗標**

// 從後端獲取熱門搜尋數據
const fetchHotSearches = async () => {
  try {
    const response = await axios.get(`${API_URL}/api/web-recom`);
    hotSearches.value = response.data.sort((a, b) => a.prime - b.prime);
    filteredHotSearches.value = [...hotSearches.value];
  } catch (error) {
    console.error('獲取熱門搜尋失敗:', error);
  }
};

// 頁面加載時獲取熱門搜尋
onMounted(() => {
  fetchHotSearches();
});

// 監聽 props.initialSearch 並同步到 searched
watch(
  () => props.initialSearch,
  (newVal) => {
    searched.value = newVal;
  }
);

// 監聽 searched 並發出 update:search 事件
watch(searched, (newVal) => {
  emit('update:search', newVal);
});

// 處理焦點事件
const handleFocus = () => {
  // 這裡維持原樣，會在獲得焦點時清空搜尋欄
  searched.value = '';
  showDropdown.value = true;
  filterSuggestions();
};

// 保存搜索歷史
const saveSearchHistory = () => {
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value));
};

// 處理搜索 - 將搜尋詞發送給父組件
const handleSearch = () => {
  if (searched.value.trim()) {
    const searchTerm = searched.value.trim();
    if (!searchHistory.value.includes(searchTerm)) {
      searchHistory.value.unshift(searchTerm);
      if (searchHistory.value.length > 5) {
        searchHistory.value.pop();
      }
      saveSearchHistory();
    }
    emit('search', searchTerm); // 觸發 'search' 事件並傳遞搜尋詞
    showDropdown.value = false; // 搜尋後關閉下拉選單
  } else {
    // 如果搜尋框為空，也觸發搜尋事件，讓父組件重置列表
    emit('search', '');
    showDropdown.value = false;
  }
};

// 新增：處理在 input 欄位內部的點擊事件 (用於已經聚焦時的再次點擊)
const handleClickInInput = () => {
  // 只有當 input 已經聚焦且顯示下拉選單時，才清空內容
  // 否則，讓 handleFocus 處理第一次的點擊（使其聚焦並顯示）
  if (showDropdown.value) { // 或者可以使用 document.activeElement === event.target 來判斷是否已經聚焦
    searched.value = ''; // 清空內容
    filterSuggestions(); // 重新過濾建議（此時會顯示所有歷史和熱門）
  }
  showDropdown.value = true; // 確保下拉選單顯示
};

// 選擇建議 - **重要修改**
const selectSuggestion = (item) => {
  searched.value = item;
  preventBlur = true; // **設定旗標，阻止 input 的 blur 事件立即隱藏下拉選單**
  // showDropdown.value = false; // **手動關閉下拉選單**
  handleSearch(); // 執行搜尋
};

// 移除歷史記錄
const removeHistoryItem = (item) => {
  const index = searchHistory.value.indexOf(item);
  if (index !== -1) {
    searchHistory.value.splice(index, 1);
    saveSearchHistory();
    filterSuggestions();
  }
};

// 過濾建議
const filterSuggestions = () => {
  const query = searched.value.toLowerCase().trim();
  filteredHistory.value = query
    ? searchHistory.value.filter((item) => item.toLowerCase().includes(query))
    : [...searchHistory.value];
  filteredHotSearches.value = query
    ? hotSearches.value.filter((item) => item.tag.toLowerCase().includes(query))
    : [...hotSearches.value];
};

// 延遲隱藏下拉選單 - **重要修改**
const hideDropdownWithDelay = () => {
  setTimeout(() => {
    if (!preventBlur) { // **只有當 preventBlur 為 false 時才隱藏下拉選單**
      showDropdown.value = false;
    }
    preventBlur = false; // **重置旗標，為下次互動做準備**
  }, 150); // 稍微增加延遲時間，給點擊事件足夠處理時間
};
</script>

<style scoped>
.hero-section {
  background-color: #fff;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  margin: 20px;
  border-radius: 8px;
}

.search-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.search-container input {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border: 1px solid #ccc;
  border-radius: 4px;
  outline: none;
}

.search-container button {
  padding: 12px 20px;
  background-color: #ffba20;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-left: 10px;
  width: 100px;
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
  margin-top: 5px;
}

.search-section {
  padding: 10px;
}

.search-section h4 {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  padding: 0 10px;
}

.search-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.search-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  transition: background-color 0.2s;
}

.search-item:hover {
  background-color: #f5f5f5;
}

.clear-history {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 14px;
  padding: 5px;
  line-height: 1;
}

.clear-history:hover {
  color: #ffba20;
}
</style>