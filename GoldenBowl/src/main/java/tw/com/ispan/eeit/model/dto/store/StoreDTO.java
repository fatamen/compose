package tw.com.ispan.eeit.model.dto.store;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.comment.CommentBean;
import tw.com.ispan.eeit.model.entity.food.FoodBean;
import tw.com.ispan.eeit.model.entity.food.TagBean;
import tw.com.ispan.eeit.model.entity.store.CategoryBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

// 範例 StoreDTO (請根據您的實際需求調整，確保 foods 內的 tags 也能被映射)
@Data // 提供 getter, setter, equals, hashCode, toString
@NoArgsConstructor // 提供無參數構造函數 (用於 Jackson 反序列化 JSON 到 DTO)
@AllArgsConstructor // 提供所有字段的構造函數 (方便手動創建 DTO)
public class StoreDTO {
    private Integer id;
    private Integer ownerId;
    private String name;
    private String address;
    private Double lng;
    private Double lat;
    private String storeIntro;
    private String photo;
    private Float score;
    private Boolean isOpen;
    private List<String> categoryNames; // 只傳遞類別名稱
    private List<CommentDTO> comments; // 傳遞評論的精簡數據
    private List<FoodDTO> foods; // 傳遞食物的精簡數據 (內部包含 tags)
    private Integer deliveryTime;
    private Double popularityScore;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Boolean isActive;
    private String phone;
    private String email;
    private Boolean isEmailVerified;

    // 新增：表示當前用戶是否收藏了該餐廳
    private Boolean isFavorited; // 預設為 false，除非明確設置

    // 若你要給前端顯示空間點座標，可以自行決定怎麼包
    private String storeCoords; // 通常轉 WKT 字串或 GeoJSON 字串。要怎麼顯示決定於你的前端設計

    // 這個構造函數用於將 StoreBean 轉換為 StoreDTO
    public StoreDTO(StoreBean store) {
        this.id = store.getId();
        this.ownerId = store.getOwner() != null ? store.getOwner().getId() : null;
        this.name = store.getName();
        this.photo = store.getPhoto();
        this.score = store.getScore();
        this.isOpen = store.getIsOpen();
        this.address = store.getAddress();
        this.lng = store.getLng();
        this.lat = store.getLat();
        this.storeIntro = store.getStoreIntro();
        this.photo = store.getPhoto();
        this.score = store.getScore();
        this.isOpen = store.getIsOpen();
        this.createdTime = store.getCreatedTime();
        this.updatedTime = store.getUpdatedTime();
        this.isActive = store.getIsActive();

        // 處理 storeCoords (GEOGRAPHY Point 物件轉換為字串)
        if (store.getStoreCoords() != null) {
            // 使用 toText() 方法將 JTS Point 物件轉換為 WKT (Well-Known Text) 格式的字串
            this.storeCoords = store.getStoreCoords().toText();
        } else {
            this.storeCoords = null;
        }

        // 這裡是關鍵：從 owner 取得 phone/email
        if (store.getOwner() != null) {
            this.phone = store.getOwner().getPhone();
            this.email = store.getOwner().getEmail();
            this.isEmailVerified = store.getOwner().getIsVerify();
        } else {
            this.phone = null;
            this.email = null;
            this.isEmailVerified = false;
        }

        // 這裡需要注意：當這些關聯是懶加載時，直接訪問它們可能會觸發懶加載異常
        // 但由於是在 DTO 轉換器中，通常會期望它們已經被加載（通過 EntityGraph 或 JOIN FETCH）
        // 如果它們仍是懶加載且不在事務中，可能會報 LazyInitializationException
        this.categoryNames = store.getCategories() != null
                ? store.getCategories().stream().map(CategoryBean::getName).collect(Collectors.toList())
                : new ArrayList<>();

        this.comments = store.getComments() != null
                ? store.getComments().stream()
                        .filter(comment -> !comment.getIsHidden()) // <-- 新增的過濾條件
                        .map(CommentDTO::new)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        this.foods = store.getFoods() != null ? store.getFoods().stream().map(FoodDTO::new).collect(Collectors.toList())
                : new ArrayList<>();

        this.deliveryTime = 20; // 暫時寫死
        this.popularityScore = (double) (store.getScore() != null ? store.getScore() * 10 : 0);
    }

    // 重載構造函數，用於在包含用戶上下文時設置 isFavorited
    public StoreDTO(StoreBean store, Boolean isFavorited) {
        this(store); // 調用上面的構造函數初始化基本資訊
        this.isFavorited = isFavorited;
    }

    // 定義內部 FoodDTO
    @Data // 為內部類也添加 Lombok 註解
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodDTO {
        private Integer id;
        private String name;
        private Integer price;
        private List<String> tagNames; // 只傳遞標籤名稱

        public FoodDTO(FoodBean food) {
            this.id = food.getId();
            this.name = food.getName();
            this.price = food.getPrice();
            this.tagNames = food.getTags().stream()
                    .map(TagBean::getName)
                    .collect(Collectors.toCollection(LinkedHashSet::new)) // 使用 LinkedHashSet 保留順序並去重
                    .stream()
                    .collect(Collectors.toList());
        }
    }

    // 定義內部 CommentDTO
    @Data // 為內部類也添加 Lombok 註解
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDTO {
        private Integer id;
        private Integer userId;
        private String content;
        private Integer score;

        public CommentDTO(CommentBean comment) {
            this.id = comment.getId();
            // 確保 UserBean 不為空，並且如果 UserBean 有懶加載的關聯，可能也要處理
            this.userId = comment.getUser() != null ? comment.getUser().getId() : null;
            this.content = comment.getContent();
            this.score = comment.getScore();
        }
    }
    // @com.fasterxml.jackson.annotation.JsonProperty("score") // 或直接用 @JsonProperty
    // 如果有 import
    // public String getScoreStr() {
    // if (score == null) return null;
    // return String.format("%.1f", score);
    // }
}