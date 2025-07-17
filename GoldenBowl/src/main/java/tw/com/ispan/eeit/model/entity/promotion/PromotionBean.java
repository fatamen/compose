package tw.com.ispan.eeit.model.entity.promotion;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.food.TagBean;
import tw.com.ispan.eeit.model.entity.order.OrderBean;
import tw.com.ispan.eeit.model.entity.plan.PlanBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Data
@NoArgsConstructor
@Entity
@Table(name = "promotion")
public class PromotionBean {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plan_id") // sql當中的promotion table FK
	@JsonBackReference
	private PlanBean plan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	@JoinColumn(name = "store_id")
	private StoreBean store;

	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	@JoinColumn(name = "tag_id")
	private TagBean tag;

	@Column(columnDefinition = "NVARCHAR(50)")
	private String title;

	@Column(columnDefinition = "NVARCHAR(50)")
	private String description;

	@Column(name = "discount_type", columnDefinition = "NVARCHAR(50)")
	private String discountType;

	@Column(name = "discount_value", columnDefinition = "NVARCHAR(50)")
	private String discountValue;

	@Column(name = "min_spend")
	private Integer minSpend;

	@Column(name = "start_time")
	private LocalDateTime startTime;

	@Column(name = "end_time")
	private LocalDateTime endTime;

	@Column(name = "created_time")
	private LocalDateTime createdTime;

	@Column(name = "updated_time")
	private LocalDateTime updatedTime;

	@Column
	private String code;

	@Column(name = "max_usage")
	private Integer maxUsage;

	@Column(name = "user_usage_limit")
	private Integer userUsageLimit;

	@Column(length = 20)
	private String status;

	@OneToMany(mappedBy = "promotion")
	private List<OrderBean> orders;

	@OneToMany(mappedBy = "promotion")
	private List<NotificationBean> notifications;
}