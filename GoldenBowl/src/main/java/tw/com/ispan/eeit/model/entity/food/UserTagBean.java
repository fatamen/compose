package tw.com.ispan.eeit.model.entity.food;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.UserBean;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_tag")
public class UserTagBean {
    @EmbeddedId
    private UserTagId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private UserBean user;

    @ManyToOne
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private TagBean tag;

    @Column
    private Integer counter;

    @Column(name = "is_custom")
    private Boolean isCustom;
}

@Embeddable
@Data
class UserTagId implements java.io.Serializable {
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "tag_id")
    private Integer tagId;
}
