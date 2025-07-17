package tw.com.ispan.eeit.model.entity.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "order_detail_spec")
public class OrderDetailSpecBean {

	@Id
	@Column(name = "id")
	private int id;

	@Column(name = "order_id")
	private int orderId;
	@Column(name = "food_id")
	private int foodId;
	@Column(name = "name")
	private String name;
	@Column(name = "quantity")
	private int quantity;
	@Column(name = "price")
	private int price;
	@Column(name = "sub_total")
	private int subTotal;
	@Column(name = "total")
	private int total;

}
