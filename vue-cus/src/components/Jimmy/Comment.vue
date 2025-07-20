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
            <span>{{ score }}星</span>
            <div class="progress-bar-container">
              <div class="progress-bar" :style="{ width: getScorePercentage(score) + '%' }"></div>
            </div>
            <span>{{ scoreCounts[score] || 0 }}</span>
          </div>
        </div>
      </div>

      <div class="comment-tabs">
        <Button
          label="最新評論"
          :severity="activeTab === 'latest' ? 'info' : 'secondary'"
          @click="activeTab = 'latest'"
        />
        <Button
          label="評價最高"
          :severity="activeTab === 'highest' ? 'info' : 'secondary'"
          @click="activeTab = 'highest'"
        />
        <Button
          label="評價最低"
          :severity="activeTab === 'lowest' ? 'info' : 'secondary'"
          @click="activeTab = 'lowest'"
        />
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
                <ReportButton @openReportModal="openReportModal(comment.id)" />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <ReportModal
    v-if="showReportModal"
    :commentId="selectedCommentIdForReport"
    @close="closeReportModal"
    @reportSubmitted="handleReportSubmitted"
  />
</template>

<script setup>
import { ref, onMounted, computed, watch, onUnmounted } from 'vue'; // 新增 onUnmounted
import axios from '@/plungins/axios.js';
import { format } from 'date-fns';
import Button from 'primevue/button';

// 引入新的檢舉相關組件
import ReportButton from '@/components/Jimmy/ReportButton.vue';
import ReportModal from '@/components/Jimmy/ReportModal.vue';

// --- 新增：STOMP 和 SockJS 相關的 import ---
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
// --- 結束新增 ---

const props = defineProps({
  storeId: {
    type: Number,
    required: true
  }
});

const emit = defineEmits(['close']);

const comments = ref([]);
const loading = ref(true);
const activeTab = ref('latest');

// 檢舉模態框的狀態
const showReportModal = ref(false);
const selectedCommentIdForReport = ref(null); // 用於儲存要檢舉的評論 ID

// --- 新增：WebSocket URL 和 STOMP Client 相關變數 ---
// 請確保你的 .env 檔案有定義 VITE_API_URL，例如：VITE_API_URL=http://localhost:8080
const API_URL = import.meta.env.VITE_API_URL;
let stompClient = null;
let commentSubscription = null; // 用來儲存評論主題的訂閱
// --- 結束新增 WebSocket 變數 ---


const fetchComments = async () => {
  loading.value = true;
  try {
    const response = await axios.get(`/comment/store/${props.storeId}`);
    comments.value = Array.isArray(response.data) ? response.data : [];

    comments.value = comments.value.map(comment => ({
      ...comment,
      userName: comment.userName || '匿名使用者' // 直接使用 CommentResponseDTO 中的 userName
    }));

  } catch (error) {
    console.error('Error fetching comments:', error);
    comments.value = [];
  } finally {
    loading.value = false;
  }
};

