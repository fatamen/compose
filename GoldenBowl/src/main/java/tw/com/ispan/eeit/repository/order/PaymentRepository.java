package tw.com.ispan.eeit.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.ispan.eeit.model.entity.order.PaymentBean;

public interface PaymentRepository extends JpaRepository<PaymentBean, Integer> {
    PaymentBean findByOrderId(Integer orderId);

    PaymentBean findByTransactionId(String transactionId);
}
