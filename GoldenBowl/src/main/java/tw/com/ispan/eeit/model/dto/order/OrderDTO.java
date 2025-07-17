package tw.com.ispan.eeit.model.dto.order;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;
import tw.com.ispan.eeit.model.dto.comment.CommentResponseDTO;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.comment.CommentBean;
import tw.com.ispan.eeit.model.entity.order.OrderBean;
import tw.com.ispan.eeit.model.entity.promotion.PromotionBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.util.DatetimeConvert;
import tw.com.ispan.eeit.util.StatusConvert;

@Data
public class OrderDTO {
    private Integer id;
    private Integer promotionId; // 只返回促銷ID，或者可以是一個 PromotionDto
    private Integer total;
    private String status;
    private String createTime;
    private String content;
    private String pickupTime;
    // 外部連結的部分

    private OrderUserDto User; // 一個存在在OrderDTO內的 UserDto
    private OrderStoreDto store;// 一個存在在OrderDTO內的StoreDTO
    private CommentResponseDTO comment;
    private List<OrderDetailDTO> orderDetails; // 包含訂單明細列表
    // private List<StoreDTO> stores;
    // 如果需要更詳細的User/Store/Promotion資料，可以嵌套對應的DTO

    @Data
    public static class OrderUserDto {
        private Integer id;
        private String name;
        // ... 其他用戶資訊

        public UserBean toUserBean() {
            UserBean userBean = new UserBean();
            userBean.setId(this.id);
            userBean.setName(this.name);
            // ... 其他屬性，如果 OrderUserDto 有更多
            return userBean;
        }
    }

    @Data
    public static class OrderStoreDto {
        private Integer id;
        private String name;
        private String photo;
        private Boolean isOpen;
        private Boolean isActive;

        public StoreBean toStoreBean() {
            StoreBean storeBean = new StoreBean();
            storeBean.setId(this.id);
            storeBean.setName(this.name);
            storeBean.setPhoto(this.photo);
            storeBean.setIsOpen(this.isOpen);
            storeBean.setIsActive(this.isActive);
            // ... 其他屬性
            return storeBean;
        }
    }

    public static OrderDTO fromEntity(OrderBean orderBean) {
        // 透過靜態法將Bean匯入到DTO中階產生物件
        OrderDTO orderDto = new OrderDTO();
        orderDto.setId(orderBean.getId());
        orderDto.setTotal(orderBean.getTotal());
        orderDto.setStatus(StatusConvert.toDTO(orderBean.getStatus()));
        orderDto.setCreateTime(
                DatetimeConvert.toString(orderBean.getCreateTime(), "yyyy/MM/dd HH:mm:ss"));
        // DatetimeConvert.toString(orderBean.getCreateTime(),"M月d日 EEEE HH時mm分"));
        orderDto.setContent(orderBean.getContent());
        orderDto.setPickupTime(
                DatetimeConvert.toString(orderBean.getPickupTime(), "yyyy/MM/dd HH:mm:ss"));
        // DatetimeConvert.toString(orderBean.getPickupTime(),"M月d日 EEEE HH時mm分"));

        // 複製 User 資訊
        // 檢查關聯是否已初始化且不為 null，避免 LazyInitializationException
        if (orderBean.getUser() != null &&
                org.hibernate.Hibernate.isInitialized(orderBean.getUser())) {
            // 如果 OrderDto 包含嵌套的 UserDto，可以這樣做：
            OrderDTO.OrderUserDto userDto = new OrderDTO.OrderUserDto();
            userDto.setId(orderBean.getUser().getId());
            userDto.setName(orderBean.getUser().getName());
            orderDto.setUser(userDto);
        }
        // 複製 Store資訊
        // 檢查關聯是否已初始化且不為 null，避免 LazyInitializationException
        if (orderBean.getStore() != null &&
                org.hibernate.Hibernate.isInitialized(orderBean.getStore())) {
            // 如果 OrderDto 包含嵌套的 UserDto，可以這樣做：
            OrderDTO.OrderStoreDto storeDto = new OrderDTO.OrderStoreDto();
            storeDto.setId(orderBean.getStore().getId());
            storeDto.setName(orderBean.getStore().getName());
            storeDto.setPhoto(orderBean.getStore().getPhoto());
            storeDto.setIsActive(orderBean.getStore().getIsActive());
            storeDto.setIsOpen(orderBean.getStore().getIsOpen());
            orderDto.setStore(storeDto);
        }

        // 複製 Comment 資訊
        // 檢查關聯是否已初始化且不為 null
        if (orderBean.getComment() != null &&
                org.hibernate.Hibernate.isInitialized(orderBean.getComment())) {
            orderDto.setComment(CommentResponseDTO
                    .fromCommentBean(orderBean.getComment()));
        }

        // 複製 OrderDetails 及其子關聯 (Food, LikedFoods)
        // 檢查集合是否已初始化且不為 null
        // isInitialized在觸發初始化之前，安全地檢查一個物件或集合是否已經初始化，從而避免 LazyInitializationException
        if (orderBean.getOrderDetails() != null && org.hibernate.Hibernate.isInitialized(orderBean.getOrderDetails())) {
            List<OrderDetailDTO> orderDetailDtos = orderBean.getOrderDetails().stream()
                    .map(OrderDetailDTO::fromEntity)
                    // .map(orderDetail -> OrderDetailDTO.fromEntity(orderDetail))
                    .collect(Collectors.toList());
            orderDto.setOrderDetails(orderDetailDtos);
        }

        return orderDto;
    }

