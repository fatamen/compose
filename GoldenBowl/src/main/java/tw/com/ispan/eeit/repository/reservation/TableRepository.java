package tw.com.ispan.eeit.repository.reservation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tw.com.ispan.eeit.model.entity.reservation.TableBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

public interface TableRepository extends JpaRepository<TableBean, Integer> {

    // 根據餐廳查詢桌位
    List<TableBean> findByStore(StoreBean store);

    // 根據餐廳和狀態查詢桌位
    List<TableBean> findByStoreAndStatus(StoreBean store, Boolean status);

    // 根據餐廳和座位數查詢桌位
    List<TableBean> findByStoreAndSeatsGreaterThanEqual(StoreBean store, Integer minSeats);

    // 根據餐廳、狀態和座位數查詢桌位
    List<TableBean> findByStoreAndStatusAndSeatsGreaterThanEqual(StoreBean store, Boolean status, Integer minSeats);

    // 統計餐廳的桌位數量
    @Query("SELECT COUNT(t) FROM TableBean t WHERE t.store = :store")
    Long countByStore(@Param("store") StoreBean store);

    // 統計餐廳可用桌位數量
    @Query("SELECT COUNT(t) FROM TableBean t WHERE t.store = :store AND t.status = true")
    Long countAvailableByStore(@Param("store") StoreBean store);

    // 根據餐廳和座位數查詢可用的桌位（使用StoreBean參數）
    @Query("SELECT t FROM TableBean t WHERE t.store = :store AND t.seats >= :minSeats AND t.status = true")
    List<TableBean> findAvailableTablesByStoreObjectAndMinSeats(
            @Param("store") StoreBean store,
            @Param("minSeats") Integer minSeats);

    // 根據餐廳ID查詢桌位
    @Query("SELECT t FROM TableBean t WHERE t.store.id = :storeId")
    List<TableBean> findByStoreId(@Param("storeId") Integer storeId);

    // 根據餐廳ID和座位數查詢可用的桌位（使用storeId參數）
    @Query("SELECT t FROM TableBean t WHERE t.store.id = :storeId AND t.seats >= :minSeats AND t.status = true")
    List<TableBean> findAvailableTablesByStoreIdAndMinSeats(
            @Param("storeId") Integer storeId,
            @Param("minSeats") Integer minSeats);
}
