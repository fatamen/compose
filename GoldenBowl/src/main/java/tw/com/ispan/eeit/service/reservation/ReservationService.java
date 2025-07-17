package tw.com.ispan.eeit.service.reservation;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.dto.reservation.TimeSlotSimpleDTO;
import tw.com.ispan.eeit.model.dto.reservation.TimeRangeRequest;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.reservation.ReservationBean;
import tw.com.ispan.eeit.model.entity.reservation.TableBean;
import tw.com.ispan.eeit.model.entity.reservation.TimeSettingBean;
import tw.com.ispan.eeit.model.entity.reservation.TimeSlot;
import tw.com.ispan.eeit.model.entity.store.OpenHourBean;
import tw.com.ispan.eeit.model.entity.store.SpecialHoursBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.model.enums.ReservationStatus;
import tw.com.ispan.eeit.repository.UserRepository;
import tw.com.ispan.eeit.repository.reservation.ReservationRepository;
import tw.com.ispan.eeit.repository.reservation.TableRepository;
import tw.com.ispan.eeit.repository.reservation.TimeSettingRepository;
import tw.com.ispan.eeit.repository.reservation.TimeSlotRepository;
import tw.com.ispan.eeit.repository.store.OpenHourRepository;
import tw.com.ispan.eeit.repository.store.SpecialHoursRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;
import tw.com.ispan.eeit.service.reservation.BookingAvailabilityService;
import tw.com.ispan.eeit.service.reservation.ReservationBuilder;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private TimeSettingRepository timeSettingRepository;

    @Autowired
    private OpenHourRepository openHourRepository;

    @Autowired
    private SpecialHoursRepository specialHoursRepository;

    @Autowired
    private BookingAvailabilityService bookingAvailabilityService;

    @Autowired
    private TableAllocationService tableAllocationService;

    /**
     * 🔐 核心方法：創建訂位 - 使用 Builder 模式
     */
    public ReservationBean createReservation(
            Integer userId,
            Integer storeId,
            LocalDate reservedDate,
            LocalTime reservedTime,
            Integer guests,
            Integer duration,
            String content) {

        // 驗證用戶和餐廳是否存在
        UserBean user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用戶不存在: " + userId));

        StoreBean store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("餐廳不存在: " + storeId));

        // 使用新的預約可用性檢查服務
        BookingAvailabilityResult availabilityResult = bookingAvailabilityService
                .checkBookingAvailability(storeId, reservedDate, reservedTime, guests, duration);

        if (!availabilityResult.available()) {
            throw new RuntimeException("預約失敗: " + availabilityResult.reason());
        }

        // 使用 Builder 模式創建預約
        ReservationBean reservation = ReservationBuilder.buildPendingReservation(
                userId, storeId, reservedDate, reservedTime, guests, duration, content);

        // 使用桌位分配服務
        try {
            TableAllocationService.TableAllocationResult allocationResult = tableAllocationService
                    .allocateTables(storeId, guests, reservedDate, reservedTime, duration);

            if (!allocationResult.success()) {
                throw new RuntimeException("桌位分配失敗: " + allocationResult.message());
            }

            System.out.println("成功分配桌位: " + allocationResult.tables().size() + "個");
            System.out.println("使用策略: " + allocationResult.strategyUsed());

            for (TableBean table : allocationResult.tables()) {
                System.out.println("分配的桌位ID: " + table.getId() + ", 座位數: " + table.getSeats());
            }

            // 暫時不設置桌位關聯，先讓預約功能正常運作
            // Set<TableBean> tableSet = new HashSet<>(allocationResult.tables());
            // reservation.setTables(tableSet);

        } catch (Exception e) {
            System.err.println("桌位分配失敗詳細錯誤: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("桌位分配失敗: " + e.getMessage());
        }

        return reservationRepository.save(reservation);
    }

    /**
     * 🔐 核心方法：查詢用戶的訂位記錄
     */
    public List<ReservationBean> getUserReservations(Integer userId) {
        return reservationRepository.findByUserIdOrderByReservedDateDesc(userId);
    }

    /**
     * 🔐 核心方法：查詢餐廳的訂位記錄
     */
    public List<ReservationBean> getStoreReservations(Integer storeId) {
        return reservationRepository.findByStoreId(storeId);
    }

    /**
     * 🔐 核心方法：更新訂位狀態
     */
    public ReservationBean updateReservationStatus(Integer reservationId, ReservationStatus status) {
        ReservationBean reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("訂位不存在: " + reservationId));

        reservation.setStatus(status);
        reservation.setUpdatedAt(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    /**
     * 🔐 核心方法：取消訂位
     */
    public boolean cancelReservation(Integer reservationId, Integer userId) {
        ReservationBean reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("訂位不存在: " + reservationId));

        // 檢查是否為訂位者本人
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("無權限取消此訂位");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(LocalDateTime.now());
        reservationRepository.save(reservation);
        return true;
    }

    /**
     * 🔐 核心方法：檢查指定時間是否有可用桌位
     */
    public boolean checkAvailability(Integer storeId, LocalDate date, LocalTime time, Integer guests) {
        return checkAvailabilityWithDetails(storeId, date, time, guests, null).available();
    }

    /**
     * 🔐 核心方法：檢查指定時間是否有可用桌位 - 返回詳細結果
     */
    public BookingAvailabilityResult checkAvailabilityWithDetails(
            Integer storeId, LocalDate date, LocalTime time, Integer guests, Integer duration) {
        return bookingAvailabilityService.checkBookingAvailability(storeId, date, time, guests, duration);
    }

    // ========== 桌位管理方法 ==========

    /**
     * 根據時段可用性檢查可預約性
     */
    public List<TableBean> getAvailableTables(Integer storeId, Integer minSeats) {
        return tableRepository.findAvailableTablesByStoreIdAndMinSeats(storeId, minSeats);
    }

    public List<TableBean> getStoreTables(Integer storeId) {
        return tableRepository.findByStoreId(storeId);
    }

    public List<TableBean> findAvailableTables(Integer storeId, LocalDateTime startTime, int duration, int minSeats) {
        return tableRepository.findAvailableTablesByStoreIdAndMinSeats(storeId, minSeats);
    }

    public TableBean addTable(Integer storeId, Integer seats, Integer quantity, Boolean status) {
        StoreBean store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("餐廳不存在: " + storeId));

        TableBean table = new TableBean();
        table.setStore(store);
        table.setSeats(seats);
        table.setQuantity(quantity);
        table.setStatus(status);

        return tableRepository.save(table);
    }

    public TableBean updateTable(Integer tableId, Integer seats, Integer quantity, Boolean status) {
        TableBean table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("桌位不存在: " + tableId));

        if (seats != null)
            table.setSeats(seats);
        if (quantity != null)
            table.setQuantity(quantity);
        if (status != null)
            table.setStatus(status);

        return tableRepository.save(table);
    }

    public void deleteTable(Integer tableId) {
        if (!tableRepository.existsById(tableId)) {
            throw new RuntimeException("桌位不存在: " + tableId);
        }
        tableRepository.deleteById(tableId);
    }

    public TableBean getTableById(Integer tableId) {
        return tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("桌位不存在: " + tableId));
    }

    public boolean isTableAvailable(Integer tableId, LocalTime startTime, int duration) {
        TableBean table = getTableById(tableId);
        if (!table.getStatus())
            return false;

        LocalTime endTime = startTime.plusMinutes(duration);
        // 這裡假設日期為今天，實際應根據需求傳入正確日期
        LocalDate today = LocalDate.now();
        List<ReservationBean> conflictingReservations = reservationRepository
                .findConflictingReservations(table.getStore().getId(), today, startTime, endTime);

        return conflictingReservations.isEmpty();
    }

    // ========== 時段查詢方法（優化版） ==========

    /**
     * 取得可用時段 - 使用 DTO，加上條件控制
     */
    public List<TimeSlotSimpleDTO> getAvailableTimeSlots(Integer storeId) {
        StoreBean store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return List.of();
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findAvailableTimeSlotsByStore(store);
        return convertToTimeSlotSimpleDTO(timeSlots, storeId);
    }

    /**
     * 取得簡化的時段資料 - 加上條件控制
     */
    public List<TimeSlotSimpleDTO> getAvailableTimeSlotsSimple(Integer storeId) {
        StoreBean store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return List.of();
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findAvailableTimeSlotsByStore(store);
        return convertToTimeSlotSimpleDTO(timeSlots, storeId);
    }

    /**
     * 取得可用時段（根據人數篩選）
     */
    public List<TimeSlotSimpleDTO> getAvailableTimeSlotsByGuests(Integer storeId, Integer guests) {
        StoreBean store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return List.of();
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findAvailableTimeSlotsByStore(store);

        // 根據人數篩選並排序
        return timeSlots.stream()
                .filter(slot -> {
                    BookingAvailabilityResult result = bookingAvailabilityService
                            .checkBookingAvailability(storeId, slot.getDay(), slot.getStartTime(), guests, 120);
                    return result.available();
                })
                .sorted(Comparator.comparing(TimeSlot::getStartTime))
                .map(slot -> convertToTimeSlotSimpleDTO(slot, storeId))
                .toList();
    }

    // ========== 工具方法 ==========

    /**
     * 工具方法：轉換 TimeSlot 為 TimeSlotSimpleDTO
     */
    private List<TimeSlotSimpleDTO> convertToTimeSlotSimpleDTO(List<TimeSlot> timeSlots, Integer storeId) {
        return timeSlots.stream()
                .map(slot -> new TimeSlotSimpleDTO(
                        slot.getId(),
                        storeId,
                        slot.getDay(),
                        slot.getStartTime(),
                        slot.getEndTime(),
                        slot.getIsActive()))
                .toList();
    }

    /**
     * 工具方法：轉換單個 TimeSlot 為 TimeSlotSimpleDTO
     */
    private TimeSlotSimpleDTO convertToTimeSlotSimpleDTO(TimeSlot timeSlot, Integer storeId) {
        return new TimeSlotSimpleDTO(
                timeSlot.getId(),
                storeId,
                timeSlot.getDay(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getIsActive());
    }

    // ========== 其他方法保持不變 ==========

    // 新增方法：創建訂位（重載版本）
    public ReservationBean createReservation(ReservationBean reservation, List<Integer> tableIds) {
        // 驗證用戶和餐廳
        UserBean user = userRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new RuntimeException("用戶不存在"));
        StoreBean store = storeRepository.findById(reservation.getStoreId())
                .orElseThrow(() -> new RuntimeException("餐廳不存在"));

        reservation.setUserId(user.getId());
        reservation.setStoreId(store.getId());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        return reservationRepository.save(reservation);
    }

    // 新增方法：生成時段資料（使用 SQL 儲存程序）
    public void generateTimeSlotsForRestaurant(Integer storeId, int days) {
        // 驗證餐廳存在
        StoreBean store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("餐廳不存在: " + storeId));

        try {
            // 使用 SQL 儲存程序大宗生成時段
            Integer generatedCount = timeSlotRepository.generateTimeSlotsUsingSP(storeId, days);

            System.out.println("成功透過儲存程序生成餐廳 " + storeId + " 的 " + generatedCount + " 個時段");

            // Java 後端負責處理業務邏輯：特殊營業日覆蓋
            handleSpecialBusinessHours(storeId, days);

        } catch (Exception e) {
            System.err.println("生成時段失敗: " + e.getMessage());
            // 如果儲存程序失敗，回退到原本的 Java 邏輯
            generateTimeSlotsForRestaurantFallback(storeId, days);
        }
    }

    // 回退方法：如果儲存程序失敗時使用的 Java 邏輯
    private void generateTimeSlotsForRestaurantFallback(Integer storeId, int days) {
        StoreBean store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("餐廳不存在: " + storeId));

        TimeSettingBean setting = timeSettingRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("未設定餐廳營業參數: " + storeId));

        Map<DayOfWeek, List<OpenHourBean>> businessHours = getBusinessHours(storeId);
        int intervalMinutes = setting.getInterval();

        LocalDate today = LocalDate.now();

        for (int i = 0; i < days; i++) {
            LocalDate date = today.plusDays(i);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            List<OpenHourBean> ranges = businessHours.get(dayOfWeek);
            if (ranges == null || ranges.isEmpty())
                continue;

            for (OpenHourBean range : ranges) {
                generateTimeSlotsForRange(store, date, range.getOpenTime(), range.getCloseTime(), intervalMinutes);
            }
        }

        System.out.println("使用回退邏輯成功生成餐廳 " + storeId + " 的 " + days + " 天時段資料");
    }

    // 處理特殊營業日覆蓋邏輯
    private void handleSpecialBusinessHours(Integer storeId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days - 1);

        // 查詢指定日期範圍內的特殊營業時間
        List<SpecialHoursBean> specialHours = specialHoursRepository
                .findByStoreIdAndDateBetween(storeId, today, endDate);

        for (SpecialHoursBean special : specialHours) {
            LocalDate specialDate = special.getDate();

            if (special.getIsClose()) {
                // 特殊休假日：停用所有時段
                disableTimeSlotsByDate(storeId, specialDate);
                System.out.println("停用特殊休假日 " + specialDate + " 的所有時段");
            } else if (special.getOpenTime() != null && special.getCloseTime() != null) {
                // 特殊營業時間：先停用所有時段，再生成特殊時段
                disableTimeSlotsByDate(storeId, specialDate);
                generateSpecialTimeSlots(storeId, specialDate, special.getOpenTime(), special.getCloseTime());
                System.out.println("生成特殊營業日 " + specialDate + " 的時段");
            }
        }
    }

    // 生成特殊營業日的時段
    private void generateSpecialTimeSlots(Integer storeId, LocalDate date, LocalTime openTime, LocalTime closeTime) {
        StoreBean store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("餐廳不存在: " + storeId));

        TimeSettingBean setting = timeSettingRepository.findByStoreId(storeId)
                .orElse(null);

        int intervalMinutes = (setting != null) ? setting.getInterval() : 30; // 預設 30 分鐘

        generateTimeSlotsForRange(store, date, openTime, closeTime, intervalMinutes);
    }

    /**
     * 檢查時段是否可預約（使用 CONVERT 函數優化）
     */
    public boolean isTimeSlotBookableWithConvert(Integer storeId, LocalDate date, LocalTime time, Integer guests) {
        // 使用 CONVERT 函數檢查是否有預約衝突
        Integer hasConflict = reservationRepository.hasReservationAtTime(storeId, date, time);

        if (hasConflict != null && hasConflict > 0) {
            return false;
        }

        // 檢查座位容量（使用 CONVERT 函數）
        Integer totalGuests = reservationRepository.sumGuestsInTimeRange(storeId, date, time, time.plusMinutes(120));

        // 這裡可以添加座位容量檢查邏輯
        return true;
    }

    /**
     * 使用 CONVERT 函數獲取時段統計資訊
     */
    public Map<String, Object> getTimeSlotStatisticsWithConvert(Integer storeId, LocalDate date, LocalTime startTime,
            LocalTime endTime) {
        Map<String, Object> stats = new HashMap<>();

        // 統計預約數量
        Integer reservationCount = reservationRepository.countReservationsInTimeRange(storeId, date, startTime,
                endTime);
        stats.put("reservationCount", reservationCount);

        // 統計總客人數
        Integer totalGuests = reservationRepository.sumGuestsInTimeRange(storeId, date, startTime, endTime);
        stats.put("totalGuests", totalGuests);

        // 查詢該時段的預約列表
        List<ReservationBean> reservations = reservationRepository.findConflictingReservationsInTimeRange(storeId, date,
                startTime, endTime);
        stats.put("reservations", reservations);

        return stats;
    }

    /**
     * 使用 CONVERT 函數查詢可用時段
     */
    public List<TimeSlotSimpleDTO> getAvailableTimeSlotsWithConvert(Integer storeId, LocalDate date) {
        // 使用 CONVERT 函數查詢可用時段
        List<TimeSlot> availableSlots = timeSlotRepository.findAvailableTimeSlotsAfterTime(storeId, date,
                LocalTime.now());

        return availableSlots.stream()
                .map(slot -> convertToTimeSlotSimpleDTO(slot, storeId))
                .collect(Collectors.toList());
    }

    /**
     * 使用 CONVERT 函數檢查時段重疊
     */
    public boolean hasOverlappingTimeSlotsWithConvert(Integer storeId, LocalDate date, LocalTime startTime,
            LocalTime endTime) {
        Integer hasOverlap = timeSlotRepository.hasOverlappingTimeSlots(storeId, date, startTime, endTime);
        return hasOverlap != null && hasOverlap > 0;
    }

    /**
     * 使用 CONVERT 函數查詢特定時間範圍的預約
     */
    public List<ReservationBean> getReservationsInTimeRangeWithConvert(Integer storeId, LocalDate date,
            LocalTime startTime, LocalTime endTime) {
        return reservationRepository.findConflictingReservationsInTimeRange(storeId, date, startTime, endTime);
    }

    /**
     * 測試 CONVERT 函數功能
     */
    public Map<String, Object> testConvertFunctions(Integer storeId, LocalDate date) {
        Map<String, Object> testResults = new HashMap<>();

        try {
            // 測試 OpenHour CONVERT 函數
            LocalTime testTime = LocalTime.of(12, 0);
            Integer isOpen = openHourRepository.isStoreOpenAtTime(storeId, date.getDayOfWeek().getValue(), testTime);
            testResults.put("openHourTest", isOpen);

            // 測試 SpecialHours CONVERT 函數
            Integer isHoliday = specialHoursRepository.isSpecialHolidayAtDate(storeId, date);
            testResults.put("specialHoursTest", isHoliday);

            // 測試 TimeSlot CONVERT 函數
            Integer hasOverlap = timeSlotRepository.hasOverlappingTimeSlots(storeId, date, testTime,
                    testTime.plusHours(1));
            testResults.put("timeSlotTest", hasOverlap);

            // 測試 Reservation CONVERT 函數
            Integer hasReservation = reservationRepository.hasReservationAtTime(storeId, date, testTime);
            testResults.put("reservationTest", hasReservation);

            testResults.put("status", "success");
        } catch (Exception e) {
            testResults.put("status", "error");
            testResults.put("error", e.getMessage());
        }

        return testResults;
    }

    // 判斷時段是否可預約（考慮特殊營業時間）
    public boolean isTimeSlotBookable(Integer storeId, LocalDate date, LocalTime time) {
        // 檢查時段是否存在且啟用
        List<TimeSlot> timeSlots = timeSlotRepository.findTimeSlotsByStoreAndDayAndExactTime(
                storeRepository.findById(storeId).orElse(null),
                date, time);

        if (timeSlots.isEmpty()) {
            return false; // 時段不存在
        }

        TimeSlot timeSlot = timeSlots.get(0);
        if (!timeSlot.getIsActive()) {
            return false; // 時段已停用
        }

        // 檢查是否有特殊營業時間覆蓋
        SpecialHoursBean specialHours = specialHoursRepository
                .findByStoreIdAndDate(storeId, date).orElse(null);

        if (specialHours != null) {
            if (specialHours.getIsClose()) {
                return false; // 特殊休假日
            }

            // 檢查是否在特殊營業時間內
            if (specialHours.getOpenTime() != null && specialHours.getCloseTime() != null) {
                return !time.isBefore(specialHours.getOpenTime()) &&
                        !time.isAfter(specialHours.getCloseTime().minusMinutes(1));
            }
        }

        return true; // 可預約
    }

    private void generateTimeSlotsForRange(StoreBean store, LocalDate date, LocalTime start, LocalTime end,
            int intervalMinutes) {
        for (LocalTime current = start; current.isBefore(end); current = current.plusMinutes(intervalMinutes)) {
            LocalTime slotEnd = current.plusMinutes(intervalMinutes);
            if (slotEnd.isAfter(end))
                break;
            boolean exists = timeSlotRepository
                    .existsByStoreIdAndDayAndStartTime(store.getId(), date, current) > 0;
            if (!exists) {
                TimeSlot slot = new TimeSlot();
                slot.setStore(store);
                slot.setDay(date);
                slot.setStartTime(current);
                slot.setEndTime(slotEnd);
                slot.setIsActive(true);
                timeSlotRepository.save(slot);
            }
        }
    }

    /**
     * 取得餐廳營業時段設定（從資料庫讀取）
     */
    private Map<DayOfWeek, List<OpenHourBean>> getBusinessHours(Integer storeId) {
        Map<DayOfWeek, List<OpenHourBean>> map = new HashMap<>();

        // 從資料庫讀取餐廳的營業時間設定
        List<OpenHourBean> openHours = openHourRepository.findByStoreId(storeId);

        // 如果沒有設定，使用預設值
        if (openHours.isEmpty()) {
            // 預設設定：週一休息，其它日開兩個時段
            // 注意：這裡應該在資料庫中建立預設資料，而不是在程式碼中建立
            System.out.println("警告：餐廳 " + storeId + " 沒有設定營業時間，請在資料庫中設定 open_hour 資料");
            return map;
        }

        // 將資料庫資料按星期分組
        for (OpenHourBean openHour : openHours) {
            if (openHour.getOpenTime() != null && openHour.getCloseTime() != null) {
                DayOfWeek day = openHour.getDayOfWeek();
                map.computeIfAbsent(day, k -> new ArrayList<>()).add(openHour);
            }
        }

        return map;
    }

    // TimeSlot 相關方法 - 更新為使用 DTO
    public List<TimeSlotSimpleDTO> getAvailableTimeSlotsByDate(Integer storeId, LocalDate date, Integer guests) {
        List<TimeSlot> timeSlots = bookingAvailabilityService.getAvailableTimeSlots(storeId, date, guests);
        return convertToTimeSlotSimpleDTO(timeSlots, storeId);
    }

    public List<TimeSlotSimpleDTO> getAvailableTimeSlotsByDate(Integer storeId, LocalDate date) {
        StoreBean store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return new ArrayList<>();
        }
        List<TimeSlot> timeSlots = timeSlotRepository.findByStoreAndDayAndIsActive(store, date, true);
        return convertToTimeSlotSimpleDTO(timeSlots, storeId);
    }

    public List<TimeSlotSimpleDTO> getTimeSlotsByDateRange(Integer storeId, LocalDate startDate, LocalDate endDate) {
        StoreBean store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return new ArrayList<>();
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findTimeSlotsByStoreAndDateRange(store, startDate, endDate);
        return convertToTimeSlotSimpleDTO(timeSlots, storeId);
    }

    public TimeSlotSimpleDTO addTimeSlot(Integer storeId, LocalDate day, String startTime, String endTime,
            Boolean isActive) {
        StoreBean store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("餐廳不存在: " + storeId));

        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStore(store);
        timeSlot.setDay(day);
        timeSlot.setStartTime(LocalTime.parse(startTime));
        timeSlot.setEndTime(LocalTime.parse(endTime));
        timeSlot.setIsActive(isActive != null ? isActive : true);

        TimeSlot savedTimeSlot = timeSlotRepository.save(timeSlot);
        return convertToTimeSlotSimpleDTO(savedTimeSlot, storeId);
    }

    public TimeSlotSimpleDTO updateTimeSlot(Integer timeSlotId, String startTime, String endTime, Boolean isActive) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("時段不存在: " + timeSlotId));

        // 更新屬性
        if (startTime != null) {
            timeSlot.setStartTime(LocalTime.parse(startTime));
        }
        if (endTime != null) {
            timeSlot.setEndTime(LocalTime.parse(endTime));
        }
        if (isActive != null) {
            timeSlot.setIsActive(isActive);
        }

        TimeSlot updatedTimeSlot = timeSlotRepository.save(timeSlot);
        return convertToTimeSlotSimpleDTO(updatedTimeSlot, updatedTimeSlot.getStore().getId());
    }

    public void deleteTimeSlot(Integer timeSlotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("時段不存在: " + timeSlotId));
        timeSlotRepository.delete(timeSlot);
    }

    public TimeSlotSimpleDTO getTimeSlotById(Integer timeSlotId) {
        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new RuntimeException("時段不存在: " + timeSlotId));
        return convertToTimeSlotSimpleDTO(timeSlot, timeSlot.getStore().getId());
    }

    public int disableTimeSlotsByDate(Integer storeId, LocalDate date) {
        // 查詢該日期的所有時段
        StoreBean store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return 0;
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findByStoreAndDay(store, date);
        int disabledCount = 0;

        for (TimeSlot slot : timeSlots) {
            if (slot.getIsActive()) {
                slot.setIsActive(false);
                timeSlotRepository.save(slot);
                disabledCount++;
            }
        }

        return disabledCount;
    }

    public int enableTimeSlotsByDate(Integer storeId, LocalDate date) {
        // 查詢該日期的所有時段
        StoreBean store = storeRepository.findById(storeId).orElse(null);
        if (store == null) {
            return 0;
        }

        List<TimeSlot> timeSlots = timeSlotRepository.findByStoreAndDay(store, date);
        int enabledCount = 0;

        for (TimeSlot slot : timeSlots) {
            if (!slot.getIsActive()) {
                slot.setIsActive(true);
                timeSlotRepository.save(slot);
                enabledCount++;
            }
        }

        return enabledCount;
    }

    /**
     * 根據自定義時間段生成時段
     */
    public List<TimeSlotSimpleDTO> generateTimeSlotsForCustomRanges(
            Integer storeId, LocalDate date, List<TimeRangeRequest> timeRanges) {

        StoreBean store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("餐廳不存在: " + storeId));

        TimeSettingBean setting = timeSettingRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("未設定餐廳營業參數: " + storeId));

        int intervalMinutes = setting.getInterval();
        List<TimeSlotSimpleDTO> generatedSlots = new ArrayList<>();

        // 先停用該日期的所有現有時段
        disableTimeSlotsByDate(storeId, date);

        // 為每個時間段生成時段
        for (TimeRangeRequest range : timeRanges) {
            if (range.getStartTime() != null && range.getEndTime() != null) {
                List<TimeSlot> slots = generateTimeSlotsForRangeWithReturn(
                        store, date, range.getStartTime(), range.getEndTime(), intervalMinutes);

                // 轉換為 DTO 並添加到結果列表
                for (TimeSlot slot : slots) {
                    TimeSlotSimpleDTO dto = convertToTimeSlotSimpleDTO(slot, storeId);
                    generatedSlots.add(dto);
                }
            }
        }

        return generatedSlots;
    }

    /**
     * 生成時段並返回生成的時段列表
     */
    private List<TimeSlot> generateTimeSlotsForRangeWithReturn(
            StoreBean store, LocalDate date, LocalTime start, LocalTime end, int intervalMinutes) {

        List<TimeSlot> generatedSlots = new ArrayList<>();

        for (LocalTime current = start; current.isBefore(end); current = current.plusMinutes(intervalMinutes)) {
            LocalTime slotEnd = current.plusMinutes(intervalMinutes);
            if (slotEnd.isAfter(end))
                break;

            // 檢查是否已存在相同時段
            boolean exists = timeSlotRepository
                    .existsByStoreIdAndDayAndStartTime(store.getId(), date, current) > 0;

            if (!exists) {
                TimeSlot slot = new TimeSlot();
                slot.setStore(store);
                slot.setDay(date);
                slot.setStartTime(current);
                slot.setEndTime(slotEnd);
                slot.setIsActive(true);
                TimeSlot savedSlot = timeSlotRepository.save(slot);
                generatedSlots.add(savedSlot);
            }
        }

        return generatedSlots;
    }

    /**
     * 智能桌位分配邏輯
     */
    private List<TableBean> allocateTablesForReservation(Integer storeId, Integer guests,
            LocalDate date, LocalTime startTime, Integer duration) {

        try {
            System.out.println("=== 開始桌位分配 ===");
            System.out.println("餐廳ID: " + storeId + ", 客人數: " + guests + ", 日期: " + date + ", 時間: " + startTime
                    + ", 用餐時長: " + duration);

            // 取得該時段所有可用桌位
            List<TableBean> availableTables = getAvailableTablesInTimeRange(storeId, date, startTime, duration);
            System.out.println("查詢到可用桌位數量: " + (availableTables != null ? availableTables.size() : "null"));

            if (availableTables == null || availableTables.isEmpty()) {
                System.out.println("沒有可用桌位");
                throw new RuntimeException("該時段沒有可用桌位");
            }

            // 打印可用桌位詳情
            for (int i = 0; i < availableTables.size(); i++) {
                TableBean table = availableTables.get(i);
                System.out.println("桌位" + (i + 1) + ": ID=" + table.getId() + ", 座位數=" + table.getSeats() + ", 狀態="
                        + table.getStatus());
            }

            // 策略 1: 尋找恰好合適的桌位（座位數 = 客人數 或 客人數+1）
            System.out.println("嘗試策略1: 尋找恰好合適的桌位");
            Optional<TableBean> exactMatch = availableTables.stream()
                    .filter(table -> {
                        boolean matches = table.getSeats() != null && table.getSeats() >= guests
                                && table.getSeats() <= guests + 1;
                        System.out.println("桌位ID" + table.getId() + "座位數" + table.getSeats() + " 是否匹配: " + matches);
                        return matches;
                    })
                    .min(Comparator.comparing(TableBean::getSeats));

            if (exactMatch.isPresent()) {
                System.out.println("策略1成功: 找到恰好合適的桌位ID " + exactMatch.get().getId());
                return List.of(exactMatch.get());
            }

            // 策略 2: 尋找最小的能容納所有客人的桌位
            System.out.println("嘗試策略2: 尋找最小容納桌位");
            Optional<TableBean> minSufficientTable = availableTables.stream()
                    .filter(table -> {
                        boolean matches = table.getSeats() != null && table.getSeats() >= guests;
                        System.out.println("桌位ID" + table.getId() + "座位數" + table.getSeats() + " 能否容納: " + matches);
                        return matches;
                    })
                    .min(Comparator.comparing(TableBean::getSeats));

            if (minSufficientTable.isPresent()) {
                System.out.println("策略2成功: 找到最小容納桌位ID " + minSufficientTable.get().getId());
                return List.of(minSufficientTable.get());
            }

            // 策略 3: 組合多個小桌位（簡化版本 - 最多組合2個桌位）
            System.out.println("嘗試策略3: 組合多個桌位");
            for (TableBean table1 : availableTables) {
                if (table1.getSeats() == null) {
                    System.out.println("桌位ID" + table1.getId() + " 座位數為null，跳過");
                    continue;
                }
                for (TableBean table2 : availableTables) {
                    if (table2.getSeats() == null || table1.getId().equals(table2.getId()))
                        continue;

                    int combinedSeats = table1.getSeats() + table2.getSeats();
                    System.out.println("嘗試組合桌位ID" + table1.getId() + "(" + table1.getSeats() + "位) + ID"
                            + table2.getId() + "(" + table2.getSeats() + "位) = " + combinedSeats + "位");

                    if (combinedSeats >= guests) {
                        System.out.println("策略3成功: 組合桌位ID" + table1.getId() + " + ID" + table2.getId());
                        return List.of(table1, table2);
                    }
                }
            }

            System.out.println("所有策略都失敗");
            throw new RuntimeException("無法找到合適的桌位組合");
        } catch (Exception e) {
            System.err.println("桌位分配異常: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("桌位分配過程發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 檢查指定時間範圍內的可用桌位
     */
    private List<TableBean> getAvailableTablesInTimeRange(Integer storeId, LocalDate date,
            LocalTime startTime, Integer duration) {

        try {
            System.out.println("--- 檢查可用桌位 ---");
            if (duration == null || duration <= 0) {
                duration = 120; // 預設用餐時間2小時
                System.out.println("使用預設用餐時間: " + duration + "分鐘");
            }

            LocalTime endTime = startTime.plusMinutes(duration);
            System.out.println("用餐時間範圍: " + startTime + " ~ " + endTime);

            // 取得餐廳所有啟用的桌位
            List<TableBean> allTables = tableRepository.findByStoreId(storeId);
            System.out.println("餐廳總桌位數: " + (allTables != null ? allTables.size() : "null"));

            if (allTables == null || allTables.isEmpty()) {
                System.out.println("餐廳沒有任何桌位");
                return new ArrayList<>();
            }

            List<TableBean> activeTables = allTables.stream()
                    .filter(table -> {
                        boolean isActive = table != null && Boolean.TRUE.equals(table.getStatus());
                        if (table != null) {
                            System.out.println(
                                    "桌位ID" + table.getId() + " 狀態: " + table.getStatus() + " (啟用: " + isActive + ")");
                        }
                        return isActive;
                    })
                    .collect(Collectors.toList());

            System.out.println("啟用的桌位數: " + activeTables.size());

            // 檢查每個桌位在該時段是否被預約
            List<TableBean> availableTables = activeTables.stream()
                    .filter(table -> {
                        boolean isAvailable = isTableAvailableInTimeRange(table.getId(), date, startTime, endTime);
                        System.out.println("桌位ID" + table.getId() + " 在時段內是否可用: " + isAvailable);
                        return isAvailable;
                    })
                    .collect(Collectors.toList());

            System.out.println("最終可用桌位數: " + availableTables.size());
            return availableTables;
        } catch (Exception e) {
            System.err.println("取得可用桌位時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 檢查桌位在指定時間範圍是否可用
     */
    private boolean isTableAvailableInTimeRange(Integer tableId, LocalDate date,
            LocalTime startTime, LocalTime endTime) {

        try {
            // 檢查桌位是否存在
            Optional<TableBean> tableOpt = tableRepository.findById(tableId);
            if (tableOpt.isEmpty()) {
                return false;
            }

            TableBean table = tableOpt.get();
            if (table.getStore() == null) {
                return false;
            }

            // 查詢該桌位在指定時間範圍內的預約
            List<ReservationBean> conflictingReservations = reservationRepository
                    .findConflictingReservationsInTimeRange(
                            table.getStore().getId(),
                            date, startTime, endTime);

            if (conflictingReservations == null) {
                return true;
            }

            // 檢查是否有預約使用了這個桌位
            return conflictingReservations.stream()
                    .filter(reservation -> reservation.getTables() != null)
                    .noneMatch(reservation -> reservation.getTables().stream()
                            .filter(t -> t != null && t.getId() != null)
                            .anyMatch(t -> t.getId().equals(tableId)));
        } catch (Exception e) {
            System.err.println("檢查桌位可用性時發生錯誤: " + e.getMessage());
            return false; // 發生錯誤時謹慎地返回不可用
        }
    }
}
