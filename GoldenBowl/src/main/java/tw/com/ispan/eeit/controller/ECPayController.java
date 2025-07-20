package tw.com.ispan.eeit.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.eeit.util.CheckMacValueUtil;

@RestController
@RequestMapping("/api/ecpay")
public class ECPayController {
    @PostMapping("/createOrder")
    public String createOrder(@RequestBody Map<String, Object> payload) throws Exception {
        String merchantID = "2000132";
        String hashKey = "5294y06JbISpM5x9";
        String hashIV = "v77hoKGq4kWxNNIS";
        String actionUrl = "https://payment-stage.ecpay.com.tw/Cashier/AioCheckOut/V5";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("MerchantID", merchantID);
        params.put("MerchantTradeNo", "Test" + System.currentTimeMillis());
        params.put("MerchantTradeDate", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        params.put("PaymentType", "aio");
        params.put("TotalAmount", payload.get("amount").toString());
        params.put("TradeDesc", "測試交易");
        params.put("ItemName", payload.get("ItemName").toString());
        params.put("ReturnURL", "http://localhost:5173/ecpay"); // 綠界支付完成後，瀏覽器會跳轉回此網址
        params.put("ClientBackURL", "http://localhost:5173/search");
        params.put("ChoosePayment", "ALL");

        // 產生簽章
        String checkMacValue = CheckMacValueUtil.generate(params, hashKey, hashIV);
        params.put("CheckMacValue", checkMacValue);

        // 組出 HTML 表單
        StringBuilder sb = new StringBuilder();
        sb.append("<form id='ecpayForm' method='post' action='").append(actionUrl).append("'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append("<input type='hidden' name='").append(entry.getKey())
              .append("' value='").append(entry.getValue()).append("'/>");
        }
        sb.append("</form>");
        sb.append("<script>document.getElementById('ecpayForm').submit();</script>");

        // ⬇️ 放這裡，印出整個 HTML 表單內容
        System.out.println("回傳 HTML 表單：\n" + sb.toString());

        return sb.toString();
    }
}
