package tw.com.ispan.eeit.service.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import tw.com.ispan.eeit.model.enums.BookingErrorCode;

/**
 * 預約可用性結果 - Record 模式
 */
public record BookingAvailabilityResult(
        boolean available,
        BookingErrorCode errorCode,
        String reason,
        Integer storeId,
        LocalDate date,
        LocalTime time,
        Integer guests,
        Integer availableSeats) {

    /**
     * 成功結果建構器
     */
    public static BookingAvailabilityResult success(Integer storeId, LocalDate date, LocalTime time,
            Integer guests, Integer availableSeats) {
        return new BookingAvailabilityResult(true, null, "預約時段可用",
                storeId, date, time, guests, availableSeats);
    }

    /**
     * 失敗結果建構器
     */
    public static BookingAvailabilityResult failure(BookingErrorCode errorCode, Integer storeId,
            LocalDate date, LocalTime time, Integer guests) {
        return new BookingAvailabilityResult(false, errorCode, errorCode.getMessage(),
                storeId, date, time, guests, null);
    }

    /**
     * 失敗結果建構器（帶參數）
     */
    public static BookingAvailabilityResult failure(BookingErrorCode errorCode, String param,
            Integer storeId, LocalDate date, LocalTime time, Integer guests) {
        return new BookingAvailabilityResult(false, errorCode, errorCode.getMessage(param),
                storeId, date, time, guests, null);
    }

    /**
     * 系統錯誤結果建構器
     */
    public static BookingAvailabilityResult systemError(String errorMessage, Integer storeId,
            LocalDate date, LocalTime time, Integer guests) {
        return new BookingAvailabilityResult(false, BookingErrorCode.SYSTEM_ERROR,
                "系統錯誤: " + errorMessage,
                storeId, date, time, guests, null);
    }
}