    // ---------------------------------------------------------------
    public OrderBean toBean() {
        OrderBean orderBean = new OrderBean();
        PromotionBean promotionBean = new PromotionBean();
        promotionBean.setId(this.promotionId);
        orderBean.setId(this.id);
        orderBean.setPromotion(promotionBean); // 假設 OrderBean 有 promotionId
        orderBean.setTotal(this.total);
        orderBean.setStatus(this.status);
        orderBean.setCreateTime(
                DatetimeConvert.parse(this.createTime, "yyyy-MM-dd'T'HH:mm:ss"));
        orderBean.setContent(this.content);
        orderBean.setPickupTime(
                DatetimeConvert.parse(this.pickupTime, "yyyy-MM-dd'T'HH:mm"));

        // 轉換 User (如果存在)
        if (this.User != null) {
            orderBean.setUser(this.User.toUserBean());
        }

        // 轉換 Store (如果存在)
        if (this.store != null) {
            orderBean.setStore(this.store.toStoreBean());
        }

        // 轉換 Comment (如果存在)
        // 注意：如果 comment 是 CommentResponseDTO，可能需要一個從 Response DTO 轉換回 Entity 的方法，
        // 或者直接從 CommentResponseDTO 建立一個 CommentBean。
        // 這裡假設 CommentResponseDTO 內部有 toBean() 方法，或者你可以使用一個 CommentRequestDTO.toBean()
        if (this.comment != null) {
            // 假設 CommentResponseDTO 有一個 toCommentBean() 方法
            // orderBean.setComment(this.comment.toCommentBean());
            // 或者，如果 CommentResponseDTO 僅用於響應，你可能需要從 CommentRequestDTO 轉換過來
            // 這裡為了示例，簡單假設 CommentResponseDTO 可以直接轉換回 CommentBean
            CommentBean commentBean = new CommentBean();
            commentBean.setId(this.comment.getId());
            commentBean.setContent(this.comment.getContent());
            // ... 其他 comment 屬性
            orderBean.setComment(commentBean);
        }

        // 轉換 OrderDetails 列表
        if (this.orderDetails != null && !this.orderDetails.isEmpty()) {
            orderBean.setOrderDetails(this.orderDetails.stream()
                    .map(OrderDetailDTO::toBean) // 假設 OrderDetailDTO 也有 toBean() 方法
                    .collect(Collectors.toList()));
        }

        return orderBean;
    }

}
