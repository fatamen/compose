package tw.com.ispan.eeit.model.dto.comment;

import lombok.Data;

@Data
public class CommentRequestDTO {
    private String content;
    private Integer score;
    private String reply;
    private Boolean isHidden;
    private Integer orderId; // 用於接收訂單 ID
    private Integer userId; // 用於接收用戶 ID
    private Integer storeId; // 用於接收店家 ID
}
