package tw.com.ispan.eeit.model.dto.reservation;

import java.time.LocalTime;

/**
 * 時間段請求 DTO
 * 用於接收前端傳來的自定義時間段
 */
public class TimeRangeRequest {
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isActive;

    // 預設建構函數
    public TimeRangeRequest() {
    }

    // 建構函數
    public TimeRangeRequest(LocalTime startTime, LocalTime endTime, Boolean isActive) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isActive = isActive != null ? isActive : true;
    }

    // Getter 和 Setter
    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "TimeRangeRequest{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", isActive=" + isActive +
                '}';
    }
}