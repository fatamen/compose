package tw.com.ispan.eeit.model.entity.store;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

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

@Entity
@Table(name = "open_hour")
@Data
@NoArgsConstructor
public class OpenHourBean {
	// 時間格式定義，與資料庫的 TIME(0) 匹配
	private static final String TIME_FORMAT = "HH:mm";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	// 關聯到餐廳
	@ManyToOne
	@JoinColumn(name = "store_id", nullable = false)
	@JsonBackReference
	private StoreBean store;

	// 使用數字存儲星期（0=SUNDAY, 1=MONDAY, ..., 6=SATURDAY）
	@Column(name = "day", nullable = false)
	private Integer day;

	@Column(name = "open_time", columnDefinition = "TIME(0)")
	private LocalTime openTime;
	@Column(name = "close_time", columnDefinition = "TIME(0)")
	private LocalTime closeTime;

	// @Column(name = "is_open", nullable = false)
	// private Boolean isOpen = true;

	/**
	 * 取得 DayOfWeek 枚舉值
	 */
	public DayOfWeek getDayOfWeek() {
		// 將數字轉換為 DayOfWeek（0=SUNDAY, 1=MONDAY, ..., 6=SATURDAY）
		return DayOfWeek.of(day == 0 ? 7 : day);
	}

	/**
	 * 設定 DayOfWeek 枚舉值
	 */
	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		// 將 DayOfWeek 轉換為數字（SUNDAY=0, MONDAY=1, ..., SATURDAY=6）
		this.day = dayOfWeek.getValue() == 7 ? 0 : dayOfWeek.getValue();
	}
}
