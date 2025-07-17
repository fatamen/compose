package tw.com.ispan.eeit.service.food;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tw.com.ispan.eeit.model.dto.food.FoodClassDTO;
import tw.com.ispan.eeit.model.entity.food.FoodClassBean;
import tw.com.ispan.eeit.repository.food.FoodClassRepository;

@Service
public class FoodClassService {
	
	@Autowired
    private FoodClassRepository foodClassRepository;
	
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
