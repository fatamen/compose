package tw.com.ispan.eeit.controller.food;

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

import jakarta.validation.Valid;
import tw.com.ispan.eeit.model.dto.food.FoodClassDTO;
import tw.com.ispan.eeit.model.dto.food.FoodClassRequest;
import tw.com.ispan.eeit.service.food.FoodClassService;


@RestController
@RequestMapping("/api/food-classes")
public class FoodClassController {
	@Autowired
	private FoodClassService foodClassService;
	
	@GetMapping
	public List<FoodClassDTO> getAllFoodClasses(){
		return foodClassService.findAllFoodClasses();
	}
	
	// Read (List by Store) API.  GET /api/food-classes/store/1
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<FoodClassDTO>> getClassesByStoreId(@PathVariable Integer storeId) {
        List<FoodClassDTO> classes = foodClassService.findClassesByStoreId(storeId);
        return ResponseEntity.ok(classes);
    }
    // 【新增】Create
    @PostMapping
    public ResponseEntity<FoodClassDTO> createFoodClass(@Valid @RequestBody FoodClassRequest request) {
        FoodClassDTO createdClass = foodClassService.createFoodClass(request);
        // 回傳 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(createdClass);
    }

    // 【新增】Update
    @PutMapping("/{id}")
    public ResponseEntity<FoodClassDTO> updateFoodClass(@PathVariable Integer id, @Valid @RequestBody FoodClassRequest request) {
        FoodClassDTO updatedClass = foodClassService.updateFoodClass(id, request);
        return ResponseEntity.ok(updatedClass);
    }

    // 【新增】Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoodClass(@PathVariable Integer id) {
        foodClassService.deleteFoodClass(id);
        return ResponseEntity.noContent().build(); // 回傳 204 No Content
    }

}
