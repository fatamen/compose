package tw.com.ispan.eeit.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.dto.reservation.ReservationRequest;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.reservation.ReservationBean;
import tw.com.ispan.eeit.model.entity.reservation.TableBean;
import tw.com.ispan.eeit.model.entity.reservation.TimeSlot;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.repository.UserRepository;
import tw.com.ispan.eeit.repository.reservation.TableRepository;
import tw.com.ispan.eeit.repository.reservation.TimeSlotRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;
import tw.com.ispan.eeit.service.reservation.ReservationService;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ReservationService reservationService;

    /**
     * 測試查詢所有用戶
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserBean>> getAllUsers() {
        List<UserBean> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * 測試查詢所有餐廳
     */
    @GetMapping("/stores")
    public ResponseEntity<List<StoreBean>> getAllStores() {
        List<StoreBean> stores = storeRepository.findAll();
        return ResponseEntity.ok(stores);
    }

    /**
     * 測試查詢餐廳桌位
     */
    @GetMapping("/stores/{storeId}/tables")
    public ResponseEntity<List<TableBean>> getStoreTables(@PathVariable Integer storeId) {
        try {
            List<TableBean> tables = tableRepository.findByStoreId(storeId);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 測試查詢餐廳時段（直接查詢，避免無限迴圈）
     */
    @GetMapping("/stores/{storeId}/time-slots")
    public ResponseEntity<List<TimeSlot>> getStoreTimeSlots(@PathVariable Integer storeId) {
        try {
            StoreBean store = storeRepository.findById(storeId).orElse(null);
            if (store == null) {
                return ResponseEntity.notFound().build();
            }
            List<TimeSlot> timeSlots = timeSlotRepository.findByStore(store);
            return ResponseEntity.ok(timeSlots);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 測試查詢餐廳可用桌位（未被預訂的桌位）
     */
    @GetMapping("/stores/{storeId}/available-tables")
    public ResponseEntity<List<TableBean>> getStoreAvailableTables(@PathVariable Integer storeId) {
        try {
            List<TableBean> tables = tableRepository.findByStoreId(storeId);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 測試建立訂位
     */
    @PostMapping("/create-reservation")
    public ResponseEntity<ReservationBean> createTestReservation(
            @RequestParam Integer userId,
            @RequestParam Integer storeId,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam Integer guests) {

        try {
            // 建立訂位請求
            ReservationRequest request = new ReservationRequest();
            request.setUserId(userId);
            request.setStoreId(storeId);
            request.setReservedDate(LocalDate.parse(date));
            request.setReservedTime(LocalTime.parse(time));
            request.setGuests(guests);
            request.setDuration(120); // 2小時

            // 建立訂位
            ReservationBean reservation = new ReservationBean();
            reservation.setUserId(userId);
            reservation.setStoreId(storeId);
            reservation.setReservedDate(request.getReservedDate());
            reservation.setReservedTime(request.getReservedTime());
            reservation.setGuests(request.getGuests());
            reservation.setDuration(request.getDuration());
            reservation.setContent("測試訂位");

            ReservationBean savedReservation = reservationService.createReservation(reservation, List.of(1));
            return ResponseEntity.ok(savedReservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 測試查詢用戶訂位
     */
    @GetMapping("/users/{userId}/reservations")
    public ResponseEntity<List<ReservationBean>> getUserReservations(@PathVariable Integer userId) {
        try {
            System.out.println("=== TestController 調試 ===");
            System.out.println("測試查詢用戶ID: " + userId + " 的訂位");

            List<ReservationBean> reservations = reservationService.getUserReservations(userId);
            System.out.println("查詢結果數量: " + (reservations != null ? reservations.size() : "null"));

            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            System.out.println("測試查詢失敗: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 測試查詢餐廳訂位
     */
    @GetMapping("/stores/{storeId}/reservations")
    public ResponseEntity<List<ReservationBean>> getStoreReservations(@PathVariable Integer storeId) {
        try {
            List<ReservationBean> reservations = reservationService.getStoreReservations(storeId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 測試生成時段
     */
    @PostMapping("/generate-time-slots")
    public ResponseEntity<String> generateTimeSlots(@RequestParam Integer storeId) {
        try {
            reservationService.generateTimeSlotsForRestaurant(storeId, 30);
            return ResponseEntity.ok("成功生成時段資料");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("生成時段失敗: " + e.getMessage());
        }
    }

    /**
     * 測試查詢可用桌位
     */
    @GetMapping("/available-tables")
    public ResponseEntity<List<TableBean>> getAvailableTables(
            @RequestParam Integer storeId,
            @RequestParam String startTime,
            @RequestParam(defaultValue = "120") int duration,
            @RequestParam(defaultValue = "1") int minSeats) {

        try {
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(startTime);
            List<TableBean> tables = reservationService.findAvailableTables(storeId, dateTime, duration, minSeats);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}