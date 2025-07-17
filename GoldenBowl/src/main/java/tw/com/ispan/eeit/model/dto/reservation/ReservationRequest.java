package tw.com.ispan.eeit.model.dto.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class ReservationRequest {
    private Integer userId;
    private Integer storeId;
    private LocalDate reservedDate;
    private LocalTime reservedTime;
    private Integer guests;
    private Integer duration;
    private List<Integer> tableIds;
}