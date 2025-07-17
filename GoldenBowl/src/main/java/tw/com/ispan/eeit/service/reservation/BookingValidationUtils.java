package tw.com.ispan.eeit.service.reservation;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Component;

import tw.com.ispan.eeit.model.entity.store.SpecialHoursBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.model.enums.BookingErrorCode;
import tw.com.ispan.eeit.repository.store.SpecialHoursRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;

/**
 * 預約驗證工具類
 */
@Component
public class BookingValidationUtils {

    private final StoreRepository storeRepository;
    private final SpecialHoursRepository specialHoursRepository;

    public BookingValidationUtils(StoreRepository storeRepository,
            SpecialHoursRepository specialHoursRepository) {
        this.storeRepository = storeRepository;
        this.specialHoursRepository = specialHoursRepository;
    }

    /**
     * 驗證餐廳和日期基礎要求
     */
    public ValidationResult validateStoreAndDate(Integer storeId, LocalDate date) {
        // 檢查餐廳是否存在
        StoreBean store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return ValidationResult.failure(BookingErrorCode.STORE_NOT_FOUND);
        }

        // 檢查日期是否有效
        if (date.isBefore(LocalDate.now())) {
            return ValidationResult.failure(BookingErrorCode.INVALID_DATE);
        }

        return ValidationResult.success(store);
    }

    /**
     * 檢查特殊營業時間是否關閉
     */
    public ValidationResult isSpecialHoursClosed(Integer storeId, LocalDate date) {
        Optional<SpecialHoursBean> specialHoursOpt = specialHoursRepository.findByStoreIdAndDate(storeId, date);

        if (specialHoursOpt.isPresent()) {
            SpecialHoursBean specialHours = specialHoursOpt.get();
            if (specialHours.getIsClose()) {
                return ValidationResult.failure(BookingErrorCode.SPECIAL_HOURS_CLOSED);
            }
        }

        return ValidationResult.success(specialHoursOpt.orElse(null));
    }

    /**
     * 檢查是否在特殊營業時間範圍內
     */
    public ValidationResult isWithinSpecialHours(Integer storeId, LocalDate date, java.time.LocalTime time) {
        Optional<SpecialHoursBean> specialHoursOpt = specialHoursRepository.findByStoreIdAndDate(storeId, date);

        if (specialHoursOpt.isPresent()) {
            SpecialHoursBean specialHours = specialHoursOpt.get();
            if (specialHours.getOpenTime() != null && specialHours.getCloseTime() != null
                    && (time.isBefore(specialHours.getOpenTime()) || time.isAfter(specialHours.getCloseTime()))) {
                return ValidationResult.failure(BookingErrorCode.OUTSIDE_SPECIAL_HOURS);
            }
        }

        return ValidationResult.success();
    }

    /**
     * 驗證結果類
     */
    public static class ValidationResult {
        private final boolean valid;
        private final BookingErrorCode errorCode;
        private final Object data;

        private ValidationResult(boolean valid, BookingErrorCode errorCode, Object data) {
            this.valid = valid;
            this.errorCode = errorCode;
            this.data = data;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null, null);
        }

        public static ValidationResult success(Object data) {
            return new ValidationResult(true, null, data);
        }

        public static ValidationResult failure(BookingErrorCode errorCode) {
            return new ValidationResult(false, errorCode, null);
        }

        public boolean isValid() {
            return valid;
        }

        public BookingErrorCode getErrorCode() {
            return errorCode;
        }

        @SuppressWarnings("unchecked")
        public <T> T getData() {
            return (T) data;
        }
    }
}