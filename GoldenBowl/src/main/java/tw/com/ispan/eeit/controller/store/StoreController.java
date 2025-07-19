package tw.com.ispan.eeit.controller.store;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tw.com.ispan.eeit.model.dto.store.StoreDTO;
import tw.com.ispan.eeit.model.entity.OwnerBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.service.store.StoreService;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @GetMapping
    public ResponseEntity<List<StoreDTO>> getAllStores(
            @RequestParam(value = "userId", required = false) Integer userId, // 新增 userId 參數
            @RequestParam(value = "search", required = false) String search) {

        List<StoreDTO> storeDTOs;
        if (search != null && !search.trim().isEmpty()) {
            // 調用帶有 userId 參數的 searchStores 方法
            storeDTOs = storeService.searchStores(userId, search.trim());
        } else {
            // 調用帶有 userId 參數的 getAllStores 方法
            storeDTOs = storeService.getAllStores(userId);
        }
        return new ResponseEntity<>(storeDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> getStoreById(
            @PathVariable Integer id,
            @RequestParam(value = "userId", required = false) Integer userId) { // 新增 userId 參數
        StoreDTO storeDTO = storeService.getStoreById(id, userId); // 傳遞 userId
        if (storeDTO != null) {
            return new ResponseEntity<>(storeDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<StoreDTO> createStore(@RequestBody StoreDTO storeDto) {
        // 將 StoreDTO 轉換回 StoreBean 以便 service 層處理
        StoreBean storeToCreate = new StoreBean();
        // 使用 BeanUtils.copyProperties 可以複製相同的屬性名稱
        // 但對於複雜的嵌套 DTO 或關聯，你需要手動轉換或使用 MapStruct 等映射工具
        storeToCreate.setName(storeDto.getName());
        storeToCreate.setPhoto(storeDto.getPhoto());
        storeToCreate.setScore(storeDto.getScore());
        storeToCreate.setIsOpen(storeDto.getIsOpen());
        // 對於 categoryNames, comments, foods，如果你在創建時需要設置這些關聯，
        // 你需要根據 DTO 中的資訊從資料庫查詢對應的 Bean，並設置到 storeToCreate 中。
        // 例如：
        // if (storeDto.getCategoryNames() != null &&
        // !storeDto.getCategoryNames().isEmpty()) {
        // List<CategoryBean> categories =
        // categoryRepository.findByNameIn(storeDto.getCategoryNames());
        // storeToCreate.setCategories(categories);
        // }
        // 否則，在創建或更新時，這些集合通常不會直接從 JSON 傳入。
        // 你需要根據業務邏輯決定如何處理這些關聯。
        // 暫時假設這些集合在創建時不會直接傳入，或者由 Service 層負責處理。

        StoreBean createdStoreBean = storeService.createStore(storeToCreate);

        // 將創建後的 StoreBean 轉換回 StoreDTO 返回給客戶端
        StoreDTO createdStoreDTO = new StoreDTO(createdStoreBean);
        return new ResponseEntity<>(createdStoreDTO, HttpStatus.CREATED);
    }

    /**
     * 更新商店資訊
     */
    @PutMapping("/{id}")
    public ResponseEntity<StoreDTO> updateStore(@PathVariable Integer id, @RequestBody StoreDTO storeDetailsDto) {
        // 將 StoreDTO 轉換回 StoreBean 以便 service 層處理更新
        StoreBean storeToUpdate = new StoreBean();
        storeToUpdate.setName(storeDetailsDto.getName());
        storeToUpdate.setPhoto(storeDetailsDto.getPhoto());
        storeToUpdate.setScore(storeDetailsDto.getScore());
        storeToUpdate.setIsOpen(storeDetailsDto.getIsOpen());
        storeToUpdate.setAddress(storeDetailsDto.getAddress());
        storeToUpdate.setStoreIntro(storeDetailsDto.getStoreIntro());
        storeToUpdate.setLat(storeDetailsDto.getLat());
        storeToUpdate.setLng(storeDetailsDto.getLng());

        OwnerBean owner = new OwnerBean();
        owner.setPhone(storeDetailsDto.getPhone());
        owner.setEmail(storeDetailsDto.getEmail());
        storeToUpdate.setOwner(owner);
        // 同樣，對於關聯集合的更新，你需要根據業務邏輯處理
        // 例如，如果前端傳入新的 categoryNames，你可能需要在 service 層更新 StoreBean 的 categories 集合
        // storeDetailsDto.getCategoryNames(); // <-- 這裡會包含前端傳來的類別名稱

        StoreBean updatedStoreBean = storeService.updateStore(id, storeToUpdate);

        if (updatedStoreBean != null) {
            // 將更新後的 StoreBean 轉換回 StoreDTO 返回
            StoreDTO updatedStoreDTO = new StoreDTO(updatedStoreBean);
            return new ResponseEntity<>(updatedStoreDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteStore(@PathVariable Integer id) {
        if (storeService.deleteStore(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * 商店註冊
     */
    @PostMapping("/registerInfo")
    public Map<String, Object> register(
            @RequestBody Map<String, Object> map) {
        // 驗證必填
        Integer ownerId = (map.get("ownerId") != null) ? Integer.parseInt(map.get("ownerId").toString()) : null;
        String name = (String) map.get("name");
        String storeCategory = (String) map.get("storeCategory");
        String intro = (String) map.get("storeIntro");
        String photo = (String) map.get("photo"); // 這裡收到的是分號分隔或單一 firebase 下載網址
        // 你也可以改成 List<String> photoUrls = (List<String>) map.get("photoUrls"); 如果前端直接送
        // array

        if (ownerId == null) {
            return Map.of("success", false, "message", "ownerId 不可為空");
        }
        if (name == null || name.isBlank()) {
            return Map.of("success", false, "message", "請輸入店名");
        }
        if (storeCategory == null || storeCategory.isBlank()) {
            return Map.of("success", false, "message", "請選擇餐廳類型");
        }

        // 呼叫 Service
        StoreBean store = storeService.registerStore(ownerId, name, storeCategory, intro, photo);
        if (store == null || store.getId() == null) {
            return Map.of("success", false, "message", "註冊失敗");
        }
        return Map.of("success", true, "storeId", store.getId());
    }

    /**
     * 更新商店地址
     */
    @PostMapping("/updateAddress")
    public Map<String, Object> updateAddress(@RequestBody Map<String, Object> map) {
        Object storeIdObj = map.get("storeId");
        if (storeIdObj == null) {
            return Map.of("success", false, "message", "storeId 不可為空");
        }
        Integer storeId;
        try {
            storeId = Integer.parseInt(storeIdObj.toString());
        } catch (Exception e) {
            return Map.of("success", false, "message", "storeId 格式錯誤");
        }
        String address = (String) map.get("address");

        Double lat = null, lng = null;
        try {
            if (map.get("lat") != null && !((String) map.get("lat")).isBlank())
                lat = Double.parseDouble((String) map.get("lat"));
            if (map.get("lng") != null && !((String) map.get("lng")).isBlank())
                lng = Double.parseDouble((String) map.get("lng"));
        } catch (Exception e) {
            return Map.of("success", false, "message", "經緯度格式錯誤");
        }

        boolean ok = storeService.updateAddress(storeId, address, lat, lng);
        return Map.of("success", ok);
    }

    // ========== 以下為支援多店主的新端點 ==========

    /**
     * 獲取Owner的主要Store（最新建立的）- 保持向下兼容
     */
    @GetMapping("/profile")
    public ResponseEntity<StoreDTO> getMyStoreProfile(@RequestParam Integer ownerId) {
        // ownerId 可以從 JWT 或 session 取得，不要讓前端直接傳（安全性問題）
        // 這邊為示範，暫時用 query 參數帶
        if (ownerId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<StoreBean> storeOptional = storeService.getMainStoreByOwnerId(ownerId);
        if (storeOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        StoreDTO storeDTO = new StoreDTO(storeOptional.get());
        return new ResponseEntity<>(storeDTO, HttpStatus.OK);
    }

    /**
     * 獲取Owner的所有Store
     */
    @GetMapping("/profile/all")
    public ResponseEntity<List<StoreDTO>> getAllMyStores(@RequestParam Integer ownerId) {
        if (ownerId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<StoreBean> stores = storeService.getStoresByOwnerId(ownerId);
        if (stores.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        List<StoreDTO> storeDTOs = stores.stream()
                .map(StoreDTO::new)
                .collect(Collectors.toList());

        return new ResponseEntity<>(storeDTOs, HttpStatus.OK);
    }

    /**
     * 獲取Owner的第一個Store（最早建立的）
     */
    @GetMapping("/profile/first")
    public ResponseEntity<StoreDTO> getMyFirstStore(@RequestParam Integer ownerId) {
        if (ownerId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<StoreBean> storeOptional = storeService.getFirstStoreByOwnerId(ownerId);
        if (storeOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        StoreDTO storeDTO = new StoreDTO(storeOptional.get());
        return new ResponseEntity<>(storeDTO, HttpStatus.OK);
    }

    /**
     * 獲取Owner的Store摘要資訊
     */
    @GetMapping("/profile/summary")
    public ResponseEntity<Map<String, Object>> getMyStoresSummary(@RequestParam Integer ownerId) {
        if (ownerId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<StoreBean> stores = storeService.getStoresByOwnerId(ownerId);
        if (stores.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Map<String, Object> summary = Map.of(
                "ownerId", ownerId,
                "totalStores", stores.size(),
                "stores", stores.stream()
                        .map(store -> Map.of(
                                "id", store.getId(),
                                "name", store.getName(),
                                "isOpen", store.getIsOpen() != null ? store.getIsOpen() : false,
                                "score", store.getScore() != null ? store.getScore() : 0.0,
                                "createdTime", store.getCreatedTime()))
                        .collect(Collectors.toList()));

        return new ResponseEntity<>(summary, HttpStatus.OK);
    }

    /**
     * 根據StoreId和OwnerId獲取特定Store（安全檢查）
     */
    @GetMapping("/profile/store/{storeId}")
    public ResponseEntity<StoreDTO> getSpecificStore(
            @PathVariable Integer storeId,
            @RequestParam Integer ownerId) {

        if (ownerId == null || storeId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // 檢查Store是否屬於該Owner
        if (!storeService.isStoreOwnedByOwner(storeId, ownerId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        StoreDTO storeDTO = storeService.getStoreById(storeId, ownerId);
        if (storeDTO == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(storeDTO, HttpStatus.OK);
    }

    /**
     * 獲取Owner的Store數量
     */
    @GetMapping("/profile/count")
    public ResponseEntity<Map<String, Object>> getMyStoreCount(@RequestParam Integer ownerId) {
        if (ownerId == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        long count = storeService.getStoreCountByOwnerId(ownerId);
        Map<String, Object> result = Map.of(
                "ownerId", ownerId,
                "storeCount", count);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 新增：收藏餐廳
    @PostMapping("/{storeId}/favorite/{userId}")
    public ResponseEntity<Map<String, Boolean>> addFavoriteStore(
            @PathVariable Integer storeId,
            @PathVariable Integer userId) {
        boolean success = storeService.addStoreToFavorites(userId, storeId);
        return new ResponseEntity<>(Map.of("success", success), success ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    // 新增：取消收藏餐廳
    @DeleteMapping("/{storeId}/favorite/{userId}")
    public ResponseEntity<Map<String, Boolean>> removeFavoriteStore(
            @PathVariable Integer storeId,
            @PathVariable Integer userId) {
        boolean success = storeService.removeStoreFromFavorites(userId, storeId);
        return new ResponseEntity<>(Map.of("success", success), success ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}