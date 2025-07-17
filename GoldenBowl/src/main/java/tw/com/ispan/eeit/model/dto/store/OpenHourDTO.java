package tw.com.ispan.eeit.model.dto.store;

import java.time.DayOfWeek;
import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OpenHourDTO {
    private Integer id;
    private Integer storeId;
    private DayOfWeek dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;

    // 簡化的營業時間字串格式
    private String openTimeStr;
    private String closeTimeStr;

    // 是否營業的標示
    private Boolean isOpen;

    // 星期中文名稱
    private String dayName;

    public OpenHourDTO(Integer id, Integer storeId, DayOfWeek dayOfWeek, LocalTime openTime, LocalTime closeTime) {
        this.id = id;
        this.storeId = storeId;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isOpen = (openTime != null && closeTime != null);

        // 設定時間字串
        if (openTime != null) {
            this.openTimeStr = openTime.toString();
        }
        if (closeTime != null) {
            this.closeTimeStr = closeTime.toString();
        }

        // 設定星期中文名稱
        this.dayName = getDayName(dayOfWeek);
    }

    private String getDayName(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "星期一";
            case TUESDAY:
                return "星期二";
            case WEDNESDAY:
                return "星期三";
            case THURSDAY:
                return "星期四";
            case FRIDAY:
                return "星期五";
            case SATURDAY:
                return "星期六";
            case SUNDAY:
                return "星期日";
            default:
                return dayOfWeek.toString();
        }
    }
}