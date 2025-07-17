package tw.com.ispan.eeit.controller.store;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.entity.store.SpecialHoursBean;
import tw.com.ispan.eeit.service.store.SpecialHoursService;
import tw.com.ispan.eeit.service.store.SpecialHoursService.SpecialHoursSummary;

@RestController
@RequestMapping("/api/stores/{storeId}/special")
public class SpecialHoursController {

    @Autowired
    private SpecialHoursService specialHoursService;

    /**
     * 設定特殊休假日
     */
    @PostMapping("/holiday")
    public ResponseEntity<SpecialHoursBean> setSpecialHoliday(
            @PathVariable Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "特殊休假日") String reason) {

        try {
            SpecialHoursBean result = specialHoursService.setSpecialHoliday(storeId, date, reason);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 設定特殊營業時間
     */
    @PostMapping("/business")
    public ResponseEntity<SpecialHoursBean> setSpecialBusinessHours(
            @PathVariable Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime openTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime closeTime,
            @RequestParam(defaultValue = "特殊營業時間") String reason) {

        try {
            SpecialHoursBean result = specialHoursService.setSpecialBusinessHours(
                    storeId, date, openTime, closeTime, reason);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 批量設定特殊休假日
     */
    @PostMapping("/holidays")
    public ResponseEntity<List<SpecialHoursBean>> setMultipleHolidays(
            @PathVariable Integer storeId,
            @RequestParam List<String> dates,
            @RequestParam(defaultValue = "特殊休假日") String reason) {

        try {
            List<LocalDate> localDates = dates.stream()
                    .map(LocalDate::parse)
                    .toList();
            List<SpecialHoursBean> results = specialHoursService.setMultipleHolidays(storeId, localDates, reason);
            return ResponseEntity.status(HttpStatus.CREATED).body(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 查詢餐廳所有特殊設定
     */
    @GetMapping
    public ResponseEntity<List<SpecialHoursBean>> getStoreSpecialHours(@PathVariable Integer storeId) {
        List<SpecialHoursBean> specialHours = specialHoursService.getStoreSpecialHours(storeId);
        return ResponseEntity.ok(specialHours);
    }

    /**
     * 查詢日期範圍內的特殊設定
     */
    @GetMapping("/range")
    public ResponseEntity<List<SpecialHoursBean>> getSpecialHoursByDateRange(
            @PathVariable Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<SpecialHoursBean> specialHours = specialHoursService
                .getSpecialHoursByDateRange(storeId, startDate, endDate);
        return ResponseEntity.ok(specialHours);
    }

    /**
     * 查詢特定日期的特殊設定
     */
    @GetMapping("/{date}")
    public ResponseEntity<SpecialHoursBean> getSpecialHoursByDate(
            @PathVariable Integer storeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Optional<SpecialHoursBean> specialHours = specialHoursService.getSpecialHoursByDate(storeId, date);
        return specialHours.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 取消特殊設定
     */
    @DeleteMapping("/{date}")
    public ResponseEntity<String> cancelSpecialSetting(
            @PathVariable Integer storeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        boolean success = specialHoursService.cancelSpecialSetting(storeId, date);
        if (success) {
            return ResponseEntity.ok("成功取消 " + date + " 的特殊設定");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 檢查是否為特殊休假日
     */
    @GetMapping("/holiday/{date}")
    public ResponseEntity<Boolean> isSpecialHoliday(
            @PathVariable Integer storeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        boolean isHoliday = specialHoursService.isSpecialHoliday(storeId, date);
        return ResponseEntity.ok(isHoliday);
    }

    /**
     * 檢查是否有特殊營業時間
     */
    @GetMapping("/business/{date}")
    public ResponseEntity<Boolean> hasSpecialBusinessHours(
            @PathVariable Integer storeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        boolean hasSpecial = specialHoursService.hasSpecialBusinessHours(storeId, date);
        return ResponseEntity.ok(hasSpecial);
    }

    /**
     * 取得特殊設定摘要
     */
    @GetMapping("/summary")
    public ResponseEntity<SpecialHoursSummary> getSpecialHoursSummary(
            @PathVariable Integer storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        SpecialHoursSummary summary = specialHoursService.getSpecialHoursSummary(storeId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}
