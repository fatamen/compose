package tw.com.ispan.eeit.controller.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import tw.com.ispan.eeit.model.entity.reservation.TimeSlot;
import tw.com.ispan.eeit.service.reservation.BookingAvailabilityService;
import tw.com.ispan.eeit.service.reservation.BookingAvailabilityResult;
import tw.com.ispan.eeit.model.dto.reservation.ReservationDTO;
import tw.com.ispan.eeit.model.enums.ReservationStatus;
import tw.com.ispan.eeit.service.reservation.ReservationService;

@RestController
@RequestMapping("/api/booking")
public class BookingAvailabilityController {

    @Autowired
    private BookingAvailabilityService bookingAvailabilityService;

    @Autowired
    private ReservationService reservationService;

    /**
     * 檢查特定時間是否可預約
     */
    @GetMapping("/check")
    public ResponseEntity<BookingAvailabilityResult> checkAvailability(
            @RequestParam Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam Integer guests,
            @RequestParam(defaultValue = "120") Integer duration) {

        BookingAvailabilityResult result = bookingAvailabilityService
                .checkBookingAvailability(storeId, date, time, guests, duration);

        return ResponseEntity.ok(result);
    }

    /**
     * 批量檢查多個時間的可用性
     */
    @GetMapping("/batch-check")
    public ResponseEntity<List<BookingAvailabilityResult>> batchCheckAvailability(
            @RequestParam Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam List<String> times,
            @RequestParam Integer guests,
            @RequestParam(defaultValue = "120") Integer duration) {

        List<BookingAvailabilityResult> results = times.stream()
                .map(timeStr -> {
                    try {
                        LocalTime time = LocalTime.parse(timeStr);
                        return bookingAvailabilityService
                                .checkBookingAvailability(storeId, date, time, guests, duration);
                    } catch (Exception e) {
                        return BookingAvailabilityResult.systemError("時間格式錯誤: " + timeStr,
                                storeId, date, null, guests);
                    }
                })
                .toList();

        return ResponseEntity.ok(results);
    }

    /**
     * 取得餐廳在指定日期的可用時段 - 返回簡化的時段資料
     */
    @GetMapping("/slots/{storeId}")
    public ResponseEntity<List<Map<String, Object>>> getAvailableTimeSlots(
            @PathVariable Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer guests) {

        List<TimeSlot> availableSlots = bookingAvailabilityService
                .getAvailableTimeSlots(storeId, date, guests);

        // 轉換為簡化的DTO，只包含必要的時段資訊
        List<Map<String, Object>> simplifiedSlots = availableSlots.stream()
                .map(slot -> {
                    Map<String, Object> slotMap = new java.util.HashMap<>();
                    slotMap.put("id", slot.getId());
                    slotMap.put("storeId", storeId);
                    slotMap.put("day", slot.getDay());
                    slotMap.put("startTime", slot.getStartTime());
                    slotMap.put("endTime", slot.getEndTime());
                    slotMap.put("isActive", slot.getIsActive());
                    return slotMap;
                })
                .toList();

        return ResponseEntity.ok(simplifiedSlots);
    }

    /**
     * 取得餐廳在指定日期的所有時段 - 返回簡化的時段資料
     */
    @GetMapping("/all-slots/{storeId}")
    public ResponseEntity<List<Map<String, Object>>> getAllTimeSlots(
            @PathVariable Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // 使用 service 層來獲取該餐廳在指定日期的所有啟用時段
        List<TimeSlot> timeSlots = bookingAvailabilityService.getTimeSlotsForDate(storeId, date);

        // 轉換為簡化的DTO，只包含必要的時段資訊
        List<Map<String, Object>> simplifiedSlots = timeSlots.stream()
                .map(slot -> {
                    Map<String, Object> slotMap = new java.util.HashMap<>();
                    slotMap.put("id", slot.getId());
                    slotMap.put("storeId", storeId);
                    slotMap.put("day", slot.getDay());
                    slotMap.put("startTime", slot.getStartTime());
                    slotMap.put("endTime", slot.getEndTime());
                    slotMap.put("isActive", slot.getIsActive());
                    return slotMap;
                })
                .toList();

        return ResponseEntity.ok(simplifiedSlots);
    }

    /**
     * 取得餐廳在指定日期的已預訂時段 - 返回簡化的時段資料
     */
    @GetMapping("/booked-slots/{storeId}")
    public ResponseEntity<List<Map<String, Object>>> getBookedTimeSlots(
            @PathVariable Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // 使用 service 層來獲取已預訂的預約
        List<ReservationDTO> bookedReservations = bookingAvailabilityService.getBookedReservations(storeId, date);

        // 轉換為簡化的DTO，只包含必要的時段資訊
        List<Map<String, Object>> bookedSlots = bookedReservations.stream()
                .filter(reservation -> {
                    // 只返回有效狀態的預約（CONFIRMED, PENDING）
                    return reservation.status() == ReservationStatus.CONFIRMED ||
                            reservation.status() == ReservationStatus.PENDING;
                })
                .map(reservation -> {
                    Map<String, Object> slotMap = new java.util.HashMap<>();
                    slotMap.put("id", reservation.id());
                    slotMap.put("storeId", storeId);
                    slotMap.put("date", reservation.reservedDate());
                    slotMap.put("startTime", reservation.reservedTime());
                    slotMap.put("guests", reservation.guests());
                    slotMap.put("status", reservation.status());
                    return slotMap;
                })
                .toList();

        return ResponseEntity.ok(bookedSlots);
    }

    /**
     * 取得餐廳的日曆元數據 - 用於前端日期選擇器控制
     * GET /api/booking/calendar-metadata/{storeId}
     */
    @GetMapping("/calendar-metadata/{storeId}")
    public ResponseEntity<Map<String, Object>> getCalendarMetadata(@PathVariable Integer storeId) {
        try {
            Map<String, Object> metadata = bookingAvailabilityService.getCalendarMetadata(storeId);
            return ResponseEntity.ok(metadata);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "獲取日曆元數據失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 手動生成餐廳時段資料
     * POST /api/booking/generate-timeslots/{storeId}
     */
    @PostMapping("/generate-timeslots/{storeId}")
    public ResponseEntity<Map<String, Object>> generateTimeSlots(
            @PathVariable Integer storeId,
            @RequestParam(defaultValue = "30") Integer days) {
        try {
            // 使用 ReservationService 生成時段
            reservationService.generateTimeSlotsForRestaurant(storeId, days);

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("success", true);
            response.put("message", "成功生成餐廳 " + storeId + " 的 " + days + " 天時段資料");
            response.put("storeId", storeId);
            response.put("days", days);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", true);
            errorResponse.put("message", "生成時段失敗: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}