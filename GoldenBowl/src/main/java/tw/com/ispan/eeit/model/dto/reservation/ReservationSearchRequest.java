package tw.com.ispan.eeit.model.dto.reservation;

import java.time.LocalDate;
import tw.com.ispan.eeit.model.enums.ReservationStatus;

public record ReservationSearchRequest(
        Integer storeId,
        Integer userId,
        ReservationStatus status,
        LocalDate date,
        LocalDate startDate,
        LocalDate endDate,
        Boolean upcomingOnly,
        Boolean historicalOnly) {

    public static ReservationSearchRequest of(Integer storeId, Integer userId, ReservationStatus status,
            LocalDate date) {
        return new ReservationSearchRequest(storeId, userId, status, date, null, null, false, false);
    }

    public static ReservationSearchRequest upcoming(Integer userId) {
        return new ReservationSearchRequest(null, userId, null, null, null, null, true, false);
    }

    public static ReservationSearchRequest historical(Integer userId) {
        return new ReservationSearchRequest(null, userId, null, null, null, null, false, true);
    }
}