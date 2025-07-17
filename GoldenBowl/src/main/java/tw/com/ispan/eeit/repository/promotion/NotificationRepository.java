package tw.com.ispan.eeit.repository.promotion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.promotion.NotificationBean;


@Repository
public interface NotificationRepository extends JpaRepository<NotificationBean, Integer>{
	List<NotificationBean> findByUserId(Integer userId);
}
