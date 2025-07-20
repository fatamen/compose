package tw.com.ispan.eeit.controller.webSocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

//這個 Controller 用於處理 WebSocket 訊息和推送通知
@Controller
public class OrderNotificationController {
//追蹤訂單新增狀況
 @Autowired
 private SimpMessagingTemplate messagingTemplate; // 用於發送訊息給特定訂閱的客戶端

 // 當客戶端發送訊息到 /app/hello 時，這個方法會被觸發
 // @MessageMapping("/hello")
 // public void greeting(@Payload String message) {
 //     System.out.println("Received message from client: " + message);
 //     // 可以選擇回覆給發送者或廣播
 // }
 
 /**
  * 商店有新訂單時，後端呼叫此方法推送通知給所有訂閱的商店客戶端。
  * 這裡假設商店端會訂閱 /topic/orders/{storeId}
  * @param storeId 商店ID
  * @param orderId 新增的訂單ID
  */
 public void notifyNewOrder(Integer storeId, String orderId) {
     String destination = "/topic/orders/" + storeId; // 推送給特定商店的訂閱者
     // 您可以發送任何結構的訊息，例如一個 JSON 物件
     String message = "{\"type\":\"NEW_ORDER\",\"payload\":{\"orderId\":\"" + orderId + "\", \"message\":\"您有新訂單，請確認！\"}}";
     messagingTemplate.convertAndSend(destination, message);
     System.out.println("通知新訂單到 " + destination + ": " + message);
 }

 
 
 /**
  * 當訂單狀態更新時，後端呼叫此方法推送通知。
  * @param storeId 商店ID
  * @param orderId 訂單ID
  * @param newStatus 新狀態
  */
 public void notifyOrderStatusUpdate(Integer storeId, String orderId, String newStatus) {
     String destination = "/topic/orders/" + storeId;
     String message = "{\"type\":\"ORDER_UPDATED\",\"payload\":{\"orderId\":\"" + orderId + "\", \"status\":\"" + newStatus + "\", \"message\":\"訂單狀態已更新\"}}";
     messagingTemplate.convertAndSend(destination, message);
     System.out.println("通知訂單狀態更新到 " + destination + ": " + message);
 }
 
 

 // ✨ [新增方法] 用於向特定用戶推送訂單狀態更新通知
 /**
  * 當訂單狀態更新時，後端呼叫此方法推送通知給特定用戶。
  * 用戶端會訂閱 /topic/users/{userId}/orders
  * @param userId 用戶ID
  * @param orderId 訂單ID
  * @param newStatus 新狀態
  */
 public void notifyUserOrderStatusUpdate(Integer userId, String orderId, String newStatus) {
     String destination = "/topic/users/" + userId + "/orders"; // 推送給特定用戶的訂閱者
     String message = "{\"type\":\"USER_ORDER_UPDATED\",\"payload\":{\"orderId\":\"" + orderId + "\", \"status\":\"" + newStatus + "\", \"message\":\"您的訂單狀態已更新\"}}";
     messagingTemplate.convertAndSend(destination, message);
     System.out.println("通知用戶訂單狀態更新到 " + destination + ": " + message);
 }

 // ✨ [可選] 如果您希望用戶下新訂單時也收到通知（這通常是店家的任務，但如果用戶端也需要確認），可以考慮
 /**
  * 新訂單創建時，後端呼叫此方法推送通知給下單用戶。
  * 用戶端會訂閱 /topic/users/{userId}/orders
  * @param userId 用戶ID
  * @param orderId 新訂單ID
  * @param storeName 商店名稱 (方便用戶識別)
  */
 public void notifyUserNewOrderCreated(Integer userId, String orderId, String storeName) {
     String destination = "/topic/users/" + userId + "/orders";
     String message = "{\"type\":\"USER_NEW_ORDER\",\"payload\":{\"orderId\":\"" + orderId + "\", \"storeName\":\"" + storeName + "\", \"message\":\"您已成功建立新訂單\"}}";
     messagingTemplate.convertAndSend(destination, message);
     System.out.println("通知用戶新訂單創建到 " + destination + ": " + message);
 }
}