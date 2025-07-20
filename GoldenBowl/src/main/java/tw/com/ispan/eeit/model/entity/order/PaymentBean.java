package tw.com.ispan.eeit.model.entity.order;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
public class PaymentBean {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // ?? AUTO
	@Column(name = "id")
	private Integer id;
	
	@Column(name = "method", columnDefinition = "nvarchar(50)")
	private String method;
	@Column(name = "transaction_id", columnDefinition = "varchar(100)")
	private String transactionId;
	@Column(name = "total")
	private Integer total;
	@Column(name = "is_paid")
	private Boolean isPaid;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "paid_time")
	private Date paidTime;
	
	@OneToOne
	@JoinColumn(name = "order_id")
	@JsonBackReference
	private OrderBean order;


	
	

}
