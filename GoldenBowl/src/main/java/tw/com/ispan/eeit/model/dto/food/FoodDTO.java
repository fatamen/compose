package tw.com.ispan.eeit.model.dto.food;

import java.util.List;

//3. FoodDto.java (可以定義為 OrderDetailDto 的靜態內部類，或獨立類)

import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.food.FoodBean;
import tw.com.ispan.eeit.model.entity.food.TagBean;

@Data
@NoArgsConstructor
public class FoodDTO {
	private Integer id;
	private String name;
	private Integer price;
	private Float score;
	private String imgResource;
	private String description;
	private String storeName;
	private Integer storeId;
	private List<TagBean> tagNames;

	// 新增food資料需要的映射 by kinan
	private Boolean isActive;
	private Integer stock;
	private String categoryName;
	private Integer categoryId;

	// ... 其他食物屬性
	public static FoodDTO fromEntity(FoodBean foodBean) {
		FoodDTO foodDto = new FoodDTO();
		foodDto.setId(foodBean.getId());
		foodDto.setName(foodBean.getName());
		foodDto.setPrice(foodBean.getPrice());
		foodDto.setScore(foodBean.getScore());
		foodDto.setImgResource(foodBean.getImgResource());
		foodDto.setDescription(foodBean.getDescription());
		return foodDto;
	}

}