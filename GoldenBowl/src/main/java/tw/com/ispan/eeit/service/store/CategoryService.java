package tw.com.ispan.eeit.service.store;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.entity.store.CategoryBean;
import tw.com.ispan.eeit.repository.store.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<CategoryBean> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<CategoryBean> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    public CategoryBean createCategory(CategoryBean category) {
        return categoryRepository.save(category);
    }

    public CategoryBean updateCategory(Integer id, CategoryBean categoryDetails) {
        Optional<CategoryBean> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isPresent()) {
            CategoryBean existingCategory = optionalCategory.get();
            existingCategory.setName(categoryDetails.getName());
            // 注意：對於 ManyToMany 關聯，直接在 update 方法中設置可能需要更複雜的邏輯
            // existingCategory.setStores(categoryDetails.getStores());
            return categoryRepository.save(existingCategory);
        }
        return null;
    }

    public boolean deleteCategory(Integer id) {
        if (categoryRepository.existsById(id)) {
            categoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
