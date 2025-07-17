package tw.com.ispan.eeit.model.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikedFoodRequestDTO {
    private Integer userId; // 用戶 ID
    private Integer foodId; // 食物 ID
    private Integer orderDetailId; // 訂單詳情 ID (可選，如果喜歡的食物是來自某個訂單詳情)
    private Boolean isLiked; // 是否喜歡
}
