package tw.com.ispan.eeit.service.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tw.com.ispan.eeit.controller.webSocket.OrderNotificationController;
import tw.com.ispan.eeit.model.dto.order.OrderDTO;
import tw.com.ispan.eeit.model.dto.order.OrderRequestDTO;
import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.order.OrderBean;
import tw.com.ispan.eeit.model.entity.order.OrderDetailBean;
import tw.com.ispan.eeit.model.entity.promotion.PromotionBean;
import tw.com.ispan.eeit.model.entity.store.StoreBean;
import tw.com.ispan.eeit.repository.UserRepository;
import tw.com.ispan.eeit.repository.order.OrderDetailRepository;
import tw.com.ispan.eeit.repository.order.OrderRepository;
import tw.com.ispan.eeit.repository.promotion.PromotionRepository;
import tw.com.ispan.eeit.repository.store.StoreRepository;
import tw.com.ispan.eeit.util.DatetimeConvert;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private OrderNotificationController orderNotificationController; // 注入通知控制器
    // 監控有沒有新增訂單

    // 創建訂單 (包含其明細)
    public Optional<OrderBean> createOrder(OrderDTO orderDTO) {
        // 1. 將 OrderDTO 轉換為 OrderBean
        // 在創建操作中，通常我們會創建一個新的 Bean，並將 DTO 的數據填充進去。
        // DTO 中的 ID 在這裡通常是 null，因為是資料庫自動生成的。
        OrderBean newOrderBean = orderDTO.toBean();

        // 複製 DTO 中的基本屬性到 Bean
        // newOrderBean.setId(orderDTO.getId()); // 創建時 ID 通常由資料庫生成，不需從 DTO 複製

        // --- 處理 Promotion 關聯 ---
        if (orderDTO.getPromotionId() != null) {
            promotionRepository.findById(orderDTO.getPromotionId())
                    .ifPresentOrElse(
                            newOrderBean::setPromotion, // 如果找到 PromotionBean，設定給訂單
                            () -> {
                                System.out.println("Promotion with ID " + orderDTO.getPromotionId()
                                        + " not found for order creation.");
                                // 這裡可以選擇拋出異常，或者讓 Promotion 為 null，取決於業務規則
                                // throw new RuntimeException("Associated Promotion not found.");
                            });
        } else {
            newOrderBean.setPromotion(null); // 如果 DTO 中沒有提供 promotionId，設為 null
        }
        // 設定 User 關聯
        if (orderDTO.getUser() != null && orderDTO.getUser().getId() != null) {
            userRepository.findById(orderDTO.getUser().getId())
                    .ifPresentOrElse(
                            newOrderBean::setUser, // 找到 User，就設定
                            () -> {
                                // 可以記錄日誌或拋出異常，表示關聯的 User 不存在
                                System.out.println("User with ID " + orderDTO.getUser().getId()
                                        + " not found for order creation.");
                                // throw new RuntimeException("Associated User not found."); // 拋出異常，回滾事務
                            });
        }
        // 如果 DTO 中沒有提供 User 資訊，newOrderBean.setUser() 會保持為 null

        // 設定 Store 關聯
        if (orderDTO.getStore() != null && orderDTO.getStore().getId() != null) {
            storeRepository.findById(orderDTO.getStore().getId())
                    .ifPresentOrElse(
                            newOrderBean::setStore, // 找到 Store，就設定
                            () -> {
                                System.out.println("Store with ID " + orderDTO.getStore().getId()
                                        + " not found for order creation.");
                                // throw new RuntimeException("Associated Store not found.");
                            });
        }
        // 2. 保存 OrderBean 到資料庫，以獲取生成的 ID
        OrderBean savedOrder = orderRepository.save(newOrderBean);

        // 3. 處理 OrderDetails 列表
        if (orderDTO.getOrderDetails() != null && !orderDTO.getOrderDetails().isEmpty()) {
            List<OrderDetailBean> orderDetails = orderDTO.getOrderDetails().stream()
                    .map(detailDTO -> {
                        OrderDetailBean detailBean = detailDTO.toBean(); // 將 OrderDetailDTO 轉換為 OrderDetailBean
                        detailBean.setOrder(savedOrder); // !!! 重要：設定 OrderDetailBean 指向新保存的 OrderBean
                        return detailBean;
                    })
                    .collect(Collectors.toList());
            // 批量保存訂單明細
            orderDetailRepository.saveAll(orderDetails);
            // 重新載入訂單以確保 orderDetails 列表被正確填充
            // 或者，如果你的 JPA 配置了 OneToMany 的 FetchType.LAZY 且你希望立即返回明細，
            // 且之前的 saveAll 並沒有自動更新 savedOrder 的關聯集合，
            // 你可以重新查詢或手動添加到 savedOrder 的集合中。
            // 這裡直接將保存的明細列表設定回 savedOrder，前提是關係被正確管理
            savedOrder.setOrderDetails(orderDetails);

            // 也可以選擇重新從資料庫載入
            // savedOrder = orderRepository.findById(savedOrder.getId()).orElse(savedOrder);
        }
        // 檢查日誌 1: 確認是否進入通知發送邏輯
        if (savedOrder.getStore() != null && savedOrder.getStore().getId() != null && savedOrder.getId() != null) {
            System.out.println("準備發送 WebSocket 通知給 StoreID: " + savedOrder.getStore().getId() + ", OrderID: "
                    + savedOrder.getId());
            orderNotificationController.notifyNewOrder(savedOrder.getStore().getId(), savedOrder.getId().toString());
            System.out.println("WebSocket 通知已嘗試發送。");
        } else {
            System.err.println("警告：創建訂單後，無法獲取有效的商店ID或訂單ID，未發送WebSocket通知。");
        }

        // 4. 返回保存後的 OrderBean，包裝在 Optional 中
        return Optional.of(savedOrder);
    }

    // 根據 ID 查找訂單
    public Optional<OrderDTO> findOrderById(Integer id) {
        Optional<OrderBean> orderOptional = orderRepository.findById(id);
        return orderOptional.map(orderBean -> {
            // 在這裡，OrderBean 還在事務中
            orderBean.getOrderDetails().size(); // 觸發惰性載入

            // 然後再從 OrderBean 轉換為 OrderDTO
            return OrderDTO.fromEntity(orderBean);
        });
    }

    // 查找所有訂單
    public List<OrderBean> findAllOrders() {
        return orderRepository.findAll();
    }

    // 更新訂單
    public Optional<OrderBean> updateOrder(Integer id, OrderDTO orderDTO) {
        Optional<OrderBean> existingOrderOptional = orderRepository.findById(id);

        if (existingOrderOptional.isEmpty()) {
            return Optional.empty(); // 找不到訂單，返回空 Optional
        }

        OrderBean existingOrderBean = existingOrderOptional.get();
        // 找尋promotion
        // if (orderDTO.getPromotionId() != null) {
        // // 從資料庫中查找 PromotionBean
        // promotionRepository.findById(orderDTO.getPromotionId())
        // .ifPresentOrElse(
        // existingOrderBean::setPromotion,
        // () -> {
        // // 如果找不到對應的 Promotion，這裡可以選擇：
        // // 1. 拋出一個業務異常，因為設置了一個不存在的 Promotion ID
        // // throw new YourBusinessException("Promotion with ID " +
        // orderDTO.getPromotionId() + " not found.");
        // // 2. 記錄日誌，並將 promotion 設為 null
        // System.out.println("Promotion with ID " + orderDTO.getPromotionId() + " not
        // found for order update. Setting promotion to null.");
        // existingOrderBean.setPromotion(null);
        // }
        // );
        // } else {
        // // 如果 DTO 中沒有提供 Promotion ID，則將 promotion 設為 null
        // existingOrderBean.setPromotion(null);
        // }
        // 複製 OrderDTO 的資料到 existingOrderBean
        existingOrderBean.setTotal(orderDTO.getTotal());
        existingOrderBean.setStatus(orderDTO.getStatus());
        existingOrderBean.setContent(orderDTO.getContent());
        existingOrderBean.setPickupTime(
                DatetimeConvert.parse(orderDTO.getPickupTime(), "yyyy-MM-dd'T'HH:mm"));

        // 更新 User 關聯
        // if (orderDTO.getUser() != null && orderDTO.getUser().getId() != null) {
        // userRepository.findById(orderDTO.getUser().getId())
        // .ifPresentOrElse(
        // existingOrderBean::setUser,
        // () -> System.out.println("User with ID " + orderDTO.getUser().getId() + " not
        // found for order update.") // 或使用 logger
        // );
        // } else {
        // existingOrderBean.setUser(null);
        // }

        // 更新 Store 關聯
        // if (orderDTO.getStore() != null && orderDTO.getStore().getId() != null) {
        // storeRepository.findById(orderDTO.getStore().getId())
        // .ifPresentOrElse(
        // existingOrderBean::setStore,
        // () -> System.out.println("Store with ID " + orderDTO.getStore().getId() + "
        // not found for order update.") // 或使用 logger
        // );
        // } else {
        // existingOrderBean.setStore(null);
        // }

        // 更新 Comment 關聯
        // if (orderDTO.getComment() != null && orderDTO.getComment().getId() != null) {
        // CommentBean commentBean = new CommentBean(); // 這裡僅設定 ID，若需更新內容需從資料庫查詢
        // commentBean.setId(orderDTO.getComment().getId());
        // existingOrderBean.setComment(commentBean);
        // } else {
        // existingOrderBean.setComment(null);
        // }
        // 處理 OrderDetails 列表的更新
        // if (existingOrderBean.getOrderDetails() != null) {
        // existingOrderBean.getOrderDetails().clear(); // 清除舊的明細
        // }
        // if (orderDTO.getOrderDetails() != null &&
        // !orderDTO.getOrderDetails().isEmpty()) {
        // List<OrderDetailBean> newOrderDetails = orderDTO.getOrderDetails().stream()
        // .map(detailDTO -> {
        // OrderDetailBean detailBean = detailDTO.toBean();
        // detailBean.setOrder(existingOrderBean); // 設定父訂單關聯
        // return detailBean;
        // })
        // .collect(Collectors.toList());
        //
        // if (existingOrderBean.getOrderDetails() == null) {
        // existingOrderBean.setOrderDetails(newOrderDetails);
        // } else {
        // existingOrderBean.getOrderDetails().addAll(newOrderDetails);
        // }
        // }
        // 檢查日誌 1: 確認是否進入通知發送邏輯
        if (existingOrderBean.getStore() != null && existingOrderBean.getStore().getId() != null
                && existingOrderBean.getId() != null) {
            System.out.println("準備發送 WebSocket 通知給 StoreID: " + existingOrderBean.getStore().getId() + ", OrderID: "
                    + existingOrderBean.getId());
            orderNotificationController.notifyOrderStatusUpdate(
                    existingOrderBean.getStore().getId(),
                    existingOrderBean.getId().toString(),
                    existingOrderBean.getStatus());
            System.out.println("WebSocket 通知已嘗試發送。");
        } else {
            System.err.println("警告:更新訂單後，無法獲取有效的商店ID或訂單ID，未發送WebSocket通知。");
        }
        // 保存更新後的 OrderBean
        return Optional.of(orderRepository.save(existingOrderBean));
    }

    // 更新訂單狀態
    public Optional<OrderBean> updateOrderStatus(Integer id, OrderDTO orderDTO) {
        Optional<OrderBean> existingOrderOptional = orderRepository.findById(id);

        if (existingOrderOptional.isEmpty()) {
            return Optional.empty(); // 找不到訂單，返回空 Optional
        }
        OrderBean existingOrderBean = existingOrderOptional.get();
        existingOrderBean.setStatus(orderDTO.getStatus());

        // 檢查日誌 1: 確認是否進入通知發送邏輯
        if (existingOrderBean.getStore() != null && existingOrderBean.getStore().getId() != null
                && existingOrderBean.getId() != null) {
            System.out.println("準備發送 WebSocket 通知給 StoreID: " + existingOrderBean.getStore().getId() + ", OrderID: "
                    + existingOrderBean.getId());
            orderNotificationController.notifyOrderStatusUpdate(
                    existingOrderBean.getStore().getId(),
                    existingOrderBean.getId().toString(),
                    existingOrderBean.getStatus());
            System.out.println("WebSocket 通知已嘗試發送。");
        } else {
            System.err.println("警告:更新訂單後，無法獲取有效的商店ID或訂單ID，未發送WebSocket通知。");
        }
        // 保存更新後的 OrderBean
        return Optional.of(orderRepository.save(existingOrderBean));
    }

    // 刪除訂單
    public boolean deleteOrder(Integer id) {
        if (orderRepository.existsById(id)) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 根據用戶 ID 查找訂單
    // 這個方法現在可以正常工作，因為 OrderRepository 中已添加 findByUser_Id
    public List<OrderDTO> findOrdersByUserId(Integer userId) {
        if (userId != null) {
            // orderRepository.findByUser_Id(userId);
            List<OrderBean> orderBeans = orderRepository.findByUser_Id(userId);
            return orderBeans.stream()
                    .map(OrderDTO::fromEntity)
                    .collect(Collectors.toList());
            // .map(orderBean -> OrderDto.fromEntity(orderBean))
            // 最終，map 操作會產生一個包含 orderDto1、orderDto2、orderDto3 的新 Stream。
            // 最後的 .collect(Collectors.toList()) 則會將這個 Stream 中的所有 OrderDto 物件收集起來，
            // 形成一個 List<OrderDto>。
        }
        return null;
    }

    // 根據用戶 ID 查找已經完成的訂單
    // 這個方法現在可以正常工作，因為 OrderRepository 中已添加 findByUser_Id
    public List<OrderDTO> findHistoryByUserId(Integer userId) {
        if (userId != null) {
            // orderRepository.findByUser_Id(userId);
            List<OrderBean> orderBeans = orderRepository.findHistoryByUserId(userId);
            return orderBeans.stream()
                    .map(OrderDTO::fromEntity)
                    .collect(Collectors.toList());
            // .map(orderBean -> OrderDto.fromEntity(orderBean))
            // 最終，map 操作會產生一個包含 orderDto1、orderDto2、orderDto3 的新 Stream。
            // 最後的 .collect(Collectors.toList()) 則會將這個 Stream 中的所有 OrderDto 物件收集起來，
            // 形成一個 List<OrderDto>。
        }
        return null;
    }

    // 根據用戶 ID 查找訂單
    // 這個方法現在可以正常工作，因為 OrderRepository 中已添加 findByUser_Id
    public List<OrderDTO> findUncompleteByUserId(Integer userId) {
        if (userId != null) {
            List<OrderBean> orderBeans = orderRepository.findUncompleteByUserId(userId);
            return orderBeans.stream()
                    .map(OrderDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        return null;
    }

    public List<OrderDTO> findOrderByStoreId(Integer storeId) {
        if (storeId != null) {
            List<OrderBean> orderBeans = orderRepository.findByStore_Id(storeId);
            return orderBeans.stream()
                    .map(OrderDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        return null;
    }

    // yifan新增
    // 查詢某個使用者使用某張優惠券的次數（僅統計已完成訂單）
    public int countUserPromotionUsage(Integer userId, Integer promotionId) {
        if (userId != null && promotionId != null) {
            return orderRepository.countUsageByUserAndPromotion(userId, promotionId);
        }
        return 0;
    }

    // 查詢某張優惠券總共被使用過幾次（僅統計已完成訂單）
    public int countPromotionUsage(Integer promotionId) {
        if (promotionId != null) {
            return orderRepository.countUsageByPromotion(promotionId);
        }
        return 0;
    }

    public OrderDTO createOrderFromRequest(OrderRequestDTO dto) {
        System.out.println("已選 promotionId: " + dto.getPromotionId());
        System.out.println("已選 storeId: " + dto.getStoreId());

        OrderBean order = new OrderBean();

        // 從資料庫查詢 user、store、promotion 實體
        UserBean user = new UserBean();
        user.setId(dto.getUserId());
        order.setUser(user);

        // 苡帆改掉
        // StoreBean store = storeRepository.findById(dto.getStoreId())
        // .orElseThrow(() -> new RuntimeException("找不到店家"));
        // order.setStore(store);

        // 改成這個：
        StoreBean store = storeRepository.getReferenceById(dto.getStoreId());
        order.setStore(store);

        if (dto.getPromotionId() != null) {
            PromotionBean promo = new PromotionBean();
            promo.setId(dto.getPromotionId());
            order.setPromotion(promo);
        }

        order.setTotal(dto.getTotal());
        order.setStatus(dto.getStatus());
        order.setCreateTime(LocalDateTime.now());

        OrderBean savedOrder = orderRepository.save(order);
        return OrderDTO.fromEntity(savedOrder); // 回傳給前端
    }

}