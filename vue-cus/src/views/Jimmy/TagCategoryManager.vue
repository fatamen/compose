<template>
    <div class="container mt-4">
      <Message v-if="message" :severity="message.type" :life="3000">{{ message.text }}</Message>
  
      <div class="card p-4 mb-5">
        <h2 class="mb-4">標籤管理 (Tags)</h2>
        <div class="d-flex mb-3">
          <InputText
            v-model="newTag.name"
            placeholder="新標籤名稱"
            class="p-inputtext-sm me-2"
          />
          <Button
            label="新增標籤"
            icon="pi pi-plus"
            @click="addTag"
            :loading="loading.tag.add"
          />
        </div>
  
        <div class="table-responsive">
          <table class="table table-hover">
            <thead>
              <tr>
                <th scope="col">ID</th>
                <th scope="col">名稱</th>
                <th scope="col">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tag in tags" :key="tag.id">
                <td>{{ tag.id }}</td>
                <td>
                  <span v-if="!tag.editing">{{ tag.name }}</span>
                  <InputText
                    v-else
                    v-model="tag.name"
                    @keyup.enter="saveTag(tag)"
                    @keyup.escape="cancelEditTag(tag)"
                    class="p-inputtext-sm"
                  />
                </td>
                <td>
                  <Button
                    v-if="!tag.editing"
                    icon="pi pi-pencil"
                    class="p-button-rounded p-button-info me-2"
                    @click="editTag(tag)"
                    aria-label="編輯標籤"
                  />
                  <Button
                    v-else
                    icon="pi pi-check"
                    class="p-button-rounded p-button-success me-2"
                    @click="saveTag(tag)"
                    :loading="loading.tag.save[tag.id]"
                    aria-label="保存標籤"
                  />
                  <Button
                    v-if="tag.editing"
                    icon="pi pi-times"
                    class="p-button-rounded p-button-secondary me-2"
                    @click="cancelEditTag(tag)"
                    aria-label="取消編輯"
                  />
                  <Button
                    icon="pi pi-trash"
                    class="p-button-rounded p-button-danger"
                    @click="deleteTag(tag.id)"
                    :loading="loading.tag.delete[tag.id]"
                    aria-label="刪除標籤"
                  />
                </td>
              </tr>
              <tr v-if="tags.length === 0">
                <td colspan="3" class="text-center">目前沒有標籤。</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
  
      <div class="card p-4">
        <h2 class="mb-4">分類管理 (Categories)</h2>
        <div class="d-flex mb-3">
          <InputText
            v-model="newCategory.name"
            placeholder="新分類名稱"
            class="p-inputtext-sm me-2"
          />
          <Button
            label="新增分類"
            icon="pi pi-plus"
            @click="addCategory"
            :loading="loading.category.add"
          />
        </div>
  
        <div class="table-responsive">
          <table class="table table-hover">
            <thead>
              <tr>
                <th scope="col">ID</th>
                <th scope="col">名稱</th>
                <th scope="col">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="category in categories" :key="category.id">
                <td>{{ category.id }}</td>
                <td>
                  <span v-if="!category.editing">{{ category.name }}</span>
                  <InputText
                    v-else
                    v-model="category.name"
                    @keyup.enter="saveCategory(category)"
                    @keyup.escape="cancelEditCategory(category)"
                    class="p-inputtext-sm"
                  />
                </td>
                <td>
                  <Button
                    v-if="!category.editing"
                    icon="pi pi-pencil"
                    class="p-button-rounded p-button-info me-2"
                    @click="editCategory(category)"
                    aria-label="編輯分類"
                  />
                  <Button
                    v-else
                    icon="pi p-check"
                    class="p-button-rounded p-button-success me-2"
                    @click="saveCategory(category)"
                    :loading="loading.category.save[category.id]"
                    aria-label="保存分類"
                  />
                  <Button
                    v-if="category.editing"
                    icon="pi pi-times"
                    class="p-button-rounded p-button-secondary me-2"
                    @click="cancelEditCategory(category)"
                    aria-label="取消編輯"
                  />
                  <Button
                    icon="pi pi-trash"
                    class="p-button-rounded p-button-danger"
                    @click="deleteCategory(category.id)"
                    :loading="loading.category.delete[category.id]"
                    aria-label="刪除分類"
                  />
                </td>
              </tr>
              <tr v-if="categories.length === 0">
                <td colspan="3" class="text-center">目前沒有分類。</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, reactive } from 'vue';
  import axios from 'axios';
  import { useRoute } from 'vue-router'; // 引入 useRoute
  
  // 定義一個訊息狀態
  const message = ref(null);
  
  // API 基本路徑
  const API_BASE_URL = 'http://localhost:8080/api'; // 請根據你的後端實際運行地址調整
  
  // 標籤相關狀態
  const tags = ref([]);
  const newTag = reactive({ name: '' });
  
  // 分類相關狀態
  const categories = ref([]);
  const newCategory = reactive({ name: '' });
  
  // 加載狀態
  const loading = reactive({
    tag: {
      add: false,
      save: {}, // 用於保存每個標籤的獨立加載狀態
      delete: {}, // 用於刪除每個標籤的獨立加載狀態
    },
    category: {
      add: false,
      save: {}, // 用於保存每個分類的獨立加載狀態
      delete: {}, // 用於刪除每個分類的獨立加載狀態
    }
  });
  
  // 訊息顯示函數
  const showMessage = (type, text) => {
    message.value = { type, text };
    setTimeout(() => {
      message.value = null;
    }, 3000);
  };
  
  // --- 標籤相關操作 ---
  
  const fetchTags = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/tags`);
      tags.value = response.data.map(tag => ({ ...tag, editing: false, originalName: tag.name }));
    } catch (error) {
      console.error('獲取標籤失敗:', error);
      showMessage('error', '獲取標籤失敗！');
    }
  };
  
  const addTag = async () => {
    if (!newTag.name.trim()) {
      showMessage('warn', '標籤名稱不能為空。');
      return;
    }
    loading.tag.add = true;
    try {
      const response = await axios.post(`${API_BASE_URL}/tags`, { name: newTag.name });
      tags.value.push({ ...response.data, editing: false, originalName: response.data.name });
      newTag.name = '';
      showMessage('success', '標籤新增成功！');
    } catch (error) {
      console.error('新增標籤失敗:', error);
      showMessage('error', '新增標籤失敗！');
    } finally {
      loading.tag.add = false;
    }
  };
  
  const editTag = (tag) => {
    tag.editing = true;
    tag.originalName = tag.name; // 保存原始名稱以便取消
  };
  
  const saveTag = async (tag) => {
    if (!tag.name.trim()) {
      showMessage('warn', '標籤名稱不能為空。');
      return;
    }
    loading.tag.save[tag.id] = true;
    try {
      const response = await axios.put(`${API_BASE_URL}/tags/${tag.id}`, { name: tag.name });
      Object.assign(tag, { ...response.data, editing: false, originalName: response.data.name });
      showMessage('success', '標籤更新成功！');
    } catch (error) {
      console.error('更新標籤失敗:', error);
      showMessage('error', '更新標籤失敗！');
      tag.name = tag.originalName; // 恢復原始名稱
    } finally {
      loading.tag.save[tag.id] = false;
      tag.editing = false;
    }
  };
  
  const cancelEditTag = (tag) => {
    tag.name = tag.originalName;
    tag.editing = false;
  };
  
  const deleteTag = async (id) => {
    if (!confirm('確定要刪除此標籤嗎？')) return;
    loading.tag.delete[id] = true;
    try {
      await axios.delete(`${API_BASE_URL}/tags/${id}`);
      tags.value = tags.value.filter(tag => tag.id !== id);
      showMessage('success', '標籤刪除成功！');
    } catch (error) {
      console.error('刪除標籤失敗:', error);
      showMessage('error', '刪除標籤失敗！');
    } finally {
      loading.tag.delete[id] = false;
    }
  };
  
  // --- 分類相關操作 ---
  
  const fetchCategories = async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/categories`);
      categories.value = response.data.map(category => ({ ...category, editing: false, originalName: category.name }));
    } catch (error) {
      console.error('獲取分類失敗:', error);
      showMessage('error', '獲取分類失敗！');
    }
  };
  
  const addCategory = async () => {
    if (!newCategory.name.trim()) {
      showMessage('warn', '分類名稱不能為空。');
      return;
    }
    loading.category.add = true;
    try {
      const response = await axios.post(`${API_BASE_URL}/categories`, { name: newCategory.name });
      categories.value.push({ ...response.data, editing: false, originalName: response.data.name });
      newCategory.name = '';
      showMessage('success', '分類新增成功！');
    } catch (error) {
      console.error('新增分類失敗:', error);
      showMessage('error', '新增分類失敗！');
    } finally {
      loading.category.add = false;
    }
  };
  
  const editCategory = (category) => {
    category.editing = true;
    category.originalName = category.name; // 保存原始名稱以便取消
  };
  
  const saveCategory = async (category) => {
    if (!category.name.trim()) {
      showMessage('warn', '分類名稱不能為空。');
      return;
    }
    loading.category.save[category.id] = true;
    try {
      const response = await axios.put(`${API_BASE_URL}/categories/${category.id}`, { name: category.name });
      Object.assign(category, { ...response.data, editing: false, originalName: response.data.name });
      showMessage('success', '分類更新成功！');
    } catch (error) {
      console.error('更新分類失敗:', error);
      showMessage('error', '更新分類失敗！');
      category.name = category.originalName; // 恢復原始名稱
    } finally {
      loading.category.save[category.id] = false;
      category.editing = false;
    }
  };
  
  const cancelEditCategory = (category) => {
    category.name = category.originalName;
    category.editing = false;
  };
  
  const deleteCategory = async (id) => {
    if (!confirm('確定要刪除此分類嗎？')) return;
    loading.category.delete[id] = true;
    try {
      await axios.delete(`${API_BASE_URL}/categories/${id}`);
      categories.value = categories.value.filter(category => category.id !== id);
      showMessage('success', '分類刪除成功！');
    } catch (error) {
      console.error('刪除分類失敗:', error);
      showMessage('error', '刪除分類失敗！');
    } finally {
      loading.category.delete[id] = false;
    }
  };
  
  // 組件掛載時獲取數據
  onMounted(() => {
    fetchTags();
    fetchCategories();
  });
  </script>
  
  <style scoped>
  /* 可以根據需要添加組件內的樣式 */
  .container {
    max-width: 900px;
  }
  
  .card {
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
  }
  
  .p-button-sm {
    height: 38px; /* 調整按鈕高度 */
  }
  
  /* 調整輸入框樣式 */
  .p-inputtext-sm {
    flex-grow: 1; /* 讓輸入框佔據更多空間 */
  }
  
  /* 表格的樣式 */
  .table th, .table td {
    vertical-align: middle;
  }
  
  .table tbody tr:hover {
    background-color: #f8f9fa;
  }
  </style>