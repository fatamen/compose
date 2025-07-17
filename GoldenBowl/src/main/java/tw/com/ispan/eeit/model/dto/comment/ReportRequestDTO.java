package tw.com.ispan.eeit.model.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {
    // 不包含 ID，因為創建時 ID 由資料庫生成
    private Integer reportTypeId;
    private Integer commentId;
    private Integer submitterId;
    private String submitterType;
    private String status;
    // reportDate 通常由後端生成，所以這裡可以不包含，或者如果前端需要指定則包含
    // private LocalDateTime reportDate;
}