package tw.com.ispan.eeit.model.enums;

/**
 * 預約錯誤代碼枚舉
 * 用於國際化和錯誤處理一致性
 */
public enum BookingErrorCode {

    // 基礎驗證錯誤
    STORE_NOT_FOUND("ERR_STORE_NOT_FOUND", "餐廳不存在"),
    INVALID_DATE("ERR_INVALID_DATE", "不能預約過去的日期"),

    // 營業時間錯誤
    SPECIAL_HOURS_CLOSED("ERR_SPECIAL_HOURS_CLOSED", "該日期餐廳不營業"),
    OUTSIDE_SPECIAL_HOURS("ERR_OUTSIDE_SPECIAL_HOURS", "不在特殊營業時間內"),
    OUTSIDE_REGULAR_HOURS("ERR_OUTSIDE_REGULAR_HOURS", "不在營業時間內"),
    NO_TIME_SLOT_AVAILABLE("ERR_NO_TIME_SLOT_AVAILABLE", "該時間沒有開放預約時段"),

    // 桌位容量錯誤
    INSUFFICIENT_SEATS("ERR_INSUFFICIENT_SEATS", "座位不足"),
    NO_SUITABLE_TABLES("ERR_NO_SUITABLE_TABLES", "沒有適合的桌位組合"),

    // 系統錯誤
    SYSTEM_ERROR("ERR_SYSTEM_ERROR", "系統錯誤");

    private final String code;
    private final String defaultMessage;

    BookingErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public String getMessage() {
        return defaultMessage;
    }

    public String getMessage(String availableSeats) {
        if (this == INSUFFICIENT_SEATS && availableSeats != null) {
            return "座位不足，該時段僅剩 " + availableSeats + " 個座位";
        }
        if (this == NO_SUITABLE_TABLES && availableSeats != null) {
            return "沒有適合 " + availableSeats + " 人的桌位組合";
        }
        return defaultMessage;
    }
}