// --- 新增：STOMP WebSocket 連線函數 ---
const connectStompWebSocket = () => {
  // 如果 STOMP Client 已經存在且已連接，則不需要重複連接
  if (stompClient && stompClient.connected) {
    console.log('CommentModal.vue: STOMP Client 已連接，無需重複連接。');
    return;
  }

  // 假設 WebSocket 連線端點與 API_URL 基礎一致，並帶有 /ws
  const wsUrl = `${API_URL}/ws`; // 請根據你的 Spring Boot WebSocket 配置來設定

  console.log(`CommentModal.vue: 嘗試連線到 WebSocket (STOMP) 伺服器: ${wsUrl}`);

  stompClient = new Client({
    webSocketFactory: () => new SockJS(wsUrl),
    debug: (str) => {
      // console.log('STOMP Debug (CommentModal): ' + str); // 在開發模式下可以開啟更多日誌
    },
    reconnectDelay: 5000, // 斷線後 5 秒嘗試重連
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  stompClient.onConnect = (frame) => {
    console.log('CommentModal.vue: STOMP Connected: ' + frame);

    // 訂閱特定餐廳的評論更新主題
    // === 這裡非常重要：請務必確認後端推播評論更新的主題格式！ ===
    // 例如：/topic/store-comments/123 或 /topic/comments/store/123
    const commentTopic = `/topic/store-comments/${props.storeId}`; // <-- 請根據後端實際主題修改
    console.log(`CommentModal.vue: 訂閱主題: ${commentTopic}`);

    // 取消舊的訂閱，確保只訂閱一次 (尤其是在 storeId 改變時)
    if (commentSubscription) {
      commentSubscription.unsubscribe();
      console.log('CommentModal.vue: 已取消舊的評論訂閱。');
    }

    commentSubscription = stompClient.subscribe(commentTopic, (message) => {
      // 收到評論更新訊息
      try {
        const update = JSON.parse(message.body);
        console.log('CommentModal.vue: 收到 STOMP 訊息 (評論更新):', update);

        // 根據訊息類型處理：新增評論或更新現有評論
        if (update.type === 'NEW_COMMENT' && update.comment) {
          // 添加新評論
          const newComment = {
            ...update.comment,
            userName: update.comment.userName || '匿名使用者'
          };
          // 檢查是否已存在，防止重複添加（雖然通常後端會避免重複發送）
          if (!comments.value.some(c => c.id === newComment.id)) {
            comments.value.unshift(newComment); // 將新評論添加到列表開頭
            console.log('CommentModal.vue: 新評論已添加。');
          }
          // 由於評論數量和平均分數會變化，computed 屬性會自動更新
        } else if (update.type === 'UPDATE_COMMENT' && update.comment) {
          // 更新現有評論 (例如商家回覆)
          const index = comments.value.findIndex(c => c.id === update.comment.id);
          if (index !== -1) {
            comments.value[index] = {
              ...comments.value[index], // 保留現有數據
              ...update.comment, // 覆蓋更新的數據
              userName: update.comment.userName || comments.value[index].userName || '匿名使用者'
            };
            console.log(`CommentModal.vue: 評論 ID ${update.comment.id} 已更新。`);
          }
        }
      } catch (error) {
        console.error('CommentModal.vue: 解析 STOMP 訊息失敗:', error);
      }
    }, (error) => {
      console.error(`CommentModal.vue: 訂閱 ${commentTopic} 錯誤:`, error);
    });
  };

  stompClient.onStompError = (frame) => {
    console.error('CommentModal.vue: Broker reported STOMP error: ' + frame.headers['message']);
    console.error('CommentModal.vue: Additional STOMP details: ' + frame.body);
  };

  stompClient.onDisconnect = (frame) => {
    console.log('CommentModal.vue: STOMP 連線已關閉:', frame);
    // 斷線後可以嘗試重連
    setTimeout(connectStompWebSocket, 5000); // 5 秒後重試連線
  };

  stompClient.activate(); // 啟動 STOMP 連線
};

// --- 新增：STOMP WebSocket 斷開連線函數 ---
const disconnectStompWebSocket = () => {
  if (stompClient) {
    console.log('CommentModal.vue: 關閉 STOMP WebSocket 連線...');
    if (commentSubscription) {
      commentSubscription.unsubscribe(); // 取消評論主題訂閱
      commentSubscription = null;
    }
    stompClient.deactivate(); // 斷開 STOMP 連線
    stompClient = null;
  }
};
// --- 結束新增 WebSocket 函數 ---


const averageScore = computed(() => {
  if (comments.value.length === 0) return 0;
  const total = comments.value.reduce((sum, comment) => sum + (comment.score || 0), 0);
  return total / comments.value.length;
});

const totalReviews = computed(() => comments.value.length);

const scoreCounts = computed(() => {
  const counts = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
  comments.value.forEach(comment => {
    if (comment.score >= 1 && comment.score <= 5) {
      counts[comment.score]++;
    }
  });
  return counts;
});

const getScorePercentage = (score) => {
  if (totalReviews.value === 0) return 0;
  return (scoreCounts.value[score] / totalReviews.value) * 100;
};

const filteredComments = computed(() => {
  let sortedComments = [...comments.value];
  if (activeTab.value === 'latest') {
    sortedComments.sort((a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime());
  } else if (activeTab.value === 'highest') {
    sortedComments.sort((a, b) => b.score - a.score);
  } else if (activeTab.value === 'lowest') {
    sortedComments.sort((a, b) => a.score - b.score);
  }
  return sortedComments;
});

const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '';
  try {
    return format(new Date(dateTimeString), 'yyyy/MM/dd HH:mm');
  } catch (e) {
    console.error("Error formatting date:", e);
    return dateTimeString;
  }
};

// 打開檢舉模態框
const openReportModal = (commentId) => {
  selectedCommentIdForReport.value = commentId;
  showReportModal.value = true;
};

// 關閉檢舉模態框
const closeReportModal = () => {
  showReportModal.value = false;
  selectedCommentIdForReport.value = null;
};

// 處理檢舉成功後的回調
const handleReportSubmitted = () => {
  console.log('檢舉已提交成功！');
};

// --- 新增：在生命週期鉤子中管理 WebSocket 連線 ---
onMounted(() => {
  fetchComments();
  connectStompWebSocket(); // 在組件掛載時建立 WebSocket 連線
});

onUnmounted(() => {
  disconnectStompWebSocket(); // 在組件卸載時關閉 WebSocket 連線，防止記憶體洩漏
});

// 當 storeId 改變時，重新獲取評論並重新訂閱 WebSocket
watch(() => props.storeId, (newStoreId, oldStoreId) => {
  if (newStoreId !== oldStoreId) {
    console.log(`CommentModal.vue: storeId 從 ${oldStoreId} 變更為 ${newStoreId}，重新載入評論並重新連線 WebSocket。`);
    fetchComments();
    // 重新連線 WebSocket 以訂閱新的 storeId 主題
    disconnectStompWebSocket(); // 先斷開舊的
    connectStompWebSocket();    // 再連線新的
  }
}, { immediate: true }); // immediate: true 確保在組件初始化時也觸發一次
</script>

<style scoped>
/* 保持與您現有的 CommentModal.vue 樣式一致，只添加 ReportButton 相關的樣式或調整 */

/* 模態框疊層 */
.comment-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 半透明黑色背景 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000; /* 確保在其他內容之上 */
}

/* 模態框內容區域 */
.comment-modal-content {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px; /* 最大寬度 */
  max-height: 80vh; /* 最大高度，防止過高 */
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 確保內容不溢出圓角 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2); /* 輕微陰影效果 */
}

