package tw.com.ispan.eeit.service.promotion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import tw.com.ispan.eeit.controller.webSocket.PromotionNotificationController;
import tw.com.ispan.eeit.model.dto.promotion.NotificationCreateDTO;
import tw.com.ispan.eeit.model.dto.promotion.NotificationDTO;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.promotion.NotificationBean;
import tw.com.ispan.eeit.model.entity.promotion.PromotionBean;
import tw.com.ispan.eeit.repository.UserRepository;
import tw.com.ispan.eeit.repository.promotion.NotificationRepository;
import tw.com.ispan.eeit.repository.promotion.PromotionRepository;
import tw.com.ispan.eeit.util.DatetimeConvert;

@Service
@Transactional // 建議在 Service 層加上 @Transactional，確保資料庫操作的一致性
public class NotificationService {

	@Autowired
	private NotificationRepository notificationRepository;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PromotionRepository promotionRepository;
	
	@Autowired // 注入您的 WebSocket 推播控制器
    private PromotionNotificationController promotionNotificationController;

	/**
	 * 新增一筆通知
	 * @param dto 包含 userId 和 promotionId 的 DTO
	 * @return 儲存後的 NotificationBean 實體
	 */
	public NotificationBean createFromDTO(NotificationCreateDTO dto) {
	    // 1. 根據 ID 查找 User，如果找不到就拋出例外，防止建立不完整的資料
	    UserBean user = userRepository.findById(dto.getUserId())
	        .orElseThrow(() -> new EntityNotFoundException("新增通知失敗：找不到 ID 為 " + dto.getUserId() + " 的使用者"));


	    // 2. 根據 ID 查找 Promotion，如果找不到就拋出例外
	    PromotionBean promotion = promotionRepository.findById(dto.getPromotionId())
	        .orElseThrow(() -> new EntityNotFoundException("新增通知失敗：找不到 ID 為 " + dto.getPromotionId() + " 的優惠活動"));
	    
	    // 3. 建立並設定 NotificationBean
	    NotificationBean notification = new NotificationBean();
	    notification.setUser(user);
	    notification.setPromotion(promotion);
	    notification.setCreatedTime(LocalDateTime.now());
	    notification.setIsRead(false);
	    
	    
        NotificationBean savedNotification = notificationRepository.save(notification);

        // --- 在這裡觸發 WebSocket 推播 ---
        // 確保 PromotionNotificationController 中的 notifyNewPromotion 方法
        // 能夠接收到正確的 userId 和 promotionTitle
        promotionNotificationController.notifyNewPromotion(
            savedNotification.getUser().getId(), // 假設 UserBean 有 getUserId() 方法
            savedNotification.getPromotion().getTitle() // 假設 PromotionBean 有 getTitle() 方法
        );
        // ------------------------------------
	    
	    
	    
	    // 4. 儲存到資料庫並回傳
        return savedNotification;
	}

	/**
	 * 查詢某使用者的所有通知 (回傳 DTO 格式)
	 * @param userId 使用者 ID
	 * @return List<NotificationDTO>
	 */
	public List<NotificationDTO> findDTOByUserId(Integer userId) {
        // 建議在 Repository 方法中直接排序，例如 findByUserIdOrderByCreatedTimeDesc
        List<NotificationBean> list = notificationRepository.findByUserId(userId);
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

	/**
	 * 查詢單筆通知 (回傳實體)
	 * @param id 通知 ID
	 * @return NotificationBean 或 null
	 */
	public NotificationBean findById(Integer id) {
	    return notificationRepository.findById(id).orElse(null);
	}
	
	/**
	 * 將通知標示為已讀
	 * @param id 通知 ID
	 * @return 更新後的 NotificationBean 或 null
	 */
	public NotificationBean markAsRead(Integer id) {
		Optional<NotificationBean> optional = notificationRepository.findById(id);
        if (optional.isPresent()) {
            NotificationBean noti = optional.get();
            // 如果尚未已讀，才更新，避免重複更新
            if (!noti.getIsRead()) {
                noti.setIsRead(true);
                noti.setReadTime(LocalDateTime.now());
                return notificationRepository.save(noti);
            }
            return noti; // 如果已讀，直接回傳
        }
        return null; // 找不到該通知
	}
	public NotificationBean create(NotificationBean notification) {
	    notification.setCreatedTime(LocalDateTime.now());
	    notification.setIsRead(false);
	    return notificationRepository.save(notification);
	}

	public void markAllAsReadByUserId(Integer userId) {
	    List<NotificationBean> unreadList = notificationRepository.findByUserIdAndIsReadFalse(userId);
	    for (NotificationBean noti : unreadList) {
	        noti.setIsRead(true);
	        noti.setReadTime(LocalDateTime.now());
	    }
	    notificationRepository.saveAll(unreadList);
	}
	

    /**
     * 根據 id 刪除一筆通知
     * @param id 通知 ID
     */
    public void deleteById(Integer id) {
        notificationRepository.deleteById(id);
    }
  
	/**
	 * 私有方法：將 NotificationBean 轉換為 NotificationDTO
	 * @param bean 實體物件
	 * @return DTO 物件
	 */
	private NotificationDTO toDTO(NotificationBean bean) {
	    NotificationDTO dto = new NotificationDTO();
	    dto.setId(bean.getId());
	    dto.setIsRead(bean.getIsRead());
	    dto.setCreatedTime(bean.getCreatedTime());
	    dto.setReadTime(bean.getReadTime());

	    // 檢查關聯的 Promotion 是否存在，避免 NullPointerException
	    if (bean.getPromotion() != null) {
	        dto.setPromotionTitle(bean.getPromotion().getTitle());

	        String startStr = DatetimeConvert.toString(
	        	    bean.getPromotion().getStartTime(), "yyyy/MM/dd(E)", Locale.TAIWAN
	        	);
	        dto.setPromotionStartTimeStr(startStr);

	        String endStr = DatetimeConvert.toString(
	        	    bean.getPromotion().getEndTime(), "yyyy/MM/dd(E)", Locale.TAIWAN
	        	);
	        dto.setPromotionEndTimeStr(endStr);

	    } else {
	        // 作為防呆，如果 promotion 因某些原因被刪除，也能正常顯示
	        dto.setPromotionTitle("（活動已不存在）");
	        dto.setPromotionStartTimeStr("—");
	        dto.setPromotionEndTimeStr("—");
	    }

	    return dto;
	}
}