package tw.com.ispan.eeit.repository.store;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.store.SpecialHoursBean;

@Repository
public interface SpecialHoursRepository extends JpaRepository<SpecialHoursBean, Integer> {

        /**
         * 根據餐廳ID和日期查詢特殊營業時間
         */
        @Query("SELECT sh FROM SpecialHoursBean sh WHERE sh.storeId = :storeId AND sh.date = :date")
        Optional<SpecialHoursBean> findByStoreIdAndDate(@Param("storeId") Integer storeId,
                        @Param("date") LocalDate date);

        /**
         * 根據餐廳ID查詢所有特殊營業時間
         */
        List<SpecialHoursBean> findByStoreId(Integer storeId);

        /**
         * 根據餐廳ID和日期範圍查詢特殊營業時間
         */
        @Query("SELECT sh FROM SpecialHoursBean sh WHERE sh.storeId = :storeId AND sh.date BETWEEN :startDate AND :endDate ORDER BY sh.date")
        List<SpecialHoursBean> findByStoreIdAndDateBetween(
                        @Param("storeId") Integer storeId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // 使用 CONVERT 函數的原生 SQL 查詢

        /**
         * 檢查特定時間是否在特殊營業時間內（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT sh.* FROM special_hours sh
                        WHERE sh.store_id = :storeId
                        AND sh.date = :date
                        AND sh.is_close = 0
                        AND sh.open_time IS NOT NULL
                        AND sh.close_time IS NOT NULL
                        AND (
                            (CONVERT(time, sh.close_time) > CONVERT(time, sh.open_time)
                             AND CONVERT(time, :checkTime) BETWEEN CONVERT(time, sh.open_time) AND CONVERT(time, sh.close_time))
                            OR
                            (CONVERT(time, sh.close_time) < CONVERT(time, sh.open_time)
                             AND (CONVERT(time, :checkTime) >= CONVERT(time, sh.open_time) OR CONVERT(time, :checkTime) <= CONVERT(time, sh.close_time)))
                        )
                        """, nativeQuery = true)
        List<SpecialHoursBean> findSpecialHoursContainingTime(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("checkTime") java.time.LocalTime checkTime);

        /**
         * 檢查特定日期是否為特殊休假日（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
                        FROM special_hours sh
                        WHERE sh.store_id = :storeId
                        AND sh.date = :date
                        AND sh.is_close = 1
                        """, nativeQuery = true)
        Integer isSpecialHolidayAtDate(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date);

        /**
         * 查詢餐廳在特定時間範圍內的特殊營業時間（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT sh.* FROM special_hours sh
                        WHERE sh.store_id = :storeId
                        AND sh.date = :date
                        AND sh.is_close = 0
                        AND sh.open_time IS NOT NULL
                        AND sh.close_time IS NOT NULL
                        AND (
                            (CONVERT(time, sh.open_time) <= CONVERT(time, :endTime)
                             AND CONVERT(time, sh.close_time) >= CONVERT(time, :startTime))
                            OR
                            (CONVERT(time, sh.close_time) < CONVERT(time, sh.open_time)
                             AND (CONVERT(time, sh.open_time) <= CONVERT(time, :endTime) OR CONVERT(time, sh.close_time) >= CONVERT(time, :startTime)))
                        )
                        """, nativeQuery = true)
        List<SpecialHoursBean> findSpecialHoursInTimeRange(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("startTime") java.time.LocalTime startTime,
                        @Param("endTime") java.time.LocalTime endTime);

        /**
         * 檢查餐廳在特定日期和時間是否有特殊營業時間（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
                        FROM special_hours sh
                        WHERE sh.store_id = :storeId
                        AND sh.date = :date
                        AND sh.is_close = 0
                        AND sh.open_time IS NOT NULL
                        AND sh.close_time IS NOT NULL
                        AND (
                            (CONVERT(time, sh.close_time) > CONVERT(time, sh.open_time)
                             AND CONVERT(time, :checkTime) BETWEEN CONVERT(time, sh.open_time) AND CONVERT(time, sh.close_time))
                            OR
                            (CONVERT(time, sh.close_time) < CONVERT(time, sh.open_time)
                             AND (CONVERT(time, :checkTime) >= CONVERT(time, sh.open_time) OR CONVERT(time, :checkTime) <= CONVERT(time, sh.close_time)))
                        )
                        """, nativeQuery = true)
        Integer hasSpecialHoursAtTime(
                        @Param("storeId") Integer storeId,
                        @Param("date") LocalDate date,
                        @Param("checkTime") java.time.LocalTime checkTime);

        /**
         * 查詢餐廳的特殊休假日列表
         */
        @Query(value = "SELECT sh.date FROM special_hours sh WHERE sh.store_id = :storeId AND sh.is_close = 1", nativeQuery = true)
        List<LocalDate> findClosedDates(@Param("storeId") Integer storeId);
}
