package tw.com.ispan.eeit.service.food;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tw.com.ispan.eeit.exception.ResourceNotFoundException;
import tw.com.ispan.eeit.model.dto.food.FoodClassDTO;
import tw.com.ispan.eeit.model.dto.food.FoodClassRequest;
import tw.com.ispan.eeit.model.entity.food.FoodClassBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.repository.store.StoreRepository; // 【新增】引入 StoreRepository
import tw.com.ispan.eeit.repository.food.FoodClassRepository;

@Service
public class FoodClassService {
	
	@Autowired
    private FoodClassRepository foodClassRepository;
	
	@Autowired
    private StoreRepository storeRepository;
	
	public List<FoodClassDTO> findAllFoodClasses(){
		List<FoodClassBean> foodClassBeans = foodClassRepository.findAll();
		return foodClassBeans.stream()
                .map(this::convertToDTO) // 對每個 FoodClassBean 執行 convertToDTO 方法
                .collect(Collectors.toList()); // 將結果收集成一個新的 List
	}
	
	public List<FoodClassDTO> findClassesByStoreId(Integer storeId) {
        List<FoodClassBean> foodClassBeans = foodClassRepository.findByStoreId(storeId);
        return foodClassBeans.stream()
            .map(this::convertToDTO) // 重複使用已有的轉換方法
            .collect(Collectors.toList());
    }
	
	// 【新增】Create
    @Transactional
    public FoodClassDTO createFoodClass(FoodClassRequest request) {
        StoreBean store = storeRepository.findById(request.getStoreId())
            .orElseThrow(() -> new ResourceNotFoundException("..."));
        
        FoodClassBean newClass = new FoodClassBean();
        newClass.setStore(store);
        newClass.setName(request.getName());
        newClass.setDescription(request.getDescription());
        newClass.setSort(request.getSort());
        
        FoodClassBean savedClass = foodClassRepository.save(newClass);
        return convertToDTO(savedClass);
    }

    // 【新增】Update
    @Transactional
    public FoodClassDTO updateFoodClass(Integer id, FoodClassRequest request) {
        FoodClassBean existingClass = foodClassRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("..."));
        
        // 更新欄位
        existingClass.setName(request.getName());
        existingClass.setDescription(request.getDescription());
        existingClass.setSort(request.getSort());
        
        FoodClassBean updatedClass = foodClassRepository.save(existingClass);
        return convertToDTO(updatedClass);
    }

    // 【新增】Delete
    @Transactional
    public void deleteFoodClass(Integer id) {
        if (!foodClassRepository.existsById(id)) {
            throw new ResourceNotFoundException("...");
        }
        // 注意：刪除分類前，可能需要檢查是否有食物仍在使用這個分類
        foodClassRepository.deleteById(id);
    }	
	
    private FoodClassDTO convertToDTO(FoodClassBean foodClassBean) {
        FoodClassDTO dto = new FoodClassDTO();
        dto.setId(foodClassBean.getId());
        dto.setName(foodClassBean.getName());
        dto.setDescription(foodClassBean.getDescription());
        dto.setSort(foodClassBean.getSort());
        
//         安全地取得關聯物件的屬性
        if (foodClassBean.getStore() != null) {
            dto.setStoreId(foodClassBean.getStore().getId());
        }
        
        return dto;
    }
}
