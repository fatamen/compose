package tw.com.ispan.eeit.model.dto.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

import tw.com.ispan.eeit.model.enums.ReservationStatus;

public record ReservationDTO(
        Integer id,
        String userName,
        String storeName,
        LocalDate reservedDate,
        LocalTime reservedTime,
        Integer guests,
        ReservationStatus status,
        // 創建預約時需要的額外欄位（可選）
        Integer userId,
        Integer storeId,
        Integer duration,
        String content) {

    // 用於查詢結果的建構函數
    public ReservationDTO(
            Integer id,
            String userName,
            String storeName,
            LocalDate reservedDate,
            LocalTime reservedTime,
            Integer guests,
            ReservationStatus status) {
        this(id, userName, storeName, reservedDate, reservedTime, guests, status, null, null, null, null);
    }

    // 用於創建預約的建構函數
    public static ReservationDTO forCreation(
            Integer userId,
            Integer storeId,
            LocalDate reservedDate,
            LocalTime reservedTime,
            Integer guests,
            Integer duration,
            String content) {
        return new ReservationDTO(null, null, null, reservedDate, reservedTime, guests, null, userId, storeId, duration,
                content);
    }
}
