package tw.com.ispan.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tw.com.ispan.domain.CustomerBean;
import tw.com.ispan.jwt.JsonWebTokenUtility;
import tw.com.ispan.service.CustomerService;
import tw.com.ispan.util.DatetimeConverter;

@RestController
@RequestMapping("/ajax/secure/login")
public class LoginAjaxController {
    @Autowired
    private CustomerService customerService;

	@Autowired
	private JsonWebTokenUtility jsonWebTokenUtility;

    @PostMapping("/exists")
    public String exists(@RequestBody String body) {
        JSONObject obj = new JSONObject(body);
        String username = obj.isNull("username") ? null : obj.getString("username");

        if(username != null && username.length()!=0) {
            if(customerService.exists(username)) {
                return "帳號存在";
            }
        }
        return "帳號不存在";
    }

    @PostMapping
    public String login(@RequestBody String body) {
        JSONObject responseJson = new JSONObject();

//接收資料
        JSONObject obj = new JSONObject(body);
		String username = obj.isNull("username") ? null : obj.getString("username");
        String password = obj.isNull("password") ? null : obj.getString("password");

//轉換資料
//驗證資料
        if(username == null || username.length()==0 || password == null || password.length()==0 ) {
            responseJson.put("success", false);
            responseJson.put("message", "請輸入帳號與密碼以便執行登入");
            return responseJson.toString();
        }

//呼叫企業邏輯
        CustomerBean bean = customerService.login(username, password);
        if(bean == null) {
            responseJson.put("success", false);
            responseJson.put("message", "帳號或密碼錯誤");
        } else {
            responseJson.put("success", true);
            responseJson.put("message", "登入成功");
            //JWT begin
            String birth = DatetimeConverter.toString(bean.getBirth(), "yyyy-MM-dd");
            JSONObject data = new JSONObject()
            		.put("custid", bean.getCustid())
            		.put("email", bean.getEmail())
            		.put("birth", birth);
            
            String token = jsonWebTokenUtility.createToken(data.toString());
            responseJson.put("token", token);
            responseJson.put("user", bean.getEmail());
            //JWT end
        }
        return responseJson.toString();
    }
}
