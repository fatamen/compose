package tw.com.ispan.eeit.model.dto.promotion;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionDTO {
    private Integer id;
    private String title;
    private String description;
    private String discountType;
    private String discountValue;
    private Integer minSpend;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    private String type; // 用於前端分類 (global / restaurant / food / member)

    // 額外附帶欄位（可選）
    private String tagName;
    private Integer storeId;
    private String storeName;
    private Integer planId;
    private String planName;
	private Integer planPrice;
	private Integer planValidMonths;
}
