package tw.com.ispan.eeit.controller.store;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import tw.com.ispan.eeit.model.dto.store.OpenHourDTO;
import tw.com.ispan.eeit.model.dto.store.StoreOpenStatusDTO;
import tw.com.ispan.eeit.model.entity.store.OpenHourBean;
import tw.com.ispan.eeit.service.store.OpenHourService;

@RestController
@RequestMapping("/api/stores/{storeId}/hours")
public class OpenHourController {

    @Autowired
    private OpenHourService openHourService;

    /**
     * 取得餐廳的營業時間設定 (簡化版本，只返回核心資料)
     */
    @GetMapping
    public ResponseEntity<List<OpenHourDTO>> getOpenHours(@PathVariable Integer storeId) {
        try {
            List<OpenHourDTO> openHours = openHourService.getOpenHoursDTOByStore(storeId);
            return ResponseEntity.ok(openHours);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 取得餐廳的完整營業時間設定（包括公休日）
     */
    @GetMapping("/complete")
    public ResponseEntity<List<OpenHourDTO>> getCompleteOpenHours(@PathVariable Integer storeId) {
        try {
            List<OpenHourDTO> openHours = openHourService.getCompleteOpenHoursByStore(storeId);
            return ResponseEntity.ok(openHours);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
        
    
    
    /**
     * 取得餐廳的營業時間設定 (完整版本，包含所有關聯資料)
     */
    @GetMapping("/full")
    public ResponseEntity<List<OpenHourBean>> getOpenHoursFull(@PathVariable Integer storeId) {
        try {
            List<OpenHourBean> openHours = openHourService.getOpenHoursByStore(storeId);
            return ResponseEntity.ok(openHours);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 取得餐廳特定星期的營業時間設定 (簡化版本)
     */
    @GetMapping("/{day}")
    public ResponseEntity<OpenHourDTO> getOpenHourByDay(
            @PathVariable Integer storeId,
            @PathVariable DayOfWeek day) {
        try {
            OpenHourBean openHour = openHourService.getOpenHourByStoreAndDay(storeId, day);
            OpenHourDTO openHourDTO = new OpenHourDTO(
                    openHour.getId(),
                    storeId,
                    openHour.getDayOfWeek(),
                    openHour.getOpenTime(),
                    openHour.getCloseTime());
            return ResponseEntity.ok(openHourDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 設定餐廳的營業時間
     */
    @PostMapping
    public ResponseEntity<OpenHourDTO> createOpenHour(
            @PathVariable Integer storeId,
            @RequestParam DayOfWeek day,
            @RequestParam(required = false) String openTime,
            @RequestParam(required = false) String closeTime,
            @RequestParam(defaultValue = "true") boolean isOpen) {
        try {
            OpenHourBean openHour = openHourService.setOpenHour(storeId, day, openTime, closeTime, isOpen);
            OpenHourDTO openHourDTO = new OpenHourDTO(
                    openHour.getId(),
                    storeId,
                    openHour.getDayOfWeek(),
                    openHour.getOpenTime(),
                    openHour.getCloseTime());
            return ResponseEntity.status(HttpStatus.CREATED).body(openHourDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 更新營業時間設定
     */
    @PutMapping("/{openHourId}")
    public ResponseEntity<OpenHourDTO> updateOpenHour(
            @PathVariable Integer storeId,
            @PathVariable Integer openHourId,
            @RequestParam(required = false) String openTime,
            @RequestParam(required = false) String closeTime,
            @RequestParam(required = false) Boolean isOpen) {
        try {
            OpenHourBean openHour = openHourService.updateOpenHour(openHourId, openTime, closeTime, isOpen);
            OpenHourDTO openHourDTO = new OpenHourDTO(
                    openHour.getId(),
                    storeId,
                    openHour.getDayOfWeek(),
                    openHour.getOpenTime(),
                    openHour.getCloseTime());
            return ResponseEntity.ok(openHourDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 刪除營業時間設定
     */
    @DeleteMapping("/{openHourId}")
    public ResponseEntity<Void> deleteOpenHour(
            @PathVariable Integer storeId,
            @PathVariable Integer openHourId) {
        try {
            openHourService.deleteOpenHour(openHourId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 設定預設營業時間
     */
    @PostMapping("/default")
    public ResponseEntity<String> setDefaultOpenHours(@PathVariable Integer storeId) {
        try {
            openHourService.setDefaultOpenHours(storeId);
            return ResponseEntity.ok("成功設定預設營業時間");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("設定預設營業時間時發生錯誤: " + e.getMessage());
        }
    }

    /**
     * 檢查餐廳在指定時間是否營業
     */
    @GetMapping("/check")
    public ResponseEntity<StoreOpenStatusDTO> isStoreOpen(
            @PathVariable Integer storeId,
            @RequestParam DayOfWeek day,
            @RequestParam String time) {
        try {
            java.time.LocalTime localTime = java.time.LocalTime.parse(time);
            boolean isOpen = openHourService.isStoreOpen(storeId, day, localTime);
            StoreOpenStatusDTO statusDTO = new StoreOpenStatusDTO(storeId, isOpen, day, localTime);
            return ResponseEntity.ok(statusDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 檢查餐廳在指定日期和時間是否營業
     */
    @GetMapping("/check/{date}")
    public ResponseEntity<StoreOpenStatusDTO> isStoreOpenByDate(
            @PathVariable Integer storeId,
            @PathVariable String date,
            @RequestParam String time) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            java.time.LocalTime localTime = java.time.LocalTime.parse(time);
            boolean isOpen = openHourService.isStoreOpen(storeId, localDate, localTime);
            StoreOpenStatusDTO statusDTO = new StoreOpenStatusDTO(storeId, isOpen, localDate, localTime);
            return ResponseEntity.ok(statusDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 檢查某天是否為公休日
     */
    @GetMapping("/closed/{day}")
    public ResponseEntity<Boolean> isClosedDay(
            @PathVariable Integer storeId,
            @PathVariable DayOfWeek day) {
        try {
            boolean isClosed = openHourService.isClosedDay(storeId, day);
            return ResponseEntity.ok(isClosed);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * 檢查指定日期是否為公休日
     */
    @GetMapping("/closed/date/{date}")
    public ResponseEntity<Boolean> isClosedDayByDate(
            @PathVariable Integer storeId,
            @PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            boolean isClosed = openHourService.isClosedDay(storeId, localDate);
            return ResponseEntity.ok(isClosed);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ========== 特殊營業時間相關 API ==========

    @PutMapping("/saveAll") // 使用 PUT 來表示整個資源的更新或替換
    public ResponseEntity<List<OpenHourBean>> saveAll(
            @PathVariable Integer storeId,
            @RequestBody List<OpenHourDTO> dtoList) {

        // 1. 數據一致性檢查：確保所有 DTO 中的 storeId 與 PathVariable 中的 storeId 一致
        for (OpenHourDTO dto : dtoList) {
            // 如果 DTO 的 storeId 不存在或者與路徑中的 storeId 不匹配，則返回錯誤
            if (dto.getStoreId() == null || !dto.getStoreId().equals(storeId)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 返回 400 Bad Request
            }
        }

        try {
            // 2. 調用 Service 層的方法來處理批量保存邏輯
            List<OpenHourBean> savedHours = openHourService.saveAllOpenHours(dtoList);

            // 3. 返回成功響應和保存後的數據
            return new ResponseEntity<>(savedHours, HttpStatus.OK); // 返回 200 OK

        } catch (ResourceNotFoundException e) {
            // 4. 處理店家未找到的異常
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // 返回 404 Not Found
        } catch (IllegalArgumentException e) {
            // 5. 處理業務邏輯中的無效參數異常 (例如 DTO 列表為空)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // 返回 400 Bad Request
        } catch (Exception e) {
            // 6. 捕獲其他未預期的異常
            // 實際應用中應該更詳細地記錄錯誤日誌
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 返回 500 Internal Server Error
        }
    }


}
