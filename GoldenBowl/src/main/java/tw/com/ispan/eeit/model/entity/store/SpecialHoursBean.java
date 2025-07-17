package tw.com.ispan.eeit.model.entity.store;

import java.time.LocalDate;
import java.time.LocalTime;

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

@Data
@Entity
@Table(name = "special_hours")
@NoArgsConstructor
public class SpecialHoursBean {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	@Column(name = "store_id")
	private Integer storeId;
	// 修正：雖然資料庫是 datetime2(6)，但我們只需要日期部分
	@Column(name = "date")
	private LocalDate date;
	@Column(name = "open_time", columnDefinition = "time(0)")
	private LocalTime openTime;
	@Column(name = "close_time", columnDefinition = "time(0)")
	private LocalTime closeTime;
	@Column(name = "is_close")
	private Boolean isClose;
}
