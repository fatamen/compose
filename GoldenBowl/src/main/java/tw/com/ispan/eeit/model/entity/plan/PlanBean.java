package tw.com.ispan.eeit.model.entity.plan;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.promotion.PromotionBean;

@Data
@NoArgsConstructor
@JsonIgnoreProperties("plan") // 避免反向循環
@Entity
@Table(name = "plans")
public class PlanBean {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(columnDefinition = "NVARCHAR(50)")
	private String name;

	private Integer price;

	@Column(name = "valid_months")
	private Integer validMonths;

	@Column(name = "created_time")
	private LocalDateTime createdTime;

	@Column(name = "update_time")
	private LocalDateTime updateTime;

	@OneToOne(mappedBy = "plan",fetch = FetchType.LAZY) //對應到 promotionbean 中的 java 屬性名稱
	@JsonManagedReference
	private PromotionBean promotion;

	@OneToMany(mappedBy = "plan",fetch = FetchType.LAZY)
	@JsonIgnore
	private List<SubRecordBean> subRecords;
}
