package tw.com.ispan.eeit.model.entity.comment;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Data
@NoArgsConstructor
@Entity
@Table(name = "category_searched")
public class CategorySearchedBean {
    @EmbeddedId
    private CategorySearchedId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserBean user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("storeId")
    @JoinColumn(name = "store_id")
    @JsonBackReference
    private StoreBean store;

    @Column
    private Integer counter;
}

@Embeddable
@Data
class CategorySearchedId implements java.io.Serializable {
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "store_id")
    private Integer storeId;
}
