package tw.com.ispan.eeit.model.entity.reservation;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Entity
@Table(name = "time_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @JsonIgnoreProperties({ "owner", "stores", "timeSlots", "tables", "reservations" })
    private StoreBean store;

    @Column(name = "day", columnDefinition = "date")
    private LocalDate day;

    @Column(name = "start_time", columnDefinition = "time(0)")
    private LocalTime startTime;

    @Column(name = "end_time", columnDefinition = "time(0)")
    private LocalTime endTime;

    @Column(name = "is_active")
    private Boolean isActive;
}