/* 模態框頭部 */
.comment-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee; /* 底部細線 */
  flex-shrink: 0; /* 防止被內容擠壓 */
}

.comment-modal-header h3 {
  margin: 0;
  font-size: 1.2em; /* 標題字體大小 */
  color: #333;
}

/* 關閉按鈕 */
.close-button {
  background: none;
  border: none;
  font-size: 1.8em;
  cursor: pointer;
  color: #666;
  padding: 5px; /* 增加點擊區域 */
  line-height: 1; /* 確保 X 符號居中 */
}

.close-button:hover {
  color: #000;
}

/* 評論總結區塊 */
.comment-summary {
  display: flex;
  padding: 20px;
  border-bottom: 1px solid #eee;
  gap: 20px; /* 兩個子元素之間的間距 */
  background-color: #fcfcfc; /* 輕微背景色 */
  flex-shrink: 0;
}

/* 整體評分區塊 */
.overall-score {
  flex: 1; /* 佔用可用空間的 1 份 */
  text-align: center;
  border-right: 1px solid #eee; /* 分隔線 */
  padding-right: 20px; /* 與分隔線的距離 */
}

.score-value {
  font-size: 3em;
  font-weight: bold;
  color: #ff9800; /* 主要評分顏色 */
  display: block; /* 確保獨佔一行 */
  margin-bottom: 5px;
}

