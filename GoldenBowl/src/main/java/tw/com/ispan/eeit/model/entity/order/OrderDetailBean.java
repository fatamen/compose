package tw.com.ispan.eeit.model.entity.order;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.comment.LikedFoodBean;
import tw.com.ispan.eeit.model.entity.food.FoodBean;
import tw.com.ispan.eeit.model.entity.store.SpecBean;

@Data
@Entity
@Table(name = "order_detail")
@NoArgsConstructor
public class OrderDetailBean {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer quantity;

	private Integer price; // 這個字段通常是 redundant 的，因為 food bean 已經有 price 了。同上。

	@Column(name = "sub_total")
	private Integer subTotal;

	private Integer total;

	// ------------comment資料夾--------------------------------~---
	@OneToOne(mappedBy = "orderDetail")
	@JsonManagedReference
	private LikedFoodBean likedFood;
	// 0709 OneToMany修正成OneToOne
	// ------------food 資料夾-----------------------------------
	@ManyToOne
	@JoinColumn(name = "food_id")
	@JsonBackReference
	private FoodBean food;

	// ------------order 資料夾-----------------------------------
	@ManyToOne
	@JoinColumn(name = "order_id") // SQLServer的名稱
	@JsonBackReference
	private OrderBean order;

	// ------------多對多關聯表--------------------------------------
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "order_detail_spec", joinColumns = @JoinColumn(name = "spec_id"), inverseJoinColumns = @JoinColumn(name = "order_detail_id"))
	@JsonManagedReference
	private List<SpecBean> specs;
}
