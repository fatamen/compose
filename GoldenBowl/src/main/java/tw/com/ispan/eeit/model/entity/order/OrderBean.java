package tw.com.ispan.eeit.model.entity.order;

import java.time.LocalDateTime;
import java.util.List;

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

	
//	@OneToOne
//	@JoinColumn(name = "order_id")
//	private OrderBean order;
	
	@OneToOne(mappedBy = "order", fetch = FetchType.LAZY)
	@JsonManagedReference
	private PaymentBean payment;


}
