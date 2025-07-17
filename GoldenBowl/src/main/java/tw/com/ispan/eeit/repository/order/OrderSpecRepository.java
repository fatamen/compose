package tw.com.ispan.eeit.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;

import tw.com.ispan.eeit.model.entity.order.OrderSpecBean;

public interface OrderSpecRepository extends JpaRepository<OrderSpecBean, Integer> {

}
