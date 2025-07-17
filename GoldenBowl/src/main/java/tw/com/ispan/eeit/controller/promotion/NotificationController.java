package tw.com.ispan.eeit.controller.promotion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.entity.promotion.NotificationBean;
import tw.com.ispan.eeit.service.promotion.NotificationService;

@RestController
@RequestMapping("/notifications")
@CrossOrigin
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 查全部通知
    @GetMapping
    public List<NotificationBean> findAll() {
        return notificationService.findAll();
    }

    // 查單筆通知
    @GetMapping("/{id}")
    public NotificationBean findById(@PathVariable Integer id) {
        return notificationService.findById(id);
    }

    // 查某使用者的通知
    @GetMapping("/user/{userId}")
    public List<NotificationBean> findByUserId(@PathVariable Integer userId) {
        return notificationService.findByUserId(userId);
    }

    // 新增通知
    @PostMapping
    public NotificationBean create(@RequestBody NotificationBean notification) {
        return notificationService.create(notification);
    }

    // 刪除通知
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Integer id) {
        notificationService.deleteById(id);
    }
}
