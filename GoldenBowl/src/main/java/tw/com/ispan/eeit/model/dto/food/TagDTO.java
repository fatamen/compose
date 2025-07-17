package tw.com.ispan.eeit.model.dto.food;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagDTO {
    private Integer id;
    private String name;
    // 不包含 foods 和 userTags 屬性，因為它們是導致 ConcurrentModificationException 的原因
}
