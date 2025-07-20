package tw.com.ispan.eeit.model.dto.promotion;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NotificationDTO {
    private Integer id;
    private Boolean isRead;
    private LocalDateTime readTime;
    private LocalDateTime createdTime;
    private String promotionTitle;
    private String promotionStartTimeStr;
    private String promotionEndTimeStr;
}