.stars i {
  color: #ffc107; /* 星星顏色 */
  font-size: 1.2em;
  margin: 0 2px; /* 星星間距 */
}

.review-count {
  display: block;
  margin-top: 5px;
  color: #666;
  font-size: 0.9em;
}

/* 評分分佈區塊 */
.score-distribution {
  flex: 2; /* 佔用可用空間的 2 份 */
  display: flex;
  flex-direction: column;
  gap: 5px; /* 行間距 */
  padding-left: 20px; /* 與分隔線的距離 */
}

.score-row {
  display: flex;
  align-items: center;
  gap: 10px; /* 元素間距 */
}

.score-row span:first-child {
  width: 40px; /* 確保 "X星" 的寬度一致 */
  text-align: right;
  color: #555;
  font-size: 0.9em;
}

.progress-bar-container {
  flex-grow: 1; /* 填充剩餘空間 */
  height: 10px;
  background-color: #f0f0f0;
  border-radius: 5px;
  overflow: hidden; /* 確保進度條圓角 */
}

.progress-bar {
  height: 100%;
  background-color: #ffc107; /* 進度條顏色 */
  border-radius: 5px;
  transition: width 0.3s ease-in-out; /* 寬度變化動畫 */
}

/* 評論篩選標籤區塊 */
.comment-tabs {
  display: flex;
  justify-content: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
  gap: 10px;
  flex-shrink: 0;
}

/* PrimeVue Button 樣式覆蓋 */
.comment-tabs .p-button {
  background-color: #f0f0f0;
  color: #333;
  border: none;
  padding: 8px 15px;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.2s, color 0.2s;
  flex-grow: 1; /* 讓按鈕平均分佈 */
  max-width: 120px; /* 限制按鈕最大寬度 */
}

.comment-tabs .p-button:hover {
  background-color: #e0e0e0;
}

/* 選中按鈕的樣式 */
.comment-tabs .p-button[severity="info"] { /* PrimeVue severity="info" */
  background-color: #007bff; /* 藍色 */
  color: white;
}

.comment-tabs .p-button[severity="info"]:hover {
  background-color: #0056b3;
}


/* 評論列表 */
.comment-list {
  flex-grow: 1; /* 讓列表填充剩餘空間 */
  overflow-y: auto; /* 評論多時可滾動 */
  padding: 20px;
}

/* 載入中 / 無評論訊息 */
.loading-message,
.no-comments-message {
  text-align: center;
  padding: 20px;
  color: #666;
  font-size: 1.1em;
}

/* 單個評論項目 */
.comment-item {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px; /* 評論項目之間的間距 */
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05); /* 輕微陰影 */
}

/* 評論頭部 */
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
  font-size: 1.5em; /* 用戶頭像圖標大小 */
  color: #555;
}

.user-name {
  font-weight: bold;
  color: #333;
}

.comment-score-stars i {
  color: #ffc107; /* 評論星級顏色 */
  font-size: 1em;
  margin-left: 2px;
}

/* 評論內容 */
.comment-content p {
  margin: 0 0 10px 0;
  line-height: 1.6;
  color: #444;
}

/* 商家回覆 */
.comment-reply {
  background-color: #f9f9f9;
  border-left: 3px solid #007bff; /* 回覆的藍色邊框 */
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

/* 評論元數據 (時間、點贊) */
.comment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9em;
  color: #888;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #eee; /* 上方虛線分隔 */
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 15px; /* 增加操作間距 */
}

.comment-actions i {
  margin-right: 5px;
  cursor: pointer;
  color: #666;
}

.comment-actions i:hover {
  color: #007bff;
}
</style>