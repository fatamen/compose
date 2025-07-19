package tw.com.ispan.eeit.service.store;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.ispan.eeit.exception.ResourceNotFoundException;
import tw.com.ispan.eeit.model.dto.store.OpenHourDTO;
import tw.com.ispan.eeit.model.entity.store.OpenHourBean;
import tw.com.ispan.eeit.model.entity.store.SpecialHoursBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.repository.store.OpenHourRepository;
import tw.com.ispan.eeit.repository.store.SpecialHoursRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;

@Service
@Transactional
public class OpenHourService {

	@Autowired
	private OpenHourRepository openHourRepo;

	@Autowired
	private StoreRepository storeRepo;

	@Autowired
	private SpecialHoursRepository specialHoursRepo;


	
	
	
	// ---------------------------------------------------------
	/**
	 * 為餐廳設定營業時間
	 */
	public OpenHourBean setOpenHour(Integer storeId, DayOfWeek day, String openTime, String closeTime) {
		StoreBean store = storeRepo.findById(storeId)
				.orElseThrow(() -> new ResourceNotFoundException("Store not found"));
		
		return setOpenHour(storeId, day, openTime, closeTime, true);
	}
	
	 public OpenHourBean updateOpenHour(OpenHourDTO hourDto) {
	        // 1. 根據 hourDto 中的 storeId 查找 StoreBean
	        // 假設 hourDto 裡有 storeId 屬性，並且是更新現有 OpenHour，所以需要找到關聯的 Store。
	        // 如果是新增操作，且 storeId 是必需的，則也需要這個查找。
	        StoreBean store = storeRepo.findById(hourDto.getStoreId()) // 使用 getStoreId() 查找 StoreBean
	                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + hourDto.getStoreId()));

	        OpenHourBean existingOpenHour;

	        if (hourDto.getId() != null) {
	            // 2. 如果 DTO 提供了 ID，則嘗試查找現有的 OpenHourBean
	            existingOpenHour = openHourRepo.findById(hourDto.getId())
	                    .orElseThrow(() -> new ResourceNotFoundException("OpenHour not found with ID: " + hourDto.getId()));

	            // 3. 更新現有 OpenHourBean 的屬性
	            // 使用 Mapper 將 DTO 的值賦給現有的 Bean
	            // 注意：Mapper 中的 toBean 方法會根據 isOpen 設置 openTime 和 closeTime 為 null
	            OpenHourBean updatedOpenHour = OpenHourDTO.toBean(hourDto); // Mapper 轉換會產生新的實例或更新
	            
	            // 將 DTO 中的可變屬性複製到現有 Bean
	            existingOpenHour.setDayOfWeek(updatedOpenHour.getDayOfWeek());
	            existingOpenHour.setOpenTime(updatedOpenHour.getOpenTime());
	            existingOpenHour.setCloseTime(updatedOpenHour.getCloseTime());
	            
	            // 4. 保存更新後的 Bean
	            return openHourRepo.save(existingOpenHour);

	        } else {
	            // 如果 hourDto.getId() 為 null，這表示可能是新增操作而不是更新
	            // 在這種情況下，你可以創建一個新的 OpenHourBean 並保存
	            OpenHourBean newOpenHour = OpenHourDTO.toBean(hourDto);
	            return openHourRepo.save(newOpenHour);
	            // 或者如果你這個方法只用於更新，可以在這裡拋出異常
	            // throw new IllegalArgumentException("OpenHour ID must be provided for update operation.");
	        }
	    }
	 
	    public List<OpenHourBean> saveAllOpenHours(List<OpenHourDTO> hourDtos) {
	        if (hourDtos == null || hourDtos.isEmpty()) {
	            // 如果列表為空，可以選擇返回空列表或拋出異常
	            return new ArrayList<>();
	            // throw new IllegalArgumentException("OpenHour DTO list cannot be empty.");
	        }

	        // 1. 獲取關聯的 StoreBean
	        // 我們假設列表中的所有 DTO 都屬於同一個 storeId。
	        // 可以從第一個 DTO 中獲取 storeId。
	        Integer storeId = hourDtos.get(0).getStoreId();
	        if (storeId == null) {
	            throw new IllegalArgumentException("Store ID is required for saving open hours.");
	        }

	        StoreBean store = storeRepo.findById(storeId)
	                .orElseThrow(() -> new ResourceNotFoundException("Store not found with ID: " + storeId));

	        // 2. 從資料庫中獲取該店鋪現有的所有營業時間，並按星期幾進行映射，便於查找
	        List<OpenHourBean> existingOpenHours = openHourRepo.findByStore(store); // 假設你有這個查詢方法
	        // 將現有營業時間按 DayOfWeek 映射，以便快速查找
	        Map<DayOfWeek, OpenHourBean> existingHoursMap = existingOpenHours.stream()
	                .collect(Collectors.toMap(OpenHourBean::getDayOfWeek, openHour -> openHour));

	        List<OpenHourBean> beansToSave = new ArrayList<>(); // 最終要保存的 Bean 列表

	        // 3. 遍歷傳入的 DTO 列表，判斷是更新還是新增
	        for (OpenHourDTO dto : hourDtos) {
	            if (dto.getStoreId() == null || !dto.getStoreId().equals(storeId)) {
	                // 防禦性編程：確保列表中所有 DTO 的 storeId 一致
	                throw new IllegalArgumentException("All OpenHour DTOs in the list must belong to the same store.");
	            }

	            // 嘗試根據 DayOfWeek 查找現有的 Bean
	            OpenHourBean existingBean = existingHoursMap.get(dto.getDayOfWeek());

	            if (existingBean != null) {
	                // 如果找到現有的 Bean，則更新其屬性
	                // 使用 Mapper 轉換 DTO 以獲取更新後的 LocalTime 值
	                OpenHourBean tempBean = OpenHourDTO.toBean(dto); // Mapper 轉換的目的是獲取 LocalTime

	                // 更新現有 Bean 的可變屬性
	                existingBean.setOpenTime(tempBean.getOpenTime());
	                existingBean.setCloseTime(tempBean.getCloseTime());
	                // ID 和 Store 關聯通常不變
	                beansToSave.add(existingBean); // 將更新後的現有 Bean 加入待保存列表
	            } else {
	                // 如果沒有找到現有的 Bean，則創建一個新的
	                OpenHourBean newBean = OpenHourDTO.toBean(dto);
	                newBean.setId(null); // 確保是新記錄，ID 由資料庫生成
	                beansToSave.add(newBean); // 將新 Bean 加入待保存列表
	            }
	        }
	        // 4. 清理：處理那些在 DTO 列表中不再存在的營業時間 (可選，但推薦)
	        // 這一步是為了刪除前端傳來列表中不再包含的營業時間（例如某天從營業改為不營業，且資料庫中是獨立的記錄）
	        // 或者，如果 "isOpen = false" 意味著刪除記錄而不是設置時間為 null，則需要單獨處理
	        // 這裡的邏輯是：所有在 `existingHoursMap` 中但不在 `hourDtos` 中的 DayOfWeek 應該被刪除。
	        List<DayOfWeek> incomingDayOfWeeks = hourDtos.stream()
	                .map(OpenHourDTO::getDayOfWeek)
	                .collect(Collectors.toList());
	        List<OpenHourBean> beansToDelete = existingOpenHours.stream()
	                .filter(bean -> !incomingDayOfWeeks.contains(bean.getDayOfWeek()))
	                .collect(Collectors.toList());
	        if (!beansToDelete.isEmpty()) {
	            openHourRepo.deleteAll(beansToDelete); // 批量刪除
	        }
	        // 5. 執行批量保存操作
	        return openHourRepo.saveAll(beansToSave);
	    }
	
	
	
	/**
	 * 為餐廳設定營業時間 (包含是否營業選項)
	 */
	public OpenHourBean setOpenHour(Integer storeId, DayOfWeek day, String openTime, String closeTime, boolean isOpen) {
		StoreBean store = storeRepo.findById(storeId)
				.orElseThrow(() -> new ResourceNotFoundException("Store not found"));

		// 檢查是否已存在設定
		Optional<OpenHourBean> existingOpenHour = openHourRepo.findByStoreAndDay(store, day);

		OpenHourBean openHour;
		if (existingOpenHour.isPresent()) {
			openHour = existingOpenHour.get();
		} else {
			openHour = new OpenHourBean();
			openHour.setStore(store);
			openHour.setDayOfWeek(day);
		}

		if (!isOpen) {
			// 如果設定為不營業，將時間設為NULL
			openHour.setOpenTime(null);
			openHour.setCloseTime(null);
		} else {
			// 設定營業時間
			if (openTime != null && !openTime.trim().isEmpty()) {
				openHour.setOpenTime(java.time.LocalTime.parse(openTime));
			}
			if (closeTime != null && !closeTime.trim().isEmpty()) {
				openHour.setCloseTime(java.time.LocalTime.parse(closeTime));
			}
		}

		return openHourRepo.save(openHour);
	}

	/**
	 * 取得餐廳的營業時間設定
	 */
	public List<OpenHourBean> getOpenHoursByStore(Integer storeId) {
		StoreBean store = storeRepo.findById(storeId)
				.orElseThrow(() -> new ResourceNotFoundException("Store not found"));
		return openHourRepo.findByStore(store);
	}

	/**
	 * 取得餐廳的營業時間設定 (DTO 版本，只返回核心資料)
	 */
	public List<OpenHourDTO> getOpenHoursDTOByStore(Integer storeId) {
		StoreBean store = storeRepo.findById(storeId)
				.orElseThrow(() -> new ResourceNotFoundException("Store not found"));

		List<OpenHourBean> openHours = openHourRepo.findByStoreOrderByDayAsc(store);
		return openHours.stream()
				.map(oh -> new OpenHourDTO(
						oh.getId(),
						storeId,
						oh.getDayOfWeek(),
						oh.getOpenTime(),
						oh.getCloseTime()))
				.collect(java.util.stream.Collectors.toList());
	}

	/**
	 * 取得餐廳特定星期的營業時間設定
	 */
	public OpenHourBean getOpenHourByStoreAndDay(Integer storeId, DayOfWeek day) {
		StoreBean store = storeRepo.findById(storeId)
				.orElseThrow(() -> new ResourceNotFoundException("Store not found"));

		return openHourRepo.findByStoreAndDay(store, day)
				.orElseThrow(
						() -> new ResourceNotFoundException("Open hour not found for store " + storeId + " on " + day));
	}

	/**
	 * 為餐廳設定預設營業時間（週一到週日）
	 */
	public void setDefaultOpenHours(Integer storeId) {
		StoreBean store = storeRepo.findById(storeId)
				.orElseThrow(() -> new ResourceNotFoundException("Store not found"));

		// 週一到週五：11:00-22:00
		for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
				DayOfWeek.FRIDAY)) {
			setOpenHour(storeId, day, "11:00", "22:00");
		}

		// 週六週日：10:00-23:00
		for (DayOfWeek day : List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) {
			setOpenHour(storeId, day, "10:00", "23:00");
		}
	}

	/**
	 * 更新營業時間
	 */
	public OpenHourBean updateOpenHour(Integer openHourId, String openTime, String closeTime, Boolean isOpen) {
		OpenHourBean openHour = openHourRepo.findById(openHourId)
				.orElseThrow(() -> new ResourceNotFoundException("Open hour not found"));

		if (isOpen != null) {
			if (!isOpen) {
				// 設定為公休日
				openHour.setOpenTime(null);
				openHour.setCloseTime(null);
			} else {
				// 設定營業時間
				if (openTime != null && !openTime.trim().isEmpty()) {
					openHour.setOpenTime(java.time.LocalTime.parse(openTime));
				}
				if (closeTime != null && !closeTime.trim().isEmpty()) {
					openHour.setCloseTime(java.time.LocalTime.parse(closeTime));
				}
			}
		} else {
			// 只更新時間，不改變營業狀態
			if (openTime != null && !openTime.trim().isEmpty()) {
				openHour.setOpenTime(java.time.LocalTime.parse(openTime));
			}
			if (closeTime != null && !closeTime.trim().isEmpty()) {
				openHour.setCloseTime(java.time.LocalTime.parse(closeTime));
			}
		}

		return openHourRepo.save(openHour);
	}

	/**
	 * 刪除營業時間設定
	 */
	public void deleteOpenHour(Integer openHourId) {
		if (!openHourRepo.existsById(openHourId)) {
			throw new ResourceNotFoundException("Open hour not found");
		}
		openHourRepo.deleteById(openHourId);
	}

	/**
	 * 檢查餐廳在指定時間是否營業（包含特殊營業時間判定）
	 */
	public boolean isStoreOpen(Integer storeId, DayOfWeek day, java.time.LocalTime time) {
		return isStoreOpen(storeId, LocalDate.now(), time);
	}

	/**
	 * 檢查餐廳在指定日期和時間是否營業（包含特殊營業時間判定）
	 */
	public boolean isStoreOpen(Integer storeId, LocalDate date, java.time.LocalTime time) {
		// 1. 首先檢查是否有特殊營業時間設定
		Optional<SpecialHoursBean> specialHours = specialHoursRepo.findByStoreIdAndDate(storeId, date);

		if (specialHours.isPresent()) {
			SpecialHoursBean special = specialHours.get();

			// 如果設定為休息一天
			if (special.getIsClose() != null && special.getIsClose()) {
				return false;
			}

			// 如果有設定特殊營業時間，使用特殊營業時間
			if (special.getOpenTime() != null && special.getCloseTime() != null) {
				return time.isAfter(special.getOpenTime()) && time.isBefore(special.getCloseTime());
			}
		}

		// 2. 如果沒有特殊設定，使用一般營業時間
		try {
			OpenHourBean openHour = getOpenHourByStoreAndDay(storeId, date.getDayOfWeek());

			// 檢查是否為公休日（open_time和close_time都為NULL）
			if (openHour.getOpenTime() == null || openHour.getCloseTime() == null) {
				return false;
			}

			return time.isAfter(openHour.getOpenTime()) && time.isBefore(openHour.getCloseTime());
		} catch (ResourceNotFoundException e) {
			// 如果沒有設定，使用預設邏輯
			return isDefaultOpenTime(date.getDayOfWeek(), time);
		}
	}

	/**
	 * 預設營業時間檢查
	 */
	private boolean isDefaultOpenTime(DayOfWeek day, java.time.LocalTime time) {
		switch (day) {
			case MONDAY:
			case TUESDAY:
			case WEDNESDAY:
			case THURSDAY:
			case FRIDAY:
				return time.isAfter(java.time.LocalTime.of(11, 0)) && time.isBefore(java.time.LocalTime.of(22, 0));
			case SATURDAY:
			case SUNDAY:
				return time.isAfter(java.time.LocalTime.of(10, 0)) && time.isBefore(java.time.LocalTime.of(23, 0));
			default:
				return false;
		}
	}

	/**
	 * 檢查某天是否為公休日
	 */
	public boolean isClosedDay(Integer storeId, DayOfWeek day) {
		try {
			OpenHourBean openHour = getOpenHourByStoreAndDay(storeId, day);
			return openHour.getOpenTime() == null || openHour.getCloseTime() == null;
		} catch (ResourceNotFoundException e) {
			// 如果沒有設定，預設為營業
			return false;
		}
	}

	/**
	 * 檢查某天是否為公休日（使用日期）
	 */
	public boolean isClosedDay(Integer storeId, LocalDate date) {
		return isClosedDay(storeId, date.getDayOfWeek());
	}

	

	/**
	 * 取得餐廳的完整營業時間資訊（包括公休日）
	 */
	public List<OpenHourDTO> getCompleteOpenHoursByStore(Integer storeId) {
		StoreBean store = storeRepo.findById(storeId)
				.orElseThrow(() -> new ResourceNotFoundException("Store not found"));

		// 取得現有的營業時間設定
		List<OpenHourBean> existingOpenHours = openHourRepo.findByStore(store);

		// 建立完整的營業時間列表（週一到週日）
		List<OpenHourDTO> completeOpenHours = new java.util.ArrayList<>();

		for (DayOfWeek day : DayOfWeek.values()) {
			// 尋找現有的設定
			Optional<OpenHourBean> existing = existingOpenHours.stream()
					.filter(oh -> oh.getDayOfWeek() == day)
					.findFirst();

			if (existing.isPresent()) {
				// 使用現有設定
				OpenHourBean oh = existing.get();
				completeOpenHours.add(new OpenHourDTO(
						oh.getId(),
						storeId,
						oh.getDayOfWeek(),
						oh.getOpenTime(),
						oh.getCloseTime()));
			} else {
				// 建立預設設定（營業）
				OpenHourDTO defaultHour = new OpenHourDTO(
						null,
						storeId,
						day,
						java.time.LocalTime.of(11, 0),
						java.time.LocalTime.of(22, 0));
				completeOpenHours.add(defaultHour);
			}
		}

		return completeOpenHours;
	}
}
