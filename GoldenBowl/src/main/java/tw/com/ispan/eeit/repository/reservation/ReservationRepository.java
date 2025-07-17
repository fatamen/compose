package tw.com.ispan.eeit.repository.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tw.com.ispan.eeit.model.dto.reservation.ReservationDTO;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.reservation.ReservationBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.model.enums.ReservationStatus;

public interface ReservationRepository extends JpaRepository<ReservationBean, Integer> {

        // 🔐 保留方法（Entity 查詢用途）

        // 使用者查詢簡易入口
        List<ReservationBean> findByUserId(Integer userId);

        // 餐廳查詢簡易入口
        List<ReservationBean> findByStoreId(Integer storeId);

        // 衝突判斷邏輯（預約重疊）
        @Query("SELECT r FROM ReservationBean r WHERE r.storeId = :storeId AND r.reservedDate = :date AND r.reservedTime BETWEEN :startTime AND :endTime AND r.status IN ('CONFIRMED', 'PENDING')")
        List<ReservationBean> findConflictingReservations(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        // 有效預約統計
        @Query(value = """
                        SELECT COUNT(*) FROM reservation r
                        WHERE r.store_id = :storeId
                        AND r.reserved_date = :date
                        AND CONVERT(time, r.reserved_time) >= CONVERT(time, :startTime)
                        AND CONVERT(time, r.reserved_time) <= CONVERT(time, :endTime)
                        AND r.status IN ('CONFIRMED', 'PENDING')
                        """, nativeQuery = true)
        Integer countReservationsInTimeRange(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        // 客人總數統計
        @Query(value = """
                        SELECT COALESCE(SUM(r.guests), 0) FROM reservation r
                        WHERE r.store_id = :storeId
                        AND r.reserved_date = :date
                        AND CONVERT(time, r.reserved_time) >= CONVERT(time, :startTime)
                        AND CONVERT(time, r.reserved_time) <= CONVERT(time, :endTime)
                        AND r.status IN ('CONFIRMED', 'PENDING')
                        """, nativeQuery = true)
        Integer sumGuestsInTimeRange(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        // 時段是否已被占用
        @Query(value = """
                        SELECT COUNT(*) FROM reservation r
                        WHERE r.store_id = :storeId
                        AND r.reserved_date = :date
                        AND CONVERT(time, r.reserved_time) = :time
                        AND r.status IN ('CONFIRMED', 'PENDING')
                        """, nativeQuery = true)
        Integer hasReservationAtTime(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("time") LocalTime time);

        // 歷史訂位查詢
        @Query("SELECT r FROM ReservationBean r WHERE r.userId = :userId ORDER BY r.reservedDate DESC")
        List<ReservationBean> findByUserIdOrderByReservedDateDesc(@Param("userId") Integer userId);

        // ✨ 查詢中心 - searchReservations
        @Query("""
                            SELECT new tw.com.ispan.eeit.model.dto.reservation.ReservationDTO(
                                r.id,
                                u.name,
                                s.name,
                                r.reservedDate,
                                r.reservedTime,
                                r.guests,
                                r.status
                            )
                            FROM ReservationBean r
                            LEFT JOIN UserBean u ON r.userId = u.id
                            LEFT JOIN StoreBean s ON r.storeId = s.id
                            WHERE (:storeId IS NULL OR r.storeId = :storeId)
                              AND (:userId IS NULL OR r.userId = :userId)
                              AND (:status IS NULL OR r.status = :status)
                              AND (:date IS NULL OR r.reservedDate = :date)
                        """)
        List<ReservationDTO> searchReservations(
                        @Param("storeId") Integer storeId,
                        @Param("userId") Integer userId,
                        @Param("status") ReservationStatus status,
                        @Param("date") LocalDate date);

        // 🔧 BookingAvailabilityService 專用方法

        // 查詢時間範圍內的衝突預約（使用 CONVERT 函數）
        @Query(value = """
                        SELECT * FROM reservation r
                        WHERE r.store_id = :storeId
                        AND r.reserved_date = :date
                        AND CONVERT(time, r.reserved_time) >= CONVERT(time, :startTime)
                        AND CONVERT(time, r.reserved_time) <= CONVERT(time, :endTime)
                        AND r.status IN ('CONFIRMED', 'PENDING')
                        """, nativeQuery = true)
        List<ReservationBean> findConflictingReservationsInTimeRange(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime);

        // 查詢餐廳特定日期的預約
        @Query("SELECT r FROM ReservationBean r WHERE r.storeId = :storeId AND r.reservedDate = :date")
        List<ReservationBean> findByStoreIdAndReservedDate(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date);
}
