package tw.com.ispan.eeit.model.dto.order;

import lombok.Data;

@Data
public class OrderRequestDTO {
    private Integer userId;
    private Integer storeId;
    private Integer promotionId;
    private Integer total;
    private String status;
    // 如果有 pickupTime 或其他欄位也可以加上去
}
