package tw.com.ispan.eeit.model.dto.promotion;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionCreateDTO {
    private String title;
    private String description;
    private String discountType;
    private String discountValue;
    private Integer minSpend;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String code;
    private Integer maxUsage;
    private Integer userUsageLimit;

    private Integer storeId;   // 前端只傳 ID
    private Integer tagId;
    private Integer planId;
}

