package tw.com.ispan.eeit.repository.promotion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.promotion.NotificationBean;


@Repository
public interface NotificationRepository extends JpaRepository<NotificationBean, Integer>{
	// 查詢某使用者的所有通知
    List<NotificationBean> findByUserId(Integer userId);

    // ✅ 查詢某使用者所有「未讀」的通知
    List<NotificationBean> findByUserIdAndIsReadFalse(Integer userId);
	
}
