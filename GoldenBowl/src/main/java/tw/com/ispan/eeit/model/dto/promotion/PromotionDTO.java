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
    private String status; // 僅表示後台是否啟用 open / close
    private boolean available; // 自動計算是否可用（前端根據這個顯示）


    private String type; // 用於前端分類 (global / restaurant / food / member)

    // 額外附帶欄位（可選）
    private Integer tagId;
    private String tagName;
    private Integer storeId;
    private String storeName;
    private Integer planId;
    private String planName;
	private Integer planPrice;
	private Integer planValidMonths;
	private String code;
	private Integer maxUsage;
	private Integer userUsageLimit;
	
	//時間格式
	private String startTimeStr;
    private String endTimeStr;
}
