package tw.com.ispan.eeit.controller.promotion;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.dto.promotion.NotificationCreateDTO;
import tw.com.ispan.eeit.model.dto.promotion.NotificationDTO;
import tw.com.ispan.eeit.model.entity.promotion.NotificationBean;
import tw.com.ispan.eeit.service.promotion.NotificationService;

@RestController
@RequestMapping("/notifications")
@CrossOrigin
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 查全部通知
//    @GetMapping
//    public List<NotificationBean> findAll() {
//        return notificationService.findAll();
//    }

    // 查單筆通知
    @GetMapping("/{id}")
    public NotificationBean findById(@PathVariable Integer id) {
        return notificationService.findById(id);
    }

    // 查某使用者的通知
    @GetMapping("/user/{userId}")
    public List<NotificationDTO> getUserNotifications(@PathVariable Integer userId) {
        return notificationService.findDTOByUserId(userId);
    }

    // 新增通知
    @PostMapping
    public NotificationBean create(@RequestBody NotificationCreateDTO dto) {
        return notificationService.createFromDTO(dto);
    }


    // 刪除通知
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Integer id) {
        notificationService.deleteById(id);
    }
    
    //已讀
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        notificationService.markAsRead(id); // 這邊會更新 isRead=true + readTime
        return ResponseEntity.ok().build();
    }

 // 這個 API 負責標記使用者所有通知為已讀
    @PostMapping("/mark-all-as-read")
    public ResponseEntity<?> markAllAsRead(@RequestBody Map<String, Integer> body) {
        Integer userId = body.get("userId");
        if (userId == null) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        notificationService.markAllAsReadByUserId(userId);
        return ResponseEntity.ok("所有通知已標記為已讀");
    }

}
