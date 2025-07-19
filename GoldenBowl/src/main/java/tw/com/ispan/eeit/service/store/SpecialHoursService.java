package tw.com.ispan.eeit.service.store;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.ispan.eeit.model.dto.store.SpecialHoursDTO;
import tw.com.ispan.eeit.model.entity.store.SpecialHoursBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.repository.store.SpecialHoursRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;
import tw.com.ispan.eeit.util.DatetimeConvert;

@Service
public class SpecialHoursService {

	private final SpecialHoursRepository specialHoursRepository;
    private final StoreRepository storeRepository;

    @Autowired // 自動注入 Repository
    public SpecialHoursService(SpecialHoursRepository specialHoursRepository, StoreRepository storeRepository) {
        this.specialHoursRepository = specialHoursRepository;
        this.storeRepository = storeRepository;
    }

    
    //儲存多筆
    @Transactional // 確保整個批次操作的原子性
    public List<SpecialHoursDTO> saveAll(List<SpecialHoursDTO> specialHoursDTOList) {
        if (specialHoursDTOList == null || specialHoursDTOList.isEmpty()) {
            // 如果傳入的列表為空，可直接返回空列表或拋出異常，視業務需求而定
            return new ArrayList<>();
        }

        // 並獲取 StoreBean 實體
        Integer commonStoreId = specialHoursDTOList.get(0).getStoreId();
        if (commonStoreId == null) {
            throw new IllegalArgumentException("Store ID cannot be null in the SpecialHoursDTO list.");
        }
        StoreBean store = storeRepository.findById(commonStoreId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with ID: " + commonStoreId));

        List<SpecialHoursDTO> savedResultList = new ArrayList<>();

        // 遍歷每個傳入的 DTO 進行處理
        for (SpecialHoursDTO dto : specialHoursDTOList) {
            // 確保每個 DTO 的 storeId 都與批次處理的 storeId 一致
           

            // 1. 數據格式轉換與驗證
            LocalDate parsedDate;
            LocalTime parsedOpenTime = null;
            LocalTime parsedCloseTime = null;
            Boolean isClose = dto.getIsClose(); // 直接使用 Boolean 類型

            try {
                // 將日期字串轉換為 LocalDate
                parsedDate = DatetimeConvert.parseLocalDate(dto.getDate(), SpecialHoursDTO.DATE_FORMAT);
                
                // 如果不是全日關閉，則轉換並驗證時間
                if (isClose == null || !isClose) { // 注意 null 或 false 都表示需要時間
                    parsedOpenTime = DatetimeConvert.parseLocalTime(dto.getOpenTime(), SpecialHoursDTO.TIME_FORMAT);
                    parsedCloseTime = DatetimeConvert.parseLocalTime(dto.getCloseTime(), SpecialHoursDTO.TIME_FORMAT);
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date or time format for DTO (Date: " + dto.getDate() + ", OpenTime: " + dto.getOpenTime() + ", CloseTime: " + dto.getCloseTime() + "): " + e.getMessage());
            } catch (NullPointerException e) { // 捕獲可能的 null 引用，例如 openTime/closeTime 是 null 但 isClose 是 false
                throw new IllegalArgumentException("Missing open/close time for non-closed special hours: " + e.getMessage());
            }

            // 2. 業務邏輯驗證
            if (isClose == null || !isClose) { // 如果不是全日關閉
                if (parsedOpenTime == null || parsedCloseTime == null) {
                    throw new IllegalArgumentException("Open and close times cannot be null if isClose is false for date: " + dto.getDate());
                }
                if (parsedCloseTime.isBefore(parsedOpenTime) || parsedCloseTime.equals(parsedOpenTime)) {
                    throw new IllegalArgumentException("Close time must be strictly after open time for date: " + dto.getDate());
                }
            }

            // (可選) 檢查日期是否為過去日期，是否允許設定
            // if (parsedDate.isBefore(LocalDate.now())) {
            //     throw new IllegalArgumentException("Cannot set special hours for past dates: " + dto.getDate());
            // }

           

            // 4. 創建新的 SpecialHoursBean 實體並保存
            SpecialHoursBean newBean = new SpecialHoursBean();
            // 對於新增操作，ID 應該為 null，讓資料庫自動生成
            // newBean.setId(dto.getId()); // 如果 dto.getId() 有值，這裡可以考慮是否是更新而不是覆蓋
                                        // 但在「覆蓋」邏輯下，通常是新生成 ID
            newBean.setStore(store); // 設定關聯的 StoreBean 實體
            newBean.setDate(parsedDate);
            newBean.setOpenTime(parsedOpenTime);
            newBean.setCloseTime(parsedCloseTime);
            newBean.setIsClose(isClose);

            SpecialHoursBean savedBean = specialHoursRepository.save(newBean); // 保存到資料庫

            // 5. 將保存後的 Bean 轉換回 DTO，加入結果列表
            savedResultList.add(SpecialHoursDTO.toDto(savedBean));
        }

        return savedResultList; // 返回所有成功保存的特殊營業時間 DTO 列表
    } 

    /**
     * 根據商店 ID 和日期查詢該商店的特殊營業時間列表。
     *
     * @param storeId 商店的 ID
     * @param dateStr 日期字串 (例如 "yyyy-MM-dd")
     * @return 該商店在指定日期的特殊營業時間列表
     * @throws IllegalArgumentException 如果 storeId 或日期格式無效
     */
    public List<SpecialHoursDTO> getSpecialHoursByStoreId(Integer storeId) {

        
    	if(storeId != null) {
        List<SpecialHoursBean> beans = specialHoursRepository.findByStoreId(storeId);
        return beans.stream()
                    .map(SpecialHoursDTO::toDto)
                    .collect(Collectors.toList());
    	}
    	return null;
    }


    /**
     * 刪除指定 ID 的特殊營業時間記錄。
     *
     * @param id 要刪除的特殊營業時間記錄 ID
     * @throws IllegalArgumentException 如果 ID 為 null
     */
    @Transactional
    public void deleteById(Integer id) {
    	  if (id!=null &&!specialHoursRepository.existsById(id)) {
              // 如果嘗試刪除不存在的 ID，可以選擇拋出自定義異常或 IllegalArgumentException
              throw new IllegalArgumentException("Special hour with ID " + id + " not found.");
          }
          specialHoursRepository.deleteById(id);
    } 
    
    
    
    
    //--------------------------------------------------------------------------------------------
	/**
	 * 設定特殊休假日
	 */
	public SpecialHoursBean setSpecialHoliday(Integer storeId, LocalDate date, String reason) {
		// 檢查是否已存在該日期的特殊設定
		Optional<SpecialHoursBean> existingOpt = specialHoursRepository.findByStoreIdAndDate(storeId, date);
		SpecialHoursBean specialHours;
		if (existingOpt.isPresent()) {
			specialHours = existingOpt.get();
		} else {
			specialHours = new SpecialHoursBean();
			specialHours.setStore(existingOpt.get().getStore());
			specialHours.setDate(date);
		}
		// 設定為休假日
		specialHours.setIsClose(true);
		specialHours.setOpenTime(null);
		specialHours.setCloseTime(null);
		SpecialHoursBean saved = specialHoursRepository.save(specialHours);
		return saved;
	}

	/**
	 * 設定特殊營業時間
	 */
	public SpecialHoursBean setSpecialBusinessHours(
			Integer storeId, LocalDate date, LocalTime openTime, LocalTime closeTime, String reason) {
		// 檢查是否已存在該日期的特殊設定
		Optional<SpecialHoursBean> existingOpt = specialHoursRepository.findByStoreIdAndDate(storeId, date);
		SpecialHoursBean specialHours;
		if (existingOpt.isPresent()) {
			specialHours = existingOpt.get();
		} else {
			specialHours = new SpecialHoursBean();
			specialHours.setStore(existingOpt.get().getStore());
			specialHours.setDate(date);
		}
		// 設定特殊營業時間
		specialHours.setIsClose(false);
		specialHours.setOpenTime(openTime);
		specialHours.setCloseTime(closeTime);
		SpecialHoursBean saved = specialHoursRepository.save(specialHours);
		return saved;
	}

	/**
	 * 取消特殊設定，恢復正常營業
	 */
	public boolean cancelSpecialSetting(Integer storeId, LocalDate date) {
		Optional<SpecialHoursBean> existingOpt = specialHoursRepository.findByStoreIdAndDate(storeId, date);

		if (existingOpt.isPresent()) {
			specialHoursRepository.delete(existingOpt.get());

			// 重新生成該日期的時段（按正常營業時間）

			System.out.println("取消特殊設定 " + date + "，恢復正常營業時間");
			return true;
		}

		return false;
	}

	/**
	 * 查詢餐廳的所有特殊設定
	 */
	public List<SpecialHoursBean> getStoreSpecialHours(Integer storeId) {
		return specialHoursRepository.findByStoreId(storeId);
	}

	/**
	 * 查詢餐廳在日期範圍內的特殊設定
	 */
	public List<SpecialHoursBean> getSpecialHoursByDateRange(
			Integer storeId, LocalDate startDate, LocalDate endDate) {
		return specialHoursRepository.findByStoreIdAndDateBetween(storeId, startDate, endDate);
	}

	/**
	 * 查詢特定日期的特殊設定
	 */
	public Optional<SpecialHoursBean> getSpecialHoursByDate(Integer storeId, LocalDate date) {
		return specialHoursRepository.findByStoreIdAndDate(storeId, date);
	}

	/**
	 * 批量設定特殊休假日
	 */
	public List<SpecialHoursBean> setMultipleHolidays(Integer storeId, List<LocalDate> dates, String reason) {
		return dates.stream()
				.map(date -> setSpecialHoliday(storeId, date, reason))
				.toList();
	}

	/**
	 * 檢查日期是否為特殊休假日
	 */
	public boolean isSpecialHoliday(Integer storeId, LocalDate date) {
		Optional<SpecialHoursBean> specialHours = specialHoursRepository.findByStoreIdAndDate(storeId, date);
		return specialHours.isPresent() && specialHours.get().getIsClose();
	}

	/**
	 * 檢查日期是否有特殊營業時間
	 */
	public boolean hasSpecialBusinessHours(Integer storeId, LocalDate date) {
		Optional<SpecialHoursBean> specialHours = specialHoursRepository.findByStoreIdAndDate(storeId, date);
		return specialHours.isPresent() && !specialHours.get().getIsClose() &&
				specialHours.get().getOpenTime() != null && specialHours.get().getCloseTime() != null;
	}

	/**
	 * 取得特殊設定的摘要資訊
	 */
	public SpecialHoursSummary getSpecialHoursSummary(Integer storeId, LocalDate startDate, LocalDate endDate) {
		List<SpecialHoursBean> specialHours = getSpecialHoursByDateRange(storeId, startDate, endDate);

		long holidayCount = specialHours.stream()
				.filter(sh -> sh.getIsClose())
				.count();

		long specialBusinessDaysCount = specialHours.stream()
				.filter(sh -> !sh.getIsClose() && sh.getOpenTime() != null)
				.count();

		return new SpecialHoursSummary(holidayCount, specialBusinessDaysCount, specialHours);
	}

	/**
	 * 特殊營業時間摘要
	 */
	public static class SpecialHoursSummary {
		private final long holidayCount;
		private final long specialBusinessDaysCount;
		private final List<SpecialHoursBean> details;

		public SpecialHoursSummary(long holidayCount, long specialBusinessDaysCount, List<SpecialHoursBean> details) {
			this.holidayCount = holidayCount;
			this.specialBusinessDaysCount = specialBusinessDaysCount;
			this.details = details;
		}

		public long getHolidayCount() {
			return holidayCount;
		}

		public long getSpecialBusinessDaysCount() {
			return specialBusinessDaysCount;
		}

		public List<SpecialHoursBean> getDetails() {
			return details;
		}
	}
}
