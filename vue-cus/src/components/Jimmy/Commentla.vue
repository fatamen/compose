<template>
    <div class="comment-modal-overlay" @click.self="$emit('close')">
      <div class="comment-modal-content">
        <div class="comment-modal-header">
          <h3>評論</h3>
          <button class="close-button" @click="$emit('close')">&times;</button>
        </div>
  
        <div class="comment-summary">
          <div class="overall-score">
            <span class="score-value">{{ averageScore.toFixed(1) }}</span>
            <div class="stars">
              <i v-for="n in 5" :key="n" :class="['bi', n <= Math.round(averageScore) ? 'bi-star-fill' : 'bi-star']"></i>
            </div>
            <span class="review-count">評論數 ({{ totalReviews }})</span>
          </div>
          <div class="score-distribution">
            <div v-for="score in 5" :key="score" class="score-row">
              <span>{{ score }}會</span>
              <div class="progress-bar-container">
                <div class="progress-bar" :style="{ width: getScorePercentage(score) + '%' }"></div>
              </div>
              <span>{{ scoreCounts[score] || 0 }}</span>
            </div>
          </div>
        </div>
  
        <div class="comment-tabs">
          <Button label="最新評論" :class="{ 'p-button-primary': activeTab === 'latest' }" @click="activeTab = 'latest'" />
          <Button label="評價最高" :class="{ 'p-button-primary': activeTab === 'highest' }" @click="activeTab = 'highest'" />
          <Button label="評價最低" :class="{ 'p-button-primary': activeTab === 'lowest' }" @click="activeTab = 'lowest'" />
        </div>
  
        <div class="comment-list">
          <div v-if="loading" class="loading-message">載入中...</div>
          <div v-else-if="filteredComments.length === 0" class="no-comments-message">
            目前沒有評論。
          </div>
          <div v-else>
            <div v-for="comment in filteredComments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <div class="user-info">
                  <i class="bi bi-person-circle user-avatar"></i>
                  <span class="user-name">{{ comment.userName || '匿名使用者' }}</span>
                </div>
                <div class="comment-score-stars">
                  <i v-for="n in 5" :key="n" :class="['bi', n <= comment.score ? 'bi-star-fill' : 'bi-star']"></i>
                </div>
              </div>
              <div class="comment-content">
                <p>{{ comment.content }}</p>
                <div v-if="comment.reply" class="comment-reply">
                  <span class="reply-label">店家回覆:</span>
                  <p>{{ comment.reply }}</p>
                  <span class="reply-time">{{ formatDateTime(comment.replyUpdateTime) }}</span>
                </div>
              </div>
              <div class="comment-meta">
                <span class="comment-time">{{ formatDateTime(comment.createTime) }}</span>
                <div class="comment-actions">
                  <i class="bi bi-hand-thumbs-up"></i>
                  <span>贊</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, computed, watch } from 'vue';
  import axios from 'axios';
  import { format } from 'date-fns';
  
  const props = defineProps({
    storeId: {
      type: Number,
      required: true
    }
  });
  
  const emit = defineEmits(['close']);
  
  const comments = ref([]);
  const loading = ref(true);
  const activeTab = ref('latest'); // 'latest', 'highest', 'lowest'
  
  const fetchComments = async () => {
    loading.value = true;
    try {
      // 假設您的後端 API 支援按 storeId 過濾評論
      const response = await axios.get(`http://localhost:8080/comment/store/${props.storeId}`);
      comments.value = response.data;
  
      // 為了顯示使用者名稱，這裡假設 CommentBean 有一個關聯的 UserBean，且 UserBean 有一個 name 屬性。
      // 如果您的 CommentBean 中沒有直接提供使用者名稱，您可能需要在後端進行聯結查詢或在前端額外發送請求。
      // 這裡我們模擬一個 userName，實際應用中應從後端獲取。
      comments.value = comments.value.map(comment => ({
        ...comment,
        userName: comment.user ? comment.user.name : '匿名使用者' // 假設 UserBean 有 name 屬性
      }));
  
    } catch (error) {
      console.error('Error fetching comments:', error);
      // 處理錯誤，例如顯示錯誤訊息給用戶
      comments.value = [];
    } finally {
      loading.value = false;
    }
  };
  
  const averageScore = computed(() => {
    if (comments.value.length === 0) return 0;
    const total = comments.value.reduce((sum, comment) => sum + comment.score, 0);
    return total / comments.value.length;
  });
  
  const totalReviews = computed(() => comments.value.length);
  
  const scoreCounts = computed(() => {
    const counts = {};
    for (let i = 1; i <= 5; i++) {
      counts[i] = comments.value.filter(comment => comment.score === i).length;
    }
    return counts;
  });
  
  const getScorePercentage = (score) => {
    if (totalReviews.value === 0) return 0;
    return (scoreCounts.value[score] / totalReviews.value) * 100;
  };
  
  const filteredComments = computed(() => {
    let sortedComments = [...comments.value];
    if (activeTab.value === 'latest') {
      sortedComments.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
    } else if (activeTab.value === 'highest') {
      sortedComments.sort((a, b) => b.score - a.score);
    } else if (activeTab.value === 'lowest') {
      sortedComments.sort((a, b) => a.score - b.score);
    }
    return sortedComments;
  });
  
  const formatDateTime = (dateTimeString) => {
    if (!dateTimeString) return '';
    return format(new Date(dateTimeString), 'yyyy/MM/dd HH:mm');
  };
  
  onMounted(() => {
    fetchComments();
  });
  
  // 監聽 storeId 變化，如果變化則重新載入評論
  watch(() => props.storeId, () => {
    fetchComments();
  });
  </script>
  
  <style scoped>
  .comment-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
  }
  
  .comment-modal-content {
    background-color: white;
    border-radius: 8px;
    width: 90%;
    max-width: 600px;
    max-height: 80vh;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
  
  .comment-modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #eee;
  }
  
  .comment-modal-header h3 {
    margin: 0;
    font-size: 1.2em;
  }
  
  .close-button {
    background: none;
    border: none;
    font-size: 1.8em;
    cursor: pointer;
    color: #666;
  }
  
  .comment-summary {
    display: flex;
    padding: 20px;
    border-bottom: 1px solid #eee;
    gap: 20px;
  }
  
  .overall-score {
    flex: 1;
    text-align: center;
  }
  
  .score-value {
    font-size: 3em;
    font-weight: bold;
    color: #ff9800;
  }
  
  .stars i {
    color: #ffc107;
    font-size: 1.2em;
  }
  
  .review-count {
    display: block;
    margin-top: 5px;
    color: #666;
  }
  
  .score-distribution {
    flex: 2;
    display: flex;
    flex-direction: column;
    gap: 5px;
  }
  
  .score-row {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .score-row span:first-child {
    width: 40px; /* Adjust as needed */
    text-align: right;
  }
  
  .progress-bar-container {
    flex-grow: 1;
    height: 10px;
    background-color: #f0f0f0;
    border-radius: 5px;
    overflow: hidden;
  }
  
  .progress-bar {
    height: 100%;
    background-color: #ffc107;
    border-radius: 5px;
    transition: width 0.3s ease-in-out;
  }
  
  .comment-tabs {
    display: flex;
    justify-content: center;
    padding: 15px 20px;
    border-bottom: 1px solid #eee;
    gap: 10px;
  }
  
  .comment-tabs .p-button {
    background-color: #f0f0f0;
    color: #333;
    border: none;
    padding: 8px 15px;
    border-radius: 5px;
    cursor: pointer;
  }
  
  .comment-tabs .p-button.p-button-primary {
    background-color: #007bff;
    color: white;
  }
  
  .comment-list {
    flex-grow: 1;
    overflow-y: auto;
    padding: 20px;
  }
  
  .comment-item {
    border: 1px solid #eee;
    border-radius: 8px;
    padding: 15px;
    margin-bottom: 15px;
    background-color: #fff;
  }
  
  .comment-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  
  .user-avatar {
    font-size: 1.5em;
    color: #555;
  }
  
  .user-name {
    font-weight: bold;
  }
  
  .comment-score-stars i {
    color: #ffc107;
    font-size: 1em;
  }
  
  .comment-content p {
    margin: 0 0 10px 0;
    line-height: 1.6;
  }
  
  .comment-reply {
    background-color: #f9f9f9;
    border-left: 3px solid #007bff;
    padding: 10px;
    margin-top: 10px;
    border-radius: 4px;
  }
  
  .reply-label {
    font-weight: bold;
    color: #007bff;
    margin-right: 5px;
  }
  
  .reply-time {
    font-size: 0.8em;
    color: #999;
    display: block;
    margin-top: 5px;
    text-align: right;
  }
  
  .comment-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 0.9em;
    color: #888;
    margin-top: 10px;
  }
  
  .comment-actions i {
    margin-right: 5px;
    cursor: pointer;
  }
  
  .loading-message, .no-comments-message {
    text-align: center;
    padding: 20px;
    color: #666;
  }
  </style>