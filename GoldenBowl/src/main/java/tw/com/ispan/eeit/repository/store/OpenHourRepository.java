package tw.com.ispan.eeit.repository.store;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.store.OpenHourBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Repository
public interface OpenHourRepository extends JpaRepository<OpenHourBean, Integer> {

        /**
         * 根據餐廳ID查詢營業時間
         */
        List<OpenHourBean> findByStoreId(Integer storeId);

        /**
         * 根據餐廳查詢營業時間
         */

        List<OpenHourBean> findByStore(StoreBean store);

        List<OpenHourBean> findByStoreOrderByDayAsc(StoreBean store);

        List<OpenHourBean> findByStoreOrderByDayDesc(StoreBean store);

        /**
         * 根據餐廳ID和星期查詢營業時間
         */
        List<OpenHourBean> findByStoreIdAndDay(Integer storeId, Integer day);

        /**
         * 根據餐廳和星期查詢營業時間
         */
        Optional<OpenHourBean> findByStoreAndDay(StoreBean store, Integer day);

        /**
         * 根據餐廳和星期查詢營業時間（使用 DayOfWeek 枚舉）
         */
        default Optional<OpenHourBean> findByStoreAndDay(StoreBean store, DayOfWeek day) {
                // 將 DayOfWeek 轉換為數字（SUNDAY=0, MONDAY=1, ..., SATURDAY=6）
                Integer dayInt = day.getValue() == 7 ? 0 : day.getValue();
                return findByStoreAndDay(store, dayInt);
        }

        /**
         * 根據餐廳ID和星期查詢營業時間（使用 DayOfWeek 枚舉）
         */
        @Query("SELECT oh FROM OpenHourBean oh WHERE oh.store.id = :storeId AND oh.day = :day")
        List<OpenHourBean> findByStoreIdAndDay(@Param("storeId") Integer storeId, @Param("day") DayOfWeek day);

        /**
         * 根據餐廳ID和星期查詢營業時間（使用 DayOfWeek 枚舉）- 別名方法
         */
        default List<OpenHourBean> findByStoreIdAndDayOfWeek(Integer storeId, DayOfWeek dayOfWeek) {
                return findByStoreIdAndDay(storeId, dayOfWeek);
        }

        // 使用 CONVERT 函數的原生 SQL 查詢

        /**
         * 檢查特定時間是否在營業時間內（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT oh.* FROM open_hour oh
                        WHERE oh.store_id = :storeId
                        AND oh.day = :dayOfWeek
                        AND (
                            (CONVERT(time, oh.close_time) > CONVERT(time, oh.open_time)
                             AND CONVERT(time, :checkTime) BETWEEN CONVERT(time, oh.open_time) AND CONVERT(time, oh.close_time))
                            OR
                            (CONVERT(time, oh.close_time) < CONVERT(time, oh.open_time)
                             AND (CONVERT(time, :checkTime) >= CONVERT(time, oh.open_time) OR CONVERT(time, :checkTime) <= CONVERT(time, oh.close_time)))
                        )
                        """, nativeQuery = true)
        List<OpenHourBean> findOpenHoursContainingTime(
                        @Param("storeId") Integer storeId,
                        @Param("dayOfWeek") Integer dayOfWeek,
                        @Param("checkTime") java.time.LocalTime checkTime);

        /**
         * 查詢餐廳在特定時間範圍內的營業時間（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT oh.* FROM open_hour oh
                        WHERE oh.store_id = :storeId
                        AND oh.day = :dayOfWeek
                        AND (
                            (CONVERT(time, oh.open_time) <= CONVERT(time, :endTime)
                             AND CONVERT(time, oh.close_time) >= CONVERT(time, :startTime))
                            OR
                            (CONVERT(time, oh.close_time) < CONVERT(time, oh.open_time)
                             AND (CONVERT(time, oh.open_time) <= CONVERT(time, :endTime) OR CONVERT(time, oh.close_time) >= CONVERT(time, :startTime)))
                        )
                        """, nativeQuery = true)
        List<OpenHourBean> findOpenHoursInTimeRange(
                        @Param("storeId") Integer storeId,
                        @Param("dayOfWeek") Integer dayOfWeek,
                        @Param("startTime") java.time.LocalTime startTime,
                        @Param("endTime") java.time.LocalTime endTime);

        /**
         * 檢查餐廳是否在特定時間營業（使用 CONVERT 函數）
         */
        @Query(value = """
                        SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END
                        FROM open_hour oh
                        WHERE oh.store_id = :storeId
                        AND oh.day = :dayOfWeek
                        AND (
                            (CONVERT(time, oh.close_time) > CONVERT(time, oh.open_time)
                             AND CONVERT(time, :checkTime) BETWEEN CONVERT(time, oh.open_time) AND CONVERT(time, oh.close_time))
                            OR
                            (CONVERT(time, oh.close_time) < CONVERT(time, oh.open_time)
                             AND (CONVERT(time, :checkTime) >= CONVERT(time, oh.open_time) OR CONVERT(time, :checkTime) <= CONVERT(time, oh.close_time)))
                        )
                        """, nativeQuery = true)
        Integer isStoreOpenAtTime(
                        @Param("storeId") Integer storeId,
                        @Param("dayOfWeek") Integer dayOfWeek,
                        @Param("checkTime") java.time.LocalTime checkTime);
}
