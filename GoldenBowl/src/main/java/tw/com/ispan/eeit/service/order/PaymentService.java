package tw.com.ispan.eeit.service.order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import tw.com.ispan.eeit.model.entity.order.OrderBean;
import tw.com.ispan.eeit.model.entity.order.PaymentBean;
import tw.com.ispan.eeit.repository.order.OrderRepository;
import tw.com.ispan.eeit.repository.order.PaymentRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

	 @Autowired
	 private PaymentRepository paymentRepository;

	  @Autowired
	  private OrderRepository orderRepository;

    // 建立付款記錄
    public PaymentBean createPaymentRecord(Integer orderId, Integer amount, String description) {
        PaymentBean payment = new PaymentBean();
        OrderBean order = new OrderBean();
        order.setId(orderId);
        payment.setOrder(order);
        payment.setTotal(amount);
        payment.setMethod("ECPay");
        payment.setIsPaid(false);
        payment.setTransactionId(generateTradeNo(orderId));
        Date now = new java.util.Date();
        payment.setPaidTime(now);
        // 你也可以把 description 存入備註欄位

        return paymentRepository.save(payment);
    }

    // 處理綠界回傳並更新 DB
    public void processEcpayCallback(String merchantTradeNo, Map<String, String> callbackData) {
    	PaymentBean payment = paymentRepository.findByTransactionId(merchantTradeNo);


        if (payment != null) {
            payment.setIsPaid(true);
            payment.setTransactionId(callbackData.get("TradeNo")); // 寫入金流回傳交易編號

            // 直接抓伺服器當下時間
            Date now = new java.util.Date();
            System.out.println("寫入 paidTime: " + now);
            payment.setPaidTime(now);
            paymentRepository.save(payment);
            
            // 可加入 rtnMsg、paymentType 等欄位，若 PaymentBean 有擴充
            paymentRepository.save(payment);
        } else {
            System.err.println("找不到對應的付款記錄，MerchantTradeNo: " + merchantTradeNo);
            // 這裡可以加入錯誤處理，例如記錄日誌或發送警報
        }
    }

    // 建立唯一交易編號
    private String generateTradeNo(Integer orderId) {
        return "ORDER" + orderId + "_" + System.currentTimeMillis();
    }

    // 時間轉換工具
    private Date parseDate(String dateStr) {
        try {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}