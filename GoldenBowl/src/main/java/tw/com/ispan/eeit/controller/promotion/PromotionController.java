package tw.com.ispan.eeit.controller.promotion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.dto.promotion.PromotionCreateDTO;
import tw.com.ispan.eeit.model.dto.promotion.PromotionDTO;
import tw.com.ispan.eeit.model.dto.promotion.PromotionRequestDTO;
import tw.com.ispan.eeit.model.dto.promotion.PromotionUpdateDTO;
import tw.com.ispan.eeit.model.entity.promotion.PromotionBean;
import tw.com.ispan.eeit.service.promotion.PromotionService;

@RestController
@RequestMapping("/promotions")
@CrossOrigin
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    // 查全部（後台管理用，回傳 Entity）
//    @GetMapping
//    public List<PromotionDTO> findAll() {
//        return promotionService.findAll().stream().map(promotionService::toDTO).toList(); // ✅
//    }

    @GetMapping
    public List<PromotionDTO> findAll() {
        List<PromotionDTO> result = promotionService.findAll().stream().map(promotionService::toDTO).toList();
        System.out.println("⭐ 後台撈到幾筆優惠券：" + result.size());
        return result;
    }

    //查可用優惠券（POST 版）
    @PostMapping("/available")
    public List<PromotionDTO> getAvailablePromotionsPost(@RequestBody PromotionRequestDTO request) {
        return promotionService.getAvailablePromotions(
            request.getUserId(),
            request.getStoreId(),
            request.getAmount(),
            request.getTagIds() != null ? request.getTagIds() : List.of(),
            request.getTagSpendMap()
        );
    }
    
    // 查單筆（回傳 DTO）
    @GetMapping("/{id}")
    public PromotionDTO findById(@PathVariable Integer id) {
        return promotionService.findById(id);
    }

    // 查某方案的優惠券
    @GetMapping("/plan/{planId}")
    public List<PromotionDTO> findByPlanId(@PathVariable Integer planId) {
        return promotionService.findByPlanId(planId).stream().map(promotionService::toDTO).toList(); // ✅
    }

    // 查某商店的優惠券
    @GetMapping("/store/{storeId}")
    public List<PromotionDTO> findByStoreId(@PathVariable Integer storeId) {
        return promotionService.findByStoreId(storeId).stream().map(promotionService::toDTO).toList(); // ✅
    }

    // 新增優惠券
    @PostMapping("")
    public PromotionBean create(@RequestBody PromotionCreateDTO dto) {
        return promotionService.createFromDTO(dto);
    }

    // 修改優惠券
//    @PutMapping("/{id}")
//    public PromotionBean update(@PathVariable Integer id, @RequestBody PromotionUpdateDTO dto) {
//        return promotionService.updateFromDTO(id, dto);
//    }
    @PutMapping("/{id}")
    public ResponseEntity<PromotionDTO> updatePromotion(
            @PathVariable Integer id,
            @RequestBody PromotionUpdateDTO dto) {
        PromotionBean promotion = promotionService.updateFromDTO(id, dto);
        return ResponseEntity.ok(promotionService.toDTO(promotion)); // ✅ 改這裡！
    }

    // 刪除優惠券
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        promotionService.deleteById(id);
    }

    // ✅ 查詢目前所有有效未使用的優惠券（給優惠券清單頁面用）
    @GetMapping("/all-available")
    public List<PromotionDTO> findAllAvailable() {
        return promotionService.findAllAvailable();
    }

    // ✅ 回傳 DTO，查可用優惠券，條件：userId + storeId + 金額（可用於結帳）+ tagId
    @GetMapping("/available")
    public List<PromotionDTO> getAvailablePromotions(
            @RequestParam Integer userId,
            @RequestParam Integer storeId,
            @RequestParam Integer amount,
            @RequestParam(required = false) List<Integer> tagIds,
            @RequestParam(required = false) String tagSpendMap // JSON 字串
    ) {
    	// 如果前端沒傳 tagIds，預設為空陣列（代表沒有限定 tag）
        if (tagIds == null) {
            tagIds = List.of(); // Java 9+，可用 Collections.emptyList() 取代
        }
        return promotionService.getAvailablePromotions(userId, storeId, amount, tagIds, tagSpendMap);
    }

    // ✅ 根據分類 type 回傳優惠券列表，前端 tab 分類用
    // 範例：/promotions/by-type?type=global
    @GetMapping("/by-type")
    public List<PromotionDTO> findByType(@RequestParam String type) {
        return promotionService.findByType(type);
    }
    // 修改優惠券 open / close
    @PutMapping("/promotions/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        try {
            promotionService.updateStatus(id, status);
            return ResponseEntity.ok("狀態已更新為：" + status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
 // 查某使用者用過的優惠券（根據訂單紀錄）
    @GetMapping("/used")
    public List<PromotionDTO> getUsedPromotions(@RequestParam Integer userId) {
        return promotionService.findUsedPromotions(userId);
    }

}
