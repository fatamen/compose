<template>
  <h2>分類管理</h2>
  <div class="p-4">
    <hr>

    <div class="table-card mb-4 p-4">
      <div class="row g-3 align-items-center">
        <div class="col-md-6">
          <label for="newCategoryInput" class="form-label mb-0">新增分類：</label>
          <div class="input-group">
            <input id="newCategoryInput" v-model="newCategoryName" placeholder="輸入新分類名稱" class="form-control" type="text" />
            <button class="btn btn-primary" @click="addCategory">新增</button>
          </div>
        </div>

        <div class="col-md-6">
          <label for="searchCategoryInput" class="form-label mb-0">搜尋分類：</label>
          <div class="input-group">
            <input id="searchCategoryInput" v-model="searchQuery" placeholder="輸入分類名稱搜尋" class="form-control" type="text" />
            <button class="btn btn-secondary" @click="clearSearch">清除搜尋</button>
          </div>
        </div>
      </div>
    </div>

    <div class="table-card p-4">
      <h3 class="mb-3">所有分類</h3>
      <div v-if="loading" class="text-center text-primary py-4">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">載入中...</span>
        </div>
        <p class="mt-2">載入中...</p>
      </div>
      <div v-else-if="error" class="alert alert-danger" role="alert">
        載入分類失敗: {{ error }}
      </div>
      <div v-else-if="paginatedCategories.length === 0" class="alert alert-info" role="alert">
        沒有找到任何分類。
      </div>
      <div v-else>
        <div class="row row-cols-1 row-cols-md-2 g-3">
          <div class="col" v-for="category in paginatedCategories" :key="category.id">
            <div class="tag-item border rounded p-2 d-flex align-items-center justify-content-between">
              <span v-if="editingCategoryId !== category.id">{{ category.name }}</span>
              <input v-else v-model="editedCategoryName" @keyup.enter="saveEdit(category.id)" @blur="cancelEdit" class="form-control flex-grow-1 me-2" type="text" />
              <div class="tag-actions d-flex gap-2">
                <button v-if="editingCategoryId !== category.id" class="btn btn-warning btn-sm" @click="startEdit(category)">編輯</button>
                <button v-else class="btn btn-success btn-sm" @click="saveEdit(category.id)" @mousedown.prevent>儲存</button>
                <button v-if="editingCategoryId === category.id" class="btn btn-secondary btn-sm" @click="cancelEdit">取消</button>
                <button class="btn btn-danger btn-sm" @click="deleteCategory(category.id)">刪除</button>
              </div>
            </div>
          </div>
        </div>

        <div class="pagination d-flex justify-content-end align-items-center pagebar-wrap mt-3">
          <button class="btn btn-outline-secondary me-2"
            :disabled="currentPage === 1"
            @click="goToPage(currentPage - 1)"
          >&lt; 上一頁</button>
          <nav>
            <ul class="pagination mb-0">
              <li class="page-item disabled">
                <span class="page-link">頁數：{{ currentPage }} / {{ totalPages }}</span>
              </li>
            </ul>
          </nav>
          <button class="btn btn-outline-secondary ms-2" :disabled="currentPage === totalPages" @click="goToPage(currentPage + 1)">下一頁 &gt;</button>
          <div class="ms-3">
            <select class="form-select" v-model.number="itemsPerPage" style="width:120px; min-width: 90px;">
              <option :value="5">5/每頁</option>
              <option :value="10">10/每頁</option>
              <option :value="20">20/每頁</option>
            </select>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2'; // 引入 SweetAlert2

// API 基礎 URL
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// 響應式數據
const categories = ref([]); // 儲存從後端獲取的所有分類
const newCategoryName = ref(''); // 用於新增分類的輸入框
const searchQuery = ref(''); // 用於搜尋分類的輸入框
const loading = ref(true); // 載入狀態
const error = ref(null); // 錯誤訊息

const editingCategoryId = ref(null); // 當前正在編輯的分類 ID
const editedCategoryName = ref(''); // 編輯時的分類名稱暫存

// 分頁相關
const itemsPerPage = ref(10); // 每頁顯示的分類數量
const currentPage = ref(1); // 當前頁碼

// 計算屬性
const filteredCategories = computed(() => {
  if (!searchQuery.value) {
    return categories.value;
  }
  const query = searchQuery.value.toLowerCase();
  return categories.value.filter(category => category.name.toLowerCase().includes(query));
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredCategories.value.length / itemsPerPage.value));
});

const paginatedCategories = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredCategories.value.slice(start, end);
});

// 監聽 searchQuery 變化，當搜索內容改變時，重置回第一頁
watch(searchQuery, () => {
  currentPage.value = 1;
});

// 監聽 itemsPerPage 變化，當每頁顯示數量改變時，重置回第一頁
watch(itemsPerPage, () => {
  currentPage.value = 1;
});

// 函式
// 獲取所有分類
const fetchCategories = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await axios.get(`${API_URL}/api/categories`);
    categories.value = response.data;
  } catch (err) {
    console.error('獲取分類失敗:', err);
    error.value = '無法載入分類，請稍後再試。';
    Swal.fire('錯誤', '無法載入分類，請稍後再試。', 'error'); // SweetAlert2 提示
  } finally {
    loading.value = false;
  }
};

