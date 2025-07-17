package tw.com.ispan.eeit.controller.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.service.reservation.ReservationService;

@RestController
@RequestMapping("/api/store-reservations")
@CrossOrigin(origins = "*")
public class StoreReservationController {

    @Autowired
    private ReservationService reservationService;

    /**
     * 搜尋餐廳
     */
    @GetMapping("/search")
    public Map<String, Object> searchStores(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isOpen) {
        return Map.of("success", false, "message", "功能尚未實現");
    }

    /**
     * 獲取餐廳完整資訊
     */
    @GetMapping("/{storeId}")
    public Map<String, Object> getStoreInfo(@PathVariable Integer storeId) {
        return Map.of("success", false, "message", "功能尚未實現");
    }

    /**
     * 檢查餐廳可用性
     */
    @GetMapping("/{storeId}/availability")
    public Map<String, Object> checkAvailability(
            @PathVariable Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam Integer guests) {
        try {
            boolean available = reservationService.checkAvailability(storeId, date, time, guests);
            return Map.of("success", true, "available", available);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * 創建訂位（整合版本）
     */
    @PostMapping("/{storeId}/reservations")
    public Map<String, Object> createReservation(
            @PathVariable Integer storeId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer userId = Integer.parseInt(request.get("userId").toString());
            LocalDate reservedDate = LocalDate.parse(request.get("reservedDate").toString());
            LocalTime reservedTime = LocalTime.parse(request.get("reservedTime").toString());
            Integer guests = Integer.parseInt(request.get("guests").toString());
            Integer duration = Integer.parseInt(request.get("duration").toString());
            String content = (String) request.get("content");

            var reservation = reservationService.createReservation(
                    userId, storeId, reservedDate, reservedTime, guests, duration, content);
            return Map.of("success", true, "reservationId", reservation.getId());
        } catch (Exception e) {
            return Map.of("success", false, "message", "參數錯誤: " + e.getMessage());
        }
    }

    /**
     * 獲取餐廳訂位統計
     */
    @GetMapping("/{storeId}/stats")
    public Map<String, Object> getStoreStats(@PathVariable Integer storeId) {
        return Map.of("success", false, "message", "功能尚未實現");
    }
}