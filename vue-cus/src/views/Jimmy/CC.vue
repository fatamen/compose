<template>
    <div class="restaurant-comments-page">
      <h1>查看餐廳評論</h1>
  
      <div class="input-section">
        <label for="storeIdInput">請輸入餐廳 ID:</label>
        <InputText id="storeIdInput" v-model="inputStoreId" type="number" placeholder="例如: 1" />
        <Button label="載入評論" @click="loadComments" />
      </div>
  
      <div v-if="showCommentModal && selectedStoreId" class="comment-modal-wrapper">
        <Comment :storeId="selectedStoreId" @close="showCommentModal = false" />
      </div>
  
      <Message v-if="errorMessage" severity="error" :life="3000">{{ errorMessage }}</Message>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue';
  import Comment from '@/components/Jimmy/Commentla.vue'; // 確保路徑正確
  
  const inputStoreId = ref(null);
  const selectedStoreId = ref(null);
  const showCommentModal = ref(false);
  const errorMessage = ref('');
  
  const loadComments = () => {
    errorMessage.value = '';
    if (inputStoreId.value && inputStoreId.value > 0) {
      selectedStoreId.value = inputStoreId.value;
      showCommentModal.value = true;
    } else {
      errorMessage.value = '請輸入有效的餐廳 ID。';
    }
  };
  </script>
  
  <style scoped>
  .restaurant-comments-page {
    padding: 20px;
    max-width: 800px;
    margin: 0 auto;
  }
  
  h1 {
    text-align: center;
    margin-bottom: 30px;
  }
  
  .input-section {
    display: flex;
    gap: 15px;
    align-items: center;
    margin-bottom: 30px;
    justify-content: center;
  }
  
  .input-section label {
    font-weight: bold;
  }
  
  .input-section InputText {
    flex-grow: 1;
    max-width: 200px;
  }
  
  .comment-modal-wrapper {
    /* 這是一個佔位符，實際的 Comment.vue 會是 fixed 定位 */
    /* 可以根據需要調整這個 wrapper 的樣式 */
    min-height: 400px; /* 為了在模態框關閉時避免頁面跳動 */
  }
  </style>