// 新增分類
const addCategory = async () => {
  if (!newCategoryName.value.trim()) {
    Swal.fire('警告', '分類名稱不能為空。', 'warning'); // SweetAlert2 提示
    return;
  }
  try {
    const response = await axios.post(`${API_URL}/api/categories`, { name: newCategoryName.value.trim() });
    categories.value.push(response.data); // 將新分類加入列表
    newCategoryName.value = ''; // 清空輸入框
    Swal.fire('成功', '分類新增成功！', 'success'); // SweetAlert2 提示
    await fetchCategories(); // 重新載入分類確保資料最新
  } catch (err) {
    console.error('新增分類失敗:', err);
    Swal.fire('錯誤', '新增分類失敗，請檢查網路或後端。', 'error'); // SweetAlert2 提示
  }
};

// 刪除分類
const deleteCategory = async (id) => {
  const result = await Swal.fire({
    title: '確定要刪除嗎？',
    text: "刪除後將無法恢復！",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#3085d6',
    confirmButtonText: '是的，刪除！',
    cancelButtonText: '取消'
  });

  if (result.isConfirmed) {
    try {
      await axios.delete(`${API_URL}/api/categories/${id}`);
      categories.value = categories.value.filter(category => category.id !== id); // 從列表中移除
      Swal.fire('已刪除！', '分類已成功刪除。', 'success'); // SweetAlert2 提示
    } catch (err) {
      console.error('刪除分類失敗:', err);
      Swal.fire('錯誤', '刪除分類失敗，請檢查網路或後端。', 'error'); // SweetAlert2 提示
    }
  }
};

// 開始編輯分類
const startEdit = (category) => {
  editingCategoryId.value = category.id;
  editedCategoryName.value = category.name;
};

// 取消編輯
const cancelEdit = () => {
  editingCategoryId.value = null;
  editedCategoryName.value = '';
};

// 保存編輯
const saveEdit = async (id) => {
  if (!editedCategoryName.value.trim()) {
    Swal.fire('警告', '分類名稱不能為空。', 'warning'); // SweetAlert2 提示
    return;
  }
  try {
    const response = await axios.put(`${API_URL}/api/categories/${id}`, { id: id, name: editedCategoryName.value.trim() });
    // 更新列表中對應的分類名稱
    const index = categories.value.findIndex(category => category.id === id);
    if (index !== -1) {
      categories.value[index].name = response.data.name;
    }
    editingCategoryId.value = null; // 退出編輯模式
    editedCategoryName.value = ''; // 清空編輯暫存
    Swal.fire('成功', '分類修改成功！', 'success'); // SweetAlert2 提示
  } catch (err) {
    console.error('修改分類失敗:', err);
    Swal.fire('錯誤', '修改分類失敗，請檢查網路或後端。', 'error'); // SweetAlert2 提示
  }
};

// 清除搜索框
const clearSearch = () => {
  searchQuery.value = '';
};

// 分頁跳轉
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

// 組件掛載時獲取分類
onMounted(() => {
  fetchCategories();
});
</script>

<style scoped>
.table-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-bar {
  /* 這裡不再需要明確的 padding，因為已由外層 .p-4 提供 */
}

/* 確保搜尋和新增輸入框的寬度適中 */
/* 由於現在使用了 row 和 col，個別 input-group 的 max-width 應由 col 處理 */
.input-group .form-control {
  /* max-width: 200px; - 移除此行，讓其在 col-md-6 中自動填充 */
  flex-grow: 1; /* 確保輸入框在 input-group 中能撐開 */
}

/* 將標籤列表調整為網格卡片樣式，一行兩個 */
.tag-item {
  background-color: #f8f9fa;
  transition: all 0.2s ease-in-out;
  display: flex; /* 確保flex屬性存在 */
  align-items: center;
  justify-content: space-between;
}

.tag-item:hover {
  background-color: #e2e6ea;
  transform: translateY(-2px);
}

/* 確保名稱或輸入框能夠彈性縮放，並預留右側空間 */
.tag-item span,
.tag-item input[type="text"] { /* 精確指定 input 類型 */
  flex-grow: 1; /* 讓它盡可能佔據空間 */
  flex-shrink: 1; /* 允許它在必要時縮小 */
  min-width: 0; /* 防止內容過長導致溢出 */
  margin-right: 0.5rem; /* 給按鈕組預留一些間距 */
}

/* 確保按鈕組不會被壓縮 */
.tag-actions {
  flex-shrink: 0; /* 阻止這個區域被壓縮 */
  display: flex;
  gap: 0.5rem; /* 保持按鈕間距 */
  /* 可以選擇性地設定一個最小寬度，以確保在極端情況下按鈕不會重疊 */
  /* min-width: 150px; */
}

/* 分頁區塊的樣式與訂閱紀錄頁面保持一致 */
.pagebar-wrap {
  margin-top: 1rem;
}
</style>