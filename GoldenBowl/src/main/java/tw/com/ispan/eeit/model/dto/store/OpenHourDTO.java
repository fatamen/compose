package tw.com.ispan.eeit.model.dto.store;

import java.time.DayOfWeek;
import java.time.LocalTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.store.OpenHourBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Data
@NoArgsConstructor
public class OpenHourDTO {

    // 時間格式定義，與資料庫的 TIME(0) 匹配
    private static final String TIME_FORMAT = "HH:mm";

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
    
    
    public static OpenHourDTO toDto(OpenHourBean bean) {
        if (bean == null) {
            return null;
        }

        Integer id = bean.getId();
        Integer storeId = (bean.getStore() != null) ? bean.getStore().getId() : null; // 從 StoreBean 獲取 storeId
        DayOfWeek dayOfWeek = bean.getDayOfWeek(); // 使用 bean 提供的 getDayOfWeek()
        LocalTime openTime = bean.getOpenTime();
        LocalTime closeTime = bean.getCloseTime();

        // 直接使用 OpenHourDTO 的建構子來初始化
        return new OpenHourDTO(id, storeId, dayOfWeek, openTime, closeTime);
    }

    /**
     * 將 OpenHourDTO 轉換為 OpenHourBean。
     * 注意：此方法需要一個 StoreBean 物件，因為 OpenHourBean 實際關聯的是 StoreBean 實例，
     * 而 DTO 只包含 storeId。在實際應用中，你可能需要從資料庫或其他地方根據 storeId 獲取 StoreBean。
     * @param dto 要轉換的 OpenHourDTO 物件
     * @param storeBean 關聯的 StoreBean 物件，必須提供
     * @return 轉換後的 OpenHourBean 物件
     * @throws IllegalArgumentException 如果 storeBean 為 null
     */
    public static OpenHourBean toBean (OpenHourDTO dto) {
        if (dto == null) {
            return null;
        }
        StoreBean storeBean = new StoreBean();
        
        OpenHourBean bean = new OpenHourBean();
        bean.setId(dto.getId()); // 如果是新增，id 可能為 null，由資料庫生成
     // 設定關聯的 StoreBean 物件
        storeBean.setId(dto.getStoreId());
        bean.setStore(storeBean);

        // 設定 DayOfWeek。DTO 中的 DayOfWeek 是枚舉，直接設定
        bean.setDayOfWeek(dto.getDayOfWeek());

        // 判斷 isOpen 狀態並設定 openTime 和 closeTime
//        設定時間

        if (dto.getIsOpen() != null && dto.getIsOpen()) {
            // 如果 isOpen 為 true，則解析時間字串
            // LocalTime.parse() 預設支持 "HH:mm" 或 "HH:mm:ss" 格式
            bean.setOpenTime(dto.getOpenTimeStr() != null ? LocalTime.parse(dto.getOpenTimeStr()) : null);
            bean.setCloseTime(dto.getCloseTimeStr() != null ? LocalTime.parse(dto.getCloseTimeStr()) : null);
        } else {
            // 如果 isOpen 為 false 或 null，則設定時間為 null
            bean.setOpenTime(null);
            bean.setCloseTime(null);
        }
        return bean;
    }
   
   
}