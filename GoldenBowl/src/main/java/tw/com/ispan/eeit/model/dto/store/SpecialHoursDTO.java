package tw.com.ispan.eeit.model.dto.store;

import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.store.SpecialHoursBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.util.DatetimeConvert;

@Data
@NoArgsConstructor
public class SpecialHoursDTO {

	public static final String DATE_FORMAT = "yyyy-MM-dd"; // 日期格式
	public static final String TIME_FORMAT = "HH:mm"; // 時間格式 (根據您的需求調整，例如 "HH:mm" 如果只需要小時分鐘)

	private Integer id;
	private Integer storeId;
	private String date;
	private String openTime;
	private String closeTime;
	private Boolean isClose;

	// 簡化的時間字串格式
	private String dateStr;

	/**
	 * 將 SpecialHoursDTO 轉換為 SpecialHoursBean
	 * 
	 * @param dto SpecialHoursDTO 物件
	 * @return 轉換後的 SpecialHoursBean 物件
	 */
	public static SpecialHoursBean toBean(SpecialHoursDTO dto) {
		if (dto == null) {
			return null;
		}

		SpecialHoursBean bean = new SpecialHoursBean();
		bean.setId(dto.getId());
		bean.setDate(DatetimeConvert.parseLocalDate(dto.getDate(), DATE_FORMAT));
		bean.setOpenTime(DatetimeConvert.parseLocalTime(dto.getOpenTime(), TIME_FORMAT));
		bean.setCloseTime(DatetimeConvert.parseLocalTime(dto.getCloseTime(), TIME_FORMAT));
		bean.setIsClose(dto.getIsClose());

		// storeId 在 SpecialHoursBean 中需要設置 StoreBean 物件，這裡暫時不處理，
		// 因為DTO通常只包含ID，Bean則需要完整的關聯物件。
		// 如果需要設定 StoreBean，需要從資料庫或其他服務獲取 StoreBean 實例。
		// 例如：
		if (dto.getStoreId() != null) {
			StoreBean store = new StoreBean(); // 這裡應該是從資料庫查詢獲得的 StoreBean
			store.setId(dto.getStoreId());
			bean.setStore(store);
		}

		return bean;
	}

	/**
	 * 將 SpecialHoursBean 轉換為 SpecialHoursDTO
	 * 
	 * @param bean SpecialHoursBean 物件
	 * @return 轉換後的 SpecialHoursDTO 物件
	 */
	public static SpecialHoursDTO toDto(SpecialHoursBean bean) {
		if (bean == null) {
			return null;
		}
		SpecialHoursDTO dto = new SpecialHoursDTO();
		dto.setId(bean.getId());
		dto.setDate(DatetimeConvert.toString(bean.getDate(), DATE_FORMAT));
		dto.setOpenTime(DatetimeConvert.toString(bean.getOpenTime(), TIME_FORMAT));
		dto.setCloseTime(DatetimeConvert.toString(bean.getCloseTime(), TIME_FORMAT));
		dto.setIsClose(bean.getIsClose());
		// 設定 storeId
		if (bean.getStore() != null) {
			dto.setStoreId(bean.getStore().getId());
		}
		return dto;
	}

}