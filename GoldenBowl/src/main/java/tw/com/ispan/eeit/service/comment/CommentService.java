package tw.com.ispan.eeit.service.comment;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import tw.com.ispan.eeit.model.dto.comment.CommentRequestDTO;
import tw.com.ispan.eeit.model.dto.comment.CommentResponseDTO;
import tw.com.ispan.eeit.model.entity.comment.CommentBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.repository.comment.CommentRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 創建評論
    @Transactional // 確保創建和更新分數在同一事務中
    public CommentBean createComment(CommentBean comment) {
        System.out.println("DEBUG: Entering createComment method.");
        comment.setCreateTime(LocalDateTime.now());
        comment.setIsHidden(false); // 預設不隱藏

        CommentBean savedComment = commentRepository.save(comment);
        System.out.println("DEBUG: Comment created and saved with ID: " + savedComment.getId());

        // 新增: 在評論創建後更新商店評分
        if (savedComment.getStore() != null && savedComment.getStore().getId() != null) {
            System.out.println("DEBUG: Calling updateStoreScore from createComment for Store ID: " + savedComment.getStore().getId());
            updateStoreScore(savedComment.getStore().getId());
        } else {
            System.out.println("DEBUG: Cannot update store score from createComment, Store object or ID is null.");
        }
        return savedComment;
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
        System.out.println("DEBUG: Entering updateCommentFromDto for Comment ID: " + id);
        return commentRepository.findById(id).map(existingComment -> {
            Integer originalStoreId = null;
            if (existingComment.getStore() != null) {
                originalStoreId = existingComment.getStore().getId();
            }
            System.out.println("DEBUG: Original Store ID for comment " + id + ": " + originalStoreId);


            // 檢查並更新 isHidden 屬性
            if (commentDto.getIsHidden() != null) {
                boolean originalIsHidden = existingComment.getIsHidden();
                existingComment.setIsHidden(commentDto.getIsHidden());
                System.out.println("DEBUG: updateCommentFromDto: isHidden changed from " + originalIsHidden + " to " + existingComment.getIsHidden());
            }

            if (commentDto.getContent() != null) {
                existingComment.setContent(commentDto.getContent());
            }
            if (commentDto.getScore() != null) {
                existingComment.setScore(commentDto.getScore());
            }
            if (commentDto.getReply() != null) {
                existingComment.setReply(commentDto.getReply());
                existingComment.setReplyUpdateTime(LocalDateTime.now());
            }

            CommentBean updatedComment = commentRepository.save(existingComment);
            System.out.println("DEBUG: Comment " + updatedComment.getId() + " updated and saved.");

            // 新增: 在評論更新後更新商店評分
            // 由於更新評論 (特別是 isHidden) 可能會影響平均分數，所以這裡需要觸發更新
            if (originalStoreId != null) { // 使用原始的 Store ID，確保能找到對應的店家
                System.out.println("DEBUG: Calling updateStoreScore from updateCommentFromDto for Store ID: " + originalStoreId);
                updateStoreScore(originalStoreId);
            } else {
                System.out.println("DEBUG: Cannot update store score from updateCommentFromDto, Original Store ID is null.");
            }
            return updatedComment;
        });
    }

    // 刪除評論（邏輯刪除，將 isHidden 設為 true）
    // 注意: 你目前的 deleteById 是物理刪除。如果你想做邏輯刪除，應該是更新 isHidden 字段。
    // 如果是物理刪除，需要在刪除前獲取 storeId，然後再呼叫 updateStoreScore。
    @Transactional
    public void deleteById(Integer id) {
        System.out.println("DEBUG: Entering deleteById for Comment ID: " + id);
        Optional<CommentBean> commentOptional = commentRepository.findById(id);

        if (commentOptional.isEmpty()) {
            System.out.println("DEBUG: Comment with ID " + id + " not found for deletion.");
            throw new IllegalArgumentException("Record with ID " + id + " does not exist");
        }

        CommentBean commentToDelete = commentOptional.get();
        Integer storeId = null;
        if (commentToDelete.getStore() != null) {
            storeId = commentToDelete.getStore().getId();
        }

        commentRepository.deleteById(id); // 執行物理刪除
        System.out.println("DEBUG: Comment " + id + " physically deleted.");

        // 新增: 在評論刪除後更新商店評分
        if (storeId != null) {
            System.out.println("DEBUG: Calling updateStoreScore from deleteById for Store ID: " + storeId);
            updateStoreScore(storeId);
        } else {
            System.out.println("DEBUG: Cannot update store score from deleteById, Store ID is null for deleted comment.");
        }
    }

    @Transactional
    private void updateStoreScore(Integer storeId) {
    System.out.println("DEBUG: Entering updateStoreScore for Store ID: " + storeId);

    Optional<StoreBean> storeOptional = storeRepository.findById(storeId);
    if (storeOptional.isEmpty()) {
        System.out.println("DEBUG: Store not found for ID: " + storeId);
        return;
    }
    StoreBean store = storeOptional.get();

    // 查詢所有未隱藏的評論
    List<CommentBean> activeComments = commentRepository.findByStoreIdAndIsHiddenFalse(storeId);
    System.out.println("DEBUG: Found " + activeComments.size() + " active comments for Store ID: " + storeId);

    double newAverageScore = activeComments.stream()
            .mapToDouble(CommentBean::getScore)
            .average()
            .orElse(0.0); // 如果沒有評論，預設分數為 0

    // 轉換為 float 類型，並處理精度
    float finalScore = (float) (Math.round(newAverageScore * 10.0) / 10.0);
    System.out.println("DEBUG: Calculated new average score for Store ID " + storeId + ": " + finalScore);

    // 获取评论数量
    int newCommentCount = activeComments.size(); // <--- 獲取評論數量

    // 只有在評分改變時才更新並發送 WebSocket 訊息
    // 或者，如果評論數量改變了，也應該發送
    if (store.getScore() == null || store.getScore() != finalScore || store.getComments().size() != newCommentCount) { // <--- 這裡要考慮評論數量變化
        float oldScore = store.getScore() != null ? store.getScore() : 0.0f;
        store.setScore(finalScore);
        storeRepository.save(store); // 保存更新到資料庫
        System.out.println("DEBUG: Store ID " + storeId + " score updated from " + oldScore + " to " + finalScore + " in DB.");
        
        // 為了確保 store.getComments().size() 能正確反映 newCommentCount，
        // 您可能需要確保 store Bean 的 comments 集合被重新載入或同步。
        // 但由於您在上面重新查詢了 activeComments，newCommentCount 已經是準確的了。
        // 所以這裡無需額外對 store.setComments()，因為我們只傳遞數量。


        // --- 發送 WebSocket 訊息 ---
        try {
            // 創建訊息物件
            Map<String, Object> message = new HashMap<>();
            message.put("type", "UPDATE_STORE_INFO"); // <--- 建議改為更通用的類型
            message.put("storeId", storeId);
            message.put("newScore", finalScore);
            message.put("newCommentCount", newCommentCount); // <--- 新增評論數量

            // 將 Map 轉換為 JSON 字串
            String jsonMessage = new ObjectMapper().writeValueAsString(message);

            messagingTemplate.convertAndSend("/topic/scores", jsonMessage); // 主題名稱可以考慮改成 /topic/storeUpdates
            System.out.println("CommentService: 已發送商店資訊更新 WebSocket 訊息給 /topic/scores: " + jsonMessage);
        } catch (JsonProcessingException e) {
            System.err.println("CommentService: 無法將商店資訊更新訊息轉換為 JSON: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("CommentService: 發送 WebSocket 訊息失敗: " + e.getMessage());
        }
    } else {
        System.out.println("DEBUG: Store ID " + storeId + " score and comment count did not change (" + finalScore + ", " + newCommentCount + "), no update/WebSocket sent.");
    }
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

        if (commentBean.getUser() != null) {
            dto.setUserId(commentBean.getUser().getId());
            dto.setUserName(commentBean.getUser().getName());
        } else {
            dto.setUserName("匿名使用者");
            dto.setUserId(null);
        }

        return dto;
    }
}