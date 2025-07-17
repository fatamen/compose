package tw.com.ispan.eeit.repository.store;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Repository
public interface StoreRepository extends JpaRepository<StoreBean, Integer> {

        @EntityGraph(attributePaths = { "categories", "comments", "foods" })
        Optional<StoreBean> findById(Integer id);

        @EntityGraph(attributePaths = { "categories", "foods", "foods.tags" })
        List<StoreBean> findAll();

        // 複雜查詢示例：模糊搜尋商店名稱、地址、分類名稱、食物名稱、食物標籤名稱
        // 注意：實際查詢會根據你的資料模型和關聯關係來調整。
        // 使用 JOIN FETCH 確保一次性載入所有相關聯的集合，避免 N+1 問題和 LazyInitializationException。
        @Query("SELECT s FROM StoreBean s " + // 移除 DISTINCT
                        "LEFT JOIN FETCH s.categories c " +
                        "LEFT JOIN FETCH s.foods f " +
                        "LEFT JOIN FETCH f.tags t " +
                        "WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(s.address) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(s.storeIntro) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(f.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                        "OR LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        List<StoreBean> findStoresBySearchTerm(@Param("searchTerm") String searchTerm);

        // 如果你的 getAllStores() 也要載入所有細節，也可以定義類似的 @Query
        @Query("SELECT DISTINCT s FROM StoreBean s " +
                        "LEFT JOIN FETCH s.categories c " +
                        "LEFT JOIN FETCH s.foods f " +
                        "LEFT JOIN FETCH f.tags t")
        List<StoreBean> findAllWithDetails(); // 建議你這樣修改 getAllStores，以確保資料一次載入

        @Query("SELECT s FROM StoreBean s LEFT JOIN FETCH s.comments WHERE s.id = :id")
        Optional<StoreBean> findByIdWithComments(@Param("id") Integer id);

        // 查詢用戶並同時載入其收藏的商店，避免 N+1 問題
        @Query("SELECT u FROM UserBean u LEFT JOIN FETCH u.favoriteStores WHERE u.id = :userId")
        Optional<UserBean> findByIdWithFavoriteStores(@Param("userId") Integer userId);

        // 判斷特定用戶是否收藏了特定商店 (更輕量級的查詢)
        @Query("SELECT COUNT(fs) > 0 FROM UserBean u JOIN u.favoriteStores fs WHERE u.id = :userId AND fs.id = :storeId")
        boolean existsFavoriteByUserIdAndStoreId(@Param("userId") Integer userId, @Param("storeId") Integer storeId);


         // 修改：回傳該Owner的所有Store（包含相關資料）
        @EntityGraph(attributePaths = { "categories", "foods", "foods.tags", "owner" })
        List<StoreBean> findByOwner_Id(Integer ownerId);

        
    // 新增：獲取Owner的最新Store（按建立時間排序）
    @Query("SELECT s FROM StoreBean s " +
           "LEFT JOIN FETCH s.categories " +
           "LEFT JOIN FETCH s.foods " +
           "WHERE s.owner.id = :ownerId " +
           "ORDER BY s.createdTime DESC")
    List<StoreBean> findByOwnerIdOrderByCreatedTimeDesc(@Param("ownerId") Integer ownerId);
    
    // 新增：獲取Owner的主要Store（第一個建立的）
    @Query("SELECT s FROM StoreBean s " +
           "LEFT JOIN FETCH s.categories " +
           "LEFT JOIN FETCH s.foods " +
           "WHERE s.owner.id = :ownerId " +
           "ORDER BY s.createdTime ASC")
    List<StoreBean> findByOwnerIdOrderByCreatedTimeAsc(@Param("ownerId") Integer ownerId);
    
    // 新增：檢查Owner是否擁有指定的Store
    @Query("SELECT COUNT(s) > 0 FROM StoreBean s WHERE s.id = :storeId AND s.owner.id = :ownerId")
    boolean existsByIdAndOwnerId(@Param("storeId") Integer storeId, @Param("ownerId") Integer ownerId);
 
    // 計算Owner擁有的Store數量（效能更好）
    long countByOwner_Id(Integer ownerId);

    // 如果上面的方法名稱解析有問題，可以使用 @Query
    @Query("SELECT COUNT(s) FROM StoreBean s WHERE s.owner.id = :ownerId")
    long countStoresByOwnerId(@Param("ownerId") Integer ownerId);
        


}
