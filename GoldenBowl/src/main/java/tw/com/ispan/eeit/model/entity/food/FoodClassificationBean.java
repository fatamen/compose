package tw.com.ispan.eeit.model.entity.food;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

/**
 * 這是一個代表「多對多」中間表的 Entity。
 * 它映射到 'food_class_id' 這張資料表，
 * 並將 FoodBean 和 FoodClassBean 之間的 Many-to-Many 關係，
 * 拆解成了兩個 One-to-Many 關係。
 * 它自己也包含了額外的業務欄位，如 store 和 sort。
 */
@Entity
@Table(name = "food_class_id") // 【關鍵】這裡的名稱要和你資料庫的中間表名稱完全一致！
@Data
@NoArgsConstructor
public class FoodClassificationBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 建議為中間表也加上自己的主鍵，雖然不是強制，但這是好習慣

    // --- 關聯到 FoodBean (多對一) ---
    @ManyToOne
    @JoinColumn(name = "food_id")
    @ToString.Exclude // 避免在 toString() 中產生無限迴圈
    @EqualsAndHashCode.Exclude // 避免在 equals/hashCode 中產生無限迴圈
    private FoodBean food;

    // --- 關聯到 FoodClassBean (多對一) ---
    @ManyToOne
    @JoinColumn(name = "food_class_id")
    private FoodClassBean foodClass;
    
    // --- 關聯到 StoreBean (多對一) ---
    @ManyToOne
    @JoinColumn(name = "store_id")
    private StoreBean store;
    
    // --- 中間表的額外欄位 ---
    @Column(name = "sort")
    private Integer sort;

    /**
     * 方便在 Service 層中建立實例的建構子
     * @param food 關聯的食物實體
     * @param foodClass 關聯的食物分類實體
     * @param store 關聯的店家實體
     * @param sort 排序值
     */
    public FoodClassificationBean(FoodBean food, FoodClassBean foodClass, StoreBean store, Integer sort) {
        this.food = food;
        this.foodClass = foodClass;
        this.store = store;
        this.sort = sort;
    }
}
