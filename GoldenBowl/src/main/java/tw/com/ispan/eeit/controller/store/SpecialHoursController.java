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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.dto.store.SpecialHoursDTO;
import tw.com.ispan.eeit.model.entity.store.SpecialHoursBean;
import tw.com.ispan.eeit.service.store.SpecialHoursService;
import tw.com.ispan.eeit.service.store.SpecialHoursService.SpecialHoursSummary;

@RestController
@RequestMapping("/api/stores/{storeId}/special")
public class SpecialHoursController {

    @Autowired
    private SpecialHoursService specialHoursService;
    
    //取得店家所有的營業時間
    @GetMapping("/all")
    public ResponseEntity<List<SpecialHoursDTO>> getAllSpecialHours(@PathVariable Integer storeId) {

	    try {
	
	    // 假設 Service 層有這樣一個方法
	
	     List<SpecialHoursDTO> specialHours = specialHoursService.getSpecialHoursByStoreId(storeId);
	
	    // 這裡暫時用 getSpecialHoursByStoreIdAndDate 模擬，但需要日期參數
	
	    // 考慮實現一個查詢所有或未來特殊時間的方法
	
	   	
	     return ResponseEntity.ok(specialHours);
	
	    } catch (IllegalArgumentException e) {
	
	    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	
	    } catch (Exception e) {
	
	    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
	
	    }

    }
    
    
    
    @DeleteMapping("/{dayId}")
    public ResponseEntity<Void> deleteById(@PathVariable Integer dayId) {
    	
    	 try {
             specialHoursService.deleteById(dayId);
             // 成功刪除後，通常返回 204 No Content (表示請求成功但沒有內容可返回)
             return ResponseEntity.noContent().build();
         } catch (IllegalArgumentException e) {
             // 例如，如果 ID 不存在或格式不正確，可以返回 400 Bad Request
             return ResponseEntity.badRequest().build();
         } catch (Exception e) {
             // 捕獲其他所有異常，返回 500 Internal Server Error
             // 建議在這裡記錄日誌 (e.g., logger.error("Failed to delete special hour with ID: " + dayId, e);)
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
         }
    }
    
    
    //-----------------------------------------------------------------------

    /**
     * 設定特殊休假日
     * 
     * 
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
    
    //儲存所有
    @PutMapping("/saveAll") // 使用 PUT，表示對某個集合的替換或更新
    public ResponseEntity<List<SpecialHoursDTO>> saveAllSpecialHours(
            @PathVariable Integer storeId,
            @RequestBody List<SpecialHoursDTO> specialHoursDTOs) {
        try {
            // 驗證傳入的 storeId 與 DTO 中的 storeId 是否一致 (可選，但建議)
            boolean allMatchStoreId = specialHoursDTOs.stream()
                    .allMatch(dto -> storeId.equals(dto.getStoreId()));
            if (!allMatchStoreId) {
                return ResponseEntity.badRequest().body(null); // 或返回錯誤訊息
            }

            List<SpecialHoursDTO> savedHours = specialHoursService.saveAll(specialHoursDTOs);
            return ResponseEntity.ok(savedHours);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            // 記錄日誌
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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
