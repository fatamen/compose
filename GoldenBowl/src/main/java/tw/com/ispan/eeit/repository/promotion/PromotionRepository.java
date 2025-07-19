//資料存取層
package tw.com.ispan.eeit.repository.promotion;
//引入 List，因為你在下面的自訂查詢方法會回傳 List<Promotion> 型別
import java.util.List;

//引入 JpaRepository，Spring Data JPA 提供的介面，它會幫你自動實作常見的 CRUD 功能（例如 findAll()、save()、deleteById() 等等）
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//引入 @Repository 註解，這個註解告訴 Spring：「這是一個資料存取層的類別」，Spring 啟動時會自動偵測並把它加入 Spring 容器（IOC 容器）
import org.springframework.stereotype.Repository;

//引入你要操作的實體（Entity）類別 Promotion，這個 Repository 就是為了操作 promotion 資料表的資料，所以需要引入 Promotion 這個 Java 類別
import tw.com.ispan.eeit.model.entity.promotion.PromotionBean;

//定義一個介面 PromotionRepository，並且繼承 JpaRepository，就不用自己實作 CRUD 功能
//<Promotion,Integer> 告訴 JPA：這個 repository 操作的物件是 Promotion，Integer 是它的主鍵 id 的型別
@Repository
public interface PromotionRepository extends JpaRepository<PromotionBean, Integer> {
    List<PromotionBean> findByPlanId(Integer planId);
    List<PromotionBean> findByStoreId(Integer storeId);

    @Query("""
    	    SELECT p
    	    FROM PromotionBean p
    	    WHERE p.status = 'open'
    	      AND CURRENT_TIMESTAMP BETWEEN p.startTime AND p.endTime
    	      AND (
    	            (p.tag IS NULL AND p.minSpend <= :amount)
    	         OR (p.tag IS NOT NULL AND p.tag.id IN :tagIds)
    	      )
    	      AND (p.store IS NULL OR p.store.id = :storeId)
    	      AND (
    	        p.plan IS NULL OR EXISTS (
    	          SELECT 1 FROM SubRecordBean sr
    	          WHERE sr.user.id = :userId
    	            AND sr.plan = p.plan
    	            AND CURRENT_TIMESTAMP BETWEEN sr.startTime AND sr.endTime
    	        )
    	      )
    	""")
    	List<PromotionBean> findAvailablePromotions(
    	    @Param("userId") Integer userId,
    	    @Param("storeId") Integer storeId,
    	    @Param("amount") Integer amount,
    	    @Param("tagIds") List<Integer> tagIds
    	);


    @Query("SELECT DISTINCT o.promotion FROM OrderBean o WHERE o.user.id = :userId AND o.promotion IS NOT NULL")
    List<PromotionBean> findUsedByUserId(@Param("userId") Integer userId);

}

