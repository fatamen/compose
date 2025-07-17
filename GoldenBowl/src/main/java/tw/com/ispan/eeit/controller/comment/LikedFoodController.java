package tw.com.ispan.eeit.controller.comment;

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
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.dto.comment.LikedFoodRequestDTO;
import tw.com.ispan.eeit.model.dto.comment.LikedFoodResponseDTO;
import tw.com.ispan.eeit.service.comment.LikedFoodService;

@RestController
@RequestMapping("/liked-food")
public class LikedFoodController {

    @Autowired
    private LikedFoodService likedFoodService;

    // 創建或更新喜歡的食物
    @PostMapping
    public ResponseEntity<LikedFoodResponseDTO> createOrUpdateLikedFood(@RequestBody LikedFoodRequestDTO likedFoodDto) {
        LikedFoodResponseDTO savedLikedFood = likedFoodService.createOrUpdateLikedFood(likedFoodDto);
        return new ResponseEntity<>(savedLikedFood, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LikedFoodResponseDTO> updateLikedFood(@PathVariable Integer id,
            @RequestBody LikedFoodRequestDTO likedFoodDto) {
        // 在 Service 層處理更新邏輯，返回 Optional<LikedFoodResponseDTO>
        return likedFoodService.updateLikedFood(id, likedFoodDto)
                .map(ResponseEntity::ok) // 如果更新成功，返回 200 OK 和更新後的對象
                .orElse(ResponseEntity.notFound().build()); // 如果找不到對應 ID 的記錄，返回 404 Not Found
    }

    // 根據 ID 查找喜歡的食物
    @GetMapping("/{id}")
    public ResponseEntity<LikedFoodResponseDTO> getLikedFoodById(@PathVariable Integer id) {
        return likedFoodService.findLikedFoodDtoById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 根據用戶 ID 查找所有喜歡的食物
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LikedFoodResponseDTO>> getLikedFoodsByUserId(@PathVariable Integer userId) {
        List<LikedFoodResponseDTO> likedFoods = likedFoodService.findLikedFoodsByUserIdAsDto(userId);
        if (likedFoods.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(likedFoods);
    }

    // 根據食物 ID 查找所有喜歡此食物的紀錄
    @GetMapping("/food/{foodId}")
    public ResponseEntity<List<LikedFoodResponseDTO>> getLikedFoodsByFoodId(@PathVariable Integer foodId) {
        List<LikedFoodResponseDTO> likedFoods = likedFoodService.findLikedFoodsByFoodIdAsDto(foodId);
        if (likedFoods.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(likedFoods);
    }

    // 刪除喜歡的食物
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLikedFood(@PathVariable Integer id) {
        try {
            likedFoodService.deleteLikedFood(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
