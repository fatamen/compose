package tw.com.ispan.eeit.model.dto.store;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StoreOpenStatusDTO {
    private Integer storeId;
    private Boolean isOpen;
    private String status;
    private String message;

    // 檢查的時間資訊
    private DayOfWeek dayOfWeek;
    private LocalDate date;
    private LocalTime checkTime;
    private String checkTimeStr;

    // 營業時間資訊（如果有的話）
    private LocalTime openTime;
    private LocalTime closeTime;
    private String openTimeStr;
    private String closeTimeStr;

    public StoreOpenStatusDTO(Integer storeId, Boolean isOpen, String status, String message) {
        this.storeId = storeId;
        this.isOpen = isOpen;
        this.status = status;
        this.message = message;
    }

    public StoreOpenStatusDTO(Integer storeId, Boolean isOpen, DayOfWeek dayOfWeek, LocalTime checkTime) {
        this.storeId = storeId;
        this.isOpen = isOpen;
        this.dayOfWeek = dayOfWeek;
        this.checkTime = checkTime;

        if (checkTime != null) {
            this.checkTimeStr = checkTime.toString();
        }

        this.status = isOpen ? "營業中" : "休息中";
        this.message = isOpen ? "餐廳目前營業中" : "餐廳目前休息中";
    }

    public StoreOpenStatusDTO(Integer storeId, Boolean isOpen, LocalDate date, LocalTime checkTime) {
        this.storeId = storeId;
        this.isOpen = isOpen;
        this.date = date;
        this.checkTime = checkTime;

        if (checkTime != null) {
            this.checkTimeStr = checkTime.toString();
        }

        this.status = isOpen ? "營業中" : "休息中";
        this.message = isOpen ? "餐廳目前營業中" : "餐廳目前休息中";
    }

    public StoreOpenStatusDTO(Integer storeId, Boolean isOpen, DayOfWeek dayOfWeek, LocalTime checkTime,
            LocalTime openTime, LocalTime closeTime) {
        this(storeId, isOpen, dayOfWeek, checkTime);
        this.openTime = openTime;
        this.closeTime = closeTime;

        if (openTime != null) {
            this.openTimeStr = openTime.toString();
        }
        if (closeTime != null) {
            this.closeTimeStr = closeTime.toString();
        }
    }
}