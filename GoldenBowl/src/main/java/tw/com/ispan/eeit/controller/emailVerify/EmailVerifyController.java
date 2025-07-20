package tw.com.ispan.eeit.controller.emailVerify;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import tw.com.ispan.eeit.model.entity.UserBean;
import tw.com.ispan.eeit.model.entity.emailVerify.UserTokenBean;
import tw.com.ispan.eeit.repository.UserRepository;
import tw.com.ispan.eeit.repository.emailVerify.UserTokenRepository;
import tw.com.ispan.eeit.service.UserService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class EmailVerifyController {
	@Value("${app.frontend.url}")
	private String frontendUrl;
	//http://localhost:5173/
	
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserTokenRepository userTokenRepository;
    
    @Autowired
    private UserService userService;

    // 1. 寄送驗證信
    @PostMapping("/send-verify-email")
    public ResponseEntity<String> sendVerifyEmail(@RequestParam String email) {
        if (userRepository.findByEmail(email.trim()).isPresent()) {
            return ResponseEntity.badRequest().body("此 email 已被註冊，請直接登入！");
        }

        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // 先查有沒有舊 token，有就更新，沒有才新增
        Optional<UserTokenBean> exist = userTokenRepository.findByEmail(email.trim());
        if (exist.isPresent()) {
            UserTokenBean userToken = exist.get();
            userToken.setVerifyCode(token);
            userToken.setExpiration(LocalDateTime.now().plusHours(1));
            userToken.setUsed(false);
            userTokenRepository.save(userToken);
        } else {
            UserTokenBean userToken = new UserTokenBean();
            userToken.setVerifyCode(token);
            userToken.setEmail(email.trim());
            userToken.setUsed(false);
            userToken.setExpiration(LocalDateTime.now().plusHours(1));
            userTokenRepository.save(userToken);
        }

        String verifyUrl = frontendUrl+"/verify-email?token=" + token + "&email=" + email.trim();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("eattiy1986@gmail.com");
        message.setTo(email);
        message.setSubject("Email 註冊驗證信");
        message.setText("您好！請點擊以下連結完成驗證：\n" + verifyUrl);

        mailSender.send(message);

        return ResponseEntity.ok("驗證信已寄出到 " + email + "，請查收！");
    }
    
    // 2. 驗證連結 (前端收到驗證成功後再跳註冊)
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token, @RequestParam String email) {
        Optional<UserTokenBean> tokenOpt = userTokenRepository.findByVerifyCodeAndUsedFalse(token);
        if (tokenOpt.isPresent() && tokenOpt.get().getEmail().equals(email.trim())) {
            UserTokenBean userToken = tokenOpt.get();
            userToken.setUsed(true);
            userTokenRepository.save(userToken);
            
            // 同時把 user.isVerify 設為 true
            userService.verifyEmail(email.trim());
            
            return ResponseEntity.ok("驗證成功！");
        }
        return ResponseEntity.badRequest().body("驗證失敗，請檢查連結或重新發送驗證信！");
    }

    // 3. 註冊 API
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestParam String email, @RequestParam String password, @RequestParam String name) {
        System.out.println("register API called with " + email);

        boolean verified = userTokenRepository.findTopByEmailAndUsedTrueOrderByIdDesc(email.trim()).isPresent();
        System.out.println("checked verified: " + verified);
        if (!verified) {
            System.out.println("尚未驗證，return！");
            return ResponseEntity.badRequest().body("請先完成 Email 驗證再註冊！");
        }

        boolean alreadyExists = userRepository.findByEmail(email.trim()).isPresent();
        System.out.println("checked alreadyExists: " + alreadyExists);
        if (alreadyExists) {
            System.out.println("email 已註冊，return！");
            return ResponseEntity.badRequest().body("此 email 已被註冊！");
        }

        UserBean newUser = new UserBean();
        newUser.setEmail(email.trim());
        newUser.setPassword(password); // 記得加密
        newUser.setName(name);
        newUser.setIsVerify(true);
        newUser.setSignupDate(LocalDateTime.now());
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setIsActive(true); 

        System.out.println("save start!");
        userRepository.save(newUser);
        System.out.println("save done!");

        // 不需要 verifiedEmailSet 了
        return ResponseEntity.ok("註冊成功！");
    }

    // 4. 查詢 email 是否已驗證（前端可用來控制註冊按鈕啟用）
    @GetMapping("/check-email-verified")
    public ResponseEntity<Boolean> checkEmailVerified(@RequestParam String email) {
        boolean verified = userTokenRepository.findTopByEmailAndUsedTrueOrderByIdDesc(email.trim()).isPresent();
        return ResponseEntity.ok(verified);
    }
    
    // 5. 寄送重設密碼信
    @PostMapping("/send-reset-password")
    public ResponseEntity<String> sendResetPassword(@RequestParam String email) {
        // 跟上面邏輯幾乎一樣，但主旨、內容、連結不同
        Optional<UserBean> userOpt = userRepository.findByEmail(email.trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("查無此 email，請檢查是否註冊！");
        }
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Optional<UserTokenBean> exist = userTokenRepository.findByEmail(email.trim());
        if (exist.isPresent()) {
            UserTokenBean userToken = exist.get();
            userToken.setVerifyCode(token);
            userToken.setExpiration(LocalDateTime.now().plusHours(1));
            userToken.setUsed(false);
            userTokenRepository.save(userToken);
        } else {
            UserTokenBean userToken = new UserTokenBean();
            userToken.setVerifyCode(token);
            userToken.setEmail(email.trim());
            userToken.setUsed(false);
            userToken.setExpiration(LocalDateTime.now().plusHours(1));
            userTokenRepository.save(userToken);
        }
        String resetUrl = "http://localhost:5173/search?reset=1&email=" + email.trim() + "&token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("eattiy1986@gmail.com");
        message.setTo(email);
        message.setSubject("重設密碼連結");
        message.setText("您好！請點擊以下連結重設密碼：\n" + resetUrl);

        mailSender.send(message);

        return ResponseEntity.ok("重設密碼信已寄出到 " + email + "，請查收！");
    }
    
    // 重設密碼導向前端網址
    @GetMapping("/reset-password")
    public RedirectView redirectResetPassword(@RequestParam String token) {
        return new RedirectView(frontendUrl + "/search?reset=1&token=" + token);
    }
    
    //重設密碼API
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
    	
    	// 🔍 放在這裡，一進來就記錄參數內容
        System.out.println("Received token: " + token);
        System.out.println("Received new password: " + newPassword);
    	
        Optional<UserTokenBean> tokenOpt = userTokenRepository.findByVerifyCodeAndUsedFalse(token);
        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiration().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("驗證失敗，請重新操作！");
        }
        String email = tokenOpt.get().getEmail();
        Optional<UserBean> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("查無此 email！");
        }
        UserBean user = userOpt.get();
        user.setPassword(newPassword); // 記得加密
        userRepository.save(user);

        tokenOpt.get().setUsed(true);
        userTokenRepository.save(tokenOpt.get());

        return ResponseEntity.ok("密碼重設成功！");
    }
}
