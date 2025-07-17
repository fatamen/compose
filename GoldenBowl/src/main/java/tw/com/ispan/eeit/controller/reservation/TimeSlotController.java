package tw.com.ispan.eeit.controller.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.exception.ResourceNotFoundException;
import tw.com.ispan.eeit.model.entity.reservation.TimeSlot;
import tw.com.ispan.eeit.model.dto.reservation.TimeSlotSimpleDTO;
import tw.com.ispan.eeit.model.dto.reservation.TimeRangeRequest;
import tw.com.ispan.eeit.service.reservation.ReservationService;

@RestController
@RequestMapping("/api/stores/{storeId}/timeslots")
public class TimeSlotController {

    @Autowired
    private ReservationService reservationService;

    /**
     * 取得餐廳的所有可用時段
     */
    @GetMapping
    public ResponseEntity<List<TimeSlotSimpleDTO>> getTimeSlots(@PathVariable Integer storeId) {
        try {
            List<TimeSlotSimpleDTO> timeSlots = reservationService.getAvailableTimeSlotsSimple(storeId);
            return ResponseEntity.ok(timeSlots);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 取得特定日期的可用時段
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<TimeSlotSimpleDTO>> getTimeSlotsByDate(
            @PathVariable Integer storeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<TimeSlotSimpleDTO> timeSlots = reservationService.getAvailableTimeSlotsByDate(storeId, date);
            return ResponseEntity.ok(timeSlots);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 取得日期範圍內的可用時段
     */
    @GetMapping("/range")
    public ResponseEntity<List<TimeSlotSimpleDTO>> getTimeSlotsByDateRange(
            @PathVariable Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<TimeSlotSimpleDTO> timeSlots = reservationService.getTimeSlotsByDateRange(storeId, startDate, endDate);
            return ResponseEntity.ok(timeSlots);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 新增時段
     */
    @PostMapping
    public ResponseEntity<TimeSlotSimpleDTO> addTimeSlot(
            @PathVariable Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        try {
            TimeSlotSimpleDTO timeSlot = reservationService.addTimeSlot(storeId, day, startTime, endTime, isActive);
            return ResponseEntity.status(HttpStatus.CREATED).body(timeSlot);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 取得時段詳情
     */
    @GetMapping("/{timeSlotId}")
    public ResponseEntity<TimeSlotSimpleDTO> getTimeSlotById(
            @PathVariable Integer storeId,
            @PathVariable Integer timeSlotId) {
        try {
            TimeSlotSimpleDTO timeSlot = reservationService.getTimeSlotById(timeSlotId);
            return ResponseEntity.ok(timeSlot);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新時段
     */
    @PutMapping("/{timeSlotId}")
    public ResponseEntity<TimeSlotSimpleDTO> updateTimeSlot(
            @PathVariable Integer storeId,
            @PathVariable Integer timeSlotId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Boolean isActive) {
        try {
            TimeSlotSimpleDTO timeSlot = reservationService.updateTimeSlot(timeSlotId, startTime, endTime, isActive);
            return ResponseEntity.ok(timeSlot);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 刪除時段
     */
    @DeleteMapping("/{timeSlotId}")
    public ResponseEntity<Void> deleteTimeSlot(
            @PathVariable Integer storeId,
            @PathVariable Integer timeSlotId) {
        try {
            reservationService.deleteTimeSlot(timeSlotId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 批量生成時段
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateTimeSlots(
            @PathVariable Integer storeId,
            @RequestParam(defaultValue = "30") int daysAhead) {
        try {
            reservationService.generateTimeSlotsForRestaurant(storeId, daysAhead);
            return ResponseEntity.ok("成功為餐廳 " + storeId + " 生成未來 " + daysAhead + " 天的時段資料");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("生成時段資料時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 根據指定時間段生成時段
     */
    @PostMapping("/generate-custom")
    public ResponseEntity<Map<String, Object>> generateCustomTimeSlots(
            @PathVariable Integer storeId,
            @RequestParam LocalDate date,
            @RequestBody List<TimeRangeRequest> timeRanges) {
        try {
            List<TimeSlotSimpleDTO> generatedSlots = reservationService.generateTimeSlotsForCustomRanges(
                    storeId, date, timeRanges);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "成功生成 " + generatedSlots.size() + " 個時段");
            response.put("generatedSlots", generatedSlots);
            response.put("date", date);

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "生成時段失敗: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 停用特定日期的時段
     */
    @PutMapping("/disable/{date}")
    public ResponseEntity<String> disableTimeSlotsByDate(
            @PathVariable Integer storeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            int disabledCount = reservationService.disableTimeSlotsByDate(storeId, date);
            return ResponseEntity.ok("成功停用 " + disabledCount + " 個時段");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 啟用特定日期的時段
     */
    @PutMapping("/enable/{date}")
    public ResponseEntity<String> enableTimeSlotsByDate(
            @PathVariable Integer storeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            int enabledCount = reservationService.enableTimeSlotsByDate(storeId, date);
            return ResponseEntity.ok("成功啟用 " + enabledCount + " 個時段");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 測試 CONVERT 函數功能
     */
    @GetMapping("/test-convert")
    public ResponseEntity<Map<String, Object>> testConvertFunctions(
            @PathVariable Integer storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        try {
            if (date == null) {
                date = LocalDate.now();
            }

            Map<String, Object> testResults = reservationService.testConvertFunctions(storeId, date);
            return ResponseEntity.ok(testResults);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "測試 CONVERT 函數時發生錯誤: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}