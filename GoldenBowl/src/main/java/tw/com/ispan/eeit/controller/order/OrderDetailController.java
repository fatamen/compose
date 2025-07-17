package tw.com.ispan.eeit.controller.order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.model.entity.order.OrderDetailBean;
import tw.com.ispan.eeit.service.order.OrderDetailService;

@RestController
@RequestMapping("/api/order-details") // 建議使用 /api/ 作為 RESTful API 前綴
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;

    // 創建新的訂單明細
    @PostMapping
    public ResponseEntity<OrderDetailBean> createOrderDetail(@RequestBody OrderDetailBean orderDetail) {
        OrderDetailBean createdDetail = orderDetailService.createOrderDetail(orderDetail);
        return new ResponseEntity<>(createdDetail, HttpStatus.CREATED);
    }

    // 根據 ID 獲取單一訂單明細
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailBean> getOrderDetailById(@PathVariable Integer id) {
        return orderDetailService.findOrderDetailById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 獲取所有訂單明細
    @GetMapping
    public ResponseEntity<List<OrderDetailBean>> getAllOrderDetails() {
        List<OrderDetailBean> details = orderDetailService.findAllOrderDetails();
        if (details.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(details);
    }

    // 根據訂單 ID 獲取所有訂單明細
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderDetailBean>> getOrderDetailsByOrderId(@PathVariable Integer orderId) {
        List<OrderDetailBean> details = orderDetailService.findOrderDetailsByOrderId(orderId);
        if (details.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(details);
    }

    // 更新現有訂單明細
    @PutMapping("/{id}")
    public ResponseEntity<OrderDetailBean> updateOrderDetail(@PathVariable Integer id,
            @RequestBody OrderDetailBean orderDetail) {
        return orderDetailService.updateOrderDetail(id, orderDetail)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 刪除訂單明細
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable Integer id) {
        if (orderDetailService.deleteOrderDetail(id)) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.notFound().build(); // 404 Not Found
    }
}
