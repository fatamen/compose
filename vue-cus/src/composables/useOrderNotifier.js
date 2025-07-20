// src/composables/useOrderNotifier.js
import { ref, onMounted, onUnmounted, watch } from 'vue'; // 引入 watch
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';

/**
 * 處理 WebSocket 訂單通知的 Composables
 * @param {string} type - 訂閱類型：'store' (店家) 或 'user' (用戶)
 * @param {Ref<number|string>} idRef - storeId 或 userId 的 Ref 物件
 * @param {Function} fetchOrdersCallback - 收到通知時調用的回呼函式，會傳入當前的 ID 值
 */
export function useOrderNotifier(type, idRef, fetchOrdersCallback) {
  const isConnected = ref(false);
  let stompClient = null;
  let currentSubscription = null; // 用來儲存當前訂閱，方便取消

  const connectWebSocket = () => {
    // 避免重複連接或在 ID 無效時連接
    if (stompClient && stompClient.connected) {
      console.log('useOrderNotifier: WebSocket 已連接，無需重複連接。');
      return;
    }
    // 如果 idRef.value 無效（例如為 null），則不連接
    if (!idRef.value) {
      console.warn('useOrderNotifier: ID 為空，無法建立 WebSocket 連線。等待有效 ID...');
      return;
    }

    console.log(`useOrderNotifier: 正在嘗試連接 WebSocket... Type: ${type}, ID: ${idRef.value}`);

    const wsUrl = `${import.meta.env.VITE_API_URL}/ws`;
    const socket = new SockJS(wsUrl);
    stompClient = Stomp.over(socket);

    stompClient.connect({}, (frame) => {
      // 連接成功
      isConnected.value = true;
      console.log('STOMP 連接成功:', frame);

      let topicPath;
      if (type === 'store') {
        topicPath = `/topic/orders/${idRef.value}`; // 店家訂單 Topic
      } else if (type === 'user') {
        topicPath = `/topic/users/${idRef.value}/orders`; // 用戶訂單 Topic (根據您的後端設計)
      } else {
        console.error('useOrderNotifier: 未知的訂閱類型:', type);
        disconnectWebSocket();
        return;
      }

      console.log(`useOrderNotifier: 嘗試訂閱 Topic: ${topicPath}`);

      // 取消舊的訂閱，確保只訂閱最新的 Topic
      if (currentSubscription) {
        currentSubscription.unsubscribe();
        console.log('useOrderNotifier: 已取消舊的訂閱。');
      }

      currentSubscription = stompClient.subscribe(topicPath, (message) => {
        // 收到訊息
        const receivedData = JSON.parse(message.body);
        console.log(`收到 ${type} 訂單通知:`, receivedData);

        // 觸發外部傳入的回呼函式，通知組件刷新訂單數據
        if (fetchOrdersCallback) {
          fetchOrdersCallback(idRef.value); // 回呼時傳入當前 ID 值
        }
      }, (error) => {
        // 訂閱錯誤處理
        console.error(`useOrderNotifier: 訂閱 ${topicPath} 錯誤:`, error);
        // 訂閱失敗通常不會斷開整個連接，但可能需要重新訂閱
      });

      // 連線成功後，主動獲取一次訂單（確保拿到最新的狀態，以防剛連線就錯過推送）
      if (fetchOrdersCallback) {
        console.log('useOrderNotifier: 連線成功後，首次載入訂單數據。');
        fetchOrdersCallback(idRef.value); // 這裡也要傳入 ID 值
      }

    }, (error) => {
      // 連線錯誤或斷開
      console.error('STOMP 連接錯誤:', error);
      isConnected.value = false;
      // 斷開後嘗試重連，但給點延遲，避免無限重連導致服務端壓力
      setTimeout(connectWebSocket, 3000);
    });
  };

  const disconnectWebSocket = () => {
    if (stompClient && stompClient.connected) {
      if (currentSubscription) {
        currentSubscription.unsubscribe(); // 取消所有訂閱
        currentSubscription = null;
      }
      stompClient.disconnect(() => {
        console.log('STOMP 連接已斷開');
        isConnected.value = false;
      });
    }
  };

  // 監聽 idRef 的變化，當 idRef.value 改變時重新連線/訂閱
  // immediate: true 確保在組件掛載時，即使 idRef.value 已經有值，也會立即執行一次
  watch(idRef, (newIdValue, oldIdValue) => {
    console.log(`useOrderNotifier: ID 監聽到變化 -> Old: ${oldIdValue}, New: ${newIdValue}`);
    if (newIdValue) { // 只有當有有效的新 ID 時才嘗試連接或重新連接
      if (newIdValue !== oldIdValue || !stompClient || !stompClient.connected) {
        // 如果 ID 變化了，或者還沒有連線，或者連線斷開了，就重新連線
        console.log(`useOrderNotifier: ID 有效 (${newIdValue}) 或變化，重新連接 WebSocket。`);
        disconnectWebSocket(); // 先斷開舊的連線和訂閱
        connectWebSocket(); // 再建立新的連線和訂閱
      }
    } else if (oldIdValue) { // 如果新 ID 為空，但舊 ID 存在，則斷開連線
      console.log('useOrderNotifier: ID 變為空值，斷開 WebSocket 連線。');
      disconnectWebSocket();
    }
  }, { immediate: true }); // 在組件掛載時立即執行一次

  // 移除 onMounted 中的 connectWebSocket()，因為 watch(idRef, ..., { immediate: true }) 會處理

  onUnmounted(() => {
    disconnectWebSocket();
  });

  return {
    isConnected,
  };
}