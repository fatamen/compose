package tw.com.ispan.eeit.controller.webSocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class PromotionNotificationController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 新增優惠後推播通知
    public void notifyNewPromotion(Integer userId, String promotionTitle) {
        String destination = "/topic/promotions/" + userId;
        String message = String.format(
            "{\"type\":\"NEW_PROMOTION\",\"payload\":{\"title\":\"%s\",\"message\":\"您有新的優惠券：%s\"}}",
            promotionTitle, promotionTitle
        );
        messagingTemplate.convertAndSend(destination, message);
        System.out.println("推送新優惠通知至 " + destination + ": " + message);
    }
}
