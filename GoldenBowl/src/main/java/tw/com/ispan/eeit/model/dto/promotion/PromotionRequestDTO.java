package tw.com.ispan.eeit.model.dto.promotion;

import java.util.List;
import lombok.Data;

@Data
public class PromotionRequestDTO {
    private Integer userId;
    private Integer storeId;
    private Integer amount;
    private List<Integer> tagIds;
    private String tagSpendMap;
}
