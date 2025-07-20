<template>
  <h2>標籤管理</h2>
  <div class="p-4">   
    <hr>

    <div class="table-card mb-4 p-4">
      <div class="row g-3 align-items-center">
        <div class="col-md-6">
          <label for="newTagInput" class="form-label mb-0">新增標籤：</label>
          <div class="input-group">
            <input id="newTagInput" v-model="newTagName" placeholder="輸入新標籤名稱" class="form-control" type="text" />
            <button class="btn btn-primary" @click="addTag">新增</button>
          </div>
          </div>

        <div class="col-md-6">
          <label for="searchInput" class="form-label mb-0">搜尋標籤：</label>
          <div class="input-group">
            <input id="searchInput" v-model="searchQuery" placeholder="輸入標籤名稱搜尋" class="form-control" type="text" />
            <button class="btn btn-secondary" @click="clearSearch">清除搜尋</button>
          </div>
        </div>
      </div>
    </div>

    <div class="table-card p-4">
      <h3 class="mb-3">所有標籤</h3>
      <div v-if="loading" class="text-center text-primary py-4">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">載入中...</span>
        </div>
        <p class="mt-2">載入中...</p>
      </div>
      <div v-else-if="error" class="alert alert-danger" role="alert">
        載入標籤失敗: {{ error }}
      </div>
      <div v-else-if="paginatedTags.length === 0" class="alert alert-info" role="alert">
        沒有找到任何標籤。
      </div>
      <div v-else>
        <div class="row row-cols-1 row-cols-md-2 g-3">
          <div class="col" v-for="tag in paginatedTags" :key="tag.id">
            <div class="tag-item border rounded p-2 d-flex align-items-center justify-content-between">
              <span v-if="editingTagId !== tag.id">{{ tag.name }}</span>
              <input v-else v-model="editedTagName" @keyup.enter="saveEdit(tag.id)" @blur="cancelEdit" class="form-control flex-grow-1 me-2" type="text" />
              <div class="tag-actions d-flex gap-2">
                <button v-if="editingTagId !== tag.id" class="btn btn-warning btn-sm" @click="startEdit(tag)">編輯</button>
                <button v-else class="btn btn-success btn-sm" @click="saveEdit(tag.id)" @mousedown.prevent>儲存</button>
                <button v-if="editingTagId === tag.id" class="btn btn-secondary btn-sm" @click="cancelEdit">取消</button>
                <button class="btn btn-danger btn-sm" @click="deleteTag(tag.id)">刪除</button>
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

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const tags = ref([]);
const newTagName = ref('');
const searchQuery = ref('');
const loading = ref(true);
const error = ref(null);

const editingTagId = ref(null);
const editedTagName = ref('');

// 移除 addMessage 和 addMessageType

const itemsPerPage = ref(10);
const currentPage = ref(1);

const filteredTags = computed(() => {
  if (!searchQuery.value) {
    return tags.value;
  }
  const query = searchQuery.value.toLowerCase();
  return tags.value.filter(tag => tag.name.toLowerCase().includes(query));
});

const totalPages = computed(() => {
  return Math.max(1, Math.ceil(filteredTags.value.length / itemsPerPage.value));
});

const paginatedTags = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage.value;
  const end = start + itemsPerPage.value;
  return filteredTags.value.slice(start, end);
});

watch(searchQuery, () => {
  currentPage.value = 1;
});

watch(itemsPerPage, () => {
  currentPage.value = 1;
});

const fetchTags = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await axios.get(`${API_URL}/api/tags`);
    tags.value = response.data;
  } catch (err) {
    console.error('獲取標籤失敗:', err);
    error.value = '無法載入標籤，請稍後再試。';
    Swal.fire('錯誤', '無法載入標籤，請稍後再試。', 'error'); // SweetAlert2 提示
  } finally {
    loading.value = false;
  }
};

const addTag = async () => {
  if (!newTagName.value.trim()) {
    Swal.fire('警告', '標籤名稱不能為空。', 'warning'); // SweetAlert2 提示
    return;
  }
  try {
    const response = await axios.post(`${API_URL}/api/tags`, { name: newTagName.value.trim() });
    tags.value.push(response.data);
    newTagName.value = '';
    Swal.fire('成功', '標籤新增成功！', 'success'); // SweetAlert2 提示
    await fetchTags(); // 重新載入標籤確保資料最新
  } catch (err) {
    console.error('新增標籤失敗:', err);
    Swal.fire('錯誤', '新增標籤失敗，請檢查網路或後端。', 'error'); // SweetAlert2 提示
  }
};

const deleteTag = async (id) => {
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
      await axios.delete(`${API_URL}/api/tags/${id}`);
      tags.value = tags.value.filter(tag => tag.id !== id);
      Swal.fire('已刪除！', '標籤已成功刪除。', 'success'); // SweetAlert2 提示
    } catch (err) {
      console.error('刪除標籤失敗:', err);
      Swal.fire('錯誤', '刪除標籤失敗，請檢查網路或後端。', 'error'); // SweetAlert2 提示
    }
  }
};

const startEdit = (tag) => {
  editingTagId.value = tag.id;
  editedTagName.value = tag.name;
};

const cancelEdit = () => {
  editingTagId.value = null;
  editedTagName.value = '';
};

const saveEdit = async (id) => {
  if (!editedTagName.value.trim()) {
    Swal.fire('警告', '標籤名稱不能為空。', 'warning'); // SweetAlert2 提示
    return;
  }
  try {
    const response = await axios.put(`${API_URL}/api/tags/${id}`, { id: id, name: editedTagName.value.trim() });
    const index = tags.value.findIndex(tag => tag.id === id);
    if (index !== -1) {
      tags.value[index].name = response.data.name;
    }
    editingTagId.value = null;
    editedTagName.value = '';
    Swal.fire('成功', '標籤修改成功！', 'success'); // SweetAlert2 提示
  } catch (err) {
    console.error('修改標籤失敗:', err);
    Swal.fire('錯誤', '修改標籤失敗，請檢查網路或後端。', 'error'); // SweetAlert2 提示
  }
};

const clearSearch = () => {
  searchQuery.value = '';
};

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

onMounted(() => {
  fetchTags();
});
</script>

<style scoped>
.table-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 移除 filter-bar 的 padding-top/bottom，讓它僅作為 flex 容器使用 */
/* 現在外層的 table-card p-4 提供了整體內距 */
.filter-bar {
  /* 這裡不再需要明確的 padding，因為已由外層 .p-4 提供 */
}

/* 訊息提示的關閉按鈕與 Bootstrap alert 搭配 */
/* 此部分樣式隨著 SweetAlert2 的引入，原有的 .alert .btn-close 不再需要 */


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