package tw.com.ispan.eeit.model.entity.reservation;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "time_setting")
@Data // 包含 @Getter、@Setter、@ToString、@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class TimeSettingBean {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "store_id", nullable = false)
    private Integer storeId;

    @Column(name = "interval", nullable = false)
    private Integer interval;

    @Column(name = "meal_time", nullable = false, columnDefinition = "int default 90")
    private Integer mealTime = 90;
}
