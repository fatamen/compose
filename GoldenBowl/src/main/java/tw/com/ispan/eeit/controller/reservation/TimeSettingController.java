package tw.com.ispan.eeit.controller.reservation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.dto.store.TimeSettingUpdateRequest;
import tw.com.ispan.eeit.model.entity.reservation.TimeSettingBean;
import tw.com.ispan.eeit.service.reservation.TimeSettingService;

@RestController
@RequestMapping("/api/time-setting")
public class TimeSettingController {

    @Autowired
    private TimeSettingService service;

    @GetMapping("/{storeId}")
    public ResponseEntity<TimeSettingBean> get(@PathVariable Integer storeId) {
        return ResponseEntity.ok(service.getByStoreId(storeId));
    }

    @PutMapping("/{storeId}")
    public ResponseEntity<TimeSettingBean> update(
            @PathVariable Integer storeId,
            @RequestBody TimeSettingUpdateRequest request) {
        TimeSettingBean updated = service.update(
                storeId, request.getInterval(), request.getMealTime());
        return ResponseEntity.ok(updated);
    }
}