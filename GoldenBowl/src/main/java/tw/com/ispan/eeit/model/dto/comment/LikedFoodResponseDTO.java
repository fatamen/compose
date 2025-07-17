package tw.com.ispan.eeit.model.dto.comment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.comment.LikedFoodBean;
import tw.com.ispan.eeit.model.entity.food.FoodBean;
import tw.com.ispan.eeit.model.entity.order.OrderDetailBean;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikedFoodResponseDTO {
    private Integer id;
    private Boolean isLiked;
    private LocalDateTime updatedTime;

    // 來自 UserBean 的資訊
    private Integer userId;
    private String userName;

    // 來自 FoodBean 的資訊
    private Integer foodId;
    private String foodName; // 假設 FoodBean 有一個 getName() 方法

    // 來自 OrderDetailBean 的資訊
    private Integer orderDetailId;

    /**
     * 靜態方法，將 LikedFoodBean 轉換為 LikedFoodResponseDTO。
     * 會包含關聯的 UserBean 和 FoodBean 信息。
     *
     * @param likedFoodBean 要轉換的 LikedFoodBean 對象。
     * @return 轉換後的 LikedFoodResponseDTO 對象。
     */
    public static LikedFoodResponseDTO fromEntity(LikedFoodBean likedFoodBean) {
        if (likedFoodBean == null) {
            return null;
        }

        LikedFoodResponseDTO dto = new LikedFoodResponseDTO();
        dto.setId(likedFoodBean.getId());
        dto.setIsLiked(likedFoodBean.getIsLiked());
        dto.setUpdatedTime(likedFoodBean.getUpdatedTime());

        // 處理 UserBean 信息
        UserBean user = likedFoodBean.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUserName(user.getName()); // 假設 UserBean 有 getName() 方法
        }

        // 處理 FoodBean 信息
        FoodBean food = likedFoodBean.getFood();
        if (food != null) {
            dto.setFoodId(food.getId());
            dto.setFoodName(food.getName()); // 假設 FoodBean 有 getName() 方法
        }

        // 處理 OrderDetailBean 信息
        OrderDetailBean orderDetail = likedFoodBean.getOrderDetail();
        if (orderDetail != null) {
            dto.setOrderDetailId(orderDetail.getId());
        }

        return dto;
    }
}
