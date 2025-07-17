package tw.com.ispan.eeit.model.dto.plan;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlanDTO {
    private Integer id;
    private String name;
    private Integer price;
    private Integer validMonths;
}
