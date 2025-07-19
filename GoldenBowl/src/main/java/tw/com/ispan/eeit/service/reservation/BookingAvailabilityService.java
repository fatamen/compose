package tw.com.ispan.eeit.service.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.dto.reservation.ReservationDTO;
import tw.com.ispan.eeit.model.dto.reservation.ReservationSearchRequest;
import tw.com.ispan.eeit.model.entity.reservation.ReservationBean;
import tw.com.ispan.eeit.model.entity.reservation.TimeSlot;
import tw.com.ispan.eeit.model.entity.store.OpenHourBean;
import tw.com.ispan.eeit.model.entity.store.SpecialHoursBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.model.enums.BookingErrorCode;
import tw.com.ispan.eeit.repository.reservation.ReservationRepository;
import tw.com.ispan.eeit.repository.reservation.TimeSlotRepository;
import tw.com.ispan.eeit.repository.store.OpenHourRepository;
import tw.com.ispan.eeit.repository.store.SpecialHoursRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;

/**
 * 預約可用性檢查服務 - 優化重構版
 */
@Service
public class BookingAvailabilityService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private SpecialHoursRepository specialHoursRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationQueryService reservationQueryService;

    @Autowired
    private TableAllocationService tableAllocationService;

    @Autowired
    private BookingValidationUtils validationUtils;

    @Autowired
    private OpenHourRepository openHourRepository;

    /**
     * 🔐 核心方法：檢查預約可用性
     */
    public BookingAvailabilityResult checkBookingAvailability(
            Integer storeId, LocalDate date, LocalTime time, Integer guests, Integer duration) {

        try {
            // 1. 基礎驗證
            BookingValidationUtils.ValidationResult validationResult = validationUtils.validateStoreAndDate(storeId,
                    date);
            if (!validationResult.isValid()) {
                return BookingAvailabilityResult.failure(validationResult.getErrorCode(),
                        storeId, date, time, guests);
            }

            // 2. 營業時間檢查
            BookingAvailabilityResult businessHoursResult = checkBusinessHours(storeId, date, time);
            if (!businessHoursResult.available()) {
                return businessHoursResult;
            }

            // 3. 桌位容量檢查
            TableAllocationService.TableCapacityResult capacityResult = tableAllocationService
                    .checkTableCapacity(storeId, date, time, guests, duration);

            if (!capacityResult.isSufficient()) {
                return BookingAvailabilityResult.failure(BookingErrorCode.INSUFFICIENT_SEATS,
                        String.valueOf(capacityResult.getAvailableSeats()),
                        storeId, date, time, guests);
            }

            if (!capacityResult.hasSuitableTables()) {
                return BookingAvailabilityResult.failure(BookingErrorCode.NO_SUITABLE_TABLES,
                        String.valueOf(guests),
                        storeId, date, time, guests);
            }

            // 全部檢查通過
            return BookingAvailabilityResult.success(storeId, date, time, guests, capacityResult.getAvailableSeats());

        } catch (Exception e) {
            return BookingAvailabilityResult.systemError(e.getMessage(), storeId, date, time, guests);
        }
    }

    /**
     * 🔐 核心方法：取得可用時段列表
     */
    public List<TimeSlot> getAvailableTimeSlots(Integer storeId, LocalDate date, Integer guests) {
        StoreBean store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return List.of();
        }

        // 取得基本時段列表
        List<TimeSlot> allTimeSlots = timeSlotRepository.findByStoreIdAndDayAndIsActive(storeId, date, true);

        // 過濾出真正可用的時段
        return allTimeSlots.stream()
                .filter(slot -> {
                    BookingAvailabilityResult result = checkBookingAvailability(
                            storeId, date, slot.getStartTime(), guests, 120);
                    return result.available();
                })
                .toList();
    }

    /**
     * 🔐 核心方法：取得日曆元數據
     */
    public Map<String, Object> getCalendarMetadata(Integer storeId) {
        Map<String, Object> metadata = new java.util.HashMap<>();

        try {
            // 1. 獲取時段資料最新日期
            LocalDate maxSlotDate = timeSlotRepository.findMaxDateByStoreId(storeId);
            metadata.put("maxDate", maxSlotDate != null ? maxSlotDate.toString() : LocalDate.now().toString());

            // 2. 獲取禁用日期列表

            return metadata;
        } catch (Exception e) {
            metadata.put("maxDate", LocalDate.now().toString());
            metadata.put("disabledDates", List.of());
            return metadata;
        }
    }

    /**
     * 🔐 核心方法：取得禁用日期列表
     */
    public List<LocalDate> getClosedDates(Integer storeId, int daysAhead) {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(daysAhead);

        // 獲取週期性公休日（沒有營業時間的日期）
        List<OpenHourBean> allOpenHours = openHourRepository.findByStoreId(storeId);
        Set<Integer> weeklyClosedDays = allOpenHours.stream()
                .filter(oh -> oh.getOpenTime() == null || oh.getCloseTime() == null)
                .map(OpenHourBean::getDay)
                .collect(java.util.stream.Collectors.toSet());

        // 獲取特殊休假日
        List<SpecialHoursBean> allSpecialHours = specialHoursRepository.findByStoreId(storeId);
        List<LocalDate> specialClosedDates = allSpecialHours.stream()
                .filter(sh -> Boolean.TRUE.equals(sh.getIsClose()))
                .map(SpecialHoursBean::getDate)
                .collect(java.util.stream.Collectors.toList());

        return start.datesUntil(end.plusDays(1))
                .filter(date -> {
                    int dayOfWeek = date.getDayOfWeek().getValue() % 7;
                    return weeklyClosedDays.contains(dayOfWeek) || specialClosedDates.contains(date);
                })
                .toList();
    }

    // ========== 私有輔助方法 ==========

    /**
     * 營業時間檢查 - 拆分成三個子方法
     */
    private BookingAvailabilityResult checkBusinessHours(Integer storeId, LocalDate date, LocalTime time) {
        // 1. 檢查特殊營業時間是否關閉
        BookingValidationUtils.ValidationResult specialHoursResult = validationUtils.isSpecialHoursClosed(storeId,
                date);
        if (!specialHoursResult.isValid()) {
            return BookingAvailabilityResult.failure(specialHoursResult.getErrorCode(),
                    storeId, date, time, null);
        }

        // 2. 檢查是否在特殊營業時間範圍內
        BookingValidationUtils.ValidationResult withinSpecialHoursResult = validationUtils.isWithinSpecialHours(storeId,
                date, time);
        if (!withinSpecialHoursResult.isValid()) {
            return BookingAvailabilityResult.failure(withinSpecialHoursResult.getErrorCode(),
                    storeId, date, time, null);
        }

        // 3. 檢查是否在常態營業時間內
        BookingAvailabilityResult regularHoursResult = isWithinRegularHours(storeId, date, time);
        if (!regularHoursResult.available()) {
            return regularHoursResult;
        }

        return BookingAvailabilityResult.success(storeId, date, time, null, null);
    }

    /**
     * 檢查是否在常態營業時間內
     */
    private BookingAvailabilityResult isWithinRegularHours(Integer storeId, LocalDate date, LocalTime time) {
        // 取得該日期對應的星期幾
        java.time.DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayValue = dayOfWeek.getValue() == 7 ? 0 : dayOfWeek.getValue();

        // 檢查是否在營業時間內
        Integer isOpen = openHourRepository.isStoreOpenAtTime(storeId, dayValue, time);
        if (isOpen == null || isOpen == 0) {
            return BookingAvailabilityResult.failure(BookingErrorCode.OUTSIDE_REGULAR_HOURS,
                    storeId, date, time, null);
        }

        // 檢查時段是否開放
        List<TimeSlot> timeSlots = timeSlotRepository.findByStoreIdAndDayAndStartTimeAndIsActive(
                storeId, date, time, true);
        if (timeSlots.isEmpty()) {
            return BookingAvailabilityResult.failure(BookingErrorCode.NO_TIME_SLOT_AVAILABLE,
                    storeId, date, time, null);
        }

        return BookingAvailabilityResult.success(storeId, date, time, null, null);
    }

    // ========== 便利方法（使用查詢中心） ==========

    /**
     * 取得餐廳在指定日期的所有時段
     */
    public List<TimeSlot> getTimeSlotsForDate(Integer storeId, LocalDate date) {
        StoreBean store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return List.of();
        }
        return timeSlotRepository.findByStoreIdAndDayAndIsActive(storeId, date, true);
    }

    /**
     * 取得餐廳在指定日期的已預訂預約
     */
    public List<ReservationDTO> getBookedReservations(Integer storeId, LocalDate date) {
        ReservationSearchRequest request = new ReservationSearchRequest(
                storeId, null, null, date, null, null, false, false);
        return reservationQueryService.searchReservations(request);
    }

    /**
     * 尋找最佳桌位組合
     */
    public Optional<TableAllocationService.TableCombination> findBestTableCombination(
            Integer storeId, Integer guests) {
        return tableAllocationService.findBestTableCombination(storeId, guests);
    }
}