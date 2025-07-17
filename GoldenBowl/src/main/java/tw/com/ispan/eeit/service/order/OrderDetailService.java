package tw.com.ispan.eeit.service.order;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import tw.com.ispan.eeit.model.entity.order.OrderDetailBean;
import tw.com.ispan.eeit.repository.order.OrderDetailRepository;

@Service
@Transactional
public class OrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // 創建訂單明細
    public OrderDetailBean createOrderDetail(OrderDetailBean orderDetail) {
        return orderDetailRepository.save(orderDetail);
    }

    // 根據 ID 查找訂單明細
    public Optional<OrderDetailBean> findOrderDetailById(Integer id) {
        return orderDetailRepository.findById(id);
    }

    // 查找所有訂單明細
    public List<OrderDetailBean> findAllOrderDetails() {
        return orderDetailRepository.findAll();
    }

    // 根據訂單 ID 查找訂單明細列表
    // 這個方法現在可以正常工作，因為 OrderDetailRepository 中已添加 findByOrder_Id
    public List<OrderDetailBean> findOrderDetailsByOrderId(Integer orderId) {
        return orderDetailRepository.findByOrder_Id(orderId);
    }

    // 更新訂單明細
    public Optional<OrderDetailBean> updateOrderDetail(Integer id, OrderDetailBean updatedOrderDetail) {
        return orderDetailRepository.findById(id).map(detail -> {
            // detail.setOrder(updatedOrderDetail.getOrder()); // 通常不建議直接更新關聯
            detail.setFood(updatedOrderDetail.getFood());
            detail.setQuantity(updatedOrderDetail.getQuantity());
            detail.setPrice(updatedOrderDetail.getPrice());
            detail.setSubTotal(updatedOrderDetail.getSubTotal());
            detail.setTotal(updatedOrderDetail.getTotal());
            return orderDetailRepository.save(detail);
        });
    }

    // 刪除訂單明細
    public boolean deleteOrderDetail(Integer id) {
        if (orderDetailRepository.existsById(id)) {
            orderDetailRepository.deleteById(id);
            return true;
        }
        return false;
    }
}