package tw.com.ispan.eeit.model.entity.comment;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.order.OrderBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Entity
@Table(name = "comment")
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id") // <--- 在這裡加上這一行
public class CommentBean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 500)
    private String content;

    private Integer score;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(length = 500)
    private String reply;

    @Column(name = "reply_update_time")
    private LocalDateTime replyUpdateTime;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    // 將 insertable = false, updatable = false 移除，因為通常關聯會自動處理外鍵
    // 或者，如果您只是想通過 ID 進行手動關聯，但同時需要對象實例，則保持它們。
    // 但更常見的做法是直接讓 JPA 管理這些外鍵。
    // 如果您保留了 id 字段並設置了 insertable=false, updatable=false，這表示您只讀取 id
    // 但在保存時 JPA 會使用 OrderBean 對象來設置外鍵。
    // 如果您刪除重複的 ID 字段，則無需 insertable = false, updatable = false
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id") // 移除 insertable = false, updatable = false 測試
    @JsonBackReference
    private OrderBean order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 移除 insertable = false, updatable = false 測試
    @JsonBackReference
    private UserBean user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // 移除 insertable = false, updatable = false 測試
    @JsonBackReference
    private StoreBean store;
}