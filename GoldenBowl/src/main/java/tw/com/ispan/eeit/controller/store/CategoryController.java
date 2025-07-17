package tw.com.ispan.eeit.controller.store;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
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

import tw.com.ispan.eeit.model.dto.store.CategoryDTO;
import tw.com.ispan.eeit.model.entity.store.CategoryBean;
import tw.com.ispan.eeit.service.store.CategoryService;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryBean> categories = categoryService.getAllCategories();
        // 將 CategoryBean 列表轉換為 CategoryDTO 列表
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(bean -> {
                    CategoryDTO dto = new CategoryDTO();
                    BeanUtils.copyProperties(bean, dto); // 複製 id 和 name
                    return dto;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Integer id) {
        Optional<CategoryBean> category = categoryService.getCategoryById(id);
        return category.map(bean -> {
            CategoryDTO dto = new CategoryDTO();
            BeanUtils.copyProperties(bean, dto);
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryDTO categoryDto) {
        CategoryBean categoryBean = new CategoryBean();
        BeanUtils.copyProperties(categoryDto, categoryBean); // 將 DTO 屬性複製到 Bean

        CategoryBean createdCategory = categoryService.createCategory(categoryBean);

        CategoryDTO responseDto = new CategoryDTO();
        BeanUtils.copyProperties(createdCategory, responseDto); // 將創建的 Bean 屬性複製回 DTO
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    // 更新分類 (使用 DTO 作為請求體)
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Integer id,
            @RequestBody CategoryDTO categoryDto) {
        CategoryBean categoryBean = new CategoryBean();
        BeanUtils.copyProperties(categoryDto, categoryBean); // DTO -> Bean

        CategoryBean updatedCategory = categoryService.updateCategory(id, categoryBean); // 假設 Service 會處理更新邏輯

        if (updatedCategory != null) {
            CategoryDTO responseDto = new CategoryDTO();
            BeanUtils.copyProperties(updatedCategory, responseDto); // Bean -> DTO
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteCategory(@PathVariable Integer id) {
        if (categoryService.deleteCategory(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
