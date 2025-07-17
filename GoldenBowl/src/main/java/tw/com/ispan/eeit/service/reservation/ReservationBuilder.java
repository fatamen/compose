package tw.com.ispan.eeit.service.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import tw.com.ispan.eeit.model.entity.reservation.ReservationBean;
import tw.com.ispan.eeit.model.enums.ReservationStatus;

/**
 * ReservationBean 建構器
 */
public class ReservationBuilder {

    /**
     * 建構待確認的預約
     */
    public static ReservationBean buildPendingReservation(
            Integer userId,
            Integer storeId,
            LocalDate reservedDate,
            LocalTime reservedTime,
            Integer guests,
            Integer duration,
            String content) {

        ReservationBean reservation = new ReservationBean();
        reservation.setUserId(userId);
        reservation.setStoreId(storeId);
        reservation.setReservedDate(reservedDate);
        reservation.setReservedTime(reservedTime);
        reservation.setGuests(guests);
        reservation.setDuration(duration);
        reservation.setContent(content);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());

        return reservation;
    }

    /**
     * 建構已確認的預約
     */
    public static ReservationBean buildConfirmedReservation(
            Integer userId,
            Integer storeId,
            LocalDate reservedDate,
            LocalTime reservedTime,
            Integer guests,
            Integer duration,
            String content) {

        ReservationBean reservation = buildPendingReservation(userId, storeId, reservedDate, reservedTime, guests,
                duration, content);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservation;
    }

    /**
     * 建構已取消的預約
     */
    public static ReservationBean buildCancelledReservation(
            Integer userId,
            Integer storeId,
            LocalDate reservedDate,
            LocalTime reservedTime,
            Integer guests,
            Integer duration,
            String content) {

        ReservationBean reservation = buildPendingReservation(userId, storeId, reservedDate, reservedTime, guests,
                duration, content);
        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservation;
    }
}