package tw.com.ispan.eeit.model.entity.comment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "report_type")
@Data
@NoArgsConstructor
public class ReportTypeBean {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100)
    private String type;

    @Column(length = 100)
    private String description;

    @Column(name = "prime")
    private Integer prime;
}
