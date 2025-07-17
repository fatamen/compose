package tw.com.ispan.eeit.service.reservation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.dto.reservation.ReservationDTO;
import tw.com.ispan.eeit.model.dto.reservation.ReservationSearchRequest;
import tw.com.ispan.eeit.model.entity.reservation.ReservationBean;
import tw.com.ispan.eeit.model.enums.ReservationStatus;
import tw.com.ispan.eeit.repository.reservation.ReservationRepository;

@Service
public class ReservationQueryService {

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * 通用查詢方法 - 使用 searchReservations
     */
    public List<ReservationDTO> searchReservations(ReservationSearchRequest request) {
        return reservationRepository.searchReservations(
                request.storeId(),
                request.userId(),
                request.status(),
                request.date());
    }

    /**
     * 查詢用戶未來預約
     */
    public List<ReservationBean> findUpcomingReservationsByUserId(Integer userId) {
        LocalDate today = LocalDate.now();
        return reservationRepository.findByUserIdOrderByReservedDateDesc(userId)
                .stream()
                .filter(r -> r.getReservedDate().isAfter(today) ||
                        (r.getReservedDate().equals(today)
                                && r.getReservedTime().isAfter(java.time.LocalDateTime.now().toLocalTime())))
                .toList();
    }

    /**
     * 查詢餐廳未來預約
     */
    public List<ReservationBean> findUpcomingReservationsByStoreId(Integer storeId) {
        LocalDate today = LocalDate.now();
        return reservationRepository.findByStoreId(storeId)
                .stream()
                .filter(r -> r.getReservedDate().isAfter(today) ||
                        (r.getReservedDate().equals(today)
                                && r.getReservedTime().isAfter(java.time.LocalDateTime.now().toLocalTime())))
                .toList();
    }

    /**
     * 查詢用戶歷史預約
     */
    public List<ReservationBean> findHistoricalReservationsByUserId(Integer userId) {
        LocalDate today = LocalDate.now();
        return reservationRepository.findByUserIdOrderByReservedDateDesc(userId)
                .stream()
                .filter(r -> r.getReservedDate().isBefore(today) ||
                        (r.getReservedDate().equals(today)
                                && r.getReservedTime().isBefore(java.time.LocalDateTime.now().toLocalTime())))
                .toList();
    }

    /**
     * 查詢特定狀態的預約
     */
    public List<ReservationDTO> findReservationsByStatus(ReservationStatus status) {
        return reservationRepository.searchReservations(null, null, status, null);
    }

    /**
     * 查詢特定日期的預約
     */
    public List<ReservationDTO> findReservationsByDate(LocalDate date) {
        return reservationRepository.searchReservations(null, null, null, date);
    }

    /**
     * 查詢用戶特定狀態的預約
     */
    public List<ReservationDTO> findUserReservationsByStatus(Integer userId, ReservationStatus status) {
        return reservationRepository.searchReservations(null, userId, status, null);
    }

    /**
     * 查詢餐廳特定狀態的預約
     */
    public List<ReservationDTO> findStoreReservationsByStatus(Integer storeId, ReservationStatus status) {
        return reservationRepository.searchReservations(storeId, null, status, null);
    }
}