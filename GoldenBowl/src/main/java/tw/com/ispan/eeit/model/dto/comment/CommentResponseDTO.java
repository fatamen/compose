package tw.com.ispan.eeit.model.dto.comment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.comment.CommentBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {
    private Integer id;
    private String content;
    private Integer score;
    private LocalDateTime createTime;
    private String reply;
    private LocalDateTime replyUpdateTime;
    private Boolean isHidden; // 如果前端需要知道評論是否被隱藏

    // 來自 UserBean 的資訊
    private Integer userId; // 用戶 ID (可選)
    private String userName; // 使用者名稱

    // 如果前端需要，可以加入其他關聯實體的簡單資訊，例如：
    private Integer storeId;
    private String storeName;

    /**
     * 静态方法，将 CommentBean 转换为 CommentResponseDTO。
     * 会包含关联的 UserBean 和 StoreBean 信息。
     *
     * @param commentBean 要转换的 CommentBean 对象。
     * @return 转换后的 CommentResponseDTO 对象。
     */
    public static CommentResponseDTO fromCommentBean(CommentBean commentBean) {
        if (commentBean == null) {
            return null;
        }

        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(commentBean.getId());
        dto.setContent(commentBean.getContent());
        dto.setScore(commentBean.getScore());
        dto.setCreateTime(commentBean.getCreateTime());
        dto.setReply(commentBean.getReply());
        dto.setReplyUpdateTime(commentBean.getReplyUpdateTime());
        dto.setIsHidden(commentBean.getIsHidden());

        // 处理 UserBean 信息
        UserBean user = commentBean.getUser();
        if (user != null) {
            dto.setUserId(user.getId());
            dto.setUserName(user.getName()); // 假设 UserBean 有一个 getName() 方法
        }

        // 处理 StoreBean 信息
        StoreBean store = commentBean.getStore();
        if (store != null) {
            dto.setStoreId(store.getId());
            dto.setStoreName(store.getName()); // 假设 StoreBean 有一个 getName() 方法
        }

        // 注意：OrderBean 的信息目前没有在 CommentResponseDTO 中定义字段，
        // 如果需要，可以根据需求添加 orderId 等字段。

        return dto;
    }
}