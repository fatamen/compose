package tw.com.ispan.eeit.model.dto.food;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
public class FoodClassDTO {
	 private Integer id;	 
	 private String name;
	 private String description;
	 private Integer sort;
	 
	 // 關聯store
	 private Integer storeId;
}
