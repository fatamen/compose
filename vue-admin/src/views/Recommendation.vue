<template>
    <h2>站方推薦標籤管理</h2>
    <div class="p-4">
      <hr>
  
      <div class="table-card mb-4 p-4">
        <div class="row g-3 align-items-center">
          <div class="col-md-6">
            <div class="input-group">
              <input id="newTagInput" v-model="newTag.tag" placeholder="輸入標籤名稱" class="form-control" type="text" />
              <button class="btn btn-custom-add" @click="saveTag" :disabled="!newTag.tag">新增</button>
            </div>
            <div v-if="errorMessage" class="alert alert-danger mt-2" role="alert">
              {{ errorMessage }}
            </div>
          </div>
        </div>
      </div>
  
      <div class="table-card p-4">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <h3>標籤列表</h3>
          <button class="btn btn-success" @click="saveSortOrder">確認排序</button>
        </div>
        <div class="row row-cols-1 g-3">
          <div class="col" v-for="tag in displayedTags" :key="tag.id"
               draggable="true"
               @dragstart="onDragStart(tag)"
               @dragover.prevent
               @drop="onDrop(tag)"
               @dragenter.prevent>
            <div class="tag-item border rounded p-2 d-flex align-items-center justify-content-between">
              <span v-if="editingTagId !== tag.id" class="me-2">優先級: {{ tag.prime }} - {{ tag.tag }}</span>
              <input v-else v-model="editedTagName" @keyup.enter="saveEdit(tag)" @blur="cancelEdit" class="form-control flex-grow-1 me-2" type="text" />
              <div class="tag-actions d-flex gap-2">
                <button v-if="editingTagId !== tag.id" class="btn btn-warning btn-sm" @click="startEdit(tag)">編輯</button>
                <button v-else class="btn btn-success btn-sm" @click="saveEdit(tag)" @mousedown.prevent>儲存</button>
                <button v-if="editingTagId === tag.id" class="btn btn-secondary btn-sm" @click="cancelEdit">取消</button>
                <button class="btn btn-danger btn-sm" @click="deleteTag(tag.id)">刪除</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, computed, onMounted } from 'vue';
  import axios from 'axios';
  import Swal from 'sweetalert2';
  import InputText from 'primevue/inputtext';
  import Button from 'primevue/button';
  import Message from 'primevue/message';
  
  const API_URL = import.meta.env.VITE_RECOM_URL;
  
  const tags = ref([]);
  const newTag = ref({ tag: '' });
  const errorMessage = ref('');
  const draggedTag = ref(null);
  const tempOrder = ref([]);
  const editingTagId = ref(null);
  const editedTagName = ref('');
  
  const displayedTags = computed(() => {
    if (tempOrder.value.length > 0) {
      return tempOrder.value;
    }
    return [...tags.value].sort((a, b) => (a.prime || 0) - (b.prime || 0));
  });
  
  const fetchTags = async () => {
    try {
      const response = await axios.get(API_URL);
      tags.value = response.data;
      tempOrder.value = [];
    } catch (error) {
      errorMessage.value = '無法獲取標籤列表：' + error.message;
      Swal.fire('錯誤', '無法獲取標籤列表，請稍後再試。', 'error');
    }
  };
  
  const onDragStart = (tag) => {
    draggedTag.value = tag;
    if (tempOrder.value.length === 0) {
      tempOrder.value = [...tags.value].sort((a, b) => (a.prime || 0) - (b.prime || 0));
    }
  };
  
  const onDrop = (dropTag) => {
    if (!draggedTag.value) return;
    const fromIndex = tempOrder.value.findIndex(t => t.id === draggedTag.value.id);
    const toIndex = tempOrder.value.findIndex(t => t.id === dropTag.id);
    const newOrder = [...tempOrder.value];
    newOrder.splice(fromIndex, 1);
    newOrder.splice(toIndex, 0, draggedTag.value);
    tempOrder.value = newOrder;
    draggedTag.value = null;
  };
  
  const saveSortOrder = async () => {
  const result = await Swal.fire({
    title: '確認保存排序嗎？',
    text: "您將保存當前標籤的顯示順序。",
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#3085d6',
    cancelButtonColor: '#d33',
    confirmButtonText: '是的，保存！',
    cancelButtonText: '取消'
  });

  if (result.isConfirmed) {
    try {
      // 確保 tempOrder.value 是最新的拖曳排序結果
      if (tempOrder.value.length === 0) {
        // 如果用戶沒有拖曳就點擊保存，則使用當前 tags.value 的排序
        // 這通常不會發生，因為拖曳會自動填充 tempOrder
        Swal.fire('提示', '沒有檢測到排序變動。', 'info');
        return;
      }

      const updatedTags = tempOrder.value.map((tag, index) => ({
        ...tag,
        prime: index + 1 // 根據新順序賦予新的 prime 值
      }));

      // 呼叫後端 API 進行批量更新
      await axios.post(`${API_URL}/batch-update`, updatedTags);

      // 更新本地的 tags 數據為已排序且帶有新 prime 值的數據
      // 這裡直接將 updatedTags 賦值給 tags.value
      tags.value = updatedTags; // <--- 關鍵修改：tags.value 直接是已排序的結果
      
      // 清空 tempOrder.value，因為 tags.value 現在已經是正確的排序了
      // 這樣 displayedTags 會直接使用 tags.value
      tempOrder.value = []; // <--- 關鍵修改：清空臨時排序，讓 displayedTags 使用 tags.value

      errorMessage.value = ''; // 清空錯誤訊息
      Swal.fire('成功', '排序已保存！', 'success');
    } catch (error) {
      errorMessage.value = `保存排序失敗：${error.response?.data?.message || error.message}`;
      Swal.fire('錯誤', '保存排序失敗，請稍後再試。', 'error');
    }
  }
};
  
  const saveTag = async () => {
    if (!newTag.value.tag.trim()) {
      Swal.fire('警告', '標籤名稱不能為空。', 'warning');
      return;
    }
    try {
      const maxPrime = Math.max(...tags.value.map(t => t.prime || 0), 0);
      await axios.post(API_URL, { ...newTag.value, prime: maxPrime + 1 });
      Swal.fire('成功', '標籤新增成功！', 'success');
      newTag.value = { tag: '' };
      await fetchTags();
      errorMessage.value = '';
    } catch (error) {
      errorMessage.value = `操作失敗：${error.response?.data?.message || error.message}`;
      Swal.fire('錯誤', '操作失敗，請稍後再試。', 'error');
    }
  };
  
  const startEdit = (tag) => {
    editingTagId.value = tag.id;
    editedTagName.value = tag.tag;
  };
  
  const cancelEdit = () => {
    editingTagId.value = null;
    editedTagName.value = '';
  };
  
  const saveEdit = async (tag) => {
    if (!editedTagName.value.trim()) {
      Swal.fire('警告', '標籤名稱不能為空。', 'warning');
      return;
    }
    try {
      await axios.put(`${API_URL}/${tag.id}`, { id: tag.id, tag: editedTagName.value.trim(), prime: tag.prime });
      const index = tags.value.findIndex(t => t.id === tag.id);
      if (index !== -1) {
        tags.value[index].tag = editedTagName.value.trim();
        tags.value[index].prime = tag.prime;
      }
      editingTagId.value = null;
      editedTagName.value = '';
      Swal.fire('成功', '標籤修改成功！', 'success');
      await fetchTags();
    } catch (error) {
      errorMessage.value = `操作失敗：${error.response?.data?.message || error.message}`;
      Swal.fire('錯誤', '操作失敗，請稍後再試。', 'error');
    }
  };
  
  const deleteTag = async (id) => {
    const result = await Swal.fire({
      title: '確定要刪除此標籤嗎？',
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
        await axios.delete(`${API_URL}/${id}`);
        await fetchTags();
        errorMessage.value = '';
        Swal.fire('已刪除！', '標籤已成功刪除。', 'success');
      } catch (error) {
        errorMessage.value = `刪除失敗：${error.response?.data?.message || error.message}`;
        Swal.fire('錯誤', '刪除失敗，請稍後再試。', 'error');
      }
    }
  };
  
  onMounted(fetchTags);
  </script>
  
  <style scoped>
  .table-card {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
  
  .input-group .form-control {
    flex-grow: 1;
  }
  
  .tag-item {
    background-color: #f8f9fa;
    transition: all 0.2s ease-in-out;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  
  .tag-item:hover {
    background-color: #e2e6ea;
    transform: translateY(-2px);
  }
  
  .tag-item span,
  .tag-item input[type="text"] {
    flex-grow: 1;
    flex-shrink: 1;
    min-width: 0;
    margin-right: 0.5rem;
  }
  
  .tag-actions {
    flex-shrink: 0;
    display: flex;
    gap: 0.5rem;
  }
  
  .btn-custom-add {
    background-color: #0055b3;
    border-color: #0055b3;
    color: #fff;
  }
  
  .btn-custom-add:hover {
    background-color: #003d82;
    border-color: #003d82;
  }
  
  .btn-custom-add:disabled {
    background-color: #6699cc;
    border-color: #6699cc;
    color: #fff;
  }
  </style>