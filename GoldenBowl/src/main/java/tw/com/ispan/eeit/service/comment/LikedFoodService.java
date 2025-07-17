package tw.com.ispan.eeit.service.comment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tw.com.ispan.eeit.model.dto.comment.LikedFoodRequestDTO;
import tw.com.ispan.eeit.model.dto.comment.LikedFoodResponseDTO;
import tw.com.ispan.eeit.model.entity.comment.LikedFoodBean;
import tw.com.ispan.eeit.repository.UserRepository;
import tw.com.ispan.eeit.repository.comment.LikedFoodRepository;
import tw.com.ispan.eeit.repository.food.FoodRepository;
import tw.com.ispan.eeit.repository.order.OrderDetailRepository;

@Service
public class LikedFoodService {

    @Autowired
    private LikedFoodRepository likedFoodRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository; // 假設您有這個 Repository

    // 創建或更新喜歡的食物 (如果已存在則更新，否則創建)
    @Transactional
    public LikedFoodResponseDTO createOrUpdateLikedFood(LikedFoodRequestDTO likedFoodDto) {
        Optional<LikedFoodBean> existingLikedFood = likedFoodRepository.findByUserIdAndFoodId(
                likedFoodDto.getUserId(), likedFoodDto.getFoodId());

        LikedFoodBean likedFood;
        if (existingLikedFood.isPresent()) {
            likedFood = existingLikedFood.get();
            likedFood.setIsLiked(likedFoodDto.getIsLiked()); // 更新喜歡狀態
            likedFood.setUpdatedTime(LocalDateTime.now());
        } else {
            likedFood = new LikedFoodBean();
            likedFood.setIsLiked(likedFoodDto.getIsLiked());
            likedFood.setUpdatedTime(LocalDateTime.now());

            // 設置關聯實體
            userRepository.findById(likedFoodDto.getUserId())
                    .ifPresent(likedFood::setUser);
            foodRepository.findById(likedFoodDto.getFoodId())
                    .ifPresent(likedFood::setFood);
            if (likedFoodDto.getOrderDetailId() != null) {
                orderDetailRepository.findById(likedFoodDto.getOrderDetailId())
                        .ifPresent(likedFood::setOrderDetail);
            }
        }
        LikedFoodBean savedLikedFood = likedFoodRepository.save(likedFood);
        return LikedFoodResponseDTO.fromEntity(savedLikedFood);
    }

    // 根據 ID 查找喜歡的食物 (返回 DTO)
    public Optional<LikedFoodResponseDTO> findLikedFoodDtoById(Integer id) {
        return likedFoodRepository.findById(id)
                .map(LikedFoodResponseDTO::fromEntity);
    }

    // 根據用戶 ID 查找喜歡的食物列表 (返回 DTO 列表)
    public List<LikedFoodResponseDTO> findLikedFoodsByUserIdAsDto(Integer userId) {
        List<LikedFoodBean> likedFoods = likedFoodRepository.findByUserId(userId);
        return likedFoods.stream()
                .map(LikedFoodResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 根據食物 ID 查找喜歡的食物列表 (返回 DTO 列表)
    public List<LikedFoodResponseDTO> findLikedFoodsByFoodIdAsDto(Integer foodId) {
        List<LikedFoodBean> likedFoods = likedFoodRepository.findByFoodId(foodId);
        return likedFoods.stream()
                .map(LikedFoodResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 刪除喜歡的食物 (根據 ID)
    public void deleteLikedFood(Integer id) {
        if (!likedFoodRepository.existsById(id)) {
            throw new IllegalArgumentException("Liked Food with ID " + id + " does not exist.");
        }
        likedFoodRepository.deleteById(id);
    }

    // 更新現有的喜歡的食物狀態
    public Optional<LikedFoodResponseDTO> updateLikedFood(Integer id, LikedFoodRequestDTO likedFoodDto) {
        return likedFoodRepository.findById(id).map(existingLikedFood -> {
            // 更新 isLiked 狀態
            // 這裡假設您只更新 isLiked 字段，如果需要更新其他字段，請相應添加
            existingLikedFood.setIsLiked(likedFoodDto.getIsLiked());
            LikedFoodBean updatedBean = likedFoodRepository.save(existingLikedFood);
            return LikedFoodResponseDTO.fromEntity(updatedBean);
        });
    }
}
