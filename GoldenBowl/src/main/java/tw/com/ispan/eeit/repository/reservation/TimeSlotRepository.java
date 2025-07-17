package tw.com.ispan.eeit.repository.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.reservation.TimeSlot;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Integer> {

        // 根據餐廳查詢時段
        List<TimeSlot> findByStore(StoreBean store);

        // 根據餐廳和日期查詢時段
        List<TimeSlot> findByStoreAndDay(StoreBean store, LocalDate day);

        // 根據餐廳、日期和狀態查詢時段
        List<TimeSlot> findByStoreAndDayAndIsActive(StoreBean store, LocalDate day, Boolean isActive);

        // 根據餐廳查詢可用的時段（所有啟用的時段）
        @Query("SELECT ts FROM TimeSlot ts WHERE ts.store = :store AND ts.isActive = true ORDER BY ts.day, ts.startTime")
        List<TimeSlot> findAvailableTimeSlotsByStore(@Param("store") StoreBean store);

        // 根據餐廳和日期範圍查詢時段（只查詢啟用的）
        @Query("SELECT ts FROM TimeSlot ts WHERE ts.store = :store AND ts.day BETWEEN :startDate AND :endDate AND ts.isActive = true ORDER BY ts.day, ts.startTime")
        List<TimeSlot> findActiveTimeSlotsByStoreAndDateRange(
                        @Param("store") StoreBean store,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // 根據餐廳、日期和精確時間查詢時段
        @Query("SELECT ts FROM TimeSlot ts WHERE ts.store = :store AND ts.day = :day AND ts.startTime = :startTime AND ts.isActive = true")
        List<TimeSlot> findTimeSlotsByStoreAndDayAndExactTime(
                        @Param("store") StoreBean store,
                        @Param("day") LocalDate day,
                        @Param("startTime") LocalTime startTime);

        // 根據餐廳、日期和時間範圍查詢時段 - 使用JPQL
        @Query("SELECT ts FROM TimeSlot ts WHERE ts.store = :store AND ts.day = :day AND ts.startTime >= :startTime AND ts.startTime < :endTime AND ts.isActive = true")
        List<TimeSlot> findTimeSlotsByStoreAndDayAndTimeRange(
                        @Param("store") StoreBean store,
                        @Param("day") LocalDate day,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        // 根據餐廳和日期範圍查詢時段（包含所有狀態）
        @Query("SELECT ts FROM TimeSlot ts WHERE ts.store = :store AND ts.day BETWEEN :startDate AND :endDate ORDER BY ts.day, ts.startTime")
        List<TimeSlot> findTimeSlotsByStoreAndDateRange(
                        @Param("store") StoreBean store,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // 根據餐廳、日期範圍和狀態查詢時段
        @Query("SELECT ts FROM TimeSlot ts WHERE ts.store = :store AND ts.day BETWEEN :startDate AND :endDate AND ts.isActive = :isActive ORDER BY ts.day, ts.startTime")
        List<TimeSlot> findTimeSlotsByStoreAndDateRangeAndStatus(
                        @Param("store") StoreBean store,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("isActive") Boolean isActive);

        // 查詢餐廳的可用時段（未來日期且啟用）- 重新命名避免重複
        @Query("SELECT ts FROM TimeSlot ts WHERE ts.store = :store AND ts.day >= :today AND ts.isActive = true ORDER BY ts.day, ts.startTime")
        List<TimeSlot> findAvailableTimeSlotsByStoreFromToday(
                        @Param("store") StoreBean store,
                        @Param("today") LocalDate today);

        // 統計餐廳在日期範圍內的時段數量
        @Query("SELECT COUNT(ts) FROM TimeSlot ts WHERE ts.store = :store AND ts.day BETWEEN :startDate AND :endDate")
        Long countByStoreAndDateRange(
                        @Param("store") StoreBean store,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // 檢查時段是否存在（使用原生 SQL）
        @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM time_slots WHERE store_id = :storeId AND day = :day AND start_time = CONVERT(time, :startTime)", nativeQuery = true)
        Integer existsByStoreIdAndDayAndStartTime(@Param("storeId") Integer storeId, @Param("day") LocalDate day,
                        @Param("startTime") LocalTime startTime);

        // 呼叫 SQL 儲存程序來大宗生成時段
        @Query(value = "EXEC sp_generate_time_slots @store_id = :storeId, @days_ahead = :daysAhead", nativeQuery = true)
        Integer generateTimeSlotsUsingSP(
                        @Param("storeId") Integer storeId,
                        @Param("daysAhead") Integer daysAhead);

        // 使用 CONVERT 函數檢查時段與預約時間衝突（原生 SQL）
        @Query(value = """
                        SELECT ts.* FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :date
                        AND ts.start_time = CONVERT(time, :reservedTime)
                        AND ts.is_active = 1
                        """, nativeQuery = true)
        List<TimeSlot> findTimeSlotsByStoreAndDateAndReservedTime(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("reservedTime") java.time.LocalDateTime reservedTime);

        // === 移除重複的衝突查詢方法 - 此功能應由ReservationRepository負責 ===
        // 原 countConflictingReservationsInTimeRange 方法已移除

        // 使用 CONVERT 函數的更多時段查詢

        /**
         * 查詢特定時間範圍內的可用時段（使用 CONVERT 函數）- 重新命名避免重複
         */
        @Query(value = """
                        SELECT ts.* FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :date
                        AND ts.is_active = 1
                        AND CONVERT(time, ts.start_time) BETWEEN CONVERT(time, :startTime) AND CONVERT(time, :endTime)
                        ORDER BY ts.start_time
                        """, nativeQuery = true)
        List<TimeSlot> findActiveTimeSlotsInTimeRange(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        /**
         * 查詢與預約時間衝突的時段（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT ts.* FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :date
                        AND ts.is_active = 1
                        AND (
                            CONVERT(time, ts.start_time) <= CONVERT(time, :reservedTime)
                            AND CONVERT(time, ts.end_time) > CONVERT(time, :reservedTime)
                        )
                        """, nativeQuery = true)
        List<TimeSlot> findTimeSlotsContainingReservedTime(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("reservedTime") java.time.LocalDateTime reservedTime);

        /**
         * 檢查時段是否與預約時間重疊（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
                        FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :date
                        AND ts.is_active = 1
                        AND (
                            (CONVERT(time, ts.start_time) <= CONVERT(time, :startTime) AND CONVERT(time, ts.end_time) > CONVERT(time, :startTime))
                            OR
                            (CONVERT(time, ts.start_time) < CONVERT(time, :endTime) AND CONVERT(time, ts.end_time) >= CONVERT(time, :endTime))
                            OR
                            (CONVERT(time, ts.start_time) >= CONVERT(time, :startTime) AND CONVERT(time, ts.end_time) <= CONVERT(time, :endTime))
                        )
                        """, nativeQuery = true)
        Integer hasOverlappingTimeSlots(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        /**
         * 查詢指定時間之後的可用時段（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT ts.* FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :date
                        AND ts.is_active = 1
                        AND CONVERT(time, ts.start_time) >= CONVERT(time, :afterTime)
                        ORDER BY ts.start_time
                        """, nativeQuery = true)
        List<TimeSlot> findAvailableTimeSlotsAfterTime(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("afterTime") LocalTime afterTime);

        /**
         * 查詢指定時間之前的可用時段（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT ts.* FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :date
                        AND ts.is_active = 1
                        AND CONVERT(time, ts.end_time) <= CONVERT(time, :beforeTime)
                        ORDER BY ts.start_time DESC
                        """, nativeQuery = true)
        List<TimeSlot> findAvailableTimeSlotsBeforeTime(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("beforeTime") LocalTime beforeTime);

        /**
         * 根據 storeId、日期、開始時間和狀態查詢時段（使用 CONVERT 函數處理類型轉換）
         * 這個方法用來替代 Spring Data JPA 自動生成的方法，避免時間類型不相容錯誤
         */
        @Query(value = """
                        SELECT ts.* FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :day
                        AND CONVERT(time, ts.start_time) = CONVERT(time, :startTime)
                        AND ts.is_active = :isActive
                        """, nativeQuery = true)
        List<TimeSlot> findByStoreIdAndDayAndStartTimeAndIsActive(
                        @Param("storeId") Integer storeId,
                        @Param("day") LocalDate day,
                        @Param("startTime") LocalTime startTime,
                        @Param("isActive") Boolean isActive);

        /**
         * 根據 storeId、日期和開始時間查詢時段（使用 CONVERT 函數處理類型轉換）
         */
        @Query(value = """
                        SELECT ts.* FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :day
                        AND CONVERT(time, ts.start_time) = CONVERT(time, :startTime)
                        """, nativeQuery = true)
        List<TimeSlot> findByStoreIdAndDayAndStartTime(
                        @Param("storeId") Integer storeId,
                        @Param("day") LocalDate day,
                        @Param("startTime") LocalTime startTime);

        /**
         * 根據 storeId、日期和開始時間查詢時段（支援 LocalDateTime 參數）
         * 使用 CONVERT 函數處理類型轉換，避免時間類型不相容錯誤
         */
        @Query(value = """
                        SELECT ts.* FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :day
                        AND CONVERT(time, ts.start_time) = CONVERT(time, :startTime)
                        """, nativeQuery = true)
        List<TimeSlot> findByStoreIdAndDayAndStartTimeFromDateTime(
                        @Param("storeId") Integer storeId,
                        @Param("day") LocalDate day,
                        @Param("startTime") java.time.LocalDateTime startTime);

        /**
         * 根據 storeId、日期、開始時間和狀態查詢時段（支援 LocalDateTime 參數）
         * 使用 CONVERT 函數處理類型轉換，避免時間類型不相容錯誤
         */
        @Query(value = """
                        SELECT ts.* FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :day
                        AND CONVERT(time, ts.start_time) = CONVERT(time, :startTime)
                        AND ts.is_active = :isActive
                        """, nativeQuery = true)
        List<TimeSlot> findByStoreIdAndDayAndStartTimeFromDateTimeAndIsActive(
                        @Param("storeId") Integer storeId,
                        @Param("day") LocalDate day,
                        @Param("startTime") java.time.LocalDateTime startTime,
                        @Param("isActive") Boolean isActive);

        /**
         * 查詢餐廳時段資料的最新日期
         */
        @Query(value = "SELECT MAX(day) FROM time_slots WHERE store_id = :storeId", nativeQuery = true)
        LocalDate findMaxDateByStoreId(@Param("storeId") Integer storeId);

        /**
         * 根據 storeId、日期和狀態查詢時段
         */
        @Query(value = """
                        SELECT ts.* FROM time_slots ts
                        WHERE ts.store_id = :storeId
                        AND ts.day = :day
                        AND ts.is_active = :isActive
                        ORDER BY ts.start_time
                        """, nativeQuery = true)
        List<TimeSlot> findByStoreIdAndDayAndIsActive(
                        @Param("storeId") Integer storeId,
                        @Param("day") LocalDate day,
                        @Param("isActive") Boolean isActive);
}