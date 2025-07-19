package tw.com.ispan.eeit.model.dto.promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PromotionUpdateDTO {
    private Integer id; // 可加上這個幫助辨識
    private String title;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private Integer minSpend;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String code;
    private Integer maxUsage;
    private Integer userUsageLimit;
    private Integer storeId;
    private Integer tagId;
    private Integer planId;
}