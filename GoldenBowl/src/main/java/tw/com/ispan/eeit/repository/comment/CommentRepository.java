package tw.com.ispan.eeit.repository.comment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.ispan.eeit.model.entity.comment.CommentBean;

public interface CommentRepository extends JpaRepository<CommentBean, Integer> {
    // 根據 orderId 查詢評論
    // List<CommentBean> findByOrderId(Integer orderId);

    // // 根據 userId 查詢評論
    List<CommentBean> findByUserId(Integer userId);

    // // 根據 storeId 查詢評論
    // List<CommentBean> findByStoreId(Integer storeId);

    // // 根據 isHidden 查詢評論
    // List<CommentBean> findByIsHidden(Boolean isHidden);
    List<CommentBean> findByStoreId(Integer storeId);

}
