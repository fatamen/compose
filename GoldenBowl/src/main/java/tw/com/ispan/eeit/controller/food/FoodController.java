package tw.com.ispan.eeit.controller.food;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import tw.com.ispan.eeit.model.dto.food.FoodDTO;
import tw.com.ispan.eeit.model.dto.food.FoodRequest;
import tw.com.ispan.eeit.service.food.FoodService;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    @Autowired
    private FoodService foodService;

    // --- Create ---
    @PostMapping
    public ResponseEntity<FoodDTO> createFood(@Valid @RequestBody FoodRequest request) {
        FoodDTO createdFood = foodService.createFood(request);
        URI location = URI.create("/api/foods/" + createdFood.getId());
        return ResponseEntity.created(location).body(createdFood);
    }

    // --- Read (Single) ---
    @GetMapping("/{id}")
    public ResponseEntity<FoodDTO> getFoodById(@PathVariable Integer id) {
        FoodDTO food = foodService.findFoodById(id);
        return ResponseEntity.ok(food);
    }

    // --- Read (List by Store) ---
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<FoodDTO>> getFoodsByStoreId(@PathVariable Integer storeId) {
        List<FoodDTO> foods = foodService.findFoodsByStoreId(storeId);
        return ResponseEntity.ok(foods);
    }

    // --- Update ---
    @PutMapping("/{id}")
    public ResponseEntity<FoodDTO> updateFood(@PathVariable Integer id, @Valid @RequestBody FoodRequest request) {
        FoodDTO updatedFood = foodService.updateFood(id, request);
        return ResponseEntity.ok(updatedFood);
    }

    // --- Delete ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Integer id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build(); // 回傳 204 No Content
    }
 // 增加有上架的食物--ted
    @GetMapping("/active/store/{storeId}")
    public ResponseEntity<List<FoodDTO>> findActiveFoodsByStoreId(@PathVariable Integer storeId) {
        List<FoodDTO> foods = foodService.findActiveFoodsByStoreId(storeId);
        return ResponseEntity.ok(foods);
    }
    
    @PostMapping("/{foodId}/upload-photo")
    public ResponseEntity<String> uploadFoodPhoto(@PathVariable Integer foodId,
                                                  @RequestParam("file") MultipartFile file) {
        try {
            // 生成檔案名稱（可以包含時間戳避免重複）
            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String filename = "food_" + foodId + "_" + timestamp + extension;
            
            Path path = Paths.get("/var/www/images/" + filename);
            Files.createDirectories(path.getParent());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // 更新 food 的 imgResource 欄位
            String relativePath = "images/" + filename;
            foodService.updateImagePath(foodId, relativePath);

            return ResponseEntity.ok(relativePath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("圖片儲存失敗");
        }
    }

    // 通用上傳 API（用於新增 food 時）
    @PostMapping("/upload-image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            String timestamp = String.valueOf(System.currentTimeMillis());
            
            // 生成檔案名稱
            String nameWithoutExt = originalName.substring(0, originalName.lastIndexOf("."));
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String filename = nameWithoutExt + "_" + timestamp + extension;
            
            // 🔥 新增：定義兩個儲存位置
            Path primaryPath = Paths.get("/var/www/images/" + filename);
            Path backupPath1 = Paths.get("../vue-cus/public/image/" + filename);
            Path backupPath2 = Paths.get("../vue-store/public/image/" + filename);
            
            // 確保兩個目錄都存在
            Files.createDirectories(primaryPath.getParent());
            Files.createDirectories(backupPath1.getParent());
            Files.createDirectories(backupPath2.getParent());
            
            // 🔥 儲存到主要位置
            Files.copy(file.getInputStream(), primaryPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 🔥 備份到 vue-cus、vue-store
            Files.copy(primaryPath, backupPath1, StandardCopyOption.REPLACE_EXISTING);
            Files.copy(primaryPath, backupPath2, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("✅ 圖片已儲存到主要位置: " + primaryPath);
            System.out.println("✅ 圖片已備份到 vue-cus: " + backupPath1);
            System.out.println("✅ 圖片已備份到 vue-store: " + backupPath2);

            String relativePath = "images/" + filename;
            return ResponseEntity.ok(relativePath);
            
        } catch (IOException e) {
            System.err.println("❌ 圖片儲存失敗: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("圖片儲存失敗: " + e.getMessage());
        }
    }
    
    @GetMapping("/test-upload")
    public ResponseEntity<String> testUpload() {
        return ResponseEntity.ok("Upload API is working!");
    }
    
}
