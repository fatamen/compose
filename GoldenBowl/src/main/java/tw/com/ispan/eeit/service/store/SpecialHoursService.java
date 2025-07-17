package tw.com.ispan.eeit.service.store;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.entity.store.SpecialHoursBean;
import tw.com.ispan.eeit.repository.store.SpecialHoursRepository;
import tw.com.ispan.eeit.service.reservation.ReservationService;

@Service
public class SpecialHoursService {

	@Autowired
	private SpecialHoursRepository specialHoursRepository;

	@Autowired
	private ReservationService reservationService;

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
			specialHours.setStoreId(storeId);
			specialHours.setDate(date);
		}

		// 設定為休假日
		specialHours.setIsClose(true);
		specialHours.setOpenTime(null);
		specialHours.setCloseTime(null);

		SpecialHoursBean saved = specialHoursRepository.save(specialHours);

		// 停用該日期的所有時段
		int disabledSlots = reservationService.disableTimeSlotsByDate(storeId, date);
		System.out.println("設定特殊休假日 " + date + "，停用了 " + disabledSlots + " 個時段");

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
			specialHours.setStoreId(storeId);
			specialHours.setDate(date);
		}

		// 設定特殊營業時間
		specialHours.setIsClose(false);
		specialHours.setOpenTime(openTime);
		specialHours.setCloseTime(closeTime);

		SpecialHoursBean saved = specialHoursRepository.save(specialHours);

		// 重新生成該日期的時段
		regenerateTimeSlotsForSpecialDate(storeId, date, openTime, closeTime);

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
			reservationService.generateTimeSlotsForRestaurant(storeId, 1);

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
	 * 為特殊日期重新生成時段
	 */
	private void regenerateTimeSlotsForSpecialDate(
			Integer storeId, LocalDate date, LocalTime openTime, LocalTime closeTime) {

		// 先停用該日期的所有時段
		int disabledSlots = reservationService.disableTimeSlotsByDate(storeId, date);

		// 生成特殊營業時間的時段
		// 這裡可以呼叫 ReservationService 的方法或直接在這裡實現
		// 為了簡化，先留下日誌
		System.out.println("為特殊營業日 " + date + " (" + openTime + "-" + closeTime +
				") 重新生成時段，停用了 " + disabledSlots + " 個舊時段");

		// 可以在這裡調用時段生成邏輯
		// reservationService.generateSpecialTimeSlots(storeId, date, openTime,
		// closeTime);
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
