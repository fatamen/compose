package tw.com.ispan.eeit.model.dto.store;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SpecialHoursDTO {
    private Integer id;
    private Integer storeId;
    private LocalDate date;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Boolean isClose;

    // 簡化的時間字串格式
    private String openTimeStr;
    private String closeTimeStr;
    private String dateStr;

    // 狀態描述
    private String status;

    public SpecialHoursDTO(Integer id, Integer storeId, LocalDate date, LocalTime openTime, LocalTime closeTime,
            Boolean isClose) {
        this.id = id;
        this.storeId = storeId;
        this.date = date;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClose = isClose;

        // 設定字串格式
        if (date != null) {
            this.dateStr = date.toString();
        }
        if (openTime != null) {
            this.openTimeStr = openTime.toString();
        }
        if (closeTime != null) {
            this.closeTimeStr = closeTime.toString();
        }

        // 設定狀態描述
        if (isClose != null && isClose) {
            this.status = "休息";
        } else if (openTime != null && closeTime != null) {
            this.status = "特殊營業時間";
        } else {
            this.status = "未設定";
        }
    }
}