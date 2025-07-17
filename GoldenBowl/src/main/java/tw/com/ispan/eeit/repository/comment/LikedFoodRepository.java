package tw.com.ispan.eeit.repository.comment;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.comment.LikedFoodBean;

@Repository
public interface LikedFoodRepository extends JpaRepository<LikedFoodBean, Integer> {
    // 根據用戶ID和食物ID查找喜歡的食物 (可能存在)
    Optional<LikedFoodBean> findByUserIdAndFoodId(Integer userId, Integer foodId);

    // 根據用戶ID查找所有喜歡的食物
    List<LikedFoodBean> findByUserId(Integer userId);

    // 根據食物ID查找所有喜歡的食物
    List<LikedFoodBean> findByFoodId(Integer foodId);
}
