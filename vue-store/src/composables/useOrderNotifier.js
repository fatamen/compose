// src/composables/useOrderNotifier.js
import { ref, onMounted, onUnmounted } from 'vue';
import Stomp from 'stompjs'; // 引入 stompjs
import SockJS from 'sockjs-client'; // 引入 sockjs-client

export function useOrderNotifier(storeId, fetchOrdersCallback) {
  const isConnected = ref(false);
  let stompClient = null; // 使用 stompClient 來管理連線

  const connectWebSocket = () => {
    // 避免重複連接
    if (stompClient && stompClient.connected) {
      return;
    }

    // 這是後端註冊的 STOMP 端點 URL
    const wsUrl = `${import.meta.env.VITE_API_URL}/ws`; 
    // 假設您的 API Base URL 是 http://localhost:8080/api，WebSocket 端點在 /ws

    // 使用 SockJS 來創建 WebSocket 連線，兼容性更好
    const socket = new SockJS(wsUrl);
    stompClient = Stomp.over(socket); // 使用 Stomp.over 讓 Stomp 協議跑在 SockJS 上

    stompClient.connect({}, (frame) => {
      // 連接成功
      isConnected.value = true;
      console.log('STOMP 連接成功:', frame);

      // 訂閱特定商店的訂單通知 Topic
      // 這裡的 topic 路徑要和後端 `messagingTemplate.convertAndSend` 的 `destination` 對應
      stompClient.subscribe(`/topic/orders/${storeId}`, (message) => {
        // 收到訊息
        const receivedData = JSON.parse(message.body);
        console.log('收到訂單通知:', receivedData);

        // 觸發外部傳入的回呼函式，通知組件刷新訂單數據
        if (fetchOrdersCallback) {
          fetchOrdersCallback(storeId);
        }
      });

      // 連線成功後，主動獲取一次訂單（確保拿到最新的狀態，以防剛連線就錯過推送）
      if (fetchOrdersCallback) {
        fetchOrdersCallback(storeId);
      }

    }, (error) => {
      // 連線錯誤或斷開
      console.error('STOMP 連接錯誤:', error);
      isConnected.value = false;
      // 斷開後嘗試重連
      setTimeout(connectWebSocket, 3000);
    });
  };

  const disconnectWebSocket = () => {
    if (stompClient && stompClient.connected) {
      stompClient.disconnect(() => {
        console.log('STOMP 連接已斷開');
        isConnected.value = false;
      });
    }
  };

  onMounted(() => {
    connectWebSocket();
  });

  onUnmounted(() => {
    disconnectWebSocket();
  });

  return {
    isConnected,
  };
}