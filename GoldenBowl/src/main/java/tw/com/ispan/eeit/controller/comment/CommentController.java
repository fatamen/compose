package tw.com.ispan.eeit.controller.comment;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.dto.comment.CommentRequestDTO;
import tw.com.ispan.eeit.model.dto.comment.CommentResponseDTO;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.comment.CommentBean;
import tw.com.ispan.eeit.model.entity.order.OrderBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.repository.UserRepository;
import tw.com.ispan.eeit.repository.order.OrderRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;
import tw.com.ispan.eeit.service.comment.CommentService;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StoreRepository storeRepository;

    @PostMapping
    public ResponseEntity<CommentBean> createComment(@RequestBody CommentRequestDTO commentDto) { // <--- 這裡改為
                                                                                                  // CommentRequestDTO
        CommentBean comment = new CommentBean(); // 創建一個 CommentBean 實例
        comment.setContent(commentDto.getContent());
        comment.setScore(commentDto.getScore());
        comment.setReply(commentDto.getReply());
        // isHidden 預設值在 CommentBean 中已經設置，也可以在這裡根據 DTO 設定
        comment.setIsHidden(commentDto.getIsHidden() != null ? commentDto.getIsHidden() : false);

        // 根據 DTO 中的 ID 查找並設置關聯實體
        // 使用 Optional.ifPresent 防止 NullPointerException
        if (commentDto.getOrderId() != null) {
            Optional<OrderBean> orderOptional = orderRepository.findById(commentDto.getOrderId());
            orderOptional.ifPresent(comment::setOrder);
        }
        if (commentDto.getUserId() != null) {
            Optional<UserBean> userOptional = userRepository.findById(commentDto.getUserId());
            userOptional.ifPresent(comment::setUser);
        }
        if (commentDto.getStoreId() != null) {
            Optional<StoreBean> storeOptional = storeRepository.findById(commentDto.getStoreId());
            storeOptional.ifPresent(comment::setStore);
        }

        CommentBean savedComment = commentService.createComment(comment); // 將完整的 CommentBean 傳遞給 Service
        return ResponseEntity.ok(savedComment);
    }

    // 根據 ID 查找評論 (返回 DTO)
    @GetMapping("/{id}")
    public ResponseEntity<CommentResponseDTO> getCommentById(@PathVariable Integer id) {
        return commentService.findCommentDtoById(id) // 調用返回 DTO 的方法
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 查找所有評論 (返回 DTO 列表)
    @GetMapping
    public ResponseEntity<List<CommentResponseDTO>> getAllComments() {
        List<CommentResponseDTO> comments = commentService.findAllAsDto(); // 調用返回 DTO 列表的方法
        return ResponseEntity.ok(comments);
    }

    // 更新評論
    @PutMapping("/{id}")
    public ResponseEntity<CommentBean> updateComment(@PathVariable Integer id,
            @RequestBody CommentRequestDTO commentDto) {
        // 在 Service 層處理 DTO 到 Bean 的轉換和更新邏輯
        return commentService.updateCommentFromDto(id, commentDto) // 新增一個方法處理 DTO
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 刪除評論（邏輯刪除）
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer id) {
        try {
            commentService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 根據 Store ID 查找評論列表 (返回 DTO 列表)
    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByStoreId(@PathVariable Integer storeId) {
        List<CommentResponseDTO> comments = commentService.findByStoreIdAsDto(storeId); // 調用返回 DTO 列表的方法
        if (comments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(comments);
    }

    // 新增：根據 User ID 查找評論列表 (返回 DTO 列表)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByUserId(@PathVariable Integer userId) {
        List<CommentResponseDTO> comments = commentService.findByUserIdAsDto(userId);
        if (comments.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(comments);
    }
}