package tw.com.ispan.eeit.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tw.com.ispan.eeit.model.entity.order.PaymentBean;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentBean, Integer> {

}
