package tw.com.ispan.eeit.service.comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tw.com.ispan.eeit.model.dto.comment.CommentRequestDTO;
import tw.com.ispan.eeit.model.dto.comment.CommentResponseDTO;
import tw.com.ispan.eeit.model.entity.comment.CommentBean;
import tw.com.ispan.eeit.repository.comment.CommentRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    // 創建評論
    public CommentBean createComment(CommentBean comment) {
        comment.setCreateTime(LocalDateTime.now());
        comment.setIsHidden(false); // 預設不隱藏
        return commentRepository.save(comment);
    }

    // 根據 ID 查找評論 (返回 DTO)
    public Optional<CommentResponseDTO> findCommentDtoById(Integer id) {
        return commentRepository.findById(id)
                .map(this::convertToDto); // 使用轉換方法
    }

    // 新增此方法以根據 storeId 查找評論列表 (返回 DTO 列表)
    public List<CommentResponseDTO> findByStoreIdAsDto(Integer storeId) {
        List<CommentBean> comments = commentRepository.findByStoreId(storeId);
        return comments.stream()
                .filter(comment -> !comment.getIsHidden()) // 過濾隱藏的評論
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 新增：根據 userId 查找評論列表 (返回 DTO 列表)
    public List<CommentResponseDTO> findByUserIdAsDto(Integer userId) {
        List<CommentBean> comments = commentRepository.findByUserId(userId);
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 查找所有評論 (返回 DTO 列表)
    public List<CommentResponseDTO> findAllAsDto() {
        List<CommentBean> comments = commentRepository.findAll();
        return comments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 更新評論
    @Transactional
    public Optional<CommentBean> updateCommentFromDto(Integer id, CommentRequestDTO commentDto) {
        return commentRepository.findById(id).map(comment -> {
            // 根據 DTO 的內容更新 Bean 的屬性
            if (commentDto.getContent() != null) {
                comment.setContent(commentDto.getContent());
            }
            if (commentDto.getScore() != null) {
                comment.setScore(commentDto.getScore());
            }
            if (commentDto.getReply() != null) {
                comment.setReply(commentDto.getReply());
                comment.setReplyUpdateTime(LocalDateTime.now()); // 回覆更新時間
            }
            if (commentDto.getIsHidden() != null) {
                comment.setIsHidden(commentDto.getIsHidden());
            }

            // 對於關聯字段 (orderId, userId, storeId)，通常在更新時不會改變這些主關聯。
            // 如果需要更新關聯，你需要額外從 repository 查詢並設定。
            // 例如：
            // if (commentDto.getOrderId() != null &&
            // !comment.getOrder().getId().equals(commentDto.getOrderId())) {
            // orderRepository.findById(commentDto.getOrderId()).ifPresent(comment::setOrder);
            // }

            return commentRepository.save(comment);
        });
    }

    // 刪除評論（邏輯刪除，將 isHidden 設為 true）
    public void deleteById(Integer id) {
        if (!commentRepository.existsById(id)) {
            throw new IllegalArgumentException("Record with ID " + id + " does not exist");
        }
        commentRepository.deleteById(id);

    }

    // **新增：將 CommentBean 轉換為 CommentResponseDTO 的私有方法**
    private CommentResponseDTO convertToDto(CommentBean commentBean) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(commentBean.getId());
        dto.setContent(commentBean.getContent());
        dto.setScore(commentBean.getScore());
        dto.setCreateTime(commentBean.getCreateTime());
        dto.setReply(commentBean.getReply());
        dto.setReplyUpdateTime(commentBean.getReplyUpdateTime());
        dto.setIsHidden(commentBean.getIsHidden());

        // 從 UserBean 中獲取使用者名稱和 ID
        if (commentBean.getUser() != null) {
            dto.setUserId(commentBean.getUser().getId());
            dto.setUserName(commentBean.getUser().getName()); // 假設 UserBean 有 getName() 方法
        } else {
            // 如果 user 是 null (例如，評論的 user_id 在資料庫中為空或沒有關聯), 則設置為預設值
            dto.setUserName("匿名使用者");
            dto.setUserId(null);
        }

        return dto;
    }
}