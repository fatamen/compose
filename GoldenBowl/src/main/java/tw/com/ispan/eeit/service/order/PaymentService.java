package tw.com.ispan.eeit.service.order;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.ispan.eeit.repository.order.PaymentRepository;

@Service
@Transactional
public class PaymentService {
	@Autowired
	PaymentRepository paymentRepository;
}
