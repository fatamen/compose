package tw.com.ispan.eeit.model.dto.comment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.comment.LikedFoodBean; // 引入 LikedFoodBean
import tw.com.ispan.eeit.model.entity.food.FoodBean;
import tw.com.ispan.eeit.model.entity.order.OrderDetailBean;

import org.hibernate.Hibernate; // 引入 Hibernate 工具類，用於判斷延遲加載是否初始化

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikedFoodDTO {
    private Integer id;
    private Boolean isLiked;
    private LocalDateTime updatedTime;

    // 來自 UserBean 的資訊
    private Integer userId;
    private String userName; // 假設 UserBean 有 getName() 方法

    // 來自 FoodBean 的資訊
    private Integer foodId;
    private String foodName; // 假設 FoodBean 有 getName() 方法
    // private Integer foodPrice; // 如果需要食物價格也可以加進來

    // 來自 OrderDetailBean 的資訊
    private Integer orderDetailId;

    /**
     * 靜態方法，將 LikedFoodBean 轉換為 LikedFoodDTO。
     * 會包含關聯的 UserBean, FoodBean, OrderDetailBean 的部分資訊。
     *
     * @param likedFoodBean 要轉換的 LikedFoodBean 物件。
     * @return 轉換後的 LikedFoodDTO 物件。
     */
    public static LikedFoodDTO fromLikedFoodBean(LikedFoodBean likedFoodBean) {
        if (likedFoodBean == null) {
            return null;
        }

        LikedFoodDTO dto = new LikedFoodDTO();
        dto.setId(likedFoodBean.getId());
        dto.setIsLiked(likedFoodBean.getIsLiked());
        dto.setUpdatedTime(likedFoodBean.getUpdatedTime());

        // 處理 UserBean 資訊 (Lazy Load，需要檢查是否初始化)
        if (likedFoodBean.getUser() != null && Hibernate.isInitialized(likedFoodBean.getUser())) {
            dto.setUserId(likedFoodBean.getUser().getId());
            dto.setUserName(likedFoodBean.getUser().getName()); // 假設 UserBean 有 getName()
        }

        // 處理 FoodBean 資訊 (Lazy Load，需要檢查是否初始化)
        if (likedFoodBean.getFood() != null && Hibernate.isInitialized(likedFoodBean.getFood())) {
            dto.setFoodId(likedFoodBean.getFood().getId());
            dto.setFoodName(likedFoodBean.getFood().getName()); // 假設 FoodBean 有 getName()
            // dto.setFoodPrice(likedFoodBean.getFood().getPrice());
        }

        // 處理 OrderDetailBean 資訊 (Lazy Load，需要檢查是否初始化)
        if (likedFoodBean.getOrderDetail() != null && Hibernate.isInitialized(likedFoodBean.getOrderDetail())) {
            dto.setOrderDetailId(likedFoodBean.getOrderDetail().getId());
        }

        return dto;
    }

    public LikedFoodBean toLikedFoodBean(UserBean userBean, FoodBean foodBean, OrderDetailBean orderDetailBean) {
        LikedFoodBean bean = new LikedFoodBean();
        bean.setId(this.id); // 如果是更新現有資料，id 會被使用；如果是新增，id 通常由資料庫生成
        bean.setIsLiked(this.isLiked);
        bean.setUpdatedTime(this.updatedTime != null ? this.updatedTime : LocalDateTime.now()); // 如果 DTO 沒有提供，則用當前時間

        // 設定關聯的 Bean 物件
        bean.setUser(userBean);
        bean.setFood(foodBean);
        bean.setOrderDetail(orderDetailBean);

        return bean;
    }
}
