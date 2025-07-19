package tw.com.ispan.eeit.model.dto.food;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FoodClassRequest {
 @NotNull(message = "店家 ID 不可為空")
 private Integer storeId;

 @NotBlank(message = "分類名稱不可為空")
 private String name;

 private String description;

 private Integer sort;
}
