package tw.com.ispan.eeit.model.dto.comment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {
    private Integer id;
    private Integer reportTypeId;
    private Integer commentId;
    private Integer submitterId;
    private String submitterType;
    private String status;
    private LocalDateTime reportDate;

    // 關聯物件的簡化資訊
    private String reportTypeName; // 來自 ReportTypeBean.name
    private String commentContent; // 來自 CommentBean.content
    private Integer commentScore; // 來自 CommentBean.score (可選，但通常會一起顯示)
    private Boolean commentIsHidden; // 0716 JIMMY 新增：來自 CommentBean.isHidden
}