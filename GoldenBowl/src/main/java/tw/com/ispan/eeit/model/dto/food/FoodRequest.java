package tw.com.ispan.eeit.model.dto.food;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class FoodRequest {

    @NotNull(message = "店家 ID 不可為空")
    private Integer storeId;

    @NotBlank(message = "食物名稱不可為空")
    private String name;

    @NotNull(message = "價格不可為空")
    @Min(value = 0, message = "價格不可為負數")
    private Integer price;

    private String description;

    @NotNull(message = "庫存量不可為空")
    @Min(value = 0, message = "庫存量不可為負數")
    private Integer stock;

    private String imgResource;
    
 // 🔥 新增：供應狀態欄位
    private Boolean isActive;

    // 接收食物所屬的分類 ID 列表
    // 一個食物可以屬於多個分類
    @NotEmpty(message = "食物分類不可為空")
    private List<Integer> foodClassIds;
}