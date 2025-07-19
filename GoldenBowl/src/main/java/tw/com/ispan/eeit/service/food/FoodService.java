package tw.com.ispan.eeit.service.food;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.ispan.eeit.exception.ResourceNotFoundException;
import tw.com.ispan.eeit.model.dto.food.FoodDTO;
import tw.com.ispan.eeit.model.dto.food.FoodRequest;
import tw.com.ispan.eeit.model.entity.food.FoodBean;
import tw.com.ispan.eeit.model.entity.food.FoodClassBean;
import tw.com.ispan.eeit.model.entity.food.FoodClassificationBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.repository.food.FoodClassRepository;
import tw.com.ispan.eeit.repository.food.FoodRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;

@Service
public class FoodService {

    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private FoodClassRepository foodClassRepository;

    // --- Create ---
    @Transactional
    public FoodDTO createFood(FoodRequest request) {
        StoreBean store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到店家，ID: " + request.getStoreId()));

        List<FoodClassBean> foodClasses = foodClassRepository.findAllById(request.getFoodClassIds());
        if (foodClasses.size() != request.getFoodClassIds().size()) {
            throw new ResourceNotFoundException("部分食物分類不存在，請確認 ID 是否正確。");
        }

        FoodBean newFood = new FoodBean();
        newFood.setStore(store);
        newFood.setName(request.getName());
        newFood.setPrice(request.getPrice());
        newFood.setDescription(request.getDescription());
        newFood.setStock(request.getStock());
        newFood.setImgResource(request.getImgResource());

        // 【核心修正：使用新的中間表 Entity】
        newFood.getClassifications().clear(); // 清理（對新增來說是多餘的，但好習慣）
        for (FoodClassBean foodClass : foodClasses) {

            newFood.getClassifications().add(
                    new FoodClassificationBean(newFood, foodClass, store, foodClass.getSort()));
        }

        // newFood.setFoodClasses(foodClasses); // 設定多對多關聯
        newFood.setCreateTime(LocalDateTime.now());
        newFood.setUpdateTime(LocalDateTime.now());
        newFood.setScore(0.0f);

        FoodBean savedFood = foodRepository.save(newFood);
        return convertToDTO(savedFood);
    }

    // --- Read (Single) ---
    public FoodDTO findFoodById(Integer id) {
        FoodBean foodBean = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到食物，ID: " + id));
        return convertToDTO(foodBean);
    }

    // --- Read (List by Store) ---
    public List<FoodDTO> findFoodsByStoreId(Integer storeId) {
        // 可以在這裡加一個檢查，確認店家是否存在
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("找不到店家，ID: " + storeId);
        }
        List<FoodBean> foodBeans = foodRepository.findByStoreId(storeId);
        return foodBeans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // 增加有上架的食物--ted
    public List<FoodDTO> findActiveFoodsByStoreId(Integer storeId) {
        // 可以在這裡加一個檢查，確認店家是否存在
        if (!storeRepository.existsById(storeId)) {
            throw new ResourceNotFoundException("找不到店家，ID: " + storeId);
        }
        List<FoodBean> foodBeans = foodRepository.findActiveFoodsByStoreId(storeId);
        return foodBeans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // --- Update ---
    @Transactional
    public FoodDTO updateFood(Integer id, FoodRequest request) {
        FoodBean existingFood = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("找不到要更新的食物，ID: " + id));

        // 驗證店家 ID 是否匹配 (可選，但更安全)
        if (!existingFood.getStore().getId().equals(request.getStoreId())) {
            throw new IllegalArgumentException("無法變更食物所屬的店家。");
        }

        List<FoodClassBean> foodClasses = foodClassRepository.findAllById(request.getFoodClassIds());
        if (foodClasses.size() != request.getFoodClassIds().size()) {
            throw new ResourceNotFoundException("部分食物分類不存在，請確認 ID 是否正確。");
        }

        existingFood.setName(request.getName());
        existingFood.setPrice(request.getPrice());
        existingFood.setDescription(request.getDescription());
        existingFood.setStock(request.getStock());
        existingFood.setImgResource(request.getImgResource());
        // existingFood.setFoodClasses(foodClasses);

        // 🔥 新增：設定供應狀態
        if (request.getIsActive() != null) {
            existingFood.setIsActive(request.getIsActive());
        }

        existingFood.setUpdateTime(LocalDateTime.now());

        existingFood.getClassifications().clear(); // 清理掉所有舊的關聯
        for (FoodClassBean foodClass : foodClasses) {

            existingFood.getClassifications().add(
                    new FoodClassificationBean(existingFood, foodClass, existingFood.getStore(), foodClass.getSort()));
        }

        existingFood.setUpdateTime(LocalDateTime.now());

        FoodBean updatedFood = foodRepository.save(existingFood);
        return convertToDTO(updatedFood);
    }
    
