package tw.com.ispan.eeit.model.entity.order;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Hibernate;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.comment.CommentBean;
import tw.com.ispan.eeit.model.entity.promotion.PromotionBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Data
@Entity
@Table(name = "customer_order")
@NoArgsConstructor
@EqualsAndHashCode(of = "id") // <--- 在這裡加上這一行
public class OrderBean {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	@JsonBackReference
	private UserBean user;

	private Integer total;

	@Column(length = 10)
	private String status;

	@Column(name = "create_time")
	private LocalDateTime createTime;

	@Column(length = 50)
	private String content;

	@Column(name = "pickup_time")
	private LocalDateTime pickupTime;
	// ------------comment資料夾-----------------------------------
	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
	@JsonManagedReference
	private CommentBean comment;
	// mappedBy="order" 是comment的java屬性order

	// ------------order 資料夾-----------------------------------
	@OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<OrderDetailBean> orderDetails;
	// mappedBy的是java物件名稱

	// ------------promotion資料夾---------------------------------
	@ManyToOne
	@JoinColumn(name = "promotion_id")
	@JsonBackReference
	private PromotionBean promotion; // 假設 Promotion Entity 存在
	// ------------store 資料夾-----------------------------------
	@ManyToOne
	@JoinColumn(name = "store_id") // customer_order表中的欄位
	@JsonIgnore
	private StoreBean store;

	@Override
	public String toString() {
		// 安全地獲取 ID，而不觸發懶加載
		Integer userId = (user != null) ? user.getId() : null;
		Integer storeId = (store != null) ? store.getId() : null;
		Integer promotionId = (promotion != null) ? promotion.getId() : null;

		// 檢查集合是否已初始化，以避免 LazyInitializationException
		String orderDetailsInfo = (orderDetails != null && Hibernate.isInitialized(orderDetails))
				? "orderDetails.size=" + orderDetails.size()
				: "orderDetails=[Not Loaded]";

		return "OrderBean ["
				+ "id=" + id
				+ ", userId=" + userId
				+ ", storeId=" + storeId
				+ ", promotionId=" + promotionId
				+ ", total=" + total
				+ ", status='" + status + "'"
				+ ", createTime=" + createTime
				+ ", content='" + content + "'"
				+ ", pickupTime=" + pickupTime
				+ ", " + orderDetailsInfo
				+ ", "
				+ "]";
	}

}
