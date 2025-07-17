package tw.com.ispan.eeit.service.promotion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.entity.promotion.NotificationBean;
import tw.com.ispan.eeit.repository.promotion.NotificationRepository;

@Service
public class NotificationService {

	@Autowired
	//注入 NotificationRepository，讓Service可以使用
	private NotificationRepository notificationRepository;
	
	//查詢所有通知
	public List<NotificationBean> findAll(){
		return notificationRepository.findAll();
	}
	
	//查詢某使用者的所有通知
	public List<NotificationBean> findByUserId(Integer userId){
		return notificationRepository.findByUserId(userId);
	}
	
	// 查詢單筆通知
	public NotificationBean findById(Integer id) {
	    Optional<NotificationBean> optional = notificationRepository.findById(id);
	    return optional.orElse(null); // 如果沒找到就回傳 null
	}
	
	//新增一筆通知
	public NotificationBean create (NotificationBean notification) {
		notification.setCreatedTime(LocalDateTime.now());
		notification.setIsRead(false); //新增時預設未讀
		return notificationRepository.save(notification);
	}
	
	//將通知預設為已讀
	public NotificationBean markAsRead(Integer id) {
		Optional<NotificationBean> optional = notificationRepository.findById(id);
        if (optional.isPresent()) {
            NotificationBean noti = optional.get();
            noti.setIsRead(true);
            noti.setReadTime(LocalDateTime.now());
            return notificationRepository.save(noti);
        }
        return null;
	}
    //根據 id 刪除一筆通知
    public void deleteById(Integer id) {
        notificationRepository.deleteById(id);
    }
}