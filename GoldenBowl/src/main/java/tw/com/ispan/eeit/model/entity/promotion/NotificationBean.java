package tw.com.ispan.eeit.model.entity.promotion;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.UserBean;

@Data
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "orders", "notifications"}) //雙向關聯，所以加上去
@Entity
@Table(name = "notification")
public class NotificationBean {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "user_id")
	private UserBean user;

	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "promotion_id")
	private PromotionBean promotion;

	@Column(name = "created_time")
	private LocalDateTime createdTime;

	@Column(name = "is_read")
	private Boolean isRead;

	@Column(name = "read_time")
	private LocalDateTime readTime;
}