    /**
     * 更新食物的圖片路徑
     * @param foodId 食物 ID
     * @param imagePath 圖片相對路徑
     * @return 更新後的 FoodDTO
     */
    public FoodDTO updateImagePath(Integer foodId, String imagePath) {
        try {
            FoodBean food = foodRepository.findById(foodId)
                    .orElseThrow(() -> new ResourceNotFoundException("找不到食物，ID: " + foodId));
            
            // 記錄舊的圖片路徑（如果需要刪除舊圖片）
            String oldImagePath = food.getImgResource();
            if (oldImagePath != null && !oldImagePath.equals(imagePath)) {
                System.out.println("📝 食物 ID:" + foodId + " 圖片路徑變更: " + oldImagePath + " → " + imagePath);
            }
            
            // 更新圖片路徑
            food.setImgResource(imagePath);
            food.setUpdateTime(LocalDateTime.now());
            
            // 儲存到資料庫
            FoodBean updatedFood = foodRepository.save(food);
            
            System.out.println("✅ 已更新食物 ID:" + foodId + " 的圖片路徑為: " + imagePath);
            
            // 轉換為 DTO 回傳
            return convertToDTO(updatedFood);
            
        } catch (Exception e) {
            System.err.println("❌ 更新食物圖片路徑失敗: " + e.getMessage());
            throw new RuntimeException("更新圖片路徑失敗: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新圖片路徑（如果需要）
     * @param imageUpdates Map<foodId, imagePath>
     */
    public void updateMultipleImagePaths(Map<Integer, String> imageUpdates) {
        for (Map.Entry<Integer, String> entry : imageUpdates.entrySet()) {
            updateImagePath(entry.getKey(), entry.getValue());
        }
    }

    // --- Delete ---
    @Transactional
    public void deleteFood(Integer id) {
        if (!foodRepository.existsById(id)) {
            throw new ResourceNotFoundException("找不到要刪除的食物，ID: " + id);
        }
        foodRepository.deleteById(id);
    }

    // --- Helper Method ---
    private FoodDTO convertToDTO(FoodBean foodBean) {
        FoodDTO dto = new FoodDTO();
        dto.setId(foodBean.getId());
        dto.setName(foodBean.getName());
        dto.setPrice(foodBean.getPrice());
        dto.setDescription(foodBean.getDescription());
        dto.setScore(foodBean.getScore());
        dto.setIsActive(foodBean.getIsActive());
        dto.setStock(foodBean.getStock());
        dto.setImgResource(foodBean.getImgResource());
        dto.setTagNames(foodBean.getTags());
        if (foodBean.getStore() != null) {
            dto.setStoreId(foodBean.getStore().getId());
            dto.setStoreName(foodBean.getStore().getName());
        }

        if (foodBean.getClassifications() != null && !foodBean.getClassifications().isEmpty()) {
            // 為了簡化，DTO 只顯示第一個分類的資訊
            FoodClassificationBean primaryClassification = foodBean.getClassifications().iterator().next();
            dto.setCategoryName(primaryClassification.getFoodClass().getName());
            dto.setCategoryId(primaryClassification.getFoodClass().getId());
        }
        if (foodBean.getTags() != null && !foodBean.getTags().isEmpty()) {
            dto.setTagNames(foodBean.getTags());
        }

        return dto;
    }
}