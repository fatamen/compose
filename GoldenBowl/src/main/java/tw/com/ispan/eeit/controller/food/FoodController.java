package tw.com.ispan.eeit.controller.food;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
