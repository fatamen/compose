<template>
    <div class="tag-manager-container p-4">
      <h2 class="text-center mb-4">標籤管理</h2>
      <hr>
  
      <div class="card mb-4 p-3 shadow-sm">
        <div class="row g-3 align-items-center">
          <div class="col-md-6">
            <label for="newTagInput" class="form-label">新增標籤:</label>
            <div class="input-group">
              <InputText id="newTagInput" v-model="newTagName" placeholder="輸入新標籤名稱" class="form-control" />
              <Button label="新增" icon="pi pi-plus" @click="addTag" class="p-button-success" />
            </div>
            <Message v-if="addMessage" :severity="addMessageType" :life="3000" @close="addMessage = ''">{{ addMessage }}</Message>
          </div>
          <div class="col-md-6">
            <label for="searchInput" class="form-label">搜尋標籤:</label>
            <div class="input-group">
              <InputText id="searchInput" v-model="searchQuery" placeholder="輸入標籤名稱搜尋" class="form-control" />
              <Button label="清除搜尋" icon="pi pi-times" @click="clearSearch" class="p-button-secondary" />
            </div>
          </div>
        </div>
      </div>
  
      <div class="card p-3 shadow-sm">
        <h3 class="mb-3">所有標籤</h3>
        <div v-if="loading" class="text-center text-primary">
          <i class="pi pi-spinner pi-spin" style="font-size: 2rem"></i> 載入中...
        </div>
        <div v-else-if="error" class="alert alert-danger">
          載入標籤失敗: {{ error }}
        </div>
        <div v-else-if="paginatedTags.length === 0" class="alert alert-info">
          沒有找到任何標籤。
        </div>
        <div v-else>
          <div class="tag-list-grid">
            <div v-for="tag in paginatedTags" :key="tag.id" class="tag-item border rounded p-2 d-flex align-items-center justify-content-between mb-2">
              <span v-if="editingTagId !== tag.id">{{ tag.name }}</span>
              <InputText v-else v-model="editedTagName" @keyup.enter="saveEdit(tag.id)" @blur="cancelEdit" class="flex-grow-1 me-2" />
              <div class="tag-actions">
                <Button v-if="editingTagId !== tag.id" icon="pi pi-pencil" class="p-button-rounded p-button-warning mr-2" @click="startEdit(tag)" />
                <Button v-else icon="pi pi-check" class="p-button-sm p-button-success me-2" @click="saveEdit(tag.id)" @mousedown.prevent/>
                <Button v-if="editingTagId === tag.id" icon="pi pi-times" class="p-button-sm p-button-secondary me-2" @click="cancelEdit" />
                <Button icon="pi pi-trash" class="p-button-rounded p-button-danger" @click="deleteTag(tag.id)" />
              </div>
            </div>
          </div>
  
          <nav v-if="totalPages > 1" aria-label="Tag list pagination">
            <ul class="pagination justify-content-center mt-3">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="goToPage(currentPage - 1)">上一頁</a>
              </li>
              <li class="page-item" v-for="page in totalPages" :key="page" :class="{ active: page === currentPage }">
                <a class="page-link" href="#" @click.prevent="goToPage(page)">{{ page }}</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link" href="#" @click.prevent="goToPage(currentPage + 1)">下一頁</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, computed, watch } from 'vue';
  import axios from 'axios';
  // PrimeVue components are globally registered in main.js, so no explicit import needed here.
  // import InputText from 'primevue/inputtext';
  // import Button from 'primevue/button';
  // import Message from 'primevue/message';
  
  // API 基礎 URL
  const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'; // 請根據你的後端設定調整
  
  // 響應式數據
  const tags = ref([]); // 儲存從後端獲取的所有標籤
  const newTagName = ref(''); // 用於新增標籤的輸入框
  const searchQuery = ref(''); // 用於搜尋標籤的輸入框
  const loading = ref(true); // 載入狀態
  const error = ref(null); // 錯誤訊息
  
  const editingTagId = ref(null); // 當前正在編輯的標籤 ID
  const editedTagName = ref(''); // 編輯時的標籤名稱暫存
  
  const addMessage = ref(''); // 新增/修改/刪除操作的訊息
  const addMessageType = ref('success'); // 訊息類型 (success/error/info)
  
  // 分頁相關
  const itemsPerPage = 10; // 每頁顯示的標籤數量
  const currentPage = ref(1); // 當前頁碼
  
  // 計算屬性
  const filteredTags = computed(() => {
    if (!searchQuery.value) {
      return tags.value;
    }
    const query = searchQuery.value.toLowerCase();
    return tags.value.filter(tag => tag.name.toLowerCase().includes(query));
  });
  
  const totalPages = computed(() => {
    return Math.ceil(filteredTags.value.length / itemsPerPage);
  });
  
  const paginatedTags = computed(() => {
    const start = (currentPage.value - 1) * itemsPerPage;
    const end = start + itemsPerPage;
    return filteredTags.value.slice(start, end);
  });
  
  // 監聽 searchQuery 變化，當搜索內容改變時，重置回第一頁
  watch(searchQuery, () => {
    currentPage.value = 1;
  });
  
  // 函式
  // 獲取所有標籤
  const fetchTags = async () => {
    loading.value = true;
    error.value = null;
    try {
      const response = await axios.get(`${API_URL}/api/tags`);
      tags.value = response.data;
    } catch (err) {
      console.error('獲取標籤失敗:', err);
      error.value = '無法載入標籤，請稍後再試。';
    } finally {
      loading.value = false;
    }
  };
  
  // 新增標籤
  const addTag = async () => {
    if (!newTagName.value.trim()) {
      addMessage.value = '標籤名稱不能為空。';
      addMessageType.value = 'warn';
      return;
    }
    try {
      const response = await axios.post(`${API_URL}/api/tags`, { name: newTagName.value.trim() });
      tags.value.push(response.data); // 將新標籤加入列表
      newTagName.value = ''; // 清空輸入框
      addMessage.value = '標籤新增成功！';
      addMessageType.value = 'success';
      await fetchTags(); // 重新獲取標籤，確保列表更新和排序
    } catch (err) {
      console.error('新增標籤失敗:', err);
      addMessage.value = '新增標籤失敗，請檢查網路或後端。';
      addMessageType.value = 'error';
    }
  };
  
  // 刪除標籤
  const deleteTag = async (id) => {
    if (!confirm('確定要刪除這個標籤嗎？')) {
      return;
    }
    try {
      await axios.delete(`${API_URL}/api/tags/${id}`);
      tags.value = tags.value.filter(tag => tag.id !== id); // 從列表中移除
      addMessage.value = '標籤刪除成功！';
      addMessageType.value = 'success';
    } catch (err) {
      console.error('刪除標籤失敗:', err);
      addMessage.value = '刪除標籤失敗，請檢查網路或後端。';
      addMessageType.value = 'error';
    }
  };
  
  // 開始編輯標籤
  const startEdit = (tag) => {
    editingTagId.value = tag.id;
    editedTagName.value = tag.name;
  };
  
  // 取消編輯
  const cancelEdit = () => {
    editingTagId.value = null;
    editedTagName.value = '';
  };
  
  // 保存編輯
  const saveEdit = async (id) => {
    if (!editedTagName.value.trim()) {
      addMessage.value = '標籤名稱不能為空。';
      addMessageType.value = 'warn';
      return;
    }
    try {
      const response = await axios.put(`${API_URL}/api/tags/${id}`, { id: id, name: editedTagName.value.trim() });
      // 更新列表中對應的標籤名稱
      const index = tags.value.findIndex(tag => tag.id === id);
      if (index !== -1) {
        tags.value[index].name = response.data.name;
      }
      editingTagId.value = null; // 退出編輯模式
      editedTagName.value = ''; // 清空編輯暫存
      addMessage.value = '標籤修改成功！';
      addMessageType.value = 'success';
    } catch (err) {
      console.error('修改標籤失敗:', err);
      addMessage.value = '修改標籤失敗，請檢查網路或後端。';
      addMessageType.value = 'error';
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
  
  // 組件掛載時獲取標籤
  onMounted(() => {
    fetchTags();
  });
  </script>
  
  <style scoped>
  .tag-manager-container {
    max-width: 900px;
    margin: 40px auto;
    background-color: #ffffff;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
  
  .card {
    border: none;
    border-radius: 8px;
  }
  
  .input-group .p-button {
    border-top-left-radius: 0;
    border-bottom-left-radius: 0;
  }
  
  .input-group .form-control {
    border-top-right-radius: 0;
    border-bottom-right-radius: 0;
  }
  
  /* 為了 PrimeVue InputText 與 Bootstrap 表單控制對齊 */
  .p-inputtext.form-control {
    display: flex; /* 讓其內置的輸入框能撐開 */
    align-items: center;
  }
  
  .tag-list-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); /* 自動填充，每列最小寬度280px */
    gap: 15px; /* 項目間距 */
    max-height: 400px; /* 設置最大高度並啟用滾動 */
    overflow-y: auto;
    padding-right: 10px; /* 避免滾動條遮擋內容 */
  }
  
  .tag-item {
    background-color: #f8f9fa;
    transition: all 0.2s ease-in-out;
  }
  
  .tag-item:hover {
    background-color: #e2e6ea;
    transform: translateY(-2px);
  }
  
  .tag-actions .p-button {
    width: 32px; /* 統一按鈕大小 */
    height: 32px;
    padding: 0; /* 移除內部填充 */
    display: flex;
    justify-content: center;
    align-items: center;
  }
  
  /* PrimeVue Message 樣式調整 */
  .p-message {
    margin-top: 10px;
    border-radius: 5px;
  }
  </style>